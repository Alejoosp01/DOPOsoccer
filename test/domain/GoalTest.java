package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoalTest {

    private Goal goal;

    @BeforeEach
    public void setUp() {
        goal = new Goal(0, 360, 100, 280);
    }

    @Test
    public void shouldContainBallFullyInside() {
        assertTrue(goal.contains(50, 500, 18));
    }

    @Test
    public void shouldNotContainBallPartiallyInside() {
        assertFalse(goal.contains(95, 500, 18));
    }

    @Test
    public void shouldNotContainBallAboveGoal() {
        assertFalse(goal.contains(50, 370, 18));
    }

    @Test
    public void shouldNotContainBallBelowGoal() {
        assertFalse(goal.contains(50, 630, 18));
    }

    @Test
    public void shouldContainBallUsingItsOwnRadius() {
        Ball inside = new Ball(50, 500, 18);
        Ball partial = new Ball(95, 500, 18);
        assertTrue(goal.contains(inside));
        assertFalse(goal.contains(partial));
    }

    @Test
    public void shouldExposeItsRectangle() {
        assertEquals(0, goal.getX(), 0.001);
        assertEquals(360, goal.getY(), 0.001);
        assertEquals(100, goal.getWidth(), 0.001);
        assertEquals(280, goal.getHeight(), 0.001);
        assertEquals(100, goal.getRight(), 0.001);
        assertEquals(640, goal.getBottom(), 0.001);
    }
}
