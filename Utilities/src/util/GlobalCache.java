package util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbGlobalMap;
import com.ibm.broker.plugin.MbGlobalMapSessionPolicy;



public class GlobalCache {
	private static String currentDayAmountMap = "currentDayAmount";
	private final static int secondsInOneDay = 86400;
	private static SimpleDateFormat hours = new SimpleDateFormat("HH");
	private static SimpleDateFormat minutes = new SimpleDateFormat("mm");
	private static SimpleDateFormat seconds = new SimpleDateFormat("ss");
	public static void main(String[] args) {
		
	}
	
	
	public static void cacheBrokerValues(String guid, Long requestNumber, Long requestInterval) {
		ArrayList<Long> lst = new ArrayList<>();
		lst.add(requestNumber);
		lst.add(requestInterval);
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap("BrockerVariables", new MbGlobalMapSessionPolicy(300));
			globalMap.put(guid, lst);			
		} catch (MbException e) {
			e.printStackTrace();
		} 
	}
	
	public static Long getInterval(String guid) {
		Long result = 7L;
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap("BrockerVariables");
			if (globalMap.containsKey(guid)) {
				ArrayList<Long> lst = (ArrayList<Long>)globalMap.get(guid);			
				result = lst.get(1);
			}			
		} catch (MbException e) {		
				e.printStackTrace();			
		} 
		return result;
	}
	
	public static synchronized void restoreCurrentDayAmount(String clientId, Long amount) {
		try {			
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap(currentDayAmountMap);
			if(globalMap.containsKey(clientId)) {
				Long currentDayAmount = getCurrentDayAmount(clientId);
				String sum = String.valueOf(currentDayAmount - amount);
				globalMap.update(clientId, sum);	
			}
			else {
				globalMap.put(clientId, amount);
			}	
		} catch (MbException e) {
			e.printStackTrace();
		}
	}
	
	public static Long getNumber(String guid) {
		Long result = 5L;
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap("BrockerVariables");
			if (globalMap.containsKey(guid)) {
				ArrayList<Long> lst = (ArrayList<Long>)globalMap.get(guid);				
				result = lst.get(0);

			}
		} catch (MbException e) {
			e.printStackTrace();
		} 
		return result;
	}
	
	public static void cacheReplyIdentifier(String guid, String repId ){
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap("Map", new MbGlobalMapSessionPolicy(300));
			globalMap.put(guid, repId);
			
		} catch (MbException e) {
			e.printStackTrace();
		}
	}
	
	public static String getReplyIdentifier(String messageId) {
		String result = null;
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap("Map");
			result = (String)globalMap.get(messageId);
		} catch (MbException e) {
			e.printStackTrace();
		}	
		return result;
	}
	
	public static synchronized Boolean checkCurrentDayAmmount(String clientId, Long amount) {
		Long currentDayAmount = getCurrentDayAmount(clientId);
		if (amount + currentDayAmount > 100_000) {
			return false;
		}
		cacheCurrentDayAmount(clientId, String.valueOf(amount));
		return true;
	}
	
	public static void cacheCurrentDayAmount(String clientId, String amount) {
		String currentAmount = null;
		long currentTimeMillis = System.currentTimeMillis();
		int current_time_hours = Integer.parseInt(hours.format(currentTimeMillis));
		int current_time_minutes = Integer.parseInt(minutes.format(currentTimeMillis));
		int current_time_seconds = Integer.parseInt(seconds.format(currentTimeMillis));
		int ttl = secondsInOneDay - (current_time_hours * 60 * 60 + current_time_minutes * 60 + current_time_seconds);
		try {			
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap(currentDayAmountMap, new MbGlobalMapSessionPolicy(ttl));
			if(globalMap.containsKey(clientId)) {
				currentAmount = (String)globalMap.get(clientId);
				Integer sum = Integer.parseInt(currentAmount) + Integer.parseInt(amount);
				globalMap.update(clientId, String.valueOf(sum));
				
			}
			else {
				globalMap.put(clientId, amount);
			}	
		} catch (MbException e) {
			
			e.printStackTrace();
		} 
	}
	
	public static Long getCurrentDayAmount(String clientId) {
		Long result = 0L;
		try {
			MbGlobalMap globalMap = MbGlobalMap.getGlobalMap(currentDayAmountMap);
			if (globalMap.containsKey(clientId)) {
				result = Long.parseLong((String) globalMap.get(clientId));
			}
		} catch (MbException e) {
			e.printStackTrace();
		} 
		return result;		
	}
}
