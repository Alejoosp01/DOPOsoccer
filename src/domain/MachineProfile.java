package domain;

/**
 * Perfiles de maquina que se pueden elegir en jugador vs maquina.
 * Cada perfil sabe crear su controlador; para agregar un perfil nuevo se crea
 * su subclase de MachineController y se agrega aqui.
 */
public enum MachineProfile {

    /** Escoge al azar sus movimientos. */
    CLUMSY("Torpe"),

    /** Su prioridad es anotar. */
    OFFENSIVE("Ofensiva"),

    /** Su prioridad es defender el arco. */
    DEFENSIVE("Defensiva"),

    /** Decide la accion segun el estado del partido. */
    EXPERT("Experta");

    private final String displayName;

    MachineProfile(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Crea el controlador de maquina de este perfil.
     */
    public MachineController createController() {
        switch (this) {
            case CLUMSY:
                return new ClumsyMachine();
            case OFFENSIVE:
                return new OffensiveMachine();
            case DEFENSIVE:
                return new DefensiveMachine();
            default:
                return new ExpertMachine();
        }
    }

    /**
     * Devuelve el nombre del perfil en español para mostrarlo.
     */
    public String getDisplayName() {
        return displayName;
    }
}
