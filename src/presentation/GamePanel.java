package presentation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import domain.Ball;
import domain.Field;
import domain.GameCharacter;
import domain.GameMode;
import domain.Goal;
import domain.HumanController;
import domain.Match;
import domain.MatchState;
import domain.MatchType;
import domain.Player;
import domain.PlayerController;
import domain.Score;
import domain.Side;
import domain.Winner;

/**
 * Panel de juego: dibuja la cancha con Java2D, ejecuta el bucle de simulacion
 * y traduce el teclado en acciones para los controladores humanos.
 */
@SuppressWarnings("serial")
public final class GamePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final double STEP = 1.0 / 120.0;
    private static final long MAX_FRAME_NANOS = 250_000_000L;
    private static final int TIMER_DELAY = 16;

    private final GameSession session;
    private final Runnable onMenu;
    private final Runnable onRematch;
    private final Timer timer;
    private final ArrayList<CrowdDot> crowd = new ArrayList<CrowdDot>();
    private final Set<Integer> leftKeys = new HashSet<Integer>();
    private final Set<Integer> rightKeys = new HashSet<Integer>();

    private long lastNanos;
    private double accumulator;
    private double animationTime;
    private String banner;
    private long bannerUntil;
    private MatchState lastState;
    private int lastLeftGoals;
    private int lastRightGoals;
    private int lastRoundsWon;

    public GamePanel(GameSession session, Runnable onMenu, Runnable onRematch) {
        if (session == null) {
            throw new IllegalArgumentException("La sesion no puede ser null");
        }
        this.session = session;
        this.onMenu = onMenu;
        this.onRematch = onRematch;
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(true);
        setBackground(UiTheme.BG_TOP);
        setFocusable(true);

        session.start();
        Match match = session.getMatch();
        lastState = match.getState();
        lastLeftGoals = match.getScore().getLeftGoals();
        lastRightGoals = match.getScore().getRightGoals();
        lastRoundsWon = 0;

        generateCrowd();
        installKeyBindings();
        timer = new Timer(TIMER_DELAY, this);
        timer.setCoalesce(true);
        lastNanos = System.nanoTime();
        timer.start();
    }

    private void generateCrowd() {
        Color[] palette = new Color[] {
            new Color(236, 196, 90), new Color(214, 92, 86), new Color(86, 152, 214),
            new Color(154, 196, 120), new Color(220, 220, 230), new Color(172, 118, 208)
        };
        Random random = new Random(20261108L);
        for (int i = 0; i < 190; i++) {
            int x = 18 + random.nextInt(1244);
            int y = 24 + random.nextInt(104);
            int size = 3 + random.nextInt(4);
            crowd.add(new CrowdDot(x, y, size, palette[random.nextInt(palette.length)]));
        }
    }

    private void installKeyBindings() {
        bindPress(KeyEvent.VK_A, "leftLeft", () -> {
            leftKeys.add(KeyEvent.VK_A);
            updateLeftDirection();
        });
        bindRelease(KeyEvent.VK_A, "leftLeft", () -> {
            leftKeys.remove(KeyEvent.VK_A);
            updateLeftDirection();
        });
        bindPress(KeyEvent.VK_D, "leftRight", () -> {
            leftKeys.add(KeyEvent.VK_D);
            updateLeftDirection();
        });
        bindRelease(KeyEvent.VK_D, "leftRight", () -> {
            leftKeys.remove(KeyEvent.VK_D);
            updateLeftDirection();
        });
        bindPress(KeyEvent.VK_W, "leftJump", () -> {
            HumanController controller = leftHuman();
            if (controller != null) {
                controller.requestJump();
            }
        });
        bindPress(KeyEvent.VK_S, "leftKick", () -> {
            HumanController controller = leftHuman();
            if (controller != null) {
                controller.requestKick();
            }
        });
        bindPress(KeyEvent.VK_Q, "leftPower", () -> {
            HumanController controller = leftHuman();
            if (controller != null) {
                controller.requestPower();
            }
        });

        bindWithCtrl(KeyEvent.VK_LEFT, "rightLeft", false, () -> {
            rightKeys.add(KeyEvent.VK_LEFT);
            updateRightDirection();
        });
        bindWithCtrl(KeyEvent.VK_LEFT, "rightLeft", true, () -> {
            rightKeys.remove(KeyEvent.VK_LEFT);
            updateRightDirection();
        });
        bindWithCtrl(KeyEvent.VK_RIGHT, "rightRight", false, () -> {
            rightKeys.add(KeyEvent.VK_RIGHT);
            updateRightDirection();
        });
        bindWithCtrl(KeyEvent.VK_RIGHT, "rightRight", true, () -> {
            rightKeys.remove(KeyEvent.VK_RIGHT);
            updateRightDirection();
        });
        bindWithCtrl(KeyEvent.VK_UP, "rightJump", false, () -> {
            HumanController controller = rightHuman();
            if (controller != null) {
                controller.requestJump();
            }
        });
        bindWithCtrl(KeyEvent.VK_DOWN, "rightKick", false, () -> {
            HumanController controller = rightHuman();
            if (controller != null) {
                controller.requestKick();
            }
        });
        // La tecla Control llega con CTRL_DOWN_MASK en modifiersEx, por eso
        // no se puede enlazar como una tecla normal sin modificadores.
        bind(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK, "rightPower", false, () -> {
            HumanController controller = rightHuman();
            if (controller != null) {
                controller.requestPower();
            }
        });

        bindPress(KeyEvent.VK_ESCAPE, "pause", this::togglePause);
        bindPress(KeyEvent.VK_P, "pauseKey", this::togglePause);
        bindPress(KeyEvent.VK_M, "menu", () -> {
            if (onMenu != null) {
                onMenu.run();
            }
        });
        bindPress(KeyEvent.VK_R, "rematch", this::rematch);
        bindPress(KeyEvent.VK_ENTER, "rematchEnter", this::rematch);
    }

    private void togglePause() {
        if (session.isFinished()) {
            return;
        }
        session.togglePause();
        accumulator = 0;
        clearInput();
        repaint();
    }

    private void rematch() {
        if (session.isFinished() && onRematch != null) {
            onRematch.run();
        }
    }

    private void bindPress(int keyCode, String name, Runnable action) {
        bind(keyCode, 0, name, false, action);
    }

    private void bindRelease(int keyCode, String name, Runnable action) {
        bind(keyCode, 0, name, true, action);
    }

    /**
     * Enlaza la tecla con y sin Ctrl. Asi las flechas del jugador derecho
     * siguen respondiendo mientras mantiene Ctrl para lanzar su poder.
     */
    private void bindWithCtrl(int keyCode, String name, boolean release, Runnable action) {
        bind(keyCode, 0, name, release, action);
        bind(keyCode, InputEvent.CTRL_DOWN_MASK, name + ".ctrl", release, action);
    }

    private void bind(int keyCode, int modifiers, String name, boolean release, Runnable action) {
        String actionName = name + (release ? ".released" : ".pressed");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, modifiers, release), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private HumanController leftHuman() {
        PlayerController controller = session.getLeftController();
        if (controller instanceof HumanController) {
            return (HumanController) controller;
        }
        return null;
    }

    private HumanController rightHuman() {
        PlayerController controller = session.getRightController();
        if (controller instanceof HumanController) {
            return (HumanController) controller;
        }
        return null;
    }

    private void updateLeftDirection() {
        HumanController controller = leftHuman();
        if (controller == null) {
            return;
        }
        int direction = 0;
        if (leftKeys.contains(KeyEvent.VK_D)) {
            direction++;
        }
        if (leftKeys.contains(KeyEvent.VK_A)) {
            direction--;
        }
        controller.setDirection(direction);
    }

    private void updateRightDirection() {
        HumanController controller = rightHuman();
        if (controller == null) {
            return;
        }
        int direction = 0;
        if (rightKeys.contains(KeyEvent.VK_RIGHT)) {
            direction++;
        }
        if (rightKeys.contains(KeyEvent.VK_LEFT)) {
            direction--;
        }
        controller.setDirection(direction);
    }

    /**
     * Suelta todas las teclas, por ejemplo cuando la ventana pierde el foco.
     */
    public void clearInput() {
        leftKeys.clear();
        rightKeys.clear();
        HumanController left = leftHuman();
        if (left != null) {
            left.setDirection(0);
            left.clearRequests();
        }
        HumanController right = rightHuman();
        if (right != null) {
            right.setDirection(0);
            right.clearRequests();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        long now = System.nanoTime();
        long elapsed = now - lastNanos;
        lastNanos = now;
        if (elapsed < 0) {
            elapsed = 0;
        }
        if (elapsed > MAX_FRAME_NANOS) {
            elapsed = MAX_FRAME_NANOS;
        }
        double frameSeconds = elapsed / 1_000_000_000.0;
        animationTime += frameSeconds;

        if (session.isPaused() || session.isFinished()) {
            accumulator = 0;
            repaint();
            return;
        }

        accumulator += frameSeconds;
        int guard = 0;
        while (accumulator >= STEP && guard < 400) {
            session.update(STEP);
            accumulator -= STEP;
            guard++;
            if (session.isFinished()) {
                break;
            }
        }
        if (guard >= 400) {
            accumulator = 0;
        }
        updateEvents(now);
        repaint();
    }

    private void updateEvents(long now) {
        Match match = session.getMatch();
        if (match == null) {
            return;
        }
        Score score = match.getScore();
        MatchState state = match.getState();

        if (state == MatchState.GOAL && lastState != MatchState.GOAL) {
            String scorer = "";
            if (score.getLeftGoals() > lastLeftGoals) {
                scorer = session.getLeftCharacter().getName();
            } else if (score.getRightGoals() > lastRightGoals) {
                scorer = session.getRightCharacter().getName();
            }
            banner = scorer.isEmpty() ? "GOL" : "GOL DE " + scorer.toUpperCase();
            bannerUntil = now + 1_200_000_000L;
        }

        if (session.isSurvival()) {
            int roundsWon = session.getRoundsWon();
            if (roundsWon > lastRoundsWon) {
                banner = "RONDA GANADA";
                bannerUntil = now + 2_000_000_000L;
            }
            lastRoundsWon = roundsWon;
        }

        if (session.isFinished()) {
            banner = null;
        }

        lastState = state;
        lastLeftGoals = score.getLeftGoals();
        lastRightGoals = score.getRightGoals();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        drawBackground(g2);

        Match match = session.getMatch();
        if (match != null) {
            Field field = match.getField();
            drawArena(g2, field);
            drawGoal(g2, field.getLeftGoal(), true);
            drawGoal(g2, field.getRightGoal(), false);
            drawMarkings(g2, field);
            drawPlayer(g2, match.getLeftPlayer(), match.getBall(), true);
            drawPlayer(g2, match.getRightPlayer(), match.getBall(), false);
            drawBall(g2, match.getBall());
            drawHud(g2, match);
        }

        drawBanner(g2);
        if (session.isPaused()) {
            drawPauseOverlay(g2);
        }
        if (session.isFinished()) {
            drawResultOverlay(g2, match);
        }
        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        UiTheme.paintVerticalGradient(g2, getWidth(), getHeight(),
                UiTheme.BG_TOP, UiTheme.BG_BOTTOM);

        g2.setColor(new Color(13, 21, 36));
        g2.fillRect(0, 0, getWidth(), 150);
        for (CrowdDot dot : crowd) {
            g2.setColor(UiTheme.withAlpha(dot.color, 150));
            g2.fillOval(dot.x, dot.y, dot.size, dot.size);
        }
        g2.setColor(new Color(8, 13, 24));
        g2.fillRect(0, 140, getWidth(), 14);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(getWidth() * 0.18, -30),
                520,
                new float[] {0f, 1f},
                new Color[] {UiTheme.withAlpha(UiTheme.GOLD, 42),
                        UiTheme.withAlpha(UiTheme.GOLD, 0)}));
        g2.fillRect(0, 0, getWidth() / 2, 430);
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(getWidth() * 0.82, -30),
                520,
                new float[] {0f, 1f},
                new Color[] {UiTheme.withAlpha(UiTheme.RIGHT_COLOR, 42),
                        UiTheme.withAlpha(UiTheme.RIGHT_COLOR, 0)}));
        g2.fillRect(getWidth() / 2, 0, getWidth() / 2, 430);
    }

    private void drawArena(Graphics2D g2, Field field) {
        int width = getWidth();
        int ground = (int) field.getGroundY();
        g2.setPaint(new GradientPaint(0, 150, new Color(19, 42, 43),
                0, ground, new Color(11, 24, 28)));
        g2.fillRect(0, 154, width, ground - 154);

        g2.setColor(UiTheme.withAlpha(Color.WHITE, 10));
        for (int y = 200; y < ground; y += 56) {
            g2.drawLine(0, y, width, y);
        }
        for (int x = 0; x < width; x += 80) {
            g2.drawLine(x, 154, x, ground);
        }

        int cx = width / 2;
        g2.setColor(UiTheme.withAlpha(Color.WHITE, 14));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(cx - 170, 300, 340, 340);
        g2.drawOval(cx - 90, 380, 180, 180);
        g2.setFont(new Font("SansSerif", Font.BOLD, 42));
        drawCentered(g2, "POOB", cx, 448, UiTheme.withAlpha(Color.WHITE, 18));

        g2.setPaint(new GradientPaint(0, ground, new Color(40, 112, 62),
                0, getHeight(), new Color(19, 62, 40)));
        g2.fillRect(0, ground, width, getHeight() - ground);
        for (int x = 0; x < width; x += 96) {
            g2.setColor(UiTheme.withAlpha(Color.BLACK, 22));
            g2.fillRect(x, ground, 48, getHeight() - ground);
        }
        g2.setColor(UiTheme.withAlpha(Color.WHITE, 130));
        g2.fillRect(0, ground, width, 3);
    }

    private void drawMarkings(Graphics2D g2, Field field) {
        int ground = (int) field.getGroundY();
        int cx = getWidth() / 2;

        g2.setColor(UiTheme.withAlpha(Color.WHITE, 50));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(cx, 170, cx, ground);
        g2.drawOval(cx - 115, ground + 18, 230, 42);

        g2.setColor(UiTheme.withAlpha(Color.WHITE, 85));
        g2.setStroke(new BasicStroke(3));
        g2.drawLine((int) field.getLeftGoalLineX(), (int) field.getLeftGoal().getY(),
                (int) field.getLeftGoalLineX(), ground);
        g2.drawLine((int) field.getRightGoalLineX(), (int) field.getRightGoal().getY(),
                (int) field.getRightGoalLineX(), ground);
    }

    private void drawGoal(Graphics2D g2, Goal goal, boolean left) {
        int x = (int) goal.getX();
        int y = (int) goal.getY();
        int width = (int) goal.getWidth();
        int height = (int) goal.getHeight();

        g2.setColor(new Color(8, 15, 27, 235));
        g2.fillRect(x, y, width, height);

        Shape oldClip = g2.getClip();
        g2.clipRect(x, y, width, height);
        g2.setColor(UiTheme.withAlpha(new Color(150, 190, 225), 70));
        g2.setStroke(new BasicStroke(1));
        for (int i = x - height; i < x + width + height; i += 15) {
            g2.drawLine(i, y, i + height, y + height);
            g2.drawLine(i, y + height, i + height, y);
        }
        g2.setClip(oldClip);

        g2.setColor(new Color(226, 238, 252));
        g2.fillRect(x, y, width, 6);
        if (left) {
            g2.fillRect(x, y, 6, height);
            g2.fillRect(x + width - 7, y, 7, height);
        } else {
            g2.fillRect(x + width - 6, y, 6, height);
            g2.fillRect(x, y, 7, height);
        }
        g2.setColor(UiTheme.withAlpha(new Color(226, 238, 252), 120));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, width, height);
    }

    private void drawPlayer(Graphics2D g2, Player player, Ball ball, boolean left) {
        Rectangle2D.Double bounds = player.getBounds();
        double cx = player.getX();
        double ground = session.getField().getGroundY();
        Color sideColor = left ? UiTheme.LEFT_COLOR : UiTheme.RIGHT_COLOR;
        Color primary = characterPrimary(player.getCharacter());
        Color secondary = characterSecondary(player.getCharacter());

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval((int) (cx - bounds.width * 0.62), (int) (ground - 10),
                (int) (bounds.width * 1.24), 16);

        double bodyWidth = bounds.width * 0.58;
        double bodyTop = bounds.y + bounds.width * 0.72;
        double bodyBottom = bounds.y + bounds.height;
        double legTop = bodyBottom - bounds.height * 0.18;

        g2.setColor(UiTheme.darken(primary, 0.42f));
        double legWidth = bodyWidth * 0.34;
        g2.fillRoundRect((int) (cx - bodyWidth / 2), (int) legTop,
                (int) legWidth, (int) (bodyBottom - legTop), 8, 8);
        g2.fillRoundRect((int) (cx + bodyWidth / 2 - legWidth), (int) legTop,
                (int) legWidth, (int) (bodyBottom - legTop), 8, 8);

        g2.setColor(primary);
        g2.fillRoundRect((int) (cx - bodyWidth / 2), (int) bodyTop,
                (int) bodyWidth, (int) (bodyBottom - bodyTop - bounds.height * 0.1),
                16, 16);
        g2.setColor(sideColor);
        g2.fillRoundRect((int) (cx - bodyWidth / 2 + 4), (int) (bodyTop + 6),
                (int) (bodyWidth * 0.22), (int) (bodyBottom - bodyTop - 16), 8, 8);

        double headRadius = bounds.width / 2;
        double headCenterY = bounds.y + headRadius;
        Ellipse2D head = new Ellipse2D.Double(cx - headRadius, headCenterY - headRadius,
                headRadius * 2, headRadius * 2);

        g2.setColor(primary);
        g2.fill(head);
        Shape oldClip = g2.getClip();
        g2.clip(head);
        g2.setColor(secondary);
        g2.fillRect((int) (cx - headRadius), (int) (headCenterY - headRadius),
                (int) (headRadius * 2), (int) (headRadius * 0.42));
        g2.fillRect((int) (cx - headRadius), (int) (headCenterY + headRadius * 0.58),
                (int) (headRadius * 2), (int) (headRadius * 0.42));
        g2.setClip(oldClip);

        g2.setColor(UiTheme.withAlpha(Color.WHITE, 55));
        g2.fillOval((int) (cx - headRadius * 0.58), (int) (headCenterY - headRadius * 0.62),
                (int) (headRadius * 0.6), (int) (headRadius * 0.38));

        g2.setStroke(new BasicStroke(5f));
        g2.setColor(sideColor);
        g2.draw(head);

        double eyeOffset = headRadius * 0.34;
        double eyeRadius = headRadius * 0.21;
        double eyeY = headCenterY - headRadius * 0.12;
        drawEye(g2, cx - eyeOffset, eyeY, eyeRadius, ball, cx, headCenterY);
        drawEye(g2, cx + eyeOffset, eyeY, eyeRadius, ball, cx, headCenterY);

        g2.setColor(UiTheme.darken(primary, 0.55f));
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(new Arc2D.Double(cx - headRadius * 0.26, headCenterY + headRadius * 0.28,
                headRadius * 0.52, headRadius * 0.3, 200, 140, Arc2D.OPEN));

        if (player.isPowerArmed()) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(animationTime * 8));
            g2.setColor(UiTheme.withAlpha(UiTheme.GOLD, (int) (70 + 90 * pulse)));
            g2.setStroke(new BasicStroke(5f));
            g2.draw(new RoundRectangle2D.Double(bounds.x - 7, bounds.y - 7,
                    bounds.width + 14, bounds.height + 14, 26, 26));
        }
        if (player.isFrozen()) {
            drawFrozenPlayer(g2, bounds);
        }
        if (player.isStunned()) {
            drawStunStars(g2, cx, headCenterY - headRadius - 12);
        }
        if (player.isConfused()) {
            drawConfused(g2, cx, headCenterY - headRadius - 16);
        }
    }

    private void drawEye(Graphics2D g2, double x, double y, double radius,
                         Ball ball, double headX, double headY) {
        g2.setColor(new Color(250, 250, 255));
        g2.fill(new Ellipse2D.Double(x - radius, y - radius * 1.15,
                radius * 2, radius * 2.3));
        double dx = ball.getX() - headX;
        double dy = ball.getY() - headY;
        double length = Math.sqrt(dx * dx + dy * dy);
        double shift = radius * 0.38;
        double pupilX = x;
        double pupilY = y;
        if (length > 0.001) {
            pupilX += dx / length * shift;
            pupilY += dy / length * shift;
        }
        g2.setColor(new Color(22, 28, 42));
        g2.fill(new Ellipse2D.Double(pupilX - radius * 0.45, pupilY - radius * 0.52,
                radius * 0.9, radius * 1.04));
    }

    private void drawFrozenPlayer(Graphics2D g2, Rectangle2D.Double bounds) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
        g2.setColor(UiTheme.ICE);
        g2.fill(new RoundRectangle2D.Double(bounds.x - 5, bounds.y - 5,
                bounds.width + 10, bounds.height + 10, 18, 18));
        g2.setComposite(old);
        g2.setColor(new Color(220, 248, 255));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(new RoundRectangle2D.Double(bounds.x - 5, bounds.y - 5,
                bounds.width + 10, bounds.height + 10, 18, 18));
        drawSnowflake(g2, bounds.getCenterX(), bounds.getCenterY(), bounds.width * 0.32);
    }

    private void drawSnowflake(Graphics2D g2, double x, double y, double radius) {
        g2.setColor(UiTheme.withAlpha(Color.WHITE, 210));
        g2.setStroke(new BasicStroke(2.4f));
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * i / 3;
            double x2 = x + Math.cos(angle) * radius;
            double y2 = y + Math.sin(angle) * radius;
            g2.drawLine((int) x, (int) y, (int) x2, (int) y2);
            double branch = radius * 0.32;
            double bx = x + Math.cos(angle) * radius * 0.68;
            double by = y + Math.sin(angle) * radius * 0.68;
            g2.drawLine((int) bx, (int) by,
                    (int) (bx + Math.cos(angle + 0.7) * branch),
                    (int) (by + Math.sin(angle + 0.7) * branch));
            g2.drawLine((int) bx, (int) by,
                    (int) (bx + Math.cos(angle - 0.7) * branch),
                    (int) (by + Math.sin(angle - 0.7) * branch));
        }
    }

    private void drawStunStars(Graphics2D g2, double x, double y) {
        g2.setColor(UiTheme.GOLD);
        for (int i = 0; i < 3; i++) {
            double angle = animationTime * 3.2 + i * Math.PI * 2 / 3;
            double sx = x + Math.cos(angle) * 18;
            double sy = y + Math.sin(angle) * 5;
            g2.fillOval((int) (sx - 4), (int) (sy - 4), 8, 8);
            g2.setColor(UiTheme.withAlpha(UiTheme.GOLD, 150));
            g2.drawOval((int) (sx - 7), (int) (sy - 7), 14, 14);
            g2.setColor(UiTheme.GOLD);
        }
    }

    private void drawConfused(Graphics2D g2, double x, double y) {
        g2.setColor(UiTheme.withAlpha(new Color(220, 130, 255), 220));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 2; i++) {
            double offset = animationTime * 2 + i * Math.PI;
            g2.draw(new Arc2D.Double(x - 18 + Math.cos(offset) * 4, y - 6,
                    36, 16, 30 + i * 40, 250, Arc2D.OPEN));
        }
    }

    private void drawBall(Graphics2D g2, Ball ball) {
        double ground = session.getField().getGroundY();
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval((int) (ball.getX() - ball.getRadius() * 1.1), (int) (ground - 7),
                (int) (ball.getRadius() * 2.2), 11);

        if (ball.isPushCharged()) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(animationTime * 9));
            g2.setColor(UiTheme.withAlpha(new Color(86, 200, 255), (int) (70 + 80 * pulse)));
            double glow = ball.getRadius() * (1.9 + pulse * 0.5);
            g2.fill(new Ellipse2D.Double(ball.getX() - glow, ball.getY() - glow,
                    glow * 2, glow * 2));
        }

        if (ball.isIllusionActive()) {
            int decoys = Math.max(1, ball.getDecoyCount());
            for (int i = 0; i < decoys; i++) {
                double angle = animationTime * 2.4 + i * Math.PI * 2 / decoys;
                double distance = ball.getRadius() * (2.6 + 0.5 * Math.sin(animationTime * 3 + i));
                double x = ball.getX() + Math.cos(angle) * distance * 1.5;
                double y = ball.getY() + Math.sin(angle) * distance * 0.8;
                drawBallShape(g2, x, y, ball.getRadius() * 0.9, 90);
            }
        }

        drawBallShape(g2, ball.getX(), ball.getY(), ball.getRadius(), 255);
        if (ball.isIllusionActive()) {
            g2.setColor(UiTheme.withAlpha(UiTheme.GOLD, 190));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(ball.getX() - ball.getRadius() - 4,
                    ball.getY() - ball.getRadius() - 4,
                    (ball.getRadius() + 4) * 2, (ball.getRadius() + 4) * 2));
        }
    }

    private void drawBallShape(Graphics2D g2, double x, double y, double radius, int alpha) {
        Composite old = g2.getComposite();
        if (alpha < 255) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
        }
        Ellipse2D circle = new Ellipse2D.Double(x - radius, y - radius,
                radius * 2, radius * 2);
        g2.setColor(new Color(247, 250, 255));
        g2.fill(circle);

        double rotation = x * 0.018 + y * 0.011;
        g2.setColor(new Color(24, 33, 52));
        g2.fill(pentagon(x, y, radius * 0.4, rotation));
        for (int i = 0; i < 5; i++) {
            double angle = rotation + i * Math.PI * 2 / 5;
            double px = x + Math.cos(angle) * radius * 0.68;
            double py = y + Math.sin(angle) * radius * 0.68;
            g2.fill(pentagon(px, py, radius * 0.2, angle + 0.4));
        }
        g2.setColor(new Color(44, 58, 84));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(circle);
        g2.setComposite(old);
    }

    private Path2D pentagon(double x, double y, double radius, double rotation) {
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < 5; i++) {
            double angle = rotation + i * Math.PI * 2 / 5 - Math.PI / 2;
            double px = x + Math.cos(angle) * radius;
            double py = y + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

    private void drawHud(Graphics2D g2, Match match) {
        Score score = match.getScore();
        int boardWidth = 620;
        int boardHeight = 96;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = 16;

        g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 225));
        g2.fill(new RoundRectangle2D.Double(boardX, boardY, boardWidth, boardHeight, 22, 22));
        g2.setColor(UiTheme.withAlpha(UiTheme.BORDER, 220));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(boardX, boardY, boardWidth, boardHeight, 22, 22));

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.setColor(UiTheme.LEFT_COLOR);
        g2.drawString(session.getLeftCharacter().getName().toUpperCase(), boardX + 22, boardY + 30);
        g2.setColor(UiTheme.RIGHT_COLOR);
        drawRight(g2, session.getRightCharacter().getName().toUpperCase(),
                boardX + boardWidth - 22, boardY + 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(UiTheme.MUTED);
        g2.drawString(session.getLeftCharacter().getPower().getName(), boardX + 22, boardY + 49);
        drawRight(g2, session.getRightCharacter().getPower().getName(),
                boardX + boardWidth - 22, boardY + 49);

        g2.setFont(new Font("SansSerif", Font.BOLD, 46));
        g2.setColor(UiTheme.TEXT);
        drawCentered(g2, score.getLeftGoals() + "  -  " + score.getRightGoals(),
                getWidth() / 2, boardY + 56);

        String subtitle = matchSubtitle(match);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(match.isGoldenGoal() ? UiTheme.GOLD : UiTheme.MUTED);
        drawCentered(g2, subtitle, getWidth() / 2, boardY + 82);

        if (session.getConfig().getMatchType() == MatchType.SURVIVAL) {
            drawBallsLeft(g2, true);
            drawBallsLeft(g2, false);
        }

        drawPowerBar(g2, match.getLeftPlayer(), true);
        drawPowerBar(g2, match.getRightPlayer(), false);
        drawBottomHints(g2, match);
    }

    private String matchSubtitle(Match match) {
        MatchType type = session.getConfig().getMatchType();
        if (match.isGoldenGoal()) {
            return "GOL DE ORO";
        }
        if (type == MatchType.TIME) {
            return formatTime(match.getTimeLeft());
        }
        if (type == MatchType.GOALS) {
            return "PRIMERO A " + session.getConfig().getGoalsTarget();
        }
        return "RONDA " + (session.getRoundsPlayed() + 1)
                + "   |   GANADAS " + session.getRoundsWon();
    }

    private String formatTime(double seconds) {
        int total = (int) Math.ceil(Math.max(0, seconds));
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private void drawBallsLeft(Graphics2D g2, boolean left) {
        int remaining = session.getRemainingBalls(left ? Side.LEFT : Side.RIGHT);
        int x = left ? 34 : getWidth() - 34;
        int y = 44;
        drawBallIcon(g2, x, y, 11);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(left ? UiTheme.LEFT_COLOR : UiTheme.RIGHT_COLOR);
        String text = "x " + remaining;
        if (left) {
            g2.drawString(text, x + 20, y + 8);
        } else {
            drawRight(g2, text, x - 20, y + 8);
        }
    }

    private void drawBallIcon(Graphics2D g2, double x, double y, double radius) {
        g2.setColor(new Color(247, 250, 255));
        g2.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
        g2.setColor(new Color(26, 36, 56));
        g2.fill(pentagon(x, y, radius * 0.45, 0.3));
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
    }

    private void drawPowerBar(Graphics2D g2, Player player, boolean left) {
        int barWidth = 310;
        int barHeight = 18;
        int x = left ? 36 : getWidth() - 36 - barWidth;
        int y = getHeight() - 62;
        Color color = left ? UiTheme.LEFT_COLOR : UiTheme.RIGHT_COLOR;

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(UiTheme.TEXT);
        String label = player.getCharacter().getName() + "  |  "
                + player.getCharacter().getPower().getName();
        if (left) {
            g2.drawString(label, x, y - 8);
        } else {
            drawRight(g2, label, x + barWidth, y - 8);
        }

        g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 230));
        g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barHeight, 12, 12));
        g2.setColor(UiTheme.withAlpha(UiTheme.BORDER, 230));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(x, y, barWidth, barHeight, 12, 12));

        double charge = Math.max(0, Math.min(1, player.getPowerCharge()));
        if (charge > 0) {
            double fillWidth = Math.max(4, (barWidth - 4) * charge);
            g2.setPaint(new GradientPaint((float) x, 0, UiTheme.darken(color, 0.18f),
                    (float) (x + barWidth), 0, UiTheme.lighten(color, 0.28f)));
            g2.fill(new RoundRectangle2D.Double(x + 2, y + 2, fillWidth, barHeight - 4, 9, 9));
        }

        for (int i = 1; i < 4; i++) {
            int tickX = x + barWidth * i / 4;
            g2.setColor(UiTheme.withAlpha(UiTheme.BG_TOP, 120));
            g2.drawLine(tickX, y + 3, tickX, y + barHeight - 3);
        }

        if (player.isPowerReady()) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(animationTime * 7));
            g2.setColor(UiTheme.withAlpha(UiTheme.GOLD, (int) (150 + 90 * pulse)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(new RoundRectangle2D.Double(x - 2, y - 2,
                    barWidth + 4, barHeight + 4, 14, 14));
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(UiTheme.TEXT);
        boolean human = left ? leftHuman() != null : rightHuman() != null;
        String status;
        if (player.isPowerArmed()) {
            status = "ARMADO - GOLPEA EL BALON";
        } else if (player.isPowerReady()) {
            if (!human) {
                status = "LISTO";
            } else {
                status = left ? "LISTO - PRESIONA Q" : "LISTO - PRESIONA CTRL";
            }
        } else {
            status = (int) Math.round(charge * 100) + "%";
        }
        drawCentered(g2, status, x + barWidth / 2, y + 13);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(UiTheme.MUTED);
        String hint;
        if (!human) {
            hint = "MAQUINA";
        } else if (left) {
            hint = "A/D mover   W saltar   S patear   Q poder";
        } else {
            hint = "Flechas mover   Arriba saltar   Abajo patear   Ctrl poder";
        }
        if (left) {
            g2.drawString(hint, x, getHeight() - 16);
        } else {
            drawRight(g2, hint, x + barWidth, getHeight() - 16);
        }
    }

    private void drawBottomHints(Graphics2D g2, Match match) {
        GameMode mode = session.getConfig().getGameMode();
        String text;
        if (session.isSurvival()) {
            text = "SUPERVIVENCIA";
        } else if (mode == GameMode.PVP) {
            text = "1 VS 1";
        } else if (mode == GameMode.PVM) {
            text = "HUMANO VS MAQUINA";
        } else {
            text = "MAQUINA VS MAQUINA";
        }
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(UiTheme.withAlpha(UiTheme.MUTED, 190));
        drawCentered(g2, text, getWidth() / 2, getHeight() - 16);
        if (!session.isFinished()) {
            drawCentered(g2, "ESC pausa   M menu", getWidth() / 2, getHeight() - 32);
        }
    }

    private void drawBanner(Graphics2D g2) {
        if (banner == null) {
            return;
        }
        long now = System.nanoTime();
        if (now >= bannerUntil) {
            banner = null;
            return;
        }
        long remaining = bannerUntil - now;
        float alpha = 1f;
        if (remaining < 400_000_000L) {
            alpha = remaining / 400_000_000f;
        }
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setFont(new Font("SansSerif", Font.BOLD, 62));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(banner)) / 2;
        int y = 258;
        g2.setColor(UiTheme.withAlpha(Color.BLACK, 150));
        g2.drawString(banner, x + 3, y + 3);
        g2.setColor(UiTheme.GOLD);
        g2.drawString(banner, x, y);
        g2.setComposite(old);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(2, 6, 12, 170));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int width = 520;
        int height = 230;
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 245));
        g2.fill(new RoundRectangle2D.Double(x, y, width, height, 26, 26));
        g2.setColor(UiTheme.withAlpha(UiTheme.BORDER, 240));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, width, height, 26, 26));

        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        g2.setColor(UiTheme.TEXT);
        drawCentered(g2, "PAUSA", getWidth() / 2, y + 66);
        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.setColor(UiTheme.MUTED);
        drawCentered(g2, "ESC / P  continuar", getWidth() / 2, y + 118);
        drawCentered(g2, "M  volver al menu", getWidth() / 2, y + 150);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(UiTheme.withAlpha(UiTheme.MUTED, 190));
        drawCentered(g2, "La partida queda exactamente donde la dejaste",
                getWidth() / 2, y + 190);
    }

    private void drawResultOverlay(Graphics2D g2, Match match) {
        g2.setColor(new Color(2, 6, 12, 195));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int width = 660;
        int height = 380;
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 248));
        g2.fill(new RoundRectangle2D.Double(x, y, width, height, 30, 30));
        g2.setColor(UiTheme.withAlpha(UiTheme.BORDER, 240));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, width, height, 30, 30));
        g2.setColor(UiTheme.withAlpha(UiTheme.GOLD, 200));
        g2.fill(new RoundRectangle2D.Double(x + width * 0.3, y + 20, width * 0.4, 4, 4, 4));

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(UiTheme.GOLD);
        drawCentered(g2, session.isSurvival() ? "SERIE DE SUPERVIVENCIA" : "PARTIDO TERMINADO",
                getWidth() / 2, y + 58);

        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2.setColor(UiTheme.TEXT);
        drawCentered(g2, resultTitle(), getWidth() / 2, y + 112);

        if (match != null) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 56));
            g2.setColor(UiTheme.GOLD);
            drawCentered(g2, match.getScore().getLeftGoals() + "  -  "
                    + match.getScore().getRightGoals(), getWidth() / 2, y + 182);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.setColor(UiTheme.MUTED);
        if (session.isSurvival()) {
            drawCentered(g2, "Rondas ganadas: " + session.getRoundsWon(),
                    getWidth() / 2, y + 228);
            drawCentered(g2, "Rondas jugadas: " + session.getRoundsPlayed(),
                    getWidth() / 2, y + 256);
        } else {
            drawCentered(g2, session.getLeftCharacter().getName() + "  vs  "
                    + session.getRightCharacter().getName(), getWidth() / 2, y + 228);
            drawCentered(g2, "Poderes: " + session.getLeftCharacter().getPower().getName()
                    + "  /  " + session.getRightCharacter().getPower().getName(),
                    getWidth() / 2, y + 256);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(UiTheme.TEXT);
        drawCentered(g2, "R / ENTER  revancha          M  menu", getWidth() / 2, y + 326);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(UiTheme.withAlpha(UiTheme.MUTED, 180));
        drawCentered(g2, "La revancha conserva la misma configuracion",
                getWidth() / 2, y + 350);
    }

    private String resultTitle() {
        Winner winner = session.getWinner();
        if (session.isSurvival()) {
            return "SERIE TERMINADA";
        }
        if (winner == Winner.NONE) {
            return "EMPATE";
        }
        GameMode mode = session.getConfig().getGameMode();
        if (mode == GameMode.PVP) {
            return winner == Winner.LEFT ? "GANA JUGADOR 1" : "GANA JUGADOR 2";
        }
        if (mode == GameMode.PVM) {
            return winner == Winner.LEFT ? "GANASTE" : "GANO LA MAQUINA";
        }
        return winner == Winner.LEFT ? "GANA MAQUINA IZQUIERDA" : "GANA MAQUINA DERECHA";
    }

    private Color characterPrimary(GameCharacter character) {
        String name = character.getName();
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

    private Color characterSecondary(GameCharacter character) {
        String name = character.getName();
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

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY, Color color) {
        g2.setColor(color);
        drawCentered(g2, text, centerX, baselineY);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        g2.drawString(text, x, baselineY);
    }

    private void drawRight(Graphics2D g2, String text, int rightX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, rightX - metrics.stringWidth(text), baselineY);
    }

    @Override
    public void removeNotify() {
        if (timer != null) {
            timer.stop();
        }
        super.removeNotify();
    }

    private static class CrowdDot {
        private final int x;
        private final int y;
        private final int size;
        private final Color color;

        CrowdDot(int x, int y, int size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }
}
