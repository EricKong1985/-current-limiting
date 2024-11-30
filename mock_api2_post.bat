@echo off
setlocal enabledelayedexpansion

set "url=http://localhost:9000/current-limiting/api2"  REM 确保URL正确
set "token=user1_token"  REM 替换为有效的token
set "data={\"key\":\"value\"}"  REM 你需要传递的POST数据，按照实际情况修改

for /L %%i in (1,1,10001) do (
    echo Sending POST request %%i...
    curl -X POST -H "token: %token%" -H "Content-Type: application/json" -d "%data%" %url%
    if !errorlevel! neq 0 (
        echo Request %%i failed!
    ) else (
        echo Request %%i succeeded!
    )
)

echo All requests sent.
endlocal
