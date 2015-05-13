package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class Policy {
    public String name;
    public List<String> apis;
    public HashMap<String, Integer> phrases;

    public Policy(String name){
        this.name = name;
        phrases = new HashMap<String, Integer>();
        apis = new ArrayList<String>();
    }

    /**
     * Adds a phrase to the policy. Keeps track of occurrences of a phrase.
     * @param phrase
     */
    public void addPhrase(String phrase){
        if (phrases.containsKey(phrase)) {
            int oldVal = phrases.get(phrase);
            phrases.put(phrase, oldVal + 1);
        }else{
            phrases.put(phrase, 1);
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
            Phrase newPhraseObj = new Phrase(phrase.getKey());
            newPhraseObj.occurrences = phrase.getValue();
            HashMap<String, Integer> phraseAPIs = new HashMap<String, Integer>();
            for(String api : apis){
                phraseAPIs.put(api, 1);
            }
            newPhraseObj.apis = new HashMap<>(phraseAPIs);
            newPhraseObj.policies.add(this);
            phraseObjects.add(newPhraseObj);
        }
        return phraseObjects;
    }

    public String toString(){
        String ret = "Name: " + this.name;
        for(Map.Entry<String, Integer> phrase : phrases.entrySet())
            ret += "\nPhrase: " + phrase.getKey() + " (" + phrase.getValue() + ")";
        for(String api : apis)
            ret+= "\nAPI: " + api;
        return ret;
    }
}
