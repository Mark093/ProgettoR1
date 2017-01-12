import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by marco on 27/10/16.
 * This class has to get te file and use this to compute all the headers names (frequency of the word in tables check)
 */
public class HeaderList {
    //'frequence' is the min number of times that a word has to appear in order to be considered header.
    private static final int frequence=3;
    private static final int refresh_freq=400;
    private Map<String, Integer> headers = new HashMap<>();
    private int pages_seen;

    public HeaderList() {
        pages_seen=0;
    }

    public Map<String, Integer> getHeaders() {
        return new HashMap<>(headers);
    }

    //Given the cell String, it returns the frequency of the word if it is greater than the threshold, otherwise returns 0
    public int getHeader(String cell) {
        if (headers.containsKey(cell)) {
            int frvalue=headers.get(cell).intValue();
            if (frvalue>=frequence) {
                return frvalue;
            }
        }
        return 0;
    }

    public int getRefreshFreq() {
        return refresh_freq;
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

    private static Map<String, Integer> getPageCells(Document doc) {
        Map<String, Integer> headers=new HashMap<>();
        //Get all the tables
        if (doc != null) {
            Elements tables = doc.getElementsByTag("table");
            //Extract the words from the cells and check them
            for (Element table : tables) {
                if (!(table.className().equalsIgnoreCase("audiotable") || table.className().equalsIgnoreCase("toc"))) {
                    Elements rows = table.getElementsByTag("tr");
                    for (Element row : rows) {
                        Elements cells = row.children();
                        for (Element cell : cells) {
                            if (cell.hasText()) {
                                String celltext = cell.text().trim().toUpperCase();
                                if (!celltext.isEmpty()) {
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
        return headers;
    }

    // WHAT IF WE CHECK THE KIND OF CELL AND SET AN HIGHER VALUE IF THE CELL IS A TH (AS IN THE PREVIOUS VERSION)
    private static Map<String, Integer> frequentCells(String page/*, Map<String,Integer> headers*/){
        //parse cells push them into a Map (hint: remove whitespaces at the beginning and the end of the string by using trim())
        Map<String, Integer> headers = new HashMap<>();
        try {
            //Open the Wiktionary page
            Document doc = Jsoup.connect("https://en.wiktionary.org/wiki/" + page).get();
            headers=getPageCells(doc);
        }
        catch (IOException e) {}
        return headers;
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
