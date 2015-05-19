package com.sefm;

import java.util.*;

/**
 * Created by Rocky on 5/12/2015.
 */
public class Phrase {
    public HashMap<String, Integer> apis;
    public Set<Policy> policies;
    public String name;
    public int occurrences;
    private APIMapper apiMapper;

    public Phrase(String name, APIMapper apiMapper) {
        this.apiMapper = apiMapper;
        this.name = name;
        apis = new HashMap<>();
        policies = new HashSet<>();
    }

    public void addAPIs(HashMap<String, Integer> newAPIs) {
        for (Map.Entry<String, Integer> newApi : newAPIs.entrySet()) {
            if (apis.containsKey(newApi.getKey())) {
                apis.put(newApi.getKey(), newApi.getValue() + apis.get(newApi.getKey()));
            } else {
                apis.put(newApi.getKey(), newApi.getValue());
            }
        }
    }

    public String toStringBasic() {
        String ret = "Phrase: " + name +
                "\nApps Present In: " + policies.size() +
                "\nTotal Occurrences: " + occurrences;
        for (Policy policy : policies)
            ret += "\nPolicy: " + policy.name;
        return ret;
    }

    /**
     * Returns information about the Phrase with APIs listed and sorted
     * by their TF-IDF scores.
     * @return
     */
    public String toString() {
        String ret = toStringBasic();
        ret += "\n" + sortAPIsbyTFIDF();
        return ret;
    }

    /**
     * Returns information about the Phrase with APIs listed and sorted either
     * by TF-IDF scores or frequency
     * @param frequency true for APIs by frequency, false for APIs by TF-IDF
     * @return
     */
    public String toString(boolean frequency) {
        if (frequency) {
            String ret = toStringBasic();
            sortAPIsByValue();
            for (Map.Entry<String, Integer> api : apis.entrySet())
                ret += "API: " + api.getKey() + " (Frequency: " + api.getValue() + ", " +
                        "TF: " + apiTF(api.getKey()) + ", IDF: " + apiMapper.apiIDF(api.getKey()) + ", " +
                        "TF-IDF: " + apiMapper.apiIDF(api.getKey()) * apiTF(api.getKey()) + ")\n";

            return ret;
        }
        return toString();
    }

    /**
     * Sorts APIs by TF-IDF from highest to lowest.
     *
     * @return
     */
    private String sortAPIsbyTFIDF() {
        Map<Map.Entry<String, Integer>, Double> unSorted = new HashMap<>();
        for (Map.Entry<String, Integer> api : apis.entrySet())
            unSorted.put(api, apiMapper.apiIDF(api.getKey()) * apiTF(api.getKey()));

        List<Map.Entry<Map.Entry<String, Integer>, Double>> list = new LinkedList<>(unSorted.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Map.Entry<String, Integer>, Double>>() {
            public int compare(Map.Entry<Map.Entry<String, Integer>, Double> o1, Map.Entry<Map.Entry<String, Integer>, Double> o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        String ret = "";
        for (Map.Entry<Map.Entry<String, Integer>, Double> api : list) {
            ret += "API: " + api.getKey().getKey() +
                    " (Frequency: " + api.getKey().getValue() +
                    ", TF-IDF: " + api.getValue() + ")\n";
        }

        return ret;
    }

    private void sortAPIsByValue() {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(apis.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        apis = (HashMap<String, Integer>) sorted;
    }

    /**
     * Returns an ArrayList of apis without frequencies
     *
     * @return
     */
    public ArrayList<String> getAPIsAsList() {
        ArrayList<String> keys = new ArrayList<String>();
        for (Map.Entry<String, Integer> api : apis.entrySet())
            keys.add(api.getKey());
        return keys;
    }

    /**
     * Returns an ArrayList of apis with each api appearing the number of times
     * as its frequency.
     *
     * @return
     */
    public ArrayList<String> getAPIsAsListWithFreq() {
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, Integer> api : apis.entrySet())
            for (int i = 0; i < api.getValue(); i++)
                keys.add(api.getKey());
        return keys;
    }

    /**
     * Calculates the term frequency of an api for the Phrase
     *
     * @param api
     * @return
     */
    public double apiTF(String api) {
        return Calc.tf(this.getAPIsAsListWithFreq(), api);
    }

}
