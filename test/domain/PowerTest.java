package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PowerTest {

    private Ball ball;
    private Player left;
    private Player right;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        GameCharacter character = new GameCharacter("Canada", new Country("Canada", new FreezePower()), 3, 3, 3);
        ball = new Ball(400, 300, 10);
        left = new Player(character, true, 200, 400);
        right = new Player(character, false, 600, 400);
    }

    @Test
    public void shouldFreezeOpponent() {
        new FreezePower().activate(ball, left, right);
        assertTrue(right.isFrozen());
        assertFalse(right.canAct());
        assertFalse(left.isFrozen());
    }

    @Test
    public void shouldUnfreezeAfterDuration() {
        FreezePower freeze = new FreezePower();
        freeze.activate(ball, left, right);
        right.updateEffects(freeze.getDuration());
        assertFalse(right.isFrozen());
        assertTrue(right.canAct());
    }

    @Test
    public void shouldShrinkOpponent() {
        new ShrinkPower().activate(ball, left, right);
        assertTrue(right.isShrunk());
        assertTrue(right.canAct());
    }

    @Test
    public void shouldConfuseOpponent() {
        new ConfusionPower().activate(ball, right, left);
        assertTrue(left.isConfused());
        assertFalse(right.isConfused());
    }

    @Test
    public void shouldShootPushTowardOpponentGoal() {
        new PushPower().activate(ball, left, right);
        assertEquals(PushPower.SHOT_SPEED, ball.getVelocityX());
        assertEquals(0, ball.getVelocityY());
        assertTrue(ball.isPushCharged());

        new PushPower().activate(ball, right, left);
        assertEquals(-PushPower.SHOT_SPEED, ball.getVelocityX());
    }

    @Test
    public void shouldPushBackAndStunOpponentWhoTouchesBall() {
        PushPower push = new PushPower();
        push.activate(ball, left, right);
        push.hit(ball, right);
        assertEquals(600 + PushPower.PUSH_DISTANCE, right.getX());
        assertTrue(right.isStunned());
        assertFalse(ball.isPushCharged());
    }

    @Test
    public void shouldNotPushWhenBallIsNotCharged() {
        new PushPower().hit(ball, right);
        assertEquals(600, right.getX());
        assertFalse(right.isStunned());
    }

    @Test
    public void shouldCreateIllusionWithSevenDecoys() {
        IllusionPower illusion = new IllusionPower();
        illusion.activate(ball, left, right);
        assertTrue(ball.isIllusionActive());
        assertEquals(IllusionPower.DECOYS, ball.getDecoyCount());
        assertEquals(IllusionPower.SHOT_SPEED, ball.getVelocityX());
    }

    @Test
    public void shouldEndIllusionAfterDuration() {
        IllusionPower illusion = new IllusionPower();
        illusion.activate(ball, left, right);
        ball.updateEffects(illusion.getDuration());
        assertFalse(ball.isIllusionActive());
        assertEquals(0, ball.getDecoyCount());
    }

    @Test
    public void shouldStunForThreeSeconds() {
        right.stun();
        right.updateEffects(2.5);
        assertTrue(right.isStunned());
        right.updateEffects(0.5);
        assertFalse(right.isStunned());
    }
}
