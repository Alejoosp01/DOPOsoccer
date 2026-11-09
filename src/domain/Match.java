package domain;

/**
 * Motor de un partido.
 * Une cancha, balon, jugadores, marcador, tiempo y estado.
 * No depende de Swing: toda la simulacion se avanza con update(dt).
 */
public class Match {

    public static final double GOAL_PAUSE_SECONDS = 1.0;
    public static final double MAX_STEP = 1.0 / 120.0;

    private final MatchConfig config;
    private final Field field;
    private final Player leftPlayer;
    private final Player rightPlayer;
    private final Ball ball;
    private final Score score;

    private MatchState state;
    private MatchState stateBeforePause;
    private double timeLeft;
    private double goalPauseRemaining;
    private int remainingBallsLeft;
    private int remainingBallsRight;
    private boolean goldenGoal;
    private Winner winner;

    public Match(MatchConfig config, Player leftPlayer, Player rightPlayer) {
        this(config, new Field(), leftPlayer, rightPlayer);
    }

    public Match(MatchConfig config, Field field, Player leftPlayer, Player rightPlayer) {
        if (config == null) {
            throw new IllegalArgumentException("La configuracion no puede ser null");
        }
        if (field == null) {
            throw new IllegalArgumentException("La cancha no puede ser null");
        }
        if (leftPlayer == null || rightPlayer == null) {
            throw new IllegalArgumentException("Los jugadores no pueden ser null");
        }
        if (!leftPlayer.isLeftSide() || rightPlayer.isLeftSide()) {
            throw new IllegalArgumentException("Los jugadores deben estar en lados opuestos");
        }
        this.config = config;
        this.field = field;
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.ball = new Ball(field.getBallStartX(), field.getBallStartY(), Field.BALL_RADIUS);
        this.score = new Score();
        this.state = MatchState.READY;
        this.stateBeforePause = null;
        this.timeLeft = config.getDuration();
        this.goalPauseRemaining = 0;
        this.remainingBallsLeft = config.getBallsPerSide();
        this.remainingBallsRight = config.getBallsPerSide();
        this.goldenGoal = false;
        this.winner = Winner.NONE;
        resetPositions();
    }

    /**
     * Prepara el partido y lo pone en juego desde cero.
     */
    public void start() {
        score.reset();
        winner = Winner.NONE;
        goldenGoal = false;
        stateBeforePause = null;
        timeLeft = config.getDuration();
        remainingBallsLeft = config.getBallsPerSide();
        remainingBallsRight = config.getBallsPerSide();
        goalPauseRemaining = 0;
        leftPlayer.resetPower();
        rightPlayer.resetPower();
        resetPositions();
        state = MatchState.PLAYING;
    }

    /**
     * Avanza el partido un instante. dt esta en segundos.
     * Si el partido esta pausado, listo o terminado no cambia nada.
     */
    public void update(double dt) {
        if (dt <= 0) {
            return;
        }
        if (state == MatchState.READY || state == MatchState.PAUSED
                || state == MatchState.FINISHED) {
            return;
        }
        if (state == MatchState.GOAL) {
            updateGoalPause(dt);
            return;
        }

        double remaining = dt;
        while (remaining > 0 && state == MatchState.PLAYING) {
            double step = Math.min(remaining, MAX_STEP);
            leftPlayer.update(step, field);
            rightPlayer.update(step, field);
            resolvePlayerCollision();
            ball.update(step, field);
            resolveBallTouch(leftPlayer);
            resolveBallTouch(rightPlayer);
            remaining = remaining - step;

            if (checkGoal()) {
                break;
            }
            if (config.isTimed() && !goldenGoal) {
                timeLeft = timeLeft - step;
                if (timeLeft <= 0) {
                    handleTimeExpired();
                }
            }
        }
    }

    /**
     * Pausa el partido conservando el estado anterior.
     */
    public void pause() {
        if (state == MatchState.PLAYING || state == MatchState.GOAL) {
            stateBeforePause = state;
            state = MatchState.PAUSED;
        }
    }

    /**
     * Reanuda el partido despues de una pausa.
     */
    public void resume() {
        if (state == MatchState.PAUSED) {
            if (stateBeforePause == null) {
                state = MatchState.PLAYING;
            } else {
                state = stateBeforePause;
            }
            stateBeforePause = null;
        }
    }

