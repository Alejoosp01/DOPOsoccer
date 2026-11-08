package domain;

import java.awt.geom.Rectangle2D;

/**
 * Personaje dentro de la cancha.
 * Maneja posicion, movimiento, salto, patada, efectos y barra de poder.
 * El centro del jugador es (x, y) y el eje Y crece hacia abajo.
 */
public class Player {

    public static final double STUN_SECONDS = 3;
    public static final double WIDTH = 70;
    public static final double HEIGHT = 120;
    public static final double SHRUNK_SCALE = 0.5;

    public static final double GRAVITY = 1800;

    public static final double MOVE_SPEED_BASE = 200;
    public static final double MOVE_SPEED_PER_STAT = 50;
    public static final double JUMP_SPEED_BASE = 500;
    public static final double JUMP_SPEED_PER_STAT = 80;
    public static final double KICK_SPEED_BASE = 550;
    public static final double KICK_SPEED_PER_STAT = 60;
    public static final double KICK_LIFT = 0.35;
    public static final double KICK_COOLDOWN = 0.25;
    public static final double KICK_PUSH_DISTANCE = 40;
    public static final double KICK_REACH = 30;
    public static final int MIN_STRENGTH_TO_STUN = 1;

    public static final double TOUCH_SPEED_BASE = 220;
    public static final double TOUCH_SPEED_PER_STRENGTH = 30;
    public static final double TOUCH_LIFT = 200;

    public static final double POWER_CHARGE_SECONDS = 10;

    private GameCharacter character;
    private boolean leftSide;
    private double x;
    private double y;
    private double velocityY;

    private double stunTime;
    private double frozenTime;
    private double shrinkTime;
    private double confusedTime;
    private double kickCooldown;

    private double powerCharge;
    private boolean powerArmed;

    public Player(GameCharacter character, boolean leftSide, double x, double y) {
        this.character = character;
        this.leftSide = leftSide;
        this.x = x;
        this.y = y;
        this.velocityY = 0;
        this.stunTime = 0;
        this.frozenTime = 0;
        this.shrinkTime = 0;
        this.confusedTime = 0;
        this.kickCooldown = 0;
        this.powerCharge = 0;
        this.powerArmed = false;
    }

    /**
     * Pone al jugador en una posicion y limpia sus efectos.
     * La barra de poder se conserva; para vaciarla se usa resetPower().
     */
    public void reset(double x, double y) {
        setPosition(x, y);
        velocityY = 0;
        stunTime = 0;
        frozenTime = 0;
        shrinkTime = 0;
        confusedTime = 0;
        kickCooldown = 0;
        powerArmed = false;
    }

    /**
     * Pone la posicion del centro del jugador.
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Avanza la fisica del jugador: efectos, barra de poder y gravedad.
     * El movimiento horizontal se hace con move(...).
     */
    public void update(double dt, Field field) {
        if (dt <= 0 || field == null) {
            return;
        }
        updateEffects(dt);
        addPowerCharge(dt);

        velocityY += GRAVITY * dt;
        y += velocityY * dt;

        double halfHeight = getHeight() / 2;
        if (y - halfHeight < 0) {
            y = halfHeight;
            if (velocityY < 0) {
                velocityY = 0;
            }
        }
        if (y + halfHeight > field.getGroundY()) {
            y = field.getGroundY() - halfHeight;
            velocityY = 0;
        }
    }

    /**
     * Mueve al jugador horizontalmente. direction solo usa su signo.
     * Devuelve false si esta aturdido, congelado o no puede moverse.
     */
    public boolean move(double direction, double dt, Field field) {
        if (field == null || dt <= 0 || direction == 0 || !canAct()) {
            return false;
        }
        double sign = direction > 0 ? 1 : -1;
        if (isConfused()) {
            sign = -sign;
        }
        x += sign * getMoveSpeed() * dt;
        clampHorizontally(field);
        return true;
    }

    /**
     * Salta si el jugador esta en el suelo y puede actuar.
     */
    public boolean jump(Field field) {
        if (field == null || !canAct() || !isOnGround(field)) {
            return false;
        }
        velocityY = -getJumpSpeed();
        return true;
    }

    /**
     * Devuelve si el jugador esta apoyado en el suelo.
     */
    public boolean isOnGround(Field field) {
        if (field == null) {
            return false;
        }
        return y + getHeight() / 2 >= field.getGroundY() - 0.5;
    }

