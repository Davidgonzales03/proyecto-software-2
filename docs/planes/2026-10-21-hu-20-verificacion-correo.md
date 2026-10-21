# Plan de trabajo — HU-20: Verificación de correo electrónico

## Objetivo
Implementar la verificación de correo electrónico para clientes («verificar para comprar»), bloqueando la confirmación de pedidos si el correo no está verificado, con envío de token seguro de 24 horas al registrarse o solicitar reenvío, y pantalla de confirmación en el frontend.

## Tareas

- [ ] **Tarea 1: Backend — Entidad VerificacionCorreo, campo en Usuario y Repositorio (HU-20 RF-1, RF-8)**
  - Agregar campo `correoVerificado` en `Usuario` (`@ColumnDefault("true")` en BD, `false` al registrar cliente, `true` para administrador).
  - Incluir `correoVerificado` en `UsuarioResponse`.
  - Crear entidad `VerificacionCorreo` (id, usuario, hashToken, emitido, vence, usado) y `VerificacionCorreoRepository`.
  - Pruebas unitarias citando `HU-20 RF-1, RF-8`.

- [ ] **Tarea 2: Backend — Servicio y envío de correo (HU-20 RF-1, RF-2, RF-3, RF-5, RF-6)**
  - Agregar `enviarVerificacionCorreo(correo, nombre, token)` en `NotificadorCorreo`, `SimuladorCorreo` y `NotificadorCorreoSmtp`.
  - Crear `LimiteSolicitudesVerificacion` (máx. 3 en 15 min).
  - Crear `VerificacionCorreoService`: generación de token, verificación (invalida token y marca usuario), reenvío.
  - Al registrar cliente en `UsuarioService`: generar token y enviar correo de verificación.
  - Pruebas unitarias de servicio.

- [ ] **Tarea 3: Backend — Bloqueo en confirmación de pedido y Endpoints (HU-20 RF-2, RF-3, RF-4, RF-5)**
  - Crear excepción `CorreoNoVerificadoException` (HTTP 409 `CORREO_NO_VERIFICADO`).
  - En `PedidoService.confirmar()`: verificar que `usuario.isCorreoVerificado()`, lanzando excepción si no lo está.
  - Crear endpoints en `AuthController`:
    - `POST /api/auth/verificacion-correo`: recibe `{ token }`, verifica.
    - `POST /api/auth/verificacion-correo/reenvio`: reenvía correo para el cliente autenticado.
  - Pruebas de integración en `PedidoIntegracionTest` y `AuthIntegracionTest`.

- [ ] **Tarea 4: Frontend — Modelo, servicios y mensajes (HU-20 RF-1, RF-4)**
  - Actualizar `Usuario` en frontend con `correoVerificado: boolean`.
  - Agregar `verificarCorreo(token)` y `reenviarVerificacion()` en `AutenticacionService`.
  - Agregar mensajes de error en `mensajes-error.ts` para `CORREO_NO_VERIFICADO` y `TOKEN_VERIFICACION_INVALIDO`.

- [ ] **Tarea 5: Frontend — Pantalla VerificarCorreoPage y aviso en Confirmación (HU-20 RF-2, RF-3, RF-7)**
  - Crear `features/verificar-correo/verificar-correo.page.ts`, template y estilos.
  - Registrar ruta `/verificar-correo` en `app.routes.ts`.
  - En `ConfirmacionPage`: mostrar aviso destacado si `!usuario.correoVerificado` con botón para reenviar correo y bloquear confirmación.
  - Pruebas unitarias en Vitest.

- [ ] **Tarea 6: Verificación y Definition of Done**
  - Ejecutar suite completa de backend (`./mvnw.cmd test`).
  - Ejecutar suite completa de frontend (`npx ng test --watch=false`).
  - Validar build de producción (`npx ng build`).
