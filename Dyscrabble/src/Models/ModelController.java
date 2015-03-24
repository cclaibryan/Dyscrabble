package Models;

import org.python.antlr.PythonParser.return_stmt_return;

public class ModelController {
		
		private static ModelController instance;
		private char[][] map = null;
		private int mapSize = 15;
		private String articleString;
		private String titleString;
		
		private ModelController() {
			
		}
		
		public static synchronized ModelController getInstance() {  
			 if (instance == null) {  
				 instance = new ModelController();  
			 }  
			 return instance;  
		}  
		//articles/58921--breaking_news_detail.asp.txt
		public void loadElements(int size) {
			this.mapSize = size;
			ArticleParsing parser = new ArticleParsing("/Users/Bryan/Desktop/test.txt");
			articleString = parser.getArtileString();
			titleString = parser.getTitleString();
			String[] words = parser.pickWords();
			MapGenerator generator = new MapGenerator(words,this.mapSize);
			map = generator.getMap();
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
		
		
//		PythonInterpreter interpreter = new PythonInterpreter();
//		interpreter.execfile("articles/Crawler.py");

}
