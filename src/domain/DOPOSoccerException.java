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
    public static final String NO_MACHINE_PROFILE = "En jugador vs maquina hay que elegir el perfil de la maquina";
    public static final String NO_FILE = "No se indico el archivo";
    public static final String FILE_NOT_FOUND = "El archivo no existe";
    public static final String SAVE_ERROR = "No se pudo guardar el archivo";
    public static final String OPEN_ERROR = "No se pudo abrir el archivo";
    public static final String INVALID_FILE = "El archivo no es una partida o un progreso de DOPOSoccer";
    public static final String MATCH_NOT_STARTED = "El partido aun no ha empezado";

    public DOPOSoccerException(String message) {
        super(message);
    }
}
