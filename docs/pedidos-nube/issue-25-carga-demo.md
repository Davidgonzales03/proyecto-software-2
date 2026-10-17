# Pedido para claude.ai/code: carga del catálogo de demostración (#25)

*17/10/2026.* Para una sesión en la nube sobre `FECHDEV/proyecto-software-2`, desde `main`.
Solo la parte 2 del issue (el código que carga); el contenido lo entrega David.
Copiar el bloque entero:

```text
Lee CLAUDE.md, AGENTS.md (sección «Datos de demostración (issue #25)»), docs/flujo-sdd.md,
docs/decisiones.md (índice y «Imágenes subidas»), docs/historias/HU-07.md y HU-10.md, y el issue #25.
Responde y escribe todo en español, siguiendo el estilo del código existente.

Tarea: implementar SOLO la parte 2 del issue #25 (el código que carga el catálogo de
demostración). El contenido (catalogo.csv y las fotos) lo entrega David aparte: NO lo crees,
no busques ni descargues imágenes de internet.

Qué hacer (backend, Spring Boot):
1. Un ApplicationRunner, como AdministradorInicial, que cargue el catálogo solo si la propiedad
   tienda.demo.directorio (variable TIENDA_DEMO_DIRECTORIO) tiene valor. Sin valor, no hace nada.
   Va con @ConfigurationProperties validadas, como AdministradorInicialProperties.
2. Ese directorio contiene catalogo.csv y las imágenes. Columnas, en este orden:
   categoria,nombre,descripcion,precio_bs,stock_inicial,imagenes,modelo_3d
   - imagenes: nombres de archivo separados por |; modelo_3d: se ignora por ahora (spike #14).
   - Crea las categorías que falten y los productos usando los servicios y las reglas que ya
     existen (validaciones, AlmacenImagenes, límite de 2 MB, formatos permitidos), sin duplicarlas.
3. Idempotente: si ya hay productos en la base, no carga nada y lo deja en el log.
4. Si una fila es inválida (precio, stock, imagen faltante o demasiado grande), que se vea en el
   log con el número de fila y el motivo. Decide si se salta la fila o se aborta la carga, y
   justifícalo en el PR.
5. Los campos del producto que el CSV no trae (por ejemplo marca, código o destacado) quedan con
   el valor que permitan las reglas existentes. Si alguno es obligatorio, NO lo inventes: anótalo
   en el PR como pregunta para Fabio.
6. Agrega TIENDA_DEMO_DIRECTORIO a .env.example, vacía y comentada, y al servicio del backend en
   docker-compose.yml, con un volumen de solo lectura hacia la carpeta. Sin secretos.

Pruebas (TDD, sobre H2, perfil test; nada de Testcontainers):
- Un CSV y dos o tres imágenes mínimas como fixture en src/test/resources (PNG pequeños generados).
- Casos: sin la propiedad no carga; carga categorías, productos e imágenes; una segunda corrida no
  duplica; una fila inválida se maneja como decidiste.
- Corre ./mvnw -q test y deja todo en verde.

Reglas:
- No instales dependencias ni cambies pom.xml. Si un parser de CSV te parece necesario, escribe uno
  simple (sin comillas con comas, documentado) o pregúntalo en el PR.
- No toques frontend/ ni las historias.
- Rama: chore/datos-demo-carga. Un commit en español, terminado con:
  Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
- Abre el PR con «Refs #25» (NO Closes: el issue se cierra cuando esté el contenido de David).
  En el PR: qué hace, cómo se activa, las decisiones que tomaste y las preguntas para Fabio.
```
