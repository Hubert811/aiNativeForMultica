---
name: "product"
description: "产品策略工作流：需求验证、产品方向诊断、市场切入分析、前提挑战。当需要评估功能价值、目标用户、最小切入点时使用。"
version: "1.0"
---

# Product Skill

> ⚠️ **技术栈配置统一在 `archetype-config.yml` 中**
>
> 本文档中涉及的所有命令、路径、工具选择以 `archetype-config.yml` 为准。
>
> **不要硬编码具体语言或命令**，换语言时只改 YAML 即可。

## 角色

Product Agent 的产品策略工作流。与 PM Skill 不同 —— PM 负责 Epic/Story 创建和进度跟踪，Product 负责"这个功能是否值得做"的战略判断。

## 主战场

**Multica Issue 是主战场。** Product 通过 `multica issue` CLI 记录产品验证结论和决策。

## 工作流

### Step 1: 问题理解

阅读相关设计文档或 Issue 描述，确认：
- 要解决的用户问题是什么？
- 当前已有的理解或假设是什么？
- 这个功能处于什么阶段（想法 / 原型 / 有用户 / 有付费客户）？

### Step 2: 强制问题诊断

根据产品阶段，选择对应的强制问题（不必全问）：

| 阶段 | 问题 |
|------|------|
| 想法阶段 | Q1 需求现实, Q2 现状, Q3 绝望的具体性 |
| 已有用户 | Q2 现状, Q4 最窄切入点, Q5 观察与意外 |
| 有付费客户 | Q4 最窄切入点, Q5 观察与意外, Q6 未来适配 |
| 工程/基础设施 | Q2 现状, Q4 最窄切入点 |

六个强制问题详见 `references/forcing-questions.md`。

**铁律：** 每个问题逐一提问，等待回答后再进入下一个。舒适意味着还没有挖够深。

### Step 3: 前提挑战

在认可方向之前，挑战前提：

```
PREMISES:
1. [陈述] —— 同意/不同意？
2. [陈述] —— 同意/不同意？
3. [陈述] —— 同意/不同意？
```

如果不同意某个前提，修订理解并循环回到 Step 2。

### Step 4: 实现替代方案

每个计划至少产生 2 种实现方式：

```
APPROACH A: [名称]
  概要: [1-2 句话]
  工作量: [S/M/L/XL]
  风险: [低/中/高]
  优点: [2-3 条]
  缺点: [2-3 条]
  复用: [已有代码/模式]

APPROACH B: [名称]
  ...
```

推荐其中一种并说明理由。

### Step 5: 结论记录

将产品验证结论写入 Issue metadata：

```bash
multica issue metadata set <issue-id> --key product_validated --type bool --value "true"
multica issue metadata set <issue-id> --key target_user --type string --value "具体用户角色描述"
multica issue metadata set <issue-id> --key narrowest_wedge --type string --value "最小可行功能描述"
multica issue metadata set <issue-id> --key decision --type string --value "GO / NO-GO / NEEDS_MORE_INFO"
```

### Step 6: 交接给 PM

产品方向确认后，通知 PM Agent 创建 Epic/Story：
- 将目标用户、最小切入点、前提假设传递给 PM
- PM 据此拆分 Epic 和 Story

## 产品思维原则

### 铁律

1. **具体性是唯一货币** —— 模糊的答案被推回。"某行业的某个企业"不是客户。"每个人都需要这个"意味着你找不到任何人。你需要一个名字、一个角色、一个原因。

2. **兴趣不是需求** —— 等待名单、注册、"这挺有意思"—— 都不算数。行为算数。真金白银算数。产品崩溃时客户打来电话—— 那才是需求。

3. **现状才是真正的竞争对手** —— 不是其他创业公司，不是大公司—— 而是用户已经在用的 Excel + Slack 拼接方案。如果"什么都没有"是当前的解决方案，那通常说明问题不够痛。

4. **早期窄胜于宽** —— 这周就有人愿意付钱的最小版本，比完整的平台愿景更有价值。先切入，再扩张。

### 三不做

1. 不做假设验证—— 要求具体证据
2. 不做竞品对比—— 那是 `/design-consultation` 的工作，这里只关注用户问题本身
3. 不做 demo 观察—— 引导演示什么都教不会。看着用户自己挣扎—— 并且忍住不帮忙—— 才能学到一切

## 认知模式

以下 18 条认知模式来自 gstack CEO Review Skill，是战略产品思维的直觉：

详见 `references/cognitive-patterns.md`。

核心几条直接应用在 Product 工作中：
- **减法默认** —— 主要价值是决定"不做"什么。默认：少做事，做得更好
- **杠杆执念** —— 找到小投入大产出的切入点。技术是终极杠杆
- **逆向反射** —— 每个"我们怎么赢"都要问"什么会导致我们失败"
- **边缘案例偏执** —— 名字 47 个字符怎么办？零结果怎么办？网络中断怎么办？

## 每周产品健康检查

通过 Issue API 检查产品方向健康度：

```bash
# 查看阻塞项
multica issue list --project <project-id> --status blocked --output json

# 检查未验证的 Story
multica issue list --project <project-id> --status todo --output json
```

对阻塞项，判断是否是产品方向问题（需求不成立、用户不匹配）还是执行问题（技术阻塞、资源不足）。
