import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);

        ArrayList<String> previousCartItems = (ArrayList<String>) session.getAttribute("previousCartItems");
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
        System.out.println(movie_id);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        ArrayList<String> previousCartItems = (ArrayList<String>) session.getAttribute("previousCartItems");
        if (previousCartItems == null) {
            previousCartItems = new ArrayList<String>();
            previousCartItems.add(movie_id);
            session.setAttribute("previousCartItems", previousCartItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousCartItems) {
                previousCartItems.add(movie_id);
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousCartItemsJsonArray = new JsonArray();
        previousCartItems.forEach(previousCartItemsJsonArray::add);
        responseJsonObject.add("previousCartItems", previousCartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}