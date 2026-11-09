package presentation;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicacion de escritorio.
 */
public final class DOPOSoccerApp {

    private DOPOSoccerApp() {
    }

    /**
     * Arranca el juego en el hilo de Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> openWindow());
    }

    /**
     * Configura el estilo de Swing y abre la ventana principal.
     */
    private static void openWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exception) {
            // Si el look and feel no esta disponible se usa el del sistema.
        }
        GameFrame frame = new GameFrame();
        frame.setVisible(true);
    }
}
