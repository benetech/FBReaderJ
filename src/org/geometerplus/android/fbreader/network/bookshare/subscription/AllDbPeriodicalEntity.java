package org.geometerplus.android.fbreader.network.bookshare.subscription;

/**
 * @author thushan
 * 
 */
public class AllDbPeriodicalEntity extends PeriodicalEntity {

	public AllDbPeriodicalEntity() {
	}

	public AllDbPeriodicalEntity(String id, String title) {
		super(id, title);
	}

	public AllDbPeriodicalEntity(String id, String title, String edition,
			int revision, String dwnDate, String dwnTime) {
		super(id, title);
		this.edition = edition;
		this.revision = revision;
		this.dwnldDate = dwnDate;
		this.dwnldTime = dwnTime;
	}

	private String edition;
	private int revision;
	private String dwnldDate;
	private String dwnldTime;

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getDwnldDate() {
		return dwnldDate;
	}

	public void setDwnldDate(String dwnldDate) {
		this.dwnldDate = dwnldDate;
	}

	public String getDwnldTime() {
		return dwnldTime;
	}

	public void setDwnldTime(String dwnldTime) {
		this.dwnldTime = dwnldTime;
	}

}
