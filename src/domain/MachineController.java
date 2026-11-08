package domain;

/**
 * Controlador de la maquina.
 * Cada cierto tiempo (su tiempo de reaccion) decide a donde ir y si salta:
 * ataca poniendose detras del balon, vuelve a cuidar su arco cuando el balon
 * le queda atras y salta para bloquear balones altos que vienen hacia ella.
 * Patea solo si el balon esta delante (hacia el arco rival) y usa el poder
 * cuando la barra esta llena. Un nivel mas alto reacciona mas rapido.
 */
public class MachineController implements PlayerController {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    public static final int DEFAULT_LEVEL = 2;
    public static final double BASE_REACTION = 0.30;
    public static final double REACTION_PER_LEVEL = 0.05;

    public static final double CONTACT_MARGIN = 4;
    public static final double ARRIVAL_TOLERANCE = 1;
    public static final double GUARD_DISTANCE = 90;
    public static final double BLOCK_DISTANCE = 220;

    private final int level;
    private double reactionTimer;
    private boolean hasTarget;
    private double targetX;
    private boolean jumpPending;

    public MachineController() {
        this(DEFAULT_LEVEL);
    }

    public MachineController(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException("El nivel de la maquina debe estar entre 1 y 5");
        }
        this.level = level;
        this.reactionTimer = 0;
        this.hasTarget = false;
        this.targetX = 0;
        this.jumpPending = false;
    }

    /**
     * Decide (cuando le toca) y ejecuta las acciones de la maquina en este instante.
     */
    @Override
    public void update(Match match, Side side, double dt) {
        if (match == null || side == null || dt <= 0) {
            return;
        }
        if (match.getState() != MatchState.PLAYING) {
            return;
        }

        Player self = match.getPlayer(side);
        Ball ball = match.getBall();

        if (self.isPowerReady() && !self.isPowerArmed()) {
            match.armPower(side);
        }

        reactionTimer = reactionTimer - dt;
        if (!hasTarget || reactionTimer <= 0) {
            decide(match, self, ball);
            reactionTimer = getReactionTime();
        }

        if (jumpPending) {
            match.jump(side);
            jumpPending = false;
        }

        if (self.touchesBall(ball) && isBallInFront(self, ball)) {
            if (self.canKick()) {
                match.kick(side);
            }
            return;
        }

        double difference = targetX - self.getX();
        if (Math.abs(difference) > ARRIVAL_TOLERANCE) {
            if (difference > 0) {
                match.move(side, 1, dt);
            } else {
                match.move(side, -1, dt);
            }
        }
    }

    /**
     * Elige a donde moverse y si debe saltar, segun donde esta el balon.
     */
    private void decide(Match match, Player self, Ball ball) {
        int direction = self.getAttackDirection();
        double attackX = ball.getX()
                - direction * (self.getWidth() / 2 + ball.getRadius() - CONTACT_MARGIN);

        if (isBallBehind(self, ball)) {
            double guardX = getOwnGoalLine(match, self) + direction * GUARD_DISTANCE;
            boolean ballPassedGuard = (ball.getX() - guardX) * direction < 0;
            if (ballPassedGuard) {
                targetX = attackX;
            } else {
                targetX = guardX;
            }
        } else {
            targetX = attackX;
        }
        hasTarget = true;
        jumpPending = shouldJump(self, ball);
    }

    /**
     * Salta si el balon esta por encima de la cabeza y, o esta muy cerca,
     * o viene hacia su arco desde una distancia corta (bloqueo).
     */
    private boolean shouldJump(Player self, Ball ball) {
        double headY = self.getY() - self.getHeight() / 2;
        boolean aboveHead = ball.getY() + ball.getRadius() < headY;
        if (!aboveHead) {
            return false;
        }
        double distance = Math.abs(ball.getX() - self.getX());
        if (distance <= self.getWidth()) {
            return true;
        }
        boolean comingHome = ball.getVelocityX() * self.getAttackDirection() < 0;
        return comingHome && isBallInFront(self, ball) && distance <= BLOCK_DISTANCE;
    }

    /**
     * Devuelve si el balon esta delante del jugador, es decir, hacia el arco rival.
     */
    private boolean isBallInFront(Player self, Ball ball) {
        return (ball.getX() - self.getX()) * self.getAttackDirection() >= 0;
    }

    /**
     * Devuelve si el balon quedo entre el jugador y su propio arco.
     */
    private boolean isBallBehind(Player self, Ball ball) {
        return !isBallInFront(self, ball);
    }

    /**
     * Devuelve la X de la linea del arco que defiende el jugador.
     */
    private double getOwnGoalLine(Match match, Player self) {
        if (self.isLeftSide()) {
            return match.getField().getLeftGoalLineX();
        }
        return match.getField().getRightGoalLineX();
    }

    /**
     * Devuelve cada cuantos segundos vuelve a decidir: 0.30 en nivel 1 y 0.10 en nivel 5.
     */
    public double getReactionTime() {
        return BASE_REACTION - REACTION_PER_LEVEL * (level - 1);
    }

    /**
     * Devuelve el nivel de la maquina (1 a 5).
     */
    public int getLevel() {
        return level;
    }
}
