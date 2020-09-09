import java.util.ArrayList;

public class Explosion extends Sprite {
	protected int counter;
	protected float radius;
	private float[] explosionColor1 = {1.0f, 0.0f, 0.0f};
	private float[] explosionColor2 = {1.0f, 0.45f, 0.0f};
	private ArrayList<float[]> colorArray;
	
	// Constructor
	public Explosion(float x, float y, float r) {
		super(x, y, r, r);
		this.counter = 0;
		radius = r;
		colorArray = new ArrayList<float[]>();
		colorArray.add(explosionColor1);
		colorArray.add(explosionColor2);
	}

	// Get size of explosion
	public float getRadius() {
		return radius;
	}

	// Counter used for different explosion sprite variants
	public int getCounter() {
		return this.counter;
	}

	public void updateCounter() {
		this.counter++;
	}

	// Get the colours of the explosion sprites
	public ArrayList<float[]> getColors() {
		return colorArray;
	}

	// Fade sprite colours to black 
	public ArrayList<float[]> fadeColors() {
		for( int n = 0; n < colorArray.size(); n++) {
			for( int i = 0; i < 3; i++) {
				colorArray.get(n)[i] = colorArray.get(n)[i] * 0.7f;
			}
		}
		return colorArray;
	}
}