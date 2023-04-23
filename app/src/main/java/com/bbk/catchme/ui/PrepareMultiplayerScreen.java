package com.bbk.catchme.ui;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.impl.network.NetworkScreen;
import com.bbk.catchme.app.CatchMeGame;
import com.bbk.catchme.tools.Assets;

import java.util.List;

public class PrepareMultiplayerScreen extends NetworkScreen {
    private final CatchMeGame myGame;

    private int creatingJoining = 0;
    private long creatingJoiningStartTime;

    public PrepareMultiplayerScreen(Game game) {
        super(game);
        myGame = (CatchMeGame) game;

        if (myGame.connected) {
            state = ScreenState.Connected;
        } else {
            state = ScreenState.Normal;
        }

        myGame.gameDataReceived = false;

        resume();
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

        if (state == ScreenState.Normal)
            updateNormal(touchEvents, keyEvents);
        else if (state == ScreenState.CreatingServer)
            updateCreatingServer(touchEvents, keyEvents);
        else if (state == ScreenState.ServerCreated)
            updateServerCreated(touchEvents, keyEvents);
        else if (state == ScreenState.FindingGame)
            updateFindingGame(touchEvents, keyEvents);
        else if (state == ScreenState.ConnectingToServer)
            updateConnectingToServer(touchEvents, keyEvents);
        else if (state == ScreenState.Connected)
            updateConnected(touchEvents, keyEvents);
        else if (state == ScreenState.NoPlayersFound)
            updateNoPlayersFound(touchEvents, keyEvents);
        else if (state == ScreenState.PressedStart)
            updatePressedStart(touchEvents, keyEvents);

        if (myGame.connected) {
            double timeout;
            if (myGame.otherPlayerStopped)
                timeout = myGame.CONNECTION_TIMEOUT_PERIOD_PAUSED;
            else
                timeout = myGame.CONNECTION_TIMEOUT_PERIOD;

            if (System.nanoTime() - myGame.lastTimeGotResponseFromOtherPlayer >= timeout) {
                state = ScreenState.Normal;

                justStartedConnectingToGame = true;

                myGame.connected = false;
                myGame.isHost = false;

                myGame.otherPlayerPressedStart = false;
                myGame.gameDataReceived = false;

                myGame.restartNetworkManager();
            } else if (System.nanoTime() - myGame.lastTimeISentToOtherPlayer >= 1e9) {
                myGame.sendMessage("Iam Alive");
                myGame.lastTimeISentToOtherPlayer = System.nanoTime();
            }
        }
    }

