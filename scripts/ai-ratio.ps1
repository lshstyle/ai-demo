# AI 代码贡献占比统计（对齐 Qoder 官方口径）
#
# 公式：AI 占比 = (Agent + Inline Chat + NEXT) 代码行数 / 总代码行数
#
# 标签规范：
#   [AI:AGENT]   Agent 模式
#   [AI:INLINE]  Inline Chat
#   [AI:NEXT]    NEXT 补全
#   [AI]         历史兼容：视为 Agent
#   [HUMAN]      人工
#
# 用法（项目根目录）：
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '2025-01-01'
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '1 week ago'
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -Since '2025-10-01' -Until '2025-10-31'
#   powershell -ExecutionPolicy Bypass -File scripts/ai-ratio.ps1 -IncludeLockFiles

param(
    [string]$Since,
    [string]$Until,
    [switch]$IncludeLockFiles
)

$ErrorActionPreference = 'Stop'

$LockPatterns = @(
    'package-lock.json', 'yarn.lock', 'pnpm-lock.yaml',
    'composer.lock', 'Gemfile.lock', 'poetry.lock'
)

function Test-IsLockFile($path) {
    foreach ($p in $LockPatterns) {
        if ($path -like "*$p") { return $true }
    }
    return $false
}

# 根据 subject 首行判断来源类别
function Get-CommitSource($subject) {
    if ($subject -match '^\[AI:AGENT\]')   { return 'AGENT' }
    if ($subject -match '^\[AI:INLINE\]')  { return 'INLINE' }
    if ($subject -match '^\[AI:NEXT\]')    { return 'NEXT' }
    if ($subject -match '^\[AI\]')         { return 'AGENT' }  # 兼容历史
    if ($subject -match '^\[HUMAN\]')      { return 'HUMAN' }
    return 'UNTAGGED'
}

# 收集全部 commit 与其 stat
function Get-CommitStats {
    $gitArgs = @('log', '--pretty=format:__COMMIT__%H|%s')
    if ($Since) { $gitArgs += "--since=$Since" }
    if ($Until) { $gitArgs += "--until=$Until" }
    $gitArgs += '--numstat'

    $raw = & git @gitArgs
    if (-not $raw) { return @() }

    $results = @()
    $curHash = $null
    $curSubject = $null
    $curAdd = 0
    $curDel = 0

    foreach ($line in $raw) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line.StartsWith('__COMMIT__')) {
            if ($curHash) {
                $results += [pscustomobject]@{
                    Hash = $curHash
                    Subject = $curSubject
                    Source = Get-CommitSource $curSubject
                    Added = $curAdd
                    Deleted = $curDel
                }
            }
            $payload = $line.Substring('__COMMIT__'.Length)
            $idx = $payload.IndexOf('|')
            $curHash = $payload.Substring(0, $idx)
            $curSubject = $payload.Substring($idx + 1)
            $curAdd = 0
            $curDel = 0
            continue
        }
        # numstat 行: <added>\t<deleted>\t<file>
        $parts = $line -split "`t", 3
        if ($parts.Count -lt 3) { continue }
        $a, $d, $file = $parts[0], $parts[1], $parts[2]
        if ($a -eq '-' -or $d -eq '-') { continue }  # 二进制
        if (-not $IncludeLockFiles -and (Test-IsLockFile $file)) { continue }
        $curAdd += [int]$a
        $curDel += [int]$d
    }

    if ($curHash) {
        $results += [pscustomobject]@{
            Hash = $curHash
            Subject = $curSubject
            Source = Get-CommitSource $curSubject
            Added = $curAdd
            Deleted = $curDel
        }
    }

    return $results
}

# ---------- 主流程 ----------
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "      AI 代码贡献占比统计（Qoder 口径）" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
$range = '全部历史'
if ($Since -and $Until) { $range = "$Since ~ $Until" }
elseif ($Since)         { $range = "$Since 至今" }
elseif ($Until)         { $range = "最早 至 $Until" }
Write-Host "时间范围: $range" -ForegroundColor DarkGray
if ($IncludeLockFiles) { Write-Host "统计范围: 包含锁文件" -ForegroundColor DarkGray }
else                   { Write-Host "统计范围: 已排除 package-lock.json / yarn.lock 等锁文件" -ForegroundColor DarkGray }
Write-Host ""

