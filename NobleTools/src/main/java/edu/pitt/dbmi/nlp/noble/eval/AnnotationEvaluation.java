package edu.pitt.dbmi.nlp.noble.eval;

import edu.pitt.dbmi.nlp.noble.eval.ehost.InstancesToEhost;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.FileTools;
import edu.pitt.dbmi.nlp.noble.util.HTMLExporter;
import edu.pitt.dbmi.nlp.noble.util.UITools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class AnnotationEvaluation implements ActionListener {
	public static final String DISJOINT_SPANS = "\\s+"; // span seperate
	public static final String SPAN_SEPERATOR = ":";    //within a span Ex: 12:45
	public static final String ANALYSIS_HTML = "analysis.html";
	public static final String EVALUATION_HTML = "evaluation.html";
	public static final String ANALYSIS_TSV = "analysis.tsv";
	public static boolean STRICT_VALUE_CALCULATION = false;
	public static boolean PRINT_RECORD_LEVEL_STATS = false;
	private Map<String,Double> attributeWeights;
	private Set<IClass> validAnnotations;
	private Analysis analysis;
	
	// UI components
	private JDialog dialog;
	private JTextField goldOntology, inputDocuments,systemOntology, goldWeights, classFilter;
	private JTextArea console;
	private JPanel buttonPanel;
	private JProgressBar progress;
	private File lastFile;
	
	public static void main(String[] args) throws Exception {
		new AnnotationEvaluation().getDialog().setVisible(true);
	}

	/**
	 * output HTML files to a given parent directory
	 * @param parentFile - directory
	 * @throws IOException - in case we mess up
	 */
	public void outputHTML(File parentFile) throws IOException {
		HTMLExporter exporter = new HTMLExporter(parentFile);
		exporter.export(getAnalysis());
	}

	/**
	 * output TSV to file
	 * @param parentFile - directory
	 * @throws FileNotFoundException - in case we mess up
	 */
	public void outputTSV(File parentFile) throws FileNotFoundException {
		PrintStream fos = new PrintStream(new File(parentFile,ANALYSIS_TSV));
		getAnalysis().printResultTable(fos);
		fos.close();
	}


	
	/**
	 * get analysis object for a given evaluation
	 * @return analysis object
	 */
	public Analysis getAnalysis(){
		if(analysis == null)
			analysis = new Analysis();
		return  analysis;
	}

	/**
	 * load attribute weights
	 * @param weights - weights file
	 * @throws IOException  - in case we mess up
	 * @throws FileNotFoundException  - in case we mess up
	 * @throws NumberFormatException  - in case we mess up
	 */
	public void loadWeights(File weights) throws NumberFormatException, FileNotFoundException, IOException {
		if(weights != null){
			for(String l: TextTools.getText(new FileInputStream(weights)).split("\n")){
				String [] p = l.split("\t");
				if(p.length == 2){
					getAttributeWeights().put(p[0].trim(),Double.parseDouble(p[1]));
				}
			}
		}
	}
	
	
	/**
	 * get attribute weights loaded from the corpus
	 * @return mapping of attribute name to its weight
	 */
	public Map<String, Double> getAttributeWeights() {
		if(attributeWeights == null)
			attributeWeights = new LinkedHashMap<String, Double>();
		return attributeWeights;
	}

	
	/**
	 * evaluate phenotype of two .OWL instances files
	 * @param file1 - gold instances file
	 * @param file2 - system instances file
	 * @throws IOException  - in case we mess up
	 * @throws IOntologyException  - in case we mess up
	 */
	
	public void evaluate(File file1, File file2) throws IOException, IOntologyException {
		IOntology goldInstances = OOntology.loadOntology(file1);
		IOntology systemInstances = OOntology.loadOntology(file2);
	
		// load valid annotations for totals
		loadAnnotationFilter(goldInstances);
		
		// init confusionMatrix
		analysis = new Analysis();
		analysis.setTitle("Results for "+file1.getName()+" on "+new Date());
		Analysis.ConfusionMatrix mentionConfusion = analysis.getConfusionMatrix(" Overall Mention");
		//Analysis.ConfusionMatrix documentConfusion = analysis.getConfusionMatrix(" Document");

		
		// get composition
		List<IInstance> goldCompositions = getCompositions(goldInstances);
		List<IInstance> candidateCompositions = getCompositions(systemInstances);
		

		for(IInstance gold: goldCompositions){
			IInstance cand = getMatchingComposition(candidateCompositions,gold);
			if(cand != null){
				calculateDocumentConfusion(gold, cand,DomainOntology.HAS_MENTION_ANNOTATION,mentionConfusion);
				//calculateDocumentConfusion(gold, cand,DomainOntology.HAS_DOCUMENT_ANNOTATION,documentConfusion);
			}
		}
		
		// print results
		analysis.printResultTable(System.out);
	}

	/**
	 * go over 
	 * @param ontology
	 */
	private void loadAnnotationFilter(IOntology ontology) {
		validAnnotations = new HashSet<IClass>();
		for(IClass cls: ontology.getClass(DomainOntology.ANNOTATION).getSubClasses()){
			if(cls.getDirectInstances().length > 0)
				validAnnotations.add(cls);	
		}
	}

	private Set<IClass> getAnnotationFilter(){
		if(validAnnotations == null)
			validAnnotations = new HashSet<IClass>();
		return validAnnotations;
	}
	
	public static boolean isPrintErrors(){
		return PRINT_RECORD_LEVEL_STATS;
	}
	public static boolean isStrict(){
		return STRICT_VALUE_CALCULATION;
	}
	
	/**
	 * calculate confusion for two composition on a given annotation type
	 * @param gold - gold document instance
	 * @param system - system document instance
	 * @param prop - property for a type of annotations to fetch
	 * @param confusion - total confusion matrix
	 */
	private void calculateDocumentConfusion(IInstance gold, IInstance system, String prop, Analysis.ConfusionMatrix confusion){
		// get a list of gold variables and system vars for each document
		String docTitle = getDocumentTitle(gold);
		List<IInstance> goldVariables = getAnnotationVariables(gold,gold.getOntology().getProperty(prop));
		List<IInstance> systemVariables = getAnnotationVariables(system,system.getOntology().getProperty(prop));
		Set<IInstance> usedSystemCandidates = new HashSet<IInstance>();
		
		for(IInstance goldInst: goldVariables){
			Analysis.ConfusionMatrix varConfusion = getConfusionMatrix(goldInst);
			List<IInstance> sysInstances = getMatchingAnnotationVaiables(systemVariables,goldInst);
			if(sysInstances.isEmpty()){
				confusion.FN ++;
				varConfusion.FN++;
				getAnalysis().addError(confusion.getLabelFN(),docTitle,goldInst);
				getAnalysis().addError(varConfusion.getLabelFN(),docTitle,goldInst);
			}else{
				for(IInstance sysInst : sysInstances ){
					usedSystemCandidates.add(sysInst);
					double score = getWeightedScore(goldInst,sysInst);
					confusion.TPP ++;
					confusion.TP += score;

					varConfusion.TPP ++;
					varConfusion.TP += score;

					// add errors for scores that are TP, but not good enought
					if(score < 1.0){
						getAnalysis().addError(confusion.getLabelTP(),docTitle,goldInst);
						getAnalysis().addError(varConfusion.getLabelTP(),docTitle,goldInst);
					}
					
				}
			}
		}
		for(IInstance inst: systemVariables){
			if(!usedSystemCandidates.contains(inst)){
				// there could be some annotations that we simply don't evaluate because
				// GOLD didn't bother to annotate them
				if(getAnnotationFilter().contains(inst.getDirectTypes()[0])){
					confusion.FP ++;
					getAnalysis().addError(confusion.getLabelFP(),docTitle,inst);
					getConfusionMatrix(inst).FP++;
					getAnalysis().addError(getConfusionMatrix(inst).getLabelFP(),docTitle,inst);
				}
				
			}
		}
	}

	/**
	 * get document title
	 * @param doc
	 * @return
	 */
	private String getDocumentTitle(IInstance doc) {
		IProperty title = doc.getOntology().getProperty(DomainOntology.HAS_TITLE);
		return (String) doc.getPropertyValue(title);
	}

	/**
	 * get confusion matrix for a given type of instance
	 * @param goldInst
	 * @return
	 */
	private Analysis.ConfusionMatrix getConfusionMatrix(IInstance goldInst) {
		String name = goldInst.getDirectTypes()[0].getName();
		return getAnalysis().getConfusionMatrix(name);
	}


	private List<IInstance> getMatchingAnnotationVaiables(List<IInstance> candidateVariables, IInstance goldInst) {
		List<IInstance> matchedInstances = new ArrayList<IInstance>();
		IProperty prop = null; 
		String goldSpan = getPropertyValue(goldInst.getOntology().getProperty(DomainOntology.HAS_SPAN),goldInst);
		IClass goldType = null;
		
		// set to the percent of overlap of gold 
		double overlapThreshold = 0;
		
		// go through all possible candidate variables and select the ones that are the same type (or more specific)
		// and have a span overlap above threshold
		for(IInstance inst: candidateVariables){
			if(prop == null)
				prop = inst.getOntology().getProperty(DomainOntology.HAS_SPAN);
			if(goldType == null)
				goldType = inst.getOntology().getClass(goldInst.getDirectTypes()[0].getName());
			String span = getPropertyValue(prop,inst);
			IClass type = inst.getDirectTypes()[0];
			// if candidate type is identical to gold or more specific
			if(type.equals(goldType) || type.hasSuperClass(goldType)){
				int overlap = spanOverlap(goldSpan,span);
				if(overlap > overlapThreshold){
					matchedInstances.add(inst);
				}
			}
			
		}
		return matchedInstances;
	}
	
	private String getPropertyValue(IProperty prop, IInstance inst){
		StringBuilder str = new StringBuilder();
		for(Object o: inst.getPropertyValues(prop)){
			str.append(o+" ");
		}
		return str.toString();
	}
	

	/**
	 * do spans overlap on anchor
	 * @param goldSpan
	 * @param candidateSpan
	 * @return
	 */
	private int spanOverlap(String goldSpan, String candidateSpan) {
		int overlap = 0;
		List<Span> goldSpans = parseSpans(goldSpan);
		List<Span> candidateSpans = parseSpans(candidateSpan);
		for(Span sp: goldSpans){
			for(Span csp: candidateSpans){
				// if GOLD overlaps any candidate span, BINGO, we got it for now ..
				overlap += sp.overlapLength(csp);		
			}
		}
		return overlap;
	}

	public static class Span {
		public int start,end;
		public static Span getSpan(String st, String en){
			Span sp = new Span();
			sp.start = Integer.parseInt(st);
			sp.end = Integer.parseInt(en);
			return sp;
		}
		public static Span getSpan(String span){
			String [] p = span.split(SPAN_SEPERATOR);
			if(p.length == 2){
				return getSpan(p[0],p[1]);
			}
			return null;
		}
		
		public boolean overlaps(Span s){
			if(s == null)
				return false;
			//NOT this region ends before this starts or other region ends before this one starts
			return !(end < s.start || s.end < start);
		}
		public int overlapLength(Span s){
			if(overlaps(s)){
				return Math.min(end,s.end) - Math.max(start,s.start);
			}
			return 0;
		}
	}
	
	
	/**
	 * pars spans from string
	 * @param text
	 * @return
	 */
	private List<Span> parseSpans(String text) {
		List<Span> list = new ArrayList<Span>();
		for(String span: text.split(DISJOINT_SPANS)){
			Span sp = Span.getSpan(span);
			if(sp != null){
				list.add(sp);
			}
		}
		return list;
	}

	private double getWeightedScore(IInstance goldInst, IInstance systemInst) {
		// we start with score of 1.0 cause we did match up the (anchor aprory)
		//double defaultWeight =  1.0; //getDefaultWeight(goldInst);
		double numerator   = 1.0;  // initial weight of an anchor
		double denominator = 1.0;  // initial total score 
		for(IProperty goldProp: getProperties(goldInst)){
			for(IInstance gVal: getInstanceValues(goldInst.getPropertyValues(goldProp))){
				double weight = getWeight(gVal);
				//if(weight == 0)
				//	weight = defaultWeight;
				denominator += weight;
				numerator += weight * hasAttributeValue(systemInst,goldProp,gVal);
			}
		}
		return numerator / denominator;
	}

	/*private double getDefaultWeight(IInstance inst){
		double count = 0;
		for(IProperty goldProp: getProperties(inst)) {
			for (IInstance gVal : getInstanceValues(inst.getPropertyValues(goldProp))) {
				count++;
			}
		}
		return count > 0?1.0 / count:0;
	}*/

	/**
	 * does a system instance have a given value
	 * @param systemInst - system instnce
	 * @param prop - property
	 * @param goldValue - gold value
	 * @return 1 or 0
	 */
	private int hasAttributeValue(IInstance systemInst, IProperty prop, IInstance goldValue){
		IClass goldValueClass = goldValue.getDirectTypes()[0];
		prop = systemInst.getOntology().getProperty(prop.getName());
		for(IInstance val: getInstanceValues(systemInst.getPropertyValues(prop))){
			if(goldValueClass.equals(val.getDirectTypes()[0])){
				return 1;
			}
		}
		return 0;
	}

	private double getWeight(IInstance inst){
		String name = inst.getDirectTypes()[0].getName();
		if(getAttributeWeights().containsKey(name)){
			return getAttributeWeights().get(name);
		}
		// default weight in case we don't have a good one
		//System.err.println("no weight for: "+name);
		return 1.0;
	}

	private List<IProperty> getProperties(IInstance inst){
		List<IProperty> props = new ArrayList<IProperty>();
		for(IProperty p: inst.getProperties()){
			if(p.isObjectProperty()){
				props.add(p);
			}
		}
		return props;
	}
	
	private List<IInstance> getInstanceValues(Object [] objects ){
		List<IInstance> list = new ArrayList<IInstance>();
		for(Object o: objects){
			if(o instanceof IInstance){
				list.add((IInstance)o);
			}
		}
		return list;
	}
	

	/**
	 * select matching composition
	 * @param candidateCompositions
	 * @param gold
	 * @return
	 */
	private IInstance getMatchingComposition(List<IInstance> candidateCompositions, IInstance gold) {
		IProperty prop = null; 
		String goldTitle = ""+gold.getPropertyValue(gold.getOntology().getProperty(DomainOntology.HAS_TITLE));
		for(IInstance inst: candidateCompositions){
			if(prop == null)
				prop = inst.getOntology().getProperty(DomainOntology.HAS_TITLE);
			String title = ""+inst.getPropertyValue(prop);
			if(goldTitle.equals(title))
				return inst;
		}
		return null;
	}

	/**
	 * get annotation variables of a composition instance
	 * @param composition
	 * @param prop - property
	 * @return
	 */
	private List<IInstance> getAnnotationVariables(IInstance composition, IProperty prop) {
		List<IInstance> list = new ArrayList<IInstance>();
		for(Object o: composition.getPropertyValues(prop)){
			if(o instanceof IInstance)
				list.add((IInstance)o);
		}
		return list;
	}

	
	/**
	 * get composition instances
	 * @param ont - ontology
	 * @return list of composition instances
	 */
	private List<IInstance> getCompositions(IOntology ont) {
		List<IInstance> list = new ArrayList<IInstance>();
		for(IInstance inst: ont.getClass(DomainOntology.COMPOSITION).getInstances()){
			list.add(inst);
		}
		return list;
	}

	/**
	 * get composition instances
	 * @param ont - ontology
	 * @return list of composition instances
	 */
	private IInstance getComposition(IOntology ont, String title) {
		for(IInstance inst: ont.getClass(DomainOntology.COMPOSITION).getInstances()){
			if(title.equals(inst.getPropertyValue(ont.getProperty(DomainOntology.HAS_TITLE)))){
				return inst;
			}
		}
		return null;
	}
	
	public JDialog getDialog(){
		return getDialog(null);
	}
	
	public JDialog getDialog(Frame owner){
		if(dialog == null){
			dialog = new JDialog(owner,"Annotation Evaluation",false);
			//dialog.setIconImage(new ImageIcon(LOGO_ICON).getImage());
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			GridBagConstraints c = new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,5,5,5),0,0);
			GridBagLayout l = new GridBagLayout();
			l.setConstraints(panel,c);
			panel.setLayout(l);
			
			// gold ontology instances
			goldOntology = new JTextField(30);
			JButton browse = new JButton("Browse");
			browse.addActionListener(this);
			browse.setActionCommand("g_browser");
			
			panel.add(new JLabel("Gold Instantiated Ontology"),c);c.gridx++;
			panel.add(goldOntology,c);c.gridx++;
			panel.add(browse,c);c.gridx=0;c.gridy++;
	
			goldWeights = new JTextField(30);
			browse = new JButton("Browse");
			browse.addActionListener(this);
			browse.setActionCommand("w_browser");
			
			panel.add(new JLabel("Gold Weights File"),c);c.gridx++;
			panel.add(goldWeights,c);c.gridx++;
			panel.add(browse,c);c.gridx=0;c.gridy++;


			systemOntology = new JTextField(30);
			browse = new JButton("Browse");
			browse.addActionListener(this);
			browse.setActionCommand("s_browser");
		
			panel.add(new JLabel("System Instantiated Ontology "),c);c.gridx++;
			panel.add(systemOntology,c);c.gridx++;
			panel.add(browse,c);c.gridx=0;c.gridy++;
			panel.add(Box.createRigidArea(new Dimension(10,10)),c);


			inputDocuments = new JTextField(30);
			browse = new JButton("Browse");
			browse.addActionListener(this);
			browse.setActionCommand("i_browser");

			panel.add(new JLabel("Input Documents"),c);c.gridx++;
			panel.add(inputDocuments,c);c.gridx++;
			panel.add(browse,c);c.gridx=0;c.gridy++;

			
			classFilter = new JTextField(30);
			browse = new JButton("Browse");
			browse.addActionListener(this);
			browse.setActionCommand("c_browser");

			panel.add(new JLabel("eHOST class filter"),c);c.gridx++;
			panel.add(classFilter,c);c.gridx++;
			panel.add(browse,c);c.gridx=0;c.gridy++;
			
			

			JPanel conp = new JPanel();
			conp.setLayout(new BorderLayout());
			conp.setBorder(new TitledBorder("Output Results"));
			console = new JTextArea(10,40);
			//console.setLineWrap(true);
			console.setEditable(false);
			conp.add(new JScrollPane(console),BorderLayout.CENTER);
			//c.gridwidth=3;		
			//panel.add(conp,c);c.gridy++;c.gridx=0;
			
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1,3,10,10));
			buttonPanel.setBorder(new EmptyBorder(10,30,10,30));
			
			JButton generate = new JButton("Generate Weights");
			generate.addActionListener(this);
			generate.setActionCommand("weights");

			JButton explore = new JButton("Visualize");
			explore.addActionListener(this);
			explore.setActionCommand("explore");

			JButton eHost = new JButton("eHOST");
			eHost.addActionListener(this);
			eHost.setActionCommand("ehost");
			
			JButton run = new JButton("Evaluate");
			run.addActionListener(this);
			run.setActionCommand("evaluate");
			buttonPanel.add(generate);
			buttonPanel.add(explore);
			buttonPanel.add(eHost);
			buttonPanel.add(run);
			//panel.add(buttonPanel,c);
			
			progress = new JProgressBar();
			progress.setIndeterminate(true);
			progress.setString("Please Wait. It may take a while ...");
			progress.setStringPainted(true);
			
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add(panel,BorderLayout.NORTH);
			p.add(conp,BorderLayout.CENTER);
			p.add(buttonPanel,BorderLayout.SOUTH);
			
				
			// wrap up, and display
			dialog.setContentPane(p);
			dialog.pack();
		
			//center on screen
			Dimension d = dialog.getSize();
			Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation(new Point((s.width-d.width)/2,(s.height-d.height)/2));
			
			// load prior values
			loadSettings();
			
		}
		return dialog;	
	}
	
	/**
	 * save UI settings
	 */
	private void saveSettings(){
		Properties p = new Properties();
		p.setProperty("goldOntology",goldOntology.getText());
		p.setProperty("goldWeights",goldWeights.getText());
		p.setProperty("systemOntology",systemOntology.getText());
		p.setProperty("inputDocuments",inputDocuments.getText());
		p.setProperty("classFilter",classFilter.getText());
		UITools.saveSettings(p,getClass());
	}
	
	/**
	 * save UI settings
	 */
	private void loadSettings(){
		Properties p = UITools.loadSettings(getClass());
		if(p.containsKey("goldOntology"))
			goldOntology.setText(p.getProperty("goldOntology"));
		if(p.containsKey("goldWeights"))
			goldWeights.setText(p.getProperty("goldWeights"));
		if(p.containsKey("systemOntology"))
			systemOntology.setText(p.getProperty("systemOntology"));
		if(p.containsKey("inputDocuments"))
			inputDocuments.setText(p.getProperty("inputDocuments"));
		if(p.containsKey("classFilter"))
			classFilter.setText(p.getProperty("classFilter"));
	}
	
	/**
	 * set busy .
	 *
	 * @param b the new busy
	 */
	private void setBusy(boolean b){
		final boolean busy = b;
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(busy){
					progress.setIndeterminate(true);
					progress.setString("Please Wait. It may take a while ...");
					progress.setStringPainted(true);
					getDialog().getContentPane().remove(buttonPanel);
					getDialog().getContentPane().add(progress,BorderLayout.SOUTH);
					console.setText("");
				}else{
					getDialog().getContentPane().remove(progress);
					getDialog().getContentPane().add(buttonPanel,BorderLayout.SOUTH);
				}
				getDialog().getContentPane().validate();
				getDialog().pack();
				
			}
		});
	}

	public void setSystemInstanceOntlogy(String text){
		systemOntology.setText(text);
	}
	public void setInputDocuments(String text){
		inputDocuments.setText(text);
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if("evaluate".equals(cmd)){
			doEvaluate();
		}else if("g_browser".equals(cmd)){
			doBrowse(goldOntology);
		}else if("w_browser".equals(cmd)){
			doBrowse(goldWeights);
		}else if("s_browser".equals(cmd)){
			doBrowse(systemOntology);
		}else if("i_browser".equals(cmd)){
			doBrowse(inputDocuments);
		}else if("c_browser".equals(cmd)){
			doBrowse(classFilter);
		}else if("explore".equals(cmd)){
			doExplore();
		}else if("exit".equals(cmd)){
			System.exit(0);
		}else if("weights".equals(cmd)){
			doWeights();
		}else if("ehost".equals(cmd)){
			doEHOST();
		}	
	}
	
	private void doEHOST() {
		new Thread(new Runnable() {
			public void run() {
				File gold = new File(goldOntology.getText());
				File candidate = new File(systemOntology.getText());
				File input = new File(inputDocuments.getText());
				File output = new File(candidate.getParentFile(),"eHOST");
				File filter = new File(classFilter.getText());
				
				if(!gold.exists()){
					JOptionPane.showMessageDialog(getDialog(),"Can't find gold instance ontology: "+gold,"Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!candidate.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find system instance ontology: "+candidate);
					return;
				}
				saveSettings();
				setBusy(true);
				try{
				
					// load
					InstancesToEhost i2e = new InstancesToEhost();
					i2e.setOutputDir(output);
					i2e.setCorpusDir(input);
					
					if(filter.exists()){
						i2e.setClassFilter(Arrays.asList(TextTools.getText(new FileInputStream(filter)).split("\n")));
					}
					
					progress("converting "+candidate.getAbsolutePath()+ "..\n");
					i2e.setAnnotator("A2");
					i2e.convert(OOntology.loadOntology(candidate));
					progress("converting "+gold.getAbsolutePath()+ "..\n");
					i2e.addAnnotations(OOntology.loadOntology(gold),"A1");
					progress("ok");		
							
					
				}catch(Exception ex){
					JOptionPane.showMessageDialog(getDialog(),"There was a prolbem with evaluation: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					return;
				}finally {
					setBusy(false);
				}
			}
		}).start();
		
	}

	private void doWeights() {
		new Thread(new Runnable() {
			public void run() {
				File gold = new File(goldOntology.getText());
				File weights = new File(goldWeights.getText());
				if(!gold.exists()){
					JOptionPane.showMessageDialog(getDialog(),"Can't find gold instance ontology: "+gold,"Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!weights.getParentFile().canWrite()){
					JOptionPane.showMessageDialog(getDialog(),"Can't save gold weights file to "+weights.getParentFile(),"Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				saveSettings();
				setBusy(true);
				try{
				
					Map<String,Double> weightMap = computeWeights(gold);
					writeWeights(weightMap, weights);
					final String text = getWeightsAsText(weightMap);
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							console.setText(text);
							console.repaint();
						}
					});
					
				}catch(Exception ex){
					JOptionPane.showMessageDialog(getDialog(),"There was a prolbem with evaluation: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					return;
				}finally {
					setBusy(false);
				}
			}
		}).start();
		
	}

	private void doExplore() {
		new Thread(new Runnable() {
			public void run() {
				File gold = new File(goldOntology.getText());
				File system = new File(systemOntology.getText());
				File input = new File(inputDocuments.getText());

				if(!gold.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find gold instance ontology: "+gold);
					return;
				}
				if(!system.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find system instance ontology: "+system);
					return;
				}
				if(!input.exists()){
					UITools.showErrorDialog(getDialog(),"Can't input document directory "+input);
					return;
				}
				saveSettings();
				setBusy(true);
				try {
					outputAnnotationsAsHTML(system.getParentFile(), input, gold, system);
					UITools.browseURLInSystemBrowser(new File(system.getParentFile().getAbsolutePath()+File.separator+EVALUATION_HTML).toURI().toString());

				}catch(Exception ex){
					UITools.showErrorDialog(getDialog(),"Problem saving annotations as HTML",ex);
				}finally {
					setBusy(false);

				}
			}
		}).start();

	}


	private void doEvaluate() {
		new Thread(new Runnable() {
			public void run() {
				File gold = new File(goldOntology.getText());
				File weights = new File(goldWeights.getText());
				File candidate = new File(systemOntology.getText());
				if(!gold.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find gold instance ontology: "+gold);
					return;
				}
				if(!candidate.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find system instance ontology: "+candidate);
					return;
				}
				if(goldWeights.getText().length() > 0 && !weights.exists()){
					UITools.showErrorDialog(getDialog(),"Can't find gold weights file: "+weights);
					return;
				}
				

				// save settings
				saveSettings();
				
				setBusy(true);
				try{
					if(weights.exists())
						loadWeights(weights);
					evaluate(gold,candidate);
					outputHTML(candidate.getParentFile());
					outputTSV(candidate.getParentFile());
					
					// output result
					final String text = getAnalysis().getResultTableAsText();
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							console.setText(text);
							console.repaint();
						}
					});
					
					// open in browser
					try{
						UITools.browseURLInSystemBrowser(new File(candidate.getParentFile().getAbsolutePath()+File.separator+ANALYSIS_HTML).toURI().toString());
					}catch(Exception ex){
						UITools.showErrorDialog(getDialog(),ex);
					}
						
				}catch(Exception ex){
					UITools.showErrorDialog(getDialog(),"There was a prolbem with evaluation: ",ex);
					return;
				}finally {
					setBusy(false);
				}
			}

		
		}).start();
	}

	/**
	 * Do browse.
	 *
	 * @param text the text
	 */
	private void doBrowse(JTextField text){
		File file = text.getText().length() > 0? new File(text.getText()):lastFile;
		JFileChooser fc = new JFileChooser(file);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileFilter() {
			public String getDescription() {
				return "Text files (.txt)";
			}
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".txt");
			}
		});
	
		int r = fc.showOpenDialog(getDialog());
		if(r == JFileChooser.APPROVE_OPTION){
			file = fc.getSelectedFile();
			text.setText(file.getAbsolutePath());
			lastFile = file;
		}
	}

	/**
	 * print weights matrix
	 * @param computeWeights - weight matrix
	 * @param out - print stream
	 */
	public void printWeights(Map<String,Double> computeWeights, PrintStream out) {
		for(String key: computeWeights.keySet()){
			out.println(key+"\t"+TextTools.toString(computeWeights.get(key)));
		}
	}

	/**
	 * get weights as string
	 * @param computeWeights - weight matrix
	 * @return String representation of them
	 */
	public String getWeightsAsText(Map<String,Double> computeWeights){
		StringBuilder str = new StringBuilder();
		for(String key: computeWeights.keySet()){
			str.append(key+"\t"+TextTools.toString(computeWeights.get(key))+"\n");
		}
		return str.toString();
	}

	/**
	 * write weights to a file
	 * @param computeWeights - matrix of weights
	 * @param outFile - output file
	 * @throws IOException exception to be thrown
	 */
	public void writeWeights(Map<String,Double> computeWeights,File outFile) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		writer.write(getWeightsAsText(computeWeights));
		writer.close();
	}


	/**
	 * compute weights matrix
	 * @param gold - gold instances file
	 * @return map of weights
	 * @throws IOntologyException - in case we mess up
	 */
	public Map<String,Double> computeWeights(File gold) throws IOntologyException {
		Map<String,Double> weights = new LinkedHashMap<String, Double>();
		Map<String,Map<String,Double>> counts = new LinkedHashMap<String, Map<String,Double>>();
		IOntology ont = OOntology.loadOntology(gold);
		for(IClass cls: ont.getClass("Annotation").getSubClasses()){
			for(IInstance inst: cls.getDirectInstances()){
				for(IProperty prop : inst.getProperties()){
					if(prop.isObjectProperty()){
						countModifier(inst,prop,counts);
					}
				}
			}
		}
		// now compute weights
		for(String prop: counts.keySet()){
			Map<String,Double> map = counts.get(prop);
			double total = 0;
			// compute total
			for(String modifier: map.keySet()){
				total +=  map.get(modifier);
			}
			// compute weights
			for(String modifier: map.keySet()){
				double count =  map.get(modifier);
				double weight = 1-(count/total);
				weights.put(modifier,weight);
			}
		}

		return weights;

	}

	/**
	 * count modifiers for a given annotation instance on a given property
	 * @param inst - annotation instance
	 * @param prop - modifier property
	 * @param counts - counts map
	 */
	private void countModifier(IInstance inst, IProperty prop, Map<String, Map<String, Double>> counts) {
		Map<String,Double> map = counts.get(prop.getName());
		if(map == null){
			map = new HashMap<String, Double>();
			counts.put(prop.getName(),map);
		}
		for(Object obj: inst.getPropertyValues(prop)){
			if(obj instanceof IInstance){
				IClass modifierCls = ((IInstance) obj).getDirectTypes()[0];
				Double num = map.get(modifierCls.getName());
				if(num == null)
					num = new Double(0);
				map.put(modifierCls.getName(),num.doubleValue()+1);
			}
		}
	}

	/**
	 * output gold and system annotations as HTML files
	 * @param outputDir - HTML directory
	 * @param inputDir - input text files
	 * @param goldOntology - instantiated gold file
	 * @param systemOntology - instantiated system file
	 * @throws Exception  - in case we mess up
	 */
	public void outputAnnotationsAsHTML(File outputDir, File inputDir, File goldOntology, File systemOntology) throws Exception {
		HTMLExporter exporter = new HTMLExporter(outputDir);
		progress("loading gold instances ..\n");
		IOntology gold = OOntology.loadOntology(goldOntology);
		progress("loading system instances ..\n");
		IOntology system = OOntology.loadOntology(systemOntology);
		List<File> files = FileTools.getFilesInDirectory(inputDir,".txt");

		for(File file: files){
			IInstance g = getComposition(gold,file.getName());
			IInstance s = getComposition(system,file.getName());
			progress("processing "+file.getName()+" ..\n");
			exporter.export(file,g,s);
		}
		exporter.flush();
	}
	
	/**
	 * Progress.
	 *
	 * @param str the str
	 */
	private void progress(String str){
		System.out.print(str);
		if(console != null){
			final String s = str;
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					console.append(s);
				}
			});
			
		}
	}
}
