package movies;

import java.util.HashSet;
import java.util.Set;

public class Star {

    private String id;

    private String name;

    private int birthYear;

    private Set<String> movieIds;

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
        this.movieIds = new HashSet<>();
    }

    public String getId() { return this.id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public int getBirthYear() { return this.birthYear; }

    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    public Set<String> getMovieIds() { return this.movieIds; }

    public void addMovieId(String movieId) {
        if (movieId != null && !movieId.trim().isEmpty()) {
            this.movieIds.add(movieId.trim());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("movies.Star Details - ");
        sb.append("movies.Star ID: " + getId());
        sb.append(", ");
        sb.append("Name: " + getName());
        sb.append(", ");
        sb.append("Birth Year: " + getBirthYear());
        sb.append(", ");
        sb.append("MovieIDs: " + String.join(", ", movieIds));
        sb.append(".");

        return sb.toString();
    }

}
