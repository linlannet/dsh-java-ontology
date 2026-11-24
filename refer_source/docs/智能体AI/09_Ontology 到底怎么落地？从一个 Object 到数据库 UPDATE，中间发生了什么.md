---
title: Ontology 到底怎么落地？从一个 Object 到数据库 UPDATE，中间发生了什么
subtitle: 从一张表到一个客户：Ontology 如何让 AI 看懂企业世界？
kicker: 三、建模篇：从 Object、Link 到 Action 的企业世界
author: 智能体AI
date: 2026-10-16
tags: [Ontology, 本体, 企业AI]
source: https://mp.weixin.qq.com/s?__biz=Mzg5Mjc3NTg1Mw==&mid=2247492501&idx=1&sn=671867ae716ea3d08e88169147eb5f04&chksm=c03a5f57f74dd641b0ac0701d3ab74f11b8dc98cb4b6b082891c08f3b7c49c62a4be7db25a9f#rd
theme: azure
---

数据库里的一行记录，永远不等于企业世界里的一个对象——Ontology 做的不是给表换名字，而是把数据、关系、动作、权限，组织成AI 可以行动的世界。

做企业 AI 项目，聊到 Ontology 的时候，经常会出现一个有意思的现象。大家都能理解这些是企业里的“对象”：Customer、User、Account、Order、Product、Contract，也能理解它们之间的关系：

relation

Customer ──hasUser──→ User

Customer ──owns──→ Account

User ──uses──→ Product

客户有用户，客户有账户，用户使用产品，都很好懂。但只要再追问两个问题，事情马上就复杂起来。

第一个问题：这些 Object 到底是从哪来的？数据库里明明是这样的：

db

customer

customer_contact

customer_level

subscriber

account

product_instance

bill

为什么到了 Ontology 里，就变成了 Customer、User、Account、Product、Bill？

第二个问题：Agent 操作了一个 Object，数据库到底是谁改的？比如用户说：“把客户 C001 的等级调整成 VIP。”Agent 最后是不是直接生成一条 SQL？

sql

UPDATE customer

SET level = ‘VIP’

WHERE customer_id = ‘C001’；

如果不是，那 Ontology Object、Action、API、Workflow 和数据库之间，到底是什么关系？我觉得这个问题比“Ontology 是什么”更重要。只有把它想明白了，才算真正理解企业 Ontology。

📌 本文看点

1. 两层映射：数据如何变成企业对象

5. Action：为什么 Agent 不直接写 SQL

9. 完整案例：从报障到恢复服务全链路

## 先说结论：Ontology 不是在数据库上再套一层对象

这是理解 Ontology 最容易踩的坑。

很多人第一次接触，会觉得：数据库里有一张 customer 表，那我在上面建一个 Customer Object 就行了。如果只是这样，Ontology 的价值其实很有限，因为 ORM、GraphQL、数据中台、数据虚拟化 这些技术，早就能解决类似的问题。

Ontology 真正要做的，是把下面这些东西，组织成一个统一的企业世界模型：

formula

企业数据

+ 企业关系

+ 企业语义

+ 业务规则

+ 业务动作

+ 权限

+ 治理

你可以看到，数据只是其中一项，关系、规则、动作、权限都在里面。所以数据库里的一行记录，和 Ontology 里的一个对象，不是一回事：

「Database Row ≠ Ontology Object」

整个链路大概是这样：

从上往下看：企业现有的数据库和系统，经过映射，变成 Ontology 里的对象；对象之间有关系和业务逻辑；对象上可以定义动作；动作最后通过 API、工作流或事件，回到企业系统里去执行。你会发现第一行和最后一行是同一批系统，数据从系统里出来，操作又回到系统里去，这就是一个完整的闭环。

## 数据库和业务人员，说的本来就是两种话

企业为什么需要 Ontology？因为数据库描述的东西，和业务人员理解的东西，本来就不是一回事。

数据库工程师看到的是：

db

customer_id

customer_type

customer_status

