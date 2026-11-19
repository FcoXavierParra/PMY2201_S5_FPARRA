# Propuesta S5 · Integrando Kotlin en Android Studio

**Asignatura:** Desarrollo App Móviles I (PMY2201) · Exp 2 · Semana 5
**Base de trabajo:** proyecto `PizzeriaApp` de S4 (repositorio en `S4_Formativa3/PizzeriaApp`, último commit `b753610`)
**Estado:** PROPUESTA PENDIENTE DE VALIDACIÓN. No se modificó código.
**Retroalimentación docente de S4:** pendiente. Esta propuesta no la incorpora; ver sección 8.

**Caso de negocio adoptado (según S5):** gestión de los procesos de una **veterinaria**: clientes (dueños), productos y pedidos, con resumen de clientes registrados, pedidos realizados y último cliente. La entrega de S5 menciona mascotas, dueños y consultas. S5 prevalece sobre la base de S4, que es una pizzería.

**Qué se reutiliza de S4:** la arquitectura y la base técnica (paquetes `modelo`, `servicio`, `datos`, `utils`, `ui`; Compose; Gradle con version catalog; patrón de registro y validaciones; pruebas unitarias). **Qué se reemplaza:** las entidades y reglas de negocio de la pizzería (productos, pizzas, inventario, repartidores, zonas), que se sustituyen por las de la veterinaria.

---

## 1. Diagnóstico de la base S4

| Aspecto | Situación actual | Consecuencia para S5 |
|---|---|---|
| Módulos | Un solo módulo `:app` con paquetes `modelo`, `servicio`, `datos`, `utils`, `ui` | Hay que separarlo en módulos Gradle reales (S5 Paso 1) |
| Arquitectura y base técnica | Paquetes por capa, `Registro` sin duplicados, validaciones, Compose, Gradle con version catalog, 45 pruebas unitarias | Se reutiliza la estructura y el patrón. Las entidades de pizzería se reemplazan por las de la veterinaria |
| Estado de la UI | `PantallaPizzeria` crea `DatosIniciales()` con `remember` dentro de la composición | Al rotar la pantalla se pierde el estado. Hay que moverlo a un ViewModel |
| Pedidos | `Pizzeria` no guarda los pedidos confirmados; los 3 pedidos previos se descuentan del stock pero no se registran | Falta un registro de pedidos para contar "pedidos realizados" |
| Clientes | `Registro<Cliente>` conserva orden de inserción (`LinkedHashSet`) | "Último cliente" se puede obtener sin cambios, con una regla explícita (ver V3) |
| ViewModel / LiveData | No existen | Se agregan (exigidos por S5 Paso 2) |

---

## 2. Inconsistencias detectadas en los documentos de S5

1. **Caso de negocio.** La descripción de la actividad dice que la app gestiona "los procesos de una veterinaria", y la entrega (Paso 7) pide "datos de mascotas, dueños y consultas". S4 y el código actual son de una pizzería. Se adopta el caso de S5 (veterinaria). Consecuencia: la lógica de la pizzería no sirve tal cual; hay que reconstruir el modelo veterinario (ver V1).
2. **Cantidad de módulos.** La guía (caso FitTrack) usa dos módulos (`app` y `core`). Las instrucciones piden tres (datos, UI, utilidades). Esta propuesta sigue las instrucciones.
3. **Dependencias Compose.** El Paso 2 muestra `androidx.compose.ui:ui:<latest_version>`. Con el Compose BOM ya definido en S4, la versión la fija el BOM y no se escribe a mano.
4. **ViewModel y LiveData.** Las instrucciones los exigen, pero la guía no los desarrolla. Esta propuesta los implementa sobre la base que ya existe.

---

## 3. Arquitectura modular propuesta

```
PizzeriaApp/
├── settings.gradle.kts         include(":app", ":datos", ":ui", ":utilidades")
├── app/                        Entrada: MainActivity, tema, recurso mensaje_bienvenida
├── ui/                         Pantallas Compose y ResumenViewModel
├── datos/                      Modelo de dominio, Pizzeria, registro de pedidos, datos iniciales
└── utilidades/                 Validaciones, Formato, errores genéricos (sin dependencias)
```

**Dependencias:** `app → ui → datos → utilidades` y `ui → utilidades`.

### Decisión clave: mover `Calculos` y `Reflexion` a `datos`

Se verificó en el código que existe un ciclo:

- `utils/Calculos.kt` y `utils/Reflexion.kt` importan `modelo.Producto` (y `Reflexion` importa `modelo.Promocionable`).
- `modelo/Pedido.kt` y `modelo/Combo.kt` importan `utils.calcularTotales` y `utils.Totales`.

Si `utilidades` fuera módulo aparte, Gradle rechazaría la dependencia circular. La solución es:

