package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static papercastle.com.papercastle.Level.Terrain.NONE;

/**
 * Class that encapsulates one instance of a level
 * Created by Josh on 5/28/2017.
 */

public class LevelState {

    enum GameState {
        PLAN, EXECUTE, SUCCESS, FAILURE
    }

    private final Level.Terrain[][] terrain; // array is [y][x]
    private final CoordinateSpace cs;
    private GameState gameState;

    private int canvasWidth = -1;
    private int uiWidth = -1;
    private int height = -1;

    // non-static objects (objects that need to move each frame)

    private final Object objectsLock = new Object();// used to synchronize access to objects sets between game and ui threads
    private final Set<GameObject> objects;
    // selectable objects
    private final Set<SelectableGameObject> selectableObjects;
    // selected object
    private volatile SelectableGameObject selectedObject;
    // player object
    private SelectableGameObject playerObject;

    // clone stuff
    private final int[] availableClones;
    private int activeClonePlacement;

    // traps
    private final Point endPos;

    // available clones
    // activeClonePlacement?

    public LevelState(Level l, int gridSize) {
        objects = new LinkedHashSet<>(); // linked for draw order in insertion order instead of random
        selectableObjects = new LinkedHashSet<>();

        if (l.getCsType() == Level.CSType.GRID) {
            cs = new GridCoordinateSpace(new Point(0, 0), gridSize, l.getLayout()[0].length, l.getLayout().length);
        } else {
            throw new IllegalArgumentException("Invalid grid type " + l.getCsType());
        }

        Point startPos = null;
        Point endPos = null;

        // deep copy terrain
        final Level.Terrain[][] layout = l.getLayout();
        terrain = new Level.Terrain[layout.length][];
        for (int y = 0; y < layout.length; y++) {
            terrain[y] = Arrays.copyOf(layout[y], layout[y].length);
            for (int x = 0; x < terrain[y].length; x++) {
                if (terrain[y][x] == NONE) {
                    continue;
                } else if (terrain[y][x] == Level.Terrain.WALL) {
                    objects.add(new WallObject(new Point(x, y)));
                } else if (terrain[y][x] == Level.Terrain.START) {
                    if (startPos != null) throw new IllegalArgumentException("Multiple start positions defined in level!");
                    startPos = new Point(x, y);
                    terrain[y][x] = NONE;
                } else if (terrain[y][x] == Level.Terrain.END) {
                    if (endPos != null) throw new IllegalArgumentException("Multiple end positions defined in level!");
                    endPos = new Point(x, y);
                    terrain[y][x] = NONE;
                    objects.add(new EndObject(new Point(x, y)));
                }
            }
        }

        if (startPos == null) throw new IllegalArgumentException("No start position defined in level");
        if (endPos == null) throw new IllegalArgumentException("No end position defined in level");
        this.endPos = endPos;

        playerObject = new SelectableGameObject(startPos, 1.0, Color.argb(255, 0, 255, 0));
        objects.add(playerObject);
        selectableObjects.add(playerObject);
        selectObject(playerObject);

        availableClones = Arrays.copyOf(l.getCloneTypes(), l.getCloneTypes().length);
        if (availableClones.length > SelectableGameObject.CLONE_TYPE_DEFS.length) {
            throw new IllegalArgumentException("want " + availableClones.length + " types of clones but only " + SelectableGameObject.CLONE_TYPE_DEFS.length);
        }
        activeClonePlacement = -1;

        switchToPlan();
    }

    private void selectObject(final SelectableGameObject newSelection) {
        if (selectedObject != null) {
            selectedObject.unselect();
        }
        if (newSelection != null) {
            newSelection.select();
        }
        selectedObject = newSelection;
    }

    public void update(final long ms) {
        if (gameState == GameState.EXECUTE) {
            // move all movable objects
            synchronized (objectsLock) {
                for (GameObject object : objects) {
                    object.update(ms);
                }
            }

            // object interactions
            final Point playerScreenPos = playerObject.getCurScreenPos(cs);
            final Point playerPos = cs.screenToPos(playerScreenPos);
            if (playerPos.equals(endPos)) {
                // won the game
                levelOver(true);
                return;
            }
        }
    }

