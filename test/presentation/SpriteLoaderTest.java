package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import domain.BlueAuraShot;
import domain.CharacterCatalog;
import domain.DOPOSoccerException;
import domain.GameCharacter;

/**
 * Pruebas del cargador de sprites; no necesitan pantalla ni imagenes reales.
 */
public class SpriteLoaderTest {

    private BufferedImage strip(int frames, int size) {
        BufferedImage image = new BufferedImage(frames * size, size, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < frames; i++) {
            image.setRGB(i * size, 0, new Color(10 * (i + 1), 0, 0).getRGB());
        }
        return image;
    }

    @Test
    public void shouldReturnNullForMissingImage() {
        assertNull(SpriteLoader.load("/no/existe.png"));
        assertNull(SpriteLoader.load(null));
        assertFalse(SpriteLoader.exists("/no/existe.png"));
    }

    @Test
    public void shouldReturnNoFramesForMissingStrip() {
        assertEquals(0, SpriteLoader.frames("/no/existe.png").length);
    }

    @Test
    public void shouldSplitStripIntoFrames() {
        BufferedImage[] frames = SpriteLoader.split(strip(4, 32), 32);

        assertEquals(4, frames.length);
        assertEquals(32, frames[2].getWidth());
        assertEquals(new Color(30, 0, 0).getRGB(), frames[2].getRGB(0, 0));
    }

    @Test
    public void shouldPickFrameByTime() {
        BufferedImage[] frames = SpriteLoader.split(strip(4, 16), 16);

        assertSame(frames[0], SpriteLoader.frameAt(frames, 0, 8));
        assertSame(frames[2], SpriteLoader.frameAt(frames, 0.25, 8));
        assertSame(frames[1], SpriteLoader.frameAt(frames, 0.625, 8));
        assertNull(SpriteLoader.frameAt(new BufferedImage[0], 1, 8));
    }

    @Test
    public void shouldFlipImageHorizontally() {
        BufferedImage image = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
        int red = Color.RED.getRGB();
        image.setRGB(0, 0, red);

        BufferedImage flipped = SpriteLoader.flipHorizontally(image);

        assertEquals(red, flipped.getRGB(2, 0));
        assertNotEquals(red, flipped.getRGB(0, 0));
    }

    @Test
    public void shouldMapEveryCatalogCharacterToAFolder() throws DOPOSoccerException {
        for (GameCharacter character : CharacterCatalog.createCharacters()) {
            assertNotNull(SpriteLoader.characterFolder(character));
        }
        GameCharacter korea = new GameCharacter("Corea del Sur", new BlueAuraShot());
        assertEquals("/sprites/characters/korea/walk.png",
                SpriteLoader.characterPath(korea, SpriteLoader.WALK));
    }

    @Test
    public void shouldUseFallbackFontWhenMissing() {
        Font fallback = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        assertSame(fallback, SpriteLoader.font("/fonts/no-existe.ttf", 20, fallback));
    }
}
