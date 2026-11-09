package domain;

/**
 * Personaje jugando en la cancha.
 * Une el perfil del jugador (estadisticas y puntos) con el personaje elegido
 * (pais y tiro de poder) y maneja posicion, vida, aturdimiento, aura,
 * barra de poder y el efecto que este sufriendo.
 * El centro del jugador es (x, y) y el eje Y crece hacia abajo.
 */
public class Player {

    public static final double WIDTH = 70;
    public static final double HEIGHT = 120;
    public static final double STUN_SECONDS = 3;

    public static final double BASE_SPEED = 300;
    public static final double BASE_JUMP_HEIGHT = 150;
    public static final double KICK_SPEED = 700;
    public static final double KICK_LIFT = 0.35;
    public static final double KICK_COOLDOWN = 0.25;
    public static final double KICK_REACH = 30;
    public static final double TOUCH_SPEED = 300;
    public static final double TOUCH_LIFT = 200;
    public static final double GROUND_FRICTION = 6;

    private final PlayerProfile profile;
    private final GameCharacter character;
    private final boolean leftSide;
    private final PowerBar powerBar;

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double life;
    private double stunTime;
    private double kickCooldown;
    private boolean auraActive;
    private Effect effect;

    public Player(PlayerProfile profile, GameCharacter character, boolean leftSide) {
        if (profile == null || character == null) {
            throw new IllegalArgumentException("El jugador necesita un perfil y un personaje");
        }
        this.profile = profile;
        this.character = character;
        this.leftSide = leftSide;
        this.powerBar = new PowerBar();
        this.x = 0;
        this.y = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.life = profile.getStats().getMaxLife();
        this.stunTime = 0;
        this.kickCooldown = 0;
        this.auraActive = false;
        this.effect = null;
    }

    /**
     * Prepara al jugador para un partido nuevo: vida al maximo, barra vacia y sin aura.
     */
    public void startMatch(double x, double y) {
        life = getMaxLife();
        powerBar.empty();
        auraActive = false;
        resetPosition(x, y);
    }

    /**
     * Vuelve a una posicion inicial (despues de un gol) y quita efectos y aturdimiento.
     */
    public void resetPosition(double x, double y) {
        setPosition(x, y);
        velocityX = 0;
        velocityY = 0;
        effect = null;
        kickCooldown = 0;
        if (isStunned()) {
            stunTime = 0;
            life = getMaxLife();
        }
    }

    /**
     * Pone la posicion del centro del jugador.
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Avanza el jugador un instante: efecto, aturdimiento, barra y fisica.
     */
    public void update(double dt, Field field) {
        if (dt <= 0 || field == null) {
            return;
        }
        updateEffect(dt);
        updateStun(dt);
        powerBar.update(dt);
        kickCooldown = Math.max(0, kickCooldown - dt);

        velocityY += Field.GRAVITY * dt;
        y += velocityY * dt;
        x += velocityX * dt;

        double halfHeight = HEIGHT / 2;
        if (y - halfHeight < 0) {
            y = halfHeight;
            if (velocityY < 0) {
                velocityY = 0;
            }
        }
        if (y + halfHeight >= field.getGroundY()) {
            y = field.getGroundY() - halfHeight;
            velocityY = 0;
            velocityX = velocityX * Math.max(0, 1 - GROUND_FRICTION * dt);
            if (Math.abs(velocityX) < 5) {
                velocityX = 0;
            }
        }
        double before = x;
        clampHorizontally(field);
        if (x != before) {
            velocityX = 0;
        }
    }

    /**
     * Descuenta el efecto actual y lo quita cuando termina.
     */
    private void updateEffect(double dt) {
        if (effect == null) {
            return;
        }
        effect.update(dt);
        if (effect.isOver()) {
            effect = null;
        }
    }

    /**
     * Descuenta el aturdimiento; al terminar, la vida vuelve al maximo.
     */
    private void updateStun(double dt) {
        if (stunTime <= 0) {
            return;
        }
        stunTime = Math.max(0, stunTime - dt);
        if (stunTime == 0) {
            life = getMaxLife();
        }
    }

