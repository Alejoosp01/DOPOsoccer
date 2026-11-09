package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MachineControllerTest {

    private Match match;
    private MachineController machine;
    private Player left;
    private Player right;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        left = TestData.player(true);
        right = TestData.player(false);
        match = new Match(TestData.byTime(GameMode.PVM, 1), left, right);
        machine = new MachineController();
        match.start();
    }

    @Test
    public void shouldMoveTowardBall() {
        match.getBall().setPosition(700, 500);
        double startX = left.getX();

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() > startX);
    }

    @Test
    public void shouldMoveBehindBallToAttack() {
        match.getBall().setPosition(600, 580);
        left.setPosition(700, match.getField().getPlayerStartY());
        double startX = left.getX();

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() < startX);
    }

    @Test
    public void shouldKickWhenTouchingBall() {
        match.getBall().setPosition(400, match.getField().getPlayerStartY());
        left.setPosition(360, match.getField().getPlayerStartY());

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(match.getBall().getVelocityX() > 0);
    }

    @Test
    public void shouldJumpForHighBall() {
        match.getBall().setPosition(320, 450);

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.getVelocityY() < 0);
    }

    @Test
    public void shouldActivateAuraWhenBarIsFull() {
        fillPower(left);

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.hasAura());
    }

    @Test
    public void shouldFirePowerShotWhenTouchingBallWithAura() {
        fillPower(left);
        match.getBall().setPosition(400, match.getField().getPlayerStartY());
        left.setPosition(360, match.getField().getPlayerStartY());

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(match.getBall().hasActiveShot());
        assertFalse(left.hasAura());
        assertTrue(match.getBall().getVelocityX() > 0);
    }

    @Test
    public void shouldPositionRightPlayerToRightOfBall() {
        match.getBall().setPosition(600, match.getField().getPlayerStartY());
        right.setPosition(500, match.getField().getPlayerStartY());
        double startX = right.getX();

        machine.update(match, Side.RIGHT, 0.1);

        assertTrue(right.getX() > startX);
    }

    @Test
    public void shouldNotActWhenPaused() {
        match.pause();
        double startX = left.getX();
        machine.update(match, Side.LEFT, 0.1);

        assertEquals(startX, left.getX(), 0.001);
        assertEquals(0, left.getVelocityY(), 0.001);
    }

    @Test
    public void shouldReturnToDefendWhenBallIsBehind() {
        match.getBall().setPosition(600, 580);
        left.setPosition(700, match.getField().getPlayerStartY());

        for (int i = 0; i < 20; i++) {
            machine.update(match, Side.LEFT, 0.1);
        }

        assertTrue(left.getX() < match.getBall().getX());
    }

    @Test
    public void shouldJumpToBlockHighBallComingHome() {
        match.getBall().setPosition(480, 400);
        match.getBall().setVelocity(-400, 0);

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.getVelocityY() < 0);
    }

    @Test
    public void shouldNotJumpForHighBallGoingAway() {
        match.getBall().setPosition(480, 400);
        match.getBall().setVelocity(400, 0);

        machine.update(match, Side.LEFT, 0.1);

        assertEquals(0, left.getVelocityY(), 0.001);
    }

    @Test
    public void shouldNotKickTowardOwnGoal() {
        double groundCenter = match.getField().getPlayerStartY();
        left.setPosition(400, groundCenter);
        match.getBall().setPosition(362, groundCenter);

        machine.update(match, Side.LEFT, 0.1);

        assertEquals(0, match.getBall().getVelocityX(), 0.001);
    }

    @Test
    public void shouldKeepDecisionUntilReactionTimePasses() {
        MachineController slow = new MachineController(1);
        match.getBall().setPosition(700, 580);

        double start = left.getX();
        slow.update(match, Side.LEFT, 0.1);
        assertTrue(left.getX() > start);

        match.getBall().setPosition(150, 580);
        double beforeSecond = left.getX();
        slow.update(match, Side.LEFT, 0.1);
        assertTrue(left.getX() > beforeSecond);

        double beforeThird = left.getX();
        slow.update(match, Side.LEFT, 0.25);
        assertTrue(left.getX() < beforeThird);
    }

    @Test
    public void shouldReactFasterWithHigherLevel() {
        assertEquals(0.30, new MachineController(1).getReactionTime(), 0.0001);
        assertEquals(0.10, new MachineController(5).getReactionTime(), 0.0001);
        assertEquals(MachineController.DEFAULT_LEVEL, new MachineController().getLevel());
    }

    @Test
    public void shouldRejectInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> new MachineController(0));
        assertThrows(IllegalArgumentException.class, () -> new MachineController(6));
    }

    private void fillPower(Player player) {
        TestData.fillPowerBar(player);
    }
}
