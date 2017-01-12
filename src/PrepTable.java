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
        this.title = title;
        this.language = language;
        this.pos = pos;
		this.headers = headers;
		this.word = new ArrayList<>();
		//You can call the function below (preprocessTable) from here.
		preprocessTable();
    }

    public PrepTable (String title, String language, String pos) {
		this.title = title;
		this.language = language;
		this.pos = pos;
		this.word = new ArrayList<>();
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
					//DONE: check the dimension of the cell: if it's more than 1x1, then replicate it.
					int rowsp=1, colsp=1;
					if (el.hasAttr("rowspan"))
						rowsp=Integer.parseInt(el.attr("rowspan")); //the height of the cell
					if (el.hasAttr("colspan"))
						colsp=Integer.parseInt(el.attr("colspan")); //the width of the cell
					for (int i=0; i<rowsp; i++)
						for (int j=0; j<colsp; j++) {
							WordHeaders wh = new WordHeaders(r+i,c+j,el.text().trim().toUpperCase());
							tableHeaders.add(wh);
						}
        		}
        		//else the element is an inflection word, create the Word and associate its headers to it wrt the position
				//NOTE: we can do this because the headers of a cell MUST BE among the ones we have already seen. We don't care of the next cells.
        		else{
        			String inflection = el.text().trim();
					//create a Word element at the beginning without headers
					Word word1 = new Word(inflection);
					List<WordHeaders> wordheads = new ArrayList<>();
					//For each header we have, we create a new headers for the word, calculating the distance from the headers and the word in the table
					//by checking if the header is ok for us or not.
					//Step 1: column/row headers
					for (WordHeaders head : tableHeaders) {
						int rowdist = r-head.getRowdistance();
						int coldist = c-head.getColdistance();
						WordHeaders head1=new WordHeaders(rowdist,coldist,head.getHeader());
						wordheads.add(head1);
					}
					//System.out.print("Number of headers for the word: ");
					//System.out.print(word1.getWord());
					//System.out.print(": ");
					//System.out.println(wordheads.size());
					if (wordheads.size()>0) {
						//Step 2: select only the headers of interest
						WordHeaders minwh, wh;
						//Step 2.1: check on the columns
						List<WordHeaders> filteredcolheaders = new ArrayList<>();
						//Step 2.1.1: find the nearest header (on the column)
						Iterator<WordHeaders> coliterator = wordheads.iterator();
						if (coliterator.hasNext()) {
							minwh = coliterator.next();
							while (coliterator.hasNext() && minwh.getColdistance() != 0) {
								minwh = coliterator.next();
							}
							while (coliterator.hasNext()) {
								wh = coliterator.next();
								if (wh.getColdistance() == 0 && wh.getRowdistance() < minwh.getRowdistance())
									minwh = wh;
							}
							filteredcolheaders.add(minwh);
							//Step 2.1.2: find the consecutive ones and check that we need them
							wh = findHeaderList(minwh.getRowdistance() + 1, 0, wordheads);
					/*	In practice below it checks that the cell on the left is an header too, otherwise it stops.*/
							while (wh != null && (findHeaderList(wh.getRowdistance(), 1, wordheads) != null/*||findHeaderList(wh.getRowdistance(),-1,wordheads)!=null*/)) {
								filteredcolheaders.add(wh);
								wh = findHeaderList(wh.getRowdistance() + 1, 0, wordheads);
							}
						}
						//Step 2.2: check on the rows... As for the columns
						List<WordHeaders> filteredrowheaders = new ArrayList<>();
						//Step 2.2.1: find the nearest header (on the row)
						Iterator<WordHeaders> rowiterator = wordheads.iterator();
						if (rowIterator.hasNext()) {
							minwh = rowiterator.next();
							while (rowiterator.hasNext() && minwh.getRowdistance() != 0) {
								minwh = rowiterator.next();
							}
							while (rowiterator.hasNext()) {
								wh = rowiterator.next();
								if (wh.getRowdistance() == 0 && wh.getColdistance() < minwh.getColdistance()) {
									minwh = wh;
								}
							}
							filteredrowheaders.add(minwh);
							//Step 2.2.2: find the consecutive ones and add in the list
							wh = findHeaderList(0, minwh.getColdistance() + 1, wordheads);
							while (wh != null) {
								filteredrowheaders.add(wh);
								wh = findHeaderList(0, wh.getColdistance() + 1, wordheads);
							}
						}
						//Step 3: get the corner headers by using the obtained two lists... and add all in the word list
						List<WordHeaders> filteredcornheaders = new ArrayList<>();
						if (filteredrowheaders.size()>0 && filteredcolheaders.size()>0) {
							for (WordHeaders colhead : filteredcolheaders) {
								for (WordHeaders rowhead : filteredrowheaders) {
									wh = findHeaderList(colhead.getRowdistance(), rowhead.getColdistance(), wordheads);
									if (wh != null) {
										filteredcornheaders.add(wh);
									}
								}
							}
						}
						//Step 4: add the lists to the word
						word1.setHeaders(filteredrowheaders);
						for (WordHeaders wh1 : filteredcolheaders) {
							word1.addHeader(wh1);
						}
						for (WordHeaders wh1 : filteredcornheaders) {
							word1.addHeader(wh1);
						}
					}
        			//Add the obtained word in our list
					word.add(word1);
        		}
    			//Output done: in in the word list.
        	}
        }
    }

    private static WordHeaders findHeaderList(int rowdist, int coldist, List<WordHeaders> whlist) {
		for (WordHeaders wh : whlist)
			if (wh.getRowdistance()==rowdist && wh.getColdistance()==coldist)
				return wh;
		return null;
	}

	public List<Word> getWord() {
		return word;
	}

	public void addWord(Word s_word) {
		word.add(s_word);
	}
    
    
}