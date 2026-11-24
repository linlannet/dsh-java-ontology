---
title: 深度拆解Ontology：如何把零散的数据表，翻译成AI能上手的“企业世界”？
subtitle: 数据库记录事实，Ontology描绘世界：企业AI的下一场革命来了
kicker: 三、建模篇：从 Object、Link 到 Action 的企业世界
author: 智能体AI
date: 2026-09-24
tags: [Ontology, 本体, 企业AI]
source: https://mp.weixin.qq.com/s?__biz=Mzg5Mjc3NTg1Mw==&mid=2247492306&idx=1&sn=d861734963fe8ecd24a8543a6fae0122&chksm=c03a5e10f74dd706439b6597b66380b12a69bea67884b5f975d7a17be5e3b85b526cacb0eb7f#rd
theme: azure
---

企业 AI 真正缺的，从来不只是一个更强的模型，而是一个可以『生活』在里面的企业世界。

最近，我和一位制造业朋友探讨。他们刚上线了一套 AI 客服系统，技术栈很完整：大模型、RAG、向量库、企业 API，该有的都有。

『效果怎么样？』我问。

他苦笑：『客户问报销标准，AI 答得挺好。问订单进度，也能查到。但只要问题稍微复杂点，比如「这个客户最近为什么流失了，我们该怎么挽回」，AI 就开始胡说八道。』

『为什么？』

「因为它根本不懂我们公司是怎么运转的。」

这句话点出了一个很多企业都在经历的困境：AI 知道很多，但不懂你这家公司。

它可以从知识库里找到制度，可以调 API 查订单，但当业务负责人问：『这个客户为什么突然流失？现在该做什么把他留下来？』——AI 就卡壳了。

因为它需要同时理解 CRM 里的客户、ERP 里的订单、仓库里的库存、客服系统里的投诉、物流系统里的配送状态，还要知道这些东西之间的关系。更重要的是，它还得知道：谁能处理？什么情况下能处理？该调用哪个业务动作？动作执行后会影响哪些系统？

这时候你会发现：企业 AI 真正缺的，从来不只是一个更强的模型。

它缺的，是一个可以『生活』在里面的企业世界

这正是 Palantir Ontology最有价值的地方。它表面上是在做数据语义层，实际上做的是：把企业的数据、对象、关系、规则、行动、权限和反馈，组织成一个可以被人和 AI 共同理解、查询和操作的业务世界。

📌 本文看点

1. 企业 AI 到底缺什么

2. 对象 · 关系 · 行动

03

企业世界模型

## 企业 AI 到底缺什么

一个简单的例子：客户 Acme

一家制造企业有个客户叫 Acme。数据库里的记录是这样的：

customer_id = 10086

order_id = 88492　status = delayed

warehouse_id = WH-009　status = quality_check

delivery_id = DHL-88492　eta = next Tuesday

ticket_id = T-7788　category = delivery_delay

每个系统的数据都很正常。但业务人员脑子里的世界不是这样的。业务人员看到的是：

Acme 是我们的重要客户。他们最近有笔订单延期了，产品还在汉堡仓库做质检，物流预计下周二才能到上海。客户已经投诉了。如果继续延期，可能影响下季度续约。

同样的底层数据，两种完全不同的世界。

一个是数据世界，另一个是业务世界

这就是企业 AI 经常遇到的问题。过去我们花了大量时间解决『数据在哪里』。现在真正困难的问题变成了：这些数据在企业世界里到底意味着什么？

数据库记录事实，Ontology 描绘世界

数据库就像城市档案室。里面有身份证、房产证、订单、发票、银行流水……每份材料都真实存在。

但如果你第一次来这个城市，只给你一堆档案，你还是不知道这个城市怎么运行。你不知道谁是市长，哪些部门负责什么，哪条路通向哪里，什么事情该找谁，发生事故后怎么处理。

Ontology 做的事情，就像给这个城市画了一张『可运行的地图』。

它不仅告诉你『这里有栋楼』，还告诉你：这栋楼属于哪个公司，哪个公司租用了它，公司有哪些员工，哪些员工负责审批，什么情况下可以审批，审批后会触发什么动作。

「Ontology 不是简单给数据库加标签。它是在数据库之上，重新构建一套企业世界模型。」

