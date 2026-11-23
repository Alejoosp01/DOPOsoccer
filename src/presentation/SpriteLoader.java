package presentation;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import domain.BlueAuraShot;
import domain.ErrorLog;
import domain.GameCharacter;
import domain.IllusionShot;
import domain.LightningShot;
import domain.PowerShot;
import domain.TornadoShot;

/**
 * Carga las imagenes y fuentes de la carpeta resources desde el classpath.
 * Guarda cada imagen la primera vez que se pide para no leerla de disco en
 * cada cuadro. Si un archivo no existe devuelve null (o un arreglo vacio),
 * lo anota una sola vez en el log de errores y el juego sigue: quien dibuja
 * puede usar el dibujo de respaldo. Las rutas siguen resources/ASSETS.md.
 */
public final class SpriteLoader {

    public static final String CHARACTERS = "/sprites/characters/";
    public static final String BALL = "/sprites/ball/";
    public static final String SHOTS = "/sprites/shots/";
    public static final String EFFECTS = "/sprites/effects/";
    public static final String FIELD = "/field/";
    public static final String BACKGROUND = FIELD + "background.png";
    public static final String STADIUM = FIELD + "stadium.png";
    public static final String GOAL = FIELD + "goal.png";
    public static final String BUTTONS = "/ui/buttons/";
    public static final String HUD = "/ui/hud/";
    public static final String GOAL_BANNER = HUD + "goal_banner.png";
    public static final String CONFETTI = HUD + "confetti_burst.png";
    public static final String ICONS = "/ui/icons/";
    public static final String FLAGS = "/ui/flags/";
    public static final String FONTS = "/fonts/";
    public static final String EXTENSION = ".png";

    /** Filas de los cuadros del balon y de los tiros de poder. */
    public static final String ROLL = "roll";
    public static final String FLY = "fly";
    public static final String CHARGE = "charge";
    public static final String IMPACT = "impact";

    /** Lado de cada cuadro del balon y de los tiros (256x256), con el balon de 96 px en el centro. */
    public static final int BALL_FRAME_SIZE = 256;
    public static final int BALL_DIAMETER = 96;

    public static final String IDLE = "idle";
    public static final String RUN = "run";
    public static final String JUMP = "jump";
    public static final String KICK = "kick";
    public static final String HEADER = "header";
    public static final String POWER = "power";
    public static final String CELEBRATION = "celebration";
    public static final String DEFEAT = "defeat";

    /** Lado de cada cuadro de personaje en pixeles (los cuadros son de 384x384). */
    public static final int CELL_SIZE = 384;
    /** Punto del cuadro donde estan los pies del personaje, igual en todos los cuadros. */
    public static final int FEET_X = 192;
    public static final int FEET_Y = 340;
    /** Alto del personaje parado dentro del cuadro; se usa para escalarlo al alto del jugador. */
    public static final int CHARACTER_HEIGHT = 240;
    /** Maximo de cuadros que se buscan en una secuencia numerada. */
    public static final int MAX_SEQUENCE = 60;

    private static final Map<String, BufferedImage> IMAGES = new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage[]> FRAMES = new HashMap<String, BufferedImage[]>();
    private static final Map<String, BufferedImage[]> SEQUENCES = new HashMap<String, BufferedImage[]>();
    private static final Set<String> MISSING = new HashSet<String>();

    private SpriteLoader() {
    }

    /**
     * Devuelve la imagen de la ruta indicada (por ejemplo "/sprites/ball/ball.png"), o null si no existe.
     */
    public static BufferedImage load(String path) {
        if (path == null) {
            return null;
        }
        if (IMAGES.containsKey(path)) {
            return IMAGES.get(path);
        }
        BufferedImage image = read(path);
        if (image != null) {
            IMAGES.put(path, image);
        }
        return image;
    }

    /**
     * Devuelve si existe un archivo en esa ruta del classpath.
     */
    public static boolean exists(String path) {
        return path != null && SpriteLoader.class.getResource(path) != null;
    }

    /**
     * Devuelve los cuadros de una tira horizontal (cuadros cuadrados del alto de la imagen); vacio si no existe.
     */
    public static BufferedImage[] frames(String path) {
        if (path == null) {
            return new BufferedImage[0];
        }
        if (FRAMES.containsKey(path)) {
            return FRAMES.get(path);
        }
        BufferedImage strip = load(path);
        if (strip == null) {
            return new BufferedImage[0];
        }
        BufferedImage[] result = split(strip, strip.getHeight());
        FRAMES.put(path, result);
        return result;
    }

