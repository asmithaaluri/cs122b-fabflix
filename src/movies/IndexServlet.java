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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This movies.IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "movies.IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Claims claims = (Claims) request.getAttribute("claims");

        JsonObject responseJsonObject = new JsonObject();

        String username = claims.getSubject();
        responseJsonObject.addProperty("username", username);

        ArrayList<String> previousCartItems = claims.get("previousCartItems", ArrayList.class);
        if (previousCartItems == null) {
            previousCartItems = new ArrayList<String>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousCartItems.size() + " items");

        JsonArray previousCartItemsJsonArray = new JsonArray();
        previousCartItems.forEach(previousCartItemsJsonArray::add);
        responseJsonObject.add("previousCartItems", previousCartItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_id = request.getParameter("movie_id");

        Claims claims = (Claims) request.getAttribute("claims");

        // get the previous items in a ArrayList
        ArrayList<String> previousCartItems = claims.get("previousCartItems", ArrayList.class);
        if (previousCartItems == null) {
            previousCartItems = new ArrayList<String>();
            previousCartItems.add(movie_id);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousCartItems) {
                previousCartItems.add(movie_id);
            }
        }

        Map<String, Object> updatedClaims = new HashMap<>(claims);
        updatedClaims.put("previousCartItems", previousCartItems);
        String newToken = JwtUtil.generateToken(claims.getSubject(), updatedClaims);
        JwtUtil.updateJwtCookie(request, response, newToken);

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousCartItemsJsonArray = new JsonArray();
        previousCartItems.forEach(previousCartItemsJsonArray::add);
        responseJsonObject.add("previousCartItems", previousCartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}