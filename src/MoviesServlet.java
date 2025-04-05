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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();
            String queryMovies = "SELECT m.id, m.title, m.year, m.director, r.rating " +
                                 "FROM movies m " +
                                 "JOIN ratings r " +
                                 "ON m.id = r.movieId " +
                                 "ORDER BY r.rating DESC LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(queryMovies);

            JsonArray jsonArray = new JsonArray();

            // Prepare statements to be executed iteratively for each movie id
            String genresQuery = "SELECT g.name " +
                                 "FROM genres g " +
                                 "JOIN genres_in_movies gim " +
                                 "ON g.id = gim.genreId " +
                                 "WHERE gim.movieId = ? " +
                                 "LIMIT 3;";
            PreparedStatement genreStatement = conn.prepareStatement(genresQuery);

            String starsQuery = "SELECT s.id, s.name " +
                                "FROM stars s " +
                                "JOIN stars_in_movies sim " +
                                "ON s.id = sim.starId " +
                                "WHERE sim.movieId = ? " +
                                "LIMIT 3;";
            PreparedStatement starsStatement = conn.prepareStatement(starsQuery);

            // Iterate through each row of rs
            while (rs.next()) {
                // Get movie info and store as JSON
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                float rating = rs.getFloat("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("rating", rating);

                // Get first 3 genres for current movie.
                genreStatement.setString(1, movie_id);
                ResultSet genresQueryResult = genreStatement.executeQuery();
                JsonArray genresJsonArray = new JsonArray();
                while (genresQueryResult.next()) {
                    String genreName = genresQueryResult.getString("name");
                    genresJsonArray.add(genreName);
                }
                jsonObject.add("genres", genresJsonArray);
                genresQueryResult.close();

                // Get first 3 stars for current movie.
                starsStatement.setString(1, movie_id);
                ResultSet starsQueryResult = starsStatement.executeQuery();
                JsonArray starsJsonArray = new JsonArray();
                JsonArray starIdsJsonArray = new JsonArray();
                while (starsQueryResult.next()) {
                    String starName = starsQueryResult.getString("name");
                    String starId = starsQueryResult.getString("id");
                    starsJsonArray.add(starName);
                    starIdsJsonArray.add(starId);
                }
                jsonObject.add("stars", starsJsonArray);
                jsonObject.add("star_ids", starIdsJsonArray);
                starsQueryResult.close();
                jsonArray.add(jsonObject);
            }
            rs.close();
            genreStatement.close();
            starsStatement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}

