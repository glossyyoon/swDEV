package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	/** Current game difficulty settings. */
	private GameSettings gameSettings;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship[] ship;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Current score. */
	private int[] score;
	/** Player lives left. */
	private int[] lives;
	/** Total bullets shot by the player. */
	private int[] bulletsShot;
	/** Total ships destroyed by the player. */
	private int[] shipsDestroyed;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. */
	private boolean bonusLife;
	
	private final int KEYCODE_1P_MOVE_RIGHT = KeyEvent.VK_D;
	private final int KEYCODE_1P_MOVE_LEFT = KeyEvent.VK_A;
	private final int KEYCODE_1P_MOVE_SHOOT = KeyEvent.VK_SPACE;
	
	private final int KEYCODE_2P_MOVE_RIGHT = KeyEvent.VK_RIGHT;
	private final int KEYCODE_2P_MOVE_LEFT = KeyEvent.VK_LEFT;
	private final int KEYCODE_2P_MOVE_SHOOT = KeyEvent.VK_SLASH;
	
	private final int INDEX_MOVE_LEFT = 0; // index of key for keycode array
	private final int INDEX_MOVE_RIGHT = 1;
	private final int INDEX_SHOOT = 2;
	
	private int[][] keyForPlayer;

	private int playerNum;
	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonnusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState[] gameState,
			final GameSettings gameSettings, final boolean bonusLife,
			final int width, final int height, final int fps) {
		super(width, height, fps);
		
		this.playerNum = gameState.length;
		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.score = new int[playerNum];
		this.lives = new int[playerNum];
		this.bulletsShot = new int[playerNum];
		this.shipsDestroyed = new int[playerNum];
		
		for(int i = 0; i < playerNum; i++)
		{
			this.level = gameState[i].getLevel();
			this.score[i] = gameState[i].getScore();
			this.lives[i] = gameState[i].getLivesRemaining();
			if (this.bonusLife)
				this.lives[i]++;
			this.bulletsShot[i] = gameState[i].getBulletsShot();
			this.shipsDestroyed[i] = gameState[i].getShipsDestroyed();
		}
		
		// setting keys, using in loops
		keyForPlayer = new int[playerNum][3]; // ex keyForPlayer[1p][move_right_key], keyForPlayer[2p][shoot_key]
		keyForPlayer[0][INDEX_MOVE_LEFT] = KEYCODE_1P_MOVE_LEFT;
		keyForPlayer[0][INDEX_MOVE_RIGHT] = KEYCODE_1P_MOVE_RIGHT;
		keyForPlayer[0][INDEX_SHOOT] = KEYCODE_1P_MOVE_SHOOT;
		
		keyForPlayer[1][INDEX_MOVE_LEFT] = KEYCODE_2P_MOVE_LEFT;
		keyForPlayer[1][INDEX_MOVE_RIGHT] = KEYCODE_2P_MOVE_RIGHT;
		keyForPlayer[1][INDEX_SHOOT] = KEYCODE_2P_MOVE_SHOOT;
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this);
		
		this.ship = new Ship[playerNum];
		for(int i = 0; i < playerNum; i++)
		{
			this.ship[i] = new Ship(this.width * (i+1) / (playerNum + 1), this.height - 30, i);
		}
			
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		for(int i = 0; i < playerNum; i++)
		{
			this.score[i] += LIFE_SCORE * (this.lives[i] - 1);
		}
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		boolean isPause = true;
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			for(int i = 0; i < playerNum; i++) {
				if (!this.ship[i].isDestroyed() && this.lives[i] > 0) {
					boolean moveRight = inputManager.isKeyDown(keyForPlayer[i][INDEX_MOVE_RIGHT]);
					boolean moveLeft = inputManager.isKeyDown(keyForPlayer[i][INDEX_MOVE_LEFT]);
	
					boolean isRightBorder = this.ship[i].getPositionX()
							+ this.ship[i].getWidth() + this.ship[i].getSpeed() > this.width - 1;
					boolean isLeftBorder = this.ship[i].getPositionX()
							- this.ship[i].getSpeed() < 1;
	
					if (moveRight && !isRightBorder) {
						this.ship[i].moveRight();
					}
					if (moveLeft && !isLeftBorder) {
						this.ship[i].moveLeft();
					}
					if (inputManager.isKeyDown(keyForPlayer[i][INDEX_SHOOT]))
						if (this.ship[i].shoot(this.bullets))
							this.bulletsShot[i]++;
				}
			}

			if (this.enemyShipSpecial != null) {
				if (!this.enemyShipSpecial.isDestroyed())
					this.enemyShipSpecial.move(2, 0);
				else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
					this.enemyShipSpecial = null;

			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip();
				this.enemyShipSpecialCooldown.reset();
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			
		
			//여기서 부터
			boolean pause = inputManager.isKeyDown(KeyEvent.VK_ESCAPE);
			if (pause) {
				//아래의 코드를 함수로 
				
				while(isPause) {
					drawPause();
					boolean regame = inputManager.isKeyDown(KeyEvent.VK_R);
					isPause = isPause(regame);
				}
			}
			//여기까지 고친 코드

			for(int i = 0; i < playerNum; i++)
			{
				this.ship[i].update();
			}

			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets);
		}

		manageCollisions();
		cleanBullets();
		draw();

		boolean isAllLive = false;
		for(int i = 0; i < playerNum; i++) // check if all ships is dead
		{
			isAllLive = isAllLive || (this.lives[i] > 0);
		}
		
		if ((this.enemyShipFormation.isEmpty() || !isAllLive)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;

	}
	
	//이거 만든 메소드
	private boolean isPause(boolean regame) {
		
		if(regame) {
			return false;
		}else {
			return true;
		}
	}
	
	//이것도 만든 메소드
	private void drawPause() {
		drawManager.initDrawing(this);
		drawManager.drawPauseMenu(this);
		drawManager.completeDrawing(this);
		
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		for(int i = 0; i < playerNum; i++)
		{
			if(this.lives[i] > 0 || this.ship[i].isDestroyed()) // draw ship when destructionCooldown is reseted.
				drawManager.drawEntity(this.ship[i], this.ship[i].getPositionX(),
						this.ship[i].getPositionY());
		}
			
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());

		// Interface.
		// temporary using score[0], lives[0]. modify this line after modify drawScore and drawLives
		drawManager.drawScore(this, this.score[0]);
		drawManager.drawLives(this, this.lives[0]);
		
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				for(int i = 0; i < playerNum; i++) {
					if (this.lives[i] > 0 && checkCollision(bullet, this.ship[i]) && !this.levelFinished) {
						recyclable.add(bullet);
						if (!this.ship[i].isDestroyed()) {
							this.ship[i].destroy();
							this.lives[i]--;
							this.logger.info("Hit on player ship, " + this.lives
									+ " lives remaining.");
						}
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score[0] += enemyShip.getPointValue(); // modify this line after make score system for each ship
						this.shipsDestroyed[0]++;
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score[0] += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed[0]++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	public final GameState[] getGameState()
	{
		GameState[] states = new GameState[playerNum];
		for(int i = 0; i < playerNum; i++)
		{
			states[i] = new GameState(this.level, this.score[i], this.lives[i], this.bulletsShot[i], this.shipsDestroyed[i]);
		}
		return states;
	}
}