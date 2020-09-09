public class Asteroid extends Sprite {
	protected float radius;
	public Asteroid(float x, float y, float r) {
		super(x, y, r, r);
		this.radius = r;
	}

	public float getRadius() {
		return radius;
	}
}