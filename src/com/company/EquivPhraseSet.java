package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 5/12/2015.
 */
public class EquivPhraseSet {
    public List<Phrase> phrases;
    public Map<String, Integer> apis;

    public EquivPhraseSet(){
        phrases = new ArrayList<>();
        apis = new HashMap<>();

    }

    /**
     * Add a list of APIs to the equivalency. Updates frequencies.
     * @param apis
     */
    public void addAPIs(List<String> apis){
        for(String api : apis){
            if(this.apis.containsKey(api))
                this.apis.put(api, this.apis.get(api) + 1);
            else
                this.apis.put(api, 1);
        }
    }

    public boolean equals(EquivPhraseSet e2){
        if(e2.phrases.size() != this.phrases.size() || e2.apis.size() != this.apis.size())
            return false;
        for(Phrase p1 : this.phrases){
            if(!e2.phrases.contains(p1))
                return false;
        }
        // TODO use the one with the most keys as the outer loop
        for(Map.Entry<String, Integer> api : apis.entrySet()){
            if(!e2.apis.get(api.getKey()).equals(api.getValue()))
                return false;
        }

        return true;
    }

    public String toString(){
        String ret = "Equivalent Set";
        for(Phrase phrase : phrases){
            ret += "\nPhrase: " + phrase.name;
        }
        for(Map.Entry<String, Integer> api : apis.entrySet())
            ret += "\nAPI: " + api.getKey() + " (" + api.getValue() + ")";
        return ret;
    }

}
