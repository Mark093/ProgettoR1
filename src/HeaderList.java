import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by marco on 27/10/16.
 * This class has to get te file and use this to compute all the headers names (frequency of the word in tables check)
 */
public class HeaderList {
    //'frequence' is the min number of times that a word has to appear in order to be considered header.
    private static int frequence;
    private static int refresh_freq;
    private static List<String> languages;
    private Map<String, Integer> headers = new HashMap<>();
    private int pages_seen;

    public HeaderList(int ref_freq, int thr_freq, List<String> langs) {
        pages_seen=0;
        refresh_freq = ref_freq;
        frequence = thr_freq;
        languages = langs;
    }

    public Map<String, Integer> getHeaders() {
        return new HashMap<>(headers);
    }

    public int getRefreshFreq() {
        return refresh_freq;
    }

    public static int getThrFrequence() {
        return frequence;
    }

    //This function is used in order to learn continuously new headers
    public void addTables(Document doc) {
        Map<String, Integer> pageheaders = getPageCells(doc);
        headers=addCells(pageheaders,headers);
        pages_seen++;
        //In order to don't store too many values in the hashmap, sometimes (it depends on refresh_freq) we delete the values that are not headers
        if (pages_seen%refresh_freq==0) {
            headers=removeNotFrequent(headers);
            System.out.println("Not frequent headers removed");
        }
    }

    //Prints all the headers
    public void printHeaders() {
        Set<String> keys = headers.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println(key+", "+headers.get(key).toString());
        }
    }

    //Parse the tables of the page and stores all the words contained in it with counter 1
    private static Map<String, Integer> getPageCells(Document doc) {
        Map<String, Integer> headers=new HashMap<>();
        //Get all the tables
        if (doc != null) {
            Elements tables = doc.getElementsByTag("table");
            //Extract the words from the cells and check them
            for (Element table : tables) {
                if (!(table.className().equalsIgnoreCase("audiotable") || table.className().equalsIgnoreCase("toc"))) {
                    if (allowedLang(HTMLParser.getLanguage(table))) {
                        Elements rows = table.getElementsByTag("tr");
                        for (Element row : rows) {
                            Elements cells = row.children();
                            for (Element cell : cells) {
                                if (cell.hasText()) {
                                    String celltext = cell.text().replaceAll("\\s", " ").trim().toUpperCase();
                                    if (!celltext.isEmpty() && !celltext.matches("-|—")) {
                                        if (!headers.containsKey(celltext)) {
                                            headers.put(celltext, new Integer(1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return headers;
    }

    private static boolean allowedLang(String lang) {
        //languages null means that we want to parse all the languages
        if (languages == null)
            return true;
        for (String all_lang : languages) {
            if (all_lang.equalsIgnoreCase(lang))
                return true;
        }
        return false;
    }

    private static Map<String, Integer> removeNotFrequent(Map<String, Integer> headers) {
        Iterator<Map.Entry<String,Integer>> iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Integer> entry = iter.next();
            if(entry.getValue().intValue()<frequence){
                iter.remove();
            }
        }
        return headers;
    }

    private static Map<String,Integer> addCells(Map<String,Integer> pageheaders, Map<String,Integer> headers) {
        Iterator<Map.Entry<String,Integer>> iter = pageheaders.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Integer> entry = iter.next();
            String text=entry.getKey();
            if (headers.containsKey(text)) {
                headers.put(text, headers.get(text) + 1);
            }
            else {
                headers.put(text,new Integer(1));
            }
        }
        return headers;
    }

}
