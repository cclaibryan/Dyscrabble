package Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ModelController {
		
		private static ModelController instance;
		private char[][] map = null;
		private int mapSize = 15;
		private String articleString;
		private String titleString;
		
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
		
		public void crawl() {
			searcher.callCrawler();
		}
		
		//size: the length of a side of the map
		public void loadElements(int size) {
			this.mapSize = size;
			
			ArticleParsing parser = new ArticleParsing("articles/" + searcher.pickArticle());
			articleString = parser.getArtileString();
			titleString = parser.getTitleString();
			String[] words = parser.pickWords();
			MapGenerator generator = new MapGenerator(words,this.mapSize);
			map = generator.getMap();
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
		
		public static void main(String[] args) {
			ModelController con = ModelController.getInstance();
			if (con.netDetect()) System.out.println("good!");
			else				System.out.println("bad!");
			
		}
		
}
