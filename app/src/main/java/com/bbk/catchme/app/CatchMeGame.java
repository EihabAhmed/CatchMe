package com.bbk.catchme.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.network.NetworkGame;
import com.badlogic.androidgames.framework.impl.network.NetworkManager;
import com.bbk.catchme.tools.Assets;
import com.bbk.catchme.tools.GameGenerator;
import com.bbk.catchme.tools.Settings;
import com.bbk.catchme.ui.GameScreen;
import com.bbk.catchme.ui.PrepareMultiplayerScreen;

public class CatchMeGame extends NetworkGame {

    // Other player data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        worldWidth = 1080;
        worldHeight = 1920;

        TAG = "CatchMe";

        super.onCreate(savedInstanceState);

        handlerToMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // Receive messages from other player
                if (message.what == 1) {
                    receiveMessage((String) (message.obj));
                }
                // Connected as client
                if (message.what == 2) {
                    if (getCurrentScreen() instanceof PrepareMultiplayerScreen) {
                        connected = true;
                        lastTimeGotResponseFromOtherPlayer = System.nanoTime();
                        lastTimeISentToOtherPlayer = System.nanoTime();
                        networkManager.stopDiscovery();
                    }
                }
                // Connected as server
                if (message.what == 3) {
                    if (getCurrentScreen() instanceof PrepareMultiplayerScreen) {
                        connected = true;
                        lastTimeGotResponseFromOtherPlayer = System.nanoTime();
                        lastTimeISentToOtherPlayer = System.nanoTime();
                        isHost = true;
                    }
                }
                // Disconnected as server
                if (message.what == 4 || message.what == 5 || message.what == 6) {
                    // 4 = Disconnected as server
                    // 5 = Disconnected as client
                    // 6 = Messaging server disconnected

                    connected = false;
                    isHost = false;
                    otherPlayerPressedStart = false;
                    gameDataReceived = false;

                    if (getCurrentScreen() instanceof PrepareMultiplayerScreen) {
                        ((PrepareMultiplayerScreen) getCurrentScreen()).state = PrepareMultiplayerScreen.ScreenState.Normal;

                        ((PrepareMultiplayerScreen) getCurrentScreen()).justStartedConnectingToGame = true;
                    } else if (getCurrentScreen() instanceof GameScreen) {
                        if (GameGenerator.multiplayer) {
                            ((GameScreen) getCurrentScreen()).state = GameScreen.GameState.Disconnected;
                        }

                        otherPlayerPressedResume = false;
                        otherPlayerWon = false;
                    }

                    restartNetworkManager();
                }
            }
        };

        networkManager = new NetworkManager(handlerToMainThread, getApplicationContext(), TAG);
    }

    public void receiveMessage(String msg) {
        if (msg.equals("Pressed Start")) {
            //Toast.makeText(getApplicationContext(), "Opponent ready", Toast.LENGTH_SHORT).show();
            synchronized (this) {
                otherPlayerPressedStart = true;
            }
        } else if (msg.equals("Cancelled Start")) {
            //Toast.makeText(getApplicationContext(), "Opponent not ready", Toast.LENGTH_SHORT).show();
            synchronized (this) {
                otherPlayerPressedStart = false;
            }
        } else if (msg.startsWith("GameSetup")) {
            synchronized (this) {

                gameDataReceived = true;
            }
        } else if (msg.equals("Game Paused")) {
            synchronized (this) {
                if (getCurrentScreen() instanceof GameScreen) {
                    ((GameScreen) getCurrentScreen()).state = GameScreen.GameState.Paused;
                }
            }
        } else if (msg.equals("Pressed Resume")) {
            otherPlayerPressedResume = true;
        } else if (msg.equals("Resume Cancelled")) {
            otherPlayerPressedResume = false;
        } else if (msg.equals("Exit Game")) {
            synchronized (this) {
                if (getCurrentScreen() instanceof GameScreen) {
                    ((GameScreen) getCurrentScreen()).state = GameScreen.GameState.ExitGame;
                }
            }
        } else if (msg.equals("IWon")) {
            synchronized (this) {
                otherPlayerWon = true;
                ((GameScreen) getCurrentScreen()).state = GameScreen.GameState.GameOver;
            }
        }  else if (msg.equals("Iam Alive")) {
            lastTimeGotResponseFromOtherPlayer = System.nanoTime();
            otherPlayerStopped = false;
        } else if (msg.equals("Iam Closing")) {
            connected = false;
            isHost = false;
            restartNetworkManager();

            otherPlayerPressedStart = false;
            gameDataReceived = false;
            otherPlayerStopped = false;

            if (getCurrentScreen() instanceof PrepareMultiplayerScreen) {
                ((PrepareMultiplayerScreen) getCurrentScreen()).state = PrepareMultiplayerScreen.ScreenState.Normal;

                ((PrepareMultiplayerScreen) getCurrentScreen()).justStartedConnectingToGame = true;
            }
        } else if (msg.equals("Iam Stopped")) {
            otherPlayerStopped = true;
        } else if (msg.startsWith("MyFace")) {
            msg = msg.substring(msg.indexOf(" ") + 1);

//            for (int i = 0; i < otherPlayerFace.length; i++) {
//                int color = Integer.parseInt(nextToken(msg));
//                switch (color) {
//                    case 0:
//                        otherPlayerFace[i] = Colors.BLACK;
//                        break;
//                    case 1:
//                        otherPlayerFace[i] = Colors.BLUE;
//                        break;
//                    case 2:
//                        otherPlayerFace[i] = Colors.ORANGE;
//                        break;
//                    case 3:
//                        otherPlayerFace[i] = Colors.WHITE;
//                        break;
//                    case 4:
//                        otherPlayerFace[i] = Colors.YELLOW;
//                        break;
//                }
//                msg = msg.substring(msg.indexOf(" ") + 1);
//            }
        }
    }

    String nextToken(String str) {
        int index = str.indexOf(" ");

        return str.substring(0, index);
    }

    @Override
    public Screen getStartScreen() {
        return new PrepareMultiplayerScreen(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstTimeCreate) {
            Settings.loadPrefs(this);
            Assets.load(this);
            firstTimeCreate = false;
        }
    }
}
