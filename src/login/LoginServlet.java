package login;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.jasypt.util.password.StrongPasswordEncryptor;
import common.JwtUtil;
import java.util.Date;

@WebServlet(name = "login.LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJsonObject = new JsonObject();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("LoginServlet: login request for user " + username);

        // Declare our query statement.
        String query = "SELECT email, password " +
                       "FROM customers " +
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

                        // use username as subject of JWT
                        String subject = username;
                        // store user login time in JWT
                        Map<String, Object> claims = new HashMap<>();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        claims.put("loginTime", dateFormat.format(new Date()));

                        // default values for session attributes
                        claims.put("moviesPerPage", "25");
                        claims.put("page", "1");
                        claims.put("sort", "tu-rd");

                        // Generate new JWT and add it to Header
                        String token = JwtUtil.generateToken(subject, claims);
                        JwtUtil.updateJwtCookie(request, response, token);


                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    } else {
                        // login.User entered incorrect password.
                        responseJsonObject.addProperty("status", "fail");
                        request.getServletContext().log("Login failed"); // Logging to localhost.
                        responseJsonObject.addProperty("message", "Incorrect password.");
                    }
                } else {
                    // Invalid email for username was entered.
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed"); // Logging to localhost.
                    responseJsonObject.addProperty("message", "login.User " + username + " does not exist.");
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
