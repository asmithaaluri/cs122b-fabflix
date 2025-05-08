import java.util.ArrayList;

public class Movie {

    private String id;

    private String title;

    private String year;

    private String director;

    private ArrayList<String> genres;

    public Movie(String id, String title, String year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = new ArrayList<>();
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }

    public void setYear(String year) { this.year = year; }

    public String getDirector() { return director; }

    public void setDirector(String director) { this.director = director; }

    public ArrayList<String> getGenres() { return genres; }

    public void addGenre(String genre) {
        if (genre != null && !genre.trim().isEmpty()) {
            this.genres.add(genre.trim());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("ID: " + getId());
        sb.append(", ");
        sb.append("Title: " + getTitle());
        sb.append(", ");
        sb.append("Year: " + getYear());
        sb.append(", ");
        sb.append("Director: " + getDirector());
        sb.append(", ");
        sb.append("Genres: " + String.join(", ", genres));
        sb.append(".");

        return sb.toString();
    }

}