    private void updateNormal(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                myGame.finish();
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 300 && event.y <= 300 + 185) {
                        firstTimeStartCreatingServer = true;
                        state = ScreenState.CreatingServer;
                        creatingJoiningStartTime = System.nanoTime();

                        return;
                    } else if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 600 && event.y <= 600 + 185) {
                        firstTimeStartFindingGame = true;
                        state = ScreenState.FindingGame;
                        creatingJoiningStartTime = System.nanoTime();

                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }
    }

    private void updateCreatingServer(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Normal;
                myGame.restartNetworkManager();
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 800 && event.y <= 800 + 185) {
                        state = ScreenState.Normal;
                        myGame.restartNetworkManager();
                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }

        if (firstTimeStartCreatingServer) {
            firstTimeStartCreatingServer = false;
            myGame.createServer(this);
        } else {
            if (myGame.networkManager.nsdHelper.serviceRegistered) {
                state = ScreenState.ServerCreated;
            }
        }

        if (System.nanoTime() - creatingJoiningStartTime >= 0.3e9) {
            creatingJoiningStartTime = System.nanoTime();
            creatingJoining++;
            if (creatingJoining == 4) {
                creatingJoining = 0;
            }
        }
    }

    private void updateServerCreated(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Normal;
                myGame.restartNetworkManager();
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 800 && event.y <= 800 + 185) {
                        state = ScreenState.Normal;
                        myGame.restartNetworkManager();
                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }

        if (myGame.connected && System.nanoTime() - startConnectingTime >= 2e9) {
            state = ScreenState.Connected;
        }

        if (System.nanoTime() - creatingJoiningStartTime >= 0.3e9) {
            creatingJoiningStartTime = System.nanoTime();
            creatingJoining++;
            if (creatingJoining == 4) {
                creatingJoining = 0;
            }
        }
    }

    private void updateFindingGame(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Normal;
                myGame.restartNetworkManager();
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 800 && event.y <= 800 + 185) {
                        state = ScreenState.Normal;
                        myGame.restartNetworkManager();
                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }

        if (firstTimeStartFindingGame) {
            firstTimeStartFindingGame = false;
            myGame.findServer(this);
        } else {
            if (myGame.networkManager.nsdHelper.serviceResolved) {
                myGame.networkManager.getHostInfo();
                firstTimeStartConnectingToServer = true;
                state = ScreenState.ConnectingToServer;
            }
        }

        if (System.nanoTime() - creatingJoiningStartTime >= 0.3e9) {
            creatingJoiningStartTime = System.nanoTime();
            creatingJoining++;
            if (creatingJoining == 4) {
                creatingJoining = 0;
            }
        }
    }


    private void updateConnectingToServer(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Normal;
                myGame.restartNetworkManager();
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 230 && event.x <= 230 + 620 && event.y >= 800 && event.y <= 800 + 185) {
                        state = ScreenState.Normal;
                        myGame.restartNetworkManager();
                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }

        if (firstTimeStartConnectingToServer) {
            firstTimeStartConnectingToServer = false;
            myGame.connectToServer();
        } else {
            if (justStartedConnectingToGame) {
                justStartedConnectingToGame = false;
                connectingToGameStartTime = System.nanoTime();
            } else {
                if (System.nanoTime() - connectingToGameStartTime >= 10e9) {
                    state = ScreenState.NoPlayersFound; // Todo: display status "cannot connect" to the user
                    justStartedConnectingToGame = true;
                }
            }
        }

        if (myGame.connected && System.nanoTime() - startConnectingTime >= 2e9) {
            state = ScreenState.Connected;
        }

        if (System.nanoTime() - creatingJoiningStartTime >= 0.3e9) {
            creatingJoiningStartTime = System.nanoTime();
            creatingJoining++;
            if (creatingJoining == 4) {
                creatingJoining = 0;
            }
        }
    }

    private void updateConnected(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                myGame.finish();
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 422 && event.x <= 422 + 236 && event.y >= 800 && event.y <= 800 + 40) {
                        state = ScreenState.PressedStart;
                        //Send a message to the other player to notify him that this player pressed start
                        myGame.sendMessage("Pressed Start");
                        return;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }
    }

    private void updateNoPlayersFound(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Normal;
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {

                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }
    }

    private void updatePressedStart(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = ScreenState.Connected;
                //Send a message to the other player to notify him that this player is not ready to start
                myGame.sendMessage("Cancelled Start");
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    state = ScreenState.Connected;
                    //Send a message to the other player to notify him that this player is not ready to start
                    myGame.sendMessage("Cancelled Start");
                    return;
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {

                }

                if (event.type == TouchEvent.TOUCH_UP) {

                }
            }
        }

        if (myGame.otherPlayerPressedStart) {
            myGame.otherPlayerPressedStart = false;
            // start game
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();
        
        g.drawPixmap(Assets.blackBackgroundImage, 0, 0);

        if (state == ScreenState.Normal)
            drawNormal(g);
        else if (state == ScreenState.CreatingServer || state == ScreenState.ServerCreated)
            drawConnectingAsServer(g);
        else if (state == ScreenState.FindingGame || state == ScreenState.ConnectingToServer)
            drawConnectingAsClient(g);
        else if (state == ScreenState.Connected)
            drawConnected(g);
        else if (state == ScreenState.NoPlayersFound)
            drawNoPlayersFound(g);
        else if (state == ScreenState.PressedStart)
            drawPressedStart(g);
    }

    private void drawNormal(Graphics g) {
        g.drawPixmap(Assets.createGameImage, 230, 300);
        g.drawPixmap(Assets.joinGameImage, 230, 600);
    }

    private void drawConnectingAsServer(Graphics g) {
        switch (creatingJoining) {
            case 0:
                g.drawPixmap(Assets.creatingGameImage, 316, 300, 0, 0, 410, 36);
                break;
            case 1:
                g.drawPixmap(Assets.creatingGameImage, 316, 300, 0, 0, 422, 36);
                break;
            case 2:
                g.drawPixmap(Assets.creatingGameImage, 316, 300, 0, 0, 436, 36);
                break;
            case 3:
                g.drawPixmap(Assets.creatingGameImage, 316, 300);
                break;
        }

        g.drawPixmap(Assets.cancelImage, 230, 800);
    }

    private void drawConnectingAsClient(Graphics g) {
        switch (creatingJoining) {
            case 0:
                g.drawPixmap(Assets.joiningGameImage, 338, 300, 0, 0, 365, 45);
                break;
            case 1:
                g.drawPixmap(Assets.joiningGameImage, 338, 300, 0, 0, 378, 45);
                break;
            case 2:
                g.drawPixmap(Assets.joiningGameImage, 338, 300, 0, 0, 392, 45);
                break;
            case 3:
                g.drawPixmap(Assets.joiningGameImage, 338, 300);
                break;
        }

        g.drawPixmap(Assets.cancelImage, 230, 800);
    }

    private void drawConnected(Graphics g) {
        g.drawPixmap(Assets.connectedImage, 390, 300);
        g.drawPixmap(Assets.notReadyImage, 422, 800);
    }

    private void drawNoPlayersFound(Graphics g) {
        g.drawPixmap(Assets.noPlayersFoundImage, 340, 500);
    }

    private void drawPressedStart(Graphics g) {
        g.drawPixmap(Assets.connectedImage, 390, 300);
        g.drawPixmap(Assets.readyImage, 422, 800);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        Graphics g = game.getGraphics();

        Assets.blackBackgroundImage = g.newPixmap("blackbackground-1080x1920.png", Graphics.PixmapFormat.RGB565);
        Assets.createGameImage = g.newPixmap("creategame-620x185.png", Graphics.PixmapFormat.ARGB4444);
        Assets.joinGameImage = g.newPixmap("joingame-620x185.png", Graphics.PixmapFormat.ARGB4444);
        Assets.cancelImage = g.newPixmap("cancel-620x185.png", Graphics.PixmapFormat.ARGB4444);
        Assets.creatingGameImage = g.newPixmap("creatinggame-448x36.png", Graphics.PixmapFormat.ARGB4444);
        Assets.joiningGameImage = g.newPixmap("joininggame-404x45.png", Graphics.PixmapFormat.ARGB4444);
        Assets.connectedImage = g.newPixmap("connected-300x36.png", Graphics.PixmapFormat.ARGB4444);
        Assets.readyImage = g.newPixmap("ready-236x40.png", Graphics.PixmapFormat.ARGB4444);
        Assets.notReadyImage = g.newPixmap("notready-236x40.png", Graphics.PixmapFormat.ARGB4444);
        Assets.noPlayersFoundImage = g.newPixmap("noplayersfound-400x285.png", Graphics.PixmapFormat.ARGB4444);
    }

    @Override
    public void dispose() {
        Assets.blackBackgroundImage.dispose();
        Assets.createGameImage.dispose();
        Assets.joinGameImage.dispose();
        Assets.cancelImage.dispose();
        Assets.creatingGameImage.dispose();
        Assets.joiningGameImage.dispose();
        Assets.connectedImage.dispose();
        Assets.readyImage.dispose();
        Assets.notReadyImage.dispose();
        Assets.noPlayersFoundImage.dispose();
    }
}
