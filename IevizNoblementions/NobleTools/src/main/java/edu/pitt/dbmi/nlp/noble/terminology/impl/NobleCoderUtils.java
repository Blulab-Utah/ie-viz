package edu.pitt.dbmi.nlp.noble.terminology.impl;

import static edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderUtils.saveWordTermsInStorage;

import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.SemanticType;
import edu.pitt.dbmi.nlp.noble.terminology.Source;
import edu.pitt.dbmi.nlp.noble.terminology.Term;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology.Storage;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology.WordStat;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.ConceptImporter;


/**
 * stand alone helper methods to unload some of the code bulk.
 *
 * @author tseytlin
 */
public class NobleCoderUtils {
	
	/**
	 * save word statistics for a given word.
	 *
	 * @param storage the storage
	 * @param termList the term list
	 * @param word the word
	 */
	public static void saveWordStats(NobleCoderTerminology.Storage storage,Set<String> termList, String word){
		int termCount = termList.size();
		
		// if word already existed, subtract previous value from the total
		if(storage.getWordStatMap().containsKey(word)){
			int oldCount = storage.getWordStatMap().get(word).termCount;
			storage.totalTermsPerWord -= oldCount;
			termCount += oldCount;
		}
		WordStat ws = new WordStat();
		ws.termCount = termCount;
		ws.isTerm = termList.contains(word);
		storage.getWordStatMap().put(word,ws);
		storage.totalTermsPerWord += termCount;
		if(termCount > storage.maxTermsPerWord)
			storage.maxTermsPerWord = termCount;
	}
	
	/**
	 * only return terms where given word occures.
	 *
	 * @param word the word
	 * @param terms the terms
	 * @return the sets the
	 */
	public static Set<String> filterTerms(String word, Set<String> terms){
		Set<String> result = new HashSet<String>();
		for(String t: terms){
			if(t.contains(word))
				result.add(t);
		}
		return result;
	}
	
	/**
	 * Singleton.
	 *
	 * @param term the term
	 * @return the sets the
	 */
	public static Set<String> singleton(String term){
		Set<String> result = new HashSet<String>();
		result.add(term);
		return result;
	}
	
	/**
	 * Gets the single word synonyms.
	 *
	 * @param synonyms the synonyms
	 * @return the single word synonyms
	 */
	public static Set<String> getSingleWordSynonyms(String [] synonyms){
		Set<String> list = new TreeSet<String>( Collections.reverseOrder());
		for(String s: synonyms){
			if(!s.contains(" ")){
				list.add(s);
			}
		}
		return list;
	}

	/**
	 * add entry to word table.
	 *
	 * @param storage the storage
	 * @param word - a given word
	 * @param termList the term list
	 */
	public static void saveWordTermsInStorage(NobleCoderTerminology.Storage storage, String word,Set<String> termList){
		if(termList.isEmpty())
			return;
		
		// add to term list stuff that is already in the hash table
		if(storage.getWordMap().containsKey(word)){
			termList.addAll(storage.getWordMap().get(word));
		}
		
		try{
			storage.getWordMap().put(word,termList);
			storage.commit(storage.getWordMap());
		}catch(IllegalArgumentException e ){
			// this is the case where the termList is too big to insert into hashtabe, there is nothing we can do frankly
			storage.getWordMap().put(word,new HashSet<String>(Collections.singleton(word)));
			//pcs.firePropertyChange(LOADING_MESSAGE,null,"Warning: failed to insert word \""+word+"\", reason: "+e.getMessage());
			
		}
	}
	
	
	/**
	 * Gets the rarest word.
	 *
	 * @param storage the storage
	 * @param term the term
	 * @return the rarest word
	 */
	public static String getRarestWord(Storage storage, String term){
		String rarest = null;
		int rarestTermCount = Integer.MAX_VALUE;
		for(String word: TextTools.getWords(term)){
			// skip one letter words
			if(word.length() <= 1)
				continue;
			
			WordStat st = storage.getWordStatMap().get(word);
			int i = (st != null)?st.termCount:Integer.MAX_VALUE;
			if( i < rarestTermCount){
				rarest = word;
				rarestTermCount = i;
			}
		}
		return rarest;
	}
	
