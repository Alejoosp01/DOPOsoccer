package presentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Ventana "Como jugar" con los colores del juego, en lugar del cuadro blanco
 * por defecto de JOptionPane.
 */
public final class HelpDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String MUTED = "#9eb0c8";
    private static final String GOLD = "#ffcd46";
    private static final String RED = "#e74c3c";
    private static final String BLUE = "#3498db";

    public HelpDialog(Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Como jugar", ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                UiTheme.paintVerticalGradient(g2, getWidth(), getHeight(),
                        UiTheme.PANEL, UiTheme.BG_TOP);
                g2.dispose();
            }
        };
        content.setBorder(BorderFactory.createEmptyBorder(22, 30, 18, 30));

        JLabel text = new JLabel(buildText());
        text.setFont(UiTheme.bodyFont());
        text.setForeground(UiTheme.TEXT);
        content.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setOpaque(false);
        MenuButton close = new MenuButton("ENTENDIDO", UiTheme.ACCENT);
        close.addActionListener(event -> dispose());
        buttons.add(close);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Devuelve el texto de ayuda en HTML: controles, reglas y poderes.
     */
    private String buildText() {
        return "<html><head><style>p { margin-top: 7px }</style></head><body style='width:470px'>"
                + "<h2 style='color:white;margin:0'>Como jugar</h2>"
                + "<p style='color:" + MUTED + "'>Mete el balon completo en el arco rival. "
                + "Si entra en tu propio arco, el gol es para el rival.</p>"
                + "<p><b style='color:" + RED + "'>JUGADOR 1</b> &nbsp; A / D mover &nbsp; W saltar"
                + " &nbsp; S patear &nbsp; Q poder</p>"
                + "<p><b style='color:" + BLUE + "'>JUGADOR 2</b> &nbsp; Flechas mover &nbsp; Arriba saltar"
                + " &nbsp; Abajo patear &nbsp; Ctrl derecho poder</p>"
                + "<p><b style='color:" + GOLD + "'>PARTIDA</b> &nbsp; ESC o P pausa &nbsp; "
                + "M menu &nbsp; R revancha al terminar</p>"
                + "<p style='color:" + GOLD + "'><b>PODERES</b></p>"
                + "<p style='color:" + MUTED + "'>La barra se llena en 10 segundos. Con la barra llena, "
                + "la tecla de poder lo deja armado y se activa con el siguiente golpe al balon.</p>"
                + "<p>Empuje (Corea del Sur) &nbsp; Ilusion (Estados Unidos) &nbsp; "
                + "Congelacion (Canada) &nbsp; Reduccion (Colombia) &nbsp; Confusion (Brasil)</p>"
                + "<p style='color:" + MUTED + "'>Un jugador aturdido no se mueve, no salta ni patea "
                + "durante 3 segundos.</p>"
                + "</body></html>";
    }
}
