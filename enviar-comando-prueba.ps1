param(
    [string]$From = "estudiante@pruebas.local",
    [string]$To = "grupo01sa@pruebas.local",
    [string]$Subject = "usuario listar",
    [string]$Body = "Comando de prueba desde entorno local Docker"
)

$smtpHost = "127.0.0.1"
$smtpPort = 3025

$message = New-Object System.Net.Mail.MailMessage
$message.From = $From
$null = $message.To.Add($To)
$message.Subject = $Subject
$message.Body = $Body
$message.IsBodyHtml = $false

$client = New-Object System.Net.Mail.SmtpClient($smtpHost, $smtpPort)
$client.DeliveryMethod = [System.Net.Mail.SmtpDeliveryMethod]::Network
$client.EnableSsl = $false
$client.Send($message)

Write-Host "[OK] Correo de prueba enviado a $To con subject '$Subject'."
