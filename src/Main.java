import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Game game = new Game();
        // Measure the time taken
        long startTime = System.nanoTime();
        game.play();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double elapsedSeconds = elapsedTime / 1_000_000_000.0;
        System.out.printf("Elapsed time: %.2f seconds%n", elapsedSeconds);
        double gamesPerSecond = 680_000 / elapsedSeconds;
        System.out.printf("Games per second: %.2f%n", gamesPerSecond);


    }
}