	/**
	 * Save temporary term file.
	 *
	 * @param tempLocation the temp location
	 * @param word the word
	 * @param termList the term list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void saveTemporaryTermFile(File tempLocation, String word, Collection<String> termList) throws IOException{
		if(word == null)
			return;
		// if windows OS, check for some speccial files
		if(System.getProperty("os.name").toLowerCase().startsWith("win")){
			if(Arrays.asList("con","prn").contains(word))
				return;
		}
		
		if(!tempLocation.exists())
			tempLocation.mkdirs();
		File f = new File(tempLocation,word);
		BufferedWriter w = new BufferedWriter(new FileWriter(f,true));
		for(String t: termList){
			w.write(t+"\n");
		}
		w.close();
	}
	

	/**
	 * Removes the temporary term files.
	 *
	 * @param pcs the pcs
	 * @param tempDir the temp dir
	 */
	public static void removeTemporaryTermFiles(PropertyChangeSupport pcs, File tempDir){
		// remove temp word files 
		if(tempDir.exists()){
			pcs.firePropertyChange(ConceptImporter.LOADING_MESSAGE,null,"Deleting Temporary Files ...");
			File [] files = tempDir.listFiles();
			pcs.firePropertyChange(ConceptImporter.LOADING_TOTAL,null,files.length);
			int i=0;
			for(File f: files){
				pcs.firePropertyChange(ConceptImporter.LOADING_PROGRESS,null,i++);
				f.delete();
			}
			tempDir.delete();
		}
	}
	
	
	/**
	 * load temporary term files.
	 *
	 * @param pcs the pcs
	 * @param storage the storage
	 * @param tempDir the temp dir
	 * @param useRarestWord the use rarest word
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void loadTemporaryTermFiles(PropertyChangeSupport pcs,NobleCoderTerminology.Storage storage, File tempDir, boolean useRarestWord) throws IOException{
		if(tempDir == null)
			return;
		if(!tempDir.exists())
			tempDir.mkdir();
		pcs.firePropertyChange(ConceptImporter.LOADING_MESSAGE,null,"Loading terms into datastructure ...");
		storage.useTempWordFolder = false;
		File [] fileList = tempDir.listFiles();
		if(fileList != null){
			
			pcs.firePropertyChange(ConceptImporter.LOADING_TOTAL,null,fileList.length);
			int i = 0;
			for(File f: fileList){
					
				//load file content
				String word = f.getName();
				Set<String> terms = new HashSet<String>();
				BufferedReader rd = new BufferedReader(new FileReader(f));
				for(String l = rd.readLine();l != null; l = rd.readLine()){
					terms.add(l.trim());
				}
				rd.close();
				
				// set words
				saveWordTermsInStorage(storage,word,(useRarestWord)?getTermsByRarestWord(storage,word, terms):terms);
				
				// progress bar
				pcs.firePropertyChange(ConceptImporter.LOADING_PROGRESS,null,i++);
			}
		}
	}
	
	
	/**
	 * Gets the terms by rarest word.
	 *
	 * @param storage the storage
	 * @param word the word
	 * @param terms the terms
	 * @return the terms by rarest word
	 */
	private static Set<String> getTermsByRarestWord(Storage storage, String word,Set<String> terms){
		// sort by longest string first
		Set<String> rarestTerms = new HashSet<String>();
		for(String term: terms){
			String rareWord = getRarestWord(storage,term);
			if(word.equals(rareWord)){
				rarestTerms.add(term);
			}
		}
		return rarestTerms;
	}
	

	/**
	 * get list of normalized terms from from the class.
	 *
	 * @param term the term
	 * @param cls the cls
	 * @return the terms
	 */
	public static Set<String> getNormalizedTerms(NobleCoderTerminology term,Concept cls){
		return getNormalizedTerms(term,cls,term.isStemWords());
	}
	
