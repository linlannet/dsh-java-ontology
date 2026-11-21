-- =============================================================================
-- 演示数据脚本：平台本体库（demo_ontology_onto.sql）
-- -----------------------------------------------------------------------------
-- 目标库    : dsh_java_ontodb
-- 执行顺序  : 第 2 步（本脚本）；请先执行 dsh-ontolab-siis/docs/dev-ops/demo/demo_ontology_siis.sql
-- 适用范围  : 仅用于 demo / 演示环境，严禁在生产或正式数据环境执行
-- 是否 Flyway: 否，不属于任何 Flyway 迁移目录，不会被应用启动自动执行
-- 可重复执行: 是。所有演示行 created_by = 'demo-0.2.2'，执行前先删 i18n，再删实体
-- 前置要求  : 先完成数据库备份；确认当前连接的是 demo 库
-- 跨库依赖  : 本脚本从 dsh_java_siisdb 读取国家/机构/人物/媒体/书籍/论文的 id。
--             如 SIIS 库名不是 dsh_java_siisdb，请全局替换为本环境实际库名；
--             跨库访问要求两个库位于同一 MySQL 实例且当前账号具备跨库 SELECT 权限。
-- 口径说明  : onto_ontology_version.entity_count 统一由 demo_ontology_siis.sql 回填，
--             本脚本不重复写入，避免 SIIS 业务实体与平台本体实体双重计数。
-- 字符集    : utf8mb4
-- =============================================================================

SET NAMES utf8mb4;

-- =============================================================================
-- 0. 清理旧演示数据（i18n 必须先于实体；discovered 单独删除）
-- =============================================================================
DELETE FROM onto_ontology_i18n WHERE created_by = 'demo-0.2.2';

DELETE FROM onto_common_discovered_entity WHERE created_by = 'demo-0.2.2';

DELETE FROM onto_ontology_term     WHERE created_by = 'demo-0.2.2';
DELETE FROM onto_ontology_location WHERE created_by = 'demo-0.2.2';

-- =============================================================================
-- 1. 术语 onto_ontology_term（20 行）
-- term_name 无数据库唯一约束，通过 DELETE + INSERT 保证重复执行不累积
-- =============================================================================
INSERT INTO onto_ontology_term
(domain, term_name, category, definition, source, lang, created_by) VALUES
('siis', '知识图谱',       '知识表示',     '以图结构组织实体及其关系的知识表示方式。',                     '公开资料', 'zh', 'demo-0.2.2'),
('siis', '实体对齐',       '知识融合',     '识别不同数据源中指向同一对象的实体并建立对应关系。',           '公开资料', 'zh', 'demo-0.2.2'),
('siis', '关系抽取',       '知识抽取',     '从文本中识别实体之间的语义关系。',                             '公开资料', 'zh', 'demo-0.2.2'),
('siis', '本体推理',       '知识推理',     '基于本体公理与规则推导隐含知识的过程。',                       '公开资料', 'zh', 'demo-0.2.2'),
('siis', '语义网',         '知识表示',     '强调数据可被机器理解与互操作的网络数据组织方式。',             '公开资料', 'zh', 'demo-0.2.2'),
('siis', '知识表示',       '知识表示',     '将知识组织为计算机可处理形式的方法与理论。',                   '公开资料', 'zh', 'demo-0.2.2'),
('siis', '三元组',         '知识表示',     '由主体、谓词、客体构成的原子知识陈述。',                       '公开资料', 'zh', 'demo-0.2.2'),
('siis', '实体消歧',       '知识抽取',     '在同一名称可能指向多个实体时确定其真实指代对象。',             '公开资料', 'zh', 'demo-0.2.2'),
('siis', '命名实体识别',   '知识抽取',     '从文本中识别具有特定语义类别的实体提及。',                     '公开资料', 'zh', 'demo-0.2.2'),
('siis', '本体',           '知识表示',     '描述领域概念、属性及概念间关系的共享概念模型。',               '公开资料', 'zh', 'demo-0.2.2'),
('siis', '知识库',         '知识基础设施', '按结构化方式存储、组织与管理知识的系统。',                     '公开资料', 'zh', 'demo-0.2.2'),
('siis', '推理引擎',       '知识推理',     '执行逻辑规则并推导新事实的软件组件。',                         '公开资料', 'zh', 'demo-0.2.2'),
('siis', '图数据库',       '知识基础设施', '以节点和边为主要数据模型的数据库系统。',                       '公开资料', 'zh', 'demo-0.2.2'),
('siis', '向量嵌入',       '知识表示',     '将对象映射为低维连续向量的表示方法。',                         '公开资料', 'zh', 'demo-0.2.2'),
('siis', '知识融合',       '知识融合',     '整合多源异构知识并消除冲突与冗余的过程。',                     '公开资料', 'zh', 'demo-0.2.2'),
('siis', '元数据',         '数据治理',     '描述数据内容、结构、来源与管理规则的数据。',                   '公开资料', 'zh', 'demo-0.2.2'),
('siis', '数据治理',       '数据治理',     '围绕数据质量、安全与合规建立的管理机制。',                     '公开资料', 'zh', 'demo-0.2.2'),
('siis', '模式匹配',       '知识融合',     '识别不同数据模式之间的对应关系。',                             '公开资料', 'zh', 'demo-0.2.2'),
('siis', '查询扩展',       '知识应用',     '通过同义、上下位等关系扩充原始查询以提升召回。',               '公开资料', 'zh', 'demo-0.2.2'),
('siis', '语义推理',       '知识推理',     '利用语义关系与约束推导隐含结论的方法。',                       '公开资料', 'zh', 'demo-0.2.2');

