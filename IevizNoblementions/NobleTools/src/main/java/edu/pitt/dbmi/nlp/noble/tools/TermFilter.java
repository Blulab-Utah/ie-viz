package edu.pitt.dbmi.nlp.noble.tools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This singleton class tries to filter UMLS terms to remove bady synonymy This
 * implementation is based on Hettne, Kristina M., et al. "Rewriting and
 * suppressing UMLS terms for improved biomedical term identification." Journal
 * of biomedical semantics 1.1 (2010): 1. Created by tseytlin on 12/11/16.
 */
public class TermFilter {
	private static String QUALIFIER_FILTER_FILE = "/resources/TermFilterQualifiers.txt";
	private static Set<String> qualifierFilter;
	public interface Filter {
		boolean isApplicable(String term);
		Set<String> filter(String term);
	}
	private static List<Filter> filters;

	/**
	 * get qualifier filters
	 * @return list of qualifiers
	 */
	private static Set<String> getQualifierFilterSet(){
		if(qualifierFilter == null){
			qualifierFilter = new HashSet<String>();
			try {
				for(String l: TextTools.getText(TermFilter.class.getResourceAsStream(QUALIFIER_FILTER_FILE)).split("\n")){
					l = l.trim();
					if(l.length()> 0)
						qualifierFilter.add(l);
				}
			} catch (IOException e) {
				throw new Error(e);
			}
		}
		return qualifierFilter;
	}
	
