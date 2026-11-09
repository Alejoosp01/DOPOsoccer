package domain;

/**
 * Confusion (tiro Ilusion de Estados Unidos): el rival no sabe cual es el
 * balon real. No le quita movimientos; mientras dura, los 8 balones se ven
 * iguales para ambos jugadores.
 */
public class ConfusionEffect extends Effect {

    public ConfusionEffect(double duration) {
        super("Confusion", duration);
    }
}
