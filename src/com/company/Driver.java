package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Driver {


    private static final boolean VERBOSE = true;

    public static void main(String args[]){
        ArrayList<String> phrases;

        String apiLogs = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\androidPolicyDownloader\\API Calls\\outAPI";
        String policyFiles = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\50 policies";
        String phraseFile = "C:\\cygwin64\\home\\Rocky\\bin\\androidPolicyTools\\phraseAnalyser\\testPhrases.txt";


        try {
            phrases = getPhrases(phraseFile);
        }catch(IOException e){
            System.err.println("Phrase file not found");
            return;
        }
        for(String phrase : phrases){
            System.out.println("(" + phrase + ")");
        }

        try {
            APIMapper mapper = new APIMapper(phrases, policyFiles, apiLogs, VERBOSE);
            mapper.apiPhraseFrequency(VERBOSE);
          //  mapper.equivalenceFinder(10, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