-- =============================================================================
-- 2. 地名 onto_ontology_location（15 行）
-- =============================================================================
INSERT INTO onto_ontology_location
(domain, name, loc_type, parent_id, country, capital, population, area, description, lang, created_by) VALUES
('siis', '北京',           'city',   NULL, '中国',   '北京',       '约2180万', '约1.64万平方公里', '中国首都，政治、文化与科技创新中心。',           'zh', 'demo-0.2.2'),
('siis', '上海',           'city',   NULL, '中国',   NULL,         '约2480万', '约6340平方公里',   '中国东部沿海城市，国际金融与航运中心之一。',     'zh', 'demo-0.2.2'),
('siis', '广州',           'city',   NULL, '中国',   NULL,         '约1880万', '约7434平方公里',   '中国南部城市，重要的商贸与交通枢纽。',           'zh', 'demo-0.2.2'),
('siis', '深圳',           'city',   NULL, '中国',   NULL,         '约1770万', '约1997平方公里',   '中国南部城市，科技创新与制造业集聚地。',         'zh', 'demo-0.2.2'),
('siis', '东京',           'city',   NULL, '日本',   '东京',       '约1400万', '约2194平方公里',   '日本首都，重要的经济与金融中心。',               'zh', 'demo-0.2.2'),
('siis', '巴黎',           'city',   NULL, '法国',   '巴黎',       '约210万',  '约105平方公里',    '法国首都，重要的文化与商业中心。',               'zh', 'demo-0.2.2'),
('siis', '伦敦',           'city',   NULL, '英国',   '伦敦',       '约900万',  '约1572平方公里',   '英国首都，重要的金融与文化中心。',               'zh', 'demo-0.2.2'),
('siis', '纽约',           'city',   NULL, '美国',   NULL,         '约840万',  '约1214平方公里',   '美国重要城市，国际金融与商业中心之一。',         'zh', 'demo-0.2.2'),
('siis', '柏林',           'city',   NULL, '德国',   '柏林',       '约370万',  '约891平方公里',    '德国首都，重要的政治与文化中心。',               'zh', 'demo-0.2.2'),
('siis', '莫斯科',         'city',   NULL, '俄罗斯', '莫斯科',     '约1300万', '约2561平方公里',   '俄罗斯首都，重要的政治与经济中心。',             'zh', 'demo-0.2.2'),
('siis', '新加坡',         'city',   NULL, '新加坡', '新加坡',     '约590万',  '约728平方公里',    '城市国家，重要的国际航运与金融中心。',           'zh', 'demo-0.2.2'),
('siis', '首尔',           'city',   NULL, '韩国',   '首尔',       '约940万',  '约605平方公里',    '韩国首都，重要的经济与文化中心。',               'zh', 'demo-0.2.2'),
('siis', '粤港澳大湾区',   'region', NULL, '中国',   NULL,         '约8600万', '约5.6万平方公里',  '由珠三角城市群组成的区域发展空间。',             'zh', 'demo-0.2.2'),
('siis', '长三角地区',     'region', NULL, '中国',   NULL,         '约2.3亿',  '约35.8万平方公里', '由长江三角洲城市群组成的区域发展空间。',         'zh', 'demo-0.2.2'),
('siis', '京津冀地区',     'region', NULL, '中国',   NULL,         '约1.1亿',  '约21.6万平方公里', '由北京、天津与河北组成的区域发展空间。',         'zh', 'demo-0.2.2');

-- =============================================================================
-- 3. 多语种 i18n onto_ontology_i18n（预期 632 行）
-- 行数构成：country 32×7=224；organization 20×7=140；person 30×2=60；
--           media 10×2=20；book 10×1=10；paper 8×1=8；term 20×7=140；
--           location 15×2=30。合计 632。
-- 唯一键 uk_entity_category_lang(entity_id, category, lang) 由文件开头的 DELETE 保证不冲突。
-- 说明：大表只抽取代表实体翻译；国家覆盖全部 32 国 × 7 语种。
-- =============================================================================

