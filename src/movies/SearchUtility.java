package movies;

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
        if (pstar != null && !pstar.isEmpty()) {
            query.append("JOIN stars_in_movies sm " +
                        "ON m.id = sm.movieId " +
                        "JOIN stars s " +
                        "ON s.id = sm.starId " +
                        "WHERE s.name LIKE ?");
            previousWhereClause = 1;
        }
        if (ptitle != null && !ptitle.isEmpty()) {
            if (previousWhereClause == 1) {
                query.append(" AND MATCH(m.title) AGAINST (? IN BOOLEAN MODE)");
            } else {
                query.append("WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE)");
                previousWhereClause = 1;
            }
        }
        if (pyear != null && !pyear.isEmpty()) {
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
        if (pdirector != null && !pdirector.isEmpty()) {
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
        if (pstar != null && !pstar.isEmpty()) {
            index++;
            statement.setString(index, "%" + pstar + "%");
        }
        if (ptitle != null && !ptitle.isEmpty()) {
            index++;
            statement.setString(index, buildMovieTitleFullTextSearchQueryParams(ptitle));
        }
        if (pyear != null && !pyear.isEmpty() && validYear) {
            index++;
            statement.setInt(index, Integer.parseInt(pyear));
        }
        if (pdirector != null && !pdirector.isEmpty()) {
            index++;
            statement.setString(index, "%" + pdirector + "%");
        }
        return index;
    }

    public static String buildMovieTitleFullTextSearchQueryParams(String movieTitles) {
        String[] tokens = movieTitles.split(" ");
        StringBuilder queryParams = new StringBuilder();
        for (String token : tokens) {
            queryParams.append("+").append(token).append("* ");
        }
        return queryParams.toString();
    }
}
