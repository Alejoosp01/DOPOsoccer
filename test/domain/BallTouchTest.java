package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de como reacciona el balon al tocar el cuerpo de los jugadores,
 * en especial que nunca se quede pegado entre los dos.
 */
public class BallTouchTest {

    private Match match;
    private Player left;
    private Player right;
    private Ball ball;
    private double standY;
    private double ballOnGroundY;

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

    @Test
    public void shouldPopBallUpWhenSqueezedBetweenPlayers() {
        left.setPosition(600, standY);
        right.setPosition(700, standY);
        ball.setPosition(650, ballOnGroundY);
        ball.setVelocity(0, 0);

        match.update(0.01);

        assertTrue(ball.getVelocityY() < 0);
        assertTrue(ball.getY() + ball.getRadius() <= left.getBounds().getY() + 1);
    }

    @Test
    public void shouldNotStayStuckWhenBothPlayersPushTheBall() {
        left.setPosition(560, standY);
        right.setPosition(740, standY);
        ball.setPosition(650, ballOnGroundY);
        ball.setVelocity(0, 0);

        boolean wentOverHeads = false;
        double step = 1.0 / 120.0;
        for (int i = 0; i < 240; i++) {
            match.move(Side.LEFT, 1, step);
            match.move(Side.RIGHT, -1, step);
            match.update(step);
            if (ball.getY() + ball.getRadius() < left.getBounds().getY()) {
                wentOverHeads = true;
            }
        }

        assertTrue(wentOverHeads);
    }

    @Test
    public void shouldHeadBallUpAndForward() {
        left.setPosition(400, standY);
        double top = left.getBounds().getY();
        ball.setPosition(400, top - ball.getRadius() + 3);
        ball.setVelocity(0, 300);

        match.update(0.005);

        assertTrue(ball.getVelocityY() < 0);
        assertTrue(ball.getVelocityX() > 0);
        assertTrue(ball.getY() + ball.getRadius() <= top + 0.5);
    }

    @Test
    public void shouldPushBallAwayFromTheSide() {
        left.setPosition(400, standY);
        ball.setPosition(left.getBounds().getMaxX() + ball.getRadius() - 4, ballOnGroundY);
        ball.setVelocity(0, 0);

        match.update(0.005);

        assertTrue(ball.getVelocityX() >= Player.TOUCH_SPEED - 0.001);
        assertTrue(ball.getX() - ball.getRadius() >= left.getBounds().getMaxX());
    }

    @Test
    public void shouldBounceFastBallBackFromPlayer() {
        left.setPosition(400, standY);
        ball.setPosition(left.getBounds().getMaxX() + ball.getRadius() - 2, 560);
        ball.setVelocity(-800, 0);

        match.update(0.002);

        assertTrue(ball.getVelocityX() > Player.TOUCH_SPEED);
    }

    @Test
    public void shouldStillHitRivalWithPowerShotWhenSqueezed() {
        left.setPosition(600, standY);
        right.setPosition(700, standY);
        ball.setPosition(650, ballOnGroundY);
        ball.launchShot(new LightningShot(), left);
        double lifeBefore = right.getLife();

        match.update(0.01);

        assertTrue(right.getLife() < lifeBefore);
        assertFalse(ball.hasActiveShot());
    }
}
