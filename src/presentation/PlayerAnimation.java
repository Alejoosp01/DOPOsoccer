package presentation;

import java.awt.image.BufferedImage;

import domain.Ball;
import domain.Match;
import domain.Player;
import domain.Side;
import domain.Winner;

/**
 * Decide que animacion muestra un jugador segun su estado en el partido y
 * cuanto tiempo lleva en ella. Hay un objeto por jugador. Solo lee el dominio:
 * no cambia nada del partido.
 *
 * Orden de prioridad:
 *  1. partido terminado: el ganador celebra y el perdedor muestra derrota;
 *  2. aturdido o con un efecto que no lo deja moverse: derrota;
 *  3. acaba de activar el aura: poder (se ve completa);
 *  4. acaba de patear: patada (se ve completa);
 *  5. en el aire: cabezazo si el balon esta junto a la cabeza, si no salto;
 *  6. caminando: carrera;
 *  7. quieto.
 */
public class PlayerAnimation {

    public static final double IDLE_FPS = 6;
    public static final double RUN_FPS = 12;
    public static final double JUMP_FPS = 10;
    public static final double KICK_FPS = 20;
    public static final double HEADER_FPS = 14;
    public static final double POWER_FPS = 12;
    public static final double CELEBRATION_FPS = 8;
    public static final double DEFEAT_FPS = 6;

    /** Tiempo minimo que se muestra la patada aunque la espera de la patada termine antes. */
    public static final double KICK_SECONDS = 0.25;
    /** Tiempo que se muestra la animacion de poder al activar el aura. */
    public static final double POWER_SECONDS = 0.5;
    /** Movimiento minimo en x (pixeles por cuadro) para considerar que esta caminando. */
    public static final double RUN_THRESHOLD = 0.5;
    /** Distancia extra alrededor de la cabeza para considerar que el balon esta para cabecear. */
    public static final double HEADER_MARGIN = 30;

    private final Side side;
    private String action;
    private double time;
    private double lockTime;
    private boolean hadAura;
    private boolean hasLastX;
    private double lastX;

    public PlayerAnimation(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null");
        }
        this.side = side;
        this.action = SpriteLoader.IDLE;
        this.time = 0;
        this.lockTime = 0;
        this.hadAura = false;
        this.hasLastX = false;
        this.lastX = 0;
    }

    /**
     * Avanza la animacion dt segundos mirando el estado actual del partido.
     */
    public void update(Match match, double dt) {
        if (match == null || dt < 0) {
            return;
        }
        Player player = match.getPlayer(side);
        String next = chooseAction(match, player);
        if (next.equals(action)) {
            time = time + dt;
        } else {
            action = next;
            time = 0;
        }
        lockTime = Math.max(0, lockTime - dt);
        hadAura = player.hasAura();
        lastX = player.getX();
        hasLastX = true;
    }

    /**
     * Elige la accion siguiendo el orden de prioridad de la clase.
     */
    private String chooseAction(Match match, Player player) {
        if (match.isFinished()) {
            lockTime = 0;
            return finalAction(match.getWinner());
        }
        if (player.isStunned() || !player.canMove()) {
            lockTime = 0;
            return SpriteLoader.DEFEAT;
        }
        if (player.hasAura() && !hadAura) {
            lockTime = POWER_SECONDS;
            return SpriteLoader.POWER;
        }
        if (lockTime > 0 && SpriteLoader.POWER.equals(action)) {
            return SpriteLoader.POWER;
        }
        if (player.isKicking() && !SpriteLoader.KICK.equals(action)) {
            lockTime = KICK_SECONDS;
            return SpriteLoader.KICK;
        }
        if (lockTime > 0 && SpriteLoader.KICK.equals(action)) {
            return SpriteLoader.KICK;
        }
        if (!player.isOnGround(match.getField())) {
            if (isBallAtHead(player, match.getBall())) {
                return SpriteLoader.HEADER;
            }
            if (SpriteLoader.HEADER.equals(action)) {
                return SpriteLoader.HEADER;
            }
            return SpriteLoader.JUMP;
        }
        if (hasLastX && Math.abs(player.getX() - lastX) > RUN_THRESHOLD) {
            return SpriteLoader.RUN;
        }
        return SpriteLoader.IDLE;
    }

    /**
     * Devuelve celebracion si este lado gano, derrota si perdio y quieto si no hubo ganador.
     */
    private String finalAction(Winner winner) {
        if (winner == Winner.NONE) {
            return SpriteLoader.IDLE;
        }
        if (winner == Winner.fromSide(side)) {
            return SpriteLoader.CELEBRATION;
        }
        return SpriteLoader.DEFEAT;
    }

    /**
     * Devuelve si el balon esta junto a la parte de arriba del jugador.
     */
    private boolean isBallAtHead(Player player, Ball ball) {
        double headX = player.getX();
        double headY = player.getY() - player.getHeight() / 2;
        double dx = ball.getX() - headX;
        double dy = ball.getY() - headY;
        double reach = ball.getRadius() + HEADER_MARGIN;
        return dx * dx + dy * dy <= reach * reach;
    }

    /**
     * Devuelve el cuadro que toca dibujar de la animacion actual, o null si no hay imagenes.
     */
    public BufferedImage currentFrame(Player player) {
        if (player == null) {
            return null;
        }
        BufferedImage[] frames = SpriteLoader.characterFrames(player.getCharacter(), action);
        if (isLooping()) {
            return SpriteLoader.frameAt(frames, time, getFramesPerSecond());
        }
        return SpriteLoader.frameOnce(frames, time, getFramesPerSecond());
    }

    /**
     * Devuelve si la animacion actual se repite (quieto, carrera, celebracion y derrota).
     */
    public boolean isLooping() {
        return SpriteLoader.IDLE.equals(action) || SpriteLoader.RUN.equals(action)
                || SpriteLoader.CELEBRATION.equals(action) || SpriteLoader.DEFEAT.equals(action);
    }

    /**
     * Devuelve la velocidad en cuadros por segundo de la animacion actual.
     */
    public double getFramesPerSecond() {
        if (SpriteLoader.RUN.equals(action)) {
            return RUN_FPS;
        }
        if (SpriteLoader.JUMP.equals(action)) {
            return JUMP_FPS;
        }
        if (SpriteLoader.KICK.equals(action)) {
            return KICK_FPS;
        }
        if (SpriteLoader.HEADER.equals(action)) {
            return HEADER_FPS;
        }
        if (SpriteLoader.POWER.equals(action)) {
            return POWER_FPS;
        }
        if (SpriteLoader.CELEBRATION.equals(action)) {
            return CELEBRATION_FPS;
        }
        if (SpriteLoader.DEFEAT.equals(action)) {
            return DEFEAT_FPS;
        }
        return IDLE_FPS;
    }

    /**
     * Devuelve si el jugador debe dibujarse volteado (el de la derecha mira a la izquierda).
     */
    public boolean isFlipped() {
        return side == Side.RIGHT;
    }

    /**
     * Devuelve la accion actual (una de las constantes de SpriteLoader).
     */
    public String getAction() {
        return action;
    }

    /**
     * Devuelve cuantos segundos lleva en la accion actual.
     */
    public double getTime() {
        return time;
    }

    /**
     * Devuelve el lado del jugador que anima.
     */
    public Side getSide() {
        return side;
    }
}
