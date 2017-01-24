package com.example.tassadar.foobar;

import java.util.Random;

/**
 * Created by apophis on 24/01/17.
 */

public class GameController {
    private static final int TILES_CNT = RenderImpl.GRID*RenderImpl.GRID;
    private static final int START_TILES = 4;

    Renderer r;

    private class Tile {
        int id;
        int value; // value = 0 <-> empty tile
    }

    Tile[] tiles;

    GameController(Renderer r) {
        this.r = r;

        tiles = new Tile[TILES_CNT];
        for(int i = 0; i < TILES_CNT; i++)
            tiles[i] = new Tile();
    }

    void restart() {
        // remove all tails
        for (int i = 0; i < START_TILES; i++) {
            tiles[i].value = 0;
            tiles[i].id = 0;
        }
        r.removeAllTails();

        // create new tails
        Random rnd = new Random();
        int pos;

        for(int i = 0; i < START_TILES; i++) {
            // generate unique pos
            do {
                pos = rnd.nextInt(TILES_CNT);
            } while (tiles[pos].value != 0);

            tiles[pos].id = i;
            if (rnd.nextBoolean())
                tiles[pos].value = 2;
            else
                tiles[pos].value = 4;

            r.addTile(tiles[pos].id, tiles[pos].value, pos);
        }
    }

}
