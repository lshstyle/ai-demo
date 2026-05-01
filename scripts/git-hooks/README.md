# Git Hooks

为 `ai-demo` 仓库提供统一的 Git 钩子，用于在 commit message 中自动标记协作来源。

## 钩子列表

### `commit-msg` — AI / HUMAN 前缀标记

自动为 commit message 首行补全 `[AI]` 或 `[HUMAN]` 前缀，便于后续统计 AI 生成代码的占比。

**行为**：
| 场景 | 结果 |
|------|------|
| message 已含 `[AI]` 或 `[HUMAN]` | 保持原样 |
| Merge / Revert / fixup! / squash! 等自动消息 | 不改动 |
| `AI_COMMIT=1` | 强制前缀 `[AI] ` |
| `AI_COMMIT=0` | 强制前缀 `[HUMAN] ` |
| 未设置 `AI_COMMIT` | 默认 `[AI] `；若 `git config ai-demo.defaultAuthor human` 则为 `[HUMAN] ` |

## 安装

在项目根目录执行：

**PowerShell（Windows）**
```powershell
Copy-Item scripts/git-hooks/commit-msg .git/hooks/commit-msg -Force
```

**Bash / Git Bash**
```bash
cp scripts/git-hooks/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
```

## 使用示例

```powershell
# 默认（AI 协助）
git commit -m "feat: 新增订单接口"
# → 实际写入："[AI] feat: 新增订单接口"

# 强制标记为人工编写
$env:AI_COMMIT=0; git commit -m "fix: 修正单元测试断言"
# → "[HUMAN] fix: 修正单元测试断言"

# 设置仓库默认身份为 human（一次即可）
git config ai-demo.defaultAuthor human
```

## AI 占比统计

安装钩子后，可通过以下命令快速计算 AI 代码行数占比：

```bash
# AI 提交的新增行数
ai=$(git log --grep='^\[AI\]' --pretty=tformat: --numstat | awk '{a+=$1} END{print a+0}')
human=$(git log --grep='^\[HUMAN\]' --pretty=tformat: --numstat | awk '{a+=$1} END{print a+0}')
total=$((ai + human))
echo "AI 行数: $ai / 总行数: $total / 占比: $(echo "scale=2; $ai*100/$total" | bc)%"
```
