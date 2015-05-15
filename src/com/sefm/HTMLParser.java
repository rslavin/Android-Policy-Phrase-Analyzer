package com.sefm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rocky on 5/15/2015.
 */
public class HTMLParser {
    private String html;

    public HTMLParser(String html){
        this.html = html;
    }

    /**
     * Sanitizes html by removing JavaScript and any html tags. Only
     * plain text remains.
     * @return Plaintext version of html
     */
    public String removeHTMLTags(){
        Document doc = Jsoup.parse(html);
        doc.select("script").remove();
        return Jsoup.clean(doc.body().html(), Whitelist.none());
    }


    /**
     * Counts the occurrences of a substring within a larger String.
     * @param bigString
     * @param subString
     * @return
     */
    public static int countOccurrences(String bigString, String subString){
        Pattern pattern = Pattern.compile(subString);
        Matcher matcher = pattern.matcher(bigString);
        int count = 0;

        while(matcher.find())
            count++;

        return count;
    }
}
