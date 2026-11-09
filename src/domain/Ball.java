package domain;

/**
 * Balon del partido.
 * Guarda posicion y velocidad, y el tiro de poder que lleva (si lo lleva).
 * La gravedad y los rebotes se calculan en update(dt, field).
 */
public class Ball {

    public static final double RESTITUTION = 0.75;
    public static final double GROUND_FRICTION = 0.98;
    public static final double MIN_SPEED = 5;
    public static final double MIN_BOUNCE_SPEED = 60;
    public static final double SHOT_MAX_SECONDS = 2;

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private final double radius;

    private PowerShot activeShot;
    private Player shotOwner;
    private double shotTime;
    private int decoyCount;
    private double decoyTime;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.velocityX = 0;
        this.velocityY = 0;
        this.activeShot = null;
        this.shotOwner = null;
        this.shotTime = 0;
        this.decoyCount = 0;
        this.decoyTime = 0;
    }

    /**
     * Pone la velocidad del balon en los ejes x y y.
     */
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /**
     * Pone la posicion del centro del balon.
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Avanza la fisica del balon un instante: gravedad (salvo en tiros rectos),
     * movimiento y rebotes contra suelo, techo, paredes y arcos.
     */
    public void update(double dt, Field field) {
        if (dt <= 0 || field == null) {
            return;
        }
        if (activeShot == null || activeShot.isAffectedByGravity()) {
            velocityY += Field.GRAVITY * dt;
        }
        x += velocityX * dt;
        y += velocityY * dt;

        bounceOnCeiling();
        bounceOnGround(field);
        bounceOnLeftSide(field);
        bounceOnRightSide(field);

        updateTimers(dt);
    }

    /**
     * Descuenta el tiempo del tiro activo y de los balones falsos.
     * Un tiro que no golpeo a nadie se apaga a los SHOT_MAX_SECONDS.
     */
    private void updateTimers(double dt) {
        if (activeShot != null) {
            shotTime = shotTime + dt;
            if (shotTime >= SHOT_MAX_SECONDS) {
                clearShot();
            }
        }
        if (decoyTime > 0) {
            decoyTime = Math.max(0, decoyTime - dt);
            if (decoyTime == 0) {
                decoyCount = 0;
            }
        }
    }

    /**
     * Devuelve el balon al centro de la cancha, quieto y sin tiro, para que caiga.
     */
    public void reset(Field field) {
        if (field == null) {
            return;
        }
        x = field.getBallStartX();
        y = field.getBallStartY();
        velocityX = 0;
        velocityY = 0;
        clearShot();
        decoyCount = 0;
        decoyTime = 0;
    }

    /**
     * Devuelve el rectangulo que ocupa el balon.
     */
    public Hitbox getBounds() {
        return new Hitbox(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Rebota el balon contra el techo de la cancha (y = 0).
     */
    private void bounceOnCeiling() {
        if (y - radius < 0) {
            y = radius;
            if (velocityY < 0) {
                velocityY = -velocityY * RESTITUTION;
            }
        }
    }

    /**
     * Rebota el balon contra el suelo y le aplica friccion.
     */
    private void bounceOnGround(Field field) {
        if (y + radius <= field.getGroundY()) {
            return;
        }
        y = field.getGroundY() - radius;
        if (Math.abs(velocityY) < MIN_BOUNCE_SPEED) {
            velocityY = 0;
        } else {
            velocityY = -velocityY * RESTITUTION;
        }
        velocityX = velocityX * GROUND_FRICTION;
        if (Math.abs(velocityX) < MIN_SPEED) {
            velocityX = 0;
        }
    }

    /**
     * Rebota contra la pared sobre el arco izquierdo o el poste, si no entra por la boca.
     */
    private void bounceOnLeftSide(Field field) {
        double line = field.getLeftGoalLineX();
        double goalTop = field.getLeftGoal().getY();
        if (y + radius <= goalTop) {
            if (x - radius < line) {
                x = line + radius;
                if (velocityX < 0) {
                    velocityX = -velocityX * RESTITUTION;
                }
            }
            return;
        }
        if (x < line) {
            bounceInsideLeftGoal(field);
            return;
        }
        if (x - radius >= line || field.isInsideGoalMouth(y, radius)) {
            return;
        }
        x = line + radius;
        if (velocityX < 0) {
            velocityX = -velocityX * RESTITUTION;
        }
    }

    /**
     * Rebota contra el fondo y el techo del arco izquierdo.
     */
    private void bounceInsideLeftGoal(Field field) {
        if (x - radius < 0) {
            x = radius;
            if (velocityX < 0) {
                velocityX = -velocityX * RESTITUTION;
            }
        }
        double goalTop = field.getLeftGoal().getY();
        if (y - radius < goalTop) {
            y = goalTop + radius;
            if (velocityY < 0) {
                velocityY = -velocityY * RESTITUTION;
            }
        }
    }

    /**
     * Rebota contra la pared sobre el arco derecho o el poste, si no entra por la boca.
     */
    private void bounceOnRightSide(Field field) {
        double line = field.getRightGoalLineX();
        double goalTop = field.getRightGoal().getY();
        if (y + radius <= goalTop) {
            if (x + radius > line) {
                x = line - radius;
                if (velocityX > 0) {
                    velocityX = -velocityX * RESTITUTION;
                }
            }
            return;
        }
        if (x > line) {
            bounceInsideRightGoal(field);
            return;
        }
        if (x + radius <= line || field.isInsideGoalMouth(y, radius)) {
            return;
        }
        x = line - radius;
        if (velocityX > 0) {
            velocityX = -velocityX * RESTITUTION;
        }
    }

    /**
     * Rebota contra el fondo y el techo del arco derecho.
     */
    private void bounceInsideRightGoal(Field field) {
        if (x + radius > field.getWidth()) {
            x = field.getWidth() - radius;
            if (velocityX > 0) {
                velocityX = -velocityX * RESTITUTION;
            }
        }
        double goalTop = field.getRightGoal().getY();
        if (y - radius < goalTop) {
            y = goalTop + radius;
            if (velocityY < 0) {
                velocityY = -velocityY * RESTITUTION;
            }
        }
    }

    /**
     * Marca que el balon lleva un tiro de poder lanzado por owner.
     */
    public void launchShot(PowerShot shot, Player owner) {
        activeShot = shot;
        shotOwner = owner;
        shotTime = 0;
    }

    /**
     * Quita el tiro de poder del balon.
     */
    public void clearShot() {
        activeShot = null;
        shotOwner = null;
        shotTime = 0;
    }

    /**
     * Devuelve si el balon lleva un tiro de poder.
     */
    public boolean hasActiveShot() {
        return activeShot != null;
    }

    /**
     * Devuelve el tiro de poder que lleva el balon, o null.
     */
    public PowerShot getActiveShot() {
        return activeShot;
    }

    /**
     * Devuelve quien lanzo el tiro de poder, o null.
     */
    public Player getShotOwner() {
        return shotOwner;
    }

    /**
     * Crea balones falsos que duran los segundos indicados (tiro Ilusion).
     */
    public void startDecoys(int count, double seconds) {
        decoyCount = count;
        decoyTime = seconds;
    }

    /**
     * Devuelve cuantos balones falsos hay ahora.
     */
    public int getDecoyCount() {
        return decoyCount;
    }

    /**
     * Devuelve la posicion en el eje x.
     */
    public double getX() {
        return x;
    }

    /**
     * Devuelve la posicion en el eje y.
     */
    public double getY() {
        return y;
    }

    /**
     * Devuelve la velocidad en el eje x.
     */
    public double getVelocityX() {
        return velocityX;
    }

    /**
     * Devuelve la velocidad en el eje y.
     */
    public double getVelocityY() {
        return velocityY;
    }

    /**
     * Devuelve el radio del balon.
     */
    public double getRadius() {
        return radius;
    }
}