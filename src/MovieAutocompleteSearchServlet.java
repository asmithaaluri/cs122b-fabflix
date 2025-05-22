import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MovieAutocompleteSearchServlet", urlPatterns = "/api/movies/autocomplete-search")
public class MovieAutocompleteSearchServlet extends HttpServlet {
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
        String movieTitles = request.getParameter("title").trim();
        JsonObject result = new JsonObject();
        JsonArray movieArray = new JsonArray();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            if (!movieTitles.isEmpty()) {
                String queryParams = SearchUtility.buildMovieTitleFullTextSearchQueryParams(movieTitles);

                String queryToGet10Movies = "SELECT id, title " +
                                            "FROM movies " +
                                            "WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) " +
                                            "LIMIT 10";

                try (PreparedStatement statementToGet10Movies = conn.prepareStatement(queryToGet10Movies)) {
                    statementToGet10Movies.setString(1, queryParams);

                    try (ResultSet rs = statementToGet10Movies.executeQuery()) {
                        while (rs.next()) {
                            JsonObject movieObject = new JsonObject();
                            movieObject.addProperty("movie_id", rs.getString("id"));
                            movieObject.addProperty("title", rs.getString("title"));
                            movieArray.add(movieObject);
                        }
                    }
                }

                if (movieArray.isEmpty()) {
                    String editDistanceQuery = "SELECT id, title " +
                                                "FROM movies " +
                                                "WHERE " +
                                                SearchUtility.buildEditDistanceClause("title", movieTitles) +
                                                "LIMIT 10;";

                    try (PreparedStatement statementToGetRemainingMoviesFromEditDistance =
                                 conn.prepareStatement(editDistanceQuery)) {
                        statementToGetRemainingMoviesFromEditDistance.setString(1, movieTitles);
                        try (ResultSet rs = statementToGetRemainingMoviesFromEditDistance.executeQuery()) {
                            while (rs.next()) {
                                JsonObject movieObject = new JsonObject();
                                movieObject.addProperty("movie_id", rs.getString("id"));
                                movieObject.addProperty("title", rs.getString("title"));
                                movieArray.add(movieObject);
                            }
                        }
                    }
                }

            }

            result.add("movieData", movieArray);
            out.write(result.toString());
            out.close();
            response.setStatus(HttpServletResponse.SC_OK);

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
