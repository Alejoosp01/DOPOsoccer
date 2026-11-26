package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la conduccion del balon por el piso: el balon no debe subir solo.
 */
public class BallDribbleTest {

    private static final double STEP = 1.0 / 120.0;
    private static final double MAX_LIFT = 15;

    private Match match;
    private Player left;
    private Player right;
    private Ball ball;
    private double standY;
    private double ballOnGroundY;
    private double maxBallHeight;
    private double maxPlayerHeight;

    @BeforeEach
    public void setUp() {
        left = TestData.player(true);
        right = TestData.player(false);
        match = new Match(TestData.byGoals(GameMode.PVP, 5), left, right);
        match.start();
        ball = match.getBall();
        standY = match.getField().getPlayerStartY();
        ballOnGroundY = match.getField().getGroundY() - ball.getRadius();
    }

    /** Conduce el balon con el jugador indicado durante los segundos dados y mide las alturas maximas. */
    private void dribble(Side side, Player player, int dir, double seconds) {
        maxBallHeight = 0;
        maxPlayerHeight = 0;
        int steps = (int) Math.round(seconds / STEP);
        for (int i = 0; i < steps; i++) {
            match.move(side, dir, STEP);
            match.update(STEP);
            maxBallHeight = Math.max(maxBallHeight, ballOnGroundY - ball.getY());
            maxPlayerHeight = Math.max(maxPlayerHeight, standY - player.getY());
        }
    }

    /** Prepara el balon pegado al lado del jugador, a ras de piso y quieto. */
    private void ballBeside(Player player, int dir) {
        double offset = player.getBounds().getWidth() / 2 + ball.getRadius() - 2;
        ball.setPosition(player.getX() + dir * offset, ballOnGroundY);
        ball.setVelocity(0, 0);
    }

    /** El jugador derecho conduce el balon hacia la izquierda sin que este suba. */
    @Test
    public void rightPlayerDribblingLeftKeepsBallOnGround() {
        left.setPosition(200, standY);
        right.setPosition(900, standY);
        ballBeside(right, -1);
        dribble(Side.RIGHT, right, -1, 1.5);
        assertTrue(maxBallHeight <= MAX_LIFT, "balon subio " + maxBallHeight);
        assertTrue(maxPlayerHeight <= 1, "jugador subio " + maxPlayerHeight);
    }

    /** El jugador derecho conduce el balon hacia la derecha sin que este suba. */
    @Test
    public void rightPlayerDribblingRightKeepsBallOnGround() {
        left.setPosition(200, standY);
        right.setPosition(500, standY);
        ballBeside(right, 1);
        dribble(Side.RIGHT, right, 1, 1.5);
        assertTrue(maxBallHeight <= MAX_LIFT, "balon subio " + maxBallHeight);
        assertTrue(maxPlayerHeight <= 1, "jugador subio " + maxPlayerHeight);
    }

    /** El jugador izquierdo conduce el balon hacia la derecha sin que este suba. */
    @Test
    public void leftPlayerDribblingRightKeepsBallOnGround() {
        left.setPosition(400, standY);
        right.setPosition(1100, standY);
        ballBeside(left, 1);
        dribble(Side.LEFT, left, 1, 1.5);
        assertTrue(maxBallHeight <= MAX_LIFT, "balon subio " + maxBallHeight);
        assertTrue(maxPlayerHeight <= 1, "jugador subio " + maxPlayerHeight);
    }

    /** El jugador izquierdo conduce el balon hacia la izquierda sin que este suba. */
    @Test
    public void leftPlayerDribblingLeftKeepsBallOnGround() {
        left.setPosition(800, standY);
        right.setPosition(1100, standY);
        ballBeside(left, -1);
        dribble(Side.LEFT, left, -1, 1.5);
        assertTrue(maxBallHeight <= MAX_LIFT, "balon subio " + maxBallHeight);
        assertTrue(maxPlayerHeight <= 1, "jugador subio " + maxPlayerHeight);
    }

    /** Un toque aislado a un balon quieto en el piso sigue levantandolo. */
    @Test
    public void isolatedTouchOnStillBallStillLiftsIt() {
        left.setPosition(200, standY);
        right.setPosition(900, standY);
        ballBeside(right, -1);
        match.update(STEP);
        assertTrue(ball.getVelocityY() < 0, "vy = " + ball.getVelocityY());
    }
}
