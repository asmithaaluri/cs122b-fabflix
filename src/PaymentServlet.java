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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        String totalPrice = request.getParameter("totalPrice");
        HttpSession session = request.getSession();
        session.setAttribute("totalPrice", totalPrice);

    }

}

