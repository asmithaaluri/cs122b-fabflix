package movies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class ActorSAXParser extends DefaultHandler {
    private Map<String, Star> starMap; // Key will be starName, value is starObject.
    private Star tempStar;
    private String tempVal;
    private String tempStarName;
    private int tempStarBirthYear;
    private ArrayList<Star> inconsistentStarsWithEmptyName;
    private ArrayList<Star> inconsistentStarsWithDifferentBirthYear;


    public ActorSAXParser() {
        this.starMap = new HashMap<String, Star>();
        this.inconsistentStarsWithEmptyName = new ArrayList<Star>();
        this.inconsistentStarsWithDifferentBirthYear = new ArrayList<Star>();
    }

    public Map<String, Star> getStarMap() {
        return this.starMap;
    }

    public void runExample() {
        parseDocument();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("actors63.xml", this);

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
    public void printData(int totalStarInsertions) {
        System.out.println("========== STAR INFORMATION AFTER PARSING ACTORS ==========");
        System.out.printf("%40s%d%n", "TOTAL NUMBER OF STARS INSERTED: ", totalStarInsertions);
        int inconsistentStarsSize = inconsistentStarsWithEmptyName.size() + inconsistentStarsWithDifferentBirthYear.size();
        System.out.printf("%40s%d%n", "TOTAL NUMBER OF INCONSISTENT STARS: ", inconsistentStarsSize);
        System.out.printf("%40s%d%n", "  Stars with No Name: ", inconsistentStarsWithEmptyName.size());
        System.out.printf("%40s%d%n", "  Stars with Conflicting Birth Years: ", inconsistentStarsWithDifferentBirthYear.size());
    }

    public void writeInconsistentStarOutputToFile() {
        try {
            File file = new File("inconsistent_star_info.txt");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("=============================================\n");
                writer.write("                                             \n");
                writer.write("========== INCONSISTENT STAR INFO ==========\n");
                writer.write("                                             \n");
                writer.write("=============================================\n");
                writer.write("========== NO NAME ==========\n");
                for (Star star : inconsistentStarsWithEmptyName) {
                    writer.write(star.toString() + "\n");
                }
                writer.write("========== CONFLICTING BIRTH YEAR ==========\n");
                for (Star star : inconsistentStarsWithDifferentBirthYear) {
                    writer.write(star.toString() + "\n");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: Cannot write to newly created file inconsistent_star_info.txt.");
            e.printStackTrace();
        }

    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star(null, null, -1);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

       if (qName.equalsIgnoreCase("actor")) {
           boolean starIsInconsistent = checkIfStarIsInconsistent();
           if (!starIsInconsistent) {
               Star starAlreadyInMap = starMap.get(handleCaseSensitivity(tempStarName));
               if (starAlreadyInMap != null) {
                   starAlreadyInMap.setBirthYear(tempStarBirthYear);
               } else {
                   starMap.put(handleCaseSensitivity(tempStarName), tempStar);
               }
           }
           tempStarName = "";
           tempStarBirthYear = -1;
       } else if (qName.equalsIgnoreCase("stagename")) {
            if (tempVal != null && !tempVal.trim().isEmpty()) {
                tempStarName = tempVal.trim();
                tempStar.setName(tempVal.trim());
            }
        } else if (qName.equalsIgnoreCase("dob")) {
            int birthYear = getYearInCorrectFormat(tempVal);
            tempStarBirthYear = birthYear;
            tempStar.setBirthYear(tempStarBirthYear);
        }
    }

    private boolean checkIfStarIsInconsistent() {
        if (tempStarName == null || tempStarName.isEmpty()) {
            inconsistentStarsWithEmptyName.add(new Star(null, tempStarName, tempStarBirthYear));
            return true;
        }
        Star starAlreadyInMap = starMap.get(handleCaseSensitivity(tempStarName));
        if (starAlreadyInMap != null &&
                starAlreadyInMap.getBirthYear() != -1 &&
                starAlreadyInMap.getBirthYear() != tempStarBirthYear) {
            // More than one entry for the same star without matching birthYear
            inconsistentStarsWithDifferentBirthYear.add(new Star(null, tempStarName, tempStarBirthYear));
            return true;
        }

        return false;
    }

    private String handleCaseSensitivity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return input.trim().toLowerCase();
    }

    private Integer getYearInCorrectFormat(String tempVal) {
        String yearFormattedCorrectly = tempVal.trim();

        if (yearFormattedCorrectly.matches("\\d{4}") && yearFormattedCorrectly.length() == 4) {
            return Integer.parseInt(yearFormattedCorrectly);
        }

        return -1;
    }


    public static void main(String[] args) {

        ActorSAXParser actorSPE = new ActorSAXParser();
        actorSPE.runExample();
    }

}