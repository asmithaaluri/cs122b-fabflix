package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.JwtUtil;
import io.jsonwebtoken.Claims;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@WebServlet(name = "movies.ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = JwtUtil.getCookieValue(request, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);

        if (claims == null) {
            System.out.println("Invalid token in ShoppingCartServlet doGet");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        ArrayList<String> previousCartItems = claims.get("previousCartItems", ArrayList.class);
        if (previousCartItems == null) {
            previousCartItems = new ArrayList<String>();
        }

        JsonArray previousCartItemsJsonArray = new JsonArray();
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String query = "SELECT id, title FROM movies WHERE id = ?";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            Map<String, Integer> movieOccurrencesInCart = new HashMap<>();
            for (String movie_id : previousCartItems) {
                movieOccurrencesInCart.put(movie_id, movieOccurrencesInCart.getOrDefault(movie_id, 0) + 1);
            }

            for (String movie_id : movieOccurrencesInCart.keySet()) {
                statement.setString(1, movie_id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        Double price = 1.00;
                        Integer quantity = movieOccurrencesInCart.get(movie_id);

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", movie_id);
                        jsonObject.addProperty("title", title);
                        jsonObject.addProperty("quantity", quantity);
                        jsonObject.addProperty("price", price);

                        previousCartItemsJsonArray.add(jsonObject);
                    }
                }
            }

            // Log to localhost log
            request.getServletContext().log("getting " + previousCartItemsJsonArray.size() + " results");

            // Write JSON string to output
            out.write(previousCartItemsJsonArray.toString());
            // Set response status to 200 (OK)
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

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_id = request.getParameter("movie_id");
        String action = request.getParameter("action");

        String token = JwtUtil.getCookieValue(request, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);

        if (claims == null) {
            System.out.println("Invalid token in ShoppingCartServlet doPost");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get the previous items in a ArrayList
        ArrayList<String> previousCartItems = claims.get("previousCartItems", ArrayList.class);
        if (previousCartItems == null) {
            previousCartItems = new ArrayList<String>();
            previousCartItems.add(movie_id);
        } else if (action.equals("increase-movie-count")) {
            previousCartItems.add(movie_id);
        } else if (action.equals("decrease-movie-count")) {
            previousCartItems.remove(movie_id);
        } else if (action.equals("delete-movie-from-cart")) {
            Iterator<String> iterator = previousCartItems.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(movie_id)) {
                    iterator.remove();
                }
            }
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousCartItems) {
                previousCartItems.add(movie_id);
            }
        }

        claims.put("previousCartItems", previousCartItems);
        String newToken = JwtUtil.generateToken(claims.getSubject(), claims);
        JwtUtil.updateJwtCookie(request, response, newToken);

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousCartItemsJsonArray = new JsonArray();
        previousCartItems.forEach(previousCartItemsJsonArray::add);
        responseJsonObject.add("previousCartItems", previousCartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}

