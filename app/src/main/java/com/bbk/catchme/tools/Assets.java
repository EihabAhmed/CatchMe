package com.bbk.catchme.tools;

import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.impl.AndroidGame;

public class Assets {
    public static Pixmap blackBackgroundImage;

    public static void load(AndroidGame game) {

        Settings.loadPrefs(game);
    }
}
