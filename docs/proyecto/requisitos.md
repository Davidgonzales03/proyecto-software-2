# Requisitos del Sistema — Tienda Montero

Este documento consolida los Requisitos Funcionales (RF) extraídos de las
historias de usuario especificadas en `docs/historias/` y los Requisitos No
Funcionales (RNF) derivados de `CLAUDE.md` y `docs/decisiones.md`.

---

## 1. Requisitos Funcionales

### HU-01 — Registro de clientes

| ID | Requisito | Rol |
|---|---|---|
| HU-01 RF-1 | CUANDO un visitante envía nombre, apellido, correo y contraseña válidos, y opcionalmente un teléfono, EL SISTEMA crea la cuenta con rol **Cliente** y estado **activa**, sin confirmación por correo. | visitante |
| HU-01 RF-2 | CUANDO se crea la cuenta, EL SISTEMA inicia la sesión de esa persona (responde con un token, igual que el inicio de sesión) y la lleva a la pantalla de la que venía, o al inicio si no venía de ninguna. | visitante |
| HU-01 RF-3 | EL SISTEMA exige nombre, apellido, correo y contraseña; un dato ausente o hecho solo de espacios se rechaza indicando el campo. | visitante |
| HU-01 RF-4 | EL SISTEMA limita las longitudes: nombre y apellido hasta 80 caracteres, correo hasta 120 y teléfono hasta 20. | visitante |
| HU-01 RF-5 | SI el correo no tiene formato de correo, ENTONCES EL SISTEMA lo rechaza indicando el campo. | visitante |
| HU-01 RF-6 | SI la contraseña tiene menos de 8 caracteres o más de 72 bytes en UTF-8, ENTONCES EL SISTEMA la rechaza indicando el motivo. | visitante |
| HU-01 RF-7 | EL SISTEMA guarda el correo sin espacios alrededor y en minúsculas, y busca los duplicados del mismo modo. | visitante |
| HU-01 RF-8 | SI ya existe una cuenta con ese correo, ENTONCES EL SISTEMA no crea otra y responde que el correo ya está registrado (409 `CUENTA_EXISTENTE`), también cuando dos registros con el mismo correo llegan a la vez. | visitante |
| HU-01 RF-9 | CUANDO hay varios datos inválidos, EL SISTEMA indica todos los campos a corregir en una sola respuesta, cada uno con su mensaje. | visitante |
| HU-01 RF-10 | SI el teléfono llega vacío o solo con espacios, ENTONCES EL SISTEMA lo guarda como ausente. | visitante |
| HU-01 RF-11 | EL SISTEMA guarda la contraseña solo como hash BCrypt y nunca la devuelve en la respuesta. | visitante |
| HU-01 RF-12 | EL SISTEMA asigna siempre el rol Cliente y el estado activa, aunque la solicitud traiga otro rol, otro estado u otros campos de más. | visitante |
| HU-01 RF-13 | SI desde una misma IP ya se crearon 5 cuentas en la última hora, ENTONCES EL SISTEMA rechaza el registro con 429 durante una hora contada desde la quinta, sin crear la cuenta. | visitante |
| HU-01 RF-14 | MIENTRAS la persona tiene la sesión iniciada, EL SISTEMA lleva la pantalla «Crear cuenta» al inicio. | visitante |

### HU-02 — Inicio de sesión

| ID | Requisito | Rol |
|---|---|---|
| HU-02 RF-1 | CUANDO un usuario envía el correo y la contraseña de una cuenta activa, EL SISTEMA responde con un token que vence a las 8 horas y los datos de la cuenta, sin la contraseña, y la pantalla lo lleva a la que quería ver, o al inicio. | usuario |
| HU-02 RF-2 | EL SISTEMA normaliza el correo como en el registro (sin espacios alrededor y en minúsculas); la contraseña se compara tal cual. | usuario |
| HU-02 RF-3 | SI el correo no existe o la contraseña no coincide, ENTONCES EL SISTEMA responde el mismo mensaje en los dos casos («El correo o la contraseña no son correctos.»), y el correo inexistente también compara una contraseña, para no revelar qué correos tienen cuenta ni por el texto ni por la demora. | usuario |
| HU-02 RF-4 | SI la cuenta está desactivada y la contraseña es correcta, ENTONCES EL SISTEMA rechaza el inicio de sesión avisando que la cuenta está desactivada; con la contraseña incorrecta responde como en RF-3. | usuario |
| HU-02 RF-5 | SI hay 5 intentos fallidos para el mismo correo desde la misma IP en 15 minutos, ENTONCES EL SISTEMA rechaza ese correo desde esa IP durante 15 minutos (429), aunque la contraseña sea correcta. | usuario |
| HU-02 RF-6 | CUANDO el inicio de sesión es exitoso, EL SISTEMA reinicia el conteo de intentos fallidos de ese correo desde esa IP. | usuario |
| HU-02 RF-7 | SI la contraseña tiene más de 72 bytes, ENTONCES EL SISTEMA la rechaza como incorrecta (RF-3), con la misma demora. | usuario |
| HU-02 RF-8 | EL SISTEMA exige correo y contraseña; uno vacío se marca en su campo sin llamar al servidor. | usuario |
| HU-02 RF-9 | MIENTRAS la sesión está iniciada, EL SISTEMA atiende cada petición con el rol y el estado de la cuenta en la base: si se desactiva la cuenta, se le cambia el rol o la contraseña, el token deja de valer desde la siguiente petición. | usuario |
| HU-02 RF-10 | CUANDO la sesión se cierra sola (el token vence, o el servidor lo rechaza con 401), EL SISTEMA lleva a «Iniciar sesión» con el aviso «Tu sesión se cerró. Inicia sesión de nuevo.» y, al volver a entrar, a la pantalla donde estaba. | usuario |
| HU-02 RF-11 | CUANDO el usuario elige «Cerrar sesión», EL SISTEMA borra la sesión guardada en el navegador y lo lleva a «Iniciar sesión», sin aviso. | usuario |
| HU-02 RF-12 | MIENTRAS la persona tiene la sesión iniciada, EL SISTEMA lleva la pantalla «Iniciar sesión» al inicio. | usuario |
| HU-02 RF-13 | EL SISTEMA ofrece en el campo de la contraseña un botón para mostrarla u ocultarla, que dice qué hace («Mostrar contraseña» / «Ocultar contraseña») y empieza oculta. | usuario |

