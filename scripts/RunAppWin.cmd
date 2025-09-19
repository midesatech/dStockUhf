@echo off
echo Running dPassFx application...

REM Use PowerShell to start the Java process minimized
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Start-Process 'java' -ArgumentList '--module-path=libs', '--add-modules', 'javafx.controls,javafx.fxml', '-cp', 'libs/*', '-jar', 'dPassFx.jar' -WindowStyle Minimized"

REM The batch file itself will likely close immediately after starting the Java app.