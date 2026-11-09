package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MatchTest {

    private GameCharacter character;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        character = new GameCharacter("Test", new Country("Test", new FreezePower()), 3, 3, 3);
    }

    private Match createMatch(MatchConfig config) {
        Player left = new Player(character, true, 0, 0);
        Player right = new Player(character, false, 0, 0);
        return new Match(config, left, right);
    }

    private void scoreInLeftGoal(Match match) {
        match.getBall().setPosition(50, 500);
        match.getBall().setVelocity(0, 0);
        match.update(0.01);
    }

    private void scoreInRightGoal(Match match) {
        match.getBall().setPosition(1230, 500);
        match.getBall().setVelocity(0, 0);
        match.update(0.01);
    }

    @Test
    public void shouldStartReadyAndAtZero() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        assertEquals(MatchState.READY, match.getState());
        assertEquals(0, match.getScore().getLeftGoals());
        assertEquals(0, match.getScore().getRightGoals());
        assertEquals(Winner.NONE, match.getWinner());
        assertEquals(60, match.getTimeLeft(), 0.001);
        assertFalse(match.isGoldenGoal());
    }

    @Test
    public void shouldStartPlayingWithPositionsReset() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(match.getField().getBallStartX(), match.getBall().getX(), 0.001);
        assertEquals(match.getField().getBallStartY(), match.getBall().getY(), 0.001);
        assertEquals(match.getField().getLeftPlayerStartX(), match.getLeftPlayer().getX(), 0.001);
        assertEquals(match.getField().getRightPlayerStartX(), match.getRightPlayer().getX(), 0.001);
    }

    @Test
    public void shouldNotUpdateBeforeStart() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.update(5);
        assertEquals(MatchState.READY, match.getState());
        assertEquals(60, match.getTimeLeft(), 0.001);
    }

    @Test
    public void shouldCreditRivalWhenBallEntersLeftGoal() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        scoreInLeftGoal(match);
        assertEquals(0, match.getScore().getLeftGoals());
        assertEquals(1, match.getScore().getRightGoals());
        assertEquals(MatchState.GOAL, match.getState());
        assertEquals(Winner.NONE, match.getWinner());
    }

    @Test
    public void shouldCreditLeftWhenBallEntersRightGoal() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        scoreInRightGoal(match);
        assertEquals(1, match.getScore().getLeftGoals());
        assertEquals(0, match.getScore().getRightGoals());
    }

    @Test
    public void shouldNotScoreWhenBallIsOnlyPartiallyInsideGoal() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        match.getBall().setPosition(95, 500);
        match.update(0.01);
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(0, match.getScore().getTotalGoals());
    }

    @Test
    public void shouldResetAfterGoalPause() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        scoreInLeftGoal(match);
        match.update(0.5);
        assertEquals(MatchState.GOAL, match.getState());
        match.update(0.6);
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(match.getField().getBallStartX(), match.getBall().getX(), 0.001);
        assertEquals(match.getField().getLeftPlayerStartX(), match.getLeftPlayer().getX(), 0.001);
        assertEquals(1, match.getScore().getRightGoals());
    }

    @Test
    public void shouldPauseAndResumeWithoutAdvancing() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        match.update(0.5);
        double timeBeforePause = match.getTimeLeft();

        match.pause();
        assertEquals(MatchState.PAUSED, match.getState());
        match.update(5);
        assertEquals(timeBeforePause, match.getTimeLeft(), 0.001);

        match.resume();
        assertEquals(MatchState.PLAYING, match.getState());
    }

    @Test
    public void shouldFinishWhenGoalTargetIsReached() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.goals(GameMode.PVP, 3));
        match.start();
        for (int i = 0; i < 2; i++) {
            scoreInLeftGoal(match);
            assertEquals(Winner.NONE, match.getWinner());
            match.update(1.1);
            assertEquals(MatchState.PLAYING, match.getState());
        }
        scoreInLeftGoal(match);
        assertEquals(3, match.getScore().getRightGoals());
        assertEquals(Winner.RIGHT, match.getWinner());
        match.update(1.1);
        assertEquals(MatchState.FINISHED, match.getState());
    }

    @Test
    public void shouldFinishSurvivalWhenRivalRunsOutOfBalls() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.survival(1));
        match.start();
        assertEquals(1, match.getRemainingBalls(Side.LEFT));
        assertEquals(1, match.getRemainingBalls(Side.RIGHT));

        scoreInLeftGoal(match);
        assertEquals(0, match.getRemainingBalls(Side.LEFT));
        assertEquals(1, match.getRemainingBalls(Side.RIGHT));
        assertEquals(Winner.RIGHT, match.getWinner());

        match.update(1.1);
        assertEquals(MatchState.FINISHED, match.getState());
    }

    @Test
    public void shouldActivateGoldenGoalWhenTimeExpiresTied() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP, 1));
        match.start();
        match.update(1.1);
        assertEquals(MatchState.PLAYING, match.getState());
        assertTrue(match.isGoldenGoal());
        assertEquals(0, match.getTimeLeft(), 0.001);

        scoreInRightGoal(match);
        assertEquals(Winner.LEFT, match.getWinner());
        match.update(1.1);
        assertEquals(MatchState.FINISHED, match.getState());
        assertEquals(Winner.LEFT, match.getWinner());
    }

    @Test
    public void shouldFinishByTimeWithLeader() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP, 1));
        match.start();
        scoreInRightGoal(match);
        assertEquals(Winner.NONE, match.getWinner());

        match.update(1.1);
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(1, match.getScore().getLeftGoals());

        match.update(1.1);
        assertEquals(MatchState.FINISHED, match.getState());
        assertEquals(Winner.LEFT, match.getWinner());
    }

    @Test
    public void shouldMovePlayerFromMatch() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        double startX = left.getX();
        assertTrue(match.move(Side.LEFT, 1, 0.5));
        assertEquals(startX + left.getMoveSpeed() * 0.5, left.getX(), 0.001);
    }

    @Test
    public void shouldNotMoveWhenPaused() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        double startX = match.getLeftPlayer().getX();
        match.pause();
        assertFalse(match.move(Side.LEFT, 1, 1));
        assertEquals(startX, match.getLeftPlayer().getX(), 0.001);
    }

    @Test
    public void shouldJumpFromMatch() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        assertTrue(match.jump(Side.LEFT));
        assertTrue(match.getLeftPlayer().getVelocityY() < 0);
    }

    @Test
    public void shouldPushBallWhenPlayerTouchesIt() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        match.getBall().setPosition(left.getX() + 50, left.getY());
        match.getBall().setVelocity(0, 0);

        match.update(0.02);

        assertTrue(match.getBall().getVelocityX() > 0);
        assertTrue(match.getBall().getX() > left.getX());
    }

    @Test
    public void shouldKeepPlayersApart() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        left.setPosition(640, match.getField().getPlayerStartY());
        right.setPosition(640, match.getField().getPlayerStartY());

        match.update(0.02);

        assertFalse(left.intersects(right));
    }

    @Test
    public void shouldStunRivalHitDirectlyWithoutBall() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        double groundCenter = match.getField().getPlayerStartY();
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter);
        double ballX = match.getBall().getX();

        assertTrue(match.kick(Side.LEFT));

        assertTrue(right.isStunned());
        assertTrue(right.getX() > 400);
        assertEquals(ballX, match.getBall().getX(), 0.001);
        assertEquals(0, match.getBall().getVelocityX(), 0.001);
    }

    @Test
    public void shouldNotStunRivalJumpingOverTheFoot() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        double groundCenter = match.getField().getPlayerStartY();
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter - 130);

        assertFalse(match.kick(Side.LEFT));

        assertFalse(right.isStunned());
    }

    @Test
    public void shouldNotStunRivalOutOfReach() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        double groundCenter = match.getField().getPlayerStartY();
        left.setPosition(320, groundCenter);
        right.setPosition(470, groundCenter);

        assertFalse(match.kick(Side.LEFT));

        assertFalse(right.isStunned());
        assertEquals(470, right.getX(), 0.001);
    }

    @Test
    public void shouldNotStunWithKickWhileKickIsCoolingDown() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        double groundCenter = match.getField().getPlayerStartY();
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter);
        left.startKickCooldown();

        assertFalse(match.kick(Side.LEFT));

        assertFalse(right.isStunned());
    }

    @Test
    public void shouldStunRivalHitDirectlyWhileKickingTheBall() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        Player left = match.getLeftPlayer();
        Player right = match.getRightPlayer();
        double groundCenter = match.getField().getPlayerStartY();
        match.getBall().setPosition(370, groundCenter);
        left.setPosition(320, groundCenter);
        right.setPosition(420, groundCenter);

        assertTrue(match.kick(Side.LEFT));

        assertTrue(right.isStunned());
        assertTrue(right.getX() > 420);
    }

    @Test
    public void shouldArmPowerFromMatchWhenReady() throws DOPOSoccerException {
        Match match = createMatch(MatchConfig.time(GameMode.PVP));
        match.start();
        for (int i = 0; i < 100; i++) {
            match.getLeftPlayer().update(0.1, match.getField());
        }
        assertTrue(match.armPower(Side.LEFT));
        assertTrue(match.getLeftPlayer().isPowerArmed());
    }
}
