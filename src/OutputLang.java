import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by marco on 15/01/17.
 */

public class OutputLang {
    //Keep track of the headers (on the top of the output file
    private static String headersfname;
    private static String wordsfname;
    private static List<String> headers;

    //create a new file (it will erase existing ones)
    public OutputLang(String lang) {
        headers = new ArrayList<>();
        headersfname = new String("out/headers_"+lang+".txt");
        wordsfname = new String("out/table_"+lang+".txt");
        File file = new File(headersfname);
        if (file.exists())
            file.delete();
        file = new File(wordsfname);
        if (file.exists())
            file.delete();
        try {
            FileWriter hfw = new FileWriter(headersfname, true);
            BufferedWriter hbw = new BufferedWriter(hfw);
            PrintWriter hout = new PrintWriter(hbw);
            hout.print("WORD\tPOS");
        }
        catch (IOException e) {}
    }

    //Append new words on the file
    public void writeFile(PrepTable words) {
        try {
            //Headers filehandler declaration
            FileWriter hfw = new FileWriter(headersfname, true);
            BufferedWriter hbw = new BufferedWriter(hfw);
            PrintWriter hout = new PrintWriter(hbw);
            //Words filehandler declaration
            FileWriter wfw = new FileWriter(wordsfname, true);
            BufferedWriter wbw = new BufferedWriter(wfw);
            PrintWriter wout = new PrintWriter(wbw);
            //For each word in PrepTable
            for (Word word : words.getWords()) {
                //Check for new headers
                newHeaders(word, hout);
                writeChecks(word, words.getPos(), wout);
            }
        }
        catch (IOException e) {}
    }

    private static void newHeaders(Word word, PrintWriter out) {
        List<String> newheads = new ArrayList<>();
        for (String head : word.getHeaders()) {
            if (!headers.contains(head))
                newheads.add(head);
        }
        headers.addAll(newheads);
        writeNewHeaders(newheads, out);
    }

    private static void writeNewHeaders(List<String> newheads, PrintWriter out) {
        for (String head : newheads) {
            out.print("\t"+head);
        }
    }

    private static void writeChecks(Word word, String pos, PrintWriter out) {
        Set<String> wordheaders = word.getHeaders();
        String line = word.getWord()+"\t"+pos;
        for (String head : headers) {
            line += "\t";
            if (wordheaders.contains(head))
                line+="x";
        }
        out.println(line);
    }

}
