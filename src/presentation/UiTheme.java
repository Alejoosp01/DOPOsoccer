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

import domain.ConfusionPower;
import domain.FreezePower;
import domain.GameCharacter;
import domain.IllusionPower;
import domain.Power;
import domain.PushPower;
import domain.ShrinkPower;

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
    public static final Color GRASS_LIGHT = new Color(64, 150, 74);
    public static final Color GRASS_DARK = new Color(50, 128, 62);
    public static final Color HUD_BAND = new Color(14, 21, 35);

    public static final String FONT_FAMILY = "SansSerif";

    private UiTheme() {
    }

    /**
     * Devuelve una fuente de la familia del juego con el estilo y tamano indicados.
     */
    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    /**
     * Devuelve el color principal de la camiseta de un pais.
     */
    public static Color countryPrimary(GameCharacter character) {
        String name = character.getCountry().getName();
        if ("Corea del Sur".equals(name)) {
            return new Color(205, 48, 62);
        }
        if ("Estados Unidos".equals(name)) {
            return new Color(58, 64, 122);
        }
        if ("Canada".equals(name)) {
            return new Color(214, 45, 34);
        }
        if ("Colombia".equals(name)) {
            return new Color(250, 205, 25);
        }
        if ("Brasil".equals(name)) {
            return new Color(255, 220, 0);
        }
        return new Color(120, 140, 170);
    }

    /**
     * Devuelve el color secundario (franjas) de un pais.
     */
    public static Color countrySecondary(GameCharacter character) {
        String name = character.getCountry().getName();
        if ("Corea del Sur".equals(name)) {
            return new Color(0, 55, 125);
        }
        if ("Estados Unidos".equals(name)) {
            return new Color(178, 34, 52);
        }
        if ("Canada".equals(name)) {
            return new Color(246, 248, 252);
        }
        if ("Colombia".equals(name)) {
            return new Color(0, 56, 147);
        }
        if ("Brasil".equals(name)) {
            return new Color(0, 156, 59);
        }
        return new Color(60, 76, 100);
    }

    /**
     * Devuelve una frase corta que explica que hace un poder.
     */
    public static String powerDescription(Power power) {
        if (power instanceof PushPower) {
            return "Tiro recto muy rapido: empuja y aturde al rival que lo toque";
        }
        if (power instanceof IllusionPower) {
            return "El balon se divide en 8; el real atraviesa al rival";
        }
        if (power instanceof FreezePower) {
            return "Congela al rival en un bloque de hielo";
        }
        if (power instanceof ShrinkPower) {
            return "Encoge al rival a un tamano diminuto";
        }
        if (power instanceof ConfusionPower) {
            return "Invierte los controles de movimiento del rival";
        }
        return "";
    }

    public static Font titleFont() {
        return new Font(FONT_FAMILY, Font.BOLD, 58);
    }

    public static Font headingFont() {
        return new Font(FONT_FAMILY, Font.BOLD, 30);
    }

    public static Font subtitleFont() {
        return new Font(FONT_FAMILY, Font.BOLD, 18);
    }

    public static Font bodyFont() {
        return new Font(FONT_FAMILY, Font.PLAIN, 15);
    }

    public static Font smallFont() {
        return new Font(FONT_FAMILY, Font.BOLD, 12);
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
