package movies;

import java.util.ArrayList;
import java.util.Map;

public class XMLParser {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        DatabaseModificationsFromXML databaseModificationObject = new DatabaseModificationsFromXML();

        MovieSAXParser movieParser = new MovieSAXParser();
        movieParser.runExample();
        ArrayList<Movie> movies = movieParser.getMovies();
        Map<String, Star> starMap = movieParser.getStarMap();

        try {
            int totalMovieInsertions = databaseModificationObject.insertMoviesInDatabase(movies);
            int totalGenreInsertions = databaseModificationObject.insertGenresInDatabaseIfTheyDoNotAlreadyExist(movies);
            int totalGenreInMovieInsertions = databaseModificationObject.insertIntoGenresInMoviesTable(movies);
            int totalStarInsertions = databaseModificationObject.updateStarsAndStarsInMoviesTable(starMap);

            movieParser.displaySummaryToUserAndWriteInconsistencyReports(totalStarInsertions);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            databaseModificationObject.closeConnection();

            // TODO: Comment out before running on AWS instance.
            // Stop the timer and calculate elapsed time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Show minutes.
            if (executionTime > 60000) {
                System.out.println("Total execution time: " + (executionTime / 60000.0) + " minutes");
            }
        }

        System.exit(0); // Get rid of thread error in AWS.

    }

}
