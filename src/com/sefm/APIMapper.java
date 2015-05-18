package com.sefm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class APIMapper {
    public List<Policy> policies;
    public List<Phrase> phrases;
    public List<APIMapping> apiMappings;
    public boolean verbose;
    public int countNoPhrases = 0;
    public int countNoAPIs = 0;
    public ArrayList<ArrayList<String>> synonyms;
    public boolean sortByFrequency = false;

    public APIMapper(ArrayList<String> phrases, String policyDirectoryPath, String apiLogDirectoryPath, boolean verbose, ArrayList<ArrayList<String>> synonyms) throws IOException {
        this.verbose = verbose;
        this.synonyms = synonyms;
        policies = new ArrayList<>();
        apiMappings = new ArrayList<>();
        // Look through each policy and find matching phrases
        File policyDirectory = new File(policyDirectoryPath);
        for (File fileEntry : policyDirectory.listFiles()) {
            Policy newPolicy = new Policy(fileEntry.getName(), this);
            String fileContents = new String(Files.readAllBytes(fileEntry.toPath()));
            HTMLParser html = new HTMLParser(fileContents);
            fileContents = html.removeHTMLTags();

            // BufferedReader br = new BufferedReader(new FileReader(fileEntry));
            // for each line, check if it contains a phrase from the list
            for (String phrase : phrases) {
                int count;
                if ((count = HTMLParser.countOccurrences(fileContents.toLowerCase(), phrase.toLowerCase())) > 0) {
                    if (verbose)
                        System.out.println("Matched phrase '" + phrase + "' to policy '" + fileEntry.getName());
//                    if (fileEntry.getName().equals("my.googlemusic.play.html")) {
//                        System.out.println(fileContents);
//                        System.out.println("\n\nPhrase: " + phrase + " - " + count);
//                    }
                    newPolicy.addPhrase(synonymFixer(phrase), count);
                }
            }
            // if phrases found, look for apis
            if (newPolicy.phrases.size() > 0)
                newPolicy.apis = getAPIs(newPolicy, apiLogDirectoryPath);
            else
                countNoPhrases++;
            if (newPolicy.apis.size() == 0)
                countNoAPIs++;
            if (verbose)
                System.out.println("==== New Policy ====\n" + newPolicy);

            if (newPolicy.isValid())
                policies.add(newPolicy);
        }

    }

    /**
     * If a "master" synonym exists for a phrase, it is replaced with the "master".
     *
     * @param phrase Phrase to check for a "master" synonym.
     * @return
     */
    private String synonymFixer(String phrase) {
        for (List<String> synList : synonyms) {
            if (synList.contains(phrase)) {
                return synList.get(0);
            }
        }
        return phrase;
    }

    /**
     * Calculates the overall frequency of API calls for phrases.
     * Verbose mode will print phrases with their associated policies and APIs
     * along with the frequency of the API calls in order from most to least.
     */
    public void apiPhraseFrequency(boolean verbose) {
        phraseMapper();
        System.out.println("=== Phrases ===");
        for (Phrase phrase : this.phrases) {
            System.out.println(phrase.toString(sortByFrequency));
        }
        System.out.println("Total Policies: " + policies.size());
    }

    /**
     * Checks for equivalence among phrases by grouping phrases with a minimum common
     * API calls for API calls called a minimum number of times per phrase.
     *
     * @param minCommon   Minimum number of API calls two Phrases must share
     *                    in order to be considered equivalent
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
    public void equivalenceFinder(int minCommon, int minAPICalls) {
        List<EquivPhraseSet> equivsSet = new ArrayList<>();
        for (Phrase phrase : phrases) {
            //for(int i = 0; i < phrases.size(); i++){
            EquivPhraseSet equivs = new EquivPhraseSet();
            for (Phrase phrase2 : phrases) {
                //for(int j = i + 1; j < phrases.size(); j++){
                if (phrase != phrase2) {
                    // check for minimum size intersection for equivalence
                    List<String> matchingApis = new ArrayList<>();
                    for (Map.Entry<String, Integer> api : phrase.apis.entrySet()) {
                        // if the api is associated with the phrase at least
                        // minAPICalls times
                        if (api.getValue() > minAPICalls) {
                            for (Map.Entry<String, Integer> api2 : phrase2.apis.entrySet()) {
                                // if the two apis are equal and called enough times
                                if (api2.getValue() > minAPICalls &&
                                        api2.getKey().equals(api.getKey())) {
                                    // CHECK ALL OTHER PHRASES FOR SAME MATCH
                                    // USE A MAP with phrase=>api
                                    matchingApis.add(api.getKey());
                                }
                            }
                        }
                    }
                    // if matched phrases is more than minCommon, add it to equivs
                    if (matchingApis.size() > minCommon) {
                        equivs.phrases.add(phrase2);
                        equivs.addAPIs(matchingApis);
                    }
                }
            }
            // if equivalents were found to the first phrase, add the first phrase
            if (equivs.phrases.size() > 0) {
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
        for (int i = 0; i < equivsSet.size(); i++) {
            for (int j = (i + 1); j < equivsSet.size(); j++)
                if (!equivsSet.get(i).equals(equivsSet.get(j))) {
                    equivsSet.remove(equivsSet.get(j));
                }

        }

        // print
        for (EquivPhraseSet equivPhrases : equivsSet) {
            System.out.println(equivPhrases);
        }
        System.out.println("Total Equivalent Sets: " + equivsSet.size());
    }

    /**
     * Associates APIs to a policy by reading in FlowDroid data from existing files.
     *
     * @param policy
     * @param apiLogDirectoryPath
     * @return
     * @throws IOException
     */
    private ArrayList<String> getAPIs(Policy policy, String apiLogDirectoryPath) throws IOException {
        File apiLog = new File(apiLogDirectoryPath + "/" + policy.name.replaceAll("\\.html$", ".apk.log"));
        BufferedReader br = new BufferedReader(new FileReader(apiLog));
        String line;
        ArrayList<String> apis = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            apis.add(APIMapping.getApiFromFlowDroid(line));
//            apis.add(line.substring(0, line.length() - 4));
        }
        return apis;
    }

    /**
     * Creates Phrase objects for every Phrase in a Policy. The resulting
     * Phrases are mapped to all APIs from the Policy.
     */
    private void phraseMapper() {
        phrases = new ArrayList<>();
        for (Policy policy : policies) {
            for (Phrase newPhrase : policy.toPhrases()) {
                phraseAdder(newPhrase);
            }
        }
    }

    /**
     * Add a new Phrase to the list of Phrase objects
     *
     * @param newPhrase
     */
    private void phraseAdder(Phrase newPhrase) {
        // merge a Phrase if it already exists
        for (Phrase phrase : phrases) {
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

    /**
     * Calculates the inverse document frequency of an API given all Phrases
     * in all Policies.
     *
     * @param api
     * @return
     */
    public double apiIDF(String api) {
        List<List<String>> phrasesSet = new ArrayList<>();
        for (Policy policy : policies)
            phrasesSet.add(policy.apis);
        return Calc.idf(phrasesSet, api);
    }

    /**
     * Map phrases to each api.
     */
    public void apiMap() {
        if (policies.size() == 0) {
            System.err.println("No policies parsed yet. Run apiPhraseFrequency() first.");
            return;
        }

        for (Policy policy : policies) {
            for (String api : policy.apis)
                addAPIMapping(api, policy);
        }

        for (APIMapping mapping : apiMappings) {
            System.out.println("\n" + mapping);
        }

    }

    /**
     * Adds an api occurrence to apiMappings
     *
     * @param api
     */
    private void addAPIMapping(String api, Policy policy) {
        for (APIMapping existingApi : apiMappings) {
            if (existingApi.api.equals(api)) {
                existingApi.addPolicy(policy);
                return;
            }
        }
        apiMappings.add(new APIMapping(api, policy));
    }
}
