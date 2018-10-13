package com.tassadar.twoohfoureight;

public interface Renderer {
    void addTile(int id, int value, int position);
    void removeTile(int id);
    void removeAllTiles();
    void setTileValue(int tileId, int value);
    void setTilePosition(int tileId, int gridIdx);
    void mergeTiles(int tileIdA, int tileIdB, int targetGridIdx);

    void shake();
    void finishAllAnimations();

    void invalidate();
}
