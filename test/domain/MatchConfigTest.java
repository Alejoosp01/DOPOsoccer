package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MatchConfigTest {

    @Test
    public void shouldLastSixtySecondsByDefault() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.time(GameMode.PVP);
        assertEquals(MatchType.TIME, config.getMatchType());
        assertEquals(60, config.getDuration(), 0.001);
        assertTrue(config.isTimed());
    }

    @Test
    public void shouldAcceptGoalTargetsThreeFiveAndSeven() throws DOPOSoccerException {
        assertEquals(3, MatchConfig.goals(GameMode.PVP, 3).getGoalsTarget());
        assertEquals(5, MatchConfig.goals(GameMode.PVM, 5).getGoalsTarget());
        assertEquals(7, MatchConfig.goals(GameMode.MVM, 7).getGoalsTarget());
    }

    @Test
    public void shouldRejectOtherGoalTargets() throws DOPOSoccerException {
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.goals(GameMode.PVP, 1));
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.goals(GameMode.PVP, 4));
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.goals(GameMode.PVP, 8));
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.goals(GameMode.PVP, 0));
    }

    @Test
    public void shouldPlaySurvivalOnlyAgainstMachine() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.survival(3);
        assertEquals(MatchType.SURVIVAL, config.getMatchType());
        assertEquals(GameMode.PVM, config.getGameMode());
        assertEquals(3, config.getBallsPerSide());
        assertFalse(config.isTimed());
    }

    @Test
    public void shouldRejectInvalidDurationAndBalls() throws DOPOSoccerException {
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.time(GameMode.PVP, 0));
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.time(GameMode.PVP, -5));
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.survival(0));
    }

    @Test
    public void shouldUseConstantMessageForInvalidGoalTarget() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> MatchConfig.goals(GameMode.PVP, 4));
        assertEquals(DOPOSoccerException.INVALID_GOAL_TARGET, e.getMessage());
    }

    @Test
    public void shouldRejectMissingGameMode() throws DOPOSoccerException {
        assertThrows(IllegalArgumentException.class, () -> MatchConfig.time(null));
    }
}
