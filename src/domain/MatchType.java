package domain;

/**
 * Tipo de partido: forma de decidir el ganador.
 */
public enum MatchType {

    /** Gana quien tenga mas goles al acabarse los minutos; si empatan, muerte subita. */
    TIME,

    /** Gana el primero que llegue al numero de goles definido. */
    GOALS
}
