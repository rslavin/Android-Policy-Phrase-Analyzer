package edu.utsa.cs.sefm.mapping;

import edu.utsa.cs.sefm.utils.Calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rocky on 5/18/2015.
 */
public class APIMapping {
    public static final String[] sources = {"NETWORK_INFORMATION", "LOCATION_INFORMATION", "UNIQUE_IDENTIFIER"};
    public String api;
    public List<Policy> policies;
    public Map<String, Integer> phrases; // unique list of phrases

    public APIMapping(String api, Policy policy) {
        policies = new ArrayList();
        phrases = new HashMap<>();
        addPolicy(policy);
        this.api = api;

    }

    /**
     * Finds the SuSi category for an api.
     *
     * @param api
     * @return
     */
    public static String getSuSiCategory(String api) {
        for (String source : sources) {
            if (api.contains(source))
                return source;
        }
        return "NO CATEGORY FOUND";

    }

    public static String getSimpleApi(String api) {
        Pattern pattern = Pattern.compile("<.* (.*)>");
        Matcher m = pattern.matcher(api);
        while (m.find())
            return m.group(1);
        return api;
    }

    public static String getApiFromFlowDroid(String api) {
        Pattern pattern = Pattern.compile("(<.*)==>");
        Matcher m = pattern.matcher(api);
        while (m.find())
            return m.group(1);
        return api;
    }

    public static int getApiFreqFromFlowDroid(String api) {
        Pattern pattern = Pattern.compile("==>(\\d+)");
        Matcher m = pattern.matcher(api);
        int count = 1;
        if (m.find()) {
            try {
                count = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                count = 0;
            }
        }
        return count;
        // TODO instead of returning 0, this should throw an exception
    }

    /**
     * Returns an ArrayList of apis for each phrase appearing as many times
     * as its frequency.
     *
     * @return
     */
    public ArrayList<String> getPhrasesAsListWithFreq() {
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, Integer> phrase : phrases.entrySet())
            for (int i = 0; i < phrase.getValue(); i++)
                keys.add(phrase.getKey());
        return keys;
    }

    /**
     * Calculates the term frequency of a phrase for the API.
     *
     * @param phrase
     * @return
     */
    public double phraseTF(String phrase) {
        return Calc.tf(this.getPhrasesAsListWithFreq(), phrase);
    }

    public String toString() {
        String ret = "API: " + getSimpleApi(api);
        ret += "\nApps present in: " + policies.size();
        ret += "\nTotal phrases: " + phrases.size();
        for (Policy policy : policies)
            ret += "\nPolicy: " + policy.name;
        for (Map.Entry<String, Integer> phrase : phrases.entrySet())
            ret += "\nPhrase: " + phrase.getKey() + " (" + phrase.getValue() + ")";
        return ret;
    }

    public void addPolicy(Policy policy) {
        if (!policies.contains(policy)) {
            policies.add(policy);
            for (Map.Entry<String, Integer> phrase : policy.phrases.entrySet())
                addPhrase(phrase.getKey(), phrase.getValue());
        }
    }

    public void addPhrase(String phrase, int count) {
        if (phrases.containsKey(phrase)) {
            int oldVal = phrases.get(phrase);
            phrases.put(phrase, oldVal + count);
        } else {
            phrases.put(phrase, count);
        }
    }

    /**
     * Creates list of rows for use with CSVWriter. Formatted as
     * api, category, phrase, phrase frequency
     *
     * @return
     */
    public ArrayList<ArrayList<String>> toCSVFormat() {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> phrase : phrases.entrySet()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(api);
            row.add(getSuSiCategory(api));
            row.add(phrase.getKey());
            row.add("" + phrase.getValue());
            rows.add(row);
        }
        return rows;
    }
}
