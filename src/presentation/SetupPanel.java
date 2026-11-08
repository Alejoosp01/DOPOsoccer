package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import domain.CharacterCatalog;
import domain.GameCharacter;
import domain.GameMode;
import domain.MatchConfig;
import domain.MatchType;
import domain.POOBSoccerException;

/**
 * Pantalla de configuracion: modalidad, forma de terminar, valor de la regla
 * y personajes de cada lado.
 */
@SuppressWarnings("serial")
public final class SetupPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Consumer<GameSetup> onStart;
    private final Runnable onBack;

    private final JRadioButton pvpOption = new JRadioButton("1 vs 1 (dos jugadores)");
    private final JRadioButton pvmOption = new JRadioButton("Humano vs Maquina");
    private final JRadioButton mvmOption = new JRadioButton("Maquina vs Maquina");

    private final JRadioButton timeOption = new JRadioButton("Por tiempo");
    private final JRadioButton goalsOption = new JRadioButton("Por goles");
    private final JRadioButton survivalOption = new JRadioButton("Supervivencia");

    private final JLabel valueLabel = UiTheme.label("Duracion (segundos)",
            UiTheme.subtitleFont(), UiTheme.TEXT);
    private final JSpinner valueSpinner = new JSpinner(new SpinnerNumberModel(60, 15, 180, 5));
    private final JLabel hintLabel = UiTheme.label(" ",
            UiTheme.bodyFont(), UiTheme.MUTED);

    private final ArrayList<GameCharacter> characters;
    private final JComboBox<GameCharacter> leftBox = new JComboBox<GameCharacter>();
    private final JComboBox<GameCharacter> rightBox = new JComboBox<GameCharacter>();

    private MatchType currentType = MatchType.TIME;
    private int timeValue = 60;
    private int goalsValue = 3;
    private int ballsValue = 5;

    public SetupPanel(Consumer<GameSetup> onStart, Runnable onBack) {
        this.onStart = onStart;
        this.onBack = onBack;
        this.characters = loadCharacters();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(true);
        buildInterface();
    }

    private ArrayList<GameCharacter> loadCharacters() {
        try {
            return CharacterCatalog.createCharacters();
        } catch (POOBSoccerException exception) {
            return new ArrayList<GameCharacter>();
        }
    }

    private void buildInterface() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));

        JLabel title = UiTheme.label("CONFIGURAR PARTIDA", UiTheme.headingFont(), UiTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = UiTheme.label("Elige como se juega y quien entra a la cancha",
                UiTheme.bodyFont(), UiTheme.MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        header.add(title);
        header.add(javax.swing.Box.createVerticalStrut(4));
        header.add(subtitle);
        header.setBorder(BorderFactory.createEmptyBorder(26, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(9, 0, 9, 22);
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.LINE_START;
        fieldConstraints.insets = new Insets(9, 0, 9, 0);

        styleRadio(pvpOption);
        styleRadio(pvmOption);
        styleRadio(mvmOption);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(pvpOption);
        modeGroup.add(pvmOption);
        modeGroup.add(mvmOption);
        pvmOption.setSelected(true);

        styleRadio(timeOption);
        styleRadio(goalsOption);
        styleRadio(survivalOption);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(timeOption);
        typeGroup.add(goalsOption);
        typeGroup.add(survivalOption);
        timeOption.setSelected(true);

        timeOption.addActionListener(event -> applyTypeChange());
        goalsOption.addActionListener(event -> applyTypeChange());
        survivalOption.addActionListener(event -> applyTypeChange());

        UiTheme.styleSpinner(valueSpinner);
        valueSpinner.setPreferredSize(new Dimension(150, 34));

        addRow(form, labelConstraints, fieldConstraints, 0, "Modalidad",
                row(pvpOption, pvmOption, mvmOption));
        addRow(form, labelConstraints, fieldConstraints, 1, "Forma de terminar",
                row(timeOption, goalsOption, survivalOption));
        addRow(form, labelConstraints, fieldConstraints, 2, valueLabel,
                valueSpinner);
        addRow(form, labelConstraints, fieldConstraints, 3, "Jugador 1 (izquierda)",
                createCharacterBox(leftBox));
        addRow(form, labelConstraints, fieldConstraints, 4, "Jugador 2 (derecha)",
                createCharacterBox(rightBox));

        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 0;
        hintConstraints.gridy = 5;
        hintConstraints.gridwidth = 2;
        hintConstraints.insets = new Insets(10, 0, 0, 0);
        hintConstraints.anchor = GridBagConstraints.CENTER;
        form.add(hintLabel, hintConstraints);

        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formWrapper.setOpaque(false);
        formWrapper.add(form);
        add(formWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 22));
        footer.setOpaque(false);
        MenuButton back = new MenuButton("VOLVER", UiTheme.PANEL_LIGHT);
        back.setForeground(UiTheme.TEXT);
        back.addActionListener(event -> onBack.run());
        MenuButton start = new MenuButton("COMENZAR", UiTheme.ACCENT);
        start.addActionListener(event -> startGame());
        footer.add(back);
        footer.add(start);
        add(footer, BorderLayout.SOUTH);

        populateCharacters();
    }

    private void populateCharacters() {
        for (GameCharacter character : characters) {
            leftBox.addItem(character);
            rightBox.addItem(character);
        }
        if (characters.size() > 2) {
            leftBox.setSelectedIndex(2);
        }
        if (characters.size() > 4) {
            rightBox.setSelectedIndex(4);
        } else if (characters.size() > 1) {
            rightBox.setSelectedIndex(1);
        }
    }

    private JComboBox<GameCharacter> createCharacterBox(JComboBox<GameCharacter> box) {
        box.setFont(UiTheme.bodyFont());
        box.setBackground(UiTheme.PANEL_LIGHT);
        box.setForeground(UiTheme.TEXT);
        box.setPreferredSize(new Dimension(660, 34));
        box.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 1));
        box.setRenderer(new CharacterRenderer());
        return box;
    }

    private JPanel row(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        panel.setOpaque(false);
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    private void addRow(JPanel form, GridBagConstraints labelConstraints,
                        GridBagConstraints fieldConstraints, int row, String label,
                        Component component) {
        addRow(form, labelConstraints, fieldConstraints, row,
                UiTheme.label(label, UiTheme.subtitleFont(), UiTheme.TEXT), component);
    }

    private void addRow(JPanel form, GridBagConstraints labelConstraints,
                        GridBagConstraints fieldConstraints, int row, JLabel label,
                        Component component) {
        labelConstraints.gridy = row;
        fieldConstraints.gridy = row;
        form.add(label, labelConstraints);
        form.add(component, fieldConstraints);
    }

    private void styleRadio(JRadioButton button) {
        button.setOpaque(false);
        button.setForeground(UiTheme.TEXT);
        button.setFont(UiTheme.bodyFont());
        button.setFocusPainted(false);
    }

    /**
     * Guarda el valor de la regla anterior y cambia el modelo del spinner.
     */
    private void applyTypeChange() {
        saveCurrentValue();
        MatchType next = selectedType();
        currentType = next;
        SpinnerNumberModel model;
        if (next == MatchType.GOALS) {
            valueLabel.setText("Goles para ganar");
            model = new SpinnerNumberModel(goalsValue, 1, 15, 1);
        } else if (next == MatchType.SURVIVAL) {
            valueLabel.setText("Balones por lado");
            model = new SpinnerNumberModel(ballsValue, 1, 10, 1);
        } else {
            valueLabel.setText("Duracion (segundos)");
            model = new SpinnerNumberModel(timeValue, 15, 180, 5);
        }
        valueSpinner.setModel(model);

        boolean survival = next == MatchType.SURVIVAL;
        pvpOption.setEnabled(!survival);
        mvmOption.setEnabled(!survival);
        if (survival) {
            pvmOption.setSelected(true);
            hintLabel.setText("Supervivencia: humano a la izquierda vs maquina a la derecha.");
        } else {
            hintLabel.setText(" ");
        }
    }

    private void saveCurrentValue() {
        int value = (Integer) valueSpinner.getValue();
        if (currentType == MatchType.TIME) {
            timeValue = value;
        } else if (currentType == MatchType.GOALS) {
            goalsValue = value;
        } else {
            ballsValue = value;
        }
    }

    private MatchType selectedType() {
        if (goalsOption.isSelected()) {
            return MatchType.GOALS;
        }
        if (survivalOption.isSelected()) {
            return MatchType.SURVIVAL;
        }
        return MatchType.TIME;
    }

    private GameMode selectedMode() {
        if (pvpOption.isSelected()) {
            return GameMode.PVP;
        }
        if (mvmOption.isSelected()) {
            return GameMode.MVM;
        }
        return GameMode.PVM;
    }

    private void startGame() {
        GameCharacter left = (GameCharacter) leftBox.getSelectedItem();
        GameCharacter right = (GameCharacter) rightBox.getSelectedItem();
        if (left == null || right == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los personajes.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        saveCurrentValue();
        int value = (Integer) valueSpinner.getValue();
        MatchType type = selectedType();
        MatchConfig config;
        if (type == MatchType.GOALS) {
            config = MatchConfig.goals(selectedMode(), value);
        } else if (type == MatchType.SURVIVAL) {
            config = MatchConfig.survival(value);
        } else {
            config = MatchConfig.time(selectedMode(), value);
        }
        onStart.accept(new GameSetup(config, left, right));
    }

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

    /**
     * Renderer que muestra nombre, estadisticas y poder de cada personaje.
     */
    private static class CharacterRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean selected,
                                                      boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            if (value instanceof GameCharacter) {
                GameCharacter character = (GameCharacter) value;
                setText(character.getName()
                        + "   |   Vel " + character.getSpeed()
                        + "   Salto " + character.getJump()
                        + "   Fuerza " + character.getStrength()
                        + "   |   " + character.getPower().getName());
            }
            setFont(UiTheme.bodyFont());
            if (selected) {
                setBackground(UiTheme.ACCENT_DARK);
                setForeground(Color.WHITE);
            } else {
                setBackground(UiTheme.PANEL_LIGHT);
                setForeground(UiTheme.TEXT);
            }
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }
}
