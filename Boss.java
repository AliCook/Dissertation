import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class Boss extends Sprite {
	ArrayList<Missile> missiles = new ArrayList<Missile>();
	ArrayList<Shield> shields = new ArrayList<Shield>();
	protected float speedX;
    protected float speedY;
    protected float turretOffset;
    private int hp;
    private float missileSpeed;
    protected static float acceleration;
    private DecimalFormat concatenate = new DecimalFormat("#.####");
    private double startTime;

    // Boss constructor class
    public Boss(float x, float y, float w, float h) {
		super(x, y, w, h);
		acceleration = 0.0001f;
        hp = 100;
        missileSpeed = 0.005f;
        turretOffset = 0;
        shields.add(new Shield(x, (y-0.5f), 0.8f, 0.05f));
        shields.add(new Shield((x-2f), (y-1.0f), 1.2f, 0.05f));
        shields.add(new Shield((x+2f), (y-1.0f), 1.2f, 0.05f));
        startTime = getSeconds();
	}

    // Get current seconds
    private double getSeconds() {
		return System.currentTimeMillis()/1000.0;
	}

    // Get current health points
    public int getHealth() {
        return hp;
    }

    // Return array of shield objects
    public ArrayList<Shield> getShields() {
    	return shields;
    }

    // Get current x and y speeds of boss and move sprite on screen
    @Override
    public float getSpeedX() {
        if ((this.getX() - 1) <= -3.95) {
            speedX = 0.005f;
        } else if ((this.getX() + 1) >= 3.95) {
            speedX = -0.005f;
        }
        return speedX;
    }

    @Override
    public float getSpeedY() {
        if ((this.getY() - 1) <= -3) {
            speedY = (float) 0.005f;
        } else if ((this.getY() + 1) >= 2.7) {
            speedY = -0.0065f;
        }
        return speedY;
    }

    // Return arraylist of the boss' missiles
    public ArrayList<Missile> getMissiles() {
        return missiles;
    }

    // Return position of the turret
    public float getTurret() {
    	turretOffset = (((float) Math.sin((getSeconds()-startTime)*0.5f))*1.5f);
    	return turretOffset;
    }

    // Set x and y speed
    public void setSpeedX(float xSpeed) {
        speedX = xSpeed;
    }

    public void setSpeedY(float ySpeed) {
        speedY = ySpeed;
    }

    // Set turret position
    public void setTurret(float x) {
    	turretOffset = x;
    }

    // Apply deceleration to the ship if necessary
    public void setSpeed() {
        speedX = Float.parseFloat(concatenate.format(speedX));
        speedY = Float.parseFloat(concatenate.format(speedY));

        if (speedX > 0) {
            speedX -= acceleration;
        } else if (speedX < 0) {
            speedX += acceleration;
        } else {
            speedX = 0;
        }

        if (speedY > 0) {
            speedY -= acceleration;
        } else if (speedY < 0) {
            speedY += acceleration;
        } else {
            speedY = 0;
        }

        for (int i = 0; i < shields.size(); i++) {
    		shields.get(i).setSpeedY(speedY);
            shields.get(i).setY(shields.get(i).getY()+shields.get(i).getSpeedY());
    	}
    }

    // Decrement health by 1
    public void setHealth() {
        --hp;
    }

    // Fire 3 missiles, 1 from the turret and 2 aimed at the player
    public void fire(float playerX, float playerY) {
        missiles.add(new Missile((this.getX()+this.getTurret()), this.getY(), 0.1f, 0.1f));
        missiles.get(missiles.size()-1).setSpeedY(-(missileSpeed*3.5f));
        missiles.add(new Missile((this.getX()-2.25f), (this.getY()-0.5f), 0.1f, 0.1f));
        missiles.get(missiles.size()-1).setSpeedX((playerX-missiles.get(missiles.size()-1).getX())*missileSpeed);
        missiles.get(missiles.size()-1).setSpeedY((playerY-missiles.get(missiles.size()-1).getY())*missileSpeed);
        missiles.add(new Missile((this.getX()+2.25f), (this.getY()-0.5f), 0.1f, 0.1f));
        missiles.get(missiles.size()-1).setSpeedX((playerX-missiles.get(missiles.size()-1).getX())*missileSpeed);
        missiles.get(missiles.size()-1).setSpeedY((playerY-missiles.get(missiles.size()-1).getY())*missileSpeed);
    }

    // Update the position of the missiles
    public void updateMissiles() {
        for (int i = 0; i < this.missiles.size(); i++) {
            if (missiles.get(i).getY() > (float) 5.0) {
                missiles.remove(i);
            } else {
                missiles.get(i).setY(missiles.get(i).getY() + (missiles.get(i).getSpeedY()));
                missiles.get(i).setX(missiles.get(i).getX() + (missiles.get(i).getSpeedX()));
            }
        }
    }
}