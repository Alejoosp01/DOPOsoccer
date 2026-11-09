package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CountryTest {

    @Test
    public void shouldCreateCountryWithPower() throws DOPOSoccerException {
        Country brasil = new Country("Brasil", new ConfusionPower());
        assertEquals("Brasil", brasil.getName());
        assertEquals("Confusion", brasil.getPower().getName());
    }

    @Test
    public void shouldNotCreateCountryWithoutName() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new Country("", new ConfusionPower()));
        assertEquals(DOPOSoccerException.EMPTY_NAME, e.getMessage());
    }

    @Test
    public void shouldNotCreateCountryWithoutPower() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new Country("Brasil", null));
        assertEquals(DOPOSoccerException.NO_POWER, e.getMessage());
    }
}
