package ViewControllers;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import Models.ModelController;
import Utilities.Difficulty;
import Utilities.GameStatus;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


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
	
	private JButton btnFinish;
	
	final int mapSize = 18;						//map size
	private char[][] map;						//current map content
	
	private int coX;						//current coordinate x of focused text field
	private int coY;						//current coordinate y of focused text field
	
	private GameStatus gameStatue;
	
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
		btnFinish.setEnabled(true);
		gameStatue = GameStatus.INGAME;
		
		//reload data
		controller.loadElements(mapSize);	
		map = controller.getMap();
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
						case Easy:		threshold = 0.40; break;
						case Medium:	threshold = 0.25; break;
						case Hard:		threshold = 0.1; break;
					}
					if (ran > threshold) {
						tempField.setEditable(true);
						tempField.setBackground(Color.white);
						tempField.setForeground(Color.black);
						map[i][j] = ' ';
					}
					else {
						tempField.setText(String.format("%c", map[i][j]));
						tempField.setEditable(false);
						tempField.setBackground(Color.white);
						tempField.setForeground(Color.orange);
					}	
				}
				else {
					tempField.setText("");
					tempField.setEditable(false);
					tempField.setBackground(Color.gray);
					tempField.setForeground(Color.black);
				}
			}
		coX = controller.getGenerator().getStartPointX();
		coY = controller.getGenerator().getStartPointY();
		mapTable[coX][coY].requestFocusInWindow();	//central point get first focus
		mapTable[coX][coY].setBorder(new LineBorder(Color.blue,3));
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
		
		btnFinish = new JButton("Finish");
		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnFinish.setEnabled(false);
				gameStatue = GameStatus.GAMEOVER;
				int [][] res = controller.checkAns(map, mapSize);
				if (res == null)	{
					JOptionPane.showMessageDialog(null, "Congratulations! You have finished all the grids correctly!");
					for(int i = 0;i<mapSize;i++)
						for(int j = 0;j<mapSize;j++) {
								mapTable[i][j].setForeground(Color.green);
						}
				}
				else {
					JOptionPane.showMessageDialog(null, "Wrong answer!");
					for(int i = 0;i<mapSize;i++)
						for(int j = 0;j<mapSize;j++) {
							if (res[i][j] == -1)
								mapTable[i][j].setForeground(Color.red);
							else
								mapTable[i][j].setForeground(Color.green);
						}
				}				
			}
		});
		btnFinish.setBounds(269, 627, 117, 29);
		contentPane.add(btnFinish);
		mapTable = new JTextField[mapSize][mapSize];
		for(int i = 0;i<mapSize;i++)
			for (int j = 0;j<mapSize;j++) {
				mapTable[i][j] = new JTextField();
				final JTextField tempField = mapTable[i][j];
				tempField.setHorizontalAlignment(JTextField.CENTER);
				tempField.setCaretColor(Color.white);
				tempField.setFont(new Font("Letter", Font.BOLD, 25));
				tempField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				final int tempI = i;
				final int tempJ = j;
				tempField.addKeyListener(new KeyListener() { 
					public void keyTyped(KeyEvent e) {
						if (gameStatue == GameStatus.GAMEOVER) return;
						if (tempField.isEditable() == false) return;
						char inputChar = e.getKeyChar();
						
						if(tempField.getText().length() > 0) {
							if (inputChar <= 'z' && inputChar >= 'a')  {
								tempField.setText("");
								map[coX][coY] = inputChar;
							}
							else if (inputChar <= 'Z' && inputChar >= 'A') {
								tempField.setText("");
								e.setKeyChar((char) (e.getKeyChar() + 32));
								map[coX][coY] = inputChar;
							}
							else e.setKeyChar('\0');
						}
					} 
					public void keyPressed(KeyEvent e){
						if (gameStatue == GameStatus.GAMEOVER) return;
						int keyCode = e.getKeyCode();
						switch (keyCode) {
						case 37:	//left
							if (tempJ == 0) {
								mapTable[tempI][tempJ].requestFocus();
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
								if (find) 	{ 
									mapTable[tempI][tempLeft].requestFocus();
									mapTable[tempI][tempLeft].setBorder(new LineBorder(Color.blue,3));
									mapTable[coX][coY].setBorder(new LineBorder(Color.gray));
									coX = tempI; coY = tempLeft;
								}
								else { 
									mapTable[tempI][tempJ].requestFocus(); 
									coX = tempI; coY = tempJ; 
								}
							}
							break;
							
							
						case 38:	//up
							
							if (tempI == 0) {
								mapTable[tempI][tempJ].requestFocus();
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
								if (find) {	
									mapTable[tempUp][tempJ].requestFocus(); 
									mapTable[tempUp][tempJ].setBorder(new LineBorder(Color.blue,3));
									mapTable[coX][coY].setBorder(new LineBorder(Color.gray));
									coX = tempUp; coY = tempJ;
								}
								else { 
									mapTable[tempI][tempJ].requestFocus(); 
									coX = tempI; coY = tempJ; 
								}
							}
							break;
							
						case 39:	//right
							
							if (tempJ == mapSize - 1) {
								mapTable[tempI][tempJ].requestFocus();
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
								if (find) {	
									mapTable[tempI][tempRight].requestFocus();
									mapTable[tempI][tempRight].setBorder(new LineBorder(Color.blue,3));
									mapTable[coX][coY].setBorder(new LineBorder(Color.gray));
									coX = tempI; coY = tempRight;
								}
								else {	
									mapTable[tempI][tempJ].requestFocus(); 
									coX = tempI; coY = tempJ; 
								}
							}
							break;
							
						case 40:
							
							if (tempI == mapSize - 1) {
								mapTable[tempI][tempJ].requestFocus();
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
								if (find) {	
									mapTable[tempDown][tempJ].requestFocus(); 
									mapTable[tempDown][tempJ].setBorder(new LineBorder(Color.blue,3));
									mapTable[coX][coY].setBorder(new LineBorder(Color.gray));
									coX = tempDown; coY = tempJ;
								}
								else {	
									mapTable[tempI][tempJ].requestFocus(); 
									coX = tempI; coY = tempJ; 
								}
							}
							break;
						default:
							break;
						}
					} 
					public void keyReleased(KeyEvent e){
						
					} 
				});
			
				tempField.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						mapTable[coX][coY].requestFocus();
					}
					
					@Override
					public void mousePressed(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseExited(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				});
				panel.add(mapTable[i][j]);
			}
		
		this.controller = ModelController.getInstance();
		this.difficulty = difficulty;
		this.reload();
	}
}
