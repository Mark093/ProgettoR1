import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by marco on 26/10/16.
 * In this class we have the single word and the list of the related headers in form row, column, corner...
 */
public class Word {
    private String word;
    private Set<String> headers;

    public Word(String word){
    	this.word = word;
		headers=new TreeSet<>();
	}

	public Word(String word, String header){
		this.word = word;
		headers=new TreeSet<>();
		headers.add(header);
	}

	public void addHeaders(Set<String> headerlist) {
		this.headers.addAll(headerlist);
	}

	public String getWord() {
		return word;
	}

	public Set<String> getHeaders() {
		return headers;
	}

	public void PrintHeaders() {
		System.out.println("Word: "+word);
		for (String wh : this.headers) {
			System.out.println(" * "+wh);
		}
	}
    
}
