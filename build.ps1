# Скрипт для сборки плагина

Write-Host "--- Начинаю сборку плагина H-Manhunt ---" -ForegroundColor Cyan

# 1. Компиляция через Maven
mvn clean package

if ($LASTEXITCODE -ne 0) {
    Write-Host "!!! Ошибка при сборке плагина !!!" -ForegroundColor Red
    exit $LASTEXITCODE
}

# 2. Поиск скомпилированного файла
$jarFile = Get-ChildItem "target\H-Manhunt-*.jar" | Where-Object { $_.Name -notlike "*original-*" } | Select-Object -First 1

if ($null -eq $jarFile) {
    Write-Host "!!! Не удалось найти скомпилированный JAR файл в папке target !!!" -ForegroundColor Red
    exit 1
}

Write-Host "--- Сборка завершена успешно! ---" -ForegroundColor Green
Write-Host "--- Файл: $($jarFile.FullName) ---" -ForegroundColor Cyan
