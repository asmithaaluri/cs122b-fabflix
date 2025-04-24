import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SearchUtility {
    public static boolean addSearchClausesToQuery(StringBuilder query,
                                           String pstar,
                                           String ptitle,
                                           String pyear,
                                           String pdirector) {
        int previousWhereClause = 0;
        boolean validYear = false;
        if (pstar != null && !pstar.trim().isEmpty()) {
            query.append("JOIN stars_in_movies sm " +
                         "ON m.id = sm.movieId " +
                         "JOIN stars s " +
                         "ON s.id = sm.starId " +
                         "WHERE s.name LIKE ?");
            previousWhereClause = 1;
        }
        if (ptitle != null && !ptitle.trim().isEmpty()) {
            if (previousWhereClause == 1) {
                query.append(" AND m.title LIKE ?");
            } else {
                query.append("WHERE m.title LIKE ?");
                previousWhereClause = 1;
            }
        }
        if (pyear != null && !pyear.trim().isEmpty()) {
            try {
                Integer.parseInt(pyear);
                validYear = true;
                if (previousWhereClause == 1) {
                    query.append(" AND m.year = ?");
                } else {
                    query.append("WHERE m.year = ?");
                    previousWhereClause = 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (pdirector != null && !pdirector.trim().isEmpty()) {
            if (previousWhereClause == 1) {
                query.append(" AND m.director LIKE ?");
            } else {
                query.append("WHERE m.director LIKE ?");
            }
        }
        return validYear;
    }

    public static int addSearchParamsToQuery(
            PreparedStatement statement,
            String pstar,
            String ptitle,
            String pyear,
            String pdirector,
            boolean validYear
    ) throws SQLException {
        int index = 0;
        if (pstar != null && !pstar.trim().isEmpty()) {
            index++;
            statement.setString(index, "%" + pstar + "%");
        }
        if (ptitle != null && !ptitle.trim().isEmpty()) {
            index++;
            statement.setString(index, "%" + ptitle + "%");
        }
        if (pyear != null && !pyear.trim().isEmpty() && validYear) {
            index++;
            statement.setInt(index, Integer.parseInt(pyear));
        }
        if (pdirector != null && !pdirector.trim().isEmpty()) {
            index++;
            statement.setString(index, "%" + pdirector + "%");
        }
        return index;
    }
}
