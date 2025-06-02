package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet(name = "movies.PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        try (PrintWriter out = response.getWriter()) {
            String token = JwtUtil.getCookieValue(request, "jwtToken");
            Claims claims = JwtUtil.validateToken(token);

            if (claims == null) {
                System.out.println("Invalid token in Payment Servlet doGet");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String totalPrice = Optional.ofNullable((String) claims.get("totalPrice")).orElse("0");

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("totalPrice", totalPrice);
            jsonArray.add(jsonObject);

            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(HttpServletResponse.SC_OK);
        }

    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String totalPrice = request.getParameter("totalPrice");

        String token = JwtUtil.getCookieValue(request, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);

        if (claims == null) {
            System.out.println("Invalid token in Payment Servlet doPost");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        claims.put("totalPrice", totalPrice);

        String newToken = JwtUtil.generateToken(claims.getSubject(), claims);
        JwtUtil.updateJwtCookie(request, response, newToken);

    }

}

