package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PowerBarTest {

    @Test
    public void shouldStartEmptyAndFillInTenSeconds() {
        PowerBar bar = new PowerBar();
        assertEquals(0, bar.getCharge(), 0.001);
        bar.update(5);
        assertEquals(0.5, bar.getCharge(), 0.001);
        assertFalse(bar.isFull());
        bar.update(5);
        assertTrue(bar.isFull());
    }

    @Test
    public void shouldNotPassFull() {
        PowerBar bar = new PowerBar();
        bar.update(30);
        assertEquals(1, bar.getCharge(), 0.001);
    }

    @Test
    public void shouldAddAQuarterWhenGoalIsReceived() {
        PowerBar bar = new PowerBar();
        bar.addQuarter();
        assertEquals(0.25, bar.getCharge(), 0.001);
        bar.update(9);
        bar.addQuarter();
        assertEquals(1, bar.getCharge(), 0.001);
    }

    @Test
    public void shouldEmpty() {
        PowerBar bar = new PowerBar();
        bar.update(10);
        bar.empty();
        assertEquals(0, bar.getCharge(), 0.001);
    }
}
