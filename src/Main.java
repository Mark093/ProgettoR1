import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 20/10/16.
 * Open the XML Wiktionary dump and starts parsing it.
 */



public class Main {

    public static void main(String argv[]) {

        //Open the XML file and parse its "titles" for listing the pages that we have to check on Wiktionary
        String nFile = "enwiktionary-20160305-pages-articles.xml";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        String fname=null;
        int refresh_freq = 400;
        int threshold_freq = 3;
        List<String> languages=null;
        String startword = null;

        switch (argv.length) {
            case 0:
                System.out.println("Using the default parameters: trying to parse all languages");
                break;
            case 1:
                fname = argv[0];
                System.out.println("Loading languages file: "+fname);
                break;
            case 3:
                fname = argv[0];
                System.out.println("Loading languages file: "+fname);
                refresh_freq = Integer.parseInt(argv[1]);
                threshold_freq = Integer.parseInt(argv[2]);
                System.out.println("Using custom parameters");
                break;
            case 4:
                fname = argv[0];
                System.out.println("Loading languages file: "+fname);
                refresh_freq = Integer.parseInt(argv[1]);
                threshold_freq = Integer.parseInt(argv[2]);
                System.out.println("Using custom parameters");
                nFile = argv[3];
                System.out.println("Using custom Wiktionary dump: "+nFile);
                break;
            case 5:
                fname = argv[0];
                System.out.println("Loading languages file: "+fname);
                refresh_freq = Integer.parseInt(argv[1]);
                threshold_freq = Integer.parseInt(argv[2]);
                System.out.println("Using custom parameters");
                nFile = argv[3];
                System.out.println("Using custom Wiktionary dump: "+nFile);
                startword = argv[4];
                System.out.println("Starting from the word: "+startword);
                break;
            default:
                System.out.println("Number of arguments different from 0, 1, 3, and 5: arguments ignored.");
                System.out.println("The admissible arguments are:\n * languages_filename\n * languages_filename refresh_freq threshold_freq");
                System.out.println(" * languages_filename refresh_freq threshold_freq wiktionary_dump");
                System.out.println(" * languages_filename refresh_freq threshold_freq wiktionary_dump start_word");
                System.out.println("examples:\n * java -jar ProgettoR1.jar lang.txt\n * java -jar ProgettoR1.jar lang.txt 200 4");
                System.out.println(" * java -jar ProgettoR1.jar lang.txt 200 4 dump.xml");
                System.out.println(" * java -jar ProgettoR1.jar lang.txt 200 4 dump.xml mullein");
                break;
        }
        if (fname!=null) {
            languages=loadLanguages(fname);
        }
        if (languages != null) {
            System.out.println("List of languages to parse:");
            for (String lang : languages) {
                System.out.println("\t- "+lang);
            }
        }
        try {
            SAXParser parser = factory.newSAXParser();
            File file = new File(nFile);
            TitlesHandler handler;
            if (startword == null)
                handler = new TitlesHandler(refresh_freq, threshold_freq, languages);
            else
                handler = new TitlesHandler(refresh_freq, threshold_freq, languages, startword);
            parser.parse(file, handler);
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private static List<String> loadLanguages(String lan_filename) {
        List<String> lan_list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(lan_filename));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lan_list.add(line.trim());
                }
                line = reader.readLine();
            }
        }
        catch (Exception e) {}
        if (lan_list.size()<=0)
            return null;
        return lan_list;
    }

}