-- 3.1 国家 country：en（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'en',
       CASE c.iso_code
           WHEN 'CHN' THEN 'China'
           WHEN 'USA' THEN 'United States'
           WHEN 'JPN' THEN 'Japan'
           WHEN 'DEU' THEN 'Germany'
           WHEN 'FRA' THEN 'France'
           WHEN 'GBR' THEN 'United Kingdom'
           WHEN 'RUS' THEN 'Russia'
           WHEN 'IND' THEN 'India'
           WHEN 'BRA' THEN 'Brazil'
           WHEN 'CAN' THEN 'Canada'
           WHEN 'AUS' THEN 'Australia'
           WHEN 'KOR' THEN 'South Korea'
           WHEN 'ITA' THEN 'Italy'
           WHEN 'ESP' THEN 'Spain'
           WHEN 'PRT' THEN 'Portugal'
           WHEN 'NLD' THEN 'Netherlands'
           WHEN 'CHE' THEN 'Switzerland'
           WHEN 'SWE' THEN 'Sweden'
           WHEN 'NOR' THEN 'Norway'
           WHEN 'DNK' THEN 'Denmark'
           WHEN 'FIN' THEN 'Finland'
           WHEN 'POL' THEN 'Poland'
           WHEN 'TUR' THEN 'Turkey'
           WHEN 'SAU' THEN 'Saudi Arabia'
           WHEN 'ARE' THEN 'United Arab Emirates'
           WHEN 'EGY' THEN 'Egypt'
           WHEN 'ZAF' THEN 'South Africa'
           WHEN 'NGA' THEN 'Nigeria'
           WHEN 'MEX' THEN 'Mexico'
           WHEN 'ARG' THEN 'Argentina'
           WHEN 'IDN' THEN 'Indonesia'
           WHEN 'SGP' THEN 'Singapore'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.2 国家 country：fr（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'fr',
       CASE c.iso_code
           WHEN 'CHN' THEN 'Chine'
           WHEN 'USA' THEN 'États-Unis'
           WHEN 'JPN' THEN 'Japon'
           WHEN 'DEU' THEN 'Allemagne'
           WHEN 'FRA' THEN 'France'
           WHEN 'GBR' THEN 'Royaume-Uni'
           WHEN 'RUS' THEN 'Russie'
           WHEN 'IND' THEN 'Inde'
           WHEN 'BRA' THEN 'Brésil'
           WHEN 'CAN' THEN 'Canada'
           WHEN 'AUS' THEN 'Australie'
           WHEN 'KOR' THEN 'Corée du Sud'
           WHEN 'ITA' THEN 'Italie'
           WHEN 'ESP' THEN 'Espagne'
           WHEN 'PRT' THEN 'Portugal'
           WHEN 'NLD' THEN 'Pays-Bas'
           WHEN 'CHE' THEN 'Suisse'
           WHEN 'SWE' THEN 'Suède'
           WHEN 'NOR' THEN 'Norvège'
           WHEN 'DNK' THEN 'Danemark'
           WHEN 'FIN' THEN 'Finlande'
           WHEN 'POL' THEN 'Pologne'
           WHEN 'TUR' THEN 'Turquie'
           WHEN 'SAU' THEN 'Arabie saoudite'
           WHEN 'ARE' THEN 'Émirats arabes unis'
           WHEN 'EGY' THEN 'Égypte'
           WHEN 'ZAF' THEN 'Afrique du Sud'
           WHEN 'NGA' THEN 'Nigéria'
           WHEN 'MEX' THEN 'Mexique'
           WHEN 'ARG' THEN 'Argentine'
           WHEN 'IDN' THEN 'Indonésie'
           WHEN 'SGP' THEN 'Singapour'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.3 国家 country：ar（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'ar',
       CASE c.iso_code
           WHEN 'CHN' THEN 'الصين'
           WHEN 'USA' THEN 'الولايات المتحدة'
           WHEN 'JPN' THEN 'اليابان'
           WHEN 'DEU' THEN 'ألمانيا'
           WHEN 'FRA' THEN 'فرنسا'
           WHEN 'GBR' THEN 'المملكة المتحدة'
           WHEN 'RUS' THEN 'روسيا'
           WHEN 'IND' THEN 'الهند'
           WHEN 'BRA' THEN 'البرازيل'
           WHEN 'CAN' THEN 'كندا'
           WHEN 'AUS' THEN 'أستراليا'
           WHEN 'KOR' THEN 'كوريا الجنوبية'
           WHEN 'ITA' THEN 'إيطاليا'
           WHEN 'ESP' THEN 'إسبانيا'
           WHEN 'PRT' THEN 'البرتغال'
           WHEN 'NLD' THEN 'هولندا'
           WHEN 'CHE' THEN 'سويسرا'
           WHEN 'SWE' THEN 'السويد'
           WHEN 'NOR' THEN 'النرويج'
           WHEN 'DNK' THEN 'الدنمارك'
           WHEN 'FIN' THEN 'فنلندا'
           WHEN 'POL' THEN 'بولندا'
           WHEN 'TUR' THEN 'تركيا'
           WHEN 'SAU' THEN 'المملكة العربية السعودية'
           WHEN 'ARE' THEN 'الإمارات العربية المتحدة'
           WHEN 'EGY' THEN 'مصر'
           WHEN 'ZAF' THEN 'جنوب أفريقيا'
           WHEN 'NGA' THEN 'نيجيريا'
           WHEN 'MEX' THEN 'المكسيك'
           WHEN 'ARG' THEN 'الأرجنتين'
           WHEN 'IDN' THEN 'إندونيسيا'
           WHEN 'SGP' THEN 'سنغافورة'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.4 国家 country：ru（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'ru',
       CASE c.iso_code
           WHEN 'CHN' THEN 'Китай'
           WHEN 'USA' THEN 'Соединённые Штаты'
           WHEN 'JPN' THEN 'Япония'
           WHEN 'DEU' THEN 'Германия'
           WHEN 'FRA' THEN 'Франция'
           WHEN 'GBR' THEN 'Великобритания'
           WHEN 'RUS' THEN 'Россия'
           WHEN 'IND' THEN 'Индия'
           WHEN 'BRA' THEN 'Бразилия'
           WHEN 'CAN' THEN 'Канада'
           WHEN 'AUS' THEN 'Австралия'
           WHEN 'KOR' THEN 'Республика Корея'
           WHEN 'ITA' THEN 'Италия'
           WHEN 'ESP' THEN 'Испания'
           WHEN 'PRT' THEN 'Португалия'
           WHEN 'NLD' THEN 'Нидерланды'
           WHEN 'CHE' THEN 'Швейцария'
           WHEN 'SWE' THEN 'Швеция'
           WHEN 'NOR' THEN 'Норвегия'
           WHEN 'DNK' THEN 'Дания'
           WHEN 'FIN' THEN 'Финляндия'
           WHEN 'POL' THEN 'Польша'
           WHEN 'TUR' THEN 'Турция'
           WHEN 'SAU' THEN 'Саудовская Арабия'
           WHEN 'ARE' THEN 'Объединённые Арабские Эмираты'
           WHEN 'EGY' THEN 'Египет'
           WHEN 'ZAF' THEN 'Южно-Африканская Республика'
           WHEN 'NGA' THEN 'Нигерия'
           WHEN 'MEX' THEN 'Мексика'
           WHEN 'ARG' THEN 'Аргентина'
           WHEN 'IDN' THEN 'Индонезия'
           WHEN 'SGP' THEN 'Сингапур'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.5 国家 country：es（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'es',
       CASE c.iso_code
           WHEN 'CHN' THEN 'China'
           WHEN 'USA' THEN 'Estados Unidos'
           WHEN 'JPN' THEN 'Japón'
           WHEN 'DEU' THEN 'Alemania'
           WHEN 'FRA' THEN 'Francia'
           WHEN 'GBR' THEN 'Reino Unido'
           WHEN 'RUS' THEN 'Rusia'
           WHEN 'IND' THEN 'India'
           WHEN 'BRA' THEN 'Brasil'
           WHEN 'CAN' THEN 'Canadá'
           WHEN 'AUS' THEN 'Australia'
           WHEN 'KOR' THEN 'Corea del Sur'
           WHEN 'ITA' THEN 'Italia'
           WHEN 'ESP' THEN 'España'
           WHEN 'PRT' THEN 'Portugal'
           WHEN 'NLD' THEN 'Países Bajos'
           WHEN 'CHE' THEN 'Suiza'
           WHEN 'SWE' THEN 'Suecia'
           WHEN 'NOR' THEN 'Noruega'
           WHEN 'DNK' THEN 'Dinamarca'
           WHEN 'FIN' THEN 'Finlandia'
           WHEN 'POL' THEN 'Polonia'
           WHEN 'TUR' THEN 'Turquía'
           WHEN 'SAU' THEN 'Arabia Saudita'
           WHEN 'ARE' THEN 'Emiratos Árabes Unidos'
           WHEN 'EGY' THEN 'Egipto'
           WHEN 'ZAF' THEN 'Sudáfrica'
           WHEN 'NGA' THEN 'Nigeria'
           WHEN 'MEX' THEN 'México'
           WHEN 'ARG' THEN 'Argentina'
           WHEN 'IDN' THEN 'Indonesia'
           WHEN 'SGP' THEN 'Singapur'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.6 国家 country：ja（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'ja',
       CASE c.iso_code
           WHEN 'CHN' THEN '中国'
           WHEN 'USA' THEN 'アメリカ合衆国'
           WHEN 'JPN' THEN '日本'
           WHEN 'DEU' THEN 'ドイツ'
           WHEN 'FRA' THEN 'フランス'
           WHEN 'GBR' THEN 'イギリス'
           WHEN 'RUS' THEN 'ロシア'
           WHEN 'IND' THEN 'インド'
           WHEN 'BRA' THEN 'ブラジル'
           WHEN 'CAN' THEN 'カナダ'
           WHEN 'AUS' THEN 'オーストラリア'
           WHEN 'KOR' THEN '韓国'
           WHEN 'ITA' THEN 'イタリア'
           WHEN 'ESP' THEN 'スペイン'
           WHEN 'PRT' THEN 'ポルトガル'
           WHEN 'NLD' THEN 'オランダ'
           WHEN 'CHE' THEN 'スイス'
           WHEN 'SWE' THEN 'スウェーデン'
           WHEN 'NOR' THEN 'ノルウェー'
           WHEN 'DNK' THEN 'デンマーク'
           WHEN 'FIN' THEN 'フィンランド'
           WHEN 'POL' THEN 'ポーランド'
           WHEN 'TUR' THEN 'トルコ'
           WHEN 'SAU' THEN 'サウジアラビア'
           WHEN 'ARE' THEN 'アラブ首長国連邦'
           WHEN 'EGY' THEN 'エジプト'
           WHEN 'ZAF' THEN '南アフリカ'
           WHEN 'NGA' THEN 'ナイジェリア'
           WHEN 'MEX' THEN 'メキシコ'
           WHEN 'ARG' THEN 'アルゼンチン'
           WHEN 'IDN' THEN 'インドネシア'
           WHEN 'SGP' THEN 'シンガポール'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.7 国家 country：de（32 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', c.id, 'country', 'de',
       CASE c.iso_code
           WHEN 'CHN' THEN 'China'
           WHEN 'USA' THEN 'Vereinigte Staaten'
           WHEN 'JPN' THEN 'Japan'
           WHEN 'DEU' THEN 'Deutschland'
           WHEN 'FRA' THEN 'Frankreich'
           WHEN 'GBR' THEN 'Vereinigtes Königreich'
           WHEN 'RUS' THEN 'Russland'
           WHEN 'IND' THEN 'Indien'
           WHEN 'BRA' THEN 'Brasilien'
           WHEN 'CAN' THEN 'Kanada'
           WHEN 'AUS' THEN 'Australien'
           WHEN 'KOR' THEN 'Südkorea'
           WHEN 'ITA' THEN 'Italien'
           WHEN 'ESP' THEN 'Spanien'
           WHEN 'PRT' THEN 'Portugal'
           WHEN 'NLD' THEN 'Niederlande'
           WHEN 'CHE' THEN 'Schweiz'
           WHEN 'SWE' THEN 'Schweden'
           WHEN 'NOR' THEN 'Norwegen'
           WHEN 'DNK' THEN 'Dänemark'
           WHEN 'FIN' THEN 'Finnland'
           WHEN 'POL' THEN 'Polen'
           WHEN 'TUR' THEN 'Türkei'
           WHEN 'SAU' THEN 'Saudi-Arabien'
           WHEN 'ARE' THEN 'Vereinigte Arabische Emirate'
           WHEN 'EGY' THEN 'Ägypten'
           WHEN 'ZAF' THEN 'Südafrika'
           WHEN 'NGA' THEN 'Nigeria'
           WHEN 'MEX' THEN 'Mexiko'
           WHEN 'ARG' THEN 'Argentinien'
           WHEN 'IDN' THEN 'Indonesien'
           WHEN 'SGP' THEN 'Singapur'
           ELSE c.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_country c
