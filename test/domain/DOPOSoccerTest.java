package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DOPOSoccerTest {

    private GameCharacter korea;
    private GameCharacter cameroon;
    private PlayerProfile player1;
    private PlayerProfile player2;

    @BeforeEach
    public void setUp() {
        korea = TestData.character("Corea del Sur", new BlueAuraShot());
        cameroon = TestData.character("Camerun", new LightningShot());
        player1 = TestData.profile("Jugador 1");
        player2 = TestData.profile("Jugador 2");
    }

    private DOPOSoccer game(MatchConfig config) {
        return new DOPOSoccer(config, korea, cameroon, player1, player2);
    }

    private void scoreInRightGoal(DOPOSoccer game) {
        game.getMatch().getBall().setPosition(1230, 500);
        game.getMatch().getBall().setVelocity(0, 0);
        game.update(0.01);
    }

    @Test
    public void shouldCreateHumanControllersForPvp() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 1));
        assertTrue(game.getLeftController() instanceof HumanController);
        assertTrue(game.getRightController() instanceof HumanController);
    }

    @Test
    public void shouldCreateHumanAndMachineForPvm() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVM, 1));
        assertTrue(game.getLeftController() instanceof HumanController);
        assertTrue(game.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldUseTheChosenCharactersAndProfiles() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 1));
        assertSame(korea, game.getLeftPlayer().getCharacter());
        assertSame(cameroon, game.getRightPlayer().getCharacter());
        assertSame(player1, game.getLeftPlayer().getProfile());
        assertSame(player2, game.getRightPlayer().getProfile());
    }

    @Test
    public void shouldStartPlaying() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 1));
        game.start();
        assertTrue(game.isStarted());
        assertEquals(MatchState.PLAYING, game.getMatch().getState());
    }

    @Test
    public void shouldPauseAndResume() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 1));
        game.start();
        assertTrue(game.pause());
        double time = game.getMatch().getTimeLeft();
        game.update(1);
        assertEquals(time, game.getMatch().getTimeLeft(), 0.001);
        assertTrue(game.resume());
        game.update(0.5);
        assertTrue(game.getMatch().getTimeLeft() < time);
    }

    @Test
    public void shouldGiveHundredToWinnerAndTwentyFiveToLoser() {
        DOPOSoccer game = game(TestData.byGoals(GameMode.PVP, 1));
        game.start();
        scoreInRightGoal(game);
        game.update(Match.GOAL_PAUSE_SECONDS + 0.1);

        assertTrue(game.isFinished());
        assertEquals(Winner.LEFT, game.getWinner());
        assertEquals(100, game.getPointsEarned(Side.LEFT));
        assertEquals(25, game.getPointsEarned(Side.RIGHT));
        assertEquals(100, player1.getPoints());
        assertEquals(25, player2.getPoints());
    }

    @Test
    public void shouldGivePointsOnlyOnce() {
        DOPOSoccer game = game(TestData.byGoals(GameMode.PVP, 1));
        game.start();
        scoreInRightGoal(game);
        game.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        game.update(1);
        game.endMatch();
        assertEquals(100, player1.getPoints());
    }

    @Test
    public void shouldEndMatchAtAnyMomentAndGivePointsToLeader() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 2));
        game.start();
        scoreInRightGoal(game);
        game.endMatch();
        assertTrue(game.isFinished());
        assertEquals(100, player1.getPoints());
        assertEquals(25, player2.getPoints());
    }

    @Test
    public void shouldNotGivePointsWhenEndedTied() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 2));
        game.start();
        game.endMatch();
        assertTrue(game.isFinished());
        assertEquals(Winner.NONE, game.getWinner());
        assertEquals(0, player1.getPoints());
        assertEquals(0, player2.getPoints());
    }

    @Test
    public void shouldNotPauseAfterFinished() {
        DOPOSoccer game = game(TestData.byTime(GameMode.PVP, 1));
        game.start();
        game.endMatch();
        assertFalse(game.pause());
    }
}
