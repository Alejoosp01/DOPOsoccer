package domain;

import java.io.Serializable;

/**
 * Motor de un partido.
 * Une cancha, balon, jugadores, marcador, tiempo y estado.
 * No depende de Swing: toda la simulacion se avanza con update(dt).
 */
public class Match implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double GOAL_PAUSE_SECONDS = 1.0;
    public static final double MAX_STEP = 1.0 / 120.0;
    public static final double KICK_DAMAGE = 10;
    public static final double SQUEEZE_POP_SPEED = 650;
    public static final double SQUEEZE_SIDE_SPEED = 260;
    public static final double SQUEEZE_MARGIN = 2;
    public static final double HEADER_FORWARD_SPEED = 180;
    /** Rapidez de alejamiento desde la cual un toque lateral es conduccion y ya no levanta el balon. */
    public static final double DRIBBLE_SPEED = Player.TOUCH_SPEED / 2;

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
    private double nextPopSide;

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
        this.nextPopSide = 1;
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
            resolveBallTouches();
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
     * La patada siempre inicia la espera (y la animacion), aunque no toque nada.
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
        if (player.hasAura() && player.reachesBallWithKick(ball)) {
            firePowerShot(player);
            touchedBall = true;
        } else {
            touchedBall = player.kick(ball);
        }
        if (hitsRival) {
            opponent.receiveDamage(KICK_DAMAGE * player.getKickMultiplier());
        }
        // La patada se anima aunque sea al aire.
        player.startKickCooldown();
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
        ball.impactShot();
    }

    /**
     * Resuelve los toques de balon de los dos jugadores. Si el balon esta
     * apretado entre los dos (no cabe en el espacio que dejan), lo hace saltar
     * por encima de ellos; asi no queda rebotando de uno a otro sin salir.
     */
    private void resolveBallTouches() {
        boolean touchesSomeone = leftPlayer.touchesBall(ball) || rightPlayer.touchesBall(ball);
        if (touchesSomeone && isBallSqueezed()) {
            resolveSqueezedShot();
            popBallUp();
            return;
        }
        resolveBallTouch(leftPlayer);
        resolveBallTouch(rightPlayer);
    }

    /**
     * Si el balon apretado lleva un tiro de poder, primero mira si toca al
     * rival del dueño (daño y efecto); solo si no, lo apaga el dueño.
     */
    private void resolveSqueezedShot() {
        if (!ball.hasActiveShot()) {
            return;
        }
        Player owner = ball.getShotOwner();
        Player rival = leftPlayer;
        if (owner == leftPlayer) {
            rival = rightPlayer;
        }
        if (rival.touchesBall(ball)) {
            resolvePowerShotHit(rival);
        } else if (owner.touchesBall(ball)) {
            resolvePowerShotHit(owner);
        }
    }

    /**
     * Devuelve si el balon esta entre los dos jugadores, a la altura de ambos
     * cuerpos, y el espacio entre ellos es menor que el diametro del balon.
     */
    private boolean isBallSqueezed() {
        Hitbox first = leftPlayer.getBounds();
        Hitbox second = rightPlayer.getBounds();
        if (leftPlayer.getX() > rightPlayer.getX()) {
            first = rightPlayer.getBounds();
            second = leftPlayer.getBounds();
        }
        double radius = ball.getRadius();
        double gap = second.getX() - first.getMaxX();
        if (gap >= 2 * radius + SQUEEZE_MARGIN) {
            return false;
        }
        boolean between = ball.getX() >= first.getMaxX() - radius
                && ball.getX() <= second.getX() + radius;
        return between && overlapsVertically(first) && overlapsVertically(second);
    }

    /**
     * Devuelve si el balon esta a la altura del cuerpo indicado.
     */
    private boolean overlapsVertically(Hitbox body) {
        return ball.getY() + ball.getRadius() > body.getY()
                && ball.getY() - ball.getRadius() < body.getMaxY();
    }

    /**
     * Saca el balon por encima de la cabeza mas alta de los dos jugadores y lo
     * lanza hacia arriba, inclinado hacia el lado donde haya mas espacio.
     */
    private void popBallUp() {
        double leftTop = leftPlayer.getBounds().getY();
        double rightTop = rightPlayer.getBounds().getY();
        double top = Math.min(leftTop, rightTop);
        double newY = Math.max(ball.getRadius(), top - ball.getRadius() - 1);
        double middle = (leftPlayer.getX() + rightPlayer.getX()) / 2;
        double direction;
        if (ball.getX() > middle + 1) {
            direction = 1;
        } else if (ball.getX() < middle - 1) {
            direction = -1;
        } else {
            direction = nextPopSide;
            nextPopSide = -nextPopSide;
        }
        ball.setPosition(ball.getX(), newY);
        ball.setVelocity(direction * SQUEEZE_SIDE_SPEED, -SQUEEZE_POP_SPEED);
    }

    /**
     * Rebota el balon contra el cuerpo del jugador como un circulo contra un
     * rectangulo: lo saca por el lado mas cercano (costado, cabeza o pies) y le
     * da al menos TOUCH_SPEED hacia afuera. Si cae sobre la cabeza hace un
     * cabezazo y lo empuja hacia adelante para que no se quede quieto encima.
     */
    private void pushBall(Player player) {
        Hitbox body = player.getBounds();
        double radius = ball.getRadius();
        double closestX = clamp(ball.getX(), body.getX(), body.getMaxX());
        double closestY = clamp(ball.getY(), body.getY(), body.getMaxY());
        double normalX = ball.getX() - closestX;
        double normalY = ball.getY() - closestY;
        double distance = Math.sqrt(normalX * normalX + normalY * normalY);
        if (distance > radius) {
            return;
        }
        if (distance < 0.0001) {
            double dx = ball.getX() - player.getX();
            if (ball.getY() < body.getY() + radius) {
                normalX = 0;
                normalY = -1;
                closestX = ball.getX();
                closestY = body.getY();
            } else {
                normalX = sideOf(dx, player.getAttackDirection());
                normalY = 0;
                if (normalX > 0) {
                    closestX = body.getMaxX();
                } else {
                    closestX = body.getX();
                }
                closestY = ball.getY();
            }
        } else {
            normalX = normalX / distance;
            normalY = normalY / distance;
        }
        ball.setPosition(closestX + normalX * (radius + 0.5), closestY + normalY * (radius + 0.5));

        double relativeX = ball.getVelocityX() - player.getVelocityX();
        double relativeY = ball.getVelocityY() - player.getVelocityY();
        double along = relativeX * normalX + relativeY * normalY;
        // Rapidez con que el balon ya se alejaba del jugador antes del toque (sin restar al jugador).
        double leavingSpeed = ball.getVelocityX() * normalX + ball.getVelocityY() * normalY;
        if (along < Player.TOUCH_SPEED) {
            double bounce = Math.max(-along * Ball.RESTITUTION, Player.TOUCH_SPEED);
            double change = bounce - along;
            relativeX = relativeX + change * normalX;
            relativeY = relativeY + change * normalY;
        }
        double velocityX = relativeX + player.getVelocityX();
        double velocityY = relativeY + player.getVelocityY();

        boolean header = normalY < -0.7;
        if (header && Math.abs(velocityX) < HEADER_FORWARD_SPEED) {
            double side = sideOf(ball.getX() - player.getX(), player.getAttackDirection());
            velocityX = side * HEADER_FORWARD_SPEED;
        }
        // Si el balon ya iba conducido (se alejaba rapido) no se levanta, solo se acompana.
        if (Math.abs(normalX) > 0.7 && leavingSpeed < DRIBBLE_SPEED) {
            velocityY = Math.min(velocityY, -Player.TOUCH_LIFT);
        }
        ball.setVelocity(velocityX, velocityY);
    }

    /**
     * Devuelve 1 o -1 segun el signo de dx; si es casi 0 usa la direccion indicada.
     */
    private static double sideOf(double dx, int fallback) {
        if (dx > 0.001) {
            return 1;
        }
        if (dx < -0.001) {
            return -1;
        }
        return fallback;
    }

    /**
     * Devuelve value limitado al rango [min, max].
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
