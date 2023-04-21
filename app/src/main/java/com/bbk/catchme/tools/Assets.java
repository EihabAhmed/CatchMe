package com.bbk.catchme.tools;

import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.impl.AndroidGame;

public class Assets {
    public static Pixmap blackBackgroundImage;
    public static Pixmap createGameImage;
    public static Pixmap joinGameImage;
    public static Pixmap cancelImage;
    public static Pixmap creatingGameImage;
    public static Pixmap joiningGameImage;
    public static Pixmap connectedImage;
    public static Pixmap readyImage;
    public static Pixmap notReadyImage;
    public static Pixmap noPlayersFoundImage;

    public static void load(AndroidGame game) {

        Settings.loadPrefs(game);
    }
}
