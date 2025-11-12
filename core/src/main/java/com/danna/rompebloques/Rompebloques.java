package com.danna.rompebloques;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Rompebloques extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
