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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
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
        HttpSession session = request.getSession();
        JsonArray jsonArray = new JsonArray();

        String totalPrice = (String) session.getAttribute("totalPrice");
        if (totalPrice == null) {
            totalPrice = "0";
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("totalPrice", totalPrice);
        jsonArray.add(jsonObject);

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
                    System.out.println("Credit card form input valid.");
                    responseJsonObject.addProperty("status", "success");

                    // Get customer ID for identification to add to sales table later.
                    int customerId = getCustomerId(connection, creditCardNumber);
                    System.out.println("Customer ID: " + customerId);

                    // Insert into sales table.
                    HttpSession session = request.getSession();
                    addEachMovieFromCartOfCustomerToSalesTable(session, connection, customerId);
                    System.out.println("Inserted into sales database");

                    // Clear the cart.
                    session.removeAttribute("previousCartItems");
                    session.removeAttribute("totalPrice");

                } else {
                    System.out.println("Credit card form input invalid.");
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
     */
    private void addEachMovieFromCartOfCustomerToSalesTable(HttpSession session, Connection connection, int customerId) {
        ArrayList<String> previousCartItems = (ArrayList<String>) session.getAttribute("previousCartItems");

        if (previousCartItems == null) {
            System.out.println("Cart is empty");
            return;
        }

        java.sql.Date saleDate = java.sql.Date.valueOf(LocalDate.now());
        String insertIntoSalesQuery = "INSERT INTO sales (customerId, movieId, saleDate) " +
                                      "VALUES (?, ?, ?)";

        try (PreparedStatement insertIntoSalesStatement = connection.prepareStatement(insertIntoSalesQuery)) {
            for (String movieId : previousCartItems) {
                insertIntoSalesStatement.setInt(1, customerId);
                insertIntoSalesStatement.setString(2, movieId);
                insertIntoSalesStatement.setDate(3, saleDate);
                insertIntoSalesStatement.executeUpdate(); // Makes the change in the database.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

