import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 15/01/17.
 */
public class OutputList {
    private Map<String,OutputLang> outputs;

    public OutputList() {
        outputs = new HashMap<>();
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
        String lang = words.getLanguage();
        if (outputs.containsKey(lang)) {
            //In this case we have already an output file to use
            OutputLang outputobj = outputs.get(lang);
            outputobj.writeFile(words);
        }
        else {
            //New language: in this case we create a new file through the object OutputLang
            OutputLang outputobj = new OutputLang(lang);
            outputobj.writeFile(words);
            outputs.put(lang,outputobj);
        }
    }

}
