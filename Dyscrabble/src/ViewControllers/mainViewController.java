package ViewControllers;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

import org.python.modules.thread.thread;

import Models.ModelController;
import Utilities.Difficulty;


public class MainViewController extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;					//the article frame
	private JScrollPane scrollPane;				//the scroll pane
	private JTextField[][] mapTable;			//the map
	private Difficulty difficulty;				//current difficulty
	private ModelController controller;			//the model controller
	final int mapSize = 18;						//map size
	
	void setDocs(JTextPane textPane, String str, Color col, boolean isBold, int fontSize) {
		SimpleAttributeSet attset = new SimpleAttributeSet();
		StyleConstants.setForeground(attset	, col);
		StyleConstants.setBold(attset, isBold);
		StyleConstants.setFontSize(attset, fontSize);
		Document doc = textPane.getDocument();   
		try {
			doc.insertString(doc.getLength(),str,attset);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	public void reload() {
		scrollPane.setViewportView(null);
		
		//reload data
		controller.loadElements(mapSize);	
		char[][] map = controller.getMap();
		String article = controller.getArticleString();
		String title = controller.getTitleString();
		
		JTextPane txtrTest = new JTextPane();
		setDocs(txtrTest, title+"\n\n", Color.black, true, 18);
		setDocs(txtrTest, article, Color.black, false, 14);
		txtrTest.setEditable(false);
		txtrTest.setCaretPosition(0);				//set the scroll bar on the top
		scrollPane.setViewportView(txtrTest);
		
		for(int i = 0;i<mapSize;i++)
			for (int j = 0;j<mapSize;j++) {
				JTextField tempField = mapTable[i][j];
				if (map[i][j]!= '\0') {
					double ran = Math.random();
					double threshold = 0;
					switch (difficulty) {
						case Easy:		threshold = 0.4; break;
						case Medium:	threshold = 0.25; break;
						case Hard:		threshold = 0.1; break;
					}
					if (ran > threshold) {
//						tempField.setText(String.format("%c", map[i][j]));
						tempField.setEditable(true);
						tempField.setBackground(Color.white);
						tempField.setForeground(Color.black);
					}
					else {
						tempField.setText(String.format("%c", map[i][j]));
						tempField.setEditable(false);
						tempField.setBackground(Color.white);
						tempField.setForeground(Color.red);
					}	
				}
				else {
					tempField.setText("");
					tempField.setEditable(false);
					tempField.setBackground(Color.gray);
					tempField.setForeground(Color.black);
				}
			}
	}
	
	public MainViewController(Difficulty difficulty) {
		//initiate the frame
		setTitle("Dyscrabble");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1150, 780);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//initiate the scroll pane
		scrollPane = new JScrollPane();
		scrollPane.setLayout(new ScrollPaneLayout());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(27, 20, 359, 584);
		contentPane.add(scrollPane);

		//initiate the map panel
		JPanel panel = new JPanel();
		panel.setBounds(403, 20, 40*mapSize, 40*mapSize);
		contentPane.add(panel);
		panel.setLayout(new GridLayout(mapSize, mapSize, 0, 0));
		mapTable = new JTextField[mapSize][mapSize];
		for(int i = 0;i<mapSize;i++)
			for (int j = 0;j<mapSize;j++) {
				mapTable[i][j] = new JTextField();
				final JTextField tempField = mapTable[i][j];
				tempField.setHorizontalAlignment(JTextField.CENTER);
				tempField.setFont(new Font("Letter", Font.BOLD, 25));
				tempField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				final int tempI = i;
				final int tempJ = j;
				tempField.addKeyListener(new KeyListener() { 
					public void keyTyped(KeyEvent e) {
						char inputChar = e.getKeyChar();
						
						if(tempField.getText().length() > 0) {
							if (inputChar <= 'z' && inputChar >= 'a')  {
								tempField.setText("");
							}
							else if (inputChar <= 'Z' && inputChar >= 'A') {
								tempField.setText("");
								e.setKeyChar((char) (e.getKeyChar() + 32));
							}
							else e.setKeyChar('\0');
						}
					} 
					public void keyPressed(KeyEvent e){
						int keyCode = e.getKeyCode();
						switch (keyCode) {
						case 37:	//left
							if (tempJ == 0) {
								mapTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempLeft = tempJ-1;
								boolean find = false;
								
								while (tempLeft >= 0) {
									if (mapTable[tempI][tempLeft].getBackground() == Color.gray) 
										tempLeft--;
									else {
										find = true;
										break;
									}
								}
								if (find) 	mapTable[tempI][tempLeft].requestFocus();
								else		mapTable[tempI][tempJ].requestFocus();
							}
							break;
							
							
						case 38:	//up
							
							if (tempI == 0) {
								mapTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempUp = tempI-1;
								boolean find = false;
								
								while (tempUp >= 0) {
									if (mapTable[tempUp][tempJ].getBackground() == Color.gray) 
										tempUp--;
									else {
										find = true;
										break;
									}
								}
								if (find) 	mapTable[tempUp][tempJ].requestFocus();
								else		mapTable[tempI][tempJ].requestFocus();
							}
							break;
							
						case 39:	//right
							
							if (tempJ == mapSize - 1) {
								mapTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempRight = tempJ+1;
								boolean find = false;
								
								while (tempRight < mapSize) {
									if (mapTable[tempI][tempRight].getBackground() == Color.gray) 
										tempRight++;
									else {
										find = true;
										break;
									}
								}
								if (find) 	mapTable[tempI][tempRight].requestFocus();
								else		mapTable[tempI][tempJ].requestFocus();
							}
							break;
							
						case 40:
							
							if (tempI == mapSize - 1) {
								mapTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempDown = tempI+1;
								boolean find = false;
								
								while (tempDown < mapSize) {
									if (mapTable[tempDown][tempJ].getBackground() == Color.gray) 
										tempDown++;
									else {
										find = true;
										break;
									}
								}
								if (find) 	mapTable[tempDown][tempJ].requestFocus();
								else		mapTable[tempI][tempJ].requestFocus();
							}
							break;
						default:
							break;
						}
					} 
					public void keyReleased(KeyEvent e){
						
					} 
				});
				panel.add(mapTable[i][j]);
			}
		
		this.controller = ModelController.getInstance();
		this.difficulty = difficulty;
		this.reload();
		
	}
	
}
