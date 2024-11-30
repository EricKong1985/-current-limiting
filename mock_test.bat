@echo off
setlocal enabledelayedexpansion

set "url=http://localhost:9000/current-limiting/api1"  REM 确保URL正确
set "token=user1_token"  REM 替换为有效的token

for /L %%i in (1,1,10001) do (
    echo Sending request %%i...
    curl -H "token: %token%" %url%
    if !errorlevel! neq 0 (
        echo Request %%i failed!
    ) else (
        echo Request %%i succeeded!
    )
)

echo All requests sent.
endlocal