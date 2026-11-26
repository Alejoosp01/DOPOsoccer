package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los cuatro perfiles de maquina: torpe, ofensiva, defensiva y experta.
 */
public class MachineProfileTest {

    private Match match;
    private Player left;
    private Ball ball;
    private double standY;

    @BeforeEach
    public void setUp() {
        left = TestData.player(true);
        Player right = TestData.player(false);
        match = new Match(TestData.byTime(GameMode.PVM, 3), left, right);
        match.start();
        ball = match.getBall();
        standY = match.getField().getPlayerStartY();
    }

    @Test
    public void shouldCreateTheControllerOfEachProfile() {
        assertTrue(MachineProfile.CLUMSY.createController() instanceof ClumsyMachine);
        assertTrue(MachineProfile.OFFENSIVE.createController() instanceof OffensiveMachine);
        assertTrue(MachineProfile.DEFENSIVE.createController() instanceof DefensiveMachine);
        assertTrue(MachineProfile.EXPERT.createController() instanceof ExpertMachine);
        for (MachineProfile profile : MachineProfile.values()) {
            assertEquals(profile, profile.createController().getProfile());
        }
    }

    @Test
    public void shouldHaveSpanishNames() {
        assertEquals("Torpe", MachineProfile.CLUMSY.getDisplayName());
        assertEquals("Ofensiva", MachineProfile.OFFENSIVE.getDisplayName());
        assertEquals("Defensiva", MachineProfile.DEFENSIVE.getDisplayName());
        assertEquals("Experta", MachineProfile.EXPERT.getDisplayName());
    }

    @Test
    public void offensiveShouldGoAfterBallInRivalHalf() {
        left.setPosition(600, standY);
        ball.setPosition(1000, 600);

        new OffensiveMachine().update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() > 600);
    }

    @Test
    public void defensiveShouldStayHomeWhenBallIsInRivalHalf() {
        left.setPosition(600, standY);
        ball.setPosition(1000, 600);
        DefensiveMachine machine = new DefensiveMachine();

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() < 600);
        assertEquals(match.getField().getLeftGoalLineX() + MachineController.GUARD_DISTANCE,
                machine.getTargetX(), 0.001);
    }

    @Test
    public void defensiveShouldClearBallInOwnHalf() {
        left.setPosition(200, standY);
        ball.setPosition(450, 600);

        new DefensiveMachine().update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() > 200);
    }

    @Test
    public void expertShouldDefendWhenWinning() {
        match.getScore().score(Side.LEFT);
        left.setPosition(600, standY);
        ball.setPosition(1000, 600);

        new ExpertMachine().update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() < 600);
    }

    @Test
    public void expertShouldAttackWhenLosing() {
        match.getScore().score(Side.RIGHT);
        left.setPosition(600, standY);
        ball.setPosition(1000, 600);

        new ExpertMachine().update(match, Side.LEFT, 0.1);

        assertTrue(left.getX() > 600);
    }

    @Test
    public void expertShouldGoBackToDefendWhenTiedAndBallBehind() {
        left.setPosition(700, standY);
        ball.setPosition(600, 600);
        ExpertMachine machine = new ExpertMachine();

        machine.update(match, Side.LEFT, 0.1);

        assertTrue(machine.getTargetX() < 600);
    }

    @Test
    public void clumsyShouldPickTargetsInsideTheField() {
        ClumsyMachine machine = new ClumsyMachine(new Random(42));
        double minX = match.getField().getLeftGoalLineX() + left.getWidth() / 2;
        double maxX = match.getField().getRightGoalLineX() - left.getWidth() / 2;
        for (int i = 0; i < 50; i++) {
            machine.update(match, Side.LEFT, ClumsyMachine.REACTION_TIME);
            assertTrue(machine.getTargetX() >= minX && machine.getTargetX() <= maxX);
        }
    }

    @Test
    public void clumsyShouldBeRepeatableWithTheSameSeed() {
        ClumsyMachine first = new ClumsyMachine(new Random(5));
        ClumsyMachine second = new ClumsyMachine(new Random(5));
        Match other = TestData.match(TestData.byTime(GameMode.PVM, 3));
        other.start();

        first.update(match, Side.LEFT, 0.01);
        second.update(other, Side.LEFT, 0.01);

        assertEquals(first.getTargetX(), second.getTargetX(), 0.0001);
    }

    @Test
    public void shouldRejectNullRandom() {
        assertThrows(IllegalArgumentException.class, () -> new ClumsyMachine(null));
    }

    @Test
    public void clearInputShouldForceANewDecision() {
        OffensiveMachine machine = new OffensiveMachine();
        ball.setPosition(1000, 600);
        machine.update(match, Side.LEFT, 0.01);
        double firstTarget = machine.getTargetX();

        ball.setPosition(300, 600);
        machine.clearInput();
        machine.update(match, Side.LEFT, 0.01);

        assertNotEquals(firstTarget, machine.getTargetX(), 0.001);
    }

    @Test
    public void runnerShouldUseTheChosenMachineProfile() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.byGoals(GameMode.PVM, 3, MachineProfile.DEFENSIVE);
        MatchRunner runner = MatchRunner.forMatch(TestData.match(config));

        assertTrue(runner.getRightController() instanceof DefensiveMachine);
    }

    @Test
    public void configShouldRequireMachineProfileInPvm() {
        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> MatchConfig.byTime(GameMode.PVM, 2, null));
        assertEquals(DOPOSoccerException.NO_MACHINE_PROFILE, error.getMessage());
    }

    @Test
    public void configShouldIgnoreMachineProfileInPvp() throws DOPOSoccerException {
        assertNull(MatchConfig.byGoals(GameMode.PVP, 2, MachineProfile.CLUMSY).getMachineProfile());
        assertNull(MatchConfig.byGoals(GameMode.PVP, 2, null).getMachineProfile());
    }

    @Test
    public void configShouldUseExpertMachineByDefault() throws DOPOSoccerException {
        assertEquals(MachineProfile.EXPERT, MatchConfig.byTime(GameMode.PVM, 2).getMachineProfile());
    }
}