## 对象、关系与三层架构

先分清三层：Foundry / Ontology / AIP

理解 Palantir，第一件事不是研究 Object Type，而是分清这三层：

AIP（AI 应用层）

↓

Ontology（企业世界层）

↓

Foundry（数据层）

↓

ERP / CRM / MES / IoT

简单说：Foundry 管数据，Ontology 管企业世界，AIP 让 AI 进入这个世界。

Foundry

数据怎么进来、怎么清洗、怎么治理、怎么计算。

Ontology

这些数据代表什么、它们之间什么关系、可以对它们做什么、谁可以做。

AIP

怎么让 AI、Agent、Copilot 使用这些能力。

三者不是并列关系，而是：数据 → 业务世界 → AI 应用。这也是为什么只把 Palantir 理解成『一个 AI 平台』容易低估它——它真正有意思的地方，是中间这层：Ontology。

把『表』变成『对象』

数据库里有张 Customer 表，包含 customer_id、name、industry、region 等字段。在 Ontology 里，它变成：

Customer 对象

├── id

├── name

├── industry

├── region

├── lifetime_spend

└── risk_score

这个变化看起来不大，但对 AI 来说差别巨大。因为：

「表是数据结构，对象是业务概念。」

人类讨论业务时，从来不会说『请查询 customer_table 中 customer_id=10086 的记录』。人会说：『查一下 Acme。』

所以 Ontology 的第一步，是把数据库世界的 Table → Object Type、Row → Object、Column → Property，重新翻译成业务语言。

真正重要的是『关系』

假设我们已经有了 Customer、Order、Product、Factory、Delivery 这些对象。问题来了：Acme 和哪个订单有关？订单包含什么产品？产品在哪个工厂生产？工厂通过什么物流发货？这时候就需要 Link（关系）：

Customer

↓ placed

Order

↓ contains

Product

↓ produced by

Factory

↓ ships via

Delivery

这张关系网一旦建立，很多原本麻烦的问题突然简单了。比如『Acme 这笔订单为什么延期』，AI 可以沿着关系链查询：Customer → Order → Product → Factory → Delivery，最终发现：产品已生产完成，但质检出现异常，导致出库延期，最终导致物流延期。

这和传统 RAG 的思路有很大区别。RAG 主要在问『哪几段文本可能相关』，Ontology 更关注『这个问题涉及哪些对象？这些对象之间什么关系』。

「RAG 解决『找资料』，Ontology 解决『认世界』。」

## 最关键的知识，不在文档里

我们过去做企业知识库，会放大量内容：制度、规范、FAQ、操作手册、会议纪要、合同……然后做 Embedding、Vector DB、RAG。这当然有价值。但企业里最关键的知识，很多不在文档里。比如：

1. A 客户属于华东区域。

2. A 客户的订单由 B 销售负责。

3. B 销售不能审批超过 100 万的订单。

4. 超过 100 万需要区域总监审批。

5. 订单延期超过 7 天，需要触发客户风险流程。

这些是：对象 + 关系 + 规则。如果只把它们写成文本，AI 每次都要从文本里重新理解关系。而 Ontology 直接把关系显式建模出来。

「RAG 给 AI 一本城市说明书，Ontology 给 AI 一张城市地图，还告诉它哪些路能走、哪些门能进、哪些事谁可以做。」

两者不是互斥的。成熟的企业 AI 很可能是：RAG + Ontology + Agent + Tool Use。

## 真正让 Ontology 不一样的：Action

Action：让企业世界『动起来』

如果说 Object 是名词，Link 是关系，那么：Action 就是动词。这是理解 Palantir 最关键的一步。

企业世界里不只有客户、订单、产品、员工，还有：审批、退货、派单、退款、发货、转交、通知、关闭工单。这些才是业务真正『动起来』的地方。

传统 AI 擅长告诉你该做什么，企业真正需要的是：把事情做掉

AI 客服为什么停在『智能问答』阶段

客户说：『我的订单已经延期三天了，什么时候能发货？』AI 查询后回答：『您的订单目前处于延期状态，预计三天后发货。』

看起来不错。但真实客服可能还会：查询订单 → 查看库存 → 查看物流 → 判断延期原因 → 联系仓库 → 申请加急 → 创建工单 → 通知客户。

