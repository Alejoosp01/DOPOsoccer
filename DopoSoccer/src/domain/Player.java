package domain;

/**
 * Personaje dentro de la cancha.
 * Por ahora solo maneja posicion, lado y los efectos que reciben los poderes.
 * Movimiento, salto, patada y barra de poder se agregan en la siguiente iteracion.
 */
public class Player {

    public static final double STUN_SECONDS = 3;

    private GameCharacter character;
    private boolean leftSide;
    private double x;
    private double y;

    private double stunTime;
    private double frozenTime;
    private double shrinkTime;
    private double confusedTime;

    public Player(GameCharacter character, boolean leftSide, double x, double y) {
        this.character = character;
        this.leftSide = leftSide;
        this.x = x;
        this.y = y;
        this.stunTime = 0;
        this.frozenTime = 0;
        this.shrinkTime = 0;
        this.confusedTime = 0;
    }

    /**
     * Direccion hacia el arco rival: 1 si ataca a la derecha, -1 si ataca a la izquierda.
     */
    public int getAttackDirection() {
        if (leftSide) {
            return 1;
        }
        return -1;
    }

    /**
     * Aturde al jugador durante 3 segundos.
     */
    public void stun() {
        stunTime = STUN_SECONDS;
    }

    /**
     * Congela al jugador durante los segundos indicados.
     */
    public void freeze(double seconds) {
        frozenTime = seconds;
    }

    /**
     * Encoge al jugador durante los segundos indicados.
     */
    public void shrink(double seconds) {
        shrinkTime = seconds;
    }

    /**
     * Invierte los controles del jugador durante los segundos indicados.
     */
    public void confuse(double seconds) {
        confusedTime = seconds;
    }

    /**
     * Empuja al jugador hacia su propio arco.
     */
    public void pushBack(double distance) {
        x = x - getAttackDirection() * distance;
    }

    /**
     * Descuenta el tiempo de todos los efectos. dt esta en segundos.
     */
    public void updateEffects(double dt) {
        stunTime = reduce(stunTime, dt);
        frozenTime = reduce(frozenTime, dt);
        shrinkTime = reduce(shrinkTime, dt);
        confusedTime = reduce(confusedTime, dt);
    }

    /**
     * Resta dt al tiempo de un efecto sin bajar de 0.
     */
    private double reduce(double time, double dt) {
        if (time - dt < 0) {
            return 0;
        }
        return time - dt;
    }

    /**
     * Un jugador aturdido o congelado no puede moverse, saltar ni patear.
     */
    public boolean canAct() {
        return !isStunned() && !isFrozen();
    }

    /**
     * Devuelve si el jugador esta aturdido.
     */
    public boolean isStunned() {
        return stunTime > 0;
    }

    /**
     * Devuelve si el jugador esta congelado.
     */
    public boolean isFrozen() {
        return frozenTime > 0;
    }

    /**
     * Devuelve si el jugador esta encogido.
     */
    public boolean isShrunk() {
        return shrinkTime > 0;
    }

    /**
     * Devuelve si el jugador tiene los controles invertidos.
     */
    public boolean isConfused() {
        return confusedTime > 0;
    }

    /**
     * Devuelve el personaje que controla este jugador.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * Devuelve si el jugador defiende el arco izquierdo.
     */
    public boolean isLeftSide() {
        return leftSide;
    }

    /**
     * Devuelve la posicion en el eje x.
     */
    public double getX() {
        return x;
    }

    /**
     * Devuelve la posicion en el eje y.
     */
    public double getY() {
        return y;
    }
}
