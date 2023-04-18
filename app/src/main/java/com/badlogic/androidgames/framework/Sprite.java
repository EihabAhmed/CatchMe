package com.badlogic.androidgames.framework;

import com.badlogic.androidgames.framework.math.Point;
import com.badlogic.androidgames.framework.math.Rect;

import java.util.Random;

public class Sprite {
    public Pixmap image;
    public Rect position;
    public Rect collision;
    public Point velocity;
    public int zOrder;
    public Rect bounds;
    public int boundsAction = BoundsAction.BA_STOP;
    public boolean hidden;
    public Game game;
    public Graphics g;
    public int numFrames, curFrame;
    public int frameDelay, frameTrigger;
    public boolean dying;
    public boolean oneCycle = false;

    public Sprite(Game game, Pixmap image) {
        // Initialize the member variables
        this.game = game;
        g = game.getGraphics();
        this.image = image;
        numFrames = 1;
        curFrame = frameDelay = frameTrigger = 0;
        //setRect(position, 0, 0, image.getWidth(), image.getHeight());
        position = new Rect(0, 0, image.getWidth() / numFrames, image.getHeight());
        calcCollisionRect();
        velocity = new Point(0, 0);
        zOrder = 0;
        bounds = new Rect(0, 0, g.getWidth(), g.getHeight());
        boundsAction = BoundsAction.BA_STOP;
        hidden = false;
        dying = false;
        oneCycle = false;
    }

    public Sprite(Game game, Pixmap image, Rect bounds, int boundsAction) {
        // Calculate a random position
        Random random = new Random();
        int xPos = random.nextInt(32767) % (bounds.right - bounds.left);
        int yPos = random.nextInt(32767) % (bounds.bottom - bounds.top);

        // Initialize the member variables
        this.game = game;
        g = game.getGraphics();
        this.image = image;
        numFrames = 1;
        curFrame = frameDelay = frameTrigger = 0;
        position = new Rect(xPos, yPos, xPos + image.getWidth() / numFrames, yPos + image.getHeight());
        calcCollisionRect();
        velocity = new Point(0, 0);
        zOrder = 0;
        this.bounds = bounds;
        //copyRect(this.bounds, bounds);
        this.boundsAction = boundsAction;
        hidden = false;
        dying = false;
        oneCycle = false;
    }

