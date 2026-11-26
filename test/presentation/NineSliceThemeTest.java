package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del escalado en 9 partes, del escalado proporcional y de los helpers de UiTheme.
 */
public class NineSliceThemeTest {

    private static final int RED = new Color(255, 0, 0).getRGB();
    private static final int GREEN = new Color(0, 255, 0).getRGB();

    private BufferedImage cornersImage() {
        BufferedImage image = new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 100, 80);
        g.setColor(Color.RED);
        g.fillRect(0, 0, 20, 20);
        g.dispose();
        return image;
    }

    @Test
    public void nineSliceShouldKeepExactSizeAndCorners() {
        BufferedImage result = SpriteLoader.nineSlice(cornersImage(), 300, 200, 20, 20, 20, 20, 1.0);
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
        assertEquals(RED, result.getRGB(5, 5));
        assertEquals(GREEN, result.getRGB(150, 100));
        assertEquals(GREEN, result.getRGB(295, 195));
    }

    @Test
    public void nineSliceShouldNotThrowWhenBordersDoNotFit() {
        BufferedImage result = SpriteLoader.nineSlice(cornersImage(), 30, 20, 20, 20, 20, 20, 1.0);
        assertEquals(30, result.getWidth());
        assertEquals(20, result.getHeight());
        BufferedImage invalid = SpriteLoader.nineSlice(cornersImage(), 50, 50, 90, 70, 90, 70, 1.0);
        assertEquals(50, invalid.getWidth());
        assertEquals(50, invalid.getHeight());
    }

    @Test
    public void nineSliceShouldShrinkFactorInsteadOfStretching() {
        BufferedImage result = SpriteLoader.nineSlice(cornersImage(), 30, 30, 20, 20, 20, 20, 1.0);
        assertEquals(RED, result.getRGB(2, 2));
        assertEquals(GREEN, result.getRGB(27, 27));
    }

    @Test
    public void nineSliceByPathShouldReturnNullIfMissingAndCacheIfPresent() {
        assertNull(SpriteLoader.nineSlice("/no/existe.png", 10, 10, SpriteLoader.RIBBON_INSETS, 0.2));
        org.junit.jupiter.api.Assumptions.assumeTrue(SpriteLoader.exists(SpriteLoader.STATS_BOARD));
        BufferedImage first = SpriteLoader.nineSlice(SpriteLoader.STATS_BOARD, 400, 200,
                SpriteLoader.STATS_BOARD_INSETS, 0.25);
        BufferedImage second = SpriteLoader.nineSlice(SpriteLoader.STATS_BOARD, 400, 200,
                SpriteLoader.STATS_BOARD_INSETS, 0.25);
        assertSame(first, second);
        assertEquals(400, first.getWidth());
    }

    @Test
    public void fitShouldKeepProportionInsideBox() {
        BufferedImage result = SpriteLoader.fit(new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB), 50, 50);
        assertEquals(50, result.getWidth());
        assertEquals(25, result.getHeight());
        BufferedImage tall = SpriteLoader.fit(new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB), 80, 40);
        assertEquals(20, tall.getWidth());
        assertEquals(40, tall.getHeight());
    }

    @Test
    public void scaledToFitShouldReturnNullForInvalidOrMissing() {
        assertNull(SpriteLoader.scaledToFit("/no/existe.png", 50, 50));
        assertNull(SpriteLoader.scaledToFit(SpriteLoader.STATS_BOARD, 0, 50));
        assertNull(SpriteLoader.scaledToFit(SpriteLoader.STATS_BOARD, 50, -1));
    }

    @Test
    public void scaledToFitShouldFitAndCache() {
        org.junit.jupiter.api.Assumptions.assumeTrue(SpriteLoader.exists(SpriteLoader.RIBBON_BLUE));
        BufferedImage first = SpriteLoader.scaledToFit(SpriteLoader.RIBBON_BLUE, 200, 200);
        assertTrue(first.getWidth() <= 200 && first.getHeight() <= 200);
        assertEquals(200, first.getWidth());
        assertSame(first, SpriteLoader.scaledToFit(SpriteLoader.RIBBON_BLUE, 200, 200));
    }

    @Test
    public void paintPanelShouldDrawOrangeBorderAndDarkCenter() {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        java.awt.Paint before = g.getPaint();
        java.awt.Stroke stroke = g.getStroke();
        UiTheme.paintPanel(g, 0, 0, 200, 100);
        assertSame(before, g.getPaint());
        assertSame(stroke, g.getStroke());
        g.dispose();
        Color border = new Color(image.getRGB(1, 50));
        assertTrue(border.getRed() > 180 && border.getRed() > border.getBlue());
        Color center = new Color(image.getRGB(100, 50));
        assertTrue(center.getBlue() > center.getRed() && center.getRed() < 90);
    }

    @Test
    public void textHelpersShouldNotThrowWithEmptyOrNullText() {
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        Rectangle area = new Rectangle(0, 0, 100, 50);
        assertDoesNotThrow(() -> UiTheme.drawCenteredText(g, null, area, Color.WHITE));
        assertDoesNotThrow(() -> UiTheme.drawCenteredText(g, "", area, Color.WHITE));
        assertDoesNotThrow(() -> UiTheme.drawShadowedText(g, null, 0, 0, Color.WHITE));
        UiTheme.drawCenteredText(g, "OK", area, Color.WHITE);
        g.dispose();
        boolean painted = false;
        for (int x = 0; x < 100 && !painted; x++) {
            for (int y = 0; y < 50; y++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    painted = true;
                    break;
                }
            }
        }
        assertTrue(painted);
    }
}
