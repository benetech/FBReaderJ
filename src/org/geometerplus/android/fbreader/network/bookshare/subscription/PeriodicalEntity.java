package org.geometerplus.android.fbreader.network.bookshare.subscription;

/**
 * @author thushan
 * 
 */
public abstract class PeriodicalEntity {

	private String title;
	private String id;

	public PeriodicalEntity() {
	}

	public PeriodicalEntity(String id, String title) {
		this.id = id;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
