package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JToggleButton;

import domain.GameCharacter;

/**
 * Tarjeta seleccionable de un personaje: cabeza con los colores del pais,
 * nombre, poder y barras de velocidad, salto y fuerza (de 1 a 5).
 */
public final class CharacterCard extends JToggleButton {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 212;
    public static final int HEIGHT = 124;

    private final transient GameCharacter character;
    private final Color sideColor;

    public CharacterCard(GameCharacter character, Color sideColor) {
        this.character = character;
        this.sideColor = sideColor;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setToolTipText(character.getPower().getName() + ": "
                + UiTheme.powerDescription(character.getPower()));
    }

    /**
     * Devuelve el personaje que representa la tarjeta.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * Dibuja la tarjeta completa; si esta elegida se resalta con el color del lado.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        RoundRectangle2D.Double card = new RoundRectangle2D.Double(
                2, 2, getWidth() - 4, getHeight() - 4, 18, 18);

        if (isSelected()) {
            g2.setColor(UiTheme.PANEL_LIGHT);
        } else {
            g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 235));
        }
        g2.fill(card);
        if (isSelected()) {
            g2.setColor(sideColor);
            g2.setStroke(new BasicStroke(3f));
        } else if (getModel().isRollover()) {
            g2.setColor(UiTheme.MUTED);
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(UiTheme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
        }
        g2.draw(card);

        drawHead(g2, 40, 40, 22);

        g2.setFont(UiTheme.font(Font.BOLD, 15));
        g2.setColor(UiTheme.TEXT);
        g2.drawString(character.getName(), 74, 36);
        g2.setFont(UiTheme.font(Font.BOLD, 12));
        g2.setColor(UiTheme.GOLD);
        g2.drawString(character.getPower().getName(), 74, 54);

        drawStat(g2, "VEL", character.getSpeed(), 82);
        drawStat(g2, "SALTO", character.getJump(), 98);
        drawStat(g2, "FUERZA", character.getStrength(), 114);
        g2.dispose();
    }

    /**
     * Dibuja la cabeza del personaje con las franjas de su pais.
     */
    private void drawHead(Graphics2D g2, double cx, double cy, double radius) {
        Ellipse2D head = new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2);
        g2.setColor(UiTheme.countryPrimary(character));
        g2.fill(head);
        Shape oldClip = g2.getClip();
        g2.clip(head);
        g2.setColor(UiTheme.countrySecondary(character));
        g2.fillRect((int) (cx - radius), (int) (cy - radius),
                (int) (radius * 2), (int) (radius * 0.42));
        g2.fillRect((int) (cx - radius), (int) (cy + radius * 0.58),
                (int) (radius * 2), (int) (radius * 0.42));
        g2.setClip(oldClip);

        g2.setColor(new Color(250, 250, 255));
        g2.fillOval((int) (cx - radius * 0.5), (int) (cy - radius * 0.3), 9, 11);
        g2.fillOval((int) (cx + radius * 0.12), (int) (cy - radius * 0.3), 9, 11);
        g2.setColor(new Color(22, 28, 42));
        g2.fillOval((int) (cx - radius * 0.5) + 3, (int) (cy - radius * 0.3) + 3, 5, 6);
        g2.fillOval((int) (cx + radius * 0.12) + 3, (int) (cy - radius * 0.3) + 3, 5, 6);

        if (isSelected()) {
            g2.setColor(sideColor);
        } else {
            g2.setColor(UiTheme.BORDER);
        }
        g2.setStroke(new BasicStroke(3f));
        g2.draw(head);
    }

    /**
     * Dibuja una estadistica como 5 segmentos; se llenan tantos como su valor.
     */
    private void drawStat(Graphics2D g2, String label, int value, int baseline) {
        g2.setFont(UiTheme.font(Font.BOLD, 11));
        g2.setColor(UiTheme.MUTED);
        g2.drawString(label, 16, baseline);
        for (int i = 0; i < GameCharacter.MAX_STAT; i++) {
            int x = 76 + i * 25;
            if (i < value) {
                g2.setColor(sideColor);
            } else {
                g2.setColor(UiTheme.withAlpha(UiTheme.BORDER, 120));
            }
            g2.fillRoundRect(x, baseline - 9, 21, 9, 4, 4);
        }
    }
}
