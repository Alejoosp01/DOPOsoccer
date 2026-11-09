package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.DOPOSoccer;
import domain.DOPOSoccerException;
import domain.GameCharacter;
import domain.GameMode;
import domain.HumanController;
import domain.LightningShot;
import domain.MatchConfig;
import domain.PlayerProfile;

/**
 * Pruebas de los enlaces de teclado del panel de juego.
 * Se ejecutan sin mostrar la ventana, asi que no necesitan pantalla.
 */
public class GamePanelInputTest {

    private GamePanel panel;
    private DOPOSoccer session;

    @BeforeEach
    public void setUp() throws DOPOSoccerException {
        GameCharacter left = new GameCharacter("Left", new LightningShot());
        GameCharacter right = new GameCharacter("Right", new LightningShot());
        session = new DOPOSoccer(MatchConfig.byTime(GameMode.PVP, 1), left, right,
                new PlayerProfile("Jugador 1"), new PlayerProfile("Jugador 2"));
        panel = new GamePanel(session, () -> { }, () -> { }, Resolution.MEDIUM);
    }

    @AfterEach
    public void tearDown() {
        if (panel != null) {
            panel.removeNotify();
        }
    }

    private Object actionFor(KeyStroke stroke) {
        return panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(stroke);
    }

    private KeyEvent controlPressed(int location) {
        return new KeyEvent(panel, KeyEvent.KEY_PRESSED, 0, InputEvent.CTRL_DOWN_MASK,
                KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED, location);
    }

    @Test
    public void shouldRequestRightPowerWithRightControl() {
        HumanController right = (HumanController) session.getRightController();

        panel.handleRightControl(controlPressed(KeyEvent.KEY_LOCATION_RIGHT));

        assertTrue(right.isPowerRequested());
    }

    @Test
    public void shouldIgnoreLeftControl() {
        HumanController left = (HumanController) session.getLeftController();
        HumanController right = (HumanController) session.getRightController();

        panel.handleRightControl(controlPressed(KeyEvent.KEY_LOCATION_LEFT));

        assertFalse(right.isPowerRequested());
        assertFalse(left.isPowerRequested());
    }

    @Test
    public void shouldNotBindControlAsKeyStroke() {
        KeyStroke stroke = KeyStroke.getKeyStroke(
                KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK, false);

        assertNull(actionFor(stroke));
    }

    @Test
    public void shouldRegisterKeyListenerForRightControl() {
        HumanController right = (HumanController) session.getRightController();

        panel.getKeyListeners()[0].keyPressed(controlPressed(KeyEvent.KEY_LOCATION_RIGHT));

        assertTrue(right.isPowerRequested());
    }

    @Test
    public void shouldKeepArrowsBoundWhileControlIsHeld() {
        KeyStroke leftWithControl = KeyStroke.getKeyStroke(
                KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK, false);

        assertEquals("rightLeft.ctrl.pressed", actionFor(leftWithControl));
    }

    @Test
    public void clearInputShouldClearDirectionsAndPendingRequests() {
        HumanController left = (HumanController) session.getLeftController();
        HumanController right = (HumanController) session.getRightController();
        left.setDirection(-1);
        right.setDirection(1);
        left.requestJump();
        left.requestKick();
        left.requestPower();
        right.requestJump();
        right.requestKick();
        right.requestPower();

        panel.clearInput();

        assertEquals(0, left.getDirection());
        assertEquals(0, right.getDirection());
        assertFalse(left.isJumpRequested());
        assertFalse(left.isKickRequested());
        assertFalse(left.isPowerRequested());
        assertFalse(right.isJumpRequested());
        assertFalse(right.isKickRequested());
        assertFalse(right.isPowerRequested());
    }
}
