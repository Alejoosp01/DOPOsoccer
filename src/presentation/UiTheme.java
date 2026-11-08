package presentation;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Colores, fuentes y utilidades de dibujo compartidas por la interfaz.
 */
public final class UiTheme {

    public static final Color BG_TOP = new Color(9, 14, 26);
    public static final Color BG_BOTTOM = new Color(20, 30, 48);
    public static final Color PANEL = new Color(24, 35, 54);
    public static final Color PANEL_LIGHT = new Color(34, 48, 72);
    public static final Color BORDER = new Color(70, 92, 124);
    public static final Color TEXT = new Color(238, 244, 255);
    public static final Color MUTED = new Color(158, 176, 200);
    public static final Color ACCENT = new Color(74, 210, 128);
    public static final Color ACCENT_DARK = new Color(34, 132, 78);
    public static final Color GOLD = new Color(255, 205, 70);
    public static final Color DANGER = new Color(232, 84, 84);
    public static final Color LEFT_COLOR = new Color(231, 76, 60);
    public static final Color RIGHT_COLOR = new Color(52, 152, 219);
    public static final Color ICE = new Color(150, 225, 255);

    private UiTheme() {
    }

    public static Font titleFont() {
        return new Font("SansSerif", Font.BOLD, 58);
    }

    public static Font headingFont() {
        return new Font("SansSerif", Font.BOLD, 30);
    }

    public static Font subtitleFont() {
        return new Font("SansSerif", Font.BOLD, 18);
    }

    public static Font bodyFont() {
        return new Font("SansSerif", Font.PLAIN, 15);
    }

    public static Font smallFont() {
        return new Font("SansSerif", Font.BOLD, 12);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(bodyFont());
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JTextField field = editor.getTextField();
        field.setBackground(PANEL_LIGHT);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    public static void paintVerticalGradient(Graphics2D g2, int width, int height,
                                             Color top, Color bottom) {
        g2.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
        g2.fillRect(0, 0, width, height);
    }

    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color lighten(Color color, float amount) {
        int red = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * amount));
        int green = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * amount));
        int blue = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * amount));
        return new Color(red, green, blue);
    }

    public static Color darken(Color color, float amount) {
        int red = Math.max(0, (int) (color.getRed() * (1 - amount)));
        int green = Math.max(0, (int) (color.getGreen() * (1 - amount)));
        int blue = Math.max(0, (int) (color.getBlue() * (1 - amount)));
        return new Color(red, green, blue);
    }
}
