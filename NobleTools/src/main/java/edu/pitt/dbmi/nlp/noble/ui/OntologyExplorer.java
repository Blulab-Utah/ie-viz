package edu.pitt.dbmi.nlp.noble.ui;


import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.ui.widgets.ResourceCellRenderer;
import edu.pitt.dbmi.nlp.noble.util.PathHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.*;
import java.util.List;

import static edu.pitt.dbmi.nlp.noble.ontology.OntologyUtils.getRootPaths;
import static edu.pitt.dbmi.nlp.noble.ontology.OntologyUtils.toHTML;

/**
 * The Class OntologyExplorer.
 */
public class OntologyExplorer extends JPanel implements HyperlinkListener, ActionListener {
	public static final String VALUE_SELECTED_EVENT = "VALUE_SELECTED_EVENT";
	private final URL ONTOLOGY_ICON = getClass().getResource("/icons/Ontology.gif");
	private final URL TERMINOLOGY_ICON = getClass().getResource("/icons/Terminology.gif");
	private final URL CLASS_ICON = getClass().getResource("/icons/Class.gif");
	private final URL INSTANCE_ICON = getClass().getResource("/icons/Instance.gif");
	private final int SEARCH_CHAR_LIMIT = 3;
	
	//private Icon ontologyIcon = new ImageIcon(ONTOLOGY_ICON);
	//private Icon terminologyIcon = new ImageIcon(TERMINOLOGY_ICON);
	private Icon classIcon = new ImageIcon(CLASS_ICON);
	private Icon instanceIcon = new ImageIcon(INSTANCE_ICON);	
	
	
	private final int panelNumber = 3;
	private IOntology ontology;
	private JPanel panel;
	private JScrollPane scroll,infoScroll;
	private ExplorerPanel root,currentPanel; //root panel
	private JEditorPane info;
	private SearchComboBox search;
	private OntologyExplorer explorer;
	private Object selected;
	private JProgressBar progress;
	private JLabel status;
	private Object currentConcept = null;
	private JPopupMenu popup;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private Terminology terminology;
	private PathHelper pathHelper;
	
