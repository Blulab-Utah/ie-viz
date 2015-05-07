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
}
