package com.sefm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Driver {


    private static final boolean VERBOSE = true;
    private static String apiLogs = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\androidPolicyDownloader\\API Calls\\outAPI";
    private static String policyFiles = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\50 policies";
    private static String phraseFile = "phrases.txt";
    private static String synonymFile = "synonyms.txt";

    public static void main(String args[]){
        ArrayList<String> phrases;




        try {
            phrases = getPhrases(phraseFile);
        }catch(IOException e){
            System.err.println("Phrase file not found");
            return;
        }

        try {
            APIMapper mapper = new APIMapper(phrases, policyFiles, apiLogs, VERBOSE, parseSynonyms());
            mapper.apiPhraseFrequency(VERBOSE);
       //     mapper.equivalenceFinder(10, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a List of Lists of synonyms based on input file.
     * @return
     * @throws IOException
     */
    private static ArrayList<ArrayList<String>> parseSynonyms() throws IOException{
        File file = new File(synonymFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<ArrayList<String>> synonymList = new ArrayList<>();
        while((line = br.readLine()) != null){
            ArrayList<String> synonyms = new ArrayList<>();
            synonyms.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));
            while(synonyms.remove(" "));
            for(int i = 0; i < synonyms.size(); i++)
                synonyms.set(i, synonyms.get(i).trim());
            if(synonyms.size() > 1)
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
        while((line = br.readLine()) != null)
            phrases.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));

        br.close();
        while(phrases.remove(" "));
        for(int i = 0; i < phrases.size(); i++)
            phrases.set(i, phrases.get(i).trim());
        return phrases;
    }


}
