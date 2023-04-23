package com.bbk.catchme.ui;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.math.Circle;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.bbk.catchme.app.CatchMeGame;
import com.bbk.catchme.model.World;
import com.bbk.catchme.tools.Assets;
import com.bbk.catchme.tools.GameGenerator;

import java.util.List;

public class GameScreen extends Screen {
    private CatchMeGame myGame;

    private int touchX = 0;
    private int touchY = 0;

    Input input;
    private Circle button;
    private int initialX = 0;
    private int initialY = 0;
    private int currentX = 0;
    private int currentY = 0;
    private boolean isTouchDown = false;
    final long UPDATE_DELAY = 10_000_000;
    long lastMoved = System.nanoTime();

    public enum GameState {
        Running,
        Paused,
        PressedResume,
        GameOver,
        ExitGame,
        Disconnected
    }

    public GameState state;

    public World world;

    public GameScreen(Game game) {
        super(game);

        myGame = (CatchMeGame) game;

        input = myGame.getInput();
        button = new Circle(540, 1475, 175);

        startANewGame();
    }

    private void startANewGame() {
        if (GameGenerator.multiplayer)
            setupANewMultiplayerGame();
        world = new World(myGame);
        resume();

        synchronized (this) {
            myGame.otherPlayerPressedResume = false;
            myGame.otherPlayerWon = false;
        }
        state = GameState.Running;
    }

    private void setupANewMultiplayerGame() {
        if (myGame.isHost) {
            int gameSetup = GameGenerator.generateGame();
            // Send the game data to the other player
            String gameSetupMsg = "GameSetup ";

            gameSetupMsg += gameSetup;

            myGame.sendMessage(gameSetupMsg);
        } else {
            long startTime = System.nanoTime();
            while (!myGame.gameDataReceived) {
                if (System.nanoTime() - startTime >= 10e9) {
                    game.setScreen(new PrepareMultiplayerScreen(game));
                    return;
                }
            }

            myGame.gameDataReceived = false;
        }
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

        if (state == GameState.Running)
            updateRunning(touchEvents, keyEvents);
        else if (state == GameState.Paused)
            updatePaused(touchEvents);
        else if (state == GameState.PressedResume)
            updatePressedResume(touchEvents, keyEvents);
        else if (state == GameState.GameOver)
            updateGameOver(touchEvents, keyEvents);
        else if (state == GameState.ExitGame)
            updateExitGame();
        else if (state == GameState.Disconnected)
            updateDisconnected(touchEvents);

        // TODO (Done): Send a message every one second to inform the peer that the connection is still alive
        if (myGame.connected) {
            double timeout;
            if (myGame.otherPlayerStopped) {
                timeout = myGame.CONNECTION_TIMEOUT_PERIOD_PAUSED;
            } else if (state == GameState.GameOver) {
                timeout = myGame.CONNECTION_TIMEOUT_PERIOD_GAMEOVER;
            } else {
                timeout = myGame.CONNECTION_TIMEOUT_PERIOD;
            }

            if (System.nanoTime() - myGame.lastTimeGotResponseFromOtherPlayer >= timeout) {
                if (GameGenerator.multiplayer) {
                    state = GameState.Disconnected;
                }

                myGame.connected = false;
                myGame.isHost = false;

                myGame.otherPlayerPressedResume = false;
                myGame.otherPlayerWon = false;
                myGame.otherPlayerPressedStart = false;

                myGame.restartNetworkManager();
            } else if (System.nanoTime() - myGame.lastTimeISentToOtherPlayer >= 1e9) {
                myGame.sendMessage("Iam Alive");
                myGame.lastTimeISentToOtherPlayer = System.nanoTime();
            }
        }
    }

    private void updateRunning(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = GameState.Paused;
                synchronized (this) {
                    if (GameGenerator.multiplayer) {
                        myGame.sendMessage("Game Paused");
                    } else {
                        myGame.finish();
                    }
                }

                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (OverlapTester.pointInCircle(button, event.x, event.y)) {
                        initialX = currentX = event.x;
                        initialY = currentY = event.y;
                        isTouchDown = true;
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {
                }

                if (event.type == TouchEvent.TOUCH_UP) {
                    isTouchDown = false;
                }
            }
        }

        double xComponent = 0;
        double yComponent = 0;

