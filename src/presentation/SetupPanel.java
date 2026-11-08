package presentation;

import java.awt.BorderLayout;
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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import domain.CharacterCatalog;
import domain.GameCharacter;
import domain.GameMode;
import domain.MatchConfig;
import domain.MatchType;
import domain.POOBSoccerException;

/**
 * Pantalla de configuracion: modalidad, forma de terminar, regla del partido
 * y el personaje de cada lado, elegido con tarjetas.
 */
@SuppressWarnings("serial")
public final class SetupPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final int SURVIVAL_BALLS = 3;
    private static final int[] GOAL_TARGETS = {3, 5, 7};

    private final transient Consumer<GameSetup> onStart;
    private final transient Runnable onBack;

    private final ChoiceChip pvpOption = new ChoiceChip("Jugador vs Jugador");
    private final ChoiceChip pvmOption = new ChoiceChip("Jugador vs Maquina");
    private final ChoiceChip mvmOption = new ChoiceChip("Maquina vs Maquina");

    private final ChoiceChip timeOption = new ChoiceChip("Por tiempo");
    private final ChoiceChip goalsOption = new ChoiceChip("Por goles");
    private final ChoiceChip survivalOption = new ChoiceChip("Supervivencia");

    private final ChoiceChip[] goalOptions = new ChoiceChip[GOAL_TARGETS.length];

    private final JLabel ruleLabel = UiTheme.label("Duracion", UiTheme.subtitleFont(), UiTheme.TEXT);
    private final JPanel rulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JLabel ruleInfo = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);

    private final JLabel leftTitle = UiTheme.label(" ", UiTheme.font(Font.BOLD, 16), UiTheme.LEFT_COLOR);
    private final JLabel rightTitle = UiTheme.label(" ", UiTheme.font(Font.BOLD, 16), UiTheme.RIGHT_COLOR);
    private final JLabel leftDetail = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);
    private final JLabel rightDetail = UiTheme.label(" ", UiTheme.bodyFont(), UiTheme.MUTED);

    private final transient ArrayList<GameCharacter> characters;
    private final ArrayList<CharacterCard> leftCards = new ArrayList<CharacterCard>();
    private final ArrayList<CharacterCard> rightCards = new ArrayList<CharacterCard>();

    public SetupPanel(Consumer<GameSetup> onStart, Runnable onBack) {
        this.onStart = onStart;
        this.onBack = onBack;
        this.characters = loadCharacters();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(true);
        buildInterface();
    }

    /**
     * Carga los personajes del catalogo; si falla devuelve una lista vacia.
     */
    private ArrayList<GameCharacter> loadCharacters() {
        try {
            return CharacterCatalog.createCharacters();
        } catch (POOBSoccerException exception) {
            return new ArrayList<GameCharacter>();
        }
    }

    /**
     * Arma la pantalla: encabezado, opciones del partido, tarjetas y botones.
     */
    private void buildInterface() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UiTheme.label("CONFIGURAR PARTIDA", UiTheme.headingFont(), UiTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = UiTheme.label("Elige como se juega y quien entra a la cancha",
                UiTheme.bodyFont(), UiTheme.MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        header.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildOptions());
        content.add(Box.createVerticalStrut(2));
        content.add(buildPlayerSection(leftTitle, leftCards, leftDetail, UiTheme.LEFT_COLOR));
        content.add(Box.createVerticalStrut(12));
        content.add(buildPlayerSection(rightTitle, rightCards, rightDetail, UiTheme.RIGHT_COLOR));
        add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        footer.setOpaque(false);
        MenuButton back = new MenuButton("VOLVER", UiTheme.PANEL_LIGHT);
        back.setForeground(UiTheme.TEXT);
        back.addActionListener(event -> onBack.run());
        MenuButton start = new MenuButton("COMENZAR", UiTheme.ACCENT);
        start.addActionListener(event -> startGame());
        footer.add(back);
        footer.add(start);
        add(footer, BorderLayout.SOUTH);

        selectCard(leftCards, 2);
        selectCard(rightCards, 4);
        applyModeChange();
        applyTypeChange();
    }

    /**
     * Crea las tres filas de opciones: modalidad, forma de terminar y regla.
     */
    private JPanel buildOptions() {
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(pvpOption);
        modeGroup.add(pvmOption);
        modeGroup.add(mvmOption);
        pvmOption.setSelected(true);
        pvpOption.addActionListener(event -> applyModeChange());
        pvmOption.addActionListener(event -> applyModeChange());
        mvmOption.addActionListener(event -> applyModeChange());

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(timeOption);
        typeGroup.add(goalsOption);
        typeGroup.add(survivalOption);
        timeOption.setSelected(true);
        timeOption.addActionListener(event -> applyTypeChange());
        goalsOption.addActionListener(event -> applyTypeChange());
        survivalOption.addActionListener(event -> applyTypeChange());

        ButtonGroup goalGroup = new ButtonGroup();
        for (int i = 0; i < GOAL_TARGETS.length; i++) {
            goalOptions[i] = new ChoiceChip(String.valueOf(GOAL_TARGETS[i]));
            goalGroup.add(goalOptions[i]);
        }
        goalOptions[0].setSelected(true);
        rulePanel.setOpaque(false);
        ruleInfo.setBorder(BorderFactory.createEmptyBorder(9, 4, 0, 0));

        JPanel options = new JPanel(new GridBagLayout());
        options.setOpaque(false);
        addOptionRow(options, 0, UiTheme.label("Modalidad", UiTheme.subtitleFont(), UiTheme.TEXT),
                chipRow(pvpOption, pvmOption, mvmOption));
        addOptionRow(options, 1, UiTheme.label("Forma de terminar", UiTheme.subtitleFont(), UiTheme.TEXT),
                chipRow(timeOption, goalsOption, survivalOption));
        addOptionRow(options, 2, ruleLabel, rulePanel);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(options);
        return wrapper;
    }

    /**
     * Agrega una fila con su titulo a la derecha y sus opciones a la izquierda.
     */
    private void addOptionRow(JPanel options, int row, JLabel label, JPanel field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(5, 0, 5, 20);
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.anchor = GridBagConstraints.LINE_START;
        fieldConstraints.insets = new Insets(5, 0, 5, 0);
        label.setPreferredSize(new Dimension(190, 38));
        label.setHorizontalAlignment(JLabel.RIGHT);
        field.setPreferredSize(new Dimension(620, 38));
        options.add(label, labelConstraints);
        options.add(field, fieldConstraints);
    }

    /**
     * Devuelve una fila horizontal con las pastillas indicadas.
     */
    private JPanel chipRow(ChoiceChip first, ChoiceChip second, ChoiceChip third) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.add(first);
        row.add(second);
        row.add(third);
        return row;
    }

    /**
     * Crea la seccion de un jugador: titulo, fila de tarjetas y detalle del poder.
     */
    private JPanel buildPlayerSection(JLabel title, ArrayList<CharacterCard> cards,
                                      JLabel detail, Color sideColor) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(1108, 24));
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
        detailRow.setMaximumSize(new Dimension(1108, 22));
        detailRow.add(detail);

        section.add(titleRow);
        section.add(cardRow);
        section.add(detailRow);
        return section;
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
     * Escribe debajo de cada fila de tarjetas que hace el poder elegido.
     */
    private void updateDetails() {
        leftDetail.setText(detailText(selectedCharacter(leftCards)));
        rightDetail.setText(detailText(selectedCharacter(rightCards)));
    }

    /**
     * Devuelve el texto de detalle de un personaje: su poder y lo que hace.
     */
    private String detailText(GameCharacter character) {
        if (character == null) {
            return " ";
        }
        return character.getName() + "  -  " + character.getPower().getName() + ": "
                + UiTheme.powerDescription(character.getPower());
    }

    /**
     * Cambia los titulos de cada lado segun quien juega: humano o maquina.
     */
    private void applyModeChange() {
        GameMode mode = selectedMode();
        if (mode == GameMode.MVM) {
            leftTitle.setText("MAQUINA  (IZQUIERDA)");
        } else {
            leftTitle.setText("JUGADOR 1  (IZQUIERDA)   A / D / W / S  +  Q poder");
        }
        if (mode == GameMode.PVP) {
            rightTitle.setText("JUGADOR 2  (DERECHA)   Flechas  +  Ctrl derecho poder");
        } else {
            rightTitle.setText("MAQUINA  (DERECHA)");
        }
    }

    /**
     * Cambia la fila de la regla segun la forma de terminar elegida.
     * La supervivencia solo se permite en Jugador vs Maquina.
     */
    private void applyTypeChange() {
        MatchType type = selectedType();
        rulePanel.removeAll();
        if (type == MatchType.GOALS) {
            ruleLabel.setText("Goles para ganar");
            for (ChoiceChip option : goalOptions) {
                rulePanel.add(option);
            }
        } else if (type == MatchType.SURVIVAL) {
            ruleLabel.setText("Balones");
            ruleInfo.setText(SURVIVAL_BALLS + " balones por lado  |  serie de partidos contra la maquina");
            rulePanel.add(ruleInfo);
        } else {
            ruleLabel.setText("Duracion");
            ruleInfo.setText("60 segundos  |  si terminan empatados se juega gol de oro");
            rulePanel.add(ruleInfo);
        }

        boolean survival = type == MatchType.SURVIVAL;
        pvpOption.setEnabled(!survival);
        mvmOption.setEnabled(!survival);
        if (survival) {
            pvmOption.setSelected(true);
            applyModeChange();
        }
        rulePanel.revalidate();
        rulePanel.repaint();
    }

    /**
     * Devuelve la forma de terminar elegida.
     */
    private MatchType selectedType() {
        if (goalsOption.isSelected()) {
            return MatchType.GOALS;
        }
        if (survivalOption.isSelected()) {
            return MatchType.SURVIVAL;
        }
        return MatchType.TIME;
    }

    /**
     * Devuelve la modalidad elegida.
     */
    private GameMode selectedMode() {
        if (pvpOption.isSelected()) {
            return GameMode.PVP;
        }
        if (mvmOption.isSelected()) {
            return GameMode.MVM;
        }
        return GameMode.PVM;
    }

    /**
     * Devuelve la meta de goles elegida (3, 5 o 7).
     */
    private int selectedGoalTarget() {
        for (int i = 0; i < goalOptions.length; i++) {
            if (goalOptions[i].isSelected()) {
                return GOAL_TARGETS[i];
            }
        }
        return GOAL_TARGETS[0];
    }

    /**
     * Crea la configuracion con lo elegido y avisa que se puede empezar.
     */
    private void startGame() {
        GameCharacter left = selectedCharacter(leftCards);
        GameCharacter right = selectedCharacter(rightCards);
        if (left == null || right == null) {
            JOptionPane.showMessageDialog(this,
                    "Elige un personaje para cada lado.",
                    "Falta un personaje", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MatchType type = selectedType();
        MatchConfig config;
        if (type == MatchType.GOALS) {
            config = MatchConfig.goals(selectedMode(), selectedGoalTarget());
        } else if (type == MatchType.SURVIVAL) {
            config = MatchConfig.survival(SURVIVAL_BALLS);
        } else {
            config = MatchConfig.time(selectedMode());
        }
        onStart.accept(new GameSetup(config, left, right));
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