    /**
     * Patea el balon si esta al alcance y la patada no esta en enfriamiento.
     * El balon sale hacia el arco rival con velocidad segun la fuerza.
     */
    public boolean kick(Ball ball) {
        if (ball == null || !canKick() || !touchesBall(ball)) {
            return false;
        }
        double speed = getKickSpeed();
        double front = getWidth() / 2 + ball.getRadius() + 1;
        ball.setPosition(x + getAttackDirection() * front, ball.getY());
        ball.setVelocity(getAttackDirection() * speed, -speed * KICK_LIFT);
        kickCooldown = KICK_COOLDOWN;
        return true;
    }

    /**
     * Devuelve si la zona del pie alcanza el cuerpo de otro jugador (golpe directo).
     * La zona es un rectangulo de KICK_REACH delante del jugador, en la mitad baja del cuerpo.
     */
    public boolean reachesWithKick(Player other) {
        if (other == null) {
            return false;
        }
        Rectangle2D.Double body = getBounds();
        double footTop = body.getY() + body.getHeight() / 2;
        double footBottom = body.getMaxY();
        double footStart;
        double footEnd;
        if (leftSide) {
            footStart = body.getMaxX();
            footEnd = footStart + KICK_REACH;
        } else {
            footEnd = body.getX();
            footStart = footEnd - KICK_REACH;
        }
        Rectangle2D.Double rival = other.getBounds();
        return rival.getX() <= footEnd
                && rival.getMaxX() >= footStart
                && rival.getY() <= footBottom
                && rival.getMaxY() >= footTop;
    }

    /**
     * Devuelve si la fuerza del personaje alcanza para aturdir con una patada.
     */
    public boolean canStunWithKick() {
        return character.getStrength() >= MIN_STRENGTH_TO_STUN;
    }

    /**
     * Inicia la espera entre patadas aunque la patada no haya tocado el balon.
     */
    public void startKickCooldown() {
        kickCooldown = KICK_COOLDOWN;
    }

    /**
     * Devuelve si el balon toca el rectangulo del jugador.
     */
    public boolean touchesBall(Ball ball) {
        if (ball == null) {
            return false;
        }
        Rectangle2D.Double bounds = getBounds();
        double radius = ball.getRadius();
        return ball.getX() + radius >= bounds.getX()
                && ball.getX() - radius <= bounds.getMaxX()
                && ball.getY() + radius >= bounds.getY()
                && ball.getY() - radius <= bounds.getMaxY();
    }

    /**
     * Devuelve si el rectangulo de este jugador se cruza con otro.
     */
    public boolean intersects(Player other) {
        if (other == null) {
            return false;
        }
        return getBounds().intersects(other.getBounds());
    }

    /**
     * Mantiene al jugador entre las lineas de los arcos.
     */
    public void clampHorizontally(Field field) {
        if (field == null) {
            return;
        }
        double halfWidth = getWidth() / 2;
        double minX = field.getLeftGoalLineX() + halfWidth;
        double maxX = field.getRightGoalLineX() - halfWidth;
        x = Math.max(minX, Math.min(maxX, x));
    }

    /**
     * Devuelve la velocidad horizontal de movimiento segun la estadistica.
     */
    public double getMoveSpeed() {
        return MOVE_SPEED_BASE + MOVE_SPEED_PER_STAT * character.getSpeed();
    }

    /**
     * Devuelve la velocidad inicial del salto segun la estadistica.
     */
    public double getJumpSpeed() {
        return JUMP_SPEED_BASE + JUMP_SPEED_PER_STAT * character.getJump();
    }

    /**
     * Devuelve la velocidad del balon al patear segun la fuerza.
     */
    public double getKickSpeed() {
        return KICK_SPEED_BASE + KICK_SPEED_PER_STAT * character.getStrength();
    }

    /**
     * Devuelve la velocidad con la que el cuerpo empuja el balon al tocarlo.
     */
    public double getTouchSpeed() {
        return TOUCH_SPEED_BASE + TOUCH_SPEED_PER_STRENGTH * character.getStrength();
    }

    /**
     * Suma carga a la barra de poder. Se llena en POWER_CHARGE_SECONDS.
     */
    public void addPowerCharge(double dt) {
        if (dt <= 0) {
            return;
        }
        powerCharge = Math.min(1, powerCharge + dt / POWER_CHARGE_SECONDS);
    }

    /**
     * Devuelve la carga actual de la barra entre 0 y 1.
     */
    public double getPowerCharge() {
        return powerCharge;
    }

    /**
     * Devuelve si la barra esta llena.
     */
    public boolean isPowerReady() {
        return powerCharge >= 1;
    }

