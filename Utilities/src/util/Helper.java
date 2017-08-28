package util;

public class Helper {
	public static void main(String[] args) {
		
		System.out.println(getLastName("Vasya Ivanov"));
	}
	
	public static String getFirstName(String cardOwner) {
		String result = null;
		result = cardOwner.split(" ")[0];		
		return result;
	}
	
	public static String getLastName(String cardOwner) {
		String result = null;
		result = cardOwner.split(" ")[1];		
		return result;
	}
}
