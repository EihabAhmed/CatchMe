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

    PrepareMultiplayerScreen(Game game) {
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
                    if (event.x >= 283 && event.x <= 283 + 86 && event.y >= 395 && event.y <= 395 + 155) {
                        connectingDotPosition = 5;
                        connectingDotX = 255;
                        connectingDotIncreasing = false;
                        firstTimeConnecting = true;
                        startConnectingTime = startConnectingAnimationTime = System.nanoTime();
                        firstTimeStartCreatingServer = true;
                        state = ScreenState.CreatingServer;
                        //myGame.createServer();

                        return;
                    } else if (event.x >= 75 && event.x <= 75 + 86 && event.y >= 395 && event.y <= 395 + 155) {
                        connectingDotPosition = 1;
                        connectingDotX = 167;
                        connectingDotIncreasing = true;
                        firstTimeConnecting = true;
                        startConnectingTime = startConnectingAnimationTime = System.nanoTime();
                        firstTimeStartFindingGame = true;
                        state = ScreenState.FindingGame;

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
    }

    private void updateConnected(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new ChooseGameScreen(game));
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.isTouchInside(event)) {
                            startSlider.touched = true;
                            touchX = event.x;
                        }
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.touched) {
                            int distance = event.x - touchX;
                            distance = Math.min(144, distance);
                            distance = Math.max(0, distance);
                            startSlider.x = startSlider.xDefault + distance;
                        }
                    }
                }

                if (event.type == TouchEvent.TOUCH_UP) {
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.touched) {
                            startSlider.touched = false;
                            int distance = event.x - touchX;
                            distance = Math.min(144 , distance);
                            distance = Math.max(0, distance);
                            if (distance == 144) {
                                startSlider.xDefault = startSlider.x;
                                state = ScreenState.PressedStart;
                                //Send a message to the other player to notify him that this player pressed start
                                myGame.sendMessage("Pressed Start");
                                return;
                            } else {
                                startSlider.x = startSlider.xDefault;
                            }
                        }
                    }
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
                startSlider.xDefault = startSlider.x = startSlider.xDefault - 144;
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
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.isTouchInside(event)) {
                            startSlider.touched = true;
                            touchX = event.x;
                        }
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.touched) {
                            int distance = event.x - touchX;
                            distance = Math.min(0, distance);
                            distance = Math.max(-144, distance);
                            startSlider.x = startSlider.xDefault + distance;
                        }
                    }
                }

                if (event.type == TouchEvent.TOUCH_UP) {
                    if (myGame.startGameButtonEnabled) {
                        if (startSlider.touched) {
                            startSlider.touched = false;
                            int distance = event.x - touchX;
                            distance = Math.min(0 , distance);
                            distance = Math.max(-144, distance);
                            if (distance == -144) {
                                startSlider.xDefault = startSlider.x;

                                state = ScreenState.Connected;
                                //Send a message to the other player to notify him that this player is not ready to start
                                myGame.sendMessage("Cancelled Start");
                                return;
                            } else {
                                startSlider.x = startSlider.xDefault;
                            }
                        }
                    }
                }
            }
        }

        if (myGame.otherPlayerPressedStart) {
            myGame.otherPlayerPressedStart = false;
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
        //g.drawPixmap(Assets.createMultiplayerGameButtonImage, 51, 150);
        //g.drawPixmap(Assets.joinMultiplayerGameButtonImage, 51, 300);
        //g.drawPixmap(Assets.connectImage, 51, 450);
        g.drawPixmap(Assets.startRaceTextDisabledImage, 193, 619);
        g.drawPixmap(Assets.startRaceSliderDisabledImage, 122, 603);

        g.drawPixmap(Assets.createMultiplayerGameButtonImage, 283, 395);
        g.drawPixmap(Assets.joinMultiplayerGameButtonImage, 75, 395);
    }

    private void drawConnectingAsServer(Graphics g) {
        g.drawPixmap(Assets.startRaceTextDisabledImage, 193, 619);
        g.drawPixmap(Assets.startRaceSliderDisabledImage, 122, 603);

        boolean animatingMobileVibration = false;
        if (firstTimeConnecting) {
            firstTimeConnecting = false;
            startMobileVibrationTime = System.nanoTime();
            mobileVibrationAnimationCounter = 0;
        } else {
            if (System.nanoTime() - startMobileVibrationTime >= 1e9) {
                animatingMobileVibration = true;
                int animationShiftStep = 3;
                if (firstTimeMobileVibrationAnimation) {
                    firstTimeMobileVibrationAnimation = false;
                    startMobileVibrationAnimationTime = System.nanoTime();

                    animationShiftDistance = animationShiftStep;
                    animationShiftDistanceIncreasing = true;

                    mobileVibrationAnimationCounter = 1;
                } else {
                    if (System.nanoTime() - startMobileVibrationAnimationTime >= 0.01e9) {
                        startMobileVibrationAnimationTime = System.nanoTime();
                        mobileVibrationAnimationCounter++;
                        if (mobileVibrationAnimationCounter <= 21) {
                            if (animationShiftDistanceIncreasing) {
                                if (animationShiftDistance < animationShiftStep * 2) {
                                    animationShiftDistance += animationShiftStep;
                                } else {
                                    animationShiftDistanceIncreasing = false;
                                    animationShiftDistance -= animationShiftStep;
                                }
                            } else {
                                if (animationShiftDistance > -animationShiftStep * 2) {
                                    animationShiftDistance -= animationShiftStep;
                                } else {
                                    animationShiftDistanceIncreasing = true;
                                    animationShiftDistance += animationShiftStep;
                                }
                            }
                        } else {
                            startMobileVibrationTime = System.nanoTime();
                            firstTimeMobileVibrationAnimation = true;
                            animationShiftDistance = 0;
                            animatingMobileVibration = false;
                        }
                    }
                }
            }
        }

        if (animatingMobileVibration) {
            g.drawPixmap(Assets.createMultiplayerGameButtonActiveImage, 283 - 25 + animationShiftDistance, 395 - 25 - animationShiftDistance);
        } else {
            g.drawPixmap(Assets.createMultiplayerGameButtonConnectedImage, 283 - 12, 395 - 12);
        }

        g.drawPixmap(Assets.joinMultiplayerGameButtonImage, 75, 395);

        if (System.nanoTime() - startConnectingAnimationTime >= 0.1e9) {
            startConnectingAnimationTime = System.nanoTime();
            if (connectingDotIncreasing) {
                if (connectingDotPosition < 5) {
                    connectingDotPosition++;
                    connectingDotX += 22;
                } else {
                    connectingDotIncreasing = false;
                    connectingDotPosition--;
                    connectingDotX -= 22;
                }
            } else {
                if (connectingDotPosition > 1) {
                    connectingDotPosition--;
                    connectingDotX -= 22;
                } else {
                    connectingDotIncreasing = true;
                    connectingDotPosition++;
                    connectingDotX += 22;
                }
            }
        }

        g.drawPixmap(Assets.connectionDotOn, connectingDotX, 463);
    }

    private void drawConnectingAsClient(Graphics g) {
        g.drawPixmap(Assets.startRaceTextDisabledImage, 193, 619);
        g.drawPixmap(Assets.startRaceSliderDisabledImage, 122, 603);

        boolean animatingMobileVibration = false;
        if (firstTimeConnecting) {
            firstTimeConnecting = false;
            startMobileVibrationTime = System.nanoTime();
            mobileVibrationAnimationCounter = 0;
        } else {
            if (System.nanoTime() - startMobileVibrationTime >= 1e9) {
                animatingMobileVibration = true;
                int animationShiftStep = 3;
                if (firstTimeMobileVibrationAnimation) {
                    firstTimeMobileVibrationAnimation = false;
                    startMobileVibrationAnimationTime = System.nanoTime();

                    animationShiftDistance = animationShiftStep;
                    animationShiftDistanceIncreasing = true;

                    mobileVibrationAnimationCounter = 1;
                } else {
                    if (System.nanoTime() - startMobileVibrationAnimationTime >= 0.01e9) {
                        startMobileVibrationAnimationTime = System.nanoTime();
                        mobileVibrationAnimationCounter++;
                        if (mobileVibrationAnimationCounter <= 21) {
                            if (animationShiftDistanceIncreasing) {
                                if (animationShiftDistance < animationShiftStep * 2) {
                                    animationShiftDistance += animationShiftStep;
                                } else {
                                    animationShiftDistanceIncreasing = false;
                                    animationShiftDistance -= animationShiftStep;
                                }
                            } else {
                                if (animationShiftDistance > -animationShiftStep * 2) {
                                    animationShiftDistance -= animationShiftStep;
                                } else {
                                    animationShiftDistanceIncreasing = true;
                                    animationShiftDistance += animationShiftStep;
                                }
                            }
                        } else {
                            startMobileVibrationTime = System.nanoTime();
                            firstTimeMobileVibrationAnimation = true;
                            animationShiftDistance = 0;
                            animatingMobileVibration = false;
                        }
                    }
                }
            }
        }

        if (animatingMobileVibration) {
            g.drawPixmap(Assets.joinMultiplayerGameButtonActiveImage, 75 - 25 + animationShiftDistance, 395 - 25 - animationShiftDistance);
        } else {
            g.drawPixmap(Assets.joinMultiplayerGameButtonConnectedImage, 75 - 12, 395 - 12);
        }

        g.drawPixmap(Assets.createMultiplayerGameButtonImage, 283, 395);

        if (System.nanoTime() - startConnectingAnimationTime >= 0.1e9) {
            startConnectingAnimationTime = System.nanoTime();
            if (connectingDotIncreasing) {
                if (connectingDotPosition < 5) {
                    connectingDotPosition++;
                    connectingDotX += 22;
                } else {
                    connectingDotIncreasing = false;
                    connectingDotPosition--;
                    connectingDotX -= 22;
                }
            } else {
                if (connectingDotPosition > 1) {
                    connectingDotPosition--;
                    connectingDotX -= 22;
                } else {
                    connectingDotIncreasing = true;
                    connectingDotPosition++;
                    connectingDotX += 22;
                }
            }
        }

        g.drawPixmap(Assets.connectionDotOn, connectingDotX, 463);
    }

    private void drawConnected(Graphics g) {
        if (myGame.isHost) {
            g.drawPixmap(Assets.createMultiplayerGameButtonConnectedImage, 283 - 12, 395 - 12);

            if (myGame.firstTimeConnected) {
                startSlider.xDefault = startSlider.x = 122;
                myGame.firstTimeConnected = false;
                myGame.startConnectedTime = System.nanoTime();
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 4, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 2) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 3, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 3) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 2, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 4) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 5) {
                g.drawPixmap(Assets.connectionDotOn, 167, 463);
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 6) {
                g.drawPixmap(Assets.joinMultiplayerGameButtonConnectedImage, 75 - 12, 395 - 12);
            } else {
                g.drawPixmap(Assets.joinMultiplayerGameButtonImage, 75, 395);
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 7) {
                g.drawPixmap(Assets.startRaceSliderContainerActiveImage, 109, 593);
                g.drawPixmap(Assets.startRaceTextActiveImage, 193, 619);
                startSlider.draw();
                myGame.startGameButtonEnabled = true;
            } else {
                g.drawPixmap(Assets.startRaceTextDisabledImage, 193, 619);
                g.drawPixmap(Assets.startRaceSliderDisabledImage, 122, 603);
            }
        } else {
            g.drawPixmap(Assets.joinMultiplayerGameButtonConnectedImage, 75 - 12, 395 - 12);

            if (myGame.firstTimeConnected) {
                startSlider.xDefault = startSlider.x = 122;
                myGame.firstTimeConnected = false;
                myGame.startConnectedTime = System.nanoTime();
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9) {
                g.drawPixmap(Assets.connectionDotOn, 167, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 2) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 3) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 2, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 4) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 3, 463);
            }
            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 5) {
                g.drawPixmap(Assets.connectionDotOn, 167 + 22 * 4, 463);
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 6) {
                g.drawPixmap(Assets.createMultiplayerGameButtonConnectedImage, 283 - 12, 395 - 12);
            } else {
                g.drawPixmap(Assets.createMultiplayerGameButtonImage, 283, 395);
            }

            if (System.nanoTime() - myGame.startConnectedTime >= 0.1e9 * 7) {
                g.drawPixmap(Assets.startRaceSliderContainerActiveImage, 109, 593);
                g.drawPixmap(Assets.startRaceTextActiveImage, 193, 619);
                startSlider.draw();
                myGame.startGameButtonEnabled = true;
            } else {
                g.drawPixmap(Assets.startRaceTextDisabledImage, 193, 619);
                g.drawPixmap(Assets.startRaceSliderDisabledImage, 122, 603);
            }
        }
    }

    private void drawNoPlayersFound(Graphics g) {
        g.drawPixmap(Assets.noPlayersFoundImage, 26, 280);
    }

    private void drawPressedStart(Graphics g) {
        g.drawPixmap(Assets.createMultiplayerGameButtonConnectedImage, 283 - 12, 395 - 12);
        g.drawPixmap(Assets.joinMultiplayerGameButtonConnectedImage, 75 - 12, 395 - 12);

        for (int i = 0; i <= 4; i++) {
            g.drawPixmap(Assets.connectionDotOn, 167 + 22 * i, 463);
        }

        g.drawPixmap(Assets.startRaceSliderContainerActiveImage, 109, 593);
        g.drawPixmap(Assets.waitingOtherPlayerTextImage, 125, 621);
        startSlider.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        Graphics g = game.getGraphics();

        Assets.blackBackgroundImage = g.newPixmap("blackbackground-1080x1920.png", Graphics.PixmapFormat.RGB565);
    }

    @Override
    public void dispose() {
        Assets.blackBackgroundImage.dispose();
    }
}
