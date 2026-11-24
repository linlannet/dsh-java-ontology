---
title: 从 RAG 到 GraphRAG，再到 Ontology：企业 AI 的演进终于到了“运营层”
subtitle: 当 Ontology 遇上 MCP：外部 Agent 如何进入企业业务世界？
kicker: 五、架构篇：RAG、GraphRAG、MCP 与 Ontology 的边界与选型
author: 智能体AI
date: 2026-09-23
tags: [Ontology, 本体, 企业AI]
source: https://mp.weixin.qq.com/s?__biz=Mzg5Mjc3NTg1Mw==&mid=2247492302&idx=1&sn=b96ae7b398e3c877850a34b1926aacee&chksm=c03a5e0cf74dd71a803d1d2052c0cdb5954e87b39dd9a3e9a29f043fe13f1267d893b6de7adf#rd
theme: azure
---

Ontology 不只描述企业里 有什么 ，更定义 AI 能对企业 做什么 。

过去两年，企业 AI 的焦点在不断迁移。最开始是大模型，接着是 RAG，然后是 Agent、MCP、Skills、Workflow、Multi-Agent。到了 2026 年，一个并不新鲜的概念重新浮出水面：Ontology（本体论）。

很多人第一次听到这个词，会把它理解成知识图谱的另一种说法，或者数据模型的升级版。但如果只停留在这个层面，很难真正理解 Palantir 在做什么。

Palantir 想解决的核心问题不是“怎么把企业数据存得更好”，而是——

「AI 如何理解一个企业，以及理解之后，如何在这个企业里真正做事？」

传统数据库告诉你数据在哪里，RAG 告诉你相关知识在哪里，GraphRAG 告诉你知识之间的关系。而 Ontology 更进一步：它定义企业里真实存在的业务对象是什么，它们的关系如何，谁有权改变它们，以及 AI 可以对它们做什么。

这也是为什么在 Agent 时代，Ontology 值得重新认识。

📌 本文看点

2. Ontology 的六个核心构件

5. Ontology MCP 正式 GA

9. 哪些企业才需要它

## 从“这是什么”到“我能对它做什么”

Ontology 不是 Palantir 发明的。这个概念源自哲学，研究“什么是真实存在的”。后来被引入计算机科学，演化出 RDF、OWL、SPARQL 等技术，最终发展成知识图谱。

传统 Ontology 关注的是名词：Customer、Product、Order、Supplier、Factory。它们的属性是什么，彼此的关系如何。到这一步，已经很像知识图谱了。

但 Palantir 走得更远。它不仅定义企业世界的“名词”，还定义这个世界的动词：Approve、Assign、Cancel、Update、Reallocate、Notify。

这个转变至关重要。企业运营本质上是动态的——每天都在创建订单、修改订单、分配员工、调整库存、更换供应商、审批合同、调整生产计划。一个能支撑 Agent 的企业模型，不能只回答“Customer 是什么”，还要回答“AI 可以对 Customer 做什么”。

Palantir 官方将 Objects、Properties、Links 视为 Ontology 的语义部分，而 Actions、Functions 则是让组织发生变化的动力元素（kinetic elements）。

「Ontology 不只描述企业，还描述企业如何运行。」

## Ontology 的核心构成

拆开来看，Palantir Ontology 由几个核心概念组成：

Object：企业世界里的实体

如 Customer、Supplier、Product、Factory、Order。它们不再只是数据库的一行记录，而是代表真实的业务对象。

Property：对象的特征

Customer 的 name、industry、revenue、riskLevel 等。这些不只是技术字段，而是业务对象的组成部分。当 riskLevel = HIGH 时，意味着这个客户需要进入风险处理流程——数据、业务语义、业务动作就连接起来了。

Link：对象之间的关系

企业真正的复杂性往往不在对象本身，而在关系网络。一个供应商出问题，可能影响零件、产品、订单、客户——这条链路的可见性，才是企业真正关心的。

Action：决定 AI 是只能看，还是能做

如果只能从 Ontology 里选一个最值得关注的概念，我会选这个。因为它决定 AI 是只能看，还是能做。

传统系统发现异常后生成报警、通知人员，接下来全靠人工处理。而 Ontology 的思路是把业务操作定义成受治理的 Action。当供应商评级下降时，系统可以触发分析、寻找备选方案、计算影响、生成调整方案、提交审批、执行 Action、更新业务系统。

关键不在于“AI 自动化”，而在于：AI 的行为被映射成企业定义好的业务动作。这和让 LLM 随便调用 update_database 完全不是一回事。前者是企业定义了 AI 能做什么，后者只是开发人员给了 AI 一个数据库接口。

