/**
 * Created by marco on 21/10/16.
 * In this class we will read the webpage related to the given word and we will extract data from it.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class HTMLParser {

    private static HeaderList headers;
    /*public HTMLParser( String word ) {
        try {
            Document doc = Jsoup.connect("https://en.wiktionary.org/wiki/" + word).get();
            ParseText(doc, word);
        }
        catch (IOException e) {}
    }*/

    public HTMLParser( Document doc, HeaderList headers ) {
        this.headers=headers;
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
        boolean tableflag=false;
        boolean listflag=false;
        System.out.println("Title: "+word);
        Elements tables= doc.getElementsByTag("table");
        Iterator<Element> tableiterator = tables.iterator();
        while (tableiterator.hasNext()) {
            Element table = tableiterator.next();
            //TODO: implement the class used below and the classes related to it, then choose how to use them
            //The table has always class attribute which contains the word "inflection-table"
            //an example is at https://en.wiktionary.org/wiki/parlare ).
			if (table.className().equalsIgnoreCase("inflection-table")||table.className().contains("inflection-table")){
            		PrepTable p_table = ParseTable(table, word);
                    tableflag=true;
			}
        }
        if (!tableflag) 
        	System.out.println("\tNo tables for this word");
        //Parse the Parenthetical lists. Typically, you would have a h3 section with the POS (Noun, Verb..) and 
        //the next element is the p which contains the parenthetical list of inflection
        //an example is at https://en.wiktionary.org/wiki/parlare
        Elements lists = doc.getAllElements();
        Iterator<Element> listiterator = lists.iterator();
        while (listiterator.hasNext()) {
            Element list = listiterator.next();
            list.tag();
        	//if the actual element is an h3 and has as id a known POS, then the next element is the p containing the inflection list
        	//best example is https://en.wiktionary.org/wiki/able
            if(Tag.isKnownTag("h3") && (list.id().contains("Noun")||list.id().contains("Verb")||list.id().contains("Adjective"))){
        		Element l = listiterator.next();
        		ParseList(l);
        		listflag=true;
            }
        }
        if (!listflag) 
        	System.out.println("\tNo lists for this word");
    }

    //Parser for the single table
    static PrepTable ParseTable(Element table, String title) {
    	String lang = getLanguage(table);
    	String pos = getPOS(table, lang);
        if (lang.isEmpty()) lang = "Language not found";
        if (pos.isEmpty()) pos = "Part of Speech not found";
        if (allowedPos(pos))
            System.out.println("\t" + lang + "\t" + pos);
        return new PrepTable(table, title, lang, pos, headers);
    }

    //Parser for the single list
    static void ParseList(Element list) {
    	String lang = getLanguage(list);
        String pos = getPOS(list, lang);
        if (lang.isEmpty()) lang = "Language not found";
        if (pos.isEmpty()) pos = "Part of Speech not found";
        if (allowedPos(pos))
            System.out.println("\t" + lang + "\t" + pos);
        //in the list, the i contains the header (e.g., comparative, 3rd person,..) and the successive span contains the corresponding inflection
        Iterator<Element> listiterator = list.children().iterator();
        while (listiterator.hasNext()) {
        	//in the list, you have one only header associated to each inflection
        	Element el = listiterator.next();
        	el.tag();
			if(Tag.isKnownTag("i")){
				//TODO: is it ownText???
        		WordHeaders wh = new WordHeaders(1,1,el.ownText());
        		String inflection = listiterator.next().ownText();
        		Word inflectionWord = new Word(inflection, wh);
            	//TODO: output
        	}
        }
    }

    //Get the language of a node (usually of a table)
    static String getLanguage(Element element) {
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
    static String getPOS(Element table, String lang) {
        boolean found=false;
        String pos=new String("");
        Element divFrame=table;
        //Get the node at the same level of the XML tag with the language
        while(!divFrame.parent().id().equalsIgnoreCase("mw-content-text")) {
            divFrame=divFrame.parent();
        }
        //Search the node containing the POS
        while (!found && divFrame!=null) {
            if (divFrame.tagName().equalsIgnoreCase("h3")||divFrame.tagName().equalsIgnoreCase("h4")||divFrame.tagName().equalsIgnoreCase("h5")) {
                Element posel=divFrame.child(0);
                if (posel.className().equalsIgnoreCase("mw-headline")) {
                    //Check if the POS found too is related to the table language
                    if (lang.equalsIgnoreCase(getLanguage(divFrame))) {
                        pos = posel.text();
                    }
                    else {
                        pos="POS language not matched";
                    }
                    found = true;
                }
            }
            divFrame = divFrame.previousElementSibling();
        }
        return pos;
    }

}
