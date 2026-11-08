package domain;

/**
 * Controlador de la maquina.
 * Persigue el balon, se ubica del lado correcto para empujarlo hacia el arco
 * rival, salta si el balon esta alto, patea al tocarlo y usa el poder cuando
 * la barra esta llena.
 */
public class MachineController implements PlayerController {

    public static final double CONTACT_MARGIN = 4;
    public static final double ARRIVAL_TOLERANCE = 1;

    @Override
    public void update(Match match, Side side, double dt) {
        if (match == null || side == null || dt <= 0) {
            return;
        }
        if (match.getState() != MatchState.PLAYING) {
            return;
        }

        Player self = match.getPlayer(side);
        Ball ball = match.getBall();

        if (self.isPowerReady() && !self.isPowerArmed()) {
            match.armPower(side);
        }

        if (shouldJump(self, ball)) {
            match.jump(side);
        }

        if (self.touchesBall(ball)) {
            if (self.canKick()) {
                match.kick(side);
            }
            return;
        }

        double offset = self.getWidth() / 2 + ball.getRadius() - CONTACT_MARGIN;
        double targetX = ball.getX() - self.getAttackDirection() * offset;
        double difference = targetX - self.getX();
        if (Math.abs(difference) > ARRIVAL_TOLERANCE) {
            match.move(side, difference > 0 ? 1 : -1, dt);
        }
    }

    /**
     * Salta si el balon esta por encima de su cabeza y cerca en horizontal.
     */
    private boolean shouldJump(Player self, Ball ball) {
        double headY = self.getY() - self.getHeight() / 2;
        boolean aboveHead = ball.getY() + ball.getRadius() < headY;
        boolean closeHorizontally = Math.abs(ball.getX() - self.getX()) <= self.getWidth();
        return aboveHead && closeHorizontally;
    }
}
