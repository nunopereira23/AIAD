package agents;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class PharmGui extends JFrame {	
	private Pharm myAgent;

	private JTextField titleField, priceField,orderField;
	private JTextField stockField;
	
	PharmGui(Pharm a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(6, 6));
		p.add(new JLabel("medicine title:"));
		titleField = new JTextField(15);
		p.add(titleField);
		p.add(new JLabel("Price:"));
		priceField = new JTextField(15);
		p.add(priceField);
		p.add(new JLabel("Order Delay:"));
		orderField = new JTextField(15);
		p.add(orderField);
		stockField = new JTextField("Stock");
		p.add(stockField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					String price = priceField.getText().trim();
					String stock = stockField.getText().trim();
					String order_time = orderField.getText().trim();
					myAgent.updateCatalogue(title, Integer.parseInt(price),Integer.parseInt(stock),1,Integer.parseInt(order_time));
					titleField.setText("");
					priceField.setText("");
					orderField.setText("");
					stockField.setText("");
					
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(PharmGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
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
