package edu.pitt.dbmi.nlp.noble.mentions.model;

import edu.pitt.dbmi.nlp.noble.coder.model.*;
import edu.pitt.dbmi.nlp.noble.coder.processor.DictionarySectionProcessor;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.ConTextHelper;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.ConceptImporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * This class is a wrapper for http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl
 * DomainOntology.owl was developed by Melissa Castine and Wendy Chapmen (University of Utah)
 *
 * @author Eugene Tseytlin (University of Pittsburgh)
 */
public class DomainOntology {
    public static final String ANCHOR = "Anchor";
    public static final String COMPOUND_ANCHOR = "CompoundAnchor";
    public static final String MODIFIER = "Modifier";

    public static final String PSEUDO_ANCHOR = "PseudoAnchor";
    public static final String PSEUDO_MODIFIER = "PseudoModifier";
    public static final String ANNOTATION = "Annotation";
    public static final List<String> ANCHOR_ROOTS = Arrays.asList(ANCHOR, COMPOUND_ANCHOR, PSEUDO_ANCHOR);
    public static final List<String> MODIFIER_ROOTS = Arrays.asList("Closure", PSEUDO_MODIFIER, MODIFIER);
    public static final String LANGUAGE = "en";
    public static final String SEMTYPE_INSTANCE = "Instance";
    public static final String HAS_ANCHOR = "hasAnchor";
    public static final String SCHEMA_OWL = "Schema.owl";
    public static final String HAS_MODIFIER = "hasModifier";
    public static final String IS_ANCHOR_OF = "isAnchorOf";
    public static final String HAS_ANNOTATION_TYPE = "hasAnnotationType";
    public static final String ANNOTATION_MENTION = "MentionAnnotation";
    public static final String LINGUISTIC_MODIFER = ConText.LINGUISTIC_MODIFIER;
    public static final String NUMERIC_MODIFER = "NumericModifier";
    public static final String HAS_COMPOUND_ARGUMENT = "hasCompoundArgument";
    public static final String COMPOSITION = "Composition";
    public static final String HAS_TITLE = "hasTitle";
    public static final String HAS_MENTION_ANNOTATION = "hasMentionAnnotation";
    public static final String HAS_DOCUMENT_ANNOTATION = "hasDocumentAnnotation";
    public static final String QUANTITY = "Quantity";
    public static final String RANGE = "RangeModifier";
    public static final String RATIO = "Ratio";
    public static final String DIMENSIONAL_MEASUREMENT = "DimensionalMeasurement";
    public static final String HAS_NUM_VALUE = "hasNumValue";
    public static final String HAS_QUANTITY_VALUE = "hasQuantityValue";
    public static final String HAS_NUMERATOR_VALUE = "hasNumeratorValue";
    public static final String HAS_DENOMINATOR_VALUE = "hasDenominatorValue";
    public static final String HAS_LOW_VALUE = "hasLowValue";
    public static final String HAS_HIGH_VALUE = "hasHighValue";
    public static final String HAS_RELATION = "hasRelation";
    private static final String DIMENSION_VALUE = "DimensionValue";
    public static final String HAS_UNIT = "hasUnit";
    public static final String HAS_VALUE = "hasValue";
    public static final String UNIT = "Unit";
    protected static final String EVAL_INSTANCE_SUFFIX = "_evaluation_inst";
    public static final String HAS_SECTION = "hasSection";
    public static final String DOCUMENT_SECTION = "DocumentSection";
    public static final String HAS_SPAN = "hasSpan";
    public static final String HAS_ANNOTATION_TEXT = "hasAnnotationText";
    public static final String PARAGRAPH_SCOPE = "paragraph";
    public static final String SECTION_SCOPE = "section";

    private IOntology ontology;
    private Terminology anchorTerminology, modifierTerminology, sectionTerminology;
    //private Map<String,SemanticType> semanticTypeMap;
    private ConText.ModifierResolver modifierResolver;
    private Map<IClass, Set<IClass>> compoundAnchorMap;
    private File ontologyLocation;
    private Map<String, String> defaultValues;
    private static int instanceCounter = 1;
    private Map<IClass, List<IInstance>> classInstanceMap;
    private boolean normalizeAnchors, normalizeModifiers, scoreConcepts;
    private String annotatioRelationSkope = PARAGRAPH_SCOPE;

    /**
     * File or URL location of the domain ontology
     *
     * @param location of ontology
     * @throws IOntologyException if there was something wrong
     */
    public DomainOntology(String location) throws IOntologyException {
        //	this(OOntology.loadOntology(location));
        URI ontologyURI = OntologyUtils.createOntologyInstanceURI(location);
        File file = new File(location);
        if (file.exists()) {
            setOntology(OOntology.createOntology(ontologyURI, file));
            ontologyLocation = file;
        } else if (location.startsWith("http")) {
            setOntology(OOntology.createOntology(ontologyURI, URI.create(location)));
        } else {
            throw new IOntologyException("Unable to identify ontology schema location " + location);
        }

    }

    /**
     * File or URL location of the domain ontology
     *
     * @param ont - ontology that this domain is based on
     * @throws IOntologyException if something went wrong
     */
    public DomainOntology(IOntology ont) throws IOntologyException {
        setOntology(ont);
        File file = new File(ont.getLocation());
        if (file.exists())
            ontologyLocation = file;
    }

    public File getOntologyLocation() {
        return ontologyLocation;
    }


    public boolean isNormalizeAnchorTerms() {
        return normalizeAnchors;
    }

    public void setNormalizeAnchorTerms(boolean stemAnchorTerms) {
        this.normalizeAnchors = stemAnchorTerms;
    }

    public boolean isNormalizeModifierTerms() {
        return normalizeModifiers;
    }

    public void setNormalizeModifierTerms(boolean normalizeModifiers) {
        this.normalizeModifiers = normalizeModifiers;
    }

    public String getAnnotatioRelationSkope() {
        return annotatioRelationSkope;
    }

    public void setAnnotatioRelationSkope(String annotatioRelationSkope) {
        this.annotatioRelationSkope = annotatioRelationSkope;
    }


    public boolean isScoreAnchors() {
        return scoreConcepts;
    }

    public void setScoreAnchors(boolean scoreConcepts) {
        this.scoreConcepts = scoreConcepts;
    }