WHERE c.created_by = 'demo-0.2.2';

-- 3.8 机构 organization：en（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'en',
       CASE o.name
           WHEN '联合国' THEN 'United Nations'
           WHEN '世界卫生组织' THEN 'World Health Organization'
           WHEN '世界银行' THEN 'World Bank'
           WHEN '国际货币基金组织' THEN 'International Monetary Fund'
           WHEN '世界贸易组织' THEN 'World Trade Organization'
           WHEN '经济合作与发展组织' THEN 'Organisation for Economic Co-operation and Development'
           WHEN '北大西洋公约组织' THEN 'North Atlantic Treaty Organization'
           WHEN '欧洲联盟' THEN 'European Union'
           WHEN '红十字国际委员会' THEN 'International Committee of the Red Cross'
           WHEN '无国界医生' THEN 'Médecins Sans Frontières'
           WHEN '国际特赦组织' THEN 'Amnesty International'
           WHEN '绿色和平' THEN 'Greenpeace'
           WHEN '世界自然基金会' THEN 'World Wide Fund for Nature'
           WHEN '乐施会' THEN 'Oxfam'
           WHEN '世界经济论坛' THEN 'World Economic Forum'
           WHEN '国际标准化组织' THEN 'International Organization for Standardization'
           WHEN '苹果公司' THEN 'Apple Inc.'
           WHEN '微软公司' THEN 'Microsoft Corporation'
           WHEN '丰田汽车' THEN 'Toyota Motor Corporation'
           WHEN '西门子' THEN 'Siemens AG'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.9 机构 organization：fr（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'fr',
       CASE o.name
           WHEN '联合国' THEN 'Organisation des Nations unies'
           WHEN '世界卫生组织' THEN 'Organisation mondiale de la Santé'
           WHEN '世界银行' THEN 'Banque mondiale'
           WHEN '国际货币基金组织' THEN 'Fonds monétaire international'
           WHEN '世界贸易组织' THEN 'Organisation mondiale du commerce'
           WHEN '经济合作与发展组织' THEN 'Organisation de coopération et de développement économiques'
           WHEN '北大西洋公约组织' THEN 'Organisation du traité de l''Atlantique nord'
           WHEN '欧洲联盟' THEN 'Union européenne'
           WHEN '红十字国际委员会' THEN 'Comité international de la Croix-Rouge'
           WHEN '无国界医生' THEN 'Médecins Sans Frontières'
           WHEN '国际特赦组织' THEN 'Amnesty International'
           WHEN '绿色和平' THEN 'Greenpeace'
           WHEN '世界自然基金会' THEN 'World Wide Fund for Nature'
           WHEN '乐施会' THEN 'Oxfam'
           WHEN '世界经济论坛' THEN 'Forum économique mondial'
           WHEN '国际标准化组织' THEN 'Organisation internationale de normalisation'
           WHEN '苹果公司' THEN 'Apple Inc.'
           WHEN '微软公司' THEN 'Microsoft Corporation'
           WHEN '丰田汽车' THEN 'Toyota Motor Corporation'
           WHEN '西门子' THEN 'Siemens AG'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.10 机构 organization：ar（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'ar',
       CASE o.name
           WHEN '联合国' THEN 'الأمم المتحدة'
           WHEN '世界卫生组织' THEN 'منظمة الصحة العالمية'
           WHEN '世界银行' THEN 'البنك الدولي'
           WHEN '国际货币基金组织' THEN 'صندوق النقد الدولي'
           WHEN '世界贸易组织' THEN 'منظمة التجارة العالمية'
           WHEN '经济合作与发展组织' THEN 'منظمة التعاون الاقتصادي والتنمية'
           WHEN '北大西洋公约组织' THEN 'حلف شمال الأطلسي'
           WHEN '欧洲联盟' THEN 'الاتحاد الأوروبي'
           WHEN '红十字国际委员会' THEN 'اللجنة الدولية للصليب الأحمر'
           WHEN '无国界医生' THEN 'أطباء بلا حدود'
           WHEN '国际特赦组织' THEN 'منظمة العفو الدولية'
           WHEN '绿色和平' THEN 'غرينبيس'
           WHEN '世界自然基金会' THEN 'الصندوق العالمي للطبيعة'
           WHEN '乐施会' THEN 'أوكسفام'
           WHEN '世界经济论坛' THEN 'المنتدى الاقتصادي العالمي'
           WHEN '国际标准化组织' THEN 'المنظمة الدولية للتوحيد القياسي'
           WHEN '苹果公司' THEN 'شركة آبل'
           WHEN '微软公司' THEN 'شركة مايكروسوفت'
           WHEN '丰田汽车' THEN 'شركة تويوتا للسيارات'
           WHEN '西门子' THEN 'شركة سيمنس'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.11 机构 organization：ru（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'ru',
       CASE o.name
           WHEN '联合国' THEN 'Организация Объединённых Наций'
           WHEN '世界卫生组织' THEN 'Всемирная организация здравоохранения'
           WHEN '世界银行' THEN 'Всемирный банк'
           WHEN '国际货币基金组织' THEN 'Международный валютный фонд'
           WHEN '世界贸易组织' THEN 'Всемирная торговая организация'
           WHEN '经济合作与发展组织' THEN 'Организация экономического сотрудничества и развития'
           WHEN '北大西洋公约组织' THEN 'Организация Североатлантического договора'
           WHEN '欧洲联盟' THEN 'Европейский союз'
           WHEN '红十字国际委员会' THEN 'Международный комитет Красного Креста'
           WHEN '无国界医生' THEN 'Врачи без границ'
           WHEN '国际特赦组织' THEN 'Amnesty International'
           WHEN '绿色和平' THEN 'Гринпис'
           WHEN '世界自然基金会' THEN 'Всемирный фонд дикой природы'
           WHEN '乐施会' THEN 'Оксфам'
           WHEN '世界经济论坛' THEN 'Всемирный экономический форум'
           WHEN '国际标准化组织' THEN 'Международная организация по стандартизации'
           WHEN '苹果公司' THEN 'Apple Inc.'
           WHEN '微软公司' THEN 'Microsoft Corporation'
           WHEN '丰田汽车' THEN 'Toyota Motor Corporation'
           WHEN '西门子' THEN 'Siemens AG'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.12 机构 organization：es（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'es',
       CASE o.name
           WHEN '联合国' THEN 'Organización de las Naciones Unidas'
           WHEN '世界卫生组织' THEN 'Organización Mundial de la Salud'
           WHEN '世界银行' THEN 'Banco Mundial'
           WHEN '国际货币基金组织' THEN 'Fondo Monetario Internacional'
           WHEN '世界贸易组织' THEN 'Organización Mundial del Comercio'
           WHEN '经济合作与发展组织' THEN 'Organización para la Cooperación y el Desarrollo Económicos'
           WHEN '北大西洋公约组织' THEN 'Organización del Tratado del Atlántico Norte'
           WHEN '欧洲联盟' THEN 'Unión Europea'
           WHEN '红十字国际委员会' THEN 'Comité Internacional de la Cruz Roja'
           WHEN '无国界医生' THEN 'Médicos Sin Fronteras'
           WHEN '国际特赦组织' THEN 'Amnistía Internacional'
           WHEN '绿色和平' THEN 'Greenpeace'
           WHEN '世界自然基金会' THEN 'Fondo Mundial para la Naturaleza'
           WHEN '乐施会' THEN 'Oxfam'
           WHEN '世界经济论坛' THEN 'Foro Económico Mundial'
           WHEN '国际标准化组织' THEN 'Organización Internacional de Normalización'
           WHEN '苹果公司' THEN 'Apple Inc.'
           WHEN '微软公司' THEN 'Microsoft Corporation'
           WHEN '丰田汽车' THEN 'Toyota Motor Corporation'
           WHEN '西门子' THEN 'Siemens AG'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.13 机构 organization：ja（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'ja',
       CASE o.name
           WHEN '联合国' THEN '国際連合'
           WHEN '世界卫生组织' THEN '世界保健機関'
           WHEN '世界银行' THEN '世界銀行'
           WHEN '国际货币基金组织' THEN '国際通貨基金'
           WHEN '世界贸易组织' THEN '世界貿易機関'
           WHEN '经济合作与发展组织' THEN '経済協力開発機構'
           WHEN '北大西洋公约组织' THEN '北大西洋条約機構'
           WHEN '欧洲联盟' THEN '欧州連合'
           WHEN '红十字国际委员会' THEN '赤十字国際委員会'
           WHEN '无国界医生' THEN '国境なき医師団'
           WHEN '国际特赦组织' THEN 'アムネスティ・インターナショナル'
           WHEN '绿色和平' THEN 'グリーンピース'
           WHEN '世界自然基金会' THEN '世界自然保護基金'
           WHEN '乐施会' THEN 'オックスファム'
           WHEN '世界经济论坛' THEN '世界経済フォーラム'
           WHEN '国际标准化组织' THEN '国際標準化機構'
           WHEN '苹果公司' THEN 'アップル'
           WHEN '微软公司' THEN 'マイクロソフト'
           WHEN '丰田汽车' THEN 'トヨタ自動車'
           WHEN '西门子' THEN 'シーメンス'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.14 机构 organization：de（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', o.id, 'organization', 'de',
       CASE o.name
           WHEN '联合国' THEN 'Vereinte Nationen'
           WHEN '世界卫生组织' THEN 'Weltgesundheitsorganisation'
           WHEN '世界银行' THEN 'Weltbank'
           WHEN '国际货币基金组织' THEN 'Internationaler Währungsfonds'
           WHEN '世界贸易组织' THEN 'Welthandelsorganisation'
           WHEN '经济合作与发展组织' THEN 'Organisation für wirtschaftliche Zusammenarbeit und Entwicklung'
           WHEN '北大西洋公约组织' THEN 'Nordatlantikvertrags-Organisation'
           WHEN '欧洲联盟' THEN 'Europäische Union'
           WHEN '红十字国际委员会' THEN 'Internationales Komitee vom Roten Kreuz'
           WHEN '无国界医生' THEN 'Ärzte ohne Grenzen'
           WHEN '国际特赦组织' THEN 'Amnesty International'
           WHEN '绿色和平' THEN 'Greenpeace'
           WHEN '世界自然基金会' THEN 'World Wide Fund for Nature'
           WHEN '乐施会' THEN 'Oxfam'
           WHEN '世界经济论坛' THEN 'Weltwirtschaftsforum'
           WHEN '国际标准化组织' THEN 'Internationale Organisation für Normung'
           WHEN '苹果公司' THEN 'Apple Inc.'
           WHEN '微软公司' THEN 'Microsoft Corporation'
           WHEN '丰田汽车' THEN 'Toyota Motor Corporation'
           WHEN '西门子' THEN 'Siemens AG'
           ELSE o.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_organization o
