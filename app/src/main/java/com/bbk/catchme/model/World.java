package com.bbk.catchme.model;

import com.bbk.catchme.app.CatchMeGame;

import java.util.ArrayList;
import java.util.Arrays;

public class World {
    private CatchMeGame myGame;

//    public Colors[] targetColors;
//    public FacePiece[] facePieces;
//    public Cover[] covers;
//    public ArrayList<Cover> coversOn;
//
//    public final String HAT = "HAT";
//    public final String RIGHT_COVER = "RIGHT_COVER";
//    public final String LEFT_COVER = "LEFT_COVER";
//    public final String EYES_COVER = "EYES_COVER";
//    public final String MASK = "MASK";

    public boolean gameOver = false;

    public World(CatchMeGame myGame) {
        this.myGame = myGame;

//        facePieces = new FacePiece[18];
//        for (int i = 0; i < facePieces.length; i++) {
//            facePieces[i] = new FacePiece(i);
//        }
//
//        covers = new Cover[5];
//        covers[0] = new Cover(HAT, new int[] {0, 9, 10, 11, 12, 13, 14, 15, 16, 17});
//        covers[1] = new Cover(RIGHT_COVER, new int[] {4, 6, 11, 13, 17});
//        covers[2] = new Cover(LEFT_COVER, new int[] {0, 5, 7, 12, 14});
//        covers[3] = new Cover(EYES_COVER, new int[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
//        covers[4] = new Cover(MASK, new int[] {0, 2, 3, 4, 5, 10, 13, 14, 15, 17});
//
//        coversOn = new ArrayList<>();
//
//        if (!GameGenerator.multiplayer) {
//            newGame();
//        } else {
//            targetColors = GameGenerator.targetColors;
//
//            Arrays.fill(myGame.otherPlayerFace, Colors.ORANGE);
//        }
    }

    public void newGame() {
//        targetColors = GameGenerator.generateGame();
//        gameOver = false;
//        resetGame();
    }

    public void resetGame() {
//        for (FacePiece facePiece : facePieces) {
//            facePiece.reset();
//        }
//        for (Cover cover : covers) {
//            cover.remove();
//        }
//        coversOn.clear();
    }

    public void draw() {

    }

    public void update() {
//        if (GameGenerator.multiplayer) {
//            // Send my current face to the other player
//            String myFaceMsg = "MyFace ";
//            for (FacePiece facePiece : facePieces) {
//                switch (facePiece.color) {
//                    case BLACK:
//                        myFaceMsg += "0 ";
//                        break;
//                    case BLUE:
//                        myFaceMsg += "1 ";
//                        break;
//                    case ORANGE:
//                        myFaceMsg += "2 ";
//                        break;
//                    case WHITE:
//                        myFaceMsg += "3 ";
//                        break;
//                    case YELLOW:
//                        myFaceMsg += "4 ";
//                        break;
//                }
//            }
//            myGame.sendMessage(myFaceMsg);
//        }
//
//        if (gameOver) {
//            if (GameGenerator.multiplayer) {
//                myGame.sendMessage("IWon");
//            }
//        }
    }

    private void checkWinning() {

//        boolean win = true;
//
//        for (int i = 0; i < targetColors.length; i++) {
//            if (targetColors[i] != facePieces[i].color) {
//                win = false;
//                break;
//            }
//        }
//
//        if (win) {
//            gameOver = true;
//        }
    }
}
