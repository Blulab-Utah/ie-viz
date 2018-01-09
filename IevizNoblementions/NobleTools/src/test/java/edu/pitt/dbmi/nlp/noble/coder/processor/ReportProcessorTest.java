package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Paragraph;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;

public class ReportProcessorTest {
	
	public static void main(String [] args) throws Exception{
		processFiles(new File(args[0]),new File(args[1]));
	}
	
	/**
	 * process document.
	 *
	 * @param f the f
	 * @throws Exception the exception
	 */
	public static void processFiles(File f, File out) throws Exception {
		ReportProcessor rp = new ReportProcessor();
		if(f.isDirectory()){
			for(File c: f.listFiles()){
				processFiles(c,out);
			}
		}else if(f.getName().endsWith(".txt")){
			System.out.print(f.getName() +" .. ");
			Document d = rp.process(f);
			System.out.println(d.getProcessTime());
			if(!out.exists())
				out.mkdirs();
			
			// debug sections
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File(out,f.getName()+".sections.txt")));
			for(Section s: d.getSections()){
				bf.write("["+s.getTitle()+"]\n"+s.getBody().replaceAll("\n"," ")+"\n\n");
			}
			bf.close();
			
			// debug sentences
			bf = new BufferedWriter(new FileWriter(new File(out,f.getName()+".sentences.txt")));
			for(Sentence s: d.getSentences()){
				bf.write(s.getSentenceType()+"\t|\t"+s.getText().replaceAll("\n"," ").trim()+"\n");
			}
			bf.close();
			
			// debug paragraphs
			bf = new BufferedWriter(new FileWriter(new File(out,f.getName()+".paragraphs.txt")));
			for(Paragraph s: d.getParagraphs()){
				if(s.getLength() == 0)
					continue;
				String prefix = s.getPart() != null? s.getPart()+"\\t|\t":"";
				bf.write(prefix+s.getText().replaceAll("\n", " ").trim()+"\n\n");
			}
			bf.close();
			
			
		}
	}
}