	/**
	 * get list of normalized terms from from the class.
	 *
	 * @param term the term
	 * @param cls the cls
	 * @param stem the stem
	 * @return the terms
	 */
	public static Set<String> getNormalizedTerms(NobleCoderTerminology term, Concept cls, boolean stem){
		if(cls == null)
			return Collections.EMPTY_SET;
		
		String name = cls.getName();
		//Pattern pt = Pattern.compile("(.*)[\\(\\[].*[\\)\\]]");
		Set<String> terms = new HashSet<String>();
		Set<String> synonyms = new LinkedHashSet<String>();
		synonyms.add(name);
		Collections.addAll(synonyms,cls.getSynonyms());
		for(String str: synonyms){
			if(isRegExp(str))
				terms.add(str);
			else{
				// if we have a limit on size of words in a term, enforce it.
				boolean addTerm = true;
				if(term.getMaximumWordsInTerm() > -1 && term.getMaximumWordsInTerm() < TextTools.charCount(str,' ')){
					addTerm = false;
				}
				if(addTerm)
					terms.add(TextTools.normalize(str,stem,term.isIgnoreDigits(),term.isStripStopWords(),true,false));
			}
		}
		return terms;
	}

	/**
	 * check if string is a regular expression.
	 *
	 * @param s the s
	 * @return true, if is reg exp
	 */
	public static boolean isRegExp(String s){
		return s != null && s.startsWith("/") && s.endsWith("/");
	}
	
	/**
	 * Checks if is included.
	 *
	 * @param list the list
	 * @param src the src
	 * @return true, if is included
	 */
	public static boolean isIncluded(List<String> list, String src){
		return isIncluded(list,src,true);
	}
	
	/**
	 * Checks if is included.
	 *
	 * @param list the list
	 * @param src the src
	 * @param strict the strict
	 * @return true, if is included
	 */
	public static boolean isIncluded(List<String> list, String src,boolean strict){
		if(list == null)
			return true;
		
		// filter by known source if
		if(strict)
			return list.contains(src);
		
		// else look at substring
		for(String s: list){
			if(src.contains(s))
				return true;
		}	
		
		return false;
	}
	
	/**
	 * check that the term is contigous within the limits allowed.
	 *
	 * @param words the words
	 * @param twords the twords
	 * @param maxWordGap the max word gap
	 * @return true, if successful
	 */
	public static boolean checkContiguity(List<String> words,List<String> twords, int maxWordGap){
		// go over every word in a sentence
		boolean continguous = false;
		for(int i=0;i<words.size();i++){
			// if term words contain that word, then
			// look at the sublist that includes it and + allowed gap
			// if this sublist contains ALL term words, then we have contigous match
			// FROM MELISSA: the word window span is actually good, just need to do gap analysis after to 
			// make sure that no gap exceeds the word gap 
			
			if(twords.contains(words.get(i))){
				int n = i+((maxWordGap+1)*(twords.size()-1))+1;
				if(n > words.size())
					n = words.size();
				if(words.subList(i,n).containsAll(twords)){
					continguous = true;
					break;
				}
			}
		}
		return continguous; 
	}
	
	
	/**
	 * Index of.
	 *
	 * @param list the list
	 * @param w the w
	 * @param n the n
	 * @return the int
	 */
	public static int indexOf(List<String> list,String w, int n){
		for(int i=n;i<list.size();i++){
			if(list.get(i).equals(w))
				return i;
		}
		return -1;
	}
	
	/**
	 * check word order.
	 *
	 * @param words the words
	 * @param twords the twords
	 * @param term the term
	 * @return true, if successful
	 */
	public static boolean checkWordOrder(List<String> words,List<String> twords,String term){
		boolean ordered = true;
		
		// assume that term word order is the same, since we stopped sorting terms when storing them
		int lastI = 0;
		for(String tw: twords){
			int i = indexOf(words,tw,lastI);
			if(i < lastI){
				ordered = false;
				break;
			}
			lastI = i;	
		}
		return ordered;
	
	}
	
