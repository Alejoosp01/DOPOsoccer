package domain;

import java.awt.geom.Rectangle2D;

/**
 * Balon del partido.
 * Guarda posicion, velocidad y los efectos de los poderes (Empuje e Ilusion).
 * La gravedad y los rebotes se calculan en update(dt, field).
 */
public class Ball {

    public static final double GRAVITY = 1800;
    public static final double RESTITUTION = 0.75;
    public static final double GROUND_FRICTION = 0.98;
    public static final double MIN_SPEED = 5;
    public static final double MIN_BOUNCE_SPEED = 60;

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius;

    private boolean pushCharged;
    private int decoyCount;
    private double illusionTime;
    private Power activePower;
    private Player powerOwner;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.velocityX = 0;
        this.velocityY = 0;
        this.pushCharged = false;
        this.decoyCount = 0;
        this.illusionTime = 0;
        this.activePower = null;
        this.powerOwner = null;
    }

    /**
     * Pone la velocidad del balon en los ejes x y y
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
     * Avanza la fisica del balon un instante.
     * Aplica gravedad, rebotes contra suelo, techo, paredes y arcos.
     */
    public void update(double dt, Field field) {
        if (dt <= 0 || field == null) {
            return;
        }
        velocityY += GRAVITY * dt;
        x += velocityX * dt;
        y += velocityY * dt;

        bounceOnCeiling(field);
        bounceOnGround(field);
        bounceOnLeftSide(field);
        bounceOnRightSide(field);

        updateEffects(dt);
    }

    /**
     * Devuelve el balon al centro de la cancha, quieto y sin efectos.
     */
    public void reset(Field field) {
        if (field == null) {
            return;
        }
        x = field.getBallStartX();
        y = field.getBallStartY();
        velocityX = 0;
        velocityY = 0;
        pushCharged = false;
        decoyCount = 0;
        illusionTime = 0;
        activePower = null;
        powerOwner = null;
    }

    /**
     * Devuelve el rectangulo que ocupa el balon.
     */
    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    private void bounceOnCeiling(Field field) {
        if (y - radius < 0) {
            y = radius;
            if (velocityY < 0) {
                velocityY = -velocityY * RESTITUTION;
            }
        }
    }

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
     * Carga el balon con el Empuje: el primer rival que lo toque sale empujado.
     */
    public void chargePush() {
        pushCharged = true;
    }

    /**
     * Descarga el balon del Empuje.
     */
    public void releasePush() {
        pushCharged = false;
    }

    /**
     * Inicia la Ilusion: aparecen copias falsas durante unos segundos.
     */
    public void startIllusion(int decoys, double seconds) {
        decoyCount = decoys;
        illusionTime = seconds;
    }

    /**
     * Guarda el poder que dejo el balon cargado y quien lo activo.
     */
    public void setActivePower(Power power, Player owner) {
        activePower = power;
        powerOwner = owner;
    }

    /**
     * Limpia el poder cargado en el balon.
     */
    public void clearActivePower() {
        activePower = null;
        powerOwner = null;
    }

    /**
     * Devuelve el poder que dejo el balon cargado, o null.
     */
    public Power getActivePower() {
        return activePower;
    }

    /**
     * Devuelve el jugador que activo el poder cargado, o null.
     */
    public Player getPowerOwner() {
        return powerOwner;
    }

    /**
     * Descuenta el tiempo de los efectos. dt esta en segundos.
     */
    public void updateEffects(double dt) {
        if (illusionTime > 0) {
            illusionTime = illusionTime - dt;
            if (illusionTime <= 0) {
                illusionTime = 0;
                decoyCount = 0;
            }
        }
    }

    /**
     * Devuelve si el balon tiene cargado el Empuje.
     */
    public boolean isPushCharged() {
        return pushCharged;
    }

    /**
     * Devuelve si la Ilusion esta activa.
     */
    public boolean isIllusionActive() {
        return illusionTime > 0;
    }

    /**
     * Devuelve la cantidad de copias falsas de la Ilusion.
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