	// initialize filter rules
	static {
		filters = new ArrayList<Filter>();

		/*
		 * Non-essential parentheticals [24,26] has been split into four rules
		 * in order to make the error analysis more transparent: 1. Begin
		 * parentheses: remove expressions within parenthesis at the beginning
		 * of a term (e.g. (protein) methionine-R-sulfoxide reductase) 2. Begin
		 * brackets: remove expressions within brackets at the beginning of a
		 * term (e.g. [V] Alcohol use) 3. End parentheses removes expressions
		 * within parenthesis at the end of a term (e.g. flagellar filament
		 * (sensu Bacteria)) 4. End brackets removes expressions within brackets
		 * at the end of a term (e.g. Gluten-free foods [generic 1]) In
		 * addition, we have added the condition that the rule does not apply to
		 * terms belonging to the semantic group Chemicals & Drugs. The reason
		 * for this condition is that chemical expressions by nature often
		 * contain both brackets and parentheses at the beginning or end of a
		 * term.
		 * 
		 * NOTE: skip for now
		 */
		/*
		 * filters.add(new Filter () { public Set<String> filter(String term){
		 * //TODO: implement return Collections.singleton(term); } });
		 */
		/*
		 * Short token [24,26]: remove term if the whole term after tokenization
		 * and removal of stop words is a single character, or is an arabic or
		 * roman number. For this rule, the stop word list from PubMed [32] was
		 * used. This rule differs from the one in [24,26] in that it takes each
		 * token into account separately (e.g. the term “10*9/ L” would be
		 * tokenised to “10 9 L” and removed by this rule since every token
		 * either is a number or a single character).
		 * 
		 * NOTE: Melissa doesn't like it
		 * 
		 * filters.add(new Filter () { public Set<String> filter(String term){
		 * //TODO: implement return Collections.singleton(term); } });
		 */
		/*
		 * Dosages [24]: the original rule addressed terms belonging to certain
		 * term types defined by the NLM in the UMLS, namely BD (Fully-specified
		 * drug brand name that can be prescribed), CD (Clinical Drug) or MS
		 * (Multiple names of branded and generic supplies or supplements). This
		 * rule was further refined by us to remove all terms that contain a
		 * dosage in percent, gram, microgram or milliliter (e.g. Oxygen 2%).
		 * 
		 * NOTE: Might cause issues
		 * 
		 * filters.add(new Filter () { public Set<String> filter(String term){
		 * //TODO: implement & ask return Collections.singleton(term); } });
		 */
		/*
		 * At-sign: this rule was implemented by us to remove terms that contain
		 * the @-character (e.g. ADHESIVE @@ BANDAGE).
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.contains("@");
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});
		/*
		 * EC numbers [26]: Remove terms that contain enzyme classification
		 * numbers as defined by IUPAC (e.g. EC 2.7.1.112). The justification
		 * for this rule is that an EC number in the UMLS usually is mapped to a
		 * specific enzyme while it actually refers to a class of enzymes.
		 * 
		 * NOTE: might be useful
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*");
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});
		/*
		 * Any classification [24]: remove terms containing the following
		 * properties: “NEC” at the end of a term and preceded by a comma, “NEC”
		 * within parentheses or brackets at the end of a term and preceded by a
		 * space, “not elsewhere classified”, “unclassified”, “without mention”
		 * (e.g. “Unclassified sequences”).
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				String t = term.toLowerCase();
				for (String affix : Arrays.asList("nec", "not elsewhere classified", "unclassified",
						"without mention")) {
					if (t.startsWith(affix) || t.endsWith(" " + affix) || term.endsWith("," + affix)) {
						return true;
					}
				}
				return false;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});
		/*
		 * Any underspecification [24,26]: remove terms containing the following
		 * properties: “not otherwise specified”, “not specified”, or
		 * “unspecified"; “NOS” at the end of a term and preceded by a comma, or
		 * “NOS” within parentheses or brackets at the end of a term and
		 * preceded by a space (e.g. “Other and unspecified leukaemia”).
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				String t = term.toLowerCase();
				for (String affix : Arrays.asList("nos", "not specified", "unspecified", "not otherwise specified")) {
					if (t.startsWith(affix + " ") || t.endsWith(" " + affix) || t.endsWith("," + affix)) {
						return true;
					}
				}
				return false;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}

		});
		/*
		 * Miscellaneous [24,26]: remove terms containing the following
		 * properties: “other” at the beginning of a term and followed by a
		 * space character or at the end of a term and preceded by a space
		 * character; “deprecated”, “unknown”, “obsolete”, “miscellaneous”, or
		 * “no” at the beginning of a term and followed by a space character
		 * (e.g."Other”).
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				String t = term.toLowerCase();
				for (String affix : Arrays.asList("other", "deprecated", "unspecified", "unknown", "miscellaneous",	"no")) {
					if (t.startsWith(affix + " ") || t.endsWith(" " + affix) || term.endsWith("," + affix)) {
						return true;
					}
				}
				return false;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});
		/*
		 * Words > 5 [25]: remove terms that contain more that five words (e.g.
		 * “Head and Neck Squamous Cell Carcinoma”). This rule is not applied to
		 * terms belonging to the semantic group Chemicals & Drugs.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return TextTools.normalizeWords(term, false, true, true).size() > 5;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});

		/*
		 * Remove terms that have "structure of", "entire". This rule pertains
		 * mostly to Anatomic Sites.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				String t = term.toLowerCase();
				for (String affix : Arrays.asList("structure of", "entire", "structure")) {
					if (t.startsWith(affix + " ") || t.endsWith(" " + affix)) {
						return true;
					}
				}
				return false;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});

		/*
		 * Remove concepts that contain "Left", "Right", "Bilateral", "OR",
		 * "AND"
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				String t = term.toLowerCase();
				for (String affix : Arrays.asList("left", "right", "bilateral", "or", "and")) {
					if (t.matches(".*\\b" + affix + "\\b.*")) {
						return true;
					}
				}
				return false;
			}

			public Set<String> filter(String term) {
				return Collections.EMPTY_SET;
			}
		});

		/*
		 * Syntactic inversion [24,26]: add syntactic inversion of term if a
		 * term contains a comma followed by a space and does not contain a
		 * preposition or conjunction (e.g.“Failure, Renal”). We added the
		 * condition that only one such pattern of a comma followed by a space
		 * is to be found in a term for the rule to be executed.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.matches("([^,]+), ([^,]+)");
			}

			public Set<String> filter(String term) {
				String t = term;
				Pattern p = Pattern.compile("([^,]+), ([^,]+)");
				Matcher m = p.matcher(term);
				if (m.matches())
					t = m.group(2) + " " + m.group(1);
				return Collections.singleton(t);
			}
		});
		/*
		 * Possessives [26]: remove the possessive “’s” at the end of a word
		 * (e.g. “Alzheimer’s disease”) and add the rewritten term.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.matches(".*['’]s.*");
			}

			public Set<String> filter(String term) {
				if (term.matches(".*['’]s.*"))
					term = term.replaceAll("([A-Za-z]+)['’]s ", "$1 ");
				return Collections.singleton(term);
			}
		});
		/*
		 * Short form/long form [29]: add short form and long form of term (e.g.
		 * “Selective Serotonin Reuptake Inhibitors (SSRIs)”). Schwartz and
		 * Hearst’s algorithm [29] achieved 96% precision and 82% recall on a
		 * standard test collection, which was as good as existing approaches at
		 * the time [29] and still competitive according to recent comparison
		 * studies [30,31]. An advantage of the algorithm is that, unlike other
		 * approaches, it does not require any training data. Two extra
		 * conditions were added to the original rule by Schwartz and Hearst: 1)
		 * the short form must be found at the end of the term, and 2) the first
		 * letter of the short form should be the same as the first letter of
		 * the long form. These conditions were added in order to adjust the
		 * rule to extract abbreviations from a dictionary instead of from
		 * biomedical text.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return AcronymDetector.extractAcronym(term) != null;
			}

			public Set<String> filter(String term) {
				Set<String> terms = new LinkedHashSet<String>();
				AcronymDetector.Acronym acr = AcronymDetector.extractAcronym(term);
				if (acr != null) {
					terms.add(acr.longForm);
					//TODO: perhaps don't do that as it adds polysemy on grand scale
					terms.add(acr.shortForm);
					return terms;
				}
				return Collections.singleton(term);
			}
		});
		/*
		 * Angular brackets [26]: remove expressions within angular brackets
		 * anywhere in a term. This pattern was previously used in the UMLS to
		 * denote polysemy or homonymy of a term, i.e. a term having different
		 * meanings. Terms having this property still exist in the UMLS, even
		 * though the property is not assigned to new terms. We have adjusted
		 * the rule to remove expressions within angular brackets anywhere in a
		 * term since these expressions usually contain meta-information about a
		 * term, which is unlikely to be found in text (e.g. “Chondria
		 * <beetle>“). NOTE: or square brackets
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.matches(".*[<\\[].*[>\\]].*");
			}

			public Set<String> filter(String term) {
				return Collections.singleton(term.replaceAll("[<\\[].*[>\\]]", "").trim());
			}
		});
		/*
		 * Semantic type: remove expressions within parentheses that match the
		 * list of semantic types in the UMLS (e.g. “Surgical intervention
		 * (finding)”). This rule was developed by our group based on the
		 * observation that the semantic type to which the term belongs to is
		 * often added as meta-information about the term.
		 */
		filters.add(new Filter() {
			public boolean isApplicable(String term) {
				return term.matches(".* \\(([A-Za-z ]+)\\)");
			}

			public Set<String> filter(String term) {
				Pattern p = Pattern.compile(".* \\(([A-Za-z ]+)\\)");
				Matcher m = p.matcher(term);
				if (m.matches() && getQualifierFilterSet().contains(m.group(1))) {
					term = term.replaceAll("\\([A-Za-z ]+\\)", "");
				}
				return Collections.singleton(term.trim());
			}
		});

	}

	/**
	 * filter UMLS terms based on Hettne rules
	 * 
	 * @param synonyms - input strings
	 * @return list of synonyms taht pass the filter
	 */
	public static Set<String> filter(String[] synonyms) {
		Set<String> terms = new LinkedHashSet<String>();
		for (String term : synonyms) {
			boolean applied = false;
			for (Filter filter : filters) {
				if (filter.isApplicable(term)) {
					terms.addAll(filter.filter(term));
					applied = true;
					break;
				}
			}
			if(!applied)
				terms.add(term);
		}
		return terms;
	}

	/**
	 * filter UMLS terms based on Hettne rules
	 * 
	 * @param synonyms  - input strings
	 * @return list of synonyms taht pass the filter
	 */
	public static Set<String> filter(Collection<String> synonyms) {
		return filter(synonyms.toArray(new String[0]));
	}
	
	/**
	 * filter UMLS terms based on Hettne rules
	 * 
	 * @param synonym  - input string
	 * @return list of synonyms taht pass the filter
	 */
	public static Set<String> filter(String synonym) {
		return filter(Collections.singleton(synonym));
	}
}