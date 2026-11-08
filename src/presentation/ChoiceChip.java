package presentation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JToggleButton;

/**
 * Opcion seleccionable con forma de pastilla. Se agrupa con un ButtonGroup
 * para que solo una opcion del grupo quede marcada, como un radio button.
 */
public final class ChoiceChip extends JToggleButton {

    private static final long serialVersionUID = 1L;

    public ChoiceChip(String text) {
        super(text);
        setFont(UiTheme.font(Font.BOLD, 14));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int width = getFontMetrics(getFont()).stringWidth(text) + 44;
        setPreferredSize(new Dimension(Math.max(width, 64), 38));
    }

    /**
     * Dibuja la pastilla: verde si esta elegida, gris si no, apagada si esta deshabilitada.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        RoundRectangle2D.Double shape = new RoundRectangle2D.Double(
                1, 1, getWidth() - 2, getHeight() - 2, getHeight() - 2, getHeight() - 2);

        Color fill = UiTheme.PANEL_LIGHT;
        Color border = UiTheme.BORDER;
        Color text = UiTheme.TEXT;
        if (!isEnabled()) {
            fill = UiTheme.PANEL;
            border = UiTheme.withAlpha(UiTheme.BORDER, 90);
            text = UiTheme.withAlpha(UiTheme.MUTED, 110);
        } else if (isSelected()) {
            fill = UiTheme.ACCENT;
            border = UiTheme.lighten(UiTheme.ACCENT, 0.3f);
            text = UiTheme.BG_TOP;
        } else if (getModel().isRollover()) {
            fill = UiTheme.lighten(UiTheme.PANEL_LIGHT, 0.12f);
        }

        g2.setColor(fill);
        g2.fill(shape);
        g2.setColor(border);
        g2.draw(shape);

        g2.setFont(getFont());
        g2.setColor(text);
        int textWidth = g2.getFontMetrics().stringWidth(getText());
        int baseline = (getHeight() + g2.getFontMetrics().getAscent()
                - g2.getFontMetrics().getDescent()) / 2;
        g2.drawString(getText(), (getWidth() - textWidth) / 2, baseline);
        g2.dispose();
    }
}
