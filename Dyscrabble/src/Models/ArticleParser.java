package Models;
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
import java.util.Map.Entry;

public class ArticleParser {
	
	private HashMap<String, Long> freqMap;
	private String artileString;
	private String titleString;
	
	public ArticleParser(String address) {
		freqMap = new HashMap<String, Long>();
		File tempFile = new File(address);
		artileString = new String();
		titleString = new String();
		
		if (tempFile.isFile() && tempFile.exists())	 {
			InputStreamReader reader;
			try {
				reader = new InputStreamReader(new FileInputStream(tempFile),"iso-8859-1");	//need to read as GB18030
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(reader);
				
				String lineTxt = null;
				boolean titleRead = false;	
				
				while((lineTxt = bufferedReader.readLine()) != null){
					if (titleRead == false)	{
						titleString += lineTxt;
						titleRead = true;
					}
					else {
						artileString += lineTxt;
						artileString += "\n";
					}
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
			 String lowerWord = words[i].toLowerCase();
			 long times = myDb.queryForFreq(lowerWord);
			 if (lowerWord.length() > 4)
				 freqMap.put(lowerWord, new Long(times));
		 }
		 return freqMap;
	 }
	 
	 public String[] pickWords() {
		 int pickNumber = 15;
		 String[] picked = new String[pickNumber];
		 
		 ArrayList<Entry<String, Long>> listData = new ArrayList<Entry<String,Long>>(freqMap.entrySet());
		 Collections.sort(listData, new Comparator<Entry<String, Long>>() {

			@Override
			public int compare(Entry<String, Long> o1,
					Entry<String, Long> o2) {
				if (o1.getValue().longValue() > o2.getValue().longValue())	return 1;
				else if (o1.getValue().longValue() == o2.getValue().longValue())	return 0;
				else return -1;
			}
		});
		 
		 int pickTime = pickNumber;
		 int currentPicked = 0;
		 for(Entry<String, Long> entry:listData) {
			 if (entry.getValue().longValue() > 0)	picked[currentPicked++] = entry.getKey().toLowerCase();
			 if (currentPicked >= pickTime) 	break;
		 }
		 
		 return picked;
	 }

	 //filter the article, delete the non-alphabets
	 private String filterStr(String original) {
		 String firstFilteredString = original.trim().toLowerCase();
		 
		 int length = firstFilteredString.length();
		 char[] charStr = new char[length];
		 
		 for(int i = 0;i < length; i++) {
			char current = firstFilteredString.charAt(i);
			if (current <= 'z' && current >= 'a') {
					charStr[i] = current; 
			}
			else charStr[i] = ' '; 
		 }
		 return new String(charStr).trim();
	 }

	public String getArtileString() {
		return artileString;
	}

	public String getTitleString() {
		return titleString;
	}
	public HashMap<String, Long> getFreqMap()
	{
		return this.freqMap;
	}
}
