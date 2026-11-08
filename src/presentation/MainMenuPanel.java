package presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Menu principal del juego.
 */
@SuppressWarnings("serial")
public final class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Runnable onPlay;
    private final Runnable onExit;

    public MainMenuPanel(Runnable onPlay, Runnable onExit) {
        this.onPlay = onPlay;
        this.onExit = onExit;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(true);
        buildInterface();
    }

    private void buildInterface() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        javax.swing.JLabel title = UiTheme.label("POOB SOCCER", UiTheme.titleFont(), UiTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        javax.swing.JLabel subtitle = UiTheme.label(
                "Futbol de cabezas - Proyecto Final DOPO 2026-2",
                UiTheme.subtitleFont(), UiTheme.MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(8));
        titleBlock.add(subtitle);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        MenuButton play = new MenuButton("JUGAR", UiTheme.ACCENT);
        play.setAlignmentX(CENTER_ALIGNMENT);
        play.setMaximumSize(new Dimension(300, 54));
        play.addActionListener(event -> onPlay.run());

        MenuButton how = new MenuButton("COMO JUGAR", UiTheme.PANEL_LIGHT);
        how.setForeground(UiTheme.TEXT);
        how.setAlignmentX(CENTER_ALIGNMENT);
        how.setMaximumSize(new Dimension(300, 54));
        how.addActionListener(event -> showHelp());

        MenuButton exit = new MenuButton("SALIR", UiTheme.PANEL_LIGHT);
        exit.setForeground(UiTheme.TEXT);
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.setMaximumSize(new Dimension(300, 54));
        exit.addActionListener(event -> onExit.run());

        buttons.add(play);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(how);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(exit);

        center.add(Box.createVerticalGlue());
        center.add(titleBlock);
        center.add(Box.createVerticalStrut(54));
        center.add(buttons);
        center.add(Box.createVerticalGlue());

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        javax.swing.JLabel hint = UiTheme.label(
                "Swing + Java2D  |  5 paises  |  PvsP, PvsM y MvsM",
                UiTheme.bodyFont(), UiTheme.MUTED);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(hint, BorderLayout.CENTER);
        footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        add(footer, BorderLayout.SOUTH);
    }

    private void showHelp() {
        String message = "<html><body style='width:420px;font-family:SansSerif'>"
                + "<h2>Como jugar</h2>"
                + "<p>Elige modalidad, forma de terminar y un pais para cada lado.</p>"
                + "<h3>Jugador 1 (izquierda)</h3>"
                + "<p><b>A / D</b> mover &nbsp; <b>W</b> saltar &nbsp; "
                + "<b>S</b> patear &nbsp; <b>Q</b> poder</p>"
                + "<h3>Jugador 2 (derecha)</h3>"
                + "<p><b>Flechas</b> mover y saltar &nbsp; <b>Abajo</b> patear &nbsp; "
                + "<b>Ctrl</b> poder</p>"
                + "<h3>Partida</h3>"
                + "<p><b>ESC</b> pausa &nbsp; <b>R</b> revancha al terminar &nbsp; "
                + "<b>M</b> volver al menu</p>"
                + "<h3>Poderes</h3>"
                + "<p>La barra se llena en 10 segundos. Al presionar la tecla de poder queda "
                + "armado y se activa con el siguiente golpe al balon.</p>"
                + "<p><b>Congelacion</b> (Canada), <b>Reduccion</b> (Colombia), "
                + "<b>Confusion</b> (Brasil), <b>Empuje</b> (Corea del Sur) e "
                + "<b>Ilusion</b> (Estados Unidos).</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, message, "Como jugar",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        UiTheme.paintVerticalGradient(g2, getWidth(), getHeight(),
                UiTheme.BG_TOP, UiTheme.BG_BOTTOM);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(getWidth() / 2.0, -80),
                Math.max(getWidth() * 0.7f, 400),
                new float[] {0f, 1f},
                new java.awt.Color[] {UiTheme.withAlpha(UiTheme.ACCENT, 70),
                        UiTheme.withAlpha(UiTheme.ACCENT, 0)}));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(UiTheme.withAlpha(UiTheme.ACCENT, 28));
        g2.fillOval(getWidth() / 2 - 210, 150, 420, 420);
        g2.setColor(UiTheme.withAlpha(UiTheme.RIGHT_COLOR, 18));
        g2.fillOval(-160, 360, 520, 520);
        g2.setColor(UiTheme.withAlpha(UiTheme.LEFT_COLOR, 18));
        g2.fillOval(getWidth() - 360, 380, 520, 520);
        g2.dispose();
    }
}
