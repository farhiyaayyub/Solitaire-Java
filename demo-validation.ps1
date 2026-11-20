$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $cases = @(
        @{ File = 'input.txt'; Expected = 0; Description = 'Complete, valid deck' },
        @{ File = 'duplicate.txt'; Expected = 2; Description = 'Repeated H-A' },
        @{ File = 'invalid.txt'; Expected = 2; Description = 'Unknown suit Z' },
        @{ File = 'invalid-rank.txt'; Expected = 2; Description = 'Unknown rank 15' },
        @{ File = 'invalid-format.txt'; Expected = 2; Description = 'Missing hyphen in D2' },
        @{ File = 'incomplete.txt'; Expected = 2; Description = 'Only four cards' },
        @{ File = 'missing-validation-demo.txt'; Expected = 1; Description = 'File does not exist' }
    )
    $passed = 0
    foreach ($case in $cases) {
        Write-Host ""
        Write-Host ("CASE: {0} - {1}" -f $case.File, $case.Description)
        Write-Host ("Expected exit code: {0}" -f $case.Expected)
        & "$PSScriptRoot/run.ps1" validate $case.File
        $actualExitCode = $LASTEXITCODE
        if ($actualExitCode -eq $case.Expected) {
            $passed++
            Write-Host 'PASS - result matches expectation'
        } else {
            throw "Expected exit $($case.Expected), received $actualExitCode"
        }
    }
    Write-Host ""
    Write-Host "$passed / $($cases.Count) validation cases passed."
} finally {
    Pop-Location
}
