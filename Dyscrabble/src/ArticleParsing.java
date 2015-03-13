import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ArticleParsing {
	
	private HashMap<String, Long> freqMap;
	
	public ArticleParsing(String address) {
		freqMap = new HashMap<String, Long>();
		File tempFile = new File(address);
		if (tempFile.isFile() && tempFile.exists())	 {
			InputStreamReader reader;
			try {
				reader = new InputStreamReader(new FileInputStream(tempFile));
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(reader);
				
				String lineTxt = null;
				
				while((lineTxt = bufferedReader.readLine()) != null){
					parse(lineTxt);
	            }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	//return all the words in the article
	 public HashMap<String,	Long> parse(String article) {
		 String res = filterStr(article);
		 String[] words = res.split("\\s+");
		 
		 WordsDB myDb = new WordsDB();		//generate the sqlite3 database for words frequency
		 
		 for(int i = 0;i<words.length;i++) {
			 long times = myDb.queryForFreq(words[i]);
			 if (words[i].length() > 3)
				 freqMap.put(words[i], new Long(times));
				 //System.out.printf("word:%s	num:%d\n",words[i],times);
		 }
		 return freqMap;
	 }
	 
	 public String[] pickWords() {
		 String[] picked = new String[15];
		 
		 ArrayList<Map.Entry<String, Long>> listData = new ArrayList<Map.Entry<String,Long>>(freqMap.entrySet());
		 Collections.sort(listData, new Comparator<Map.Entry<String, Long>>() {

			@Override
			public int compare(java.util.Map.Entry<String, Long> o1,
					java.util.Map.Entry<String, Long> o2) {
				if (o1.getValue().longValue() > o2.getValue().longValue())	return 1;
				else if (o1.getValue().longValue() == o2.getValue().longValue())	return 0;
				else return -1;
			}
		});
		 
		 int pickTime = 15;
		 int currentPicked = 0;
		 for(Map.Entry<String, Long> entry:listData) {
			 if (entry.getValue().longValue() > 0)	picked[currentPicked++] = entry.getKey().toLowerCase();
			 if (currentPicked >= pickTime) 	break;
		 }
		 System.out.println(listData);
		 for(int i = 0;i<picked.length;i++)	System.out.printf("word:%s\n", picked[i]);
		 return picked;
	 }
	 
	 public void show() {
		 Iterator<java.util.Map.Entry<String, Long>> it = freqMap.entrySet().iterator();
		 
		 while(it.hasNext()) {
			 java.util.Map.Entry<String,Long> entry = it.next();
			 System.out.printf("word:%s	freq:%d\n", entry.getKey(),entry.getValue());
		 }
	 }

	 private String filterStr(String original) {
		 String firstFilteredString = original.trim();
		 
		 int length = firstFilteredString.length();
		 char[] charStr = new char[length];
		 
		 for(int i = 0;i < length; i++) {
			char current = firstFilteredString.charAt(i);
			if ( (current <= 'z' && current >= 'a') || 
				 (current <= 'Z' && current >= 'A')) {
					charStr[i] = current; 
			}
			else charStr[i] = ' '; 
		 }
		 return new String(charStr);
	 }
}
