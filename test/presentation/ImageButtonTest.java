package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de ImageButton; pintan en memoria, sin mostrar ventanas.
 */
public class ImageButtonTest {

    @BeforeAll
    public static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private void paint(ImageButton button) {
        button.setSize(220, 56);
        BufferedImage canvas = new BufferedImage(220, 56, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        button.paint(g2);
        g2.dispose();
    }

    private void paintAllStates(ImageButton button) {
        paint(button);
        button.getModel().setRollover(true);
        paint(button);
        button.getModel().setArmed(true);
        button.getModel().setPressed(true);
        paint(button);
        button.getModel().setPressed(false);
        button.getModel().setArmed(false);
        button.getModel().setRollover(false);
        button.setEnabled(false);
        paint(button);
    }

    @Test
    public void shouldFallBackWhenImageIsMissing() {
        ImageButton button = new ImageButton("no_existe", "Jugar", Color.ORANGE);
        assertFalse(button.hasImages());
        assertDoesNotThrow(() -> paintAllStates(button));
        assertTrue(button.getPreferredSize().width > 0);
    }

    @Test
    public void shouldPaintImagesInEveryState() {
        Assumptions.assumeTrue(SpriteLoader.buttonNormal(SpriteLoader.BUTTON_PLAY) != null
                && SpriteLoader.buttonPressed(SpriteLoader.BUTTON_PLAY) != null);
        ImageButton button = new ImageButton(SpriteLoader.BUTTON_PLAY, "Jugar", Color.ORANGE);
        assertTrue(button.hasImages());
        assertDoesNotThrow(() -> paintAllStates(button));
    }

    @Test
    public void shouldBeSquareWithoutLabel() {
        Assumptions.assumeTrue(SpriteLoader.buttonNormal(SpriteLoader.BUTTON_PLAY) != null
                && SpriteLoader.buttonPressed(SpriteLoader.BUTTON_PLAY) != null);
        ImageButton button = new ImageButton(SpriteLoader.BUTTON_PLAY, "Jugar", Color.ORANGE, false);
        Dimension size = button.getPreferredSize();
        assertEquals(size.width, size.height);
    }

    @Test
    public void shouldKeepTooltipAndAccessibleName() {
        ImageButton button = new ImageButton("no_existe", "Volver", Color.GRAY);
        assertEquals("Volver", button.getToolTipText());
        assertEquals("Volver", button.getAccessibleContext().getAccessibleName());
        assertTrue(button.isFocusable());
    }
}
