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

            String query = "SELECT m.id, m.title, m.year, m.director FROM movies m JOIN ratings r ON m.id = r.movieId ORDER BY r.rating DESC LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");

                //                String star_name = rs.getString("name");
                //                String star_dob = rs.getString("birthYear");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);

                //                jsonObject.addProperty("star_name", star_name);
                //                jsonObject.addProperty("star_dob", star_dob);

                // Get first 3 genres for current movie.
                Statement generesStatement = conn.createStatement();
                String genresQuery = "SELECT g.name FROM genres g JOIN genres_in_movies gim ON g.id = gim.genreId WHERE gim.movieId = '" + movie_id + "' LIMIT 3";
                ResultSet genresQueryResult = generesStatement.executeQuery(genresQuery);
                JsonArray genresJsonArray = new JsonArray();
                while (genresQueryResult.next()) {
                    String genreName = genresQueryResult.getString("name");
                    if (genresQueryResult.wasNull()) {
                        genreName = "N/A";
                    }
                    genresJsonArray.add(genreName);
                }
                jsonObject.add("genres", genresJsonArray);
                genresQueryResult.close();
                generesStatement.close();

                // Get first 3 stars for current movie.
                Statement starsStatement = conn.createStatement();
                String starsQuery = "SELECT s.name FROM stars s JOIN stars_in_movies sim ON s.id = sim.starId WHERE sim.movieId = '" + movie_id + "' LIMIT 3";
                ResultSet starsQueryResult = starsStatement.executeQuery(starsQuery);
                JsonArray starsJsonArray = new JsonArray();
                while (starsQueryResult.next()) {
                    String starName = starsQueryResult.getString("name");
                    if (starsQueryResult.wasNull()) {
                        starName = "N/A";
                    }
                    starsJsonArray.add(starName);
                }
                jsonObject.add("stars", starsJsonArray);
                starsQueryResult.close();
                starsStatement.close();

                // Get the rating of the current movie.
                Statement ratingStatement = conn.createStatement();
                String ratingQuery = "SELECT r.rating FROM ratings r WHERE r.movieId = '" + movie_id + "'";
                ResultSet ratingQueryResult = ratingStatement.executeQuery(ratingQuery);
                JsonArray ratingJsonArray = new JsonArray();
                while (ratingQueryResult.next()) {
                    float rating = ratingQueryResult.getFloat("rating");
                    if (ratingQueryResult.wasNull()) {
                        rating = 0;
                    }
                    ratingJsonArray.add(rating);
                }
                jsonObject.add("rating", ratingJsonArray);
                ratingQueryResult.close();
                ratingStatement.close();

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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

