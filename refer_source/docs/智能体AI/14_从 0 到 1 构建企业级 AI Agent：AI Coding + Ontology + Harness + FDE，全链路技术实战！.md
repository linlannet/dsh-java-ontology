---
title: 从 0 到 1 构建企业级 AI Agent：AI Coding + Ontology + Harness + FDE，全链路技术实战！
subtitle: FDE 正在改变 AI 软件交付！从业务需求、AI Coding 到 Ontology 和生产级 Agent，一篇讲透
kicker: 四、落地篇：数据映射、语义路由与受控 Action
author: 智能体AI
date: 2026-11-23
tags: [Ontology, 本体, 企业AI]
source: https://mp.weixin.qq.com/s?__biz=Mzg5Mjc3NTg1Mw==&mid=2247493371&idx=1&sn=984f5f9b0be86bd0bd4aa19990a91b49&chksm=c03a5a39f74dd32f2704f5a26f993a86c049f1c72c217a1310814b9c0f7ca9a8cb0cd9b1d005#rd
theme: azure
---

用户问“订单到哪了”，查一下物流就能答；可一旦说“帮我把这笔订单退掉，钱原路退回”，Agent 就得跨订单、支付、物流、售后一路做下去。企业级 Agent 的分水岭，从来不在能不能回答问题，而在能不能把一句话，安全地做成一笔真实业务操作。

用户问“我的订单到哪了”，它查一下物流接口就能答。用户问“这个商品能不能退”，它把退货政策检索一遍，也能生成一段像样的解释。可用户一旦说“帮我把这笔订单退掉，钱原路退回”，事情立刻变复杂。系统得先确认这是谁，找到对应订单，查支付记录和物流状态，看商品符不符合退货条件，算出该退多少，确认有没有操作权限，最后调到售后系统把申请提交出去。这中间任何一步出错，都是真实的业务损失。

企业级 Agent 要面对的，正是这一段。

过去一年我一直在看企业 AI Agent 的技术架构，重点放在 AI Coding、Ontology 和 Harness Engineering 这几块。把它们摆到一起研究，会冒出一个我想认真谈的问题，企业到底要一套什么样的工程体系，才能让 Agent 从一个能演示的 Demo，变成真能处理业务的生产系统。

我拿电商行业当贯穿案例，从一个智能售后 Agent 讲起，再慢慢扩到商品、库存、订单和营销。

整篇分成三段。第一段用 AI Coding 把电商 Agent 搭出来。第二段做 Ontology 建模，让 Agent 看懂商品、订单、库存和售后之间的业务关系。第三段讲 FDE 工程交付，靠 Agent Runtime、Harness Engineering 和 Evaluation，把 Agent 送进生产环境。

📌 本文看点

1. 用 AI Coding 从 0 搭出售后 Agent

2. Ontology 让 Agent 看懂业务关系

3. Harness + FDE 把 Agent 送进生产

## 先看业务，电商企业为什么需要 FDE

先摆一个实际的业务问题。

一家电商企业想做一套智能售后系统。每天有大量用户来问订单、物流、退货和退款。原来的客服系统主要靠关键词匹配和知识库检索，简单问题还能应付，一旦牵扯到多个业务系统，就得人工客服接手。

比如用户说，“我的订单 A1024 已经收到货了，商品有质量问题，我想退货，能不能直接帮我申请退款？”

客服要把这一单办完，得从头做到尾。

1. 根据用户身份找到对应订单

2. 检查订单是否已经完成支付

3. 查询商品和 SKU 信息

4. 获取物流签收记录

5. 查询这件商品适用的退货政策

6. 检查是否已经存在售后申请

7. 按规则判断能不能退

8. 创建售后申请，必要时转人工审核

这一串横跨订单中心、商品中心、物流系统、支付系统和售后系统。

要是按传统软件开发的路子走，通常是产品经理先梳理需求，架构师设计系统，开发写接口，测试验证业务流程，最后再做系统集成。每个环节都得花时间。

FDE 到底是什么

FDE 全称 Forward Deployed Engineer，一般译成前线部署工程师。

这个概念跟 Palantir 的工程实践关系很近。Palantir 在招聘和岗位介绍里，把 Forward Deployed Engineering 描述成一种深入客户实际业务环境、围绕客户问题构建解决方案的工程工作方式。我比较认同其中一个思路，工程师得真正理解客户在解决什么问题，并且对最终交付的业务结果负责。

