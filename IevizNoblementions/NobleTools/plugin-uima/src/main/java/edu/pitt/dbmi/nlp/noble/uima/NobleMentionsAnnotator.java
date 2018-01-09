package edu.pitt.dbmi.nlp.noble.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;

import edu.pitt.dbmi.nlp.noble.mentions.NobleMentions;

public class NobleMentionsAnnotator extends JCasAnnotator_ImplBase {
	private Logger logger;
	private UimaContext uimaContext;
	private NobleMentions nobleMentions;
	
	/**
	 * Initialize the annotator, which includes compilation of regular
	 * expressions, fetching configuration parameters from XML descriptor file,
	 * and loading of the dictionary file.
	 */
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		this.uimaContext = uimaContext;
		logger = uimaContext.getLogger();
	
	}
	
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

	}

}
