$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$buildScript = Join-Path $repositoryRoot 'build-lambdas.ps1'
if (-not (Test-Path -LiteralPath $buildScript)) {
    throw 'build-lambdas.ps1 is missing.'
}

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

function java { $global:LASTEXITCODE = 0 }

function docker {
    $global:financeBotBuildTestCalls.Add('docker ' + ($args -join ' '))
    $global:LASTEXITCODE = 0
    if ($global:financeBotBuildTestScenario -eq 'docker-failure') {
        $global:LASTEXITCODE = 1
    }
}

function mvn {
    $global:financeBotBuildTestCalls.Add('mvn ' + ($args -join ' '))
    $global:LASTEXITCODE = 0
    if ($global:financeBotBuildTestScenario -eq 'test-failure' -and $args -contains 'test') {
        $global:LASTEXITCODE = 1
        return
    }
    if ($args -contains 'package' -and $global:financeBotBuildTestScenario -ne 'missing-artifact') {
        $moduleRoot = Join-Path (Get-Location).Path (Split-Path -Parent $args[1])
        $targetDirectory = Join-Path $moduleRoot 'target'
        [System.IO.Directory]::CreateDirectory($targetDirectory) | Out-Null
        [System.IO.File]::WriteAllText((Join-Path $targetDirectory 'function.zip'), 'mock artifact')
    }
}

$originalLocation = Get-Location
foreach ($global:financeBotBuildTestScenario in @('success', 'test-failure', 'docker-failure', 'missing-artifact')) {
    $scratchDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('financebot-build-test-' + [guid]::NewGuid())
    [System.IO.Directory]::CreateDirectory($scratchDirectory) | Out-Null
    try {
        $scratchScript = Join-Path $scratchDirectory 'build-lambdas.ps1'
        Copy-Item -LiteralPath $buildScript -Destination $scratchScript
        $global:financeBotBuildTestCalls = New-Object 'System.Collections.Generic.List[string]'
        $caught = $null
        try {
            & $scratchScript | Out-Null
        } catch {
            $caught = $_
        }

        Assert-True ((Get-Location).Path -eq $originalLocation.Path) 'The working directory was not restored.'
        if ($global:financeBotBuildTestScenario -eq 'success') {
            Assert-True ($null -eq $caught) "Build failed unexpectedly: $caught"
            $mavenCalls = @($global:financeBotBuildTestCalls | Where-Object { $_ -like 'mvn *' })
            Assert-True ($mavenCalls.Count -eq 5) 'Expected core install, two tests, and two native builds.'
            Assert-True ($mavenCalls[0] -like '*finance-core/pom.xml*install*') 'Shared library must be installed first.'
            Assert-True ($mavenCalls[1] -like '*gateway-function/pom.xml*test*') 'Gateway tests must precede builds.'
            Assert-True ($mavenCalls[2] -like '*transaction-function/pom.xml*test*') 'Transaction tests must precede builds.'
            foreach ($call in $mavenCalls[3..4]) {
                Assert-True ($call -like '*-Dnative*-DskipTests*-Dquarkus.native.container-build=true*-Dquarkus.native.march=x86-64-v2*') 'Missing native build flags.'
            }
        } else {
            Assert-True ($null -ne $caught) "$global:financeBotBuildTestScenario should fail."
            if ($global:financeBotBuildTestScenario -eq 'test-failure') {
                Assert-True (-not ($global:financeBotBuildTestCalls | Where-Object { $_ -like '*package*' })) 'Build continued after failing tests.'
            }
            if ($global:financeBotBuildTestScenario -eq 'docker-failure') {
                Assert-True (-not ($global:financeBotBuildTestCalls | Where-Object { $_ -like 'mvn *' })) 'Maven ran despite Docker failure.'
            }
        }
        Write-Host "PASS: $global:financeBotBuildTestScenario"
    } finally {
        # Delete only the unique temporary directory created by this test.
        $resolvedScratch = [System.IO.Path]::GetFullPath($scratchDirectory)
        $temporaryRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
        if (-not $resolvedScratch.StartsWith($temporaryRoot, [StringComparison]::OrdinalIgnoreCase) -or
            (Split-Path -Leaf $resolvedScratch) -notlike 'financebot-build-test-*') {
            throw 'Refusing to remove a directory outside the test temporary directory.'
        }
        Remove-Item -LiteralPath $resolvedScratch -Recurse -Force
    }
}