Function：把复杂计算纳入业务模型

很多企业逻辑需要复杂计算，如客户信用评分、库存安全水平、供应风险、生产能力。Ontology 中的 Function 可以把这些计算逻辑纳入业务模型，形成一个闭环：

Object → Function → Decision → Action

Interface：更清晰的抽象方式

大型企业的业务对象很多。Car、Truck、Motorcycle、Bus 看起来不同，但可能都具有 Vehicle 的共同特征。Interface 提供了更清晰的抽象方式，避免出现什么都有、却没人知道它到底代表什么的 God Object。

从建模角度看，Object、Property、Link、Interface 定义企业世界长什么样；Function、Action 定义企业世界如何变化。两部分合起来，才接近一个真正的运营模型。

## Ontology 不等于知识图谱

两者确实有关系，但简单画等号会把 Ontology 的价值讲窄。

知识图谱关注：Entity + Relationship + Knowledge。它擅长告诉你“这些东西有什么关系”。

Ontology 更进一步关注：Entity + Relationship + Business Logic + Action + Security + Governance。它要回答“这些东西在企业运营里是什么，以及企业允许对它们做什么”。

「Palantir 官方把 Ontology 称为组织的运营层（operational layer），而不仅仅是知识表示层。」

## 从 RAG 到 GraphRAG，再到 Ontology

这条技术路线值得放在一起看。

传统 RAG 架构是 Document → Chunk → Embedding → Vector Database → Retriever → LLM，它解决“相关信息在哪里”。

但企业问题往往不是“哪段文字和我的问题最相似”，而是“A 发生变化会影响哪些 B、C、D”。于是 GraphRAG 出现，把 Entity + Relationship 加入检索和推理过程，支持更复杂的多跳关联。

可企业真正走到 Agent 阶段后，又会遇到新问题：知道关系之后，然后呢？AI 发现 Supplier A 供应风险上升会影响 Product B、订单 C、客户 D——很好，但企业真正想问的是：怎么办？

RAG 找到知识 → GraphRAG 理解关系 → Ontology 理解业务世界 → Agent 做出决策 → Action 执行决策

所以 GraphRAG 和 Ontology 不是替代关系。RAG 是知识入口，GraphRAG 是关系推理，Ontology 是业务语义与运营模型。它们可以同时存在，各自解决不同问题。

## 2026 年的关键变化：Ontology MCP

如果说过去几年 Palantir Ontology 的价值主要在 Foundry／AIP 内部，那么 2026 年出现的变化值得高度关注：Ontology MCP 正式 GA。

「外部 Agent 可以通过 MCP 直接使用企业 Ontology。」

Palantir 官方文档显示，Ontology MCP 把 Object Types、Action Types 和 Query Functions 暴露成 MCP tools，让外部 AI Agent 读取 Ontology 数据、执行预定义 Actions、运行查询函数，同时沿用 Foundry 的权限体系。

过去的架构是 Agent → Custom API → Foundry → Ontology，现在则变成：

Claude／OpenAI Agent／Other Agent → MCP → Ontology MCP → Ontology → Enterprise Systems

Ontology 的角色发生了变化。它不再只是 Palantir 平台内部的一层能力，而是可以被外部 Agent 消费的企业语义和业务操作层。

Ontology MCP ≠ Palantir MCP

这里有个容易混淆的地方。Ontology MCP 面向 Ontology Consumer，即那些想使用企业业务数据和业务动作的 Agent，重点是“使用业务世界”。

Palantir MCP 面向 Ontology Builder，即开发人员和 AI Coding Agent，更多用于构建 Ontology、修改 Object Types、Link Types、Action Types、操作数据集、开发应用，重点是“构建业务世界”。

！别混淆两个 MCP 🕳

让 Agent 使用企业业务，和让 Agent 修改企业业务模型，是两件完全不同的事情。这个区分非常重要。

## MCP + Ontology：一种新的 Agent 架构

过去谈 Agent，习惯画：Model → Tools → External Systems。

但企业 Agent 真正进入生产后，架构可能更像：

Model → Agent → MCP → Enterprise Ontology → ERP／CRM／MES／WMS

这时候，MCP 解决“怎么连接”，Ontology 解决“连接以后，AI 看到的企业世界是什么”。

「Ontology 正在成为 Agent 的“企业上下文”。」

聊天机器人需要的 Context 是 Conversation History + Documents + User Query。企业 Agent 需要的是 Customer + Order + Product + Supplier + Inventory + Employee + Permissions + Business Rules + Current State。这不是简单的 Prompt Context，而是 Enterprise Context。

Ontology 提供了一种结构化表达企业 Context 的方式。从这个角度看，Agent + Tools 最终可能演变成 Agent + Ontology + MCP + Actions。

