import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by marco on 15/01/17.
 */

public class OutputLang {
    //Keep track of the headers (on the top of the output file
    private String headersfname;
    private String wordsfname;
    private List<String> headers;
    private String language;

    //create a new file (it will erase existing ones)
    public OutputLang(String lang) {
        language = new String(lang);
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
            hout.print("WORD\tPOS\tBASE FORM");
            hout.close();
            hbw.close();
            hfw.close();
        }
        catch (IOException e) {}
        return;
    }

    //Append new words on the file
    public void writeFile(PrepTable words) {
        try {
            System.out.println("Using file: "+headersfname);
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
                newHeaders(word, hout, headers);
                writeChecks(word, words.getPos(), words.getTitle(), headers, wout);
            }
            hout.close();
            hbw.close();
            hfw.close();
            wout.close();
            wbw.close();
            wfw.close();
        }
        catch (IOException e) {}
        return;
    }

    private static void newHeaders(Word word, PrintWriter out, List<String> headers) {
        List<String> newheads = new ArrayList<>();
        for (String head : word.getHeaders()) {
            if (!headers.contains(head))
                newheads.add(head);
        }
        headers.addAll(newheads);
        writeNewHeaders(newheads, out);
        return;
    }

    private static void writeNewHeaders(List<String> newheads, PrintWriter out) {
        for (String head : newheads) {
            out.print("\t"+head);
        }
        return;
    }

    private static void writeChecks(Word word, String pos, String title, List<String> headers, PrintWriter out) {
        Set<String> wordheaders = word.getHeaders();
        String line = word.getWord()+"\t"+pos+"\t"+title;
        for (String head : headers) {
            line += "\t";
            if (wordheaders.contains(head))
                line+="x";
        }
        out.println(line);
        return;
    }

    public String getLanguage() {
        return new String(language);
    }

    public String getHFname() {
        return new String(headersfname);
    }

}
