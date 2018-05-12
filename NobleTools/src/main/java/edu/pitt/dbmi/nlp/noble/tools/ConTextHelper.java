package edu.pitt.dbmi.nlp.noble.tools;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Relation;
import edu.pitt.dbmi.nlp.noble.terminology.SemanticType;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

import java.util.*;

import static edu.pitt.dbmi.nlp.noble.tools.ConText.*;

/**
 * helper methods for context alogirthm
 * @author tseytlin
 */

public class ConTextHelper {
	
	/**
	 * Checks if is modifier type.
	 *
	 * @param cls the cls
	 * @return true, if is modifier type
	 */
	private static  boolean isSemanticType(IClass cls){
		// if defined in context or schema, then it is SemType
		if(cls.getURI().toString().matches(".*("+CONTEXT_OWL+"|"+SCHEMA_OWL+").*")  && !cls.getName().contains("_"))
			return true;
		// else if direct parent is modifier modifier
		for(IClass p: cls.getDirectSuperClasses()){
			if(Arrays.asList(ConText.LINGUISTIC_MODIFIER,ConText.SEMANTIC_MODIFIER,ConText.NUMERIC_MODIFIER,ConText.QUALIFIER).contains(p.getName()))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if is default value.
	 *
	 * @param cls the cls
	 * @return true, if is default value
	 */
	public static boolean isDefaultValue(IClass cls){
		for(Object o: cls.getDirectNecessaryRestrictions()){
			if(o instanceof IRestriction){
				IRestriction r = (IRestriction) o;
				if(PROP_IS_DEFAULT_VALUE.equals(r.getProperty().getName())){
					for(Object v: r.getParameter()){
						return Boolean.parseBoolean(v.toString());
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Adds the concept.
	 *
	 * @param cls the cls
	 * @return the concept
	 * @throws TerminologyException the terminology exception
	 */
	public static Concept createConcept(IClass cls) throws TerminologyException{
		Concept concept = cls.getConcept();
		//overwrite URI, with name
		concept.setCode(cls.getName());
		
		// add semantic type
		for(SemanticType st: getSemanticTypes(cls))
			concept.addSemanticType(st);
	
		// if we actually have synonyms defined beyound the name,it should be treated as an instance
		if(concept.getSynonyms().length > 1)
			concept.addSemanticType(SemanticType.getSemanticType(SEMTYPE_INSTANCE));
		
		
		// add relations to concept
		for(IClass c: cls.getDirectSuperClasses()){
			concept.addRelatedConcept(Relation.BROADER,c.getName());
		}
		
		// add relations to concept
		for(IClass c: cls.getDirectSubClasses()){
			concept.addRelatedConcept(Relation.NARROWER,c.getName());
			
			// get the default value for this type
			if(isSemanticType(cls)){
				if(isDefaultValue(c)){
					concept.addProperty(PROP_HAS_DEFAULT_VALUE,c.getName());
				}
			}
		}
		
		// add relations to concept
		for(IInstance c: cls.getDirectInstances()){
			concept.addRelatedConcept(Relation.NARROWER,c.getName());
		}
		
		// add other properties defined in ConText to concept properties
		for(IProperty prop: cls.getProperties()){
			if(prop.getURI().toString().contains(CONTEXT_OWL)){
				Object o = cls.getPropertyValue(prop);
				if(o != null){
					concept.addProperty(prop.getName(),""+o);
				}
			}
		}
		
		// add other properties
		for(Object o: cls.getDirectNecessaryRestrictions()){
			if(o instanceof IRestriction){
				IRestriction r = (IRestriction) o;
				for(Object v: r.getParameter()){
					if(!(v instanceof IClass)){
						concept.addProperty(r.getProperty().getName(),""+v);
					}
				}
			}
		}
		
		
		// add other relations to a concept
		for(Object o: cls.getNecessaryRestrictions()){
			if(o instanceof IRestriction){
				IRestriction r = (IRestriction) o;
				for(Object v: r.getParameter()){
					if(v instanceof IClass){
						concept.addRelatedConcept(Relation.getRelation(r.getProperty().getName()), ((IClass)v).getName());
					}
				}
			}
		}
		
		return concept;
	}
	

	/**
	 * Checks if is root instance.
	 *
	 * @param inst the inst
	 * @return true, if is root instance
	 */
	private static boolean isRootInstance(IInstance inst) {
		for(IClass c: inst.getDirectTypes()){
			if(CONTEXT_ROOTS.contains(c.getName()))
				return true;
		}
		return false;
	}

	


	/**
	 * get modifier value.
	 *
	 * @param type the type
	 * @param c the c
	 * @return the modifier value
	 */
	private static String getModifierValue(String type, IClass c){
		IClass typeCls = c.getOntology().getClass(type);
		// if the type is a direct superclass, no problem, just return it
		if(c.hasDirectSuperClass(typeCls)){
			return c.getName();
		}
		// else, if we got some nested "polarity" shit going on, go over parents
		for(IClass p: getEquivalentClasses(c)){
			if(p.hasDirectSuperClass(typeCls))
				return p.getName();
		}
		

		// should never, be here, but make this a default
		return c.getName();
	}
	
	private static Set<IClass> getEquivalentClasses(IClass c){
		Set<IClass> list = new HashSet<IClass>();
		for(IClass p: c.getEquivalentClasses()){
			if(!p.equals(c))
				list.add(p);
		}
		for(Object o: c.getEquivalentRestrictions()){
			if(o instanceof ILogicExpression){
				for(Object oo: ((ILogicExpression)o)){
					if(oo instanceof IClass)
						list.add((IClass)oo);
				}
			}
		}
		return list;
	}
	
	
	/**
	 * Adds the concept.
	 *
	 * @param inst the inst
	 * @return the concept
	 * @throws TerminologyException the terminology exception
	 */
	public static Concept createConcept(IInstance inst) throws TerminologyException {
		Concept concept = new Concept(inst);
		concept.setCode(inst.getName());
		if(!isRootInstance(inst))
			concept.addSemanticType(SemanticType.getSemanticType(SEMTYPE_INSTANCE));
		
		
		// add relations to concept
		for(IClass c: inst.getDirectTypes()){
			for(SemanticType st: getSemanticTypes(c)){
				concept.addSemanticType(st);
				concept.addProperty(st.getName(),getModifierValue(st.getName(),c));
			}
			concept.addRelatedConcept(Relation.BROADER,c.getName());
			
			// add default value if parent is default
			if(isDefaultValue(c)){
				concept.addProperty(PROP_IS_DEFAULT_VALUE,"true");
			}
		}
		
		// add other relations to concept
		for(IProperty p:  inst.getProperties()){
			for(Object o: inst.getPropertyValues(p)){
				if(o instanceof IResource){
					concept.addProperty(p.getName(),((IResource)o).getName());
				}else{
					concept.addProperty(p.getName(),o.toString());
				}
			}
		}
		
		for(IClass cls: inst.getDirectTypes()){
			// add other relations to a concept
			for(Object o: cls.getNecessaryRestrictions()){
				if(o instanceof IRestriction){
					IRestriction r = (IRestriction) o;
					for(Object v: r.getParameter()){
						if(v instanceof IClass){
							concept.addRelatedConcept(Relation.getRelation(r.getProperty().getName()), ((IClass)v).getName());
						}
					}
				}
			}
		}
		
		
		return concept;
	}
	
	
	/**
	 * Gets the semantic types.
	 *
	 * @param cls the cls
	 * @return the semantic types
	 */
	private static Set<SemanticType> getSemanticTypes(IClass cls) {
		Set<SemanticType> semTypes = new LinkedHashSet<SemanticType>();
		List<IClass> parents = new ArrayList<IClass>();
		parents.add(cls);
		Collections.addAll(parents,cls.getSuperClasses());
		for(IClass c: parents){
			if(isSemanticType(c)){
				semTypes.add(SemanticType.getSemanticType(c.getName()));
			}
		}
		return semTypes;
	}
	
}
