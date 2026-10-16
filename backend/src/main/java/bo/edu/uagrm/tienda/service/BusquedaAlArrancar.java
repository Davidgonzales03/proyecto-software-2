package bo.edu.uagrm.tienda.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

// Sin migraciones, la columna de búsqueda de HU-09 llega vacía a los productos que ya existían: se completa al
// arrancar. Después la mantiene el propio producto al crearse y al editarse
@Component
@RequiredArgsConstructor
public class BusquedaAlArrancar {

	private final ProductoRepository productoRepository;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void completar() {
		productoRepository.findByBusquedaIsNull().forEach(Producto::actualizarBusqueda);
	}
}
