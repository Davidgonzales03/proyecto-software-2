# Metodología de Desarrollo — Scrum y Flujo SDD

Este documento describe la adaptación de la metodología ágil **Scrum** y la
disciplina de desarrollo guiado por especificaciones (**SDD - Spec-Driven
Development**) empleadas en el proyecto **Tienda Montero**.

---

## 1. Scrum Adaptado al Equipo

El equipo de trabajo está compuesto por dos integrantes con dedicación parcial.
Por este motivo, Scrum se implementa con ajustes prácticos orientados a
entregar incrementos funcionales continuos sin burocracia innecesaria:

### 1.1. Sprints Orientados a Objetivos
A diferencia de los ciclos tradicionales de duración fija en semanas, los sprints
en este proyecto se definen **por objetivo funcional** (un incremento tangible y
demostrable):
* Un sprint concluye cuando todas sus historias de usuario asociadas cumplen
  estrictamente la **Definition of Done** y están integradas en `main`.
* En la documentación se registran las fechas reales de inicio y cierre de cada
  sprint para fines de auditoría académica.

| Sprint | Objetivo | Historias e Issues |
|---|---|---|
| **Sprint 1** | Cuentas y sistema de diseño | Chore de diseño, HU-01 a HU-05 |
| **Sprint 2** | Catálogo y visualización pública | HU-06 a HU-10, spike 3D, datos de demostración (#25) |
| **Sprint 3** | Proceso de compra y checkout | HU-11 a HU-14, HU-20 |
| **Sprint 4** | Operación, visor 3D y reportes | HU-15 a HU-19 |

### 1.2. Roles
* **Product Owner y Desarrollador Principal:** Fabio (`@FECHDEV`). Prioriza el
  backlog, define requerimientos de negocio, programa el núcleo del backend y
  frontend asistido por Claude Code, y aprueba los Pull Requests.
* **Documentación, Verificación y Soporte:** David (`@Davidgonzales03`). Elabora
  la documentación académica en `docs/proyecto/`, realiza pruebas manuales de
  verificación cruzada en cada PR y desarrolla tareas asignadas asistido por
  opencode.
* **Scrum Master:** No se asigna a una persona dedicada; el cumplimiento de las
  reglas y procesos ágiles se garantiza mediante el flujo SDD automatizado y
  las revisiones de Integración Continua (CI).

### 1.3. Eventos
* **Sprint Planning:** Selección de historias de usuario del backlog priorizado
  que componen el objetivo del sprint, verificando la especificación inicial.
* **Sincronización:** Sin reuniones diarias presenciales (*daily standups*); la
  sincronización se realiza de forma asíncrona mediante el tablero de GitHub
  Projects y los comentarios en los Pull Requests.
* **Sprint Review:** Demostración del incremento integrado en `main` ante los
  docentes o usuarios, complementada con la verificación cruzada de pruebas
  manuales de cada historia.
* **Sprint Retrospective:** Registro de lecciones aprendidas y mejoras en
  `docs/flujo-sdd.md` y `docs/decisiones.md`.

### 1.4. Artefactos
* **Product Backlog:** Gestionado en **GitHub Projects**, compuesto por issues
  con el formato estándar: `HU-XX Como [rol] quiero [acción] para [beneficio]`.
* **Sprint Backlog:** Conjunto de historias asignadas al sprint activo dentro del
  tablero (*Backlog → Sprint → En curso → En revisión → Hecho*).
* **Incremento:** Software potencialmente desplegable, integrado en `main`
  mediante Pull Requests aprobados con pruebas automatizadas en verde.

---

## 2. Flujo SDD (Spec-Driven Development)

Toda historia de usuario sigue obligatoriamente un ciclo de vida riguroso antes de
modificar el código de la aplicación:

```
Historia (/sdd:historia) ➔ Plan Corto ➔ Análisis (/sdd:analizar) ➔ TDD ➔ Cierre DoD (/sdd:cerrar) ➔ PR
```

### Paso 1: Especificación de la Historia (`docs/historias/HU-XX.md`)
A partir del issue de GitHub, se redacta la historia detallando:
* Identificador y título con rol, acción y propósito.
* Contexto y decisiones previas tomadas en la entrevista.
* **Criterios de aceptación funcionales en sintaxis EARS:** Numerados de forma
  inmutable como `HU-XX RF-N` (las pruebas automáticas los citan textualmente).
* **Escenarios BDD:** Estructurados en cláusulas `Dado / Cuando / Entonces`
  abarcando casos nominales y de borde.

### Paso 2: Plan Corto de Implementación (`docs/planes/AAAA-MM-DD-hu-xx-nombre.md`)
Documento sintético que desglosa el trabajo en tareas técnicas atómicas y
secuenciales:
1. Tareas de backend (entidad, repositorio, servicio, controlador, DTO).
2. Tareas de frontend (servicios, componentes, formularios reactivos, rutas).
3. Matriz de trazabilidad entre cada tarea técnica y su RF correspondiente.

### Paso 3: Análisis de Consistencia Previa
Antes de escribir código, se ejecuta una verificación formal que contrasta:
* La historia de usuario contra las reglas de negocio de `CLAUDE.md`.
* Las decisiones de arquitectura registradas en `docs/decisiones.md`.
* Los posibles impactos colaterales en entidades o DTOs compartidos.
Si se detecta un hallazgo crítico o contradicción, no se inicia la programación
hasta que se aclare con el Product Owner.

### Paso 4: TDD (Test-Driven Development)
El desarrollo se realiza aplicando desarrollo guiado por pruebas:
1. **La prueba primero:** Se escribe la prueba unitaria o de integración citando
   el requisito exacto (ejemplo: `// HU-12 RF-03`). La prueba debe fallar
   inicialmente (*Red*).
2. **Código mínimo:** Se implementa el código funcional necesario para que la
   prueba pase (*Green*).
3. **Refactorización:** Se limpia el diseño asegurando que la prueba continúe
   pasando (*Refactor*).
* **Regla de ejecución de pruebas:** Las suites de Backend (`./mvnw test`) y
  Frontend (`npx ng test --watch=false`) se corren siempre de forma sucesiva,
  nunca en paralelo, para evitar degradación de recursos en la máquina local.

### Paso 5: Cierre y Definition of Done (DoD)
Para dar por concluida la historia y preparar el PR, se debe verificar:
- [x] Todas las pruebas del backend pasan sobre H2 (`./mvnw test`).
- [x] Todas las pruebas del frontend pasan con Vitest (`npx ng test --watch=false`).
- [x] El frontend compila limpiamente sin errores de TypeScript (`npx ng build`).
- [x] Todos los RF de la historia cuentan con al menos una prueba automatizada.
- [x] El código respeta el diseño visual (tokens, modo claro/oscuro) y no añade
      dependencias sin autorización previa.
- [x] No existen credenciales, claves ni secretos en el código versionado.

---

## 3. Estrategia de Ramas y Control de Versiones

### 3.1. Protección de la Rama `main`
* La rama `main` contiene únicamente código verificado y en estado de producción.
* Está protegida mediante reglas de repositorio de GitHub (*ruleset*):
  * Prohibido hacer push directo a `main`.
  * Todo cambio ingresa por Pull Request.
  * Requiere que el flujo de Integración Continua (`.github/workflows/pruebas.yml`)
    complete con éxito todos los checks.

### 3.2. Nomenclatura de Ramas
* **Historias de usuario:** `feature/hu-XX-nombre-corto` (ejemplo: `feature/hu-06-categorias`).
* **Documentación técnica:** `docs/nombre-tema` (ejemplo: `docs/datos-demo`, `docs/metodologia`).
* **Correcciones:** `fix/descripcion-corta`.
* **Tareas de mantenimiento:** `chore/descripcion-corta`.

### 3.3. Convención de Commits y Cierre de Issues
Los mensajes de commit describen de forma concisa el cambio y su contexto:
* Para historias completas que cierran un issue: `feat: implementación de HU-XX (Closes #N)`.
* Para entregas parciales o documentación de soporte: `docs: catálogo de demostración (Refs #25)`.