account_id

subscriber_id

product_instance_id

业务人员看到的是：客户、用户、账户、套餐、服务。

这两套语言没有谁对谁错，只是抽象层次不同。数据库关心的是数据怎么存；业务关心的是企业世界里有什么东西，它们是什么关系，现在处于什么状态，可以对它们做什么。

举个最简单的例子。数据库里有这样一张表：

db

customer

customer_id

customer_name

status

业务人员不会说“请查询 customer_id = C001 的 row”，他会说“帮我查一下客户 C001”。

听起来只是叫法的区别，但企业越复杂，这个区别就越大。比如“客户”这一个概念，同时涉及 CRM、ERP、Billing、合同系统、客服系统、营销系统、数据仓库。数据库里有十几张表，但在业务世界里，它仍然只是一个客户。

所以 Ontology 做的第一件事，就是建立 Data → Business Object 的映射，把数据库里的数据，对应到业务人员脑子里的那个“东西”上。

## 第一层映射：数据怎么变成 Ontology Object

最简单的情况：一张表对应一个 Object

先看最简单的。数据库里有一张 customer 表：

db

customer

customer_id

customer_name

customer_type

status

在 Ontology 里定义一个 Customer，字段一一对应就行：

map

customer．customer_id　　→　Customer．customerId

customer．customer_name　→　Customer．name

customer．customer_type　→　Customer．type

customer．status　　　　　→　Customer．status

左边是表里的字段，右边是对象的属性，只是换了个更像业务语言的名字。放在一张图里，就是这样：

map

Database

┌─────────────────────┐

│ customer　　　　　　│

│　　　　　　　　　　　│

│ customer_id　　　　 │

│ customer_name　　　 │

│ customer_type　　　 │

│ status　　　　　　　│

└──────────┬──────────┘

│

Mapping

│

↓

┌─────────────────────┐

│ Customer Object　　 │

│　　　　　　　　　　　│

│ customerId　　　　　│

│ name　　　　　　　　│

│ type　　　　　　　　│

│ status　　　　　　　│

└─────────────────────┘

上面是数据库里的表，下面是 Ontology 里的对象，中间那层 Mapping 就是刚才那组对应关系。到这一步，可以说 customer 表被映射成了 Customer Object。

但千万别以为企业 Ontology 都这么简单，真正的麻烦，从这里才开始。

现实中，一个 Object 往往来自很多张表

还是以 Customer 为例。企业里的“客户”，很少只有一张表，更常见的是：

db

customer

customer_contact

customer_level

customer_address

customer_contract

customer_tag

customer_manager

这些表分别存着基本信息、联系人、等级、地址、合同、标签、客户经理，合在一起才是业务上完整的“一个客户”。

但在 Ontology 里，我们仍然只提供一个 Customer：

tree

Customer

├── customerId

├── name

├── type

├── status

├── level

├── contact

├── address

├── contracts

└── manager

这是使用者看到的样子。而在它背后，数据其实是从好几张表里拼出来的：

source

Customer

│

├── customer

├── customer_contact

├── customer_level

├── customer_address

├── customer_contract

└── customer_manager

比如 level 来自 customer_level，contact 来自 customer_contact。上面那张图是对象的“外观”，下面这张是对象的“来源”。用的人不用关心这些表是怎么拆、怎么拼的。

到这里就能看出 Ontology 和简单 ORM 的区别了。它不是给表换个名字，而是按企业语义，把数据重新组织了一遍。

更复杂的情况：一个 Object 来自多个业务系统

现实中的企业，更不会只有一套数据库。拿电信运营商来说：

systems

CRM

└── Customer

BSS

└── Subscriber

Billing

└── Account

OSS

└── Service

ERP

└── Contract

客户在 CRM 里，用户在 BSS 里，账户在计费系统里，服务在 OSS 里，合同在 ERP 里。每个系统都有自己的数据模型：CRM 里是 customer_id 和 customer_name，BSS 用 subscriber_id，Billing 用 account_id，OSS 用 service_id。