### HU-03 — Recuperación de contraseña

| ID | Requisito | Rol |
|---|---|---|
| HU-03 RF-1 | CUANDO alguien pide recuperar la contraseña con el correo de una cuenta activa, EL SISTEMA le envía un correo con un enlace para elegir una contraseña nueva, que vence en 1 hora y sirve una sola vez. | usuario |
| HU-03 RF-2 | EL SISTEMA responde siempre el mismo mensaje al pedir la recuperación, exista o no el correo y esté o no activa la cuenta, y el envío no cambia la demora ni la respuesta (se hace en segundo plano). | usuario |
| HU-03 RF-3 | SI el correo no tiene cuenta, o la cuenta está desactivada, ENTONCES EL SISTEMA no envía ningún correo. | usuario |
| HU-03 RF-4 | EL SISTEMA normaliza el correo de la solicitud como en el registro. | usuario |
| HU-03 RF-5 | EL SISTEMA guarda solo el SHA-256 del token, nunca el token; el token es de 256 bits aleatorios. | usuario |
| HU-03 RF-6 | CUANDO se pide una recuperación nueva, EL SISTEMA invalida los enlaces anteriores de esa cuenta, también si dos pedidos llegan a la vez. | usuario |
| HU-03 RF-7 | SI hay 3 solicitudes para el mismo correo desde la misma IP en 15 minutos, ENTONCES EL SISTEMA rechaza las siguientes durante 15 minutos (429), exista o no el correo. | usuario |
| HU-03 RF-8 | CUANDO alguien envía un token vigente y una contraseña nueva válida, EL SISTEMA cambia la contraseña de esa cuenta y de ninguna otra, invalida el token y los demás pendientes de la cuenta, y lleva a «Iniciar sesión» con el aviso «Tu contraseña quedó actualizada». | usuario |
| HU-03 RF-9 | SI el token no existe, venció, ya se usó o es de una cuenta desactivada, ENTONCES EL SISTEMA no cambia nada y responde el mismo mensaje en todos los casos. | usuario |
| HU-03 RF-10 | EL SISTEMA aplica a la contraseña nueva las reglas del registro: al menos 8 caracteres y hasta 72 bytes en UTF-8. | usuario |
| HU-03 RF-11 | CUANDO se restablece la contraseña, EL SISTEMA invalida las sesiones abiertas de la cuenta (el token de sesión deja de valer) y quita el bloqueo por intentos fallidos de ese correo. | usuario |
| HU-03 RF-12 | EL SISTEMA nunca muestra el token del enlace en la pantalla. | usuario |
| HU-03 RF-13 | CUANDO el proveedor de correo es `smtp`, EL SISTEMA envía por Gmail con la cuenta del proyecto (`TIENDA_CORREO_USUARIO`), en texto plano y en HTML con los colores de la marca, y escapa en el HTML lo que escribió el usuario (su nombre). | usuario |

### HU-04 — Mis datos

| ID | Requisito | Rol |
|---|---|---|
| HU-04 RF-1 | MIENTRAS la sesión está iniciada, EL SISTEMA muestra en «Mis datos» el correo, el tipo de cuenta (rol), el estado, la fecha de registro, el nombre, el apellido y el teléfono de la cuenta de la sesión, sin la contraseña. | usuario |
| HU-04 RF-2 | CUANDO el usuario guarda nombre, apellido y teléfono válidos, EL SISTEMA los actualiza en su cuenta, avisa «Datos guardados.» y el nombre del encabezado cambia sin volver a iniciar sesión. | usuario |
| HU-04 RF-3 | EL SISTEMA aplica a nombre, apellido y teléfono las reglas del registro: nombre y apellido obligatorios y sin ser solo espacios, hasta 80 caracteres; teléfono opcional hasta 20, y vacío queda como ausente. | usuario |
| HU-04 RF-4 | EL SISTEMA no cambia el correo, el rol, el estado ni la fecha de registro desde «Mis datos», aunque la solicitud los traiga. | usuario |
| HU-04 RF-5 | CUANDO el usuario da su contraseña actual correcta y una nueva válida, EL SISTEMA cambia la contraseña, responde con una sesión nueva que reemplaza a la actual, avisa «Contraseña actualizada.» y vacía los campos. | usuario |
| HU-04 RF-6 | CUANDO se cambia la contraseña, EL SISTEMA invalida las demás sesiones abiertas de la cuenta (su token deja de valer desde la siguiente petición). | usuario |
| HU-04 RF-7 | SI la contraseña actual es incorrecta, ENTONCES EL SISTEMA no cambia nada, lo dice junto al campo de la actual, y el intento cuenta en el límite del inicio de sesión (5 en 15 minutos desde la misma IP). | usuario |
| HU-04 RF-8 | EL SISTEMA aplica a la contraseña nueva las reglas del registro: al menos 8 caracteres y hasta 72 bytes en UTF-8. | usuario |
| HU-04 RF-9 | MIENTRAS hay datos escritos y sin guardar, EL SISTEMA pregunta antes de salir de «Mis datos»; sin sesión (porque se cerró) no pregunta. | usuario |
| HU-04 RF-10 | SI no se pudieron cargar los datos, ENTONCES EL SISTEMA lo avisa y ofrece reintentar. | usuario |
| HU-04 RF-11 | EL SISTEMA solo deja entrar a «Mis datos» con la sesión iniciada; sin sesión lleva a «Iniciar sesión» y, al entrar, vuelve a «Mis datos». | usuario |
| HU-04 RF-12 | EL SISTEMA ofrece, en cada campo de contraseña de «Mis datos», «Crear cuenta», «Elegir contraseña nueva» e «Iniciar sesión», un botón para mostrarla u ocultarla, que dice qué hace y empieza oculta. | usuario |

### HU-05 — Gestión de usuarios

