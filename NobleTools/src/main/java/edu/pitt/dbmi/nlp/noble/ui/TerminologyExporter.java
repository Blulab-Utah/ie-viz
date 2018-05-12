package edu.pitt.dbmi.nlp.noble.ui;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.tools.TermFilter;
import edu.pitt.dbmi.nlp.noble.ui.widgets.DynamicList;
import edu.pitt.dbmi.nlp.noble.ui.widgets.ResourceCellRenderer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * this class is responsible for copying one ontology into the other.
 *
 * @author tseytlin
 */

public class TerminologyExporter implements ActionListener {
	public static final String PROPERTY_PROGRESS_MSG = "ProgressMessage";
	private final URL ADD_ICON = getClass().getResource("/icons/Plus16.gif");
	private final URL REM_ICON = getClass().getResource("/icons/Minus16.gif");
	private final URL PREVIEW_ICON = getClass().getResource("/icons/Information24.gif");
	private final URL EXPORT_ICON = getClass().getResource("/icons/Export24.gif");
	public final String CODE = "Code";
	public final String SYNONYM = "Synonym";
	public final String DEFINITION = "Definition";
	public final String SEM_TYPE = "SemanticType";
	public final String ALT_CODE = "AlternateCode";
	public final String PREF_TERM = "PreferredTerm";
	//public static final String TERMINOLOGY_CORE = "http://blulab.chpc.utah.edu/ontologies/Terminology.owl";
	public static final String TERMINOLOGY_CORE = OntologyUtils.TERMINOLOGY_CORE;
	
	// GUI components
	private JPanel main;
	private TerminologyBrowser browser;
	//private OntologyExplorer ontologyExplorer;
	//private QueryTool query;
	private JList rootList,semTypeList;
	private DynamicList semanticTypeList;
	private JComboBox<Terminology> metathesaurusList; //terminologyList,
	private JCheckBox useMetaInfo,useMapping,useTermCore,depthCheck,termFilter;
	private JTextField outputFile,recursionDepth;
	private JTextArea console;
	private JPanel statusPanel,semanticTypePanel,mappingPanel;
	private JDialog wizard;
	private JProgressBar progress;
	private IRepository repository;
	private String defaultURI = "http://www.ontologies.com/";
	private Map<String,JTextField> mappingTable;
	private Map<String,String> propertyMap;
	private boolean exporting;
	private JButton export,preview;
	private JCheckBox useParent;
	private JTextField useParentText;

	/**
	 * Instantiates a new terminology exporter.
	 *
	 * @param r the r
	 */
	public TerminologyExporter(IRepository r){
		repository = r;
	}


