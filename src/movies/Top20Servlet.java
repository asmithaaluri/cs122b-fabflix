package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.sql.*;

@WebServlet(name = "movies.Top20Servlet", urlPatterns = "/api/top20")
public class Top20Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
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
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()
        ) {
            String query = "WITH top_movies (movieId, title, year, director, rating) AS ( " +
                               "SELECT m.id, m.title, m.year, m.director, r.rating " +
                               "FROM movies m " +
                               "JOIN ratings r " +
                               "ON m.id = r.movieId " +
                               "ORDER BY r.rating " +
                               "DESC LIMIT 20" +
                           ") " +
                           "SELECT t.movieId, t.title, t.year, t.director, t.rating, " +
                           "(" +
                           "SELECT JSON_ARRAYAGG(g.name) " +
                           "FROM genres_in_movies gm " +
                           "JOIN genres g ON gm.genreId = g.id " +
                           "WHERE gm.movieId = t.movieId " +
                            ") AS genres, " +
                           "(" +
                           "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', s.id, 'name', s.name)) " +
                           "FROM stars_in_movies sm " +
                           "JOIN stars s ON sm.starId = s.id " +
                           "WHERE t.movieId = sm.movieId " +
                           ") AS stars " +
                           "FROM top_movies t;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // Get movie info and store as JSON
                String movie_id = rs.getString("movieId");
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

                // Get first 3 genres
                String genre_map = rs.getString("genres");
                JsonArray genreArray = JsonParser.parseString(genre_map).getAsJsonArray();
                jsonObject.add("genres", genreArray);

                // Get first 3 stars
                String star_map = rs.getString("stars");
                JsonArray starArray = JsonParser.parseString(star_map).getAsJsonArray();
                jsonObject.add("stars", starArray);

                jsonArray.add(jsonObject);
            }
            rs.close();

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
    }
}

