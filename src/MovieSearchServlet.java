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

@WebServlet(name = "MovieSearchServlet", urlPatterns = "/api/movies/search")
public class MovieSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String allMoviesQuery =
            "WITH starred_movies (starId, movieCount) AS (" +
                    "SELECT s.id, COUNT(sm.movieId) " +
                    "FROM stars s " +
                    "JOIN stars_in_movies sm ON s.id = sm.starId " +
                    "GROUP BY s.id " +
                    ") " +
                    "SELECT DISTINCT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, " +
                    "( " +
                    "SELECT JSON_ARRAYAGG(" +
                    "JSON_OBJECT('id', g.id, 'name', g.name)" +
                    ") " +
                    "FROM genres_in_movies gm " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = m.id " +
                    "LIMIT 3 " +
                    ") AS genres, " +
                    "( " +
                    "SELECT JSON_ARRAYAGG(" +
                    "JSON_OBJECT('id', s.id, 'name', s.name, 'count', star.movieCount)" +
                    ") " +
                    "FROM stars_in_movies sm " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "JOIN starred_movies star ON star.starId = s.id " +
                    "WHERE m.id = sm.movieId " +
                    "LIMIT 3 " +
                    ") AS stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId ";

    private static final String PAGE_ONE = "1";
    private static final int ZERO_ROW_OFFSET = 0;

    private DataSource dataSource;
    private SessionAttribute<String> genreAttribute;
    private SessionAttribute<String> prefixAttribute;

    private SessionAttribute<String> titleAttribute;
    private SessionAttribute<String> yearAttribute;
    private SessionAttribute<String> directorAttribute;
    private SessionAttribute<String> starAttribute;

    private SessionAttribute<String> sortAttribute;
    private SessionAttribute<String> moviesPerPageAttribute;
    private SessionAttribute<String> pageAttribute;

    public void init(ServletConfig config) {
        this.genreAttribute = new SessionAttribute<>(String.class, "genre");
        this.prefixAttribute = new SessionAttribute<>(String.class, "prefix");

        this.titleAttribute = new SessionAttribute<>(String.class, "title");
        this.yearAttribute = new SessionAttribute<>(String.class, "year");
        this.directorAttribute = new SessionAttribute<>(String.class, "director");
        this.starAttribute = new SessionAttribute<>(String.class, "star");

        this.pageAttribute = new SessionAttribute<>(String.class, "page");
        this.sortAttribute = new SessionAttribute<>(String.class, "sort");
        this.moviesPerPageAttribute = new SessionAttribute<>(String.class, "moviesPerPage");

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

        System.out.println("MovieSearchServlet doGet");
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            HttpSession session = request.getSession();

            // reset other filters
            genreAttribute.set(session, null);
            prefixAttribute.set(session, null);
            pageAttribute.set(session, PAGE_ONE);

            // 'p' indicates variable storing parameter
            String ptitle = request.getParameter("title");
            String pyear = request.getParameter("year");
            String pdirector = request.getParameter("director");
            String pstar = request.getParameter("star");

            // save search params
            titleAttribute.set(session, ptitle);
            yearAttribute.set(session, pyear);
            directorAttribute.set(session, pdirector);
            starAttribute.set(session, pstar);

            // use existing sorting and movies per page
            String sort = sortAttribute.get(session);
            String moviesPerPage = moviesPerPageAttribute.get(session);

            StringBuilder query = new StringBuilder(allMoviesQuery);
            boolean validYear = SearchUtility.addSearchClausesToQuery(query, pstar, ptitle, pyear, pdirector);
            SortingUtility.addSortingToQuery(sort, query);

            try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
                int index = 1;
                index += SearchUtility.addSearchParamsToQuery(statement, pstar, ptitle, pyear, pdirector, validYear);
                statement.setInt(index, Integer.parseInt(moviesPerPage) + 1);
                index++;
                statement.setInt(index, ZERO_ROW_OFFSET);

                ResultSet rs = statement.executeQuery();
                JsonObject responseObject = new JsonObject();
                JsonArray jsonArray = new JsonArray();

                int count = 0;
                while (count < Integer.parseInt(moviesPerPage) && rs.next()) {
                    count++;
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
                responseObject.addProperty("hasNextPage", rs.next());
                rs.close();
                responseObject.addProperty("movieData", jsonArray.toString());

                // Log to localhost log
                request.getServletContext().log("getting " + jsonArray.size() + " results");

                out.write(responseObject.toString());
            }
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