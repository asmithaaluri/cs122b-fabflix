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

@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;
    private static final String callAddStarProcedure = "{CALL add_star(?, ?, ?)}";
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String starName = request.getParameter("star_name");
        String birthYear = request.getParameter("birth_year");

        JsonObject resultingStar = new JsonObject();
        try (Connection connection = dataSource.getConnection();
             CallableStatement addStarCall =
                     connection.prepareCall(callAddStarProcedure)) {

            addStarCall.setString(1, starName);
            if (birthYear != null && !birthYear.isEmpty()) {
                addStarCall.setInt(2, Integer.parseInt(birthYear));
            } else {
                addStarCall.setNull(2, Types.INTEGER);
            }

            addStarCall.registerOutParameter("starId", Types.VARCHAR);
            addStarCall.execute();
            resultingStar.addProperty("starId", addStarCall.getString("starId"));
            resultingStar.addProperty("status", "success");

            response.setContentType("application/json");
            response.getWriter().write(resultingStar.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            resultingStar.addProperty("status", "failure");
            response.setContentType("application/json");
            response.getWriter().write(resultingStar.toString());
            response.setStatus(500);

            e.printStackTrace();
        }
    }
}

