package presentation;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import domain.DOPOSoccer;
import domain.DOPOSoccerException;
import domain.ErrorLog;
import domain.PlayerProfile;

/**
 * Ventana principal. Usa CardLayout para alternar entre menu, configuracion
 * y juego sin abrir varias ventanas. Guarda los perfiles de los dos jugadores
 * (estadisticas y puntos) mientras el programa esta abierto.
 */
public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel cards;
    private final MainMenuPanel menuPanel;
    private final SetupPanel setupPanel;
    private final transient PlayerProfile player1;
    private final transient PlayerProfile player2;

    private GamePanel gamePanel;
    private transient GameSetup lastSetup;

    public GameFrame() {
        super("DOPO Soccer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        player1 = createProfile("Jugador 1");
        player2 = createProfile("Jugador 2");
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this::showSetup, this::exitGame);
        setupPanel = new SetupPanel(this::startGame, this::showMenu, player1, player2);
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

    /**
     * Crea un perfil; si fallara, lo registra en el log y avisa al usuario.
     */
    private PlayerProfile createProfile(String name) {
        try {
            return new PlayerProfile(name);
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Muestra el menu principal y vuelve al tamaño normal de la ventana.
     */
    private void showMenu() {
        removeGamePanel();
        cardLayout.show(cards, "menu");
        pack();
        setLocationRelativeTo(null);
        menuPanel.requestFocusInWindow();
    }

    /**
     * Muestra la configuracion con los puntos y estadisticas actuales.
     */
    private void showSetup() {
        setupPanel.refreshProfiles();
        cardLayout.show(cards, "setup");
    }

    /**
     * Crea el partido con lo elegido y abre el panel de juego en su resolucion.
     */
    private void startGame(GameSetup setup) {
        if (setup == null) {
            return;
        }
        lastSetup = setup;
        removeGamePanel();
        DOPOSoccer session = new DOPOSoccer(setup.getConfig(),
                setup.getLeftCharacter(), setup.getRightCharacter(), player1, player2);
        gamePanel = new GamePanel(session, this::showMenu, this::rematch, setup.getResolution());
        cards.add(gamePanel, "game");
        cardLayout.show(cards, "game");
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    /**
     * Quita el panel de juego anterior, si lo hay.
     */
    private void removeGamePanel() {
        if (gamePanel != null) {
            cards.remove(gamePanel);
            gamePanel = null;
        }
    }

    /**
     * Juega otra vez con la misma configuracion.
     */
    private void rematch() {
        if (lastSetup != null) {
            startGame(lastSetup);
        }
    }

    /**
     * Cierra el juego.
     */
    private void exitGame() {
        dispose();
        System.exit(0);
    }
}