    /**
     * Mueve al jugador del lado indicado. Solo funciona con el partido en juego.
     */
    public boolean move(Side side, double direction, double dt) {
        if (state != MatchState.PLAYING) {
            return false;
        }
        return getPlayer(side).move(direction, dt, field);
    }

    /**
     * Hace saltar al jugador del lado indicado.
     */
    public boolean jump(Side side) {
        if (state != MatchState.PLAYING) {
            return false;
        }
        return getPlayer(side).jump(field);
    }

    /**
     * Patea con el jugador del lado indicado.
     * Si el pie alcanza el balon, lo patea. Si el pie alcanza al rival (golpe
     * directo), el rival queda aturdido y empujado, haya balon o no.
     * Devuelve true si la patada toco el balon o al rival.
     */
    public boolean kick(Side side) {
        if (state != MatchState.PLAYING) {
            return false;
        }
        Player player = getPlayer(side);
        Player opponent = getPlayer(side.opposite());
        if (!player.canKick()) {
            return false;
        }
        boolean hitsRival = player.reachesWithKick(opponent);
        boolean kicked = player.kick(ball);
        if (kicked) {
            activatePower(player);
        }
        if (hitsRival) {
            if (!kicked) {
                player.startKickCooldown();
            }
            if (player.canStunWithKick()) {
                opponent.stun();
                opponent.pushBack(Player.KICK_PUSH_DISTANCE);
                opponent.clampHorizontally(field);
            }
        }
        return kicked || hitsRival;
    }

    /**
     * Arma el poder del jugador del lado indicado para el siguiente golpe.
     */
    public boolean armPower(Side side) {
        if (state != MatchState.PLAYING) {
            return false;
        }
        return getPlayer(side).armPower();
    }

    /**
     * Consume el poder armado y lo ejecuta sobre el balon.
     */
    private void activatePower(Player player) {
        if (!player.canAct() || !player.isPowerArmed() || !player.consumePower()) {
            return;
        }
        Player opponent = getPlayer(player.getSide().opposite());
        Power power = player.getCharacter().getPower();
        power.activate(ball, player, opponent);
        ball.setActivePower(power, player);
    }

    private void updateGoalPause(double dt) {
        goalPauseRemaining = goalPauseRemaining - dt;
        if (goalPauseRemaining > 0) {
            return;
        }
        goalPauseRemaining = 0;
        if (winner != Winner.NONE) {
            state = MatchState.FINISHED;
            return;
        }
        resetPositions();
        state = MatchState.PLAYING;
    }

    private boolean checkGoal() {
        if (field.getLeftGoal().contains(ball)) {
            registerGoal(Side.RIGHT);
            return true;
        }
        if (field.getRightGoal().contains(ball)) {
            registerGoal(Side.LEFT);
            return true;
        }
        return false;
    }

    /**
     * El lado que anota es el rival del arco donde entro el balon.
     * Asi el autogol queda cubierto sin logica especial.
     */
    private void registerGoal(Side scorer) {
        score.score(scorer);
        Side conceding = scorer.opposite();
        if (config.getMatchType() == MatchType.SURVIVAL) {
            if (conceding == Side.LEFT) {
                remainingBallsLeft = Math.max(0, remainingBallsLeft - 1);
            } else {
                remainingBallsRight = Math.max(0, remainingBallsRight - 1);
            }
        }
        Winner decided = decideWinner(scorer);
        if (decided != Winner.NONE) {
            winner = decided;
        }
        state = MatchState.GOAL;
        goalPauseRemaining = GOAL_PAUSE_SECONDS;
    }

    private Winner decideWinner(Side scorer) {
        if (goldenGoal) {
            return Winner.fromSide(scorer);
        }
        if (config.getMatchType() == MatchType.GOALS) {
            if (score.getLeftGoals() >= config.getGoalsTarget()) {
                return Winner.LEFT;
            }
            if (score.getRightGoals() >= config.getGoalsTarget()) {
                return Winner.RIGHT;
            }
            return Winner.NONE;
        }
        if (config.getMatchType() == MatchType.SURVIVAL) {
            if (remainingBallsLeft <= 0) {
                return Winner.RIGHT;
            }
            if (remainingBallsRight <= 0) {
                return Winner.LEFT;
            }
            return Winner.NONE;
        }
        return Winner.NONE;
    }

    private void handleTimeExpired() {
        timeLeft = 0;
        if (score.isTie()) {
            goldenGoal = true;
            return;
        }
        finish(score.leader());
    }