    /**
     * get ontology object
     *
     * @return IOntology object
     */
    public IOntology getOntology() {
        return ontology;
    }


    /**
     * set domain ontology based on http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl
     *
     * @param ontology - an ontology object representing domain ontology
     * @throws IOntologyException in case something went wrong
     */

    public void setOntology(IOntology ontology) throws IOntologyException {
        this.ontology = ontology;
        if (anchorTerminology != null)
            anchorTerminology.dispose();
        if (modifierTerminology != null)
            modifierTerminology.dispose();
        anchorTerminology = null;
        modifierTerminology = null;
    }


    /**
     * check if ontology is complient with Schema.owl ontology
     *
     * @return true or false
     */
    public boolean isOntologyValid() {
        // make sure it derives from schema
        boolean fromSchema = false;
        for (IOntology ont : getOntology().getImportedOntologies()) {
            if (ont.getURI().toString().contains(SCHEMA_OWL)) {
                fromSchema = true;
                break;
            }
        }
		/*if(!fromSchema){
			throw new IOntologyException("Ontology "+ontology.getName()+" does not derive from "+SCHEMA_OWL+" ontology");
		}*/
        return fromSchema;
    }

    /**
     * get locations of terminology cache
     *
     * @param ontologyLocation - location of ontology file
     * @return File directory location
     */
    public static File getTerminologyCacheLocation(File ontologyLocation) {
        if (ontologyLocation != null) {
            File file = new File(ontologyLocation + ".terminologies");
            if (!file.exists())
                file.mkdirs();
            return file;
        }
        return null;
    }


    /**
     * get locations of terminology cache
     *
     * @return File directory location
     */
    public File getTerminologyCacheLocation() {
        return getTerminologyCacheLocation(ontologyLocation);
    }

    private File getAnchorTerminologyFile() {
        if (ontologyLocation != null) {
            File file = new File(getTerminologyCacheLocation(ontologyLocation), "Anchors.term");
            return file;
        }
        return null;
    }

    private File getSectionTerminologyFile() {
        if (ontologyLocation != null) {
            File file = new File(getTerminologyCacheLocation(ontologyLocation), "Sections.term");
            return file;
        }
        return null;
    }


    private File getModifierTerminologyFile() {
        if (ontologyLocation != null) {
            File file = new File(getTerminologyCacheLocation(ontologyLocation), "Modifiers.term");
            return file;
        }
        return null;
    }


    /**
     * get document section terminology
     *
     * @return section terminology
     */
    public Terminology getSectionTerminology() {
        if (sectionTerminology == null) {
            File sectionFile = getSectionTerminologyFile();
            try {
                // check if there is a cache available
                if (sectionFile != null && sectionFile.exists()) {
                    sectionTerminology = new NobleCoderTerminology(sectionFile);
                } else {
                    Terminology terminology = DictionarySectionProcessor.loadDocumentSections(ontology, sectionFile);
                    terminology.dispose();
                    sectionTerminology = new NobleCoderTerminology(sectionFile);
                }
            } catch (Exception e) {
                throw new TerminologyError("Unable to load anchor terminology from " + sectionTerminology, e);
            }
        }
        return sectionTerminology;
    }


    /**
     * get a terminology of anchors
     *
     * @return anchor terminology
     */
    public Terminology getAnchorTerminology() {
        if (anchorTerminology == null) {
            File anchorFile = getAnchorTerminologyFile();
            try {
                // check if there is a cache available
                if (anchorFile != null && anchorFile.exists()) {
                    anchorTerminology = new NobleCoderTerminology(anchorFile);
                } else {
                    NobleCoderTerminology terminology = null;
                    if (anchorFile.getParentFile().exists()) {
                        terminology = new NobleCoderTerminology(anchorFile, false);
                    } else {
                        terminology = new NobleCoderTerminology();
                        terminology.setName("Anchors");
                    }

                    // set some options
                    terminology.setStemWords(isNormalizeAnchorTerms());
                    terminology.setScoreConcepts(isScoreAnchors());
                    terminology.setSelectBestCandidate(false);

                    //TODO: maybe custom params
                    // set language filter to only return English values
                    if (ontology instanceof OOntology)
                        ((OOntology) ontology).setLanguageFilter(Arrays.asList(LANGUAGE));

                    for (String root : ANCHOR_ROOTS) {
                        IClass cls = ontology.getClass(root);
                        if (cls != null) {
                            // add roots to terminology
                            terminology.addRoot(addConcept(terminology, cls).getCode());
                            // go over all subclasses
                            for (IClass c : cls.getSubClasses()) {
                                addConcept(terminology, c);
                            }
                        }
                    }
                    // save cache
                    if (anchorFile.exists()) {
                        // compact
                        ConceptImporter.getInstance().compact(terminology);

                        // save
                        terminology.save();
                        terminology.dispose();
                        // reload
                        terminology = new NobleCoderTerminology(anchorFile);
                    }

                    //semanticTypeMap = null;
                    anchorTerminology = terminology;
                }
            } catch (IOException e) {
                throw new TerminologyError("Unable to load anchor terminology from " + anchorFile, e);
            }

        }

        return anchorTerminology;
    }


