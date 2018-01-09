package edu.pitt.dbmi.nlp.noble.tools;

import java.io.IOException;
import java.util.Arrays;

import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Modifier;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

public class ConTextTest {
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOntologyException the i ontology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static void main(String[] args) throws IOntologyException, IOException, TerminologyException {
		ConText conText = new ConText();
		
		NobleCoderTerminology terminology = new NobleCoderTerminology("nlpBreastCancer");
		terminology.setScoreConcepts(true);
		terminology.setSelectBestCandidate(true);
		terminology.setDefaultSearchMethod(NobleCoderTerminology.PRECISE_MATCH);
		terminology.setSemanticTypeFilter("Neoplastic Process; Sign or Symptom; Finding");

		for(String text: Arrays.asList(
				"There was no evidence of melanoma for this patient, but there was a family history of breast cancer.",
				"The patient presents with a 3 day history of cough.",
				"There is no significant change in lymphacitic infiltrate.",
				"The patient reports mother has had breast cancer in the past.",
				"Images show possible dysplastic nevus vs melanoma.",
				"No lytic or blastic osseous lesions are seen.",
				"Heart Trouble: No High Blood Pressure: No Integumentary Skin Cancer/Skin Condition: No Skin Lesion/Rash: No Respiratory",
				"No definite ultrasonographic correlation of the posterior focus of enhancement at 3 o'clock of the left breast."
			
				)){
			Sentence sentence = new Sentence(text);
			
			// process with regular dictionary
			terminology.process(sentence);
			
			// process with context
			conText.process(sentence);
		
			// print results
			System.out.println("sentence: "+text+" | nc: "+terminology.getProcessTime()+" | context: "+conText.getProcessTime());
			for(Mention m: sentence.getMentions()){
				Concept c = m.getConcept();
				System.out.println("\t"+c.getName()+" ("+c.getCode()+") "+Arrays.toString(c.getSemanticTypes())+" \""+
						m.getText()+"\"");
				for(String context: m.getModifierTypes()){
					//Modifier modifier = m.getModifier(context);
					//String mention = modifier.getMention() != null?"\""+modifier.getMention()+"\"":"(default)";
					System.out.println("\t\t"+context+" : "+m.getModifiers(context));
				}

			}
			
		}
		
		
		/*	// display the ConText browser
		TerminologyBrowser tb = new TerminologyBrowser();
		tb.setTerminologies(new Terminology []{terminology,conText.getTerminology()});
		tb.showDialog(null,"ConText");*/
		
	}

}
