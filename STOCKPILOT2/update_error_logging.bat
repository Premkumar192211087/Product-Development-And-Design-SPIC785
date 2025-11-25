@echo off
setlocal enabledelayedexpansion

echo StockPilot Error Logging Update Script
echo =====================================
echo.
echo This script will update all Java files in the project to use the ErrorLogger class.
echo.

set JAVA_DIR=app\src\main\java\com\example\stockpilot

echo Scanning for Java files in %JAVA_DIR%...
echo.

set FILE_COUNT=0

for /r "%JAVA_DIR%" %%f in (*.java) do (
    set /a FILE_COUNT+=1
    echo Processing: %%~nxf
    
    rem Create a temporary file
    type nul > "%%f.tmp"
    
    rem Check if the file already imports ErrorLogger
    findstr /c:"import com.example.stockpilot.utils.ErrorLogger" "%%f" > nul
    if errorlevel 1 (
        rem Add import if not already present
        set IMPORT_ADDED=0
        
        for /f "tokens=*" %%l in ('type "%%f"') do (
            echo %%l | findstr /c:"^import " > nul
            if not errorlevel 1 (
                if !IMPORT_ADDED!==0 (
                    echo %%l >> "%%f.tmp"
                    echo import com.example.stockpilot.utils.ErrorLogger; >> "%%f.tmp"
                    set IMPORT_ADDED=1
                ) else (
                    echo %%l >> "%%f.tmp"
                )
            ) else (
                if !IMPORT_ADDED!==0 (
                    if "%%l"=="package com.example.stockpilot;" (
                        echo %%l >> "%%f.tmp"
                    ) else if "%%l"=="package com.example.stockpilot.adapters;" (
                        echo %%l >> "%%f.tmp"
                    ) else if "%%l"=="package com.example.stockpilot.models;" (
                        echo %%l >> "%%f.tmp"
                    ) else if "%%l"=="package com.example.stockpilot.utils;" (
                        echo %%l >> "%%f.tmp"
                    ) else (
                        echo %%l >> "%%f.tmp"
                    )
                ) else (
                    echo %%l >> "%%f.tmp"
                )
            )
        )
    ) else (
        rem Import already exists, just copy the file
        type "%%f" > "%%f.tmp"
    )
    
    rem Replace the original file with the modified one
    move /y "%%f.tmp" "%%f" > nul
)

echo.
echo Processed %FILE_COUNT% Java files.
echo.
echo Next steps:
echo 1. Open each Java file that contains error handling code
echo 2. Replace direct Log.e calls with ErrorLogger.logError
echo 3. Replace network error logging with ErrorLogger.logNetworkError
echo 4. Replace API error logging with ErrorLogger.logApiError
echo 5. Add TAG constant if missing: private static final String TAG = "ClassName";
echo.
echo See ErrorLoggerUpdater.java for examples of how to update the code.
echo.
echo Done!

pause