    /**
     * create a modifier terminology for a given domain ontology
     *
     * @return modifier terminology
     */
    public Terminology getModifierTerminology() {
        if (modifierTerminology == null) {
            // check if there is a cache available
            File modifierFile = getModifierTerminologyFile();
            try {
                if (modifierFile != null && modifierFile.exists()) {
                    modifierTerminology = new NobleCoderTerminology(modifierFile);
                } else {
                    // setup special interest of noble coder
                    NobleCoderTerminology terminology = null;
                    if (modifierFile.getParentFile().exists()) {
                        terminology = new NobleCoderTerminology(modifierFile, false);
                    } else {
                        terminology = new NobleCoderTerminology();
                        terminology.setName("Modifiers");
                    }


                    //terminology.load(getClass().getSimpleName(),false);
                    //terminology.setName("ModifierTerminology");
                    terminology.setDefaultSearchMethod(NobleCoderTerminology.CUSTOM_MATCH);
                    terminology.setContiguousMode(true);
                    terminology.setSubsumptionMode(false);
                    terminology.setOverlapMode(true);
                    terminology.setPartialMode(false);
                    terminology.setOrderedMode(true);
                    terminology.setMaximumWordGap(0);
                    terminology.setScoreConcepts(false);
                    terminology.setHandlePossibleAcronyms(false);
                    terminology.setLanguageFilter(new String[]{LANGUAGE});
                    terminology.setStemWords(isNormalizeModifierTerms());
                    terminology.setStripStopWords(isNormalizeModifierTerms());
                    terminology.setIgnoreSmallWords(false);
                    terminology.setIgnoreDigits(false);
                    terminology.setSemanticTypeFilter(SEMTYPE_INSTANCE);

                    // set language filter to only return English values
                    if (ontology instanceof OOntology)
                        ((OOntology) ontology).setLanguageFilter(Arrays.asList(LANGUAGE));


                    // load classes
                    for (String root : MODIFIER_ROOTS) {
                        IClass cls = ontology.getClass(root);
                        if (cls != null) {
                            // add roots to terminology
                            terminology.addRoot(addConcept(terminology, cls).getCode());
                            for (IInstance inst : cls.getDirectInstances()) {
                                addConcept(terminology, inst);
                            }

                            // go over all subclasses
                            for (IClass c : cls.getSubClasses()) {
                                addConcept(terminology, c);
                                for (IInstance inst : c.getDirectInstances()) {
                                    addConcept(terminology, inst);
                                }
                            }
                        }
                    }
                    if (modifierFile.exists()) {
                        // compact
                        ConceptImporter.getInstance().compact(terminology);

                        //save
                        terminology.save();
                        terminology.dispose();
                        // reload
                        terminology = new NobleCoderTerminology(modifierFile);
                    }
                    modifierTerminology = terminology;
                }
            } catch (IOException e) {
                throw new TerminologyError("Unable to load anchor terminology from " + modifierFile, e);
            }

        }
        return modifierTerminology;
    }

    /**
     * get anchor and modifier terminology together
     *
     * @return all terminologies associated with this ontology
     */
    public Terminology[] getTerminologies() {
        return new Terminology[]{getAnchorTerminology(), getModifierTerminology(), getSectionTerminology()};
    }

    /**
     * Adds the concept.
     *
     * @param inst the inst
     * @return the concept
     * @throws TerminologyException the terminology exception
     */
    private Concept addConcept(Terminology terminology, IInstance inst) {
        try {
            Concept concept = ConTextHelper.createConcept(inst);

            // add inverse relationships based on ranges
            addInverseRelationships(inst.getDirectTypes()[0], ontology.getProperty(HAS_MODIFIER), concept);

            terminology.addConcept(concept);

            return concept;
        } catch (TerminologyException ex) {
            throw new TerminologyError("Unable to add a concept object", ex);
        }
    }

    /**
     * Adds the concept.
     *
     * @param cls the cls
     * @return the concept
     * @throws TerminologyException the terminology exception
     */
    private Concept addConcept(Terminology terminology, IClass cls) {
        try {
            Concept concept = ConTextHelper.createConcept(cls);

            // add inverse relationships based on ranges
            addInverseRelationships(cls, ontology.getProperty(HAS_MODIFIER), concept);

            terminology.addConcept(concept);

            return concept;
        } catch (TerminologyException ex) {
            throw new TerminologyError("Unable to add a concept object", ex);
        }
    }

    /**
     * add inverse relationships for a class from top property to a concept
     *
     * @param cls         that has relationship
     * @param hasModifier -property
     * @param concept     - concept to add the inverse relationship to
     */
    private void addInverseRelationships(IClass cls, IProperty hasModifier, Concept concept) {
        for (IProperty prop : hasModifier.getSubProperties()) {
            LogicExpression exp = new LogicExpression(ILogicExpression.OR, prop.getRange());
            if (exp.evaluate(cls)) {
                for (IClass domain : prop.getDomain()) {
                    concept.addRelatedConcept(Relation.getRelation(getInversePropertyName(prop.getName())), domain.getName());
                }
            }
        }
    }


    /**
     * convert the property name that follows a certain convention to a likely inverted form
     * Ex:  hasModifier -> isModifierOf and vice versa
     *
     * @param name - original property name
     * @return inverse property name
     */
    private String getInversePropertyName(String name) {
        if (name.startsWith("has")) {
            return "is" + name.substring(3) + "Of";
        } else if (name.startsWith("is") && name.endsWith("Of")) {
            return "has" + name.substring(2, name.length() - 2);
        }
        return name;
    }

    /**
     * get concept class for a given mention
     *
     * @param modifier object
     * @return class that represents this mention
     */
    public IClass getConceptClass(Modifier modifier) {
        if (modifier.getMention() != null)
            return getConceptClass(modifier.getMention());
        return ontology.getClass(modifier.getValue());
    }

    /**
     * get concept class for a given mention
     *
     * @param mention object
     * @return class that represents this mention
     */
    public IClass getConceptClass(Mention mention) {
        if (mention == null)
            return null;
        return getConceptClass(mention.getConcept());
    }

    /**
     * get concept class for a given concept
     *
     * @param concept object
     * @return class object
     */
    public IClass getConceptClass(Concept concept) {
        if (concept == null)
            return null;
        String uri = concept.getCode(Source.URI);
        if (uri != null) {
            IClass cls = ontology.getClass(uri);
            if (cls == null) {
                IInstance inst = ontology.getInstance(uri);
                if (inst != null) {
                    return inst.getDirectTypes()[0];
                }
            }
            return cls;
        }
        return null;
    }

    /**
     * get concept class for a given mention
     *
     * @param mention object
     * @return ontology instnace object
     */
    public IInstance getConceptInstance(Mention mention) {
        if (mention == null)
            return null;
        return getConceptInstance(mention.getConcept());
    }

    /**
     * get concept class for a given concept
     *
     * @param concept object
     * @return ontology instance object
     */
    public IInstance getConceptInstance(Concept concept) {
        if (concept == null)
            return null;
        String uri = concept.getCode(Source.URI);
        if (uri != null) {
            return ontology.getInstance(uri);
        }
        return null;
    }


