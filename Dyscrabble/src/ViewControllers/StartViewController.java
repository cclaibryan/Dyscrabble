package ViewControllers;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;

import Models.ModelController;
import Utilities.Difficulty;
import Utilities.NetworkStatus;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

public class StartViewController extends JFrame implements Observer, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextPane textPane;
	
	private JButton btnStart;
	private JButton btnEasy;
	private JButton btnMedium;
	private JButton btnHard;
	private JButton btnBack;
	
	private ModelController controller;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartViewController frame = new StartViewController(true);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public JTextPane getTextPane() {
		 return textPane;
	}
	public JButton getBtnStart() {
		return btnStart;
	}
	public ModelController getController() {
		return controller;
	}
	/**
	 * Create the frame.
	 */
	private void loadComponents() {
		setTitle("Dyscrabble");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 586);
		
		ImageIcon image = new ImageIcon("src/Images/Welcome.png");
		JLabel imageLbl = new JLabel(image);
		
		getLayeredPane().add(imageLbl, new Integer(Integer.MIN_VALUE));
		imageLbl.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setOpaque(false);
		
		//button definitions
		
		btnStart = new JButton("Start");
		btnStart.setBounds(341, 240, 117, 29);
		contentPane.add(btnStart);
	
		btnEasy = new JButton("Easy");
		btnEasy.addActionListener(this);
		btnEasy.setBounds(111, 240, 117, 29);
		contentPane.add(btnEasy);
		
		btnMedium = new JButton("Medium");
		btnMedium.addActionListener(this);
		btnMedium.setBounds(341, 240, 117, 29);
		contentPane.add(btnMedium);
		
		btnHard = new JButton("Difficult");
		btnHard.addActionListener(this);
		btnHard.setBounds(583, 240, 117, 29);
		contentPane.add(btnHard);
		
		btnBack = new JButton("Back");
		btnBack.setBounds(341, 294, 117, 29);
		contentPane.add(btnBack);
		
		//difficulty buttons are not visible at first
		btnEasy.setVisible(false);
		btnMedium.setVisible(false);
		btnHard.setVisible(false);
		btnBack.setVisible(false);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStart.setVisible(false);
				btnEasy.setVisible(true);
				btnMedium.setVisible(true);
				btnHard.setVisible(true);
				btnBack.setVisible(true);
			}
		});
		
		btnBack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnStart.setVisible(true);
				btnEasy.setVisible(false);
				btnMedium.setVisible(false);
				btnHard.setVisible(false);
				btnBack.setVisible(false);
			}
		});
		
		btnStart.setEnabled(false);		//start button is not available before the network download is finished
		
		//tip field below
		textPane = new JTextPane();
		
		//set textPane to align center
		StyledDocument doc = textPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		
		textPane.setBorder(new EmptyBorder(0,0,0,0));
		textPane.setText("Enjoy yourself with Dyscrabble!");
		textPane.setBounds(0, 546, 800, 36);
		textPane.setBackground(new Color(237, 209, 166));
		textPane.setEditable(false);
		
		contentPane.add(textPane);
	}
	
	
	public StartViewController(boolean needCrawl) {
		loadComponents();
		controller = ModelController.getInstance();		//initiate the model controller
		controller.getSearcher().addObserver(this);
		//new thread for initial and crawling the articles
		if (needCrawl) {
			LoadingThread thread = new LoadingThread(this);	
			thread.start();
		}
	}
	
	class LoadingThread extends Thread {
		private StartViewController frame;
		public LoadingThread(StartViewController frame) {
			this.frame = frame;
		}
		@Override
		public void run() {
			ModelController controller = frame.getController();
			frame.getTextPane().setText("Network detecting...");
			NetworkStatus status = controller.netDetect();
			if (status == NetworkStatus.AVAILABLE) {
				frame.getTextPane().setText("Network is available.");
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				frame.getTextPane().setText("Data initiating...");
				frame.getController().crawl();
				frame.getTextPane().setText("Data preparation Finished!");
			}
			else if (status == NetworkStatus.UNAVAILABLE)
				frame.getTextPane().setText("Network is not available!");
			else if (status == NetworkStatus.TIMEOUT)
				frame.getTextPane().setText("Network connection timeout!");
			
			frame.getBtnStart().setEnabled(true);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		if (arg instanceof String) 
			textPane.setText((String) arg);	
	}
	
	public void actionPerformed(ActionEvent e) {
		
		int res = JOptionPane.showConfirmDialog(null, "Take a deep breath...Are you ready for the game?\n(Timing will start when push \"Yes\")", "Dyscrabble", JOptionPane.YES_NO_OPTION); 
        if (res == JOptionPane.NO_OPTION) { 
        	return;
        } 
        
        if(e.getSource()==btnEasy){
        	this.setVisible(false);
        	MainViewController temp = new MainViewController(Difficulty.Easy);
        	temp.setVisible(true);
        }
        else if(e.getSource()==btnMedium){
        	this.setVisible(false);
        	MainViewController temp = new MainViewController(Difficulty.Medium);
        	temp.setVisible(true);
        }
        else if (e.getSource()==btnHard){
        	this.setVisible(false);
        	MainViewController temp = new MainViewController(Difficulty.Hard);
        	temp.setVisible(true);
        }
	}
}