    /**
     * Devuelve si el poder fue armado y espera el siguiente golpe al balon.
     */
    public boolean isPowerArmed() {
        return powerArmed;
    }

    /**
     * Arma el poder si la barra esta llena y el jugador puede actuar.
     */
    public boolean armPower() {
        if (!isPowerReady() || !canAct()) {
            return false;
        }
        powerArmed = true;
        return true;
    }

    /**
     * Consume el poder armado, vacia la barra y devuelve si se consumio.
     */
    public boolean consumePower() {
        if (!powerArmed) {
            return false;
        }
        powerArmed = false;
        powerCharge = 0;
        return true;
    }

    /**
     * Deja la barra vacia y desarma el poder.
     */
    public void resetPower() {
        powerCharge = 0;
        powerArmed = false;
    }

    /**
     * Aturde al jugador durante 3 segundos.
     */
    public void stun() {
        stunTime = STUN_SECONDS;
    }

    /**
     * Congela al jugador durante los segundos indicados.
     */
    public void freeze(double seconds) {
        frozenTime = seconds;
    }

    /**
     * Encoge al jugador durante los segundos indicados.
     */
    public void shrink(double seconds) {
        shrinkTime = seconds;
    }

    /**
     * Invierte los controles del jugador durante los segundos indicados.
     */
    public void confuse(double seconds) {
        confusedTime = seconds;
    }

    /**
     * Empuja al jugador hacia su propio arco.
     */
    public void pushBack(double distance) {
        x = x - getAttackDirection() * distance;
    }

    /**
     * Descuenta el tiempo de todos los efectos. dt esta en segundos.
     */
    public void updateEffects(double dt) {
        stunTime = reduce(stunTime, dt);
        frozenTime = reduce(frozenTime, dt);
        shrinkTime = reduce(shrinkTime, dt);
        confusedTime = reduce(confusedTime, dt);
        kickCooldown = reduce(kickCooldown, dt);
    }

    /**
     * Resta dt al tiempo de un efecto sin bajar de 0.
     */
    private double reduce(double time, double dt) {
        if (time - dt < 0) {
            return 0;
        }
        return time - dt;
    }

    /**
     * Un jugador aturdido o congelado no puede moverse, saltar ni patear.
     */
    public boolean canAct() {
        return !isStunned() && !isFrozen();
    }

    /**
     * Devuelve si la patada esta lista (puede actuar y no hay enfriamiento).
     */
    public boolean canKick() {
        return canAct() && kickCooldown <= 0;
    }

    /**
     * Devuelve si el jugador esta aturdido.
     */
    public boolean isStunned() {
        return stunTime > 0;
    }

    /**
     * Devuelve si el jugador esta congelado.
     */
    public boolean isFrozen() {
        return frozenTime > 0;
    }

    /**
     * Devuelve si el jugador esta encogido.
     */
    public boolean isShrunk() {
        return shrinkTime > 0;
    }

    /**
     * Devuelve si el jugador tiene los controles invertidos.
     */
    public boolean isConfused() {
        return confusedTime > 0;
    }

    /**
     * Devuelve el personaje que controla este jugador.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * Devuelve si el jugador defiende el arco izquierdo.
     */
    public boolean isLeftSide() {
        return leftSide;
    }

    /**
     * Devuelve el lado que defiende el jugador.
     */
    public Side getSide() {
        if (leftSide) {
            return Side.LEFT;
        }
        return Side.RIGHT;
    }

    /**
     * Devuelve el ancho actual del jugador (cambia si esta encogido).
     */
    public double getWidth() {
        if (isShrunk()) {
            return WIDTH * SHRUNK_SCALE;
        }
        return WIDTH;
    }

    /**
     * Devuelve el alto actual del jugador (cambia si esta encogido).
     */
    public double getHeight() {
        if (isShrunk()) {
            return HEIGHT * SHRUNK_SCALE;
        }
        return HEIGHT;
    }

    /**
     * Devuelve el rectangulo que ocupa el jugador.
     */
    public Rectangle2D.Double getBounds() {
        double width = getWidth();
        double height = getHeight();
        return new Rectangle2D.Double(x - width / 2, y - height / 2, width, height);
    }

    /**
     * Direccion hacia el arco rival: 1 si ataca a la derecha, -1 si ataca a la izquierda.
     */
    public int getAttackDirection() {
        if (leftSide) {
            return 1;
        }
        return -1;
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
     * Devuelve la velocidad vertical actual.
     */
    public double getVelocityY() {
        return velocityY;
    }
}
