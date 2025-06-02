package movies;

import java.util.HashMap;
import java.util.Map;

public class GenreXMLTranslations {
    public static final Map<String, String> GENRE_MAP = new HashMap<>();

    static {
        GENRE_MAP.put("Actn", "Violence");
        GENRE_MAP.put("Comd", "Comedy");
        GENRE_MAP.put("Disa", "Disaster");
        GENRE_MAP.put("Epic", "Epic");
        GENRE_MAP.put("Horr", "Horror");
        GENRE_MAP.put("Noir", "Black");
        GENRE_MAP.put("ScFi", "Sci-Fi");
        GENRE_MAP.put("S.F.", "Sci-Fi");
        GENRE_MAP.put("West", "Western");
        GENRE_MAP.put("Advt", "Adventure");
        GENRE_MAP.put("Cart", "Cartoon");
        GENRE_MAP.put("Docu", "Documentary");
        GENRE_MAP.put("Faml", "Family");
        GENRE_MAP.put("Musc", "Musical");
        GENRE_MAP.put("Porn", "Pornography");
        GENRE_MAP.put("Dram", "Drama");
        GENRE_MAP.put("Hist", "History");
        GENRE_MAP.put("Myst", "Mystery");
        GENRE_MAP.put("Romt", "Romantic");
        GENRE_MAP.put("Susp", "Thriller");
        GENRE_MAP.put("CnR", "Cops and Robbers");
        GENRE_MAP.put("BioP", "Biographical Picture");
        GENRE_MAP.put("TV", "TV Show");
        GENRE_MAP.put("TVs", "TV Series");
        GENRE_MAP.put("TVm", "TV Miniseries");
    }
}
