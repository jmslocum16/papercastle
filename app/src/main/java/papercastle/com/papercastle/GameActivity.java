package papercastle.com.papercastle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.LinkedHashSet;
import java.util.Set;

public class GameActivity extends Activity {

    private static final long TARGET_FRAME_MS = 16;

    GameView gameView;

    public static final String LEVEL_MESSAGE = "start.level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        final int startLevel = intent.getIntExtra(LEVEL_MESSAGE, 0);

        gameView = new GameView(this, startLevel);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

    static class GameView extends SurfaceView {

        // pause and resume stuff
        private Thread gameThread = null;
        private volatile boolean running;

        // drawing stuff
        private final SurfaceHolder canvasHolder;
        private final Paint paint;

        private int width = -1;
        private int height = -1;
        private int gridSize = -1; // TODO

        // level state
        private int currentLevel;
        private LevelState levelState;

        public GameView(final Context context, final int startLevel) {
            super(context);
            currentLevel = startLevel;

            canvasHolder = getHolder();
            paint = new Paint();

            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() != MotionEvent.ACTION_UP) {
                        // TODO probably do dragging with this later
                        return true;
                    }
                    if (v == GameView.this) {
                        handleClick((int)event.getX(), (int)event.getY());
                    } else {
                        Log.e("GameView", "touch listener got something for another view");
                    }
                    return true;
                }
            });

            setup();

            resume();


        }

        private void handleClick(int screenX, int screenY) {
            if (levelState.isDone()) {
                if (levelState.getGameState() == LevelState.GameState.SUCCESS) {
                    currentLevel++;
                }
                setupLevel();
            } else {
                final LevelState.GameState prevState = levelState.getGameState();
                levelState.handleClick(screenX, screenY);
                final LevelState.GameState newState = levelState.getGameState();
                if (prevState != newState) {
                    // TODO UI stuff
                    Log.e("GameView", "game state changed to " + newState);
                }
            }
        }

        // TODO make this work based on screen dimensions
        private void computeGridSize() {
            gridSize = 135;
        }

        private void setup() {
            // TODO bunch of stuff
            setupLevel();
        }

        private void setupLevel() {
            currentLevel = Math.min(currentLevel, Level.ALL_LEVELS.length - 1);
            levelState = new LevelState(Level.ALL_LEVELS[currentLevel], gridSize);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            width = getWidth();
            height = getHeight();
            computeGridSize();
            if (levelState != null) {
                levelState.updateGridSize(gridSize);
            }
        }

        private void doRun() {
            long lastFrameMS = TARGET_FRAME_MS;
            while (running) {
                long frameTime = -System.currentTimeMillis();

                doUpdate(lastFrameMS);

                doDraw();

                frameTime += System.currentTimeMillis();

                lastFrameMS = Math.max(TARGET_FRAME_MS, frameTime);

                // TODO record statistics about frame time?

                final long sleepTime = Math.max(0, TARGET_FRAME_MS - frameTime);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Log.e("Error:", "runner thread interrupted while sleeping");
                    }
                }

            }
        }

        private void doUpdate(final long ms) {
            levelState.update(ms);
        }

        private void doDraw() {
            if (canvasHolder.getSurface().isValid() && width != -1 && height != -1) {
                // Lock the canvas ready to draw
                final Canvas canvas = canvasHolder.lockCanvas();

                levelState.draw(canvas, paint, width, height);

                // Draw everything to the screen
                canvasHolder.unlockCanvasAndPost(canvas);
            }
        }


        // pause and resume stuff
        private void pause() {
            running = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        private void resume() {
            running = true;
            gameThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doRun();
                }
            });
            gameThread.start();
        }
    }
}
