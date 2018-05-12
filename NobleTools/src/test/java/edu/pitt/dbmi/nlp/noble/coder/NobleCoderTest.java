package edu.pitt.dbmi.nlp.noble.coder;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

import java.io.File;
import java.io.PrintStream;

public class NobleCoderTest {
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		String domain = "Melanoma";
		
		NobleCoder nc = new NobleCoder("NCI_Thesaurus");
		nc.setProcessFilter(nc.getProcessFilter()|NobleCoder.FILTER_WORKSHEET);
		((NobleCoderTerminology)nc.getTerminology()).setSelectBestCandidate(true);

		File destinationFile = new File("/home/tseytlin/Data/DeepPhe/"+domain+"/sample/deid/");
		if(null != destinationFile.listFiles()){
            for(File file : destinationFile.listFiles()){
                if(file.getName().endsWith(".txt")){
                    System.out.print("processing\t"+file.getName()+"\t..\t");
                    Document doc = nc.process(file);
                    PrintStream out = new PrintStream(new File(file.getParentFile(),file.getName()+".processed"));
                    out.println(doc.getTitle());
                    out.println("---------------------------------------");
                    for(Object prop : doc.getProperties().keySet()){
                        out.println(prop+"\t->\t"+doc.getProperties().get(prop));
                    }
                    out.println("---------------------------------------");
                    for(Sentence s: doc.getSentences()){
                        String sec = s.getSection() != null?s.getSection().getTitle():"none";
                        String tm = s.getProperties().containsKey("time")?s.getProperties().get("time"):"";
                        out.println("sentence:\t|"+s.getOffset()+"|\t"+s.getSentenceType()+"|\t"+sec+"|\t"+s+"\t|"+tm);
                        //System.out.println("extracted:\t"+doc.getText().substring(s.getStartPosition(),s.getEndPosition()));
                        for(Mention m: s.getMentions()){
                            out.println("\tmention:\t"+m+" | "+m.getConcept().getCode()+" | "+m.getConcept().getName()+" | "+m.getAnnotations());
                        }
                    }
                    out.println("---------------------------------------");
                    out.println(nc.getProcessTime()+" ms");
                    out.close();
                    System.out.println(nc.getProcessTime()+" ms");
                }
            }
        }

		
	}

}
