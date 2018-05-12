package edu.pitt.dbmi.nlp.noble.ui;

import edu.pitt.dbmi.nlp.noble.ontology.DefaultRepository;
import edu.pitt.dbmi.nlp.noble.ontology.IRepository;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;


/**
 * The Class TerminologyBrowser.
 */
/*
 * Terminology Browser dialog
 */
public class TerminologyBrowser {
	private QueryTool query;
	private OntologyExplorer ontologyExplorer;
	private JTabbedPane tabs;
	private JPanel browserPanel;
	private List<Concept> selectedConcepts;
	private JComboBox<Terminology> terminologyList;
	private JEditorPane infoText;
	
	/**
	 * create panel.
	 *
	 * @return the j panel
	 */
	public JPanel createPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		terminologyList = new JComboBox<Terminology>();
		terminologyList.setBorder(new TitledBorder("Terminologies"));
		terminologyList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSelectedTerminology((Terminology) terminologyList.getSelectedItem());
			}
		});
		
		if(query == null){
			query = new QueryTool();
			query.setPreferredSize(new Dimension(700,600));
		}
		if(ontologyExplorer == null){
			ontologyExplorer = new OntologyExplorer();
			ontologyExplorer.setPreferredSize(new Dimension(850,600));
		}
				
		infoText = new JEditorPane();
		infoText.setContentType("text/html; charset=UTF-8");
		infoText.setEditable(false);
		infoText.setPreferredSize(new Dimension(400,400));
		infoText.setBorder(new LineBorder(Color.gray));
		JScrollPane scroll = new JScrollPane(infoText);
		scroll.setPreferredSize(infoText.getPreferredSize());
		
		tabs = new JTabbedPane();
		tabs.addTab("Search", query);
		tabs.addTab("Browse", ontologyExplorer);
		tabs.addTab("Info",scroll);
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JTabbedPane t = (JTabbedPane) e.getSource();
		        switch(t.getSelectedIndex()){
		        case 0: 
		        	if(ontologyExplorer.getSelectedEntry() != null){
		        		query.setSelectedConcept((Concept)ontologyExplorer.getSelectedEntry());
		        	}
		        	break;
		        case 1:
		        	if(query.getSelectedConcept() != null){
		        		ontologyExplorer.setConcept(query.getSelectedConcept());
		        	}
		        	break;
		        }
		        
			}
		});
		
		panel.add(terminologyList,BorderLayout.NORTH);
		panel.add(tabs,BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * get panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel(){
		if(browserPanel == null)
			browserPanel = createPanel();
		return browserPanel;
	}
	
	/**
	 * set terminology .
	 *
	 * @param term the new selected terminology
	 */
	public void setSelectedTerminology(Terminology term){
		getPanel();
		query.setTerminology(term);
		try {
			ontologyExplorer.setTerminology(term);
			ontologyExplorer.setRoot(term.getRootConcepts());
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		terminologyList.setSelectedItem(term);
		setTerminologyInfo(term);
	}
	
	/**
	 * Sets the terminology info.
	 *
	 * @param t the new terminology info
	 */
	private void setTerminologyInfo(Terminology t){
		StringBuffer desc = new StringBuffer();
		desc.append("<h2>"+t.getName()+" ("+t.getVersion()+")</h2><hr>");
		desc.append("<p>"+t.getDescription()+"</p><br>");
		/*desc.append("<p>Languages: "+Arrays.toString(t.getLanguages())+"</p>");
		desc.append("<p>Sources: "+Arrays.toString(t.getSources())+"</p>");*/

		// add other information
		if(t instanceof NobleCoderTerminology){
			NobleCoderTerminology term = (NobleCoderTerminology)t;
			
			// get terminology properties
			Map<String,String> info = term.getTerminologyProperties();
			desc.append("<h3>Terminology Properties</h3>");
			desc.append("<ul>");
			for(String key: new TreeSet<String>(info.keySet())){
				desc.append("<li>"+key+" = "+info.get(key)+"</li>");
			}
			desc.append("</ul>");
			
			// set search properties
			Properties prop = term.getSearchProperties();
			desc.append("<h3>Search Properties</h3>");
			desc.append("<ul>");
			for(Object key: new TreeSet(prop.keySet())){
				desc.append("<li>"+key+" = "+prop.get(key)+"</li>");
			}
			desc.append("</ul>");
		}
		
		infoText.setText(desc.toString());
		infoText.setCaretPosition(0);
	}
	
	

	/**
	 * Sets the terminologies.
	 *
	 * @param terms the new terminologies
	 */
	public void setTerminologies(Terminology [] terms){
		getPanel();
		Arrays.sort(terms, new Comparator<Terminology>() {
			public int compare(Terminology o1, Terminology o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		((DefaultComboBoxModel) terminologyList.getModel()).removeAllElements();
		for(int i=0;i<terms.length;i++){
			terminologyList.addItem(terms[i]);
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				terminologyList.repaint();
			}
		});
	}
	

	/**
	 * Show dialog.
	 *
	 * @param parent the parent
	 * @param title the title
	 */
	public void showDialog(Component parent, String title){
		int r = JOptionPane.showConfirmDialog(parent,getPanel(),title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);		
		if(r == JOptionPane.OK_OPTION){
			selectedConcepts = new ArrayList<Concept>();
			if(tabs.getSelectedIndex() != 0){
				//selectedConcepts.add((Concept)ontologyExplorer.getSelectedEntry());
				for(Object obj: ontologyExplorer.getSelectedEntries()){
					selectedConcepts.add((Concept)obj);
				}
			}else{
				for(Concept c: query.getSelectedConcepts()){
					selectedConcepts.add(c);
				}
			}
		}else{
			selectedConcepts = null;
		}
	}
	
	/**
	 * Gets the selected concepts.
	 *
	 * @return the selected concepts
	 */
	public List<Concept> getSelectedConcepts(){
		return selectedConcepts != null? selectedConcepts:Collections.EMPTY_LIST;
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		IRepository r = new DefaultRepository();
		TerminologyBrowser browser = new TerminologyBrowser();
		browser.setTerminologies(r.getTerminologies());
		browser.setSelectedTerminology(r.getTerminology("NCI_Thesaurus"));
		browser.showDialog(null,"");
	}
}
