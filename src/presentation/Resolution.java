package presentation;

/**
 * Resoluciones de la cancha que se pueden elegir al iniciar el juego.
 * El dominio siempre usa una cancha logica de 1280 x 720; la interfaz la
 * escala al tamaño de la resolucion elegida.
 */
public enum Resolution {

    SMALL("Pequena 960 x 540", 960, 540),
    MEDIUM("Mediana 1280 x 720", 1280, 720),
    LARGE("Grande 1600 x 900", 1600, 900);

    public static final double BASE_WIDTH = 1280;
    public static final double BASE_HEIGHT = 720;

    private final String label;
    private final int width;
    private final int height;

    Resolution(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    /**
     * Devuelve el texto que se muestra en la configuracion.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Devuelve el ancho de la ventana en pixeles.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Devuelve el alto de la ventana en pixeles.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Devuelve cuanto se escala el dibujo de la cancha logica (1 = 1280 x 720).
     */
    public double getScale() {
        return width / BASE_WIDTH;
    }
}