| Archivo | Destino | Motivo |
|---|---|---|
| `Validaciones.kt`, `Formato.kt` | `utilidades` | No importan nada del dominio |
| `Calculos.kt` (promociones, IVA, descuentos) | `datos` | Son reglas de negocio sobre `Producto` y `Pedido` |
| `Reflexion.kt` | `datos` | Lee `@Promocionable` de `Producto` |
| Manejo de errores genérico (nuevo) | `utilidades` | Tipo `Resultado` reutilizable |

Con esto `utilidades` queda sin dependencias de otros módulos, que es lo que S5 Paso 1 describe como "funciones generales reutilizables".

---

## 4. Cambios de Gradle

**`gradle/libs.versions.toml`** (agregar):
- Plugin `android-library` (mismo `agp` que ya existe).
- `androidx-lifecycle-viewmodel-compose` y `androidx-lifecycle-livedata-ktx` (ViewModel y LiveData).
- `androidx-compose-runtime-livedata` (para observar LiveData desde Compose).
- `kotlinx-coroutines-android` (Coroutines, Paso 2). Se usa dentro del ViewModel.

Las versiones se toman de la última estable publicada al momento de implementar. El catálogo actual fija `lifecycleRuntimeKtx = 2.6.1`; se debe alinear con las demás librerías de lifecycle.

**Módulos nuevos (`datos`, `ui`, `utilidades`):** cada uno con su `build.gradle.kts`:
- `datos` y `utilidades`: plugin `android-library` (o `kotlin-jvm` si no necesitan Android; `datos` usa `java.time`, por lo que conviene `android-library` con desugaring, igual que en S4).
- `ui`: `android-library` + `kotlin-compose`. Depende de `datos` y `utilidades`.
- `app`: sin cambios de lógica; agrega `implementation(project(":ui"))`.

Los tests se mueven con su código: `CalculosTest`, `PedidoTest`, etc. quedan en `datos/src/test`; `ValidacionesTest` en `utilidades/src/test`.

---

## 5. Pantalla de resumen (S5 Pasos 3 a 5)

**Datos del resumen:**
- Total de clientes registrados: `Pizzeria.cantidadClientes` (ya existe).
- Total de pedidos realizados: nuevo, requiere registro de pedidos en `Pizzeria` (ver V2).
- Último cliente registrado: nuevo `Pizzeria.ultimoClienteRegistrado`, que toma el último elemento de `Registro` (ver V3).

**Componentes:**
- `ResumenViewModel` (en `ui`): recibe la `Pizzeria` como dependencia. Mantiene `LiveData<ResumenPizzeria>` y lo actualiza después de cada registro o despacho. La `Pizzeria` vive en el ViewModel, no en `remember`, para que sobreviva a la rotación.
- `ResumenPizzeria` (data class): `totalClientes`, `totalPedidos`, `ultimoCliente: String?`.
- `PantallaPizzeria`: recibe el resumen observado con `observeAsState` y lo muestra en la parte superior ("Clientes: N · Pedidos: M · Último cliente: X").
- `MainActivity`: obtiene el ViewModel con `viewModel()` y lo pasa a la pantalla. Conserva `Greeting` y el recurso `mensaje_bienvenida`, ya que están en `app`.

**Expectativa inicial** (con V2 y V3 aceptadas):
- Clientes: **2** (Juan Pérez y María López).
- Pedidos: **3** (los pedidos previos de Juan ×2 y María ×1).
- Último cliente: **María López** (el último agregado a `DatosIniciales`).

Al confirmar un pedido nuevo con cliente nuevo, los tres valores deben cambiar sin reiniciar la app. Ese cambio es la evidencia de "actualización dinámica".

---

## 6. Mapa de requisitos → implementación → evidencia

| Requisito S5 | Implementación | Evidencia que debe entregar |
|---|---|---|
| Paso 1 · Módulos datos, UI y utilidades | Sección 3 | Captura del árbol de módulos en Project view |
| Paso 2 · Gradle (Compose, ViewModel, LiveData, Coroutines) | Sección 4 | Captura de `libs.versions.toml` y de `Sync` exitoso |
| Paso 3 · Pantalla inicial con Compose | Pantalla existente, con resumen añadido | Captura de la pantalla en el emulador |
| Paso 4 · MainActivity conecta lógica y UI | `MainActivity` + `ResumenViewModel` | Captura de código de `MainActivity` |
| Paso 5 · Mensaje con clientes, pedidos y último cliente | Sección 5 | Dos capturas: estado inicial y después de registrar un pedido con cliente nuevo |
| Paso 6 · Pruebas en AVD | Emulador Pixel 4 (API 37, como en S4) | Captura de la app ejecutándose; captura de `gradlew testDebugUnitTest` en verde |
| Paso 7 · Entrega | Zip sin `build/` ni `.gradle/` | Ver sección 7 |

