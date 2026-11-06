# Changes one value in the Kubernetes ConfigMap (k8s/configmap.yaml), shows the change,
# commits it and pushes it. The push triggers the CI/CD pipeline, which applies the
# ConfigMap with Ansible and rolls out new pods that use the new value.
#
# Usage (from the project folder):
#   powershell -ExecutionPolicy Bypass -File .\update-config.ps1 -Key APP_THEME_COLOR -Value "#0b7a3e"
#   powershell -ExecutionPolicy Bypass -File .\update-config.ps1 -Key APP_MESSAGE -Value "Config updated using a ConfigMap"

param(
    [Parameter(Mandatory = $true)][string]$Key,
    [Parameter(Mandatory = $true)][string]$Value
)

$file = "k8s/configmap.yaml"
if (-not (Test-Path $file)) {
    Write-Error "Run this script from the project folder (C:\projects\robo-advisor-cicd)"
    exit 1
}

$content = Get-Content $file -Raw
$pattern = "(?m)^(\s*" + [regex]::Escape($Key) + ":\s*).*$"

if ($content -notmatch $pattern) {
    Write-Error "Key '$Key' not found in $file. Available keys: APP_TITLE, APP_ENVIRONMENT, APP_THEME_COLOR, APP_MESSAGE"
    exit 1
}

$oldLine = $Matches[0].Trim()
$newContent = [regex]::Replace($content, $pattern, ('${1}"' + $Value + '"'))
Set-Content -Path $file -Value $newContent -NoNewline

Write-Host ""
Write-Host "ConfigMap change:" -ForegroundColor Cyan
Write-Host "  before: $oldLine"
Write-Host "  after : $Key`: `"$Value`""
Write-Host ""
git --no-pager diff -- $file

git add $file
git commit -m "ConfigMap: set $Key to $Value"
git push

Write-Host ""
Write-Host "Pushed. The pipeline is now applying the new ConfigMap:" -ForegroundColor Green
Write-Host "  https://github.com/rahulraghavendrar/robo-advisor-cicd/actions"
