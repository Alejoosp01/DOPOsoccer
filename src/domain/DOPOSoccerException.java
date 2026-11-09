package domain;

/**
 * Excepcion de negocio de DOPOSoccer.
 * Los mensajes son constantes para poder compararlos en las pruebas
 * y para mostrar mensajes claros en la interfaz.
 */
public class DOPOSoccerException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String EMPTY_NAME = "El nombre no puede estar vacio";
    public static final String NO_POWER_SHOT = "El personaje debe tener un tiro de poder";
    public static final String INVALID_MINUTES = "Los minutos del partido deben estar entre 1 y 10";
    public static final String INVALID_GOALS = "Los goles para ganar deben estar entre 1 y 10";
    public static final String STAT_AT_MAX = "La estadistica ya esta en el maximo (10)";
    public static final String NOT_ENOUGH_POINTS = "No hay puntos suficientes para esta mejora";

    public DOPOSoccerException(String message) {
        super(message);
    }
}
