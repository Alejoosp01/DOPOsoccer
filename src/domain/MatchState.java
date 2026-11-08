package domain;

/**
 * Estado actual de un partido.
 */
public enum MatchState {

    /** El partido esta listo pero aun no ha empezado. */
    READY,

    /** El partido esta en juego. */
    PLAYING,

    /** Se acaba de anotar un gol y se espera la pausa reglamentaria. */
    GOAL,

    /** El partido esta pausado manualmente. */
    PAUSED,

    /** El partido ya termino. */
    FINISHED
}
