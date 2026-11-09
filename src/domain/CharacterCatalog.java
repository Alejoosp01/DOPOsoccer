package domain;

import java.util.ArrayList;

/**
 * Personajes disponibles para escoger.
 * Por ahora cada personaje lleva el nombre de su pais.
 */
public class CharacterCatalog {

    /**
     * Crea y devuelve los 5 personajes disponibles.
     */
    public static ArrayList<GameCharacter> createCharacters() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = new ArrayList<GameCharacter>();
        characters.add(new GameCharacter("Corea del Sur", new Country("Corea del Sur", new PushPower()), 1, 2, 5));
        characters.add(new GameCharacter("Estados Unidos", new Country("Estados Unidos", new IllusionPower()), 2, 2, 4));
        characters.add(new GameCharacter("Canada", new Country("Canada", new FreezePower()), 3, 3, 4));
        characters.add(new GameCharacter("Colombia", new Country("Colombia", new ShrinkPower()), 5, 3, 2));
        characters.add(new GameCharacter("Brasil", new Country("Brasil", new ConfusionPower()), 3, 4, 3));
        return characters;
    }
}
