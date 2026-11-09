package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EffectTest {

    private Field field;
    private Player left;

    @BeforeEach
    public void setUp() {
        field = new Field();
        left = TestData.player(true);
        left.startMatch(400, field.getPlayerStartY());
    }

    @Test
    public void shouldPushRivalTowardItsOwnGoal() {
        left.applyEffect(new PushEffect(1.5));
        assertTrue(left.getVelocityX() < 0);
        assertFalse(left.canMove());
        assertFalse(left.canJump());
        assertTrue(left.canKick());
    }

    @Test
    public void shouldSlowAndForbidJumpWhenElectrocuted() {
        double normalSpeed = left.getMoveSpeed();
        left.applyEffect(new ElectrocutedEffect(3));
        assertEquals(normalSpeed * 0.5, left.getMoveSpeed(), 0.001);
        assertFalse(left.canJump());
        assertTrue(left.canMove());
        assertFalse(left.jump(field));
    }

    @Test
    public void shouldLaunchAndBlockEverythingWhenUnconscious() {
        left.applyEffect(new UnconsciousEffect(3));
        assertTrue(left.getVelocityY() < 0);
        assertFalse(left.canMove());
        assertFalse(left.canJump());
        assertFalse(left.canKick());
    }

    @Test
    public void shouldNotBlockAnythingWhenConfused() {
        left.applyEffect(new ConfusionEffect(2.5));
        assertTrue(left.canMove());
        assertTrue(left.canJump());
        assertTrue(left.canKick());
        assertEquals("Confusion", left.getEffect().getName());
    }

    @Test
    public void shouldReplaceThePreviousEffect() {
        left.applyEffect(new ElectrocutedEffect(3));
        left.applyEffect(new ConfusionEffect(2.5));
        assertTrue(left.getEffect() instanceof ConfusionEffect);
    }

    @Test
    public void shouldEndAfterItsDuration() {
        left.applyEffect(new ElectrocutedEffect(3));
        left.update(2.5, field);
        assertNotNull(left.getEffect());
        left.update(0.5, field);
        assertNull(left.getEffect());
        assertTrue(left.canJump());
    }
}