| ID | Requisito | Rol |
|---|---|---|
| HU-05 RF-1 | EL SISTEMA solo deja gestionar usuarios al Administrador: el backend responde 403 a los demás roles y 401 sin sesión, y la opción «Usuarios» solo aparece en el menú del Administrador. | administrador |
| HU-05 RF-2 | CUANDO el Administrador abre «Usuarios», EL SISTEMA lista las cuentas de a 20 por página (hasta 100 si se pide), de la más reciente a la más antigua, con correo, nombre, apellido, rol, estado y fecha de registro, sin teléfono ni contraseña. | administrador |
| HU-05 RF-3 | CUANDO el Administrador filtra por rol, por estado o busca un texto, EL SISTEMA muestra solo las cuentas que cumplen todo a la vez (el texto se busca en correo, nombre y apellido), vuelve a la primera página y guarda los filtros en la dirección, para recargar o compartir la vista. | administrador |
| HU-05 RF-4 | CUANDO el Administrador cambia el rol de otra cuenta activa y lo confirma, EL SISTEMA guarda el rol nuevo, que rige desde la siguiente petición de esa persona. | administrador |
| HU-05 RF-5 | CUANDO el Administrador desactiva otra cuenta y lo confirma, EL SISTEMA la desactiva: no puede iniciar sesión, sus sesiones abiertas dejan de valer y sus enlaces de recuperación pendientes se anulan. | administrador |
| HU-05 RF-6 | CUANDO el Administrador reactiva una cuenta, EL SISTEMA la reactiva sin pedir confirmación; las sesiones que tenía antes de desactivarla siguen sin valer. | administrador |
| HU-05 RF-7 | SI el Administrador intenta cambiar su propio rol, ENTONCES EL SISTEMA lo rechaza (409 `ROL_PROPIO_NO_MODIFICABLE`); su fila no ofrece acciones. | administrador |
| HU-05 RF-8 | SI el Administrador intenta desactivar su propia cuenta, ENTONCES EL SISTEMA lo rechaza (409 `CUENTA_PROPIA_NO_DESACTIVABLE`). | administrador |
| HU-05 RF-9 | SI el cambio dejaría sin ningún Administrador activo, ENTONCES EL SISTEMA lo rechaza (409 `ULTIMO_ADMINISTRADOR_ACTIVO`), también cuando dos administradores se quitan el rol al mismo tiempo. | administrador |
| HU-05 RF-10 | SI se intenta cambiar el rol de una cuenta desactivada, ENTONCES EL SISTEMA lo rechaza (409 `ROL_DE_CUENTA_DESACTIVADA`): primero hay que reactivarla. | administrador |
| HU-05 RF-11 | SI la cuenta no existe, ENTONCES EL SISTEMA responde 404 `USUARIO_NO_ENCONTRADO`; si dos operaciones chocan y se agota la espera, 409 `OPERACION_SIMULTANEA`. | administrador |
| HU-05 RF-12 | CUANDO el backend rechaza una operación, EL SISTEMA muestra el motivo y deja la fila como estaba. | administrador |
| HU-05 RF-13 | EL SISTEMA ofrece los cuatro roles fijos (Cliente, Administrador, Empleado, Contador), y no permite crear roles ni permisos. | administrador |

### HU-06 — Categorías

| ID | Requisito | Rol |
|---|---|---|
| HU-06 RF-1 | EL SISTEMA solo deja crear, editar, ocultar, mostrar y borrar categorías al Administrador: el backend responde 403 a los demás roles y 401 sin sesión. | administrador |
| HU-06 RF-2 | CUANDO el Administrador crea una categoría con un nombre válido, y opcionalmente una descripción, EL SISTEMA la guarda visible y la muestra en la lista. | administrador |
| HU-06 RF-3 | EL SISTEMA exige el nombre, sin ser solo espacios, de hasta 60 caracteres, y acepta una descripción de hasta 300; recorta los espacios alrededor de los dos. | administrador |
| HU-06 RF-4 | SI ya existe una categoría con el mismo nombre, sin importar mayúsculas, tildes ni espacios alrededor, ENTONCES EL SISTEMA no la crea ni la renombra y responde 409 `CATEGORIA_EXISTENTE`, también si dos llegan a la vez. | administrador |
| HU-06 RF-5 | CUANDO el Administrador edita el nombre o la descripción de una categoría, EL SISTEMA los actualiza con las mismas reglas. | administrador |
| HU-06 RF-6 | CUANDO el Administrador sube una imagen para una categoría, EL SISTEMA la guarda con las reglas de «Imágenes subidas» (JPG, PNG o WebP por su contenido, hasta 2 MB, nombre generado por el sistema), reemplaza la anterior si había, y la sirve en el catálogo; también se puede quitar. | administrador |
| HU-06 RF-7 | SI la imagen no es JPG, PNG ni WebP, o pasa de 2 MB, ENTONCES EL SISTEMA la rechaza y la categoría queda como estaba. | administrador |
| HU-06 RF-8 | CUANDO el Administrador oculta una categoría, EL SISTEMA deja de mostrarla en el catálogo, y sus productos tampoco se listan por ella; CUANDO la vuelve a mostrar, reaparece con sus productos. | administrador |
| HU-06 RF-9 | CUANDO el Administrador borra una categoría sin productos, EL SISTEMA la borra junto con su imagen. | administrador |
| HU-06 RF-10 | SI la categoría tiene productos, ENTONCES EL SISTEMA no la borra, responde 409 `CATEGORIA_CON_PRODUCTOS` y ofrece ocultarla. | administrador |
| HU-06 RF-11 | CUANDO cualquiera (con o sin sesión) pide las categorías del catálogo, EL SISTEMA devuelve solo las visibles, en orden alfabético, con nombre, descripción y la dirección de su imagen. | administrador |
| HU-06 RF-12 | CUANDO el Administrador abre «Categorías», EL SISTEMA lista todas, visibles y ocultas, en orden alfabético, con su estado y cuántos productos tiene cada una. | administrador |
| HU-06 RF-13 | SI la categoría no existe, ENTONCES EL SISTEMA responde 404 `CATEGORIA_NO_ENCONTRADA`. | administrador |
| HU-06 RF-14 | CUANDO el Administrador borra una categoría, EL SISTEMA pide confirmación nombrándola antes de borrar. | administrador |

### HU-07 — Productos