如果让 Agent 直接面对这些系统，它得自己去搞清楚一堆事情：C001 到底是不是 Customer 001？subscriber_id 和 user_id 是什么关系？account_id 和 customer_id 怎么关联？service_id 又对应哪个用户？

这显然不是 Agent 该干的活。所以 Ontology 要做的，就是把这些系统里的数据，重新组织成一个统一的企业世界：

world

Customer

│

┌───────┴────────┐

↓　　　　　　　　↓

User　　　　　　Account

│　　　　　　　　│

↓　　　　　　　　↓

Product　　　　　Bill

│

↓

Service

你看，一个客户下面分出两条线：一条通向用户，一条通向账户。用户下面挂着产品，产品下面是具体的服务；账户下面挂着账单。底层还是那几套系统，但 Agent 面对的，是这张干干净净的图，它看到的是 Customer、User、Account、Product、Service，不再是 CRM、BSS、Billing、OSS。

这是 Ontology 非常核心的价值：把系统世界，转换成企业世界。

所以，Object 其实是一层企业语义抽象

说到这里，可以这样理解 Ontology Object：

formula

Ontology Object

=

企业语义

+

底层数据映射

+

对象状态

+

对象关系

+

业务能力

也就是说，它既有业务含义，也知道数据从哪来，还带着当前状态、和其他对象的关系，以及能做的事情。所以一个 Customer，绝不是一行数据库记录，它更像这样：

anatomy

Customer

│

├── Properties

│

├── Relationships

│

├── Current State

│

├── Business Logic

│

├── Actions

│

└── Permissions

属性、关系、当前状态、业务逻辑、动作、权限，一样不少。到这一步，Ontology 才真正和普通的数据模型拉开了距离。

## 第二层映射：不只要映射属性，还要映射关系

这是另一个关键的地方。

数据库里， customer 表里有个 customer_id，user 表里也有个 customer_id，通过外键关联起来。数据库理解的是“这两个字段之间有关联”。

但业务理解的是：一个客户拥有多个用户。

所以在 Ontology 里，它不该只是 customer_id = C001，而应该是：

relation

Customer

│

│ hasUser

↓

User

箭头旁边的 hasUser，就是这条关系的名字。外键被提升成了一条业务关系，Agent 一眼就能看明白：这个客户下面有哪些用户。

用电信“三户模型”来看，会非常清楚

电信运营商最经典的，就是“客户、用户、账户”这三户：

three-accounts

Customer

（客户）

│

┌────────┴────────┐

│　　　　　　　　　 │

hasUser　　　　　hasAccount

│　　　　　　　　　 │

↓　　　　　　　　　↓

User　　　　　　Account

（用户）　　　　 （账户）

客户是签约的主体，往下分出两条线：hasUser 指向用户，也就是实际使用服务的那个号码；hasAccount 指向账户，也就是负责付费结算的地方。

再往下展开，就是这样：

tree

Customer

│

├── User

│　　　│

│　　　├── Product

│　　　└── Service

│

└── Account

│

└── Bill

用户下面有产品和服务，账户下面有账单。

有了这个结构，Agent 就能理解下面这句话了：

张三是一个 Customer，他下面有三个 User，其中两个 User 使用移动通信产品，这些 User 的费用由某个 Account 负责结算。

这已经不是在“查表”了，而是在理解企业世界。

## 但是，光能看懂对象还不够

这就是 Ontology 最关键的另一半。如果 Ontology 只有Customer、User、Account，再加上属性和关系，它其实还只是一个比较高级的数据模型。真正让它变成企业 AI 执行底座的，是Action，也就是：这个对象允许做什么？

Object 解决“是什么”，Action 解决“能做什么”

以 Customer 为例，它有 customerId、name、level、status 这些属性。但企业真正关心的是：这个客户能不能升级？能不能暂停？能不能注销？能不能增加用户？能不能修改联系人？

所以 Customer 除了属性，还得有动作：

actions-customer

