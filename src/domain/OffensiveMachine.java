package domain;

/**
 * Maquina ofensiva: su prioridad es anotar.
 * Siempre se pone detras del balon para empujarlo al arco rival y nunca vuelve
 * a cuidar su arco.
 */
public class OffensiveMachine extends MachineController {

    private static final long serialVersionUID = 1L;

    public static final double REACTION_TIME = 0.15;

    public OffensiveMachine() {
        super(REACTION_TIME);
    }

    /**
     * Va siempre a atacar el balon.
     */
    @Override
    protected void decide(Match match, Player self, Ball ball) {
        decideOffensive(self, ball);
    }

    /**
     * Devuelve el perfil de esta maquina.
     */
    @Override
    public MachineProfile getProfile() {
        return MachineProfile.OFFENSIVE;
    }
}
