package edu.pitt.dbmi.nlp.noble.tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Class SynopticReportDetector.
 */
public class SynopticReportDetector {
	private static List<Detector> synopticDetectors,falseDetectors;
	private List<String> emptyFiles  = new ArrayList<String>();
	private int total,empty,gross,prostate;
	
	/**
	 * The Interface Detector.
	 */
	public static interface Detector {
		
		/**
		 * Detect.
		 *
		 * @param line the line
		 * @return true, if successful
		 */
		public boolean detect(String line);
	}
	
	/**
	 * get a list of synoptic line detectors.
	 *
	 * @return the synoptic detectors
	 */
	private static List<Detector> getSynopticDetectors(){
		if(synopticDetectors == null){
			synopticDetectors = new ArrayList<Detector>();
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// if line starts with letter and space and line, then take the latter portion
					//Matcher mt =  Pattern.compile("[a-z0A-Z0-9]{1,2}\\.?[\\s-]+(.*)").matcher(line);
					//if(mt.matches())
					//	line = mt.group(1);
					// detect white _ or . gaps >=4 between words
					Matcher mt = Pattern.compile("[^\\s]+[\\._]{4,}[^\\s]+").matcher(line);
					return mt.find();
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// detect empty click cells s.a. ( )
					Matcher mt = Pattern.compile("\\(\\s*\\)").matcher(line);
					return mt.find();
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// detect tabs 
					Matcher mt = Pattern.compile("[^\\s]+[\\t]{1,}[^\\s]+").matcher(line);
					return mt.find();
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// does the line start with a word synoptic
					Matcher mt = Pattern.compile("^\\s*synoptic\\b").matcher(line.toLowerCase());
					return mt.find();
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// do we have ___ or _X_ lines representing worksheets?
					Matcher mt = Pattern.compile("^_[xX_]_").matcher(line);
					return mt.find();
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// do we have lines that start with a single letter or number end end with digits or hash
					return line.matches("^[A-Z0-9]\\.\\s*.*:\\s*(\\d|#)$");
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// do we have a line that has a set of upper case words, followed by column and at least 4 spaces and then other characters
					if(line.matches("^[A-Za-z ,'\\(\\)]+:\\s{4,}.*$")){
						// skip known false positives
						return !line.matches("^(PROCEDURE|POST-OP).*");
					}
					return false;
				}
			});
			synopticDetectors.add(new Detector(){
				public boolean detect(String line){
					// if line starts with letter and space and line, then take the latter portion
					Matcher mt =  Pattern.compile("[a-z0A-Z0-9]{1,2}\\.?[\\s-]+(.*)").matcher(line);
					if(mt.matches())
						line = mt.group(1);
					// detect white space or . gaps >=4 between words
					mt = Pattern.compile("[^\\s]+[\\s]{4,}[^\\s]+").matcher(line);
					if(mt.find()){
						// if alphabetical characters are less then 60% of the total, then we are good and not a false positive
						return (double)line.replaceAll("[^A-Za-z]","").length()/line.length() < 0.6;
					}
					return false;
				}
			});
			
		}
		return synopticDetectors;
	}
	
	/**
	 * get a list of synoptic line detectors.
	 *
	 * @return the false detectors
	 */
	private static List<Detector> getFalseDetectors(){
		if(falseDetectors == null){
			falseDetectors = new ArrayList<Detector>();
			falseDetectors.add(new Detector(){
				// skip line dividers 
				public boolean detect(String line){
					return line.trim().matches("[_\\-=]+");
				}
			});
		}
		return falseDetectors;
	}
	
	
	/**
	 * does line belong to synoptic report?.
	 *
	 * @param line the line
	 * @return true, if successful
	 */
	public static boolean detect(String line){
		// if synoptic detector fired
		if(detect(line,getSynopticDetectors())){
			// check known false positives
			return (detect(line,getFalseDetectors()))?false:true;
			//return true;
		}
		return false;
	}
	
	/**
	 * does line belong to synoptic report?.
	 *
	 * @param line the line
	 * @param list the list
	 * @return true, if successful
	 */
	private static boolean detect(String line,List<Detector> list){
		for(Detector d: list){
			if(d.detect(line))
				return true;
		}
		return false;
	}
	
	
	/**
	 * process document.
	 *
	 * @param f the f
	 * @throws Exception the exception
	 */
	public void process(File f) throws Exception {
		if(f.isDirectory()){
			for(File c: f.listFiles()){
				process(c);
			}
		}else if(f.getName().endsWith(".txt")){
			System.out.println(f.getName()+"\n---------------------------");
			String tx = getText(f);
			String rp = getSynopticReport(tx);
			total++;
			if(rp.length()== 0){
				empty++;
				if(tx.contains("SYNOPTIC-  PRIMARY PROSTATE TUMORS")){
					prostate++;
				}
				emptyFiles.add(f.getName());
			}
			if(rp.contains("[Gross Description]"))
				gross++;
			System.out.println(rp);
		}
	}
	
	/**
	 * Gets the text.
	 *
	 * @param f the f
	 * @return the text
	 * @throws Exception the exception
	 */
	public String getText(File f) throws Exception {
		BufferedReader r = new BufferedReader(new FileReader(f));
		StringBuffer b = new StringBuffer();
		for(String l=r.readLine();l != null; l=r.readLine()){
			b.append(l+"\n");
		}
		return b.toString();
	}
	
	/**
	 * Gets the synoptic report range.
	 *
	 * @param text the text
	 * @return the synoptic report range
	 */
	public int[] getSynopticReportRange(String text) {
		int st = -1, en = -1, offs = 0;
		int dcount = 0;
		for(String l: text.split("\n")){
			if(detect(l)){
				// if detected line, put starte offset
				if(st < 0)
					st = offs;
				//carry on with end offset
				en = offs+l.length()+1;
				// inclrement line count
				dcount ++;
			}else if(l.trim().length() > 0){
				//if not empty line
				
				// if we have a start, but there are less then 2 lines
				// reset count
				if(st > -1 && dcount < 2){
					st = -1;
					dcount = 0;
				}
				
				// if we get into another section heading
				// we overshot it, so quit
				if(st > -1 && (l.matches("^\\[[\\w ]+\\]$") || l.matches("^[A-Z\\- ]{4,20}:$"))){
					break;
				}
				
			}
			offs += l.length()+1;
		}
		int[] offsets = new int[2];
		if (st > 0 && dcount > 1) {
			offsets[0] = st;
			offsets[1] = en;
		}
		else {
			offsets[0] = -1;
			offsets[1] = -1;
		}
		return offsets;
	}
	
	
	/**
	 * process document.
	 *
	 * @param text the text
	 * @return the synoptic report
	 */
	public String getSynopticReport(String text){
		int[] offsets = getSynopticReportRange(text);
		int st = offsets[0];
		int en = offsets[1];
		return (st > 0 && en > 0) ? text.substring(st,en) : "";
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		//args = new String [] {"/home/tseytlin/Data/Reports/SynopticReportsMany"};
		args = new String [] {"/home/tseytlin/Data/DeepPhe/Datasets/GOLD/reports/patient02/patient02_report026_SP.txt"};
		//args = new String [] {"/home/tseytlin/Data/Reports/SynopticReports"};
		//args = new String [] {"/home/tseytlin/Data/Reports/SynopticReports","/home/tseytlin/Data/Reports/TIES Reports","/home/tseytlin/Data/Reports/SynopticReportsMany"};
		//args = new String [] {"/home/tseytlin/Data/Reports/TIES Reports"};
		SynopticReportDetector srd = new SynopticReportDetector();
		for(String a: args){
			srd.process(new File(a));
		}
		
		System.out.println("\n\nTotal: "+srd.total+"\nEmpty: "+srd.empty+"\nGross: "+srd.gross+"\nProstate: "+srd.prostate);
		System.out.println(srd.emptyFiles);
	}

}
