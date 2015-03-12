import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MapGenerator {
	private int maxSize;					//size of the map
	private BasicNode[][] scrabbleMap;		//the map
	private String[] words;					//the words input
	private boolean[] picks;				//whether the words are picked
	
	private Object[] indexList = new Object[26];		//database for parsing words
	private Queue<BasicNode> waitQueue = new LinkedBlockingQueue<BasicNode>();	//BFS queue
	
	//Assess parameters
	float leftBound,rightBound,upBound,lowBound;
	int crossNum,wordsUsed;
	float totalMark = 0f;	//highest map mark
	
	//constructor
	public MapGenerator(String[] words,int size) {
		this.maxSize = size;
		this.words = words;
		for (int i = 0; i < 26; i++)  indexList[i] = new ArrayList<StoreIndex>();
		wordsParsing();
	}
	
	@SuppressWarnings("unchecked")
	public char[][] getMap() {
		
		char[][] res = new char[maxSize][maxSize];
		
		int times = 2000;
		
		while (times>0) {
			initMap();
			
			//choose the cross coordinate
			int pickX = (int)(Math.random()*6 + maxSize/2 - 3);
			int pickY = (int)(Math.random()*6 + maxSize/2 - 3);
					
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
				
			float thisMark = assessMap();
			if (thisMark > totalMark) {
				totalMark = thisMark;
				for(int i = 0;i<maxSize;i++)
					for(int j = 0;j<maxSize;j++)
						res[i][j] = scrabbleMap[i][j].content;
			}
			
			times--;
		}
		System.out.println(totalMark);
		return res;
	}
	
	private void initMap() {
		//map initialization
		scrabbleMap = new BasicNode[maxSize][maxSize]; 	//map declaration
		waitQueue.clear();								//clear the queue
		
		for (int i = 0; i < maxSize; i++) {
			for(int j = 0; j< maxSize; j++) {
				scrabbleMap[i][j] = new BasicNode();
				scrabbleMap[i][j].x = i;
				scrabbleMap[i][j].y = j;
				scrabbleMap[i][j].horizontal = 0;
				scrabbleMap[i][j].vertical = 0;
				scrabbleMap[i][j].content = '\0';				//initiate to '\0'
				scrabbleMap[i][j].consider = true;
			}
		}
		
		picks = new boolean[words.length];
		for(int i = 0;i<picks.length;i++) picks[i] = false; 
		
		leftBound = (float) (maxSize/2.0);
		rightBound = (float) (maxSize/2.0);
		upBound = (float) (maxSize/2.0);
		lowBound = (float) (maxSize/2.0);
		crossNum = 0;
		wordsUsed = 0;
	}
	//check whether a word can put at a special spot
	//direction: 0 for horizontal, 1 for vertical
	private boolean put(int x, int y, StoreIndex index, int direction) {
		
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
		
		if (startX < 0 || startY < 0 || endX >= maxSize || endY >= maxSize) return false;			//out of the bound
		
		//the input word can not be next to other input words
		if (direction == 0) {	//horizontally
			if ( (startY > 0 && scrabbleMap[startX][startY-1].content != '\0') ||
			     (endY < maxSize-1 && scrabbleMap[endX][endY+1].content != '\0') )
					return false;
		}
		else if (direction == 1) {	//vertically
			if ( (startX > 0 && scrabbleMap[startX-1][startY].content != '\0') ||
				 (endX < maxSize-1 && scrabbleMap[endX+1][endY].content != '\0') )
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
						 (tempX < maxSize-1 && scrabbleMap[tempX+1][tempY].content != '\0'))
						return false;
				}
				else {
					if ( (tempY > 0 && scrabbleMap[tempX][tempY-1].content != '\0') || 
						 (tempY < maxSize-1 && scrabbleMap[tempX][tempY+1].content != '\0'))
							return false;
				}
			}
		}
		
		//put the word into the grid
		picks[index.wordIndex] = true;
		wordsUsed++;
		for(int xx = startX,yy = startY,i=0; i<index.length; i++, xx += vertiScale, yy += horiScale) {
			if (scrabbleMap[xx][yy].content != '\0')	crossNum++;
			else 										scrabbleMap[xx][yy].content = thisWordString.charAt(i);
			
			if (horiScale == 1) scrabbleMap[xx][yy].horizontal = horiScale;
			else if (vertiScale == 1) scrabbleMap[xx][yy].vertical = vertiScale;
		}
		
		//modify the bounds
		if(startX < upBound)	upBound = startX;
		if (endX > lowBound)	lowBound = endX;
		if (startY < leftBound)	leftBound = startY;
		if (endY > rightBound)	rightBound = endY;
		
		return true;
	}

	//add the four nodes with distance = 1 or 2 to (x,y) to the queue
	private void addFour(int pickX, int pickY) {
		
		int left = 0, right = 0, up = 0, down = 0;
		if (pickX - 2 >=0)	if (checkFourCorners(pickX-2, pickY) && scrabbleMap[pickX-2][pickY].consider == true)	up = 2;
		if (pickX - 1 >=0)	if (checkFourCorners(pickX-1, pickY) && scrabbleMap[pickX-1][pickY].consider == true)	up = 1;
		
		if (pickX + 2 < maxSize)	if (checkFourCorners(pickX+2, pickY) && scrabbleMap[pickX+2][pickY].consider == true)	down = 2;
		if (pickX + 1 < maxSize)	if (checkFourCorners(pickX+1, pickY) && scrabbleMap[pickX+1][pickY].consider == true)	down = 1;
		
		if (pickY - 2 >= 0)	if (checkFourCorners(pickX, pickY-2) && scrabbleMap[pickX][pickY-2].consider == true)	left = 2;
		if (pickY - 1 >= 0)	if (checkFourCorners(pickX, pickY-1) && scrabbleMap[pickX][pickY-1].consider == true)	left = 1;
		
		if (pickY + 2 < maxSize)	if (checkFourCorners(pickX, pickY+2) && scrabbleMap[pickX][pickY+2].consider == true)	right = 2;
		if (pickY + 1 < maxSize)	if (checkFourCorners(pickX, pickY+1) && scrabbleMap[pickX][pickY+1].consider == true)	right = 1;
		
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
	private boolean checkFourCorners(int x, int y) {
		if (x > 0 && y > 0)
			if (scrabbleMap[x-1][y-1].content != '\0')	return false;
		if (x > 0 && y < maxSize - 1)
			if (scrabbleMap[x-1][y+1].content != '\0')	return false;
		if (x < maxSize -1 && y > 0)
			if (scrabbleMap[x+1][y-1].content != '\0')	return false;
		if (x < maxSize -1 && y < maxSize - 1)
			if (scrabbleMap[x+1][y+1].content != '\0')	return false;
		return true;
	}

	//pick a letter with more than 1 element
	@SuppressWarnings("unchecked")
	private boolean randomInit(int x, int y) {
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
	
	//parsing words
	@SuppressWarnings("unchecked")
	private void wordsParsing() {
		//words parsing
		for (int i = 0; i < words.length; i++ ) {
			String currentWord = words[i];
			int wordLength = currentWord.length();
			for (int j = 0; j < wordLength; j++) {
				char currentAlph = currentWord.charAt(j);
				((ArrayList<StoreIndex>) indexList[currentAlph - 'a']).add(new StoreIndex(i, j, wordLength,currentAlph));
			}
		}
	}
	
	private float assessMap() {
		float mark = 0f;
		mark = wordsUsed + 3 * (crossNum - wordsUsed + 1) - ((lowBound-upBound+1)/maxSize) * ((rightBound-leftBound+1)/maxSize) + 1;
		
		return mark;
	}
}
