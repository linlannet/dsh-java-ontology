/* DSH Ontology Admin 0.2.2 — 7 视图：#/login #/overview #/entities #/search #/i18n #/promote #/ai */
(function () {
  'use strict';

  const API = {
    model: '/api/model',
    auth: '/api/auth',
    ontologies: '/api/ontologies',
    siis: (localStorage.getItem('dsh_siis_base') || 'http://127.0.0.1:8101') + '/api/ontology'
  };
  const PROMOTABLE = ['country', 'organization', 'person', 'hotspot', 'media', 'book', 'paper', 'term', 'location'];
  const viewEl = document.getElementById('view');
  const statusEl = document.getElementById('status-bar');
  const sidebar = document.getElementById('sidebar');
  const userEl = document.getElementById('current-user');

  function token() { return localStorage.getItem('dsh_token'); }
  function userName() { return localStorage.getItem('dsh_user') || '-'; }
  function esc(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }
  function snake(name) {
    return String(name).replace(/[A-Z]/g, ch => '_' + ch.toLowerCase());
  }
  function camel(name) {
    return String(name).replace(/_([a-z])/g, (_, ch) => ch.toUpperCase());
  }
  function aliasOf(category) {
    if (!category) return '';
    return 'Ontology' + category.charAt(0).toUpperCase() + category.slice(1);
  }
  function status(message, type) {
    statusEl.className = 'status-bar ' + (type || 'info');
    statusEl.textContent = message || '';
    if (message && type === 'success') setTimeout(() => { if (statusEl.textContent === message) statusEl.textContent = ''; }, 4000);
  }
  function clearStatus() { statusEl.textContent = ''; statusEl.className = 'status-bar'; }

  async function request(url, options) {
    const opts = Object.assign({ method: 'GET' }, options || {});
    opts.headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
    if (token() && !opts.headers.Authorization) opts.headers.Authorization = 'Bearer ' + token();
    let response;
    try {
      response = await fetch(url, opts);
    } catch (e) {
      throw new Error('无法连接服务，请确认平台 8100 / 业务服务 8101 已启动');
    }
    if (response.status === 401) {
      logout();
      throw new Error('登录已过期，请重新登录');
    }
    const text = await response.text();
    let body = null;
    try { body = text ? JSON.parse(text) : null; } catch (e) { body = null; }
    if (!response.ok) {
      const message = (body && (body.msg || body.message)) || ('请求失败 HTTP ' + response.status);
      const error = new Error(message);
      error.status = response.status;
      error.body = body;
      throw error;
    }
    return body;
  }
  const get = (url) => request(url);
  const post = (url, data) => request(url, { method: 'POST', body: JSON.stringify(data || {}) });
  const put = (url, data) => request(url, { method: 'PUT', body: JSON.stringify(data || {}) });
  const del = (url) => request(url, { method: 'DELETE' });

  function unwrap(body) { return body && 'data' in body ? body.data : body; }

  function errorLine(error) {
    if (error.status === 400 && error.body && error.body.data && Array.isArray(error.body.data)) {
      return error.body.data.map(v => v.field + '(' + v.rule + '): ' + (v.message || '')).join('；');
    }
    return error.message;
  }

  function guard(fn) {
    return async function () {
      clearStatus();
      try {
        await fn.apply(null, arguments);
      } catch (error) {
        status(errorLine(error), 'error');
      }
    };
  }

  function logout() {
    localStorage.removeItem('dsh_token');
    localStorage.removeItem('dsh_user');
    if (location.hash !== '#/login') location.hash = '#/login';
  }

  document.getElementById('btn-logout').addEventListener('click', logout);

  function showSidebar(visible) {
    sidebar.hidden = !visible;
    userEl.textContent = userName();
  }

  function markActive(route) {
    document.querySelectorAll('.sidebar nav a').forEach(link => {
      link.classList.toggle('active', link.getAttribute('data-route') === route);
    });
  }

  /* ---------------- 登录 ---------------- */
  function renderLogin() {
    showSidebar(false);
    viewEl.innerHTML = `
      <div class="panel" style="max-width:420px;margin:8vh auto 0">
        <h1>DSH 本体管理台</h1>
        <p>请使用平台账号登录（主任 / 研究员 / 访客）。</p>
        <form id="login-form">
          <label><span>用户名</span><input id="login-user" required autocomplete="username" value="researcher"></label>
          <label><span>密码</span><input id="login-pass" type="password" required autocomplete="current-password"></label>
          <button type="submit">登录</button>
        </form>
      </div>`;
    document.getElementById('login-form').addEventListener('submit', guard(async (event) => {
      event.preventDefault();
      const userName = document.getElementById('login-user').value.trim();
      const password = document.getElementById('login-pass').value;
      const body = await post(API.auth + '/login', { userName, password });
      const data = unwrap(body);
      if (!data || !data.token) throw new Error((body && body.msg) || '登录失败：用户名或密码错误');
      localStorage.setItem('dsh_token', data.token);
      localStorage.setItem('dsh_user', data.userName || userName);
      status('登录成功', 'success');
      location.hash = '#/overview';
    }));
  }

  /* ---------------- 总览 ---------------- */
  async function renderOverview() {
    showSidebar(true); markActive('overview');
    viewEl.innerHTML = '<h1>总览</h1><div class="panel"><p>加载模型与完整性...</p></div>';
    const [groupsBody, integrityBody, manifestBody] = await Promise.all([
      get(API.model + '/aggregates'),
      get(API.model + '/integrity'),
      get(API.model + '/manifest')
    ]);
    const groups = unwrap(groupsBody) || [];
    const integrity = unwrap(integrityBody) || {};
    const manifest = unwrap(manifestBody) || {};
    const tree = groups.map(group => `
      <div class="tree-group">
        <h3>${esc(group.tag)} <span class="badge">${group.aggregateCount}</span></h3>
        <div class="tree-items">
          ${(group.aggregates || []).map(item => `
            <button class="tree-item" type="button" data-category="${esc(item.category || '')}">
              ${esc(item.alias)}${item.category ? ' · ' + esc(item.category) : ''}
              <span class="badge">${item.attributeCount}</span>
            </button>`).join('')}
        </div>
      </div>`).join('');
    viewEl.innerHTML = `
      <h1>总览</h1>
      <div class="panel">
        <div class="grid">
          <div class="stat"><div class="label">聚合根</div><div class="value">${integrity.aggregateCount ?? '-'}</div></div>
          <div class="stat"><div class="label">字段</div><div class="value">${integrity.attributeCount ?? '-'}</div></div>
          <div class="stat"><div class="label">枚举</div><div class="value">${integrity.enumCount ?? '-'}</div></div>
          <div class="stat"><div class="label">重复 alias</div><div class="value">${integrity.duplicateAliases ?? 0}</div></div>
          <div class="stat"><div class="label">悬空引用</div><div class="value">${integrity.danglingRefs ?? 0}</div></div>
          <div class="stat"><div class="label">模型层</div><div class="value" style="font-size:15px">${manifest.projectLayerPresent ? '基座 + 项目' : '基座'}</div></div>
        </div>
        <p style="margin-top:12px">
          完整性：
          <span class="badge ${integrity.hasErrors ? 'danger' : 'ok'}">${integrity.hasErrors ? '存在 ERROR' : '全部通过'}</span>
          <span class="badge">基座 ${esc(manifest.baseVersion || '-')}</span>
          <span class="badge">项目 ${esc(manifest.projectVersion || '-')}</span>
        </p>
      </div>
      <div class="panel"><h2>模型目录</h2><div class="tree">${tree || '<div class="empty">暂无聚合根</div>'}</div></div>`;
    viewEl.querySelectorAll('.tree-item').forEach(button => {
      button.addEventListener('click', () => {
        const category = button.getAttribute('data-category');
        if (category) location.hash = '#/entities/' + encodeURIComponent(category);
      });
    });
  }

  /* ---------------- 实体维护 ---------------- */
  async function renderEntities(category) {
    showSidebar(true); markActive('entities');
    const categoriesBody = await get(API.siis + '/categories');
    const categories = (unwrap(categoriesBody) || []).slice().sort();
    if (!category) category = categories[0];
    if (!category) { viewEl.innerHTML = '<h1>实体维护</h1><div class="empty">无可维护的分类</div>'; return; }

    let schema = null;
    try {
      const schemaBody = await get(API.model + '/aggregates/' + aliasOf(category));
      schema = unwrap(schemaBody);
    } catch (e) { schema = null; }

    const page = unwrap(await get(API.siis + '/' + encodeURIComponent(category) + '/page?pageNum=1&pageSize=30'));
    const rows = (page && (page.records || page.list)) || [];
    const attributes = (schema && schema.attributes) || [];
    // 列表列以接口返回的行数据为准（EAV 行键为 snake_case），无数据时回退模型属性
    const columns = rows[0] ? Object.keys(rows[0]) : attributes.map(a => a.name);

    viewEl.innerHTML = `
      <h1>实体维护 <span class="badge">${esc(category)}</span></h1>
      <div class="panel">
        <div class="row" style="justify-content:space-between">
          <label style="min-width:220px;margin:0"><span>分类</span>
            <select id="entity-category">${categories.map(c => `<option ${c === category ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          </label>
          <div class="row">
            <button id="btn-new" type="button">新增</button>
            <button id="btn-reload" type="button" class="secondary">刷新</button>
          </div>
        </div>
        <form id="entity-form" class="grid-form" hidden></form>
        <div id="entity-table"></div>
      </div>`;
    document.getElementById('entity-category').addEventListener('change', e => {
      location.hash = '#/entities/' + encodeURIComponent(e.target.value);
    });
    document.getElementById('btn-reload').addEventListener('click', () => renderEntities(category));
    document.getElementById('btn-new').addEventListener('click', () => openForm(null));

    function renderTable() {
      const container = document.getElementById('entity-table');
      if (!rows.length) { container.innerHTML = '<div class="empty">暂无记录</div>'; return; }
      const cols = columns.length ? columns : ['id'];
      // 模型属性名为 camelCase（如 isoCode），接口行数据为 snake_case（如 iso_code），需双向兼容
      const cellOf = (row, col) => {
        if (row[col] != null) return row[col];
        const snakeKey = snake(col);
        return row[snakeKey] != null ? row[snakeKey] : '';
      };
      container.innerHTML = `<table><thead><tr>${cols.map(c => `<th>${esc(c)}</th>`).join('')}<th>操作</th></tr></thead>
        <tbody>${rows.map(row => `<tr>${cols.map(c => `<td>${esc(cellOf(row, c))}</td>`).join('')}
          <td><button type="button" class="secondary" data-edit="${row.id}">编辑</button>
          <button type="button" class="danger" data-del="${row.id}">删除</button></td></tr>`).join('')}</tbody></table>`;
      container.querySelectorAll('[data-edit]').forEach(btn => btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-edit');
        openForm(rows.find(row => String(row.id) === id));
      }));
      container.querySelectorAll('[data-del]').forEach(btn => btn.addEventListener('click', guard(async () => {
        if (!confirm('确认删除该记录？')) return;
        await del(API.siis + '/' + encodeURIComponent(category) + '/' + btn.getAttribute('data-del'));
        status('删除成功', 'success');
        await renderEntities(category);
      })));
    }

    function openForm(row) {
      const form = document.getElementById('entity-form');
      form.hidden = false;
      if (!attributes.length) {
        form.innerHTML = `<label style="grid-column:1/-1"><span>JSON（该分类无模型定义）</span>
          <textarea id="entity-json">${esc(row ? JSON.stringify(row, null, 2) : '{}')}</textarea></label>
          <div class="row" style="grid-column:1/-1"><button type="submit">保存</button>
          <button type="button" class="secondary" id="btn-cancel">取消</button></div>`;
      } else {
        form.innerHTML = attributes.map(attr => {
          const key = snake(attr.name);
          const value = row ? (row[key] ?? row[attr.name] ?? '') : '';
          const label = attr.label || attr.name;
          if (attr.enumValues && attr.enumValues.length) {
            return `<label class="${attr.required ? 'required' : ''}"><span>${esc(label)}（${esc(attr.name)}）</span>
              <select name="${esc(key)}"><option value="">-</option>${attr.enumValues.map(v =>
                `<option ${String(v) === String(value) ? 'selected' : ''}>${esc(v)}</option>`).join('')}</select></label>`;
          }
          return `<label class="${attr.required ? 'required' : ''}"><span>${esc(label)}（${esc(attr.name)}）</span>
            <input name="${esc(key)}" value="${esc(value)}" ${attr.name === 'id' ? 'readonly' : ''}></label>`;
        }).join('') + `<div class="row" style="grid-column:1/-1">
          <button type="submit">保存</button><button type="button" class="secondary" id="btn-cancel">取消</button></div>`;
      }
      form.onsubmit = guard(async (event) => {
        event.preventDefault();
        let data;
        if (!attributes.length) {
          data = JSON.parse(document.getElementById('entity-json').value || '{}');
        } else {
          data = {};
          new FormData(form).forEach((value, key) => { if (value !== '') data[key] = value; });
          if (row && row.id != null) data.id = row.id;
        }
        if (row) await put(API.siis + '/' + encodeURIComponent(category) + '/' + row.id, data);
        else await post(API.siis + '/' + encodeURIComponent(category), data);
        status('保存成功', 'success');
        await renderEntities(category);
      });
      document.getElementById('btn-cancel').addEventListener('click', () => { form.hidden = true; });
    }

    renderTable();
  }

  /* ---------------- 搜索 ---------------- */
  async function renderSearch() {
    showSidebar(true); markActive('search');
    viewEl.innerHTML = `
      <h1>搜索</h1>
      <div class="panel">
        <form id="search-form" class="row">
          <label style="flex:1;min-width:260px;margin:0"><span>关键词</span><input id="search-keyword" required></label>
          <label style="min-width:160px;margin:0"><span>分类（可选）</span><input id="search-category" placeholder="country"></label>
          <button type="submit">搜索实体</button>
        </form>
        <p>同时检查多语种翻译表，便于发现缺失的英文/法文/阿文名称。</p>
      </div>
      <div class="panel"><h2>实体结果</h2><div id="search-result"><div class="empty">输入关键词开始搜索</div></div></div>
      <div class="panel"><h2>翻译结果</h2><div id="i18n-result"><div class="empty">输入关键词开始搜索</div></div></div>`;
    document.getElementById('search-form').addEventListener('submit', guard(async (event) => {
      event.preventDefault();
      const keyword = document.getElementById('search-keyword').value.trim();
      const category = document.getElementById('search-category').value.trim();
      if (!keyword) return;
      const list = unwrap(await get(API.siis + '/search?keyword=' + encodeURIComponent(keyword)
        + (category ? '&category=' + encodeURIComponent(category) : ''))) || [];
      document.getElementById('search-result').innerHTML = list.length
        ? `<table><thead><tr><th>分类</th><th>记录</th></tr></thead><tbody>${list.map(item =>
          `<tr><td>${esc(item._entity_type || item.category || category || '-')}</td>
           <td class="pre" style="max-height:120px">${esc(JSON.stringify(item))}</td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">无匹配实体</div>';
      const translations = unwrap(await get(API.siis + '/i18n/search?keyword=' + encodeURIComponent(keyword))) || [];
      document.getElementById('i18n-result').innerHTML = translations.length
        ? `<table><thead><tr><th>实体</th><th>语种</th><th>名称</th></tr></thead><tbody>${translations.map(item =>
          `<tr><td>${esc(item.entity_id)}</td><td>${esc(item.lang)}</td><td>${esc(item.name)}</td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">无匹配翻译</div>';
    }));
  }

  /* ---------------- 多语种 ---------------- */
  async function renderI18n(category, entityId) {
    showSidebar(true); markActive('i18n');
    const categoriesBody = await get(API.siis + '/categories');
    const categories = (unwrap(categoriesBody) || []).slice().sort();
    category = category || categories[0] || 'country';
    viewEl.innerHTML = `
      <h1>多语种维护</h1>
      <div class="panel">
        <form id="i18n-form" class="row">
          <label style="min-width:180px;margin:0"><span>分类</span>
            <select id="i18n-category">${categories.map(c =>
              `<option ${c === category ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select></label>
          <label style="min-width:140px;margin:0"><span>实体 ID</span>
            <input id="i18n-entity" type="number" min="1" value="${esc(entityId || '')}" required></label>
          <button type="submit">加载翻译</button>
        </form>
        <form id="i18n-edit" class="grid-form" hidden>
          <label><span>语种</span>
            <select name="lang"><option>en</option><option>fr</option><option>ar</option><option>zh</option></select></label>
          <label><span>名称</span><input name="name" required></label>
          <label style="grid-column:1/-1"><span>描述</span><textarea name="description"></textarea></label>
          <div class="row" style="grid-column:1/-1"><button type="submit">保存翻译</button></div>
        </form>
        <div id="i18n-list"><div class="empty">输入实体 ID 后加载</div></div>
      </div>`;
    const categorySelect = document.getElementById('i18n-category');
    const entityInput = document.getElementById('i18n-entity');
    document.getElementById('i18n-form').addEventListener('submit', guard(async (event) => {
      event.preventDefault();
      await loadTranslations(categorySelect.value, entityInput.value);
    }));
    document.getElementById('i18n-edit').addEventListener('submit', guard(async (event) => {
      event.preventDefault();
      const form = event.target;
      const data = {};
      new FormData(form).forEach((value, key) => { data[key] = value; });
      await post(API.siis + '/i18n/' + encodeURIComponent(categorySelect.value) + '/' + entityInput.value, data);
      status('翻译已保存', 'success');
      await loadTranslations(categorySelect.value, entityInput.value);
    }));

    async function loadTranslations(cat, id) {
      if (!id) { status('请输入实体 ID', 'error'); return; }
      const list = unwrap(await get(API.siis + '/i18n/' + encodeURIComponent(cat) + '/' + id)) || [];
      document.getElementById('i18n-edit').hidden = false;
      document.getElementById('i18n-list').innerHTML = list.length
        ? `<table><thead><tr><th>语种</th><th>名称</th><th>描述</th><th>操作</th></tr></thead><tbody>${list.map(item => `
            <tr><td>${esc(item.lang)}</td><td>${esc(item.name)}</td><td>${esc(item.description)}</td>
            <td><button type="button" class="danger" data-del="${esc(item.lang)}">删除</button></td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">该实体暂无翻译</div>';
      document.querySelectorAll('#i18n-list [data-del]').forEach(btn => btn.addEventListener('click', guard(async () => {
        await del(API.siis + '/i18n/' + encodeURIComponent(cat) + '/' + id + '/' + btn.getAttribute('data-del'));
        status('翻译已删除', 'success');
        await loadTranslations(cat, id);
      })));
    }
    if (entityId) await loadTranslations(category, entityId);
  }

  /* ---------------- 晋升工作台 ---------------- */
  async function renderPromote() {
    showSidebar(true); markActive('promote');
    const rows = unwrap(await get(API.siis + '/discovered?status=DISCOVERED&limit=50')) || [];
    viewEl.innerHTML = `
      <h1>晋升工作台</h1>
      <div class="panel">
        <div class="row" style="justify-content:space-between">
          <p style="margin:0">发现实体经匹配确认后晋升到 9 类本体表；已晋升记录幂等返回。</p>
          <label style="min-width:150px;margin:0"><span>目标类型</span>
            <select id="promote-target"><option value="">按原类型</option>${PROMOTABLE.map(t =>
              `<option>${t}</option>`).join('')}</select></label>
        </div>
        ${rows.length ? `<table><thead><tr><th>ID</th><th>名称</th><th>原类型</th><th>热度</th><th>操作</th></tr></thead><tbody>
          ${rows.map(row => `<tr><td>${esc(row.id)}</td><td>${esc(row.entity_name)}</td><td>${esc(row.entity_type)}</td>
            <td>${esc(row.mention_count ?? 0)}</td>
            <td><button type="button" class="secondary" data-match="${esc(row.id)}"
              data-name="${esc(row.entity_name)}" data-type="${esc(row.entity_type)}">匹配预览</button>
            <button type="button" data-promote="${esc(row.id)}">晋升</button></td></tr>`).join('')}
          </tbody></table>` : '<div class="empty">暂无待晋升的发现实体</div>'}
      </div>
      <div class="panel"><h2>匹配结果</h2><div id="match-result"><div class="empty">选择记录进行匹配预览</div></div></div>`;
    viewEl.querySelectorAll('[data-match]').forEach(btn => btn.addEventListener('click', guard(async () => {
      const name = btn.getAttribute('data-name');
      const type = document.getElementById('promote-target').value || btn.getAttribute('data-type');
      const body = await get(API.siis + '/match?name=' + encodeURIComponent(name) + '&type=' + encodeURIComponent(type));
      document.getElementById('match-result').innerHTML = `<div class="pre">${esc(JSON.stringify(body, null, 2))}</div>`;
    })));
    viewEl.querySelectorAll('[data-promote]').forEach(btn => btn.addEventListener('click', guard(async () => {
      if (!confirm('确认晋升该发现实体？')) return;
      const target = document.getElementById('promote-target').value;
      const url = API.siis + '/entity/promote?discoveredEntityId=' + btn.getAttribute('data-promote')
        + (target ? '&targetType=' + encodeURIComponent(target) : '');
      const body = await post(url, {});
      status('晋升结果：' + JSON.stringify(unwrap(body)), 'success');
      await renderPromote();
    })));
  }

  /* ---------------- AI 对话 ---------------- */
  async function renderAi() {
    showSidebar(true); markActive('ai');
    viewEl.innerHTML = `
      <h1>AI 对话</h1>
      <div class="panel">
        <form id="ai-form" class="grid-form">
          <label><span>Agent ID</span><input id="ai-agent" value="dsh-ontology" required></label>
          <label><span>本体库 ID（可选）</span><input id="ai-ontology"></label>
          <label style="grid-column:1/-1"><span>问题</span><textarea id="ai-message" required
            placeholder="例如：列出本体库中的国家实体，并说明其字段结构。"></textarea></label>
          <div class="row" style="grid-column:1/-1">
            <button type="submit">发送</button>
            <button type="button" id="ai-stream" class="secondary">流式发送</button>
          </div>
        </form>
      </div>
      <div class="panel"><h2>回答</h2><div id="ai-output" class="pre">等待提问...</div></div>`;
    const payload = () => ({
      agentId: document.getElementById('ai-agent').value.trim(),
      ontologyId: document.getElementById('ai-ontology').value.trim(),
      message: document.getElementById('ai-message').value.trim()
    });
    document.getElementById('ai-form').addEventListener('submit', guard(async (event) => {
      event.preventDefault();
      document.getElementById('ai-output').textContent = '思考中...';
      const body = await post(API.ontologies + '/ai', payload());
      document.getElementById('ai-output').textContent = (body && (body.answer || unwrap(body))) || '无回答';
    }));
    document.getElementById('ai-stream').addEventListener('click', guard(async () => {
      const output = document.getElementById('ai-output');
      output.textContent = '';
      const response = await fetch(API.ontologies + '/ai/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token() },
        body: JSON.stringify(payload())
      });
      if (!response.ok) throw new Error('流式请求失败 HTTP ' + response.status);
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split('\n\n');
        buffer = events.pop();
        events.forEach(event => {
          event.split('\n').filter(line => line.startsWith('data:')).forEach(line => {
            output.textContent += line.slice(5).trimStart();
          });
        });
      }
    }));
  }

  /* ---------------- 路由 ---------------- */
  function resolve() {
    const hash = location.hash.replace(/^#/, '') || '/overview';
    // 桌面端暂无登录界面：未登录时不再强制跳转，由后端按主任身份默认放行
    if (hash === '/login' && token()) { location.hash = '#/overview'; return; }
    if (hash === '/login') { renderLogin(); return; }
    const parts = hash.split('/').filter(Boolean);
    const route = parts[0];
    if (route === 'overview') guard(renderOverview)();
    else if (route === 'entities') guard(() => renderEntities(decodeURIComponent(parts[1] || '')))();
    else if (route === 'search') guard(renderSearch)();
    else if (route === 'i18n') guard(() => renderI18n(decodeURIComponent(parts[1] || ''), parts[2]))();
    else if (route === 'promote') guard(renderPromote)();
    else if (route === 'ai') guard(renderAi)();
    else { location.hash = '#/overview'; }
  }

  window.addEventListener('hashchange', resolve);
  resolve();
})();
