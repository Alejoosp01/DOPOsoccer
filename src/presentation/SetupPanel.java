package presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
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

    private final JLabel leftTitle = UiTheme.label(" ", UiTheme.font(Font.BOLD, 15), UiTheme.LEFT_COLOR);
    private final JLabel rightTitle = UiTheme.label(" ", UiTheme.font(Font.BOLD, 15), UiTheme.RIGHT_COLOR);
    private final JLabel leftDetail = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);
    private final JLabel rightDetail = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);

    private final transient ArrayList<GameCharacter> characters;
    private final ArrayList<CharacterCard> leftCards = new ArrayList<CharacterCard>();
    private final ArrayList<CharacterCard> rightCards = new ArrayList<CharacterCard>();
    private final ArrayList<ImageButton> leftUpgrades = new ArrayList<ImageButton>();
    private final ArrayList<ImageButton> rightUpgrades = new ArrayList<ImageButton>();

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
        teams.add(buildPlayerSection(leftTitle, leftCards, leftDetail, UiTheme.LEFT_COLOR, true));
        teams.add(Box.createVerticalStrut(8));
        teams.add(buildPlayerSection(rightTitle, rightCards, rightDetail, UiTheme.RIGHT_COLOR, false));

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
     * Crea la seccion de un jugador: titulo, fila de tarjetas, detalle del poder y mejoras.
     */
    private JPanel buildPlayerSection(JLabel title, ArrayList<CharacterCard> cards,
                                      JLabel detail, Color sideColor, boolean isLeft) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(1076, 22));
        titleRow.add(title);

        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        cardRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (GameCharacter character : characters) {
            CharacterCard card = new CharacterCard(character, sideColor);
            card.addActionListener(event -> updateDetails());
            group.add(card);
            cards.add(card);
            cardRow.add(card);
        }

        JPanel detailRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detailRow.setOpaque(false);
        detailRow.setMaximumSize(new Dimension(1076, 22));
        detail.setForeground(UiTheme.TEXT);
        detailRow.add(detail);

        StatsBoardPanel statsBoard = new StatsBoardPanel();
        statsBoard.add(detailRow);
        statsBoard.add(buildUpgradeRow(isLeft));

        section.add(titleRow);
        section.add(cardRow);
        section.add(statsBoard);
        return section;
    }

    /**
     * Panel que pinta el tablero de estadisticas como fondo de los detalles y las mejoras.
     */
    private static final class StatsBoardPanel extends JPanel {
        /** Ancho maximo del tablero en pixeles. */
        private static final int BOARD_MAX_WIDTH = 1076;
        /** Alto maximo del tablero en pixeles. */
        private static final int BOARD_MAX_HEIGHT = 150;
        /** Factor uniforme del 9-slice (los bordes de origen estan en SpriteLoader.STATS_BOARD_INSETS). */
        private static final double SLICE_FACTOR = 0.25;

        /**
         * Crea el panel transparente con margenes que evitan las cintas del tablero.
         */
        StatsBoardPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(40, 24, 38, 24));
            setMaximumSize(new Dimension(BOARD_MAX_WIDTH, BOARD_MAX_HEIGHT));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        /**
         * Pinta la imagen del tablero escalada al tamano actual o un respaldo translucido.
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            BufferedImage board =
                    SpriteLoader.nineSlice(SpriteLoader.STATS_BOARD, getWidth(), getHeight(),
                            SpriteLoader.STATS_BOARD_INSETS, SLICE_FACTOR);
            if (board != null) {
                g2.drawImage(board, 0, 0, null);
            } else {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D shape =
                        new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(UiTheme.withAlpha(UiTheme.PANEL, 220));
                g2.fill(shape);
                g2.setColor(UiTheme.BORDER);
                g2.draw(shape);
            }
            g2.dispose();
        }
    }

    /**
     * Crea la fila con un boton de mejora por cada estadistica del jugador indicado.
     */
    private JPanel buildUpgradeRow(boolean isLeft) {
        ArrayList<ImageButton> buttons = isLeft ? leftUpgrades : rightUpgrades;
        JPanel upgradeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        upgradeRow.setOpaque(false);
        upgradeRow.setMaximumSize(new Dimension(1076, 46));
        for (StatType type : StatType.values()) {
            ImageButton button = new ImageButton(SpriteLoader.BUTTON_CONFIRM, abbreviation(type), UiTheme.PANEL_LIGHT, true);
            button.setFont(UiTheme.font(java.awt.Font.BOLD, 14));
            button.setPreferredSize(new Dimension(170, 40));
            button.addActionListener(event -> upgradeStat(isLeft ? player1 : player2, type));
            buttons.add(button);
            upgradeRow.add(button);
        }
        return upgradeRow;
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
     * Actualiza el texto y la disponibilidad de los botones de mejora de un perfil.
     */
    private void refreshUpgradeButtons(ArrayList<ImageButton> buttons, PlayerProfile profile) {
        StatType[] types = StatType.values();
        for (int i = 0; i < buttons.size() && i < types.length; i++) {
            ImageButton button = buttons.get(i);
            String abbreviation = abbreviation(types[i]);
            if (profile == null) {
                button.setText(abbreviation);
                button.setEnabled(false);
                continue;
            }
            int level = profile.getStats().get(types[i]);
            if (level >= Stats.MAX) {
                button.setText(abbreviation + " MAX");
                button.setEnabled(false);
            } else {
                int cost = profile.getUpgradeCost(types[i]);
                button.setText(abbreviation + " " + level + " (+" + cost + ")");
                button.setEnabled(profile.getPoints() >= cost);
            }
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
     * Actualiza los titulos de cada jugador con sus puntos y estadisticas.
     */
    public void refreshProfiles() {
        leftTitle.setText("JUGADOR 1 (IZQUIERDA)   A / D / W / S  +  Q poder      "
                + profileText(player1));
        rightTitle.setText("JUGADOR 2 (DERECHA)   Flechas  +  Ctrl derecho poder      "
                + profileText(player2));
        refreshUpgradeButtons(leftUpgrades, player1);
        refreshUpgradeButtons(rightUpgrades, player2);
    }

    /**
     * Devuelve los puntos y estadisticas de un perfil en una linea.
     */
    private String profileText(PlayerProfile profile) {
        if (profile == null) {
            return "";
        }
        Stats stats = profile.getStats();
        StringBuilder text = new StringBuilder("Puntos " + profile.getPoints() + "   |  ");
        for (StatType type : StatType.values()) {
            text.append(' ').append(abbreviation(type)).append(' ').append(stats.get(type)).append(' ');
        }
        return text.toString().trim();
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
     * Escribe debajo de cada fila de tarjetas que hace el tiro elegido.
     */
    private void updateDetails() {
        leftDetail.setText(detailText(selectedCharacter(leftCards)));
        rightDetail.setText(detailText(selectedCharacter(rightCards)));
    }

    /**
     * Devuelve el texto de detalle de un personaje: su tiro y lo que hace.
     */
    private String detailText(GameCharacter character) {
        if (character == null) {
            return " ";
        }
        return character.getName() + "  -  " + character.getPowerShot().getName() + ": "
                + UiTheme.powerDescription(character.getPowerShot());
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

        g2.setColor(UiTheme.withAlpha(UiTheme.ACCENT, 22));
        g2.fillOval(-180, -220, 620, 620);
        g2.setColor(UiTheme.withAlpha(UiTheme.LEFT_COLOR, 16));
        g2.fillOval(getWidth() - 420, 160, 620, 620);
        g2.dispose();
    }
}
