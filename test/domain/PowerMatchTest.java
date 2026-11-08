package domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PowerMatchTest {

    private Match createMatch(Power leftPower, Power rightPower) throws POOBSoccerException {
        GameCharacter leftCharacter = new GameCharacter(
                "Left", new Country("Left", leftPower), 3, 3, 3);
        GameCharacter rightCharacter = new GameCharacter(
                "Right", new Country("Right", rightPower), 3, 3, 3);
        Player left = new Player(leftCharacter, true, 0, 0);
        Player right = new Player(rightCharacter, false, 0, 0);
        return new Match(MatchConfig.time(GameMode.PVP), left, right);
    }

    private void readyPower(Match match, Side side) {
        Player player = match.getPlayer(side);
        for (int i = 0; i < 100; i++) {
            player.update(0.1, match.getField());
        }
        assertTrue(player.isPowerReady());
        assertTrue(match.armPower(side));
    }

    private void placeBallInFront(Match match) {
        Player left = match.getLeftPlayer();
        match.getBall().setPosition(left.getX() + 50, left.getY());
        match.getBall().setVelocity(0, 0);
    }

    @Test
    public void shouldActivateFreezeOnNextKick() throws POOBSoccerException {
        Match match = createMatch(new FreezePower(), new PushPower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);

        assertTrue(match.kick(Side.LEFT));

        assertTrue(match.getRightPlayer().isFrozen());
        assertFalse(match.getLeftPlayer().isPowerArmed());
        assertEquals(0, match.getLeftPlayer().getPowerCharge(), 0.001);
    }

    @Test
    public void shouldActivateShrinkOnBodyTouch() throws POOBSoccerException {
        Match match = createMatch(new ShrinkPower(), new PushPower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);

        match.update(0.01);

        assertTrue(match.getRightPlayer().isShrunk());
        assertEquals(Player.WIDTH * Player.SHRUNK_SCALE,
                match.getRightPlayer().getWidth(), 0.001);
        assertFalse(match.getLeftPlayer().isPowerArmed());
    }

    @Test
    public void shouldConfuseOpponentAndInvertMovement() throws POOBSoccerException {
        Match match = createMatch(new ConfusionPower(), new PushPower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);
        assertTrue(match.kick(Side.LEFT));

        assertTrue(match.getRightPlayer().isConfused());
        double startX = match.getRightPlayer().getX();
        assertTrue(match.move(Side.RIGHT, 1, 0.2));
        assertTrue(match.getRightPlayer().getX() < startX);
    }

    @Test
    public void shouldChargePushBallAndStunRivalOnTouch() throws POOBSoccerException {
        Match match = createMatch(new PushPower(), new FreezePower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);
        assertTrue(match.kick(Side.LEFT));

        assertTrue(match.getBall().isPushCharged());
        Player right = match.getRightPlayer();
        right.setPosition(500, match.getField().getPlayerStartY());

        match.update(0.2);

        assertTrue(right.isStunned());
        assertFalse(match.getBall().isPushCharged());
        assertTrue(right.getX() > 500);
    }

    @Test
    public void shouldLetIllusionBallPassThroughRival() throws POOBSoccerException {
        Match match = createMatch(new IllusionPower(), new FreezePower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);
        assertTrue(match.kick(Side.LEFT));

        Ball ball = match.getBall();
        assertTrue(ball.isIllusionActive());
        assertEquals(IllusionPower.DECOYS, ball.getDecoyCount());
        assertEquals(IllusionPower.SHOT_SPEED, ball.getVelocityX(), 0.001);

        Player right = match.getRightPlayer();
        ball.setPosition(right.getX(), right.getY());
        ball.setVelocity(0, 0);
        double positionX = ball.getX();

        match.update(0.01);

        assertEquals(positionX, ball.getX(), 0.001);
        assertTrue(ball.isIllusionActive());
    }

    @Test
    public void shouldTouchBallAfterIllusionExpires() throws POOBSoccerException {
        Match match = createMatch(new IllusionPower(), new FreezePower());
        match.start();
        readyPower(match, Side.LEFT);
        placeBallInFront(match);
        assertTrue(match.kick(Side.LEFT));

        Ball ball = match.getBall();
        Player right = match.getRightPlayer();
        ball.setPosition(right.getX(), right.getY());
        ball.setVelocity(0, 0);
        double positionX = ball.getX();

        for (int i = 0; i < 200; i++) {
            match.update(0.01);
        }

        assertFalse(ball.isIllusionActive());
        assertNotEquals(positionX, ball.getX(), 0.001);
    }

    @Test
    public void shouldNotActivatePowerUntilBallIsHit() throws POOBSoccerException {
        Match match = createMatch(new FreezePower(), new PushPower());
        match.start();
        readyPower(match, Side.LEFT);

        match.update(0.5);

        assertTrue(match.getLeftPlayer().isPowerArmed());
        assertFalse(match.getRightPlayer().isFrozen());
    }

    @Test
    public void shouldKeepPowerArmedWhenKickMisses() throws POOBSoccerException {
        Match match = createMatch(new FreezePower(), new PushPower());
        match.start();
        readyPower(match, Side.LEFT);

        assertFalse(match.kick(Side.LEFT));

        assertTrue(match.getLeftPlayer().isPowerArmed());
        assertFalse(match.getRightPlayer().isFrozen());
    }

    @Test
    public void shouldDeclareWhichPowersReactToOpponentTouch() {
        assertTrue(new PushPower().reactsToOpponentTouch());
        assertFalse(new FreezePower().reactsToOpponentTouch());
        assertFalse(new ShrinkPower().reactsToOpponentTouch());
        assertFalse(new ConfusionPower().reactsToOpponentTouch());
        assertFalse(new IllusionPower().reactsToOpponentTouch());
    }
}