    public Sprite(Game game, Pixmap image, Point position, Point velocity, int zOrder, Rect bounds, int boundsAction) {
        // Initialize the member variables
        this.game = game;
        g = game.getGraphics();
        this.image = image;
        numFrames = 1;
        curFrame = frameDelay = frameTrigger = 0;
        this.position = new Rect(position.x, position.y, position.x + image.getWidth() / numFrames, position.y + image.getHeight());
        calcCollisionRect();
        this.velocity = velocity;
        this.zOrder = zOrder;
        this.bounds = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);
        //copyRect(this.bounds, bounds);
        this.boundsAction = boundsAction;
        hidden = false;
        dying = false;
        oneCycle = false;
    }

    public int update(int difficulty) {
        // See if the sprite needs to be killed
        if (dying)
            return SpriteAction.SA_KILL;

        // Update the frame
        updateFrame();

        // Update the position
        Point newPosition = new Point(position.left + velocity.x, position.top + velocity.y);
        Point spriteSize = new Point(position.right - position.left, position.bottom - position.top);
        Point boundsSize = new Point(bounds.right - bounds.left, bounds.bottom - bounds.top);

        // Check the bounds
        // Wrap?
        if (boundsAction == BoundsAction.BA_WRAP) {
            if ((newPosition.x + spriteSize.x) < bounds.left)
                newPosition.x = bounds.right;
            else if (newPosition.x > bounds.right)
                newPosition.x = bounds.left - spriteSize.x;
            if ((newPosition.y + spriteSize.y) < bounds.top)
                newPosition.y = bounds.bottom;
            else if (newPosition.y > bounds.bottom)
                newPosition.y = bounds.top - spriteSize.y;
        }
        
        // Bounce?
        else if (boundsAction == BoundsAction.BA_BOUNCE)
        {
            boolean bounce = false;
            Point newVelocity = velocity;
            if (newPosition.x < bounds.left)
            {
                bounce = true;
                newPosition.x = bounds.left;
                newVelocity.x = -newVelocity.x;
            }
            else if ((newPosition.x + spriteSize.x) > bounds.right)
            {
                bounce = true;
                newPosition.x = bounds.right - spriteSize.x;
                newVelocity.x = -newVelocity.x;
            }
            if (newPosition.y < bounds.top)
            {
                bounce = true;
                newPosition.y = bounds.top;
                newVelocity.y = -newVelocity.y;
            }
            else if ((newPosition.y + spriteSize.y) > bounds.bottom)
            {
                bounce = true;
                newPosition.y = bounds.bottom - spriteSize.y;
                newVelocity.y = -newVelocity.y;
            }
            if (bounce)
                setVelocity(newVelocity);
        }

        // Die?
        else if (boundsAction == BoundsAction.BA_DIE)
        {
            if ((newPosition.x + spriteSize.x) < bounds.left ||
                    newPosition.x > bounds.right ||
                    (newPosition.y + spriteSize.y) < bounds.top ||
                    newPosition.y > bounds.bottom)
                return SpriteAction.SA_KILL;
        }
        
        // Stop (default)
        else {
            if (newPosition.x < bounds.left || newPosition.x > (bounds.right - spriteSize.x)) {
                newPosition.x = Math.max(bounds.left, Math.min(newPosition.x, bounds.right - spriteSize.x));
                setVelocity(0, 0);
            }
            if (newPosition.y < bounds.top || newPosition.y > (bounds.bottom - spriteSize.y)) {
                newPosition.y = Math.max(bounds.top, Math.min(newPosition.y, bounds.bottom - spriteSize.y));
                setVelocity(0, 0);
            }
        }
        setPosition(newPosition);

        return SpriteAction.SA_NONE;
    }

    public Sprite addSprite(World world) {
        return null;
    }

    public void draw() {
        // Draw the sprite if it isn't hidden
        if (image != null && !hidden)
        {
            // Draw the appropriate frame, if necessary
            if (numFrames == 1)
                g.drawPixmap(image, position.left, position.top);
            else
                g.drawPixmap(image, position.left, position.top,
                        curFrame * getWidth(), 0, getWidth(), getHeight());
        }
    }

    public boolean isPointInside(int x, int y) {
        return x >= position.left &&
               x <= position.right &&
               y >= position.top &&
               y <= position.bottom;
    }

    public Rect getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        position = Rect.offsetRect(position, x - position.left, y - position.top);
        calcCollisionRect();
    }

    public void setPosition(Point newPosition) {
        position = Rect.offsetRect(position, newPosition.x - position.left, newPosition.y - position.top);
        calcCollisionRect();
    }

    public void setPosition(Rect position) {
        this.position = position;
        //copyRect(this.position, position);
        calcCollisionRect();
    }

    public Rect copyRect(Rect source) {
        Rect target = new Rect(source.left, source.top, source.right, source.bottom);
//        target.left = source.left;
//        target.top = source.top;
//        target.right = source.right;
//        target.bottom = source.bottom;
        return target;
    }

    public void setRect(Rect target, int left, int top, int right, int bottom) {
        target = new Rect(left, top, right, bottom);
        target.left = left;
        target.top = top;
        target.right = right;
        target.bottom = bottom;
    }

    public void offsetPosition(int x, int y) {
        position = Rect.offsetRect(position, x, y);
        calcCollisionRect();
    }

    public Point getVelocity() {
        return velocity;
    }

    public void setVelocity(int x, int y) {
        velocity.x = x;
        velocity.y = y;
    }

    public void setVelocity(Point velocity) {
        this.velocity = velocity;
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
        //copyRect(this.bounds, bounds);
    }
    public void setBoundsAction(int boundsAction) {
        this.boundsAction = boundsAction;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getWidth() {
        return (image.getWidth() / numFrames);
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void kill() {
        dying = true;
    }

    public Pixmap getPixmap() {
        return image;
    }

    public void setFrameDelay(int frameDelay) {
        this.frameDelay = frameDelay;
    }

    public Rect getCollision() {
        return collision;
    }

    public void updateFrame() {
        if ((frameDelay >= 0) && (--frameTrigger <= 0))
        {
            // Reset the frame trigger;
            frameTrigger = frameDelay;

            // Increment the frame
            if (++curFrame >= numFrames)
            {
                // If it's a one-cycle frame animation, kill the sprite
                if (oneCycle)
                    dying = true;
                else
                    curFrame = 0;
            }
        }
    }

    public void calcCollisionRect()
    {
        int xShrink = (position.left - position.right) / 12;
        int yShrink = (position.top - position.bottom) / 12;
        collision = copyRect(position);
        //collision = Rect.inflateRect(collision, xShrink, yShrink);
        collision.left -= xShrink;
        collision.right += xShrink;
        collision.top -= yShrink;
        collision.bottom += yShrink;
    }

    public boolean testCollision(Sprite testSprite)
    {
        Rect test = testSprite.getCollision();
        return collision.left <= test.right &&
                test.left <= collision.right &&
                collision.top <= test.bottom &&
                test.top <= collision.bottom;
    }

    public void setNumFrames(int numFrames, boolean oneCycle)
    {
        // Set the number of frames and the one-cycle setting
        this.numFrames = numFrames;
        this.oneCycle = oneCycle;

        // Recalculate the position
        Rect rect = getPosition();
        rect.right = rect.left + ((rect.right - rect.left) / numFrames);
        setPosition(rect);
    }

    public void setNumFrames(int numFrames)
    {
        // Set the number of frames
        this.numFrames = numFrames;

        // Recalculate the position
        Rect rect = getPosition();
        rect.right = rect.left + ((rect.right - rect.left) / numFrames);
        setPosition(rect);
    }
}