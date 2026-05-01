# Git Hooks

为 `ai-demo` 仓库提供统一的 Git 钩子，用于在 commit message 中自动标记协作来源，对齐 **Qoder 官方 AI 占比口径**。

## 标签规范

| 标签 | 含义 |
|------|------|
| `[AI:AGENT]`  | Agent 模式：多轮任务分解、自主编辑多文件 |
| `[AI:INLINE]` | Inline Chat：行内对话编辑局部片段 |
| `[AI:NEXT]`   | NEXT 补全：光标附近多行预测 |
| `[AI]`        | 历史兼容：在统计脚本中视为 Agent |
| `[HUMAN]`     | 纯人工编写 |

## 安装钩子

**PowerShell（Windows）**
```powershell
Copy-Item scripts/git-hooks/commit-msg .git/hooks/commit-msg -Force
```

**Bash / Git Bash**
```bash
cp scripts/git-hooks/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
```

> 注意：`commit-msg` 必须 **不带 BOM**，否则 `#!/bin/sh` 不会被识别。

## 使用示例

| 场景 | 命令 |
|------|------|
| Agent 模式提交（默认） | `git commit -m "feat: xxx"` → `[AI:AGENT] feat: xxx` |
| Inline Chat 提交 | `$env:AI_SOURCE='INLINE'; git commit -m "fix: xxx"` → `[AI:INLINE] fix: xxx` |
| NEXT 补全提交 | `$env:AI_SOURCE='NEXT'; git commit -m "refactor: xxx"` → `[AI:NEXT] refactor: xxx` |
| 纯人工提交 | `$env:AI_COMMIT=0; git commit -m "docs: xxx"` → `[HUMAN] docs: xxx` |
| 已含标签 | 钩子跳过，保持原样 |

使用完记得清理环境变量：
```powershell
Remove-Item Env:AI_SOURCE -ErrorAction SilentlyContinue
Remove-Item Env:AI_COMMIT -ErrorAction SilentlyContinue
```

## 决策优先级（钩子内部）

1. message 已含 `[AI:XXX]` / `[AI]` / `[HUMAN]` → 保持原样
2. `AI_COMMIT=0` → `[HUMAN]`
3. `AI_SOURCE=AGENT|INLINE|NEXT` → 对应标签
4. `AI_COMMIT=1` 且未指定 `AI_SOURCE` → `[AI:AGENT]`
5. 仓库默认人工：`git config ai-demo.defaultAuthor human`
6. 未设置任何变量 → `[AI:AGENT]`（默认）

## AI 占比统计

```powershell
# 全部历史
powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1

# 指定时间范围（Git 日期格式）
powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '2025-01-01'
powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '1 week ago'
powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '2025-10-01' -Until '2025-10-31'

# 包含 package-lock.json 等锁文件
powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -IncludeLockFiles
```

输出会按：**AI:AGENT / AI:INLINE / AI:NEXT / HUMAN / 未标注** 分类给出提交数、新增行、删除行，并计算：

- **按新增行占比**：`(Agent+Inline+NEXT 新增行) / 总新增行`
- **按提交数占比**：`AI 提交数 / 总提交数`
- **AI 内部分布**：Agent / Inline / NEXT 三者在 AI 合计中的占比
