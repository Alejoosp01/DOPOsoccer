package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HitboxTest {

    @Test
    public void shouldExposeEdgesAndCenter() {
        Hitbox box = new Hitbox(10, 20, 30, 40);
        assertEquals(10, box.getX(), 0.001);
        assertEquals(20, box.getY(), 0.001);
        assertEquals(40, box.getMaxX(), 0.001);
        assertEquals(60, box.getMaxY(), 0.001);
        assertEquals(25, box.getCenterX(), 0.001);
        assertEquals(40, box.getCenterY(), 0.001);
    }

    @Test
    public void shouldDetectOverlap() {
        Hitbox box = new Hitbox(0, 0, 50, 50);
        assertTrue(box.intersects(new Hitbox(40, 40, 20, 20)));
        assertTrue(new Hitbox(40, 40, 20, 20).intersects(box));
    }

    @Test
    public void shouldNotIntersectWhenSeparatedOrOnlyTouching() {
        Hitbox box = new Hitbox(0, 0, 50, 50);
        assertFalse(box.intersects(new Hitbox(60, 0, 10, 10)));
        assertFalse(box.intersects(new Hitbox(0, 70, 10, 10)));
        assertFalse(box.intersects(new Hitbox(50, 0, 10, 10)));
        assertFalse(box.intersects(null));
    }
}