| ID | Requisito | Rol |
|---|---|---|
| HU-07 RF-1 | EL SISTEMA solo deja crear, editar, activar y desactivar productos, y cambiar sus imágenes, al Administrador: 403 a los demás roles, 401 sin sesión. | administrador |
| HU-07 RF-2 | CUANDO el Administrador crea un producto con datos válidos, EL SISTEMA lo guarda activo, le asigna su código y lo muestra en la lista. | administrador |
| HU-07 RF-3 | EL SISTEMA exige nombre (hasta 120 caracteres, sin ser solo espacios), categoría existente, unidad de la lista y precio mayor que 0 y hasta 99 999,99 Bs con dos decimales como máximo; la marca (hasta 60) y la descripción (hasta 1000) son opcionales; recorta los espacios alrededor. | administrador |
| HU-07 RF-4 | EL SISTEMA genera el código con el prefijo de la categoría de alta y el número del producto («JAR-0007»), único, y no lo cambia después. | administrador |
| HU-07 RF-5 | CUANDO el Administrador edita un producto, EL SISTEMA actualiza sus datos con las mismas reglas, incluida la categoría; el código no cambia. | administrador |
| HU-07 RF-6 | EL SISTEMA guarda la ficha técnica como una lista ordenada de hasta 15 pares, con nombre (hasta 40) y valor (hasta 100) obligatorios; un par vacío se descarta y dos con el mismo nombre se rechazan. | administrador |
| HU-07 RF-7 | CUANDO el Administrador sube una imagen, EL SISTEMA la agrega al final con las reglas de «Imágenes subidas»; SI el producto ya tiene 5, ENTONCES la rechaza (409 `PRODUCTO_LLENO_DE_IMAGENES`). | administrador |
| HU-07 RF-8 | CUANDO el Administrador reordena las imágenes o quita una, EL SISTEMA guarda el orden nuevo y borra el archivo quitado; la primera es la principal. | administrador |
| HU-07 RF-9 | CUANDO el Administrador desactiva un producto, EL SISTEMA lo deja inactivo sin borrar nada; CUANDO lo activa, vuelve a estar activo. | administrador |
| HU-07 RF-10 | CUANDO el Administrador abre «Productos», EL SISTEMA lista los productos de a 20, del más reciente al más antiguo, con código, nombre, categoría, precio, estado e imagen principal, y deja filtrar por categoría, por estado y buscar por nombre, código o marca. | administrador |
| HU-07 RF-11 | SI el producto o la categoría no existen, ENTONCES EL SISTEMA responde 404 (`PRODUCTO_NO_ENCONTRADO`, `CATEGORIA_NO_ENCONTRADA`). | administrador |
| HU-07 RF-12 | CUANDO el Administrador intenta borrar una categoría con productos (activos o no), EL SISTEMA no la borra y responde 409 `CATEGORIA_CON_PRODUCTOS`; la pantalla de categorías ofrece ocultarla. *(Cierra HU-06 RF-10.)* | administrador |
| HU-07 RF-13 | EL SISTEMA muestra en la gestión de categorías cuántos productos tiene cada una. *(Cierra el conteo de HU-06 RF-12.)* | administrador |

### HU-08 — Inventario

| ID | Requisito | Rol |
|---|---|---|
| HU-08 RF-1 | EL SISTEMA solo deja registrar movimientos y cambiar el stock mínimo al Empleado y al Administrador; el Contador solo ve el inventario y el historial; el Cliente recibe 403 y sin sesión, 401. | empleado |
| HU-08 RF-2 | EL SISTEMA guarda el stock de cada producto, que empieza en 0, y solo lo cambia con un movimiento. | empleado |
| HU-08 RF-3 | CUANDO se registra una entrada con una cantidad válida, EL SISTEMA suma la cantidad al stock y guarda el movimiento con el stock que quedó, quién lo hizo, cuándo y la nota. | empleado |
| HU-08 RF-4 | CUANDO se registra un ajuste con la cantidad contada y un motivo, EL SISTEMA deja el stock en esa cantidad y guarda el movimiento con la diferencia (positiva o negativa), el motivo, quién, cuándo y la nota. | empleado |
| HU-08 RF-5 | EL SISTEMA exige cantidades con los decimales que admite la unidad del producto (hasta dos en metro, kilo y litro; ninguno en las demás): la entrada mayor que 0 y hasta 99 999,99; la cantidad contada de 0 a 9 999 999,99; SI no se cumple, ENTONCES responde 400 y no cambia nada. | empleado |
| HU-08 RF-6 | SI una entrada dejaría el stock por encima de 9 999 999,99, o un ajuste tiene la misma cantidad que el stock actual, ENTONCES EL SISTEMA lo rechaza (409 `STOCK_FUERA_DE_RANGO`, 409 `AJUSTE_SIN_CAMBIO`) sin cambiar nada. | empleado |
| HU-08 RF-7 | EL SISTEMA exige el motivo del ajuste de la lista fija; la nota es opcional (hasta 200 caracteres) salvo con el motivo «otro», que la exige. | empleado |
| HU-08 RF-8 | CUANDO dos movimientos del mismo producto llegan a la vez, EL SISTEMA los aplica uno después del otro, sin perder ninguno. | empleado |
| HU-08 RF-9 | CUANDO se fija o se quita el stock mínimo de un producto, EL SISTEMA lo guarda (con los decimales de su unidad, de 0 a 9 999 999,99) y marca el producto como «Stock bajo» mientras su stock sea menor o igual al mínimo. *16/10/2026 (prueba manual de HU-09):* con stock 0 la marca dice «Agotado» en lugar de «Stock bajo»; el filtro «Solo stock bajo» los sigue incluyendo, porque también hay que reponerlos. | empleado |
| HU-08 RF-10 | CUANDO se abre «Inventario», EL SISTEMA lista los productos (activos e inactivos) de a 20, por nombre, con código, nombre, categoría, unidad, stock, mínimo y la marca de stock bajo, y deja buscar por nombre o código, filtrar por categoría, por estado y solo los de stock bajo. | empleado |
| HU-08 RF-11 | CUANDO se abre el historial de un producto, EL SISTEMA lista sus movimientos de a 20, del más reciente al más antiguo, con fecha, tipo, motivo, cantidad, stock resultante, quién lo hizo y la nota. | empleado |
| HU-08 RF-12 | EL SISTEMA no permite editar ni borrar un movimiento registrado. | empleado |
| HU-08 RF-13 | SI el Administrador edita un producto y le pone una unidad sin decimales cuando su stock o su mínimo tienen decimales, ENTONCES EL SISTEMA lo rechaza (409 `UNIDAD_INCOMPATIBLE_CON_STOCK`) sin cambiar nada. | empleado |
| HU-08 RF-14 | SI el producto no existe, ENTONCES EL SISTEMA responde 404 (`PRODUCTO_NO_ENCONTRADO`). | empleado |

