import com.google.gson.JsonObject;
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
import java.sql.Connection;
import java.sql.*;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        // default values for session attributes
        session.setAttribute("moviesPerPage", "25");
        session.setAttribute("page", "1");
        session.setAttribute("sort", "tu-rd");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Declare our query statement.
        String query = "SELECT email, password " +
                       "FROM customers " +
                       "WHERE email = ?";
        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

             statement.setString(1, username);

            // Get result set and use try-with-resources so that we don't have to worry about closing it.
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Valid email for username was entered.
                    String correctPassword = rs.getString("password");
                    if (password.equals(correctPassword)) {
                        // Login success:
                        // set this user into the session
                        request.getSession().setAttribute("user", new User(username));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    } else {
                        // User entered incorrect password.
                        responseJsonObject.addProperty("status", "fail");
                        request.getServletContext().log("Login failed"); // Logging to localhost.
                        responseJsonObject.addProperty("message", "Incorrect password.");
                    }
                } else {
                    // Invalid email for username was entered.
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed"); // Logging to localhost.
                    responseJsonObject.addProperty("message", "User " + username + " does not exist.");
                }
            }

        } catch (Exception e) {
            // Database connection or query failed
            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Error:", e);
            responseJsonObject.addProperty("message", e.getMessage());

        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
