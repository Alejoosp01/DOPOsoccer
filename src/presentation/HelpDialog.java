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
                + "T terminar &nbsp; M menu &nbsp; R revancha al terminar</p>"
                + "<p style='color:" + GOLD + "'><b>PODERES</b></p>"
                + "<p style='color:" + MUTED + "'>La barra se llena en 10 segundos (y sube 1/4 al recibir "
                + "un gol). Con la barra llena, la tecla de poder activa un aura. Al tocar el balon con "
                + "el aura sale el tiro de poder: si golpea al rival le hace dano y le deja un efecto.</p>"
                + "<p>Aura Azul (Corea del Sur) &nbsp; Rayo (Camerun) &nbsp; Tornado (Nigeria) &nbsp; "
                + "Ilusion (Estados Unidos)</p>"
                + "<p style='color:" + MUTED + "'>Cada patada que alcanza al rival le quita vida. "
                + "Si la vida llega a 0 queda aturdido 3 segundos y luego se recupera.</p>"
                + "</body></html>";
    }
}
