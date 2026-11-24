---
title: Ontology 工程化实战：别再把 Ontology 当知识图谱，真正难的是让 AI 安全地"动起来"
subtitle: 别再把Ontology当知识图谱了！Palantir的核心底牌全在这四层架构里
kicker: 四、落地篇：数据映射、语义路由与受控 Action
author: 智能体AI
date: 2026-10-14
tags: [Ontology, 本体, 企业AI]
source: https://mp.weixin.qq.com/s?__biz=Mzg5Mjc3NTg1Mw==&mid=2247492432&idx=1&sn=c8dc4d8caf297e4a7e36738629e67612&chksm=c03a5f92f74dd6845d18a49766972330774adb4fd34cc51faa6fe053d90e6dbd2cc7dfa350f1#rd
theme: azure
---

企业 AI 最难的，已经不是模型会不会回答，而是 AI 能不能在真实的业务世界里，安全地把事办了。

上周和一个做安全的朋友探讨，他说了句话我记到现在：

「我们现在最怕的不是 AI 说错话，是 AI 把工单真的分错了。」

这句话背后，其实是这两年企业 AI 项目走过的一条清晰路径：先解决“怎么接入大模型”，再解决“怎么做 RAG”，再解决“怎么做 Agent”。现在越来越多项目进了生产环境，撞上了一个更麻烦的坎——AI 知道了以后，到底怎么让它真正去做事。

举个真实场景。安全团队跟 AI 说：“把所有超过 7 天还没有责任人的高危漏洞，分配给对应系统负责人。”

听着是句大白话，真要落地，问题一个接一个冒出来：“高危”按 CVSS≥7 算，还是按企业自己的 P0/P1 规则？“没有责任人”是 owner 字段为空，还是工单状态显示“未分配”？“张三”到底对应哪个员工，公司里叫张三的有仨？漏洞数据是不是最新的？AI 有没有权限改这个工单，改之前要不要审批？业务系统写成功了，图数据库没同步上怎么办？AI 要是选错了对象，谁拦得住它？万一过程没留全日志，出了事怎么查？

说白了，企业 AI 现在最难的已经不是“模型会不会回答”，而是AI 能不能在真实的业务世界里，安全地把事办了。

这也是为什么这两年 Palantir 的 Ontology 越来越值得研究。但在往下讲之前，必须先纠正一个几乎所有人都会踩的误解：Ontology 不是知识图谱换了个名字。

Palantir 官方文档把 Ontology 定义成组织的 operational layer（运行层）——它不只包含 Object、Property、Link 这些语义元素，还包含 Action、Function、动态安全策略这些“动力学”能力。用 Palantir 自己的话说，Ontology 是对企业 Data、Logic、Action、Security 的整合。

今天不讲“Ontology 是什么”这种科普文，直接讲：如果你的团队真打算走这条路，到底该怎么把它工程化。

📌 本文看点

1. Ontology 四层拆解

2. LLM 只能“申请”执行

3. 先跑通一个业务闭环

## 有图、有 RAG、有 Agent，为什么还是不够？

设想一家大型企业，CRM、ERP、工单系统、CMDB、数据仓库、数据湖、元数据平台、知识库、RAG、Agent 平台、MCP——技术组件几乎一应俱全。但这些东西堆在一起的真实样子，往往是这样的：

上面是 Agent，中间是 RAG 和 Workflow，再往下，缺了一整层，然后才是 CRM/ERP/MES 这些底层数据库。

这缺的一层，其实只干三件事。

第一，把技术数据翻译成业务世界。数据库里存的是 cust_id、prod_cd、wo_status、cvss_score，业务人员说的是客户、产品、工单、责任人、漏洞风险。AI 光认识字段名不够，它得知道“漏洞”是什么、漏洞和资产是什么关系、一个漏洞为什么会产生工单、责任人为什么属于某个团队——这就是语义建模。

第二，把业务语义变成可计算的能力。AI 不该自己去猜 SQL 怎么写，它应该看到的是 AssignWorkOrder、FindHighRiskVulnerabilities这样的接口。换句话说，AI 调用的不是数据库，而是企业的业务能力。