Customer

│

├── Properties

│

└── Actions

├── UpdateCustomer

├── ChangeLevel

├── AddUser

└── CloseCustomer

一个对象，左边是它是什么，右边是它能做什么：更新客户信息、变更等级、新增用户、注销客户。

User 也一样：

actions-user

User

│

├── Properties

│

└── Actions

├── SuspendService

├── ResumeService

├── ChangePackage

└── ChangeSIM

用户能做的是：暂停服务、恢复服务、变更套餐、更换 SIM 卡。

Account 也是：

actions-account

Account

│

├── Properties

│

└── Actions

├── Recharge

├── PayBill

├── AdjustBalance

└── FreezeAccount

账户能做的是：充值、缴费、调整余额、冻结账户。

你会发现，每种对象能做什么，完全由它的业务含义决定。到了这一步，Ontology 就从“描述企业”，走向了可以操作企业。

## 最关键的问题：Action 到底怎么执行

现在回到开头那个问题。假设用户对 Agent 说：“把客户 C001 的等级调整成 VIP。”

Agent 会把它理解成：

intent

Customer（C001）

+

ChangeLevel（VIP）

注意，这时候 Agent 还没有碰数据库，它只是提出了一个意图：对 Customer 这个对象，执行 ChangeLevel 这个 Action。

接下来真正的执行链路是这样：

这一路走下来，其实是一道一道的关卡：先查权限，当前这个人有没有资格做这件事；再看业务规则，符不符合要求；然后做参数校验，需要的话还得走审批；全部通过之后，才会去调 CRM 的接口，最后才轮到数据库里的数据被真正修改。

Agent 只负责最上面那一步，剩下的都由 Ontology 和执行层来把关。这条链路，才是企业 Ontology 真正有价值的地方。

为什么不让 Agent 直接生成 SQL？

这是很多企业 AI 项目都会遇到的问题。技术上当然可以：

sql

用户：

把 C001 升级成 VIP

Agent：

UPDATE customer

SET level = ‘VIP’

WHERE customer_id = ‘C001’；

看起来特别直接。但一放到企业场景里，马上会冒出几个问题。

第一个问题：权限

假设一个普通客服，可以查询客户，但不能修改客户等级。如果 Agent 拥有数据库的 UPDATE 权限，模型就绕过业务权限。这显然是不允许的。

更合理的方式是：

perm-check

Agent

↓

ChangeLevel

↓

Permission Check

Agent 只能提出 ChangeLevel 这个动作，能不能执行，要先过权限检查。系统会判断：当前操作者是谁？是否允许执行 ChangeLevel？是否允许改成 VIP？是否需要审批？检查通过，才能继续往下走。

第二个问题：业务规则

数据库知道 customer_id = C001，也知道 status = ACTIVE，但它未必知道完整的业务规则。比如：欠费客户不能升级 VIP；某类客户升级 VIP，必须经过客户经理审批；VIP 客户必须满足最近 6 个月消费金额超过某个阈值。

这些都是 Business Logic，不是一句 UPDATE 能表达的。所以真正的 Action 应该叫 ChangeLevel，而不是 UPDATE customer，因为 ChangeLevel 是业务语言，规则可以跟着它一起走。

第三个问题：一个业务动作，往往不只改一张表

比如“暂停这个用户的服务”。你以为只需要：

sql

UPDATE user

SET status = ‘SUSPENDED’；

但在真实的运营商系统里，事情是这样的：

multi-table

用户状态

↓

账户状态

↓

欠费判断

↓

产品实例

↓

服务实例

↓

OSS

↓

网络侧

要先改用户状态，再看账户状态，判断有没有欠费，然后处理对应的产品实例和服务实例，通知 OSS，最后在网络侧把服务真正停掉。中间涉及 CRM、BSS、Billing、OSS、Network 好几个系统一起配合。

Action 是业务操作的抽象，SQL 只是实现 Action 的其中一种方式

Action 的底层实现，不一定是 SQL

