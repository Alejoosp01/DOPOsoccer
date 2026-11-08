package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SurvivalSeriesTest {

    private Field field;
    private Player left;
    private Player right;

    @BeforeEach
    public void setUp() throws POOBSoccerException {
        field = new Field();
        GameCharacter leftCharacter = new GameCharacter(
                "Left", new Country("Left", new FreezePower()), 3, 3, 3);
        GameCharacter rightCharacter = new GameCharacter(
                "Right", new Country("Right", new FreezePower()), 3, 3, 3);
        left = new Player(leftCharacter, true, 0, 0);
        right = new Player(rightCharacter, false, 0, 0);
    }

    private SurvivalSeries createSeries(int balls) {
        return new SurvivalSeries(MatchConfig.survival(balls), field, left, right,
                new HumanController(), new HumanController());
    }

    private void scoreInRightGoal(SurvivalSeries series) {
        Match match = series.getCurrentMatch();
        match.getBall().setPosition(1230, 500);
        match.getBall().setVelocity(0, 0);
        series.update(0.01);
    }

    private void scoreInLeftGoal(SurvivalSeries series) {
        Match match = series.getCurrentMatch();
        match.getBall().setPosition(50, 500);
        match.getBall().setVelocity(0, 0);
        series.update(0.01);
    }

    @Test
    public void shouldStartWithoutCurrentMatch() {
        SurvivalSeries series = createSeries(2);
        assertNull(series.getCurrentMatch());
        assertFalse(series.isFinished());
        assertEquals(Winner.NONE, series.getSeriesWinner());
    }

    @Test
    public void shouldStartFirstRoundWithConfiguredBalls() {
        SurvivalSeries series = createSeries(2);
        series.start();

        assertNotNull(series.getCurrentMatch());
        assertEquals(MatchState.PLAYING, series.getCurrentMatch().getState());
        assertEquals(2, series.getCurrentMatch().getRemainingBalls(Side.LEFT));
        assertEquals(2, series.getCurrentMatch().getRemainingBalls(Side.RIGHT));
        assertEquals(0, series.getRoundsWon());
        assertEquals(0, series.getRoundsPlayed());
        assertFalse(series.isFinished());
    }

    @Test
    public void shouldUseHumanAndMachineByDefault() {
        SurvivalSeries series = new SurvivalSeries(MatchConfig.survival(1), field, left, right);
        series.start();

        assertTrue(series.getLeftController() instanceof HumanController);
        assertTrue(series.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldRejectNonSurvivalConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new SurvivalSeries(MatchConfig.time(GameMode.PVP), field, left, right,
                        new HumanController(), new HumanController()));
    }

    @Test
    public void shouldNotCountRoundWhileMatchIsPlaying() {
        SurvivalSeries series = createSeries(2);
        series.start();

        series.update(0.5);

        assertEquals(0, series.getRoundsPlayed());
        assertEquals(0, series.getRoundsWon());
        assertEquals(MatchState.PLAYING, series.getCurrentMatch().getState());
    }

    @Test
    public void shouldKeepRoundAliveWhileBallsRemain() {
        SurvivalSeries series = createSeries(2);
        series.start();

        scoreInLeftGoal(series);
        assertEquals(MatchState.GOAL, series.getCurrentMatch().getState());
        assertEquals(0, series.getRoundsPlayed());

        series.update(1.1);

        assertEquals(MatchState.PLAYING, series.getCurrentMatch().getState());
        assertEquals(1, series.getCurrentMatch().getRemainingBalls(Side.LEFT));
        assertEquals(2, series.getCurrentMatch().getRemainingBalls(Side.RIGHT));
        assertEquals(0, series.getRoundsPlayed());
        assertFalse(series.isFinished());
    }

    @Test
    public void shouldAdvanceWhenPlayerWinsRound() {
        SurvivalSeries series = createSeries(1);
        series.start();
        Match first = series.getCurrentMatch();

        scoreInRightGoal(series);
        assertEquals(MatchState.GOAL, first.getState());
        assertEquals(0, series.getRoundsPlayed());

        series.update(1.1);

        assertEquals(1, series.getRoundsWon());
        assertEquals(1, series.getRoundsPlayed());
        assertFalse(series.isFinished());
        assertNotSame(first, series.getCurrentMatch());
        assertEquals(MatchState.PLAYING, series.getCurrentMatch().getState());
        assertEquals(1, series.getCurrentMatch().getRemainingBalls(Side.LEFT));
        assertEquals(1, series.getCurrentMatch().getRemainingBalls(Side.RIGHT));
        assertEquals(field.getBallStartX(), series.getCurrentMatch().getBall().getX(), 0.001);
        assertEquals(field.getLeftPlayerStartX(), left.getX(), 0.001);
    }

    @Test
    public void shouldCountMultipleRoundsWon() {
        SurvivalSeries series = createSeries(1);
        series.start();

        scoreInRightGoal(series);
        series.update(1.1);
        scoreInRightGoal(series);
        series.update(1.1);

        assertEquals(2, series.getRoundsWon());
        assertEquals(2, series.getRoundsPlayed());
        assertFalse(series.isFinished());
        assertEquals(MatchState.PLAYING, series.getCurrentMatch().getState());
    }

    @Test
    public void shouldRaiseMachineLevelEachRoundWon() {
        SurvivalSeries series = new SurvivalSeries(MatchConfig.survival(1), field, left, right);
        series.start();
        assertEquals(1, series.getMachineLevel());
        assertEquals(1, ((MachineController) series.getRightController()).getLevel());

        for (int round = 2; round <= 6; round++) {
            scoreInRightGoal(series);
            series.update(1.1);
            int expected = Math.min(round, MachineController.MAX_LEVEL);
            assertEquals(expected, series.getMachineLevel());
            assertEquals(expected, ((MachineController) series.getRightController()).getLevel());
        }
        assertEquals(5, series.getRoundsWon());
    }

    @Test
    public void shouldRestartMachineLevelWhenSeriesRestarts() {
        SurvivalSeries series = new SurvivalSeries(MatchConfig.survival(1), field, left, right);
        series.start();
        scoreInRightGoal(series);
        series.update(1.1);
        assertEquals(2, series.getMachineLevel());

        series.start();
        assertEquals(1, series.getMachineLevel());
    }

    @Test
    public void shouldEndSeriesWhenPlayerLoses() {
        SurvivalSeries series = createSeries(1);
        series.start();

        scoreInLeftGoal(series);
        series.update(1.1);

        assertTrue(series.isFinished());
        assertEquals(Winner.RIGHT, series.getSeriesWinner());
        assertEquals(0, series.getRoundsWon());
        assertEquals(1, series.getRoundsPlayed());
        assertEquals(MatchState.FINISHED, series.getCurrentMatch().getState());
    }

    @Test
    public void shouldIgnoreUpdatesAfterSeriesFinished() {
        SurvivalSeries series = createSeries(1);
        series.start();
        scoreInLeftGoal(series);
        series.update(1.1);
        Match last = series.getCurrentMatch();

        series.update(1.0);

        assertSame(last, series.getCurrentMatch());
        assertEquals(MatchState.FINISHED, series.getCurrentMatch().getState());
        assertEquals(1, series.getRoundsPlayed());
    }

    @Test
    public void shouldResetPowerBetweenRounds() {
        SurvivalSeries series = createSeries(1);
        series.start();
        for (int i = 0; i < 100; i++) {
            left.update(0.1, field);
        }
        assertTrue(left.isPowerReady());
        assertTrue(left.armPower());

        scoreInRightGoal(series);
        series.update(1.1);

        assertFalse(left.isPowerReady());
        assertFalse(left.isPowerArmed());
        assertEquals(0, left.getPowerCharge(), 0.001);
    }
}
