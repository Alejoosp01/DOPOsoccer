package domain;

/**
 * Arco del partido.
 * El arco se representa como un rectangulo y el gol se valida cuando el balon
 * queda completamente contenido en el.
 */
public class Goal {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Goal(double x, double y, double width, double height) {
        if (width <= 0) {
            throw new IllegalArgumentException("El ancho del arco debe ser positivo");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("La altura del arco debe ser positiva");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Devuelve si el balon esta completamente dentro del arco.
     * Un balon parcialmente dentro no cuenta como gol.
     */
    public boolean contains(Ball ball) {
        if (ball == null) {
            return false;
        }
        return contains(ball.getX(), ball.getY(), ball.getRadius());
    }

    /**
     * Devuelve si un circulo esta completamente dentro del arco.
     */
    public boolean contains(double centerX, double centerY, double radius) {
        if (radius < 0) {
            return false;
        }
        return centerX - radius >= x
                && centerX + radius <= x + width
                && centerY - radius >= y
                && centerY + radius <= y + height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getRight() {
        return x + width;
    }

    public double getBottom() {
        return y + height;
    }

    public double getCenterX() {
        return x + width / 2;
    }

    public double getCenterY() {
        return y + height / 2;
    }
}
