import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            // Get title, year, and director of the current movie.
            String movieInfoQuery = "SELECT title, year, director " +
                                    "FROM movies " +
                                    "WHERE id = ?;";
            PreparedStatement movieInfoStatement = conn.prepareStatement(movieInfoQuery);
            movieInfoStatement.setString(1, id);
            ResultSet movieInfoQueryResult = movieInfoStatement.executeQuery();
            while (movieInfoQueryResult.next()) {
                String movieTitle = movieInfoQueryResult.getString("title");
                int movieYear = movieInfoQueryResult.getInt("year");
                String movieDirector = movieInfoQueryResult.getString("director");
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
            }
            movieInfoQueryResult.close();
            movieInfoStatement.close();

            // Get all genres of movie
            String genresQuery = "SELECT g.name " +
                                 "FROM genres g " +
                                 "JOIN genres_in_movies gim " +
                                 "ON g.id = gim.genreId " +
                                 "WHERE gim.movieId = ?;";
            PreparedStatement genreStatement = conn.prepareStatement(genresQuery);
            genreStatement.setString(1, id);
            ResultSet genreQueryResult = genreStatement.executeQuery();
            JsonArray genresJsonArray = new JsonArray();
            while (genreQueryResult.next()) {
                String genreName = genreQueryResult.getString("name");
                genresJsonArray.add(genreName);
            }
            jsonObject.add("genres", genresJsonArray);
            genreQueryResult.close();
            genreStatement.close();

            // Get all stars of movie
            String starsQuery = "SELECT s.id, s.name " +
                                "FROM stars s " +
                                "JOIN stars_in_movies sim " +
                                "ON s.id = sim.starId " +
                                "WHERE sim.movieId = ?;";
            PreparedStatement starsStatement = conn.prepareStatement(starsQuery);
            starsStatement.setString(1, id);
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
            starsStatement.close();

            // Get movie rating
            String ratingQuery = "SELECT rating " +
                                 "FROM ratings " +
                                 "WHERE movieId = ?;";
            PreparedStatement ratingStatement = conn.prepareStatement(ratingQuery);
            ratingStatement.setString(1, id);
            ResultSet ratingQueryResult = ratingStatement.executeQuery();
            while (ratingQueryResult.next()) {
                float rating = ratingQueryResult.getFloat("rating");
                jsonObject.addProperty("movie_rating", rating);
            }

            jsonArray.add(jsonObject);
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}