## 一个供应链案例

假设你是汽车制造企业，某车型连续出现交付延迟。

传统流程是：销售发现延期 → 找计划部门 → 查 MES → 查 ERP → 查供应链系统 → 开会 → 人工判断。系统很多，数据很多，但信息是碎的。

有 Ontology 后会发生什么？首先，建立业务模型：Vehicle、Battery、Supplier、Factory、ProductionLine、Order、Customer，以及它们之间的关系链。

Agent 收到问题“为什么这批车交付延期”后，可以沿着业务关系查下去：

Order → Vehicle → Battery → Supplier

最终发现某供应商的关键电池组件供应不足。继续查：

Supplier → Inventory → Production Capacity → Alternative Supplier

Agent 找到三个备选方案，通过 Function 计算成本变化、交付变化、产能变化、风险变化，给出建议：“将未来两周 20% 的采购量转移到 Supplier B”。

这时进入 Human Approval。负责人确认后，执行 Action：修改采购分配 → 更新业务状态 → 触发相关流程 → 留下审计记录。

「这才是真正有价值的企业 Agent——不是“AI 帮我查了一下资料”，而是“AI 理解了企业当前状态，提出了业务决策，在权限允许的情况下执行了动作”。」

## 企业 Agent 不能只靠“大模型 + Tools”

现在常见的 Agent 架构是：LLM + Tools = Agent。这个公式没错，但对复杂企业环境来说不完整。

Tools 只告诉 Agent“你能调用什么”，却没有完整回答“你正在操作的到底是什么世界”。

给 Agent 一个 update_customer 工具，它知道可以更新客户。但哪个客户？为什么更新？更新哪个字段？什么状态可以改？谁允许改？改完会影响什么？是否需要审批？这些问题都不是普通 Tool Description 能解决的。

真正的 Enterprise Agent 更像：Model（负责思考）+ Context（告诉它现在发生了什么）+ Tools（让它能调用外部能力）+ Ontology（让它理解企业世界）+ Harness（让 Agent 稳定运行）+ Governance（限制它应该怎么做）。

「模型能力只是 Agent 的上限，企业语义和工程体系决定了 Agent 的下限。」

## 所有企业都需要 Ontology 吗？

当然不是。Ontology 是一种很重的企业能力。

如果需求只是查询订单，SQL 就够了。搜索内部文档，RAG 就够了。分析文档里的人物和事件关系，GraphRAG 可能更合适。做 BI 报表，数据仓库 + BI 可以解决。

！别当成标准答案 🕳

千万别把 Ontology 当成所有企业 AI 的标准答案。真正值得考虑 Ontology 的场景，通常有下面这些特点。

1. 业务对象非常复杂。制造业有工厂、产线、设备、产品、物料、供应商、订单、客户；金融有客户、账户、交易、机构、产品、合同、风险事件；医疗有患者、医生、科室、药物、诊断、治疗、检查。当业务对象越来越多、关系越来越密时，简单的数据表越来越难表达业务世界。

2. 数据孤岛影响决策。企业最典型的问题不是没有数据，而是数据太多且彼此不认识。ERP、采购系统、供应链平台、财务系统都有 Supplier，但它们是同一个吗？Ontology 的价值是让企业围绕统一的业务对象建立统一语义。

3. AI 不只回答问题，还要执行动作。如果企业 AI 只是“帮我总结”，Ontology 的必要性不高。但如果变成“发现问题后帮我处理”——修改订单、调整库存、分配任务、创建工单、触发审批——那么权限、业务语义、Action、审计全部变得重要。

4. 需要强治理和审计。企业真正害怕的不是 AI“说错一句话”，而是“做错一件事”。尤其在金融、医疗、制药、制造、能源领域，如果 AI 修改了业务数据，企业必须知道：谁、什么时候、修改了什么、为什么、依据是什么、通过什么权限、产生了什么影响。

「企业 Agent 的核心问题，最终会从“AI 能不能做”变成“AI 在什么边界内可以做”。」

## Palantir 真正值得研究的是什么

Palantir 的真正护城河不是 Ontology 本身。Object、Graph、Knowledge、AI 这些技术都不是 Palantir 独有的。

真正困难的是：把 Data + Ontology + Business Logic + Security + Application + Agent + Action + Deployment 组合成一个能长期运行的企业系统。

Palantir 的价值不是“我有一个很厉害的知识图谱”，而是形成了一个闭环：

企业数据 → 统一业务语义 → Ontology → 业务逻辑 → AI／Agent → 决策 → Action → 企业系统 → 真实世界

