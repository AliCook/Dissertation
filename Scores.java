import java.util.ArrayList;
import java.io.File; 
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;

public class Scores implements Comparable<Scores> {
    private String name = "test1";
    private int time;
    private int points;

    // Score constructor
    public Scores(String name, int time, int points) {
        this.name = name;
        this.time = time;
        this.points = points;
    }

    // Get a score's name
    public String getName() {
        return name;
    }

    // Get a score's time survived
    public int getTime() {
        return time;
    }

    // Get a score's points
    public int getPoints() {
        return points;
    }

    @Override
    public int compareTo(Scores compare) {
        int comparePoints = compare.getPoints();
        return comparePoints-this.points;
    }

    // return the score as a string output
    @Override
    public String toString() {
        return "[ Name=" + name + ", time=" + Integer.toString(time) + ", points=" + Integer.toString(points) + "]";
    }

    // Read the scores from the file "Hiscores.txt" and store in scores arraylist
    public static ArrayList<Scores> readScores() {
        File file = new File("Hiscores.txt");
        ArrayList<Scores> scores = new ArrayList<Scores>();
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                scores.add(new Scores((String) sc.nextLine(), (int) Integer.parseInt(sc.nextLine()), (int) Integer.parseInt(sc.nextLine())));
            }
            return scores;
        } catch (Exception e) {
            // e.printStackTrace();
            return scores;
        }      
    }

    // Write the input score to the end of the file "Hiscores.txt"
    public static void writeScore(ArrayList<Scores> scores) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Hiscores.txt", true));
            for (int i = 0; i < scores.size(); i++) {
                writer.append(scores.get(i).getName());
                writer.newLine();
                writer.append(String.valueOf(scores.get(i).getTime()));
                writer.newLine();
                writer.append(String.valueOf(scores.get(i).getPoints()));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}