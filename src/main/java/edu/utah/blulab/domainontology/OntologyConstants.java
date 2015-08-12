package edu.utah.blulab.domainontology;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class OntologyConstants {
	public static final String SO_PM = "http://blulab.chpc.utah.edu/ontologies/SchemaOntology.owl";
	public static final String MO_PM = "http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl";
	public static final String PREF_LABEL = SO_PM + "#prefLabel";
	public static final String ALT_LABEL = SO_PM + "#altLabel";
	public static final String HIDDEN_LABEL = SO_PM + "#hiddenLabel";
	public static final String ABR_LABEL = SO_PM + "#abrLabel";
	public static final String SUBJ_EXP_LABEL = SO_PM + "#subjExpLabel";
	public static final String REGEX = SO_PM + "#regex";
	public static final String PREF_CUI = SO_PM + "#prefCUI";
	public static final String ALT_CUI = SO_PM + "#altCUI";
	public static final String SEC_HEADING = SO_PM + "#sectionHeader";
	public static final String DOC_TYPE = SO_PM + "#documentType";
	public static final String WINDOW = SO_PM + "#windowSize";
	public static final String ACTION_EN = MO_PM + "#hasActionEn";
	public static final String ACTION_DE = MO_PM + "#hasActionDe";
	public static final String ACTION_SV = MO_PM + "#hasActionSv";
	public static final String PREF_TERM = MO_PM + "#prefLabel";
	public static final String ALT_TERM = MO_PM + "#altLabel";
	public static final String EN_REGEX = MO_PM + "#hasEnRegEx";
	public static final String DE_REGEX = MO_PM + "#hasDeRegEx";
	public static final String SV_REGEX = MO_PM + "#hasSvRegEx";
	public static final String TERMINATION = MO_PM + "#hasTermination";
	public static final String CREATOR = MO_PM + "#creator";
	public static final String SOURCE = MO_PM + "#source";
	public static final String DATE = MO_PM + "#date";
	
	
	public static final String HAS_SEM_ATTRIBUTE = SO_PM + "#hasSemAttribute";
	public static final String HAS_LING_ATTRIBUTE = SO_PM + "#hasLingAttribute";
	public static final String HAS_CLOSURE = MO_PM + "#hasTermination";
	
	public static final String FORWARD_ACTION = "forward";
	public static final String BACKWARD_ACTION = "backward";
	public static final String BIDIRECTIONAL_ACTION = "bidirectional";
	public static final String DISCONTINUOUS_ACTION = "discontinuous";
	public static final String TERMINATE_ACTION = "terminate";
	
}
