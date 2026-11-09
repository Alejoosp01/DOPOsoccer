package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HumanControllerTest {

    private Match match;
    private MatchRunner runner;
    private HumanController controller;
    private Player left;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        match = new Match(TestData.byTime(GameMode.PVP, 1), TestData.player(true), TestData.player(false));
        runner = new MatchRunner(match, new HumanController(), new HumanController());
        controller = (HumanController) runner.getLeftController();
        left = match.getLeftPlayer();
        runner.start();
    }

    @Test
    public void shouldMoveWhileDirectionIsHeld() {
        double startX = left.getX();
        controller.setDirection(1);

        runner.update(0.1);
        assertEquals(startX + left.getMoveSpeed() * 0.1, left.getX(), 0.001);

        runner.update(0.1);
        assertEquals(startX + left.getMoveSpeed() * 0.2, left.getX(), 0.001);
    }

    @Test
    public void shouldStopWhenDirectionIsReleased() {
        controller.setDirection(1);
        runner.update(0.1);
        double x = left.getX();

        controller.setDirection(0);
        runner.update(0.1);

        assertEquals(x, left.getX(), 0.001);
    }

    @Test
    public void shouldNormalizeDirection() {
        controller.setDirection(5);
        assertEquals(1, controller.getDirection(), 0.001);
        controller.setDirection(-4);
        assertEquals(-1, controller.getDirection(), 0.001);
        controller.setDirection(0);
        assertEquals(0, controller.getDirection(), 0.001);
    }

    @Test
    public void shouldJumpOnRequestAndConsumeIt() {
        controller.requestJump();
        runner.update(0.01);

        assertTrue(left.getVelocityY() < 0);
        assertFalse(controller.isJumpRequested());
    }

    @Test
    public void shouldKickOnRequestAndConsumeIt() {
        match.getBall().setPosition(left.getX() + 50, left.getY());
        match.getBall().setVelocity(0, 0);
        controller.requestKick();

        runner.update(0.01);

        assertTrue(match.getBall().getVelocityX() > 0);
        assertFalse(controller.isKickRequested());
    }

    @Test
    public void shouldActivateAuraOnRequestAndConsumeIt() {
        fillPower(left);
        controller.requestPower();

        runner.update(0.01);

        assertTrue(left.hasAura());
        assertFalse(controller.isPowerRequested());
    }

    @Test
    public void shouldIgnoreActionsWhenPaused() {
        match.pause();
        double startX = left.getX();
        controller.setDirection(1);
        controller.requestJump();

        runner.update(0.1);

        assertEquals(startX, left.getX(), 0.001);
        assertEquals(0, left.getVelocityY(), 0.001);
    }

    private void fillPower(Player player) {
        TestData.fillPowerBar(player);
    }
}
