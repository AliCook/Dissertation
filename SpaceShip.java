import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SpaceShip extends Sprite {
    protected float speedX;
    protected float speedY;
    protected static float maxSpeed;
    protected static float acceleration;
    protected boolean flame;
    protected float flameAngle;
    protected DecimalFormat concatenate = new DecimalFormat("#.####");
    protected int keyX;
    protected int keyY;
    protected ArrayList<Missile> missiles = new ArrayList<Missile>();
    protected int fireType;
    protected float missileSpeed;
    protected int points;
    private double powerupTimer;

    // Player spaceship contructor
    public SpaceShip(float x, float y, float w, float h) {
        super(x, y, w, h);
        fireType = 0;
        maxSpeed = 0.05f;
        acceleration = 0.0005f;
        flame = false;
        missileSpeed = 0.1f;
        points = 0;
    }

    // Override the players speed if it exceeds the bounds of the game window
    @Override
    public float getSpeedX() {
        if ((this.getX() - 1) <= -5.2f) {
            speedX = 0.005f;
        } else if ((this.getX() + 1) >= 5.2f) {
            speedX = -0.005f;
        }
        return speedX;
    }

    @Override
    public float getSpeedY() {
        if ((this.getY() - 1) <= -3.1f) {
            speedY = (float) 0.005f;
        } else if ((this.getY() + 1) >= 3.2f) {
            speedY = -0.005f;
        }
        return speedY;
    }

    // Get the number of points the player has
    public int getPoints() {
        return points;
    }

    // Get the array of current player missiles
    public ArrayList<Missile> getMissiles() {
        return missiles;
    }

    // Check which firepower mode is currently active
    public int getFireType() {
        return fireType;
    }

    // Variables for storing which direction the player is attempting to move
    public int getKeyX() {
        return keyX;
    }

    public int getKeyY() {
        return keyY;
    }

    // Calculate the angle which the thruster trail is pointing
    public float getFlameAngle() {
        if (this.getSpeedX()>= 0) {
           flameAngle = (float) (Math.toDegrees(Math.atan(this.getSpeedY()/this.getSpeedX()))-90);
        } else {
           flameAngle = (float) (Math.toDegrees(Math.atan(this.getSpeedY()/this.getSpeedX()))+90);
        }
        return flameAngle;
    }

    // Add to the players points
    public void setPoints(int p) {
        points += p;
    }

    // Set the directions the player is attempting to travel
    public void setKeyX(int key) {
        keyX = key;
    }

    public void setKeyY(int key) {
        keyY = key;
    }

    // Change the firepower mode and start a timer
    public void setFireType(int f) {
        fireType = f;
        if (fireType == 0) {
            missileSpeed = 0.1f;
        } else if (fireType == 1) {
            missileSpeed = 0.05f;
        } else if (fireType == 2) {
            missileSpeed = 0.05f;
        }
        powerupTimer = getSeconds();
    }

    // Update the player's speed based of player input
    public boolean setSpeed() {
        speedX = Float.parseFloat(concatenate.format(speedX));
        speedY = Float.parseFloat(concatenate.format(speedY));

        if (Math.abs(speedX) < maxSpeed) {
            if (keyX*speedX < 0) {
                speedX += keyX*2f*acceleration;
            } else {
                speedX += keyX*acceleration;
            }
        }
        if ((keyX == 0)||(Math.abs(speedX) > maxSpeed)) {
            if (speedX > 0) {
                speedX -= acceleration;
            } else if (speedX < 0) {
                speedX += acceleration;
            } else {
                speedX = 0;
            }
        }

        if (Math.abs(speedY) < maxSpeed) {
            if (keyY*speedY < 0) {
                speedY += keyY*2f*acceleration;
            } else {
                speedY += keyY*acceleration;
            }
        }
        if ((keyY == 0)||(Math.abs(speedY) > maxSpeed)) {
            if (speedY > 0) {
                speedY -= acceleration;
            } else if (speedY < 0) {
                speedY += acceleration;
            } else {
                speedY = 0;
            }
        }

        if ((keyX==0)&&(keyY==0)) {
            flame = false;
        } else {
            flame = true;
        }

        return flame;
    }

    // Fire missiles depending on the firetype active
    public void fire() {
        if (fireType == 0) {
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
        } else if (fireType == 1) {
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
        } else if (fireType == 2) {
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
            missiles.get(missiles.size()-1).setSpeedX(0.025f);
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
            missiles.get(missiles.size()-1).setSpeedX(-0.025f);
            missiles.add(new Missile(this.getX(), (this.getY() + 0.3f), 0.025f, 0.1f));
            missiles.get(missiles.size()-1).setSpeedX(0.0f);
        }
    }

    // Update the missile's coordinates
    public void updateMissiles() {
        if ((powerupTimer + 20) < getSeconds()) {
            fireType = 0;
        }
        for (int i = 0; i < this.missiles.size(); i++) {
            if (missiles.get(i).getY() > 5.0f) {
                missiles.remove(i);
            } else {
                missiles.get(i).setY(missiles.get(i).getY() + missileSpeed);
            }
        } 
    }

    // Get current time in seconds
    private double getSeconds() {
        return System.currentTimeMillis()/1000.0;
    }
}