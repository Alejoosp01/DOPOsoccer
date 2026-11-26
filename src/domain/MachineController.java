package domain;

/**
 * Controlador base de las maquinas.
 * Cada cierto tiempo (su tiempo de reaccion) la maquina decide a donde ir y si
 * salta; entre decisiones se mueve hacia ese punto, patea si puede y usa el
 * poder cuando la barra esta llena. Cada perfil (subclase) cambia solo la forma
 * de decidir en decide(...), asi que agregar una maquina nueva es crear otra
 * subclase sin tocar Match ni MatchRunner.
 */
public abstract class MachineController implements PlayerController {

    private static final long serialVersionUID = 1L;

    public static final double CONTACT_MARGIN = 4;
    public static final double ARRIVAL_TOLERANCE = 1;
    public static final double GUARD_DISTANCE = 90;
    public static final double BLOCK_DISTANCE = 220;

    private final double reactionTime;
    private double reactionTimer;
    private boolean hasTarget;
    private double targetX;
    private boolean jumpPending;

    public MachineController(double reactionTime) {
        if (reactionTime <= 0) {
            throw new IllegalArgumentException("El tiempo de reaccion debe ser positivo");
        }
        this.reactionTime = reactionTime;
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

        if (self.getPowerBar().isFull() && !self.hasAura()) {
            match.activatePower(side);
        }

        reactionTimer = reactionTimer - dt;
        if (!hasTarget || reactionTimer <= 0) {
            decide(match, self, ball);
            hasTarget = true;
            reactionTimer = reactionTime;
        }

        if (jumpPending) {
            match.jump(side);
            jumpPending = false;
        }

        if (self.reachesBallWithKick(ball) && shouldKick(self, ball)) {
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
     * Elige el punto al que va la maquina y si salta; lo define cada perfil.
     */
    protected abstract void decide(Match match, Player self, Ball ball);

    /**
     * Devuelve si debe patear cuando alcanza el balon: por defecto solo si el balon esta delante.
     */
    protected boolean shouldKick(Player self, Ball ball) {
        return isBallInFront(self, ball);
    }

    /**
     * Fija el punto al que debe ir la maquina y si debe saltar.
     */
    protected void setTarget(double x, boolean jump) {
        targetX = x;
        jumpPending = jump;
    }

    /**
     * Devuelve la X para quedar detras del balon y empujarlo hacia el arco rival.
     */
    protected double attackX(Player self, Ball ball) {
        int direction = self.getAttackDirection();
        return ball.getX() - direction * (self.getWidth() / 2 + ball.getRadius() - CONTACT_MARGIN);
    }

    /**
     * Devuelve la X donde se para a cuidar su arco.
     */
    protected double guardX(Match match, Player self) {
        return getOwnGoalLine(match, self) + self.getAttackDirection() * GUARD_DISTANCE;
    }

    /**
     * Decision ofensiva: siempre va detras del balon para atacar.
     */
    protected void decideOffensive(Player self, Ball ball) {
        setTarget(attackX(self, ball), shouldJump(self, ball));
    }

    /**
     * Decision defensiva: cuida el arco y solo sale cuando el balon esta en su mitad.
     */
    protected void decideDefensive(Match match, Player self, Ball ball) {
        if (isBallInOwnHalf(match, self, ball)) {
            setTarget(attackX(self, ball), shouldJump(self, ball));
        } else {
            setTarget(guardX(match, self), shouldJump(self, ball));
        }
    }

    /**
     * Decision equilibrada: ataca si el balon esta delante y vuelve a defender si le queda atras.
     */
    protected void decideBalanced(Match match, Player self, Ball ball) {
        double attack = attackX(self, ball);
        if (isBallBehind(self, ball)) {
            double guard = guardX(match, self);
            boolean ballPassedGuard = (ball.getX() - guard) * self.getAttackDirection() < 0;
            if (ballPassedGuard) {
                setTarget(attack, shouldJump(self, ball));
            } else {
                setTarget(guard, shouldJump(self, ball));
            }
        } else {
            setTarget(attack, shouldJump(self, ball));
        }
    }

    /**
     * Salta si el balon esta por encima de la cabeza y, o esta muy cerca,
     * o viene hacia su arco desde una distancia corta (bloqueo).
     */
    protected boolean shouldJump(Player self, Ball ball) {
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
    protected boolean isBallInFront(Player self, Ball ball) {
        return (ball.getX() - self.getX()) * self.getAttackDirection() >= 0;
    }

    /**
     * Devuelve si el balon quedo entre el jugador y su propio arco.
     */
    protected boolean isBallBehind(Player self, Ball ball) {
        return !isBallInFront(self, ball);
    }

    /**
     * Devuelve si el balon esta en la mitad de la cancha que defiende el jugador.
     */
    protected boolean isBallInOwnHalf(Match match, Player self, Ball ball) {
        double middle = match.getField().getWidth() / 2;
        if (self.isLeftSide()) {
            return ball.getX() <= middle;
        }
        return ball.getX() >= middle;
    }

    /**
     * Devuelve la X de la linea del arco que defiende el jugador.
     */
    protected double getOwnGoalLine(Match match, Player self) {
        if (self.isLeftSide()) {
            return match.getField().getLeftGoalLineX();
        }
        return match.getField().getRightGoalLineX();
    }

    /**
     * Borra la decision actual para que la maquina vuelva a decidir en el siguiente instante.
     */
    @Override
    public void clearInput() {
        hasTarget = false;
        jumpPending = false;
        reactionTimer = 0;
    }

    /**
     * Devuelve cada cuantos segundos vuelve a decidir.
     */
    public double getReactionTime() {
        return reactionTime;
    }

    /**
     * Devuelve la X a la que se dirige la maquina ahora.
     */
    public double getTargetX() {
        return targetX;
    }

    /**
     * Devuelve el perfil de esta maquina.
     */
    public abstract MachineProfile getProfile();
}
