# Veterinaria · Integrando Kotlin en Android Studio

**Actividad:** Semana 5 · Actividad formativa individual · Desarrollo App Móviles I (PMY2201)
**Estudiante:** Francisco Parra
**Estado:** entrega formativa. Ejecutada y verificada en emulador (Pixel 4, API 37). Pendiente: retroalimentación docente de la semana 4.

Aplicación Android en Kotlin para la gestión de una veterinaria: dueños (clientes), mascotas, productos o servicios, pedidos y consultas. Organizada en módulos Gradle, con interfaz en Jetpack Compose, ViewModel y LiveData. La pantalla inicial muestra un resumen que se actualiza al registrar clientes y pedidos.

---

## 1. Caso y base de trabajo

- **Caso de negocio:** gestión de los procesos de una veterinaria, según la descripción de la actividad de S5.
- **Base técnica:** se reutiliza la arquitectura y la base técnica de la semana 4 (`PizzeriaApp`): paquetes por capa, `Registro` sin duplicados, validaciones de correo y teléfono, formato de montos, Compose, Gradle con catálogo de versiones y pruebas unitarias. Las entidades y reglas de negocio de la pizzería se adaptan al dominio veterinario; no se reconstruye la app desde cero.
- **Entregables previos:** no se consideraron. S4 es la base más reciente y completa de estructura.

## 2. Supuestos de negocio

Estos supuestos definen cómo se calcula el resumen. Están implementados en `datos` y probados.

| # | Supuesto | Implementación |
|---|---|---|
| S1 | **Total de clientes registrados:** cantidad de dueños distintos. Dos registros con el mismo nombre y correo (sin importar mayúsculas) cuentan como un solo cliente. | `Veterinaria.registrarCliente` reutiliza el cliente existente y actualiza su teléfono y dirección. |
| S2 | **Pedidos incluidos en el total:** los 3 pedidos de ejemplo de la jornada (dos de Juan Pérez y uno de María López) se cuentan como pedidos realizados. Se cargan como ya despachados, sin volver a descontar stock. | `DatosIniciales` y `Veterinaria.registrarPedidoPrevio`. |
| S3 | **Último cliente:** el último cliente **nuevo** registrado. Reutilizar un cliente existente no lo convierte en el último. Con los datos de ejemplo, el último es María López. | `Veterinaria.ultimoClienteNuevo`. |
| S4 | **Pedido realizado:** un pedido cuenta solo cuando queda **despachado**, es decir, cuando hay stock de todos sus productos. Si falta stock, el pedido queda pendiente: no descuenta inventario, no suma al total y la pantalla informa qué producto falta. El cliente sí queda registrado, como en la semana 4. | `Veterinaria.confirmarPedido`, `EstadoPedido`. |

Además, el IVA es del 19 % sobre el subtotal, redondeado al peso.

## 3. Arquitectura modular

```
PizzeriaApp/
├── app/          Entrada: MainActivity (conecta ViewModel y pantalla), recurso de bienvenida
├── ui/           Pantalla Compose (PantallaVeterinaria, sin estado propio), VeterinariaViewModel, tema
├── datos/        Dominio: modelo (Cliente, Mascota, Producto, Pedido, Consulta…), servicio Veterinaria, DatosIniciales
└── utilidades/   Funciones generales: validaciones, formato de montos, Resultado (manejo de errores)
```

**Dependencias:** `app → ui → datos → utilidades` y `ui → utilidades`. No hay ciclos.

**Decisión de diseño:** en la semana 4, `utils` dependía de `modelo` (cálculos y reflexión sobre `Producto`) y `modelo` dependía de `utils`. Eso impedía separar `utilidades` en un módulo propio. En esta entrega, `utilidades` solo contiene funciones sin dependencias del dominio, y la lógica que depende del dominio vive en `datos`.

**Flujo de datos:**
- `VeterinariaViewModel` contiene la `Veterinaria` (no en `remember`), por lo que sobrevive a la rotación de pantalla. Publica un `LiveData<EstadoVeterinaria>`.
- `MainActivity` crea el ViewModel, observa el LiveData y pasa a `PantallaVeterinaria` el estado y las acciones del usuario (confirmar pedido, registrar consulta).
- `PantallaVeterinaria` no tiene estado de negocio: solo muestra lo que recibe y reporta las acciones.