    private void finish(Winner matchWinner) {
        winner = matchWinner;
        state = MatchState.FINISHED;
    }

    private void resetPositions() {
        ball.reset(field);
        leftPlayer.reset(field.getLeftPlayerStartX(), field.getPlayerStartY());
        rightPlayer.reset(field.getRightPlayerStartX(), field.getPlayerStartY());
    }

    /**
     * Separa a los jugadores si sus rectangulos se cruzan.
     */
    private void resolvePlayerCollision() {
        if (!leftPlayer.intersects(rightPlayer)) {
            return;
        }
        double overlap = Math.min(leftPlayer.getBounds().getMaxX(), rightPlayer.getBounds().getMaxX())
                - Math.max(leftPlayer.getBounds().getX(), rightPlayer.getBounds().getX());
        if (overlap <= 0) {
            return;
        }
        double push = overlap / 2 + 0.1;
        if (leftPlayer.getX() <= rightPlayer.getX()) {
            leftPlayer.setPosition(leftPlayer.getX() - push, leftPlayer.getY());
            rightPlayer.setPosition(rightPlayer.getX() + push, rightPlayer.getY());
        } else {
            leftPlayer.setPosition(leftPlayer.getX() + push, leftPlayer.getY());
            rightPlayer.setPosition(rightPlayer.getX() - push, rightPlayer.getY());
        }
        leftPlayer.clampHorizontally(field);
        rightPlayer.clampHorizontally(field);
    }

    /**
     * Empuja el balon cuando el cuerpo del jugador lo toca.
     */
    private void resolveBallTouch(Player player) {
        if (!player.touchesBall(ball)) {
            return;
        }

        if (ball.isIllusionActive() && ball.getPowerOwner() != null
                && ball.getPowerOwner() != player) {
            return;
        }

        boolean powerTriggered = false;
        Power activePower = ball.getActivePower();
        if (activePower != null && ball.getPowerOwner() != null
                && ball.getPowerOwner() != player) {
            powerTriggered = activePower.reactsToOpponentTouch();
            activePower.onBallTouch(ball, ball.getPowerOwner(), player);
            ball.clearActivePower();
            player.clampHorizontally(field);
        }

        if (player.canAct() && player.isPowerArmed()) {
            activatePower(player);
        }

        if (powerTriggered) {
            return;
        }

        double dx = ball.getX() - player.getX();
        double direction;
        if (Math.abs(dx) < 0.001) {
            direction = player.getAttackDirection();
        } else {
            if (dx > 0) {
                direction = 1;
            } else {
                direction = -1;
            }
        }
        double minDistance = player.getWidth() / 2 + ball.getRadius();
        if (ball.getVelocityX() * direction <= 0) {
            ball.setPosition(player.getX() + direction * minDistance, ball.getY());
        }
        double touchSpeed = player.getTouchSpeed();
        if (ball.getVelocityX() * direction < touchSpeed) {
            ball.setVelocity(direction * touchSpeed,
                    Math.min(ball.getVelocityY(), -Player.TOUCH_LIFT));
        }
    }

    /**
     * Devuelve los balones que le quedan a un lado en supervivencia.
     */
    public int getRemainingBalls(Side side) {
        if (side == Side.LEFT) {
            return remainingBallsLeft;
        }
        if (side == Side.RIGHT) {
            return remainingBallsRight;
        }
        throw new IllegalArgumentException("El lado no puede ser null");
    }

    public MatchConfig getConfig() {
        return config;
    }

    public Field getField() {
        return field;
    }

    public Ball getBall() {
        return ball;
    }

    public Score getScore() {
        return score;
    }

    public Player getLeftPlayer() {
        return leftPlayer;
    }

    public Player getRightPlayer() {
        return rightPlayer;
    }

    /**
     * Devuelve el jugador del lado indicado.
     */
    public Player getPlayer(Side side) {
        if (side == Side.LEFT) {
            return leftPlayer;
        }
        if (side == Side.RIGHT) {
            return rightPlayer;
        }
        throw new IllegalArgumentException("El lado no puede ser null");
    }

    public MatchState getState() {
        return state;
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public double getGoalPauseRemaining() {
        return goalPauseRemaining;
    }

    public boolean isGoldenGoal() {
        return goldenGoal;
    }

    public Winner getWinner() {
        return winner;
    }

    public boolean isFinished() {
        return state == MatchState.FINISHED;
    }
}
