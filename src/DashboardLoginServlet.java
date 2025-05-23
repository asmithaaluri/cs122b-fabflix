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
import java.sql.Connection;
import java.sql.*;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/_dashboard/api/login")
public class DashboardLoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "failure");
            responseJsonObject.addProperty("message", "Recaptcha verification failed. Please try again.");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String query = "SELECT email, password " +
                        "FROM employees " +
                        "WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, username);

            // Get result set and use try-with-resources so that we don't have to worry about closing it.
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Valid email for username was entered.
                    String correctEncryptedPassword = rs.getString("password");
                    if (new StrongPasswordEncryptor().checkPassword(password, correctEncryptedPassword)) {
                        // Login success:
                        // set this user into the session
                        request.getSession().setAttribute("employee", new Employee(username));

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