## 4. Gradle

- `settings.gradle.kts` declara `:app`, `:ui`, `:datos` y `:utilidades`.
- `gradle/libs.versions.toml` agrega: ViewModel de Compose, LiveData, `runtime-livedata` de Compose y Kotlin Coroutines.
- Compose se gestiona con el BOM, por lo que no se fijan versiones individuales de Compose.
- `app` declara directamente `datos` y LiveData porque `MainActivity` los usa; las dependencias de `implementation` de `ui` no se heredan.

## 5. Pruebas

Ejecutar desde la raíz del proyecto:

```
gradlew testDebugUnitTest
```

| Módulo | Archivo | Pruebas | Resultado |
|---|---|---|---|
| utilidades | `FormatoTest` | 1 | Pasan |
| utilidades | `ValidacionesTest` | 5 | Pasan |
| datos | `VeterinariaTest` | 13 | Pasan |
| **Total** | | **19** | **Pasan** |

Las pruebas de `VeterinariaTest` cubren los supuestos S1 a S4, el registro y la actualización de clientes, el registro de mascotas y consultas, el despacho con y sin stock, y el cálculo del IVA.

**Compilación:** `gradlew assembleDebug` genera `app-debug.apk` sin errores.

**Ejecución en emulador:** la app se instaló y abrió en un Pixel 4 con API 37, sin errores en el log. Se verificó el resumen inicial, su actualización al registrar un cliente nuevo con un pedido despachado, y el registro de una consulta (sección 8).

## 6. Cumplimiento de la actividad y de la rúbrica

| Requisito de la actividad | Dónde se cumple | Evidencia |
|---|---|---|
| Paso 1 · Módulos datos, UI y utilidades | Sección 3 | E1 |
| Paso 2 · Gradle con Compose, ViewModel, LiveData y Coroutines | `gradle/libs.versions.toml`, `ui/build.gradle.kts`, `VeterinariaViewModel` | E2 |
| Paso 3 · Pantalla inicial con Compose | `ui/PantallaVeterinaria.kt` | E3 |
| Paso 4 · MainActivity conecta lógica y UI | `app/MainActivity.kt` | E3, E4 |
| Paso 5 · Resumen con clientes, pedidos y último cliente | Bloque "Resumen"; reglas en sección 2 | E3, E4 |
| Paso 6 · Pruebas en AVD | Sección 8 | E3, E4, E6 |
| Paso 7 · Entrega con capturas | Sección 8 y carpeta `evidencias/` | Todas |

**Rúbrica de la pauta formativa:**

| Criterio | Cómo se cubre |
|---|---|
| Reestructuración y modularización | Cuatro módulos con responsabilidades separadas, sin ciclos (sección 3) |
| Configuración de Gradle | Dependencias de Compose, ViewModel, LiveData y Coroutines en el catálogo y en los módulos que las usan (sección 4) |
| Diseño de interfaz | Compose con Material 3: tarjeta de resumen, formulario, listas y botones con estados habilitado y deshabilitado |
| Conexión lógica-UI | `MainActivity` observa el ViewModel y reenvía las acciones; la pantalla no tiene lógica de negocio (sección 3) |
| Mensaje de resumen | Clientes, pedidos y último cliente, actualizados al registrar datos (E3, E4) |
| Pruebas | Ejecución sin errores en el emulador (E3 a E6) y 19 pruebas unitarias en verde (E5) |
| Entrega | Este README, capturas y registros en `evidencias/`, y el proyecto en el repositorio |

## 7. Limitaciones conocidas

### 7.1 Los datos están en memoria

Clientes, mascotas, consultas y pedidos existen solo mientras la app está abierta, en la memoria del teléfono. Al cerrar la app (o al quitarla de recientes y el sistema la detiene), todo vuelve a los datos de ejemplo de `DatosIniciales`.

- Girar la pantalla **no** los pierde: el `VeterinariaViewModel` los conserva.
- Cerrar la app **sí** los pierde.

**Por qué:** la actividad no pide persistencia, y la base de S4 tampoco la tenía. **Qué implicaría:** una base de datos local (por ejemplo Room) y un repositorio con funciones `suspend`. Ese cambio es el que daría sentido real a las Coroutines (ver 7.2).

