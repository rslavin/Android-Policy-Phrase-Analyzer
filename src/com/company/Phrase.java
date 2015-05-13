package com.company;

import java.util.*;

/**
 * Created by Rocky on 5/12/2015.
 */
public class Phrase {
    public HashMap<String, Integer> apis;
    public Set<Policy> policies;
    public String name;
    public int occurrences;

    public Phrase(String name){
        this.name = name;
        apis = new HashMap<String, Integer>();
        policies = new HashSet<>();
    }

    public void addAPIs(HashMap<String, Integer> newAPIs){
        for(Map.Entry<String, Integer> newApi : newAPIs.entrySet()){
            if(apis.containsKey(newApi.getKey())){
                apis.put(newApi.getKey(), newApi.getValue() + apis.get(newApi.getKey()));
            }else{
                apis.put(newApi.getKey(), newApi.getValue());
            }
        }
    }

    public String toString(){
        String ret = "Phrase: " + name +
            "\nOccurrences: " + occurrences;
        for(Policy policy : policies)
            ret += "\nPolicy: " + policy.name;
        sortAPIsByValue();
        for(Map.Entry<String, Integer> api : apis.entrySet())
            ret += "\nAPI: " + api.getKey() + " (" + api.getValue() + ")";

        return ret;
    }

    private void sortAPIsByValue(){
        List<Map.Entry<String, Integer>> list = new LinkedList<>(apis.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>(){
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String,Integer> o2){
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list){
            sorted.put(entry.getKey(), entry.getValue());
        }
        apis = (HashMap<String, Integer>)sorted;
    }

    /**
     * Returns an ArrayList of apis without frequencies
     * @return
     */
    public ArrayList<String> getAPIsAsList(){
        ArrayList<String> keys = new ArrayList<String>();
        for(Map.Entry<String, Integer> api : apis.entrySet())
            keys.add(api.getKey());
        return keys;
    }

}
