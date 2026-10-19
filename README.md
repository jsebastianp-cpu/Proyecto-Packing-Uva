# Sistema de trazabilidad para packing de uva

Aplicación interactiva de consola en Java para registrar varias cargas durante una jornada. Solicita los datos de recepción, valida CLP, origen, MTD y LMR, y si la carga queda aprobada solicita cajas por calibre y el peso medido de cada caja. Muestra los pallets y etiquetas que corresponden y conserva cada registro en un archivo diario CSV.

## Requisitos

- JDK 17 o posterior.
- Visual Studio Code con **Extension Pack for Java**.
- Maven para ejecutar desde terminal.

## Abrir y ejecutar en Visual Studio Code

1. Abra en VS Code la carpeta `código`.
2. Espere a que la extensión Java reconozca el proyecto Maven.
3. Abra `src/main/java/RecepcionPackingUva.java` y ejecute `RecepcionPackingUva` desde el método `main`.
4. Responda las preguntas en la terminal. Al final de cada carga, escriba `S` para registrar otra o `N` para cerrar la jornada.

También puede abrir la terminal dentro de esta carpeta y ejecutar:

```bash
mvn compile
java -cp target/classes RecepcionPackingUva
```

Los pesos se ingresan caja por caja. Se acepta coma o punto decimal. El rango permitido es de 8.200 a 8.364 kg, inclusive. Las cargas con CLP vencido, MTD sobre el umbral regional o incumplimiento de LMR quedan registradas con su estado y no pasan a empaque.

## Registro diario

Al iniciar, el programa crea la carpeta `registros` si hace falta y agrega cada carga a `registros/cargas_AAAA-MM-DD.csv`. El archivo conserva los datos de recepción, cantidades por calibre, pesos de cada caja, resultado y etiquetas emitidas; puede abrirse con Excel para revisar o filtrar los registros. El resumen al cerrar muestra todas las cargas ingresadas durante esa sesión.

## Archivos principales

- `src/main/java/Carga.java`: datos de cada carga.
- `src/main/java/RecepcionPackingUva.java`: captura interactiva, validaciones, pesaje, paletizado, resumen y registro CSV.
- `pom.xml`: configuración de Java 17.

## Versión básica para practicar

El archivo `src/main/java/códigoBasico.java` contiene una versión introductoria con `int`, `double`, `String`, `Scanner`, `if`, `else`, `for` y `while`. En VS Code, ejecute la clase `CodigoBasico` desde ese archivo. Esta versión solicita los pesos de las cajas individualmente y permite registrar varias cargas mientras el programa está abierto. Su resumen es temporal; para guardar el registro diario use la versión principal `RecepcionPackingUva`.
