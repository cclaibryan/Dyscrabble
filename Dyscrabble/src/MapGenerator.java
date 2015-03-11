import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import sun.tools.tree.ThisExpression;

import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.sun.medialib.mlib.Image;
import com.sun.org.apache.bcel.internal.generic.NEW;


public class MapGenerator {
	public static int MAX_SIZE = 20;
	public static BasicNode[][] scrabbleMap = new BasicNode[MAX_SIZE][MAX_SIZE]; 	//map declaration
	public static String[] words = new String[] {"constitution", "professional", "pineapple", "proselytize", "presume","tyrande","japordize","westland","probability","tricky"};
	public static boolean[] picks = new boolean[] {false, false, false, false, false,false, false, false, false, false};
	
	public static Object[] indexList = new Object[26];
	public static Queue<BasicNode> waitQueue = new LinkedBlockingQueue<BasicNode>();
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		long startMill = System.currentTimeMillis();
		//map initialization
		for (int i = 0; i < MAX_SIZE; i++) {
			for(int j = 0; j< MAX_SIZE; j++) {
				scrabbleMap[i][j] = new BasicNode();
				scrabbleMap[i][j].x = i;
				scrabbleMap[i][j].y = j;
				scrabbleMap[i][j].horizontal = 0;
				scrabbleMap[i][j].vertical = 0;
				scrabbleMap[i][j].content = '\0';				//initiate to '\0'
				scrabbleMap[i][j].consider = true;
			}
		}
		
		for (int i = 0; i < 26; i++)  indexList[i] = new ArrayList<StoreIndex>();
		
		//words parsing
		for (int i = 0; i < words.length; i++ ) {
			String currentWord = words[i];
			int wordLength = currentWord.length();
			for (int j = 0; j < wordLength; j++) {
				char currentAlph = currentWord.charAt(j);
				((ArrayList<StoreIndex>) indexList[currentAlph - 'a']).add(new StoreIndex(i, j, wordLength,currentAlph));
			}
		}
				
	
		
		//choose the cross coordinate
		int pickX = (int)(Math.random()*6 + MAX_SIZE/2 - 3);
		int pickY = (int)(Math.random()*6 + MAX_SIZE/2 - 3);
				
		randomInit(pickX,pickY);
		scrabbleMap[pickX][pickY].consider = false;
		
		addFour(pickX, pickY);
		
		while(waitQueue.size() > 0) {
			BasicNode tempNode = waitQueue.poll();
			int alphIdx = (int)(tempNode.content - 'a');
			
			ArrayList<StoreIndex> storeIdxArray = (ArrayList<StoreIndex>) indexList[alphIdx];
			
			for(int i = 0;i<storeIdxArray.size();i++) {
				StoreIndex temp = storeIdxArray.get(i);
				
				if (picks[temp.wordIndex] == true)	continue;
				
				if(tempNode.horizontal == 1)	put(tempNode.x, tempNode.y, temp, 1);
				else if(tempNode.vertical == 1)	put(tempNode.x, tempNode.y, temp, 0);
				break;
			}
			
			addFour(tempNode.x, tempNode.y);
		}

		long endMill = System.currentTimeMillis();
		System.out.printf("time:%d ms\n",endMill-startMill);
			
