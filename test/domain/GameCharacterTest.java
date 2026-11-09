package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GameCharacterTest {

    @Test
    public void shouldCreateCharacterWithPowerShot() throws DOPOSoccerException {
        PowerShot shot = new LightningShot();
        GameCharacter character = new GameCharacter("Camerun", shot);
        assertEquals("Camerun", character.getName());
        assertSame(shot, character.getPowerShot());
    }

    @Test
    public void shouldNotCreateCharacterWithoutName() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new GameCharacter("  ", new LightningShot()));
        assertEquals(DOPOSoccerException.EMPTY_NAME, e.getMessage());
    }

    @Test
    public void shouldNotCreateCharacterWithoutPowerShot() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new GameCharacter("Camerun", null));
        assertEquals(DOPOSoccerException.NO_POWER_SHOT, e.getMessage());
    }
}
