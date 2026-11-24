$ErrorActionPreference = 'Stop'

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaCommand = Get-Command javac -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaHome = Split-Path (Split-Path $javaCommand.Source)
    } else {
        throw 'Set JAVA_HOME to your JDK installation directory (JDK 21 or newer).'
    }
}

$compiler = Join-Path $javaHome 'bin/javac.exe'
$runtime = Join-Path $javaHome 'bin/java.exe'
$outputDirectory = Join-Path $PSScriptRoot 'out/app-classes'
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'src/main/java') -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName })

& $compiler -encoding UTF-8 -d $outputDirectory @sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

# src/main/resources holds default.properties, which the program reads from its classpath.
$resourceDirectory = Join-Path $PSScriptRoot 'src/main/resources'
$classPath = $outputDirectory + [IO.Path]::PathSeparator + $resourceDirectory
& $runtime -cp $classPath solitaire.Main @args
exit $LASTEXITCODE
