package edu.utsa.cs.sefm.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rocky on 5/25/2015.
 * <p/>
 * Manipulates and searches through text. For use with privacy
 * policies.
 */
public class Text {
    public String text;
    private List<String> associators;

    public Text(String text, List<String> associators) {
        this.text = text;
        this.associators = associators;
    }

    /**
     * Reads a word file into a List.
     *
     * @param filepath Newline or comma delimited list of words.
     * @return
     * @throws FileNotFoundException
     */
    public static List<String> getWordList(String filepath) throws FileNotFoundException {
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        List<String> phrases = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null)
                phrases.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));
            br.close();
        } catch (IOException e) {
            System.err.println("Error reading file");
            e.printStackTrace();
        }

        while (phrases.remove(" ")) ;
        for (int i = 0; i < phrases.size(); i++)
            phrases.set(i, phrases.get(i).trim());
        return phrases;
    }

    /**
     * Looks for words or examples associated with a phrase. Works by
     * matching a phrase with terms such as "like, "for example", and "such
     * as".
     *
     * @param phrase
     * @return
     */
    public List<String> findAssociated(String phrase) {
        List<String> found = new ArrayList<>();

        Matcher matcher;
        for (String associator : associators) {
            matcher = Pattern.compile("([^.]*" + phrase + "[^.]*" + associator + "[^.]*)").matcher(this.text);
            while (matcher.find())
                found.add(matcher.group());
        }

        return found;
    }

    public void markAssociators(String color) {
        for (String associator : associators) {
            text = text.replaceAll(associator, "<font color=\"" + color + "\">" + associator + "</font>");
        }
    }

    public void markWord(String word, String color) {
        text = text.replaceAll(word, "<font color=\"" + color + "\">" + word + "</font>");
    }

    public String toHTML() {
        String ret = text.replaceAll("\n\n", "</li>\n<li>");
        return "<html><body>\n<ol>\n<li>\n" + ret.substring(0, ret.length() - 5) + "\n</ol></body></html>";
    }
}