这一点非常重要。一个 Ontology Action，最终通过下面这些方式中的一种或几种来完成：

action-impl

REST API

GraphQL

SQL

Workflow

Event

Message Queue

Function

Microservice

不同的动作，走的路也不一样。比如变更客户等级，可以直接走 CRM 的接口：

crm-path

ChangeCustomerLevel

↓

CRM API

↓

Customer Service

↓

Database

Agent 提出动作，CRM 接口接住，由客户服务去操作数据库，Agent 全程碰不到 SQL。

暂停服务，走的就是一条工作流：

workflow-path

SuspendService

↓

Workflow

↓

Billing Check

↓

OSS API

↓

Network Provisioning

先进工作流，在里面检查计费状态，再调 OSS 接口，最后由网络侧完成关停。

再比如创建订单，走的是接口加事件：

order-path

CreateOrder

↓

Order API

↓

Event

↓

Inventory

↓

Logistics

订单接口先接住请求，发出一个事件，库存和物流各自接着处理。

所以，千万不要把 Ontology Action 理解成 SQL Generator，这是一个非常大的误区。

合理的架构：用 Ontology 把 Agent 和底层系统隔开

把前面这些串起来，可以画成四层：

最上面是 Agent，它只负责听懂用户想干什么。第二层是 Ontology，把这句话翻译成“对 Customer 执行 ChangeLevel”。第三层是执行层，权限、规则、校验、工作流都在这里，同时决定最终用 API、SQL 还是事件去落地。最底下才是真正的企业系统和数据库。

这个架构最重要的一点是：Agent 不需要知道底层数据库长什么样。它只需要知道，Customer 有一个 ChangeLevel Action。至于这个 Action 最后是调 CRM API，还是走 Workflow，还是执行某条受控的 SQL，是执行层的事。

## 查询和修改，其实是两条不同的链路

这里再把读和写分开说一下。

查询

比如用户问：“张三现在有几个用户？”Agent 的思路是这样的：

query-path

Customer

↓

Relationship

↓

User

↓

Query

先找到客户张三，沿着“客户拥有用户”这条关系走到用户，再发起查询。查询在内部的执行过程是：

query-internals

Ontology Query

↓

Data Mapping

↓

CRM / BSS / Database

↓

Result

↓

Ontology Object

↓

Agent

Ontology 先根据映射，把对象层面的查询翻译成对底层系统的查询；底层返回结果后，再转回 Ontology 对象交给 Agent。最后 Agent 拿到的是：

result

Customer

└── Users：3

张三名下有 3 个用户。它看到的是业务结果，不是一堆表和字段。

修改

修改就麻烦多了。比如：“暂停张三的 138xxxx 手机号。”

modify-path

User

↓

SuspendService

↓

Permission

↓

Business Rule

↓

Approval

↓

Execution

↓

OSS/BSS

↓

Database / Network

对用户执行暂停服务，依次过权限、规则、审批，然后执行，最终落到 OSS、BSS，以及数据库和网络上。

而且执行完还没完，系统还需要把结果同步回来：

state-refresh

Action

↓

Execution

↓

State Change

↓

Data Update

↓

Ontology State Refresh

动作执行了，底层状态变了，数据更新了，Ontology 里对象的状态也得跟着刷新。不然 Agent 下次再看这个用户，看到的还是旧状态。

所以一个完整的企业 Agent，不应该只是这样：

naive-agent

Input

↓

LLM

↓

Output

而应该是这样：

agent-loop

Input

↓

Understand Object

↓

Read State

↓

Reason

↓

Select Action

↓

Permission

↓

Execute

↓

Observe Result

↓

Update State

↓

Return Result

接到请求，先理解涉及哪个对象，读取当前状态，推理判断，选一个动作，过权限，执行，观察结果，更新状态，最后把结果返回。中间的“读状态”和“观察结果”，让整个过程形成了循环，这已经非常接近真正的Agent Loop了。

## 这也是 Ontology 和普通知识图谱很重要的区别

