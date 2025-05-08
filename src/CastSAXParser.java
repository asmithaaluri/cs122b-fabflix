
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastSAXParser extends DefaultHandler {
    private Map<String, Star> starsFromActors; // Key will be starName, value is starObject.
    private String tempVal;
    private Star tempStar;
    private Map<String, Star> starsInBothActorsAndCasts;
    private ArrayList<Star> inconsistentStarsNotInActors;
    private ArrayList<Star> inconsistentStarsNotInCasts;
    private HashSet<String> movieIdsWithActors;

    public CastSAXParser(Map<String, Star> starsFromActors) {
        this.starsFromActors = starsFromActors;
        this.starsInBothActorsAndCasts = new HashMap<String, Star>();
        this.inconsistentStarsNotInActors = new ArrayList<Star>();
        this.inconsistentStarsNotInCasts = new ArrayList<Star>();
        this.movieIdsWithActors = new HashSet<String>();
    }

    public void runExample() {
        parseDocument();
        // printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("casts124.xml", this);
            identifyInconsistentStarsNotInCasts();

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void identifyInconsistentStarsNotInCasts() {
        this.inconsistentStarsNotInCasts =
                starsFromActors
                        .entrySet()
                        .stream()
                        .filter(star -> !starsInBothActorsAndCasts.containsKey(star.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    public void printData() {
        System.out.println("========== STAR INFORMATION AFTER PARSING CASTS ==========");
        int inconsistentStarsSize = inconsistentStarsNotInActors.size() + inconsistentStarsNotInCasts.size();
        System.out.printf("%40s%d%n", "TOTAL NUMBER OF INCONSISTENT STARS: ", inconsistentStarsSize);
        System.out.printf("%40s%d%n", "  Stars only in Actors: ", inconsistentStarsNotInCasts.size());
        System.out.printf("%40s%d%n", "  Stars only in Casts: ", inconsistentStarsNotInActors.size());
    }

    public void writeInconsistentStarOutputToFile() {
        try {
            File file = new File("inconsistent_cast_info.txt");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("=============================================\n");
                writer.write("                                             \n");
                writer.write("========== INCONSISTENT STAR INFO ==========\n");
                writer.write("                                             \n");
                writer.write("=============================================\n");
                writer.write("========== NOT IN CASTS ==========\n");
                for (Star star : starsFromActors.values()) {
                    writer.write(star.toString() + "\n");
                }
                writer.write("========== NOT IN ACTORS ==========\n");
                for (Star star : inconsistentStarsNotInActors) {
                    writer.write(star.toString() + "\n");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: Cannot write to newly created file inconsistent_casts_info.txt.");
            e.printStackTrace();
        }

    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            tempStar = new Star(null, null, -1);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            updateMapOfStarsWithMovieIds();
        } else if (qName.equalsIgnoreCase("a")) {
            if (!tempVal.trim().isEmpty()) {
                tempStar.setName(tempVal.trim());
            }
        } else if (qName.equalsIgnoreCase("f")) {
            if (!tempVal.trim().isEmpty()) {
                tempStar.addMovieId(tempVal.trim());
            }
        }

    }

    private void updateMapOfStarsWithMovieIds() {
        String starName = tempStar.getName();
        if (starName != null &&
                !starName.trim().isEmpty() &&
                starsFromActors.containsKey(handleCaseSensitivity(starName))) {
            Star starAlreadyInMap = starsInBothActorsAndCasts.get(handleCaseSensitivity(starName));
            if (starAlreadyInMap != null) {
                for (String movieId : tempStar.getMovieIds()) {
                    addMovieIdToSet(movieId);
                    starAlreadyInMap.addMovieId(movieId);
                }
            } else {
                for (String movieId : tempStar.getMovieIds()) {
                    addMovieIdToSet(movieId);
                }
                starsInBothActorsAndCasts.put(handleCaseSensitivity(starName), tempStar);
            }
        } else {
            inconsistentStarsNotInActors.add(tempStar);
        }
    }

    private String handleCaseSensitivity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return input.trim().toLowerCase();
    }

    private void addMovieIdToSet(String movieId) {
        movieIdsWithActors.add(movieId);
    }

    public Map<String, Star> getStarMap() {
        return this.starsInBothActorsAndCasts;
    }

    public HashSet<String> getMovieIdsWithActors() { return this.movieIdsWithActors; }

    public static void main(String[] args) {
        ActorSAXParser actorSPE = new ActorSAXParser();
        actorSPE.runExample();

        CastSAXParser castSPE = new CastSAXParser(actorSPE.getStarMap());
        castSPE.runExample();
    }

}