	/**
	 * Instantiates a new ontology explorer.
	 */
	public OntologyExplorer(){
		super();
		explorer = this;
		setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));	
		//panel.setLayout(new FlowLayout());	
		panel.setBorder(new TitledBorder("Browse"));
		
		search = new SearchComboBox();
		search.setEditable(true);
		search.setBorder(new TitledBorder("Search"));
		search.setActionCommand("search");
		search.addActionListener(this);
		search.setRenderer(new ResourceCellRenderer());
		add(search,BorderLayout.NORTH);
		
		// add explorer panels
		ExplorerPanel parent = null;
		for(int i=0;i<panelNumber;i++){
			ExplorerPanel ep = new ExplorerPanel();
			if(parent != null){
				parent.setNext(ep);	
				ep.setPrev(parent);
			}else{
				root = ep;	
			}
			parent = ep;
			panel.add(ep);
		}
		
		
		
		scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(800,300));
		//add(scroll,BorderLayout.CENTER);
		
		// info panel
		//JPanel p = new JPanel();   
		//Border border = new SoftBevelBorder(SoftBevelBorder.LOWERED);;
		//Border margin = new EmptyBorder(10,10,10,10);
		//p.setBorder(new CompoundBorder(margin, border));
		info = new JEditorPane();
		info.setContentType("text/html");
		info.setPreferredSize(new Dimension(800,300));
		info.setEditable(false);
		info.addHyperlinkListener(this);
		infoScroll = new JScrollPane(info);
		infoScroll.setBorder(new TitledBorder("Preview"));
		//p.add(infoScroll);
		//add(p,BorderLayout.SOUTH);
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scroll,infoScroll);
		split.setResizeWeight(0.5);
		add(split,BorderLayout.CENTER);
		// set root
		//Concept root = (Concept) Util.sendObject(new Parcel("lookupRoot",Util.getUsername(),null,null));
		//if(root != null)
		//	setRoot(root);
		status = new JLabel(" ");
		progress = new JProgressBar();
		progress.setString("Please Wait ...");
		progress.setStringPainted(true);
		progress.setIndeterminate(true);
		add(status,BorderLayout.SOUTH);
	}
	
	/**
	 * add listener.
	 *
	 * @param l the l
	 */
	public void addPropertyChangeListener(PropertyChangeListener l){
		pcs.addPropertyChangeListener(l);
	}
	
	/**
	 * remove listener.
	 *
	 * @param l the l
	 */
	public void removePropertyChangeListener(PropertyChangeListener l){
		pcs.removePropertyChangeListener(l);
	}
	
	/**
	 * create popup menu.
	 *
	 * @return the j popup menu
	 */
	private JPopupMenu createPopupMenu(){
		JPopupMenu menu = new JPopupMenu();
		//JMenuItem create
		return menu;
	}
	
	/**
	 * get popup menu.
	 *
	 * @return the popup menu
	 */
	public JPopupMenu getPopupMenu(){
		if(popup == null)
			popup = createPopupMenu();
		return popup;
	}
	
	
	/**
	 * get HTML info string for a concept.
	 *
	 * @param c the c
	 * @return the HTML info
	 */
	public String getHTMLInfo(Object c){
		if(c instanceof Concept)
			return getHTMLInfo((Concept)c);
		else if(c instanceof IResource)
			return getHTMLInfo((IResource)c);
		return null;
	}
	
	/**
	 * get HTML info string for a concept.
	 *
	 * @param c the c
	 * @return the HTML info
	 */
	public String getHTMLInfo(Concept c){
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append("<h2><font color=green>");
		buffer.append(c+"</font></h2><hr>");
		try{
			Map map = c.getRelatedConcepts();
			buffer.append("<p>");
			for(Iterator i=map.keySet().iterator();i.hasNext();){
				Object key = i.next();
				Object val = map.get(key);
				buffer.append("<b>"+key+"</b><br>"+val+"<br><br>");
			}
			buffer.append("</p>");
		}catch(TerminologyException ex){
			ex.printStackTrace();
		}
		return buffer.toString();
	}
	
	
	/**
	 * display relations map.
	 *
	 * @param e the e
	 */
	public void hyperlinkUpdate(HyperlinkEvent e){
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
			String s = e.getDescription();
			if(s == null)
				return;
			if(s.startsWith("class:")){
				setClass(ontology.getClass(s.substring(6)));
			}else if(s.startsWith("property:")){
				setBusy(true);
				final String prop = s.substring(9);
				(new Thread(new Runnable(){
					public void run(){
						JEditorPane hinfo = new JEditorPane();
						hinfo.setContentType("text/html");
						hinfo.setEditable(false);
						hinfo.setText(getHTMLInfo(ontology.getProperty(prop)));
						//hinfo.addHyperlinkListener(this);
						JScrollPane s = new JScrollPane(hinfo);
						s.setPreferredSize(new Dimension(600,600));
						setBusy(false);
						JOptionPane.showMessageDialog(explorer,s,"",JOptionPane.PLAIN_MESSAGE);
					}
				})).start();
			}else if(s.startsWith("relations:")){
				setBusy(true);
				final String prop = s.substring("relations:".length());
				(new Thread(new Runnable(){
					public void run(){
						JEditorPane hinfo = new JEditorPane();
						hinfo.setContentType("text/html");
						hinfo.setEditable(false);
						try {
							hinfo.setText(getAncestryInfo(prop));
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
						//hinfo.addHyperlinkListener(this);
						JScrollPane s = new JScrollPane(hinfo);
						s.setPreferredSize(new Dimension(600,600));
						setBusy(false);
						JOptionPane.showMessageDialog(explorer,s,"",JOptionPane.PLAIN_MESSAGE);
					}

				})).start();
			}
			//if(currentConcept != null){
				//setBusy(true);
				/*
				(new Thread(new Runnable(){
					public void run(){
						JEditorPane hinfo = new JEditorPane();
						hinfo.setContentType("text/html");
						hinfo.setEditable(false);
						hinfo.setText(getHTMLInfo(currentConcept));
						//hinfo.addHyperlinkListener(this);
						JScrollPane s = new JScrollPane(hinfo);
						s.setPreferredSize(new Dimension(600,600));
						setBusy(false);
						JOptionPane.showMessageDialog(explorer,s,"",JOptionPane.PLAIN_MESSAGE);
					}
				})).start();
			}*/
		}
	}
	

	/**
	 * Gets the ancestry info.
	 *
	 * @param code the code
	 * @return the ancestry info
	 * @throws TerminologyException the terminology exception
	 */
	private String getAncestryInfo(String code) throws TerminologyException {
		if(terminology == null)
			return "";
		Concept c = terminology.lookupConcept(code);
		if(c != null){
			StringBuffer str = new StringBuffer();
			// narrower concepts
			str.append("<h3>Direct Children of <font color=green>"+c.getName()+" ("+c.getCode()+")</font></h3>");
			str.append("<ul>");
			for(Concept a: c.getChildrenConcepts()){
				String h = "<a href=\""+a.getCode()+"\">"+a.getCode()+"</a>";
				str.append("<li><b>"+a.getName()+"</b> ("+h+") "+Arrays.toString(a.getSemanticTypes())+"</li>");
			}
			str.append("</ul>");
			str.append("<h3>Direct Parents of <font color=green>"+c.getName()+" ("+c.getCode()+")</font></h3>");
			for(Concept a: c.getParentConcepts()){
				String h = "<a href=\""+a.getCode()+"\">"+a.getCode()+"</a>";
				str.append("<li><b>"+a.getName()+"</b> ("+h+") "+Arrays.toString(a.getSemanticTypes())+"</li>");
			}
			str.append("</ul>");
			// ancestry
			str.append("<h3>All Ancestors of <font color=green>"+c.getName()+" ("+c.getCode()+") </font></h3>");
			if(getPathHelper() != null){
				Map<Concept,Integer> ancestors = getPathHelper().getAncestors(c);
				if(ancestors != null && !ancestors.isEmpty()){
					for(Concept a: ancestors.keySet()){
						try {
							a.initialize();
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
						String h = "<a href=\""+a.getCode()+"\">"+a.getCode()+"</a>";
						str.append("<li>{"+ancestors.get(a)+"} ... <b>"+a.getName()+"</b> ("+h+") "+
									Arrays.toString(a.getSemanticTypes())+"</li>");
					}
				}
			}
			// other relationshipts
			for(Relation r: c.getRelatedConcepts().keySet()){
				if(Relation.BROADER.equals(r) || Relation.NARROWER.equals(r))
					continue;
				str.append("<h3>"+r.getName()+" of <font color=green>"+c.getName()+" ("+c.getCode()+") </font></h3>");
				str.append("<ul>");
				for(Concept a: c.getRelatedConcepts(r)){
					String h = "<a href=\""+a.getCode()+"\">"+a.getCode()+"</a>";
					str.append("<li><b>"+a.getName()+"</b> ("+h+") "+Arrays.toString(a.getSemanticTypes())+"</li>");
				}
				str.append("</ul>");
			}
			
			str.append("</ul>");
			return str.toString();
		}
		return "";
	}

	/**
	 * Gets the path helper.
	 *
	 * @return the path helper
	 */
	private PathHelper getPathHelper() {
		if(pathHelper == null && terminology != null){
			pathHelper = new PathHelper(terminology);
		}
		return pathHelper;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if("search".equals(cmd)){
			Object o = search.getSelectedItem();
			if(o != null){
				if(o instanceof IClass){
					setClass((IClass) o);
				}else if(o instanceof IInstance){
					setInstance((IInstance) o);
				}else if(o instanceof Concept){
					setConcept((Concept)o);
				}
			}
		}
	}
	
	
	
	/**
	 * display specific class (path to root).
	 *
	 * @param c the new class
	 */
	public void setClass(IClass c){
		// get path
		List<ClassPath> paths = OntologyUtils.getRootPaths(c);
		// set first path
		if(!paths.isEmpty()){
			/*ExplorerPanel panel = root;
			for(IClass p : paths.get(0)){
				panel.setSelectedValue(p);
				panel = panel.getNext();
			}*/
			// pick shortest path
			ClassPath shortest = paths.get(0);
			for(ClassPath l: paths){
				if(l.size() < shortest.size())
					shortest = l;
			}
			final ClassPath list = shortest;
			new Thread( new Runnable(){
				public void run(){
					ExplorerPanel panel = root;
					for(IClass p : list){
						panel.setSelectedValue(p);
						// we just set threads in motion to select, lets wait for the 
						// next panel to load if we have kids
						boolean hasKids = p.getDirectSubClasses().length > 0;
						while(hasKids && (panel.getNext() == null || panel.getNext().isLoading())){
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
						}
						panel = panel.getNext();
					}
					// highlight concept at the end
					//if(panel != null)
					//	panel.setSelectedValue(cc);
				}
			}).start();
			
		}
	}
	
	/**
	 * display specific instance (path to root).
	 *
	 * @param i the new instance
	 */
	public void setInstance(IInstance i){
		IClass c = i.getDirectTypes()[0];
		setClass(c);
		//panel.setSelectedValue(i);
	}
	
	/**
	 * Sets the concept.
	 *
	 * @param c the new concept
	 */
	public void setConcept(Concept c){
		final Concept cc = c;
		new Thread( new Runnable(){
			public void run(){
				setBusy(true);
				// get path
				List<Concept> roots = null;
				try {
					roots = Arrays.asList(cc.getTerminology().getRootConcepts());
				} catch (TerminologyException e2) {
					e2.printStackTrace();
				}
				
				List<ConceptPath> paths = getPathHelper().getPaths(cc);
				List<ConceptPath> fpaths = new ArrayList<ConceptPath>();
				// filter paths that are not path to root
				for(ConceptPath p: paths){
					if(roots.contains(p.get(0))){
						fpaths.add(p);
					}
				}
				
				
				// set first path
				if(!fpaths.isEmpty()){
					// pick the shortest path
					List<Concept> shortest = fpaths.get(0);
					for(List<Concept> l: fpaths){
						if(l.size() < shortest.size())
							shortest = l;
					}
					List<Concept> list = shortest;
					//Collections.reverse(list);
					
					ExplorerPanel panel = root;
					for(Concept p : list){
						try {
							p.initialize();
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
						panel.setSelectedValue(p);
						// we just set threads in motion to select, lets wait for the 
						// next panel to load if we have kids
						Set ch = p.getRelationMap().get(Relation.NARROWER.getName());
						while(ch != null && !ch.isEmpty() && (panel.getNext() == null || panel.getNext().isLoading())){
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
						}
						panel = panel.getNext();
					}
					// highlight concept at the end
					if(panel != null)
						panel.setSelectedValue(cc);
				}else{
					JOptionPane.showMessageDialog(OntologyExplorer.this,"<html>Concept <font color=green>"+cc.getName()+"</font> has NO path to root available");
				}
				setBusy(false);
			}
		}).start();
			
		
	}
	
	/**
	 * display busy.
	 *
	 * @param busy the new busy
	 */
	public void setBusy(boolean busy){
		if(busy){
			remove(status);
			add(progress,BorderLayout.SOUTH);
		}else{
			remove(progress);
			add(status,BorderLayout.SOUTH);
		}
		validate();
		repaint();
	}
	
	/**
	 * Return selected Concept.
	 *
	 * @return Concept
	 */
	 public Object getSelectedEntry(){
		 return selected;
	 }
	 
	 /**
	  * get selecte entries
	  * @return list of selected entries
	  */
	 public List getSelectedEntries(){
		 if(currentPanel != null){
			 return currentPanel.getSelectedValues();
		 }
		 return Collections.EMPTY_LIST;
	 }
	 
	 
	/**
	 * set selected Concept.
	 *
	 * @param e the new selected entry
	 */
	 private void setSelectedEntry(Object e){
		 selected = e;
	 }
	
	/**
	 * Set root element for the explorer.
	 *
	 * @param e the new root
	 */	
	public void setRoot(Concept e){
		//explorer.setBusy(true);
		root.setRoot(e);	
		setTerminology(e.getTerminology());
		//explorer.setBusy(false);
	}
	
	/**
	 * Sets the terminology.
	 *
	 * @param term the new terminology
	 */
	public void setTerminology(Terminology term){
		terminology = term;
		search.setTerminology(term);
	}
	
	/**
	 * Set root element for the explorer.
	 *
	 * @param e the new root
	 */	
	public void setRoot(Concept [] e){
		//explorer.setBusy(true);
		root.setRoot(e);	
		if(e.length > 0){
			setTerminology(e[0].getTerminology());
		}
		//explorer.setBusy(false);
	}
	
	/**
	 * Set root element for the explorer.
	 *
	 * @param e the new root
	 */	
	public void setRoot(IResource e){
		//explorer.setBusy(true);
		root.setRoot(e);
		search.setOntology(e.getOntology());
		//explorer.setBusy(false);
		ontology = e.getOntology();
	}
	
	/**
	 * Set root element for the explorer.
	 *
	 * @param e the new root
	 */	
	public void setRoot(IResource [] e){
		//explorer.setBusy(true);
		root.setRoot(e);
		if(e.length > 0){
			search.setOntology(e[0].getOntology());
			ontology = e[0].getOntology();
		}
		//explorer.setBusy(false);
	}
	
	
	/**
	 * show lexical entry in info panel.
	 *
	 * @param e the e
	 */ 
	private void show(Object e){
		if(e instanceof Concept){
			show((Concept) e);
		}else if(e instanceof IResource){
			show((IResource) e);
		}else{
			info.setText("<html>"+e);
			currentConcept = e;
		}
			
	}
	
	
	/**
	 * show lexical entry in info panel.
	 *
	 * @param ee the ee
	 */ 
	private void show(Concept ee){
		final Concept e = ee;
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				StringBuffer buffer = new StringBuffer();
				if(e != null){
					buffer.append("<b>"+e.getName()+"</b> &nbsp;&nbsp;");
					buffer.append("(<i>"+e.getCode()+"</i>)<br>");
					buffer.append(""+Arrays.asList(e.getSemanticTypes()));
					buffer.append("<hr>");
					buffer.append("<b>Definition: </b> "+e.getDefinition()+"<br>");
					buffer.append("<b>Synonyms: </b> "+Arrays.asList(e.getSynonyms())+"<br>");
					buffer.append("<b>Sources: </b> "+Arrays.asList(e.getSources())+"<br>");
					buffer.append("<b>Codes: </b> "+e.getCodes()+"<br>");
					buffer.append("<b>Relations: </b> [<a href=\"relations:"+e.getCode()+"\">view related concepts</a>]<br>");
					buffer.append("<b>Properties: </b> "+e.getProperties()+"<br>");
				}
				info.setText("<html>"+buffer.toString());
				currentConcept = e;
				//infoScroll.getViewport().setViewPosition(new Point(0,0));
			}
			
		});
		
	}
	
	
	/**
	 * get info for resource.
	 *
	 * @param e the e
	 * @return the HTML info
	 */
	private String getHTMLInfo(IResource e){
		StringBuffer buffer = new StringBuffer("<html>");
		
		buffer.append("<b>"+e.getName()+"</b><br>");
		buffer.append("<a href=\"\">"+e.getURI()+"</a><br>");
		buffer.append("<hr><table border=0 cellpadding=5>");
		if(e.getLabels().length > 0)
			buffer.append("<tr valign=top><td><b>Labels:</b></td><td>"+toHTML(e.getLabels())+"</td></tr>");
		if(e.getComments().length > 0)
			buffer.append("<tr valign=top><td><b>Comments: </b></td><td> "+toHTML(e.getComments())+"</td></tr>");
		if(e instanceof IClass){
			IClass cls = (IClass) e;
			if(cls.getDirectSuperClasses().length > 0)
				buffer.append("<tr valign=top><td><b>Super Classes: </b></td><td>"+
					toHTML(cls.getDirectSuperClasses())+"</td></tr>");
			if(cls.getEquivalentClasses() != null && cls.getEquivalentClasses().length > 0)
				buffer.append("<tr valign=top><td><b>Equivalent Classes: </b></td><td>"+
						toHTML(cls.getEquivalentClasses())+"</td></tr>");
			if(cls.getDisjointClasses() != null && cls.getDisjointClasses().length > 0)
				buffer.append("<tr valign=top><td><b>Disjoint Classes: </b></td><td>"+
						toHTML(cls.getDisjointClasses())+"</td></tr>");
			ILogicExpression exp = cls.getEquivalentRestrictions();
			if(exp != null && !exp.isEmpty() )
				buffer.append("<tr valign=top><td><b>Equivalent Restrictions: </b></td><td>"+
						toHTML(exp)+"</td></tr>");
			exp = cls.getDirectNecessaryRestrictions();
			if(exp != null && !exp.isEmpty() )
				buffer.append("<tr valign=top><td><b>Necessary Restrictions: </b></td><td>"+
						toHTML(exp)+"</td></tr>");
			// do paths to root
			List<ClassPath> paths = getRootPaths(cls);
			if(!paths.isEmpty() ){
				buffer.append("<tr valign=top><td><b>Path(s) to Root: </b></td><td>");
				for(List<IClass> p: paths){
					buffer.append(toHTML(p)+"<br>");
				}
				buffer.append("</td></tr>");
			}
			
		}else if(e instanceof IProperty){
			IProperty cls = (IProperty) e;
			buffer.append("<tr valign=top><td><b>Domain: </b></td><td>"+
					toHTML(cls.getDomain())+"</td></tr>");
			buffer.append("<tr valign=top><td><b>Range: </b></td><td>"+
					toHTML(cls.getRange())+"</td></tr>");
		}else if(e instanceof IInstance){
			IInstance cls = (IInstance) e;
			buffer.append("<tr valign=top><td><b>Types: </b></td><td>"+
					toHTML(cls.getDirectTypes())+"</td></tr>");
		}
		
		IProperty[] p = e.getProperties();
		if(p.length > 0){
			buffer.append("<tr valign=top><td><b>Properties:</b>");
			buffer.append("</td><td>");
			for(int i=0;i<p.length;i++){
				buffer.append(p[i]+" = "+toHTML(e.getPropertyValues(p[i]))+"<br>");
			}
			buffer.append("</td></tr>");
		}
		buffer.append("</table>");
		return buffer.toString();
	}
	
	
	/**
	 * show lexical entry in info panel.
	 *
	 * @param e the e
	 */ 
	private void show(IResource e){
		info.setText((e != null)?getHTMLInfo(e):"");
		currentConcept = e;
		//infoScroll.getViewport().setViewPosition(new Point(0,0));
	}
	
	
	/**
	 * Adds the panel.
	 *
	 * @param p the p
	 */
	private void addPanel(JPanel p){
		panel.add(p);
		panel.validate();
		panel.getParent().validate();
	}
	
	
	/**
	 * This is represents one panel.
	 */	
	private class ExplorerPanel extends JPanel implements ListSelectionListener, ActionListener  {
		private ExplorerPanel next,prev;
		private JLabel label;
		private JList  list;
		private JPopupMenu popup;
		private ChildLoader currentChildLoader;
		private boolean loading = true;
		
		
		/**
		 * Instantiates a new explorer panel.
		 */
		public ExplorerPanel(){
			setLayout(new BorderLayout());
			label = new JLabel(" ");
			label.setPreferredSize(new Dimension(266,20));
			//label.setBorder(new EmptyBorder(10,0,15,0));
			list = new JList();
			list.setFixedCellWidth(150);
			list.setVisibleRowCount(10);
			list.addListSelectionListener(this);
			list.setCellRenderer(new ResourceCellRenderer());
			list.addMouseListener(new ExplorerMouseAdapter());
			popup = createPopup();
			// add
			add(label,BorderLayout.NORTH);
			add(new JScrollPane(list));
		}
		
		/**
		 * Sets the loading.
		 *
		 * @param b the new loading
		 */
		public void setLoading(boolean b){
			loading = b;
		}
		
		/**
		 * Checks if is loading.
		 *
		 * @return true, if is loading
		 */
		public boolean isLoading(){
			return loading;
		}
		
		/**
		 * get selected values
		 * @return
		 */
		public List getSelectedValues(){
			return list.getSelectedValuesList();
		}
		
		
		/**
		 * dispose of resources.
		 */
		public void dispose(){
			list.removeListSelectionListener(this);
			for(Component c: getPopupMenu().getComponents()){
				if(c instanceof AbstractButton)
					((AbstractButton)c).removeActionListener(this);
			}
			prev = next = null;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e){
			String cmd = e.getActionCommand();
			if(cmd.equals("delete")){
				Object obj = list.getSelectedValue();
				if(obj != null && obj instanceof IResource){
					IResource r = (IResource) obj;
					int k = JOptionPane.showConfirmDialog(list,"Are you sure you want to delete "+r);
					if(k == JOptionPane.YES_OPTION){
						r.delete();
					}
				}
			}
		}
		
		/**
		 * Creates the popup.
		 *
		 * @return the j popup menu
		 */
		private JPopupMenu createPopup(){
			JPopupMenu popup = new JPopupMenu();
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("delete");
			del.addActionListener(this);
			popup.add(del);
			return popup;
		}
		
		/**
		 * mouse adapter.
		 *
		 * @author tseytlin
		 */
		private class ExplorerMouseAdapter extends MouseAdapter {
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1){
					Rectangle r = list.getCellBounds(0,list.getModel().getSize()-1);
					if(r != null && r.contains(e.getPoint())){
						int i = list.locationToIndex(e.getPoint());
						Object obj = list.getModel().getElementAt(i);
						pcs.firePropertyChange(VALUE_SELECTED_EVENT,null,obj);
					}
				}
			}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
			 */
			public void mousePressed(MouseEvent e){
				if(e.isPopupTrigger()){
					popup.show(list,e.getX(),e.getY());
				}
			}
		}
		
		/**
		 * Sets the next.
		 *
		 * @param p the new next
		 */
		// set next panel
		public void setNext(ExplorerPanel p){
			next = p;	
		}
		
		/**
		 * Sets the prev.
		 *
		 * @param p the new prev
		 */
		// set next panel
		public void setPrev(ExplorerPanel p){
			prev = p;	
		}
		
		/**
		 * Clear.
		 */
		// recursively clear list
		public void clear(){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					list.setListData(new Object [0]);
					label.setText(" ");
					label.setIcon(null);
					label.setToolTipText(null);
				}
			});
			if(next != null)
				next.clear();
		}
		
		// get children from server
		/*
		private void loadChildren(Concept e){
			// make parameters
			Properties props = new Properties();
			props.setProperty("cui",e.getCode());
		
			// do lookup
			List children = (List) Util.sendObject(new Parcel("lookupChildren",Util.getUsername(),props,null));
			if(children != null)
				e.setChildren(children);
			e.setChildrenLoaded(true);
			
		}
		*/
		
		/**
		 * Sets the root.
		 *
		 * @param o the new root
		 */
		public void setRoot(Object o){
			if(o instanceof Concept)
				setRoot((Concept)o);
			else if(o instanceof IResource)
				setRoot((IResource)o);
			//setBusy(false);
		}
		
				
		/**
		 * Sets the root.
		 *
		 * @param e the new root
		 */
		// set root for this panel
		public void setRoot(Concept e){
			if(e != null){	
				// add items
				String num = "";
				try{
					Concept [] clist = e.getChildrenConcepts();
					num = " ("+clist.length+")";
					setRoot(clist);
				}catch(TerminologyException ex){
					ex.printStackTrace();
				}
				// set label
				final String nm = e.getName()+num;
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						label.setText(nm);
						label.setIcon(classIcon);
						label.setToolTipText(nm);
					}
				});
				
			}
		}
		
		/**
		 * Sets the root.
		 *
		 * @param clist the new root
		 */
		// set root for this panel
		public void setRoot(Concept [] clist){
			if(clist != null){	
				//label.setText(" ");
				Arrays.sort(clist,new Comparator<Concept>(){
					public int compare(Concept o1, Concept o2){
						if(o1 != null && o2 != null)
							return o1.getName().compareToIgnoreCase(o2.getName());
						return 0;
					}
				});
				//list.setListData(clist);
				final Concept [] rlist = clist;
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						list.setListData(rlist);
						setLoading(false);
					}
				});
			}
		}
		
		/**
		 * Sets the root.
		 *
		 * @param e the new root
		 */
		//		 set root for this panel
		public void setRoot(IResource e){
			if(e != null){	
				// add items
				if(e instanceof IClass){
					IClass [] clist = ((IClass)e).getDirectSubClasses();
					IInstance [] ilist = ((IClass)e).getDirectInstances();
					IResource [] list = new IResource [clist.length+ilist.length];
					for(int i=0;i<list.length;i++)
						list[i] = (i < clist.length)?clist[i]:ilist[i-clist.length];
					setRoot(list);
					label.setIcon(classIcon);
				}else if(e instanceof IInstance)
					label.setIcon(instanceIcon);
				
				// set label
				label.setText(e.getName());
			}
		}
		
		/**
		 * Sets the root.
		 *
		 * @param clist the new root
		 */
		// set root for this panel
		public void setRoot(IResource [] clist){
			if(clist != null){	
				label.setText(" ");
				Arrays.sort(clist,new Comparator<IResource>(){
					public int compare(IResource o1, IResource o2){
						if(o1 instanceof IClass && o2 instanceof IInstance)
							return -1;
						else if(o1 instanceof IInstance && o2 instanceof IClass)
							return 1;
						return (""+o1).compareToIgnoreCase(""+o2);
					}
				});
				
				final IResource [] rlist = clist;
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						list.setListData(rlist);
						setLoading(false);
					}
				});
				
			}
		}
		
		
		
		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		// value changes		
		public void valueChanged(ListSelectionEvent e){
			//System.out.println(e.getValueIsAdjusting()+" "+list.getSelectedValue());
			if(!e.getValueIsAdjusting()){
				loadChildren();
				OntologyExplorer.this.currentPanel = this;
			}
		}
		
		/**
		 * Load children.
		 */
		private void loadChildren(){
			Object entry = list.getSelectedValue();
			if(entry != null){
				explorer.setBusy(true);
				// cancel previous
				if(currentChildLoader != null)
					currentChildLoader.cancel();
				
				// now start new one
				currentChildLoader = new ChildLoader(this,entry);
				currentChildLoader.start();
				//SwingUtilities.invokeLater(currentChildLoader);
			}
		}
		
		/**
		 * Sets the selected value.
		 *
		 * @param entry the new selected value
		 */
		public void setSelectedValue(Object entry){
			// if selecting value, next panel might be busy
			if(getNext() != null)
				getNext().setLoading(true);
			// wait for previous panel to load this panel's content
			final Object e = entry;
			new Thread(new Runnable(){
				public void run(){
					while(isLoading()){
						try {
							Thread.sleep(200);
						} catch (InterruptedException ex) {
						}
					}
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							list.clearSelection();
							list.setSelectedValue(e,true);
						}
					});
				}
			}).start();
		}
		
		/**
		 * Gets the next.
		 *
		 * @return the next
		 */
		public ExplorerPanel getNext() {
			return next;
		}
		
		/**
		 * Gets the prev.
		 *
		 * @return the next
		 */
		public ExplorerPanel getPrev() {
			return prev;
		}
	}
	
	/**
	 * search combobox for searches.
	 *
	 * @author tseytlin
	 */
	private class SearchComboBox extends JComboBox implements DocumentListener, ActionListener {
		private IOntology ontology;
		private Terminology terminology;
		private JTextField text;
		private boolean block,selected,search;
		
		/**
		 * Instantiates a new search combo box.
		 */
		public SearchComboBox(){
			super();
			setEditable(true);
			text = (JTextField) getEditor().getEditorComponent();
			text.getDocument().addDocumentListener(this);
			addActionListener(this);
			putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.JComboBox#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e){
			search = false;
		}
		
		/**
		 * get text editor.
		 *
		 * @return the text editor
		 */
		public JTextField getTextEditor(){
			return text;
		}
		
		/**
		 * Sets the ontology.
		 *
		 * @param ont the new ontology
		 */
		public void setOntology(IOntology ont){
			ontology = ont;
		}
		
		/**
		 * Sets the terminology.
		 *
		 * @param term the new terminology
		 */
		public void setTerminology(Terminology term){
			terminology = term;
		}
		
		/**
		 * notify that we are done.
		 */
		protected void fireActionEvent() {
			//disable firing during sync
			//System.out.println(block+" "+selected);
			if(!block || selected){ 
				super.fireActionEvent();
			}
		}

		/**
		 * Clear.
		 */
		public void clear(){
			removeAllItems();
			//s = new StringBuffer();
			text.setText("");
		}
		
		/**
		 * Sync.
		 *
		 * @param str the str
		 */
		//sync combobox w/ what is typed in
		private void sync(String str) {
			sync(str,false);
		}
		
		/**
		 * Sync.
		 *
		 * @param str the str
		 * @param force the force
		 */
		//sync combobox w/ what is typed in
		private void sync(String str,boolean force) {
			if(!search)
				return;
			
			removeAllItems();
			hidePopup();
			boolean show = false;
			if(str != null && str.length() != 0 && (force || str.length() >= SEARCH_CHAR_LIMIT)){
				Set list = new TreeSet();
				if(ontology != null){
					IResourceIterator i = ontology.getMatchingResources(str.replaceAll(" ","_"));
					while(i.hasNext()){
						Object o = i.next();
						if(o != null){
							list.add(o);
							show = true;
						}
					}
				}else if(terminology != null){
					try{
						for(Concept c: terminology.search(str)){
							list.add(c);
							show = true;
						}
					}catch(TerminologyException ex){
						ex.printStackTrace();
					}
				}
				// add in alphabetical order
				addItem("");
				for(Object o: list){
					addItem(o);
				}
			}
			revalidate();
			if(show && isShowing())
				showPopup();
			text.setText(str);
		}

		
		/**
		 * Sync.
		 */
		private synchronized void sync(){
			sync(false);
		}
		
		/**
		 * Sync.
		 *
		 * @param force the force
		 */
		private synchronized void sync(boolean force){
			block = true;
			final boolean f = force;
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					sync(text.getText(),f);
					block = false;
				}
			});
		}
		
		/**
		 * get selected item.
		 *
		 * @return the selected item
		 */
		public Object getSelectedItem() {
			selected = false;
			Object obj = super.getSelectedItem();
			
			//System.out.println(obj+" "+getSelectedIndex());
			if(obj != null){
				if(obj.toString().length() == 0){
					return null;
				}
				selected = true;
			}
			return obj;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		public void changedUpdate(DocumentEvent arg0) {
			search = true;
			if(!block)
				sync();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		public void insertUpdate(DocumentEvent arg0) {
			search = true;
			if(!block)
				sync();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		public void removeUpdate(DocumentEvent arg0) {
			search = true;
			if(!block)
				sync();
		}
	}
	
	
	
	/**
	 * loading of children.
	 */
	private class ChildLoader extends Thread {
		private ExplorerPanel panel;
		private Object entry;
		private boolean cancel;
		
		/**
		 * Instantiates a new child loader.
		 *
		 * @param next the next
		 * @param entry the entry
		 */
		public ChildLoader(ExplorerPanel next, Object entry){
			this.panel = next;
			this.entry = entry;
		}
		
		/**
		 * Checks for children.
		 *
		 * @param entry the entry
		 * @return true, if successful
		 */
		// check if concept has children
		private boolean hasChildren(Object entry){
			if(entry instanceof Concept){
				try{
					return ((Concept)entry).getChildrenConcepts().length > 0;
				}catch(TerminologyException ex){}
			}else if(entry instanceof IClass){
				IClass cls = (IClass) entry;
				return cls.getDirectSubClasses().length > 0 || cls.getDirectInstances().length > 0;
			}
			return false;
		}
		
		/*
		private void removeExplorerPanel(ExplorerPanel panel, int n){
			Container parent = panel.getParent();
			if(parent == null)
				return;
			// don't remove panels if total count is less or equals
			// to set number
			if(n > panelNumber){
				// remove child panel
				if(panel.getNext() != null)
					removeExplorerPanel(panel.getNext(),n-1);
				// set parent to null
				if(panel.getPrev() != null)
					panel.getPrev().setNext(null);
				panel.dispose();
				parent.remove(panel);
			}
		}
		*/
		
		/**
		 * Removes the panel.
		 *
		 * @param panel the panel
		 */
		private void removePanel(ExplorerPanel panel){
			// remove next panel first
			if(panel.getNext() != null)
				removePanel(panel.getNext());
			
			// once last panel was removed do we need to remove this one?
			Container parent = panel.getParent();
			if(parent.getComponentCount() > panelNumber){
				// set parent of this to null
				if(panel.getPrev() != null)
					panel.getPrev().setNext(null);
				panel.dispose();
				parent.remove(panel);
			}
		}
		
		/**
		 * Cancel.
		 */
		public void cancel(){
			cancel = true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		//run loading of resource
		public void run(){
			boolean hasChildren = hasChildren(entry);
			
			// don't do anything with canceled entries
			if(cancel)
				return;
			
			// set next panel
			if(panel.getNext() != null){
				panel.getNext().clear();
				panel.getNext().setRoot(entry);
			
				//	remove empty panels after the last one
				ExplorerPanel gpanel = panel.getNext().getNext();
				if(gpanel != null){
					removePanel(gpanel);
					panel.getParent().validate();
				}
			}else if(hasChildren){
				ExplorerPanel ep = new ExplorerPanel();
				ep.setPrev(panel);
				panel.setNext(ep);
				explorer.addPanel(ep);
				ep.setRoot(entry);
				ep.setLoading(false);
			}
			
			// display next panel
			if(panel.getNext() != null){
				Rectangle r = new Rectangle(panel.getNext().getBounds());
				explorer.scroll.getViewport().scrollRectToVisible(r);
			}
			
			// don't do anything with canceled entries
			if(cancel)
				return;
			
			//prefetch all info before displaying
			if(entry instanceof IResource)
				explorer.getHTMLInfo((IResource)entry);

			if(cancel)
				return;
			
			explorer.show(entry);
			explorer.setSelectedEntry(entry);
			explorer.repaint();
			explorer.setBusy(false);
			
			if(panel.getNext() != null)
				panel.getNext().setLoading(false);
		}
	}
	
	
	/**
	 * display ontology explorer in a non-modal window.
	 *
	 * @param o the o
	 * @return the j dialog
	 */
	public static JDialog showOntology(IOntology o){
		final IOntology ont = o;
		final OntologyExplorer exp = new OntologyExplorer();
		
		// init dialog
		JDialog d = new JDialog();
		d.setTitle("OntologyExplorer - ["+ont.getName()+"]");
		d.setModal(false);
		d.setResizable(true);
		d.getContentPane().setLayout(new BorderLayout());
		d.getContentPane().add(exp,BorderLayout.CENTER);
		d.pack();
		
		// start the loading process
		exp.setBusy(true);
		(new Thread(new Runnable(){
			public void run(){
				exp.setRoot(ont.getRoot());
				exp.setBusy(false);
			}
		})).start();
		
		// display
		d.setVisible(true);
		return d;
	}
}
