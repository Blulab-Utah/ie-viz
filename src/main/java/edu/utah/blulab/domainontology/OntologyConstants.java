package edu.utah.blulab.domainontology;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class OntologyConstants {
	public static final String SO_PM = "http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl";
	public static final String MO_PM = "http://blulab.chpc.utah.edu/ontologies/v2/Modifier.owl";
	public static final String CT_PM = "http://blulab.chpc.utah.edu/ontologies/v2/ConText.owl";
	public static final String TM_PM = "http://blulab.chpc.utah.edu/ontologies/TermMapping.owl";
	public static final String PREF_TERM = TM_PM + "#preferredTerm";
	public static final String SYNONYM = TM_PM + "#synonym";
	public static final String MISSPELLING = TM_PM + "#misspelling";
	public static final String ABBREVIATION = TM_PM + "#abbreviation";
	public static final String SUBJ_EXP = TM_PM + "#subjectiveExpressionl";
	public static final String REGEX = TM_PM + "#regex";
	public static final String PREF_CODE = TM_PM + "#code";
	public static final String ALT_CODE = SO_PM + "#alternateCode";
	public static final String SEC_HEADING = SO_PM + "#sectionHeader";
	public static final String DOC_TYPE = SO_PM + "#documentType";
	public static final String WINDOW = CT_PM + "#windowSize";
	public static final String ACTION_EN = CT_PM + "#hasActionEn";
	public static final String ACTION_DE = CT_PM + "#hasActionDe";
	public static final String ACTION_SV = CT_PM + "#hasActionSv";
	public static final String TERMINATION = CT_PM + "#hasTermination";
	public static final String CREATOR = CT_PM + "#author";
	public static final String SOURCE = CT_PM + "#source";
	//public static final String DATE = MO_PM + "#date";
	public static final String ENGLISH = "en";
	public static final String SWEDISH = "sv";
	public static final String GERMAN = "de";
	
	
	public static final String HAS_SEM_ATTRIBUTE = SO_PM + "#hasSemAttribute";
	public static final String HAS_LING_ATTRIBUTE = SO_PM + "#hasLingAttribute";
	public static final String HAS_CLOSURE = MO_PM + "#hasTermination";
	public static final String HAS_ANCHOR = SO_PM + "#hasAnchor";
	
	public static final String FORWARD_ACTION = "forward";
	public static final String BACKWARD_ACTION = "backward";
	public static final String BIDIRECTIONAL_ACTION = "bidirectional";
	public static final String DISCONTINUOUS_ACTION = "discontinuous";
	public static final String TERMINATE_ACTION = "terminate";
	
}
