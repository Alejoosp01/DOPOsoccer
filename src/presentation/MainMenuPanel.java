package presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JButton;

/**
 * Menu principal del juego.
 */
public final class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient Runnable onPlay;
    private final transient Runnable onExit;
    private final transient Runnable onOpen;

    public MainMenuPanel(Runnable onPlay, Runnable onOpen, Runnable onExit) {
        this.onPlay = onPlay;
        this.onOpen = onOpen;
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

        javax.swing.JLabel title = UiTheme.label("DOPO SOCCER", UiTheme.titleFont(), UiTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        javax.swing.JLabel subtitle = UiTheme.label(
                "Proyecto Final DOPO 2026-2",
                UiTheme.subtitleFont(), UiTheme.MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(8));
        titleBlock.add(subtitle);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        ImageButton play = new ImageButton(SpriteLoader.BUTTON_PLAY, "JUGAR", UiTheme.ACCENT);
        play.setAlignmentX(CENTER_ALIGNMENT);
        play.setMaximumSize(new Dimension(300, 54));
        play.addActionListener(event -> onPlay.run());

        TileButton open = new TileButton(TileButton.FOLDER, "ABRIR PARTIDA");
        open.setAlignmentX(CENTER_ALIGNMENT);
        open.setMaximumSize(new Dimension(300, 54));
        open.addActionListener(event -> onOpen.run());

        TileButton how = new TileButton(TileButton.HELP, "COMO JUGAR");
        how.setAlignmentX(CENTER_ALIGNMENT);
        how.setMaximumSize(new Dimension(300, 54));
        how.addActionListener(event -> showHelp());

        TileButton exit = new TileButton(TileButton.EXIT, "SALIR");
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.setMaximumSize(new Dimension(300, 54));
        exit.addActionListener(event -> onExit.run());

        buttons.add(play);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(open);
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

        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.add(new HintPlaque("PvsP y PvsM"));
        footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Abre la ventana "Como jugar" con los colores del juego.
     */
    private void showHelp() {
        HelpDialog dialog = new HelpDialog(this);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        java.awt.image.BufferedImage stadium = SpriteLoader.load(SpriteLoader.STADIUM);
        if (stadium != null) {
            SpriteLoader.draw(g2, stadium, 0, 0, getWidth(), getHeight(), false);
            g2.setColor(UiTheme.withAlpha(UiTheme.BG_TOP, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            return;
        }
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

    /**
     * Placa azul marino semitransparente con el texto del pie, legible sobre cualquier fondo.
     */
    private static final class HintPlaque extends JPanel {

        private static final long serialVersionUID = 1L;
        private final String text;

        /** Crea la placa con el texto indicado. */
        HintPlaque(String text) {
            this.text = text;
            setOpaque(false);
        }

        /** Devuelve el tamaño de la placa según el texto. */
        @Override
        public Dimension getPreferredSize() {
            java.awt.FontMetrics m = getFontMetrics(UiTheme.bodyFont());
            return new Dimension(m.stringWidth(text) + 44, m.getHeight() + 14);
        }

        /** Dibuja la placa y el texto claro con sombra. */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            UiTheme.enableAntialiasing(g2);
            g2.setColor(UiTheme.withAlpha(UiTheme.BG_TOP, 215));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(UiTheme.withAlpha(UiTheme.ACCENT, 160));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setFont(UiTheme.bodyFont());
            java.awt.FontMetrics m = g2.getFontMetrics();
            int x = (getWidth() - m.stringWidth(text)) / 2;
            int y = (getHeight() - m.getHeight()) / 2 + m.getAscent();
            g2.setColor(new java.awt.Color(0, 0, 0, 150));
            g2.drawString(text, x + 1, y + 2);
            g2.setColor(UiTheme.TEXT);
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    /**
     * Boton del menu con ficha naranja dibujada en Java2D (mismo estilo que los iconos de resources/ui/buttons).
     */
    private static final class TileButton extends JButton {

        static final int FOLDER = 0;
        static final int HELP = 1;
        static final int EXIT = 2;
        private static final long serialVersionUID = 1L;
        private static final java.awt.Color DARK = new java.awt.Color(0x2B2F3A);
        private static final java.awt.Color TOP = new java.awt.Color(0xFFB13B);
        private static final java.awt.Color BOTTOM = new java.awt.Color(0xF2641A);

        private final int glyph;
        private final String label;
        private int cachedSide;
        private java.awt.image.BufferedImage tile;
        private java.awt.image.BufferedImage tileHover;

        /** Crea el boton con el glifo (FOLDER, HELP o EXIT) y el texto dados. */
        TileButton(int glyph, String text) {
            super(text);
            this.glyph = glyph;
            this.label = text;
            setFont(UiTheme.font(java.awt.Font.BOLD, 18));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(text);
        }

        /** Devuelve el tamaño preferido: ficha mas texto. */
        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) {
                return super.getPreferredSize();
            }
            return new Dimension(56 + 12 + getFontMetrics(getFont()).stringWidth(label) + 16, 56);
        }

        /** Dibuja la ficha (con realce o hundida) y el texto con sombra a su derecha. */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            UiTheme.enableAntialiasing(g2);
            int side = Math.max(1, Math.min(getHeight() - 6, getWidth() - 6));
            ensureTiles(side);
            boolean down = getModel().isArmed() && getModel().isPressed();
            g2.drawImage(!down && getModel().isRollover() ? tileHover : tile,
                    3, (getHeight() - side) / 2 + (down ? 2 : 0), null);
            g2.setFont(getFont());
            java.awt.FontMetrics m = g2.getFontMetrics();
            int x = 3 + side + 12;
            int y = (getHeight() - m.getHeight()) / 2 + m.getAscent();
            g2.setColor(new java.awt.Color(0, 0, 0, 150));
            g2.drawString(label, x + 1, y + 2);
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString(label, x, y);
            if (isFocusOwner()) {
                g2.setColor(UiTheme.withAlpha(java.awt.Color.WHITE, 220));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);
            }
            g2.dispose();
        }

        /** Genera una sola vez (por tamaño) la ficha normal y la aclarada. */
        private void ensureTiles(int side) {
            if (tile != null && cachedSide == side) {
                return;
            }
            cachedSide = side;
            tile = new java.awt.image.BufferedImage(side, side, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = tile.createGraphics();
            UiTheme.enableAntialiasing(g);
            paintTile(g, side);
            g.dispose();
            tileHover = new java.awt.image.BufferedImage(side, side, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g = tileHover.createGraphics();
            g.drawImage(tile, 0, 0, null);
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_ATOP, 0.22f));
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, side, side);
            g.dispose();
        }

        /** Dibuja la ficha naranja con degradado, borde oscuro, brillo y el glifo. */
        private void paintTile(Graphics2D g, int s) {
            double u = s / 56.0;
            int arc = (int) (16 * u);
            g.setColor(DARK);
            g.fillRoundRect(0, (int) (3 * u), s - 1, s - (int) (3 * u) - 1, arc, arc);
            g.setPaint(new java.awt.GradientPaint(0, 0, TOP, 0, s - 4 * (float) u, BOTTOM));
            g.fillRoundRect((int) (3 * u), (int) (3 * u), s - (int) (7 * u), s - (int) (11 * u), arc - 4, arc - 4);
            g.setColor(new java.awt.Color(255, 255, 255, 90));
            g.fillRoundRect((int) (8 * u), (int) (6 * u), (int) (16 * u), (int) (5 * u), 8, 8);
            g.setColor(DARK);
            g.setStroke(new java.awt.BasicStroke((float) (4.5 * u), java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            double cx = s / 2.0;
            double cy = s / 2.0 - u;
            if (glyph == FOLDER) {
                java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
                p.moveTo(cx - 14 * u, cy + 11 * u);
                p.lineTo(cx - 14 * u, cy - 11 * u);
                p.lineTo(cx - 4 * u, cy - 11 * u);
                p.lineTo(cx, cy - 6 * u);
                p.lineTo(cx + 14 * u, cy - 6 * u);
                p.lineTo(cx + 14 * u, cy + 11 * u);
                p.closePath();
                g.fill(p);
                g.setColor(TOP);
                g.setStroke(new java.awt.BasicStroke((float) (2 * u)));
                g.draw(new java.awt.geom.Line2D.Double(cx - 14 * u, cy - 1 * u, cx + 14 * u, cy - 1 * u));
            } else if (glyph == EXIT) {
                g.draw(new java.awt.geom.Line2D.Double(cx - 10 * u, cy - 10 * u, cx + 10 * u, cy + 10 * u));
                g.draw(new java.awt.geom.Line2D.Double(cx - 10 * u, cy + 10 * u, cx + 10 * u, cy - 10 * u));
            } else {
                g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, (int) (34 * u)));
                java.awt.FontMetrics m = g.getFontMetrics();
                g.drawString("?", (int) (cx - m.stringWidth("?") / 2.0),
                        (int) (cy + (m.getAscent() - m.getDescent()) / 2.0));
            }
        }
    }
}
