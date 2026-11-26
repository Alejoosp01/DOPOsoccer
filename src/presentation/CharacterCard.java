package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JToggleButton;

import domain.GameCharacter;

/**
 * Tarjeta seleccionable de un personaje: retrato del sprite, nombre y tiro de poder.
 */
public final class CharacterCard extends JToggleButton {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 140;
    public static final int HEIGHT = 150;
    private static final int PORTRAIT_WIDTH = 124;
    private static final int PORTRAIT_HEIGHT = 98;

    private final transient GameCharacter character;
    private final Color sideColor;
    private final transient BufferedImage portrait;

    public CharacterCard(GameCharacter character, Color sideColor) {
        this.character = character;
        this.sideColor = sideColor;
        this.portrait = loadPortrait(character);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setToolTipText(character.getPowerShot().getName() + ": "
                + UiTheme.powerDescription(character.getPowerShot()));
    }

    /**
     * Devuelve el retrato escalado una sola vez (primer cuadro idle) o null si no hay sprite.
     */
    private static BufferedImage loadPortrait(GameCharacter character) {
        BufferedImage[] frames = SpriteLoader.characterFrames(character, SpriteLoader.IDLE);
        if (frames == null || frames.length == 0 || frames[0] == null) {
            return null;
        }
        return SpriteLoader.fit(frames[0], PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
    }

    /**
     * Devuelve el personaje que representa la tarjeta.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * Dibuja la tarjeta: fondo azul marino, borde naranja si esta elegida, retrato, nombre y tiro.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        RoundRectangle2D.Double card = new RoundRectangle2D.Double(
                2, 2, getWidth() - 5, getHeight() - 5, 18, 18);

        Color top = isSelected() ? UiTheme.NAVY_LIGHT : UiTheme.NAVY;
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), UiTheme.NAVY_DARK));
        g2.fill(card);
        if (isSelected()) {
            g2.setColor(UiTheme.ORANGE);
            g2.setStroke(new BasicStroke(4f));
        } else if (getModel().isRollover()) {
            g2.setColor(UiTheme.ORANGE_DARK);
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(UiTheme.NAVY_LIGHT);
            g2.setStroke(new BasicStroke(1.5f));
        }
        g2.draw(card);

        int portraitCenterY = 12 + PORTRAIT_HEIGHT / 2;
        if (portrait != null) {
            g2.drawImage(portrait, (getWidth() - portrait.getWidth()) / 2,
                    12 + (PORTRAIT_HEIGHT - portrait.getHeight()) / 2, null);
        } else {
            drawHead(g2, getWidth() / 2.0, portraitCenterY, 28);
        }

        drawCentered(g2, character.getName(), UiTheme.font(Font.BOLD, 15), UiTheme.TEXT_LIGHT, 124);
        drawCentered(g2, character.getPowerShot().getName(), UiTheme.font(Font.BOLD, 12),
                UiTheme.GOLD_LIGHT, 141);
        g2.dispose();
    }

    /**
     * Dibuja un texto con sombra centrado horizontalmente sobre la linea base indicada.
     */
    private void drawCentered(Graphics2D g2, String text, Font font, Color color, int baseline) {
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        UiTheme.drawShadowedText(g2, text, (getWidth() - metrics.stringWidth(text)) / 2, baseline, color);
    }

    /**
     * Dibuja la cabeza del personaje con las franjas de su pais (respaldo si no hay sprite).
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
}
