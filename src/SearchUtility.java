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
                        "WHERE s.name LIKE ? OR ")
                    .append(buildEditDistanceClause("s.name", pstar));
            previousWhereClause = 1;
        }
        if (ptitle != null && !ptitle.isEmpty()) {
            if (previousWhereClause == 1) {
                query.append(" AND MATCH(m.title) AGAINST (? IN BOOLEAN MODE) OR ");
            } else {
                query.append("WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) OR ");
                previousWhereClause = 1;
            }
            query.append(buildEditDistanceClause("m.title", ptitle));
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
                query.append(" AND m.director LIKE ? OR ");
            } else {
                query.append("WHERE m.director LIKE ? OR ");
            }
            query.append(buildEditDistanceClause("m.director", pdirector));
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
            index++;
            statement.setString(index, pstar);
        }
        if (ptitle != null && !ptitle.isEmpty()) {
            index++;
            statement.setString(index, buildMovieTitleFullTextSearchQueryParams(ptitle));
            index++;
            statement.setString(index, ptitle);
        }
        if (pyear != null && !pyear.isEmpty() && validYear) {
            index++;
            statement.setInt(index, Integer.parseInt(pyear));
        }
        if (pdirector != null && !pdirector.isEmpty()) {
            index++;
            statement.setString(index, "%" + pdirector + "%");
            index++;
            statement.setString(index, pdirector);
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

    public static String buildEditDistanceClause(String columnName, String columnValue) {
//        int allowedEditDistance = columnValue.length() / 6 + 1;
//        return "edth(LOWER(" + columnName + "), LOWER(?), " +
//                allowedEditDistance + ") ";
        return "LOWER(?) != '' ";
    }
}
