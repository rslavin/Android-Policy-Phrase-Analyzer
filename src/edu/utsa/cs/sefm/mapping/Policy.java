package edu.utsa.cs.sefm.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class Policy {
    public String name;
    //    public ArrayList<String> apis;
    public Map<String, Integer> apis;
    public HashMap<String, Integer> phrases;
    private APIMapper apiMapper;

    public Policy(String name, APIMapper apiMapper){
        this.apiMapper = apiMapper;
        this.name = name;
        phrases = new HashMap<>();
        apis = new HashMap<>();
    }

    /**
     * Adds a phrase to the policy. Keeps track of occurrences of a phrase.
     * @param phrase
     */
    public void addPhrase(String phrase, int count){
        if (phrases.containsKey(phrase)) {
            int oldVal = phrases.get(phrase);
            phrases.put(phrase, oldVal + count);
        }else{
            phrases.put(phrase, count);
        }
    }

    public boolean isValid(){
        if(phrases.size() == 0 || apis.size() == 0)
            return false;
        return true;
    }

    /**
     * Creates an ArrayList of Phrase objects based on the Policy.
     * @return
     */
    public ArrayList<Phrase> toPhrases(){
        ArrayList<Phrase> phraseObjects = new ArrayList<Phrase>();
        for(Map.Entry<String, Integer> phrase : phrases.entrySet()){
            Phrase newPhraseObj = new Phrase(phrase.getKey(), apiMapper);
            newPhraseObj.occurrences = phrase.getValue();
            HashMap<String, Integer> phraseAPIs = new HashMap<String, Integer>();
//            for(String api : apis){
            for (Map.Entry<String, Integer> api : apis.entrySet())
                phraseAPIs.put(api.getKey(), api.getValue());
            newPhraseObj.apis = new HashMap<>(phraseAPIs);
            newPhraseObj.policies.add(this);
            phraseObjects.add(newPhraseObj);
        }
        return phraseObjects;
    }

    /**
     * Returns an ArrayList of phrases with each phrase appearing the number of times
     * as its frequency.
     * @return
     */
    public ArrayList<String> getPhrasesAsListWithFreq(){
        ArrayList<String> keys = new ArrayList<>();
        for(Map.Entry<String, Integer> phrase : phrases.entrySet())
            for(int i = 0; i < phrase.getValue(); i++)
                keys.add(phrase.getKey());
        return keys;
    }

    public ArrayList<String> getAPIsAsListWithFreq() {
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, Integer> api : apis.entrySet())
            for (int i = 0; i < api.getValue(); i++)
                keys.add(api.getKey());
        return keys;
    }

    public String toString(){
        String ret = "Name: " + this.name;
        for(Map.Entry<String, Integer> phrase : phrases.entrySet())
            ret += "\nPhrase: " + phrase.getKey() + " (" + phrase.getValue() + ")";
//        for(String api : apis)
        for (Map.Entry<String, Integer> api : apis.entrySet())
            ret += "\nAPI: " + api.getKey() + " (" + api.getValue() + ")";
        return ret;
    }
}
