package presentation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.BlueAuraShot;
import domain.DOPOSoccerException;
import domain.ElectrocutedEffect;
import domain.GameCharacter;
import domain.GameMode;
import domain.LightningShot;
import domain.Match;
import domain.MatchConfig;
import domain.Player;
import domain.PlayerProfile;
import domain.PowerBar;
import domain.Side;
import domain.UnconsciousEffect;

/**
 * Pruebas de la eleccion de animacion segun el estado del jugador.
 */
public class PlayerAnimationTest {

    private static final double DT = 1.0 / 60.0;

    private Match match;
    private Player left;
    private PlayerAnimation animation;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        left = new Player(new PlayerProfile("Uno"),
                new GameCharacter("Corea del Sur", new BlueAuraShot()), true);
        Player right = new Player(new PlayerProfile("Dos"),
                new GameCharacter("Camerun", new LightningShot()), false);
        match = new Match(MatchConfig.byGoals(GameMode.PVP, 1), left, right);
        match.start();
        match.getBall().setPosition(900, 200);
        animation = new PlayerAnimation(Side.LEFT);
    }

    @Test
    public void shouldBeIdleWhenStill() {
        animation.update(match, DT);
        animation.update(match, DT);
        assertEquals(SpriteLoader.IDLE, animation.getAction());
        assertNotNull(animation.currentFrame(left));
    }

    @Test
    public void shouldRunWhenMoving() {
        animation.update(match, DT);
        match.move(Side.LEFT, 1, DT);
        animation.update(match, DT);
        assertEquals(SpriteLoader.RUN, animation.getAction());
    }

    @Test
    public void shouldJumpWhenInTheAir() {
        match.jump(Side.LEFT);
        match.update(0.05);
        animation.update(match, DT);
        assertEquals(SpriteLoader.JUMP, animation.getAction());
    }

    @Test
    public void shouldHeadWhenBallIsAtTheHeadInTheAir() {
        match.jump(Side.LEFT);
        match.update(0.05);
        double headY = left.getY() - left.getHeight() / 2;
        match.getBall().setPosition(left.getX(), headY - 10);
        animation.update(match, DT);
        assertEquals(SpriteLoader.HEADER, animation.getAction());
    }

    @Test
    public void shouldShowWholeKick() {
        left.startKickCooldown();
        animation.update(match, DT);
        assertEquals(SpriteLoader.KICK, animation.getAction());
        assertFalse(animation.isLooping());

        animation.update(match, 0.1);
        assertEquals(SpriteLoader.KICK, animation.getAction());
    }

    @Test
    public void shouldShowPowerWhenAuraIsActivated() {
        left.getPowerBar().update(PowerBar.FILL_SECONDS);
        animation.update(match, DT);
        match.activatePower(Side.LEFT);
        animation.update(match, DT);
        assertEquals(SpriteLoader.POWER, animation.getAction());

        animation.update(match, 0.2);
        assertEquals(SpriteLoader.POWER, animation.getAction());

        animation.update(match, PlayerAnimation.POWER_SECONDS);
        animation.update(match, DT);
        assertEquals(SpriteLoader.IDLE, animation.getAction());
    }

    @Test
    public void shouldShowDefeatWhenStunnedOrUnconscious() {
        left.receiveDamage(1000);
        animation.update(match, DT);
        assertEquals(SpriteLoader.DEFEAT, animation.getAction());

        PlayerAnimation other = new PlayerAnimation(Side.RIGHT);
        match.getRightPlayer().applyEffect(new UnconsciousEffect(3));
        other.update(match, DT);
        assertEquals(SpriteLoader.DEFEAT, other.getAction());
    }

    @Test
    public void electrocutedPlayerCanStillRun() {
        left.applyEffect(new ElectrocutedEffect(3));
        animation.update(match, DT);
        match.move(Side.LEFT, 1, DT);
        animation.update(match, DT);
        assertEquals(SpriteLoader.RUN, animation.getAction());
    }

    @Test
    public void winnerShouldCelebrateAndLoserShowDefeat() {
        match.getScore().score(Side.LEFT);
        match.finishNow();
        PlayerAnimation right = new PlayerAnimation(Side.RIGHT);

        animation.update(match, DT);
        right.update(match, DT);

        assertEquals(SpriteLoader.CELEBRATION, animation.getAction());
        assertEquals(SpriteLoader.DEFEAT, right.getAction());
        assertTrue(right.isFlipped());
        assertFalse(animation.isFlipped());
    }

    @Test
    public void shouldCountTimeInsideTheSameAction() {
        animation.update(match, DT);
        animation.update(match, 0.5);
        assertEquals(SpriteLoader.IDLE, animation.getAction());
        assertEquals(DT + 0.5, animation.getTime(), 0.0001);
    }

    @Test
    public void shouldRejectNullSide() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerAnimation(null));
    }
}
