**开源不易，还请您点个Star 多谢！🎉**

# AI 来了

AI 目前来说可以是非常非常的火热！但凡是个程序员都不好意思说没用过 AI，在面试的时候面试官也爱问 AI 这些东西。

尤其是又出现了AI Agent、RAG、向量数据库、MCP这些东西，进一步对 AI 进行了赋能。让 AI 的火热程度又燃烧了一把！

对于 Java 来说，在使用 AI 上也是迎来了一次大的变革，它就是 SpringAI！

看名字就知道它是由 Spring 官方团队开发的，与 SpringBoot 高度的集成，在使用体验上非常的爽快，

还是 **"配置 --> 注入 --> 使用"** 这三把斧。



# 项目介绍

**大麦AI** 是一套具有 AI 实战意义的项目，通过此项目能够帮助大家掌握 AI 的核心技能，包括：AI 的概念和作用、AI的模型、AI的使用、AI的开发。

用户可以通过 AI 模型，智能的帮用户推荐想要看的节目和演唱会，显示节目和演唱会的详情介绍、演出时间、各个票档价格、是否有余票，以及帮助生成订单等功能。

有时用户想了解相关的购票和退票规则，如果自己在系统中搜索的话，需要一点点仔细的查询，这样会花费很长的时间。通过 AI 模型，用户可以直接询问想要了解的规则，AI 模型就会根据已经制定好的规则内容根据用户的问题来将对应的内容告诉给用户。

- **大麦 AI 项目详细讲解：** [👉 点击查看讲解](https://javaup.chat/damai-ai/overview/project-intro)



# 能学到的硬货

此项目集成了目前主流的 AI 技术，通过此项目你能学到



- **SpringBoot 框架的深度使用和自动装配的实际应用**

  

- **RAG、向量数据库的作用和使用**

  

- **SpringAI 的深度适配以及注意项**

  

- **不同 AI 模型的特点以及如何使用（Ollama、OpenAI、DeepSeek、阿里百炼）**

  

- **Advisor 的自定义高级玩法**

  

- **Function Calling 的深度使用**

  

- **MCP 模型的使用**

  

- **RAG 的实战应用**

  

- **VectorStore 向量数据库的实战应用**

  

- **文档内容的抓取分析**

  

- **AI 和传统数据库的相互协调**

  

- **和传统项目的高级交互**

  

- **Cursor 工具的深度使用**



# 和其他项目的联动

真实的 AI 项目肯定不是自己单独的执行逻辑，自己和自己玩，而是要和其他不同的项目进行交互，查询数据，修改数据，提供文件 等等。

大麦AI 就是做到了和之前开发的 **大麦** 进行联动，**大麦** 负责高并发业务的逻辑执行，**大麦AI** 负责和用户进行高度智能的交互。

## 大麦项目

大麦项目是用来应对高并发产生的各种问题，设计各种实际落地的解决方案，如：**分库分表**、**锁的优化**、**缓存穿透**、**缓存雪崩**、**缓存击穿**，以及 **如何解决高流量下的订单生成**、**缓存中如何分片存储数据**、**快速扣减库存** 等等一系列问题。



大麦项目代码地址： [👉 点击即可跳转](https://gitee.com/java-up-up/damai)

大麦项目详细介绍： [👉 点击即可跳转](https://javaup.chat/damai/overview/project-intro)

# 项目流程

大麦AI 是根据官方正式版的文档进行搭建的，将 **每一步搭建**、**每一步执行**、**每一步踩坑** 都进行了总结。如果公司需要的话，小伙伴可以直接拿这套架构用到自己的项目上。

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E6%9E%B6%E6%9E%84/%E5%A4%A7%E9%BA%A6AI%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

# 项目功能

此项目具有两个角色：**大麦贴心助手**、**大麦规则助手**、**大麦运维助手**，也就是说具有两个业务功能

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E4%B8%BB%E9%A1%B5%E6%98%BE%E7%A4%BA.png)

## 大麦贴心助手

用户在进行购买演唱会时，想让贴心助手帮自己来干一些事，用户自己就不需要去系统中操作了，比如：

- 推荐哪些比较好的演唱会
- 告诉用户某场演唱会的详情介绍
- 告诉用户对应的票价金额、演出时间
- 帮助用户自动的完成购票

### 询问 我在北京，推荐有哪些演唱会

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%AF%A2%E9%97%AE%20%E6%88%91%E5%9C%A8%E5%8C%97%E4%BA%AC%EF%BC%8C%E6%8E%A8%E8%8D%90%E6%9C%89%E5%93%AA%E4%BA%9B%E6%BC%94%E5%94%B1%E4%BC%9A.gif)



### 询问 详细介绍周杰伦演唱会

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%AF%A2%E9%97%AE%20%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D%E5%91%A8%E6%9D%B0%E4%BC%A6%E6%BC%94%E5%94%B1%E4%BC%9A.gif)

### 询问 准备购票

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%AF%A2%E9%97%AE%20%E5%87%86%E5%A4%87%E8%B4%AD%E7%A5%A8.gif)