FDE 的活通常横跨好几个阶段。

工作阶段

主要工作

业务调研

理解电商业务流程、用户角色和实际痛点

需求拆解

把售后需求拆成可以开发的功能

架构设计

设计 Agent、数据、工具和业务系统架构

应用开发

用 AI Coding 构建前后端应用

系统集成

对接商品、订单、支付、物流等系统

测试评估

验证业务逻辑、工具调用和执行结果

生产交付

部署、监控、处理异常并持续迭代

FDE 没有统一的技术标准，也不是某个现成的产品或开发框架。它更像一种贴着业务结果走的工程角色，同一个人从调研一直管到交付。

FDE 这个词最近热起来，跟 AI Coding 有直接关系。过去工程师要自己写大量代码，现在 Claude Code、Codex 这类 Coding Agent 已经能参与编写代码、分析项目、调试和测试。熟悉业务的人因此能更快做出原型，一个小团队也能扛起过去要好几个角色配合的活。

问题也跟着来了。AI 能生成代码，不代表它懂企业的业务规则。它可以帮你把退款接口写出来，却不知道什么情况下才该允许退款；它能生成订单查询的 SQL，却未必分得清哪个字段才是业务上真正的订单号。

AI Coding 把实现速度提上去了，需求定义和交付质量反而变得更要紧。

所以 FDE 的活还是绕不开业务分析、架构设计和工程验证。

## 第一步，用 AI Coding 从 0 到 1 搭出电商 Agent

先不碰复杂的 Multi-Agent，也不急着上 Ontology。先做出一个能跑起来的电商智能售后 Agent。

把业务需求拆成 Agent 能力

头一件事是想清楚 Agent 到底要干什么。电商售后业务可以拆成这些功能。

业务功能

Agent 要完成的任务

对应业务工具

订单查询

查询订单金额、状态和明细

query_order

物流查询

查询包裹和签收状态

query_logistics

商品查询

获取商品和 SKU 信息

query_product

退款查询

查询退款金额和进度

query_refund

退货申请

检查条件并创建退货申请

create_return

人工转接

整理上下文并创建客服工单

create_ticket

每个工具都该有明确的输入、输出和权限要求。订单查询工具可以写成这样。

def query_order(order_id: str, customer_id: str):

"""

根据客户身份查询订单。

"""

return order_service.get_order(

order_id=order_id,

customer_id=customer_id

)

！踩坑提示 🕳

用户输入订单编号，不代表他就有权看这笔订单。后端必须拿已认证的客户身份去做数据访问控制，不能直接相信模型传进来的 customer_id。退款工具也一样，不能因为模型判断用户想退款，就直接动资金。

工具负责执行确定性的业务动作，权限和业务校验必须交给可信的后端服务来做。

设计技术架构

一个简单的电商售后 Agent，可以用下面这套技术组合。

技术模块

推荐技术

作用

前端

React + Vite

用户交互界面

UI

Tailwind CSS

页面样式

后端

Python + FastAPI

业务服务

实时通信

WebSocket

对话与任务状态更新

大模型

Qwen / DeepSeek

意图理解和任务规划

Agent

LangChain 或自定义实现

工具调用与任务组织

数据库

PostgreSQL

会话、任务及业务记录

AI Coding

Claude Code / Codex

辅助开发与测试

这套栈只是一个可选的参考实现，不要求每家企业都用同样的组合。如果企业已经有 Java 微服务、Spring Boot 和成熟的订单系统，完全可以保留原来的后端，只在上层加一个 Agent 服务。架构应该去适应企业现有的系统，别为了用上 AI 把所有业务重写一遍。

用 Claude Code 或 Codex 开发

接下来可以让 Coding Agent 参与整个开发过程。但我不建议只丢给它一句“帮我开发一个电商智能客服”。应该先把需求说明、接口定义和项目结构准备好，再把任务拆开。比如这样一份说明。

项目名称：Ecommerce Agent

业务目标：

构建一个能够处理电商售后业务的 AI Agent。

核心功能：

1. 查询客户订单。

2. 查询物流状态。

3. 查询退款进度。

4. 判断订单是否符合退货条件。

5. 创建退货申请。

6. 将复杂问题转交人工客服。

技术要求：

Frontend:

React + Vite + Tailwind CSS

Backend:

Python + FastAPI

Database:

PostgreSQL

Agent:

LLM + Tool Calling

工程要求：

1. 前后端分离。