WHERE o.created_by = 'demo-0.2.2'
  AND o.name IN ('联合国','世界卫生组织','世界银行','国际货币基金组织','世界贸易组织','经济合作与发展组织',
                 '北大西洋公约组织','欧洲联盟','红十字国际委员会','无国界医生','国际特赦组织','绿色和平',
                 '世界自然基金会','乐施会','世界经济论坛','国际标准化组织','苹果公司','微软公司','丰田汽车','西门子');

-- 3.15 人物 person：en（30 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', p.id, 'person', 'en',
       CASE p.name
           WHEN '习近平' THEN 'Xi Jinping'
           WHEN '李强' THEN 'Li Qiang'
           WHEN '王毅' THEN 'Wang Yi'
           WHEN '安东尼奥·古特雷斯' THEN 'António Guterres'
           WHEN '弗拉基米尔·普京' THEN 'Vladimir Putin'
           WHEN '埃马纽埃尔·马克龙' THEN 'Emmanuel Macron'
           WHEN '纳伦德拉·莫迪' THEN 'Narendra Modi'
           WHEN '路易斯·伊纳西奥·卢拉·达席尔瓦' THEN 'Luiz Inácio Lula da Silva'
           WHEN '西里尔·拉马福萨' THEN 'Cyril Ramaphosa'
           WHEN '奥拉夫·朔尔茨' THEN 'Olaf Scholz'
           WHEN '杰弗里·辛顿' THEN 'Geoffrey Hinton'
           WHEN '杨立昆' THEN 'Yann LeCun'
           WHEN '约书亚·本吉奥' THEN 'Yoshua Bengio'
           WHEN '李飞飞' THEN 'Fei-Fei Li'
           WHEN '王昊奋' THEN 'Haofen Wang'
           WHEN '陈华钧' THEN 'Huajun Chen'
           WHEN '漆桂林' THEN 'Guilin Qi'
           WHEN '埃隆·马斯克' THEN 'Elon Musk'
           WHEN '蒂姆·库克' THEN 'Tim Cook'
           WHEN '萨提亚·纳德拉' THEN 'Satya Nadella'
           WHEN '黄仁勋' THEN 'Jensen Huang'
           WHEN '任正非' THEN 'Ren Zhengfei'
           WHEN '马云' THEN 'Jack Ma'
           WHEN '张一鸣' THEN 'Zhang Yiming'
           WHEN '沃伦·巴菲特' THEN 'Warren Buffett'
           WHEN '余华' THEN 'Yu Hua'
           WHEN '莫言' THEN 'Mo Yan'
           WHEN '村上春树' THEN 'Haruki Murakami'
           WHEN '奥普拉·温弗瑞' THEN 'Oprah Winfrey'
           WHEN '阿尔伯特·爱因斯坦' THEN 'Albert Einstein'
           ELSE p.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_person p
