package domain;

/**
 * Forma de decidir el final de un partido.
 */
public enum MatchType {

    /** Gana quien tenga mas goles cuando se acaba el tiempo. */
    TIME,

    /** Gana quien llegue primero a la cantidad de goles objetivo. */
    GOALS,

    /** Cada gol le quita un balon al rival; pierde quien se quede sin balones. */
    SURVIVAL
}
