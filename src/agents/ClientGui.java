package agents;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class ClientGui extends JFrame {	
	private Client myAgent;

	private JTextField titleField, dlField;
	private JCheckBox stockField;
	private JRadioButton price,time,mixed; 
	
	ClientGui(Client a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(6, 6));
		p.add(new JLabel("Product Name:"));
		titleField = new JTextField(15);
		p.add(titleField);
		p.add(new JLabel("Deadline:"));
		dlField = new JTextField(15);
		p.add(dlField);
		
		//Type of search
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    price = new JRadioButton("Price");
	    price.setActionCommand("price");
	    time = new JRadioButton("Time");
	    time.setActionCommand("time");
	    mixed = new JRadioButton("Mixed");
	    mixed.setActionCommand("mixed");
	    price.setSelected(true);
	    group.add(price);
	    group.add(time);
	    group.add(mixed);
	    p.add(new JLabel("Type of Search: "));
	    p.add(price);
	    p.add(time);
	    p.add(mixed);
	   
	  
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("BUY!");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					String deadline = dlField.getText().trim();
					String s_type = group.getSelection().getActionCommand();
					myAgent.buyProduct(title, s_type);
					titleField.setText("");
					dlField.setText("");
					
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(ClientGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