如果 AI 只能回答问题，它最多替代第一步。如果 AI 理解 Ontology，它看到的就不只是 Order．status = delayed，而是完整的关系链：Customer → Order → Product → Warehouse → Shipment → Ticket。

同时，它知道有哪些合法的业务动作：Create Ticket、Escalate Order、Notify Customer、Request Expedited Shipment。这时候 Agent 才真正进入业务流程。

Action 不是普通 API 调用

很多人会说：『那不就是让 Agent 调 API 吗？』从技术实现看，确实可以通过 API 调用业务系统。但企业真正需要的不只是 API。

假设有个退款接口 POST /refund。真正的业务规则可能是：

退款规则

退款金额 ＜ 1000　　　客服直接操作

1000 ≤ 金额 ＜ 10000　主管审批

金额 ≥ 10000　　　　　财务审批

特殊客户　　　　　　　必须人工审核

所以一个真正的企业 Action，背后包含：参数 + 规则 + 校验 + 权限 + 副作用 + 审计。

Palantir 把 Action Type 拆成了 Parameters、Rules、Submission Criteria、Side Effects 和 Action Log，特别强调它不是简单的 API 调用，而是一个受治理的业务操作单元。

「Agent 进入企业后，最危险的不是答错题，而是做错动作。」

回答错了，人工还能纠正。退款错了十万，问题就完全不同了。

从『回答正确』到『行动可控』

这是企业 AI 和普通 Chatbot 最大的区别。

两种范式

Chatbot　　问题 → 答案

企业 Agent　问题 → 理解业务对象 → 查询企业状态

→ 分析关系 → 制定方案 → 调用 Action

→ 权限校验 → 人工审批 → 执行 → 记录结果

这已经不是聊天机器人，更像数字员工。但这里的『数字员工』不是让 AI 随便操作。真正成熟的设计是：

AI 建议 → 权限判断 → 规则校验 → Human Approval → Action → Audit

「让 AI 有能力，但不要让 AI 失去边界。」

## Ontology 开始直接连接 Agent

Ontology MCP 正式 GA

2026 年 6 月，Palantir 宣布 Ontology MCP 正式 GA。它把已有的 Ontology 资源——Object Types、Action Types 和 Functions——暴露为 MCP tools。一个支持 MCP 的 Agent，可以直接通过标准协议访问这些企业能力。

更关键的是，Ontology MCP 使用 Foundry 标准 OAuth 2．0 流程，工具调用沿用用户已有的 Foundry 权限。这个变化非常值得关注。

连接方式的变化

过去　Agent → 自己开发 API → ERP / CRM / Database

现在　Agent → MCP → Ontology → Object / Function / Action → 企业系统

Ontology 开始从『企业内部的数据语义层』，变成『Agent 进入企业世界的接口层』。

MCP 真正重要的，不是『工具多了』

过去谈 MCP，容易理解成『给 Agent 增加工具』——数据库是工具，搜索是工具，Git 是工具，ERP 是工具。但企业真正的问题是：工具多了以后，Agent 怎么知道哪个工具可以用？调用时权限怎么办？

比如：Agent → 退款 → 金额 100 万。这时不能简单说『有 refund_tool 就调用』。还必须知道：谁在调用？有没有权限？这个客户是谁？订单是否符合退款条件？是否超过审批额度？是否需要人工确认？执行后怎么审计？

所以 MCP 只是连接协议。真正决定企业 Agent 能否可靠运行的，是：MCP 后面连接的企业世界是什么。

「后面是零散 API，Agent 得到的是一堆工具；后面是完整 Ontology，Agent 得到的是一个有对象、有关系、有规则、有权限、有行动能力的企业世界。」

Agent 开始参与『造系统』

2026 年 7 月，Palantir 推出 Pro-code Agents。这类 Agent 可以读取和写入 Ontology 数据，还能修复构建问题、迁移 Legacy Systems，通过 OSDK、Ontology MCP 和 Palantir MCP 进行受控访问。Agent 开始从使用系统走向构建系统。

这和我们今天经历的 AI Coding 浪潮是同一方向。只不过普通 AI Coding 是『AI 帮你写代码』，而企业级 AI Coding 更进一步：AI 必须理解企业里的对象、关系、权限、规则和现有架构，才能安全地修改系统。

