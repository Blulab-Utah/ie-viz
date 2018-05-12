package edu.pitt.dbmi.nlp.noble.ui.widgets;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The Class DynamicList.
 */
public class DynamicList extends JList implements DocumentListener {
	public static final int STARTS_WITH_MATCH = 0;
	public static final int CONTAINS_MATCH = 1;
	private JTextField text;
	private Collection content;
	private boolean block,selected;
	private DefaultListModel model;
	private int matchMode;
	
	/**
	 * Instantiates a new dynamic list.
	 *
	 * @param text the text
	 */
	public DynamicList(JTextField text){
		this(text,Collections.EMPTY_LIST);
	}
	
		
	/**
	 * Instantiates a new dynamic list.
	 *
	 * @param text the text
	 * @param content the content
	 */
	public DynamicList(JTextField text,Collection content){
		super(new DefaultListModel());
		this.content = content;
		this.text = text;
		this.model = (DefaultListModel) getModel();
		load(content);
		text.setText("Search");
		text.setForeground(Color.LIGHT_GRAY);
		text.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if("".equals(DynamicList.this.text.getText())){
					DynamicList.this.text.setForeground(Color.LIGHT_GRAY);
					DynamicList.this.text.setText("Search");
				}
			}
			public void focusGained(FocusEvent e) {
				if("Search".equals(DynamicList.this.text.getText())){
					DynamicList.this.text.setForeground(Color.BLACK);
					DynamicList.this.text.setText("");
				}
			}
		});
		text.getDocument().addDocumentListener(this);
		text.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				int c = e.getKeyCode();
				if(c == KeyEvent.VK_DOWN || c == KeyEvent.VK_TAB){
					grabFocus();
				}
				
			}
		});
	}
	
	/**
	 * Load.
	 *
	 * @param list the list
	 */
	private void load(Collection list){
		for(Object o: list){
			model.addElement(o);
		}
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
	 * Clear.
	 */
	public void clear(){
		model.removeAllElements();
		text.setText("");
	}
	
	/**
	 * set match mode
	 * STARTS_WITH_MATCH,CONTAINS_MATCH.
	 *
	 * @param mode the new match mode
	 */
	public void setMatchMode(int mode){
		matchMode = mode;
	}
	
	
	/**
	 * Sync.
	 *
	 * @param str the str
	 */
	//sync combobox w/ what is typed in
	private void sync(String str) {
		model.removeAllElements();
		boolean show = false;
		if(str != null && str.length() > 0){
			for(Object word: getMatchingObjects(str)){
				model.addElement(word);
				show = true;
			}
		}else{
			for(Object word: content){
				model.addElement(word);
			}
		}
		revalidate();
		text.setText(str);
	}

	
	/**
	 * Sync.
	 */
	private synchronized void sync(){
		block = true;
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				sync(text.getText());
				block = false;
			}
		});
	}
	
	/**
	 * Gets the matching objects.
	 *
	 * @param str the str
	 * @return the matching objects
	 */
	private List getMatchingObjects(String str){
		str = str.toLowerCase();
		List list = new ArrayList();
		for(Object w: content){
			if(match(w.toString(),str)){
				list.add(w);
			}
		}
		return list;
	}
	
	
	/**
	 * Match.
	 *
	 * @param s1 the s 1
	 * @param s2 the s 2
	 * @return true, if successful
	 */
	private boolean match(String s1, String s2){
		if(text.getForeground().equals(Color.LIGHT_GRAY))
			return true;
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		switch(matchMode){
			case CONTAINS_MATCH:
				return s1.contains(s2);
			default:
				return s1.startsWith(s2);
				
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent arg0) {
		if(!block)
			sync();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent arg0) {
		if(!block)
			sync();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent arg0) {
		if(!block)
			sync();
	}
}