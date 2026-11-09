package domain;

/**
 * Rectangulo de colision de un objeto del juego.
 * (x, y) es la esquina superior izquierda y el eje Y crece hacia abajo.
 * Reemplaza a java.awt.geom.Rectangle2D para que el dominio no dependa de AWT.
 */
public final class Hitbox {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Hitbox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Devuelve si este rectangulo y otro se solapan (tienen area en comun).
     * Si solo se tocan en el borde no cuenta, igual que Rectangle2D.intersects.
     */
    public boolean intersects(Hitbox other) {
        if (other == null) {
            return false;
        }
        return x < other.getMaxX()
                && getMaxX() > other.getX()
                && y < other.getMaxY()
                && getMaxY() > other.getY();
    }

    /**
     * Devuelve la X del borde izquierdo.
     */
    public double getX() {
        return x;
    }

    /**
     * Devuelve la Y del borde de arriba.
     */
    public double getY() {
        return y;
    }

    /**
     * Devuelve el ancho.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Devuelve el alto.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Devuelve la X del borde derecho.
     */
    public double getMaxX() {
        return x + width;
    }

    /**
     * Devuelve la Y del borde de abajo.
     */
    public double getMaxY() {
        return y + height;
    }

    /**
     * Devuelve la X del centro.
     */
    public double getCenterX() {
        return x + width / 2;
    }

    /**
     * Devuelve la Y del centro.
     */
    public double getCenterY() {
        return y + height / 2;
    }
}