### HU-09 — Catálogo

| ID | Requisito | Rol |
|---|---|---|
| HU-09 RF-1 | EL SISTEMA muestra la portada y el catálogo a cualquiera, con o sin sesión. | visitante |
| HU-09 RF-2 | EL SISTEMA lista en la portada y en el catálogo solo los productos activos de categorías visibles; CUANDO el Administrador oculta una categoría, sus productos dejan de listarse, y CUANDO la vuelve a mostrar, reaparecen (HU-06 RF-8). | visitante |
| HU-09 RF-3 | CUANDO alguien abre la portada, EL SISTEMA muestra, en este orden: «Destacados» (si hay alguno), «Recientes» y un carrusel por cada categoría visible que tenga productos disponibles, en orden alfabético. Cada carrusel lleva hasta 12 productos disponibles, del más reciente al más antiguo, y un enlace «Ver todo» al catálogo con ese filtro, salvo «Destacados». Arriba, la ventana del letrero muestra la sección que se está mirando, y su cinta lleva a cada una. | visitante |
| HU-09 RF-4 | CUANDO el Administrador marca o desmarca «Destacado» en el formulario de un producto, al crearlo o al editarlo, EL SISTEMA lo agrega o lo quita del carrusel «Destacados». Sin marcarlo, un producto nuevo no es destacado. | visitante |
| HU-09 RF-5 | EL SISTEMA desplaza cada carrusel solo cuando la persona lo pide: con las flechas, la rueda o el trackpad, el teclado o arrastrando con el dedo, y se detiene en el borde de una tarjeta. Las flechas se desactivan en los extremos y no se muestran si todo entra en pantalla. Con movimiento reducido, el desplazamiento es inmediato. | visitante |
| HU-09 RF-6 | EL SISTEMA muestra en cada tarjeta la foto principal (sin foto, un ícono), el nombre, la marca si tiene, y el precio en bolivianos con su unidad de venta («Bs 12,50 / metro»; la unidad «unidad» no se escribe). La tarjeta entera lleva al detalle del producto (HU-10). | visitante |
| HU-09 RF-7 | MIENTRAS un producto tiene stock 0, EL SISTEMA lo marca «Agotado» (con ícono y texto) en el catálogo y no lo incluye en los carruseles de la portada. EL SISTEMA nunca muestra la cantidad en stock. | visitante |
| HU-09 RF-8 | CUANDO alguien busca un texto, EL SISTEMA devuelve los productos cuyo nombre, marca o código lo contienen, sin importar mayúsculas ni tildes. La búsqueda del encabezado lleva al catálogo con ese texto. | visitante |
| HU-09 RF-9 | CUANDO alguien elige filtros en el catálogo, EL SISTEMA combina los que estén puestos: categoría, marca, precio mínimo, precio máximo y «Solo disponibles». La lista de marcas sale de los productos que se pueden listar (RF-2), en orden alfabético. | visitante |
| HU-09 RF-10 | CUANDO alguien elige el orden, EL SISTEMA ordena por «Más recientes» (el de entrada), «Precio: menor a mayor», «Precio: mayor a menor» o «Nombre (A-Z)»; en cualquier orden, los disponibles van antes que los agotados. | visitante |
| HU-09 RF-11 | EL SISTEMA muestra el catálogo en páginas numeradas de 24 productos, con el total de resultados. La búsqueda, los filtros, el orden y la página quedan en la dirección, así que se pueden compartir, volver atrás y recargar; cambiar la búsqueda, un filtro o el orden vuelve a la página 1. | visitante |
| HU-09 RF-12 | SI no hay productos para la búsqueda o los filtros, ENTONCES EL SISTEMA lo dice y ofrece quitar los filtros; SI la tienda no tiene ningún producto para listar, ENTONCES la portada lo dice en lugar de los carruseles. | visitante |
| HU-09 RF-13 | SI la dirección trae un parámetro que no se entiende (página, categoría, precio u orden que no son válidos), ENTONCES EL SISTEMA lo descarta y usa el valor por defecto; la API responde 4xx, nunca 500. | visitante |
| HU-09 RF-14 | SI el precio mínimo es mayor que el máximo, o alguno no es un precio válido, ENTONCES EL SISTEMA lo avisa junto al filtro y no lo aplica. | visitante |
| HU-09 RF-15 | EL SISTEMA no expone en la API del catálogo la cantidad en stock ni datos de gestión (stock mínimo, estado activo, fechas de los movimientos): solo si el producto está disponible. | visitante |
| HU-09 RF-16 | EL SISTEMA muestra el panel con los accesos de cada rol en `/panel`, y un enlace «Panel» en el encabezado para el Administrador, el Empleado y el Contador. CUANDO alguien del personal inicia sesión sin un destino pendiente, va al panel; CUANDO un Cliente inicia sesión, vuelve a su destino o a la portada. | visitante |
| HU-09 RF-17 | EL SISTEMA muestra en el encabezado, con o sin sesión, el enlace «Catálogo» y una búsqueda. | visitante |

### HU-10 — Detalle del producto

