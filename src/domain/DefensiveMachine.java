package domain;

/**
 * Maquina defensiva: su prioridad es defender el arco.
 * Se queda cerca de su arco y solo sale a despejar cuando el balon entra en
 * su mitad de la cancha.
 */
public class DefensiveMachine extends MachineController {

    private static final long serialVersionUID = 1L;

    public static final double REACTION_TIME = 0.15;

    public DefensiveMachine() {
        super(REACTION_TIME);
    }

    /**
     * Cuida el arco y despeja cuando el balon llega a su mitad.
     */
    @Override
    protected void decide(Match match, Player self, Ball ball) {
        decideDefensive(match, self, ball);
    }

    /**
     * Devuelve el perfil de esta maquina.
     */
    @Override
    public MachineProfile getProfile() {
        return MachineProfile.DEFENSIVE;
    }
}