第三，把“能执行”变成“受控执行”。AI 说“我要把这个工单分给张三”，不等于它真能执行。参数合不合法？对象存不存在？当前用户和 AI 有没有权限？状态允不允许改？要不要人工审批？谁发起的、最终改了什么？

所以真正的链路应该是：

自然语言 → LLM → 业务意图 → Ontology → Function/Action → Policy/Permission → 业务规则 → 执行 → 审计

这和“LLM+Tool Calling”，已经是两个完全不同的工程层次了。

## Ontology 和知识图谱，到底差在哪儿？

这一步理解错了，后面的架构基本全走偏。

知识图谱擅长的是组织关系——张三负责系统 A，系统 A 存在漏洞 V001，这类三元组关联。

GraphRAG 擅长的是从非结构化知识里挖关系。微软目前的 GraphRAG 实现，本质是从原始文本里抽取实体、关系和 claims，再构建社区层级和摘要，查询时支持 Local Search、Global Search、DRIFT Search 等不同方式。这类工具特别适合处理合同、报告、威胁情报这类堆积如山的非结构化文档，比如“这几份安全报告里，哪些攻击组织经常盯着同一类基础设施打”。

Ontology 关心的是企业世界怎么运行。它的核心对象是 Customer、Order、WorkOrder、Vulnerability 这些企业真正认可的业务实体，更重要的是，它还要知道 CreateOrder、AssignWorkOrder、EscalateVulnerability 这些“动作”能干什么。

「GraphRAG 关心知识怎么关联，Ontology 关心业务世界怎么运行。」

两者不冲突，成熟的企业 AI 系统完全可以并存——GraphRAG 补充知识，Ontology 支撑操作，两条线最后汇入同一个 Agent。

## 把 Ontology 拆成四层来看

只用“语义层”三个字解释 Ontology，是不够的，因为语义层只能回答“这是什么”。企业真正要的是：它是什么、它和谁有关系、能对它做什么、谁能做、什么条件下能做、做完之后发生了什么。

从工程角度，Ontology 其实是四层叠起来的：

1. Data 层：ERP/CRM/MES/DB 这些原始系统。

2. Semantic 层：Object/Property/Link，把数据变成业务人员能看懂的名词。

3. Action/Logic 层：Function 负责算，Action 负责改变世界。

4. Security 层：权限、策略、审计。

Palantir 现在的官方架构，已经明确把 Ontology 描述成 Data、Logic、Action、Security 的四重整合，而且特别强调它是用来表示企业里的复杂决策，而不只是数据本身。这个变化很关键——Ontology 的目标已经不是把数据整理得更好看，而是把企业的决策过程本身建模出来。

## 别推倒重来，让 Ontology 长在现有数据之上

这是企业真正落地时最容易忽略、却最重要的一条经验。

千万别一上来就说“我们要建一套全新的 Ontology 数据平台”，然后新建图数据库、新建存储、新建同步系统、新建数据治理、新建权限系统。这么干的项目，往往两年过去了业务价值还没冒头。

更现实的做法是：让 Ontology 尽量映射已有的数据资产。Palantir 官方也明确说过，Ontology 是长在已有数字资产之上的，把数据集、模型这类资产映射成 Object Type、Property、Link Type、Action Type。

比如原来数据库里那张普通的 work_order 表，可以逐渐长成：Object Type 是 WorkOrder，Properties 是 title/status/createdAt，Links 连到 Asset 和 Person，Function 能 FindOpenWorkOrders、CalculateRisk，Action 能 AssignWorkOrder、CloseWorkOrder。

原来的业务系统不用消失，数据库也不用消失——Ontology 只是在它们之上加了一层“企业业务语义和执行能力”。

## 名词和动词：Object、Link，然后是 Function、Action

Ontology 最基础的部分，仍然是 Object、Property、Link，可以类比成面向对象编程：

面向对象编程

Ontology 对应

Class（类）

Object Type

Field（字段）

Property

