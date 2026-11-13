$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$lib = Join-Path $root "lib"
$classes = Join-Path $root "build\classes"

# Configuracion institucional del servidor del ingeniero.
$env:MAIL_HOST = "mail.tecnoweb.org.bo"
$env:MAIL_SMTP_PORT = "25"
$env:MAIL_POP3_PORT = "110"
$env:MAIL_IMAP_PORT = "143"
$env:MAIL_USER = "grupo01sa"
$env:MAIL_PASSWORD = "grup001grup001*"
$env:MAIL_ADDRESS = "grupo01sa@tecnoweb.org.bo"

$dependencies = @{
    "activation-1.1.1.jar" = "https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar"
    "gson-2.8.2.jar" = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar"
    "javax.mail.jar" = "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar"
    "json-20231013.jar" = "https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar"
    "json-simple-1.1.1.jar" = "https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar"
    "okhttp-3.11.0.jar" = "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/3.11.0/okhttp-3.11.0.jar"
    "okio-1.14.0.jar" = "https://repo1.maven.org/maven2/com/squareup/okio/okio/1.14.0/okio-1.14.0.jar"
    "postgresql-42.3.0.jar" = "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.3.0/postgresql-42.3.0.jar"
}

New-Item -ItemType Directory -Force -Path $lib, $classes | Out-Null

foreach ($dependency in $dependencies.GetEnumerator()) {
    $target = Join-Path $lib $dependency.Key
    if (-not (Test-Path $target)) {
        Write-Host "[INFO] Descargando $($dependency.Key)..."
        curl.exe -fL $dependency.Value -o $target
        if ($LASTEXITCODE -ne 0) {
            throw "No se pudo descargar $($dependency.Key)"
        }
    }
}

$classpath = ((Get-ChildItem -LiteralPath $lib -Filter "*.jar" -File |
        ForEach-Object { $_.FullName }) -join ";")
$sources = Get-ChildItem -LiteralPath (Join-Path $root "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName }

Write-Host "[INFO] Compilando..."
javac -encoding UTF-8 -source 17 -target 17 -cp $classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) {
    throw "La compilación falló"
}

Write-Host "[INFO] Ejecutando proyectomailgrupo01sa.Consola..."
java -cp "$classpath;$classes" proyectomailgrupo01sa.Consola
