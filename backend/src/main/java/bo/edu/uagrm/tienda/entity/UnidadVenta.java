package bo.edu.uagrm.tienda.entity;

// Lista fija de unidades de venta (HU-07)
public enum UnidadVenta {
	UNIDAD, METRO, KILO, LITRO, CAJA, PAR, ROLLO;

	// Cuántos decimales admite una cantidad en esta unidad (HU-08): se vende 2,5 m de cable, no 2,5 taladros
	public int decimales() {
		return switch (this) {
			case METRO, KILO, LITRO -> 2;
			default -> 0;
		};
	}
}
