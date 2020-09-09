import java.awt.geom.Rectangle2D;

/*
    Sprite superclass
*/

public class Sprite {
    protected float x;
    protected float y;
    protected float speedX;
    protected float speedY;
    protected float width;
    protected float height;
    
    // Sprite constructor
    public Sprite(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
    }

    // Sprites x and y coordinates
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // Get sprites x and y speed
    public float getSpeedX() {
        return speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    // Get sprites width and height
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // Create and return bounding box for a sprite based off its width, height and position
    public Rectangle2D getBounds() {
        return new Rectangle2D.Float((x-width/2), (y-height/2), width, height);
    }

    // Set x and y coordinates
    public float setX(float xPos) {
        x = xPos;
        return x;
    }

    public float setY(float yPos) {
        y = yPos;
        return y;
    }

    // Set x and y speed
    public void setSpeedX(float xSpeed) {
        speedX = xSpeed;
    }

    public void setSpeedY(float ySpeed) {
        speedY = ySpeed;
    }

    
}