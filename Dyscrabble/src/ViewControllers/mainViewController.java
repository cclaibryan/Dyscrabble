package ViewControllers;

import java.awt.Color;
import java.awt.EventQueue;
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

import Models.ModelController;


public class MainViewController extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField[][] textFieldTable;
	final int mapSize = 18;		//map size
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainViewController frame = new MainViewController();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
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
	/**
	 * Create the frame.
	 */
	public MainViewController() {
		setTitle("Dyscrabble");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1150, 780);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setLayout(new ScrollPaneLayout());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(27, 20, 359, 628);
		contentPane.add(scrollPane);

		JPanel panel = new JPanel();
		panel.setBounds(403, 20, 40*mapSize, 40*mapSize);
		contentPane.add(panel);
		panel.setLayout(new GridLayout(mapSize, mapSize, 0, 0));
		
		textFieldTable = new JTextField[mapSize][mapSize];
		
		ModelController controller = ModelController.getInstance();
//		if (controller.netDetect())	System.out.println("good!");
//		else						System.out.println("bad!");
controller.loadElements(mapSize);		//whether to use crawler or not
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
				
				textFieldTable[i][j] = new JTextField();
				
				final JTextField tempField = textFieldTable[i][j];
				tempField.setHorizontalAlignment(JTextField.CENTER);
				tempField.setFont(new Font("Letter", Font.BOLD, 25));
				tempField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				
				if (map[i][j]!= '\0') {
					tempField.setText(String.format("%c", map[i][j]));
				}
				else {
					tempField.setEditable(false);
					tempField.setBackground(Color.gray);
				}
					
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
								textFieldTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempLeft = tempJ-1;
								boolean find = false;
								
								while (tempLeft >= 0) {
									if (textFieldTable[tempI][tempLeft].isEditable() == false) 
										tempLeft--;
									else {
										find = true;
										break;
									}
								}
								if (find) 	textFieldTable[tempI][tempLeft].requestFocus();
								else		textFieldTable[tempI][tempJ].requestFocus();
							}
							break;
							
							
						case 38:	//up
							
							if (tempI == 0) {
								textFieldTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempUp = tempI-1;
								boolean find = false;
								
								while (tempUp >= 0) {
									if (textFieldTable[tempUp][tempJ].isEditable() == false) 
										tempUp--;
									else {
										find = true;
										break;
									}
								}
								if (find) 	textFieldTable[tempUp][tempJ].requestFocus();
								else		textFieldTable[tempI][tempJ].requestFocus();
							}
							break;
							
						case 39:	//right
							
							if (tempJ == mapSize - 1) {
								textFieldTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempRight = tempJ+1;
								boolean find = false;
								
								while (tempRight < mapSize) {
									if (textFieldTable[tempI][tempRight].isEditable() == false) 
										tempRight++;
									else {
										find = true;
										break;
									}
								}
								if (find) 	textFieldTable[tempI][tempRight].requestFocus();
								else		textFieldTable[tempI][tempJ].requestFocus();
							}
							break;
							
						case 40:
							
							if (tempI == mapSize - 1) {
								textFieldTable[tempI][tempJ].requestFocus();
								break;
							}
							else {
								int tempDown = tempI+1;
								boolean find = false;
								
								while (tempDown < mapSize) {
									if (textFieldTable[tempDown][tempJ].isEditable() == false) 
										tempDown++;
									else {
										find = true;
										break;
									}
								}
								if (find) 	textFieldTable[tempDown][tempJ].requestFocus();
								else		textFieldTable[tempI][tempJ].requestFocus();
							}
							break;
						default:
							break;
						}
					} 
					public void keyReleased(KeyEvent e){
						
					} 
				});
				
				panel.add(textFieldTable[i][j]);
			}
			
	}
}