---

## 7. Plan de pruebas

- **Migradas (45):** las de S4, reubicadas en su módulo. Deben seguir en verde.
- **Nuevas:**
  - `ResumenViewModel`: registrar un cliente nuevo suma 1 al total; registrar un cliente existente no lo suma.
  - Último cliente: cambia al agregar un cliente nuevo; no cambia al reutilizar uno existente.
  - Pedidos: un pedido confirmado suma 1; un pedido sin stock o sin repartidor no suma (ver V4).
- **Manual en emulador:** estado inicial, registro de un cliente nuevo, rotación de pantalla (el resumen debe conservarse).

---

## 8. Validaciones y evidencias que necesito de ti

### Decisiones que debes validar

| # | Pregunta | Mi recomendación |
|---|---|---|
| **V1** | **Caso adoptado: veterinaria (según S5).** Falta la lógica veterinaria de S1 a S3, que no está en el repositorio. ¿Me compartes esos entregables, o confirmas que reconstruyo el modelo veterinario desde cero (Cliente/dueño, Mascota, Producto, Pedido, Consulta)? | No partir de cero: adaptar el código de S4 al dominio veterinario. `Cliente` pasa a dueño (con nombre, teléfono, correo y dirección), `Producto` pasa a productos/servicios de la veterinaria, `Pedido` se conserva, y se agregan `Mascota` y `Consulta`. Se mantienen `Registro`, `Validaciones`, `Formato`, la estructura de paquetes y el patrón de pruebas |
| **V2** | ¿Los 3 pedidos previos (precargados en `DatosIniciales`) cuentan en "total de pedidos"? | Sí, para que el resumen inicial muestre datos. Se pueden etiquetar como "previos" |
| **V3** | ¿"Último cliente registrado" es el último cliente **nuevo** agregado, o el último cliente que hizo un pedido? | El último cliente nuevo agregado. Reutilizar un cliente no lo convierte en "último" |
| **V4** | ¿Un pedido cuenta como "realizado" si queda pendiente por falta de stock o repartidor? | No. Contar solo los pedidos que llegaron a despacharse |
| **V5** | ¿Aceptas el app + 3 módulos (datos, ui, utilidades) como estructura? El módulo `app` es obligatorio en la plantilla. | Sí |
| **V6** | ¿El trabajo de S5 va sobre el mismo repositorio `PizzeriaApp` (evolución de S4) o en una copia? | El mismo repositorio, en una rama nueva. No commitear sin tu confirmación |
| **V7** | ¿Confirmas que ViewModel + LiveData son obligatorios (según S5 Paso 2) aunque Compose recomienda StateFlow? | Sí, usar ViewModel + LiveData, tal como pide la pauta |

### Evidencias que debes generar tú

Yo no puedo abrir Android Studio ni ejecutar el emulador. Estas capturas las debes tomar tú:

1. **E1 · Estructura modular:** Project view de Android Studio, con los módulos visibles.
2. **E2 · Gradle sync:** resultado de la sincronización sin errores (o `BUILD SUCCESSFUL`).
3. **E3 · Estado inicial:** pantalla en el emulador con el resumen (esperado: 2 clientes, 3 pedidos, María López).
4. **E4 · Dinamismo:** captura después de registrar un cliente nuevo y un pedido, donde se vean los valores cambiados.
5. **E5 · Pruebas:** `gradlew testDebugUnitTest` en verde (o el reporte de Android Studio).
6. **E6 · Caso de negocio:** la captura de V1 (la que el docente valide).

Como el caso es veterinaria, E3 y E4 deben mostrar el resumen del modelo veterinario, y E6 debe mostrar mascotas, dueños y consultas, como pide el Paso 7. Si el docente indica otra cosa, se ajusta.

### Información que necesito para cerrar la propuesta

- **Retroalimentación docente de S4.** Cuando llegue, puede cambiar la base: por ejemplo, `minSdk` (en S4 quedó en 23 porque el asistente de Android Studio no ofrece 21; eso está documentado en el README). Esta propuesta no asume ningún cambio hasta tener el texto de la retroalimentación.
- **Confirmación de V1 y V2** antes de implementar. Son las que más cambian el resultado.

---

## 9. Próximo paso

Con las respuestas a V1 a V7 y la retroalimentación de S4 (o la confirmación de que se puede avanzar sin ella), implemento los módulos, el ViewModel, la pantalla de resumen y las pruebas nuevas en una rama del repositorio. Luego verifico que compila y que las pruebas pasan con `gradlew testDebugUnitTest`, y te entrego la lista de capturas a tomar.