	/**
	 * create an import wizzard.
	 *
	 * @param owner the owner
	 * @return the j dialog
	 */
	public JDialog showExportWizard(Component owner){
		//main = new JOptionPane(createWizardPanel(),
		//JOptionPane.PLAIN_MESSAGE,JOptionPane.CLOSED_OPTION);
		//wizard = main.createDialog(owner,"Export Terminology as OWL Ontology");
		main = createWizardPanel();
		main.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10),new BevelBorder(BevelBorder.RAISED)));
		wizard = new JDialog(JOptionPane.getFrameForComponent(owner));
		wizard.setTitle("Export Terminology as an OWL File");
		wizard.getContentPane().add(main);
		wizard.setModal(false);
		wizard.setResizable(true);
		wizard.pack();
		wizard.setLocationRelativeTo(owner);
		wizard.setVisible(true);
		loadTerminologies();
		return wizard;
	}
	


	
	/**
	 * load ontologies.
	 */
	public void loadTerminologies(){
		(new Thread(new Runnable(){
			public void run(){
				setBusy(true);
				Terminology [] ont = repository.getTerminologies();
				Arrays.sort(ont, new Comparator<Terminology>() {
					public int compare(Terminology o1, Terminology o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				for(int i=0;i<ont.length;i++){
					//terminologyList.addItem(ont[i]);
					metathesaurusList.addItem(ont[i]);
				}
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						selectDefaultMeta();
						//terminologyList.repaint();
						metathesaurusList.repaint();
						setBusy(false);
					}
				});
			}
		})).start();
	}
	
	/**
	 * Select default meta.
	 */
	private void selectDefaultMeta(){
		for(Terminology t: repository.getTerminologies()){
			if(t.getName().contains("UMLS") || t.getName().contains("Metathesaurus")){
				metathesaurusList.setSelectedItem(t);
				break;
			}
		}
	}
	
	/**
	 * display busy.
	 *
	 * @param busy the new busy
	 */
	public void setBusy(boolean busy){
		if(busy){
			statusPanel.removeAll();
			statusPanel.add(progress,BorderLayout.SOUTH);
		}else{
			JLabel lbl = new JLabel("");
			statusPanel.removeAll();
			statusPanel.add(lbl,BorderLayout.SOUTH);
		}
		statusPanel.revalidate();
		statusPanel.repaint();
	}
	
	
	/**
	 * create filter panel.
	 *
	 * @param label the label
	 * @param command the command
	 * @param list the list
	 * @return the j panel
	 */
	private JPanel createFilterPanel(String label, String command, JList list){
		list.setCellRenderer(new ResourceCellRenderer(){
			public Component getListCellRendererComponent(JList a, Object b, int c, boolean d, boolean e) {
				JLabel l = (JLabel) super.getListCellRendererComponent(a, b, c, d, e);
				if(b instanceof Concept){
					Concept cc = (Concept) b;
					int n = 0;
					try {
						n = cc.getChildrenConcepts().length;
					} catch (TerminologyException e1) {
						e1.printStackTrace();
					}
					String nn =  "<font color=\""+(n == 0?"red":"blue")+"\">"+n+"</font>";
					l.setText("<html>"+cc.getName()+" [<font color=green>"+cc.getTerminology().getName()+"</font>]  ( "+nn+" )");
				}
				return l;
			}
			
		});
		JToolBar toolbar = new JToolBar();
		JButton add = new JButton(new ImageIcon(ADD_ICON));
		add.setToolTipText("Add "+command);
		add.setActionCommand("add-"+command);
		add.addActionListener(this);
		
		JButton rem = new JButton(new ImageIcon(REM_ICON));
		rem.setToolTipText("Remove "+command);
		rem.setActionCommand("rem-"+command);
		rem.addActionListener(this);
		toolbar.add(add);
		toolbar.add(rem);
		toolbar.addSeparator();
		toolbar.add(new JLabel(label));
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new BorderLayout());
		selectPanel.add(toolbar,BorderLayout.NORTH);
		selectPanel.add(new JScrollPane(list),BorderLayout.CENTER);
		
		return selectPanel;
	}
	
	
	/**
	 * create ontology selector panel.
	 *
	 * @return the j panel
	 */
	private JPanel createWizardPanel(){
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(900,700));
				
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));
		
		//terminologyList = new JComboBox<Terminology>();
		//leftPanel.add(new JLabel("Terminology to Export"));
		//leftPanel.add(terminologyList);
		
		
		rootList = new JList(new DefaultListModel());
		rootList.setToolTipText("<html>If used, only selected branches will be exported,<br>"
				+ "if nothing is selected then the entire terminology will be exported");
		leftPanel.add(createFilterPanel("Add Branches to Export","Branch",rootList));
		
		
		semTypeList = new JList(new DefaultListModel());
		leftPanel.add(createFilterPanel("Add Semantic Types to Export","SemType",semTypeList));
		//leftPanel.add(Box.createRigidArea(new Dimension(50,50)));
	
		JPanel advanced = new JPanel();
		advanced.setBorder(new TitledBorder("Advanced Settings"));
		advanced.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),2,2);

		useMetaInfo  = new JCheckBox("Enrich concepts with another terminology");
		advanced.add(useMetaInfo,c);
		c.gridx++;
		
		metathesaurusList = new JComboBox<Terminology>();
		metathesaurusList.setEnabled(false);
		useMetaInfo.setOpaque(false);
		useMetaInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				metathesaurusList.setEnabled(useMetaInfo.isSelected());
			}
		});
		advanced.add(metathesaurusList,c);
		c.gridy++;
		c.gridx=0;
		
		useMapping = new JCheckBox("Use custom property map");
		advanced.add(useMapping,c);
		c.gridx++;
		final JToggleButton propMap = new JToggleButton("Property Mapping");
		propMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(panel,getPropertyMappingPanel(),"Property Mapping",JOptionPane.PLAIN_MESSAGE);
				propMap.setSelected(false);
			}
		});
		propMap.setEnabled(false);
		useMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propMap.setEnabled(	useMapping.isSelected());
			}
		});
		advanced.add(propMap,c);	
		c.gridy++;
		c.gridx=0;
		
		depthCheck = new JCheckBox("Limit recursion depth");
		advanced.add(depthCheck,c);
		c.gridx++;
		recursionDepth = new JTextField("3");
		recursionDepth.setEditable(false);
		advanced.add(recursionDepth,c);
		depthCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recursionDepth.setEditable(depthCheck.isSelected());
			}
		});
		c.gridx = 0;
		c.gridy++;
		
		useParent = new JCheckBox("Use parent class");
		advanced.add(useParent,c);
		c.gridx++;
		useParentText = new JTextField("Thing");
		useParentText.setEditable(false);
		advanced.add(useParentText,c);
		useParent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useParentText.setEditable(useParent.isSelected());
			}
		});
		
		c.gridy++;c.gridx = 0;
		termFilter = new JCheckBox("Filter bad synonyms",false);
		termFilter.setToolTipText("<html>Suppress problematic synonyms using rules described in <p><b>Hettne, Kristina M., et al.</b> \"<i>Rewriting and " + 
				" suppressing UMLS terms <br>for improved biomedical term identification.</i>\" Journal " + 
				" of biomedical semantics 1.1 (2010): 1. </p>");
		advanced.add(termFilter,c);
		
		
		leftPanel.add(advanced);
	
		
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(new JLabel("Output OWL File Location"));
		outputFile = new JTextField();
		JButton browse = new JButton("Browse");
		browse.addActionListener(this);
		browse.setActionCommand("browse");
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(outputFile,BorderLayout.CENTER);
		p.add(browse,BorderLayout.EAST);
		
		leftPanel.add(p);
		
		leftPanel.add(Box.createVerticalStrut(100));
		
		preview =new JButton("Preview",new ImageIcon(PREVIEW_ICON));
		preview.addActionListener(this);
		preview.setActionCommand("preview");
	
		export =new JButton("Export",new ImageIcon(EXPORT_ICON));
		export.addActionListener(this);
		export.setActionCommand("export");
		export.setPreferredSize(preview.getPreferredSize());
		
		JPanel bp = new JPanel();
		bp.setLayout(new FlowLayout(FlowLayout.CENTER));
		bp.add(preview);
		bp.add(export);
		leftPanel.add(bp);
		
		for(int i=0;i<leftPanel.getComponentCount();i++){
			if(leftPanel.getComponent(i) instanceof JComponent){
				JComponent cm = ((JComponent)leftPanel.getComponent(i));
				cm.setBorder(new CompoundBorder(cm.getBorder(),new EmptyBorder(5,5,5,5)));
				cm.setAlignmentX(Component.CENTER_ALIGNMENT);
			}
		}
		
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		console = new JTextArea();
		console.setEditable(false);
		JScrollPane s = new JScrollPane(console);
		s.setBorder(new TitledBorder("Console"));
		rightPanel.add(s,BorderLayout.CENTER);
	
		// setup main split
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//split.setLeftComponent(new JScrollPane(ontologyList));
		split.setLeftComponent(leftPanel);
		split.setRightComponent(rightPanel);
		split.setResizeWeight(0.5);
		split.setDividerLocation(520);
		panel.add(split,BorderLayout.CENTER);
		
		statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setStringPainted(false);
		statusPanel.add(new JLabel(""),BorderLayout.CENTER);
		panel.add(statusPanel,BorderLayout.SOUTH);
		
		return panel;
	}


	/**
	 * Gets the property mapping panel.
	 *
	 * @return the property mapping panel
	 */
	private JPanel getPropertyMappingPanel() {
		if(mappingPanel == null){
			mappingPanel = new JPanel();
			mappingPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),2,2);
			mappingTable = new LinkedHashMap<String, JTextField>();
			for(String p: getPropertyMapping().keySet()){
				JTextField t = new JTextField(propertyMap.get(p),20);
				mappingTable.put(p,t);
				mappingPanel.add(new JLabel(p),c);
				c.gridx++;
				mappingPanel.add(t,c);
				c.gridy++;
				c.gridx = 0;
			}
			c.gridx=0;
			c.gridy++;
			c.gridwidth = 2;
			useTermCore = new JCheckBox("<html>Import property mappings from an <a href=\""+TERMINOLOGY_CORE+"\">online resource</a>");
			useTermCore.setToolTipText("<html>Import a terminology definitions from <a href=\""+TERMINOLOGY_CORE+"\">"+TERMINOLOGY_CORE+"</a>"
					+ "<br>instead of redifining custom property mappings for each exported ontology");
			useTermCore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(String p: mappingTable.keySet()){
						mappingTable.get(p).setEditable(!useTermCore.isSelected());
					}
					
				}
			});
			mappingPanel.add(useTermCore,c);
			
		}
		return mappingPanel;
	}
	
	/**
	 * get property mapping.
	 *
	 * @return the property mapping
	 */
	private Map<String,String> getPropertyMapping(){
		if(propertyMap == null){
			propertyMap = new LinkedHashMap<String, String>();
			propertyMap.put(CODE,CODE);
			propertyMap.put(SYNONYM,SYNONYM);
			propertyMap.put(DEFINITION,DEFINITION);
			propertyMap.put(SEM_TYPE,SEM_TYPE);
			propertyMap.put(ALT_CODE,ALT_CODE);
			propertyMap.put(PREF_TERM,PREF_TERM);
		}
		if(!exporting){
			if(useMapping.isSelected() && mappingTable != null && !mappingTable.isEmpty()){
				for(String p: propertyMap.keySet()){
					JTextField t = mappingTable.get(p);
					propertyMap.put(p,t.getText().trim());
				}
			}
			if(useTermCore != null && useTermCore.isSelected()){
				propertyMap.put(CODE,TERMINOLOGY_CORE+"#code");
				propertyMap.put(SYNONYM,TERMINOLOGY_CORE+"#synonym");
				propertyMap.put(DEFINITION,TERMINOLOGY_CORE+"#definition");
				propertyMap.put(SEM_TYPE,TERMINOLOGY_CORE+"#semanticType");
				propertyMap.put(ALT_CODE,TERMINOLOGY_CORE+"#alternateCode");
				propertyMap.put(PREF_TERM,TERMINOLOGY_CORE+"#preferredTerm");
			}
		}
		return propertyMap;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	// actions
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().toLowerCase();
		if(cmd.equals("add-branch")){
			doAddRoot();
		}else if(cmd.equals("rem-branch")){
			doRemove(rootList);
		}else if(cmd.equals("add-semtype")){
			doAddSemType();
		}else if(cmd.equals("rem-semtype")){
			doRemove(semTypeList);
		}else if(cmd.equals("browse")){
			doBrowse();
		}else if(cmd.equals("export")){
			doExport();
		}else if(cmd.equals("preview")){
			doPreview();
		}
	}
	

	/**
	 * Do browse.
	 */
	private void doBrowse() {
		JFileChooser fc = new JFileChooser(outputFile.getText());
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				return "OWL Ontology File (.owl)";
			}
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".owl");
			}
		});
		int n = fc.showSaveDialog(main);
		if(n == JFileChooser.APPROVE_OPTION){
			File f = fc.getSelectedFile();
			if(!f.getName().endsWith(".owl")){
				f = new File(f.getParentFile(),f.getName()+".owl");
			}
			outputFile.setText(f.getAbsolutePath());
		}
	}

	/**
	 * Do add sem type.
	 */
	private void doAddSemType() {
		if(semanticTypePanel == null){
			semanticTypePanel = createSemanticTypeSelector();
		}
		int r = JOptionPane.showConfirmDialog(main,semanticTypePanel,"Add SemanticType",
				JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);		
		if(r == JOptionPane.OK_OPTION){
			for(Object obj: semanticTypeList.getSelectedValuesList())
				addObject(semTypeList,obj);
		}
		
	}

	/**
	 * Creates the semantic type selector.
	 *
	 * @return the j panel
	 */
	private JPanel createSemanticTypeSelector() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(350,300));
		panel.setLayout(new BorderLayout());
		JTextField search = new JTextField();
		search.setForeground(Color.lightGray);
		semanticTypeList = new DynamicList(search,getAllSemanticTypes());
		semanticTypeList.setMatchMode(DynamicList.CONTAINS_MATCH);
		panel.add(search,BorderLayout.NORTH);
		panel.add(new JScrollPane(semanticTypeList),BorderLayout.CENTER);
		return panel;
	}

	/**
	 * get a list of all emantic types.
	 *
	 * @return the all semantic types
	 */
	private List<String> getAllSemanticTypes(){
		List list = new ArrayList();
		for(SemanticType s: SemanticType.getDefinedSemanticTypes()){
			list.add(s.getName());
		}
		Collections.sort(list);
		return list;
	}
	
	
	/**
	 * Do remove.
	 *
	 * @param list the list
	 */
	private void doRemove(JList list){
		int [] sel = list.getSelectedIndices();
		List objs = new ArrayList();
		for(int i: sel){
			objs.add(((DefaultListModel)list.getModel()).elementAt(i));
		}
		for(Object i: objs){
			((DefaultListModel)list.getModel()).removeElement(i);
		}
	}
	
	
	
	/**
	 * Do add root.
	 */
	private void doAddRoot(){
		if(browser == null){
			browser = new TerminologyBrowser();
			browser.setTerminologies(repository.getTerminologies());
		}
		browser.showDialog(main,"Add Branch");
		List<Concept> result = browser.getSelectedConcepts();
		if(result != null){
			for(Concept c: result){
				addObject(rootList, c);
			}
		}
	}
	
	/**
	 * add root class.
	 *
	 * @param list the list
	 * @param obj the obj
	 */
	private void addObject(JList list, Object obj){
		final JList llist = list;
		DefaultListModel model = (DefaultListModel) list.getModel();
		if(!model.contains(obj))
			model.addElement(obj);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				llist.repaint();
			}
		});
	}
	
	/**
	 * get selected ontology.
	 *
	 * @return 	public Terminology getSelectedTerminology(){
	 * 		Object obj = terminologyList.getSelectedItem();
	 * 		return (obj != null && obj instanceof Terminology)?(Terminology)obj:null;
	 * 	}
	 */
	
	/**
	 * get selected classes
	 * @return
	 *
	public Concept [] getSelectedConcepts(){
		Terminology ont = getSelectedTerminology();
		if(ont == null)
			return new Concept [0];
		Concept [] cls = new Concept [rootList.getModel().getSize()];
		for(int i=0;i<cls.length;i++)
			cls[i] = (Concept) rootList.getModel().getElementAt(i);
		try {
			return (cls.length > 0)?cls:ont.getRootConcepts();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return new Concept [0];
	}
	*/
	
	
	/**
	 * do export
	 */

	private void doExport() {
		setBusy(true);
		export.setEnabled(false);
		console.setText("");
		(new Thread(new Runnable(){
			public void run(){
				File ontologyFile = new File(outputFile.getText());
				if(outputFile.getText().length() == 0 || ontologyFile == null || ontologyFile.isDirectory() || !ontologyFile.getParentFile().exists()){
					String msg = "Not valid ontology file "+ontologyFile.getAbsolutePath();
					//console.append("Error: "+msg+"\n");
					JOptionPane.showMessageDialog(main,msg,"Error",JOptionPane.ERROR_MESSAGE);
					setBusy(false);
					return;
				}
				if(rootList.getModel().getSize() == 0){
					String msg = "You must select a set of branches to export";
					JOptionPane.showMessageDialog(main,msg,"Error",JOptionPane.ERROR_MESSAGE);
					setBusy(false);
					return;
				}
				boolean filterTerms = termFilter.isSelected();
				int depth = Integer.MAX_VALUE;
				if(depthCheck.isSelected())
					depth = Integer.parseInt(recursionDepth.getText());
				//Terminology term = (Terminology) terminologyList.getSelectedItem();
				Terminology umls = (Terminology)((useMetaInfo.isSelected())?metathesaurusList.getSelectedItem():null);
				List<Concept> rootFilter = new ArrayList<Concept>();
				List<SemanticType> semanticTypeFilter = new ArrayList<SemanticType>();
				for(int i=0;i<rootList.getModel().getSize();i++){
					rootFilter.add((Concept)rootList.getModel().getElementAt(i));
				}
				for(int i=0;i<semTypeList.getModel().getSize();i++){
					semanticTypeFilter.add(SemanticType.getSemanticType(""+semTypeList.getModel().getElementAt(i)));
				}
				try {
					IOntology ont = null;
					if(ontologyFile.exists()){
						ont = OOntology.loadOntology(ontologyFile);
					}else{
						ont = OOntology.createOntology(URI.create(defaultURI+ontologyFile.getName()));
					}
					IClass root = ont.getRoot();
					if(useParent.isSelected()){
						IClass cls = ont.getClass(useParentText.getText().trim());
						if(cls != null){
							root = cls;
						}
					}
					export(null,umls,rootFilter,semanticTypeFilter,root,depth,termFilter.isSelected());
					if(ontologyFile.exists()){
						ont.save();
					}else{
						ont.write(new FileOutputStream(ontologyFile),IOntology.OWL_FORMAT);
					}
				} catch (Exception e) {
					console.append("Error: "+e.getMessage()+"\n");
					JOptionPane.showMessageDialog(main,"Problems encounted during export","Error",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				export.setEnabled(true);
				setBusy(false);
			}

		
		})).start();
		
	}
	
	/**
	 * do export.
	 */

	private void doPreview() {
		setBusy(true);
		preview.setEnabled(false);
		console.setText("");
		(new Thread(new Runnable(){
			public void run(){
				if(rootList.getModel().getSize() == 0){
					String msg = "You must select a set of branches to export";
					JOptionPane.showMessageDialog(main,msg,"Error",JOptionPane.ERROR_MESSAGE);
					setBusy(false);
					return;
				}
				//Terminology term = (Terminology) terminologyList.getSelectedItem();
				List<Concept> rootFilter = new ArrayList<Concept>();
				List<SemanticType> semanticTypeFilter = new ArrayList<SemanticType>();
				for(int i=0;i<rootList.getModel().getSize();i++){
					rootFilter.add((Concept)rootList.getModel().getElementAt(i));
				}
				for(int i=0;i<semTypeList.getModel().getSize();i++){
					semanticTypeFilter.add(SemanticType.getSemanticType(""+semTypeList.getModel().getElementAt(i)));
				}
				int depth = Integer.MAX_VALUE;
				if(depthCheck.isSelected())
					depth = Integer.parseInt(recursionDepth.getText());
				try {
					preview(rootFilter,semanticTypeFilter,depth);
				} catch (Exception e) {
					console.append("Error: "+e.getMessage()+"\n");
					JOptionPane.showMessageDialog(main,"Problems encounted during export","Error",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				preview.setEnabled(true);
				setBusy(false);
			}

		
		})).start();
		
	}
	
	/**
	 * export terminology to ontology.
	 *
	 * @param term the term
	 * @param umls the umls
	 * @param rootFilter the root filter
	 * @param semanticTypeFilter the semantic type filter
	 * @param root the root
	 * @throws Exception the exception
	 */
	public void export(Terminology term, Terminology umls,List<Concept> rootFilter, List<SemanticType> semanticTypeFilter,IClass root) throws Exception {
		export(term,umls,rootFilter,semanticTypeFilter,root, Integer.MAX_VALUE,false);
	}
	
	/**
	 * export terminology to ontology.
	 *
	 * @param term the term
	 * @param umls the umls
	 * @param rootFilter the root filter
	 * @param semanticTypeFilter the semantic type filter
	 * @param root the root
	 * @param depth the depth
	 * @param filterTerms true or false
	 * @throws Exception the exception
	 */
	public void export(Terminology term, Terminology umls,List<Concept> rootFilter, List<SemanticType> semanticTypeFilter,IClass root,int depth,boolean filterTerms) throws Exception {
		IOntology ont = root.getOntology();
		
		// import term if necessary
		if(getPropertyMapping().get(CODE).startsWith(TERMINOLOGY_CORE)){
			ont.addImportedOntology(OOntology.loadOntology(new URL(TERMINOLOGY_CORE)));
		}
		exporting = true;
		List<Concept> roots = (rootFilter.isEmpty())?Arrays.asList(term.getRootConcepts()):rootFilter;
		for(Concept c: roots){
			exportConcept(c,umls,semanticTypeFilter,"",root,depth,filterTerms);
		}
		exporting = false;
	}
	
	/**
	 * export terminology to ontology.
	 *
	 * @param rootFilter the root filter
	 * @param semanticTypeFilter the semantic type filter
	 * @throws Exception the exception
	 */
	public void preview(List<Concept> rootFilter, List<SemanticType> semanticTypeFilter) throws Exception {
		preview(rootFilter,semanticTypeFilter,Integer.MAX_VALUE);
	}
	
	/**
	 * export terminology to ontology.
	 *
	 * @param rootFilter the root filter
	 * @param semanticTypeFilter the semantic type filter
	 * @param depth the depth
	 * @throws Exception the exception
	 */
	public void preview(List<Concept> rootFilter, List<SemanticType> semanticTypeFilter,int depth) throws Exception {
		for(Concept c: rootFilter){
			previewConcept(c,semanticTypeFilter,"", new HashSet<Concept>(),depth);
		}
	}
	
	
	/**
	 * export terminology to ontology.
	 *
	 * @param term the term
	 * @param rootFilter the root filter
	 * @param root the root
	 * @throws Exception the exception
	 */
	public void export(Terminology term, List<Concept> rootFilter,IClass root) throws Exception {
		export(term,null, rootFilter,Collections.EMPTY_LIST, root);
	}
	
	
	/**
	 * export single concept as class.
	 *
	 * @param c the c
	 * @param umls the umls
	 * @param semanticTypeFilter the semantic type filter
	 * @param prefix the prefix
	 * @param parent the parent
	 * @param depth the depth
	 * @throws Exception the exception
	 */
	private void exportConcept(Concept c,Terminology umls, List<SemanticType> semanticTypeFilter,String prefix, IClass parent, int depth, boolean filterTerms) throws Exception {
		// first make sure that it fits the filter
		if(c == null || isFilteredOut(c, semanticTypeFilter) || depth  == 0){
			return;
		}
		IOntology ont = parent.getOntology();
		String clsName = OntologyUtils.toResourceName(c.getName());
		IClass cls = ont.getClass(clsName);
		// if class exists, then we have a cycle, just add a parent and quit
		if(cls != null){
			if(!(cls.equals(parent) || cls.hasSuperClass(parent) || cls.hasSubClass(parent)))
				cls.addSuperClass(parent);
			return;
		}
		
		// create class
		cls = parent.createSubClass(clsName);
		cls.addLabel(c.getName());
		addConceptInfo(c, cls,filterTerms);
		if(umls != null)
			addConeptInfoFromUMLS(cls,umls,filterTerms);
		
		// output
		console.append(prefix+c.getName()+"\n");
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				console.repaint();
			}
		});
		
		// now go into children
		for(Concept child: c.getChildrenConcepts()){
			exportConcept(child,umls,semanticTypeFilter,"  "+prefix,cls,depth-1,filterTerms);
		}
	}
	
	/**
	 * export single concept as class.
	 *
	 * @param c the c
	 * @param semanticTypeFilter the semantic type filter
	 * @param prefix the prefix
	 * @param parents the parents
	 * @param depth the depth
	 * @throws Exception the exception
	 */
	private void previewConcept(Concept c,List<SemanticType> semanticTypeFilter,String prefix,Set<Concept> parents, int depth) throws Exception {
		// first make sure that it fits the filter
		if(c == null || isFilteredOut(c, semanticTypeFilter) || parents.contains(c) || depth == 0){
			return;
		}
		// output
		console.append(prefix+c.getName()+"\n");
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				console.repaint();
			}
		});
		parents.add(c);
		// now go into children
		for(Concept child: c.getChildrenConcepts()){
			previewConcept(child,semanticTypeFilter,"  "+prefix,parents,depth-1);
		}
	}
	
	
	
	/**
	 * Adds the conept info from UMLS.
	 *
	 * @param cls the cls
	 * @param umls the umls
	 * @throws TerminologyException the terminology exception
	 */
	private void addConeptInfoFromUMLS(IClass cls, Terminology umls, boolean filterTerms) throws TerminologyException{
		String name = cls.getLabels()[0];
		if(umls != null){
			for(Concept c: umls.search(name)){
				if(c.getMatchedTerm().equals(name)){
					addConceptInfo(c, cls,filterTerms);
				}
			}
		}
	}
	
	/**
	 * add concept info from concept object to class.
	 *
	 * @param c the c
	 * @param cls the cls
	 */
	private void addConceptInfo(Concept c, IClass cls, boolean filterTerms) {
		IOntology ont = cls.getOntology();
		
		Map<String,String> map = getPropertyMapping();
		IProperty code = ont.getProperty(map.get(CODE));
		IProperty synonym = ont.getProperty(map.get(SYNONYM));
		IProperty definition = ont.getProperty(map.get(DEFINITION));
		IProperty semType = ont.getProperty(map.get(SEM_TYPE));
		IProperty altCode = ont.getProperty(map.get(ALT_CODE));
		IProperty prefTerm = ont.getProperty(map.get(PREF_TERM));
		
		
		
		if(code == null)
			code = ont.createProperty(map.get(CODE),IProperty.ANNOTATION);
		if(synonym == null)
			synonym = ont.createProperty(map.get(SYNONYM),IProperty.ANNOTATION);
		if(definition == null)
			definition = ont.createProperty(map.get(DEFINITION),IProperty.ANNOTATION);
		if(semType == null)
			semType = ont.createProperty(map.get(SEM_TYPE),IProperty.ANNOTATION);
		if(altCode == null)
			altCode = ont.createProperty(map.get(ALT_CODE),IProperty.ANNOTATION);
		if(prefTerm == null)
			prefTerm = ont.createProperty(map.get(PREF_TERM),IProperty.ANNOTATION);
		
		// add preferred term
		cls.addPropertyValue(prefTerm,c.getName());
		
		
		// optionally filter synonyms
		Collection<String> synonyms = null;
		if(filterTerms){
			synonyms = TermFilter.filter(c.getSynonyms());
		}else{
			synonyms = Arrays.asList(c.getSynonyms());
		}
		
		// add synonyms
		for(String s: synonyms){
			if(!cls.hasPropetyValue(synonym, s))
				cls.addPropertyValue(synonym, s);
		}
		
		// add definitions
		for(Definition d: c.getDefinitions()){
			if(!cls.hasPropetyValue(definition,d.getDefinition()))
				cls.addPropertyValue(definition,d.getDefinition());
		}
		
		// get concept code 
		cls.setPropertyValue(code,c.getCode());
		for(Object src: c.getCodes().keySet()){
			String cui = (String) c.getCodes().get(src)+" ["+src+"]";
			if(!cls.hasPropetyValue(altCode,cui))
				cls.addPropertyValue(altCode,cui);
		}
		
		// get semantic types
		for(SemanticType st: c.getSemanticTypes()){
			if(!cls.hasPropetyValue(semType,st.getName()))
				cls.addPropertyValue(semType,st.getName());
		}
		
	}


	/**
	 * get or create property.
	 *
	 * @param ont the ont
	 * @param name the name
	 * @return the property
	 */
	private IProperty getProperty(IOntology ont, String name){
		IProperty code = ont.getProperty(name);
		if(code == null){
			code = ont.createProperty(name,IProperty.ANNOTATION_DATATYPE);
			code.setRange(new String [0]);
		}
		return code;
	}
	
	
	/**
	 * is concept filtered out by sem.
	 *
	 * @param c the c
	 * @param semanticTypeFilter the semantic type filter
	 * @return true, if is filtered out
	 */
	private boolean isFilteredOut(Concept c, List<SemanticType> semanticTypeFilter){
		if(semanticTypeFilter.isEmpty())
			return false;
		for(SemanticType st: c.getSemanticTypes()){
			if(semanticTypeFilter.contains(st))
				return false;
		}
		return true;
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		TerminologyExporter importer = new TerminologyExporter(new DefaultRepository());
		JDialog d = importer.showExportWizard(null);
		
	}

}
