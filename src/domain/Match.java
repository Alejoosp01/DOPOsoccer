package domain;

/**
 * Motor de un partido.
 * Une cancha, balon, jugadores, marcador, tiempo y estado.
 * No depende de Swing: toda la simulacion se avanza con update(dt).
 */
public class Match {

    public static final double GOAL_PAUSE_SECONDS = 1.0;
    public static final double MAX_STEP = 1.0 / 120.0;
    public static final double KICK_DAMAGE = 10;

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
    private boolean suddenDeath;
    private boolean endedEarly;
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
        this.timeLeft = config.getDurationSeconds();
        this.goalPauseRemaining = 0;
        this.suddenDeath = false;
        this.endedEarly = false;
        this.winner = Winner.NONE;
        resetPositions();
    }

    /**
     * Pone el partido en juego desde cero: marcador 0-0, barras vacias y
     * vida de cada jugador en su maximo.
     */
    public void start() {
        score.reset();
        winner = Winner.NONE;
        suddenDeath = false;
        endedEarly = false;
        stateBeforePause = null;
        timeLeft = config.getDurationSeconds();
        goalPauseRemaining = 0;
        ball.reset(field);
        leftPlayer.startMatch(field.getLeftPlayerStartX(), field.getPlayerStartY());
        rightPlayer.startMatch(field.getRightPlayerStartX(), field.getPlayerStartY());
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
            if (config.isTimed() && !suddenDeath) {
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
     * Termina el partido en este momento: gana quien vaya ganando; si van
     * empatados no hay ganador (decision pendiente de confirmar con el monitor).
     */
    public void finishNow() {
        if (state == MatchState.READY || state == MatchState.FINISHED) {
            return;
        }
        winner = score.leader();
        endedEarly = true;
        state = MatchState.FINISHED;
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
     * Si el pie toca el balon, lo impulsa (o lanza el tiro de poder si tiene aura).
     * Si el pie alcanza al rival, le hace 10 x multiplicadorKick de daño.
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
        boolean touchedBall;
        if (player.hasAura() && player.touchesBall(ball)) {
            firePowerShot(player);
            player.startKickCooldown();
            touchedBall = true;
        } else {
            touchedBall = player.kick(ball);
        }
        if (hitsRival) {
            if (!touchedBall) {
                player.startKickCooldown();
            }
            opponent.receiveDamage(KICK_DAMAGE * player.getKickMultiplier());
        }
        return touchedBall || hitsRival;
    }

    /**
     * Activa el poder (aura) del jugador del lado indicado si su barra esta llena.
     */
    public boolean activatePower(Side side) {
        if (state != MatchState.PLAYING) {
            return false;
        }
        return getPlayer(side).activateAura();
    }

    /**
     * Gasta el aura del jugador y lanza su tiro de poder con el balon.
     */
    private void firePowerShot(Player player) {
        player.consumeAura();
        PowerShot shot = player.getCharacter().getPowerShot();
        shot.launch(ball, player);
        ball.launchShot(shot, player);
    }

    /**
     * Espera la pausa despues de un gol; luego reinicia posiciones o termina.
     */
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

    /**
     * Revisa si el balon entro completo en algun arco.
     */
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
     * Quien recibe el gol suma un cuarto de barra de poder.
     */
    private void registerGoal(Side scorer) {
        score.score(scorer);
        getPlayer(scorer.opposite()).getPowerBar().addQuarter();
        ball.clearShot();
        if (suddenDeath) {
            winner = Winner.fromSide(scorer);
        } else if (config.getMatchType() == MatchType.GOALS
                && score.getGoals(scorer) >= config.getGoalsTarget()) {
            winner = Winner.fromSide(scorer);
        }
        state = MatchState.GOAL;
        goalPauseRemaining = GOAL_PAUSE_SECONDS;
    }

    /**
     * Se acabo el tiempo: si hay empate empieza la muerte subita, si no gana el lider.
     */
    private void handleTimeExpired() {
        timeLeft = 0;
        if (score.isTie()) {
            suddenDeath = true;
            return;
        }
        winner = score.leader();
        state = MatchState.FINISHED;
    }

    /**
     * Devuelve el balon al centro y a los jugadores a su posicion inicial.
     */
    private void resetPositions() {
        ball.reset(field);
        leftPlayer.resetPosition(field.getLeftPlayerStartX(), field.getPlayerStartY());
        rightPlayer.resetPosition(field.getRightPlayerStartX(), field.getPlayerStartY());
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
     * Resuelve que pasa cuando el cuerpo de un jugador toca el balon:
     * primero si lo golpea un tiro de poder, luego si el jugador tiene aura
     * (lanza su tiro) y si no, el cuerpo empuja el balon.
     */
    private void resolveBallTouch(Player player) {
        if (!player.touchesBall(ball)) {
            return;
        }
        resolvePowerShotHit(player);
        if (player.hasAura() && !player.isStunned()) {
            firePowerShot(player);
            return;
        }
        pushBall(player);
    }

    /**
     * Si el balon lleva un tiro de poder y toca al rival de quien lo lanzo,
     * se aplican el daño y el efecto. Si lo toca su dueño, el tiro se apaga.
     */
    private void resolvePowerShotHit(Player player) {
        if (!ball.hasActiveShot()) {
            return;
        }
        Player owner = ball.getShotOwner();
        if (owner != player) {
            ball.getActiveShot().hit(player, owner.getKickMultiplier());
            player.clampHorizontally(field);
        }
        ball.clearShot();
    }

    /**
     * Empuja el balon alejandolo del centro del jugador.
     */
    private void pushBall(Player player) {
        double dx = ball.getX() - player.getX();
        double direction;
        if (Math.abs(dx) < 0.001) {
            direction = player.getAttackDirection();
        } else if (dx > 0) {
            direction = 1;
        } else {
            direction = -1;
        }
        double minDistance = player.getWidth() / 2 + ball.getRadius();
        if (ball.getVelocityX() * direction <= 0) {
            ball.setPosition(player.getX() + direction * minDistance, ball.getY());
        }
        if (ball.getVelocityX() * direction < Player.TOUCH_SPEED) {
            ball.setVelocity(direction * Player.TOUCH_SPEED,
                    Math.min(ball.getVelocityY(), -Player.TOUCH_LIFT));
        }
    }

    /**
     * Devuelve la configuracion del partido.
     */
    public MatchConfig getConfig() {
        return config;
    }

    /**
     * Devuelve la cancha.
     */
    public Field getField() {
        return field;
    }

    /**
     * Devuelve el balon.
     */
    public Ball getBall() {
        return ball;
    }

    /**
     * Devuelve el marcador.
     */
    public Score getScore() {
        return score;
    }

    /**
     * Devuelve el jugador izquierdo.
     */
    public Player getLeftPlayer() {
        return leftPlayer;
    }

    /**
     * Devuelve el jugador derecho.
     */
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

    /**
     * Devuelve el estado del partido.
     */
    public MatchState getState() {
        return state;
    }

    /**
     * Devuelve los segundos que quedan (partido por tiempo).
     */
    public double getTimeLeft() {
        return timeLeft;
    }

    /**
     * Devuelve los segundos que faltan para reanudar despues de un gol.
     */
    public double getGoalPauseRemaining() {
        return goalPauseRemaining;
    }

    /**
     * Devuelve si se esta jugando la muerte subita.
     */
    public boolean isSuddenDeath() {
        return suddenDeath;
    }

    /**
     * Devuelve si el partido se termino antes de tiempo.
     */
    public boolean wasEndedEarly() {
        return endedEarly;
    }

    /**
     * Devuelve el ganador, o NONE si aun no hay.
     */
    public Winner getWinner() {
        return winner;
    }

    /**
     * Devuelve si el partido ya termino.
     */
    public boolean isFinished() {
        return state == MatchState.FINISHED;
    }
}
