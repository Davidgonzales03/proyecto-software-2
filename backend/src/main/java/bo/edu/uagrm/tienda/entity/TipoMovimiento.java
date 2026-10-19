package bo.edu.uagrm.tienda.entity;

// Cómo cambió el stock (HU-08): una entrada suma; un ajuste lo deja en la cantidad contada. Un pedido resta al
// confirmarse y devuelve lo reservado al cancelarse o vencer (HU-12 RF-2, RF-9, RF-10)
public enum TipoMovimiento {
	ENTRADA, AJUSTE, RESERVA, LIBERACION
}