	/**
	 * get all used words from this term.
	 *
	 * @param terminology the terminology
	 * @param words the words
	 * @param term the term
	 * @return the used words
	 */
	public static List<String> getUsedWords(NobleCoderTerminology terminology,List<String> words, String term){
		// if not ignore used words and in overlap mode, return
		if(!terminology.isIgnoreUsedWords() && terminology.isOverlapMode())
			return Collections.EMPTY_LIST;
				
		List<String> termWords = TextTools.getWords(term);
		List<String> usedWords = new ArrayList<String>();
		// remove words that are involved in term
		if(terminology.isOverlapMode()){
			for(String w: termWords){
				usedWords.add(w);
			}
		}else{
			boolean span = false;
			for(String w: words){
				// if text word is inside terms, then
				if(termWords.contains(w)){
					usedWords.add(w);
					termWords.remove(w);
					span = true;
				}
				if(termWords.isEmpty())
					break;
				if(span)
					usedWords.add(w);
			}
		}
		return usedWords;
	}
	
	
	/**
	 * Get a list of contiguous concept annotations from a given concept
	 * Essentially converts a single concepts that annotates multiple related words to text
	 * to potentially multiple instances of a concept in text.
	 *
	 * @param c the c
	 * @param searchWords the search words
	 * @return the annotations
	 */
	public static List<Annotation> getAnnotations(Concept c,List<String> searchWords){
		List<Annotation> list = new ArrayList<Annotation>();
		List<String> matchedWords = TextTools.getWords(c.getMatchedTerm()); //Arrays.asList(c.getMatchedTerm().split(" "));
		int n = 0;
		for(String w: searchWords){
			if(matchedWords.contains(w)){
				Annotation a = new Annotation();
				a.setText(w);
				a.setOffset(c.getSearchString().indexOf(w,n));
				a.setSearchString(c.getSearchString());
				list.add(a);
			}
			n += w.length()+1;
		}
		return list;
	}

	

	/**
	 * represents a tuple of hashtable and list.
	 */
	public static class NormalizedWordsContainer {
		public Map<String,String> normalizedWordsMap;
		public List<String> normalizedWordsList;
		public List<String> originalWordsList;
		
	}
	
