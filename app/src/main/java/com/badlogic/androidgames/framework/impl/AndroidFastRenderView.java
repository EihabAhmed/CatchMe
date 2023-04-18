package com.badlogic.androidgames.framework.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AndroidFastRenderView extends SurfaceView implements Runnable {
    AndroidGame game;
    Bitmap frameBuffer;
    Thread renderThread = null;
    SurfaceHolder holder;
    /*Rect viewport;
    int scaledScreenWidth;
    int scaledScreenHeight;*/
    volatile boolean running = false;

    public AndroidFastRenderView(AndroidGame game, Bitmap frameBuffer) {
        super(game);
        this.game = game;
        this.frameBuffer = frameBuffer;
        this.holder = getHolder();
        //viewport = new Rect();
        //setViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
    }

    /*public void setViewport(Rect rect) {
        viewport.left = rect.left;
        viewport.top = rect.top;
        viewport.right = rect.right;
        viewport.bottom = rect.bottom;
        calculateBlackArea();
    }

    public void setViewport(int left, int top, int right, int bottom) {
        viewport.left = left;
        viewport.top = top;
        viewport.right = right;
        viewport.bottom = bottom;
        calculateBlackArea();
    }

    public Rect getViewport() {
        return viewport;
    }

    public void calculateBlackArea() {
        Point physicalSize = new Point();
        game.getWindowManager().getDefaultDisplay().getRealSize(physicalSize);

        int physicalScreenWidth = physicalSize.x;
        int physicalScreenHeight = physicalSize.y;

        float scaleX = (float)viewport.width() / physicalScreenWidth;
        float scaleY = (float)viewport.height() / physicalScreenHeight;
        game.scale = Math.max(scaleX, scaleY);

        if (scaleX > scaleY) {
            scaledScreenWidth = viewport.width();
            scaledScreenHeight = (int) (physicalScreenHeight * game.scale);
        } else {
            scaledScreenWidth = (int) (physicalScreenWidth * game.scale);
            scaledScreenHeight = viewport.height();
        }

        game.blackWidth = (scaledScreenWidth - viewport.width()) / 2;
        game.blackHeight = (scaledScreenHeight - viewport.height()) / 2;
    }*/

    public void resume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
    }

    public void run() {
        Rect dstRect = new Rect();
        long startTime = System.nanoTime();
        while (running) {
            if (!holder.getSurface().isValid())
                continue;

            float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
            startTime = System.nanoTime();

            game.getCurrentScreen().update(deltaTime);
            game.getCurrentScreen().present(deltaTime);

            Canvas canvas = holder.lockCanvas();
            canvas.getClipBounds(dstRect);

            Bitmap drawBuffer = Bitmap.createBitmap(game.scaledScreenWidth, game.scaledScreenHeight, Bitmap.Config.RGB_565);
            Bitmap viewportBuffer = Bitmap.createBitmap(frameBuffer, game.viewport.left, game.viewport.top, game.viewport.width(), game.viewport.height());
            Canvas tempCanvas = new Canvas(drawBuffer);
            tempCanvas.drawBitmap(viewportBuffer, game.blackWidth, game.blackHeight, null);

            canvas.drawBitmap(drawBuffer, null, dstRect, null);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        running = false;
        while (true) {
            try {
                renderThread.join();
                break;
            } catch (InterruptedException e) {
                // retry
            }
        }
    }
}
