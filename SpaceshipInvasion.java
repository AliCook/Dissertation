import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.awt.*;
import javax.swing.JFrame;
import javax.swing.*;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.applet.Applet;
import javafx.scene.media.AudioClip;
import java.net.URL;

@SuppressWarnings("serial")
public class SpaceshipInvasion extends GLJPanel implements GLEventListener, KeyListener {
   // Define the target frames per second value
   private static final int FPS = 60; // animator's target frames per second
   // Load classes for rendering text
   private TextRenderer renderer = new TextRenderer(new Font("Serif", Font.BOLD, 36));
   private TextRenderer rendererBold = new TextRenderer(new Font("Impact", Font.BOLD, 100));
 
   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            // Create the OpenGL rendering canvas
            GLJPanel canvas = new SpaceshipInvasion();
 
            // Create an animator that draws the display() on the canvas at the target FPS.
            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

            // Create the top level window
            final JFrame frame = new JFrame();
            frame.getContentPane().add(canvas);
            // Make the window full-screen
            frame.setUndecorated(true);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            // Stop the process if the window is force closed
            frame.addWindowListener(
               new WindowAdapter() {
                  @Override
                  public void windowClosing(WindowEvent e) {
                     new Thread() {
                        @Override
                        public void run() {
                           if (animator.isStarted()) animator.stop();{
                              System.exit(0);
                           }
                        }
                     }.start();
                  }
               }
            );
            frame.setTitle("Spaceship Invasion");
            frame.pack();
            frame.setVisible(true);
            animator.start();
         }
      });
   }
 
 
   private GLU glu;

   // Constants used for the entire program duration
   private double startTime;
   private double gameTime;
   private double bossTimer;
   private double pauseTime;
   private double finalTime;
   private String timeElapsed;
   // Variables for the sound clips
   private URL musicFile;
   private URL bossFile;
   private URL boomFile;
   private URL enemyShootFile;
   private URL playerFile;
   private URL powerUpFile;
   private AudioClip musicTrack;
   private AudioClip bossTrack;
   private AudioClip explosionSound;
   private AudioClip enemyShot;
   private AudioClip playerShoot;
   private AudioClip powerUpSound;
   private boolean music;
   
   // Function which returns the current time in seconds
   private double getSeconds() {
      return System.currentTimeMillis()/1000.0;
   }

   // Constructor class
   public SpaceshipInvasion() {
      this.addGLEventListener(this);
      this.setFocusable(true);
      this.requestFocus();
      addKeyListener(this);
      startTime = getSeconds();
      // On program start, randomly generate the animated stars for the background
      stars = generateStars();
      stars2 = generateStars();
      stars3 = generateStars();

      fps = false;
      frameCount = 0;

      // Load the sound clips
      try {
         music = true;
         musicFile = new File("soundtrack.wav").toURI().toURL();
         bossFile = new File("bossTrack.wav").toURI().toURL();
         boomFile = new File("explosion.wav").toURI().toURL();
         enemyShootFile = new File("enemyShot.wav").toURI().toURL();
         playerFile = new File("playerShot.wav").toURI().toURL();
         powerUpFile = new File("powerUp.wav").toURI().toURL();
         musicTrack = new AudioClip(musicFile.toString());
         bossTrack = new AudioClip(bossFile.toString());
         explosionSound = new AudioClip(boomFile.toString());
         enemyShot = new AudioClip(enemyShootFile.toString());
         playerShoot = new AudioClip(playerFile.toString());
         powerUpSound = new AudioClip(powerUpFile.toString());
         explosionSound.setVolume(0.85);
         enemyShot.setVolume(0.85);
         playerShoot.setVolume(1.0);
         bossTrack.setVolume(0.2);
         bossTrack.setCycleCount(999999);
         musicTrack.setVolume(0.2);
         musicTrack.setCycleCount(999999);
         musicTrack.play();
         powerUpSound.setVolume(0.5);
         powerUpSound.setCycleCount(999999);
      } catch(Exception e) {
         musicTrack = null;
         explosionSound = null;
         enemyShot = null;
         playerShoot = null;
         bossTrack = null;
         powerUpSound = null;
         System.out.println("Fail to load audio");
      }
   }

   // Procedure to stop all sounds currently playing
   private void stopSounds() {
      musicTrack.stop();
      bossTrack.stop();
      explosionSound.stop();
      enemyShot.stop();
      playerShoot.stop();
      powerUpSound.stop();
   }

   // Initialisation of the graphics window
   @Override
   public void init(GLAutoDrawable drawable) {
      // The OpenGL graphics context
      GL2 gl = drawable.getGL().getGL2();
      // Graphics library utilities
      glu = new GLU();
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
      gl.glClearDepth(1.0f);
      // Enables depth testing
      gl.glEnable(GL.GL_DEPTH_TEST);
      gl.glDepthFunc(GL.GL_LEQUAL);
   }
 
   @Override
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
      // The OpenGL graphics context
      GL2 gl = drawable.getGL().getGL2();
 
      if (height == 0) height = 1;
      float aspect = (float)width / height;
 
      // Set the view port (display area) to cover the entire window
      gl.glViewport(0, 0, width, height);
 
      // Setup perspective projection with matching aspect ratio
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(45.0, aspect, 0.1, 100.0);
 
      // Enable the model-view transform
      gl.glMatrixMode(GL2.GL_MODELVIEW);
      gl.glLoadIdentity();

   }

   // All game variables as used for the game code
   private boolean menu = true;
   private boolean gameover = false;
   private boolean leaderboard = false;
   private boolean choosename = false;
   private boolean refreshScores = true;
   private boolean paused = false;
   private boolean exitgame = false;
   private float flashingText;
   private float [][] stars;
   private float [][] stars2;
   private float [][] stars3;
   private SpaceShip playerChar;
   private ArrayList<Asteroid> asteroids;
   private ArrayList<EnemyShip> enemyShips;
   private ArrayList<PowerUp> powerUps;
   private ArrayList<Explosion> explosions;
   private ArrayList<Boss> bosses;
   private boolean flame = false;
   private DecimalFormat df = new DecimalFormat("#.####");
   private static float[] shipColor1 = {0.3f, 0.3f, 0.3f};
   private static float[] shipColor2 = {0.2f, 0.2f, 0.2f};
   private static float[] shipColor3 = {0.1f, 0.1f, 0.1f};
   private static float[] enemyShipColor1 = {0.35f, 0.35f, 0.35f};
   private static float[] enemyShipColor2 = {0.15f, 0.15f, 0.15f};
   private static float[] enemyShipColor3 = {0.1f, 0.1f, 0.1f};
   private static float[] flameColor1 = {1.0f, 0.0f, 0.0f};
   private static float[] flameColor2 = {1.0f, 0.6f, 0.0f};
   private static float[] asteroidColor1 = {0.3f, 0.3f, 0.3f};
   private static float[] asteroidColor2 = {0.1f, 0.1f, 0.1f};
   private static float[] missileColor = {1.0f, 0.6f, 0.0f};
   private static float[] starColor = {1.0f, 1.0f, 1.0f};
   private static float[] backgroundColor = {0.0f, 0.0f, 0.0f};
   private static float[] healthColor = {0.0f, 1.0f, 0.0f};
   private static float[] shieldColor = {0.0f, 0.85f, 1.0f};
   private static float[] black = {0.0f, 0.0f, 0.0f};
   private boolean firing = false;
   private int fireCounter = 0;
   private int shotCount = 0;
   private int enemyFireCounter = 0;
   private boolean spawnAsteroids;
   private boolean spawnShips;
   private boolean phase2;
   private int spawnSpeed;
   private float missileAngle;
   private ArrayList<Scores> printScores;
   private String charName;
   private String timeSurvivedStr;
   private String timeSurvivedDisplay;
   private boolean fps;
   private int frameCount;
   private int frameNumber;
   private long frameTime;
   private boolean bossFight;
   private int bossCounter;

   @Override
   public void display(GLAutoDrawable drawable) {
      // The OpenGL graphics context
      GL2 gl = drawable.getGL().getGL2();
      gl.setSwapInterval(0);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
      gl.glLoadIdentity();
      gl.glTranslatef(0.0f, 0.0f, -6.0f);

      // Variable used for setting the transparency of text fading in and out
      flashingText = (float) Math.abs(Math.sin(getSeconds()-startTime));

      if (menu) {
         /* 
            --- MENU PAGE ---
            Initaites starting variables
         */
         drawRectangle(gl, 8f, 6f, backgroundColor);
         renderStars(drawable, gl);
         playerChar = new SpaceShip(0.0f,-2.0f, 0.3f, 0.5f);
         asteroids = new ArrayList<Asteroid>();
         enemyShips = new ArrayList<EnemyShip>();
         powerUps = new ArrayList<PowerUp>();
         explosions = new ArrayList<Explosion>();
         bosses = new ArrayList<Boss>();
         spawnAsteroids = true;
         spawnShips = false;
         phase2 = false;
         spawnSpeed = 30;
         refreshScores = true;
         charName = "";
         bossFight = false;

         if (exitgame) {
            /* 
               --- EXIT GAME ---
               Renders the options allowing the player the close the game
            */
            renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
            renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            renderer.draw("EXIT GAME?", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("EXIT GAME?").getWidth())/2)),
               (drawable.getSurfaceHeight()/2));
            renderer.draw("[ESCAPE] TO CANCEL       [ENTER] TO EXIT", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("[ESCAPE] TO CANCEL       [ENTER] TO EXIT").getWidth())/2)),
               (drawable.getSurfaceHeight()/2-200));
         } else {
            /* 
               --- RENDER MENU PAGE ---
               Shows the menu options and invites the player to start the game
            */
            rendererBold.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
            rendererBold.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            rendererBold.draw("SPACESHIP INVASION", (int) ((drawable.getSurfaceWidth()/2)-((rendererBold.getBounds("SPACESHIP INVASION").getWidth())/2)),
               (drawable.getSurfaceHeight()/2+200));
            rendererBold.endRendering();

            renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
            renderer.setColor(1.0f, 1.0f, 1.0f, flashingText);
            renderer.draw("PRESS [ENTER] TO START GAME", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("PRESS [ENTER] TO START GAME").getWidth())/2)),
               (drawable.getSurfaceHeight()/2));
            renderer.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            renderer.draw("PRESS [SPACE] FOR LEADERBOARD", (int) ((drawable.getSurfaceWidth()/4)-((renderer.getBounds("PRESS [SPACE] FOR LEADERBOARD").getWidth())/2)),
               (drawable.getSurfaceHeight()/2-500));
            renderer.draw("PRESS [M] TO TOGGLE MUSIC", (int) ((drawable.getSurfaceWidth()*3/4)-((renderer.getBounds("PRESS [M] TO TOGGLE MUSIC").getWidth())/2)),
               (drawable.getSurfaceHeight()/2-500));
            renderer.draw("PRESS [ESCAPE] TO EXIT GAME", (int) ((drawable.getSurfaceWidth()/6)-((renderer.getBounds("PRESS [ESCAPE] TO EXIT GAME").getWidth())/2)),
               (drawable.getSurfaceHeight()/2+500));
         }
         renderer.endRendering();

      } else if (choosename){
         /* 
            --- CHOOSE NAME PAGE ---
            Shows game instructions
            Input name with chars A-Z 0-9
         */
         // Clear background and draws the animated stars
         drawRectangle(gl, 8.0f,  6.0f, backgroundColor);
         renderStars(drawable, gl);

         // Render The explanation of controls, and describes the enemies to the player
         renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
         renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         renderer.draw("CONTROLS", (int) ((drawable.getSurfaceWidth()*1/3)-((renderer.getBounds("CONTROLS").getWidth())/2)),
            (drawable.getSurfaceHeight()/2+425));
         renderer.draw("[W]", (int) ((drawable.getSurfaceWidth()*1/3-92)-((renderer.getBounds("[W]").getWidth())/2)),
            (drawable.getSurfaceHeight()/2+300));
         renderer.draw("[A] [S] [D] TO MOVE", (int) ((drawable.getSurfaceWidth()*1/3)-((renderer.getBounds("[A] [S] [D] TO MOVE").getWidth())/2)),
            (drawable.getSurfaceHeight()/2+250));
         renderer.draw("[SPACE] TO FIRE", (int) ((drawable.getSurfaceWidth()*1/3)-((renderer.getBounds("[SPACE] TO FIRE").getWidth())/2)),
            (drawable.getSurfaceHeight()/2+175));
         renderer.draw(("ENTER NAME:    " + charName + "_"), (int) ((drawable.getSurfaceWidth()/2)
            -((renderer.getBounds(("ENTER NAME:    " + charName + "_")).getWidth())/2)), (drawable.getSurfaceHeight()/2));
         renderer.draw(("DESTROY FOES"), (int) ((drawable.getSurfaceWidth()*2/3)
            -((renderer.getBounds(("DESTROY FOES")).getWidth())/2)), (drawable.getSurfaceHeight()/2+425));
         renderer.draw(("COLLECT POWERUPS"), (int) ((drawable.getSurfaceWidth()*2/3)
            -((renderer.getBounds(("COLLECT POWERUPS")).getWidth())/2)), (drawable.getSurfaceHeight()/2+225));
         renderer.endRendering();

         // Renders example enemies and power-ups
         gl.glPushMatrix();
            gl.glTranslatef(1.0f, 1.6f, 0.0f);
            drawCircle(gl, 0.5f, asteroidColor1, asteroidColor2);
         gl.glPopMatrix();
         gl.glPushMatrix();
               gl.glTranslatef(2.0f, 1.6f, 0.0f);
               gl.glPushMatrix();
                  gl.glTranslatef(0.3f, 0.1f, 0.0f);
                  drawRectangle(gl, 0.2f, 0.1f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               gl.glPushMatrix();
                  gl.glTranslatef(-0.3f, 0.1f, 0.0f);
                  drawRectangle(gl, 0.2f, 0.1f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               gl.glPushMatrix();
                  gl.glTranslatef(0.0f, -0.1f, 0.0f);
                  drawRectangle(gl, 0.1f, 0.05f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               drawRectangle(gl, 0.5f, 0.2f, enemyShipColor1, enemyShipColor3);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(1.5f, 0.7f, 0.0f);
               drawRectangle(gl, 0.3f, 0.3f, starColor);
               drawRectangle(gl, 0.3f/7f, 0.3f*0.6f, shipColor1);
               gl.glTranslatef(0.3f*2f/7f, 0.0f, 0.0f);
               drawRectangle(gl, 0.3f/7f, 0.3f*0.6f, shipColor1);
               gl.glTranslatef(-0.3f*4f/7f, 0.0f, 0.0f);
               drawRectangle(gl, 0.3f/7f, 0.3f*0.6f, shipColor1);
            gl.glPopMatrix();
      } else if (gameover){
         /* 
            --- GAMEOVER PAGE ---
            Displays and saves gamescore to file
            Shows the top 10 hiscores
         */
         // Clear background and draws the animated stars
         drawRectangle(gl, 8.0f,  6.0f, backgroundColor);
         renderStars(drawable, gl);

         // Creates arraylist for saving a new score
         ArrayList<Scores> scores = new ArrayList<Scores>();
         
         renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
         renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         if (refreshScores) {
            finalTime = getSeconds();
            timeSurvivedDisplay = (String.format("%02d:%02d", ((int) (finalTime-gameTime) / 60), ((int) (finalTime-gameTime) % 60)));
            if (playerChar.getPoints()>0) {
               scores.add(new Scores(charName, (int) (finalTime-gameTime), playerChar.getPoints()));
            }
            // Write the score to the high-scores text file
            Scores.writeScore(scores);
            // Reads the scores from the high-scores text file
            printScores = Scores.readScores();
            // Sort the high-scores to highest score first
            Collections.sort(printScores);
            refreshScores = false;
         }

         renderer.draw("GAMEOVER", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("GAMEOVER").getWidth())/2)), (drawable.getSurfaceHeight()/2+350));
         // Render players final score
         renderer.draw((charName + "     " + timeSurvivedDisplay +  "     " + playerChar.getPoints()), (int) ((drawable.getSurfaceWidth()/2)
            -((renderer.getBounds(charName + "     " + timeSurvivedDisplay + "     " + playerChar.getPoints()).getWidth())/2)),
            (drawable.getSurfaceHeight()/2+250));

         renderer.draw("Name", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Name").getWidth())/2-250), (drawable.getSurfaceHeight()/2+150));
         renderer.draw("Time Survived", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Time Survived").getWidth())/2), (drawable.getSurfaceHeight()/2+150));
         renderer.draw("Points", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Points").getWidth())/2+200), (drawable.getSurfaceHeight()/2+150));

         // Render the top 10 high-scores from the high-scores text file
         int yHeight = (drawable.getSurfaceHeight()/2+100);
         for (int i = 0; (i < printScores.size())&&(i < 10); i++) {
            timeSurvivedStr = (String.format("%02d:%02d", (printScores.get(i).getTime() / 60), (printScores.get(i).getTime()) % 60));
            renderer.draw(String.valueOf(i+1), (int) (drawable.getSurfaceWidth()/2 - 400 - ((renderer.getBounds(String.valueOf(i+1)).getWidth())/2)), yHeight);
            renderer.draw(printScores.get(i).getName(),
               (int) (drawable.getSurfaceWidth()/2 - 250 -((renderer.getBounds(printScores.get(i).getName()).getWidth())/2)), yHeight);
            renderer.draw(timeSurvivedStr, (int) (drawable.getSurfaceWidth()/2 - ((renderer.getBounds(timeSurvivedStr).getWidth())/2)), yHeight);
            renderer.draw(String.valueOf(printScores.get(i).getPoints()),
               (int) (drawable.getSurfaceWidth()/2 + 200 - ((renderer.getBounds(String.valueOf(printScores.get(i).getPoints())).getWidth())/2)), yHeight);
            yHeight -= 50;
         }
         
         renderer.draw("PRESS [ENTER] TO RETURN TO MENU", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("PRESS [ENTER] TO RETURN TO MENU").getWidth())/2)),
            (drawable.getSurfaceHeight()/2-425));
         renderer.endRendering();
      } else if (leaderboard){
         /* 
            --- TOP 10 LEADERBOARD ---
            Shows the top 10 hiscores
         */
         // Clear background and draws the animated stars
         drawRectangle(gl, 8.0f,  6.0f, backgroundColor);
         renderStars(drawable, gl);

         renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
         renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         if (refreshScores) {
            // Reads the scores from the high-scores text file
            printScores = Scores.readScores();
            // Sort the high-scores to highest score first if the hiscores file exists
            if (printScores.size() != 0) {
               Collections.sort(printScores);
            }
            refreshScores = false;
         }

         renderer.draw("Name", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Name").getWidth())/2-250), (drawable.getSurfaceHeight()/2+300));
         renderer.draw("Time Survived", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Time Survived").getWidth())/2), (drawable.getSurfaceHeight()/2+300));
         renderer.draw("Points", (int) ((drawable.getSurfaceWidth()/2)-(renderer.getBounds("Points").getWidth())/2+200), (drawable.getSurfaceHeight()/2+300));

         // Render the top 10 high-scores from the high-scores text file
         int yHeight = (drawable.getSurfaceHeight()/2+250);
         for (int i = 0; (i < printScores.size())&&(i < 10); i++) {
            timeSurvivedStr = (String.format("%02d:%02d", (printScores.get(i).getTime() / 60), (printScores.get(i).getTime()) % 60));
            renderer.draw(String.valueOf(i+1), (int) (drawable.getSurfaceWidth()/2 - 400 - ((renderer.getBounds(String.valueOf(i+1)).getWidth())/2)), yHeight);
            renderer.draw(printScores.get(i).getName(),
               (int) (drawable.getSurfaceWidth()/2 - 250 -((renderer.getBounds(printScores.get(i).getName()).getWidth())/2)), yHeight);
            renderer.draw(String.valueOf(timeSurvivedStr),
               (int) (drawable.getSurfaceWidth()/2 - ((renderer.getBounds(String.valueOf(timeSurvivedStr)).getWidth())/2)), yHeight);
            renderer.draw(String.valueOf(printScores.get(i).getPoints()),
               (int) (drawable.getSurfaceWidth()/2 + 200 - ((renderer.getBounds(String.valueOf(printScores.get(i).getPoints())).getWidth())/2)), yHeight);
            yHeight -= 60;
         }
         renderer.draw("PRESS [ESC] TO RETURN TO MENU", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("PRESS [ESC] TO RETURN TO MENU").getWidth())/2)),
            (drawable.getSurfaceHeight()/2-400));
         renderer.endRendering();
      } else {
         /* 
            --- If paused is true ---
            Stops all game transformations and freezes the timer
         */
         if (!paused) {
            /* 
               --- RUN GAME ---
               Makes game decisions and transformations
            */
            computeGameChanges();
         }
         // Render all elements in the frame
         render(drawable, gl);
      }
      
   }

   // Render a filled rectangle
   public void drawRectangle(GL2 gl, float w, float h, float[] color) {
      gl.glBegin(GL.GL_TRIANGLES);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f((w/2), (h/2), 0.0f);
         gl.glVertex3f(-(w/2), -(h/2), 0.0f);
         gl.glVertex3f((w/2), -(h/2), 0.0f);
         gl.glVertex3f(-(w/2), (h/2), 0.0f);
         gl.glVertex3f((w/2), (h/2), 0.0f);
         gl.glVertex3f(-(w/2), -(h/2), 0.0f);
      gl.glEnd();
   }

   // Render a two-tone rectangle
   public void drawRectangle(GL2 gl, float w, float h, float[] color, float[] color2) {
      gl.glBegin(GL.GL_TRIANGLES);
         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f((w/2), (h/2), 0.0f);
         gl.glVertex3f((w/2), -(h/2), 0.0f);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(0.0f, 0.0f, 0.0f);

         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f(-(w/2), (h/2), 0.0f);
         gl.glVertex3f((w/2), (h/2), 0.0f);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(0.0f, 0.0f, 0.0f);

         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f(-(w/2), -(h/2), 0.0f);
         gl.glVertex3f(-(w/2), (h/2), 0.0f);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(0.0f, 0.0f, 0.0f);

         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f((w/2), -(h/2), 0.0f);
         gl.glVertex3f(-(w/2), -(h/2), 0.0f);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(0.0f, 0.0f, 0.0f);
      gl.glEnd();
   }

   // Render a gradient rectangle
   public void drawRectangleF(GL2 gl, float w, float h, float[] color, float[] color2) {
      gl.glBegin(GL.GL_TRIANGLES);
         gl.glVertex3f(w, 0f, 0.0f);
         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f(-w, -2f*(h), 0.0f);
         gl.glVertex3f(w, -2f*(h), 0.0f);
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(-w, 0f, 0.0f);
         gl.glVertex3f(w, 0f, 0.0f);
         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f(-w, -2f*(h), 0.0f);
      gl.glEnd();
   }

   // Render a circle
   public void drawCircle(GL2 gl, float r, float[] color) {
      r = r/2;
      int circleVertexes = 40;
      gl.glColor3f(color[0], color[1], color[2]);
      gl.glBegin(GL.GL_TRIANGLES);
      for (int i = 0; i <= circleVertexes; i++) {
         gl.glVertex3f(0.0f, 0.0f, 0.0f);
         gl.glVertex3f(
            r * (float) Math.cos((i*(360/circleVertexes))/(180/(Math.PI))),
            r * (float) Math.sin((i*(360/circleVertexes))/(180/(Math.PI))),
            0.0f
         );
         gl.glVertex3f(
            r * (float) Math.cos(((i+1)*(360/circleVertexes))/(180/(Math.PI))),
            r * (float) Math.sin(((i+1)*(360/circleVertexes))/(180/(Math.PI))),
            0.0f
         );
      }
      gl.glEnd();
   }

   // Render a two-tone circle
   public void drawCircle(GL2 gl, float r, float[] color, float[] color2) {
      r = r/2;
      int circleVertexes = 40;
      gl.glBegin(GL.GL_TRIANGLES);
      for (int i = 0; i <= circleVertexes; i++) {
         gl.glColor3f(color[0], color[1], color[2]);
         gl.glVertex3f(0.0f, 0.0f, 0.0f);
         gl.glColor3f(color2[0], color2[1], color2[2]);
         gl.glVertex3f(
            r * (float) Math.cos((i*(360/circleVertexes))/(180/(Math.PI))),
            r * (float) Math.sin((i*(360/circleVertexes))/(180/(Math.PI))),
            0.0f
         );
         gl.glVertex3f(
            r * (float) Math.cos(((i+1)*(360/circleVertexes))/(180/(Math.PI))),
            r * (float) Math.sin(((i+1)*(360/circleVertexes))/(180/(Math.PI))),
            0.0f
         );
      }
      gl.glEnd();
   }

   // Render the flames from ship movement
   public void drawFlame(GL2 gl, float flameAngle) {
      float w = 0.025f;
      // Calculate the length of the thruster
      float h = 0.4f*((float) Math.sqrt(Math.abs(playerChar.getSpeedX())+Math.abs(playerChar.getSpeedY())));

      drawCircle(gl, (2*w), flameColor1);

      // Rotate the thruster to the players direction of travel
      gl.glRotatef(flameAngle, 0.0f, 0.0f, 1.0f);

      drawRectangleF(gl, w, h, flameColor1, flameColor2);
   }

   // Randomly generate all stars position and size
   private float[][] generateStars() {
      // Generates 83 stars per layer
      float[][] stars = new float[83][3];
      Random r = new Random();
      for (int i = 0; i < 83; i++) {
         stars[i][0] =  (((float) r.nextInt(998) + 3f)/50f) - 11f;
         stars[i][1] =  (((float) r.nextInt(998) + 3f)/75f) - 15f;
         stars[i][2] = (float) r.nextInt(8)/1000f + 0.006f;
      }
      return stars;
   }

   private void computeGameChanges() {
      // Creates a random seed every game revolution
      Random random = new Random();

      // Applies player movement, checks to fire a missile and updates missile positions
      flame = playerChar.setSpeed();
      playerChar.updateMissiles();
      playerChar.setX((Float.parseFloat(df.format(playerChar.getX())) + playerChar.getSpeedX()));
      playerChar.setY((Float.parseFloat(df.format(playerChar.getY())) + playerChar.getSpeedY())); 

      // If a missile is fired, play the shoot sound effect, create a missile object, and set the interval for the next missile
      if ((firing)&&(fireCounter%10==0)) {
         playerShoot.play();
         playerChar.fire();
         shotCount = fireCounter;
      }
      fireCounter++;

      // This first code for spawning regular enemies when the bossfight is not active
      if (bossFight == false) {
         // Spawn an Asteroid at a rate determined by spawnSpeed (lower value spawns faster)
         if ((spawnAsteroids)&&((int) ((getSeconds()-(gameTime))*10) % spawnSpeed == 0)) {
            asteroids.add(new Asteroid((((float) random.nextInt(790) + 10f)/100f) - 4.0f, //X
                                       3.0f,                                              //Y
                                       (((float) random.nextInt(40) + 30f)/100f)));       //Radius
            asteroids.get(asteroids.size()-1).setSpeedY(-((float) random.nextInt(30) + 5f)/1000f);
            asteroids.get(asteroids.size()-1).setSpeedX((((float) random.nextInt(30) + 5f)/1000f) - 0.025f);
            spawnAsteroids = false;
         } else if ((!spawnAsteroids)&&((int) (getSeconds()*10-(gameTime)*10) % spawnSpeed != 0)) {
            spawnAsteroids = true;
         }
         // If a time threshold has been reached, enemy ships will spawn at a rate 4x lower than asteroids, capped at 5 ships
         int shipCount = 0;
         // Variable to record only the number of enemyships that are visible on screen, used to determing if the cap is reached
         for(EnemyShip ship : enemyShips) {
            if (ship.getVisible()) { shipCount++; }
         }
         if (phase2&&(shipCount<5)) {
            if ((spawnShips)&&((int) (getSeconds()*10-(gameTime)*10) % (spawnSpeed*4) == 0)) {
               enemyShips.add(new EnemyShip((((float) random.nextInt(790) + 10f)/100f) - 4.0f, 2.5f, 0.9f, 0.3f));
               spawnShips = false;
            } else if ((!spawnShips)&&((int) (getSeconds()*10-(gameTime)*10) % (spawnSpeed*4) != 0)) {
               spawnShips = true;
            }
         }
      } else {
         // The boss code
         for(Boss boss : bosses) {
            boss.setSpeed();
            boss.setY(boss.getY() + boss.getSpeedY());

            // Fire 3 missiles simultaneously at the player and plays a sound effect
            if (bossCounter%75==0) {
               boss.fire(playerChar.getX(), playerChar.getY());
               enemyShot.play();
            }
            // Update the position of all the boss' missiles
            for(Missile missile : boss.getMissiles()) {
               missile.setX(missile.getX() + missile.getSpeedX());
               missile.setY(missile.getY() + missile.getSpeedY());
            }
            boss.updateMissiles();

            // Move the shield sprite which protects the boss' turret
            boss.getShields().get(0).setX(boss.getTurret());
         }
         // Variable used for counting the interval between missiles
         bossCounter++;
      }

      // Randomly spawn powerup at a rate of 1 per minute
      if ((random.nextInt(6001)) < 2) {
         powerUps.add(new PowerUp((((float) random.nextInt(690) + 10)/100f) - 3.5f, 3.0f, 0.3f, 0.3f));
      } 

      // Calculates position of all stars and applies parallax scrolling
      for (int i = 0; i < 83; i++) {
         stars[i][1] -= 0.005f;
         if (stars[i][1] < -10) {
            stars[i][1] += 15;
         }
         // Stars which are furthest from the player move at 50% the inverse of the players speed
         stars[i][0] -= (playerChar.getSpeedX()*0.5);
         stars[i][1] -= (playerChar.getSpeedY()*0.5);

         stars2[i][1] -= 0.005f;
         if (stars2[i][1] < -10) {
            stars2[i][1] += 15;
         }
         // Stars which are medium depth from the player move at 70% the inverse of the players speed
         stars2[i][0] -= (playerChar.getSpeedX()*0.7);
         stars2[i][1] -= (playerChar.getSpeedY()*0.7);

         stars3[i][1] -= 0.005f;
         if (stars3[i][1] < -10) {
            stars3[i][1] += 15;
         }
         // Stars which are closest to the player move at 90% the inverse of the players speed
         stars3[i][0] -= (playerChar.getSpeedX()*0.9);
         stars3[i][1] -= (playerChar.getSpeedY()*0.9);
      }

      // If the player has a powerup, apply the appropriate effect to the missile, else stop playing powerup sound
      double xOffset = 0;
      if (playerChar.getFireType() == 1) {
         // Double helix code to offset a missiles position
         for(Missile missile : playerChar.getMissiles()) {
            double tempTime = getSeconds() - startTime;
            missile.setSpeedX(0.1f*(float)(Math.sin(Math.toRadians(tempTime*200 + xOffset))));
            xOffset += 120;
         }
      } else if (playerChar.getFireType() == 2) {
         // Code for the spread shot firepower upgrade
         for(Missile missile : playerChar.getMissiles()) {
            missileAngle = -(float) (Math.toDegrees(Math.atan(missile.getSpeedX()/0.05f)));
            missile.setX(missile.getX() + missile.getSpeedX());
         }
      } else {
         // If there is no power-up active, do not play the sound
         powerUpSound.stop();
      }

      // Move the asteroid by adding its speed
      for(Asteroid asteroid : asteroids) {
         asteroid.setY(asteroid.getY() + asteroid.getSpeedY());
         asteroid.setX(asteroid.getX() + asteroid.getSpeedX());
      }

      // If asteroids are too far off screen, delete them from memory
      for (int i = 0; i < asteroids.size(); i++) {
         if ((asteroids.get(i).getY() < -3.2f)||((asteroids.get(i).getX() < -4.75f))||((asteroids.get(i).getX() > 4.75f))) {
            asteroids.remove(i);
            break;
         }
      }

      // Move the powerup
      for(PowerUp powerup : powerUps) {
         powerup.setY(powerup.getY() - 0.01f);
      }
      // If the enemyship is destroyed and none of its missiles are no longer on screen, delete the ship from memory
      for (int i = 0; i < enemyShips.size(); i++) {
         if (!(enemyShips.get(i).getVisible())&&(enemyShips.get(i).getMissiles().size()==0)){
            enemyShips.remove(i);
            break;
         }
      }

      // Ships move randomly approximately every 1.66 seconds and fire every 2.75 seconds (probabilities based off the assumption of 60 frames per second)
      for(EnemyShip ship : enemyShips) {
         ship.updateMissiles();
         if (((random.nextInt(501)) < 3)&&(ship.getVisible())) {
            enemyShot.play();
            ship.fire();
         }
         if (((random.nextInt(501)) < 5)&&(ship.getVisible())) {
            ship.setSpeedX((((float) random.nextInt(10) + 1)/100f)-0.05f);
            ship.setSpeedY((((float) random.nextInt(10) + 1)/100f)-0.05f);
         }
         ship.setSpeed();
         ship.setX((Float.parseFloat(df.format(ship.getX())) + ship.getSpeedX()));
         ship.setY((Float.parseFloat(df.format(ship.getY())) + ship.getSpeedY()));
      }

      // Check for any Collisions with the player or missiles
      checkPlayerCollisions();
      checkMissileCollisions();

      // At thresholds, change the spawn rate and start spawning ships to increase the challenge of the game
      if (((int) (getSeconds()-gameTime) > 15)&&((int) (getSeconds()-gameTime) < 45)) {
         spawnSpeed = 20;
      } else if (((int) (getSeconds()-gameTime) > 45)&&((int) (getSeconds()-gameTime) < 150)) {
         spawnSpeed = 15;
      } else if ((int) (getSeconds()-gameTime) > 150) {
         spawnSpeed = 5;
      }
      if (((int) (getSeconds()-gameTime) % 60) > 30) {
         phase2 = true;
      }
      // Spawn the boss 90 seconds into the game, and 90 seconds after every boss is defeated
      if (((int) (getSeconds()-bossTimer) % 90 == 0)&&((int) (getSeconds()-bossTimer) > 1)&&(bosses.size()==0)&&(!bossFight)) {
         bossFight = true;
         if (music) {
            musicTrack.stop();
            bossTrack.play();
         }
      }
      // When the area is clear of enemies, the boss may spawn
      if ((bossFight)&&(enemyShips.size()==0)&&(asteroids.size()<=0)&&(bosses.size()==0)) {
         bossCounter = 0;
         bosses.add(new Boss(0f, 3.5f, 7f, 0.6f));
      }
   }

   /*
      --- RENDER GAME ---
      Code the draw all ingame sprites on screen
   */
   private void render(GLAutoDrawable drawable, GL2 gl){
      // Render stars
      renderStars(drawable, gl);
      // Render player's missiles
      for (int i = 0; i < playerChar.getMissiles().size(); i++) {
         if (playerChar.getFireType() == 0) {
            gl.glPushMatrix();
               gl.glTranslatef(playerChar.getMissiles().get(i).getX(), playerChar.getMissiles().get(i).getY(), 0.0f);
               drawRectangle(gl, (playerChar.getMissiles().get(i).getWidth()*1.2f), (playerChar.getMissiles().get(i).getHeight()*1.2f), missileColor, black);
            gl.glPopMatrix();
         } else if (playerChar.getFireType() == 1) {
            gl.glPushMatrix();
               gl.glTranslatef((playerChar.getMissiles().get(i).getX() + playerChar.getMissiles().get(i).getSpeedX()),
               playerChar.getMissiles().get(i).getY(), 0.0f);
               drawRectangle(gl, (playerChar.getMissiles().get(i).getWidth()*1.2f), (playerChar.getMissiles().get(i).getHeight()*1.2f), missileColor, black);
            gl.glPopMatrix();
         } else if (playerChar.getFireType() == 2) {
            missileAngle = -(float) (Math.toDegrees(Math.atan(playerChar.getMissiles().get(i).getSpeedX()/0.05f)));
            gl.glPushMatrix();
               gl.glTranslatef(playerChar.getMissiles().get(i).getX(), playerChar.getMissiles().get(i).getY(), 0.0f);
               gl.glRotatef(missileAngle, 0.0f, 0.0f, 1.0f);
               drawRectangle(gl, (playerChar.getMissiles().get(i).getWidth()*1.2f), (playerChar.getMissiles().get(i).getHeight()*1.2f), missileColor, black);
            gl.glPopMatrix();
         }
      }
      // Render explosion animation when Sprite is destroyed
      for (int i = 0; i < explosions.size(); i++) {
         if (explosions.get(i).getCounter() <= 30) {
            gl.glPushMatrix();
               gl.glTranslatef(explosions.get(i).getX(), explosions.get(i).getY(), 0.0f);
               drawCircle(gl, (explosions.get(i).getRadius()*explosions.get(i).getCounter()*1.4f+0.3f), explosions.get(i).getColors().get(0), explosions.get(i).getColors().get(1));
            gl.glPopMatrix();
            explosions.get(i).updateCounter();
         } else if (explosions.get(i).getCounter() <= 45) {
            explosions.get(i).fadeColors();
            gl.glPushMatrix();
               gl.glTranslatef(explosions.get(i).getX(), explosions.get(i).getY(), 0.0f);
               drawCircle(gl, (explosions.get(i).getRadius()*30f*1.4f+0.3f), explosions.get(i).getColors().get(0), explosions.get(i).getColors().get(1));
            gl.glPopMatrix();
            explosions.get(i).updateCounter();
         } else {
            explosions.remove(i);
         }
      }
      // Render all Asteroids
      for (int i = 0; i < asteroids.size(); i++) {
         gl.glPushMatrix();
            gl.glTranslatef(asteroids.get(i).getX(), asteroids.get(i).getY(), 0.0f);
            drawCircle(gl, asteroids.get(i).getRadius(), asteroidColor1, asteroidColor2);
         gl.glPopMatrix();
      }
      // Render powerups
      for (int i = 0; i < powerUps.size(); i++) {
         gl.glPushMatrix();
            gl.glTranslatef(powerUps.get(i).getX(), powerUps.get(i).getY(), 0.0f);
            drawRectangle(gl, powerUps.get(i).getWidth(), powerUps.get(i).getHeight(), starColor);
            drawRectangle(gl, powerUps.get(i).getWidth()/7f, powerUps.get(i).getHeight()*0.6f, shipColor1);
            gl.glTranslatef(powerUps.get(i).getWidth()*2f/7f, 0.0f, 0.0f);
            drawRectangle(gl, powerUps.get(i).getWidth()/7f, powerUps.get(i).getHeight()*0.6f, shipColor1);
            gl.glTranslatef(-powerUps.get(i).getWidth()*4f/7f, 0.0f, 0.0f);
            drawRectangle(gl, powerUps.get(i).getWidth()/7f, powerUps.get(i).getHeight()*0.6f, shipColor1);
         gl.glPopMatrix();
      }
      // Render enemy ships and missiles
      for (int i = 0; i < enemyShips.size(); i++) {
         for (int j = 0; j < enemyShips.get(i).getMissiles().size(); j++) {
            gl.glPushMatrix();
               gl.glTranslatef(enemyShips.get(i).getMissiles().get(j).getX(), enemyShips.get(i).getMissiles().get(j).getY(), 0.0f);
               drawRectangle(gl, enemyShips.get(i).getMissiles().get(j).getWidth(), enemyShips.get(i).getMissiles().get(j).getHeight(), missileColor, black);
            gl.glPopMatrix();
         }

         if (enemyShips.get(i).getVisible()) {
            gl.glPushMatrix();
               gl.glTranslatef(enemyShips.get(i).getX(), enemyShips.get(i).getY(), 0.0f);
               if (enemyShips.get(i).getFlame()) {
                  gl.glPushMatrix();
                     gl.glTranslatef(0.3f, 0.1f, 0.0f);
                     drawFlame(gl, enemyShips.get(i).getFlameAngle());
                  gl.glPopMatrix();
                  gl.glPushMatrix();
                     gl.glTranslatef(-0.3f, 0.1f, 0.0f);
                     drawFlame(gl, enemyShips.get(i).getFlameAngle());
                  gl.glPopMatrix();
               }
               gl.glPushMatrix();
                  gl.glTranslatef(0.3f, 0.1f, 0.0f);
                  drawRectangle(gl, 0.2f, 0.1f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               gl.glPushMatrix();
                  gl.glTranslatef(-0.3f, 0.1f, 0.0f);
                  drawRectangle(gl, 0.2f, 0.1f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               gl.glPushMatrix();
                  gl.glTranslatef(0.0f, -0.1f, 0.0f);
                  drawRectangle(gl, 0.1f, 0.05f, enemyShipColor2, enemyShipColor3);
               gl.glPopMatrix();
               // Render the ships health points bar
               gl.glPushMatrix();
                  gl.glTranslatef(0.0f, 0.2f, 0.0f);
                  drawRectangle(gl, ((0.2f/3f)*enemyShips.get(i).getHealth()), 0.05f, healthColor);
               gl.glPopMatrix();
               drawRectangle(gl, 0.5f, 0.2f, enemyShipColor1, enemyShipColor3);
            gl.glPopMatrix();
         }
      }
      // Render boss
      for (int i = 0; i < bosses.size(); i++) {
         // Render boss shields
         for (int j = 0; j < bosses.get(i).getShields().size(); j++) {
            gl.glPushMatrix();
               gl.glTranslatef(bosses.get(i).getShields().get(j).getX(), bosses.get(i).getShields().get(j).getY(), 0.0f);
               drawRectangle(gl, bosses.get(i).getShields().get(j).getWidth(), bosses.get(i).getShields().get(j).getHeight(), shieldColor, black);
            gl.glPopMatrix();
         }
         // Render all boss missiles
         for (int j = 0; j < bosses.get(i).getMissiles().size(); j++) {
            gl.glPushMatrix();
               gl.glTranslatef(bosses.get(i).getMissiles().get(j).getX(), bosses.get(i).getMissiles().get(j).getY(), 0.0f);
               drawCircle(gl, 0.1f, missileColor, flameColor1);
            gl.glPopMatrix();
         }
         // Render the boss ship
         gl.glPushMatrix();
            gl.glTranslatef(bosses.get(i).getX(), bosses.get(i).getY(), 0.0f);
            drawRectangle(gl, 4.5f, 0.3f, enemyShipColor1, enemyShipColor3);
            // Render the boss' health points bar
            gl.glPushMatrix();
               gl.glTranslatef(0.0f, 0.4f, 0.0f);
               drawRectangle(gl, ((0.2f/3f)*bosses.get(i).getHealth()/2), 0.05f, healthColor);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(0.0f, 0.22f, 0.0f);
               drawRectangle(gl, 3.5f, 0.15f, enemyShipColor3);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(1.75f, 0.15f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.15f, 0.0f);
                  gl.glVertex3f(0.0f, 0.0f, 0.0f);
                  gl.glVertex3f(0.2f, 0.0f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(-1.75f, 0.15f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.15f, 0.0f);
                  gl.glVertex3f(0.0f, 0.0f, 0.0f);
                  gl.glVertex3f(-0.2f, 0.0f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(-2.25f, -0.3f, 0.0f);
               drawRectangle(gl, 0.3f, 0.3f, enemyShipColor3);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef((0.0f + bosses.get(i).getTurret()), -0.2f, 0.0f);
               drawRectangle(gl, 0.6f, 0.1f, enemyShipColor3);
               gl.glTranslatef(0.0f, -0.05f, 0.0f);
               drawRectangle(gl, 0.15f, 0.1f, enemyShipColor3);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(-2.25f, 0.0f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.15f, 0.0f);
                  gl.glVertex3f(-0.15f, -0.15f, 0.0f);
                  gl.glVertex3f(0.0f, -0.15f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(2.1f, -0.15f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.0f, 0.0f);
                  gl.glVertex3f(0.0f, -0.1f, 0.0f);
                  gl.glVertex3f(-0.1f, 0.0f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(-2.25f, -0.45f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(-0.15f, 0.0f, 0.0f);
                  gl.glVertex3f(0.15f, 0.0f, 0.0f);
                  gl.glColor3f(enemyShipColor2[0], enemyShipColor2[1], enemyShipColor2[2]);
                  gl.glVertex3f(-0.15f, -0.35f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(-2.1f, -0.15f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.0f, 0.0f);
                  gl.glVertex3f(0.0f, -0.1f, 0.0f);
                  gl.glVertex3f(0.1f, 0.0f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(2.25f, -0.3f, 0.0f);
               drawRectangle(gl, 0.3f, 0.3f, enemyShipColor3);
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(2.25f, 0.0f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(0.0f, 0.15f, 0.0f);
                  gl.glVertex3f(0.15f, -0.15f, 0.0f);
                  gl.glVertex3f(0.0f, -0.15f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
               gl.glTranslatef(2.25f, -0.45f, 0.0f);
               gl.glBegin(GL.GL_TRIANGLES);
                  gl.glColor3f(enemyShipColor3[0], enemyShipColor3[1], enemyShipColor3[2]);
                  gl.glVertex3f(-0.15f, 0.0f, 0.0f);
                  gl.glVertex3f(0.15f, 0.0f, 0.0f);
                  gl.glColor3f(enemyShipColor2[0], enemyShipColor2[1], enemyShipColor2[2]);
                  gl.glVertex3f(0.15f, -0.35f, 0.0f);
               gl.glEnd();
            gl.glPopMatrix();
         gl.glPopMatrix();
      }
      // Render player's ship
      gl.glTranslatef(playerChar.getX(), playerChar.getY(), 0.0f);
      drawRectangle(gl, 0.2f, 0.5f, shipColor1, shipColor3);
      gl.glPushMatrix();
         gl.glTranslatef(0.0f, 0.25f, 0.0f);
         drawRectangle(gl, 0.075f, 0.035f, shipColor3);
      gl.glPopMatrix();
      gl.glPushMatrix();
         gl.glTranslatef(0.1f, -0.125f, 0.0f);
         drawRectangle(gl, 0.07f, 0.2f, shipColor1, shipColor3);
         gl.glTranslatef(0.02f, -0.1f, 0.0f);
         drawRectangle(gl, 0.1f, 0.2f, shipColor2, shipColor3);
      gl.glPopMatrix();
      gl.glPushMatrix();
         gl.glTranslatef(-0.1f, -0.125f, 0.0f);
         drawRectangle(gl, 0.07f, 0.2f, shipColor1, shipColor3);
         gl.glTranslatef(-0.02f, -0.1f, 0.0f);
         drawRectangle(gl, 0.1f, 0.2f, shipColor2, shipColor3);
      gl.glPopMatrix();
      // Render player's flame trails
      if (flame) {
         gl.glPushMatrix();
            gl.glTranslatef(0.125f, -0.325f, 0.0f);
            drawFlame(gl, playerChar.getFlameAngle());
         gl.glPopMatrix();

         gl.glPushMatrix();
            gl.glTranslatef(-0.125f, -0.325f, 0.0f);
            drawFlame(gl, playerChar.getFlameAngle());
         gl.glPopMatrix();
      }
      // Render flashing powerup text (if active)
      renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
      if (playerChar.getFireType()!=0) {
         renderer.setColor(0.0f, 1.0f, 0.0f, flashingText);
         renderer.draw("POWERUP", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("POWERUP").getWidth())/2)),
            (drawable.getSurfaceHeight()/2+200));
      }
      // Renders flashing PAUSED text if the game is paused
      if (paused) {
         renderer.setColor(1.0f, 1.0f, 1.0f, flashingText);
         renderer.draw("PAUSED", (int) ((drawable.getSurfaceWidth()/2)-((renderer.getBounds("PAUSED").getWidth())/2)),
            (drawable.getSurfaceHeight()/2));
      } else {
         // Renders the time elapsed
         renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         timeElapsed = ("Survived: " + String.format("%02d:%02d", ((int) (getSeconds()-gameTime) / 60), ((int) (getSeconds()-gameTime) % 60)));
         renderer.draw(timeElapsed, (int) ((drawable.getSurfaceWidth()/2+150)-((renderer.getBounds(timeElapsed).getWidth())/2)), ((drawable.getSurfaceHeight()*14)/15));
      }
      // Displays player's points
      renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
      renderer.draw(("Points: " + String.valueOf(playerChar.getPoints())), (int) ((drawable.getSurfaceWidth()/2-150)
         -((renderer.getBounds("Points: " + String.valueOf(playerChar.getPoints())).getWidth())/2)),
         (drawable.getSurfaceHeight()*14/15));

      // Code to count the frame rate per second and draw on the window (if active)
      frameCount++;
      if ((frameTime != TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))) {
         frameNumber = frameCount;
         frameCount = 0;
         frameTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
      }
      if (frameNumber<=35) {  renderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);   }
      if (fps) { renderer.draw(("FPS: " + String.valueOf(frameNumber)), (int) ((drawable.getSurfaceWidth()/6)), (drawable.getSurfaceHeight()*14/15)); }
      renderer.endRendering();
   }

   private void renderStars(GLAutoDrawable drawable, GL2 gl){
      // If stars fall beyond the bottom of the screen, wrap back to the top
      for (int i = 0; i < 83; i++) {
         stars[i][1] -= 0.0025f;
         if (stars[i][1] < -10) {
            stars[i][1] += 15;
         }

         stars2[i][1] -= 0.0035f;
         if (stars2[i][1] < -10) {
            stars2[i][1] += 15;
         }

         stars3[i][1] -= 0.0045f;
         if (stars3[i][1] < -10) {
            stars3[i][1] += 15;
         }
      }
      // Draw stars on the drawable canvas
      for (int i = 0; i < 83; i++) {
         gl.glPushMatrix();
            gl.glTranslatef(stars[i][0], stars[i][1], 0.0f);
            drawRectangle(gl, stars[i][2], stars[i][2], starColor);
         gl.glPopMatrix();
         gl.glPushMatrix();
            gl.glTranslatef(stars2[i][0], stars2[i][1], 0.0f);
            drawRectangle(gl, stars2[i][2], stars2[i][2], starColor);
         gl.glPopMatrix();
         gl.glPushMatrix();
            gl.glTranslatef(stars3[i][0], stars3[i][1], 0.0f);
            drawRectangle(gl, stars3[i][2], stars3[i][2], starColor);
         gl.glPopMatrix();
      }
   }

   // Check if player intersects any sprites using bounding boxes
   private void checkPlayerCollisions() {
      // Bounding box of the player spaceship
      Rectangle2D r3 = playerChar.getBounds();
      // Checks collisions between the player and asteroids
      for (int i = 0; i < asteroids.size(); i++) {
         Rectangle2D r2 = asteroids.get(i).getBounds();
         if (r3.intersects(r2)) {
            // If a collision, the player will perish and the game is over
            explosionSound.setVolume(1.0);
            explosionSound.play();
            explosionSound.setVolume(0.85);
            stopSounds();
            if (music) {
               musicTrack.play();
            }
            gameover = true;
         }
      }
      // Checks collisions between the player and enemy ships
      for (int i = 0; i < enemyShips.size(); i++) {
         Rectangle2D r4 = enemyShips.get(i).getBounds();
         if (r3.intersects(r4)) {
            // If a collision, the player will perish and the game is over
            explosionSound.setVolume(1.0);
            explosionSound.play();
            explosionSound.setVolume(0.85);
            stopSounds();
            if (music) {
               musicTrack.play();
            }
            gameover = true;
         }
      }
      // Checks collisions between the player and enemy missiles
      for (int i = 0; i < enemyShips.size(); i++) {
         for (int j = 0; j < enemyShips.get(i).getMissiles().size(); j++) {
            Rectangle2D r5 = enemyShips.get(i).getMissiles().get(j).getBounds();
            if ((r3.intersects(r5))&&(enemyShips.get(i).getVisible())) {
            // If a collision, the player will perish and the game is over
               explosionSound.setVolume(1.0);
               explosionSound.play();
               explosionSound.setVolume(0.85);
               stopSounds();
               if (music) {
                  musicTrack.play();
               }
               gameover = true;
            }
         }
      }
      // Checks intersection between player and a power-up
      for (int i = 0; i < powerUps.size(); i++) {
         Rectangle2D r5 = powerUps.get(i).getBounds();
         if (r3.intersects(r5)) {
            // If they intersect, activate random firepower upgrade
            powerUps.remove(i);
            Random r = new Random();
            playerChar.setFireType(r.nextInt(2)+1);
            powerUpSound.stop();
            // Play power-up sound
            powerUpSound.play();
         }
      }
      // Checks collisions between the player and the boss
      for (int j = 0; j < bosses.size(); j++) {
         Rectangle2D r7 = bosses.get(j).getBounds();
         if (r3.intersects(r7)) {
            // If a collision, the player will perish and the game is over
            explosionSound.setVolume(1.0);
            explosionSound.play();
            explosionSound.setVolume(0.85);
            stopSounds();
            if (music) {
               musicTrack.play();
            }
            gameover = true;
         }
         // Checks collisions between the player and the boss' missiles
         for (int i = 0; i < bosses.get(j).getMissiles().size(); i++) {
            Rectangle2D r6 = bosses.get(j).getMissiles().get(i).getBounds();
            if (r3.intersects(r6)) {
               // If a collision, the player will perish and the game is over
               explosionSound.setVolume(1.0);
               explosionSound.play();
               explosionSound.setVolume(0.85);
               stopSounds();
               if (music) {
                  musicTrack.play();
               }
               gameover = true;
            }
         }
         // Checks collisions between the player and the boss' shields
         for (int i = 0; i < bosses.get(j).getShields().size(); i++) {
            Rectangle2D r8 = bosses.get(j).getShields().get(i).getBounds();
            if (r3.intersects(r8)) {
               // If a collision, the player will perish and the game is over
               explosionSound.setVolume(1.0);
               explosionSound.play();
               explosionSound.setVolume(0.85);
               stopSounds();
               if (music) {
                  musicTrack.play();
               }
               gameover = true;
            }
         }
      }
   }

   // Check if the player's missiles collide with any sprites
   private void checkMissileCollisions() {
      for (int i = 0; i < playerChar.getMissiles().size(); i++) {
         // If the player's missile collides with an asteroid
         for (int j = 0; j < asteroids.size(); j++) {
            if ((asteroids.get(j).getRadius()*0.7f) > ((float) Math.sqrt(
            Math.pow((asteroids.get(j).getX() - 0.015f - playerChar.getMissiles().get(i).getX()), 2)
            + Math.pow((asteroids.get(j).getY() - playerChar.getMissiles().get(i).getY()), 2)))) {
               // If the asteroid is large enough, the asteroid will split into smaller asteroids
               if (asteroids.get(j).getRadius() > 0.4f) {
                  asteroids.add(new Asteroid((asteroids.get(j).getX()+0.005f), asteroids.get(j).getY(), (asteroids.get(j).getRadius()/2)));
                  asteroids.get(asteroids.size()-1).setSpeedY(asteroids.get(j).getSpeedY());
                  asteroids.get(asteroids.size()-1).setSpeedX(asteroids.get(j).getSpeedX()+0.005f);
                  asteroids.add(new Asteroid((asteroids.get(j).getX()-0.005f), asteroids.get(j).getY(), (asteroids.get(j).getRadius())/2));
                  asteroids.get(asteroids.size()-1).setSpeedY(asteroids.get(j).getSpeedY());
                  asteroids.get(asteroids.size()-1).setSpeedX(asteroids.get(j).getSpeedX()-0.005f);
               }
               // Play explosion sound effect
               explosionSound.play();
               // Add points to the players score
               playerChar.setPoints((int) Math.abs(asteroids.get(j).getRadius()-10)*5);
               // Create an explosion sprite size relative to the size of the asteroid
               explosions.add(new Explosion(asteroids.get(j).getX(), asteroids.get(j).getY(), asteroids.get(j).getRadius()*0.01f));
               // Delete the asteroid from the Arraylist
               asteroids.remove(j);
               // Remove the player's missile that collided with the asteroid
               playerChar.getMissiles().remove(i);
               // Return to prevent out-of-bounds array exception as array decreased in size by 1
               return;
            }
         }
      }
      for (int i = 0; i < playerChar.getMissiles().size(); i++) {
         Rectangle2D r1 = playerChar.getMissiles().get(i).getBounds();
         for (int j = 0; j < enemyShips.size(); j++) {
            Rectangle2D r2 = enemyShips.get(j).getBounds();
            // If player's missile collides with enemy ship and it's health becomes 0
            if (((r1.intersects(r2))&&(enemyShips.get(j).getVisible()))&&(enemyShips.get(j).getHealth()<=1)) {
               // Play explosion sound effect
               explosionSound.play();
               // Create an explosion sprite
               explosions.add(new Explosion(enemyShips.get(j).getX(), enemyShips.get(j).getY(), 0.007f));
               // Set enemy ship as invisible as to not destroy its missiles instantly
               enemyShips.get(j).setVisible();
               // Add points to the players score
               playerChar.setPoints(750);
               // Remove the player's missile that collided with the ship
               playerChar.getMissiles().remove(i);
               // Return to prevent out-of-bounds array exception as array decreased in size by 1
               return;
            } else if ((r1.intersects(r2))&&(enemyShips.get(j).getVisible())) {
               // Decrease enemys health by 1
               enemyShips.get(j).setHealth();
               // Remove the player's missile that collided with the ship
               playerChar.getMissiles().remove(i);
               // Return to prevent out-of-bounds array exception as array decreased in size by 1
               return;
            }
         }
      }
      for (int n = 0; n < enemyShips.size(); n++) {
         // Code to allow enemy ship's missiles to interact with asteroids
         for (int i = 0; i < enemyShips.get(n).getMissiles().size(); i++) {
            for (int j = 0; j < asteroids.size(); j++) {
               if ((asteroids.get(j).getRadius()*0.7f) > ((float) Math.sqrt(
               Math.pow((asteroids.get(j).getX() - 0.015f - enemyShips.get(n).getMissiles().get(i).getX()), 2)
               + Math.pow((asteroids.get(j).getY() - enemyShips.get(n).getMissiles().get(i).getY()), 2)))) {
                  if (asteroids.get(j).getRadius() > 0.4f) {
                     // If the asteroid is large enough, the asteroid will split into smaller asteroids
                     asteroids.add(new Asteroid((asteroids.get(j).getX()+0.005f), asteroids.get(j).getY(), (asteroids.get(j).getRadius()/2)));
                     asteroids.get(asteroids.size()-1).setSpeedY(asteroids.get(j).getSpeedY());
                     asteroids.get(asteroids.size()-1).setSpeedX(asteroids.get(j).getSpeedX()+0.005f);
                     asteroids.add(new Asteroid((asteroids.get(j).getX()-0.005f), asteroids.get(j).getY(), (asteroids.get(j).getRadius())/2));
                     asteroids.get(asteroids.size()-1).setSpeedY(asteroids.get(j).getSpeedY());
                     asteroids.get(asteroids.size()-1).setSpeedX(asteroids.get(j).getSpeedX()-0.005f);
                  }
                  // Play explosion sound effect
                  explosionSound.play();
                  // Create an explosion sprite
                  explosions.add(new Explosion(asteroids.get(j).getX(), asteroids.get(j).getY(), 0.007f));
                  // Delete the asteroid from the Arraylist
                  asteroids.remove(j);
                  // Remove the ship's missile that collided with the asteroid
                  enemyShips.get(n).getMissiles().remove(i);
                  // Return to prevent out-of-bounds array exception as array decreased in size by 1
                  return;
               }
            }
         }
      }
      for (int i = 0; i < playerChar.getMissiles().size(); i++) {
         Rectangle2D r1 = playerChar.getMissiles().get(i).getBounds();
         // If a player's missile collides with one of the bosses shields, remove the missile
         for (int j = 0; j < bosses.size(); j++) {
            for (int n = 0; n < bosses.get(j).getShields().size(); n++) {
               Rectangle2D r3 = bosses.get(j).getShields().get(n).getBounds();
               if (r1.intersects(r3)) {
                  // Remove the ship's missile that collided with the shield
                  playerChar.getMissiles().remove(i);
                  // Return to prevent out-of-bounds array exception as array decreased in size by 1
                  return;
               }
            }
         }
         for (int j = 0; j < bosses.size(); j++) {
            Rectangle2D r2 = bosses.get(j).getBounds();
            // Player missile collides with boss ship
            if ((r1.intersects(r2))&&(bosses.get(j).getHealth()<=1)) {
               /* 
                  If the boss' health drops to 0 
               */
               // Play loud explosion sound effect
               explosionSound.setVolume(1.0);
               explosionSound.play();
               explosionSound.setVolume(0.85);
               // Spawn many explosion sprites of varying sizes
               explosions.add(new Explosion(bosses.get(j).getX(),bosses.get(j).getY(), 0.02f));
               explosions.add(new Explosion((bosses.get(j).getX()+0.7f),(bosses.get(j).getY()+0.1f), 0.007f));
               explosions.add(new Explosion((bosses.get(j).getX()-0.7f),(bosses.get(j).getY()-0.1f), 0.01f));
               explosions.add(new Explosion((bosses.get(j).getX()+1.2f),(bosses.get(j).getY()-0.1f), 0.01f));
               explosions.add(new Explosion((bosses.get(j).getX()-1.0f),(bosses.get(j).getY()+0.1f), 0.007f));
               explosions.add(new Explosion((bosses.get(j).getX()+2.0f),(bosses.get(j).getY()), 0.007f));
               explosions.add(new Explosion((bosses.get(j).getX()-2.0f),(bosses.get(j).getY()), 0.01f));
               // Allocate 7500 points to the player
               playerChar.setPoints(7500);
               // Remove the boss object
               bosses.remove(j);
               // Resume normal foe spawning
               bossFight = false;
               // Set timer for another boss spawn
               bossTimer = getSeconds();
               // End the boss soundtrack and return to the normal soundtrack
               if (music) {
                  bossTrack.stop();
                  musicTrack.play();
               }
               // Return to prevent out-of-bounds array exception as array decreased in size by 1
               return;
            } else if (r1.intersects(r2)){
               // Decrease the boss' health by 1
               bosses.get(j).setHealth();
               playerChar.getMissiles().remove(i);
               // Return to prevent out-of-bounds array exception as array decreased in size by 1
               return;
            }
         }
      }
   }

   @Override
   public void dispose(GLAutoDrawable drawable) { }

   // Get keyboard input and apply appropriate action
   @Override
   public void keyPressed(KeyEvent e) {
      // Toggles mute soundtrack at any time excluding when the player enters a name
      if ((e.getKeyCode() == KeyEvent.VK_M)&&(music)&&(!choosename)) {
         bossTrack.stop();
         musicTrack.stop();
         music = false;
      } else if ((e.getKeyCode() == KeyEvent.VK_M)&&(!music)) {
         if(bossFight){
            bossTrack.play();
         } else {
            musicTrack.play();
         }
         music = true;
      }
      // Toggles rendering of the frames per second counter
      if (e.getKeyCode() == KeyEvent.VK_F) {
         fps = !fps;
      }
      /* 
         Menu navigation through keyboard input
      */
      if (menu) {
         // Terminate program
         if ((exitgame)&&(e.getKeyCode() == KeyEvent.VK_ENTER)) {
               System.exit(0);
         }
         // Toggles exit game screen from menu page
         if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exitgame = !exitgame;
         }
         // Navigate away from menu page to enter name page
         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            menu = false;
            gameover = false;
            choosename = true;
            leaderboard = false;
         }
         // Navigate to leaderboard page
         if ((e.getKeyCode() == KeyEvent.VK_SPACE)&&(!exitgame)) {
            menu = false;
            gameover = false;
            choosename = false;
            leaderboard = true;
         }
      } else if (choosename) {
         // If pseudoname is accepted, start the game
         if ((e.getKeyCode() == KeyEvent.VK_ENTER)&&(charName.length()>2)) {
            menu = false;
            gameover = false;
            choosename = false;
            leaderboard = false;
            gameTime = getSeconds();
            bossTimer = getSeconds();
            frameTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
         } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // Navigate back to menu page
            menu = true;
            gameover = false;
            choosename = false;
            leaderboard = false;
         } else {
            // Backspace a char
            if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE)&&(charName.length()>0)) {
               charName = charName.substring(0, (charName.length()-1));
            } else if (charName.length()<=10) {
               charName = charName + KeyboardInput.getKey(e);
            }
         }
      } else if (gameover) {
         // Navigate back to menu page from gameover page
         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            menu = true;
            gameover = false;
            leaderboard = false;
            choosename = false;
         }
      } else if (leaderboard) {
         // Navigate back to menu page
         if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            menu = true;
            leaderboard = false;
            gameover = false;
            choosename = false;
         }
      } else {
         /*
            Player spaceship controls
         */
         // Left and right controls
         if (e.getKeyCode() == KeyEvent.VK_D) {
            playerChar.setKeyX(1);
         } else if (e.getKeyCode() == KeyEvent.VK_A) {
            playerChar.setKeyX(-1);
         }
         // Up and down controls
         if (e.getKeyCode() == KeyEvent.VK_W) {
            playerChar.setKeyY(1);
         } else if (e.getKeyCode() == KeyEvent.VK_S) {
            playerChar.setKeyY(-1);
         }
         // Spacebar to fire missile
         if ((e.getKeyCode() == KeyEvent.VK_SPACE)) {
            if ((!firing)&&((fireCounter-shotCount)>=10)) {
               fireCounter = 0;
            }
            firing = true;
         }
         // Escape key to pause game and freeze game timer
         if ((e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            paused = !paused;
            if (paused==false) {
               gameTime += getSeconds()-pauseTime;
               bossTimer += getSeconds()-pauseTime;
            }
            pauseTime = getSeconds();
         }
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {
      // Cancel acceleration if directional key released
      if ((e.getKeyCode() == KeyEvent.VK_D)||(e.getKeyCode() == KeyEvent.VK_A)) {
         playerChar.setKeyX(0);
      }

      if ((e.getKeyCode() == KeyEvent.VK_W)||(e.getKeyCode() == KeyEvent.VK_S)) {
         playerChar.setKeyY(0);
      }
      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
         firing = false;
      } 
   }

   @Override
   public void keyTyped(KeyEvent e) {}
}