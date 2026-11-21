package domain;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Progreso de los jugadores: guarda los perfiles (nombre, puntos y
 * estadisticas) para que se mantengan entre una ejecucion y otra.
 * Los perfiles se buscan por nombre sin importar mayusculas ni espacios
 * a los lados.
 */
public class ProfileRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FILE = "dopo_progress.dat";

    private final ArrayList<PlayerProfile> profiles;

    public ProfileRegistry() {
        profiles = new ArrayList<PlayerProfile>();
    }

    /**
     * Devuelve el perfil con ese nombre; si no existe lo crea y lo agrega.
     */
    public PlayerProfile getOrCreate(String name) throws DOPOSoccerException {
        PlayerProfile found = find(name);
        if (found != null) {
            return found;
        }
        PlayerProfile created = new PlayerProfile(name);
        profiles.add(created);
        return created;
    }

    /**
     * Devuelve el perfil con ese nombre, o null si no existe.
     */
    public PlayerProfile find(String name) {
        if (name == null) {
            return null;
        }
        String key = name.trim();
        for (PlayerProfile profile : profiles) {
            if (profile.getName().trim().equalsIgnoreCase(key)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Guarda el perfil en el registro; si ya habia uno con ese nombre lo reemplaza.
     */
    public void record(PlayerProfile profile) {
        if (profile == null) {
            return;
        }
        PlayerProfile old = find(profile.getName());
        if (old == profile) {
            return;
        }
        if (old != null) {
            profiles.set(profiles.indexOf(old), profile);
        } else {
            profiles.add(profile);
        }
    }

    /**
     * Guarda en el registro los perfiles de los dos jugadores de una partida.
     */
    public void recordMatch(DOPOSoccer game) {
        if (game == null) {
            return;
        }
        record(game.getLeftProfile());
        record(game.getRightProfile());
    }

    /**
     * Mejora una estadistica del perfil con ese nombre pagando con sus puntos.
     */
    public void upgrade(String name, StatType type) throws DOPOSoccerException {
        getOrCreate(name).upgrade(type);
    }

    /**
     * Devuelve una copia de la lista de perfiles.
     */
    public List<PlayerProfile> getProfiles() {
        return new ArrayList<PlayerProfile>(profiles);
    }

    /**
     * Devuelve cuantos perfiles hay.
     */
    public int size() {
        return profiles.size();
    }

    /**
     * Guarda el progreso en el archivo.
     */
    public void save(File file) throws DOPOSoccerException {
        ObjectFiles.write(file, this);
    }

    /**
     * Carga el progreso del archivo; si el archivo aun no existe devuelve un registro vacio.
     */
    public static ProfileRegistry load(File file) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_FILE);
        }
        if (!file.exists()) {
            return new ProfileRegistry();
        }
        Object object = ObjectFiles.read(file);
        if (!(object instanceof ProfileRegistry)) {
            ErrorLog.record("El archivo " + file.getName() + " no contiene un ProfileRegistry");
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_FILE);
        }
        return (ProfileRegistry) object;
    }
}
