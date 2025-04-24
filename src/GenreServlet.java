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
import java.sql.*;

@WebServlet(name = "GenreServlet", urlPatterns = "/api/genres")
public class GenreServlet extends HttpServlet {
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
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()
        ) {
            String query = "SELECT id, name " +
                           "FROM genres " +
                           "ORDER BY name ASC";

            // Perform the query
            try (ResultSet rs = statement.executeQuery(query)) {
                JsonArray jsonArray = new JsonArray();
                while (rs.next()) {
                    Integer genreId = rs.getInt("id");
                    String genreName = rs.getString("name");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("genre_id", genreId);
                    jsonObject.addProperty("genre_name", genreName);

                    jsonArray.add(jsonObject);

                }
                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            out.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}