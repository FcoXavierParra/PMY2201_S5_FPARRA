# Veterinaria · Integrando Kotlin en Android Studio

**Actividad:** Semana 5 · Actividad formativa individual · Desarrollo App Móviles I (PMY2201)
**Estudiante:** Francisco Parra
**Estado:** entrega en preparación. Las evidencias E1 a E6 están pendientes (sección 8).

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
├── app/          Entrada: MainActivity, recurso de bienvenida
├── ui/           Pantalla Compose (PantallaVeterinaria) y VeterinariaViewModel, tema
├── datos/        Dominio: modelo (Cliente, Mascota, Producto, Pedido, Consulta…), servicio Veterinaria, DatosIniciales
└── utilidades/   Funciones generales: validaciones, formato de montos, Resultado (manejo de errores)
```

**Dependencias:** `app → ui → datos → utilidades` y `ui → utilidades`. No hay ciclos.

**Decisión de diseño:** en la semana 4, `utils` dependía de `modelo` (cálculos y reflexión sobre `Producto`) y `modelo` dependía de `utils`. Eso impedía separar `utilidades` en un módulo propio. En esta entrega, `utilidades` solo contiene funciones sin dependencias del dominio, y la lógica que depende del dominio vive en `datos`.

**Estado:** la `Veterinaria` vive en `VeterinariaViewModel`, no en `remember`, por lo que sobrevive a la rotación de pantalla. La UI observa un `LiveData<EstadoVeterinaria>`.

## 4. Gradle

- `settings.gradle.kts` declara `:app`, `:ui`, `:datos` y `:utilidades`.
- `gradle/libs.versions.toml` agrega: ViewModel de Compose, LiveData, `runtime-livedata` de Compose y Kotlin Coroutines.
- Compose se gestiona con el BOM, por lo que no se fijan versiones individuales de Compose.

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

**Pendiente de verificar:** la ejecución en el emulador (sección 8).

## 6. Cumplimiento de la actividad

| Requisito | Dónde se cumple |
|---|---|
| Paso 1 · Módulos datos, UI y utilidades | Sección 3 |
| Paso 2 · Gradle con Compose, ViewModel, LiveData y Coroutines | `gradle/libs.versions.toml`, `ui/build.gradle.kts`, `VeterinariaViewModel` |
| Paso 3 · Pantalla inicial con Compose | `ui/PantallaVeterinaria.kt` |
| Paso 4 · MainActivity conecta lógica y UI | `app/MainActivity.kt` |
| Paso 5 · Resumen con clientes, pedidos y último cliente | Bloque "Resumen" de la pantalla; reglas en sección 2 |
| Paso 6 · Pruebas en AVD | Sección 8 (E3 y E4) |
| Paso 7 · Entrega con capturas | Sección 8 |

**Mascotas, dueños y consultas:** la pantalla muestra las listas de dueños, mascotas y consultas. Cada mascota tiene un botón "Registrar consulta", que agrega una consulta y actualiza la lista.

## 7. Limitaciones conocidas

- Los datos viven en memoria: al cerrar la app se pierden. No hay base de datos.
- Las operaciones de la veterinaria son síncronas y en memoria. Las Coroutines se usan para lanzar las acciones desde el ViewModel; no hay trabajo en segundo plano real.
- No hay pruebas instrumentadas (`androidTest`) del flujo de pantalla.
- La cantidad máxima por producto en la pantalla es 10.

## 8. Evidencias

Las capturas se guardan en `evidencias/`. Todavía no están tomadas.

| # | Archivo | Qué debe mostrar |
|---|---|---|
| E1 | `evidencias/E1_estructura_modular.png` | Project view de Android Studio con los cuatro módulos y `settings.gradle.kts` |
| E2 | `evidencias/E2_gradle_sync.png` | Sincronización de Gradle sin errores |
| E3 | `evidencias/E3_estado_inicial.png` | Pantalla en el emulador con el resumen inicial: 2 clientes, 3 pedidos, último cliente María López |
| E4 | `evidencias/E4_resumen_actualizado.png` | Después de registrar un cliente nuevo y un pedido despachado: los contadores cambian sin reiniciar la app |
| E5 | `evidencias/E5_pruebas.png` | `gradlew testDebugUnitTest` con 19 pruebas en verde |
| E6 | `evidencias/E6_mascotas_duenos_consultas.png` | Listas de dueños, mascotas y consultas en la pantalla, con una consulta registrada |

## 9. Retroalimentación docente

La retroalimentación docente de la semana 4 aún no se ha recibido. Si cambia algo de la base técnica, se incorporará en una revisión posterior de esta entrega.
