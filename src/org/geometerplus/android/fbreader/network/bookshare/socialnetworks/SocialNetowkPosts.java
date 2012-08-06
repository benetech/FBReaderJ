package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Metadata_Bean;

import android.nfc.FormatException;

public class SocialNetowkPosts {

	public static String getBookPost(Bookshare_Metadata_Bean bean){
		String post= "Hi friends, I found this interesting book on Bookshare. You might like it too!\n\n\tTitle:"+strArrayToString(bean.getTitle())
		 +"\n\tAuthor: "+strArrayToString(bean.getAuthors())
		 +"\n\tISBN: "+bean.getIsbn()+
		 "\n\tCategory: "+strArrayToString(bean.getCategory());
		
		return post;
	}
	
	public static String getShortenedBookPost(Bookshare_Metadata_Bean bean){
		String post= "Hi, I found a book on Bookshare. Title:"+strArrayToString(bean.getTitle())
		 +" ISBN: "+bean.getIsbn();
		if(post.length()<125){
		 post = post + " Category: "+strArrayToString(bean.getCategory());
		}
		
		return post;
	}
	
	public static String getShortenedPeriodicalPost(Bookshare_Edition_Metadata_Bean bean){
		String post = "Hi, I found a periodical on Bookshare."
				+ " Title: "
				+ bean.getTitle()
				+ " Edition: "
				+ formatEdition(bean.getEdition());
		if(post.length()<125){
				post = post + " Category: "+ bean.getCategory();
		}
		
		return post;
	}
	
	public static String getPeriodicalPost(Bookshare_Edition_Metadata_Bean bean){
		String post = "Hi friends, I found this interesting book on Bookshare. You might like it too!"
				+ "\n\tTitle: "
				+ bean.getTitle()
				+ "\n\tEdition: "
				+ formatEdition(bean.getEdition())
				+ "\n\tCategory: "
				+ bean.getCategory();
		
		return post;
	}
	
	private static String formatEdition(String edition){
		if(edition != null){
		String month = edition.substring(0, 2);
		String date = edition.substring(2,4);
		String year = edition.substring(4,8);
		
		return month+"/"+date+"/"+year;
		}
		return null;
	}
	private static String strArrayToString(String[] arr){
		String concatString = "";
		if(arr==null || arr.length==0 || arr[0]==null){
			return "Not available";
		}
		if(arr.length==1){
			return arr[0];
		}
		for(int i=0;i<arr.length;i++){
			
			if(i==arr.length-1){
				concatString+=arr[i];
			}else{
				concatString+=arr[i] +", ";
			}
		}
		return concatString;
	}
}
