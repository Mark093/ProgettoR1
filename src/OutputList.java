import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 15/01/17.
 */
public class OutputList {
    private List<OutputLang> outputs;

    public OutputList() {
        outputs = new ArrayList<>();
        if (Files.isDirectory(Paths.get("out"))) {
            System.out.println("The directory 'out' already exists");
        }
        else {
            System.out.println("The directory 'out' doesn't exist: creating it");
            try {
                Files.createDirectory(Paths.get("out"));
            }
            catch (Exception e) {}
        }
    }

    public void write(PrepTable words) {
        String lang = new String(words.getLanguage());
        for (OutputLang outobj : outputs) {
            if (outobj.getLanguage().equalsIgnoreCase(lang)) {
                System.out.println("Looking for files: "+lang+" found "+outobj.getLanguage());
                System.out.println(outobj.getHFname());
                outobj.writeFile(words);
                return;
            }
        }
        //New language: in this case we create a new file through the object OutputLang
        OutputLang outputobj = new OutputLang(lang);
        outputobj.writeFile(words);
        outputs.add(outputobj);
        return;
    }

}
