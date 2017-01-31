package com.example.tassadar.foobar;

import android.animation.ObjectAnimator;

public interface Renderer {
    public void addTile(int id, int value, int position);
    public ObjectAnimator setTilePosition(int id, int position, boolean animate);
    public void mergeTiles(int tileIdTarget, int tileIdToMove);
    public void removeAllTiles();
    public void removeTile(int id);
}