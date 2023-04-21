package com.badlogic.androidgames.framework.impl.network;

import android.os.Bundle;
import android.os.Handler;

import com.badlogic.androidgames.framework.impl.AndroidGame;

public abstract class NetworkGame extends AndroidGame {
    public boolean firstTimeCreate = true;

    /*************** Network **************************/
    public NetworkManager networkManager;

    public boolean isHost;
    public boolean connected;
    public final double CONNECTION_TIMEOUT_PERIOD = 2e9;
    public final double CONNECTION_TIMEOUT_PERIOD_GAMEOVER = 30e9;
    public final double CONNECTION_TIMEOUT_PERIOD_PAUSED = 30e9;

    public boolean otherPlayerPressedStart;
    public boolean gameDataReceived;
    public boolean otherPlayerPressedResume;
    public boolean otherPlayerWon;
    public boolean otherPlayerStopped;
    /*************** Network **************************/

    /*************** Multithreading *******************/
    public Handler handlerToMainThread;
    public long lastTimeGotResponseFromOtherPlayer;
    public long lastTimeISentToOtherPlayer;
    /*************** Multithreading *******************/

    public String TAG;
    public String serviceTypeName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void restartNetworkManager() {
        if (networkManager != null) {
            sendMessage("Iam Closing");
            networkManager.dispose();
        }
        networkManager = new NetworkManager(handlerToMainThread, getApplicationContext(), TAG, serviceTypeName);
    }

    public void createServer(NetworkScreen screen) {
        if (!networkManager.initializeServer()) {
            screen.state = NetworkScreen.ScreenState.Normal;
            restartNetworkManager();
        }
    }

    public void findServer(NetworkScreen screen) {
        if (!networkManager.findServer()) {
            screen.state = NetworkScreen.ScreenState.Normal;
            restartNetworkManager();
        }
    }

    public void connectToServer() {
        networkManager.connectToServer();
    }

    public void sendMessage(String msg) {
        networkManager.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendMessage("Iam Stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendMessage("Iam Closing");
        networkManager.dispose();
    }
}
