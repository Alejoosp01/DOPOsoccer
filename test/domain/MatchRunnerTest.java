package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MatchRunnerTest {

    private Match createMatch(GameMode mode) throws DOPOSoccerException {
        return TestData.match(TestData.byTime(mode, 1));
    }

    @Test
    public void shouldCreateHumanControllersForPvp() throws DOPOSoccerException {
        MatchRunner runner = MatchRunner.forMatch(createMatch(GameMode.PVP));
        assertTrue(runner.getLeftController() instanceof HumanController);
        assertTrue(runner.getRightController() instanceof HumanController);
    }

    @Test
    public void shouldCreateHumanAndMachineForPvm() throws DOPOSoccerException {
        MatchRunner runner = MatchRunner.forMatch(createMatch(GameMode.PVM));
        assertTrue(runner.getLeftController() instanceof HumanController);
        assertTrue(runner.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldAdvanceMatchThroughRunner() throws DOPOSoccerException {
        Match match = createMatch(GameMode.PVP);
        MatchRunner runner = MatchRunner.forMatch(match);
        runner.start();
        double timeBefore = match.getTimeLeft();

        runner.update(0.5);

        assertEquals(MatchState.PLAYING, match.getState());
        assertTrue(match.getTimeLeft() < timeBefore);
    }

    @Test
    public void shouldProduceSameResultWithTwoMachineRuns() throws DOPOSoccerException {
        Match first = createMatch(GameMode.PVM);
        Match second = createMatch(GameMode.PVM);
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
