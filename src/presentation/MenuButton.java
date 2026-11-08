package presentation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JButton;

/**
 * Boton plano con esquinas redondeadas y efecto hover, usado en los menus.
 */
public final class MenuButton extends JButton {

    private static final long serialVersionUID = 1L;

    private final Color baseColor;

    public MenuButton(String text, Color baseColor) {
        super(text);
        this.baseColor = baseColor;
        setFont(UiTheme.font(Font.BOLD, 18));
        setForeground(UiTheme.BG_TOP);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 38, 12, 38));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        Color color = baseColor;
        if (!isEnabled()) {
            color = UiTheme.BORDER;
        } else if (getModel().isPressed()) {
            color = UiTheme.darken(baseColor, 0.25f);
        } else if (getModel().isRollover()) {
            color = UiTheme.lighten(baseColor, 0.18f);
        }
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.setColor(UiTheme.withAlpha(Color.WHITE, 40));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
        g2.dispose();
        super.paintComponent(graphics);
    }

    @Override
    public boolean contains(int x, int y) {
        return new Rectangle(0, 0, getWidth(), getHeight()).contains(x, y);
    }
}