		showMap();
		
	}
	
	//check whether a word can put at a special spot
	//direction: 0 for horizontal, 1 for vertical
	static boolean put(int x, int y, StoreIndex index, int direction) {
		
		if (picks[index.wordIndex] == true)	return false;
		
		String thisWordString = words[index.wordIndex];
		
		int horiScale = 0;
		int vertiScale = 0;
		
		if (direction == 0) horiScale = 1;
		else 				vertiScale = 1;
		
		int startX = x - vertiScale * index.alphIndex;
		int startY = y - horiScale * index.alphIndex;
		int endX = startX + vertiScale * ( index.length - 1 );
		int endY = startY + horiScale *  ( index.length - 1 );
		
		if (startX < 0 || startY < 0 || endX >= MAX_SIZE || endY >= MAX_SIZE) return false;			//out of the bound
		
		//the input word can not be next to other input words
		if (direction == 0) {	//horizontally
			if ( (startY > 0 && scrabbleMap[startX][startY-1].content != '\0') ||
			     (endY < MAX_SIZE-1 && scrabbleMap[endX][endY+1].content != '\0') )
					return false;
		}
		else if (direction == 1) {	//vertically
			if ( (startX > 0 && scrabbleMap[startX-1][startY].content != '\0') ||
				 (endX < MAX_SIZE-1 && scrabbleMap[endX+1][endY].content != '\0') )
						return false;
		}

		//check letter by letter
		for (int i = 0;i < index.length; i++) {
			char currentChar = thisWordString.charAt(i);
			int tempX = startX + vertiScale * i;
			int tempY = startY + horiScale * i;
			
			BasicNode tempNode = scrabbleMap[tempX][tempY];
			if (tempNode.content != '\0') {
				if (tempNode.horizontal * horiScale > 0 || tempNode.vertical * vertiScale > 0)  return false;
				if (tempNode.content != currentChar)	return false;
			}
			else {	//check whether there are other letters next to the blank
				if (direction == 0) {
					if ( (tempX > 0 && scrabbleMap[tempX-1][tempY].content != '\0') || 
						 (tempX < MAX_SIZE-1 && scrabbleMap[tempX+1][tempY].content != '\0'))
						return false;
				}
				else {
					if ( (tempY > 0 && scrabbleMap[tempX][tempY-1].content != '\0') || 
						 (tempY < MAX_SIZE-1 && scrabbleMap[tempX][tempY+1].content != '\0'))
							return false;
				}
			}
		}
		
		//put the word into the grid
		picks[index.wordIndex] = true;
		for(int xx = startX,yy = startY,i=0; i<index.length; i++, xx += vertiScale, yy += horiScale) {
			scrabbleMap[xx][yy].content = thisWordString.charAt(i); 
			if (horiScale == 1) scrabbleMap[xx][yy].horizontal = horiScale;
			else if (vertiScale == 1) scrabbleMap[xx][yy].vertical = vertiScale;
		}
		
		return true;
	}

	//add the four nodes with distance = 1 or 2 to (x,y) to the queue
	static void addFour(int pickX, int pickY) {
		
		int left = 0, right = 0, up = 0, down = 0;
		if (pickX - 2 >=0)	if (checkFourCorners(pickX-2, pickY) && scrabbleMap[pickX-2][pickY].consider == true)	up = 2;
		if (pickX - 1 >=0)	if (checkFourCorners(pickX-1, pickY) && scrabbleMap[pickX-1][pickY].consider == true)	up = 1;
		
		if (pickX + 2 < MAX_SIZE)	if (checkFourCorners(pickX+2, pickY) && scrabbleMap[pickX+2][pickY].consider == true)	down = 2;
		if (pickX + 1 < MAX_SIZE)	if (checkFourCorners(pickX+1, pickY) && scrabbleMap[pickX+1][pickY].consider == true)	down = 1;
		
		if (pickY - 2 >= 0)	if (checkFourCorners(pickX, pickY-2) && scrabbleMap[pickX][pickY-2].consider == true)	left = 2;
		if (pickY - 1 >= 0)	if (checkFourCorners(pickX, pickY-1) && scrabbleMap[pickX][pickY-1].consider == true)	left = 1;
		
		if (pickY + 2 < MAX_SIZE)	if (checkFourCorners(pickX, pickY+2) && scrabbleMap[pickX][pickY+2].consider == true)	right = 2;
		if (pickY + 1 < MAX_SIZE)	if (checkFourCorners(pickX, pickY+1) && scrabbleMap[pickX][pickY+1].consider == true)	right = 1;
		
		if (up != 0 && scrabbleMap[pickX-up][pickY].content != '\0' &&
			scrabbleMap[pickX-up][pickY].horizontal * scrabbleMap[pickX-up][pickY].vertical == 0)	{
				scrabbleMap[pickX-up][pickY].consider = false;
				waitQueue.add(scrabbleMap[pickX-up][pickY]);
		}
				
		if (down != 0 && scrabbleMap[pickX+down][pickY].content != '\0' &&
			scrabbleMap[pickX+down][pickY].horizontal * scrabbleMap[pickX+down][pickY].vertical == 0)	{
			scrabbleMap[pickX+down][pickY].consider = false;
			waitQueue.add(scrabbleMap[pickX+down][pickY]);
		}
				
		
		if (left != 0 && scrabbleMap[pickX][pickY-left].content != '\0' &&
			scrabbleMap[pickX][pickY-left].horizontal * scrabbleMap[pickX][pickY-left].vertical == 0)	{
			scrabbleMap[pickX][pickY-left].consider = false;
			waitQueue.add(scrabbleMap[pickX][pickY-left]);
		}
				

		if (right != 0 && scrabbleMap[pickX][pickY+right].content != '\0' &&
			scrabbleMap[pickX][pickY+right].horizontal * scrabbleMap[pickX][pickY+right].vertical == 0) {
			scrabbleMap[pickX][pickY+right].consider = false;
			waitQueue.add(scrabbleMap[pickX][pickY+right]);
		}
	}
	
	//check whether the four corners of a node have other letters
	static boolean checkFourCorners(int x, int y) {
		if (x > 0 && y > 0)
			if (scrabbleMap[x-1][y-1].content != '\0')	return false;
		if (x > 0 && y < MAX_SIZE - 1)
			if (scrabbleMap[x-1][y+1].content != '\0')	return false;
		if (x < MAX_SIZE -1 && y > 0)
			if (scrabbleMap[x+1][y-1].content != '\0')	return false;
		if (x < MAX_SIZE -1 && y < MAX_SIZE - 1)
			if (scrabbleMap[x+1][y+1].content != '\0')	return false;
		return true;
	}

	//pick a letter with more than 1 elements
	@SuppressWarnings("unchecked")
	static boolean randomInit(int x, int y) {
		int[] boolArray = new int[26];
		int putNum = 0, res = -1;
		int first = -1,second = -1;
		boolean found = false;
		
		for(int i = 0;i < 26;i++) boolArray[i] = 0;

		while (putNum < 26) {
			res = (int)(Math.random()*26);
			
			if (boolArray[res] > 0 ) continue;
			if ( ((ArrayList<StoreIndex>) indexList[res]).size() < 2 ) {
					boolArray[res]++; 
					putNum++;
					continue;
			}
			else {
				
				//estimate if 2 words can be picked
				ArrayList<StoreIndex> localIndexList = (ArrayList<StoreIndex>) indexList[res];
				int totalSize = localIndexList.size();
				boolean theSame = true;
				int record = localIndexList.get(0).wordIndex;
				
				for(int i = 1; i < totalSize; i++) {
					if (localIndexList.get(i).wordIndex != record)	{
						theSame = false;
						break;
					}
				}
				if (theSame) continue;
				
				//find 2 store indexes with different words				
				boolean[] localBoolArray = new boolean[totalSize];
				
				for(int i = 0; i < totalSize; i++) localBoolArray[i] = false;
				
				first = (int)(Math.random()*totalSize);
				second = (int)(Math.random()*totalSize);
				localBoolArray[first] = true;
				
				while(localBoolArray[second] == true  						
				   || localIndexList.get(first).wordIndex == localIndexList.get(second).wordIndex) {	
					localBoolArray[second] = true;
					second = (int)(Math.random()*totalSize);
				}
				found = true;
				break;
			}
		}
		
		if (!found) return false;

		StoreIndex firstIdx = ((ArrayList<StoreIndex>) indexList[res]).get(first);
		StoreIndex secondIdx = ((ArrayList<StoreIndex>) indexList[res]).get(second);
		
		int dire = (int)(Math.random());
		put(x, y, firstIdx, dire);
		put(x, y, secondIdx, 1-dire);
		
		return true;
	}
	
	static void showDatabase() {
		//show the processed database
		for (int i = 0;i<26;i++) {
			System.out.printf("%c: ",(i+'a'));
			ArrayList<StoreIndex> currentArrayList = (ArrayList<StoreIndex>) indexList[i];
			for (int j = 0;j<currentArrayList.size();j++) {
				StoreIndex tempIndex = currentArrayList.get(j);
				System.out.printf("[%d,%d,%d]",tempIndex.wordIndex,tempIndex.alphIndex,tempIndex.length);
			}
			System.out.printf("\n");
		}	
	}
	
	static void showMap() {
		for(int i = 0;i<MAX_SIZE; i++) {
			System.out.printf("\t%d",i);
		}
		System.out.println();
		for(int i = 0; i<MAX_SIZE; i++) {
			System.out.printf("%d", i);
			for (int j = 0; j < MAX_SIZE; j++) {
				System.out.printf("\t%c", scrabbleMap[i][j].content);
			}
			System.out.println();
		}
	}
}
