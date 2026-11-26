package presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import domain.CharacterCatalog;
import domain.DOPOSoccerException;
import domain.ErrorLog;
import domain.GameCharacter;
import domain.GameMode;
import domain.MachineProfile;
import domain.MatchConfig;
import domain.PlayerProfile;
import domain.PowerShot;
import domain.StatType;
import domain.Stats;

/**
 * Pantalla de configuracion: modalidad, tipo de partido (minutos o goles),
 * resolucion de la cancha y el personaje de cada jugador.
 */
public final class SetupPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_MINUTES = 2;
    public static final int DEFAULT_GOALS = 3;

    private final transient Consumer<GameSetup> onStart;
    private final transient Runnable onBack;
    private final transient Runnable onProgressChanged;
    private transient PlayerProfile player1;
    private transient PlayerProfile player2;

    private final ChoiceChip pvpOption = new ChoiceChip("Jugador vs Jugador");
    private final ChoiceChip pvmOption = new ChoiceChip("Jugador vs Maquina");

    private final ChoiceChip timeOption = new ChoiceChip("Por tiempo");
    private final ChoiceChip goalsOption = new ChoiceChip("Por goles");

    private final ChoiceChip[] resolutionOptions = new ChoiceChip[Resolution.values().length];

    private final JLabel ruleLabel = UiTheme.label("Minutos", UiTheme.subtitleFont(), UiTheme.TEXT);
    private final JLabel valueLabel = UiTheme.label("2", UiTheme.font(Font.BOLD, 20), UiTheme.TEXT);
    private final JLabel ruleInfo = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);

    private final SectionHeader leftHeader = new SectionHeader("JUGADOR 1", UiTheme.LEFT_COLOR,
            "Controles: A / D mover  |  W saltar  |  S patear  |  Q poder");
    private final SectionHeader rightHeader = new SectionHeader("JUGADOR 2", UiTheme.RIGHT_COLOR,
            "Controles: Flechas mover y saltar  |  Abajo patear  |  Ctrl derecho poder");
    private final DetailArea leftDetail = new DetailArea();
    private final DetailArea rightDetail = new DetailArea();

    private final transient ArrayList<GameCharacter> characters;
    private final ArrayList<CharacterCard> leftCards = new ArrayList<CharacterCard>();
    private final ArrayList<CharacterCard> rightCards = new ArrayList<CharacterCard>();
    private final ArrayList<UpgradeRow> leftUpgrades = new ArrayList<UpgradeRow>();
    private final ArrayList<UpgradeRow> rightUpgrades = new ArrayList<UpgradeRow>();

    private final JComboBox<String> machineProfileBox = new JComboBox<>(
            new String[] { MachineProfile.CLUMSY.getDisplayName(), MachineProfile.OFFENSIVE.getDisplayName(),
                    MachineProfile.DEFENSIVE.getDisplayName(), MachineProfile.EXPERT.getDisplayName() });

    private static final String STEP_CONFIG = "config";
    private static final String STEP_TEAMS = "teams";
    private final CardLayout stepLayout = new CardLayout();
    private final JPanel steps = new JPanel(stepLayout);

    private int minutes = DEFAULT_MINUTES;
    private int goals = DEFAULT_GOALS;

    public SetupPanel(Consumer<GameSetup> onStart, Runnable onBack, Runnable onProgressChanged,
                      PlayerProfile player1, PlayerProfile player2) {
        this.onStart = onStart;
        this.onBack = onBack;
        this.onProgressChanged = onProgressChanged == null ? () -> { } : onProgressChanged;
        this.player1 = player1;
        this.player2 = player2;
        this.characters = loadCharacters();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(true);
        buildInterface();
    }

    /**
     * Carga los personajes del catalogo; si falla lo registra y deja la lista vacia.
     */
    private ArrayList<GameCharacter> loadCharacters() {
        try {
            return CharacterCatalog.createCharacters();
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            return new ArrayList<GameCharacter>();
        }
    }

    /**
     * Arma la pantalla en dos pasos: configuracion del partido y seleccion de equipos.
     */
    private void buildInterface() {
        steps.setOpaque(false);
        JPanel teams = new JPanel();
        teams.setOpaque(false);
        teams.setLayout(new BoxLayout(teams, BoxLayout.Y_AXIS));
        teams.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        teams.add(buildPlayerSection(leftHeader, leftCards, leftDetail, UiTheme.LEFT_COLOR, true));
        teams.add(Box.createVerticalStrut(10));
        teams.add(buildPlayerSection(rightHeader, rightCards, rightDetail, UiTheme.RIGHT_COLOR, false));

        steps.add(buildStep("Paso 1 de 2: Configuracion del partido", buildOptions(),
                SpriteLoader.BUTTON_BACK, "VOLVER", () -> { saveProgress(); onBack.run(); },
                SpriteLoader.BUTTON_FORWARD, "SIGUIENTE", () -> stepLayout.show(steps, STEP_TEAMS)), STEP_CONFIG);
        steps.add(buildStep("Paso 2 de 2: Seleccion de equipos", teams,
                SpriteLoader.BUTTON_BACK, "ATRAS", () -> { saveProgress(); showFirstStep(); },
                SpriteLoader.BUTTON_PLAY, "COMENZAR", () -> startGame()), STEP_TEAMS);
        add(steps, BorderLayout.CENTER);

        selectCard(leftCards, 0);
        selectCard(rightCards, 1);
        refreshProfiles();
        applyTypeChange();
    }

    /**
     * Muestra el primer paso (configuracion del partido).
     */
    public void showFirstStep() {
        stepLayout.show(steps, STEP_CONFIG);
    }

    /**
     * Crea un paso con titulo, contenido central y dos botones inferiores.
     */
    private JPanel buildStep(String titleText, JPanel body, String leftImage, String leftText, Runnable leftAction,
                             String rightImage, String rightText, Runnable rightAction) {
        JPanel step = new JPanel(new BorderLayout());
        step.setOpaque(false);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UiTheme.label(titleText, UiTheme.headingFont(), UiTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0));
        step.add(header, BorderLayout.NORTH);
        step.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        footer.setOpaque(false);
        ImageButton left = new ImageButton(leftImage, leftText, UiTheme.PANEL_LIGHT);
        left.setForeground(UiTheme.TEXT);
        left.addActionListener(event -> leftAction.run());
        ImageButton right = new ImageButton(rightImage, rightText, UiTheme.ACCENT);
        right.addActionListener(event -> rightAction.run());
        footer.add(left);
        footer.add(right);
        step.add(footer, BorderLayout.SOUTH);
        return step;
    }

    /**
     * Crea las filas de opciones: modalidad, tipo, valor y resolucion.
     */
    private JPanel buildOptions() {
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(pvpOption);
        modeGroup.add(pvmOption);
        pvpOption.setSelected(true);
        pvmOption.setEnabled(true);

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(timeOption);
        typeGroup.add(goalsOption);
        timeOption.setSelected(true);
        timeOption.addActionListener(event -> applyTypeChange());
        goalsOption.addActionListener(event -> applyTypeChange());

        ButtonGroup resolutionGroup = new ButtonGroup();
        Resolution[] resolutions = Resolution.values();
        for (int i = 0; i < resolutions.length; i++) {
            resolutionOptions[i] = new ChoiceChip(resolutions[i].getLabel());
            resolutionGroup.add(resolutionOptions[i]);
        }
        resolutionOptions[1].setSelected(true);

        MenuButton minus = new MenuButton("-", UiTheme.PANEL_LIGHT);
        minus.setForeground(UiTheme.TEXT);
        minus.setPreferredSize(new Dimension(48, 36));
        minus.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        minus.addActionListener(event -> changeValue(-1));
        MenuButton plus = new MenuButton("+", UiTheme.PANEL_LIGHT);
        plus.setForeground(UiTheme.TEXT);
        plus.setPreferredSize(new Dimension(48, 36));
        plus.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        plus.addActionListener(event -> changeValue(1));
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setPreferredSize(new Dimension(44, 36));
        ruleInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        JPanel valueRow = row();
        valueRow.add(minus);
        valueRow.add(valueLabel);
        valueRow.add(plus);
        valueRow.add(ruleInfo);

        JPanel modeRow = row();
        modeRow.add(pvpOption);
        modeRow.add(pvmOption);
        modeRow.add(machineProfileBox);
        machineProfileBox.setSelectedItem(MatchConfig.DEFAULT_MACHINE.getDisplayName());
        updateMachineProfileVisibility();
        pvmOption.addActionListener(event -> updateMachineProfileVisibility());
        pvpOption.addActionListener(event -> updateMachineProfileVisibility());
        JPanel typeRow = row();
        typeRow.add(timeOption);
        typeRow.add(goalsOption);
        JPanel resolutionRow = row();
        for (ChoiceChip option : resolutionOptions) {
            resolutionRow.add(option);
        }

        JPanel options = new JPanel(new GridBagLayout());
        options.setOpaque(false);
        addOptionRow(options, 0, UiTheme.label("Modalidad", UiTheme.subtitleFont(), UiTheme.TEXT), modeRow);
        addOptionRow(options, 1, UiTheme.label("Tipo de partido", UiTheme.subtitleFont(), UiTheme.TEXT), typeRow);
        addOptionRow(options, 2, ruleLabel, valueRow);
        addOptionRow(options, 3, UiTheme.label("Resolucion", UiTheme.subtitleFont(), UiTheme.TEXT), resolutionRow);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(options);
        return wrapper;
    }

    /**
     * Muestra el selector de perfil de la maquina solo cuando la modalidad es Jugador vs Maquina.
     */
    private void updateMachineProfileVisibility() {
        machineProfileBox.setVisible(pvmOption.isSelected());
        revalidate();
        repaint();
    }

    /**
     * Devuelve una fila horizontal vacia y transparente.
     */
    private JPanel row() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        return row;
    }

    /**
     * Agrega una fila con su titulo a la derecha y sus opciones a la izquierda.
     */
    private void addOptionRow(JPanel options, int row, JLabel label, JPanel field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(4, 0, 4, 20);
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.anchor = GridBagConstraints.LINE_START;
        fieldConstraints.insets = new Insets(4, 0, 4, 0);
        label.setPreferredSize(new Dimension(180, 38));
        label.setHorizontalAlignment(JLabel.RIGHT);
        field.setPreferredSize(new Dimension(720, 38));
        options.add(label, labelConstraints);
        options.add(field, fieldConstraints);
    }

    /**
     * Crea la seccion de un jugador: cabecera, fila de tarjetas y panel de detalle con mejoras.
     */
    private JPanel buildPlayerSection(SectionHeader header, ArrayList<CharacterCard> cards,
                                      DetailArea detail, Color sideColor, boolean isLeft) {
        JPanel section = new JPanel(new BorderLayout(0, 2));
        section.setOpaque(false);
        header.setDetail(detail);
        section.add(header, BorderLayout.NORTH);

        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cardRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (GameCharacter character : characters) {
            CharacterCard card = new CharacterCard(character, sideColor);
            card.addActionListener(event -> updateDetails());
            group.add(card);
            cards.add(card);
            cardRow.add(card);
        }

        JPanel body = new JPanel(new BorderLayout(4, 0));
        body.setOpaque(false);
        body.add(cardRow, BorderLayout.WEST);
        body.add(buildSidePanel(detail, isLeft), BorderLayout.CENTER);
        section.add(body, BorderLayout.CENTER);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48 + CharacterCard.HEIGHT));
        return section;
    }

    /**
     * Crea el panel compacto con el detalle del personaje elegido y las cuatro filas de mejora.
     */
    private JPanel buildSidePanel(DetailArea detail, boolean isLeft) {
        ArrayList<UpgradeRow> rows = isLeft ? leftUpgrades : rightUpgrades;
        SidePanel side = new SidePanel();
        JPanel upgrades = new JPanel(new GridLayout(StatType.values().length, 1, 0, 2));
        upgrades.setOpaque(false);
        upgrades.setPreferredSize(new Dimension(250, 120));
        for (StatType type : StatType.values()) {
            UpgradeRow row = new UpgradeRow(type, isLeft);
            rows.add(row);
            upgrades.add(row);
        }
        side.add(detail, BorderLayout.CENTER);
        side.add(upgrades, BorderLayout.EAST);
        return side;
    }

    /**
     * Panel a la derecha de la fila de tarjetas, pintado con el estilo azul marino y naranja del tema.
     */
    private static final class SidePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        /**
         * Crea el panel transparente con margenes interiores para el contenido.
         */
        SidePanel() {
            super(new BorderLayout(16, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        }

        /**
         * Pinta el fondo con UiTheme.paintPanel (el 9-slice del tablero se deforma a este tamano).
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            UiTheme.paintPanel(g2, 0, 0, getWidth(), getHeight(), 18, 4);
            g2.dispose();
        }
    }

    /**
     * Cabecera de un jugador: nombre a la izquierda, puntos a la derecha y controles en una linea pequena debajo.
     */
    private static final class SectionHeader extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String title;
        private final Color color;
        private final String controls;
        private String points = "";
        private transient DetailArea detail;

        /**
         * Crea la cabecera con su titulo, el color del lado y el texto de controles.
         */
        SectionHeader(String title, Color color, String controls) {
            this.title = title;
            this.color = color;
            this.controls = controls;
            setPreferredSize(new Dimension(100, 46));
        }

        /**
         * Asocia el area de detalle: si esta queda sin espacio, la cabecera muestra el resumen del personaje.
         */
        void setDetail(DetailArea detail) {
            this.detail = detail;
        }

        /**
         * Cambia el texto de puntos que se muestra a la derecha.
         */
        void setPoints(String points) {
            this.points = points;
            repaint();
        }

        /**
         * Dibuja titulo, puntos y controles dentro del area disponible.
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            UiTheme.enableAntialiasing(g2);
            g2.setFont(UiTheme.font(Font.BOLD, 22));
            UiTheme.drawShadowedText(g2, title, 4, 22, color);
            g2.setFont(UiTheme.font(Font.BOLD, 18));
            FontMetrics metrics = g2.getFontMetrics();
            UiTheme.drawShadowedText(g2, points, getWidth() - metrics.stringWidth(points) - 4, 22,
                    UiTheme.GOLD_LIGHT);
            g2.setFont(UiTheme.smallFont());
            g2.setColor(UiTheme.MUTED);
            g2.drawString(controls, 4, 40);
            paintSummary(g2, g2.getFontMetrics().stringWidth(controls) + 24, metrics.stringWidth(points));
            g2.dispose();
        }

        /**
         * Si el detalle lateral no cabe (pantalla pequena), dibuja nombre, tiro, dano y descripcion en el hueco libre.
         */
        private void paintSummary(Graphics2D g2, int left, int pointsWidth) {
            if (detail == null || detail.getCharacter() == null || detail.getWidth() >= DetailArea.MIN_WIDTH) {
                return;
            }
            int x = left + 4;
            int width = getWidth() - pointsWidth - 24 - x;
            if (width < 120) {
                return;
            }
            PowerShot shot = detail.getCharacter().getPowerShot();
            String first = detail.getCharacter().getName() + "  |  Tiro: " + shot.getName()
                    + "  |  " + DetailArea.stats(shot);
            drawFitted(g2, first, x, 18, width, Font.BOLD, 13, UiTheme.GOLD_LIGHT);
            drawFitted(g2, UiTheme.powerDescription(shot), x, 37, width, Font.PLAIN, 12, UiTheme.MUTED);
        }

        /**
         * Dibuja una linea reduciendo la fuente (hasta 9) hasta que quepa en el ancho dado.
         */
        private static void drawFitted(Graphics2D g2, String text, int x, int y, int width, int style,
                                       int size, Color color) {
            int fontSize = size;
            g2.setFont(UiTheme.font(style, fontSize));
            while (fontSize > 9 && g2.getFontMetrics().stringWidth(text) > width) {
                fontSize--;
                g2.setFont(UiTheme.font(style, fontSize));
            }
            g2.setColor(color);
            g2.drawString(text, x, y);
        }
    }

    /**
     * Texto con el detalle del personaje elegido: nombre, tiro, dano, efecto y descripcion.
     */
    private static final class DetailArea extends JComponent {
        private static final long serialVersionUID = 1L;
        static final int MIN_WIDTH = 180;
        private transient GameCharacter character;

        /**
         * Devuelve el personaje detallado (puede ser null).
         */
        GameCharacter getCharacter() {
            return character;
        }

        /**
         * Resume el dano y la duracion del efecto del poder en una sola linea.
         */
        static String stats(PowerShot shot) {
            return "Dano " + shot.getDamage() + "  |  Efecto " + formatSeconds(shot.getEffectDuration());
        }

        /**
         * Crea el area con un tamano preferido para cuatro lineas de texto.
         */
        DetailArea() {
            setPreferredSize(new Dimension(280, 120));
        }

        /**
         * Cambia el personaje que se detalla y vuelve a pintar.
         */
        void setCharacter(GameCharacter character) {
            this.character = character;
            repaint();
        }

        /**
         * Dibuja nombre, tiro, dano con duracion y la descripcion en lineas cortas.
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            if (character == null || getWidth() < MIN_WIDTH) {
                return;
            }
            Graphics2D g2 = (Graphics2D) graphics.create();
            UiTheme.enableAntialiasing(g2);
            PowerShot shot = character.getPowerShot();
            g2.setFont(UiTheme.font(Font.BOLD, 18));
            UiTheme.drawShadowedText(g2, character.getName(), 0, 18, UiTheme.TEXT_LIGHT);
            g2.setFont(UiTheme.font(Font.BOLD, 14));
            UiTheme.drawShadowedText(g2, "Tiro: " + shot.getName(), 0, 40, UiTheme.GOLD_LIGHT);
            g2.setFont(UiTheme.font(Font.BOLD, 13));
            g2.setColor(UiTheme.TEXT_LIGHT);
            g2.drawString(stats(shot), 0, 60);
            g2.setFont(UiTheme.font(Font.PLAIN, 12));
            g2.setColor(UiTheme.MUTED);
            FontMetrics metrics = g2.getFontMetrics();
            int y = 80;
            for (String line : wrap(UiTheme.powerDescription(shot), metrics, getWidth())) {
                g2.drawString(line, 0, y);
                y += 15;
            }
            g2.dispose();
        }

        /**
         * Escribe los segundos sin decimales si son enteros (3 s) o con uno (1.5 s).
         */
        private static String formatSeconds(double seconds) {
            if (seconds == Math.floor(seconds)) {
                return (int) seconds + " s";
            }
            return seconds + " s";
        }

        /**
         * Parte el texto en lineas que caben en el ancho indicado.
         */
        private static ArrayList<String> wrap(String text, FontMetrics metrics, int width) {
            ArrayList<String> lines = new ArrayList<String>();
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                if (line.length() > 0 && metrics.stringWidth(line + " " + word) > width) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(word);
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
            return lines;
        }
    }

    /**
     * Fila de mejora de una estadistica: nombre, nivel, costo y boton [+] (SPD  nivel 2/10  costo 50).
     */
    private final class UpgradeRow extends JPanel {
        private static final long serialVersionUID = 1L;
        private final StatType type;
        private final JLabel levelLabel = UiTheme.label(" ", UiTheme.font(Font.PLAIN, 14), UiTheme.TEXT_LIGHT);
        private final JLabel costLabel = UiTheme.label(" ", UiTheme.font(Font.BOLD, 14), UiTheme.GOLD_LIGHT);
        private final ImageButton button;

        /**
         * Crea la fila de la estadistica; el boton compra la mejora del perfil del lado indicado.
         */
        UpgradeRow(StatType type, boolean isLeft) {
            this.type = type;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JLabel name = UiTheme.label(abbreviation(type), UiTheme.font(Font.BOLD, 15), UiTheme.TEXT_LIGHT);
            fixWidth(name, 44);
            fixWidth(levelLabel, 92);
            fixWidth(costLabel, 70);
            button = new ImageButton(SpriteLoader.BUTTON_CONFIRM, "+", UiTheme.PANEL_LIGHT, false);
            button.setPreferredSize(new Dimension(30, 30));
            button.setToolTipText("Mejorar " + abbreviation(type));
            button.getAccessibleContext().setAccessibleName("Mejorar " + abbreviation(type));
            button.addActionListener(event -> upgradeStat(isLeft ? player1 : player2, type));
            add(name);
            add(levelLabel);
            add(costLabel);
            add(Box.createHorizontalGlue());
            add(button);
        }

        /**
         * Fija el ancho de una etiqueta para alinear las columnas de todas las filas.
         */
        private void fixWidth(JLabel label, int width) {
            Dimension size = new Dimension(width, 30);
            label.setPreferredSize(size);
            label.setMinimumSize(size);
            label.setMaximumSize(size);
        }

        /**
         * Muestra nivel, costo y estado del boton segun el perfil (MAX, sin puntos o disponible).
         */
        void refresh(PlayerProfile profile) {
            if (profile == null) {
                levelLabel.setText(" ");
                costLabel.setText(" ");
                button.setEnabled(false);
                return;
            }
            int level = profile.getStats().get(type);
            levelLabel.setText("nivel " + level + "/" + Stats.MAX);
            if (level >= Stats.MAX) {
                costLabel.setText("MAX");
                button.setEnabled(false);
            } else {
                int cost = profile.getUpgradeCost(type);
                costLabel.setText("costo " + cost);
                button.setEnabled(profile.getPoints() >= cost);
            }
        }
    }

    /**
     * Compra una mejora para el perfil; si el dominio la rechaza muestra el mensaje.
     */
    private void upgradeStat(PlayerProfile profile, StatType type) {
        if (profile == null) {
            return;
        }
        try {
            profile.upgrade(type);
        } catch (DOPOSoccerException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se puede mejorar", JOptionPane.WARNING_MESSAGE);
            refreshProfiles();
            return;
        }
        refreshProfiles();
        afterUpgrade();
    }

    /**
     * Punto de enganche que se ejecuta tras cada mejora comprada con exito.
     */
    private void afterUpgrade() {
        saveProgress();
    }

    /**
     * Pide guardar el progreso de los perfiles mediante el callback recibido.
     */
    private void saveProgress() {
        onProgressChanged.run();
    }

    /**
     * Actualiza nivel, costo y disponibilidad de las filas de mejora de un perfil.
     */
    private void refreshUpgradeRows(ArrayList<UpgradeRow> rows, PlayerProfile profile) {
        for (UpgradeRow row : rows) {
            row.refresh(profile);
        }
    }

    /**
     * Cambia los perfiles que se muestran (por ejemplo despues de abrir una partida guardada).
     */
    public void setProfiles(PlayerProfile newPlayer1, PlayerProfile newPlayer2) {
        player1 = newPlayer1;
        player2 = newPlayer2;
        refreshProfiles();
    }

    /**
     * Actualiza los puntos de cada jugador y sus filas de mejora.
     */
    public void refreshProfiles() {
        leftHeader.setPoints(player1 == null ? "" : "Puntos: " + player1.getPoints());
        rightHeader.setPoints(player2 == null ? "" : "Puntos: " + player2.getPoints());
        refreshUpgradeRows(leftUpgrades, player1);
        refreshUpgradeRows(rightUpgrades, player2);
    }

    /**
     * Devuelve la abreviatura de tres letras de una estadistica (SPD, KCK, JMP, SRV).
     */
    private String abbreviation(StatType type) {
        switch (type) {
            case SPEED:
                return "SPD";
            case KICK:
                return "KCK";
            case JUMP:
                return "JMP";
            default:
                return "SRV";
        }
    }

    /**
     * Marca la tarjeta de la posicion indicada si existe.
     */
    private void selectCard(ArrayList<CharacterCard> cards, int index) {
        if (cards.isEmpty()) {
            return;
        }
        if (index >= cards.size()) {
            index = cards.size() - 1;
        }
        cards.get(index).setSelected(true);
        updateDetails();
    }

    /**
     * Devuelve el personaje de la tarjeta marcada, o null si no hay ninguna.
     */
    private GameCharacter selectedCharacter(ArrayList<CharacterCard> cards) {
        for (CharacterCard card : cards) {
            if (card.isSelected()) {
                return card.getCharacter();
            }
        }
        return null;
    }

    /**
     * Actualiza el detalle del personaje elegido de cada jugador.
     */
    private void updateDetails() {
        leftDetail.setCharacter(selectedCharacter(leftCards));
        rightDetail.setCharacter(selectedCharacter(rightCards));
    }

    /**
     * Sube o baja el valor de la regla (minutos o goles) sin salir de 1 a 10.
     */
    private void changeValue(int delta) {
        if (timeOption.isSelected()) {
            minutes = Math.max(MatchConfig.MIN_MINUTES, Math.min(MatchConfig.MAX_MINUTES, minutes + delta));
        } else {
            goals = Math.max(MatchConfig.MIN_GOALS, Math.min(MatchConfig.MAX_GOALS, goals + delta));
        }
        applyTypeChange();
    }

    /**
     * Cambia la fila del valor segun el tipo de partido elegido.
     */
    private void applyTypeChange() {
        if (timeOption.isSelected()) {
            ruleLabel.setText("Minutos");
            valueLabel.setText(String.valueOf(minutes));
            ruleInfo.setText("1 a 10 minutos  |  al empatar: muerte subita");
        } else {
            ruleLabel.setText("Goles para ganar");
            valueLabel.setText(String.valueOf(goals));
            ruleInfo.setText("1 a 10 goles");
        }
    }

    /**
     * Devuelve la resolucion elegida.
     */
    private Resolution selectedResolution() {
        Resolution[] resolutions = Resolution.values();
        for (int i = 0; i < resolutionOptions.length; i++) {
            if (resolutionOptions[i].isSelected()) {
                return resolutions[i];
            }
        }
        return Resolution.MEDIUM;
    }

    /**
     * Devuelve el perfil de maquina elegido en el selector (o el default si no se eligió).
     */
    private MachineProfile selectedMachineProfile() {
        Object selected = machineProfileBox.getSelectedItem();
        if (selected instanceof String) {
            String name = (String) selected;
            for (MachineProfile profile : MachineProfile.values()) {
                if (profile.getDisplayName().equals(name)) {
                    return profile;
                }
            }
        }
        return MatchConfig.DEFAULT_MACHINE;
    }

    /**
     * Crea la configuracion con lo elegido y avisa que se puede empezar.
     * Si algo no es valido muestra un mensaje claro y lo registra en el log.
     */
    private void startGame() {
        GameCharacter left = selectedCharacter(leftCards);
        GameCharacter right = selectedCharacter(rightCards);
        if (left == null || right == null) {
            JOptionPane.showMessageDialog(this, "Elige un personaje",
                    "Falta un personaje", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MatchConfig config;
        try {
            GameMode gameMode = pvpOption.isSelected() ? GameMode.PVP : GameMode.PVM;
            MachineProfile machine = selectedMachineProfile();
            if (timeOption.isSelected()) {
                config = MatchConfig.byTime(gameMode, minutes, machine);
            } else {
                config = MatchConfig.byGoals(gameMode, goals, machine);
            }
        } catch (DOPOSoccerException exception) {
            ErrorLog.record(exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Configuracion no valida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        saveProgress();
        onStart.accept(new GameSetup(config, left, right, selectedResolution()));
    }

    /**
     * Pinta el fondo con el degradado y los circulos decorativos del tema.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        UiTheme.enableAntialiasing(g2);
        UiTheme.paintVerticalGradient(g2, getWidth(), getHeight(),
                UiTheme.BG_TOP, UiTheme.BG_BOTTOM);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(getWidth() / 2.0, getHeight() + 120),
                Math.max(getWidth() * 0.6f, 420),
                new float[] {0f, 1f},
                new Color[] {UiTheme.withAlpha(UiTheme.RIGHT_COLOR, 45),
                        UiTheme.withAlpha(UiTheme.RIGHT_COLOR, 0)}));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(UiTheme.withAlpha(UiTheme.ORANGE, 22));
        g2.fillOval(-180, -220, 620, 620);
        g2.setColor(UiTheme.withAlpha(UiTheme.LEFT_COLOR, 16));
        g2.fillOval(getWidth() - 420, 160, 620, 620);
        g2.dispose();
    }
}
