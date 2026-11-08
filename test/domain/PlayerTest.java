package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerTest {

    private Field field;
    private Player player;
    private Ball ball;

    @BeforeEach
    public void setUp() throws POOBSoccerException {
        field = new Field();
        GameCharacter character = new GameCharacter(
                "Test", new Country("Test", new FreezePower()), 3, 3, 3);
        player = new Player(character, true,
                field.getLeftPlayerStartX(), field.getPlayerStartY());
        ball = new Ball(0, 0, Field.BALL_RADIUS);
    }

    @Test
    public void shouldMoveUsingSpeedStat() {
        double startX = player.getX();
        assertTrue(player.move(1, 1, field));
        assertEquals(startX + player.getMoveSpeed(), player.getX(), 0.001);
    }

    @Test
    public void shouldMoveLeft() {
        double startX = player.getX();
        assertTrue(player.move(-1, 0.5, field));
        assertEquals(startX - player.getMoveSpeed() * 0.5, player.getX(), 0.001);
    }

    @Test
    public void shouldInvertMovementWhenConfused() {
        double startX = player.getX();
        player.confuse(5);
        assertTrue(player.move(1, 0.2, field));
        assertEquals(startX - player.getMoveSpeed() * 0.2, player.getX(), 0.001);
    }

    @Test
    public void shouldNotMoveWhenStunnedOrFrozen() {
        double startX = player.getX();
        player.stun();
        assertFalse(player.move(1, 1, field));
        assertEquals(startX, player.getX(), 0.001);

        player.freeze(5);
        assertFalse(player.move(-1, 1, field));
        assertEquals(startX, player.getX(), 0.001);
    }

    @Test
    public void shouldClampMovementToGoalLines() {
        for (int i = 0; i < 10; i++) {
            player.move(-1, 1, field);
        }
        double expectedMin = field.getLeftGoalLineX() + player.getWidth() / 2;
        assertEquals(expectedMin, player.getX(), 0.001);
    }

    @Test
    public void shouldJumpOnlyFromGround() {
        assertTrue(player.jump(field));
        assertTrue(player.getVelocityY() < 0);

        player.update(0.1, field);
        assertTrue(player.getY() < field.getPlayerStartY());
        assertFalse(player.isOnGround(field));
        assertFalse(player.jump(field));
    }

    @Test
    public void shouldLandOnGround() {
        player.setPosition(player.getX(), 400);
        player.update(1, field);
        assertEquals(field.getGroundY() - player.getHeight() / 2, player.getY(), 0.001);
        assertEquals(0, player.getVelocityY(), 0.001);
        assertTrue(player.isOnGround(field));
    }

    @Test
    public void shouldKickBallTowardRivalGoal() {
        ball.setPosition(player.getX() + 50, player.getY());
        assertTrue(player.kick(ball));
        assertEquals(player.getKickSpeed(), ball.getVelocityX(), 0.001);
        assertTrue(ball.getVelocityY() < 0);
        assertFalse(player.canKick());
        assertFalse(player.kick(ball));
    }

    @Test
    public void shouldNotKickBallOutOfReach() {
        ball.setPosition(player.getX() + 500, player.getY());
        assertFalse(player.kick(ball));
    }

    @Test
    public void shouldNotKickWhileStunned() {
        ball.setPosition(player.getX() + 50, player.getY());
        player.stun();
        assertFalse(player.kick(ball));
    }

    @Test
    public void shouldFillPowerBarOverTime() {
        for (int i = 0; i < 100; i++) {
            player.update(0.1, field);
        }
        assertTrue(player.isPowerReady());
        assertEquals(1, player.getPowerCharge(), 0.001);
    }

    @Test
    public void shouldArmPowerOnlyWhenReady() {
        assertFalse(player.armPower());
        fillPower();
        assertTrue(player.armPower());
        assertTrue(player.isPowerArmed());
    }

    @Test
    public void shouldNotArmPowerWhileStunned() {
        fillPower();
        player.stun();
        assertFalse(player.armPower());
    }

    @Test
    public void shouldConsumeArmedPowerAndEmptyBar() {
        fillPower();
        player.armPower();
        assertTrue(player.consumePower());
        assertEquals(0, player.getPowerCharge(), 0.001);
        assertFalse(player.isPowerArmed());
        assertFalse(player.consumePower());
    }

    @Test
    public void shouldKeepPowerChargeAfterResetButDisarm() {
        fillPower();
        player.armPower();
        player.reset(field.getLeftPlayerStartX(), field.getPlayerStartY());
        assertTrue(player.isPowerReady());
        assertFalse(player.isPowerArmed());
    }

    @Test
    public void shouldResetPowerCompletely() {
        fillPower();
        player.armPower();
        player.resetPower();
        assertFalse(player.isPowerReady());
        assertFalse(player.isPowerArmed());
        assertEquals(0, player.getPowerCharge(), 0.001);
    }

    @Test
    public void shouldShrinkCollisionBounds() {
        assertEquals(Player.WIDTH, player.getWidth(), 0.001);
        assertEquals(Player.HEIGHT, player.getHeight(), 0.001);
        player.shrink(5);
        assertEquals(Player.WIDTH * Player.SHRUNK_SCALE, player.getWidth(), 0.001);
        assertEquals(Player.HEIGHT * Player.SHRUNK_SCALE, player.getHeight(), 0.001);
    }

    @Test
    public void shouldDetectBallTouchAndPlayerIntersection() throws POOBSoccerException {
        ball.setPosition(player.getX() + 50, player.getY());
        assertTrue(player.touchesBall(ball));

        Player other = new Player(player.getCharacter(), false,
                player.getX() + 20, player.getY());
        assertTrue(player.intersects(other));
    }

    private void fillPower() {
        for (int i = 0; i < 100; i++) {
            player.update(0.1, field);
        }
    }
}
