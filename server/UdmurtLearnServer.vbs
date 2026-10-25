' Udmurt Kyl server autostart (hidden, auto-restart).
' Starts the SQLite-backed HTTP server at user logon.
Option Explicit

Dim shell, javaPath, workDir, jarPath, cmd
Set shell = CreateObject("WScript.Shell")

javaPath = "C:\Program Files\Java\jdk-23\bin\java.exe"
workDir  = "D:\Udmurt Ki"
jarPath  = "server\dist\udmurt-server-1.0.0-all.jar"

cmd = "cmd /c cd /d """ & workDir & """ && """ & javaPath & """ -jar """ & jarPath & """ >> ""server\server.log"" 2>&1"

Do
    shell.Run cmd, 0, True
    WScript.Sleep 5000
Loop