	/**
	 * perform normalization of a string @see normalize, but return unsorted list of words .
	 *
	 * @param term the term
	 * @param text the text
	 * @return NormalizedWordsContainer - normalized word for its original form
	 */
	public static NormalizedWordsContainer getNormalizedWordMap(NobleCoderTerminology term, String text){
		NormalizedWordsContainer c = new NormalizedWordsContainer();
		c.normalizedWordsMap = new LinkedHashMap<String, String>();
		c.normalizedWordsList = new ArrayList<String>();
		c.originalWordsList = TextTools.getWords(text);
		//boolean skipAbbr = false;
		
		for(String w: c.originalWordsList){
			List<String> ws = TextTools.normalizeWords(w, term.isStemWords(),term.isIgnoreDigits(),term.isStripStopWords());
			if(!ws.isEmpty() && !c.normalizedWordsMap.containsKey(ws.get(0)))
				c.normalizedWordsMap.put(ws.get(0),w);
			c.normalizedWordsList.addAll(ws);
		}
		return c;
	}

	
	/**
	 * save meta information.
	 *
	 * @param term the term
	 */
	public static void saveSearchProperteis(NobleCoderTerminology term){
		try{
			FileWriter w = new FileWriter(new File(term.getLocation(),NobleCoderTerminology.SEARCH_PROPERTIES));
			getSearchProperties(term).store(w,"Optional Search Options");
			w.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	/**
	 * To string.
	 *
	 * @param list the list
	 * @return the string
	 */
	public static String toString(Object [] list){
		StringBuffer b = new StringBuffer();
		for(Object o: list){
			b.append(o+";"); 
		}
		return (b.length()>1)?b.substring(0,b.length()-1):"";
	}
	
	/**
	 * get properties map with search options.
	 *
	 * @param term the term
	 * @return the search properties
	 */
	public static Properties getSearchProperties(NobleCoderTerminology term){
		Properties p = new Properties();
		p.setProperty("default.search.method",term.getDefaultSearchMethod());
		p.setProperty("ignore.small.words",""+term.isIgnoreSmallWords());
		p.setProperty("source.filter",toString(term.getSourceFilter()));
		p.setProperty("language.filter",toString(term.getLanguageFilter()));
		p.setProperty("semantic.type.filter",toString(term.getSemanticTypeFilter()));
		p.setProperty("ignore.common.words",""+term.isIgnoreCommonWords());
		p.setProperty("ignore.acronyms",""+term.isIgnoreAcronyms());
		p.setProperty("select.best.candidate",""+term.isSelectBestCandidate());
		p.setProperty("score.concepts",""+term.isScoreConcepts());
		p.setProperty("window.size",""+term.getWindowSize());
		p.setProperty("maximum.word.gap",""+term.getMaximumWordGap());
		//p.setProperty("enable.search.cache",""+cachingEnabled);
		p.setProperty("ignore.used.words",""+term.isIgnoreUsedWords());
		p.setProperty("subsumption.mode",""+term.isSubsumptionMode());
		p.setProperty("overlap.mode",""+term.isOverlapMode());
		p.setProperty("contiguous.mode",""+term.isContiguousMode());
		p.setProperty("ordered.mode",""+term.isOrderedMode());
		p.setProperty("partial.mode",""+term.isPartialMode());
		p.setProperty("stem.words",""+term.isStemWords());
		p.setProperty("ignore.digits",""+term.isIgnoreDigits());
		p.setProperty("handle.possible.acronyms",""+term.isHandlePossibleAcronyms());
		p.setProperty("partial.match.theshold",""+term.getPartialMatchThreshold());
		p.setProperty("max.words.in.term",""+term.getMaximumWordsInTerm());
		return p;
	}
	
	
	/**
	 * set search properties.
	 *
	 * @param term the term
	 * @param p the p
	 */
	public static void setSearchProperties(NobleCoderTerminology term, Properties p){
		// load default values
		// lookup default search method
		if(p.containsKey("default.search.method")){
			String defaultSearchMethod = NobleCoderTerminology.BEST_MATCH;
			String s = p.getProperty("default.search.method",NobleCoderTerminology.BEST_MATCH);
			for(String m: term.getSearchMethods()){
				if(s.equals(m)){
					defaultSearchMethod = s;
					break;
				}	
			}
			term.setDefaultSearchMethod(defaultSearchMethod);
		}
		
		if(p.containsKey("ignore.common.words"))
			term.setIgnoreCommonWords(Boolean.parseBoolean(p.getProperty("ignore.common.words")));
		if(p.containsKey("ignore.acronyms"))
			term.setIgnoreAcronyms(Boolean.parseBoolean(p.getProperty("ignore.acronyms")));
		if(p.containsKey("select.best.candidate"))
			term.setSelectBestCandidate(Boolean.parseBoolean(p.getProperty("select.best.candidate")));
		if(p.containsKey("window.size")){
			try{
				term.setWindowSize(Integer.parseInt(p.getProperty("window.size")));
			}catch(Exception ex){}
		}
		if(p.containsKey("word.window.size")){
			try{
				term.setMaximumWordGap(Integer.parseInt(p.getProperty("word.window.size"))-1);
			}catch(Exception ex){}
		}
		if(p.containsKey("maximum.word.gap")){
			try{
				term.setMaximumWordGap(Integer.parseInt(p.getProperty("maximum.word.gap")));
			}catch(Exception ex){}
		}
		
		if(p.containsKey("ignore.used.words"))
			term.setIgnoreUsedWords(Boolean.parseBoolean(p.getProperty("ignore.used.words")));
		if(p.containsKey("subsumption.mode"))
			term.setSubsumptionMode(Boolean.parseBoolean(p.getProperty("subsumption.mode")));
		if(p.containsKey("overlap.mode"))
			term.setOverlapMode(Boolean.parseBoolean(p.getProperty("overlap.mode")));
		if(p.containsKey("contiguous.mode"))
			term.setContiguousMode(Boolean.parseBoolean(p.getProperty("contiguous.mode")));
		if(p.containsKey("ordered.mode"))
			term.setOrderedMode( Boolean.parseBoolean(p.getProperty("ordered.mode")));
		if(p.containsKey("partial.mode"))
			term.setPartialMode( Boolean.parseBoolean(p.getProperty("partial.mode")));
		if(p.containsKey("partial.match.theshold"))
			term.setPartialMatchThreshold(Double.parseDouble(p.getProperty("partial.match.theshold")));
		if(p.containsKey("max.words.in.term"))
			term.setMaximumWordsInTerm(Integer.parseInt(p.getProperty("max.words.in.term")));
		if(p.containsKey("select.best.candidate"))
			term.setSelectBestCandidate(Boolean.parseBoolean(p.getProperty("select.best.candidate")));
		if(p.containsKey("score.concepts"))
			term.setScoreConcepts(Boolean.parseBoolean(p.getProperty("score.concepts")));
		if(p.containsKey("ignore.small.words"))
			term.setIgnoreSmallWords(Boolean.parseBoolean(p.getProperty("ignore.small.words")));
		if(p.containsKey("handle.possible.acronyms"))
			term.setHandlePossibleAcronyms(Boolean.parseBoolean(p.getProperty("handle.possible.acronyms")));
		if(p.containsKey("ignore.digits"))
			term.setIgnoreDigits(Boolean.parseBoolean(p.getProperty("ignore.digits")));
		
		// language filter
		String v = p.getProperty("language.filter");
		if(v != null && v.length() > 0){
			ArrayList<String> val = new ArrayList<String>();
			String sep = (v.indexOf(';') > -1)?";":",";
			for(String s: v.split(sep))
				val.add(s.trim());
			term.setLanguageFilter(val.toArray(new String [0]));
		}
		
		// source filter
		v = p.getProperty("source.filter");
		if(v != null && v.length() > 0){
			ArrayList<Source> val = new ArrayList<Source>();
			String sep = (v.indexOf(';') > -1)?";":",";
			for(String s: v.split(sep))
				val.add(Source.getSource(s.trim()));
			term.setSourceFilter(val.toArray(new Source [0]));
		}
		
		// semantic type filter
		v = p.getProperty("semantic.type.filter");
		if(v != null && v.length() > 0){
			ArrayList<SemanticType> val = new ArrayList<SemanticType>();
			String sep = (v.indexOf(';') > -1)?";":",";
			for(String s: v.split(sep))
				val.add(SemanticType.getSemanticType(s.trim()));
			term.setSemanticTypeFilter(val.toArray(new SemanticType [0]));
		}
	}
	
	
	
	/**
	 * save meta information.
	 *
	 * @param term the term
	 * @param f the f
	 */
	public static void loadMetaInfo(NobleCoderTerminology term,File f){
		try{
			for(String l: TextTools.getText(new FileInputStream(f)).split("\n")){
				if(l.trim().length() > 0){
					int n = l.indexOf(':');
					String key = l.substring(0,n).trim();
					String val = l.substring(n+1).trim();
					term.getStorage().getInfoMap().put(key,val);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * save meta information.
	 *
	 * @param term the term
	 * @param f the f
	 */
	public static void saveMetaInfo(NobleCoderTerminology term,File f){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("name:\t\t"+term.getName()+"\n");
			writer.write("uri:\t\t"+term.getURI()+"\n");
			writer.write("version:\t"+term.getVersion()+"\n");
			writer.write("location:\t"+term.getLocation()+"\n");
			if(term.getStorage().getInfoMap().containsKey("languages"))
				writer.write("languages:\t"+term.getStorage().getInfoMap().get("languages")+"\n");
			writer.write("description:\t"+term.getDescription()+"\n");
			if(term.getStorage().getInfoMap().containsKey("semantic.types"))
				writer.write("semantic types:\t"+term.getStorage().getInfoMap().get("semantic.types")+"\n");
			if(term.getStorage().getInfoMap().containsKey("word.count"))
				writer.write("word count:\t"+term.getStorage().getInfoMap().get("word.count")+"\n");
			if(term.getStorage().getInfoMap().containsKey("term.count"))
				writer.write("term count:\t"+term.getStorage().getInfoMap().get("term.count")+"\n");
			if(term.getStorage().getInfoMap().containsKey("concept.count"))
				writer.write("concept count:\t"+term.getStorage().getInfoMap().get("concept.count")+"\n");
			writer.write("configuration:\t");
			writer.write("stem.words="+term.isStemWords()+", ");
			writer.write("strip.digits="+term.isIgnoreDigits()+", ");
			writer.write("strip.stop.words="+term.isStripStopWords()+", ");
			//writer.write("handle.possible.acronyms="+handlePossibleAcronyms+", ");
			writer.write("max.words.in.term="+term.getMaximumWordsInTerm()+", ");
			writer.write("ignore.small.words="+term.isIgnoreSmallWords()+"\n");
			writer.write("\nsources:\n\n");
			for(String name: new TreeSet<String>(term.getStorage().getSourceMap().keySet())){
				writer.write(name+": "+term.getStorage().getSourceMap().get(name).getDescription()+"\n");
			}
			
			
			writer.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		// save info in map
		term.getStorage().getInfoMap().put("strip.digits",""+term.isIgnoreDigits());
		term.getStorage().getInfoMap().put("strip.stop.words",""+term.isStripStopWords());
		term.getStorage().getInfoMap().put("stem.words",""+term.isStemWords());
		term.getStorage().getInfoMap().put("ignore.small.words",""+term.isIgnoreSmallWords());
		term.getStorage().getInfoMap().put("max.words.in.term",""+term.getMaximumWordsInTerm());
		//storage.getInfoMap().put("handle.possible.acronyms",""+handlePossibleAcronyms);
	}
	
	
	/**
	 * get original string.
	 *
	 * @param text the text
	 * @param term the term
	 * @param map the map
	 * @return the original term
	 */
	public static String getOriginalTerm(String text, String term, Map<String,String> map){
		StringBuffer ot = new StringBuffer();
		final String txt = text.toLowerCase();
		Set<String> words = new TreeSet<String>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				if(o1.length() > 3)
					o1 = o1.substring(0,o1.length()-1);
				if(o2.length() > 3)
					o2 = o2.substring(0,o2.length()-1);
				int x = txt.indexOf(o1) - txt.indexOf(o2);
				if(x == 0)
					return o1.compareTo(o2);
				return x;
			}
		});
		Collections.addAll(words, term.split(" "));
		for(String s: words){
			String w = map.get(s);
			if(w == null)
				w = s;
			ot.append(w+" ");
		}
		String oterm = ot.toString().trim();
		return oterm;
	}
	
	
	/**
	 * optionally limit to a sublist of words.
	 *
	 * @param term the term
	 * @param words the words
	 * @param count the count
	 * @return the text words
	 */
	public static List<String> getTextWords(NobleCoderTerminology term, List<String> words,int count) {
		// currently there is a bug, so can't use window size with used words
		if(term.isIgnoreUsedWords())
			return words;
		// decrement to compensate
		count --;
		int windowSize = term.getWindowSize();
		if(windowSize > 0 && words.size() > windowSize && count < words.size()){
			int end = (count+windowSize)<words.size()?count+windowSize:words.size();
			return words.subList(count,end);
		}
		return words;
	}
	

	/**
	 * get an index of object in the collection.
	 *
	 * @param o the o
	 * @param list the list
	 * @return the int
	 */
	public static int indexOf(Object o, Collection list){
		int n = 1;
		for(Object oo: list){
			if(oo.equals(0)){
				return n;
			}
			n++;
		}
		return -1;
	}
	
	
	/**
	 * Gets the preferred name.
	 *
	 * @param c the c
	 * @return the preferred name
	 */
	public static String getPreferredName(Concept c){
		/*// if prefered name source is not set OR
		// we have filtering and the new source offset is less then old source offset (which means higher priority)
		if(prefNameSource == null || (filterSources != null && filterSources.indexOf(src) < filterSources.indexOf(prefNameSource))){
			c.setName(text);
			prefNameSource = src;
			
		}*/
		// Abbreviation for term type in source vocabulary, for example PN (Metathesaurus Preferred Name) or 
		// CD (Clinical Drug). Possible values are listed on the Abbreviations Used in Data Elements page.
		
		
		String name = null;
		// go over terms that were marked as preferred
		for(Term term: c.getTerms()){
			if(term.isPreferred()){
				if(name == null || "PN".equals(term.getForm()))
					name = term.getText();
			}
		}
		// oops nothing was apperently marked, lets look at all of them
		if(name == null){
			for(Term term: c.getTerms()){
				if(name == null || "PT".equals(term.getForm()) || "PN".equals(term.getForm())){
					name = term.getText();
				}
				
			}
		}
		// still nothing, just leave the name as is
		if(name == null)
			name = c.getName();
		return name;
	}
	
	
}