知识图谱通常可以表达这些：

knowledge-graph

Entity

+

Relationship

+

Knowledge

比如“张三属于某个客户”：

relation-1

张三

↓

属于

↓

某个客户

或者“客户拥有账户”：

relation-2

客户

↓

拥有

↓

账户

它能告诉你谁和谁有什么关系。但企业真正要执行的时候，还得继续回答：

这个对象是什么？现在是什么状态？和谁有什么关系？我能不能操作？允许做什么？谁可以操作？操作需要审批吗？操作之后会影响什么？最终调用哪个系统？

所以企业 Ontology 更接近：

enterprise-ontology

Entity

+

Relationship

+

State

+

Business Logic

+

Action

+

Permission

+

Governance

和知识图谱比，多出来的是状态、业务逻辑、动作、权限和治理。这也是为什么在企业 AI 场景里，Ontology 的价值远远不只是“知识关系”。

## 用一个完整案例，把整个链路串起来

假设运营商客服收到一个请求：

“客户张三说自己的手机号无法使用，请帮我处理。”

如果没有 Ontology，Agent 要同时面对 CRM、BSS、Billing、OSS、工单系统、知识库，然后自己猜：先查 CRM 还是先查 BSS？先查余额还是先查服务状态？

如果有 Ontology，Agent 先理解对象关系：

object-chain

Customer

↓

User

↓

Service

顺着客户、用户、服务这条线找下去，定位到：

locate

Customer = 张三

User = 138xxxx

Service = Mobile Service

也就是张三这个客户，他的号码 138xxxx，以及这个号码对应的移动通信服务。接着读取各对象的状态：

object-status

User．status = ACTIVE

Account．status = NORMAL

Service．status = SUSPENDED

用户正常，账户正常，但服务是被暂停的。问题就出在服务上。

Agent 接着判断：为什么 Service 是 SUSPENDED？于是顺着关系继续往下查：

suspend-reason

Service

↓

Suspend Reason

↓

Billing

↓

Outstanding Bill

从服务找到暂停原因，原因指向计费，计费里有一笔没结清的账单。再一看：

balance-check

Account．balance ＜ 0

账户余额是负的，欠费了。

到这一步，Agent 根本不需要自己写 SQL。它可以提出 PayBill（缴费），或者 ResumeService（恢复服务）。

如果恢复服务有前置条件，就继续往下走：

preconditions

Permission

↓

Business Rule

↓

Payment Check

↓

Action

先看有没有权限，再看规则允不允许，再确认款项是不是已经缴清，都通过了才真正执行。执行时是这样一条路：

execution-path

ResumeService

↓

BSS API

↓

OSS API

↓

Network

服务恢复之后，结果会重新回到 Ontology：

state-back

Service．status

↓

ACTIVE

最后 Agent 告诉客服：服务已经恢复。

你回头看整个过程：Agent 从头到尾，都不需要知道哪张表、哪个字段、哪个 JOIN、哪个 SQL、哪个数据库。它操作的是 Customer、User、Account、Service 和 Action。

这才是 Ontology 对企业 Agent 的意义。

## 电信“三户模型”，其实就是很典型的落地场景

如果把这个思路用到运营商的三户模型上，可以这样设计：

three-account-model

Customer

│

┌─────────┴─────────┐

│　　　　　　　　　　│

hasUser　　　　　　hasAccount

│　　　　　　　　　　│

↓　　　　　　　　　　↓

User　　　　　　　　Account

│　　　　　　　　　　│

┌─────┴─────┐　　　　　　↓

↓　　　　　　↓　　　　　　Bill

Product　　　Service

这张图前面见过类似的，只不过这次我们要给每个对象补上具体的属性。

Customer 有：

customer-properties

Properties

├── customerId

├── name

├── customerType

├── level

└── status

User 有：

user-properties

Properties

├── userId

├── phoneNumber

├── status

├── product

└── service

Account 有：

account-properties

Properties

├── accountId

├── balance

├── creditLimit

