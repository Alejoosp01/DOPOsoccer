package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScoreTest {

    private Score score;

    @BeforeEach
    public void setUp() {
        score = new Score();
    }

    @Test
    public void shouldStartAtZero() {
        assertEquals(0, score.getLeftGoals());
        assertEquals(0, score.getRightGoals());
        assertEquals(0, score.getTotalGoals());
        assertTrue(score.isTie());
        assertEquals(Winner.NONE, score.leader());
    }

    @Test
    public void shouldScoreEachSide() {
        score.score(Side.LEFT);
        score.score(Side.LEFT);
        score.score(Side.RIGHT);
        assertEquals(2, score.getLeftGoals());
        assertEquals(1, score.getRightGoals());
        assertEquals(2, score.getGoals(Side.LEFT));
        assertEquals(1, score.getGoals(Side.RIGHT));
        assertEquals(3, score.getTotalGoals());
        assertEquals(Winner.LEFT, score.leader());
    }

    @Test
    public void shouldDetectRightLeaderAndTie() {
        score.score(Side.RIGHT);
        assertEquals(Winner.RIGHT, score.leader());
        assertFalse(score.isTie());

        score.score(Side.LEFT);
        assertEquals(Winner.NONE, score.leader());
        assertTrue(score.isTie());
    }

    @Test
    public void shouldResetEverything() {
        score.score(Side.LEFT);
        score.score(Side.RIGHT);
        score.reset();
        assertEquals(0, score.getLeftGoals());
        assertEquals(0, score.getRightGoals());
        assertTrue(score.isTie());
    }
}
