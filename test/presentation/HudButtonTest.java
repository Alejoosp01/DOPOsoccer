package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del boton de imagen dibujado dentro del panel de juego.
 */
public class HudButtonTest {

    private int clicks;

    @Test
    public void containsDetectaPuntosDentroYFuera() {
        HudButton button = new HudButton("pause", "PAUSA", () -> clicks++);
        button.setBounds(10, 20, 50, 40);
        assertTrue(button.contains(10, 20));
        assertTrue(button.contains(59, 59));
        assertFalse(button.contains(60, 30));
        assertFalse(button.contains(5, 30));
    }

    @Test
    public void soltarDentroEjecutaLaAccion() {
        clicks = 0;
        HudButton button = new HudButton("pause", "PAUSA", () -> clicks++);
        button.setBounds(10, 20, 50, 40);
        assertTrue(button.press(20, 30));
        assertTrue(button.isPressed());
        assertTrue(button.release(25, 35));
        assertEquals(1, clicks);
        assertFalse(button.isPressed());
    }

    @Test
    public void soltarFueraONoPresionarNoEjecutaLaAccion() {
        clicks = 0;
        HudButton button = new HudButton("pause", "PAUSA", () -> clicks++);
        button.setBounds(10, 20, 50, 40);
        assertFalse(button.press(200, 200));
        assertFalse(button.release(20, 30));
        button.press(20, 30);
        assertFalse(button.release(300, 300));
        assertEquals(0, clicks);
    }

    @Test
    public void unBotonDecorativoNoSePresiona() {
        HudButton icon = new HudButton("trophy", "", null);
        icon.setBounds(0, 0, 40, 40);
        assertFalse(icon.press(10, 10));
    }

    @Test
    public void dibujaSinImagenSinLanzarExcepcion() {
        HudButton button = new HudButton("no_existe", "X", () -> { });
        button.setBounds(0, 0, 64, 64);
        BufferedImage canvas = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        assertDoesNotThrow(() -> button.draw(g2, 1.0));
        g2.dispose();
    }
}
