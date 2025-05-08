
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieSAXParser extends DefaultHandler {
    private ArrayList<Movie> movies;
    private ArrayList<Movie> inconsistentMoviesWithDuplicateMovieId;
    private ArrayList<Movie> inconsistentMoviesWithEmptyMovieTitle;
    private ArrayList<Movie> inconsistentMoviesWithInvalidMovieYear;
    private ArrayList<Movie> inconsistentMoviesWithUnknownMovieDirector;
    private ArrayList<Movie> inconsistentMoviesWithNoStars;
    private HashSet<String> movieIds;
    private HashSet<String> movieIdsWithActors;
    private String tempVal;
    private Movie tempMovie;
    private String directorName;
    private Map<String, Star> starMap;
    ActorSAXParser actorSPE;
    CastSAXParser castSPE;

    public MovieSAXParser() {
        this.movies = new ArrayList<Movie>();
        this.movieIds = new HashSet<>();
        this.movieIdsWithActors = new HashSet<>();
        this.starMap = new HashMap<>();
        this.actorSPE = null;
        this.castSPE = null;

        this.inconsistentMoviesWithDuplicateMovieId = new ArrayList<Movie>();
        this.inconsistentMoviesWithEmptyMovieTitle = new ArrayList<Movie>();
        this.inconsistentMoviesWithInvalidMovieYear = new ArrayList<Movie>();
        this.inconsistentMoviesWithUnknownMovieDirector = new ArrayList<Movie>();
        this.inconsistentMoviesWithNoStars = new ArrayList<Movie>();
    }

    public void runExample() {
        this.actorSPE = new ActorSAXParser();
        actorSPE.runExample();

        // Run the parser for cast so that we can keep track of all the movieIds of movies that do have actors.
        this.castSPE = new CastSAXParser(this.actorSPE.getStarMap());
        castSPE.runExample();
        movieIdsWithActors = castSPE.getMovieIdsWithActors();
        this.starMap = castSPE.getStarMap();

        parseDocument();
        compareMovieIdsWithMovieIdsWithActors();
        updateStarMap();
    }


    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {
        System.out.println("========= XML PARSING COMPLETED =========");
        System.out.println("========== MOVIE INFORMATION ==========");
        System.out.printf("%40s%d%n", "TOTAL NUMBER OF MOVIES INSERTED: ", movies.size());
        int inconsistentMoviesSize = inconsistentMoviesWithDuplicateMovieId.size() + inconsistentMoviesWithEmptyMovieTitle.size() + inconsistentMoviesWithInvalidMovieYear.size() + inconsistentMoviesWithUnknownMovieDirector.size() + inconsistentMoviesWithNoStars.size();
        System.out.printf("%40s%d%n", "TOTAL NUMBER OF INCONSISTENT MOVIES: ", inconsistentMoviesSize);
        System.out.printf("%40s%d%n", "  Duplicate Movie Id: ", inconsistentMoviesWithDuplicateMovieId.size());
        System.out.printf("%40s%d%n", "  Empty Movie Title: ", inconsistentMoviesWithEmptyMovieTitle.size());
        System.out.printf("%40s%d%n", "  Invalid Year Format: ", inconsistentMoviesWithInvalidMovieYear.size());
        System.out.printf("%40s%d%n", "  Unknown Director: ", inconsistentMoviesWithUnknownMovieDirector.size());
        System.out.printf("%40s%d%n", "  Movies without Actors: ", inconsistentMoviesWithNoStars.size());

    }

    public void displaySummaryToUserAndWriteInconsistencyReports(int totalStarInsertions) {
        printData();
        actorSPE.printData(totalStarInsertions);
        castSPE.printData();

        writeInconsistentMovieOutputToFile();
        actorSPE.writeInconsistentStarOutputToFile();
        castSPE.writeInconsistentStarOutputToFile();
    }

    public void writeInconsistentMovieOutputToFile() {
        try {
            File file = new File("inconsistent_movie_info.txt");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("=============================================\n");
                writer.write("                                             \n");
                writer.write("========== INCONSISTENT MOVIE INFO ==========\n");
                writer.write("                                             \n");
                writer.write("=============================================\n");
                writer.write("========== DUPLICATE IDS ==========\n");
                for (Movie movie : inconsistentMoviesWithDuplicateMovieId) {
                    writer.write(movie.toString());
                }
                writer.write("========== EMPTY TITLES ==========\n");
                for (Movie movie : inconsistentMoviesWithEmptyMovieTitle) {
                    writer.write(movie.toString() + "\n");
                }
                writer.write("========== INVALID YEARS ==========\n");
                for (Movie movie : inconsistentMoviesWithInvalidMovieYear) {
                    writer.write(movie.toString() + "\n");
                }
                writer.write("========== UNKNOWN DIRECTORS ==========\n");
                for (Movie movie : inconsistentMoviesWithUnknownMovieDirector) {
                    writer.write(movie.toString() + "\n");
                }
                writer.write("========== NO STARS =============\n");
                for (Movie movie : inconsistentMoviesWithNoStars) {
                    writer.write(movie.toString() + "\n");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: Cannot write to newly created file inconsistent_movie_info.txt.");
            e.printStackTrace();
        }

    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie(null, null, null, null);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirname")) {
            directorName = tempVal.trim();
        }
        else if (qName.equalsIgnoreCase("film")) {
            boolean movieIsInconsistent = checkIfMovieHasInconsistencies(tempMovie);
            if (!movieIsInconsistent) {
                movies.add(tempMovie);
            }
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal.trim());
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal.trim());
        } else if (qName.equalsIgnoreCase("dirn")) {
            if (tempMovie.getDirector() == null) {
                tempMovie.setDirector(directorName);
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            String genreFormattedCorrectly = tempVal.trim();
            String genreValueFromMap = GenreXMLTranslations.GENRE_MAP.get(genreFormattedCorrectly);
            if (!genreFormattedCorrectly.isEmpty()) {
                tempMovie.addGenre(genreValueFromMap);
            }
        }

    }

    private boolean checkIfMovieHasInconsistencies(Movie tempMovie) {
        if (movieIds.contains(tempMovie.getId())) { // Duplicate movie.
            inconsistentMoviesWithDuplicateMovieId.add(tempMovie);
            return true;
        }
        if (tempMovie.getTitle() == "" || tempMovie.getTitle() == null) {
            inconsistentMoviesWithEmptyMovieTitle.add(tempMovie);
            return true;
        }
        boolean isValidYear = checkValidYear(tempMovie.getYear());
        if (!isValidYear) {
            inconsistentMoviesWithInvalidMovieYear.add(tempMovie);
            return true;
        }
        boolean isValidDirector = checkValidDirector(tempMovie.getDirector());
        if (!isValidDirector) {
            inconsistentMoviesWithUnknownMovieDirector.add(tempMovie);
            return true;
        }
        if (!movieIdsWithActors.contains(tempMovie.getId())) { // These movies don't have actors.
            inconsistentMoviesWithNoStars.add(tempMovie);
            return true;
        }

        movieIds.add(tempMovie.getId());
        return false;
    }

    private boolean checkValidYear(String tempVal) {
        try {
            int year = Integer.parseInt(tempVal);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private boolean checkValidDirector(String movieDirectorName) {
        if (movieDirectorName == null || movieDirectorName.startsWith("Unknown") || movieDirectorName.startsWith("UnYear")) {
            return false;
        }

        return true;
    }

    public HashSet<String> getMovieIds() { return this.movieIds; }

    private void compareMovieIdsWithMovieIdsWithActors() {
        // Remove movies that have actors but don't appear in mains243.xml because the movie doesn't have a unique identifier.
        Iterator<String> iterator = movieIdsWithActors.iterator();
        while (iterator.hasNext()) {
            String movieId = iterator.next();
            if (!movieIds.contains(movieId)) {
                iterator.remove();
            }
        }
    }

    private void updateStarMap() {
        Iterator<Map.Entry<String,Star>> iterator = starMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Star> entry = iterator.next();
            String starName = entry.getKey();

            if (starName == null) { // Remove stars whose names are null.
                iterator.remove();
                continue;
            }

            Set<String> movieIdsOfStar = entry.getValue().getMovieIds();

            movieIdsOfStar.removeIf(id -> !movieIdsWithActors.contains(id)); // Remove movieIds of stars in the map where the movieId didn't appear in mains243.xml.

            if (movieIdsOfStar.isEmpty()) { // Remove the starName key in the map if the star didn't act in any movies after the movieId filtering above,
                iterator.remove();
            } else {
                // Assign the star an id here?
            }
        }

    }

    public ArrayList<Movie> getMovies() {
        return this.movies;
    }

    public ArrayList<Movie> getInconsistentMoviesWithDuplicateMovieId() {
        return this.inconsistentMoviesWithDuplicateMovieId;
    }

    public ArrayList<Movie> getInconsistentMoviesWithEmptyMovieTitle() {
        return this.inconsistentMoviesWithEmptyMovieTitle;
    }

    public ArrayList<Movie> getInconsistentMoviesWithInvalidMovieYear() {
        return this.inconsistentMoviesWithInvalidMovieYear;
    }

    public ArrayList<Movie> getInconsistentMoviesWithUnknownMovieDirector() {
        return this.inconsistentMoviesWithUnknownMovieDirector;
    }

    public ArrayList<Movie> getInconsistentMoviesWithNoStars() {
        return this.inconsistentMoviesWithNoStars;
    }

    public Map<String, Star> getStarMap() {
        return this.starMap;
    }

    public static void main(String[] args) {
        MovieSAXParser spe = new MovieSAXParser();
        spe.runExample();
    }

}