Reference（引用）

Link

读方法

Function

写方法

Action

这一步在做的事情，其实是把数据库里的“行和列”，翻译成业务人员和 AI 都能看懂的“对象和关系”。但很多知识图谱项目最后没能产生业务价值，问题就出在只做到这一步——Customer、Order、Product 建得再漂亮，如果 AI 只能查 Customer→Order，它依然只是“知道”，不能“做”。

企业真正需要的是 CreateOrder、ApproveOrder、AssignEmployee 这些动词。Object 是企业的名词，Action 是企业的动词，这是 Ontology 和传统知识图谱最大的分野之一。

那 Function 和 Action 又有什么区别？可以简单记成：

「Function 负责怎么算，Action 负责怎么改变世界。」

CalculateVulnerabilityRisk 是 Function，输入 CVSS 和资产重要性，算出一个风险等级，纯计算。但 EscalateVulnerability 是 Action，执行完可能改状态、建工单、发通知、写审计日志——这已经是真实的业务副作用了。工程上可以这么分工：Function 管查询、计算、评估、推荐；Action 管创建、修改、分配、审批、触发流程。

## LLM 只能“申请”，平台才能“执行”

这是整个 Ontology 工程化里，最值得企业刻在墙上的一条原则。

用户说“把这个漏洞分给张三”，LLM 可能吐出一段看起来完全正确的 JSON。但 JSON 格式对了，不代表可以直接执行。它必须继续走完这条链路：

LLM 生成 Action Request → Schema 校验 → 权限检查 → 前置条件 → 业务规则 → 人工审批 → 执行 → 审计

LLM 在这里的定位很明确：它是意图解析器和计划生成器，不是生产系统的最终执行者。原因也很朴素——大模型天生带概率性，但业务系统不能靠概率工作。AI 可以判断“我认为用户想调用 AssignWorkOrder”，至于到底能不能调，得平台说了算。

这里有个类比我觉得特别贴切：可以把 Schema Validator 理解成 AI 世界里的编译器。传统编程里，源代码经过编译器做语法检查、类型检查才能生成机器码；AI Agent 里，自然语言经过 LLM 生成的“中间代码”，也得经过 Validator 一层层把关——类型对不对、参数对不对、关系合不合法、状态允不允许、权限够不够、业务规则满不满足。全部通过才能执行。这样即便模型偶尔犯糊涂，也有一道够厚的安全墙兜着。

而且校验结果不该只有“通过”和“拒绝”两种。生产系统建议至少分四档：

档位

含义与场景

PASS

直接执行

WARN

候选责任人不止一个，提示用户确认

CLARIFY

公司里张三有仨，不要瞎猜，直接反问“你说的是研发中心的张三还是安全中心的张三”

REJECT

普通 Agent 想执行关闭财务账户这种操作，直接拒

这一步，才是把 AI Agent 从“聪明的聊天机器人”真正变成一套企业软件系统的关键。

## 业务系统才是“事实”，Ontology 只是它的投影

很多团队做完图数据库之后，很容易冒出一个念头：“以后所有东西都写进图里。”这个想法风险极大。

ERP 才是真正的订单系统，CRM 才是真正的客户系统，工单系统才是真正的工单系统——这些才是 System of Record。Ontology 更适合承担统一语义、关联关系、业务逻辑、AI 访问编排这些活。可以简单记成：

System of Record → Ontology → AI / Workflow / Application

由此还能推出一条很反直觉但很关键的原则：如果 AI 要改工单，先写业务系统，业务系统确认成功，再同步到 Ontology；反过来才危险——万一图写成功了、业务系统写失败了，就会出现“图里说张三负责，工单系统里查却是没人负责”这种幽灵数据。如果同步失败，也不该回滚真实的业务动作，而是丢进补偿队列，重试、最终一致。

一致性也不必一刀切。历史订单、标签、统计数据这类，允许最终一致，延迟几分钟通常不影响决策；但库存、余额、审批状态、工单当前状态这类，必须执行前实时读取，一旦过期就可能引发错误执行。实操中可以给属性打个类似 consistencyLevel的标记，比如 WorkOrder.status 设成 STRONG_ON_READ，createdAt 设成 EVENTUAL——这样既控制了风险，又不至于把所有查询都变成实时 API 调用。