2. 业务工具独立封装。

3. 查询操作与资金操作分离。

4. 所有敏感操作必须经过权限检查。

5. 退款操作支持幂等控制。

6. 为核心业务流程编写测试。

7. 记录工具调用、执行状态和异常信息。

然后让 Coding Agent 分阶段来做。先生成前端聊天界面和会话管理模块，再开发 FastAPI 后端和模型调用接口，接着封装订单、物流、退款和退货工具，然后实现 Agent 的工具选择和执行逻辑，最后编写测试跑通完整的售后流程。

这里我特别想留一个工程习惯，每做完一个模块就跑一次测试。别让 AI 一口气生成整个项目，最后才启动。真出了问题，你很难判断到底是接口、业务逻辑还是模型调用出的错。

AI Coding 的效率，最后还是落在任务拆分、上下文管理和验证方式上。

## 第二步，引入 Ontology，让 AI 看懂电商业务

到这儿，已经有一个能调用业务 API 的 Agent 了。但业务一复杂，新问题很快冒出来。

商品、订单、库存、支付和物流数据分散在不同系统里，每个系统都有自己的表结构、字段和业务口径。比如商品中心用 sku_code 标识商品规格，订单系统用 sku_id，库存系统可能又用自己的一套库存编码。

对开发人员来说，这些可以靠 SQL、接口和映射表解决。但对 Agent 来说，它得先知道这些数据代表什么，彼此之间又是什么关系。这就轮到 Ontology 了。

什么是电商 Ontology

Ontology 中文一般叫本体。在企业业务建模里，可以把它理解成一套对业务对象、属性、关系和业务语义的统一定义。

先问电商企业有哪些核心业务对象。

Customer，客户

Product，商品

SKU，商品销售规格

Order，订单

OrderItem，订单明细

Payment，支付记录

Inventory，库存

Shipment，物流包裹

Return，退货申请

Refund，退款记录

这些对象之间也有关系。Customer 拥有 Order，Order 包含 OrderItem，OrderItem 对应 SKU，SKU 关联 Inventory，Order 关联 Payment 和 Shipment，Return 关联订单里的商品，Refund 关联退款业务。把对象和关系组织起来，就是一套电商业务知识模型。

构建电商核心业务模型

可以先搭出下面这套基础结构。

Ontology Object

核心属性

业务关系

Customer

customer_id

、等级、状态

拥有订单

Product

product_id

、名称、类目

包含 SKU

SKU

sku_id

、规格、价格

对应商品、库存

Order

order_id

、金额、状态

包含订单明细

OrderItem

数量、成交价

关联 SKU

Payment

支付金额、支付状态

对应订单

Inventory

可用库存、锁定库存

对应 SKU、仓库

Shipment

运单号、物流状态

对应订单

Return

退货原因、申请状态

关联订单明细

Refund

退款金额、退款状态

关联订单及售后记录

对象之间的关系画出来大概是这样。

Customer

│

│ owns

▼

Order

│

├── hasItem ──> OrderItem

│ │

│ ▼

│ SKU

│ │

│ ▼

│ Inventory

│

├── hasPayment ──> Payment

│

├── hasShipment ─> Shipment

│

└── hasReturn ───> Return

│

▼

Refund

这套模型解决的是业务对象之间的语义关系。Agent 查询订单 A1024 时，就能顺着它找到订单明细、商品规格、支付记录、物流信息和售后记录。

当然，实际建模还要处理订单拆分、部分发货、部分退款、多包裹、多仓库这些情况。不能简单假定一个订单只对应一个 SKU、一条物流记录或者一笔退款。

从关系型数据库到 RDF 知识图谱

如果想把 Ontology 进一步落成可查询的知识图谱，可以用 RDF。RDF 的核心数据结构是三元组，每个三元组由三部分组成，主语 Subject、谓语 Predicate、宾语 Object。

比如这几条。

Customer-001

owns

Order-A1024

contains

OrderItem-001

references

SKU-1001

belongsTo

Product-001

Order-A1024

hasShipment

Shipment-001

这些三元组连起来，就形成一个可以查询的图结构。下面用 Turtle 风格写一段简单的 RDF 数据。

@prefix ex: <http://example.org/> .

ex:Customer001

a ex:Customer ;

ex:ownsOrder ex:OrderA1024 .

ex:OrderA1024

a ex:Order ;

ex:hasItem ex:OrderItem001 ;

ex:hasShipment ex:Shipment001 .

