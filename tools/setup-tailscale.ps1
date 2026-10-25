# Run as Administrator.
# Installs Tailscale (if missing) and opens inbound TCP 8080 so the phone can
# reach the database server over the Tailscale network.

$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$setup = Join-Path $here 'tailscale-setup.exe'
$ts = 'C:\Program Files\Tailscale\tailscale.exe'

$principal = New-Object System.Security.Principal.WindowsPrincipal(
    [System.Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([System.Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host 'Please run this script as Administrator (right-click -> Run as administrator).' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $ts)) {
    Write-Host 'Installing Tailscale...'
    Start-Process -FilePath $setup -ArgumentList '/quiet' -Wait
    Start-Sleep -Seconds 5
}
if (-not (Test-Path $ts)) {
    Write-Host 'Automatic install did not complete. Run tailscale-setup.exe manually, then run this script again.' -ForegroundColor Yellow
}

$name = 'Udmurt Kyl server 8080'
Get-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue | Remove-NetFirewallRule
New-NetFirewallRule -DisplayName $name -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Any | Out-Null
Write-Host 'Firewall rule added for TCP 8080 (all profiles).'

if (Test-Path $ts) {
    Write-Host ''
    Write-Host 'Now open the Tailscale tray icon and log in (Sign in).'
    Write-Host 'After logging in, this PC Tailscale address is:'
    try { & $ts ip -4 } catch { Write-Host '  (not logged in yet / no address available)' }
}
