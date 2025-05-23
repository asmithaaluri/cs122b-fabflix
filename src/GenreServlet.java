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
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()
        ) {
            String query = "SELECT id, name " +
                           "FROM genres " +
                           "ORDER BY name ASC";

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
                out.write(jsonArray.toString());
                response.setStatus(200);
            }
            out.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}