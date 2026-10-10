package bo.edu.uagrm.tienda.service;

// Una imagen leída de AlmacenImagenes, lista para servir
public record ImagenGuardada(byte[] contenido, FormatoImagen formato) {
}
