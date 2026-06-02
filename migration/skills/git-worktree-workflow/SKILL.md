# Git Worktree 开发工作流

一套完整的隔离开发工作流：创建分支 → worktree 开发 → push → 合并 → 清理。

## 何时使用

- 需要隔离开发环境（修紧急 bug、开发新功能）而不影响当前工作
- 多任务并行开发，每个任务有独立目录
- 需要干净的基线测试某个分支的代码
- 任何需要"切分支但不想丢失当前修改"的场景

## 配置参数

执行前先确认以下配置（都有合理默认值，可以按需修改）：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `base_branch` | `develop` | 从哪个分支切出来 |
| `new_branch` | `feature/<task>` | 新分支名，根据任务命名 |
| `worktree_path` | `../<repo>-<task>` | worktree 目录路径 |
| `push` | `true` | 完成后是否 push 到远端 |
| `merge_target` | `develop` | 合并到哪个目标分支 |
| `cleanup` | `ask` | 完成后是否删除 worktree 和分支（ask/yes/no） |
| `create_pr` | `false` | 是否建议创建 PR（vs 直接合并） |

## 完整流程

### 步骤 1：准备工作

```bash
# 查看当前状态和已有 worktree
cd <repo-root>
git status
git worktree list
```

检查目标分支是否已被其他 worktree 占用（`git branch` 列表中带 `+` 号）。如果 `base_branch` 被占用，需要在新 worktree 里基于该分支的最新 commit 创建一个新分支，而不是直接 checkout 它。

### 步骤 2：创建 worktree + 新分支

```bash
git worktree add <worktree_path> -b <new_branch> <base_branch>
```

这会：
1. 在 `<worktree_path>` 创建独立工作目录
2. 新建 `<new_branch>` 分支（`-b` 参数）
3. 基于 `<base_branch>` 的最新代码

如果 `base_branch` 被其他 worktree 占用，改用：

```bash
# 基于被占用分支的最新 commit 创建新分支
git worktree add <worktree_path> -b <new_branch> $(git rev-parse <base_branch>)
```

### 步骤 3：开发

```bash
cd <worktree_path>
# 改代码、commit...
git add .
git commit -m "<commit message>"
```

每个 worktree 完全独立，有自己的工作目录、暂存区、HEAD。可以正常 commit、stash、reset，不影响其他 worktree。

### 步骤 4：推送

如果 `push` 配置为 `true`：

```bash
cd <worktree_path>
git push origin <new_branch>
```

### 步骤 5：合并

**推荐方式：创建 PR/MR**

```bash
# push 后在 GitHub/GitLab 上创建 PR
# 源分支: <new_branch>
# 目标分支: <merge_target>
```

**直接合并（仅当有权限且不需要 review 时）：**

```bash
cd <worktree_path>
git fetch origin <merge_target>
git checkout <merge_target>  # 确保目标分支没被其他 worktree 占用
git merge <new_branch>
git push origin <merge_target>
# 回到 worktree 继续清理
cd <worktree_path>
```

### 步骤 6：清理

如果 `cleanup` 配置为 `yes`（或用户确认后）：

```bash
# 删除 worktree 目录
git worktree remove <worktree_path>

# 如果分支已合并到目标分支，删除本地和远端分支
git branch -d <new_branch>
git push origin --delete <new_branch>
```

如果 `cleanup` 配置为 `no`，保留 worktree 和分支供后续使用。
如果 `cleanup` 配置为 `ask`（默认），询问用户是否清理。

## 常见问题

### 分支被占用了怎么办

`git branch` 列表中带 `+` 号的分支表示被另一个 worktree 占用，无法在当前目录 checkout。解决：

1. **基于 commit 创建新分支**：用 `git rev-parse <branch>` 拿到 commit，`git worktree add path -b new-branch <commit>`
2. **释放被占用的分支**：进入占用该分支的 worktree，checkout 到其他分支，然后那个分支就自由了

### worktree 目录被 rm -rf 删了怎么办

```bash
# 清理 git 里的残留记录
git worktree prune
```

### 想基于 main 但 main 被 agent 占用了

这是 Multica 平台的常见情况。用方案 1：基于 main 的最新 commit 创建新分支，绕开占用限制。

## 速查表

| 你想... | 命令 |
|---------|------|
| 从 main 开新分支 + worktree | `git worktree add ../my-task -b feature/task main` |
| 查看有哪些 worktree | `git worktree list` |
| 查看哪些分支被占用 | `git branch`（找 `+` 号） |
| 推送新分支 | `git push origin <branch>` |
| 删除 worktree | `git worktree remove <path>` |
| 清理残留记录 | `git worktree prune` |
| 删除本地分支 | `git branch -d <branch>` |
| 删除远端分支 | `git push origin --delete <branch>` |
