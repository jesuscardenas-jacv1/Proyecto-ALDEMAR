# Guía de trabajo en equipo — Git y GitHub

Flujo de trabajo del **Equipo Pipeño** para MusselApp. Regla principal: **no desarrollar directamente sobre `main`**.

`main` mantiene siempre la versión estable e integrada del proyecto; cada integrante trabaja en su propia rama.

```
main
│
├── feature/login
├── feature/home
├── feature/muestra
├── feature/captura
├── feature/historial
└── feature/revision
```

## Ramas

| Prefijo | Uso | Ejemplo |
|---|---|---|
| `feature/` | Nueva funcionalidad de la app | `feature/login`, `feature/historial` |
| `fix/` | Corrección de un error | `fix/validacion-conteo` |
| `docs/` | Documentación, diseño y evidencias | `docs/clase-03-evidencia` |

Una funcionalidad = una rama. Cada integrante trabaja **solo en la rama que tiene asignada**.

## Flujo paso a paso

### 1. Proyecto base

Un solo integrante crea el proyecto en Android Studio, verifica que compile y ejecute, y lo sube a `main`. Los demás integrantes quedan como colaboradores del repositorio.

### 2. Clonar (una sola vez)

```bash
git clone https://github.com/jesuscardenas-jacv1/Proyecto-ALDEMAR.git
cd Proyecto-ALDEMAR
```

Todos trabajan sobre **este mismo repositorio**; no crear repositorios separados.

### 3. Antes de comenzar una funcionalidad

```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-funcionalidad
```

### 4. Trabajar y hacer commits

```bash
git status
git add .
git commit -m "Agrega formulario de muestra"
```

Commits pequeños y descriptivos, en español y en presente:

| Evitar | Preferir |
|---|---|
| `cambios` | `Agrega pantalla de historial` |
| `avance` | `Valida campos de la muestra` |
| `cosas` | `Implementa navegación al detalle` |

### 5. Subir la rama

```bash
git push -u origin feature/nombre-funcionalidad   # la primera vez
git push                                          # las siguientes
```

Esto **no modifica `main`**, solo publica la rama.

### 6. Pull Request

Antes de solicitar la integración:

- [ ] El proyecto compila
- [ ] La funcionalidad funciona
- [ ] No existen errores evidentes
- [ ] Los cambios están confirmados con commits
- [ ] La rama está subida a GitHub

Luego crear en GitHub un **Pull Request** `feature/...` → `main` explicando brevemente qué se implementó.

### 7. Revisión

El autor **no aprueba su propio PR**. Otro integrante revisa:

- que el código sea comprensible;
- que no se elimine código de otros integrantes;
- que respete la estructura **MVVM** del proyecto;
- que la funcionalidad esté terminada;
- que no existan archivos innecesarios.

Si está correcto: **Approve → Merge Pull Request**, y luego eliminar la rama ya integrada.

### 8. Después de un merge

Los demás integrantes no reciben los cambios automáticamente. Antes de continuar:

```bash
git checkout main
git pull origin main
```

## Flujo que se repite durante todo el proyecto

```
main → actualizar → crear rama feature → desarrollar → probar
     → commit + push → Pull Request → revisión del equipo → merge → main
```

## Reglas del equipo

1. **No programar directamente en `main`.**
2. Una funcionalidad = una rama `feature/...`.
3. Antes de crear una rama, actualizar `main`.
4. Hacer commits claros y frecuentes.
5. Hacer `push` regularmente para respaldar el trabajo.
6. Probar antes de crear un Pull Request.
7. Otro integrante debe revisar el PR.
8. Después de un merge, todos deben actualizar su `main`.
9. **Nunca usar `git push --force`** salvo indicación expresa del docente.
10. Ante un conflicto de Git, **no borrar archivos ni sobrescribir código de otro integrante** sin revisar primero qué cambió cada persona.

> Objetivo: que `main` siempre represente la versión integrada y funcional de la aplicación, mientras cada integrante desarrolla nuevas funcionalidades de forma aislada en su propia rama.
