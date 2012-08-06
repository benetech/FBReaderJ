package org.geometerplus.android.fbreader.subscription;

import java.util.ArrayList;
import java.util.Calendar;

public class PeriodicalDBUtils {

	
	public static AllDbPeriodicalEntity getMostRecentEdition(ArrayList<AllDbPeriodicalEntity> entities){
		if(entities != null){
			AllDbPeriodicalEntity maxEntity = entities.get(0);
			AllDbPeriodicalEntity currEntity = entities.get(0);
			
			for(int i=1; i<entities.size();i++){
				currEntity = entities.get(i);
				if(!maxEntity.getEdition().equals(getRecentEditionString(maxEntity.getEdition(), currEntity.getEdition()))){
					maxEntity = currEntity;
				}
				
			}
			return maxEntity;
		}
		return null;
	}
	
	public static String getRecentEditionString(String edition1,String edition2){
		int month1 = Integer.parseInt(edition1.substring(0, 2));
		int day1 = Integer.parseInt(edition1.substring(2, 4));
		int year1 = Integer.parseInt(edition1.substring(4));
		
		int month2 = Integer.parseInt(edition2.substring(0, 2));
		int day2 = Integer.parseInt(edition2.substring(2, 4));
		int year2 = Integer.parseInt(edition2.substring(4));
		
		Calendar cal1=Calendar.getInstance();
		cal1.set(year1, month1, day1);
		
		Calendar cal2=Calendar.getInstance();
		cal2.set(year2, month2, day2);
		
		if(cal1.after(cal2)){
			return edition1;
		}
		return edition2;
	}
}
