/**
 * Created by marco on 26/10/16.
 * in this class we store the headers:
 *  - if it is a row header the rowdistance will be 0
 *  - if it is a column header the coldistance will be 0
 *  - otherwise it is a corner header
 */
public class WordHeaders {
    private String header;
    private int rowdistance;
    private int coldistance;
    
    public WordHeaders(int rowdistance, int coldistance, String header){
    	this.rowdistance = rowdistance;
    	this.coldistance = coldistance;
    	this.header = header;
    }

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getRowdistance() {
		return rowdistance;
	}

	public void setRowdistance(int rowdistance) {
		this.rowdistance = rowdistance;
	}

	public int getColdistance() {
		return coldistance;
	}

	public void setColdistance(int coldistance) {
		this.coldistance = coldistance;
	}
}
