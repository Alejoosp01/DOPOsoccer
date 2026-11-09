package domain;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Log de errores para los programadores.
 * Cada error se agrega al final del archivo dopo_errors.log con la fecha.
 */
public final class ErrorLog {

    public static final String FILE_NAME = "dopo_errors.log";

    private ErrorLog() {
    }

    /**
     * Registra una excepcion: fecha, mensaje y traza.
     */
    public static void record(Exception error) {
        if (error == null) {
            return;
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(FILE_NAME, true));
            writer.println("[" + LocalDateTime.now() + "] " + error.getClass().getName()
                    + ": " + error.getMessage());
            error.printStackTrace(writer);
            writer.println();
        } catch (IOException exception) {
            System.err.println("No se pudo escribir el log de errores: " + exception.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Registra un mensaje de error sin excepcion.
     */
    public static void record(String message) {
        record(new Exception(message));
    }
}
