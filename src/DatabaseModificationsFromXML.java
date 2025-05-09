import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class DatabaseModificationsFromXML {
    private Connection connection;

    public DatabaseModificationsFromXML() {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";
            String username = "mytestuser";
            String password = "My6$Password";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL JDBC driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error: Failed to connect to database");
            e.printStackTrace();
        }
    }

    public int insertMoviesInDatabase(ArrayList<Movie> movies) throws SQLException {

        String query = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        int totalMovieInsertions = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            int batchSize = 100; // Every 100 records will be a batch.
            int numberOfQueriesInBatch = 0;

            for (Movie movie : movies) {
                statement.setString(1, movie.getId());
                statement.setString(2, movie.getTitle());
                statement.setInt(3, Integer.parseInt(movie.getYear()));
                statement.setString(4, movie.getDirector());

                statement.addBatch();
                numberOfQueriesInBatch++;

                if (numberOfQueriesInBatch == batchSize) {
                    int[] results = statement.executeBatch();
                    totalMovieInsertions += getNumberOfRowsAffectedInDatabase(results);
                    numberOfQueriesInBatch = 0;
                }
            }

            if (numberOfQueriesInBatch > 0) {
                int[] results = statement.executeBatch();
                totalMovieInsertions += getNumberOfRowsAffectedInDatabase(results);
            }

            connection.commit();

        } catch (Exception e) {
            System.out.println("Error in insertMoviesInDatabase");
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException sql_error) {
                System.out.println("Error: cannot rollback transaction of insertMoviesInDatabase.");
                sql_error.printStackTrace();
            }

        }

        System.out.println("Inserted " + totalMovieInsertions + " new movies.");
        return totalMovieInsertions;
    }

    public int deleteMoviesFromDatabase(ArrayList<Movie> movies) throws SQLException {

        String query = "DELETE FROM movies WHERE id = ?";
        int totalMovieDeletions = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            int batchSize = 100; // Every 100 records will be a batch.
            int numberOfQueriesInBatch = 0;

            for (Movie movie : movies) {
                statement.setString(1, movie.getId());

                statement.addBatch();
                numberOfQueriesInBatch++;

                if (numberOfQueriesInBatch == batchSize) {
                    int[] results = statement.executeBatch();
                    totalMovieDeletions += getNumberOfRowsAffectedInDatabase(results);
                    numberOfQueriesInBatch = 0;
                }
            }

            if (numberOfQueriesInBatch > 0) {
                int[] results = statement.executeBatch();
                totalMovieDeletions += getNumberOfRowsAffectedInDatabase(results);
            }

            connection.commit();

        } catch (Exception e) {
            System.out.println("Error in deleteMoviesFromDatabase");
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException sql_error) {
                System.out.println("Error: cannot rollback transaction of deleteMoviesFromDatabase.");
                sql_error.printStackTrace();
            }

        }

        System.out.println("Deleted " + totalMovieDeletions + " movies");
        return totalMovieDeletions;
    }

    public int insertGenresInDatabaseIfTheyDoNotAlreadyExist(ArrayList<Movie> movies) throws SQLException {
        String checkGenreExistenceQuery = "SELECT id FROM genres WHERE LOWER(name) = LOWER(?)";
        String insertGenreQuery = "INSERT INTO genres (name) VALUES (?)";
        int totalGenreInsertions = 0;

        try (PreparedStatement checkGenreExistenceStatement = connection.prepareStatement(checkGenreExistenceQuery);
            PreparedStatement insertGenreStatement = connection.prepareStatement(insertGenreQuery)) {

            connection.setAutoCommit(false);

            for (Movie movie : movies) {
                ArrayList<String> genresOfMovie = movie.getGenres();
                for (String genre: genresOfMovie) {
                    checkGenreExistenceStatement.setString(1, genre.toLowerCase());

                    try (ResultSet genreResultSet = checkGenreExistenceStatement.executeQuery()) {
                        if (!genreResultSet.next()) {
                            insertGenreStatement.setString(1, genre);
                            insertGenreStatement.executeUpdate(); // THIS WILL IMPACT DATABASE!
                            totalGenreInsertions++;
                        }
                    }

                }

            }

            connection.commit();

        } catch (Exception e) {
            System.out.println("Error in insertGenresInDatabaseIfTheyDoNotAlreadyExist");
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException sql_error) {
                System.out.println("Error: cannot rollback transaction of insertGenresInDatabaseIfTheyDoNotAlreadyExist.");
                sql_error.printStackTrace();
            }

        }

        System.out.println("Inserted " + totalGenreInsertions + " new genres.");
        return totalGenreInsertions;
    }

    public int insertIntoGenresInMoviesTable(ArrayList<Movie> movies) throws SQLException {
        String checkGenreExistenceQuery = "SELECT id FROM genres WHERE LOWER(name) = LOWER(?)";
        String insertIntoGenresInMoviesQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
        String checkDuplicateEntryQuery = "SELECT 1 FROM genres_in_movies WHERE genreId = ? AND movieId = ?";

        int totalInsertions = 0;

        Set<String> seenCompositeKeys = new HashSet<>(); // To avoid duplicate (genreId, movieId) pairs in the database.

        try (PreparedStatement checkGenreExistenceStatement = connection.prepareStatement(checkGenreExistenceQuery);
            PreparedStatement insertIntoGenresInMoviesStatement = connection.prepareStatement(insertIntoGenresInMoviesQuery);
             PreparedStatement checkDuplicateEntryStatement = connection.prepareStatement(checkDuplicateEntryQuery)) {

            connection.setAutoCommit(false);

            int batchSize = 100; // Every 100 records will be a batch.
            int numberOfQueriesInBatch = 0;

            for (Movie movie : movies) {
                ArrayList<String> genresOfMovie = movie.getGenres();
                for (String genre: genresOfMovie) {
                    checkGenreExistenceStatement.setString(1, genre.trim().toLowerCase());
                    try (ResultSet genreResultSet = checkGenreExistenceStatement.executeQuery()) {
                        if (genreResultSet.next()) {
                            int genreId = genreResultSet.getInt("id");
                            String movieId = movie.getId();
                            String compositeKey = genreId + "_" + movieId;

                            if (seenCompositeKeys.contains(compositeKey)) {
                                continue;
                            }

                            checkDuplicateEntryStatement.setInt(1, genreId);
                            checkDuplicateEntryStatement.setString(2, movieId);
                            try (ResultSet duplicateEntryResultSet = checkDuplicateEntryStatement.executeQuery()) {
                                if (!duplicateEntryResultSet.next()) {
                                    seenCompositeKeys.add(compositeKey);

                                    insertIntoGenresInMoviesStatement.setInt(1, genreId);
                                    insertIntoGenresInMoviesStatement.setString(2, movieId);
                                    insertIntoGenresInMoviesStatement.addBatch();
                                    numberOfQueriesInBatch++;

                                    if (numberOfQueriesInBatch == batchSize) {
                                        int[] results = insertIntoGenresInMoviesStatement.executeBatch(); // THIS WILL IMPACT DATABASE!
                                        totalInsertions += getNumberOfRowsAffectedInDatabase(results);
                                        numberOfQueriesInBatch = 0;
                                    }
                                } else {
                                    System.out.println("duplicate entry was found.");
                                }
                            }
                        }

                    }

                }

            }

            if (numberOfQueriesInBatch > 0) {
                int[] results = insertIntoGenresInMoviesStatement.executeBatch(); // THIS WILL IMPACT DATABASE!
                totalInsertions += getNumberOfRowsAffectedInDatabase(results);
            }

            connection.commit();

        } catch (Exception e) {
            System.out.println("Error in insertIntoGenresInMoviesTable");
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException sql_error) {
                System.out.println("Error: cannot rollback transaction of insertIntoGenresInMoviesTable.");
                sql_error.printStackTrace();
            }

        }

        //System.out.println("Inserted " + totalInsertions + " pairs in genres_in_movies.");
        return totalInsertions;
    }

    public int updateStarsAndStarsInMoviesTable(Map<String, Star> starMap) throws SQLException {
        String checkIfStarExistsQuery = "SELECT id FROM stars WHERE LOWER(name) = LOWER(?)";
        String insertIntoStarsQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        String insertIntoStarsInMoviesQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        int totalStarInsertions = 0;
        String assignedIdForNonExistingStar = "1";
        String currentStarId = "";

        try (PreparedStatement checkIfStarExistsStatement = connection.prepareStatement(checkIfStarExistsQuery);
            PreparedStatement insertIntoStarsStatement = connection.prepareStatement(insertIntoStarsQuery);
            PreparedStatement insertIntoStarsInMoviesStatement = connection.prepareStatement(insertIntoStarsInMoviesQuery)) {

            connection.setAutoCommit(false);

            for (String starName : starMap.keySet()) {
                checkIfStarExistsStatement.setString(1, starName.toLowerCase());
                try (ResultSet checkIfStarExistsResultSet = checkIfStarExistsStatement.executeQuery()) {
                    if (checkIfStarExistsResultSet.next()) { // Existing star, so we don't have to insert into stars table.
                        currentStarId = checkIfStarExistsResultSet.getString("id");
                    } else if (!checkIfStarExistsResultSet.next()) { // NonExistingStar, so add them to star table.
                        Star nonExistingStar = starMap.get(starName);
                        insertIntoStarsStatement.setString(1, assignedIdForNonExistingStar); // Update starId.
                        currentStarId = assignedIdForNonExistingStar; // Get the starId to use when inserting into stars_in_movies table.
                        assignedIdForNonExistingStar = String.valueOf(Integer.parseInt(assignedIdForNonExistingStar) + 1);

                        insertIntoStarsStatement.setString(2, nonExistingStar.getName());
                        insertIntoStarsStatement.setInt(3, nonExistingStar.getBirthYear());
                        insertIntoStarsStatement.addBatch();
                        totalStarInsertions++;
                    }
                }
                // Insert into stars_in_movies table. (Already checked, there should be no movieIds in starMap that are already in database.)
                Star existingStar = starMap.get(starName);
                for (String movieId: existingStar.getMovieIds()) {
                    insertIntoStarsInMoviesStatement.setString(1, currentStarId);
                    insertIntoStarsInMoviesStatement.setString(2, movieId);
                    insertIntoStarsInMoviesStatement.addBatch();
                }
            }

            insertIntoStarsStatement.executeBatch();
            insertIntoStarsInMoviesStatement.executeBatch();
            connection.commit();

        } catch (Exception e) {
            System.out.println("Error in updateStarsAndStarsInMoviesTable");
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException sql_error) {
                System.out.println("Error: cannot rollback transaction of updateStarsAndStarsInMoviesTable.");
                sql_error.printStackTrace();
            }
        }

        System.out.println("Inserted " + totalStarInsertions + " new stars.");
        return totalStarInsertions;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Closed database connection.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getNumberOfRowsAffectedInDatabase(int[] results) {
        int count = 0;
        for (int result : results) {
            if (result >= 0) {
                count++;
            }
        }
        return count;
    }
}