WHERE p.created_by = 'demo-0.2.2';

-- 3.16 人物 person：fr（30 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', p.id, 'person', 'fr',
       CASE p.name
           WHEN '习近平' THEN 'Xi Jinping'
           WHEN '李强' THEN 'Li Qiang'
           WHEN '王毅' THEN 'Wang Yi'
           WHEN '安东尼奥·古特雷斯' THEN 'António Guterres'
           WHEN '弗拉基米尔·普京' THEN 'Vladimir Poutine'
           WHEN '埃马纽埃尔·马克龙' THEN 'Emmanuel Macron'
           WHEN '纳伦德拉·莫迪' THEN 'Narendra Modi'
           WHEN '路易斯·伊纳西奥·卢拉·达席尔瓦' THEN 'Luiz Inácio Lula da Silva'
           WHEN '西里尔·拉马福萨' THEN 'Cyril Ramaphosa'
           WHEN '奥拉夫·朔尔茨' THEN 'Olaf Scholz'
           WHEN '杰弗里·辛顿' THEN 'Geoffrey Hinton'
           WHEN '杨立昆' THEN 'Yann LeCun'
           WHEN '约书亚·本吉奥' THEN 'Yoshua Bengio'
           WHEN '李飞飞' THEN 'Fei-Fei Li'
           WHEN '王昊奋' THEN 'Haofen Wang'
           WHEN '陈华钧' THEN 'Huajun Chen'
           WHEN '漆桂林' THEN 'Guilin Qi'
           WHEN '埃隆·马斯克' THEN 'Elon Musk'
           WHEN '蒂姆·库克' THEN 'Tim Cook'
           WHEN '萨提亚·纳德拉' THEN 'Satya Nadella'
           WHEN '黄仁勋' THEN 'Jensen Huang'
           WHEN '任正非' THEN 'Ren Zhengfei'
           WHEN '马云' THEN 'Jack Ma'
           WHEN '张一鸣' THEN 'Zhang Yiming'
           WHEN '沃伦·巴菲特' THEN 'Warren Buffett'
           WHEN '余华' THEN 'Yu Hua'
           WHEN '莫言' THEN 'Mo Yan'
           WHEN '村上春树' THEN 'Haruki Murakami'
           WHEN '奥普拉·温弗瑞' THEN 'Oprah Winfrey'
           WHEN '阿尔伯特·爱因斯坦' THEN 'Albert Einstein'
           ELSE p.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_person p
WHERE p.created_by = 'demo-0.2.2';

-- 3.17 媒体 media：en（10 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', m.id, 'media', 'en',
       CASE m.name
           WHEN '新华社' THEN 'Xinhua News Agency'
           WHEN '人民日报' THEN 'People''s Daily'
           WHEN '中国日报' THEN 'China Daily'
           WHEN '中央广播电视总台' THEN 'China Media Group'
           WHEN '路透社' THEN 'Reuters'
           WHEN '美联社' THEN 'Associated Press'
           WHEN '法新社' THEN 'Agence France-Presse'
           WHEN '英国广播公司' THEN 'BBC'
           WHEN '纽约时报' THEN 'The New York Times'
           WHEN '半岛电视台' THEN 'Al Jazeera'
           ELSE m.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_media m
WHERE m.created_by = 'demo-0.2.2'
  AND m.name IN ('新华社','人民日报','中国日报','中央广播电视总台','路透社','美联社','法新社','英国广播公司','纽约时报','半岛电视台');

-- 3.18 媒体 media：fr（10 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', m.id, 'media', 'fr',
       CASE m.name
           WHEN '新华社' THEN 'Agence Xinhua'
           WHEN '人民日报' THEN 'Quotidien du Peuple'
           WHEN '中国日报' THEN 'China Daily'
           WHEN '中央广播电视总台' THEN 'Groupe des médias de Chine'
           WHEN '路透社' THEN 'Reuters'
           WHEN '美联社' THEN 'Associated Press'
           WHEN '法新社' THEN 'Agence France-Presse'
           WHEN '英国广播公司' THEN 'BBC'
           WHEN '纽约时报' THEN 'The New York Times'
           WHEN '半岛电视台' THEN 'Al Jazeera'
           ELSE m.name
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_media m
WHERE m.created_by = 'demo-0.2.2'
  AND m.name IN ('新华社','人民日报','中国日报','中央广播电视总台','路透社','美联社','法新社','英国广播公司','纽约时报','半岛电视台');

-- 3.19 书籍 book：en（10 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', b.id, 'book', 'en',
       CASE b.title
           WHEN '知识图谱：方法、实践与应用' THEN 'Knowledge Graph: Methods, Practices and Applications'
           WHEN '人工智能：一种现代方法' THEN 'Artificial Intelligence: A Modern Approach'
           WHEN '深度学习' THEN 'Deep Learning'
           WHEN '语义网基础教程' THEN 'A Semantic Web Primer'
           WHEN '数据密集型应用系统设计' THEN 'Designing Data-Intensive Applications'
           WHEN '知识图谱导论' THEN 'Introduction to Knowledge Graphs'
           WHEN '国富论' THEN 'The Wealth of Nations'
           WHEN '人类简史' THEN 'Sapiens: A Brief History of Humankind'
           WHEN '世界是平的' THEN 'The World Is Flat'
           WHEN '中国大百科全书（第三版）' THEN 'Encyclopedia of China (Third Edition)'
           ELSE b.title
       END,
       'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_book b
WHERE b.created_by = 'demo-0.2.2';

-- 3.20 论文 paper：en（8 行，英文标题本身即英文名称）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', p.id, 'paper', 'en', p.title, 'demo-0.2.2'
FROM dsh_java_siisdb.gwy_ontology_paper p
WHERE p.created_by = 'demo-0.2.2';

