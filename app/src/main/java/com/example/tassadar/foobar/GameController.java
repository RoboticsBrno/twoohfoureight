package com.example.tassadar.foobar;

import android.content.Context;
import android.widget.Toast;

import java.util.Random;

public class GameController {
    public static final int GRID = 4;
    public static final int TILES_CNT = GRID * GRID;
    private static final int START_TILES = 4;

    private class Tile {
        int id;
        int value; // value = 0 <-> empty tile
    }

    public static final int D_LEFT = 0;
    public static final int D_RIGHT = 1;
    public static final int D_UP = 2;
    public static final int D_DOWN = 3;

    private Tile[] tiles;
    private Renderer m_renderer;

    GameController(Renderer r) {
        m_renderer = r;

        tiles = new Tile[TILES_CNT];
        for(int i = 0; i < TILES_CNT; i++)
            tiles[i] = new Tile();
    }

    void restart() {
        // remove all tiles
        for (int i = 0; i < START_TILES; i++) {
            tiles[i].value = 0;
            tiles[i].id = 0;
        }
        m_renderer.removeAllTiles();

        // create new tiles
        Random rnd = new Random();
        int pos;

        for(int i = 0; i < START_TILES; i++) {
            // generate unique pos
            do {
                pos = rnd.nextInt(TILES_CNT);
            } while (tiles[pos].value != 0);

            tiles[pos].id = i;
            if (rnd.nextFloat() >= 0.9f)
                tiles[pos].value = 2;
            else
                tiles[pos].value = 4;

            m_renderer.addTile(tiles[pos].id, tiles[pos].value, pos);
        }
    }

    // TODO: remove when moving is implemented
    private Toast m_directionToastTEMP;
    public void swipe(Context ctx, int direction) {
        if(m_directionToastTEMP != null) {
            m_directionToastTEMP.cancel();
        }

        String dir = "";
        switch(direction) {
            case D_LEFT: dir = "left"; break;
            case D_RIGHT: dir = "right"; break;
            case D_DOWN: dir = "down"; break;
            case D_UP: dir = "up"; break;
        }

        m_directionToastTEMP = Toast.makeText(ctx, "Swipe direction: " + dir, Toast.LENGTH_SHORT);
        m_directionToastTEMP.show();
    }

}
