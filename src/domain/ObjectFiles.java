package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * Lee y escribe objetos en archivos usando la serializacion de Java.
 * Convierte cualquier error de archivo en una DOPOSoccerException con un
 * mensaje claro y deja el detalle tecnico en el log de errores.
 */
final class ObjectFiles {

    private ObjectFiles() {
    }

    /**
     * Escribe el objeto en el archivo, reemplazando lo que hubiera.
     */
    static void write(File file, Object object) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_FILE);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(object);
        } catch (IOException exception) {
            ErrorLog.record(exception);
            throw new DOPOSoccerException(DOPOSoccerException.SAVE_ERROR);
        }
    }

    /**
     * Lee el objeto guardado en el archivo.
     */
    static Object read(File file) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_FILE);
        }
        if (!file.isFile()) {
            throw new DOPOSoccerException(DOPOSoccerException.FILE_NOT_FOUND);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return in.readObject();
        } catch (FileNotFoundException exception) {
            ErrorLog.record(exception);
            throw new DOPOSoccerException(DOPOSoccerException.FILE_NOT_FOUND);
        } catch (StreamCorruptedException | InvalidClassException | ClassNotFoundException
                | ClassCastException exception) {
            ErrorLog.record(exception);
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_FILE);
        } catch (IOException exception) {
            ErrorLog.record(exception);
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_ERROR);
        }
    }
}
