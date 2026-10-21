# Diagrama entidad-relación

Modelo de datos de la base de datos. **Sale de** el código de `backend/`
(las entidades de `entity/` en `main`, consultado el 21/10/2026); el esquema
lo genera Hibernate al arrancar, no hay script SQL en el repositorio.

```mermaid
erDiagram
    USUARIO ||--o{ PEDIDO : "realiza"
    USUARIO ||--o{ LINEA_CARRITO : "tiene"
    USUARIO ||--o{ MOVIMIENTO_STOCK : "registra"
    USUARIO ||--o{ RECUPERACION_CONTRASENA : "solicita"
    CATEGORIA ||--o{ PRODUCTO : "agrupa"
    PRODUCTO ||--o{ LINEA_CARRITO : "se agrega"
    PRODUCTO ||--o{ LINEA_PEDIDO : "se vende"
    PRODUCTO ||--o{ MOVIMIENTO_STOCK : "afecta"
    PRODUCTO ||--o{ CARACTERISTICA : "describe"
    PRODUCTO ||--o{ PRODUCTO_IMAGEN : "muestra"
    PEDIDO ||--o{ LINEA_PEDIDO : "contiene"
    PEDIDO ||--o{ COBRO : "genera"
    PEDIDO ||--o{ MOVIMIENTO_STOCK : "reserva"

    USUARIO {
        bigint id_usuario PK
        varchar nombre
        varchar apellido
        varchar correo UK
        varchar contrasena
        varchar telefono
        enum rol "CLIENTE | ADMINISTRADOR | EMPLEADO | CONTADOR"
        enum estado "ACTIVA | DESACTIVADA"
        datetime fecha_registro
        int version_sesion
    }
    RECUPERACION_CONTRASENA {
        bigint id_recuperacion PK
        bigint id_usuario FK
        varchar hash_token "SHA-256, vigencia 1 hora"
        datetime fecha_creacion
        datetime fecha_expiracion
        boolean usado
    }
    CATEGORIA {
        bigint id_categoria PK
        varchar nombre
        varchar nombre_clave "minúsculas, para comparar"
        varchar descripcion
        varchar imagen
        boolean visible
    }
    PRODUCTO {
        bigint id_producto PK
        varchar codigo UK
        varchar nombre
        varchar marca
        varchar descripcion
        decimal precio "mayor que 0"
        enum unidad "UNIDAD | METRO | KILO | LITRO | CAJA | PAR | ROLLO"
        bigint id_categoria FK
        boolean activo
        datetime fecha_alta
        decimal stock
        decimal stock_minimo
        boolean destacado
        varchar busqueda "texto para el filtrado"
    }
    CARACTERISTICA {
        varchar nombre "producto_caracteristica"
        varchar valor
    }
    PRODUCTO_IMAGEN {
        bigint id_producto FK "producto_imagen"
        varchar imagenes "nombre de archivo"
    }
    LINEA_CARRITO {
        bigint id_linea PK
        bigint id_usuario FK
        bigint id_producto FK
        decimal cantidad
        datetime fecha_agregada
    }
    PEDIDO {
        bigint id_pedido PK
        bigint id_usuario FK
        enum estado "PENDIENTE_PAGO | PAGADO | CANCELADO | VENCIDO | PAGO_A_DEVOLVER"
        decimal total
        varchar nombre_retira
        varchar telefono_retira
        varchar nota
        datetime confirmado
        datetime vence
        datetime cerrado
        datetime pagado
    }
    LINEA_PEDIDO {
        bigint id_linea PK
        bigint id_pedido FK
        bigint id_producto FK
        varchar nombre "copia al confirmar"
        enum unidad
        decimal precio "copia al confirmar"
        decimal cantidad
        decimal subtotal
        decimal cantidad_devuelta
    }
    COBRO {
        bigint id_cobro PK
        bigint id_pedido FK
        decimal monto
        varchar referencia
        varchar codigo_qr
        varchar url_pasarela
        enum estado "PENDIENTE | PAGADO | ERROR"
        datetime generado
        datetime fecha_pago
        datetime consultado_sin_pago
    }
    MOVIMIENTO_STOCK {
        bigint id_movimiento PK
        bigint id_producto FK
        enum tipo "ENTRADA | AJUSTE | RESERVA | LIBERACION"
        enum motivo "CONTEO | MERMA | DEVOLUCION | OTRO"
        decimal cantidad
        decimal stock_resultante
        bigint id_usuario FK
        datetime fecha
        varchar nota
        bigint id_pedido FK "opcional"
    }
```

## Notas

- **Tipos:** en la base real, `varchar` respeta los largos de cada columna
  (`correo` 120, `nombre` y `apellido` 80, `codigo` único, etc.) y los
  `decimal` son de tipo `DECIMAL`; el detalle exacto lo fija JPA con las
  anotaciones de cada entidad en `backend/.../entity/`.
- **Enums:** `Rol`, `EstadoCuenta`, `EstadoPedido`, `EstadoCobro`,
  `TipoMovimiento`, `MotivoAjuste` y `UnidadVenta` son enumeraciones de Java
  (`entity/`), guardadas como texto.
- **Stock:** `PRODUCTO.stock` es un contador actual; `MOVIMIENTO_STOCK` es el
  historial de cada cambio (entradas, ajustes, reservas y liberaciones), con
  `stock_resultante` para reconstruir el saldo en cualquier fecha.
- **Pedidos:** `LINEA_PEDIDO` guarda copia de `nombre`, `unidad` y `precio`
  para que el historial no cambie si el producto se modifica después.
- La entidad `VerificacionCorreo` de la HU-20 todavía no está en `main`
  (issue #32, planificada); cuando se mergee hay que agregarla acá.
