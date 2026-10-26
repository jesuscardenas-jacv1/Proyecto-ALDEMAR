# Protocolo de Pruebas de Aceptación y Guion de Demo (QA)
**Proyecto MusselApp · Caso ALDEMAR SpA**  
**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles · Duoc UC Sede Puerto Montt  
**Responsable QA:** Lucas Saldivia (QA & Data Engineer) · **Equipo:** Pipeño (Sección 004V)

---

## 1. Objetivo del Protocolo
Verificar el correcto funcionamiento de los flujos críticos de **MusselApp** previo a la presentación final ante el docente. Este documento establece los casos de prueba de aceptación (Smoke Tests) y el guion secuencial de demostración en vivo (duración estimada: 5 a 7 minutos).

---

## 2. Datos de Prueba Preconfigurados

| Entidad | Valor de Prueba | Propósito |
|---|---|---|
| **Operario** | RUT: `11.111.111-1` · PIN: `1234` | Registro de muestras en terreno |
| **Supervisor** | RUT: `22.222.222-2` · PIN: `1234` | Revisión, dictamen y auditoría técnica |
| **Centros** | Chinquihue, Huelmo, Calbuco, Dalcahue | Catálogo físico de cultivo |
| **Límites de Validación** | Conteo: 1 a 5.000 ind/m · Talla: 1 a 200 mm | Reglas de negocio biológicas |

---

## 3. Matriz de Casos de Prueba (Smoke Tests)

### CP-01: Autenticación, Formato de RUT y Rate-Limiting
- **Objetivo:** Validar la seguridad de acceso y la experiencia de usuario en el login.
- **Pasos:**
  1. Abrir la app. Seleccionar perfil **Operario**.
  2. Escribir un RUT sin puntos ni guion (ej. `111111111`).
  3. Verificar que la pantalla formatee automáticamente a `11.111.111-1`.
  4. Ingresar PIN erróneo (`9999`) repetidamente.
- **Resultado Esperado:** Al 5° intento fallido, el sistema bloquea el acceso temporalmente por 1 minuto con backoff exponencial. Al ingresar `1234`, ingresa al Dashboard principal.

### CP-02: Registro de Muestra con Catálogo Dinámico en Room
- **Objetivo:** Comprobar la integridad de las claves foráneas y la carga reactiva de datos.
- **Pasos:**
  1. En el Dashboard, presionar **Nueva Muestra**.
  2. Seleccionar un centro de cultivo (ej. *Centro Chinquihue*).
  3. Desplegar el selector de **Tren**: verificar que cargue los trenes registrados en SQLite (`Tren A, B, C`).
  4. Seleccionar un tren y desplegar **Línea**: verificar que cargue dinámicamente las líneas asociadas a ese tren específico.
  5. Seleccionar estrato de profundidad (Superficie, Media o Fondo).
  6. Presionar **Continuar** para avanzar a Captura.
- **Resultado Esperado:** El formulario valida que ningún campo quede vacío y asegura que la combinación Centro-Tren-Línea exista en la base de datos local.

### CP-03: Captura Fotográfica y Almacenamiento Seguro
- **Objetivo:** Verificar la captura de evidencia visual sin sobrecargar SQLite.
- **Pasos:**
  1. Tomar fotografía usando la cámara del dispositivo o seleccionar imagen de prueba desde la galería.
  2. Confirmar la fotografía para avanzar a la pantalla de **Vista Previa**.
- **Resultado Esperado:** La fotografía se almacena en el directorio privado de la aplicación (`filesDir/fotos/`) y no como objeto BLOB en la base de datos, garantizando alto rendimiento en consultas.

### CP-04: Confirmación y Validación de Mediciones
- **Objetivo:** Asegurar el registro de conteo y tallas según los parámetros biológicos.
- **Pasos:**
  1. En Vista Previa, ingresar conteo de individuos (ej. `320` ind/m).
  2. Ingresar talla promedio (ej. `48` mm).
  3. Añadir una observación técnica (ej. *Muestra con fijación uniforme, sin presencia de depredadores*).
  4. Presionar **Confirmar Muestra**.
- **Resultado Esperado:** Mensaje de confirmación en pantalla, guardado atómico en SQLite con estado inicial **PENDIENTE** y retorno al Dashboard.

### CP-05: Resiliencia Offline (Prueba de Corte y Persistencia)
- **Objetivo:** Demostrar la arquitectura *Offline-First*.
- **Pasos:**
  1. Tras guardar la muestra, cerrar la app completamente desde la multitarea de Android (matar proceso).
  2. Reabrir la aplicación e iniciar sesión.
  3. Navegar a la pestaña **Historial**.
- **Resultado Esperado:** La muestra recién registrada aparece en el listado con todos sus datos y fotografía intactos, demostrando persistencia en Room.

### CP-06: Dictamen de Supervisión y Trazabilidad
- **Objetivo:** Validar el flujo de control de calidad y auditoría.
- **Pasos:**
  1. Cerrar sesión e ingresar con el perfil **Supervisor** (`22.222.222-2` / `1234`).
  2. Ingresar a la sección **Revisión**.
  3. Seleccionar la muestra registrada en el CP-04.
  4. Elegir dictamen **Validado** o **Corregido**.
  5. Si se elige *Corregido*, verificar que el campo de comentario técnico sea obligatorio.
  6. Escribir dictamen técnico y confirmar.
- **Resultado Esperado:** La muestra desaparece de la cola de pendientes y en la tabla `revisiones` queda registrado el dictamen con el RUT del supervisor, fecha y comentario.

### CP-07: Exportación de Ficha Técnica PDF
- **Objetivo:** Validar la generación de documentos para entrega o impresión.
- **Pasos:**
  1. Ir a **Historial** y abrir el **Detalle** de la muestra validada.
  2. Presionar el botón **Compartir / Descargar Ficha**.
- **Resultado Esperado:** Se genera un documento PDF formato A4 maquetado con encabezado institucional, fotografía, mediciones y firma digital del supervisor vía `FileProvider`.

---

## 4. Guion Secuencial para la Presentación (5 Minutos)

```
[00:00 - 01:00] INTRODUCCIÓN Y ACCESO
- Presentar el caso: "MusselApp optimiza el muestreo manual de choritos para ALDEMAR SpA en terreno sin señal."
- Iniciar sesión como Operario mostrando el formateo en vivo de RUT.

[01:00 - 02:30] REGISTRO EN VIVO
- Abrir "Nueva Muestra".
- Destacar: "Los trenes y líneas se consultan en tiempo real desde SQLite Room para evitar registros huérfanos."
- Capturar foto y cargar mediciones (320 ind/m, 48 mm).
- Guardar la muestra.

[02:30 - 03:15] DEMOSTRACIÓN DE PERSISTENCIA
- Cerrar la app desde el administrador de tareas de Android.
- Volver a abrir: "La app es 100% offline-first; no requiere conexión externa para persistir."

[03:15 - 04:15] CONTROL DE CALIDAD (SUPERVISOR)
- Cambiar de usuario al Supervisor.
- Entrar a Revisión: auditar la muestra, dictaminar "Validado" con nota técnica.

[04:15 - 05:00] REPORTE Y CIERRE
- Abrir Detalle de la muestra y exportar la Ficha Técnica PDF.
- Mostrar la acción de sincronización satelital para la consolidación de datos.
```
