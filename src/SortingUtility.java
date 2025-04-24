public class SortingUtility {
    private static final String tuRuQuery = " ORDER BY m.title, rating";
    private static final String tuRdQuery = " ORDER BY m.title, rating DESC";
    private static final String tdRuQuery = " ORDER BY m.title DESC, rating";
    private static final String tdRdQuery = " ORDER BY m.title DESC, rating DESC";
    private static final String ruTuQuery = " ORDER BY rating, m.title";
    private static final String ruTdQuery = " ORDER BY rating, m.title DESC";
    private static final String rdTuQuery = " ORDER BY rating DESC, m.title";
    private static final String rdTdQuery = " ORDER BY rating DESC, m.title DESC";

    public static void addSortingToQuery(String sort, StringBuilder query) {
        switch (sort) {
            case "tu-ru":
                query.append(tuRuQuery);
                break;
            case "tu-rd":
                query.append(tuRdQuery);
                break;
            case "td-ru":
                query.append(tdRuQuery);
                break;
            case "td-rd":
                query.append(tdRdQuery);
                break;
            case "ru-tu":
                query.append(ruTuQuery);
                break;
            case "ru-td":
                query.append(ruTdQuery);
                break;
            case "rd-tu":
                query.append(rdTuQuery);
                break;
            case "rd-td":
                query.append(rdTdQuery);
                break;
        }
        query.append(" LIMIT ? " +
                     " OFFSET ?;");
    }
}
