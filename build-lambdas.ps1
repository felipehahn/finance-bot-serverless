<#
.SYNOPSIS
Tests and builds both Linux native Lambda ZIPs for manual AWS deployment.
.DESCRIPTION
Requires JDK 21, Maven, and a running Docker daemon. Run from any directory.
Installs finance-core, tests both functions, and builds x86_64 native artifacts.
.EXAMPLE
.\build-lambdas.ps1
#>
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-BuildCommand {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    Write-Host "> $Command $($Arguments -join ' ')"
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE. Build stopped."
    }
}

foreach ($command in @('java', 'mvn', 'docker')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "Required command '$command' was not found. Install JDK 21, Maven, and Docker and add them to PATH."
    }
}

Push-Location -LiteralPath $PSScriptRoot
try {
    Invoke-BuildCommand -Command 'docker' -Arguments @('info', '--format', '{{.ServerVersion}}')

    Invoke-BuildCommand -Command 'mvn' -Arguments @('-f', 'finance-core/pom.xml', 'install')

    $modules = @('gateway-function', 'transaction-function')
    foreach ($module in $modules) {
        Invoke-BuildCommand -Command 'mvn' -Arguments @('-f', "$module/pom.xml", 'clean', 'test')
    }

    foreach ($module in $modules) {
        Invoke-BuildCommand -Command 'mvn' -Arguments @(
            '-f', "$module/pom.xml", 'package',
            '-Dnative', '-DskipTests',
            '-Dquarkus.native.container-build=true',
            '-Dquarkus.native.march=x86-64-v2'
        )

        $artifact = Join-Path $PSScriptRoot "$module/target/function.zip"
        if (-not (Test-Path -LiteralPath $artifact -PathType Leaf)) {
            throw "Build completed without the expected Lambda artifact: $artifact"
        }
        if ((Get-Item -LiteralPath $artifact).Length -eq 0) {
            throw "Lambda artifact is empty: $artifact"
        }
    }

    Write-Host ''
    Write-Host 'Both Lambda ZIPs are ready for manual upload:'
    foreach ($module in $modules) {
        Write-Host (Join-Path $PSScriptRoot "$module/target/function.zip")
    }
    Write-Host 'AWS runtime: provided.al2023; architecture: x86_64; environment: DISABLE_SIGNAL_HANDLERS=true.'
} finally {
    Pop-Location
}
