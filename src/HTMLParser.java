/**
 * Created by marco on 21/10/16.
 * In this class we will read the webpage related to the given word and we will extract data from it.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HTMLParser {

    private static HeaderList headers;
    private static List<String> languages;
    private static OutputList output;

    public HTMLParser( Document doc, HeaderList headers, List<String> langs, OutputList output) {
        this.headers=headers;
        this.output=output;
        languages = langs;
        if (doc!=null)
            ParseText(doc);
    }

    static boolean allowedPos(String pos) {
        boolean flag=false;
        ArrayList<String> allowedpos = new ArrayList<>(Arrays.asList("Noun", "Verb", "Adjective", "Conjugation", "Inflection", "Declension"));
        Iterator<String> iterator = allowedpos.iterator();
        while (iterator.hasNext() && !flag) {
            String element = iterator.next();
            if (element.equalsIgnoreCase(pos)) {
                flag=true;
            }
        }
        return flag;
    }

    //Parser for the HTML page
    static void ParseText(Document doc) {
    	//Get the title word from the doc element, it is in the head and in the form " word - Wiktionary", we only need the word
    	String headTitle = doc.title();
    	String word = headTitle.substring(0, headTitle.indexOf(' '));
        boolean tableflag = false;
        boolean listflag = false;
        System.out.println("Title: "+word);
        Elements tables= doc.getElementsByTag("table");
        Iterator<Element> tableiterator = tables.iterator();
        while (tableiterator.hasNext()) {
            Element table = tableiterator.next();
            //The table has always class attribute which contains the word "inflection-table"
            //an example is at https://en.wiktionary.org/wiki/sprechen 
			if (table.className().equalsIgnoreCase("inflection-table")||table.className().contains("inflection-table")){
                if (allowedLang(getLanguage(table))) {
                    PrepTable p_table = ParseTable(table, word);
                    if (p_table!=null) {
                        tableflag = true;
                        //Output
                        output.write(p_table);
                    }
                }
			}
        }
        if (!tableflag) 
        	System.out.println("\tNo tables for this word");
        //Parse the Parenthetical lists. Typically, you would have a h1-2-3-... section with the POS (Noun, Verb..) and
        //the next element is the p which contains the parenthetical list of inflection. If p has no children nodes, then there is no declension;
        //else, parse the declension
        Elements lists = doc.getAllElements();
        Iterator<Element> listiterator = lists.iterator();
        while (listiterator.hasNext()) {
            Element list = listiterator.next();
            String tag = list.tag().toString();
        	//if the actual element is an h element and has as id a known POS, then the next element is the p containing the inflection list,
            //if the p element has some children nodes
        	//best example is https://en.wiktionary.org/wiki/able
            if(tag.matches("h[1-9]+") && list.id().contains("Noun")||list.id().contains("Noun_2")||list.id().contains("Noun_3")||list.id().contains("Verb")||list.id().contains("Adjective")){
        		//need to access next p element, which is next sibling
            	Element l = list.parent().nextElementSibling();
        		if(l.childNodes().size()>1) {
                    if (allowedLang(getLanguage(l))) {
                        PrepTable p_table = ParseList(l, word);
                        if (p_table!=null) {
                            listflag = true;
                            //Output
                            output.write(p_table);
                        }
                    }
        		}
            }
        }
        if (!listflag) 
        	System.out.println("\tNo lists for this word");
        return;
    }

    //Parser for the single table
    static PrepTable ParseTable(Element table, String title) {
    	String lang = getLanguage(table);
    	String pos = getPOS(table, lang);
        if (lang.isEmpty()) return null;
        if (pos.isEmpty()) return null;
        if (allowedPos(pos))
            System.out.println("\t" + lang + "\t" + pos);
        else
            return null;
        return new PrepTable(table, title, lang, pos, headers);
    }

    //Parser for the single list
    static PrepTable ParseList(Element list, String title) {
    	String lang = getLanguage(list);
        String pos = getPOS(list, lang);
        if (lang.isEmpty()) return null;
        if (pos.isEmpty()) return null;
        PrepTable p_table = new PrepTable(title, lang, pos);
        if (allowedPos(pos))
            System.out.println("\t" + lang + "\t" + pos);
        else
            return null;
        //in the list, the i contains the header (e.g., comparative, 3rd person,..) and the successive element (span, b,..) contains the corresponding inflection
        Iterator<Element> listiterator = list.children().iterator();
        //the first child element is a strong element, which contains the word itself;
        //you use ownText() because it provides the text of this only element, not its children
        String word = listiterator.next().ownText();
        while (listiterator.hasNext()) {
        	//in the list, you have one only header associated to each inflection
        	Element el = listiterator.next();
            Word inflectionWord = null;
            //special case is the span with class "gender" defining the gender attribute, either m or f or both
            if(el.hasClass("gender")){
                String wh = el.text().replaceAll("\\s", " ").trim().toUpperCase();
                inflectionWord = new Word(word,wh);
                System.out.println(word + " " + wh);
            }
            else {
                if (el.tag().isKnownTag("i")) {
                    //process header, which MUST be contained in the known headers;
                    String wh = el.text().replaceAll("\\s", " ").trim().toUpperCase();
                    //process inflection, if it exists
                    if (listiterator.hasNext()) {
                        Element n = listiterator.next();
                        String inflection = n.text();
                        inflectionWord = new Word(inflection, wh);
                        System.out.println(inflection + " " + wh);
                    }
                }
            }
			if (inflectionWord!=null)
			    p_table.addWord(inflectionWord);
        }
        return p_table;
    }

    //Get the language of a node (usually of a table)
    public static String getLanguage(Element element) {
        boolean found=false;
        String lang = new String("");
        //Get the node at the same level of the XML tag with the language
        Element divFrame = element;
        while (!divFrame.parent().id().equalsIgnoreCase("mw-content-text")) {
            divFrame=divFrame.parent();
        }
        //Search the node containing the language
        while (!found && divFrame!=null) {
            if (divFrame.tagName().equalsIgnoreCase("h2")) {
                Element langel=divFrame.child(0);
                if (langel.className().equalsIgnoreCase("mw-headline")) {
                    lang = langel.text();
                    found=true;
                }
            }
            divFrame = divFrame.previousElementSibling();
        }
        return lang;
    }

    //Get the POS of table content
    public static String getPOS(Element el, String lang) {
        boolean found=false;
        ArrayList<String> allowedpos = new ArrayList<>(Arrays.asList("Noun", "Verb", "Adjective"));
        String tag = "";
        String pos=new String("");
        Element divFrame=el;
        //Get the node at the same level of the XML tag with the language
        while(!divFrame.parent().id().equalsIgnoreCase("mw-content-text")) {
            divFrame=divFrame.parent();
        }
        //Search the node containing the POS
        while (!found && divFrame!=null) {
            if (divFrame.tagName().equalsIgnoreCase("h3")||divFrame.tagName().equalsIgnoreCase("h4")||divFrame.tagName().equalsIgnoreCase("h5")) {
                if (divFrame.tagName().equalsIgnoreCase("h3"))
                    tag = "h2";
                else {
                    if (divFrame.tagName().equalsIgnoreCase("h4"))
                        tag = "h3";
                    else
                        tag = "h4";
                }
                Element posel=divFrame.child(0);
                if (posel.className().equalsIgnoreCase("mw-headline")) {
                    //Check if the POS found too is related to the table language
                    if (lang.equalsIgnoreCase(getLanguage(divFrame))) {
                        pos = posel.text();
                    }
                    found = true;
                }
            }
            divFrame = divFrame.previousElementSibling();
        }
        if (allowedPos(pos)) {
            if (!allowedpos.contains(pos)) {
                found = false;
                while (!found && divFrame!=null) {
                    if (divFrame.tagName().equalsIgnoreCase(tag)) {
                        Element posel=divFrame.child(0);
                        if (posel.className().equalsIgnoreCase("mw-headline")) {
                            //Check if the POS found too is related to the table language
                            if (lang.equalsIgnoreCase(getLanguage(divFrame))) {
                                pos = posel.text();
                            }
                            found = true;
                        }
                    }
                    divFrame = divFrame.previousElementSibling();
                }
            }
        }
        if (allowedpos.contains(pos))
            return pos;
        return "POS not allowed";
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
    
}