| ID | Requisito | Rol |
|---|---|---|
| HU-10 RF-1 | EL SISTEMA muestra el detalle de un producto en `/producto/:id` a cualquiera, con o sin sesión. | visitante |
| HU-10 RF-2 | CUANDO alguien abre el detalle de un producto activo de una categoría visible, EL SISTEMA muestra su nombre, marca, código, categoría, precio con su unidad, descripción y ficha técnica (los pares en el orden en que se cargaron); sin marca, sin descripción o sin ficha, esa parte no sale. | visitante |
| HU-10 RF-3 | CUANDO alguien toca la categoría, EL SISTEMA abre el catálogo filtrado por ella. | visitante |
| HU-10 RF-4 | EL SISTEMA muestra «Disponible» si el producto tiene stock y «Agotado» (con ícono y texto) si no, y nunca la cantidad en stock ni el mínimo, ni en la pantalla ni en la API. | visitante |
| HU-10 RF-5 | EL SISTEMA muestra la foto principal grande y, si hay más de una, miniaturas en el orden cargado; CUANDO alguien elige una miniatura, EL SISTEMA la muestra grande. Sin fotos, o con una foto que no carga, muestra un ícono en su lugar. | visitante |
| HU-10 RF-6 | CUANDO alguien toca la foto grande, EL SISTEMA la abre a pantalla completa, donde se pasa a la anterior y a la siguiente con flechas, con el teclado y con el dedo (sin dar la vuelta: se detiene en la primera y en la última), y se cierra con un botón, con Escape o tocando afuera; al cerrarse, el foco vuelve a la foto. | visitante |
| HU-10 RF-7 | EL SISTEMA muestra debajo un carrusel «Más de <categoría>» con hasta 12 productos disponibles de la misma categoría, del más reciente al más antiguo, sin el producto que se está mirando; sin ninguno, el carrusel no sale. | visitante |
| HU-10 RF-8 | SI el producto no existe, está inactivo o su categoría está oculta, ENTONCES EL SISTEMA responde 404 sin distinguir los casos, y la pantalla dice que el producto no está disponible, con un enlace al catálogo. | visitante |
| HU-10 RF-9 | SI la dirección no trae un número de producto válido, ENTONCES EL SISTEMA muestra el mismo aviso que RF-8, sin consultar la API. | visitante |
| HU-10 RF-10 | MIENTRAS la persona con sesión es Administrador, EL SISTEMA muestra «Editar producto», que lleva a su formulario (HU-07). | visitante |
| HU-10 RF-11 | CUANDO se abre el detalle, EL SISTEMA pone el nombre del producto en la pestaña del navegador («<nombre> · Tienda Montero», como las demás pantallas). | visitante |

### HU-11 — Carrito

| ID | Requisito | Rol |
|---|---|---|
| HU-11 RF-1 | CUANDO un Cliente agrega un producto disponible con una cantidad válida, EL SISTEMA lo guarda en su carrito y actualiza la cantidad de líneas del encabezado. | cliente |
| HU-11 RF-2 | CUANDO un Cliente agrega un producto que ya está en su carrito, EL SISTEMA suma la cantidad a esa línea. | cliente |
| HU-11 RF-3 | SI la cantidad no es mayor que 0, pasa de 999 o tiene más decimales de los que admite la unidad del producto (ninguno en unidad, caja, par y rollo; dos en metro, kilo y litro), ENTONCES EL SISTEMA la rechaza con un mensaje junto al campo y no cambia el carrito. Al sumar a una línea, el límite de 999 vale para el resultado. | cliente |
| HU-11 RF-4 | SI el producto está agotado, inactivo, es de una categoría oculta o no existe, ENTONCES EL SISTEMA no lo agrega y lo informa; para inactivo, oculto o inexistente la respuesta es la misma. | cliente |
| HU-11 RF-5 | SI el carrito ya tiene 50 líneas y se agrega un producto que no está, ENTONCES EL SISTEMA no lo agrega y lo informa. | cliente |
| HU-11 RF-6 | CUANDO un visitante sin sesión toca «Agregar al carrito», EL SISTEMA lo lleva a iniciar sesión y, al terminar (también si crea la cuenta desde ahí), lo devuelve al mismo producto sin agregarlo. | cliente |
| HU-11 RF-7 | SI quien usa la API del carrito no es un Cliente, ENTONCES EL SISTEMA responde 403; sin sesión, 401. En la interfaz, el personal no ve el botón ni el ícono del carrito. | cliente |
| HU-11 RF-8 | CUANDO el Cliente abre `/carrito`, EL SISTEMA muestra cada línea con la foto principal, el nombre (enlaza al producto), el precio actual con su unidad, la cantidad y el subtotal, y el total de las líneas que se pueden comprar. | cliente |
| HU-11 RF-9 | MIENTRAS un producto del carrito está agotado, inactivo o en una categoría oculta, EL SISTEMA muestra su línea marcada «Agotado» o «No disponible», sin subtotal y sin contarla en el total, y deja quitarla. | cliente |
| HU-11 RF-10 | CUANDO el Cliente cambia la cantidad de una línea, EL SISTEMA la guarda con las reglas de RF-3 y recalcula el subtotal y el total. | cliente |
| HU-11 RF-11 | CUANDO el Cliente quita una línea o vacía el carrito (con confirmación), EL SISTEMA las borra y actualiza el total y el encabezado. | cliente |
| HU-11 RF-12 | MIENTRAS el carrito está vacío, EL SISTEMA lo dice en `/carrito`, con un enlace al catálogo. | cliente |
| HU-11 RF-13 | EL SISTEMA guarda el carrito de cada Cliente en el servidor, sin vencimiento: el mismo carrito se ve al volver a entrar y desde otro dispositivo. | cliente |
| HU-11 RF-14 | EL SISTEMA nunca muestra ni devuelve la cantidad en stock de un producto del carrito, ni en un error. | cliente |
| HU-11 RF-15 | CUANDO el Cliente toca − o + junto a la cantidad, en el detalle o en una línea del carrito, EL SISTEMA la baja o la sube de a 1, sin dejarla en 0 ni pasar de 999; en el carrito la guarda como en RF-10. *(19/10/2026, pedido en la prueba manual.)* | cliente |

### HU-12 — Confirmar el pedido

