# Plan de trabajo — HU-14: Seguimiento de Pedidos

## Objetivo
Implementar la consulta histórica y seguimiento de compras para el Cliente, tanto
en el endpoint REST del backend (`GET /api/pedidos`) como en la pantalla de la SPA
Angular (`/mis-pedidos`), asegurando aislamiento estricto por usuario y actualización
de pedidos vencidos.

## Tareas

- [ ] **Tarea 1: Backend — Consulta de pedidos en Repositorio y Servicio (HU-14 RF-1, RF-7, RF-9)**
  - Agregar en `PedidoRepository`: `findByUsuario_IdUsuarioOrderByIdPedidoDesc(Long idUsuario)`.
  - Agregar en `PedidoService`: método `listar(Long idUsuario)` que ejecuta `vencerSiCorresponde(idUsuario)` y mapea la lista a `PedidoResponse`.
  - Prueba unitaria en `PedidoServiceTest` citando `HU-14 RF-1`, `HU-14 RF-7` y `HU-14 RF-9`.

- [ ] **Tarea 2: Backend — Endpoint GET /api/pedidos en Controlador (HU-14 RF-1, RF-5, RF-6)**
  - Exponer `GET /api/pedidos` en `PedidoController` inyectando `@AuthenticationPrincipal Long idUsuario`.
  - Prueba de integración en `PedidoIntegracionTest` / `PedidoControllerTest` verificando 200 OK con lista para Cliente, 401 sin token y 403 para otros roles.

- [ ] **Tarea 3: Frontend — Métodos en PedidosService (HU-14 RF-1, RF-2)**
  - Extender `PedidosService` en Angular para incorporar `misPedidos(): Observable<PedidoResponse[]>`.
  - Pruebas unitarias de servicio en `pedidos.service.spec.ts`.

- [ ] **Tarea 4: Frontend — Página MisPedidosPage y template (HU-14 RF-2, RF-3, RF-4, RF-8)**
  - Crear componente standalone `mis-pedidos.page.ts` y su template HTML/CSS con tokens de diseño.
  - Mostrar listado de pedidos con badges de estado, fecha, total en Bs., resumen de líneas y botón a detalle.
  - Manejo de estado vacío cuando no hay pedidos (con botón a `/catalogo`).
  - Pruebas unitarias con Vitest en `mis-pedidos.page.spec.ts`.

- [ ] **Tarea 5: Frontend — Enrutamiento y enlace en cabecera (HU-14 RF-5, RF-10)**
  - Registrar ruta `mis-pedidos` en `app.routes.ts` con `rolGuard` para `CLIENTE`.
  - Añadir enlace «Mis pedidos» en la barra de navegación (`cabecera`) visible solo para clientes autenticados.
  - Pruebas de rutas en `app.routes.spec.ts` y componente de cabecera.

- [ ] **Tarea 6: Verificación y Definition of Done**
  - Ejecutar suite completa de backend (`./mvnw.cmd test`).
  - Ejecutar suite completa de frontend (`npx ng test --watch=false`).
  - Ejecutar build de producción (`npx ng build`).
