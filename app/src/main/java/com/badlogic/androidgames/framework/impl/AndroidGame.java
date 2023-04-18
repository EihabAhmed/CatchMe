package com.badlogic.androidgames.framework.impl;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.androidgames.framework.Audio;
import com.badlogic.androidgames.framework.FileIO;
import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input;
import com.badlogic.androidgames.framework.Screen;

public abstract class AndroidGame extends Activity implements Game {
    protected AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    WakeLock wakeLock;

    public int worldWidth;
    public int worldHeight;
    public int blackWidth;
    public int blackHeight;
    //public int scaledScreenWidth;
    //public int scaledScreenHeight;
    public float scale;
    Rect viewport;
    int scaledScreenWidth;
    int scaledScreenHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Window window = getWindow();
            View view = window.getDecorView();
            WindowInsets windowInsets = view.getRootWindowInsets();
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            if (displayCutout != null) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        //sourceWidth = 452;
        //sourceHeight = 800;

        /*Point physicalSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(physicalSize);
        int physicalScreenWidth = physicalSize.x;
        int physicalScreenHeight = physicalSize.y;
        //int physicalScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        //int physicalScreenHeight = getWindowManager().getDefaultDisplay().getHeight();

        float scaleX = (float) sourceWidth / physicalScreenWidth;
        float scaleY = (float) sourceHeight / physicalScreenHeight;
        float scale = Math.max(scaleX, scaleY);

        if (scaleX > scaleY) {
            scaledScreenWidth = sourceWidth;
            scaledScreenHeight = (int) (physicalScreenHeight * scale);
        } else {
            scaledScreenWidth = (int) (physicalScreenWidth * scale);
            scaledScreenHeight = sourceHeight;
        }*/

        //int frameBufferWidth = isLandscape ? scaledScreenHeight : scaledScreenWidth;
        //int frameBufferHeight = isLandscape ? scaledScreenWidth : scaledScreenHeight;
        viewport = new Rect();
        setViewport(0, 0, worldWidth, worldHeight);
        Bitmap frameBuffer = Bitmap.createBitmap(scaledScreenWidth, scaledScreenHeight, Bitmap.Config.RGB_565);

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, scale, scale);
        screen = getStartScreen();
        setContentView(renderView);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
    }

    public void calculateBlackArea() {
        Point physicalSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(physicalSize);

        int physicalScreenWidth = physicalSize.x;
        int physicalScreenHeight = physicalSize.y;

        float scaleX = (float)viewport.width() / physicalScreenWidth;
        float scaleY = (float)viewport.height() / physicalScreenHeight;
        scale = Math.max(scaleX, scaleY);

        if (scaleX > scaleY) {
            scaledScreenWidth = viewport.width();
            scaledScreenHeight = (int) (physicalScreenHeight * scale);
        } else {
            scaledScreenWidth = (int) (physicalScreenWidth * scale);
            scaledScreenHeight = viewport.height();
        }

        blackWidth = (scaledScreenWidth - viewport.width()) / 2;
        blackHeight = (scaledScreenHeight - viewport.height()) / 2;
    }

    public void setViewport(Rect rect) {
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

    @Override
    public void onResume() {
        super.onResume();

        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public FileIO getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }

    public Screen getCurrentScreen() {
        return screen;
    }
}
