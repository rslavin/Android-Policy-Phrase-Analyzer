package edu.utsa.cs.sefm.mapping;

import edu.utsa.cs.sefm.utils.CSVWriter;
import edu.utsa.cs.sefm.utils.Calc;
import edu.utsa.cs.sefm.utils.HTMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class APIMapper {
    public List<Policy> policies;
    public List<Phrase> phrases;
    public List<APIMapping> apiMappings;
    public List<Policy> noPhrasePolicies;
    public List<Policy> noApiPolicies;
    public Map<String, Integer> totalApiOccurrences;
    public int totalApiCalls;
    public Map<String, Integer> totalPhraseOccurrences;
    public int totalPhrases;
    public boolean verbose;
    public ArrayList<ArrayList<String>> synonyms;
    public boolean sortByFrequency = false;

    public APIMapper(ArrayList<String> phrases, String policyDirectoryPath, String apiLogDirectoryPath, boolean verbose, ArrayList<ArrayList<String>> synonyms) throws IOException {
        this.verbose = verbose;
        this.synonyms = synonyms;
        policies = new ArrayList<>();
        apiMappings = new ArrayList<>();
        noPhrasePolicies = new ArrayList<>();
        noApiPolicies = new ArrayList<>();
        totalApiOccurrences = new HashMap<>();
        totalPhraseOccurrences = new HashMap<>();
        totalApiCalls = 0;
        totalPhrases = 0;
        // Look through each policy and find matching phrases
        File policyDirectory = new File(policyDirectoryPath);
        for (File fileEntry : policyDirectory.listFiles()) {
            Policy newPolicy = new Policy(fileEntry.getName(), this);
            String fileContents = new String(Files.readAllBytes(fileEntry.toPath()));
            HTMLParser html = new HTMLParser(fileContents);
            fileContents = html.removeHTMLTags();

            // for each line, check if it contains a phrase from the list
            for (String phrase : phrases) {
                int count;
                if ((count = HTMLParser.countOccurrences(fileContents.toLowerCase(), phrase.toLowerCase())) > 0) {
                    if (verbose)
                        System.out.println("Matched phrase '" + phrase + "' to policy '" + fileEntry.getName());

                    newPolicy.addPhrase(synonymFixer(phrase), count);
                }
            }
            // if phrases found, look for apis
            if (newPolicy.phrases.size() > 0)
                newPolicy.apis = getAPIs(newPolicy, apiLogDirectoryPath);
            else
                noPhrasePolicies.add(newPolicy);
            if (newPolicy.apis.size() == 0)
                noApiPolicies.add(newPolicy);
            if (verbose)
                System.out.println("==== New Policy ====\n" + newPolicy);

            if (newPolicy.isValid())
                policies.add(newPolicy);
        }

    }

    public String getStats() {
        String ret = "Policies analyzed: " + policies.size() +
                "\nPolicies without phrases: " + noPhrasePolicies.size() +
                "\nPolicies without apis: " + noApiPolicies.size() +
                "\nPhrases: " + phrases.size();
        return ret;
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
     * Also adds the total occurrences of the api to totalApiOccurrences.
     *
     * @param policy
     * @param apiLogDirectoryPath
     * @return
     * @throws IOException
     */
    private HashMap<String, Integer> getAPIs(Policy policy, String apiLogDirectoryPath) throws IOException {
        File apiLog = new File(apiLogDirectoryPath + "/" + policy.name.replaceAll("\\.html$", ".apk.log"));
        BufferedReader br = new BufferedReader(new FileReader(apiLog));
        String line;
        HashMap<String, Integer> apis = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String api = APIMapping.getApiFromFlowDroid(line);
            int apiOccurrence = APIMapping.getApiFreqFromFlowDroid(line);
            apis.put(api, apiOccurrence);
            // add the api count
            if (totalApiOccurrences.containsKey(api))
                totalApiOccurrences.put(api, totalApiOccurrences.get(api) + apiOccurrence);
            else
                totalApiOccurrences.put(api, apiOccurrence);
            totalApiCalls += apiOccurrence;

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
        // update global occurrence of the phrase
        if (totalPhraseOccurrences.containsKey(newPhrase.name))
            totalPhraseOccurrences.put(newPhrase.name, totalPhraseOccurrences.get(newPhrase.name) + newPhrase.occurrences);
        else
            totalPhraseOccurrences.put(newPhrase.name, newPhrase.occurrences);

        // merge a Phrase if it already exists
        for (Phrase phrase : phrases) {
            if (phrase.name.equals(newPhrase.name)) {
                // add occurrences
                phrase.occurrences = phrase.occurrences + newPhrase.occurrences;
                totalPhrases += phrase.occurrences;
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
        ArrayList<ArrayList<String>> phraseAPIs = new ArrayList<>();
        for (Policy policy : policies)
            phraseAPIs.add(policy.getAPIsAsListWithFreq());
        return Calc.idf(phraseAPIs, api);
    }

    public double phraseIDF(String phrase) {
        ArrayList<ArrayList<String>> apiPhrases = new ArrayList<>();
        for (APIMapping apiMap : apiMappings)
            apiPhrases.add(apiMap.getPhrasesAsListWithFreq());
        return Calc.idf(apiPhrases, phrase);
    }

    /**
     * Map phrases to each api.
     */
    public void apiMap() {
        // TODO add exception
        if (policies.size() == 0) {
            System.err.println("No policies parsed yet. Run apiPhraseFrequency() first.");
            return;
        }

        for (Policy policy : policies) {
//            for (String api : policy.apis)
            for (Map.Entry<String, Integer> api : policy.apis.entrySet())
                addAPIMapping(api.getKey(), policy);
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

    public void apiMappingsToCSV(String filename) throws IOException {
        // TODO add exception
        if (policies.size() == 0) {
            System.err.println("No phrases mapped yet. Run apiMap() first.");
            return;
        }


        CSVWriter csv = new CSVWriter(filename);
        ArrayList<String> headers = new ArrayList<>();
        headers.add("API");
        headers.add("Category");
        headers.add("Phrase");
        headers.add("Frequency");
        headers.add("TF");
        headers.add("IDF");
        headers.add("TF*IDF");
        csv.addRow(headers);
        for (APIMapping mapping : apiMappings) {
            System.err.println("api: " + mapping.api);
            for (Map.Entry<String, Integer> phrase : mapping.phrases.entrySet()) {
                System.err.println("phrase: " + phrase.getKey());
                ArrayList<String> row = new ArrayList<>();
                row.add(mapping.api);
                row.add(APIMapping.getSuSiCategory(mapping.api));
                row.add(phrase.getKey());
                row.add("" + phrase.getValue());
                double tf = mapping.phraseTF(phrase.getKey());
                row.add("" + tf);
                double idf = phraseIDF(phrase.getKey());
                row.add("" + idf);
                row.add("" + idf * tf);
                csv.addRow(row);
            }
        }
        csv.writeFile();
    }

    public void phraseMappingsToCSV(String filename) throws IOException {
        // TODO add exception
        CSVWriter csv = new CSVWriter(filename);
        ArrayList<String> headers = new ArrayList<>();
        headers.add("Phrase");
        headers.add("API");
        headers.add("Frequency");
        headers.add("TF");
        headers.add("IDF");
        headers.add("TF*IDF");
        headers.add("p(api/phrase)");
        csv.addRow(headers);
        for (Phrase phrase : phrases) {
            System.err.println("phrase: " + phrase.name);
            for (Map.Entry<String, Integer> api : phrase.apis.entrySet()) {
                System.err.println("api: " + api.getKey());
                ArrayList<String> row = new ArrayList<>();
                row.add(phrase.name);
                row.add(api.getKey());
                row.add("" + api.getValue());
                double tf = phrase.apiTF(api.getKey());
                row.add("" + tf);
                double idf = apiIDF(api.getKey());
                row.add("" + idf);
                row.add("" + tf * idf);
                row.add("" + bayesApiPerPhrase(api.getKey(), phrase));
                csv.addRow(row);
            }
        }
        csv.writeFile();
    }

    public double bayesApiPerPhrase(String api, Phrase phrase) {
        double probApi = probApi(api);
        double probPhrase = probPhrase(phrase.name);
        double probApiPhrase = phrase.getApiProb(api);


        return (probApiPhrase * probPhrase) / probApi;
    }

    /**
     * Calculates the probability of an api being called over all api calls for all policies.
     *
     * @param api
     * @return
     */
    private double probApi(String api) {
        double apiOcc = (double) totalApiOccurrences.get(api);
        return apiOcc / (double) totalApiCalls;

    }

    /**
     * Calculates the probability of a phrase occurring over all phrases that occurred in policies.
     *
     * @param phrase
     * @return
     */
    private double probPhrase(String phrase) {
        double phraseOcc = (double) totalPhraseOccurrences.get(phrase);
        return phraseOcc / (double) totalPhrases;
    }
}
