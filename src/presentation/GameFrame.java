package presentation;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Ventana principal. Usa CardLayout para alternar entre menu, configuracion
 * y juego sin abrir varias ventanas.
 */
public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel cards;
    private final MainMenuPanel menuPanel;
    private final SetupPanel setupPanel;

    private GamePanel gamePanel;
    private transient GameSetup lastSetup;

    public GameFrame() {
        super("POOB Soccer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this::showSetup, this::exitGame);
        setupPanel = new SetupPanel(this::startGame, this::showMenu);
        cards.add(menuPanel, "menu");
        cards.add(setupPanel, "setup");

        setContentPane(cards);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent event) {
                if (gamePanel != null) {
                    gamePanel.clearInput();
                }
            }
        });
    }

    private void showMenu() {
        if (gamePanel != null) {
            cards.remove(gamePanel);
            gamePanel = null;
        }
        cardLayout.show(cards, "menu");
        menuPanel.requestFocusInWindow();
    }

    private void showSetup() {
        cardLayout.show(cards, "setup");
    }

    private void startGame(GameSetup setup) {
        if (setup == null) {
            return;
        }
        lastSetup = setup;
        if (gamePanel != null) {
            cards.remove(gamePanel);
        }
        GameSession session = new GameSession(setup.getConfig(),
                setup.getLeftCharacter(), setup.getRightCharacter());
        gamePanel = new GamePanel(session, this::showMenu, this::rematch);
        cards.add(gamePanel, "game");
        cardLayout.show(cards, "game");
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    private void rematch() {
        if (lastSetup != null) {
            startGame(lastSetup);
        }
    }

    private void exitGame() {
        dispose();
        System.exit(0);
    }
}