    /**
     * Mueve al jugador horizontalmente. direction solo usa su signo.
     * Devuelve false si no puede moverse (aturdido o por un efecto).
     */
    public boolean move(double direction, double dt, Field field) {
        if (field == null || dt <= 0 || direction == 0 || !canMove()) {
            return false;
        }
        double sign;
        if (direction > 0) {
            sign = 1;
        } else {
            sign = -1;
        }
        x += sign * getMoveSpeed() * dt;
        clampHorizontally(field);
        return true;
    }

    /**
     * Salta si esta en el suelo y puede saltar.
     */
    public boolean jump(Field field) {
        if (field == null || !canJump() || !isOnGround(field)) {
            return false;
        }
        velocityY = -getJumpSpeed();
        return true;
    }

    /**
     * Patea el balon si lo toca y la patada esta lista.
     * La fuerza con que sale el balon es fija: Kick solo cambia el daño.
     */
    public boolean kick(Ball ball) {
        if (ball == null || !canKick() || !touchesBall(ball)) {
            return false;
        }
        double front = WIDTH / 2 + ball.getRadius() + 1;
        ball.setPosition(x + getAttackDirection() * front, ball.getY());
        ball.setVelocity(getAttackDirection() * KICK_SPEED, -KICK_SPEED * KICK_LIFT);
        kickCooldown = KICK_COOLDOWN;
        return true;
    }

    /**
     * Inicia la espera entre patadas aunque la patada no haya tocado el balon.
     */
    public void startKickCooldown() {
        kickCooldown = KICK_COOLDOWN;
    }

    /**
     * Recibe daño. La vida nunca baja de 0; al llegar a 0 queda aturdido 3 segundos.
     */
    public void receiveDamage(double amount) {
        if (amount <= 0 || isStunned()) {
            return;
        }
        life = Math.max(0, life - amount);
        if (life == 0) {
            stunTime = STUN_SECONDS;
        }
    }

    /**
     * Pone un efecto nuevo. Los efectos no se acumulan: reemplaza al anterior.
     */
    public void applyEffect(Effect newEffect) {
        if (newEffect == null) {
            return;
        }
        effect = newEffect;
        effect.onStart(this);
    }

    /**
     * Activa el aura si la barra esta llena y no tiene ya un poder activo.
     * La barra se vacia y empieza a recargarse en ese mismo instante.
     */
    public boolean activateAura() {
        if (auraActive || !powerBar.isFull()) {
            return false;
        }
        powerBar.empty();
        auraActive = true;
        return true;
    }

    /**
     * Gasta el aura (cuando toca el balon y sale el tiro de poder).
     */
    public boolean consumeAura() {
        if (!auraActive) {
            return false;
        }
        auraActive = false;
        return true;
    }

    /**
     * Lanza al jugador con una velocidad (la usan los efectos Empuje e Inconsciente).
     */
    public void launch(double newVelocityX, double newVelocityY) {
        velocityX = newVelocityX;
        velocityY = newVelocityY;
    }

    /**
     * Devuelve si la zona del pie alcanza el cuerpo de otro jugador (golpe directo).
     * La zona es un rectangulo de KICK_REACH delante del jugador, en la mitad baja del cuerpo.
     */
    public boolean reachesWithKick(Player other) {
        if (other == null) {
            return false;
        }
        Hitbox body = getBounds();
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
        Hitbox rival = other.getBounds();
        return rival.getX() <= footEnd
                && rival.getMaxX() >= footStart
                && rival.getY() <= footBottom
                && rival.getMaxY() >= footTop;
    }

    /**
     * Devuelve si el balon toca el rectangulo del jugador.
     */
    public boolean touchesBall(Ball ball) {
        if (ball == null) {
            return false;
        }
        Hitbox bounds = getBounds();
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
        double halfWidth = WIDTH / 2;
        double minX = field.getLeftGoalLineX() + halfWidth;
        double maxX = field.getRightGoalLineX() - halfWidth;
        x = Math.max(minX, Math.min(maxX, x));
    }

