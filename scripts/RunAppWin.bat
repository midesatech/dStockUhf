@echo off

echo Running dPassFx application...
start /min "" java --module-path=libs --add-modules javafx.controls,javafx.fxml -cp "libs/*" -jar dPassFx.jar

if %ERRORLEVEL% NEQ 0 (
    echo An error occurred. Please check the console output.
    pause
) else (
    echo dPassFx application finished.
)