ex:OrderItem001

a ex:OrderItem ;

ex:referencesSKU ex:SKU1001 .

ex:SKU1001

a ex:SKU ;

ex:belongsTo ex:Product001 .

这段数据表达了客户、订单、订单明细、SKU 和商品之间的关系。RDF 适合表达跨系统的业务事实，同一个业务对象可以出现在多个三元组里，连成一张相互勾连的图。

OWL，给业务模型明确的语义

有了 RDF，还要进一步定义业务概念，这时可以用 OWL。OWL 2 是 W3C 定义的本体描述语言，能定义 Class、Property、Individual 这些概念，还能靠形式化语义支持逻辑推理。

比如这样一组定义。

Class:

Customer

Product

SKU

Order

OrderItem

Return

Refund

ObjectProperty:

ownsOrder

containsItem

referencesSKU

belongsTo

hasReturn

hasRefund

DataProperty:

orderAmount

orderStatus

skuPrice

refundAmount

还可以再加一些约束，比如 OrderItem 必须关联一个 SKU，SKU 属于某个 Product，Return 必须关联对应的订单商品，Refund 需要关联具体的退款业务记录。

有一点要分清。OWL 擅长表达概念关系、类型约束和逻辑语义，但订单是否超过退款期限、退款金额有没有超过实付金额这类动态业务判断，通常还得靠业务规则引擎或者应用程序来执行。这两类规则应该分开设计。

SPARQL，查询订单背后的业务关系

业务模型建起来以后，就能用 SPARQL 查询知识图谱了。比如查订单 A1024 包含哪些 SKU，又关联了哪条物流记录。

PREFIX ex: <http://example.org/>

SELECT ?sku ?shipment

WHERE {

ex:OrderA1024

ex:hasItem ?item .

?item

ex:referencesSKU ?sku .

ex:OrderA1024

ex:hasShipment ?shipment .

}

SPARQL 能沿着 RDF 图里的关系查数据，也支持更复杂的图模式和联合查询。三者的分工可以这样记。

技术

解决的问题

RDF

用三元组描述业务事实

OWL

定义业务概念、关系和逻辑语义

SPARQL

查询和操作 RDF 图数据

如果企业只是做一个简单的电商 Agent，不一定马上要引入完整的 RDF 和 OWL 技术栈。但业务对象越来越多、系统之间的语义映射越来越复杂时，Ontology 和知识图谱的价值就显出来了。

## 用 AI 辅助构建 Ontology，从业务资料生成知识模型

实际项目里，很少有企业一开始就有完整的 Ontology。更多时候，业务知识散落在数据库、Excel、接口文档、产品说明和员工经验里。FDE 可以先让 AI 帮忙把这些材料整理出来。

比如把下面这些资料交给 AI。

商品中心数据库表结构

订单系统字段说明

库存和仓库数据

支付和退款接口文档

物流状态定义

售后政策和业务流程

让 AI 先提取出候选的对象和关系。但有个问题必须处理。假设订单表里有个字段 order_status = 3，AI 能认出它是一个订单状态字段，却没法凭空知道数字 3 在这家企业里到底代表什么。它可能是已发货，也可能是已完成，具体含义必须拿企业的数据字典和业务定义来确认。

所以 Ontology 建模可以分成五步走。

1. 明确业务问题。先确定 Agent 要回答什么，比如订单 A1024 符不符合退货条件。

2. 识别业务对象。分析这个问题牵扯到 Customer、Order、OrderItem、SKU、Shipment 和 Return 这些对象。

3. 建立对象关系。定义订单怎么关联客户、商品、物流和售后记录。

4. 映射真实数据。把 ERP、订单中心、商品中心和售后系统的数据映射到统一对象上。这一步要重点解决跨系统 ID 对不上的问题，可以靠业务主键、映射表和数据治理规则建立统一身份，不能只凭商品名称一样就当成同一个商品。

5. 验证本体。设计一组业务问题，看模型能不能答对。比如这些问题。

哪些客户买过商品 SKU-1001

订单 A1024 包含哪些商品

这个 SKU 对应哪个商品

这个商品在哪些仓库有库存

订单是否已经签收

订单是否已经存在退货申请

有没有对应的退款记录

这些问题也叫 Competency Questions，业务能力问题，能帮团队检查 Ontology 到底覆盖了没有覆盖实际业务需求。

