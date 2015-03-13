
public class MainLogic {

	public static void main(String[] args) {
		
		int mapSize = 20;
		ArticleParsing parser = new ArticleParsing("/Users/Bryan/Desktop/test.txt");
		String[] words = parser.pickWords();
		MapGenerator generator = new MapGenerator(words,mapSize);
		char[][] res = generator.getMap();
		
		for(int i = 0;i<mapSize; i++) {
			System.out.printf("\t%d",i);
		}
		System.out.println();
		for(int i = 0; i<mapSize; i++) {
			System.out.printf("%d", i);
			for (int j = 0; j < mapSize; j++) {
				System.out.printf("\t%c", res[i][j]);
			}
			System.out.println();
		}
		
	}

}
