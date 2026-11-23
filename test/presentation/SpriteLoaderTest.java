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
        assertEquals("/sprites/characters/korea/run",
                SpriteLoader.characterPath(korea, SpriteLoader.RUN));
    }

    @Test
    public void everyCharacterShouldHaveEveryAnimation() throws DOPOSoccerException {
        String[] actions = {SpriteLoader.IDLE, SpriteLoader.RUN, SpriteLoader.JUMP, SpriteLoader.KICK,
            SpriteLoader.HEADER, SpriteLoader.POWER, SpriteLoader.CELEBRATION, SpriteLoader.DEFEAT};
        for (GameCharacter character : CharacterCatalog.createCharacters()) {
            for (String action : actions) {
                BufferedImage[] frames = SpriteLoader.characterFrames(character, action);
                assertTrue(frames.length >= 4, character.getName() + " " + action);
                assertEquals(SpriteLoader.CELL_SIZE, frames[0].getWidth());
                assertEquals(SpriteLoader.CELL_SIZE, frames[0].getHeight());
            }
        }
    }

    @Test
    public void shouldLoadNumberedFramesInOrder() throws DOPOSoccerException {
        GameCharacter korea = new GameCharacter("Corea del Sur", new BlueAuraShot());
        assertEquals(4, SpriteLoader.characterFrames(korea, SpriteLoader.IDLE).length);
        assertEquals(6, SpriteLoader.characterFrames(korea, SpriteLoader.RUN).length);
        assertSame(SpriteLoader.characterFrames(korea, SpriteLoader.RUN),
                SpriteLoader.characterFrames(korea, SpriteLoader.RUN));
        assertEquals(0, SpriteLoader.sequence("/no/existe").length);
    }

    @Test
    public void frameOnceShouldStopOnLastFrame() {
        BufferedImage[] frames = SpriteLoader.split(strip(5, 8), 8);

        assertSame(frames[0], SpriteLoader.frameOnce(frames, 0, 20));
        assertSame(frames[2], SpriteLoader.frameOnce(frames, 0.1, 20));
        assertSame(frames[4], SpriteLoader.frameOnce(frames, 3, 20));
    }

    @Test
    public void shouldDrawCharacterWithFeetOnTheGround() {
        BufferedImage canvas = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        BufferedImage frame = new BufferedImage(SpriteLoader.CELL_SIZE, SpriteLoader.CELL_SIZE,
                BufferedImage.TYPE_INT_ARGB);
        int red = Color.RED.getRGB();
        for (int x = SpriteLoader.FEET_X - 4; x <= SpriteLoader.FEET_X + 4; x++) {
            for (int y = SpriteLoader.FEET_Y - 8; y < SpriteLoader.FEET_Y; y++) {
                frame.setRGB(x, y, red);
            }
        }
        java.awt.Graphics2D g2 = canvas.createGraphics();

        assertTrue(SpriteLoader.drawCharacter(g2, frame, 100, 100, 70, 120, false));
        g2.dispose();

        assertEquals(red, canvas.getRGB(135, 218));
        assertFalse(SpriteLoader.drawCharacter(g2, null, 100, 100, 70, 120, false));
    }

    @Test
    public void shouldUseFallbackFontWhenMissing() {
        Font fallback = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        assertSame(fallback, SpriteLoader.font("/fonts/no-existe.ttf", 20, fallback));
    }
}
