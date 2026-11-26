package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import domain.BlueAuraShot;
import domain.GameCharacter;
import domain.IllusionShot;
import domain.LightningShot;
import domain.PowerShot;
import domain.TornadoShot;

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

    /** Paleta azul marino y naranja/dorado, igual a la de los botones y paneles. */
    public static final Color NAVY_DARK = new Color(0x0E, 0x1A, 0x2E);
    public static final Color NAVY = new Color(0x1E, 0x2A, 0x44);
    public static final Color NAVY_LIGHT = new Color(0x33, 0x40, 0x5E);
    public static final Color ORANGE = new Color(0xF2, 0x6B, 0x1D);
    public static final Color ORANGE_DARK = new Color(0xB8, 0x46, 0x1A);
    public static final Color GOLD_LIGHT = new Color(0xFF, 0xC5, 0x3D);
    public static final Color TEXT_LIGHT = new Color(0xF2, 0xF6, 0xFF);
    public static final Color TEXT_SHADOW = new Color(5, 10, 24, 180);
    public static final Color LIFE_GREEN = new Color(0x4C, 0xC9, 0x6E);

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
        String name = character.getName();
        if ("Corea del Sur".equals(name)) {
            return new Color(205, 48, 62);
        }
        if ("Camerun".equals(name)) {
            return new Color(0, 122, 61);
        }
        if ("Nigeria".equals(name)) {
            return new Color(0, 135, 81);
        }
        if ("Estados Unidos".equals(name)) {
            return new Color(58, 64, 122);
        }
        return new Color(120, 140, 170);
    }

    /**
     * Devuelve el color secundario (franjas) de un pais.
     */
    public static Color countrySecondary(GameCharacter character) {
        String name = character.getName();
        if ("Corea del Sur".equals(name)) {
            return new Color(0, 55, 125);
        }
        if ("Camerun".equals(name)) {
            return new Color(252, 209, 22);
        }
        if ("Nigeria".equals(name)) {
            return new Color(246, 248, 252);
        }
        if ("Estados Unidos".equals(name)) {
            return new Color(178, 34, 52);
        }
        return new Color(60, 76, 100);
    }

    /**
     * Devuelve una frase corta que explica el efecto de un tiro de poder.
     */
    public static String powerDescription(PowerShot shot) {
        if (shot instanceof BlueAuraShot) {
            return "Recto y muy rapido. Empuje: lanza al rival hacia su arco";
        }
        if (shot instanceof LightningShot) {
            return "Recto y electrico. Electrocutado: no salta y va a mitad de velocidad";
        }
        if (shot instanceof TornadoShot) {
            return "Remolino a ras de suelo. Inconsciente: no se mueve, no salta ni patea";
        }
        if (shot instanceof IllusionShot) {
            return "8 balones iguales, 7 falsos. Confusion: no se sabe cual es el real";
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

    /**
     * Pinta un panel azul marino con borde naranja, linea dorada interior y sombra, con grosor base de 4 px.
     */
    public static void paintPanel(Graphics2D g, int x, int y, int w, int h) {
        paintPanel(g, x, y, w, h, Math.max(8, Math.min(w, h) / 6), 4);
    }

    /**
     * Pinta el panel con el radio de esquina y el grosor de borde indicados y restaura el estado del Graphics2D.
     */
    public static void paintPanel(Graphics2D g, int x, int y, int w, int h, int radius, int thickness) {
        if (w <= 0 || h <= 0) {
            return;
        }
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();
        Object oldAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int shadow = Math.max(2, thickness);
        int bw = Math.max(1, w - shadow);
        int bh = Math.max(1, h - shadow);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + shadow, y + shadow, bw, bh, radius, radius);
        g.setPaint(new GradientPaint(0, y, NAVY_LIGHT, 0, y + bh, NAVY_DARK));
        g.fillRoundRect(x, y, bw, bh, radius, radius);
        g.setStroke(new BasicStroke(thickness));
        g.setColor(ORANGE);
        int half = thickness / 2;
        g.drawRoundRect(x + half, y + half, bw - thickness, bh - thickness, radius, radius);
        g.setStroke(new BasicStroke(Math.max(1f, thickness / 4f)));
        g.setColor(GOLD_LIGHT);
        int inset = thickness + 1;
        g.drawRoundRect(x + inset, y + inset, bw - 2 * inset, bh - 2 * inset,
                Math.max(2, radius - 2 * inset), Math.max(2, radius - 2 * inset));
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        if (oldAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }

    /**
     * Dibuja el texto con una sombra oscura desplazada 2 px (la base de la fuente queda en x, y).
     */
    public static void drawShadowedText(Graphics2D g, String text, int x, int y, Color color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Paint oldPaint = g.getPaint();
        g.setColor(TEXT_SHADOW);
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
        g.drawString(text, x, y);
        g.setPaint(oldPaint);
    }

    /**
     * Dibuja el texto con sombra centrado horizontal y verticalmente en el area; un texto null o vacio no dibuja nada.
     */
    public static void drawCenteredText(Graphics2D g, String text, Rectangle area, Color color) {
        if (text == null || text.isEmpty() || area == null) {
            return;
        }
        FontMetrics metrics = g.getFontMetrics();
        int x = area.x + (area.width - metrics.stringWidth(text)) / 2;
        int y = area.y + (area.height - metrics.getHeight()) / 2 + metrics.getAscent();
        drawShadowedText(g, text, x, y, color);
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