-- 3.21 术语 term：en（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'en',
       CASE t.term_name
           WHEN '知识图谱' THEN 'Knowledge Graph'
           WHEN '实体对齐' THEN 'Entity Alignment'
           WHEN '关系抽取' THEN 'Relation Extraction'
           WHEN '本体推理' THEN 'Ontology Reasoning'
           WHEN '语义网' THEN 'Semantic Web'
           WHEN '知识表示' THEN 'Knowledge Representation'
           WHEN '三元组' THEN 'Triple'
           WHEN '实体消歧' THEN 'Entity Disambiguation'
           WHEN '命名实体识别' THEN 'Named Entity Recognition'
           WHEN '本体' THEN 'Ontology'
           WHEN '知识库' THEN 'Knowledge Base'
           WHEN '推理引擎' THEN 'Reasoning Engine'
           WHEN '图数据库' THEN 'Graph Database'
           WHEN '向量嵌入' THEN 'Vector Embedding'
           WHEN '知识融合' THEN 'Knowledge Fusion'
           WHEN '元数据' THEN 'Metadata'
           WHEN '数据治理' THEN 'Data Governance'
           WHEN '模式匹配' THEN 'Schema Matching'
           WHEN '查询扩展' THEN 'Query Expansion'
           WHEN '语义推理' THEN 'Semantic Reasoning'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.22 术语 term：fr（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'fr',
       CASE t.term_name
           WHEN '知识图谱' THEN 'Graphe de connaissances'
           WHEN '实体对齐' THEN 'Alignement d''entités'
           WHEN '关系抽取' THEN 'Extraction de relations'
           WHEN '本体推理' THEN 'Raisonnement ontologique'
           WHEN '语义网' THEN 'Web sémantique'
           WHEN '知识表示' THEN 'Représentation des connaissances'
           WHEN '三元组' THEN 'Triplet'
           WHEN '实体消歧' THEN 'Désambiguïsation d''entités'
           WHEN '命名实体识别' THEN 'Reconnaissance d''entités nommées'
           WHEN '本体' THEN 'Ontologie'
           WHEN '知识库' THEN 'Base de connaissances'
           WHEN '推理引擎' THEN 'Moteur d''inférence'
           WHEN '图数据库' THEN 'Base de données de graphes'
           WHEN '向量嵌入' THEN 'Plongement vectoriel'
           WHEN '知识融合' THEN 'Fusion de connaissances'
           WHEN '元数据' THEN 'Métadonnées'
           WHEN '数据治理' THEN 'Gouvernance des données'
           WHEN '模式匹配' THEN 'Appariement de schémas'
           WHEN '查询扩展' THEN 'Expansion de requête'
           WHEN '语义推理' THEN 'Raisonnement sémantique'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.23 术语 term：ar（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'ar',
       CASE t.term_name
           WHEN '知识图谱' THEN 'رسم بياني للمعرفة'
           WHEN '实体对齐' THEN 'محاذاة الكيانات'
           WHEN '关系抽取' THEN 'استخراج العلاقات'
           WHEN '本体推理' THEN 'الاستدلال الأنطولوجي'
           WHEN '语义网' THEN 'الويب الدلالي'
           WHEN '知识表示' THEN 'تمثيل المعرفة'
           WHEN '三元组' THEN 'ثلاثي'
           WHEN '实体消歧' THEN 'توضيح الكيانات'
           WHEN '命名实体识别' THEN 'التعرف على الكيانات المسماة'
           WHEN '本体' THEN 'أنطولوجيا'
           WHEN '知识库' THEN 'قاعدة معرفة'
           WHEN '推理引擎' THEN 'محرك استدلال'
           WHEN '图数据库' THEN 'قاعدة بيانات الرسم البياني'
           WHEN '向量嵌入' THEN 'تضمين متجهي'
           WHEN '知识融合' THEN 'اندماج المعرفة'
           WHEN '元数据' THEN 'بيانات وصفية'
           WHEN '数据治理' THEN 'حوكمة البيانات'
           WHEN '模式匹配' THEN 'مطابقة المخططات'
           WHEN '查询扩展' THEN 'توسيع الاستعلام'
           WHEN '语义推理' THEN 'الاستدلال الدلالي'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.24 术语 term：ru（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'ru',
       CASE t.term_name
           WHEN '知识图谱' THEN 'Граф знаний'
           WHEN '实体对齐' THEN 'Выравнивание сущностей'
           WHEN '关系抽取' THEN 'Извлечение отношений'
           WHEN '本体推理' THEN 'Онтологический вывод'
           WHEN '语义网' THEN 'Семантическая паутина'
           WHEN '知识表示' THEN 'Представление знаний'
           WHEN '三元组' THEN 'Триплет'
           WHEN '实体消歧' THEN 'Разрешение неоднозначности сущностей'
           WHEN '命名实体识别' THEN 'Распознавание именованных сущностей'
           WHEN '本体' THEN 'Онтология'
           WHEN '知识库' THEN 'База знаний'
           WHEN '推理引擎' THEN 'Механизм логического вывода'
           WHEN '图数据库' THEN 'Графовая база данных'
           WHEN '向量嵌入' THEN 'Векторное встраивание'
           WHEN '知识融合' THEN 'Слияние знаний'
           WHEN '元数据' THEN 'Метаданные'
           WHEN '数据治理' THEN 'Управление данными'
           WHEN '模式匹配' THEN 'Сопоставление схем'
           WHEN '查询扩展' THEN 'Расширение запроса'
           WHEN '语义推理' THEN 'Семантический вывод'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.25 术语 term：es（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'es',
       CASE t.term_name
           WHEN '知识图谱' THEN 'Grafo de conocimiento'
           WHEN '实体对齐' THEN 'Alineación de entidades'
           WHEN '关系抽取' THEN 'Extracción de relaciones'
           WHEN '本体推理' THEN 'Razonamiento ontológico'
           WHEN '语义网' THEN 'Web semántica'
           WHEN '知识表示' THEN 'Representación del conocimiento'
           WHEN '三元组' THEN 'Tripleta'
           WHEN '实体消歧' THEN 'Desambiguación de entidades'
           WHEN '命名实体识别' THEN 'Reconocimiento de entidades nombradas'
           WHEN '本体' THEN 'Ontología'
           WHEN '知识库' THEN 'Base de conocimiento'
           WHEN '推理引擎' THEN 'Motor de inferencia'
           WHEN '图数据库' THEN 'Base de datos de grafos'
           WHEN '向量嵌入' THEN 'Incrustación vectorial'
           WHEN '知识融合' THEN 'Fusión de conocimiento'
           WHEN '元数据' THEN 'Metadatos'
           WHEN '数据治理' THEN 'Gobernanza de datos'
           WHEN '模式匹配' THEN 'Emparejamiento de esquemas'
           WHEN '查询扩展' THEN 'Expansión de consulta'
           WHEN '语义推理' THEN 'Razonamiento semántico'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.26 术语 term：ja（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'ja',
       CASE t.term_name
           WHEN '知识图谱' THEN '知識グラフ'
           WHEN '实体对齐' THEN 'エンティティ・アラインメント'
           WHEN '关系抽取' THEN '関係抽出'
           WHEN '本体推理' THEN 'オントロジー推論'
           WHEN '语义网' THEN 'セマンティック・ウェブ'
           WHEN '知识表示' THEN '知識表現'
           WHEN '三元组' THEN 'トリプル'
           WHEN '实体消歧' THEN '固有表現の曖昧性解消'
           WHEN '命名实体识别' THEN '固有表現抽出'
           WHEN '本体' THEN 'オントロジー'
           WHEN '知识库' THEN '知識ベース'
           WHEN '推理引擎' THEN '推論エンジン'
           WHEN '图数据库' THEN 'グラフデータベース'
           WHEN '向量嵌入' THEN 'ベクトル埋め込み'
           WHEN '知识融合' THEN '知識融合'
           WHEN '元数据' THEN 'メタデータ'
           WHEN '数据治理' THEN 'データガバナンス'
           WHEN '模式匹配' THEN 'スキーママッチング'
           WHEN '查询扩展' THEN 'クエリ拡張'
           WHEN '语义推理' THEN '意味推論'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.27 术语 term：de（20 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', t.id, 'term', 'de',
       CASE t.term_name
           WHEN '知识图谱' THEN 'Wissensgraph'
           WHEN '实体对齐' THEN 'Entitätsausrichtung'
           WHEN '关系抽取' THEN 'Relationsextraktion'
           WHEN '本体推理' THEN 'Ontologie-Reasoning'
           WHEN '语义网' THEN 'Semantisches Web'
           WHEN '知识表示' THEN 'Wissensrepräsentation'
           WHEN '三元组' THEN 'Tripel'
           WHEN '实体消歧' THEN 'Entitätsdisambiguierung'
           WHEN '命名实体识别' THEN 'Eigennamenerkennung'
           WHEN '本体' THEN 'Ontologie'
           WHEN '知识库' THEN 'Wissensbasis'
           WHEN '推理引擎' THEN 'Inferenzmaschine'
           WHEN '图数据库' THEN 'Graphdatenbank'
           WHEN '向量嵌入' THEN 'Vektoreinbettung'
           WHEN '知识融合' THEN 'Wissensfusion'
           WHEN '元数据' THEN 'Metadaten'
           WHEN '数据治理' THEN 'Daten-Governance'
           WHEN '模式匹配' THEN 'Schema-Matching'
           WHEN '查询扩展' THEN 'Query-Erweiterung'
           WHEN '语义推理' THEN 'Semantisches Schließen'
           ELSE t.term_name
       END,
       'demo-0.2.2'