| ID | Requisito | Rol |
|---|---|---|
| HU-12 RF-1 | CUANDO el Cliente toca «Confirmar pedido» en `/carrito`, EL SISTEMA muestra la confirmación: las líneas con su precio de hoy, el total, que el pedido se retira en la tienda, el nombre y el teléfono de quien retira (llenos con los de la cuenta) y una nota opcional. | cliente |
| HU-12 RF-2 | CUANDO el Cliente confirma con datos válidos, EL SISTEMA, todo en una sola operación: crea el pedido en estado «Pendiente de pago» con una copia de cada línea (producto, nombre, unidad, precio de ese momento, cantidad y subtotal), el total y los datos de retiro; resta el stock de cada producto con un movimiento «Reserva por pedido»; vacía el carrito, y fija el vencimiento a 30 minutos de la confirmación. | cliente |
| HU-12 RF-3 | SI alguna línea está agotada, no disponible, con una cantidad que la unidad no admite o pide más de lo que hay en stock, ENTONCES EL SISTEMA no crea el pedido ni reserva nada, y marca qué línea falló y por qué, con un enlace al carrito para corregirla, sin decir cuánto stock hay. | cliente |
| HU-12 RF-4 | SI el carrito está vacío, ENTONCES EL SISTEMA no confirma el pedido. | cliente |
| HU-12 RF-5 | MIENTRAS el Cliente tiene un pedido pendiente de pago, EL SISTEMA no le deja confirmar otro: el carrito lo avisa y enlaza a ese pedido. | cliente |
| HU-12 RF-6 | EL SISTEMA exige el nombre de quien retira (hasta 160 caracteres) y su teléfono (hasta 20 caracteres); la nota es opcional, hasta 200 caracteres. Los espacios alrededor no cuentan. | cliente |
| HU-12 RF-7 | CUANDO dos confirmaciones piden a la vez el último stock de un producto, EL SISTEMA reserva solo para una; la otra se rechaza como en RF-3. Dos confirmaciones del mismo cliente a la vez crean un solo pedido. | cliente |
| HU-12 RF-8 | CUANDO el pedido se confirma, EL SISTEMA muestra su página: número, líneas, total, datos de retiro, estado y el tiempo que queda para pagar. | cliente |
| HU-12 RF-9 | CUANDO el Cliente cancela su pedido pendiente de pago (con confirmación), EL SISTEMA lo pasa a «Cancelado», devuelve el stock con un movimiento «Liberación por pedido» y devuelve las líneas al carrito. | cliente |
| HU-12 RF-10 | CUANDO pasan 30 minutos de la confirmación sin que el pedido se pague, EL SISTEMA lo pasa a «Vencido» y hace lo mismo que en RF-9. El vencimiento lo controla el sistema, no el proveedor de pago. | cliente |
| HU-12 RF-11 | CUANDO las líneas de un pedido vuelven al carrito, EL SISTEMA las suma a las que ya estén, con los límites del carrito (999 por línea y 50 líneas). | cliente |
| HU-12 RF-12 | SI se cancela un pedido que ya no está pendiente de pago, ENTONCES EL SISTEMA lo rechaza sin tocar el stock ni el carrito. | cliente |
| HU-12 RF-13 | SI quien usa la API de pedidos no es un Cliente, ENTONCES EL SISTEMA responde 403; un Cliente solo ve y cancela sus pedidos, y el de otro responde 404. | cliente |
| HU-12 RF-14 | CUANDO se abre el historial de un producto en «Inventario», EL SISTEMA muestra los movimientos «Reserva por pedido» y «Liberación por pedido» con el número de pedido, y el stock nunca queda negativo. | cliente |
| HU-12 RF-15 | SI el total cambió entre que el Cliente abrió la confirmación y la confirmó (cambió un precio), ENTONCES EL SISTEMA no crea el pedido y le muestra la confirmación con el total nuevo, para que confirme de nuevo. | cliente |
| HU-12 RF-16 | SI al volver al carrito una línea pasaría de 999, ENTONCES EL SISTEMA la deja en 999; SI el carrito ya tiene 50 líneas, ENTONCES las que no entran no vuelven, y al cancelar EL SISTEMA avisa cuántas no volvieron. | cliente |
| HU-12 RF-17 | MIENTRAS la dirección y el horario de retiro están configurados (`TIENDA_RETIRO_DIRECCION` y `TIENDA_RETIRO_HORARIO` en `.env`), EL SISTEMA los muestra en la confirmación y en la página del pedido; sin configurar, no muestra esa parte. | cliente |

### HU-13 — Pagar el pedido

| ID | Requisito | Rol |
|---|---|---|
| HU-13 RF-1 | MIENTRAS el pedido está pendiente de pago, EL SISTEMA ofrece «Pagar con QR» en su página; CUANDO el Cliente lo toca, EL SISTEMA genera el cobro por el total del pedido y muestra el QR y, si el sistema de pago la da, el enlace a su página de pago. | cliente |
| HU-13 RF-2 | EL SISTEMA genera un solo cobro por pedido: volver a pedirlo (otra pestaña, otra visita) muestra el mismo QR, salvo después de un error (RF-6). | cliente |
| HU-13 RF-3 | SI el sistema de pago no puede generar el cobro, ENTONCES EL SISTEMA no guarda nada, el pedido sigue pendiente y el Cliente ve que puede intentar de nuevo. | cliente |
| HU-13 RF-4 | MIENTRAS el QR está a la vista, EL SISTEMA consulta el pago cada pocos segundos, y CUANDO el Cliente toca «Ya pagué», en el acto. Cada consulta pregunta al sistema de pago; el pago nunca se da por hecho por lo que diga o muestre el Cliente. | cliente |
| HU-13 RF-5 | CUANDO el sistema de pago informa que el cobro está pagado, EL SISTEMA pasa el pedido a «Pagado» con la fecha del pago, y la página lo muestra con dónde y cuándo retirarlo. La reserva queda como venta, sin un movimiento de stock nuevo. Un pedido pagado no se cancela ni vence. | cliente |
| HU-13 RF-6 | CUANDO el sistema de pago informa un error en el cobro, EL SISTEMA lo descarta: el pedido sigue pendiente y el Cliente puede generar otro QR. | cliente |
| HU-13 RF-7 | EL SISTEMA vence un pedido que tiene cobro solo después de que el sistema de pago dijo, pasada la hora, que no está pagado: lo consultan la tarea de cada minuto y la página del pedido al llegar a cero. Si está pagado, el pedido queda «Pagado» en vez de vencer; SI la consulta falla, ENTONCES no lo vence y lo reintenta en la próxima vuelta. Mientras tanto sigue «Pendiente de pago», como mucho un minuto más: leer el carrito o el pedido no llama al sistema de pago. | cliente |
| HU-13 RF-8 | EL SISTEMA revisa cada minuto los cobros de los pedidos pendientes y de los vencidos o cancelados en las últimas 24 horas, para aplicar los pagos aunque el Cliente haya cerrado la página. | cliente |
| HU-13 RF-9 | CUANDO llega el pago de un pedido vencido o cancelado, SI todos sus productos siguen a la venta y alcanza el stock de cada línea, ENTONCES EL SISTEMA vuelve a reservar el stock («Reserva por pedido»), pasa el pedido a «Pagado» y resta sus cantidades del carrito (una línea que queda en cero se quita); SI no, ENTONCES lo deja «Pago a devolver», sin tocar el stock ni el carrito. | cliente |
| HU-13 RF-10 | CUANDO el sistema de pago avisa un pago (`callback_url`), EL SISTEMA responde siempre 200, exista o no la referencia, y solo adelanta la consulta de RF-4: el aviso por sí solo no confirma nada. | cliente |
| HU-13 RF-11 | CUANDO la misma confirmación de pago llega varias veces a la vez (la página, el aviso, la tarea), EL SISTEMA la aplica una sola vez: no reserva dos veces ni cambia un pedido que ya está pagado o para devolver. | cliente |
| HU-13 RF-12 | MIENTRAS el proveedor es el simulador, EL SISTEMA muestra bajo el QR «Simular pago» y «Simular error», marcados como herramienta de pruebas; con otro proveedor, ni los botones ni su endpoint existen. | cliente |
| HU-13 RF-13 | MIENTRAS el proveedor es Libélula (`TIENDA_PAGO_PROVEEDOR=libelula`), EL SISTEMA cobra con su cuenta, con la appkey y el correo solo en `.env`: sin ellos no arranca, y la appkey nunca sale en el registro ni en un mensaje de error. | cliente |
| HU-13 RF-14 | SI quien usa la API de pago de pedidos no es un Cliente, ENTONCES EL SISTEMA responde 403; un Cliente solo paga y consulta sus pedidos, y el de otro responde 404. | cliente |
| HU-13 RF-15 | CUANDO el Cliente abre un pedido «Pagado» o «Pago a devolver», EL SISTEMA muestra ese estado; en «Pago a devolver», que el stock ya no alcanzaba y que la tienda le devolverá el dinero. | cliente |

