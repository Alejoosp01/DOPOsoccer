package domain;

import java.util.ArrayList;

/**
 * Personajes disponibles para escoger: los cuatro del Anexo 1 del enunciado.
 * Para agregar un personaje nuevo basta con crear su tiro de poder y sumarlo aqui.
 */
public class CharacterCatalog {

    /**
     * Crea y devuelve los personajes disponibles.
     */
    public static ArrayList<GameCharacter> createCharacters() throws DOPOSoccerException {
        ArrayList<GameCharacter> characters = new ArrayList<GameCharacter>();
        characters.add(new GameCharacter("Corea del Sur", new BlueAuraShot()));
        characters.add(new GameCharacter("Camerun", new LightningShot()));
        characters.add(new GameCharacter("Nigeria", new TornadoShot()));
        characters.add(new GameCharacter("Estados Unidos", new IllusionShot()));
        return characters;
    }
}
