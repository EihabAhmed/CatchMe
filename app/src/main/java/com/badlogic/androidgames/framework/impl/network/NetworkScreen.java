package com.badlogic.androidgames.framework.impl.network;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Screen;

public abstract class NetworkScreen extends Screen {

    public enum ScreenState {
        Normal,
        CreatingServer,
        ServerCreated,
        FindingGame,
        ConnectingToServer,
        Connected,
        NoPlayersFound,
        PressedStart
    }

    public ScreenState state;

    public boolean justStartedConnectingToGame = true;
    public long connectingToGameStartTime;

    public boolean firstTimeStartCreatingServer = true;
    public boolean firstTimeStartFindingGame = true;
    public boolean firstTimeStartConnectingToServer = true;
    public boolean firstTimeConnecting = true;
    public long startConnectingTime;

    public NetworkScreen(Game game) {
        super(game);
    }
}
