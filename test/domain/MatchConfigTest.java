package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MatchConfigTest {

    @Test
    public void shouldCreateTimedMatchInMinutes() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.byTime(GameMode.PVP, 3);
        assertEquals(MatchType.TIME, config.getMatchType());
        assertEquals(3, config.getMinutes());
        assertEquals(180, config.getDurationSeconds(), 0.001);
        assertTrue(config.isTimed());
    }

    @Test
    public void shouldCreateGoalsMatch() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.byGoals(GameMode.PVP, 4);
        assertEquals(MatchType.GOALS, config.getMatchType());
        assertEquals(4, config.getGoalsTarget());
        assertFalse(config.isTimed());
    }

    @Test
    public void shouldAcceptLimits() throws DOPOSoccerException {
        assertEquals(1, MatchConfig.byTime(GameMode.PVP, 1).getMinutes());
        assertEquals(10, MatchConfig.byTime(GameMode.PVP, 10).getMinutes());
        assertEquals(1, MatchConfig.byGoals(GameMode.PVP, 1).getGoalsTarget());
        assertEquals(10, MatchConfig.byGoals(GameMode.PVP, 10).getGoalsTarget());
    }

    @Test
    public void shouldRejectMinutesOutOfRange() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> MatchConfig.byTime(GameMode.PVP, 0));
        assertEquals(DOPOSoccerException.INVALID_MINUTES, e.getMessage());
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.byTime(GameMode.PVP, 11));
    }

    @Test
    public void shouldRejectGoalsOutOfRange() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> MatchConfig.byGoals(GameMode.PVP, 0));
        assertEquals(DOPOSoccerException.INVALID_GOALS, e.getMessage());
        assertThrows(DOPOSoccerException.class, () -> MatchConfig.byGoals(GameMode.PVP, 11));
    }

    @Test
    public void shouldRejectMissingGameMode() {
        assertThrows(IllegalArgumentException.class, () -> MatchConfig.byTime(null, 2));
    }
}
