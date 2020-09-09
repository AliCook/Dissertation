import java.awt.event.KeyEvent;
/*
	Class to capture all character inputs from the keyboard, as used for entering a name in the game
*/
public class KeyboardInput {
	public static String getKey(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_A) { return ("A"); }
		if (e.getKeyCode() == KeyEvent.VK_B) { return ("B"); }
		if (e.getKeyCode() == KeyEvent.VK_C) { return ("C"); }
		if (e.getKeyCode() == KeyEvent.VK_D) { return ("D"); }
		if (e.getKeyCode() == KeyEvent.VK_E) { return ("E"); }
		if (e.getKeyCode() == KeyEvent.VK_F) { return ("F"); }
		if (e.getKeyCode() == KeyEvent.VK_G) { return ("G"); }
		if (e.getKeyCode() == KeyEvent.VK_H) { return ("H"); }
		if (e.getKeyCode() == KeyEvent.VK_I) { return ("I"); }
		if (e.getKeyCode() == KeyEvent.VK_J) { return ("J"); }
		if (e.getKeyCode() == KeyEvent.VK_K) { return ("K"); }
		if (e.getKeyCode() == KeyEvent.VK_L) { return ("L"); }
		if (e.getKeyCode() == KeyEvent.VK_M) { return ("M"); }
		if (e.getKeyCode() == KeyEvent.VK_N) { return ("N"); }
		if (e.getKeyCode() == KeyEvent.VK_O) { return ("O"); }
		if (e.getKeyCode() == KeyEvent.VK_P) { return ("P"); }
		if (e.getKeyCode() == KeyEvent.VK_Q) { return ("Q"); }
		if (e.getKeyCode() == KeyEvent.VK_R) { return ("R"); }
		if (e.getKeyCode() == KeyEvent.VK_S) { return ("S"); }
		if (e.getKeyCode() == KeyEvent.VK_T) { return ("T"); }
		if (e.getKeyCode() == KeyEvent.VK_U) { return ("U"); }
		if (e.getKeyCode() == KeyEvent.VK_V) { return ("V"); }
		if (e.getKeyCode() == KeyEvent.VK_W) { return ("W"); }
		if (e.getKeyCode() == KeyEvent.VK_X) { return ("X"); }
		if (e.getKeyCode() == KeyEvent.VK_Y) { return ("Y"); }
		if (e.getKeyCode() == KeyEvent.VK_Z) { return ("Z"); }
		if (e.getKeyCode() == KeyEvent.VK_1) { return ("1"); }
		if (e.getKeyCode() == KeyEvent.VK_2) { return ("2"); }
		if (e.getKeyCode() == KeyEvent.VK_3) { return ("3"); }
		if (e.getKeyCode() == KeyEvent.VK_4) { return ("4"); }
		if (e.getKeyCode() == KeyEvent.VK_5) { return ("5"); }
		if (e.getKeyCode() == KeyEvent.VK_6) { return ("6"); }
		if (e.getKeyCode() == KeyEvent.VK_7) { return ("7"); }
		if (e.getKeyCode() == KeyEvent.VK_8) { return ("8"); }
		if (e.getKeyCode() == KeyEvent.VK_9) { return ("9"); }
		if (e.getKeyCode() == KeyEvent.VK_0) { return ("0"); }
		return ("");
	}
}