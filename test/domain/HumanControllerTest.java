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
        GameCharacter leftCharacter = new GameCharacter(
                "Left", new Country("Left", new FreezePower()), 3, 3, 3);
        GameCharacter rightCharacter = new GameCharacter(
                "Right", new Country("Right", new FreezePower()), 3, 3, 3);
        Player right = new Player(rightCharacter, false, 0, 0);
        match = new Match(MatchConfig.time(GameMode.PVP),
                new Player(leftCharacter, true, 0, 0), right);
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
    public void shouldArmPowerOnRequestAndConsumeIt() {
        fillPower(left);
        controller.requestPower();

        runner.update(0.01);

        assertTrue(left.isPowerArmed());
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
        for (int i = 0; i < 100; i++) {
            player.update(0.1, match.getField());
        }
    }
}
