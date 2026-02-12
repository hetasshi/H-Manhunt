# Скрипт для сборки плагина и копирования его в папку plugins

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

Write-Host "--- Файл найден: $($jarFile.Name) ---" -ForegroundColor Green

# 3. Копирование в папку plugins
$targetDir = "..\plugins"
Write-Host "--- Копирую в $targetDir ---" -ForegroundColor Yellow

$pomVersion = Select-String -Path "pom.xml" -Pattern "<version>([^<]+)</version>" | Select-Object -First 1
$version = if ($pomVersion) { $pomVersion.Matches[0].Groups[1].Value } else { "unknown" }
$pomAuthor = Select-String -Path "pom.xml" -Pattern "<author>([^<]+)</author>" | Select-Object -First 1
$author = if ($pomAuthor) { $pomAuthor.Matches[0].Groups[1].Value } else { "unknown" }
Copy-Item $jarFile.FullName -Destination "$targetDir\H-Manhunt-$version-$author.jar" -Force

Write-Host "--- Готово! Теперь перезагрузи сервер или введи /reload ---" -ForegroundColor Green
