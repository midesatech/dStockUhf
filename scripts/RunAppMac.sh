#!/bin/bash

# --- Execution ---
echo "Running dStockUhf application..."
java --module-path=libs --add-modules javafx.controls,javafx.fxml -cp "libs/*" -jar dStockUhf.jar

# --- Error Handling ---
if [ $? -ne 0 ]; then
    echo "An error occurred. Please check the console output."
    exit 1
else
    echo "dPassFx application finished."
fi