    /**
     * Devuelve los cuadros numerados base_1.png, base_2.png, ... hasta que falte uno; vacio si no hay ninguno.
     */
    public static BufferedImage[] sequence(String basePath) {
        if (basePath == null) {
            return new BufferedImage[0];
        }
        if (SEQUENCES.containsKey(basePath)) {
            return SEQUENCES.get(basePath);
        }
        ArrayList<BufferedImage> found = new ArrayList<BufferedImage>();
        for (int i = 1; i <= MAX_SEQUENCE; i++) {
            String path = basePath + "_" + i + EXTENSION;
            if (!exists(path)) {
                break;
            }
            BufferedImage image = load(path);
            if (image != null) {
                found.add(image);
            }
        }
        if (found.isEmpty()) {
            reportMissing(basePath + "_1" + EXTENSION);
        }
        BufferedImage[] result = found.toArray(new BufferedImage[found.size()]);
        SEQUENCES.put(basePath, result);
        return result;
    }

    /**
     * Divide una tira horizontal en cuadros del ancho indicado.
     */
    public static BufferedImage[] split(BufferedImage strip, int frameWidth) {
        if (strip == null || frameWidth <= 0) {
            return new BufferedImage[0];
        }
        int count = Math.max(1, strip.getWidth() / frameWidth);
        int width = Math.min(frameWidth, strip.getWidth());
        BufferedImage[] result = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            result[i] = strip.getSubimage(i * width, 0, width, strip.getHeight());
        }
        return result;
    }

    /**
     * Devuelve el cuadro que toca mostrar despues de los segundos indicados a la velocidad dada (cuadros por segundo).
     */
    public static BufferedImage frameAt(BufferedImage[] frames, double seconds, double framesPerSecond) {
        if (frames == null || frames.length == 0) {
            return null;
        }
        if (seconds <= 0 || framesPerSecond <= 0) {
            return frames[0];
        }
        int index = (int) Math.floor(seconds * framesPerSecond) % frames.length;
        return frames[index];
    }

    /**
     * Devuelve el cuadro de una animacion que se reproduce una sola vez: al acabar se queda en el ultimo.
     */
    public static BufferedImage frameOnce(BufferedImage[] frames, double seconds, double framesPerSecond) {
        if (frames == null || frames.length == 0) {
            return null;
        }
        if (seconds <= 0 || framesPerSecond <= 0) {
            return frames[0];
        }
        int index = (int) Math.floor(seconds * framesPerSecond);
        return frames[Math.min(index, frames.length - 1)];
    }

    /**
     * Devuelve una copia de la imagen volteada como en un espejo (para el jugador de la derecha).
     */
    public static BufferedImage flipHorizontally(BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flipped.createGraphics();
        AffineTransform mirror = new AffineTransform(-1, 0, 0, 1, image.getWidth(), 0);
        g2.drawImage(image, mirror, null);
        g2.dispose();
        return flipped;
    }

    /**
     * Dibuja la imagen en el rectangulo indicado; si flipped es true la dibuja volteada. Devuelve false si la imagen es null.
     */
    public static boolean draw(Graphics2D g2, BufferedImage image, double x, double y,
                               double width, double height, boolean flipped) {
        if (g2 == null || image == null) {
            return false;
        }
        int left = (int) Math.round(x);
        int top = (int) Math.round(y);
        int w = (int) Math.round(width);
        int h = (int) Math.round(height);
        Object oldInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (flipped) {
            g2.drawImage(image, left + w, top, -w, h, null);
        } else {
            g2.drawImage(image, left, top, w, h, null);
        }
        if (oldInterpolation != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
        return true;
    }

    /**
     * Dibuja un cuadro de personaje (384x384) con los pies en la base del cuerpo del jugador y escalado a su alto.
     * bodyX, bodyY, bodyWidth y bodyHeight son el rectangulo del jugador en la cancha; flipped lo voltea (jugador derecho).
     * Devuelve false si el cuadro es null, para que quien dibuja use el dibujo de respaldo.
     */
    public static boolean drawCharacter(Graphics2D g2, BufferedImage frame, double bodyX, double bodyY,
                                        double bodyWidth, double bodyHeight, boolean flipped) {
        if (g2 == null || frame == null || bodyHeight <= 0) {
            return false;
        }
        double scale = bodyHeight / CHARACTER_HEIGHT;
        double size = CELL_SIZE * scale;
        double feetX = bodyX + bodyWidth / 2;
        double feetY = bodyY + bodyHeight;
        double left = feetX - FEET_X * scale;
        double top = feetY - FEET_Y * scale;
        return draw(g2, frame, left, top, size, size, flipped);
    }

    /**
     * Dibuja un cuadro de balon (256x256 con el balon de 96 px al centro) centrado en (centerX, centerY) con el radio
     * del balon del juego, girado angle radianes y volteado si flipped. Devuelve false si el cuadro es null.
     */
    public static boolean drawBall(Graphics2D g2, BufferedImage frame, double centerX, double centerY,
                                   double radius, boolean flipped, double angle) {
        if (g2 == null || frame == null || radius <= 0) {
            return false;
        }
        double size = BALL_FRAME_SIZE * (2 * radius / BALL_DIAMETER);
        AffineTransform old = g2.getTransform();
        g2.rotate(angle, centerX, centerY);
        boolean drawn = draw(g2, frame, centerX - size / 2, centerY - size / 2, size, size, flipped);
        g2.setTransform(old);
        return drawn;
    }

    /**
     * Devuelve los cuadros de una fila del balon normal (por ejemplo ROLL); vacio si no hay imagenes.
     */
    public static BufferedImage[] ballFrames(String row) {
        if (row == null) {
            return new BufferedImage[0];
        }
        return sequence(BALL + row);
    }

    /**
     * Devuelve la carpeta de los cuadros de un tiro de poder (blue_aura, lightning, tornado o illusion), o null.
     */
    public static String shotFolder(PowerShot shot) {
        if (shot instanceof BlueAuraShot) {
            return "blue_aura";
        }
        if (shot instanceof LightningShot) {
            return "lightning";
        }
        if (shot instanceof TornadoShot) {
            return "tornado";
        }
        if (shot instanceof IllusionShot) {
            return "illusion";
        }
        return null;
    }

    /**
     * Devuelve los cuadros de una fila del balon con un tiro de poder (por ejemplo FLY); vacio si no hay imagenes.
     */
    public static BufferedImage[] shotFrames(PowerShot shot, String row) {
        String folder = shotFolder(shot);
        if (folder == null || row == null) {
            return new BufferedImage[0];
        }
        return sequence(SHOTS + folder + "/" + row);
    }

    /**
     * Devuelve la carpeta de sprites del personaje segun su pais (korea, cameroon, nigeria o usa), o null si no se conoce.
     */
    public static String characterFolder(GameCharacter character) {
        if (character == null) {
            return null;
        }
        String name = character.getName().trim().toLowerCase();
        if (name.startsWith("corea")) {
            return "korea";
        }
        if (name.startsWith("camer")) {
            return "cameroon";
        }
        if (name.startsWith("nigeria")) {
            return "nigeria";
        }
        if (name.startsWith("estados")) {
            return "usa";
        }
        return null;
    }

    /**
     * Devuelve la ruta base de los cuadros de una accion del personaje (por ejemplo ".../korea/run"), o null si no se conoce el pais.
     */
    public static String characterPath(GameCharacter character, String action) {
        String folder = characterFolder(character);
        if (folder == null || action == null) {
            return null;
        }
        return CHARACTERS + folder + "/" + action;
    }

    /**
     * Devuelve los cuadros de una accion del personaje (run_1.png, run_2.png, ...); vacio si no hay imagenes.
     */
    public static BufferedImage[] characterFrames(GameCharacter character, String action) {
        return sequence(characterPath(character, action));
    }

    /**
     * Devuelve la bandera del pais del personaje (ui/flags/pais.png), o null si no existe.
     */
    public static BufferedImage flag(GameCharacter character) {
        String folder = characterFolder(character);
        if (folder == null) {
            return null;
        }
        return load(FLAGS + folder + EXTENSION);
    }

    /**
     * Carga una fuente .ttf del tamaño indicado; si no existe devuelve la fuente de respaldo.
     */
    public static Font font(String path, float size, Font fallback) {
        if (path == null) {
            return fallback;
        }
        InputStream input = SpriteLoader.class.getResourceAsStream(path);
        if (input == null) {
            reportMissing(path);
            return fallback;
        }
        try {
            return Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(size);
        } catch (FontFormatException | IOException exception) {
            ErrorLog.record(exception);
            return fallback;
        } finally {
            closeQuietly(input);
        }
    }

    /**
     * Olvida las imagenes guardadas (por ejemplo si se cambian archivos mientras se prueba).
     */
    public static void clear() {
        IMAGES.clear();
        FRAMES.clear();
        SEQUENCES.clear();
        MISSING.clear();
    }

    /**
     * Lee una imagen del classpath; si falta o esta dañada lo anota y devuelve null.
     */
    private static BufferedImage read(String path) {
        URL url = SpriteLoader.class.getResource(path);
        if (url == null) {
            reportMissing(path);
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(url);
            if (image == null) {
                reportMissing(path);
            }
            return image;
        } catch (IOException exception) {
            ErrorLog.record(exception);
            return null;
        }
    }

    /**
     * Anota en el log que falta un archivo, una sola vez por ruta.
     */
    private static void reportMissing(String path) {
        if (MISSING.add(path)) {
            ErrorLog.record("No se encontro el asset " + path);
        }
    }

    /**
     * Cierra el flujo sin lanzar error.
     */
    private static void closeQuietly(InputStream input) {
        try {
            input.close();
        } catch (IOException exception) {
            ErrorLog.record(exception);
        }
    }
}
