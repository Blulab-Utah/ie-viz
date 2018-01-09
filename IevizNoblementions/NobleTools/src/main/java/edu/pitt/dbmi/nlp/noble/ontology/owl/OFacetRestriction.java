package edu.pitt.dbmi.nlp.noble.ontology.owl;

import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.vocab.OWLFacet;


public class OFacetRestriction extends OResource  {
	protected OWLFacetRestriction facetRestriction;
	private String facetPrefx = "";
	private Object facetLiteral = null;
	
	public OFacetRestriction(OWLFacetRestriction val, OOntology ontology) {
		super(val,ontology);
		facetRestriction = val;
		facetPrefx = createFacetPrefix();
		facetLiteral = convertOWLObject(facetRestriction.getFacetValue());
	}

	private String createFacetPrefix() {
		if(OWLFacet.MAX_INCLUSIVE.equals(facetRestriction.getFacet())){
			return "\u2264";
		}else if(OWLFacet.MIN_INCLUSIVE.equals(facetRestriction.getFacet())){
			return "\u2265";
		}else if(OWLFacet.MAX_EXCLUSIVE.equals(facetRestriction.getFacet())){
			return "<";
		}else if(OWLFacet.MIN_EXCLUSIVE.equals(facetRestriction.getFacet())){
			return"<";
			
		}
		return "";
	}

	public boolean evaluate(Object obj) {
		double value = 0, facetValue = 0;
		
		//conver paraeter to a number
		if(obj instanceof Number && facetLiteral instanceof Number){
			Number num = (Number)obj; 
			value = num.doubleValue();
			facetValue =  ((Number)facetLiteral).doubleValue();
			
			// if we restrict an integer and the parameter is a double, then invalidate restriction
			if(facetLiteral instanceof Integer && num.doubleValue() != num.intValue())
				return false;
			
			
			if(OWLFacet.MAX_INCLUSIVE.equals(facetRestriction.getFacet())){
				return value <= facetValue;
			}else if(OWLFacet.MIN_INCLUSIVE.equals(facetRestriction.getFacet())){
				return value >= facetValue;
			}else if(OWLFacet.MAX_EXCLUSIVE.equals(facetRestriction.getFacet())){
				return value < facetValue;
			}else if(OWLFacet.MIN_EXCLUSIVE.equals(facetRestriction.getFacet())){
				return value > facetValue;
				
			}
		}
		return false;
	}
		
	public String toString(){
		return facetPrefx+" "+facetLiteral;
	}

}
