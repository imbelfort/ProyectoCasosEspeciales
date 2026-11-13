## Compilar

Ejecutar desde PowerShell, ubicándose en la carpeta raíz del proyecto:

```powershell
$classpath = (Get-ChildItem .\lib\*.jar | ForEach-Object { $_.FullName }) -join ";"
$sources = Get-ChildItem .\src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

New-Item -ItemType Directory -Force .\build\classes | Out-Null
javac -encoding UTF-8 -source 17 -target 17 -cp $classpath -d .\build\classes $sources
```

## Ejecutar

Después de compilar, ejecutar la consola con:

```powershell
$classpath = (Get-ChildItem .\lib\*.jar -File | ForEach-Object { $_.FullName }) -join ";"
java -cp ".\build\classes;$classpath" proyectomailgrupo01sa.Consola
```
