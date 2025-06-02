package movies;

import com.google.gson.JsonArray;
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
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import common.JwtUtil;
import io.jsonwebtoken.Claims;

@WebServlet(name = "movies.PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
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
            System.out.println("Invalid token in PlaceOrderServlet doGet");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JsonArray jsonArray = new JsonArray();

        String totalPrice = Optional.ofNullable((String) claims.get("totalPrice")).orElse("0");

        Integer confirmationCounter = Optional.ofNullable((Integer) claims.get("confirmationCounter")).orElse(0);
        Integer customerId = (Integer) claims.get("customerId");

        if (customerId == null) {
            System.out.println("Null customerId when placing and order.");
            return;
        }

        String query = "SELECT  s.id, s.movieId, s.quantity, m.title " +
                        "FROM sales s JOIN movies m ON s.movieId = m.id " +
                        "WHERE customerId = ? " +
                        "ORDER BY id DESC " +
                        "LIMIT ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, customerId);
                statement.setInt(2, confirmationCounter);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int saleId = resultSet.getInt("id");
                        String movieTitle = resultSet.getString("title");
                        int quantity = resultSet.getInt("quantity");

                        JsonObject saleInfoObject = new JsonObject();
                        saleInfoObject.addProperty("saleId", saleId);
                        saleInfoObject.addProperty("movieTitle", movieTitle);
                        saleInfoObject.addProperty("quantity", quantity);
                        jsonArray.add(saleInfoObject);
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        claims.remove("confirmationCounter");
        claims.remove("customerId");


        JsonObject totalPriceObject = new JsonObject();
        totalPriceObject.addProperty("totalPrice", totalPrice);
        jsonArray.add(totalPriceObject);

        String newToken = JwtUtil.generateToken(claims.getSubject(), claims);
        JwtUtil.updateJwtCookie(request, response, newToken);

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        out.write(jsonArray.toString());
        // Set response status to 200 (OK)
        response.setStatus(HttpServletResponse.SC_OK);

    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String creditCardNumber = request.getParameter("credit_card_number");
        String expirationDate = request.getParameter("expiration_date");

        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();

        // query to verify information
        String query = "SELECT * " +
                        "FROM creditcards " +
                        "WHERE id = ? " +
                        "AND firstName = ? " +
                        "AND lastName = ? " +
                        "AND expiration = ?";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);) {

            statement.setString(1, creditCardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, expirationDate);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    responseJsonObject.addProperty("status", "success");

                    // Get customer ID for identification to add to sales table later.
                    int customerId = getCustomerId(connection, creditCardNumber);

                    String token = JwtUtil.getCookieValue(request, "jwtToken");
                    Claims claims = JwtUtil.validateToken(token);

                    if (claims == null) {
                        System.out.println("Invalid token in PlaceOrderServlet doPost");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    // Insert into sales table.
                    claims.put("customerId", customerId); // Create attribute for customerId.
                    int confirmationCounter = addEachMovieFromCartOfCustomerToSalesTable(claims, connection, customerId);
                    claims.put("confirmationCounter", confirmationCounter);

                    // Clear the cart.
                    claims.remove("previousCartItems");
                    claims.remove("totalPrice");

                    String newToken = JwtUtil.generateToken(claims.getSubject(), claims);
                    JwtUtil.updateJwtCookie(request, response, newToken);

                } else {
                    responseJsonObject.addProperty("status", "error");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter out = response.getWriter();
        out.write(responseJsonObject.toString());
        response.setStatus(HttpServletResponse.SC_OK);

    }

    /**
     * Returns the customer id.
     */
    private int getCustomerId(Connection connection, String creditCardNumber) {
        int customerId = -1;
        String customerIdQuery = "SELECT id " +
                "FROM customers c " +
                "WHERE c.ccld = ?";

        try (PreparedStatement customerIdStatement = connection.prepareStatement(customerIdQuery)) {
            customerIdStatement.setString(1, creditCardNumber);

            try (ResultSet rs = customerIdStatement.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("id");
                }
            }

        } catch (Exception e) {
            System.out.println("Unable to create customerIdStatement");
            e.printStackTrace();
        }
        return customerId;
    }

    /**
     * Populates sales table of database with movie purchases of customer.
     * @param connection
     * @param customerId
     * @return confirmationCounter
     */
    private int addEachMovieFromCartOfCustomerToSalesTable(Claims claims, Connection connection, int customerId) {
        ArrayList<String> previousCartItems = claims.get("previousCartItems", ArrayList.class);
        int confirmationCounter = 0;

        if (previousCartItems == null) {
            System.out.println("Cart is empty");
            return confirmationCounter;
        }

        Map<String, Integer> movieOccurrencesInCart = new HashMap<>();
        for (String movie_id : previousCartItems) {
            movieOccurrencesInCart.put(movie_id, movieOccurrencesInCart.getOrDefault(movie_id, 0) + 1);
        }

        java.sql.Date saleDate = java.sql.Date.valueOf(LocalDate.now());
        String insertIntoSalesQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) " +
                                      "VALUES (?, ?, ?, ?)";

        try (PreparedStatement insertIntoSalesStatement = connection.prepareStatement(insertIntoSalesQuery)) {
            for (Map.Entry<String, Integer> entry : movieOccurrencesInCart.entrySet()) {
                String movieId = entry.getKey();
                int quantity = entry.getValue();
                insertIntoSalesStatement.setInt(1, customerId);
                insertIntoSalesStatement.setString(2, movieId);
                insertIntoSalesStatement.setDate(3, saleDate);
                insertIntoSalesStatement.setInt(4, quantity);
                insertIntoSalesStatement.executeUpdate(); // Makes the change in the database.
                confirmationCounter++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return confirmationCounter;
    }
}

