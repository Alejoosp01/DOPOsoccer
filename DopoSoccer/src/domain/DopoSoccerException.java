package domain;

/**
 * Excepcion de negocio de POOBSoccer.
 * Los mensajes son constantes para poder compararlos en las pruebas.
 */
public class DopoSoccerException extends Exception {

    public static final String INVALID_STAT = "Las estadisticas deben estar entre 1 y 5";
    public static final String EMPTY_NAME = "El nombre no puede estar vacio";
    public static final String NO_COUNTRY = "El personaje debe tener un pais";
    public static final String NO_POWER = "El pais debe tener un poder";

    public DopoSoccerException(String message) {
        super(message);
    }
}
