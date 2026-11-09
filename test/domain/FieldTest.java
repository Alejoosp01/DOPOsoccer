package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FieldTest {

    private Field field;

    @BeforeEach
    public void setUp() {
        field = new Field();
    }

    @Test
    public void shouldUseDefaultDimensions() {
        assertEquals(1280, field.getWidth(), 0.001);
        assertEquals(720, field.getHeight(), 0.001);
        assertEquals(640, field.getGroundY(), 0.001);
    }

    @Test
    public void shouldPlaceGoalsOnBothSides() {
        Goal left = field.getLeftGoal();
        Goal right = field.getRightGoal();
        assertEquals(0, left.getX(), 0.001);
        assertEquals(360, left.getY(), 0.001);
        assertEquals(100, left.getWidth(), 0.001);
        assertEquals(280, left.getHeight(), 0.001);
        assertEquals(1180, right.getX(), 0.001);
        assertEquals(1280, right.getRight(), 0.001);
        assertEquals(100, field.getLeftGoalLineX(), 0.001);
        assertEquals(1180, field.getRightGoalLineX(), 0.001);
    }

    @Test
    public void shouldReturnStartPositions() {
        assertEquals(640, field.getBallStartX(), 0.001);
        assertEquals(360, field.getBallStartY(), 0.001);
        assertEquals(320, field.getLeftPlayerStartX(), 0.001);
        assertEquals(960, field.getRightPlayerStartX(), 0.001);
        assertEquals(580, field.getPlayerStartY(), 0.001);
    }

    @Test
    public void shouldDetectGoalMouthByBallHeight() {
        assertTrue(field.isInsideGoalMouth(500, 18));
        assertFalse(field.isInsideGoalMouth(370, 18));
        assertFalse(field.isInsideGoalMouth(630, 18));
    }

    @Test
    public void shouldRejectInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Field(0, 720, 640, 100, 280));
        assertThrows(IllegalArgumentException.class, () -> new Field(1280, 720, 800, 100, 280));
        assertThrows(IllegalArgumentException.class, () -> new Field(1280, 720, 640, 700, 280));
        assertThrows(IllegalArgumentException.class, () -> new Field(1280, 720, 640, 100, 700));
    }
}
