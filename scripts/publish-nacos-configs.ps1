<#
.SYNOPSIS
  Publish HydroCore Nacos config templates from src/main/resources/nacos/

.EXAMPLE
  .\scripts\publish-nacos-configs.ps1 -ServerAddr "localhost:8848" -Namespace "hydrocore" -Username "nacos" -Password "123456"
#>
param(
  [string]$ServerAddr = "localhost:8848",
  [string]$Namespace = "hydrocore",
  [string]$Group = "DEFAULT_GROUP",
  [string]$Username = "nacos",
  [string]$Password = "123456",
  [string]$ConfigDir = ""
)

$ErrorActionPreference = "Stop"
if (-not $ConfigDir) {
  $ConfigDir = Join-Path $PSScriptRoot "..\src\main\resources\nacos" | Resolve-Path
}

$base = "http://$ServerAddr/nacos/v1/cs/configs"
$files = @(
  @{ DataId = "hydrocore.yml"; File = "hydrocore.yml"; Type = "yaml" },
  @{ DataId = "hydrocore-constant.yml"; File = "hydrocore-constant.yml"; Type = "yaml" },
  @{ DataId = "hydrocore-config.properties"; File = "hydrocore-config.properties"; Type = "properties" },
  @{ DataId = "redis.yml"; File = "redis.yml"; Type = "yaml" },
  @{ DataId = "mybatis-plus.yml"; File = "mybatis-plus.yml"; Type = "yaml" },
  @{ DataId = "pagehelper.yml"; File = "pagehelper.yml"; Type = "yaml" },
  @{ DataId = "sec-knife4j.yml"; File = "sec-knife4j.yml"; Type = "yaml" }
)

Write-Host "Nacos: $ServerAddr  namespace(tenant)=$Namespace  group=$Group"
Write-Host "Config dir: $ConfigDir"

foreach ($item in $files) {
  $path = Join-Path $ConfigDir $item.File
  if (-not (Test-Path $path)) {
    Write-Warning "Missing file: $path"
    continue
  }
  $content = Get-Content $path -Raw -Encoding UTF8
  $body = @{
    dataId  = $item.DataId
    group   = $Group
    type    = $item.Type
    content = $content
    tenant  = $Namespace
    username = $Username
    password = $Password
  }
  try {
    $resp = Invoke-RestMethod -Method Post -Uri $base -Body $body -ContentType "application/x-www-form-urlencoded"
    Write-Host ("[OK] {0} => {1}" -f $item.DataId, $resp)
  } catch {
    Write-Host ("[FAIL] {0}: {1}" -f $item.DataId, $_.Exception.Message)
  }
}

Write-Host "Done. Create namespace '$Namespace' in Nacos console first if publish fails with namespace errors."
