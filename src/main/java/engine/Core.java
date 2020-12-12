package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import screen.*;

/**
 * Implements core game logic.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class Core {

	/** Width of current screen. */
	private static final int WIDTH = 448;
	/** Height of current screen. */
	private static final int HEIGHT = 520;
	/** Max fps of current screen. */
	private static final int FPS = 60;

	/** Max lives. */
	private static final int MAX_LIVES = 3;
	/** Levels between extra life. */
	private static final int EXTRA_LIFE_FRECUENCY = 3;
	/** Total number of levels. */
	private static final int NUM_LEVELS = 7;
	
	/** Difficulty settings for level 1. */
	private static final GameSettings SETTINGS_LEVEL_1 =
			new GameSettings(5, 4, 60, 2000);
	/** Difficulty settings for level 2. */
	private static final GameSettings SETTINGS_LEVEL_2 =
			new GameSettings(5, 5, 50, 2500);
	/** Difficulty settings for level 3. */
	private static final GameSettings SETTINGS_LEVEL_3 =
			new GameSettings(6, 5, 40, 1500);
	/** Difficulty settings for level 4. */
	private static final GameSettings SETTINGS_LEVEL_4 =
			new GameSettings(6, 6, 30, 1500);
	/** Difficulty settings for level 5. */
	private static final GameSettings SETTINGS_LEVEL_5 =
			new GameSettings(7, 6, 20, 1000);
	/** Difficulty settings for level 6. */
	private static final GameSettings SETTINGS_LEVEL_6 =
			new GameSettings(7, 7, 10, 1000);
	/** Difficulty settings for level 7. */
	private static final GameSettings SETTINGS_LEVEL_7 =
			new GameSettings(8, 7, 2, 500);

	private static final GameSettings Normal_SETTINGS_LEVEL_1 =
			new GameSettings(6, 4, 60, 2000);
	private static final GameSettings Normal_SETTINGS_LEVEL_2 =
			new GameSettings(6, 5, 50, 2500);
	private static final GameSettings Normal_SETTINGS_LEVEL_3 =
			new GameSettings(7, 5, 40, 1500);
	private static final GameSettings Normal_SETTINGS_LEVEL_4 =
			new GameSettings(7, 7, 30, 1500);
	private static final GameSettings Normal_SETTINGS_LEVEL_5 =
			new GameSettings(7, 7, 20, 1000);
	private static final GameSettings Normal_SETTINGS_LEVEL_6 =
			new GameSettings(7, 8, 10, 1000);
	private static final GameSettings Normal_SETTINGS_LEVEL_7 =
			new GameSettings(8, 8, 2, 500);

	private static final GameSettings Hard_SETTINGS_LEVEL_1 =
			new GameSettings(6, 5, 60, 1800);
	private static final GameSettings Hard_SETTINGS_LEVEL_2 =
			new GameSettings(6, 5, 50, 1500);
	private static final GameSettings Hard_SETTINGS_LEVEL_3 =
			new GameSettings(7, 6, 40, 1500);
	private static final GameSettings Hard_SETTINGS_LEVEL_4 =
			new GameSettings(7, 7, 30, 1500);
	private static final GameSettings Hard_SETTINGS_LEVEL_5 =
			new GameSettings(7, 8, 20, 1000);
	private static final GameSettings Hard_SETTINGS_LEVEL_6 =
			new GameSettings(8, 8, 10, 500);
	private static final GameSettings Hard_SETTINGS_LEVEL_7 =
			new GameSettings(9, 8, 2, 500);

	
	/** Frame to draw the screen on. */
	private static Frame frame;
	/** Screen currently shown. */
	private static Screen currentScreen;
	/** Difficulty settings list. */
	private static List<GameSettings> gameSettings;
	private static List<GameSettings> NormalgameSettings;
	private static List<GameSettings> HardgameSettings;
	/** Application logger. */
	private static final Logger LOGGER = Logger.getLogger(Core.class
			.getSimpleName());
	/** Logger handler for printing to disk. */
	private static Handler fileHandler;
	/** Logger handler for printing to console. */
	private static ConsoleHandler consoleHandler;
	
	private static int playerNum = 2;


	/**
	 * Test implementation.
	 * 
	 * @param args
	 *            Program args, ignored.
	 */
	public static void main(final String[] args) {
		try {
			LOGGER.setUseParentHandlers(false);

			fileHandler = new FileHandler("log");
			fileHandler.setFormatter(new MinimalFormatter());

			consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new MinimalFormatter());

			LOGGER.addHandler(fileHandler);
			LOGGER.addHandler(consoleHandler);
			LOGGER.setLevel(Level.ALL);

		} catch (Exception e) {
			// TODO handle exception
			e.printStackTrace();
		}

		frame = new Frame(WIDTH, HEIGHT);
		DrawManager.getInstance().setFrame(frame);
		int width = frame.getWidth();
		int height = frame.getHeight();

		gameSettings = new ArrayList<GameSettings>();
		gameSettings.add(SETTINGS_LEVEL_1);
		gameSettings.add(SETTINGS_LEVEL_2);
		gameSettings.add(SETTINGS_LEVEL_3);
		gameSettings.add(SETTINGS_LEVEL_4);
		gameSettings.add(SETTINGS_LEVEL_5);
		gameSettings.add(SETTINGS_LEVEL_6);
		gameSettings.add(SETTINGS_LEVEL_7);

		NormalgameSettings = new ArrayList<GameSettings>();
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_1);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_2);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_3);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_4);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_5);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_6);
		NormalgameSettings.add(Normal_SETTINGS_LEVEL_7);

		HardgameSettings = new ArrayList<GameSettings>();
		HardgameSettings.add(Hard_SETTINGS_LEVEL_1);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_2);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_3);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_4);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_5);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_6);
		HardgameSettings.add(Hard_SETTINGS_LEVEL_7);


		
		GameState[] gameStates = new GameState[playerNum];
		boolean isAllLive = false;
		

		int returnCode = 1;
		do {
			for(int i = 0; i < gameStates.length; i++)
			{
				gameStates[i] = new GameState(1, 0, MAX_LIVES, 0, 0);
			}

			switch (returnCode) {
			case 1:
				// Main menu.
				currentScreen = new TitleScreen(width, height, FPS);
				LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
						+ " title screen at " + FPS + " fps.");
				returnCode = frame.setScreen(currentScreen);
				LOGGER.info("Closing title screen.");
				break;
			case 2:
				// Game & score.
				do {
					// One extra live every few levels.
					boolean bonusLife = gameStates[0].getLevel()
							% EXTRA_LIFE_FRECUENCY == 0

							&& gameStates[0].getLivesRemaining() < MAX_LIVES;

					if(DifficultyLevelScreen.getOption()==3){
						currentScreen = new GameScreen(gameStates,
								NormalgameSettings.get(gameStates[0].getLevel() - 1),
								bonusLife, width, height, FPS);
					}else if(DifficultyLevelScreen.getOption()==0){
						currentScreen = new GameScreen(gameStates,
								HardgameSettings.get(gameStates[0].getLevel() - 1),
								bonusLife, width, height, FPS);
					}else {
						currentScreen = new GameScreen(gameStates,
								gameSettings.get(gameStates[0].getLevel() - 1),
								bonusLife, width, height, FPS);
					}

					LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
							+ " game screen at " + FPS + " fps.");
					frame.setScreen(currentScreen);
					LOGGER.info("Closing game screen.");
					
				gameStates = ((GameScreen) currentScreen).getGameState();
				for(int i = 0; i < gameStates.length; i++) {

					gameStates[i] = new GameState(gameStates[i].getLevel() + 1,
							gameStates[i].getScore(),
							gameStates[i].getLivesRemaining(),
							gameStates[i].getBulletsShot(),
							gameStates[i].getShipsDestroyed());
				}
				
				for(int i = 0; i < gameStates.length; i++) { // check if all ships is dead
					isAllLive = isAllLive || (gameStates[i].getLivesRemaining() > 0);
				}
				} while (isAllLive && gameStates[0].getLevel() <= NUM_LEVELS);

				for(int i = 0; i < gameStates.length; i++) {
					LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
							+ " score screen at " + FPS + " fps, with a score of "
							+ gameStates[i].getScore() + ", "
							+ gameStates[i].getLivesRemaining() + " lives remaining, "
							+ gameStates[i].getBulletsShot() + " bullets shot and "
							+ gameStates[i].getShipsDestroyed() + " ships destroyed.");
				}
					
				currentScreen = new ScoreScreen(width, height, FPS, gameStates[0]);
				returnCode = frame.setScreen(currentScreen);
				LOGGER.info("Closing score screen.");
				break;
			case 3:
				// High scores.
				currentScreen = new HighScoreScreen(width, height, FPS);
				LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
						+ " high score screen at " + FPS + " fps.");
				returnCode = frame.setScreen(currentScreen);
				LOGGER.info("Closing high score screen.");
				break;
				case 4:
					currentScreen = new DifficultyLevelScreen(width, height, FPS);
					LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
							+ " high score screen at " + FPS + " fps.");
					returnCode = frame.setScreen(currentScreen);
					LOGGER.info("Closing high score screen.");
			default:
				break;
			}

		} while (returnCode != 0);

		fileHandler.flush();
		fileHandler.close();
		System.exit(0);
	}

	/**
	 * Constructor, not called.
	 */
	private Core() {
	}

	/**
	 * Controls access to the logger.
	 * 
	 * @return Application logger.
	 */
	public static Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Controls access to the drawing manager.
	 * 
	 * @return Application draw manager.
	 */
	public static DrawManager getDrawManager() {
		return DrawManager.getInstance();
	}

	/**
	 * Controls access to the input manager.
	 * 
	 * @return Application input manager.
	 */
	public static InputManager getInputManager() {
		return InputManager.getInstance();
	}

	/**
	 * Controls access to the file manager.
	 * 
	 * @return Application file manager.
	 */
	public static FileManager getFileManager() {
		return FileManager.getInstance();
	}

	/**
	 * Controls creation of new cooldowns.
	 * 
	 * @param milliseconds
	 *            Duration of the cooldown.
	 * @return A new cooldown.
	 */
	public static Cooldown getCooldown(final int milliseconds) {
		return new Cooldown(milliseconds);
	}

	/**
	 * Controls creation of new cooldowns with variance.
	 * 
	 * @param milliseconds
	 *            Duration of the cooldown.
	 * @param variance
	 *            Variation in the cooldown duration.
	 * @return A new cooldown with variance.
	 */
	public static Cooldown getVariableCooldown(final int milliseconds,
			final int variance) {
		return new Cooldown(milliseconds, variance);
	}
}