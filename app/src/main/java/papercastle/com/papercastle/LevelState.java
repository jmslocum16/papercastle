package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashSet;
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

    // non-static objects (objects that need to move each frame)
    private final Set<GameObject> objects;
    // selectable objects
    private final Set<SelectableGameObject> selectableObjects;
    // selected object
    private volatile SelectableGameObject selectedObject;
    // player object
    private SelectableGameObject playerObject;

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

        gameState = GameState.PLAN;

        // TODO remove once cloning implemented
        final SelectableGameObject tmpClone = new SelectableGameObject(new Point(4, 4), 1.0, Color.argb(255, 255, 160, 0));
        objects.add(tmpClone);
        selectableObjects.add(tmpClone);
        selectObject(tmpClone);
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
            for (GameObject object: objects) {
                object.update(ms);
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
            // TODO UI stuff?
        } else {
            gameState = GameState.FAILURE;
            // TODO UI stuff?
        }
    }

    public void draw(Canvas canvas, Paint paint, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(255, 255, 255, 255);
        canvas.drawRect(0, 0, width, height, paint);

        // do drawing on canvas
        cs.draw(canvas, paint);

        for (GameObject object : objects) {
            object.draw(cs, canvas, paint);
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
            paint.setTextSize(200.0f);
            canvas.drawText(text, width / 2.0f, height / 2.0f, paint);
        }
    }

    public void updateGridSize(int gridSize) {
        if (cs != null) {
            cs.updateGridSize(gridSize);
        }
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
        if (gameState == GameState.EXECUTE) {
            switchToPlan();
            return;
        }
        // TODO have to move changing levels outside of here

        final Point pos = cs.screenToPos(new Point(screenX, screenY));
        // TODO clone placement
        if (selectedObject != null) {
            final Point lastPathPoint = selectedObject.getLastPathPoint();
            final int manDist = cs.distance(pos, lastPathPoint);
            if (manDist == 1 && isPassable(pos)) {
                selectedObject.addPointToPath(pos);
            } else if (manDist >= 2) {
                selectObject(getClickedSelectableObject(screenX, screenY));
            }
        } else {
            // TODO once have ui simplify
            final SelectableGameObject clicked = getClickedSelectableObject(screenX, screenY);
            selectObject(clicked);
            // TODO remove once have ui
            if (clicked == null) {
                switchToExecute();
            }
        }
    }

    private void switchToPlan() {
        gameState = GameState.PLAN;
        // TODO UI stuff will have to be out of here
    }

    private void switchToExecute() {
        gameState = GameState.EXECUTE;
        selectObject(null);
        // TODO UI stuff will have to be out of here
    }


    private SelectableGameObject getClickedSelectableObject(int screenX, int screenY) {
        SelectableGameObject closest = null;
        int smallestDist = Integer.MAX_VALUE;

        for (final SelectableGameObject object : selectableObjects) {
            final Point pos = object.getCurScreenPos(cs);
            final int distX = pos.x - screenX;
            final int distY = pos.y - screenY;
            final int screenDist2 = distX * distX + distY * distY;
            if (screenDist2 < smallestDist && (Math.abs(distX) <= cs.getGridSize()/2) && (Math.abs(distY) <= cs.getGridSize()/2)) {
                closest = object;
                smallestDist = screenDist2;
            }
        }
        return closest;
    }
}
