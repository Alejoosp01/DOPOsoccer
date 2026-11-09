package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StatsTest {

    @Test
    public void shouldStartAllStatsAtOne() {
        Stats stats = new Stats();
        assertEquals(1, stats.get(StatType.SPEED));
        assertEquals(1, stats.get(StatType.KICK));
        assertEquals(1, stats.get(StatType.JUMP));
        assertEquals(1, stats.get(StatType.SURVIVAL));
    }

    @Test
    public void shouldComputeMaxLifeLikeTable2() throws DOPOSoccerException {
        Stats stats = new Stats();
        assertEquals(100, stats.getMaxLife(), 0.001);
        stats.increase(StatType.SURVIVAL);
        stats.increase(StatType.SURVIVAL);
        assertEquals(120, stats.getMaxLife(), 0.001);
    }

    @Test
    public void shouldComputeKickMultiplierLikeTable3() throws DOPOSoccerException {
        Stats stats = new Stats();
        assertEquals(1.0, stats.getKickMultiplier(), 0.001);
        for (int i = 0; i < 4; i++) {
            stats.increase(StatType.KICK);
        }
        assertEquals(1.4, stats.getKickMultiplier(), 0.001);
    }

    @Test
    public void shouldReachTenButNotMore() throws DOPOSoccerException {
        Stats stats = new Stats();
        for (int i = 0; i < 9; i++) {
            stats.increase(StatType.SPEED);
        }
        assertEquals(10, stats.get(StatType.SPEED));
        assertEquals(1.9, stats.multiplier(StatType.SPEED), 0.001);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> stats.increase(StatType.SPEED));
        assertEquals(DOPOSoccerException.STAT_AT_MAX, e.getMessage());
    }

    @Test
    public void shouldDoubleUpgradeCostEachPoint() throws DOPOSoccerException {
        Stats stats = new Stats();
        assertEquals(500, stats.getUpgradeCost(StatType.JUMP));
        stats.increase(StatType.JUMP);
        assertEquals(1000, stats.getUpgradeCost(StatType.JUMP));
        stats.increase(StatType.JUMP);
        assertEquals(2000, stats.getUpgradeCost(StatType.JUMP));
        assertEquals(500, stats.getUpgradeCost(StatType.KICK));
    }
}
