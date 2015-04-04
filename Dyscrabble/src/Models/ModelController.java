package Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import Models.MapGenerator.AnsIndex;
import Utilities.Difficulty;
import Utilities.NetworkStatus;

public class ModelController {
		
		private static ModelController instance;
		private char[][] map = null;
		private int mapSize = 15;
		private String articleString;
		private String titleString;
		
		private MapGenerator generator;		//map generator
		private ArticleParser parser;		//article parser
		
		private ArticleSearcher searcher;
		private int dlArtsNum = 5;				//number of articles need download 
		
		private ModelController() {
			searcher = ArticleSearcher.getInstance();
		}
		
		public static synchronized ModelController getInstance() {  
			 if (instance == null) {  
				 instance = new ModelController();  
			 }  
			 return instance;  
		}
		public MapGenerator getGenerator() {
			return this.generator;
		}
		
		public ArticleSearcher getSearcher() {
			return searcher;
		}
		
		public void crawl() {
			searcher.callCrawler(dlArtsNum);
		}
		
		//size: the length of a side of the map
		public void loadElements(int size,Difficulty diff) {
			this.mapSize = size;
			
			//words parsing
			parser = new ArticleParser("articles/" + searcher.pickArticle());
			articleString = parser.getArtileString();
			titleString = parser.getTitleString();
			String[] words = parser.pickWords();
			
			//map generation
			int repeatTimes;
			switch (diff) {
			case Easy:
				repeatTimes = 2000;
				break;
			case Medium:
				repeatTimes = 4000;
			default:
				repeatTimes = 8000;
				break;
			}
			generator = new MapGenerator(words,this.mapSize,repeatTimes);
			map = generator.getMap();
		}
		
		//check whether the input answer is correct
		public int[][] checkAns(char[][] myAns,int mapSize) {
			boolean res = true;
			int[][] resChars = new int[mapSize][mapSize];
			Set<String> allWordsList = parser.getFreqMap().keySet();	//all the word list
			
			for(String str : allWordsList)	System.out.println(str);
			
			String[] pickedWords = generator.getWords();
			ArrayList<AnsIndex> ansInfo = generator.getAns();			

			for(AnsIndex idx : ansInfo) {
				int wordLength = pickedWords[idx.getWordIdx()].length();
				int delX=0,delY=0;
				if (idx.getDirection() == 0)	delY = 1;	//horizontally
				else							delX = 1;	//vertically
				
				char[] currentInputWord = new char[wordLength];
				int index = 0;
				int x = idx.getStartX();
				int y = idx.getStartY();
				
				while (index < wordLength) {
					currentInputWord[index++] = myAns[x][y];
					x += delX;
					y += delY;
				}
				String outputStr = new String(currentInputWord);
				
				if (allWordsList.contains(outputStr)==false)	{
					res = false;
					int idx1 = 0;
					int x1 = idx.getStartX();
					int y1 = idx.getStartY();
					
					while (idx1 < wordLength) {
						if(resChars[x1][y1] != 1) resChars[x1][y1] = -1;		//wrong
						x1 += delX;
						y1 += delY;
						idx1++;
					}
				}
				else {
					int idx1 = 0;
					int x1 = idx.getStartX();
					int y1 = idx.getStartY();
					
					while (idx1 < wordLength) {
						resChars[x1][y1] = 1;		//right
						x1 += delX;
						y1 += delY;
						idx1++;
					}
				}
			}
			if (res == false)	return resChars;
			else				return null;
		}
	
		//detect whether the network is available
		public NetworkStatus netDetect() {
			
				Runtime runtime = Runtime.getRuntime();  
				Process process = null;  
				String line = null;  
				InputStream is = null;  
				InputStreamReader isr = null;  
				BufferedReader br = null;
				String ip = "www.sina.com.hk";
//				String ip = "www.thestandard.com.hk";
				try  
				{  
					process = runtime.exec("ping -c 4 "+ip);
					is = process.getInputStream();  
					isr = new InputStreamReader(is);  
					br = new BufferedReader(isr);  
					int success = 0;
					int timeout = 0;
					
					Pattern successPattern = Pattern.compile("time=\\d(.*)ms");
					Pattern timeoutPattern = Pattern.compile("timeout");
					
					float totalTime = 0f;
					while((line = br.readLine()) != null) {
						System.out.println(line);
						System.out.flush();
						
						Matcher m = successPattern.matcher(line);
						if (m.find()) {
							String foundString = m.group();
							totalTime += Float.parseFloat(foundString.split("=| ")[1]);
							success++;
						}
						Matcher m2 = timeoutPattern.matcher(line);
						if (m2.find()) {
							timeout++;
						}
					}
					is.close();  
					isr.close();  
					br.close(); 
					
					if (success > 2)	{
						float aveTime = totalTime / 4;
						System.out.println("ave time:" + String.format("%f", aveTime));
						setDlArtsNum(aveTime);
						return NetworkStatus.AVAILABLE;
					}
					else if (timeout > 2) {
						return NetworkStatus.TIMEOUT;
					}
				}  
				catch(IOException  e)  
				{  
					System.out.println(e);  
					return NetworkStatus.UNAVAILABLE;  
				}
				System.out.println(666);
				return NetworkStatus.UNAVAILABLE;
		}
		
		
		public char[][] getMap() {
			return map;
		}

		public String getArticleString() {
			return articleString;
		}
		public String getTitleString() {
			return titleString;
		}

		public int getDlArtsNum() {
			return dlArtsNum;
		}

		public void setDlArtsNum(float aveTime) {
			int myDlArtsNum = 0;
			
			if (aveTime < 30)		myDlArtsNum = 50;
			else if(aveTime < 50)	myDlArtsNum = 30;
			else if (aveTime < 100)	myDlArtsNum = 10;
			else					myDlArtsNum = 5;
			
			System.out.println("download num:" + String.format("%d", myDlArtsNum));
			this.dlArtsNum = myDlArtsNum;
		}
}
