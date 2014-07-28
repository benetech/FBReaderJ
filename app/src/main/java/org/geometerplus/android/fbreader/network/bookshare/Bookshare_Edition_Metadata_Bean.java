
package org.geometerplus.android.fbreader.network.bookshare;

import java.io.Serializable;

public class Bookshare_Edition_Metadata_Bean implements Serializable{
	private String periodicalId;	//This id is common for different editions of same periodical
	private String contentId;	//This id is unique for each edition
	private String daisy;
	private String brf;
	private String[] downloadFormats;
	private String images;
	private String edition;
	private String title;
	private String revision;
	private String revisionTime;
	private String category;
	private String freelyAvailable;
	
	
	public String getPeriodicalId() {
		return periodicalId;
	}

	public void setPeriodicalId(String periodicalId) {
		this.periodicalId = periodicalId;
	}

	public Bookshare_Edition_Metadata_Bean(){
		downloadFormats=new String[0];
		freelyAvailable=0+"";
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getDaisy() {
		return daisy;
	}

	public void setDaisy(String daisy) {
		this.daisy = daisy;
	}

	public String getBrf() {
		return brf;
	}

	public void setBrf(String brf) {
		this.brf = brf;
	}

	public String[] getDownloadFormats() {
		return downloadFormats;
	}

	public void setDownloadFormats(String[] downloadFormats) {
		this.downloadFormats = downloadFormats;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getRevisionTime() {
		return revisionTime;
	}

	public void setRevisionTime(String revisionTime) {
		this.revisionTime = revisionTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFreelyAvailable() {
		return freelyAvailable;
	}

	public void setFreelyAvailable(String freelyAvailable) {
		this.freelyAvailable = freelyAvailable;
	}
	
	
}