package presentation;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import domain.DOPOSoccer;
import domain.DOPOSoccerException;
import domain.ErrorLog;
import domain.PlayerProfile;
import domain.ProfileRegistry;

/**
 * Ventana principal. Usa CardLayout para alternar entre menu, configuracion
 * y juego sin abrir varias ventanas. Al abrir carga el progreso de los
 * jugadores (puntos y estadisticas) desde dopo_progress.dat y lo vuelve a
 * guardar al salir de cada partido y al cerrar. Tambien abre partidas salvadas.
 */
public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String PLAYER_1 = "Jugador 1";
    public static final String PLAYER_2 = "Jugador 2";

    private final CardLayout cardLayout;
    private final JPanel cards;
    private final MainMenuPanel menuPanel;
    private final SetupPanel setupPanel;
    private final transient File progressFile;
    private final transient ProfileRegistry registry;
    private transient PlayerProfile player1;
    private transient PlayerProfile player2;

    private GamePanel gamePanel;
    private transient GameSetup lastSetup;

    public GameFrame() {
        super("DOPO Soccer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        progressFile = new File(ProfileRegistry.DEFAULT_FILE);
        registry = loadProgress();
        player1 = profile(PLAYER_1);
        player2 = profile(PLAYER_2);
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this::showSetup, this::openGame, this::exitGame);
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

            @Override
            public void windowClosing(WindowEvent event) {
                exitGame();
            }
        });
    }

    /**
     * Carga el progreso guardado; si el archivo esta dañado avisa y empieza con uno vacio.
     */
    private ProfileRegistry loadProgress() {
        try {
            return ProfileRegistry.load(progressFile);
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage()
                    + "\nSe empieza con un progreso nuevo.", "Progreso", JOptionPane.WARNING_MESSAGE);
            return new ProfileRegistry();
        }
    }

    /**
     * Devuelve el perfil con ese nombre del progreso (lo crea si no existe).
     */
    private PlayerProfile profile(String name) {
        try {
            return registry.getOrCreate(name);
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Guarda el progreso. Si la partida actual termino, primero registra sus perfiles
     * (una partida abierta de un archivo trae sus propias copias de los perfiles).
     */
    private void saveProgress() {
        if (gamePanel != null && gamePanel.getSession().isFinished()) {
            DOPOSoccer session = gamePanel.getSession();
            registry.recordMatch(session);
            useProfiles(session.getLeftProfile(), session.getRightProfile());
        }
        try {
            registry.save(progressFile);
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage() + "\n"
                    + progressFile.getAbsolutePath(), "Progreso", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Usa estos perfiles para los proximos partidos y los muestra en la configuracion.
     */
    private void useProfiles(PlayerProfile left, PlayerProfile right) {
        player1 = left;
        player2 = right;
        setupPanel.setProfiles(player1, player2);
    }

    /**
     * Muestra el menu principal y vuelve al tamaño normal de la ventana.
     */
    private void showMenu() {
        saveProgress();
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
        saveProgress();
        DOPOSoccer session = new DOPOSoccer(setup.getConfig(),
                setup.getLeftCharacter(), setup.getRightCharacter(), player1, player2);
        showGame(session, setup.getResolution());
    }

    /**
     * Pide un archivo .dopo y continua esa partida (abre en pausa).
     */
    private void openGame() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Abrir partida");
        chooser.setFileFilter(new FileNameExtensionFilter("Partidas DOPOSoccer (*.dopo)", "dopo"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            DOPOSoccer session = DOPOSoccer.open(file);
            Resolution resolution = Resolution.MEDIUM;
            if (lastSetup != null) {
                resolution = lastSetup.getResolution();
            }
            lastSetup = new GameSetup(session.getConfig(), session.getLeftCharacter(),
                    session.getRightCharacter(), resolution);
            showGame(session, resolution);
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage() + "\n" + file.getAbsolutePath(),
                    "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Pone el panel de juego con la partida indicada.
     */
    private void showGame(DOPOSoccer session, Resolution resolution) {
        saveProgress();
        removeGamePanel();
        gamePanel = new GamePanel(session, this::showMenu, this::rematch, resolution);
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
     * Guarda el progreso y cierra el juego.
     */
    private void exitGame() {
        saveProgress();
        dispose();
        System.exit(0);
    }
}
