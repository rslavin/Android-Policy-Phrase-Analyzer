package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class APIMapper {
    public List<Policy> policies;
    public List<Phrase> phrases;
    public boolean verbose;
    public int countNoPhrases = 0;
    public int countNoAPIs = 0;

    public APIMapper(ArrayList<String> phrases, String policyDirectoryPath, String apiLogDirectoryPath, boolean verbose) throws IOException {
        this.verbose = verbose;
        policies = new ArrayList<Policy>();
        // Look through each policy and find matching phrases
        File policyDirectory = new File(policyDirectoryPath);
        for(File fileEntry : policyDirectory.listFiles()) {
            Policy newPolicy = new Policy(fileEntry.getName());
            BufferedReader br = new BufferedReader(new FileReader(fileEntry));
            String line;
            // for each line, check if it contains a phrase from the list
            while ((line = br.readLine()) != null)
                for (String phrase : phrases) {
                    if (line.contains(phrase)) {
                        if(verbose)
                            System.out.println("Matched phrase '" + phrase + "' to policy '" + fileEntry.getName());
                        newPolicy.addPhrase(phrase);
                    }
                }
            // if phrases found, look for apis
            if(newPolicy.phrases.size() > 0)
                newPolicy.apis = getAPIs(newPolicy, apiLogDirectoryPath);
            else
                countNoPhrases++;
            if(newPolicy.apis.size() == 0)
                countNoAPIs++;
            if(verbose)
                System.out.println("==== New Policy ====\n" + newPolicy);

            if(newPolicy.isValid())
                policies.add(newPolicy);
        }

    }

    /**
     * Calculates the overall frequency of API calls for phrases.
     * Verbose mode will print phrases with their associated policies and APIs
     * along with the frequency of the API calls in order from most to least.
     */
    public void apiPhraseFrequency(boolean verbose){
        phraseMapper();
        if(verbose) {
            System.out.println("=== Phrases ===");
            for (Phrase phrase : this.phrases) {
                System.out.println(phrase);
            }
        }
    }

    /**
     * Checks for equivalence among phrases by grouping phrases with a minimum common
     * API calls for API calls called a minimum number of times per phrase.
     * @param minCommon Minimum number of API calls two Phrases must share
     *                  in order to be considered equivalent
     * @param minAPICalls Minimum number of times an API must be associated
     *                    with a phrase before it is considered in this method
     */
    /*
    Idea: Go through each other phrase. If a minimum number of apis are found,
    check every other phrase for that SAME API for the same minimum number.
    Put those together as a set.
    To deal with duplicates: before searching for a new set, check that the API-phrase
    combination hasn't already been added to the master set. If so, skip that API.
     */
    public void equivalenceFinder(int minCommon, int minAPICalls){
        List<EquivPhraseSet> equivsSet = new ArrayList<>();
        for(Phrase phrase : phrases){
        //for(int i = 0; i < phrases.size(); i++){
            EquivPhraseSet equivs = new EquivPhraseSet();
            for(Phrase phrase2 : phrases){
            //for(int j = i + 1; j < phrases.size(); j++){
                if(phrase != phrase2){
                    // check for minimum size intersection for equivalence
                    List<String> matchingApis = new ArrayList<>();
                    for(Map.Entry<String, Integer> api : phrase.apis.entrySet()) {
                        // if the api is associated with the phrase at least
                        // minAPICalls times
                        if(api.getValue() > minAPICalls) {
                            for (Map.Entry<String, Integer> api2 : phrase2.apis.entrySet()) {
                                // if the two apis are equal and called enough times
                                if (api2.getValue() > minAPICalls &&
                                        api2.getKey().equals(api.getKey())){
                                    // CHECK ALL OTHER PHRASES FOR SAME MATCH
                                    // USE A MAP with phrase=>api
                                    matchingApis.add(api.getKey());
                                }
                            }
                        }
                    }
                    // if matched phrases is more than minCommon, add it to equivs
                    if(matchingApis.size() > minCommon){
                        equivs.phrases.add(phrase2);
                        equivs.addAPIs(matchingApis);
                    }
                }
            }
            // if equivalents were found to the first phrase, add the first phrase
            if(equivs.phrases.size() > 0){
                equivs.phrases.add(phrase);
                equivsSet.add(equivs);
            }
        }

   /*     // remove duplicates from set
        for(EquivPhraseSet equivPhrases : equivsSet){
            for(EquivPhraseSet equivPhrases2 : equivsSet){
                if(equivPhrases != equivPhrases2)
                    if(equivPhrases.equals(equivPhrases2))
                        equivsSet.remove(equivPhrases2);
            }
        }
*/
        for(int i = 0; i < equivsSet.size(); i++){
            for(int j = (i + 1); j < equivsSet.size(); j++)
                if(!equivsSet.get(i).equals(equivsSet.get(j))) {
                    equivsSet.remove(equivsSet.get(j));
                }

        }

        // print
        for(EquivPhraseSet equivPhrases : equivsSet){
            System.out.println(equivPhrases);
        }
        System.out.println("Total Equivalent Sets: " + equivsSet.size());
    }

    private ArrayList<String> getAPIs(Policy policy, String apiLogDirectoryPath) throws IOException {
        File apiLog = new File(apiLogDirectoryPath + "/" + policy.name.replace(".html", ".apk.log"));
        BufferedReader br = new BufferedReader(new FileReader(apiLog));
        String line;
        ArrayList<String> apis = new ArrayList<String>();
        while((line = br.readLine()) != null){
            apis.add(line.substring(0, line.length() - 4));
        }
        return apis;
    }

    private void phraseMapper(){
        phrases = new ArrayList<>();
        for(Policy policy : policies){
            for(Phrase newPhrase : policy.toPhrases()){
                phraseAdder(newPhrase);
            }
        }
    }

    /**
     * Add a new Phrase to the list of Phrase objects
     * @param newPhrase
     */
    private void phraseAdder(Phrase newPhrase){
        // merge a Phrase if it already exists
        for(Phrase phrase : phrases) {
            if (phrase.name.equals(newPhrase.name)) {
                // add occurrences
                phrase.occurrences = phrase.occurrences + newPhrase.occurrences;
                phrase.policies.addAll(newPhrase.policies);
                phrase.addAPIs(newPhrase.apis);
                return;
            }
        }
        // add a new Phrase otherwise.
        phrases.add(newPhrase);
    }
}
