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

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String starMovieCountsCTE =
            "WITH starred_movies (starId, movieCount) AS ( " +
                    "SELECT s.id, COUNT(sm.movieId) " +
                    "FROM stars s " +
                    "JOIN stars_in_movies sm ON s.id = sm.starId " +
                    "GROUP BY s.id " +
                    ")";
    private static final String genreFilterQuery =
            ", filtered_movies (id, title, year, director) AS ( " +
                    "SELECT movies.id, movies.title, movies.year, movies.director " +
                    "FROM movies " +
                    "JOIN genres_in_movies gm ON movies.id = gm.movieId " +
                    "WHERE gm.genreId = ? " +
                    ") " +
                    "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating,  " +
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
                    "FROM filtered_movies m " +
                    "LEFT JOIN ratings r " +
                    "ON m.id = r.movieId";
    private static final String asteriskFilterQuery =
            "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating,  " +
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
                    ") " +
                    "AS stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.title REGEXP '^[^A-Za-z0-9]'";
    private static final String firstLetterFilterQuery =
            "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating,  " +
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
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE LEFT(m.title, 1) LIKE ?";
    private static final String allMoviesQuery =
            "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating,  " +
                    "( " +
                    "SELECT JSON_ARRAYAGG(" +
                    "JSON_OBJECT('id', g.id, 'name', g.name)" +
                    ") " +
                    "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = m.id " +
                    "LIMIT 3 ) " +
                    "AS genres, " +
                    "( " +
                    "SELECT JSON_ARRAYAGG(" +
                    "JSON_OBJECT('id', s.id, 'name', s.name, 'count', star.movieCount)" +
                    ") " +
                    "FROM stars_in_movies sm " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "JOIN starred_movies star ON star.starId = s.id " +
                    "WHERE m.id = sm.movieId " +
                    "LIMIT 3 ) " +
                    "AS stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r " +
                    "ON m.id = r.movieId";

    private static final String PAGE_ONE = "1";

    private DataSource dataSource;
    private SessionAttribute<String> genreAttribute;
    private SessionAttribute<String> prefixAttribute;
    private SessionAttribute<String> titleAttribute;
    private SessionAttribute<String> yearAttribute;
    private SessionAttribute<String> directorAttribute;
    private SessionAttribute<String> starAttribute;
    private SessionAttribute<String> sortAttribute;
    private SessionAttribute<String> moviesAttribute;
    private SessionAttribute<String> pageAttribute;

    public void init(ServletConfig config) {
        this.genreAttribute = new SessionAttribute<>(String.class, "genre");
        this.prefixAttribute = new SessionAttribute<>(String.class, "prefix");
        this.titleAttribute = new SessionAttribute<>(String.class, "title");
        this.yearAttribute = new SessionAttribute<>(String.class, "year");
        this.directorAttribute = new SessionAttribute<>(String.class, "director");
        this.starAttribute = new SessionAttribute<>(String.class, "star");
        this.sortAttribute = new SessionAttribute<>(String.class, "sort");
        this.moviesAttribute = new SessionAttribute<>(String.class, "moviesPerPage");
        this.pageAttribute = new SessionAttribute<>(String.class, "page");
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

        HttpSession session = request.getSession();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String genre = request.getParameter("genre");
        String prefix = request.getParameter("prefix");
        String sort = request.getParameter("sort");
        String movies = request.getParameter("moviesPerPage");
        String page = request.getParameter("page");
        String ptitle = null, pdirector = null, pstar = null, pyear = null;
        boolean validYear = false;

        if (genre != null) { // new genre to browse by
            genreAttribute.set(session, genre);
            // reset all other filters
            prefix = null;
            prefixAttribute.set(session, null);
            titleAttribute.set(session, null);
            yearAttribute.set(session, null);
            directorAttribute.set(session, null);
            starAttribute.set(session, null);
            pageAttribute.set(session, PAGE_ONE);
        } else if (prefix != null) { // new prefix to browse by
            prefixAttribute.set(session, prefix);
            // reset all other filters
            genreAttribute.set(session, null);
            titleAttribute.set(session, null);
            yearAttribute.set(session, null);
            directorAttribute.set(session, null);
            starAttribute.set(session, null);
            pageAttribute.set(session, PAGE_ONE);
        } else {
            // see if previously filtering
            genre = genreAttribute.get(session);
            prefix = prefixAttribute.get(session);
            ptitle = titleAttribute.get(session);
            pyear = yearAttribute.get(session);
            pdirector = directorAttribute.get(session);
            pstar = starAttribute.get(session);
        }
        if (sort != null) {
            sortAttribute.set(session, sort);
            pageAttribute.set(session, PAGE_ONE);
            moviesAttribute.set(session, movies);
        } else {
            sort = sortAttribute.get(session);
            movies = moviesAttribute.get(session);
        }
        if (page != null) {
            pageAttribute.set(session, page);
        } else {
            page = pageAttribute.get(session);
        }

        StringBuilder query = new StringBuilder(starMovieCountsCTE);

        try (Connection conn = dataSource.getConnection()) {
            if (genre != null) {
                query.append(genreFilterQuery);
            } else if (prefix != null) {
                if (prefix.equals("*")) {
                    query.append(asteriskFilterQuery);
                } else {
                    query.append(firstLetterFilterQuery);
                }
            } else {
                query.append(allMoviesQuery);
                if (ptitle != null || pyear != null || pdirector != null || pstar != null){
                    query.append(" ");
                    validYear = SearchUtility.addSearchClausesToQuery(query, pstar, ptitle, pyear, pdirector);
                }
            }
            SortingUtility.addSortingToQuery(sort, query);

            try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
                int index = 1;
                if (genre != null) {
                    statement.setInt(1, Integer.parseInt(genre));
                    index++;
                } else if (prefix != null && !prefix.equals("*")) {
                    statement.setString(1, prefix);
                    index++;
                } else if (ptitle != null || pyear != null || pdirector != null || pstar != null) {
                    index += SearchUtility.addSearchParamsToQuery(statement, pstar, ptitle, pyear, pdirector, validYear);
                }
                statement.setInt(index, Integer.parseInt(movies) + 1);
                index++;
                statement.setInt(index, (Integer.parseInt(page) - 1) * Integer.parseInt(movies));
                JsonArray jsonArray = new JsonArray();
                JsonObject responseObject = new JsonObject();
                try (ResultSet rs = statement.executeQuery()) {
                    int count = 0;
                    while (count < Integer.parseInt(movies) && rs.next()) {
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
                }
                responseObject.addProperty("movieData", jsonArray.toString());

                // Log to localhost log
                request.getServletContext().log("getting " + jsonArray.size() + " results");

                // Write JSON string to output
                out.write(responseObject.toString());
            }

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