    /**
     * get a list of anchors for given list of mentions typically in a sentence
     * Anchors can be compound anchors too.
     *
     * @param mentions - list of them
     * @return list of instances
     */
    public List<Instance> getAnchors(List<Mention> mentions) {
        List<Instance> anchors = new ArrayList<Instance>();

        // remove redundant mentions
        mentions = removeRedundantMentions(mentions);

        // add compound anchors as well
        Set<Mention> componentAnchors = new HashSet<Mention>();
        for (Instance a : getCompoundAnchors(mentions)) {
            anchors.add(a);
            componentAnchors.addAll(a.getCompoundComponents());
        }

        // go all mentions and create anchors for them
        for (Mention m : mentions) {
            if (isAnchor(m) && !componentAnchors.contains(m)) {
                anchors.add(new Instance(this, m));
            }
        }

        return anchors;
    }

    /**
     * remove mentions that have the same concept and overlap
     * Ex: "EPITHELIAL INCLUSION CYST" produces 2 annotations in NM. One with synonym "Epithelial cyst" and another with synonym "Inclusion Cyst".
     *
     * @param mentions
     * @return
     */
    private List<Mention> removeRedundantMentions(List<Mention> mentions) {
        // assume mentions are already sorted
        List<Mention> torem = new ArrayList<Mention>();
        Mention prioMention = null;
        for (Mention m : mentions) {
            if (prioMention != null) {
                // if prior mention contains the next one over and the share a concept nuke the next one
                if (prioMention.getConcept().equals(m.getConcept())) {
                    if (prioMention.contains(m)) {
                        torem.add(m);
                        continue;
                    } else if (m.contains(prioMention)) {
                        torem.add(prioMention);
                    }
                }
            }
            prioMention = m;
        }

        // now remove reduntant mentions
        if (!torem.isEmpty()) {
            mentions.removeAll(torem);
        }

        return mentions;
    }

    /**
     * is mention an anchor?
     *
     * @param m - mention object in question
     * @return true ro false
     */
    private boolean isAnchor(Mention m) {
        IClass cls = getConceptClass(m);
        return cls != null ? cls.hasSuperClass(ontology.getClass(ANCHOR)) || cls.hasSuperClass(ontology.getClass(COMPOUND_ANCHOR)) : false;
    }

    /**
     * is a mention of a given type
     *
     * @param m    - mention object in question
     * @param type - type in question
     * @return true or not
     */
    public boolean isTypeOf(Mention m, String type) {
        return isTypeOf(getConceptClass(m), type);
    }

    /**
     * is a mention of a given type
     *
     * @param cls  - class in question
     * @param type - type in question
     * @return true or not
     */
    public boolean isTypeOf(IClass cls, String type) {
        IClass typeCls = ontology.getClass(type);
        return cls != null && (cls.equals(typeCls) || cls.hasSuperClass(typeCls));
    }

    /**
     * get a list of compount anchors that can be constructed from a given set of mentions
     *
     * @param mentions list of them
     * @return list of Instance objects
     */
    private List<Instance> getCompoundAnchors(List<Mention> mentions) {
        List<Instance> compound = new ArrayList<Instance>();

        // fill in mention map
        //TODO: what if several mentions with same class?
        final Map<IClass, Mention> mentionMap = new LinkedHashMap<IClass, Mention>();
        for (Mention m : mentions) {
            if (isAnchor(m))
                mentionMap.put(getConceptClass(m), m);
        }

        // skip if nothing to do
        if (mentionMap.isEmpty() || getCompoundAnchorMap().isEmpty())
            return Collections.EMPTY_LIST;


        // get property
        IProperty hasCompoundArgument = ontology.getProperty(HAS_COMPOUND_ARGUMENT);
        Set<IClass> foundCompounds = new HashSet<IClass>();


        boolean change = false;
        do {
            // resort the mentions based on their position in text
            Set<IClass> mentionedClasses = new TreeSet<IClass>(new Comparator<IClass>() {
                public int compare(IClass o1, IClass o2) {
                    return mentionMap.get(o1).compareTo(mentionMap.get(o2));
                }
            });
            mentionedClasses.addAll(mentionMap.keySet());

            // go over all compounds anchors
            change = false;
            for (IClass compoundCls : getCompoundAnchorMap().keySet()) {
                // skip classes that were already found
                if (foundCompounds.contains(compoundCls))
                    continue;


                // find classes that are possible arguments
                Set<IClass> possibleArgs = getPossibleCompoundAnchorArguments(compoundCls, mentionedClasses);
                IRestriction[] compoundRestrictions = compoundCls.getRestrictions(hasCompoundArgument);

                // if number of possible arguments is good, try to see if we can match it for real
                if (possibleArgs.size() >= compoundRestrictions.length && compoundRestrictions.length > 0) {
                    // create an instance and see if it is satisfiable
                    IInstance inst = compoundCls.createInstance(createInstanceName(compoundCls));
                    List<IInstance> componentInst = new ArrayList<IInstance>();
                    List<Mention> possibleMentionComponents = new ArrayList<Mention>();
                    int n = 1;
                    String hasCompoundPrefix = hasCompoundArgument.getName();
                    for (IClass c : possibleArgs) {
                        IInstance i = c.createInstance(createInstanceName(c));
                        IProperty argProperty = (n <= 5) ? ontology.getProperty(hasCompoundPrefix + (n++)) : hasCompoundArgument;
                        inst.addPropertyValue(argProperty, i);
                        componentInst.add(i);
                        possibleMentionComponents.add(mentionMap.get(c));
                    }

                    // moment of truth does it work????
                    if (compoundCls.getEquivalentRestrictions().evaluate(inst)) {
                        Mention mention = createCompoundAnchorMention(compoundCls, possibleMentionComponents);
                        mentionMap.put(compoundCls, mention);

                        Instance compoundInstance = new Instance(this, mention, inst);
                        compoundInstance.setCompoundComponents(possibleMentionComponents);
                        compound.add(compoundInstance);
                        foundCompounds.add(compoundCls);
                        change = true;
                    } else {
                        //clean up
                        for (IInstance i : componentInst) {
                            i.delete();
                        }
                        inst.delete();
                    }

                }
            }
        } while (change);

        return compound;
    }

