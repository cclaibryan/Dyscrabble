package Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import Models.MapGenerator.AnsIndex;

public class ModelController {
		
		private static ModelController instance;
		private char[][] map = null;
		private int mapSize = 15;
		private String articleString;
		private String titleString;
		
		private MapGenerator generator;		//map generator
		private ArticleParsing parser;		//article parser
		
		private ArticleSearcher searcher;
		
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
			searcher.callCrawler();
		}
		
		//size: the length of a side of the map
		public void loadElements(int size) {
			this.mapSize = size;
			
			//words parsing
			parser = new ArticleParsing("articles/" + searcher.pickArticle());
			articleString = parser.getArtileString();
			titleString = parser.getTitleString();
			String[] words = parser.pickWords();
			
			//map generation
			generator = new MapGenerator(words,this.mapSize);
			map = generator.getMap();
		}
		
		//check whether the input answer is correct
		public int[][] checkAns(char[][] myAns,int mapSize) {
			boolean res = true;
			int[][] resChars = new int[mapSize][mapSize];
			Set<String> allWordsList = parser.getFreqMap().keySet();	//all the word list
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
		public boolean netDetect() {
				Runtime runtime = Runtime.getRuntime();  
				Process process = null;  
				String line = null;  
				InputStream is = null;  
				InputStreamReader isr = null;  
				BufferedReader br = null;  
				String ip="http://www.thestandard.com.hk";  //ping address
				try  
				{  
					process = runtime.exec("ping -c 3 "+ip);  
					is = process.getInputStream();  
					isr = new InputStreamReader(is);  
					br = new BufferedReader(isr);  
					int lineNum = 0;
					while((line = br.readLine()) != null && lineNum < 4) {
						System.out.println(line);
						System.out.flush();
						lineNum++;  
					}
					is.close();  
					isr.close();  
					br.close(); 
					if (lineNum >= 4)	{
						return true;
					}
				}  
				catch(IOException  e)  
				{  
					System.out.println(e);  
					return false;  
				}
				return false;
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
}