工具选择上，可以先用 Python RDFLib 操作 RDF 数据，再根据数据规模和查询需求考虑 Apache Jena、Fuseki 或者其他图数据平台。AI 也能参与模型草拟、字段映射和关系检查，但业务定义还是得业务人员来拍板。

5. ONTOLOGY + AGENT

## Ontology 加 AI Agent，从查询知识到执行业务

到这里，电商业务模型已经有了。接下来把 Ontology 接进 Agent。这一段是整套架构里我最想讲清楚的部分。

传统 RAG 主要解决文档检索。比如用户问“平台的退货政策是什么”，系统检索一遍退货政策文档，再让大模型生成答案就行了。可用户要是问“我的订单 A1024 能不能退货”，系统要处理的信息就多了，得查订单、支付记录、商品类别、签收时间、售后状态，再结合适用的退货政策来判断。这种时候，Ontology 能帮 Agent 找到相关的业务对象和关系。

Agent 怎么用 Ontology

可以设计一个统一的 Ontology 查询服务。

def get_order_context(order_id: str):

"""

查询订单相关的业务上下文。

"""

order = order_service.get_order(order_id)

items = order_service.get_order_items(order_id)

shipment = logistics_service.get_shipments(order_id)

payment = payment_service.get_payments(order_id)

returns = aftersale_service.get_returns(order_id)

return {

"order": order,

"items": items,

"shipment": shipment,

"payment": payment,

"returns": returns

}

这里为了好懂，用了多个业务服务组合查询。生产环境还可以进一步用一个 Ontology 查询层，把对象访问和关系遍历统一起来。这样 Agent 只要调一个工具，就能拿到订单相关的上下文。

执行流程大概是这样。

用户提出问题

│

▼

Agent 意图识别

│

▼

选择业务查询工具

│

▼

Ontology Query Service

│

▼

查询 Customer / Order / SKU

│

▼

获取支付、物流、售后信息

│

▼

业务规则引擎

│

▼

返回判断结果

│

▼

Agent 组织回答

│

▼

必要时创建售后申请

这里有三个模块要分清各自干什么。Ontology 定义业务对象、属性和关系，业务系统提供真实的业务数据，Agent 负责理解任务、调用工具和组织执行过程。至于需要确定性判断的规则，交给业务规则引擎来跑。

实战，订单 A1024 能不能退货

假设用户问，“我的订单 A1024 可以退货吗？”系统会查到这样一组演示数据。

{

"order_id": "A1024",

"order_status": "COMPLETED",

"payment_status": "PAID",

"sku_id": "SKU-1001",

"delivery_status": "SIGNED",

"return_status": "NOT_APPLIED",

"refund_status": "NONE"

}

这段是为了讲清流程编的示例数据。拿到它以后，Agent 要接着做几件事。

先查订单，确认订单存在，而且当前用户有权访问。再查订单明细，找到 SKU-1001，拿到商品类别、规格和属性。接着查物流，确认商品有没有签收，签收时间是什么时候。然后查售后记录，看有没有已经提交或者正在处理的退货申请。

查完这些，就该执行退货规则了。商家可以配置这样一段规则。

def check_return_eligibility(order, product, policy):

if order.payment_status != "PAID":

return False, "订单尚未完成支付"

if order.delivery_status != "SIGNED":

return False, "订单尚未签收"

if not policy.is_returnable(product):

return False, "商品不符合退货政策"

if policy.is_expired(order):

return False, "已超过适用退货期限"

if order.has_pending_return:

return False, "订单已有退货申请"

return True, "满足当前退货申请条件"

这里的 is_returnable 和 is_expired 应该由正式的业务政策服务来实现，不能让模型自己推测。商家的规则还得符合适用的消费者权益保护法规，不能拿内部规则排除掉法定权利。

检查通过，Agent 就可以调 create_return 工具创建退货申请。要是牵扯到退款金额、特殊商品或者争议订单，就按企业的审批规则再往下走。

！踩坑提示 🕳

假设 Agent 调了退款接口，结果网络超时了，它根本没法确定这笔退款到底成没成功。这时候直接重调，就可能重复退款。退款工具因此得用幂等键、业务流水号和执行状态查询，保证同一笔业务不会因为重复请求而动好几次钱。

走到这一步，Agent 才算真的把用户的一句话，一路做成了一个业务操作。

## 从单 Agent 到 Multi-Agent，搭一个电商智能运营系统

