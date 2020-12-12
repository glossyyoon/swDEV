package screen;

import engine.Cooldown;
import engine.Core;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class DifficultyLevelScreen extends Screen {
    private static final int SELECTION_TIME = 200;

    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;
    private static int option =2; //option을 만들어줘서 난이도 선택가능

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public DifficultyLevelScreen(final int width, final int height, final int fps) {
        super(width, height, fps);

        // Defaults to play.
        this.returnCode = 1;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (this.selectionCooldown.checkFinished()
                && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP)
                    || inputManager.isKeyDown(KeyEvent.VK_W)) {
                previousMenuItem();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
                    || inputManager.isKeyDown(KeyEvent.VK_S)) {
                nextMenuItem();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
                this.isRunning = false;
        }
    }

    private void nextMenuItem() {
        if (this.option == 3)
            this.option = 0;
        else if (this.option == 0)
            this.option = 2;
        else
            this.option++;
    }

    private void previousMenuItem() {
        if (this.option == 2)
            this.option = 0;
        else if (this.option == 0)
            this.option = 3;
        else
            this.option--;
    }

    private void draw() {
        drawManager.initDrawing(this);
        drawManager.difficultyMenu(this,option);
        drawManager.completeDrawing(this);
    }

    public static final int getOption(){
        return option;
    }
}