这也是为什么大型企业做 AI Coding，真正难的从来不是『让模型生成 Java 代码』。难的是：AI 到底知不知道这个企业的软件世界怎么运行？

企业 AI Coding 其实是同一个问题

业务提需求：『业扩预受理增加一个校验。』普通 AI Coding Agent 可能很快写出代码，能编译，测试也能跑。但架构师一看：改错地方了。为什么？因为 AI 不知道：

1. 页面入口在哪里。

2. 真正的后端服务边界在哪里。

3. 这个字段来自哪张表。

4. 哪个中心负责这个业务。

5. 这个规则是省级还是全国规则。

6. 哪个服务才是正确的扩展点。

「AI 缺的不是代码知识，而是企业世界知识。」

所以企业 AI Coding 真正需要的 Context，不应该只有 CLAUDE．md、AGENTS．md、Coding Rules，还应该包括：业务对象 + 系统关系 + 服务边界 + 数据模型 + API Contract + 业务规则 + 权限 + 运行时状态。这就是：Enterprise World Model。

## 从知识图谱到企业世界模型

Ontology 与企业知识中枢，走向同一个方向

传统企业知识库是『文档中心』：Word、PDF、Wiki、FAQ → Embedding → Vector DB → RAG。但企业真正需要的可能是：

企业知识中枢

├── Facts（事实）

├── Objects（对象）

├── Relationships（关系）

├── Rules（规则）

├── Actions（行动）

├── Permissions（权限）

├── History（历史）

└── Feedback（反馈）

企业知识不应该只是『知道什么』，还应该包括：谁是谁、什么和什么有关、什么规则约束什么、谁能做什么、做完后发生什么、过去发生过什么。这时候，知识库才真正从 Document Repository 走向 Enterprise World Model。

Ontology 最厉害的地方，不是『语义』

很多人第一次看 Ontology，会觉得：『这不就是知识图谱吗？』确实有相似之处。Object 像实体，Link 像关系，Property 像属性。但真正不同的地方在于：Ontology 不仅描述企业世界，还把企业行动纳入了这个世界。

传统知识图谱告诉你：世界是什么样。Ontology 进一步告诉你：可以 Cancel Order，但只有 Sales Manager 可以操作，金额超过 100 万需要审批，执行后会更新订单状态、通知客户、生成审计记录。

从 Knowledge Graph 走向 Operational Graph——可运行的企业世界

为什么 Action 可能比 Object 更重要

如果只选 Ontology 中一个最值得研究的概念，我会选：Action。因为企业最终创造价值，不是因为『知道』，而是因为：采取了正确行动。

企业经营本质是：发现问题 → 分析问题 → 做出决策 → 采取行动 → 产生结果。

过去与未来

过去　BI 负责发现，人负责决策，系统负责执行

未来　Agent 负责分析 → Human 负责关键决策

→ Action 负责执行 → System 记录结果

这才是企业 Agent 真正可能改变的地方。

## 代价、FDE 与真正的护城河

最大的代价：建模本身非常贵

讲到这里容易把文章写成 Palantir 宣传稿。但真实情况没这么简单。Ontology 最大的代价是：建模本身非常贵。

你要把企业真正建模出来，至少要搞清楚：有哪些业务对象？对象之间什么关系？哪些属性是事实？哪些是计算的？哪些规则需要显式化？哪些动作可以自动执行？谁有权限？哪些必须人工审批？

这不是买个软件就自动完成的。需要：数据工程师、软件工程师、业务专家、架构师、安全团队、AI 工程师，还有大量真正懂业务的人。

「Ontology 最大的成本，可能不是技术成本，而是企业知识建模成本。」

为什么 FDE 非常重要

这又回到一个老问题：FDE 到底为什么重要？因为企业真正复杂的知识，很多没写在数据库里，也没写在文档里，藏在人脑子里。比如：

『这个字段虽然叫 status，但实际只有华东系统在用』

『这个审批接口看起来能调用，但生产环境不能直接调』

『这个规则去年已经改过，只是旧文档还没更新』

『这个客户是特殊客户，不能走普通退款流程』

普通软件工程师很难一次性知道。FDE 的价值就在于：把真实业务世界翻译成工程世界。而 Ontology 做的事情，是进一步把这个工程世界组织成：对象 + 关系 + 规则 + 行动 + 权限。