售后只是电商业务的一部分。等企业开始把 AI 用到商品、营销、库存和订单管理上，单个 Agent 就会背越来越多的活。

比如运营人员说，“帮我看看本周可能缺货的重点商品，再给一份补货建议。”

这个任务同时牵扯商品、订单和库存三个业务领域。可以设计一个 Supervisor Agent 负责拆任务，再分给各个专业 Agent。

Agent

主要职责

Product Agent

商品信息、SKU 属性、商品质量检查

Marketing Agent

活动策划、优惠券和促销规则

Inventory Agent

库存监控、缺货预警和补货建议

Order Agent

订单查询、销售趋势和履约异常

Logistics Agent

物流状态和配送异常

After-Sales Agent

退货、退款和客户投诉

执行过程可以这样设计。

运营人员提出任务

│

▼

Supervisor Agent

│

▼

任务拆解

│

┌────┼────┐

▼ ▼ ▼

商品 订单 库存

Agent Agent Agent

│ │ │

▼ ▼ ▼

商品 销售 库存

数据 数据 数据

└────┼────┘

▼

综合分析

│

▼

生成补货建议

│

▼

预算与规则校验

│

▼

人工审批

│

▼

采购系统

这里有个设计原则要守住。每个 Agent 都该有明确的业务职责和工具权限。商品 Agent 不需要退款权限，库存 Agent 也不该直接去动支付系统。Supervisor 管任务分配和结果汇总，专业 Agent 各干各的领域。Ontology 则给不同 Agent 提供统一的业务对象和关系，比如商品 Agent 和库存 Agent 都会访问 SKU-1001，只有靠统一的 SKU 标识和业务定义，两个 Agent 才能确认彼此说的是同一个商品规格。

Multi-Agent 的价值来自合理的专业分工。Agent 一多，协调成本也跟着涨，该不该拆，要看任务到底有多复杂。

## Harness Engineering 加 Agent Runtime，解决生产环境的执行问题

到这儿，整个系统已经具备业务理解、数据查询、工具调用和多 Agent 协作的能力了。可生产环境还有一堆问题等着处理。API 超时以后怎么恢复？Agent 调错了订单怎么办？多个 Agent 同时改库存怎么办？模型重复调退款工具怎么办？执行任务时上下文丢了怎么办？用户没权限时怎么把操作拦下来？这些都得靠工程体系来兜底。

什么是 Harness Engineering

Harness 可以理解成围绕 Agent 建起来的一套执行管理机制。模型负责理解和推理，Harness 负责管住模型的整个执行过程，从拿上下文、选工具，到执行任务、观察结果，再到确认任务是不是真的完成了。

放到电商场景里，Harness 的主要职责可以拆成这些模块。

模块

核心职责

Goal

定义任务目标和完成条件

Context

提供订单、商品、客户等业务上下文

Planning

拆解任务和安排执行顺序

Tools

管理业务 API、MCP 工具

Execution

管理任务执行和工具调用

Observation

获取业务系统返回结果

Verification

验证业务结果是否符合要求

Recovery

处理超时、重试和异常恢复

Safety

权限校验、风险控制和审批

Evaluation

评估任务成功率和业务效果

不同项目的 Harness 结构会有差别，但核心都是让 Agent 的执行过程有边界、有状态、有验证。

Agent Runtime 又是什么

Harness 和 Agent Runtime 经常一起出现，但两者关注的不是一回事。Agent Runtime 是 Agent 的运行环境，管的是 Agent 实例、任务生命周期、状态、工具调用和运行资源。Harness 更关注怎么约束和组织 Agent 的执行过程。

打个不太严谨的比方，Runtime 负责让 Agent 跑起来，Harness 负责让 Agent 按预期把任务做完。一笔退款任务进系统以后，Runtime 创建任务实例、维护状态、调度执行，Harness 则检查上下文、选工具、验权限、处理异常、确认执行结果。

电商退款 Agent 怎么走完一整圈

一笔退款任务在 Harness 里走完整圈，大概是这个样子。

Goal

完成用户退款申请

│

▼

Context

获取客户和订单上下文

│

▼

Planning

制定退款处理计划

│

▼

Tool Calling

查询支付和售后记录

│

▼

Observation

读取业务系统返回结果

│

▼

Verification

检查订单状态、金额和规则

│

▼

Safety

校验用户权限和审批要求

│

▼

Execution

调用退款业务接口

│

▼

Verification

再次查询退款执行结果

│

▼

Finish

