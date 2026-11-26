package domain;

import java.util.Random;

/**
 * Maquina torpe: escoge al azar sus movimientos.
 * Cada vez que decide elige un punto cualquiera de la cancha, a veces salta y,
 * si toca el balon, patea o no al azar (incluso hacia su propio arco).
 */
public class ClumsyMachine extends MachineController {

    private static final long serialVersionUID = 1L;

    public static final double REACTION_TIME = 0.45;
    public static final double JUMP_CHANCE = 0.25;
    public static final double KICK_CHANCE = 0.5;

    private final Random random;

    public ClumsyMachine() {
        this(new Random());
    }

    public ClumsyMachine(Random random) {
        super(REACTION_TIME);
        if (random == null) {
            throw new IllegalArgumentException("El generador aleatorio no puede ser null");
        }
        this.random = random;
    }

    /**
     * Elige un punto al azar entre las lineas de los arcos y salta a veces.
     */
    @Override
    protected void decide(Match match, Player self, Ball ball) {
        Field field = match.getField();
        double minX = field.getLeftGoalLineX() + self.getWidth() / 2;
        double maxX = field.getRightGoalLineX() - self.getWidth() / 2;
        double x = minX + random.nextDouble() * (maxX - minX);
        setTarget(x, random.nextDouble() < JUMP_CHANCE);
    }

    /**
     * Patea al azar sin mirar hacia donde.
     */
    @Override
    protected boolean shouldKick(Player self, Ball ball) {
        return random.nextDouble() < KICK_CHANCE;
    }

    /**
     * Devuelve el perfil de esta maquina.
     */
    @Override
    public MachineProfile getProfile() {
        return MachineProfile.CLUMSY;
    }
}
