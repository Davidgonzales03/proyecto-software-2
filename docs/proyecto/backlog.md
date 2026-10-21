# Backlog

Historias del proyecto por sprint y su estado.

**Fuente:** los issues y PR de `FECHDEV/proyecto-software-2` (consultados el
21/10/2026) y `docs/flujo-sdd.md` → «Sprints por objetivo». Los sprints de
este proyecto son por objetivo y no tienen fechas fijas (`docs/flujo-sdd.md`).

**Criterio:** una historia está **Hecha** solo si su PR está mergeado en
`main`. Lo demás está **Planificado**, con su sprint. Cada historia se
describe en su `docs/historias/HU-XX.md` y sus RF en
`docs/proyecto/requisitos.md`.

## Estado general

| Sprint | Objetivo | Entradas | Hechas |
|---|---|---|---|
| 1 | Cuentas y sistema de diseño | HU-01 a HU-05 y el chore de diseño | 6 de 6 |
| 2 | Catálogo | HU-06 a HU-10, el spike del visor 3D y los datos de demostración | 6 de 7 |
| 3 | Compra | HU-11 a HU-14 y HU-20 | 3 de 5 |
| 4 | Operación y 3D | HU-15 a HU-19 | 0 de 5 |

## Sprint 1 — Cuentas y sistema de diseño

| Historia | Issue | Estado | PR |
|---|---|---|---|
| chore: sistema de diseño de la ferretería | #8 | Hecho | #29 |
| HU-01 Como visitante quiero registrarme para poder comprar | #3 | Hecha | #28 |
| HU-02 Como usuario quiero iniciar sesión para usar mi cuenta | #4 | Hecha | #30 |
| HU-03 Como usuario quiero recuperar mi contraseña por correo para volver a entrar | #5 | Hecha | #31 |
| HU-04 Como usuario quiero editar mis datos y mi contraseña para mantenerlos al día | #6 | Hecha | #33 |
| HU-05 Como administrador quiero gestionar usuarios, roles y estados para controlar el acceso | #7 | Hecha | #34 |

## Sprint 2 — Catálogo

| Historia | Issue | Estado | PR |
|---|---|---|---|
| HU-06 Como administrador quiero gestionar categorías para ordenar el catálogo | #9 | Hecha | #35 |
| HU-07 Como administrador quiero gestionar productos con precio, descripción e imágenes para venderlos | #10 | Hecha | #36 |
| HU-08 Como empleado quiero registrar entradas y ajustes de stock para mantener el inventario | #11 | Hecha | #37 |
| HU-09 Como visitante quiero ver el catálogo, buscar y filtrar para encontrar productos | #12 | Hecha | #40 |
| HU-10 Como visitante quiero ver el detalle de un producto para decidir la compra | #13 | Hecha | #41 (correcciones en #42) |
| spike: visor 3D y modelos de los productos | #14 | Hecho | #46 |
| chore: datos de demostración | #25 | Planificada | — |

El código que carga el catálogo de demostración ya está en `main` (PR #44).
El issue #25 sigue abierto hasta que entre el contenido del catálogo.

## Sprint 3 — Compra

| Historia | Issue | Estado | PR |
|---|---|---|---|
| HU-11 Como cliente quiero agregar productos a un carrito para comprar varios a la vez | #15 | Hecha | #48 |
| HU-12 Como cliente quiero confirmar un pedido y que se reserve el stock para asegurar mi compra | #16 | Hecha | #50 |
| HU-13 Como cliente quiero pagar mi pedido para completar la compra | #17 | Hecha | #51 |
| HU-14 Como cliente quiero ver mis pedidos y su estado para hacer el seguimiento | #18 | Planificada | — |
| HU-20 Como cliente quiero verificar mi correo para poder confirmar pedidos | #32 | Planificada | — |

## Sprint 4 — Operación y 3D

| Historia | Issue | Estado | PR |
|---|---|---|---|
| HU-15 Como administrador quiero subir el modelo 3D de un producto para que los clientes lo vean en 3D | #19 | Planificada | — |
| HU-16 Como visitante quiero ver el producto en 3D y en realidad aumentada para conocerlo mejor antes de comprar | #20 | Planificada | — |
| HU-17 Como empleado quiero ver los pedidos pagados y entregarlos a quien los retira para completar la venta | #21 | Planificada | — |
| HU-18 Como contador quiero ver reportes de ventas e ingresos para controlar el dinero | #22 | Planificada | — |
| HU-19 Como contador quiero exportar los reportes para entregarlos | #23 | Planificada | — |

## Fuera del backlog de historias

Los issues y PR de proceso (`chore: …`, `docs: …`, `fix: …`) no son
historias: ordenan el trabajo del equipo y no se cuentan en el estado
general. Se pueden consultar con `gh issue list --state all` y
`gh pr list --state merged`.
