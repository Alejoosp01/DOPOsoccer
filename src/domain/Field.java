package domain;

/**
 * Cancha del partido.
 * El origen (0, 0) esta en la esquina superior izquierda y el eje Y crece
 * hacia abajo, como en Java2D. Los arcos quedan dentro de la cancha: el arco
 * izquierdo ocupa los primeros pixeles y el derecho los ultimos.
 */
public class Field {

    public static final double DEFAULT_WIDTH = 1280;
    public static final double DEFAULT_HEIGHT = 720;
    public static final double DEFAULT_GROUND_Y = 640;
    public static final double DEFAULT_GOAL_WIDTH = 100;
    public static final double DEFAULT_GOAL_HEIGHT = 280;
    public static final double BALL_RADIUS = 18;
    public static final double PLAYER_WIDTH = 70;
    public static final double PLAYER_HEIGHT = 120;

    private final double width;
    private final double height;
    private final double groundY;
    private final double goalWidth;
    private final double goalHeight;

    private final Goal leftGoal;
    private final Goal rightGoal;

    public Field() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GROUND_Y,
                DEFAULT_GOAL_WIDTH, DEFAULT_GOAL_HEIGHT);
    }

    public Field(double width, double height, double groundY,
                 double goalWidth, double goalHeight) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("La cancha debe tener dimensiones positivas");
        }
        if (groundY <= 0 || groundY > height) {
            throw new IllegalArgumentException("El suelo debe estar dentro de la cancha");
        }
        if (goalWidth <= 0 || goalWidth * 2 >= width) {
            throw new IllegalArgumentException("El ancho de los arcos no es valido");
        }
        if (goalHeight <= 0 || goalHeight > groundY) {
            throw new IllegalArgumentException("La altura de los arcos no es valida");
        }
        this.width = width;
        this.height = height;
        this.groundY = groundY;
        this.goalWidth = goalWidth;
        this.goalHeight = goalHeight;
        this.leftGoal = new Goal(0, groundY - goalHeight, goalWidth, goalHeight);
        this.rightGoal = new Goal(width - goalWidth, groundY - goalHeight, goalWidth, goalHeight);
    }

    public Goal getLeftGoal() {
        return leftGoal;
    }

    public Goal getRightGoal() {
        return rightGoal;
    }

    /**
     * Devuelve el arco que defiende el lado indicado.
     */
    public Goal getGoal(Side side) {
        if (side == Side.LEFT) {
            return leftGoal;
        }
        return rightGoal;
    }

    /**
     * X de la linea del arco izquierdo (borde derecho de la boca del arco).
     */
    public double getLeftGoalLineX() {
        return leftGoal.getRight();
    }

    /**
     * X de la linea del arco derecho (borde izquierdo de la boca del arco).
     */
    public double getRightGoalLineX() {
        return rightGoal.getX();
    }

    /**
     * Devuelve si el balon cabe verticalmente por la boca de los arcos.
     * La boca mide lo mismo en ambos arcos.
     */
    public boolean isInsideGoalMouth(double centerY, double radius) {
        return centerY - radius >= leftGoal.getY()
                && centerY + radius <= groundY;
    }

    public double getBallStartX() {
        return width / 2;
    }

    public double getBallStartY() {
        return groundY - BALL_RADIUS;
    }

    public double getLeftPlayerStartX() {
        return width / 4;
    }

    public double getRightPlayerStartX() {
        return width * 3 / 4;
    }

    public double getPlayerStartY() {
        return groundY - PLAYER_HEIGHT / 2;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getGroundY() {
        return groundY;
    }

    public double getGoalWidth() {
        return goalWidth;
    }

    public double getGoalHeight() {
        return goalHeight;
    }
}