记录结果并反馈用户

工具调用要是失败了，Harness 得按错误类型分开处理。查询接口超时，可以做有限次数的重试。订单不存在，就该终止任务，返回一个明确的提示。权限不足，直接拒绝执行。退款请求超时，得先查原来那笔请求的状态，不能直接重复提交。退款金额对不上，就暂停任务交给人工。

这套处理要结合业务系统的事务、幂等和状态管理机制。模型可以参与判断和规划，但最后的安全约束必须由程序来执行。

## FDE 怎么把电商 AI Agent 从 Demo 交付到生产

把前面这些技术拼起来，就能谈一谈完整的工程交付流程了。我更建议把整个项目分成四个阶段。

阶段一，需求分析与快速原型

FDE 先要跟业务人员把目标谈定。比如电商企业想降低人工客服处理订单和售后问题的工作量，这时候还得再问细一点。哪些问题可以让 AI 独立处理？哪些只允许查询、不能执行？哪些操作必须人工审批？哪些业务系统愿意开放 API？哪些数据有权限限制？

问清楚以后，就用 Claude Code 或 Codex 快速搭一个能跑的原型。这一阶段要交付业务需求说明、核心业务流程、技术架构设计、Agent 原型和初步测试结果。

原型应该尽早交给业务人员去体验。他们能直接指出订单查得准不准、退货流程符不符合实际、转人工的时候要保留哪些信息。这些反馈都要进到下一轮开发任务里。

阶段二，用 SDD 和 TDD 立工程规范

进入正式开发，可以把 SDD 和 TDD 结合起来。SDD 是 Specification-Driven Development，规格驱动开发，先把需求转成明确的功能规格，再拆成开发任务。比如退款模块要定义清楚这些。

功能：申请退款

输入：

order_id

customer_id

refund_reason

前置条件：

订单存在

客户具有访问权限

订单满足退款政策

业务约束：

退款金额不能超过可退金额

不能重复执行同一退款请求

特殊情况需要人工审批

输出：

refund_id

refund_status

refund_amount

然后再上 TDD，也就是 Test-Driven Development，测试驱动开发，先写测试用例，再实现业务逻辑。比如这些场景。

测试场景

预期结果

正常订单申请退款

进入退款处理

订单不存在

拒绝申请

用户无权限

拒绝访问

退款金额超限

阻止执行

重复退款请求

返回原业务结果或拒绝重复执行

支付接口超时

查询原请求状态后恢复处理

AI Coding Agent 可以照着规格和测试任务帮忙写代码。但测试通过以后，还是要拿真实业务系统再验一遍，不能只信模型自己生成的测试。

阶段三，用 Evaluation 做业务和系统的双重评估

一个 Agent 能正常启动、能调工具、也能生成回答，这些都不足以证明它可以上线。得建一套评估体系，我建议分成两类来看。

先看业务指标。

指标

评估内容

AI 独立解决率

用户问题是否由 AI 独立解决

订单查询准确率

是否查询到正确订单

退货规则准确率

是否正确判断退货条件

退款处理准确率

是否正确执行退款流程

人工接管率

有多少任务需要人工处理

再看系统指标。

指标

评估内容

Tool Calling 成功率

工具调用是否成功

端到端成功率

完整业务任务是否完成

P95 延迟

绝大多数请求的响应耗时

异常恢复率

失败任务是否能够安全恢复

权限拦截准确率

越权操作是否被正确阻止

这里面我特别看重端到端成功率。假设 Agent 成功调了订单查询接口，也成功调了物流查询接口，但最后没把用户要的退货申请办成，这次任务照样算失败。评估的对象应该是最终的业务结果。

测试集也要覆盖全，正常订单、异常订单、重复申请、特殊商品、权限不足、接口超时都得有。只有把这些边界条件都盖住，评估结果才有参考价值。

阶段四，用 Canary Release 逐步上线

测试通过以后，可以用 Canary Release，也就是金丝雀发布。

电商客服系统

│

├── 95% 原有客服流程

│

└── 5% 新版 AI Agent

│

▼

运行监控

│

┌─────────┴─────────┐

▼ ▼

业务指标 系统指标

│ │

└─────────┬─────────┘

▼

达标判断

│

┌─────┴─────┐

▼ ▼

逐步扩量 暂停或回滚

上线以后重点盯订单查询准确率、售后处理成功率、退款异常率和系统响应时间。运行结果达到预定标准，就慢慢扩大流量。一旦出现错误退款、权限问题或者业务指标明显下滑，立刻暂停扩量，按预案回滚。

