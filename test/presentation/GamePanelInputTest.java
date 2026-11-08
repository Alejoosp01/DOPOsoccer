package presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Country;
import domain.FreezePower;
import domain.GameCharacter;
import domain.GameMode;
import domain.HumanController;
import domain.MatchConfig;
import domain.POOBSoccerException;

/**
 * Pruebas de los enlaces de teclado del panel de juego.
 * Se ejecutan sin mostrar la ventana, asi que no necesitan pantalla.
 */
public class GamePanelInputTest {

    private GamePanel panel;
    private GameSession session;

    @BeforeEach
    public void setUp() throws POOBSoccerException {
        GameCharacter left = new GameCharacter("Left",
                new Country("Left", new FreezePower()), 3, 3, 3);
        GameCharacter right = new GameCharacter("Right",
                new Country("Right", new FreezePower()), 3, 3, 3);
        session = new GameSession(MatchConfig.time(GameMode.PVP), left, right);
        panel = new GamePanel(session, () -> { }, () -> { });
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

    @Test
    public void shouldBindControlWithControlModifier() {
        KeyStroke stroke = KeyStroke.getKeyStroke(
                KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK, false);

        assertEquals("rightPower.pressed", actionFor(stroke));
    }

    @Test
    public void shouldBindControlLikeASwingControlStroke() {
        assertEquals(actionFor(KeyStroke.getKeyStroke("control CONTROL")),
                actionFor(KeyStroke.getKeyStroke(
                        KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK, false)));
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