        if (isTouchDown) {
            currentX = input.getTouchX(0);
            currentY = input.getTouchY(0);

            double deltaX = currentX - initialX;
            double deltaY = currentY - initialY;
            double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (delta >= 50) {
                double angle;

                if (deltaX == 0) {
                    angle = Math.PI / 2;
                } else {
                    angle = Math.atan(deltaY / deltaX);
                }

                xComponent = Math.abs(Math.cos(angle));
                yComponent = Math.abs(Math.sin(angle));

                if (currentX < initialX)
                    xComponent *= -1;
                if (currentY < initialY)
                    yComponent *= -1;

                if (System.nanoTime() - lastMoved >= UPDATE_DELAY) {
                    world.updatePlayerPosition(xComponent, yComponent);
                    lastMoved = System.nanoTime();
                }
            }
        }

        world.update();

        if (world.gameOver) {
            state = GameState.GameOver;
        }
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
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

    private void updatePressedResume(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new PrepareMultiplayerScreen(game));
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

        if (myGame.otherPlayerPressedResume) {
            myGame.otherPlayerPressedResume = false;
            state = GameState.Running;
        }
    }

    private void updateGameOver(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                if (GameGenerator.multiplayer) {
                    game.setScreen(new PrepareMultiplayerScreen(game));
                } else {
                    myGame.finish();
                }
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (GameGenerator.multiplayer) {
                        game.setScreen(new PrepareMultiplayerScreen(game));
                    } else {
                        myGame.finish();
                    }
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {
                }

                if (event.type == TouchEvent.TOUCH_UP) {
                }
            }
        }
    }

    private void updateExitGame() {
        myGame.otherPlayerPressedResume = false;

        game.setScreen(new PrepareMultiplayerScreen(game));
    }

    private void updateDisconnected(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.pointer == 0) {
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    if (event.x >= 760 + 134 && event.x <= 760 + 134 + 128) {
                        if (event.y >= 400 + 211 && event.y <= 400 + 211 + 60) {

                            game.setScreen(new PrepareMultiplayerScreen(game));

                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.gameBackgroundImage, 0, 0);

        if (state == GameState.Running)
            drawRunningUI(g);
        else if (state == GameState.Paused)
            drawPausedUI(g);
        else if (state == GameState.PressedResume)
            drawPressedResumeUI(g);
        else if (state == GameState.GameOver)
            drawGameOverUI(g);
        else if (state == GameState.Disconnected)
            drawDisconnectedUI(g);
    }

    private void drawRunningUI(Graphics g) {
        if (isTouchDown)
            g.drawPixmap(Assets.blueButtonImage, 365, 1300);
        else
            g.drawPixmap(Assets.greenButtonImage, 365, 1300);

        drawInPlayArea(g,
                Assets.bluePlayerImage,
                (int) (world.myPlayer.getPositionCenter().x - Assets.bluePlayerImage.getWidth() / 2),
                (int) (world.myPlayer.getPositionCenter().y - Assets.bluePlayerImage.getHeight() / 2));
    }

    private void drawPausedUI(Graphics g) {
        drawRunningUI(g);
    }

    private void drawPressedResumeUI(Graphics g) {
        drawRunningUI(g);
    }

    private void drawGameOverUI(Graphics g) {
        drawRunningUI(g);
    }

    private void drawDisconnectedUI(Graphics g) {
        drawRunningUI(g);
    }

    private void drawInPlayArea(Graphics g, Pixmap image, int x, int y) {

        g.drawPixmap(image, x + world.playArea.left, y + world.playArea.top);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        Graphics g = game.getGraphics();

        Assets.gameBackgroundImage = g.newPixmap("gamebackground-1080x1920.png", Graphics.PixmapFormat.RGB565);
        Assets.bluePlayerImage = g.newPixmap("playerblue-25x25.png", Graphics.PixmapFormat.ARGB4444);
        Assets.greenPlayerImage = g.newPixmap("playergreen-25x25.png", Graphics.PixmapFormat.ARGB4444);
        Assets.blueButtonImage = g.newPixmap("buttonblue-350x350.png", Graphics.PixmapFormat.ARGB4444);
        Assets.greenButtonImage = g.newPixmap("buttongreen-350x350.png", Graphics.PixmapFormat.ARGB4444);
    }

    @Override
    public void dispose() {
        Assets.gameBackgroundImage.dispose();
        Assets.bluePlayerImage.dispose();
        Assets.greenPlayerImage.dispose();
        Assets.blueButtonImage.dispose();
        Assets.greenButtonImage.dispose();
    }
}
