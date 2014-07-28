package org.geometerplus.android.fbreader.network.bookshare;

public class Bookshare_Periodical_Edition_Bean {

	private String id;	//Id for the periodical which will be used to fectch editions and revisoins
						//belonging to it
	private String title;	//Title of the Magazine
	private String edition;
	private String revision;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	
	
	
}