## 子图别按数据源切，按业务闭环切

很多团队第一反应是按系统划子图：CRM 子图、ERP 子图、MES 子图、IoT 子图……看着很整齐，但 Agent 一问复杂问题就傻了。

比如“这个延期订单为什么还没交付”，答案可能横跨 Order、Supplier、Inventory、Production、Logistics，这些数据分散在 ERP、MES、WMS、TMS、CRM 好几个系统里。按数据源切子图，Agent 回答一个问题就得跨好几个子图查，子图也就失去了意义。

更合理的方式是按业务闭环切：安全运营子图管 Asset/Vulnerability/WorkOrder/Person，能回答“上周新发现的高危漏洞里有多少还没责任人”；供应链子图管 Supplier/PurchaseOrder/Inventory，能回答“哪些供应商的订单有延期风险”。数据源是技术边界，子图应该是业务问题的边界。

！踩坑提示 🕳

子图还有一层价值容易被忽略：权限边界。Security Agent 只访问安全运营子图，供应链 Agent 只访问供应链子图，这本身就是一种天然的 Context 隔离和 Access 隔离。但要提醒一句——子图不是完整的权限系统，生产环境照样得有用户权限、对象级权限、字段级权限、Action 权限、审计，一个都不能少。

## 别把整个 Ontology 一股脑塞给 LLM

假设企业有 1000 个 Object Type、5000 个 Property、3000 个 Link、1000 个 Action，有人建议“把 Schema 全塞进 System Prompt 里”——demo 阶段或许行得通，一到生产环境很快就会撞上：Context 太长、检索效率变差、成本飙升、无关信息互相干扰、Agent 选错 Action。

更合理的架构是做一条语义路由链：

User Query → Intent Router → Ontology Retriever → Context Assembler → Planner → Schema Validator → Execution

具体怎么做？用户问“最近有哪些高危漏洞还没人处理”，第一步不是把全部 Ontology 甩给模型，而是先判断这是个 READ 请求，检索出相关的 Object、相关 Property、相关 Link，最后给模型一个很小的上下文——模型面对的是“当前问题相关的那片业务世界”，而不是“企业所有业务知识的大杂烩”。

这里有个容易被低估的坑：语义检索真正难的，不是选 Milvus 还是 Qdrant，而是语义描述本身写得好不好。比如 severity 如果只写“漏洞严重等级”，用户问“有哪些 P0 漏洞”，召回效果大概率不理想；但如果语义扩展成“漏洞严重程度、危险等级、高危、严重、P0、P1、Critical、CVSS、紧急漏洞”，召回质量会完全不一样。

「Schema 解决机器怎么理解结构，Semantic Description 解决 AI 怎么找到结构，二者缺一不可。」

所以每一个 Ontology 元素，都该有自己的 Semantic Description。这其实也是企业 AI 的一种新型知识资产，跟以前的数据字典、指标体系是同一个量级的东西。

## GraphRAG 和 Ontology，从来不是二选一

现在很多企业在纠结“我们到底该做 GraphRAG 还是做 Ontology”，这个问题本身就问错了。应该先问：你的知识从哪儿来？

如果大量知识来自 PDF、合同、邮件、技术文档，GraphRAG 很有价值——微软目前的 GraphRAG 已经支持 Local、Global、DRIFT 等多种查询模式，本质是在非结构化文本上建结构化知识和社区摘要。如果你的知识来自 ERP、CRM、工单系统这类结构化数据库，而且 AI 最终要改变这些系统里的状态，那 Ontology-driven 架构就更值得投入。两者完全可以并存：Knowledge Plane（GraphRAG）和 Operational Plane（Ontology）分头供给，最后一起汇入决策和执行。

MCP 放在哪儿？到了 2026 年，MCP 已经越来越成为 Agent 对接外部工具的标准方式，但要分清楚：

