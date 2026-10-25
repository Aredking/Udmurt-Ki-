# Run as Administrator.
# 1) Opens inbound TCP 8080 for the "Udmurt Kyl" database server in all network
#    profiles, so a phone can reach it (including over the Tailscale interface).
# 2) Prints this PC's Tailscale address to enter in the phone app.

$ErrorActionPreference = 'Stop'
$port = 8080

$principal = New-Object System.Security.Principal.WindowsPrincipal(
    [System.Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([System.Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host 'Please run this script as Administrator.' -ForegroundColor Red
    exit 1
}

$name = "Udmurt Kyl server $port"
Get-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue | Remove-NetFirewallRule
New-NetFirewallRule -DisplayName $name -Direction Inbound -Protocol TCP `
    -LocalPort $port -Action Allow -Profile Any | Out-Null
Write-Host "Firewall rule added: $name (TCP $port, all profiles)." -ForegroundColor Green

$ts = $null
$cmd = Get-Command tailscale -ErrorAction SilentlyContinue
if ($cmd) { $ts = $cmd.Source }
elseif (Test-Path 'C:\Program Files\Tailscale\tailscale.exe') {
    $ts = 'C:\Program Files\Tailscale\tailscale.exe'
}

if ($ts) {
    Write-Host ''
    Write-Host 'This PC Tailscale address (use it in the phone app):' -ForegroundColor Cyan
    & $ts ip -4
} else {
    Write-Host ''
    Write-Host 'Tailscale not found. Install it first: https://tailscale.com/download' -ForegroundColor Yellow
}
