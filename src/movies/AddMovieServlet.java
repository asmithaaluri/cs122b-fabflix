package movies;

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
import java.sql.*;

@WebServlet(name = "movies.AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;
    private static final String callAddMovieProcedure =
            "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("star_name");
        String birthYear = request.getParameter("birth_year");
        String genre = request.getParameter("genre");

        JsonObject resultingMovie = new JsonObject();
        try (Connection connection = dataSource.getConnection();
             CallableStatement addMovieCall =
                     connection.prepareCall(callAddMovieProcedure)) {

            addMovieCall.setString(1, title);
            addMovieCall.setInt(2, Integer.parseInt(year));
            addMovieCall.setString(3, director);
            addMovieCall.setString(4, starName);
            if (birthYear != null && !birthYear.isEmpty()) {
                addMovieCall.setInt(5, Integer.parseInt(birthYear));
            } else {
                addMovieCall.setNull(5, Types.INTEGER);
            }
            addMovieCall.setString(6, genre);


            addMovieCall.registerOutParameter("movieId", Types.VARCHAR);
            addMovieCall.registerOutParameter("starId", Types.VARCHAR);
            addMovieCall.registerOutParameter("genreId", Types.INTEGER);
            addMovieCall.registerOutParameter("added", Types.BIT);
            addMovieCall.execute();
            resultingMovie.addProperty("movieId", addMovieCall.getString("movieId"));
            resultingMovie.addProperty("starId", addMovieCall.getString("starId"));
            resultingMovie.addProperty("genreId", addMovieCall.getInt("genreId"));
            resultingMovie.addProperty("added", addMovieCall.getBoolean("added"));
            resultingMovie.addProperty("status", "success");

            response.setContentType("application/json");
            response.getWriter().write(resultingMovie.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            resultingMovie.addProperty("status", "failure");
            response.setContentType("application/json");
            response.getWriter().write(resultingMovie.toString());
            response.setStatus(500);

            e.printStackTrace();
        }
    }
}