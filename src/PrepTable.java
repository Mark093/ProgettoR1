import javafx.util.Pair;
import org.jsoup.nodes.Element;

import java.util.*;

/**
 * Created by marco on 26/10/16.
 * In this class we want to take the table and preprocess it to get the cell values and the headers and all the other info.
 * The stored values will be the list of words contained in the table with all the related headers.
 */
public class PrepTable {
    private static String title;
    private String language;
    private String pos;
    private static Element table;
    private static List<Word> word;
	private static HeaderList headers;
	private static List<Pair<Integer, Integer>> usedcells;

    public PrepTable(Element table, String title, String language, String pos, HeaderList headers) {
    	this.table = table;
        this.title = title;
        this.language = language;
        this.pos = pos;
		this.headers = headers;
		this.word = new ArrayList<>();
		this.usedcells = new ArrayList<>();
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
			//Check if there is already something at the beginning of the row
			//while(findHeaderList(r,c+1,tableHeaders)!=null)
			while(isUsed(r,c+1))
				c++;
        	Element row = tableIterator.next();
        	Iterator<Element> rowIterator = row.children().iterator();
        	while(rowIterator.hasNext()){
				c++;
				//while (findHeaderList(r,c,tableHeaders)!=null) {
				while (isUsed(r,c)) {
					System.out.println("Moved column in the middle: "+title);
					c++;
				}
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
							setUsed(r+i,c+j);
							tableHeaders.add(wh);
						}
					c+=colsp-1;
        		}
        		//else the element could be an inflection word, create the Word and associate its headers to it wrt the position
				//NOTE: we can do this because the headers of a cell MUST BE among the ones we have already seen. We don't care of the next cells.
        		else {
					String inflection = el.text().replaceAll("\\s", " ").trim();
					int colsp = 1;
					int rowsp = 1;
					if (el.hasAttr("colspan")) {
						colsp = Integer.parseInt(el.attr("colspan"));
						System.out.println("The word " + title + " has colspan " + colsp);
					}
					if (el.hasAttr("rowspan")) {
						rowsp = Integer.parseInt(el.attr("rowspan"));
						System.out.println("The word " + title + " has rowspan " + rowsp);
					}
					for (int i = 0; i < rowsp; i++)
						for (int j = 0; j < colsp; j++)
							setUsed(r + i, c + j);
					//Special case: the words with rowspan greater than one are ignored for now
					if (!inflection.isEmpty() && !inflection.matches("-|—")) {
						if (rowsp == 1) {
							//create a Word element at the beginning without headers
							Word word1 = new Word(inflection);
							for (int i = 0; i < colsp; i++) {
								List<WordHeaders> wordheads = new ArrayList<>();
								//For each header we have, we create a new headers for the word, calculating the distance from the headers and the word in the table
								//by checking if the header is ok for us or not.
								//Step 1: column/row headers
								for (WordHeaders head : tableHeaders) {
									int rowdist = r - head.getRowdistance();
									int coldist = c - head.getColdistance();
									WordHeaders head1 = new WordHeaders(rowdist, coldist, head.getHeader());
									wordheads.add(head1);
								}
								if (wordheads.size() > 0) {
									//Step 2: select only the headers of interest
									WordHeaders minwh = null, wh;
									//Step 2.1: check on the columns
									List<WordHeaders> filteredcolheaders = new ArrayList<>();
									//Step 2.1.1: find the nearest header (on the column)
									Iterator<WordHeaders> coliterator = wordheads.iterator();
									if (coliterator.hasNext()) {
										wh = coliterator.next();
										while (coliterator.hasNext() && wh.getColdistance() != 0) {
											wh = coliterator.next();
										}
										if (wh.getColdistance() == 0) {
											minwh = wh;
											while (coliterator.hasNext()) {
												wh = coliterator.next();
												if (wh.getColdistance() == 0 && wh.getRowdistance() < minwh.getRowdistance())
													minwh = wh;
											}
											filteredcolheaders.add(minwh);
											//Step 2.1.2: find the consecutive ones and check that we need them
											wh = findHeaderList(minwh.getRowdistance() + 1, 0, wordheads);
					/*	In practice below it checks that the cell on the left or the one on the right is an header too, otherwise it stops.*/
											while (wh != null && (findHeaderList(wh.getRowdistance(), 1, wordheads) != null || findHeaderList(wh.getRowdistance(), -1, wordheads) != null)) {
												filteredcolheaders.add(wh);
												wh = findHeaderList(wh.getRowdistance() + 1, 0, wordheads);
											}
										}
									}
									//Step 2.2: check on the rows... As for the columns
									List<WordHeaders> filteredrowheaders = new ArrayList<>();
									//Step 2.2.1: find the nearest header (on the row)
									int count = 1;
									wh = findHeaderList(0, count, wordheads);
									while (count < 20 && wh == null) {
										count++;
										wh = findHeaderList(0, count, wordheads);
									}
									while (count < 20 && wh != null) {
										count++;
										filteredrowheaders.add(wh);
										wh = findHeaderList(0, count, wordheads);
									}
									//Step 3: get the corner headers by using the obtained two lists... and add all in the word list
									List<WordHeaders> filteredcornheaders = new ArrayList<>();
									if (filteredrowheaders.size() > 0 && filteredcolheaders.size() > 0) {
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
									Set<String> rowheaders = collapseHeaders(filteredrowheaders);
									Set<String> colheaders = collapseHeaders(filteredcolheaders);
									Set<String> cornheaders = collapseHeaders(filteredcornheaders);
									word1.addHeaders(rowheaders);
									word1.addHeaders(removeRepetitions(colheaders, cornheaders));
								}
							}
							word1.PrintHeaders();
							//Add the obtained word in our list
							word.add(word1);
						}
					}
					c += colsp - 1;
        		}
    			//Output done: in the word list.
        	}
        }
    }

    private static WordHeaders findHeaderList(int rowdist, int coldist, List<WordHeaders> whlist) {
		for (WordHeaders wh : whlist)
			if (wh.getRowdistance()==rowdist && wh.getColdistance()==coldist)
				return wh;
		return null;
	}

	private static boolean isUsed(int row, int col) {
		for (Pair<Integer, Integer> pair : usedcells) {
			if (pair.getKey().intValue()==row && pair.getValue().intValue()==col)
				return true;
		}
		return false;
	}

	private static void setUsed(int row, int col) {
		Pair<Integer,Integer> pair = new Pair<>(row,col);
		usedcells.add(pair);
	}

	public static Set<String> collapseHeaders(List<WordHeaders> hlist) {
		Set<String> retlist = new TreeSet<>();
		for (WordHeaders wh : hlist) {
			retlist.add(wh.getHeader());
		}
		return retlist;
	}

	public static Set<String> removeRepetitions(Set<String> collist, Set<String> cornlist) {
		Set<String> retlist = new TreeSet<>();
		for (String word : collist) {
			if (!cornlist.contains(word)) {
				retlist.add(word);
			}
		}
		for (String word : cornlist) {
			if (!collist.contains(word)) {
				retlist.add(word);
			}
		}
		return retlist;
	}

	//Setters
	public void addWord(Word s_word) {
		word.add(s_word);
	}

	//Getters
	public List<Word> getWords() {
		return word;
	}

	public String getLanguage() {
		return language;
	}

	public String getPos() {
		return pos;
	}

	public String getTitle() {
		return title;
	}
    
}