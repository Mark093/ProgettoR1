import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 26/10/16.
 * In this class we have the single word and the list of the related headers in form row, column, corner..
 */
public class Word {
    private String word;
    private List<WordHeaders> headers;

    public Word(String word,List<WordHeaders> headers){
    	this.word = word;
    	this.headers = headers;
    }

    public Word(String word){
    	this.word = word;
		headers=new ArrayList<>();
	}

	public Word(String word, WordHeaders header){
		this.word = word;
		headers=new ArrayList<>();
		headers.add(header);
	}

	/*public WordHeaders getHeader() {
		return header;
	}
*/
	/*public void addHeader(WordHeaders header) {
		this.headers.add(header);
	}*/

	public void addHeaders(List<WordHeaders> headerlist) {
		this.headers.addAll(headerlist);
	}

	public String getWord() {
		return word;
	}

	/*public void setWord(String word) {
		this.word = word;
	}*/

	public List<WordHeaders> getHeaders() {
		return headers;
	}

	public void setHeaders(List<WordHeaders> headerlist) {
		this.headers = new ArrayList<>();
		this.headers.addAll(headerlist);
	}

	public void PrintHeaders() {
		for (WordHeaders wh : this.headers) {
			System.out.println(" * "+wh.getHeader());
		}
	}
    
}
