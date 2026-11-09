package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BallTest {

    private Field field;
    private Ball ball;

    @BeforeEach
    public void setUp() {
        field = new Field();
        ball = new Ball(640, 300, Field.BALL_RADIUS);
    }

    @Test
    public void shouldFallWithGravity() {
        ball.update(0.1, field);
        assertTrue(ball.getVelocityY() > 0);
        assertTrue(ball.getY() > 300);
    }

    @Test
    public void shouldRestOnGround() {
        ball.setPosition(640, field.getBallStartY());
        ball.update(0.01, field);
        assertEquals(field.getGroundY() - Field.BALL_RADIUS, ball.getY(), 0.001);
        assertEquals(0, ball.getVelocityY(), 0.001);
    }

    @Test
    public void shouldBounceOnGround() {
        ball.setPosition(640, 500);
        ball.setVelocity(0, 900);
        ball.update(0.2, field);
        assertEquals(field.getGroundY() - Field.BALL_RADIUS, ball.getY(), 0.001);
        assertTrue(ball.getVelocityY() < 0);
    }

    @Test
    public void shouldBounceOnWallAboveGoal() {
        ball.setPosition(200, 200);
        ball.setVelocity(-600, 0);
        ball.update(0.2, field);
        assertTrue(ball.getX() >= field.getLeftGoalLineX() + Field.BALL_RADIUS - 0.001);
        assertTrue(ball.getVelocityX() > 0);
    }

    @Test
    public void shouldEnterThroughGoalMouth() {
        ball.setPosition(200, 500);
        ball.setVelocity(-900, 0);
        ball.update(0.2, field);
        assertTrue(field.getLeftGoal().contains(ball));
    }

    @Test
    public void shouldNotEnterWallWhenBallIsAboveMouth() {
        ball.setPosition(200, 200);
        ball.setVelocity(-900, 0);
        ball.update(0.2, field);
        assertFalse(field.getLeftGoal().contains(ball));
        assertTrue(ball.getVelocityX() > 0);
    }

    @Test
    public void shouldResetToCenterWithoutEffects() {
        ball.setPosition(10, 10);
        ball.setVelocity(100, 100);
        ball.chargePush();
        ball.startIllusion(7, 1.5);

        ball.reset(field);

        assertEquals(field.getBallStartX(), ball.getX(), 0.001);
        assertEquals(field.getBallStartY(), ball.getY(), 0.001);
        assertEquals(0, ball.getVelocityX(), 0.001);
        assertEquals(0, ball.getVelocityY(), 0.001);
        assertFalse(ball.isPushCharged());
        assertFalse(ball.isIllusionActive());
        assertEquals(0, ball.getDecoyCount());
    }

    @Test
    public void shouldReturnBoundsAroundCenter() {
        ball.setPosition(640, 300);
        Hitbox bounds = ball.getBounds();
        assertEquals(640 - Field.BALL_RADIUS, bounds.getX(), 0.001);
        assertEquals(300 - Field.BALL_RADIUS, bounds.getY(), 0.001);
        assertEquals(Field.BALL_RADIUS * 2, bounds.getWidth(), 0.001);
        assertEquals(Field.BALL_RADIUS * 2, bounds.getHeight(), 0.001);
    }
}
