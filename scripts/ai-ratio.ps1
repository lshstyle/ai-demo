# AI 代码贡献占比统计脚本
#
# 用法（在项目根目录执行）：
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -IncludeLockFiles
#
# 统计原则：
# 1. 基于 commit message 首行的 [AI] / [HUMAN] 前缀区分；
# 2. 默认排除 package-lock.json / yarn.lock / pnpm-lock.yaml 等锁文件；
# 3. 二进制文件（numstat 显示为 '-'）自动跳过；
# 4. 同时给出提交数、新增行、删除行、净增行与占比。

param(
    [switch]$IncludeLockFiles
)

$ErrorActionPreference = 'Stop'

$LockPatterns = @(
    'package-lock.json',
    'yarn.lock',
    'pnpm-lock.yaml',
    'composer.lock',
    'Gemfile.lock',
    'poetry.lock'
)

function Test-IsLockFile($path) {
    foreach ($p in $LockPatterns) {
        if ($path -like "*$p") { return $true }
    }
    return $false
}

function Get-CommitStat {
    param([string]$Pattern)

    $added = 0; $deleted = 0; $commits = 0
    $commitHashes = git log --grep=$Pattern --pretty=format:%H
    if (-not $commitHashes) { return @{ Commits=0; Added=0; Deleted=0 } }

    foreach ($hash in $commitHashes) {
        if ([string]::IsNullOrWhiteSpace($hash)) { continue }
        $commits++
        $lines = git show --pretty=format: --numstat $hash
        foreach ($l in $lines) {
            if ([string]::IsNullOrWhiteSpace($l)) { continue }
            $parts = $l -split "`t"
            if ($parts.Count -lt 3) { continue }
            $a, $d, $file = $parts[0], $parts[1], $parts[2]
            if ($a -eq '-' -or $d -eq '-') { continue }
            if (-not $IncludeLockFiles -and (Test-IsLockFile $file)) { continue }
            $added += [int]$a
            $deleted += [int]$d
        }
    }
    return @{ Commits=$commits; Added=$added; Deleted=$deleted }
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "        AI 代码贡献占比统计报告" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
if ($IncludeLockFiles) {
    Write-Host "（包含锁文件）" -ForegroundColor DarkGray
} else {
    Write-Host "（已排除 package-lock.json / yarn.lock 等锁文件）" -ForegroundColor DarkGray
}
Write-Host ""

$ai    = Get-CommitStat '^\[AI\]'
$human = Get-CommitStat '^\[HUMAN\]'

$fmt = "{0,-8} {1,8} {2,10} {3,10} {4,10}"
Write-Host ($fmt -f '来源', 'Commit', '新增行', '删除行', '净增行') -ForegroundColor Yellow
Write-Host ($fmt -f '------', '------', '------', '------', '------')
Write-Host ($fmt -f 'AI',    $ai.Commits,    $ai.Added,    $ai.Deleted,    ($ai.Added - $ai.Deleted))
Write-Host ($fmt -f 'HUMAN', $human.Commits, $human.Added, $human.Deleted, ($human.Added - $human.Deleted))

$total = $ai.Added + $human.Added
Write-Host ""
if ($total -gt 0) {
    $aiRatio    = [math]::Round($ai.Added    * 100.0 / $total, 2)
    $humanRatio = [math]::Round($human.Added * 100.0 / $total, 2)
    Write-Host "AI 占比（按新增行）   : $aiRatio%" -ForegroundColor Green
    Write-Host "HUMAN 占比（按新增行）: $humanRatio%" -ForegroundColor Green
} else {
    Write-Host "当前仓库暂无 [AI] 或 [HUMAN] 前缀的提交，无法统计。" -ForegroundColor Yellow
}

$totalCommits = $ai.Commits + $human.Commits
if ($totalCommits -gt 0) {
    $aiCommitRatio    = [math]::Round($ai.Commits    * 100.0 / $totalCommits, 2)
    $humanCommitRatio = [math]::Round($human.Commits * 100.0 / $totalCommits, 2)
    Write-Host "AI 占比（按提交数）   : $aiCommitRatio%" -ForegroundColor Green
    Write-Host "HUMAN 占比（按提交数）: $humanCommitRatio%" -ForegroundColor Green
}
Write-Host ""
