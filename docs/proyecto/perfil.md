# Perfil del Proyecto — Tienda Montero

**Proyecto Académico**  
Universidad Autónoma Gabriel René Moreno (UAGRM)  
Facultad Integral del Norte (FINOR)  
Montero, Santa Cruz, Bolivia  

---

## 1. Información General

| Campo | Detalle |
|---|---|
| **Nombre del producto** | **Tienda Montero** (configurable en `core/marca.ts` y variable `TIENDA_MARCA`) |
| **Tipo de sistema** | Tienda en línea propia (comercio electrónico monovendedor, no marketplace) |
| **Rubro comercial** | Ferretería (herramientas de mano, herramientas eléctricas, hogar, jardín y construcción) |
| **Repositorio** | `git@github.com:FECHDEV/proyecto-software-2.git` |
| **Plazo de ejecución** | Dos meses y medio como máximo, a partir del 07/10/2026 |
| **Equipo de trabajo** | **Fabio (`@FECHDEV`)**: Desarrollo y Product Owner.<br>**David (`@Davidgonzales03`)**: Documentación técnica, verificación cruzada y desarrollo de soporte. |

---

## 2. Planteamiento del Problema

Las ferreterías tradicionales en la ciudad de Montero enfrentan limitaciones en sus canales de venta presenciales:

1. **Falta de visibilidad de inventario y precios:** Los clientes (habitantes de la zona, maestros constructores, aficionados al bricolaje y jardinería) deben acudir físicamente o consultar por canales informales para saber si un material o herramienta está disponible y cuál es su costo en moneda nacional (bolivianos).
2. **Brecha en la evaluación de productos a distancia:** En el rubro ferretero, evaluar dimensiones, empuñaduras y detalles constructivos de una herramienta es indispensable para tomar la decisión de compra. Las fotos estáticas tradicionales suelen ser insuficientes para transmitir la escala real y características físicas.
3. **Gestión operativa desconectada:** La falta de sincronización entre pedidos en línea, reservas temporales de stock y despachos físicos puede ocasionar sobreventa o congelamiento innecesario del inventario.

---

## 3. Objetivos del Proyecto

### 3.1. Objetivo General
Desarrollar una plataforma web de comercio electrónico propia para una ferretería de Montero, que integre catálogo de productos con visualización en 3D y realidad aumentada, carrito de compras, reservas de stock, confirmación de pagos mediante códigos QR, despacho de pedidos y reportes contables, bajo arquitectura desacoplada y metodología ágil Scrum.

### 3.2. Objetivos Específicos
1. **Seguridad y Accesos:** Implementar un esquema de autenticación stateless con JWT y cuatro roles fijos (`Cliente`, `Administrador`, `Empleado` y `Contador`) con validación estricta de permisos en backend.
2. **Catálogo e Inventario:** Construir un catálogo categorizado con soporte de unidades de venta diversas (`unidad`, `metro`, `kilo`, `litro`, `caja`, `rollo`), control estricto de movimientos de stock y carga de modelos 3D (`.glb`).
3. **Experiencia de Compra y 3D:** Brindar una interfaz reactiva e intuitiva optimizada tanto para dispositivos móviles como de escritorio, incorporando visualización interactiva en 3D y realidad aumentada (AR) en los productos compatibles.
4. **Procesamiento de Pedidos y Pagos:** Integrar un circuito de checkout con reserva temporal de stock, vencimiento automático de pedidos abandonados y pagos por QR desacoplados mediante la interfaz `IPago` (con simulador local y adaptador para proveedor real Libélula).
5. **Operación y Reportes:** Proveer un panel administrativo para que los empleados gestionen entregas y despacho de pedidos, y el contador consulte y exporte reportes financieros y de recaudación.

---

## 4. Alcance y Límites

### 4.1. Dentro del Alcance
* **Módulo de Usuarios:** Registro, inicio de sesión, recuperación de contraseña por correo electrónico, gestión de estados de cuenta (activa/desactivada) y asignación de roles fijos por el Administrador.
* **Módulo de Catálogo:** Administración de categorías y productos con múltiples fotografías (hasta 5 por producto, máximo 2 MB por archivo) y modelos 3D opcionales. Portada con carruseles de novedades y destacados.
* **Módulo de Carrito y Checkout:** Carrito persistente por usuario, validación de stock disponible, selección de modalidad de entrega (retiro en local con horarios y dirección configurables) y datos de contacto.
* **Módulo de Pagos:** Generación de cobros QR, verificación de estado del pago consultando al proveedor y conciliación automática en segundo plano.
* **Módulo de Operación:** Consulta y actualización del estado de pedidos (`PENDIENTE`, `PAGADO`, `ENTREGADO`, `CANCELADO`, `VENCIDO`) y registro inmutable de movimientos de stock (`INGRESO`, `RESERVA`, `LIBERACION`, `VENTA`, `AJUSTE`).
* **Módulo de Reportes:** Visualización y exportación de ventas, ingresos y productos más vendidos por rango de fechas para el rol Contador.
* **Datos de Demostración:** Catálogo precargado de ferretería con 20 productos y modelos 3D CC0 de Poly Haven para revisiones y defensa académica.

### 4.2. Fuera del Alcance
* **Marketplace:** El sistema es exclusivo para una única ferretería; no contempla múltiples vendedores ni comisiones.
* **Facturación Electrónica SIAT:** No forma parte del proyecto académico la conexión con el Servicio de Impuestos Nacionales.
* **Pagos con Tarjeta Directa:** Solo se admiten pagos mediante QR bancario procesados por el proveedor o simulador.
* **Aplicación Móvil Nativa:** El acceso se realiza exclusivamente mediante navegador web (PWA / Web responsive).

---

## 5. Planificación y Plazos

El desarrollo se planifica para un periodo máximo de **2 meses y medio** (del 07/10/2026 a mediados de diciembre de 2026), organizado en **4 Sprints** orientados a objetivos funcionales:

* **Sprint 1 — Cuentas y sistema de diseño:** Configuración base, arquitectura, autenticación, roles, diseño visual y HU-01 a HU-05.
* **Sprint 2 — Catálogo:** Categorías, gestión de productos, inventario, portada pública, HU-06 a HU-10 y catálogo demo.
* **Sprint 3 — Compra:** Carrito de compras, confirmación de pedidos, pasarela de pago QR, retiro en tienda, HU-11 a HU-14 y HU-20.
* **Sprint 4 — Operación y 3D:** Despacho de pedidos, visor 3D con realidad aumentada, reportes contables y HU-15 a HU-19.
