import java.util.ArrayList;
import java.text.DecimalFormat;

public class EnemyShip extends Sprite {
	ArrayList<Missile> missiles = new ArrayList<Missile>();
	protected float speedX;
    protected float speedY;
    private int hp;
    protected static float acceleration;
    private DecimalFormat concatenate = new DecimalFormat("#.####");
    private boolean flame;
    private float flameAngle;
    protected boolean visible;

    // Construct enemy ship with 3 hit points
	public EnemyShip(float x, float y, float w, float h) {
		super(x, y, w, h);
		flame = false;
		acceleration = 0.0005f;
        hp = 3;
        visible = true;
	}

    // Get health of the ship
    public int getHealth() {
        return hp;
    }

    // Get if ship is destroyed or not
    public boolean getVisible() {
        return visible;
    }

    // Override sprite speed to remain on the screen
    @Override
    public float getSpeedX() {
        if ((this.getX() - 1) <= -3.95) {
            speedX = 0.005f;
        } else if ((this.getX() + 1) >= 3.95) {
            speedX = -0.005f;
        }
        return speedX;
    }

    // Override sprite speed to remain on the screen
    @Override
    public float getSpeedY() {
        if ((this.getY() - 1) <= -1.5) {
            speedY = (float) 0.005f;
        } else if ((this.getY() + 1) >= 3) {
            speedY = -0.025f;
        }
        return speedY;
    }

    // Return arraylist of all the missiles an enemyship has active
	public ArrayList<Missile> getMissiles() {
        return missiles;
    }

    // Get the angle of thrust if the spaceship moves
    public float getFlameAngle() {
        if (this.getSpeedX()>= 0) {
           flameAngle = (float) (Math.toDegrees(Math.atan(this.getSpeedY()/this.getSpeedX()))-90);
        } else {
           flameAngle = (float) (Math.toDegrees(Math.atan(this.getSpeedY()/this.getSpeedX()))+90);
        }
        return flameAngle;
    }

    public boolean getFlame() {
		return flame;
    }

    @Override
    public void setSpeedX(float xSpeed) {
        speedX = xSpeed;
    }

    @Override
    public void setSpeedY(float ySpeed) {
        speedY = ySpeed;
    }

    // Apply acceleration to the ships speed
    public void setSpeed() {
        speedX = Float.parseFloat(concatenate.format(speedX));
        speedY = Float.parseFloat(concatenate.format(speedY));

        if (speedX > 0) {
            speedX -= acceleration;
            flame = true;
        } else if (speedX < 0) {
            speedX += acceleration;
            flame = true;
        } else {
            speedX = 0;
            flame = false;
        }

        if (speedY > 0) {
            speedY -= acceleration;
            flame = true;
        } else if (speedY < 0) {
            speedY += acceleration;
            flame = true;
        } else {
            speedY = 0;
            flame = false;
        }
    }

    // Decrement ship health by 1
    public void setHealth() {
        --hp;
    }

    // Ship is destroyed and no longer rendered on the screen
    public void setVisible() {
        visible = false;
    }

    // Fire missile
    public void fire() {
        missiles.add(new Missile(this.getX(), this.getY(), 0.06f, 0.15f));
    }

    // Set missile position
    public void updateMissiles() {
        for (int i = 0; i < this.missiles.size(); i++) {
            if (missiles.get(i).getY() < (float) -5.0) {
                missiles.remove(i);
            } else {
                missiles.get(i).setY(missiles.get(i).getY() - 0.03f);
            }
        }
    }
}