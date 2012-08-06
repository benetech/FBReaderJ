package org.geometerplus.android.fbreader.subscription;

public class SubscribedDbPeriodicalEntity extends PeriodicalEntity{

	public SubscribedDbPeriodicalEntity(){}
	
	public SubscribedDbPeriodicalEntity(String id, String title) {
		super(id, title);
	}
	public SubscribedDbPeriodicalEntity(String id, String title, String latestEdition, int latestRevision) {
		super(id, title);
		this.latestEdition = latestEdition;
		this.latestRevision = latestRevision;
	}
	private String latestEdition;	//latest edition of the periodical we currently have
	private int latestRevision; //latest revision
	
	public String getLatestEdition() {
		return latestEdition;
	}
	public void setLatestEdition(String latestEdition) {
		this.latestEdition = latestEdition;
	}
	public int getLatestRevision() {
		return latestRevision;
	}
	public void setLatestRevision(int latestRevision) {
		this.latestRevision = latestRevision;
	}
	
	
}