「MCP 解决 AI 怎么发现和调用能力，Ontology 解决企业世界是什么、哪些能力可以被调用。」

比较合理的架构是 LLM 通过 MCP 暴露的 find_vulnerabilities、assign_work_order 这类工具，这些工具背后真正调用的是 Ontology 的业务能力，再往下才是 ERP、CRM 这些执行系统。MCP 是连接层，Ontology 是业务语义层，底层系统才是真正干活的——这三层千万别混在一起。

## 把 Ontology 当软件工程，不是当一份配置文档

这是很多项目能不能真正走进生产的分水岭。如果 Ontology 只是“数据建模团队手里的一份配置”，它很快就会失控。真正成熟的做法应该像软件工程一样走一整套流程：

定义 → Git → 版本 → Review → 测试 → CI/CD → 部署

改一个 Object、一条 Rule，都该有版本记录、代码评审、自动化测试、变更日志、可回滚。原因很简单：Ontology 一旦开始驱动 Action，它就不再是单纯的数据模型了，它已经变成企业 AI 的运行时契约。

具体到测试怎么写，可以拆成几类：

1. Object 测试：输入 V001，预期 severity=HIGH。

2. Link 测试：V001 应该关联到 W001。

3. Function 测试：给定 CVSS=9.2，预期 Risk=Critical。

4. Action 测试：执行分配后，WorkOrder.owner 应该变成对应的人。

5. 权限测试：普通客服账号尝试关闭财务账户，必须被拒绝。

这样以后 AI 出了错，你能精确定位到底是模型错了、Ontology 建模错了、Function 写错了、权限配错了，还是数据本身错了——而不是所有锅最后都甩给一句“可能是模型幻觉”。

而 Action，才是真正的企业 AI 安全边界。一个生产级的 Action，不该只是一个 POST 接口，它至少得包含输入 Schema、前置条件、权限校验、业务规则、执行逻辑、副作用、审计、结果。可以记成一个公式：

Action = API + Policy + Business Rule + Audit

这也是企业 Agent 和普通 Tool Calling 之间最大的差别——传统 Agent 喜欢直接暴露 SQL、REST、Shell 给模型，Demo 阶段很方便，生产环境风险极大，因为模型看到的是 UPDATE、DELETE 这类技术原语，不是业务语义。更合理的方式，是让模型调用的始终是 AssignWorkOrder、ApproveOrder 这类业务能力，而不是技术接口。这个变化看起来只是 API 设计上的调整，背后其实是整个企业软件架构思路的转变。

## 别一上来就想建“企业全量 Ontology”

这是落地时最重要的一条经验，没有之一。千万别项目第一天就喊“我们要建设企业级 Ontology”，这句话太大了，很快就会陷入全集团统一建模、数据标准争论、图数据库选型这些泥潭，半年过去业务人员都不知道项目到底解决了什么问题。

更现实的做法：先选一个完整的业务闭环。比如安全漏洞处置，第一阶段可以小到只做四个 Object（Asset、Vulnerability、WorkOrder、Person）、几条 Link、两三个 Function（FindHighRiskVulnerabilities、CalculateRisk）、一两个 Action（AssignWorkOrder、EscalateVulnerability），Agent 只解决三个问题：

“有哪些高危漏洞？哪些没有责任人？把它们分配给对应负责人。”这样已经足够跑出一个真正的闭环，业务价值也立刻能看见。

## 完整案例：AI 自动处理高危漏洞是怎么跑通的

把前面讲的全部串起来看一遍。用户说：“把所有超过 7 天还没有责任人的高危漏洞，分配给对应系统负责人。”

系统先判断这是个写操作，检索出相关的 Object、Property、Link，然后调用 FindHighRiskVulnerabilities()拿到候选清单，按 severity、age、owner 过滤出真正符合条件的漏洞，再顺着 Vulnerability→Asset→Owner 的链路，找到每个漏洞对应的负责人，生成一批 Action Plan。

