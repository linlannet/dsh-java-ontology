async function loadOntologies() {
    const res = await fetch('/api/ontologies');
    const data = await res.json();
    const tbody = document.querySelector('#ontology-table tbody');
    tbody.innerHTML = '';
    data.forEach(o => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${o.id}</td><td>${o.name}</td><td>${o.description || ''}</td><td>${o.source || ''}</td>`
            + `<td><button onclick="deleteOntology('${o.id}')">删除</button></td>`;
        tbody.appendChild(tr);
    });
}

function showNewForm() {
    document.getElementById('new-form').style.display = 'block';
}

async function createOntology() {
    const body = {
        name: document.getElementById('new-name').value,
        description: document.getElementById('new-desc').value,
        source: document.getElementById('new-source').value
    };
    await fetch('/api/ontologies', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)});
    document.getElementById('new-form').style.display = 'none';
    loadOntologies();
}

async function deleteOntology(id) {
    await fetch('/api/ontologies/' + id, {method: 'DELETE'});
    loadOntologies();
}

async function aiChat() {
    const body = {
        agentId: document.getElementById('agent-id').value,
        ontologyId: document.getElementById('ontology-id').value,
        message: document.getElementById('ai-message').value
    };
    const res = await fetch('/api/ontologies/ai', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)});
    const data = await res.json();
    document.getElementById('ai-output').textContent = data.answer || JSON.stringify(data);
}

function aiStream() {
    const body = {
        agentId: document.getElementById('agent-id').value,
        ontologyId: document.getElementById('ontology-id').value,
        message: document.getElementById('ai-message').value
    };
    const output = document.getElementById('ai-output');
    output.textContent = '';
    fetch('/api/ontologies/ai/stream', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)})
        .then(res => {
            const reader = res.body.getReader();
            const decoder = new TextDecoder();
            function pump() {
                reader.read().then(({done, value}) => {
                    if (done) return;
                    output.textContent += decoder.decode(value);
                    pump();
                });
            }
            pump();
        });
}

loadOntologies();
