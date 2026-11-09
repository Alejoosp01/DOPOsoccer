package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class CharacterCatalogTest {

    @Test
    public void shouldHaveFiveCharacters() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = CharacterCatalog.createCharacters();
        assertEquals(5, characters.size());
    }

    @Test
    public void shouldGiveEachCountryItsPower() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = CharacterCatalog.createCharacters();
        assertTrue(characters.get(0).getPower() instanceof PushPower);
        assertTrue(characters.get(1).getPower() instanceof IllusionPower);
        assertTrue(characters.get(2).getPower() instanceof FreezePower);
        assertTrue(characters.get(3).getPower() instanceof ShrinkPower);
        assertTrue(characters.get(4).getPower() instanceof ConfusionPower);
    }
}
