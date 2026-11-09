package domain;

/**
 * Excepcion de negocio de DOPOSoccer.
 * Los mensajes son constantes para poder compararlos en las pruebas.
 */
public class DOPOSoccerException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String INVALID_STAT = "Las estadisticas deben estar entre 1 y 5";
    public static final String EMPTY_NAME = "El nombre no puede estar vacio";
    public static final String NO_COUNTRY = "El personaje debe tener un pais";
    public static final String NO_POWER = "El pais debe tener un poder";
    public static final String INVALID_DURATION = "La duracion del partido debe ser positiva";
    public static final String INVALID_GOAL_TARGET = "La meta de goles debe ser 3, 5 o 7";
    public static final String INVALID_BALLS = "La cantidad de balones debe ser positiva";

    public DOPOSoccerException(String message) {
        super(message);
    }
}
