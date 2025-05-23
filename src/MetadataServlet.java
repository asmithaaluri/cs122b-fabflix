import com.google.gson.JsonArray;
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
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MetadataServlet", urlPatterns = "/_dashboard/api/metadata")
public class MetadataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    private static final String databaseTablesQuery = "SHOW TABLES;";
    private static final String tableColumnsQuery =
                    "SELECT COLUMN_NAME, DATA_TYPE " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE table_name = ? AND table_schema = 'moviedb';";

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()
        ) {
            try (ResultSet rs = statement.executeQuery(databaseTablesQuery)) {
                JsonArray jsonArray = new JsonArray();
                while (rs.next()) {
                    String tableName = rs.getString("Tables_in_moviedb");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("table_name", tableName);

                    try (PreparedStatement preparedStatement = conn.prepareStatement(tableColumnsQuery)) {
                        preparedStatement.setString(1, tableName);
                        try (ResultSet tableColumns = preparedStatement.executeQuery()) {

                            JsonArray columnArray = new JsonArray();

                            while (tableColumns.next()) {
                                JsonObject columnObject = new JsonObject();
                                String columnName = tableColumns.getString("column_name");
                                String columnType = tableColumns.getString("data_type");
                                columnObject.addProperty("column_name", columnName);
                                columnObject.addProperty("column_type", columnType);
                                columnArray.add(columnObject);
                            }
                        jsonObject.add("columns", columnArray);
                        jsonArray.add(jsonObject);
                        }
                    }
                }
                out.write(jsonArray.toString());
                response.setStatus(200);
            }
            out.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}