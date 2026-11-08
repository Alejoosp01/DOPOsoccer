package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MatchRunnerTest {

    private Match createMatch(GameMode mode) throws POOBSoccerException {
        GameCharacter leftCharacter = new GameCharacter(
                "Left", new Country("Left", new FreezePower()), 3, 3, 3);
        GameCharacter rightCharacter = new GameCharacter(
                "Right", new Country("Right", new FreezePower()), 3, 3, 3);
        Player left = new Player(leftCharacter, true, 0, 0);
        Player right = new Player(rightCharacter, false, 0, 0);
        return new Match(MatchConfig.time(mode), left, right);
    }

    @Test
    public void shouldCreateHumanControllersForPvp() throws POOBSoccerException {
        MatchRunner runner = MatchRunner.forMatch(createMatch(GameMode.PVP));
        assertTrue(runner.getLeftController() instanceof HumanController);
        assertTrue(runner.getRightController() instanceof HumanController);
    }

    @Test
    public void shouldCreateHumanAndMachineForPvm() throws POOBSoccerException {
        MatchRunner runner = MatchRunner.forMatch(createMatch(GameMode.PVM));
        assertTrue(runner.getLeftController() instanceof HumanController);
        assertTrue(runner.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldCreateMachineControllersForMvm() throws POOBSoccerException {
        MatchRunner runner = MatchRunner.forMatch(createMatch(GameMode.MVM));
        assertTrue(runner.getLeftController() instanceof MachineController);
        assertTrue(runner.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldAdvanceMatchThroughRunner() throws POOBSoccerException {
        Match match = createMatch(GameMode.PVP);
        MatchRunner runner = MatchRunner.forMatch(match);
        runner.start();
        double timeBefore = match.getTimeLeft();

        runner.update(0.5);

        assertEquals(MatchState.PLAYING, match.getState());
        assertTrue(match.getTimeLeft() < timeBefore);
    }

    @Test
    public void shouldProduceSameResultWithTwoMachineRuns() throws POOBSoccerException {
        Match first = createMatch(GameMode.MVM);
        Match second = createMatch(GameMode.MVM);
        MatchRunner firstRunner = MatchRunner.forMatch(first);
        MatchRunner secondRunner = MatchRunner.forMatch(second);
        firstRunner.start();
        secondRunner.start();

        for (int i = 0; i < 200; i++) {
            firstRunner.update(0.01);
            secondRunner.update(0.01);
        }

        assertEquals(first.getBall().getX(), second.getBall().getX(), 0.0001);
        assertEquals(first.getBall().getY(), second.getBall().getY(), 0.0001);
        assertEquals(first.getScore().getLeftGoals(), second.getScore().getLeftGoals());
        assertEquals(first.getScore().getRightGoals(), second.getScore().getRightGoals());
        assertEquals(first.getState(), second.getState());
    }
}