### 输入相应的购票信息

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%BE%93%E5%85%A5%E7%9B%B8%E5%BA%94%E7%9A%84%E8%B4%AD%E7%A5%A8%E4%BF%A1%E6%81%AF.gif)

### 自动地生成订单

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%87%AA%E5%8A%A8%E5%9C%B0%E7%94%9F%E6%88%90%E8%AE%A2%E5%8D%95.gif)

## 大麦规则助手

用户查询节目演唱会的订票和退票规则时，自己去系统进行查询会觉得很麻烦，想让规则助手来回答自己提出的问题，比如：

- 我想要订票，怎么操作
- 没抢到票/没票了，怎么办
- 儿童票订购的相关问题
- 德云社儿童也需要凭票入场么
- 具体的退票政策
- 门票自动作废了怎么办

### 询问 具体的退票政策

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%A7%84%E5%88%99%E5%8A%A9%E6%89%8B/%E5%85%B7%E4%BD%93%E7%9A%84%E9%80%80%E7%A5%A8%E6%94%BF%E7%AD%96.gif)

### 询问 儿童票订购的相关问题

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%A7%84%E5%88%99%E5%8A%A9%E6%89%8B/%E5%84%BF%E7%AB%A5%E7%A5%A8%E8%AE%A2%E8%B4%AD%E7%9A%84%E7%9B%B8%E5%85%B3%E9%97%AE%E9%A2%98.gif)

## 大麦运维助手
当项目出现问题时，可以直接询问运维助手日志错误的内容，运维助手会帮你去日志系统中查询对应的日志内容，并且会追踪整个请求的链路，并且给出相应的解决方案。
另外还可以询问服务的各项指标，比如JVM内存情况、GC情况、线程状态等，比如：

- 系统中有哪些微服务
- 询问“用户手机号不存在”的错误日志内容
- 根据这个错误所在的traceId进行完整的链路追踪
- 帮我查看order-service的JVM内存使用情况
- 帮我查看order-service的运行状态怎么样

### 系统中有哪些微服务

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E7%B3%BB%E7%BB%9F%E4%B8%AD%E6%9C%89%E5%93%AA%E4%BA%9B%E5%BE%AE%E6%9C%8D%E5%8A%A1.gif)

### 询问“用户手机号不存在”的错误日志内容

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E8%AF%A2%E9%97%AE%E7%94%A8%E6%88%B7%E6%89%8B%E6%9C%BA%E5%8F%B7%E4%B8%8D%E5%AD%98%E5%9C%A8.gif)

### 根据这个错误所在的traceId进行完整的链路追踪

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/%E6%A0%B9%E6%8D%AE%E8%BF%99%E4%B8%AA%E9%94%99%E8%AF%AF%E6%89%80%E5%9C%A8%E7%9A%84traceId%E8%BF%9B%E8%A1%8C%E5%AE%8C%E6%95%B4%E7%9A%84%E9%93%BE%E8%B7%AF%E8%BF%BD%E8%B8%AA.gif)

### 帮我查看order-service的JVM内存使用情况

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/order-service%E7%9A%84JVM%E5%86%85%E5%AD%98%E4%BD%BF%E7%94%A8%E6%83%85%E5%86%B5.gif)

### 帮我查看order-service的运行状态怎么样

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E4%B8%9A%E5%8A%A1/order-service%E7%9A%84%E8%BF%90%E8%A1%8C%E7%8A%B6%E6%80%81%E6%80%8E%E4%B9%88%E6%A0%B7.gif)

# 架构设计

项目在使用 AI 模型时，有多个模块功能，包括 **Springboot和SpringAI的结合**、**RAG和向量数据库的解释**、**SpringAI操作向量数据库**、**MCP 和 Function Calling 操作数据**、**AI的拦截器 Advisor 的高阶用法** 等等... ...

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E6%9E%B6%E6%9E%84/%E9%A1%B9%E7%9B%AE%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

# 业务结构

通过此业务结构图进一步详细的介绍项目中的功能，包括：**核心架构**、**AI模型**、**Advisor**、**RAG**、**基础配置**、**贴心助手**、**规则助手**、**调用方式**、**存储方式**等各个方面，能够对大麦AI项目的整体架构和设计有一个清晰的认知

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E6%9E%B6%E6%9E%84/%E9%A1%B9%E7%9B%AE%E4%B8%9A%E5%8A%A1%E7%9A%84%E7%BB%93%E6%9E%84%E5%9B%BE.png)

# 文档和视频目录

文档会将每个技术点、**怎么用？** **如何用？** **为什么这么用？** **用的过程有问题了怎么解决？** 都会讲到位

视频会按照文档的讲解顺序 **从0到1** 的完整讲解，保证学习过程中有一个清晰的认知过程

![](https://multimedia-javaup.cn/%E5%A4%A7%E9%BA%A6AI/%E6%9E%B6%E6%9E%84/%E9%A1%B9%E7%9B%AE%E6%96%87%E6%A1%A3.png)