---

## 2. Requisitos No Funcionales

| ID | Requisito | Fuente |
|---|---|---|
| RNF-01 | **Arquitectura desacoplada y API REST:** Backend desarrollado en Java 21 con Spring Boot 4.1.1 estructurado en capas (controlador, servicio, repositorio) y API REST stateless; Frontend en Angular 22 como Single Page Application (SPA). | `CLAUDE.md` → Stack |
| RNF-02 | **Persistencia y modelo de datos:** Motor de base de datos relacional MySQL 8.4 LTS con codificación `utf8mb4` y cotejamiento `utf8mb4_unicode_ci`. Mapeo objeto-relacional mediante Spring Data JPA / Hibernate con `ddl-auto=update`, sin herramientas complejas de migración versionada. | `docs/decisiones.md` → Base de datos |
| RNF-03 | **Autenticación stateless:** Control de sesiones mediante JSON Web Tokens (JWT) firmados con algoritmo HMAC-SHA256 y clave criptográfica de al menos 256 bits (`TIENDA_JWT_SECRETO`). La autenticación se verifica exclusivamente en el backend; el token incluye id de usuario, rol y versión de sesión. | `docs/decisiones.md` → Autenticación por JWT stateless |
| RNF-04 | **Protección de credenciales:** Las contraseñas se almacenan únicamente como hashes unidireccionales generados con BCrypt (factor de costo 10), garantizando que nunca se persistan ni registren contraseñas en texto plano. | `docs/decisiones.md` → Contraseñas con BCrypt |
| RNF-05 | **Mitigación de ataques de fuerza bruta:** Mecanismo de bloqueo temporal de cuenta tras 5 intentos fallidos consecutivos de inicio de sesión en un lapso de 15 minutos, respondiendo con código HTTP 429 (`DEMASIADOS_INTENTOS`). | `docs/decisiones.md` → Inicio de sesión |
| RNF-06 | **Aislamiento de la pasarela de pagos:** El procesamiento de pagos opera detrás de la interfaz interna `IPago`, desacoplando la lógica de negocio del proveedor externo. Dispone de simulador local para pruebas y adaptador para el proveedor Libélula QR en producción. | `docs/decisiones.md` → Pago: interfaz propia con dos implementaciones |
| RNF-07 | **Validación y almacenamiento de imágenes:** Las imágenes de productos se limitan a 2 MB por archivo y se validan mediante la inspección de sus números mágicos de cabecera (JPG, PNG o WebP), descartando nombres originales y guardándolos en directorio configurado en el servidor (`TIENDA_IMAGENES_DIRECTORIO`). | `docs/decisiones.md` → Imágenes subidas |
| RNF-08 | **Aislamiento de entorno y secretos:** Prohibición estricta de incluir contraseñas o claves privadas en el código versionado. Todas las credenciales se inyectan mediante variables de entorno prefijadas con `TIENDA_` a través del archivo `.env`. | `docs/decisiones.md` → Variables de entorno con prefijo propio |
| RNF-09 | **Notificaciones asíncronas por correo:** Despacho de correos de recuperación de contraseña y avisos mediante adaptador SMTP conectado a Gmail con TLS o simulador en consola para pruebas de desarrollo. | `docs/decisiones.md` → Notificaciones |
| RNF-10 | **Accesibilidad y diseño responsivo:** Interfaz adaptable a pantallas móviles y de escritorio respetando estándares WCAG 2.1 nivel AA: contraste de texto de al menos 4.5:1, áreas interactivas de al menos 44 px y compatibilidad completa con navegación por teclado. | `PRODUCT.md` → Accessibility & Inclusion |
| RNF-11 | **Pruebas automatizadas y suite de CI:** Cobertura de pruebas unitarias e integración en backend sobre base de datos H2 en memoria y pruebas de componentes en frontend mediante Vitest sobre jsdom, ejecutadas en GitHub Actions en cada PR. | `CLAUDE.md` → CI, `docs/flujo-sdd.md` |
| RNF-12 | **Despliegue unificado con proxy inverso:** Despliegue empaquetado en contenedores Docker mediante Docker Compose, publicando un único puerto web a través de Nginx que redirige internamente `/api` hacia Spring Boot para eliminar problemas de CORS. | `docs/decisiones.md` → Despliegue con Docker y un solo origen |
