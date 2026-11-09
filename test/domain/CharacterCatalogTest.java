package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class CharacterCatalogTest {

    @Test
    public void shouldOfferTheFourCharactersOfAnnex1() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = CharacterCatalog.createCharacters();
        assertEquals(4, characters.size());
        assertEquals("Corea del Sur", characters.get(0).getName());
        assertEquals("Camerun", characters.get(1).getName());
        assertEquals("Nigeria", characters.get(2).getName());
        assertEquals("Estados Unidos", characters.get(3).getName());
    }

    @Test
    public void shouldGiveEachCountryItsPowerShot() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = CharacterCatalog.createCharacters();
        assertTrue(characters.get(0).getPowerShot() instanceof BlueAuraShot);
        assertTrue(characters.get(1).getPowerShot() instanceof LightningShot);
        assertTrue(characters.get(2).getPowerShot() instanceof TornadoShot);
        assertTrue(characters.get(3).getPowerShot() instanceof IllusionShot);
    }
}
