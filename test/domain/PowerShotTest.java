package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PowerShotTest {

    private Field field;
    private Player left;
    private Player right;
    private Ball ball;

    @BeforeEach
    public void setUp() {
        field = new Field();
        left = TestData.player(true);
        right = TestData.player(false);
        left.startMatch(400, field.getPlayerStartY());
        right.startMatch(900, field.getPlayerStartY());
        ball = new Ball(440, 560, Field.BALL_RADIUS);
    }

    @Test
    public void shouldHaveTheValuesOfAnnex1() {
        assertEquals(40, new BlueAuraShot().getDamage());
        assertEquals(1.5, new BlueAuraShot().getEffectDuration(), 0.001);
        assertEquals(60, new LightningShot().getDamage());
        assertEquals(3, new LightningShot().getEffectDuration(), 0.001);
        assertEquals(40, new TornadoShot().getDamage());
        assertEquals(3, new TornadoShot().getEffectDuration(), 0.001);
        assertEquals(30, new IllusionShot().getDamage());
        assertEquals(2.5, new IllusionShot().getEffectDuration(), 0.001);
    }

    @Test
    public void shouldShootStraightTowardRivalGoal() {
        new BlueAuraShot().launch(ball, left);
        assertEquals(BlueAuraShot.SHOT_SPEED, ball.getVelocityX(), 0.001);
        assertEquals(0, ball.getVelocityY(), 0.001);
        assertTrue(ball.getX() > left.getX() + Player.WIDTH / 2);

        new LightningShot().launch(ball, right);
        assertEquals(-LightningShot.SHOT_SPEED, ball.getVelocityX(), 0.001);
    }

    @Test
    public void shouldSendTornadoAlongTheGround() {
        TornadoShot tornado = new TornadoShot();
        tornado.launch(ball, left);
        assertEquals(field.getGroundY() - Field.BALL_RADIUS, ball.getY(), 0.001);
        assertTrue(ball.getVelocityX() > 0);
        assertTrue(tornado.isAffectedByGravity());
        assertFalse(new LightningShot().isAffectedByGravity());
    }

    @Test
    public void shouldCreateSevenDecoysWithIllusion() {
        new IllusionShot().launch(ball, left);
        assertEquals(IllusionShot.DECOYS, ball.getDecoyCount());
    }

    @Test
    public void shouldDamageRivalWithKickMultiplier() {
        new LightningShot().hit(right, 1.4);
        assertEquals(100 - 84, right.getLife(), 0.001);
        assertTrue(right.getEffect() instanceof ElectrocutedEffect);
    }

    @Test
    public void shouldCreateTheRightEffect() {
        assertTrue(new BlueAuraShot().createEffect() instanceof PushEffect);
        assertTrue(new LightningShot().createEffect() instanceof ElectrocutedEffect);
        assertTrue(new TornadoShot().createEffect() instanceof UnconsciousEffect);
        assertTrue(new IllusionShot().createEffect() instanceof ConfusionEffect);
    }
}