1. FDE 解决『怎么把真实企业理解清楚』。

2. Ontology 解决『怎么把理解后的企业世界结构化』。

3. Agent 解决『怎么在这个世界里工作』。

三者是一条链。

真正的护城河，也许不是大模型

很多人研究 Palantir，会问：『它的大模型是哪家的？』这不是最重要的问题。因为模型可以换。

2026 年 9 月，Palantir 的 AIP 已经持续接入不同模型，包括 GPT-5．6 系列、Gemini 系列、Claude、Grok，以及其他开放权重模型。这恰恰说明：模型本身可以替换。

但企业的 Customer、Order、Factory、Employee、Action、Permission、Business Rule 不会因为换模型就重新定义。

企业数据 → 企业 Ontology → 企业 Action → 企业权限 → 企业业务流程

模型只是：进入这个世界的一个『大脑』，而不是这个世界本身。

最值得关注的信号：AI 开始自己『进化』

2026 年 9 月 8 日，Palantir 正式发布 AIP Evolve。它可以协调多个 AI FDE Agent，对 AI 系统进行持续优化。用户给它：目标 + 优化指标 + 验证方法 + 约束条件，然后 Agent 去提出修改方案、执行验证、比较结果，最后让人审查并合并变化。

这个方向非常值得关注。因为它意味着：过去的『人 → 开发 AI → AI 工作』，现在逐渐变成：

人 → 定义目标 → Agent 开发 → Agent 验证 → Agent 优化 → 人审核 → 系统演进

AI 开始参与：构建自己工作的系统。真正成熟的企业 AI，不可能永远停留在『问一次，回答一次』。它最终一定要进入：持续运行、持续评估、持续优化。

## Ontology 其实是『企业世界模型』

到这里，可以重新回答：Ontology 到底是什么？如果只按产品定义，它是企业的语义层。如果按工程视角，它是：业务对象 + 关系 + 逻辑 + Action + 权限的统一模型。但如果站在 AI 的角度，我更愿意把它理解成：企业的 World Model。

World Model 是什么？AI 脑子里关于『这个世界怎么运转』的内部地图。

一个人刚入职时，知道公司有 CRM、ERP、OA，但不知道：谁负责什么、哪个数据是真的、哪个流程怎么走、什么情况找谁、哪些事可以自己决定。所以他只能算新员工。

工作几个月后，他慢慢形成了：人 + 组织 + 系统 + 流程 + 规则 + 经验。于是开始像真正的老员工一样工作。

「Ontology 想解决的，就是让 AI 尽可能拥有这样一套：企业世界模型。」

未来企业 AI 的完整架构

我们今天习惯的架构是：用户 → Chatbot → LLM → RAG → 答案。但未来更完整的企业 AI 架构可能是：

User → Agent → Reasoning / Planning → Ontology

→ Objects / Relations / Rules → Action

→ Enterprise Systems / Human Approval

→ Result → Feedback → Ontology

最关键的是最后那条线：Feedback 重新回到企业世界。这意味着 AI 不再只是一次性『问答』，而是：持续参与企业运行。

企业 AI 经历的四次变化

第一阶段：数据智能

Database + BI，解决『企业发生了什么？』

第二阶段：知识智能

Documents + RAG + LLM，解决『企业资料里有什么？』

第三阶段：Agent 智能

LLM + Tools + Workflow，解决『AI 可以帮我做什么？』

第四阶段：企业世界智能

Data + Knowledge + Ontology + Agent + Action + Feedback，解决『AI 能不能真正理解企业怎么运行，并在这个世界里持续工作？』

真正值得关注的，是第四阶段。

『企业 AI 知识库』可能会重新定义

过去建企业知识库，目标通常是：『把公司资料都放进去，让 AI 能回答问题。』这只是第一步。下一阶段更值得关注的问题是：

1. 公司有哪些业务对象？它们之间什么关系？

2. 每个对象当前什么状态？什么规则约束这些对象？

3. 哪些动作可以执行？谁可以执行？

4. 执行后会发生什么？结果如何反馈？

「企业知识中枢不再只是『AI 版网盘』，而开始变成：企业运行的数字镜像。」