    /**
     * create compound anchor mentions
     *
     * @param compoundCls - compund class
     * @param components  - list of mention objects that are its parts
     * @return combined Mention object
     */
    public Mention createCompoundAnchorMention(IClass compoundCls, Collection<Mention> components) {
        Concept concept = null;
        try {
            concept = getAnchorTerminology().lookupConcept(compoundCls.getName());
        } catch (TerminologyException e) {
            throw new TerminologyError("Could not find concept " + compoundCls.getName(), e);
        }
        // create new mention
        Mention mention = new Mention();
        mention.setConcept(concept);
        List<Annotation> annotations = new ArrayList<Annotation>();
        for (Mention m : components) {
            if (mention.getSentence() == null)
                mention.setSentence(m.getSentence());
            annotations.addAll(m.getAnnotations());
            mention.addModifiers(m.getModifierMap());
        }
        mention.setAnnotations(annotations);

        return mention;
    }


    /**
     * get possible compoung anchor arguments
     *
     * @param compoundCls  - compound class in question
     * @param mentionsClss -  mention classes
     * @return set of classes that are arguments
     */

    private Set<IClass> getPossibleCompoundAnchorArguments(IClass compoundCls, Set<IClass> mentionsClss) {
        Set<IClass> found = new LinkedHashSet<IClass>();
        for (IClass mention : mentionsClss) {
            Set<IClass> args = getCompoundAnchorMap().get(compoundCls);
            if (args.contains(mention))
                found.add(mention);
            else {
                for (IClass i : args) {
                    if (i.hasSubClass(mention)) {
                        found.add(mention);
                        break;
                    }
                }
            }
        }

        return found;
    }


    /**
     * get the mapping between compound anchors and its components
     *
     * @return compound anchor map
     */
    private Map<IClass, Set<IClass>> getCompoundAnchorMap() {
        if (compoundAnchorMap == null) {
            compoundAnchorMap = new HashMap<IClass, Set<IClass>>();
            for (IClass cls : ontology.getClass(COMPOUND_ANCHOR).getSubClasses()) {
                // get all possible component classes
                Set<IClass> possibleComponents = new LinkedHashSet<IClass>();
                for (IRestriction r : cls.getRestrictions(ontology.getProperty(HAS_COMPOUND_ARGUMENT))) {
                    possibleComponents.addAll(getContainedClasses(r.getParameter()));
                }
                //
                compoundAnchorMap.put(cls, possibleComponents);
            }
        }
        return compoundAnchorMap;
    }


    /**
     * get all classes contained in a given expression
     *
     * @param exp - logical expression
     * @return list of classes
     */
    public List<IClass> getContainedClasses(ILogicExpression exp) {
        List<IClass> classes = new ArrayList<IClass>();
        for (Object o : exp) {
            if (o instanceof IClass) {
                classes.add((IClass) o);
            } else if (o instanceof ILogicExpression) {
                classes.addAll(getContainedClasses((ILogicExpression) o));
            }
        }
        return classes;
    }

    /**
     * get all classes contained in a given expression
     *
     * @param rr set of restrictions
     * @return list of classes
     */
    public List<IClass> getContainedClasses(IRestriction[] rr) {
        List<IClass> classes = new ArrayList<IClass>();
        for (IRestriction r : rr) {
            classes.addAll(getContainedClasses(r.getParameter()));
        }
        return classes;
    }


