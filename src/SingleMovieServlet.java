import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonParser;
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
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
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

            // Get title, year, director, and rating of the current movie.
            String query = "WITH starred_movies (starId, movieCount) AS " +
                           "( " +
                                "SELECT s.id, COUNT(sm.movieId) " +
                                "FROM stars s " +
                                "JOIN stars_in_movies sm ON s.id = sm.starId " +
                                "GROUP BY s.id " +
                           ") " +
                           "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, " +
                           "( " +
                                "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', g.id, 'name', g.name)) " +
                                "FROM genres_in_movies gm " +
                                "JOIN genres g ON gm.genreId = g.id " +
                                "WHERE gm.movieId = m.id " +
                           ") AS genres,  " +
                           "( " +
                                "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', s.id, 'name', s.name, 'count', star.movieCount)) " +
                                "FROM stars_in_movies sm " +
                                "JOIN stars s ON sm.starId = s.id " +
                                "JOIN starred_movies star ON star.starId = s.id " +
                                "WHERE sm.movieId = m.id " +
                           ") AS stars " +
                           "FROM movies m " +
                           "LEFT JOIN ratings r ON m.id = r.movieId " +
                           "WHERE m.id = ?;";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                float rating = rs.getFloat("rating");

                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("rating", rating);

                String genre_map = rs.getString("genres");
                JsonArray genreArray;
                if (genre_map != null) {
                    genreArray = JsonParser.parseString(genre_map).getAsJsonArray();
                } else {
                    genreArray = new JsonArray();
                }
                jsonObject.add("genres", genreArray);

                String star_map = rs.getString("stars");
                JsonArray starArray;
                if (star_map != null) {
                    starArray = JsonParser.parseString(star_map).getAsJsonArray();
                } else {
                    starArray = new JsonArray();
                }
                jsonObject.add("stars", starArray);

                jsonArray.add(jsonObject);
            }
            statement.close();
            rs.close();
            
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
    }

}