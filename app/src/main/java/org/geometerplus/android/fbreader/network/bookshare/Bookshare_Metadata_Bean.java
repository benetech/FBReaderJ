package org.geometerplus.android.fbreader.network.bookshare;

public class Bookshare_Metadata_Bean {
	private String contentId;
	private String daisy;
	private String brf;
	private String[] downloadFormats;
	private String images;
	private String isbn;
	private String[] authors;
	private String[] title;
	private String publishDate;
	private String publisher;
	private String copyright;
	private String language;
	private String[] briefSynopsis;
	private String[] completeSynopsis;
	private String quality;
	private String[] category;
	private String bookshareId;
	private String freelyAvailable;

	
	public Bookshare_Metadata_Bean(){
		this.downloadFormats = new String[0];
		this.authors = new String[1];
		this.title = new String[1];
		this.briefSynopsis = new String[1];
		this.completeSynopsis = new String[1];
		this.category = new String[1];
	}
	
	// Getters and Setters
	public void setContentId(String contentId){
		this.contentId = contentId;
	}
	public String getContentId(){
		return this.contentId;
	}
	
	public void setDaisy(String daisy){
		this.daisy = daisy;
	}
	public String getDaisy(){
		return this.daisy;
	}
	
	public void setBrf(String brf){
		this.brf = brf;
	}
	public String getBrf(){
		return this.brf;
	}
	
	public void setDownloadFormats(String[] downloadFormats){
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

	public void setIsbn(String isbn){
		this.isbn = isbn;
	}
	public String getIsbn(){
		return this.isbn;
	}
	
	public void setAuthors(String[] authors){
		this.authors = authors;
	}
	public String[] getAuthors(){
		return this.authors;
	}

	public void setTitle(String[] title){
		this.title = title;
	}
	public String[] getTitle(){
		return this.title;
	}

	public void setPublishDate(String publishDate){
		this.publishDate = publishDate;
	}
	public String getPublishDate(){
		return this.publishDate;
	}
	
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}
	public String getPublisher(){
		return this.publisher;
	}
	
	public void setCopyright(String copyright){
		this.copyright = copyright;
	}
	public String getCopyright(){
		return this.copyright;
	}
	
	public void setLanguage(String language){
		this.language = language;
	}
	public String getLanguage(){
		return this.language;
	}
	
	public void setBriefSynopsis(String[] briefSynopsis){
		this.briefSynopsis = briefSynopsis;
	}
	public String[] getBriefSynopsis(){
		return this.briefSynopsis;
	}

	public void setCompleteSynopsis(String[] completeSynopsis){
		this.completeSynopsis = completeSynopsis;
	}
	public String[] getCompleteSynopsis(){
		return this.completeSynopsis;
	}

	public void setQuality(String quality){
		this.quality = quality;
	}
	public String getQuality(){
		return this.quality;
	}

	public void setCategory(String[] category){
		this.category = category;
	}
	public String[] getCategory(){
		return this.category;
	}

	public void setBookshareId(String bookshareId){
		this.bookshareId = bookshareId;
	}
	public String getBookshareId(){
		return this.bookshareId;
	}
	
	public void setFreelyAvailable(String freelyAvailable){
		this.freelyAvailable = freelyAvailable;
	}
	public String getFreelyAvailable(){
		return this.freelyAvailable;
	}
}
