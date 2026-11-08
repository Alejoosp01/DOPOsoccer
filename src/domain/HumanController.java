package domain;

/**
 * Controlador humano.
 * La direccion se mantiene mientras la tecla esta presionada; salto, patada y
 * poder son ordenes de un solo uso que se consumen en el siguiente update.
 */
public class HumanController implements PlayerController {

    private double direction;
    private boolean jumpRequested;
    private boolean kickRequested;
    private boolean powerRequested;

    public HumanController() {
        direction = 0;
        jumpRequested = false;
        kickRequested = false;
        powerRequested = false;
    }

    /**
     * Fija la direccion de movimiento: negativa a la izquierda, positiva a la derecha.
     */
    public void setDirection(double direction) {
        if (direction > 0) {
            this.direction = 1;
        } else if (direction < 0) {
            this.direction = -1;
        } else {
            this.direction = 0;
        }
    }

    public void requestJump() {
        jumpRequested = true;
    }

    public void requestKick() {
        kickRequested = true;
    }

    public void requestPower() {
        powerRequested = true;
    }

    @Override
    public void update(Match match, Side side, double dt) {
        if (match == null || side == null || dt <= 0) {
            return;
        }
        if (direction != 0) {
            match.move(side, direction, dt);
        }
        if (jumpRequested) {
            match.jump(side);
            jumpRequested = false;
        }
        if (kickRequested) {
            match.kick(side);
            kickRequested = false;
        }
        if (powerRequested) {
            match.armPower(side);
            powerRequested = false;
        }
    }

    public double getDirection() {
        return direction;
    }

    public boolean isJumpRequested() {
        return jumpRequested;
    }

    public boolean isKickRequested() {
        return kickRequested;
    }

    public boolean isPowerRequested() {
        return powerRequested;
    }

    /**
     * Descarta las ordenes pendientes sin ejecutarlas.
     */
    public void clearRequests() {
        jumpRequested = false;
        kickRequested = false;
        powerRequested = false;
    }
}
