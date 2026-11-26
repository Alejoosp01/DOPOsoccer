package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Boton con imagen dibujado dentro del panel de juego (coordenadas logicas 1280x720), con prueba de impacto de raton.
 */
final class HudButton {

    private final String imageName;
    private final String fallbackText;
    private final Runnable action;

    private int x;
    private int y;
    private int width;
    private int height;
    private boolean pressed;

    /**
     * Crea un boton con la imagen indicada; si la accion es null el boton es solo decorativo.
     */
    HudButton(String imageName, String fallbackText, Runnable action) {
        this.imageName = imageName;
        this.fallbackText = fallbackText == null ? "" : fallbackText;
        this.action = action;
    }

    /**
     * Define el rectangulo del boton en coordenadas logicas.
     */
    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Devuelve true si el punto (en coordenadas logicas) cae dentro del boton.
     */
    boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    /**
     * Devuelve true si el boton esta presionado con el raton.
     */
    boolean isPressed() {
        return pressed;
    }

    /**
     * Marca el boton como presionado si el punto cae dentro; devuelve si lo presiono.
     */
    boolean press(double px, double py) {
        pressed = action != null && contains(px, py);
        return pressed;
    }

    /**
     * Suelta el boton: ejecuta la accion solo si estaba presionado y se soltó dentro; devuelve si la ejecuto.
     */
    boolean release(double px, double py) {
        boolean fire = pressed && contains(px, py);
        pressed = false;
        if (fire && action != null) {
            action.run();
        }
        return fire;
    }

    /**
     * Quita el estado presionado sin ejecutar la accion.
     */
    void cancel() {
        pressed = false;
    }

    /**
     * Dibuja el boton; la imagen se escala (con cache) al tamaño fisico segun la escala dada.
     */
    void draw(Graphics2D g2, double scale) {
        String state = pressed ? "_pressed" : "_normal";
        int physicalW = Math.max(1, (int) Math.round(width * scale));
        int physicalH = Math.max(1, (int) Math.round(height * scale));
        BufferedImage image = SpriteLoader.scaled(SpriteLoader.BUTTONS + imageName + state
                + SpriteLoader.EXTENSION, physicalW, physicalH);
        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
            return;
        }
        drawFallback(g2);
    }

    /**
     * Dibuja el respaldo simple: rectangulo redondeado con texto.
     */
    private void drawFallback(Graphics2D g2) {
        g2.setColor(pressed ? UiTheme.ACCENT.darker() : UiTheme.ACCENT);
        g2.fill(new RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g2.setFont(UiTheme.font(Font.BOLD, Math.max(10, height / 4)));
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(fallbackText)) / 2;
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(fallbackText, textX, textY);
    }
}
