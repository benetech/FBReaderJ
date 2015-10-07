package org.geometerplus.android.fbreader.network.bookshare;

/**
 * This class acts as a bean object which holds the
 * contents of a result tag returned in "Book List Response"
 *
 */
public class Bookshare_Result_Bean implements Comparable{

	private String id;
	private String title;
	private String[] author;
	private String[] downloadFormats;
	private String images;
	private String freelyAvailable;
	private String availableToDownload;

	public Bookshare_Result_Bean(){
		this.author = new String[1];
		this.downloadFormats = new String[1];
	}
	/**
	 * Getter and setters
	 */
	public void setId(String id){
		this.id = id;
	}
	public String getId(){
		return this.id;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	public String getTitle(){
		return this.title;
	}
	
	public void setAuthor(String[] author){
		this.author = new String[author.length];
		this.author = author;
	}
	public String[] getAuthor(){
		return this.author;
	}
	
	public void setDownloadFormats(String[] downloadFormats){
		this.downloadFormats = new String[downloadFormats.length];
		this.downloadFormats = downloadFormats;
	}
	public String[] getDownloadFormats(){
		return this.downloadFormats;
	}
	
	public void setImages(String images){
		this.images = images;
	}
	public String getImages(){
		return this.images;
	}
	
	public void setFreelyAvailable(String freelyAvailable){
		this.freelyAvailable = freelyAvailable;
	}
	public String getFreelyAvailable(){
		return this.freelyAvailable;
	}
	
	public void setAvailableToDownload(String availableToDownload){
		this.availableToDownload = availableToDownload;
	}
	public String getAvailableToDownload(){
		return this.availableToDownload;
	}
	public int compareTo(Object another) {
		return 0;
	}
}
