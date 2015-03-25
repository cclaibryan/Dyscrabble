package ViewControllers;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import Models.ModelController;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class StartViewController extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JButton btnStart;
	private ModelController controller;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartViewController frame = new StartViewController();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public JTextField getTextField() {
		 return textField;
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
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//button definitions
		btnStart = new JButton("Start");
		btnStart.setBounds(163, 118, 117, 29);
		contentPane.add(btnStart);
		
		final JButton btnEasy = new JButton("Easy");
		btnEasy.setBounds(24, 173, 117, 29);
		contentPane.add(btnEasy);
		
		final JButton btnMedium = new JButton("Medium");
		btnMedium.setBounds(163, 173, 117, 29);
		contentPane.add(btnMedium);
		
		final JButton btnDifficult = new JButton("Difficult");
		btnDifficult.setBounds(301, 173, 117, 29);
		contentPane.add(btnDifficult);
		
		final JButton btnBack = new JButton("Back");
		btnBack.setBounds(163, 226, 117, 29);
		contentPane.add(btnBack);
		
		//difficulty buttons are not visible at first
		btnEasy.setVisible(false);
		btnMedium.setVisible(false);
		btnDifficult.setVisible(false);
		btnBack.setVisible(false);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStart.setVisible(false);
				btnEasy.setVisible(true);
				btnMedium.setVisible(true);
				btnDifficult.setVisible(true);
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
				btnDifficult.setVisible(false);
				btnBack.setVisible(false);
			}
		});
		
		btnStart.setEnabled(false);		//start button is not available before the network download is finished
		
		//tip field below
		textField = new JTextField();
		textField.setBorder(new EmptyBorder(0,0,0,0));
		textField.setBounds(0, 263, 450, 15);
		textField.setBackground(new Color(237, 209, 166));
		textField.setText("testing");
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		contentPane.add(textField);
		textField.setColumns(10);
	}
	
	
	public StartViewController() {
		loadComponents();
		controller = ModelController.getInstance();		//initiate the model controller
		
		//new thread for initial and crawling the articles
		LoadingThread thread = new LoadingThread(this);	
		thread.start();
	}
	
	class LoadingThread extends Thread {
		private StartViewController frame;
		public LoadingThread(StartViewController frame) {
			this.frame = frame;
		}
		@Override
		public void run() {
			ModelController controller = frame.getController();
			frame.getTextField().setText("Network detecting!");
			if (controller.netDetect()) {
				frame.getTextField().setText("Network is available!");
				try {
					sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				frame.getTextField().setText("Data initiating...");
				frame.getController().crawl();
				frame.getTextField().setText("Data preparation Finished!");
			}
			else 
				frame.getTextField().setText("Network is not available!");
			
			frame.getBtnStart().setEnabled(true);
		}
	}
}
