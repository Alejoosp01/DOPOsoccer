package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PlayerProfileTest {

    @Test
    public void shouldStartWithZeroPoints() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("Jugador 1");
        assertEquals("Jugador 1", profile.getName());
        assertEquals(0, profile.getPoints());
    }

    @Test
    public void shouldNotCreateProfileWithoutName() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new PlayerProfile(" "));
        assertEquals(DOPOSoccerException.EMPTY_NAME, e.getMessage());
    }

    @Test
    public void shouldAddOnlyPositivePoints() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("A");
        profile.addPoints(100);
        profile.addPoints(-50);
        assertEquals(100, profile.getPoints());
    }

    @Test
    public void shouldBuyUpgradeAndPayItsCost() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("A");
        profile.addPoints(600);
        profile.upgrade(StatType.KICK);
        assertEquals(2, profile.getStats().get(StatType.KICK));
        assertEquals(100, profile.getPoints());
        assertEquals(1000, profile.getUpgradeCost(StatType.KICK));
    }

    @Test
    public void shouldNotBuyUpgradeWithoutEnoughPoints() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("A");
        profile.addPoints(499);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> profile.upgrade(StatType.SPEED));
        assertEquals(DOPOSoccerException.NOT_ENOUGH_POINTS, e.getMessage());
        assertEquals(1, profile.getStats().get(StatType.SPEED));
        assertEquals(499, profile.getPoints());
    }
}