$commits = Get-CommitStats

if ($commits.Count -eq 0) {
    Write-Host "该时间范围内没有找到任何提交。" -ForegroundColor Yellow
    exit 0
}

# 聚合
$groups = $commits | Group-Object Source
$stat = @{}
foreach ($src in @('AGENT','INLINE','NEXT','HUMAN','UNTAGGED')) {
    $stat[$src] = @{ Commits=0; Added=0; Deleted=0 }
}
foreach ($g in $groups) {
    $sum = $g.Group | Measure-Object Added -Sum
    $del = $g.Group | Measure-Object Deleted -Sum
    $stat[$g.Name] = @{
        Commits = $g.Count
        Added = [int]$sum.Sum
        Deleted = [int]$del.Sum
    }
}

$aiAdded    = $stat['AGENT'].Added + $stat['INLINE'].Added + $stat['NEXT'].Added
$aiCommits  = $stat['AGENT'].Commits + $stat['INLINE'].Commits + $stat['NEXT'].Commits
$totalAdded = $aiAdded + $stat['HUMAN'].Added + $stat['UNTAGGED'].Added
$totalCommits = $aiCommits + $stat['HUMAN'].Commits + $stat['UNTAGGED'].Commits

$fmt = "{0,-12} {1,8} {2,12} {3,12} {4,12}"
Write-Host ($fmt -f '来源', 'Commit', '新增行', '删除行', '净增行') -ForegroundColor Yellow
Write-Host ('-' * 60)
foreach ($src in @('AGENT','INLINE','NEXT','HUMAN','UNTAGGED')) {
    $s = $stat[$src]
    $label = switch ($src) {
        'AGENT'    { 'AI:AGENT' }
        'INLINE'   { 'AI:INLINE' }
        'NEXT'     { 'AI:NEXT' }
        'HUMAN'    { 'HUMAN' }
        'UNTAGGED' { '未标注' }
    }
    Write-Host ($fmt -f $label, $s.Commits, $s.Added, $s.Deleted, ($s.Added - $s.Deleted))
}
Write-Host ('-' * 60)
Write-Host ($fmt -f 'AI 合计', $aiCommits, $aiAdded, '-', '-') -ForegroundColor Green
Write-Host ($fmt -f '总计',    $totalCommits, $totalAdded, '-', '-') -ForegroundColor Green

Write-Host ""
Write-Host "=== AI 占比 ===" -ForegroundColor Cyan
if ($totalAdded -gt 0) {
    $ratioLine = [math]::Round($aiAdded * 100.0 / $totalAdded, 2)
    Write-Host ("按新增行: AI {0} / 总 {1} = {2}%" -f $aiAdded, $totalAdded, $ratioLine) -ForegroundColor Green
} else {
    Write-Host "新增行为 0，无法计算按行占比。" -ForegroundColor Yellow
}
if ($totalCommits -gt 0) {
    $ratioCommit = [math]::Round($aiCommits * 100.0 / $totalCommits, 2)
    Write-Host ("按提交数: AI {0} / 总 {1} = {2}%" -f $aiCommits, $totalCommits, $ratioCommit) -ForegroundColor Green
}

# 细分占比（相对 AI 合计）
if ($aiAdded -gt 0) {
    Write-Host ""
    Write-Host "=== AI 内部分布（占 AI 合计） ===" -ForegroundColor Cyan
    foreach ($src in @('AGENT','INLINE','NEXT')) {
        $p = [math]::Round($stat[$src].Added * 100.0 / $aiAdded, 2)
        Write-Host ("AI:{0,-8} {1,6}% ({2} 行)" -f $src, $p, $stat[$src].Added)
    }
}
Write-Host ""
