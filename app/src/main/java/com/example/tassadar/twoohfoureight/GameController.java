package com.example.tassadar.twoohfoureight;

import android.os.Bundle;

import java.util.HashSet;
import java.util.Random;

public class GameController {
    public interface OnStateChangeListener {
        void onScoreChanged(int score);
    }

    public static final int GRID = 4;
    public static final int MAX_TILES = GRID*GRID;

    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    public static final int UP = -GRID;
    public static final int DOWN = GRID;

    private static final int START_TILES = 2;

    private static class Tile {
        int id;
        int value;
    }

    private Tile[] m_tiles;
    private int m_tileIdCounter;
    private int m_usedTiles;
    private Random m_random;
    private PositionSeeker m_seeker;

    private OnStateChangeListener m_listener;

    private static final int STATE_UNINITIALIZED = 0;
    private static final int STATE_IN_GAME = 1;
    private int m_state;
    private int m_score;

    GameController() {
        m_tiles = new Tile[MAX_TILES];
        for(int i = 0; i < m_tiles.length; ++i) {
            m_tiles[i] = new Tile();
        }

        m_random = new Random();
        m_seeker = new PositionSeeker();
    }

    private void initialize(Renderer renderer) {
        m_usedTiles = 0;
        m_score = 0;
        m_state = STATE_IN_GAME;

        renderer.removeAllTiles();
        for(int i = 0; i < START_TILES; ++i) {
            addRandomTile(renderer);
        }

        renderer.invalidate();
    }

    public void setListener(OnStateChangeListener listener) {
        m_listener = listener;
    }

    private void setScore(int val) {
        m_score = val;
        if(m_listener != null) {
            m_listener.onScoreChanged(m_score);
        }
    }

    public void restoreState(Renderer renderer) {
        switch(m_state) {
            case STATE_UNINITIALIZED:
                initialize(renderer);
                break;
            case STATE_IN_GAME:
                renderer.removeAllTiles();
                for(int i = 0; i < MAX_TILES; ++i) {
                    if(m_tiles[i].value != 0) {
                        renderer.addTile(m_tiles[i].id, m_tiles[i].value, i);
                    }
                }
                renderer.invalidate();
                break;
        }
    }

    public void saveInstanceState(Bundle state) {
        state.putInt("game_state", m_state);
        state.putInt("game_score", m_score);

        int[] tileValues = new int[MAX_TILES];
        for(int i = 0; i < MAX_TILES; ++i) {
            tileValues[i] = m_tiles[i].value;
        }
        state.putIntArray("game_tiles", tileValues);
    }

    public void restoreInstanceState(Bundle state) {
        m_state = state.getInt("game_state");
        setScore(state.getInt("game_score"));

        int[] tileValues = state.getIntArray("game_tiles");
        if(m_state >= STATE_IN_GAME && tileValues != null && tileValues.length == MAX_TILES) {
            for(int i = 0; i < MAX_TILES; ++i) {
                if(tileValues[i] != 0) {
                    m_tiles[i].id = ++m_tileIdCounter;
                    m_tiles[i].value = tileValues[i];
                }
            }
        }
    }

    public void onSwipe(int direction, Renderer renderer) {
        if(m_state != STATE_IN_GAME) {
            return;
        }

        boolean moved = false;
        int points = 0;
        int start, delta;

        if(direction > 0) {
            start = MAX_TILES - 1;
            delta = -1;
        } else {
            start = 0;
            delta = 1;
        }

        renderer.finishAllAnimations();

        m_seeker.reset();

        for(int i = start; i >= 0 && i < MAX_TILES; i += delta) {
            Tile t = m_tiles[i];
            if(t.value == 0) {
                continue;
            }

            if(!isEdgePosition(direction, i) && m_seeker.calculateNext(direction, i, t.value)) {
                final int next = m_seeker.currentPos;
                moved = true;

                if(m_seeker.isMerge) {
                    Tile nextTile = m_tiles[next];
                    renderer.mergeTiles(nextTile.id, t.id, next);

                    nextTile.value *= 2;
                    t.id = 0;
                    t.value = 0;

                    --m_usedTiles;
                    points += nextTile.value;
                } else {
                    renderer.setTilePosition(t.id, next, true);
                    m_tiles[i] = m_tiles[next];
                    m_tiles[next] = t;
                }
            }
        }

        if(points != 0)
            setScore(m_score + points);

        if(moved) {
            addRandomTile(renderer);
        } else {
            renderer.shake();
        }
        renderer.invalidate();
    }

    private class PositionSeeker {
        HashSet<Integer> merged;
        int currentPos;
        boolean isMerge;

        PositionSeeker() {
            merged = new HashSet<>();
        }

        void reset() {
            merged.clear();
        }

        boolean calculateNext(int direction, int startPos, int value) {
            currentPos = startPos;
            isMerge = false;

            final int startX = currentPos % GRID;
            final int startY = currentPos / GRID;

            while(true) {
                int next = currentPos + direction;

                // moves out of current row/column
                if(isPastEdge(direction, next, startX, startY)) {
                    return currentPos != startPos;
                }

                // moves over another tile
                if(m_tiles[next].value != 0){
                    // we can merge
                    if(m_tiles[next].value == value && !merged.contains(next)) {
                        currentPos = next;
                        isMerge = true;
                        merged.add(next);
                        return true;
                    } else {
                        return currentPos != startPos;
                    }
                }

                currentPos = next;
            }
        }

        private boolean isPastEdge(int direction, int pos, int startX, int startY) {
            if(pos < 0 || pos >= MAX_TILES)
                return true;

            switch(direction) {
                case LEFT:
                case RIGHT:
                    return pos / GRID != startY;
                case UP:
                case DOWN:
                    return pos % GRID != startX;
            }
            return false;
        }
    }

    private boolean isEdgePosition(int direction, int pos) {
        switch(direction) {
            case LEFT: return pos%GRID == 0;
            case RIGHT: return pos%GRID == GRID-1;
            case UP: return (pos + direction) < 0;
            case DOWN: return (pos + direction) >= MAX_TILES;
        }
        return false;
    }

    private boolean addRandomTile(Renderer renderer) {
        if(m_usedTiles >= MAX_TILES) {
            return false;
        }

        int pos = getRandomFreePosition();

        ++m_usedTiles;
        m_tiles[pos].value = m_random.nextFloat() < 0.9 ? 2 : 4;
        m_tiles[pos].id = ++m_tileIdCounter;

        renderer.addTile(m_tiles[pos].id, m_tiles[pos].value, pos);
        return true;
    }

    private int getRandomFreePosition() {
        while(m_usedTiles < MAX_TILES) {
            int idx = m_random.nextInt(MAX_TILES);
            if(m_tiles[idx].value == 0) {
                return idx;
            }
        }
        return 0;
    }
}