企业级 Agent 上线，必须留着人工接管的能力和完整的审计记录。

## 完整的企业级电商 AI Agent 技术架构

前面讲了应用开发、Ontology 建模、Multi-Agent 和 Harness Engineering。现在把它们放到一张图里，就能得到一套完整的参考架构。

用户 / 运营人员

│

▼

AI Agent 应用层

│

Supervisor Agent

│

┌──────────────┼──────────────┐

▼ ▼ ▼

商品 Agent 订单 Agent 售后 Agent

│ │ │

└──────────────┼──────────────┘

▼

Agent Runtime

│

Harness Layer

│

┌──────────────┼──────────────┐

▼ ▼ ▼

Context Tools Safety

│ │ │

▼ ▼ ▼

业务上下文 API / MCP 权限与审批

│ │ │

└──────────────┼──────────────┘

▼

Ontology

│

┌────────────┼────────────┐

▼ ▼ ▼

Objects Relationships Rules

│ │ │

└────────────┼────────────┘

▼

数据与业务系统

│

┌──────────────┼──────────────┐

▼ ▼ ▼

商品中心 订单中心 库存中心

▼ ▼ ▼

支付中心 物流中心 售后中心

│

▼

Evaluation / Monitoring

│

▼

持续迭代与优化

这套架构里，每个模块各管一段。

技术模块

核心作用

LLM

理解语言、分析问题和生成结果

AI Agent

根据目标规划任务并调用工具

Ontology

统一业务对象、关系和语义

Tools / MCP

连接外部业务能力

Harness

管理执行过程、验证和异常恢复

Agent Runtime

管理 Agent 生命周期和运行状态

Multi-Agent

按业务领域进行任务协作

Evaluation

评估业务效果和系统可靠性

FDE

连接业务需求、技术开发和最终交付

如果非要用一个公式来概括这套系统，我会写成下面这样。

Enterprise AI Agent = Model + Context + Tools + Ontology + Harness + Runtime + Evaluation

这不是哪个标准组织定下来的技术公式，只是我自己对企业级 Agent 参考架构的一种归纳。模型决定智能能力，Ontology 帮 Agent 理解企业业务，Tools 提供操作外部系统的能力，Harness 和 Runtime 管住执行过程，Evaluation 检查最终结果，FDE 则负责把这些技术攒成一个能解决实际问题的应用。

## 到了交付这一步，要拿出哪些东西

很多 AI 项目在 Demo 阶段看着进展飞快，一到交付才发现拿不出完整的工程成果。一个电商 AI Agent 项目，至少该交付下面这些。

交付物

具体内容

业务需求

用户场景、业务流程、功能规格

Ontology

业务对象、属性、关系和语义定义

应用代码

前端、后端、Agent 和工具接口

数据集成

商品、订单、支付、库存、物流等系统连接

业务规则

退货、退款、权限和审批规则

测试评估

测试用例、评估集和运行结果

生产部署

Runtime、监控、审计、异常恢复和回滚方案

尤其是 Ontology，不能只甩一张关系图就完事，还得说清楚对象定义、字段映射、数据来源、实体标识、更新机制和版本变更。Agent 也不能只交一份 Prompt，它得有工具定义、权限配置、任务状态、错误处理和执行日志。这些东西决定了系统以后还能不能维护下去。

## 一个电商 AI Agent，什么时候才算做完

回到开头那个问题。一个电商 AI Agent，到底什么时候才算做完？

我的看法是，至少要做到几件事。用户提出订单问题，它能找到正确的业务对象。用户申请退货，它能查到真实的业务数据，按规则判断。轮到执行退款，它能检查权限、金额和业务状态。碰到异常，它能停下错误的操作，留下执行记录，把问题交回给人。

这几件事，没有一件是模型自己能扛下来的。模型能力决定 Agent 能理解、能推理到什么程度。业务模型决定它看不看得懂这家企业的世界。任务能不能安全、稳定地做完，最后要看工程体系。企业最该下功夫的，是把数据、业务语义、工具和执行流程连起来，从一个能查订单的售后 Agent 起步，一步步扩到商品、营销、库存、订单和供应链，最后长出一套能参与日常运营的智能体系统。

模型决定它能想多深，业务模型决定它看不看得懂这家企业，工程体系决定它能不能把任务安全做完。