FROM onto_ontology_term t
WHERE t.created_by = 'demo-0.2.2';

-- 3.28 地名 location：en（15 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', l.id, 'location', 'en',
       CASE l.name
           WHEN '北京' THEN 'Beijing'
           WHEN '上海' THEN 'Shanghai'
           WHEN '广州' THEN 'Guangzhou'
           WHEN '深圳' THEN 'Shenzhen'
           WHEN '东京' THEN 'Tokyo'
           WHEN '巴黎' THEN 'Paris'
           WHEN '伦敦' THEN 'London'
           WHEN '纽约' THEN 'New York'
           WHEN '柏林' THEN 'Berlin'
           WHEN '莫斯科' THEN 'Moscow'
           WHEN '新加坡' THEN 'Singapore'
           WHEN '首尔' THEN 'Seoul'
           WHEN '粤港澳大湾区' THEN 'Greater Bay Area'
           WHEN '长三角地区' THEN 'Yangtze River Delta'
           WHEN '京津冀地区' THEN 'Beijing-Tianjin-Hebei Region'
           ELSE l.name
       END,
       'demo-0.2.2'
FROM onto_ontology_location l
WHERE l.created_by = 'demo-0.2.2';

-- 3.29 地名 location：fr（15 行）
INSERT INTO onto_ontology_i18n (domain, entity_id, category, lang, name, created_by)
SELECT 'siis', l.id, 'location', 'fr',
       CASE l.name
           WHEN '北京' THEN 'Pékin'
           WHEN '上海' THEN 'Shanghai'
           WHEN '广州' THEN 'Canton'
           WHEN '深圳' THEN 'Shenzhen'
           WHEN '东京' THEN 'Tokyo'
           WHEN '巴黎' THEN 'Paris'
           WHEN '伦敦' THEN 'Londres'
           WHEN '纽约' THEN 'New York'
           WHEN '柏林' THEN 'Berlin'
           WHEN '莫斯科' THEN 'Moscou'
           WHEN '新加坡' THEN 'Singapour'
           WHEN '首尔' THEN 'Séoul'
           WHEN '粤港澳大湾区' THEN 'Grande Baie'
           WHEN '长三角地区' THEN 'Delta du Yangtsé'
           WHEN '京津冀地区' THEN 'Région Jing-Jin-Ji'
           ELSE l.name
       END,
       'demo-0.2.2'
FROM onto_ontology_location l
WHERE l.created_by = 'demo-0.2.2';

-- =============================================================================
-- 4. 发现实体 onto_common_discovered_entity（12 行）
-- status 均为 DISCOVERED；覆盖 9 类实体，另 3 行为重复类型；tenant_id NOT NULL
-- source_topic_id 为 NULL，避免依赖演示主题数据
-- =============================================================================
INSERT INTO onto_common_discovered_entity
(entity_name, entity_type, source_topic_id, first_seen_time, mention_count, ontology_entity_id, status, tenant_id, created_by) VALUES
('国际可再生能源署',       'organization', NULL, '2026-09-01 00:00:00', 18, NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('全球甲烷承诺',           'hotspot',      NULL, '2026-09-02 00:00:00', 26, NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('世界数据论坛',           'hotspot',      NULL, '2026-09-03 00:00:00', 14, NULL, 'DISCOVERED', 1, 'demo-0.2.2'),
('东帝汶',                 'country',      NULL, '2026-09-04 00:00:00', 12, NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('阿卜杜勒·拉赫曼',        'person',       NULL, '2026-09-05 00:00:00', 9,  NULL, 'DISCOVERED', 1, 'demo-0.2.2'),
('非洲通讯社',             'media',        NULL, '2026-09-06 00:00:00', 7,  NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('人工智能治理白皮书',     'book',         NULL, '2026-09-07 00:00:00', 11, NULL, 'DISCOVERED', 1, 'demo-0.2.2'),
('A Survey on Knowledge Graph Embedding', 'paper', NULL, '2026-09-08 00:00:00', 15, NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('知识图谱嵌入',           'term',         NULL, '2026-09-09 00:00:00', 21, NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('长三角一体化示范区',     'location',     NULL, '2026-09-10 00:00:00', 10, NULL, 'DISCOVERED', 1, 'demo-0.2.2'),
('萨赫勒地区',             'location',     NULL, '2026-09-11 00:00:00', 8,  NULL, 'DISCOVERED', 0, 'demo-0.2.2'),
('全球疫苗免疫联盟',       'organization', NULL, '2026-09-12 00:00:00', 16, NULL, 'DISCOVERED', 0, 'demo-0.2.2');

-- =============================================================================
-- 5. 版本口径说明
-- 本库 onto_ontology_version 的 entity_count 由 demo_ontology_siis.sql 统一回填，
-- 本脚本不再写入，避免 SIIS 业务实体与平台本体实体（term/location）双重计数。
-- 如确需核对本库实体规模，可执行以下只读查询，不要回写 entity_count：
-- SELECT 'term', COUNT(*) FROM onto_ontology_term WHERE created_by='demo-0.2.2';
-- SELECT 'location', COUNT(*) FROM onto_ontology_location WHERE created_by='demo-0.2.2';
-- =============================================================================

-- =============================================================================
-- 6. 执行后校验（可选，人工执行）
-- =============================================================================
-- SELECT category, lang, COUNT(*) AS cnt
-- FROM onto_ontology_i18n
-- WHERE created_by = 'demo-0.2.2'
-- GROUP BY category, lang
-- ORDER BY category, lang;
-- 预期 i18n 总数：224 + 140 + 60 + 20 + 10 + 8 + 140 + 30 = 632
-- SELECT COUNT(*) AS i18n_total FROM onto_ontology_i18n WHERE created_by='demo-0.2.2';
-- SELECT 'discovered', COUNT(*) FROM onto_common_discovered_entity WHERE created_by='demo-0.2.2';
-- 预期：632 / 12