    /**
     * Devuelve si puede moverse: no esta aturdido y su efecto se lo permite.
     */
    public boolean canMove() {
        if (isStunned()) {
            return false;
        }
        return effect == null || effect.canMove();
    }

    /**
     * Devuelve si puede saltar: no esta aturdido y su efecto se lo permite.
     */
    public boolean canJump() {
        if (isStunned()) {
            return false;
        }
        return effect == null || effect.canJump();
    }

    /**
     * Devuelve si puede patear: no esta aturdido, su efecto lo permite y no hay espera.
     */
    public boolean canKick() {
        if (isStunned() || kickCooldown > 0) {
            return false;
        }
        return effect == null || effect.canKick();
    }

    /**
     * Devuelve si esta apoyado en el suelo.
     */
    public boolean isOnGround(Field field) {
        if (field == null) {
            return false;
        }
        return y + HEIGHT / 2 >= field.getGroundY() - 0.5;
    }

    /**
     * Devuelve la velocidad de desplazamiento: base * multiplicador de speed * efecto.
     */
    public double getMoveSpeed() {
        double speed = BASE_SPEED * profile.getStats().multiplier(StatType.SPEED);
        if (effect != null) {
            speed = speed * effect.speedFactor();
        }
        return speed;
    }

    /**
     * Devuelve la velocidad inicial del salto. Jump sube la ALTURA un 10 % por
     * punto: altura = base * multiplicador, y velocidad = raiz(2 * g * altura).
     */
    public double getJumpSpeed() {
        double height = BASE_JUMP_HEIGHT * profile.getStats().multiplier(StatType.JUMP);
        return Math.sqrt(2 * Field.GRAVITY * height);
    }

    /**
     * Devuelve el multiplicadorKick del jugador.
     */
    public double getKickMultiplier() {
        return profile.getStats().getKickMultiplier();
    }

    /**
     * Devuelve la vida maxima segun survival.
     */
    public double getMaxLife() {
        return profile.getStats().getMaxLife();
    }

    /**
     * Devuelve la vida actual.
     */
    public double getLife() {
        return life;
    }

    /**
     * Devuelve si esta aturdido.
     */
    public boolean isStunned() {
        return stunTime > 0;
    }

    /**
     * Devuelve los segundos de aturdimiento que le quedan.
     */
    public double getStunTimeLeft() {
        return stunTime;
    }

    /**
     * Devuelve si tiene el aura (poder activo).
     */
    public boolean hasAura() {
        return auraActive;
    }

    /**
     * Devuelve el efecto que sufre ahora, o null si no tiene.
     */
    public Effect getEffect() {
        return effect;
    }

    /**
     * Devuelve la barra de poder.
     */
    public PowerBar getPowerBar() {
        return powerBar;
    }

    /**
     * Devuelve el perfil del jugador (estadisticas y puntos).
     */
    public PlayerProfile getProfile() {
        return profile;
    }

    /**
     * Devuelve el personaje elegido.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * Devuelve si defiende el arco izquierdo.
     */
    public boolean isLeftSide() {
        return leftSide;
    }

    /**
     * Devuelve el lado que defiende.
     */
    public Side getSide() {
        if (leftSide) {
            return Side.LEFT;
        }
        return Side.RIGHT;
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
     * Devuelve el ancho del jugador.
     */
    public double getWidth() {
        return WIDTH;
    }

    /**
     * Devuelve el alto del jugador.
     */
    public double getHeight() {
        return HEIGHT;
    }

    /**
     * Devuelve el rectangulo que ocupa el jugador.
     */
    public Hitbox getBounds() {
        return new Hitbox(x - WIDTH / 2, y - HEIGHT / 2, WIDTH, HEIGHT);
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
     * Devuelve la velocidad horizontal (por un empuje).
     */
    public double getVelocityX() {
        return velocityX;
    }

    /**
     * Devuelve la velocidad vertical.
     */
    public double getVelocityY() {
        return velocityY;
    }
}
