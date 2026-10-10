package bo.edu.uagrm.tienda.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// Borra archivos de imágenes cuando termina la transacción: al confirmar, el primero; al revertir, el segundo. Así
// un fallo nunca deja una fila apuntando a un archivo borrado. Sin transacción activa (pruebas unitarias) se aplica
// enseguida, como si se confirmara
final class ArchivosAlTerminar {

	private ArchivosAlTerminar() {
	}

	static void borrar(AlmacenImagenes almacen, String siSeConfirma, String siSeRevierte) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			borrarSiHay(almacen, siSeConfirma);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int estado) {
				borrarSiHay(almacen, estado == STATUS_COMMITTED ? siSeConfirma : siSeRevierte);
			}
		});
	}

	private static void borrarSiHay(AlmacenImagenes almacen, String imagen) {
		if (imagen != null) {
			almacen.borrar(imagen);
		}
	}
}