    private void levelOver(boolean success) {
        if (success) {
            gameState = GameState.SUCCESS;
        } else {
            gameState = GameState.FAILURE;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(255, 255, 255, 255);
        canvas.drawRect(0, 0, canvasWidth, height, paint);

        // do drawing on canvas
        cs.draw(canvas, paint);

        if (activeClonePlacement != -1) {
            paint.setColor(SelectableGameObject.CLONE_TYPE_DEFS[activeClonePlacement].color());
            final Point playerScreenPos = playerObject.getCurScreenPos(cs);
            final Point playerPos = cs.screenToPos(playerScreenPos);
            final List<Point> playerNeighbors = cs.neighbors(playerPos);
            for (final Point neighbor : playerNeighbors) {
                cs.highlightCell(neighbor, canvas, paint);
            }
        }

        synchronized (objectsLock) {
            for (GameObject object : objects) {
                object.draw(cs, canvas, paint);
            }
        }

        if (isDone()) {
            final String text;
            if (gameState == GameState.SUCCESS) {
                paint.setColor(Color.GREEN);
                text = "Level Complete!";
            } else {
                paint.setColor(Color.RED);
                text = "Level Failed!";
            }
            paint.setTextAlign(Paint.Align.CENTER);
            final float textSize = 150.0f;
            paint.setTextSize(textSize);
            canvas.drawText(text, canvasWidth / 2.0f, height / 2.0f + textSize/4, paint);
        }


        paint.setARGB(255, 0, 0, 0);
        canvas.drawRect(canvasWidth, 0, canvasWidth + uiWidth, height, paint);

        // draw UI

        final int drawWidth = Math.min(height / 8, uiWidth / 2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        int centerX = canvasWidth + uiWidth / 2;
        int centerY = height / 8;

        if (gameState == GameState.PLAN) {
            // play button
            final Point topLeft = new Point(centerX - drawWidth / 2, centerY - drawWidth / 2);
            final Point bottomLeft = new Point(centerX - drawWidth / 2, centerY + drawWidth / 2);
            final Point right = new Point(centerX + drawWidth / 2, centerY);

            final Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(bottomLeft.x, bottomLeft.y);
            path.lineTo(right.x, right.y);
            path.lineTo(topLeft.x, topLeft.y);
            path.close();
            canvas.drawPath(path, paint);
        } else if (gameState == GameState.EXECUTE) {
            // pause button
            canvas.drawRect(centerX - drawWidth / 2, centerY - drawWidth / 2, centerX - drawWidth / 4, centerY + drawWidth / 2, paint);
            canvas.drawRect(centerX + drawWidth / 4, centerY - drawWidth / 2, centerX + drawWidth / 2, centerY + drawWidth / 2, paint);
        }

        // draw any active clone placements
        int pos = 0;
        for (int i = 0; i < availableClones.length; i++) {
            if (availableClones[i] > 0) {

                // draw a line at the top
                drawUILine((2 + pos) * height / 8, canvas, paint);

                centerX = canvasWidth + drawWidth * 2 / 3;
                centerY = (5 + pos * 2) * height / 16;
                paint.setColor(SelectableGameObject.CLONE_TYPE_DEFS[i].color());

                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, centerY, drawWidth / 3, paint);

                paint.setColor(Color.WHITE);
                final int textSize = drawWidth * 2 / 3;
                paint.setTextSize(textSize);
                canvas.drawText("" + availableClones[i], canvasWidth + drawWidth * 4 / 3, centerY + textSize / 2, paint);

                pos++;
            }
        }

        // draw a line at the bottom
        drawUILine((2 + pos) * height / 8, canvas, paint);
    }

    private void drawUILine(int uiY, Canvas canvas, Paint paint) {
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(2.0f);
        canvas.drawLine(canvasWidth, uiY, canvasWidth + uiWidth, uiY, paint);
    }

    public void updateUI(int canvasWidth, int uiWidth, int height, int gridSize) {
        if (cs != null) {
            cs.updateGridSize(gridSize);
        }
        this.canvasWidth = canvasWidth;
        this.uiWidth = uiWidth;
        this.height = height;
    }

    public boolean isDone() {
        return gameState == GameState.SUCCESS || gameState == GameState.FAILURE;
    }

    public GameState getGameState() {
        return gameState;
    }

    private boolean pointInBounds(final Point pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.y <= terrain.length && pos.x <= terrain[pos.y].length;
    }

    private boolean isPassable(final Point pos) {
        return pointInBounds(pos) && terrain[pos.y][pos.x] == NONE;
    }

    public void handleClick(int screenX, int screenY) {
        if (screenX < canvasWidth) {
            handleCanvasClick(screenX, screenY);
        } else {
            handleUIClick(screenX - canvasWidth, screenY);
        }
    }

    private void handleUIClick(int uiX, int uiY) {
        if (uiY < height / 4) {
            // play button
            if (gameState == GameState.EXECUTE) {
                switchToPlan();
            } else if (gameState == GameState.PLAN) {
                switchToExecute();
            }
        } else {
            if (gameState == GameState.EXECUTE) {
                switchToPlan();
            }
            final int index = uiY / (height / 8) - 2;
            int pos = 0;
            int i;
            // find the index'th non-zero int in available clones
            for (i = 0; i < availableClones.length; i++) {
                if (availableClones[i] > 0) {
                    if (pos == index) {
                        break;
                    }
                    pos++;
                }
            }
            if (i < availableClones.length) {
                Log.e("LevelState", "clicked clone pos " + pos + " which is clone index " + i);
                startClonePlacement(i);
            }
        }
    }

    private void stopClonePlacement() {
        activeClonePlacement = -1;
    }

    private void startClonePlacement(final int cloneTypeNum) {
        stopClonePlacement();
        activeClonePlacement = cloneTypeNum;
        selectObject(null);
    }

    private void successfulClonePlacement(final Point p, final int cloneTypeNum) {
        stopClonePlacement();
        final SelectableGameObject newClone = SelectableGameObject.CLONE_TYPE_DEFS[cloneTypeNum].create(p);
        synchronized (objectsLock) {
            objects.add(newClone);
            selectableObjects.add(newClone);
        }
        availableClones[cloneTypeNum]--;
    }

    private void handleCanvasClick(int screenX, int screenY) {
        if (gameState == GameState.EXECUTE) {
            switchToPlan();
            return;
        }

        final Point clickPos = cs.screenToPos(new Point(screenX, screenY));

        if (activeClonePlacement != -1) {
            final Point playerScreenPos = playerObject.getCurScreenPos(cs);
            final Point playerPos = cs.screenToPos(playerScreenPos);
            final int clickDist = cs.distance(clickPos, playerPos);
            if (clickDist == 1 && isPassable(clickPos)) {
                successfulClonePlacement(clickPos, activeClonePlacement);
                return;
            }
            stopClonePlacement();
            return;
        }

        if (selectedObject != null) {
            final Point lastPathPoint = selectedObject.getLastPathPoint();
            final int manDist = cs.distance(clickPos, lastPathPoint);
            if (manDist == 1 && isPassable(clickPos)) {
                selectedObject.addPointToPath(clickPos);
            } else if (manDist >= 2) {
                selectObject(getClickedSelectableObject(screenX, screenY));
            }
        } else {
            final SelectableGameObject clicked = getClickedSelectableObject(screenX, screenY);
            selectObject(clicked);
        }
    }

    private void switchToPlan() {
        gameState = GameState.PLAN;
    }

    private void switchToExecute() {
        gameState = GameState.EXECUTE;
        selectObject(null);
    }


    private SelectableGameObject getClickedSelectableObject(int screenX, int screenY) {
        SelectableGameObject closest = null;
        int smallestDist = Integer.MAX_VALUE;

        synchronized (objectsLock) {
            for (final SelectableGameObject object : selectableObjects) {
                final Point pos = object.getCurScreenPos(cs);
                final int distX = pos.x - screenX;
                final int distY = pos.y - screenY;
                final int screenDist2 = distX * distX + distY * distY;
                if (screenDist2 < smallestDist && (Math.abs(distX) <= cs.getGridSize() / 2) && (Math.abs(distY) <= cs.getGridSize() / 2)) {
                    closest = object;
                    smallestDist = screenDist2;
                }
            }
        }
        return closest;
    }
}
