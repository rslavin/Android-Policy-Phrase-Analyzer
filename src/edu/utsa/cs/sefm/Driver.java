package edu.utsa.cs.sefm;

import edu.utsa.cs.sefm.mapping.APIMapper;
import edu.utsa.cs.sefm.utils.HTMLParser;
import edu.utsa.cs.sefm.utils.Text;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Driver {

    private static final boolean VERBOSE = false;
    private static String apiLogs = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\1000policiesAPIS";
    private static String policyFiles = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\1000policies";
    private static String phraseFile = "phrases.txt";
    private static String synonymFile = "synonyms.txt";
    private static String associatorFile = "associators.txt";
    private static String outFile = "out.txt";

    public static void main(String args[]) {
        List<String> phrases;
        List<String> associators; // for example, such as, etc
        try {
            phrases = Text.getWordList(phraseFile);
            associators = Text.getWordList(associatorFile);
        } catch (IOException e) {
            System.err.println("File not found");
            e.printStackTrace();
            return;
        }

        doMappings(phrases);
/*        try {
            //doAssociations(phrases, associators);
            System.setOut(new PrintStream(new File("out.html")));
            convertAssociatorOutputToHTML(phrases, associators, "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\associators_output.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }*/


    }

    private static void convertAssociatorOutputToHTML(List<String> phrases, List<String> associators, String file) throws IOException {
        String fileContents = new String(Files.readAllBytes(new File(file).toPath()));
        Text fileText = new Text(fileContents, associators);
        fileText.markAssociators("blue");
        for (String phrase : phrases)
            fileText.markWord(phrase, "green");
        System.out.println(fileText.toHTML());
    }

    /**
     * Extracts sentences from policies where a phrase appears followed by an associator (e.g.,
     * for example, such as, etc).
     *
     * @param phrases
     * @param associators
     * @throws IOException
     */
    private static void doAssociations(List<String> phrases, List<String> associators) throws IOException {
        List<String> associated = new ArrayList<>();
        File policyDirectory = new File(policyFiles);
        for (File fileEntry : policyDirectory.listFiles()) {
            String fileContents = new String(Files.readAllBytes(fileEntry.toPath()));
            HTMLParser html = new HTMLParser(fileContents);
            fileContents = html.removeHTMLTags();
            Text text = new Text(fileContents, associators);

            // for each line, check if it contains a phrase from the list
            for (String phrase : phrases) {
                List<String> found = text.findAssociated(phrase);
                associated.addAll(found);
            }

        }

        // remove duplicates
        Set set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        set.addAll(associated);
        associated = new ArrayList(set);

        for (String sentence : associated)
            System.out.println(sentence);

    }

    /**
     * Maps APIs to phrases and phrases to APIs. Outputs data in csv files.
     * @param phrases
     */
    private static void doMappings(List<String> phrases) {
        try {
            System.setOut(new PrintStream(new File(outFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            APIMapper mapper = new APIMapper(phrases, policyFiles, apiLogs, VERBOSE, parseSynonyms());
            mapper.apiPhraseFrequency(VERBOSE);
            mapper.apiMap();
            mapper.apiMappingsToCSV("phraseToApis");
            mapper.phraseMappingsToCSV("apiToPhrases");
            System.out.println("\n" + mapper.getStats());
            //     mapper.equivalenceFinder(10, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a List of Lists of synonyms based on input file.
     *
     * @return
     * @throws IOException
     */
    private static ArrayList<ArrayList<String>> parseSynonyms() throws IOException {
        File file = new File(synonymFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<ArrayList<String>> synonymList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            ArrayList<String> synonyms = new ArrayList<>();
            synonyms.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));
            while (synonyms.remove(" ")) ;
            for (int i = 0; i < synonyms.size(); i++)
                synonyms.set(i, synonyms.get(i).trim());
            if (synonyms.size() > 1)
                synonymList.add(synonyms);
        }
        br.close();
        return synonymList;
    }



}
