package org.geometerplus.android.fbreader.network.bookshare;

/**
 * 
 * A POJO representing a Bookshare Organization Member
 *
 */
public class Bookshare_OM_Member_Bean {
    
    public static final String MEMBER_ID = "memberId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    
	private String memberId;
	private String firstName;
	private String LastName;

	/**
	 * Getters and Setters
	 */
	public void setMemberId(String memberId){
		this.memberId = memberId;
	}
	public String getMemberId(){
		return memberId;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setLastName(String lastName){
		this.LastName = lastName;
	}
	public String getlastName(){
		return LastName;
	}	
}
