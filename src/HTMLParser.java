/**
 * Created by marco on 21/10/16.
 * In this class we will read the webpage related to the given word and we will extract data from it
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class HTMLParser {


    /*public HTMLParser( String word ) {
        try {
            Document doc = Jsoup.connect("https://en.wiktionary.org/wiki/" + word).get();
            ParseText(doc, word);
        }
        catch (IOException e) {}
    }*/

    public HTMLParser( Document doc ) {
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
            tableflag=true;
            Element table = tableiterator.next();
            //TODO: implement the class used below and the classes related to it, then choose how to use them
            //The table has always class attribute which contains the word "inflection-table"
            //Note that some pages may present more inflection tables, in multiple languages: you would only take 
            //the first one, as it is linked to the one main language; 
            //an example is at https://en.wiktionary.org/wiki/parlare ).
			if (table.className().equalsIgnoreCase("inflection-table")||table.className().contains("inflection-table")){
            		PrepTable p_table = ParseTable(table, word);
            		break;
			}
        }
        if (!tableflag) 
        	System.out.println("\tNo tables for this word");
        //Parse the Parenthetical lists
        Elements lists = doc.getElementsByTag("p");
        Iterator<Element> listiterator = lists.iterator();
        while (listiterator.hasNext()) {
            listflag=true;
            Element list = listiterator.next();
            ParseList(list);
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
        return new PrepTable(table, title, lang, pos);
    }

    //Parser for the single list
    static void ParseList(Element list) {
        boolean noskip=true;
        String lang = getLanguage(list);
        String pos = getPOS(list, lang);
        if (lang.isEmpty()) lang = "Language not found";
        if (pos.isEmpty()) pos = "Part of Speech not found";
        if (allowedPos(pos))
            System.out.println("\t" + lang + "\t" + pos);

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
