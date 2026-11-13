$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$composeFile = Join-Path $root "docker-compose.greenmail.yml"

Write-Host "[INFO] Verificando Docker..."
docker --version | Out-Null

Write-Host "[INFO] Levantando GreenMail en Docker..."
docker compose -f $composeFile up -d

if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar GreenMail."
}

$env:MAIL_HOST = "127.0.0.1"
$env:MAIL_SMTP_PORT = "3025"
$env:MAIL_POP3_PORT = "3110"
$env:MAIL_IMAP_PORT = "3143"
$env:MAIL_USER = "grupo01sa"
$env:MAIL_PASSWORD = "grup001grup001*"
$env:MAIL_ADDRESS = "grupo01sa@pruebas.local"

Write-Host "[INFO] Entorno local de correo configurado:"
Write-Host "       MAIL_HOST=$env:MAIL_HOST"
Write-Host "       MAIL_SMTP_PORT=$env:MAIL_SMTP_PORT"
Write-Host "       MAIL_POP3_PORT=$env:MAIL_POP3_PORT"
Write-Host "       MAIL_USER=$env:MAIL_USER"
Write-Host "       MAIL_ADDRESS=$env:MAIL_ADDRESS"

Write-Host "[INFO] Esperando que GreenMail acepte conexiones..."
$ready = $false
for ($i = 0; $i -lt 20; $i++) {
    try {
        $socket = New-Object System.Net.Sockets.TcpClient
        $async = $socket.BeginConnect($env:MAIL_HOST, [int]$env:MAIL_POP3_PORT, $null, $null)
        if ($async.AsyncWaitHandle.WaitOne(1000)) {
            $socket.EndConnect($async)
            $socket.Close()
            $ready = $true
            break
        }
        $socket.Close()
    } catch {
    }
    Start-Sleep -Milliseconds 500
}

if (-not $ready) {
    throw "GreenMail no respondió en POP3 ($env:MAIL_HOST:$env:MAIL_POP3_PORT)."
}

Write-Host "[INFO] GreenMail listo. Iniciando consola..."
& (Join-Path $root "ejecutar-consola.ps1")
