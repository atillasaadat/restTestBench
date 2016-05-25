/**
 * @(#)bench.java
 *
 * shopify application
 *
 * @Atilla Saadat
 * @version 1.00 2016/5/25
 *
 * Library imported from JSON jar found here: http://central.maven.org/maven2/org/json/json/20151123/json-20151123.jar
 * 
 */
 
import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.*;

public class bench {

  //---------------------------------------------------------------------------------------------------
  // JSON Parse and read funtions
  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    
    try{
    	InputStream is = new URL(url).openStream();
    	try {
      		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      		String jsonText = readAll(rd);
      		JSONObject json = new JSONObject(jsonText);
      		return json;
    		} finally {
      		is.close();
    	}
    }catch(FileNotFoundException e){
    	return null;
    }
  }
  
  //---------------------------------------------------------------------------------------------------
  // return all the pages as JSONObjects
  public static ArrayList<JSONObject> getPages (String jsonLink) throws IOException, JSONException {
  	int pageCount = 1;
  	ArrayList<JSONObject> pages = new ArrayList<JSONObject>();
  	while(true){
    	JSONObject page = readJsonFromUrl(jsonLink+pageCount+".json");
    	if(page == null){
    		break;
    	}
    	else{
    		pages.add(page);
    		pageCount++;
    	}
    }
    return pages;
  }
  //---------------------------------------------------------------------------------------------------
  // return the sum of all the transaction from all the pages
  public static double findTotalBalance(ArrayList<JSONObject> pages) throws IOException, JSONException{
    double totalBalance = 0;
    
    for(JSONObject currentPage: pages){
    	int pageLength = currentPage.getJSONArray("transactions").length();
    	for(int i = 0; i < pageLength; i++){
    		JSONObject currentTransaction = currentPage.getJSONArray("transactions").getJSONObject(i);
    		
    		totalBalance += Double.parseDouble(currentTransaction.getString("Amount"));
    		currentTransaction.put("Company",fixCompanyName(currentTransaction.getString("Company")));
    		
    	}
    }   
    
    return totalBalance;
  }
  //---------------------------------------------------------------------------------------------------
  // return a hashmap of the ledger and their corresponding expenses
  public static HashMap<String, Double> categoryExpense (ArrayList<JSONObject> pages) throws IOException, JSONException{
  	HashMap<String, Double> ledgerMap = new HashMap<String, Double>();
  	    
    for(JSONObject currentPage: pages){
    	int pageLength = currentPage.getJSONArray("transactions").length();
    	for(int i = 0; i < pageLength; i++){
    		JSONObject currentTransaction = currentPage.getJSONArray("transactions").getJSONObject(i);
    		String currentLedger = currentTransaction.getString("Ledger");
    		if(currentLedger.length() == 0){
    			currentLedger = "Payment";
    		}
    		try{
				double balance = ((Double)ledgerMap.get(currentLedger)).doubleValue();
				double roundedBalance = (double)Math.round((balance+Double.parseDouble(currentTransaction.getString("Amount")))*100)/100;
				ledgerMap.put(currentLedger, new Double(roundedBalance));
    		}catch(NullPointerException e){
    			ledgerMap.put(currentLedger, new Double(Double.parseDouble(currentTransaction.getString("Amount"))));
    		}
    		
    		currentTransaction.put("Company",fixCompanyName(currentTransaction.getString("Company")));
    	}
    }
    
    return ledgerMap;
  	
  }
  
  //---------------------------------------------------------------------------------------------------
  // edit the company name, removing all string after any -'s, #'s, and xxxx's
  public static String fixCompanyName(String name){
  	name = name.split("#")[0].split("xxxx")[0].split("-")[0];
  	return name;
  }

  //---------------------------------------------------------------------------------------------------
  public static void main(String[] args) throws IOException {
    String jsonLink = "http://resttest.bench.co/transactions/";
    ArrayList<JSONObject> pages = getPages(jsonLink);
    
    System.out.println(findTotalBalance(pages) + "\n");
   	HashMap<String, Double> catExpense = categoryExpense(pages);
   	
   	Iterator iterator = catExpense.keySet().iterator();
  
	while (iterator.hasNext()) {
   		String key = iterator.next().toString();
   		String value = catExpense.get(key).toString();
  
	    System.out.println(key + " " + value);
	}
   	
  }
}

/*

SHAW CABLESYSTEMS CALGARY AB
BLACK TOP CABS VANCOUVER BC
GUILT & CO. VANCOUVER BC
VANCOUVER TAXI VANCOUVER BC
COMMODORE LANES & BILL VANCOUVER BC
COMMODORE LANES & BILL VANCOUVER BC
NINJA STAR WORLD VANCOUVER BC
PAYMENT 
DROPBOX 
NESTERS MARKET 
YELLOW CAB COMPANY LTD VANCOUVER
NESTERS MARKET 
VANCOUVER TAXI VANCOUVER BC
YELLOW CAB CO LTD VANCOUVER BC
YELLOW CAB CO LTD VANCOUVER BC
LONDON DRUGS 
LINKEDIN LINKEDIN.COM
COSTCO WHOLESALE 
BC LIQUOR 
PAYMENT 
PAYMENT RECEIVED 
SMART CITY FOODS 
LONDON DRUGS 78 POSTAL VANCOUVER BC
DYNAMEX EXPRESS 
JUSTIN STITCHES INC VANCOUVER BC
BOWS AND ARROWS COFFEE ROVICTORIA BC
CLOWN COLLEGE I VANCOUVER BC
URBAN FARE 
PAYMENT 
SMART CITY FOODS 
LONDON DRUGS 78 POSTAL VANCOUVER BC
ECHOSIGN 
APPLE STORE 
NINJA STAR WORLD VANCOUVER BC
DHL YVR GW RICHMOND BC
FEDEX 
GROWINGCITY.COM 
NESTERS MARKET
 
 */