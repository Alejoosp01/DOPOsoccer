package domain;

/**
 * Maquina experta: decide la accion segun el estado del partido.
 * Si va ganando defiende, si va perdiendo (o hay muerte subita) ataca, y si
 * van empatados juega equilibrado: ataca cuando el balon esta delante y vuelve
 * a defender cuando le queda atras. Es la que reacciona mas rapido.
 */
public class ExpertMachine extends MachineController {

    private static final long serialVersionUID = 1L;

    public static final double REACTION_TIME = 0.10;

    public ExpertMachine() {
        super(REACTION_TIME);
    }

    /**
     * Elige ataque, defensa o juego equilibrado mirando el marcador.
     */
    @Override
    protected void decide(Match match, Player self, Ball ball) {
        int own = match.getScore().getGoals(self.getSide());
        int rival = match.getScore().getGoals(self.getSide().opposite());
        if (match.isSuddenDeath() || own < rival) {
            decideOffensive(self, ball);
        } else if (own > rival) {
            decideDefensive(match, self, ball);
        } else {
            decideBalanced(match, self, ball);
        }
    }

    /**
     * Devuelve el perfil de esta maquina.
     */
    @Override
    public MachineProfile getProfile() {
        return MachineProfile.EXPERT;
    }
}
