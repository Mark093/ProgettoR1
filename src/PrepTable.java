import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by marco on 26/10/16.
 * In this class we want to take the table and preprocess it to get the cell values and the headers and all the other info.
 * The stored values will be the list of words contained in the table with all the related headers.
 */
public class PrepTable {
    private String title;
    private String language;
    private String pos;
    private static Element table;
    private static List<Word> word;
	private static HeaderList headers;

    public PrepTable(Element table, String title, String language, String pos, HeaderList headers) {
    	this.table = table;
        this.title=title;
        this.language=language;
        this.pos=pos;
		this.headers=headers;
		this.word=new ArrayList<>();
		//You can call the function below (preprocessTable) from here.
		preprocessTable();
    }
    
    static void preprocessTable(){
    	//HeaderList headers = th.getHeaders();
    	List<WordHeaders> tableHeaders = new ArrayList<>();
    	Iterator<Element> tableIterator = table.getElementsByTag("tr").iterator();
    	//iterate through all the rows, i.e. tr
    	//you need to specify the position of each element in the table wrt row and column, so keep to integers r and c
    	int r = 0;
    	int c;
        while (tableIterator.hasNext()) {
			r++;	//the row index
			c=0;	//the column index
        	Element row = tableIterator.next();
        	Iterator<Element> rowIterator = row.children().iterator();
        	while(rowIterator.hasNext()){
				c++;
        		Element el = rowIterator.next();
        		//if the element is a header, create the WordHeader and set its position in order to give priority
        		if(headers.getHeaders().keySet().contains(el.text().trim().toUpperCase())){
        			WordHeaders wh = new WordHeaders(r,c,el.text().trim().toUpperCase());
        			tableHeaders.add(wh);
        		}
        		//else the element is an inflection word, create the Word and associate its headers to it wrt the position
        		else{
        			String inflection = el.text().trim();
					//create a Word element at the beginning without headers
					Word word1 = new Word(inflection);
					//For each header we have, we create a new headers for the word, calculating the distance from the headers and the word in the table
					for (WordHeaders head : tableHeaders) {
						WordHeaders head1=new WordHeaders(r-head.getRowdistance(),c-head.getColdistance(),head.getHeader());
						word1.addHeader(head1);
					}
					//TODO: remove the useless headers
        			//Add the obtained word in our list
					word.add(word1);
        		}
    			//Output done: in in the word list.
        	}
        }
    }

	public List<Word> getWord() {
		return word;
	}

	/*public void setWord(List<Word> word) {
		this.word = word;
	}*/
    
    
}