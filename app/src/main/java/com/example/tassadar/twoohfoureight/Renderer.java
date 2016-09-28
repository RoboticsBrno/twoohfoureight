package com.example.tassadar.twoohfoureight;

import android.animation.ObjectAnimator;


public interface Renderer {
    void addTile(int id, int value, int position);
    void removeTile(int id);
    void removeAllTiles();
    void setTileValue(int tileId, int value);
    ObjectAnimator setTilePosition(int tileId, int gridIdx, boolean animate);
    void mergeTiles(int tileIdA, int tileIdB, int targetGridIdx);

    void shake();
    void finishAllAnimations();

    void invalidate();
}
