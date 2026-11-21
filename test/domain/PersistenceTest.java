package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pruebas de salvar y abrir partidas y de mantener el progreso de los jugadores.
 */
public class PersistenceTest {

    @TempDir
    File folder;

    private PlayerProfile player1;
    private PlayerProfile player2;

    @BeforeEach
    public void setUp() {
        player1 = TestData.profile("Jugador 1");
        player2 = TestData.profile("Jugador 2");
    }

    private DOPOSoccer newGame(MatchConfig config) {
        return new DOPOSoccer(config,
                TestData.character("Corea del Sur", new BlueAuraShot()),
                TestData.character("Camerun", new LightningShot()),
                player1, player2);
    }

    private File file(String name) {
        return new File(folder, name);
    }

    @Test
    public void shouldRestoreScoreTimeAndPositions() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 2));
        game.start();
        game.update(1.5);
        game.getMatch().getScore().score(Side.LEFT);
        game.getLeftPlayer().setPosition(333, game.getLeftPlayer().getY());
        game.getMatch().getBall().setPosition(777, 300);
        double timeLeft = game.getMatch().getTimeLeft();

        File saved = file("partida" + DOPOSoccer.FILE_EXTENSION);
        game.save(saved);
        DOPOSoccer opened = DOPOSoccer.open(saved);

        assertEquals(1, opened.getMatch().getScore().getLeftGoals());
        assertEquals(timeLeft, opened.getMatch().getTimeLeft(), 0.0001);
        assertEquals(333, opened.getLeftPlayer().getX(), 0.0001);
        assertEquals(777, opened.getMatch().getBall().getX(), 0.0001);
        assertEquals("Corea del Sur", opened.getLeftCharacter().getName());
        assertTrue(opened.getRightCharacter().getPowerShot() instanceof LightningShot);
    }

    @Test
    public void shouldRestoreLifeBarsAurasAndEffects() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byGoals(GameMode.PVP, 3));
        game.start();
        TestData.fillPowerBar(game.getLeftPlayer());
        game.getLeftPlayer().activateAura();
        game.getRightPlayer().receiveDamage(30);
        game.getRightPlayer().applyEffect(new ElectrocutedEffect(3));

        File saved = file("efectos.dopo");
        game.save(saved);
        DOPOSoccer opened = DOPOSoccer.open(saved);

        assertTrue(opened.getLeftPlayer().hasAura());
        assertEquals(game.getRightPlayer().getLife(), opened.getRightPlayer().getLife(), 0.0001);
        assertTrue(opened.getRightPlayer().getEffect() instanceof ElectrocutedEffect);
        assertFalse(opened.getRightPlayer().canJump());
    }

    @Test
    public void openedGameShouldBePausedAndContinueAfterResume() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        game.update(1);

        File saved = file("pausa.dopo");
        game.save(saved);
        DOPOSoccer opened = DOPOSoccer.open(saved);
        double before = opened.getMatch().getTimeLeft();

        assertTrue(opened.isPaused());
        opened.update(1);
        assertEquals(before, opened.getMatch().getTimeLeft(), 0.0001);

        assertTrue(opened.resume());
        opened.update(1);
        assertTrue(opened.getMatch().getTimeLeft() < before);
    }

    @Test
    public void openedGameShouldForgetPendingKeys() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        HumanController left = (HumanController) game.getLeftController();
        left.setDirection(1);
        left.requestJump();

        File saved = file("teclas.dopo");
        game.save(saved);
        HumanController opened = (HumanController) DOPOSoccer.open(saved).getLeftController();

        assertEquals(0, opened.getDirection());
        assertFalse(opened.isJumpRequested());
    }

    @Test
    public void shouldKeepMachineProfileWhenOpening() throws DOPOSoccerException {
        DOPOSoccer game = newGame(MatchConfig.byGoals(GameMode.PVM, 3, MachineProfile.DEFENSIVE));
        game.start();

        File saved = file("maquina.dopo");
        game.save(saved);
        DOPOSoccer opened = DOPOSoccer.open(saved);

        assertEquals(MachineProfile.DEFENSIVE, opened.getMachineProfile());
        assertTrue(opened.getRightController() instanceof DefensiveMachine);
    }

    @Test
    public void shouldAwardPointsWhenOpenedGameEnds() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        game.getMatch().getScore().score(Side.RIGHT);

        File saved = file("final.dopo");
        game.save(saved);
        DOPOSoccer opened = DOPOSoccer.open(saved);
        opened.endMatch();

        assertEquals(Winner.RIGHT, opened.getWinner());
        assertEquals(DOPOSoccer.WINNER_POINTS, opened.getRightProfile().getPoints());
        assertEquals(DOPOSoccer.LOSER_POINTS, opened.getLeftProfile().getPoints());
    }

    @Test
    public void shouldNotSaveAMatchThatHasNotStarted() {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> game.save(file("nada.dopo")));
        assertEquals(DOPOSoccerException.MATCH_NOT_STARTED, error.getMessage());
    }

    @Test
    public void shouldRejectNullFiles() {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        assertEquals(DOPOSoccerException.NO_FILE,
                assertThrows(DOPOSoccerException.class, () -> game.save(null)).getMessage());
        assertEquals(DOPOSoccerException.NO_FILE,
                assertThrows(DOPOSoccerException.class, () -> DOPOSoccer.open(null)).getMessage());
    }

    @Test
    public void shouldFailClearlyWhenFileDoesNotExist() {
        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> DOPOSoccer.open(file("no-existe.dopo")));
        assertEquals(DOPOSoccerException.FILE_NOT_FOUND, error.getMessage());
    }

    @Test
    public void shouldFailClearlyWhenFileIsNotAGame() throws IOException {
        File text = file("texto.dopo");
        FileWriter writer = new FileWriter(text);
        writer.write("esto no es una partida");
        writer.close();

        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> DOPOSoccer.open(text));
        assertEquals(DOPOSoccerException.INVALID_FILE, error.getMessage());
    }

    @Test
    public void shouldNotOpenAProgressFileAsAGame() throws DOPOSoccerException {
        File progress = file("progreso.dat");
        new ProfileRegistry().save(progress);

        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> DOPOSoccer.open(progress));
        assertEquals(DOPOSoccerException.INVALID_FILE, error.getMessage());
    }

    @Test
    public void shouldFailClearlyWhenFolderCannotBeWritten() {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        File impossible = new File(file("carpeta-que-no-existe"), "partida.dopo");

        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> game.save(impossible));
        assertEquals(DOPOSoccerException.SAVE_ERROR, error.getMessage());
    }

    @Test
    public void registryShouldCreateEachProfileOnce() throws DOPOSoccerException {
        ProfileRegistry registry = new ProfileRegistry();
        PlayerProfile first = registry.getOrCreate("Alejo");
        PlayerProfile again = registry.getOrCreate("  alejo ");

        assertSame(first, again);
        assertEquals(1, registry.size());
        assertNull(registry.find("Carlos"));
    }

    @Test
    public void registryShouldRejectEmptyNames() {
        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> new ProfileRegistry().getOrCreate("   "));
        assertEquals(DOPOSoccerException.EMPTY_NAME, error.getMessage());
    }

    @Test
    public void registryShouldKeepPointsAndStatsBetweenRuns() throws DOPOSoccerException {
        ProfileRegistry registry = new ProfileRegistry();
        PlayerProfile alejo = registry.getOrCreate("Alejo");
        alejo.addPoints(600);
        registry.upgrade("Alejo", StatType.SPEED);

        File progress = file(ProfileRegistry.DEFAULT_FILE);
        registry.save(progress);
        ProfileRegistry loaded = ProfileRegistry.load(progress);
        PlayerProfile restored = loaded.find("Alejo");

        assertNotNull(restored);
        assertEquals(100, restored.getPoints());
        assertEquals(2, restored.getStats().get(StatType.SPEED));
        assertEquals(1000, restored.getUpgradeCost(StatType.SPEED));
    }

    @Test
    public void registryShouldRecordTheProfilesOfAFinishedMatch() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        game.getMatch().getScore().score(Side.LEFT);
        game.endMatch();
        ProfileRegistry registry = new ProfileRegistry();

        registry.recordMatch(game);

        assertEquals(DOPOSoccer.WINNER_POINTS, registry.find("Jugador 1").getPoints());
        assertEquals(DOPOSoccer.LOSER_POINTS, registry.find("Jugador 2").getPoints());
    }

    @Test
    public void registryRecordShouldReplaceProfileWithSameName() throws DOPOSoccerException {
        ProfileRegistry registry = new ProfileRegistry();
        registry.getOrCreate("Jugador 1");
        player1.addPoints(25);

        registry.record(player1);

        assertSame(player1, registry.find("Jugador 1"));
        assertEquals(1, registry.size());
    }

    @Test
    public void loadingAMissingProgressFileShouldStartEmpty() throws DOPOSoccerException {
        ProfileRegistry registry = ProfileRegistry.load(file("primera-vez.dat"));
        assertEquals(0, registry.size());
    }

    @Test
    public void shouldNotLoadAGameAsProgress() throws DOPOSoccerException {
        DOPOSoccer game = newGame(TestData.byTime(GameMode.PVP, 1));
        game.start();
        File saved = file("partida.dopo");
        game.save(saved);

        DOPOSoccerException error = assertThrows(DOPOSoccerException.class,
                () -> ProfileRegistry.load(saved));
        assertEquals(DOPOSoccerException.INVALID_FILE, error.getMessage());
    }
}
