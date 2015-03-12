
public class mainLogic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int mapSize = 20;
		String[] words = new String[] {"constitution", "professional", "pineapple", "proselytize", "presume","tyrande","japordize","westland","probability","tricky"};
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
