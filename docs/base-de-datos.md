# Base de datos local — Room (SQLite)

MusselApp guarda sus datos en una base **SQLite** local usando **Room 2.8**.
Archivo: `musselapp.db`, en el almacenamiento privado de la app.
Esquema exportado: [`app/schemas/`](../app/schemas/) (se genera al compilar).

## Modelo entidad-relación

```mermaid
erDiagram
    CENTROS ||--o{ TRENES : tiene
    TRENES ||--o{ LINEAS : tiene
    LINEAS ||--o{ MUESTRAS : "se muestrea en"
    USUARIOS ||--o{ MUESTRAS : "registra (operario)"
    MUESTRAS ||--o{ REVISIONES : "recibe"
    USUARIOS ||--o{ REVISIONES : "dictamina (supervisor)"

    CENTROS {
        TEXT id PK
        TEXT nombre
    }
    TRENES {
        INTEGER id PK
        TEXT centro_id FK
        TEXT codigo "A, B, C"
    }
    LINEAS {
        INTEGER id PK
        INTEGER tren_id FK
        INTEGER numero "1 a 12"
    }
    USUARIOS {
        TEXT rut PK "normalizado: 111111111"
        TEXT nombre
        TEXT rol "OPERARIO / SUPERVISOR"
        TEXT pin
    }
    MUESTRAS {
        INTEGER id PK
        INTEGER linea_id FK
        TEXT operador_rut FK
        INTEGER profundidad_m
        INTEGER talla_mm
        INTEGER conteo_ind_m
        TEXT observacion
        TEXT estado "PENDIENTE / OBSERVADO / CORREGIDO / VALIDADO"
        INTEGER registrada_en "epoch ms"
        INTEGER sincronizada "0 / 1"
        TEXT foto_ruta "ruta del archivo, no la imagen"
    }
    REVISIONES {
        INTEGER id PK
        INTEGER muestra_id FK
        TEXT supervisor_rut FK
        TEXT estado_asignado
        TEXT comentario
        INTEGER revisada_en
    }
```

| Tabla | Modelo de la pauta | Descripción |
|---|---|---|
| `centros` | Centro | Centros de cultivo (datos ficticios) |
| `trenes` | Tren | Trenes de cultivo (longlines) de cada centro |
| `lineas` | Linea | Líneas de siembra de cada tren |
| `usuarios` | Usuario | Operarios y supervisores ficticios |
| `muestras` | Muestra, Conteo | Cada muestreo; el conteo de individuos es la columna `conteo_ind_m` |
| `revisiones` | EstadoRevision | Historial de dictámenes del supervisor (estado + comentario) |

## Decisiones de diseño

- **Fotos:** la imagen **no** se guarda en la base (una foto pesa varios MB y SQLite lee las filas en bloques de ~2 MB). Se guarda como archivo privado de la app y la tabla solo guarda `foto_ruta`.
- **Historial de revisiones:** cada dictamen es una fila nueva en `revisiones`, para mantener la trazabilidad. El estado vigente queda también en `muestras.estado`, y ambos se actualizan en una sola transacción (`MuestraDao.registrarRevision`).
- **Integridad:** claves foráneas entre todas las tablas. No se puede registrar una muestra en una línea o con un operario que no existe.
- **Datos iniciales:** al crear la base por primera vez se cargan centros, trenes A–C, líneas 1–12, los 2 usuarios de prueba y 14 muestras ficticias (`DatosIniciales.kt`).
- **PIN en texto plano:** aceptable solo porque es un MVP académico con usuarios ficticios.
- **Migraciones:** durante el desarrollo se usa `fallbackToDestructiveMigration` (si cambia el esquema, la base se recrea).
- **Un solo dispositivo:** Room es local; operario y supervisor deben usar el mismo teléfono en la demo. La columna `sincronizada` y la interfaz `MuestraRepository` dejan listo el camino para sincronizar con un servidor en una etapa futura.

## Estructura del código

```
data/local/
├── MusselDatabase.kt     # @Database, versión y creación
├── DatosIniciales.kt     # datos ficticios iniciales
├── Mapeos.kt             # filas de Room → modelos de las pantallas
├── entidad/              # @Entity: Centro, Tren, Linea, Usuario, Muestra, Revision
└── dao/                  # @Dao: CatalogoDao, UsuarioDao, MuestraDao
AppContainer.kt           # crea la base una sola vez para toda la app
MusselApplication.kt      # registra el contenedor (android:name en el Manifest)
```

## Plan de integración

| Fase | Qué | Responsable | Estado |
|---|---|---|---|
| 1 | Room, entidades, DAO, base, datos iniciales y pruebas | Ramón | ✅ esta rama |
| 2a | `RoomMuestraRepository`: listar, obtener, agregar, sincronizar; conectar Principal e Historial | Ramón | Pendiente |
| 2b | `actualizarEstado` con `registrarRevision`; conectar Detalle y Revisión | Lucas | Pendiente |
| 2c | Guardar la muestra desde el ViewModel (hoy se hace en el `NavHost`) y la foto en `filesDir/fotos/` | Jesús | Pendiente |
| 3 | `RoomAuthRepository`: login contra la tabla `usuarios` | Ramón | Pendiente |
| 4 | Prueba completa: registrar, cerrar la app, reabrir y ver la muestra; README | Todos | Pendiente |

Para ver la base en Android Studio: **App Inspection → Database Inspector** con la app corriendo.

## Pruebas

`app/src/androidTest/.../MusselDatabaseTest.kt` (base en memoria, corre en un dispositivo):
datos iniciales, consulta con joins y última revisión, registro de revisiones con historial,
inserción de muestra nueva y rechazo por clave foránea.
