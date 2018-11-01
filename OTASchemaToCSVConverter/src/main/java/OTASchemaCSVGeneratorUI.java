import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.namespace.QName;

public class OTASchemaCSVGeneratorUI extends JFrame implements ActionListener {
	
	private static final String BROWSE_ACTION = "BROWSE";
	private static final String GENERATE_ACTION = "GENERATE";
	private static final String DEFAULT_OTA_NS = "http://www.opentravel.org/OTA/2003/05";
	private JTextField schemaFile, schemaElemName, schemaElemURI, schemaCSVFile;
	
	public OTASchemaCSVGeneratorUI() {
		initialize();
	}

	public void initialize() {
		this.setTitle("OTA Schema to CSV");
		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		
		Container cPane = this.getContentPane();
		cPane.setLayout(gbl);
		
		gbc.anchor =GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		JLabel schemaFileLabel = new JLabel("Schema File");
		cPane.add(schemaFileLabel, gbc);
		
		schemaFile = new JTextField(60);
		gbc.gridx++;
		gbc.weightx = 1.0;
		cPane.add(schemaFile, gbc);
		
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(BROWSE_ACTION);
		browseButton.addActionListener(this);
		gbc.gridx++;
		gbc.weightx = 0.0;
		cPane.add(browseButton, gbc);
		
		JLabel spaceLabel = new JLabel("");
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		cPane.add(spaceLabel, gbc);
		
		//------------------------------------------------------------------------------
		JPanel elemPanel = new JPanel();
		elemPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Element to Export"));
		GridBagLayout gblElem = new GridBagLayout();
		elemPanel.setLayout(gblElem);
		
		GridBagConstraints gbcElem = new GridBagConstraints();
		gbcElem.anchor = GridBagConstraints.WEST;
		gbcElem.gridx = 0;
		gbcElem.gridy = 0;
		gbcElem.weightx = 0.0;
		gbcElem.weighty = 0.0;
		
		JLabel nsURILabel = new JLabel("Element NamespaceURI");
		elemPanel.add(nsURILabel, gbcElem);
		schemaElemURI = new JTextField(50);
		schemaElemURI.setText(DEFAULT_OTA_NS);
		gbcElem.gridx++;
		gbcElem.weightx = 1.0;
		elemPanel.add(schemaElemURI, gbcElem);
		
		JLabel elemNameLabel = new JLabel("Element Name");
		gbcElem.gridx = 0;
		gbcElem.gridy++;
		gbcElem.weightx = 0.0;
		elemPanel.add(elemNameLabel, gbcElem);
		schemaElemName = new JTextField(50);
		gbcElem.gridx++;
		gbcElem.weightx = 1.0;
		elemPanel.add(schemaElemName, gbcElem);
		//--------------------------------------------------------------------------------
		
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		cPane.add(elemPanel, gbc);
		
		gbc.gridy++;
		gbc.weightx = 0.0;
		cPane.add(spaceLabel, gbc);
		
		JLabel csvFileLabel = new JLabel("Export to CSV File");
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		cPane.add(csvFileLabel, gbc);
		schemaCSVFile = new JTextField(60);
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		cPane.add(schemaCSVFile, gbc);
		
		JButton genButton = new JButton("Generate");
		genButton.setActionCommand(GENERATE_ACTION);
		genButton.addActionListener(this);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		cPane.add(genButton, gbc);
		
		this.addWindowListener( 
				new WindowAdapter() {
					public void windowClosing(WindowEvent we) {
						System.exit(0);
					}
				}
		);
	}
	
	public static void main(String[] args) {
		OTASchemaCSVGeneratorUI otaUI = new OTASchemaCSVGeneratorUI();
		otaUI.pack();
		otaUI.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object sourceObj = e.getSource();
		if (sourceObj instanceof JButton) {
			JButton srcButton = (JButton) sourceObj;
			if (BROWSE_ACTION.equals(srcButton.getActionCommand())) {
				JFileChooser schemaFileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("XSD Schema Files", "xsd");
				schemaFileChooser.setFileFilter(filter);
				int retVal = schemaFileChooser.showOpenDialog(this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					schemaFile.setText(schemaFileChooser.getSelectedFile().getAbsolutePath());
				}
				return;
			}
			
			if (GENERATE_ACTION.equals(srcButton.getActionCommand())) {
				if (isValidateInput() == true) {
					try {
						QName schemaElem = new QName(schemaElemURI.getText(), schemaElemName.getText());
						OTASchemaTraverser.exportSchemaToCSV(schemaFile.getText(), schemaElem, schemaCSVFile.getText());
						JOptionPane.showMessageDialog(this, "Schema was successfully exported to CSV file!");
					}
					catch(Exception x) {
						JOptionPane.showMessageDialog(this, "An error occurred while exporting schema to CSV!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
	}
	
	public boolean isValidateInput() {
		File scFile = new File(schemaFile.getText());
		if (scFile.exists() == false) {
			JOptionPane.showMessageDialog(schemaFile, "Invalid file name for schema!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if (schemaElemName.getText().trim().isEmpty() || schemaElemURI.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Element name and namespaceURI for element to be exported must be specified!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File csvFile = new File(schemaCSVFile.getText());
		if (csvFile.getParentFile().exists() == false) {
			JOptionPane.showMessageDialog(this, "Directory " + csvFile.getParentFile().getAbsolutePath() + " for exporting CSV file does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if (csvFile.exists() == true) {
			if (JOptionPane.showConfirmDialog(this, "The CSV file to be exported already exists. Do you want to overwrite this file?", "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		
		return true;
	}
}