「这是一个 Data → Understand → Decide → Act → Feedback 的完整循环。」

传统企业 IT 是：系统 → 数据 → 报表 → 人 → 决策 → 人再去操作系统。Agent 真正想改变的，就是中间这条链。

## 2026 年最大的变化：闭环开始向外开放

Ontology MCP 正式 GA，意味着外部 AI Agent 可以通过 MCP 读取 Ontology Objects、查询数据、执行预定义 Action、调用 Query Functions，同时受配置好的权限约束。

未来的企业 Agent 不一定非要全部运行在 Palantir 自己的环境里。它可以是 Claude Agent、OpenAI Agent、Google Agent、LangChain Agent、企业自研 Agent，然后通过 MCP 接入 Ontology。

「企业 Ontology 开始成为 Agent 世界可以直接消费的基础设施。」

未来的架构可能越来越像：AI Agent → MCP → Enterprise Ontology（包含 Objects、Relationships、Business Logic、Actions、Permissions、Governance）→ Enterprise Systems。

这个架构有个重要变化：Agent 不再直接面对几十个企业系统，它首先面对的是企业业务世界。然后由 Ontology 把这个业务世界和底层系统连接起来。

一句话概括这套分工：MCP 是连接协议，Ontology 是业务语义，Agent 是决策者，Action 是执行机制，Governance 是边界。

## 企业 AI 下一阶段的方向

过去大家讨论哪个模型更强，后来讨论哪个 Agent 更聪明，再后来讨论 MCP 能不能成为 Agent 的标准接口。这些问题都重要，但如果把视野放到企业里，还缺了一层：Agent 到底在理解什么？

如果它理解的只是一堆 PDF、数据库、API、Tool Description，那么它仍然只是一个非常聪明的“外部操作者”。

而如果它理解的是 Customer、Order、Product、Supplier、Factory、Contract、Risk、Employee，以及这些对象之间的关系、状态、权限和业务动作，那么它开始真正进入企业业务世界。

「企业 AI 的下一阶段，不只是让 Agent 更聪明，而是让 Agent 更懂企业。」

## 一个比技术更重要的问题

很多企业现在做 AI，第一反应是“要不要上大模型”“要不要做 RAG”“是不是应该做 Agent”。但真正进入生产后，企业一定会碰到一个更难的问题：AI 到底应该如何理解我们这家公司？

你的客户是什么？订单是什么？产品是什么？供应商和产品是什么关系？什么叫高风险客户？什么叫异常订单？什么动作可以自动执行？什么动作必须审批？谁拥有权限？一个动作发生后，谁承担责任？

这些问题看起来不像 AI 问题，实际上恰恰是企业 Agent 最核心的问题。

模型负责思考，Ontology 负责定义世界，Tools 负责连接世界，Actions 负责改变世界，Governance 决定 AI 能改变多少。

「这可能才是 Palantir Ontology 在 Agent 时代真正值得研究的地方。」

## 从 RAG 到 Ontology：一条清晰的演进路线

如果把过去几年企业 AI 的变化压缩成一条线：

1. RAG——让 AI 找到知识。

2. GraphRAG——让 AI 理解知识之间的关系。

3. Agent——让 AI 自主完成任务。

4. MCP——让 Agent 更容易连接外部世界。

5. Ontology——让 Agent 理解企业世界。

6. Action——让 Agent 在企业世界里执行。

7. Governance——让这一切变得可控。

真正值得关注的不是“Ontology 会不会替代 RAG”或“Ontology 会不会替代知识图谱”，而是：当 Agent 开始从“回答问题”走向“参与企业运营”时，它需要什么样的世界模型？

EPILOGUE

## 总结

过去十年，企业数字化解决了一个问题：把现实世界搬进了计算机。数据库记录客户，ERP 记录订单，MES 记录生产，CRM 记录销售，IoT 记录设备。

但下一阶段的问题完全不同：怎么让 AI 真正理解这个数字化的世界，并且在规则允许的范围内参与其中？

RAG 让 AI 找到知识，GraphRAG 让 AI 理解关系，Agent 让 AI 开始行动。而 Ontology 试图把企业本身变成一个 AI 可以理解、推理、操作，而且受到治理的数字世界。

如果用一句话解释 Palantir Ontology：它是企业对自身业务世界的一次正式建模——不仅定义“企业里有什么”，还定义“这些东西是什么关系、谁能改变它们，以及 AI 可以对它们做什么”。

当 Ontology 开始通过 MCP 向外部 Agent 开放后，这件事变得更加值得关注。

「Agent 正在从“调用企业工具”，走向“进入企业世界”。」

而这，可能才是企业 AI 下一阶段真正值得看的地方。
