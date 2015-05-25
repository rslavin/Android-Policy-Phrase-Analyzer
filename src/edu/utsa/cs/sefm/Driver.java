package edu.utsa.cs.sefm;

import edu.utsa.cs.sefm.mapping.APIMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Driver {

    private static final boolean VERBOSE = false;
    private static String apiLogs = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\1000policiesAPIS";
    private static String policyFiles = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\1000policies";
    private static String phraseFile = "phrases.txt";
    private static String synonymFile = "synonyms.txt";
    private static String outFile = "out.txt";

    public static void main(String args[]) {
        ArrayList<String> phrases;
        try {
            System.setOut(new PrintStream(new File(outFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            phrases = getPhrases(phraseFile);
        } catch (IOException e) {
            System.err.println("Phrase file not found");
            return;
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

    private static ArrayList<String> getPhrases(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<String> phrases = new ArrayList<String>();
        while ((line = br.readLine()) != null)
            phrases.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));

        br.close();
        while (phrases.remove(" ")) ;
        for (int i = 0; i < phrases.size(); i++)
            phrases.set(i, phrases.get(i).trim());
        return phrases;
    }

}
