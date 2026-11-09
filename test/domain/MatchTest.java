package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MatchTest {

    private Match match;
    private Player left;
    private Player right;
    private double groundCenter;

    @BeforeEach
    public void setUp() {
        match = TestData.match(TestData.byTime(GameMode.PVP, 1));
        left = match.getLeftPlayer();
        right = match.getRightPlayer();
        groundCenter = match.getField().getPlayerStartY();
    }

    private void scoreInLeftGoal(Match game) {
        game.getBall().setPosition(50, 500);
        game.getBall().setVelocity(0, 0);
        game.update(0.01);
    }

    private void scoreInRightGoal(Match game) {
        game.getBall().setPosition(1230, 500);
        game.getBall().setVelocity(0, 0);
        game.update(0.01);
    }

    @Test
    public void shouldStartReadyAndAtZero() {
        assertEquals(MatchState.READY, match.getState());
        assertEquals(0, match.getScore().getLeftGoals());
        assertEquals(0, match.getScore().getRightGoals());
        assertEquals(60, match.getTimeLeft(), 0.001);
        assertFalse(match.isSuddenDeath());
    }

    @Test
    public void shouldStartWithEmptyBarsAndFullLife() {
        TestData.fillPowerBar(left);
        left.receiveDamage(30);
        match.start();
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(0, left.getPowerBar().getCharge(), 0.001);
        assertEquals(left.getMaxLife(), left.getLife(), 0.001);
        assertEquals(match.getField().getBallStartX(), match.getBall().getX(), 0.001);
        assertEquals(match.getField().getBallStartY(), match.getBall().getY(), 0.001);
    }

    @Test
    public void shouldCreditRivalAndGiveAQuarterOfBarToWhoReceivesTheGoal() {
        match.start();
        scoreInLeftGoal(match);
        assertEquals(1, match.getScore().getRightGoals());
        assertEquals(MatchState.GOAL, match.getState());
        assertEquals(0.25, left.getPowerBar().getCharge(), 0.01);
        assertTrue(right.getPowerBar().getCharge() < 0.01);
    }

    @Test
    public void shouldNotScoreWhenBallIsOnlyPartiallyInsideGoal() {
        match.start();
        match.getBall().setPosition(100, 500);
        match.getBall().setVelocity(0, 0);
        match.update(0.01);
        assertEquals(0, match.getScore().getRightGoals());
    }

    @Test
    public void shouldResetPositionsAfterGoalPause() {
        match.start();
        scoreInRightGoal(match);
        match.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        assertEquals(MatchState.PLAYING, match.getState());
        assertEquals(match.getField().getBallStartX(), match.getBall().getX(), 0.001);
        assertEquals(match.getField().getLeftPlayerStartX(), left.getX(), 0.001);
    }

    @Test
    public void shouldPauseAndResumeWithoutAdvancingTime() {
        match.start();
        match.pause();
        double time = match.getTimeLeft();
        match.update(5);
        assertEquals(time, match.getTimeLeft(), 0.001);
        match.resume();
        assertEquals(MatchState.PLAYING, match.getState());
    }

    @Test
    public void shouldFinishWhenGoalTargetIsReached() {
        Match goals = TestData.match(TestData.byGoals(GameMode.PVP, 2));
        goals.start();
        scoreInRightGoal(goals);
        goals.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        assertEquals(Winner.NONE, goals.getWinner());
        scoreInRightGoal(goals);
        assertEquals(Winner.LEFT, goals.getWinner());
        goals.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        assertTrue(goals.isFinished());
    }

    @Test
    public void shouldFinishByTimeWithLeader() {
        match.start();
        scoreInRightGoal(match);
        match.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        match.update(61);
        assertTrue(match.isFinished());
        assertEquals(Winner.LEFT, match.getWinner());
    }

    @Test
    public void shouldPlaySuddenDeathWhenTimeEndsTied() {
        match.start();
        match.update(61);
        assertTrue(match.isSuddenDeath());
        assertEquals(MatchState.PLAYING, match.getState());
        scoreInLeftGoal(match);
        assertEquals(Winner.RIGHT, match.getWinner());
        match.update(Match.GOAL_PAUSE_SECONDS + 0.1);
        assertTrue(match.isFinished());
    }

    @Test
    public void shouldFinishNowGivingTheWinToTheLeader() {
        match.start();
        scoreInRightGoal(match);
        match.finishNow();
        assertTrue(match.isFinished());
        assertTrue(match.wasEndedEarly());
        assertEquals(Winner.LEFT, match.getWinner());
    }

    @Test
    public void shouldFinishNowWithoutWinnerWhenTied() {
        match.start();
        match.finishNow();
        assertTrue(match.isFinished());
        assertEquals(Winner.NONE, match.getWinner());
    }

    @Test
    public void shouldDamageRivalHitDirectlyWithKickMultiplier() throws DOPOSoccerException {
        match.start();
        for (int i = 0; i < 4; i++) {
            left.getProfile().getStats().increase(StatType.KICK);
        }
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter);

        assertTrue(match.kick(Side.LEFT));

        assertEquals(100 - 14, right.getLife(), 0.001);
    }

    @Test
    public void shouldOnlyPushBallWhenKickDoesNotReachRival() {
        match.start();
        left.setPosition(320, groundCenter);
        right.setPosition(900, groundCenter);
        match.getBall().setPosition(360, groundCenter);

        assertTrue(match.kick(Side.LEFT));

        assertEquals(100, right.getLife(), 0.001);
        assertTrue(match.getBall().getVelocityX() > 0);
    }

    @Test
    public void shouldNotDamageRivalJumpingOverTheFoot() {
        match.start();
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter - 130);

        assertFalse(match.kick(Side.LEFT));

        assertEquals(100, right.getLife(), 0.001);
    }

    @Test
    public void shouldStunRivalWhenKicksTakeAllItsLife() {
        match.start();
        left.setPosition(320, groundCenter);
        right.setPosition(400, groundCenter);
        for (int i = 0; i < 10; i++) {
            match.kick(Side.LEFT);
            left.update(Player.KICK_COOLDOWN, match.getField());
            left.setPosition(320, groundCenter);
            right.setPosition(400, groundCenter);
        }
        assertEquals(0, right.getLife(), 0.001);
        assertTrue(right.isStunned());
    }

    @Test
    public void shouldActivatePowerOnlyWithFullBar() {
        match.start();
        assertFalse(match.activatePower(Side.LEFT));
        TestData.fillPowerBar(left);
        assertTrue(match.activatePower(Side.LEFT));
        assertTrue(left.hasAura());
    }

    @Test
    public void shouldFirePowerShotWhenPlayerWithAuraTouchesBall() {
        match.start();
        TestData.fillPowerBar(left);
        match.activatePower(Side.LEFT);
        left.setPosition(320, groundCenter);
        match.getBall().setPosition(360, groundCenter);
        match.getBall().setVelocity(0, 0);

        match.update(0.001);

        assertFalse(left.hasAura());
        assertTrue(match.getBall().hasActiveShot());
        assertSame(left, match.getBall().getShotOwner());
        assertTrue(match.getBall().getVelocityX() > 0);
    }

    @Test
    public void shouldDamageAndAffectRivalHitByPowerShot() {
        match.start();
        right.setPosition(900, groundCenter);
        match.getBall().launchShot(new LightningShot(), left);
        match.getBall().setPosition(900, groundCenter);
        match.getBall().setVelocity(0, 0);

        match.update(0.001);

        assertEquals(100 - 60, right.getLife(), 0.001);
        assertTrue(right.getEffect() instanceof ElectrocutedEffect);
        assertFalse(match.getBall().hasActiveShot());
    }

    @Test
    public void shouldCountGoalWithoutDamageWhenPowerShotEntersGoal() {
        match.start();
        match.getBall().launchShot(new LightningShot(), left);
        scoreInRightGoal(match);

        assertEquals(1, match.getScore().getLeftGoals());
        assertEquals(100, right.getLife(), 0.001);
        assertNull(right.getEffect());
        assertFalse(match.getBall().hasActiveShot());
    }

    @Test
    public void shouldTurnOffShotWhenItsOwnerTouchesIt() {
        match.start();
        left.setPosition(320, groundCenter);
        match.getBall().launchShot(new LightningShot(), left);
        match.getBall().setPosition(340, groundCenter);
        match.getBall().setVelocity(0, 0);

        match.update(0.001);

        assertFalse(match.getBall().hasActiveShot());
        assertEquals(100, left.getLife(), 0.001);
    }

    @Test
    public void shouldPushRivalTowardItsGoalWithBlueAura() {
        match.start();
        right.setPosition(900, groundCenter);
        match.getBall().launchShot(new BlueAuraShot(), left);
        match.getBall().setPosition(900, groundCenter);
        match.getBall().setVelocity(0, 0);

        match.update(0.001);

        assertTrue(right.getEffect() instanceof PushEffect);
        assertTrue(right.getVelocityX() > 0);
    }

    @Test
    public void shouldNotMoveWhenPaused() {
        match.start();
        match.pause();
        assertFalse(match.move(Side.LEFT, 1, 0.1));
        assertFalse(match.kick(Side.LEFT));
    }
}