    /**
     * get modifier target validator to check if modifier can be attached to target
     *
     * @return modifier validator
     */
    public ConText.ModifierResolver getModifierResolver() {
        if (modifierResolver == null) {
            modifierResolver = new ConText.ModifierResolver() {

                public boolean isModifierApplicable(Mention modifier, Mention target) {
                    // get an annotation class for this target
                    if (target.getConcept().getRelationMap().containsKey(IS_ANCHOR_OF)) {
                        for (String annotoationName : target.getConcept().getRelationMap().get(IS_ANCHOR_OF)) {
                            IClass annotationCls = ontology.getClass(annotoationName);
                            if (annotationCls != null) {
                                for (IRestriction r : getRestrictions(annotationCls)) {
                                    String inverseProp = getInversePropertyName(r.getProperty().getName());
                                    // if we got an inverse property, awesome lets look if they match
                                    if (modifier.getConcept().getRelationMap().containsKey(inverseProp)) {
                                        for (String domainName : modifier.getConcept().getRelationMap().get(inverseProp)) {
                                            IClass domainCls = ontology.getClass(domainName);
                                            if (domainCls != null && domainCls.evaluate(annotationCls)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // check if the target explicitly defines a relationship
                    IClass modifierCls = getConceptClass(modifier);
                    for (String propName : target.getConcept().getRelationMap().keySet()) {
                        IProperty prop = ontology.getProperty(propName);
                        if (prop != null && isPropertyRangeSatisfied(prop, modifierCls)) {
                            return true;
                        }
                    }
                    return false;
                }

                /**
                 * process numeric modifiers to get
                 */
                public void processNumericModifiers(Sentence sentence) {
                    //new ArrayList<Mention>(
                    for (Mention m : sentence.getMentions()) {
                        IClass modifierCls = getConceptClass(m);
                        if (isTypeOf(modifierCls, NUMERIC_MODIFER)) {
                            // parse numeric component
                            List<Double> numbers = parseNumbers(m);

                            // skip if there are no numbers
                            if (numbers.isEmpty())
                                continue;


                            // add appropriate fields
                            if (isTypeOf(modifierCls, QUANTITY)) {
                                if (numbers.size() > 0) {
                                    m.addModifier(Modifier.getModifier(HAS_QUANTITY_VALUE, "" + numbers.get(0)));
                                }
                            } else if (isTypeOf(modifierCls, RATIO)) {
                                if (numbers.size() > 1) {
                                    m.addModifier(Modifier.getModifier(HAS_NUMERATOR_VALUE, "" + numbers.get(0)));
                                    m.addModifier(Modifier.getModifier(HAS_DENOMINATOR_VALUE, "" + numbers.get(1)));
                                }

                            } else if (isTypeOf(modifierCls, RANGE)) {
                                if (numbers.size() > 1) {
                                    m.addModifier(Modifier.getModifier(HAS_LOW_VALUE, "" + numbers.get(0)));
                                    m.addModifier(Modifier.getModifier(HAS_HIGH_VALUE, "" + numbers.get(1)));
                                }
                            } else if (isTypeOf(modifierCls, DIMENSIONAL_MEASUREMENT)) {
                                int i = 1;
                                for (Double d : numbers) {
                                    m.addModifier(Modifier.getModifier("has" + (i++) + DIMENSION_VALUE, "" + d));
                                }
                            }

                        }
                    }
                }

            };
        }
        return modifierResolver;
    }

    /**
     * extract numbers from a mention
     *
     * @param m - mention
     * @return list of numbers
     */
    private List<Double> parseNumbers(Mention m) {
        // first see if the number is defined in the mention
        String num = m.getConcept().getProperty(HAS_QUANTITY_VALUE);
        if (num != null && num.matches(TextTools.NUMBER_PATTERN)) {
            return Collections.singletonList(new Double(num));
        }
        return TextTools.parseNumericValues(m.getText());
    }


    public IProperty getProperty(String name) {
        IProperty prop = ontology.getProperty(name);
        if (prop == null)
            prop = ontology.getProperty("has" + name);
        return prop;
    }

    private Map<IClass, Set<IProperty>> propertyRanges;

    private Map<IClass, Set<IProperty>> getPropertyRanges() {
        if (propertyRanges == null) {
            propertyRanges = new HashMap<IClass, Set<IProperty>>();
            for (IProperty p : ontology.getProperty(HAS_MODIFIER).getSubProperties()) {
                for (Object o : p.getRange()) {
                    if (o instanceof IClass) {
                        IClass c = (IClass) o;
                        put(propertyRanges, c, p);
                        for (IClass cc : c.getSubClasses()) {
                            put(propertyRanges, cc, p);
                        }
                    }
                }
            }
        }
        return propertyRanges;
    }

    private void put(Map<IClass, Set<IProperty>> map, IClass key, IProperty val) {
        Set<IProperty> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<IProperty>();
            map.put(key, set);
        }
        set.add(val);
    }

    /**
     * get property for a given modifier
     *
     * @param m - modifier
     * @return set of IProperty objects
     */
    public Set<IProperty> getProperties(Modifier m) {
        IProperty prop = getProperty(m.getType());
        if (prop == null) {
            IClass cls = ontology.getClass(m.getType());
            if (cls != null && getPropertyRanges().containsKey(cls)) {
                return getPropertyRanges().get(cls);
            }
        }
        return Collections.singleton(prop);
    }


    /**
     * get a property that a given mention can be related as
     *
     * @param cls - a target instance
     * @param m   - modifier
     * @return related property
     */
    public IProperty getRelatedProperty(IClass cls, Modifier m) {
        IClass mod = getConceptClass(m);
        for (IRestriction r : getRestrictions(cls)) {
            if (isPropertyRangeSatisfied(r.getProperty(), mod)) {
                return r.getProperty();
            }
        }
        return null;
    }


    /**
     * get specific instances tied to a given numeric class
     *
     * @param cls - class
     * @return list of IInstance objects
     */
    public List<IInstance> getSpecificInstances(IClass cls) {
        if (classInstanceMap == null) {
            classInstanceMap = new HashMap<IClass, List<IInstance>>();
        }

        List<IInstance> list = classInstanceMap.get(cls);
        if (list == null) {
            list = new ArrayList<IInstance>();
            // now see if we can get a more specific class added
            for (IClass specificNum : cls.getSubClasses()) {
                // if we have equivalence restrictions defined, lets see if we get this
                // class to be satisfied
                if (!specificNum.getEquivalentRestrictions().isEmpty()) {
                    IInstance inst = specificNum.createInstance(specificNum.getName() + EVAL_INSTANCE_SUFFIX);
                    list.add(inst);
                }
            }
            classInstanceMap.put(cls, list);
        }
        return list;
    }

    /**
     * get a list o f numeric units among the mentions
     * @param number - number mentions
     * @param mentions - all sentence mentions
     * @return list of units that follow that number
     *
    private List<Mention> getNumericUnits(Mention number,Sentence sentence) {
    List<Mention> list = new ArrayList<Mention>();
    for(Mention m: sentence.getMentions()){
    // if mention is after number, it is a unit and it is within window
    if(m.after(number) && isTypeOf(m,UNIT) && getWordDistance(number,m) < 5){
    list.add(m);
    break;
    }
    }
    return list;
    }
     */

    /**
     * get word distance between two mentions
     * @param a
     * @param b
     * @return
     *
    private int getWordDistance(Mention a, Mention b) {
    Sentence s = a.getSentence();
    String text = s.getText().substring(a.getEndPosition()-s.getOffset(),b.getStartPosition()-s.getOffset());
    return TextTools.getWords(text).size();
    }
     */

    /**
     * get a modifier concept object from a given class
     *
     * @param parentCls class
     * @return concept from terminology
     */
    public Concept getModifierConcept(IClass parentCls) {
        try {
            IInstance inst = null;
            for (IInstance i : parentCls.getDirectInstances()) {
                if (!i.getName().endsWith(EVAL_INSTANCE_SUFFIX)) {
                    inst = i;
                    break;
                }
            }
            Concept c = null;
            if (inst != null)
                c = getModifierTerminology().lookupConcept(inst.getName());
            if (c == null)
                c = getModifierTerminology().lookupConcept(parentCls.getName());
            return c;
        } catch (TerminologyException e) {
            throw new TerminologyError("Unable to find concept for class " + parentCls, e);
        }
    }

    /**
     * get modifier from class and a more general mention
     *
     * @param cls     - class
     * @param mention - mention
     * @return mention object
     */
    public Mention getModifierFromClass(IClass cls, Mention mention) {
        // try to find an instance from class
        Concept concept = getModifierConcept(cls).clone();
        concept.setSearchString(mention.getConcept().getSearchString());
        concept.setMatchedTerm(mention.getConcept().getMatchedTerm());
        concept.setTerminology(mention.getConcept().getTerminology());

        Mention m = new Mention();
        m.setConcept(concept);
        m.setAnnotations(mention.getAnnotations());
        m.addModifiers(mention.getModifierMap());
        m.setSentence(mention.getSentence());

        return m;
    }

    /**
     * is property range satisfied with a given class?
     *
     * @param prop - property in question
     * @param cls  - class in question
     * @return true or false
     */
    public boolean isPropertyRangeSatisfied(IProperty prop, IClass cls) {
        if (cls == null)
            return false;
        LogicExpression exp = new LogicExpression(ILogicExpression.OR, prop.getRange());
        return exp.evaluate(cls);
    }

    /**
     * is property range satisfied with a given class?
     *
     * @param prop- property in question
     * @param inst  - instance in question
     * @return true or false
     */
    public boolean isPropertyRangeSatisfied(IProperty prop, IInstance inst) {
        if (inst == null)
            return false;
        LogicExpression exp = new LogicExpression(ILogicExpression.OR, prop.getRange());
        return exp.evaluate(inst);
    }

    /**
     * is property range satisfied with a given class?
     *
     * @param prop- property in question
     * @param num   - number in question
     * @return true or false
     */
    public boolean isPropertyRangeSatisfied(IProperty prop, Number num) {
        if (num == null)
            return false;
        LogicExpression exp = new LogicExpression(ILogicExpression.OR, prop.getRange());
        return exp.evaluate(num);
    }


    public Set<IProperty> getProperties(IClass cls) {
        Set<IProperty> props = new HashSet<IProperty>();
        for (IRestriction r : getRestrictions(cls)) {
            props.add(r.getProperty());
        }
        return props;
    }


    /**
     * get all restrictions equivalent and necessary as a flat list
     *
     * @param cls - class in question
     * @return get all restrictions for a class
     */
    public List<IRestriction> getRestrictions(IClass cls) {
        List<IRestriction> list = new ArrayList<IRestriction>();
        for (ILogicExpression exp : Arrays.asList(cls.getEquivalentRestrictions(), cls.getNecessaryRestrictions())) {
            list.addAll(getRestrictions(exp));
        }
        return list;
    }

    /**
     * get all restrictions equivalent and necessary as a flat list
     *
     * @param exp - expression in question
     * @return get all restrictions for a class
     */
    public List<IRestriction> getRestrictions(ILogicExpression exp) {
        List<IRestriction> list = new ArrayList<IRestriction>();
        for (Object obj : exp) {
            if (obj instanceof IRestriction) {
                list.add((IRestriction) obj);
            }
        }
        return list;
    }


    /**
     * get a list of annotation variables that can be associated with a given anchor
     *
     * @param anchor - that may be related to variables
     * @return list of annotation variables
     */
    public List<AnnotationVariable> getAnnotationVariables(Instance anchor) {
        List<AnnotationVariable> list = new ArrayList<AnnotationVariable>();
        // find annotations that anchor points to
        IProperty isAnchorOf = ontology.getProperty(IS_ANCHOR_OF);
        for (IClass annotation : getContainedClasses(anchor.getConceptClass().getRestrictions(isAnchorOf))) {
            list.add(new AnnotationVariable(annotation, anchor));
        }
        return list;
    }

    /**
     * does a given class relate to another class via a property
     *
     * @param cls  - a class that we are testing
     * @param prop - a property that this class is related to another class
     * @param comp - a component class
     * @return true or false
     */
    private boolean hasDefinedRelation(IClass cls, IProperty prop, IClass comp) {
        for (IRestriction r : cls.getRestrictions(prop)) {
            if (r.getParameter().evaluate(comp)) {
                return true;
            }
        }
        return false;
    }


    /**
     * create usable and unique instance name
     *
     * @param cls in question
     * @return unique instnace name
     */
    public static String createInstanceName(IClass cls) {
        //return cls.getName()+"-"+System.currentTimeMillis()+"-"+((int)(Math.random()*1000));
        return cls.getName() + "-" + (instanceCounter++);
    }

    /**
     * get or create default instance from a class
     *
     * @param cls - which instance to return
     * @return instance to be returned
     */
    public static IInstance getDefaultInstance(IClass cls) {
        IOntology ontology = cls.getOntology();
        String name = cls.getName() + "_Instance";
        IInstance instance = ontology.getInstance(name);
        if (instance == null)
            instance = cls.createInstance(name);
        return instance;
    }


    /**
     * create a unique instance of a given class with sensible name
     *
     * @param cls - from which to create instance
     * @return instance to be returned
     */
    public static IInstance createInstance(IClass cls) {
        return cls.createInstance(createInstanceName(cls));
    }


    /**
     * ontology name
     *
     * @return name of ontology
     */
    public String toString() {
        return ontology.getName();
    }


    /**
     * output file to write the ontology as
     *
     * @param outputFile - that we want to save to
     * @throws IOntologyException    - exception
     * @throws FileNotFoundException - exception
     */
    public void write(File outputFile) throws FileNotFoundException, IOntologyException {
        ontology.write(new FileOutputStream(outputFile), IOntology.OWL_FORMAT);
    }

    /**
     * ontology name
     *
     * @return name of ontology
     */
    public String getName() {
        return ontology.getName();
    }


    /**
     * get default values map.
     *
     * @return the default values
     */
    public Map<String, String> getDefaultValues() {
        if (defaultValues == null) {
            defaultValues = new LinkedHashMap<String, String>();

            // go over ALL modifiers
            for (IClass cls : ontology.getClass(MODIFIER).getSubClasses()) {
                if (ConTextHelper.isDefaultValue(cls)) {
                    for (IClass parent : cls.getDirectSuperClasses()) {
                        defaultValues.put(parent.getName(), cls.getName());
                    }
                }
            }
        }
        return defaultValues;
    }

    /**
     * is modifier1 better specified as modifier2, that is has more modifiers or more specific.
     *
     * @param modifier1 - first modifier
     * @param modifier2 - second modifier
     * @return true or false
     */
    public boolean isBetterSpecified(Modifier modifier1, Modifier modifier2) {
        IClass mod1 = getConceptClass(modifier1);
        IClass mod2 = getConceptClass(modifier2);
        if (mod1 != null && mod2 != null) {
            // if two modifiers are the same, then
            // check based on other modifiers
            if (mod1.equals(mod2)) {
				/*	Map<String,List<Modifier>> mp1 = modifier1.getMention().getModifierMap();
				Map<String,List<Modifier>> mp2 = modifier2.getMention().getModifierMap();
				
				// make sure, that there is no huge different in attributes
				// Ex: left breast 1:30 o'cloc vs right breast
				for(String type: mp1.keySet()){
					if(mp2.containsKey(type)){
						if(!mp1.get(type).getValue().equals(mp2.get(type).getValue())){
							return false;
						}
					}
				}

				return mp1.size() > mp2.size();*/

                // make sure, that there is no huge different in attributes
                // Ex: left breast 1:30 o'cloc vs right breast
                Mention m1 = modifier1.getMention();
                Mention m2 = modifier2.getMention();
                for (String type : m1.getModifierTypes()) {
                    List<String> values = m2.getModifierValues(type);
                    if (!values.isEmpty() && !m1.getModifierValues(type).containsAll(values)) {
                        return false;
                    }
                }
                return m1.getModifierTypes().size() > m2.getModifierTypes().size();

                // if modifier1 is more specific, it is better specified
            } else if (mod1.hasSuperClass(mod2)) {
                return true;
                // if modifier1 is non-default, it is better then default one
            } else if (!modifier1.isDefaultValue() && modifier2.isDefaultValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * does this variable has a equivalent relation to another annotation
     *
     * @param var - annotation variable
     * @return true or false
     */
    public boolean hasDefiningRelatedVariable(AnnotationVariable var) {
        IClass annotation = ontology.getClass(ANNOTATION);
        for (Object o : var.getConceptClass().getEquivalentRestrictions()) {
            if (o instanceof IRestriction) {
                IRestriction r = (IRestriction) o;
                for (IClass cls : getContainedClasses(r.getParameter())) {
                    if (cls.hasSuperClass(annotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * find nearest mention of related variables by relationship
     *
     * @param var       - variable in question
     * @param variables - list of variables in the document
     * @return relation to instance map
     */
    public Map<String, Instance> getRelatedVariables(AnnotationVariable var, List<AnnotationVariable> variables) {
        Map<String, Instance> map = new LinkedHashMap<String, Instance>();
        Document doc = var.getMention().getSentence().getDocument();
        IClass cls = var.getConceptClass();
        Map<String, Set<IClass>> relatedAnnotations = getRelatedAnnotations(cls);
        if (relatedAnnotations.isEmpty())
            return map;

        // create vraiable lists
        List<AnnotationVariable> before = new Stack<AnnotationVariable>();
        List<AnnotationVariable> after = new ArrayList<AnnotationVariable>();

        // get the span that constraints the relationship
        Spannable span = null;
        if (PARAGRAPH_SCOPE.equals(getAnnotatioRelationSkope()))
            span = var.getMention().getSentence().getParagraph();
        else if (SECTION_SCOPE.equals(getAnnotatioRelationSkope()))
            span = var.getMention().getSentence().getSection();

        // default to the entire document
        if (span == null) {
            // if you can't figure out this out, don't do anything
            //span = var.getMention().getSentence().getDocument();
            return Collections.EMPTY_MAP;
        }

        // create a before and after list
        for (AnnotationVariable v : variables) {
            if (span.contains(v.getMention())) {
                if (v.getMention().before(var.getMention())) {
                    before.add(v);
                } else if (v.getMention().after(var.getMention())) {
                    after.add(v);
                }
            }
        }

        // go over relations that apply
        for (String relation : relatedAnnotations.keySet()) {
            // go over  both candidate lists: before and after
            for (List<AnnotationVariable> list : Arrays.asList(before, after)) {
                // for each candidate in a respective list
                for (AnnotationVariable candidate : list) {
                    // if this annotation variable satisfies the relation
                    if (isSatisfiable(candidate.getConceptClass(), relatedAnnotations.get(relation))) {
                        // get previous instance
                        Instance oldInstance = map.get(relation);
                        if (oldInstance == null) {
                            map.put(relation, candidate);
                        } else {
                            // if new candidate is closer then the old one
                            // replace the old one
                            int oWC = Text.getWordDistance(doc, oldInstance.getMention(), var.getMention());
                            int nWC = Text.getWordDistance(doc, candidate.getMention(), var.getMention());

                            // if word distance of the new candidate
                            // is smaller, then replace it
                            if (nWC < oWC) {
                                map.put(relation, candidate);
                            }
                        }
                        break;
                    }
                }
            }
        }
        //}
        return map;
    }

    /**
     * is the class satisfiable based on the list of candidates
     * the class needs to be equal or more specific then any of the classes
     *
     * @param cls        - class in question
     * @param candidates - list of candidates
     * @return true if class if equal or more specific
     */
    public static boolean isSatisfiable(IClass cls, Set<IClass> candidates) {
        for (IClass c : candidates) {
            if (c.equals(cls) || c.hasSubClass(cls))
                return true;
        }
        return false;
    }


    /**
     * get a mapping of relations and classes that are related annotations
     *
     * @param cls - class in question
     * @return map -  a map of relation per related classes
     */

    private Map<String, Set<IClass>> getRelatedAnnotations(IClass cls) {
        IClass annotation = cls.getOntology().getClass(ANNOTATION);
        IProperty hasRelation = cls.getOntology().getProperty(HAS_RELATION);

        Map<String, Set<IClass>> relatedAnnotations = new HashMap<String, Set<IClass>>();

        // go through equivalent, then direct, the all necessary restrictions
        for (ILogicExpression exp : Arrays.asList(
                cls.getEquivalentRestrictions(),
                cls.getDirectNecessaryRestrictions(),
                cls.getNecessaryRestrictions())) {
            for (IRestriction r : getRestrictions(exp)) {
                IProperty p = r.getProperty();
                // make sure property restriction is also legit property
                if (hasRelation.hasSubProperty(p)) {
                    // only associate annotations, when this property was not added prior
                    // this assures that equivalent property overrides the direct,
                    // then all necessary restrictions
                    if (!relatedAnnotations.containsKey(p.getName())) {
                        Set<IClass> list = new LinkedHashSet<IClass>();
                        for (IClass c : getContainedClasses(r.getParameter())) {
                            // if a class is an annotation
                            if (c.hasSuperClass(annotation)) {
                                list.add(c);
                            }
                        }
                        relatedAnnotations.put(p.getName(), list);
                    }
                }
            }
        }
        return relatedAnnotations;
    }

    /**
     * is the property the same as modifier type
     *
     * @param prop     - property
     * @param modifier - modifier
     * @return true or false
     */
    public boolean isSameProperty(IProperty prop, Modifier modifier) {
        System.out.println(prop.getName() + " " + modifier.getType() + " " + prop.getName().endsWith(modifier.getType()));
        return prop.getName().endsWith(modifier.getType());
    }
}
