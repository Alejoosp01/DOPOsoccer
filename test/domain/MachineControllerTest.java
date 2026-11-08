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
    public void setUp() throws POOBSoccerException {
        GameCharacter leftCharacter = new GameCharacter(
                "Left", new Country("Left", new FreezePower()), 3, 3, 3);
        GameCharacter rightCharacter = new GameCharacter(
                "Right", new Country("Right", new FreezePower()), 3, 3, 3);
        left = new Player(leftCharacter, true, 0, 0);
        right = new Player(rightCharacter, false, 0, 0);
        match = new Match(MatchConfig.time(GameMode.MVM), left, right);
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
    public void shouldArmPowerWhenReady() {
        fillPower(left);

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.isPowerArmed());
    }

    @Test
    public void shouldKickAndActivateArmedPower() {
        fillPower(left);
        match.getBall().setPosition(400, match.getField().getPlayerStartY());
        left.setPosition(360, match.getField().getPlayerStartY());

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(right.isFrozen());
        assertFalse(left.isPowerArmed());
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

    private void fillPower(Player player) {
        for (int i = 0; i < 100; i++) {
            player.update(0.1, match.getField());
        }
    }
}