└── status

然后再给它们定义动作。Customer 有：

customer-actions

ChangeLevel

AddUser

UpdateCustomer

CloseCustomer

User 有：

user-actions

SuspendService

ResumeService

ChangePackage

ChangeSIM

Account 有：

account-actions

Recharge

PayBill

AdjustBalance

FreezeAccount

这样一来，整个企业里的 AI Agent，就有了一个非常清晰的操作空间：有哪些对象，它们之间什么关系，每个对象上能做哪些事，一目了然。

## 回头再看：Ontology 到底是什么？

现在可以重新定义一次了。

Ontology 不是把数据库表换成 Object，也不是把知识图谱换个名字。更准确地说，它是把企业的数据、对象、关系、状态、业务逻辑、可执行动作和权限，组织成一个统一的企业语义与执行层。

它至少解决两类映射。

第一种映射：Data → Object

data-to-object

CRM

ERP

BSS

OSS

Billing

Database

API

↓

Mapping

↓

Customer

User

Account

Order

Product

Service

左边是各种系统和数据，右边是业务对象。这一步解决的是：企业数据在业务世界里，到底代表什么。

第二种映射：Action → Operation

action-to-operation

Customer．ChangeLevel

↓

Permission

↓

Business Rule

↓

Workflow / API / SQL / Event

↓

CRM / BSS / ERP / OSS

↓

Database / Real World

从一个业务动作开始，经过权限和规则，再落到具体的工作流、接口、SQL 或事件，最终改变系统里的数据，也就是改变现实世界里的状态。这一步解决的是：企业允许对这个对象做什么，以及这个动作怎么真正执行。

所以，重要的不只是 Object，而是 Object + Action

很多 Ontology 的介绍，注意力全放在 Object Type、Property、Relationship 上。但对企业 AI 来说，还有另外一半：Action、Permission、Business Logic、Execution。

Object 解决的是“世界里有什么”，Action 解决的是“这个世界里可以发生什么”。两者合在一起，Ontology 才真正成为 Agent 可以工作的“企业世界”。

## 把全文压缩成三个层次

如果把全文压缩成三个层次，会非常清楚。

第一层是 Data Model，也就是 Table、Column、Row、Primary Key、Foreign Key，解决的是数据怎么存。

第二层是 Ontology / Business Model，也就是 Customer、User、Account、Order、Product、Service，加上 Relationship 和 State，解决的是企业世界里有什么。

第三层是 Action Model，也就是 ChangeLevel、SuspendService、Recharge、PayBill、ApproveOrder、CancelOrder，解决的是企业允许做什么。

最后串起来，就是这样一条链：

full-chain

Data

↓

Object

↓

Relationship

↓

State

↓

Action

↓

Permission

↓

Business Logic

↓

Execution

↓

Real World

从数据开始，变成对象，对象之间有关系和状态，在对象上定义动作，动作受权限和业务逻辑约束，最后执行，作用到现实世界。我觉得这条链路，是理解企业 Ontology 最值得记住的一张图。

## 这也解释了：Enterprise Agent 和 Chatbot 是两回事

普通 Chatbot 是这样的：

chatbot

Question

↓

LLM

↓

Answer

它的任务，是把问题回答出来。

企业 Agent 则是这样的：

enterprise-agent

User Request

↓

Understand Business Object

↓

Read Enterprise State

↓

Understand Relationships

↓

Reason / Plan

↓

Select Action

↓

Permission Check

↓

Business Rule

↓

Human Approval

↓

Execute

↓

Observe Result

↓

Update State

↓

Audit

它要做的，是在企业世界里，采取一个可控的行动。从理解对象、读状态、做计划，到选动作、过权限和规则、必要时让人来审批，再到执行、观察结果、更新状态，最后留下审计记录。

所以真正的 Enterprise Agent，不应该只是 LLM 加 Tool Calling，它更接近：

agent-formula

Model

+

Context

+

Ontology

+

Action

+

Permission

+

Harness

+