接下来是一整套校验：这些对象是不是都存在、负责人是不是真的对应这个资产、当前 Agent 有没有分配权限、漏洞当前状态是不是仍然是 OPEN（这一步特别关键——不能因为刚才查到过，就假设现在还是这个状态，万一查询之后已经被别人处理了呢）。如果企业规定高危漏洞可以自动分配，就直接执行；如果规定 Critical 漏洞必须人工确认，就转入审批。

最后才是真正调用 AssignVulnerability 执行，并且完整记录下谁、什么时候、通过什么 Agent、改了什么、结果如何。这一整套流程走完，才算真正完成一次“AI 办事”。

## 往前一步：先模拟，再执行

如果 AI 已经能执行了，下一个问题很快就会冒出来：“能不能先让 AI 跑一遍，别直接改生产数据？”这个需求其实非常朴素也非常关键。

比如“如果把这 500 个订单重新分配给这几个仓库，会发生什么”，正确的做法不是直接执行，而是先进入一个隔离的 Scenario，让 AI 在里面做模拟，评估结果，人工确认没问题了，再受控地合并回主 Ontology。Palantir 现在的 Ontology 文档里已经提供了这样的 Scenario 机制，可以在隔离环境里测试对象状态变化和 Action，再把通过审批的变化合并进主线。

对供应链调度、生产排程、资源分配这类高风险决策，“先模拟再执行”几乎是必须的一步。

## 真正难的从来不是技术，是把业务问明白

聊了这么多架构和工程细节，但 Ontology 工程里最难啦的一块，其实是建模——而且不是画 ER 图的那种建模，是真正把业务问透。

什么叫订单？不同系统里定义可能完全不一样。什么叫客户？CRM 里一个说法，财务系统里可能又是另一套。什么叫高危漏洞？安全团队有自己的标准，审计团队可能还有另一套。谁是真正的责任人？组织架构上是一个人，业务上可能是一个团队，系统里说不定还挂着一个服务账号。

Ontology 建模真正在做的事，是把企业里那些散落在各种系统、表格、流程、甚至只存在于老员工脑子里的业务知识，一点点显性地提炼出来。这也是为什么“既懂业务又懂技术”的人会变得这么稀缺。

如果真要动手，建议按四步走：

1. 先确定要让 AI 完成的那一件具体的事，而不是先问“企业有哪些对象”。

2. 再去摸清每个 Object 的数据骨架，搞清楚谁是真正的主数据源、哪些只是副本、不同系统的 ID 怎么映射（这一步没做扎实，后面很容易出现“同一个客户三个对象”的荒唐事）。

3. 然后先搭静态结构（Object、Property、Link），再上动态能力（Function、Action），别一上来就设计几十个 Action。

4. 最后每一步都用测试验证，跑通了再进生产。

## 几条红线，与一道分水岭

最后想强调的几条红线：

1. LLM 不能直接访问生产数据库，只能通过业务能力和 Policy 去改东西。

2. 图数据库不能变成事实主库，业务系统才是。

3. 生产实例数据不能无条件塞进 LLM 上下文，尤其是客户信息、身份信息这类敏感字段。

4. Ontology 不能一上来就做大而全，先跑通一个业务域再说。

很多人第一次听说 Ontology，脑子里想到的是 Object 连 Link 画成一张图。但真正进了生产环境你会发现，这远远不够。完整的 Ontology 应该是一个闭环：从企业世界里的对象和关系，走向意义，走向可计算的能力，走向受权限约束的执行，走向结果，再走向反馈，反过来完善 Ontology 本身。

Palantir 现在对 Ontology 的描述，也早就从单纯的数据语义模型，延伸成了“企业决策系统”——把数据、逻辑、动作、安全统一起来，让人和 AI Agent 能在同一个业务世界里协作。

所以如果一定要用一句话概括今天讲的这些：

「Ontology 不是把企业数据画成一张图，而是把企业世界变成一个 AI 能理解、能推理、能调用，并且在权限和规则约束下能安全改变的运行环境。」

这也许就是企业 AI 从“会回答问题”走向“真正参与业务”的那道分水岭。
