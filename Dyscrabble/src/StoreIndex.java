
//database entry for processed data
public class StoreIndex {
	
	char letter;
	int wordIndex;	//index of word in the original word list
	int alphIndex;	//index of the letter in this word
	int length;		//length of this word
	
	public StoreIndex(int i, int j, int k,char l) {
		wordIndex = i;
		alphIndex = j;
		length = k;
		letter = l;
	}
}