Enterprise Systems

## 最后一个判断：Ontology 是企业 AI 的“中间世界”

过去的企业系统，是这样的：

past

User

↓

CRM

↓

Database

用户用 CRM，CRM 连着数据库，简单直接。

后来企业有了越来越多系统：

siloed

User

↓

┌───────────────┼───────────────┐

↓　　　　　　　　↓　　　　　　　　↓

CRM　　　　　　　ERP　　　　　　　BSS

↓　　　　　　　　↓　　　　　　　　↓

Database　　　　　Database　　　　　Database

每个系统各有各的数据库，用户要在不同系统之间来回切换。

现在 Agent 出现了，如果让它直接去连这些系统：

agent-direct

Agent

┌──────────┼──────────┐

↓　　　　　↓　　　　　↓

CRM　　　　ERP　　　　BSS

↓　　　　　↓　　　　　↓

DB　　　　　DB　　　　　DB

Agent 很快就会陷进一堆工程细节里：Schema、API、Permission、Business Rule、System Dependency、Data Mapping，每一个都是坑。

Ontology 提供的，就是在中间加一层：

with-ontology

Agent

↓

Ontology

↓

┌────────────┼────────────┐

↓　　　　　　↓　　　　　　↓

CRM　　　　　　ERP　　　　　BSS

↓　　　　　　↓　　　　　　↓

DB　　　　　　DB　　　　　DB

这样一来，Agent 面对的是：

agent-sees

Customer

User

Account

Order

Product

Service

而不是：

tables

customer_table

subscriber_table

account_table

product_instance_table

Agent 操作的是：

actions

ChangeLevel

SuspendService

Recharge

CreateOrder

而不是：

sql

UPDATE

INSERT

DELETE

Agent 面对的是对象和动作，而不是表和 SQL——这就是最大的变化。

## 总结：Ontology 真正解决的，是“让 AI 能在企业世界里做事”

如果只把 Ontology 理解成“数据库上面加一层 Object”，那其实低估了它。

真正有价值的 Ontology，至少要解决三件事。

第一，把数据变成对象。

thing-1

Data

↓

Object

让 AI 知道，企业里到底有什么。

第二，把外键变成业务关系。

thing-2

Foreign Key

↓

Business Relationship

让 AI 知道，这些对象之间是什么关系。

第三，把业务动作变成可执行的 Action。

thing-3

Action

↓

Permission

↓

Business Logic

↓

API / Workflow / SQL / Event

↓

Enterprise System

让 AI 知道，自己可以做什么，以及怎样安全地做。

所以我更愿意把企业 Ontology 理解成下面这张图：

final-diagram

Enterprise AI

│

▼

┌───────────┐

│ Ontology　│

└─────┬─────┘

│

┌──────────────┼──────────────┐

↓　　　　　　　　　　↓　　　　　　　　　　↓

Object　　　　　　Relationship　　　Action

│　　　　　　　　　　│　　　　　　　　　　│

└──────────────┼──────────────┘

↓

Business Logic

↓

Permission

↓

Execution Layer

↓

CRM / ERP / BSS / OSS / DB

企业 AI 在最上面，下面是 Ontology。Ontology 里有三样东西：对象、关系、动作。三者合起来，经过业务逻辑和权限的约束，再交给执行层，最后落到各个企业系统和数据库上。

最终，企业 AI 真正发生变化的地方，不是 AI 能不能回答一个问题，而是 AI 能不能理解企业里的对象、理解对象之间的关系、知道对象当前的状态，并且在权限和业务规则的约束下，对这些对象执行一个真正可控的动作。

从这个角度看，Ontology 就不再只是一个“数据建模技术”了。它更像是连接企业数据世界、业务世界和 AI 行动世界的一层中间语义与执行基础设施。

而这也正是企业 AI 从“会回答问题”走向“真正能够干活”的关键一步。

「企业 AI 真正的变化，不是会回答问题，而是能理解对象、理解关系、知道状态，并执行可控的动作。」