### 7.2 Las Coroutines no hacen trabajo asíncrono real

La actividad pide Kotlin Coroutines "para el manejo eficiente de operaciones asincrónicas". En esta versión esa parte no se cumple de verdad:

- Registrar un cliente, confirmar un pedido o agregar una consulta son operaciones en memoria, que tardan microsegundos. No hay espera ni acceso a disco ni a red.
- Las Coroutines solo envuelven cada acción con `viewModelScope.launch`. La dependencia está, pero no resuelve ningún problema concreto.

Esto es una **contradicción con el requerimiento**. Se resolvería con la persistencia de 7.1: las operaciones pasarían a ser `suspend` y correrían en `Dispatchers.IO`, sin bloquear la pantalla.

### 7.3 No hay pruebas instrumentadas de la pantalla

Hay dos tipos de pruebas automáticas en Android:

| Tipo | Dónde corre | Qué verifica | Estado en este proyecto |
|---|---|---|---|
| **Unitarias** (`testDebugUnitTest`) | En la computadora, sin emulador | Reglas de negocio: IVA, stock, validaciones, supuestos S1 a S4 | 19 pruebas, en verde |
| **Instrumentadas** (`androidTest`) | Dentro de un emulador o teléfono real | La pantalla: escribir en los campos, presionar botones y verificar el texto que aparece (con `createComposeRule`) | **No hay.** Solo está la plantilla `ExampleInstrumentedTest`, que no prueba la app |

**Consecuencia:** la pantalla se verificó a mano en el emulador (sección 8). Si un cambio rompe un botón o deja de mostrar el resumen, ninguna prueba automática lo detecta.

La guía de S5 y la pauta no exigen pruebas instrumentadas: el Paso 6 pide ejecutar la app en el emulador y verificar la integración y la actualización de datos, y eso es lo que se documenta en la sección 8.

### 7.4 Otras limitaciones

- La cantidad máxima por producto en la pantalla es 10.
- Los pedidos pendientes (sin stock) no se guardan; solo se informa el faltante en pantalla.
- Las capturas se tomaron con `adb` sobre el emulador. E1 y E2 son registros generados desde el proyecto, no capturas de Android Studio (sección 8).

## 8. Evidencias

Ejecutadas en emulador **Pixel 4, API 37** (AVD `Pixel_4`), con el APK de depuración instalado con `adb install`.

| # | Archivo | Qué muestra | Estado |
|---|---|---|---|
| E1 | `evidencias/E1_estructura_modular.txt` | Módulos de `settings.gradle.kts` y archivos fuente de cada módulo | Generado desde el proyecto. No es captura de Android Studio |
| E2 | `evidencias/E2_gradle_build.txt` | Resultado de `gradlew testDebugUnitTest assembleDebug` | `BUILD SUCCESSFUL`. Las tareas ya estaban actualizadas, así que el log es corto |
| E3 | `evidencias/E3_estado_inicial.png` | Pantalla inicial en el emulador: 2 clientes, 3 pedidos, último cliente María López | Captura del emulador |
| E4 | `evidencias/E4_resumen_actualizado.png` | Tras registrar a Pedro Soto con un pedido despachado: 3 clientes, 4 pedidos, último cliente Pedro Soto, sin reiniciar la app | Captura del emulador |
| E5 | `evidencias/E5_pruebas.txt` | Reporte de las 19 pruebas unitarias, todas en verde | Generado desde los reportes XML |
| E6 | `evidencias/E6_mascotas_duenos_consultas.png` | Listas de dueños (3), mascotas (2) y consultas (2), con la consulta nueva de Michi al final | Captura del emulador |

Para E4 y E6 se usaron entradas reales en la app: nombre, correo, teléfono y dirección de un cliente nuevo, una unidad de consulta general, y el botón "Registrar consulta" de Michi.

## 9. Retroalimentación docente

La retroalimentación docente de la semana 4 aún no se ha recibido. Si cambia algo de la base técnica, se incorporará en una revisión posterior de esta entrega. Las recomendaciones para la siguiente evaluación sumativa se incorporarán cuando estén disponibles.
