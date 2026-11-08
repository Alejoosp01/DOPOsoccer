package presentation;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicacion de escritorio.
 */
public final class HeadSoccerApp {

    private HeadSoccerApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception exception) {
                // Si el look and feel no esta disponible se usa el del sistema.
            }
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
