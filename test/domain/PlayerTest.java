package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerTest {

    private Field field;
    private PlayerProfile profile;
    private Player player;

    @BeforeEach
    public void setUp() {
        field = new Field();
        profile = TestData.profile("Jugador 1");
        player = new Player(profile, TestData.character("Camerun", new LightningShot()), true);
        player.startMatch(field.getLeftPlayerStartX(), field.getPlayerStartY());
    }

    @Test
    public void shouldMoveAtBaseSpeedWithSpeedOne() {
        double startX = player.getX();
        assertTrue(player.move(1, 0.5, field));
        assertEquals(startX + Player.BASE_SPEED * 0.5, player.getX(), 0.001);
    }

    @Test
    public void shouldMoveTenPercentFasterPerSpeedPoint() throws DOPOSoccerException {
        profile.getStats().increase(StatType.SPEED);
        profile.getStats().increase(StatType.SPEED);
        assertEquals(Player.BASE_SPEED * 1.2, player.getMoveSpeed(), 0.001);
    }

    @Test
    public void shouldJumpTenPercentHigherPerJumpPoint() throws DOPOSoccerException {
        double baseHeight = speedToHeight(player.getJumpSpeed());
        profile.getStats().increase(StatType.JUMP);
        assertEquals(baseHeight * 1.1, speedToHeight(player.getJumpSpeed()), 0.001);
    }

    private double speedToHeight(double speed) {
        return speed * speed / (2 * Field.GRAVITY);
    }

    @Test
    public void shouldJumpOnlyFromGround() {
        assertTrue(player.jump(field));
        player.update(0.05, field);
        assertFalse(player.jump(field));
    }

    @Test
    public void shouldStartWithMaxLifeFromSurvival() throws DOPOSoccerException {
        profile.getStats().increase(StatType.SURVIVAL);
        player.startMatch(320, field.getPlayerStartY());
        assertEquals(110, player.getLife(), 0.001);
        assertEquals(110, player.getMaxLife(), 0.001);
    }

    @Test
    public void shouldLoseLifeButNeverBelowZero() {
        player.receiveDamage(30);
        assertEquals(70, player.getLife(), 0.001);
        player.receiveDamage(500);
        assertEquals(0, player.getLife(), 0.001);
    }

    @Test
    public void shouldBeStunnedForThreeSecondsWhenLifeReachesZero() {
        player.receiveDamage(100);
        assertTrue(player.isStunned());
        assertFalse(player.canMove());
        assertFalse(player.canJump());
        assertFalse(player.canKick());
        player.update(2.5, field);
        assertTrue(player.isStunned());
        player.update(0.5, field);
        assertFalse(player.isStunned());
        assertEquals(player.getMaxLife(), player.getLife(), 0.001);
    }

    @Test
    public void shouldIgnoreDamageWhileStunned() {
        player.receiveDamage(100);
        player.receiveDamage(20);
        assertEquals(0, player.getLife(), 0.001);
        assertTrue(player.isStunned());
    }

    @Test
    public void shouldActivateAuraOnlyWithFullBar() {
        assertFalse(player.activateAura());
        TestData.fillPowerBar(player);
        assertTrue(player.activateAura());
        assertTrue(player.hasAura());
        assertEquals(0, player.getPowerBar().getCharge(), 0.001);
    }

    @Test
    public void shouldNotActivateAnotherPowerWhileHavingAura() {
        TestData.fillPowerBar(player);
        player.activateAura();
        TestData.fillPowerBar(player);
        assertFalse(player.activateAura());
        assertTrue(player.getPowerBar().isFull());
    }

    @Test
    public void shouldRechargeBarWhileHavingAura() {
        TestData.fillPowerBar(player);
        player.activateAura();
        player.update(5, field);
        assertEquals(0.5, player.getPowerBar().getCharge(), 0.001);
        assertTrue(player.hasAura());
    }

    @Test
    public void shouldConsumeAura() {
        TestData.fillPowerBar(player);
        player.activateAura();
        assertTrue(player.consumeAura());
        assertFalse(player.hasAura());
        assertFalse(player.consumeAura());
    }

    @Test
    public void shouldKickBallWithFixedSpeedWhateverTheKickStat() throws DOPOSoccerException {
        Ball ball = new Ball(player.getX() + 40, player.getY(), Field.BALL_RADIUS);
        for (int i = 0; i < 5; i++) {
            profile.getStats().increase(StatType.KICK);
        }
        assertTrue(player.kick(ball));
        assertEquals(Player.KICK_SPEED, ball.getVelocityX(), 0.001);
    }

    @Test
    public void shouldNotKickBallOutOfReach() {
        Ball ball = new Ball(player.getX() + 300, player.getY(), Field.BALL_RADIUS);
        assertFalse(player.kick(ball));
    }

    @Test
    public void shouldReachRivalInFrontWithTheFoot() {
        Player rival = TestData.player(false);
        rival.setPosition(player.getX() + Player.WIDTH + 10, player.getY());
        Player behind = TestData.player(false);
        behind.setPosition(player.getX() - Player.WIDTH - 10, player.getY());
        assertTrue(player.reachesWithKick(rival));
        assertFalse(player.reachesWithKick(behind));
        assertFalse(player.reachesWithKick(null));
    }

    @Test
    public void shouldStayBetweenGoalLines() {
        player.move(-1, 10, field);
        assertEquals(field.getLeftGoalLineX() + Player.WIDTH / 2, player.getX(), 0.001);
    }

    @Test
    public void shouldClearEffectAndStunWhenPositionResets() {
        player.applyEffect(new ElectrocutedEffect(3));
        player.receiveDamage(100);
        player.resetPosition(320, field.getPlayerStartY());
        assertNull(player.getEffect());
        assertFalse(player.isStunned());
        assertEquals(player.getMaxLife(), player.getLife(), 0.001);
    }
}
