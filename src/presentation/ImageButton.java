package presentation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

/**
 * Boton con imagen (iconos _normal y _pressed de resources/ui/buttons); si las imagenes no cargan se dibuja como MenuButton.
 */
public final class ImageButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_ICON = 56;
    private static final int MARGIN = 3;
    private static final int GAP = 12;

    private final String imageName;
    private final String label;
    private final Color fallbackColor;
    private final boolean showLabel;
    private final boolean images;

    private BufferedImage hoverSource;
    private BufferedImage hoverImage;

    /**
     * Crea un boton con imagen que muestra su texto junto al icono.
     */
    public ImageButton(String imageName, String text, Color fallbackColor) {
        this(imageName, text, fallbackColor, true);
    }

    /**
     * Crea un boton con imagen; si showLabel es false solo se ve el icono (el texto queda como tooltip).
     */
    public ImageButton(String imageName, String text, Color fallbackColor, boolean showLabel) {
        super(text);
        this.imageName = imageName;
        this.label = text == null ? "" : text;
        this.fallbackColor = fallbackColor;
        this.showLabel = showLabel;
        this.images = SpriteLoader.buttonNormal(imageName) != null
                && SpriteLoader.buttonPressed(imageName) != null;
        setFont(UiTheme.font(Font.BOLD, 18));
        setForeground(images ? Color.WHITE : UiTheme.BG_TOP);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(true);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(text);
        getAccessibleContext().setAccessibleName(text);
    }

    /**
     * Devuelve true si las imagenes normal y presionada del boton se cargaron.
     */
    public boolean hasImages() {
        return images;
    }

    /**
     * Devuelve el tamaño preferido: cuadrado si solo hay icono, o icono mas texto si se muestra la etiqueta.
     */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        FontMetrics metrics = getFontMetrics(getFont());
        int textWidth = metrics.stringWidth(label);
        if (!images) {
            return new Dimension(textWidth + 76, metrics.getHeight() + 24);
        }
        if (!showLabel || label.isEmpty()) {
            return new Dimension(DEFAULT_ICON, DEFAULT_ICON);
        }
        return new Dimension(DEFAULT_ICON + GAP + textWidth + 16, DEFAULT_ICON);
    }

    /** Dibuja el botón con su imagen o, si no cargó, con el dibujo de respaldo. */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        if (images) {
            paintImage(g2);
        } else {
            MenuButton.paintFallback(g2, this, fallbackColor);
            drawText(g2, getWidth() / 2, UiTheme.BG_TOP, false);
        }
        if (isFocusOwner()) {
            g2.setColor(UiTheme.withAlpha(Color.WHITE, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);
        }
        g2.dispose();
    }

    /**
     * Dibuja el icono (normal, presionado o con realce) y el texto a su derecha.
     */
    private void paintImage(Graphics2D g2) {
        int side = Math.max(1, Math.min(getHeight() - 2 * MARGIN, getWidth() - 2 * MARGIN));
        int x = showLabel && !label.isEmpty() ? MARGIN : (getWidth() - side) / 2;
        int y = (getHeight() - side) / 2;
        BufferedImage normal = SpriteLoader.scaled(SpriteLoader.BUTTONS + imageName + "_normal"
                + SpriteLoader.EXTENSION, side, side);
        BufferedImage pressed = SpriteLoader.scaled(SpriteLoader.BUTTONS + imageName + "_pressed"
                + SpriteLoader.EXTENSION, side, side);
        if (normal == null || pressed == null) {
            return;
        }
        BufferedImage shown = normal;
        if (isEnabled() && getModel().isArmed() && getModel().isPressed()) {
            shown = pressed;
        } else if (isEnabled() && getModel().isRollover()) {
            shown = hover(normal);
        }
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }
        g2.drawImage(shown, x, y, null);
        g2.setComposite(AlphaComposite.SrcOver);
        if (showLabel && !label.isEmpty()) {
            drawText(g2, x + side + GAP, Color.WHITE, true);
        }
    }

    /**
     * Devuelve la imagen con realce (aclarada), calculada una sola vez por cada imagen escalada.
     */
    private BufferedImage hover(BufferedImage normal) {
        if (hoverSource != normal) {
            BufferedImage copy = new BufferedImage(normal.getWidth(), normal.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = copy.createGraphics();
            g.drawImage(normal, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.22f));
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, copy.getWidth(), copy.getHeight());
            g.dispose();
            hoverSource = normal;
            hoverImage = copy;
        }
        return hoverImage;
    }

    /**
     * Dibuja el texto del boton; con sombra y alineado a la izquierda en x, o centrado en x.
     */
    private void drawText(Graphics2D g2, int x, Color color, boolean leftAligned) {
        if (label.isEmpty()) {
            return;
        }
        g2.setFont(getFont());
        FontMetrics metrics = g2.getFontMetrics();
        int left = leftAligned ? x : x - metrics.stringWidth(label) / 2;
        int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        if (leftAligned) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(label, left + 1, baseline + 2);
        }
        g2.setColor(isEnabled() ? color : UiTheme.withAlpha(color, 140));
        g2.drawString(label, left, baseline);
    }

    /** Devuelve si el punto cae dentro del área rectangular del botón. */
    @Override
    public boolean contains(int x, int y) {
        return new Rectangle(0, 0, getWidth(), getHeight()).contains(x, y);
    }
}
