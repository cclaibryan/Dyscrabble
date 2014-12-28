import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class MapGenerator {
	public static int MAX_SIZE = 25;
	public static BasicNode[][] scrabbleMap = new BasicNode[MAX_SIZE][MAX_SIZE]; 	//map declaration
	public static String[] words = new String[] {"apple", "pine", "pear", "egg", "line"};
	public static boolean[] picks = new boolean[] {false, false, false, false, false};
	
	public static Object[] indexList = new Object[26];
	public static Queue<BasicNode> waitQueue = new LinkedBlockingQueue<BasicNode>();
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		//map initialization
		for (int i = 0; i < MAX_SIZE; i++) {
			for(int j = 0; j< MAX_SIZE; j++) {
				scrabbleMap[i][j] = new BasicNode();
				scrabbleMap[i][j].x = i;
				scrabbleMap[i][j].y = j;
				scrabbleMap[i][j].horizonal = false;
				scrabbleMap[i][j].vertical = false;
			}
		}
		
		for (int i = 0; i < 26; i++)  indexList[i] = new ArrayList<StoreIndex>();
		
		//words parsing
		for (int i = 0; i < words.length; i++ ) {
			String currentWord = words[i];
			int wordLength = currentWord.length();
			for (int j = 0; j < wordLength; j++) {
				char currentAlph = currentWord.charAt(j);
				((ArrayList<StoreIndex>) indexList[currentAlph - 'a']).add(new StoreIndex(i, j));
			}
			
		}
		
		//randomly pick a word and put the alphabets in the wait queue.
		int pick = (int) (Math.random()*5);
		
		int length = words[pick].length();
		
		for (int i = 0; i< length; i++) {
			 StoreIndex temp = new StoreIndex(pick, i);
			 waitQueue.offer(temp);
		}
		
		
//		for (int i = 0; i < 26; i++) {
//			ArrayList<StoreIndex> temp = (ArrayList<StoreIndex>) indexList[i];
//			System.out.printf("%c\n", i+'a');
//			for (int j = 0; j < temp.size(); j++) {
//				StoreIndex tempIndex = temp.get(j);
//				System.out.print(Integer.toString(tempIndex.wordIndex) + ' ' + Integer.toString(tempIndex.alphIndex));
//				System.out.println();
//			}
//			System.out.println();
//		}
	}
}
