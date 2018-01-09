package edu.pitt.dbmi.nlp.noble.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.ConceptPath;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyError;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;


/**
 * The Class PathHelper.
 */
public class PathHelper {
	private int pathDepthLimit = 7, maxNumberOfPaths = 10;
	private boolean readOnly = true, debug;
	private int num;
	private Map<String,List<List<String>>> pathMap;
	private Map<String,Map<String,Integer>> ancestryMap;
	private Terminology terminology;
	
	/**
	 * initialize new path helper for a given terminology.
	 *
	 * @param t the t
	 */
	public PathHelper(Terminology t){
		this.terminology = t;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if(ancestryMap != null && ancestryMap instanceof JDBMMap){
			((JDBMMap) ancestryMap).dispose();
		}
		if(pathMap != null && pathMap instanceof JDBMMap){
			((JDBMMap) pathMap).dispose();
		}
	}

	


	/**
	 * Gets the path depth limit.
	 *
	 * @return the path depth limit
	 */
	public int getPathDepthLimit() {
		return pathDepthLimit;
	}

	/**
	 * Sets the path depth limit.
	 *
	 * @param pathDepthLimit the new path depth limit
	 */
	public void setPathDepthLimit(int pathDepthLimit) {
		this.pathDepthLimit = pathDepthLimit;
	}

	/**
	 * Gets the max number of paths.
	 *
	 * @return the max number of paths
	 */
	public int getMaxNumberOfPaths() {
		return maxNumberOfPaths;
	}

	/**
	 * Sets the max number of paths.
	 *
	 * @param maxNumberOfPaths the new max number of paths
	 */
	public void setMaxNumberOfPaths(int maxNumberOfPaths) {
		this.maxNumberOfPaths = maxNumberOfPaths;
	}

	/**
	 * get all paths to root for a given concept.
	 *
	 * @param c the c
	 * @return list of paths (path is list of concepts)
	 * WARNING: for messy terminology traversing a graph can take FOREVER!!!!
	 */
	public List<ConceptPath> getPaths(Concept c){
		if(c != null){
			try {
				// if visited return values
				if(getPathMap().containsKey(c.getCode()))
					return toPaths(terminology,getPathMap().get(c.getCode()));
				// else perform search
				List<ConceptPath> paths = findPaths(c);
				// save in cache
				getPathMap().put(c.getCode(),toList(paths));
				return paths;
			} catch (Exception e) {
				throw new TerminologyError("Problem retrieving cache", e);
			}
			
		}
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * conver to paths.
	 *
	 * @param t the t
	 * @param paths the paths
	 * @return the list
	 */
	private List<ConceptPath> toPaths(Terminology t, List<List<String>> paths) {
		List<ConceptPath> n = new ArrayList<ConceptPath>();
		for(List<String> p: paths){
			ConceptPath np = new ConceptPath();
			for(String s: p){
				Concept c = new Concept(s);
				c.setTerminology(t);
				np.add(c);
			}
			n.add(np);
		}
		return n;
	}

	/**
	 * conver to paths.
	 *
	 * @param paths the paths
	 * @return the list
	 */
	private List<List<String>> toList(List<ConceptPath> paths) {
		List<List<String>> n = new ArrayList<List<String>>();
		for(ConceptPath p: paths){
			List<String> np = new ArrayList<String>();
			for(Concept c: p){
				np.add(c.getCode());
			}
			n.add(np);
		}
		return n;
	}

	

	/**
	 * get appropriate map for given resource.
	 *
	 * @return the path map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Map<String,List<List<String>>> getPathMap() throws IOException{
		if(pathMap == null){
			if(terminology instanceof NobleCoderTerminology){
				File f = new File(terminology.getLocation(),"table_pathMap.d.0");
				if(!readOnly || f.exists()){
					pathMap = new JDBMMap<String, List<List<String>>>(terminology.getLocation()+File.separator+"table","pathMap",readOnly);
				}
			}
		}
		// initialize as normal map
		if(pathMap == null)
			pathMap= new HashMap<String, List<List<String>>>();
		
		return pathMap;
	}
	

	/**
	 * get appropriate map for given resource.
	 *
	 * @return the ancestery map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Map<String,Map<String,Integer>> getAncesteryMap() throws IOException{
		if(ancestryMap == null){
			if(terminology instanceof NobleCoderTerminology){
				File f = new File(terminology.getLocation(),"table_ancestorMap.d.0");
				if(!readOnly || f.exists()){
					ancestryMap = new JDBMMap<String, Map<String,Integer>>(terminology.getLocation()+File.separator+"table","ancestorMap",readOnly);
				}
			}
		}
		// initialize as normal map
		if(ancestryMap == null)
			ancestryMap= new HashMap<String,Map<String,Integer>>();
		
		return ancestryMap;
	}
	
	/**
	 * given a list of concept paths, it returns a map of parent concepts and their minimum levels.
	 *
	 * @param terminology the terminology
	 * @param stringMap the string map
	 * @return the map
	 */
	private Map<Concept,Integer> toAncestors(Terminology terminology,Map<String,Integer> stringMap){
		Map<Concept,Integer> map = new LinkedHashMap<Concept, Integer>();
		for(String s: stringMap.keySet()){
			Concept c = new Concept(s);
			c.setTerminology(terminology);
			map.put(c,stringMap.get(s));
		}
		return map;
	}
	
	
	/**
	 * convert a map of ancestors to a string representation.
	 *
	 * @param map the map
	 * @return the string
	 */
	public String toString(Map<Concept,Integer> map){
		if(map == null)
			return "";
		
		Set<String> cuis = new TreeSet<String>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				String [] c1 = o1.split(":");
				String [] c2 = o2.split(":");
				if(c1.length == 2 && c2.length == 2){
					int x = Integer.parseInt(c1[1]) - Integer.parseInt(c2[1]);
					if(x == 0)
						return c1[0].compareTo(c2[0]);
					return x;
				}
				return o1.compareTo(o2);
			}
		});
		for(Concept c: map.keySet()){
			cuis.add(c.getCode()+":"+map.get(c));
		}
		
		String s = cuis.toString();
		return s.substring(1,s.length()-1).replaceAll(" ","");
	}
	

	/**
	 * given a list of concept paths, it returns a map of parent concepts and their minimum levels.
	 *
	 * @param c the c
	 * @return the ancestors
	 */
	public Map<Concept,Integer> getAncestors(Concept c){
		//return getAncestors(getPaths(c));
		if(c != null){
			try {
				// if visited return values
				if(getAncesteryMap().containsKey(c.getCode()))
					return toAncestors(terminology,getAncesteryMap().get(c.getCode()));
				Map<Concept, Integer> map = findAncestors(c);
				getAncesteryMap().put(c.getCode(),toMap(map));
				return map;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Collections.EMPTY_MAP;
	}
	
	/**
	 * To map.
	 *
	 * @param map the map
	 * @return the map
	 */
	private Map<String, Integer> toMap(Map<Concept, Integer> map) {
		Map<String,Integer> n = new LinkedHashMap<String, Integer>();
 		for(Concept c: map.keySet()){
 			n.put(c.getCode(),map.get(c));
		}
		return n;
	}

	/**
	 * pre-build ancestory cache for a given terminology for faster ancestory access .
	 *
	 * @throws Exception the exception
	 */
	public void createPathCache() throws Exception {
		if(terminology instanceof NobleCoderTerminology){
			readOnly = false;
			//debug = true;
			num = 0;
			if(pathMap != null && pathMap instanceof JDBMMap){
				((JDBMMap) pathMap).dispose();
			}
			JDBMMap map = (JDBMMap) getPathMap();
			for(String cui: ((NobleCoderTerminology)terminology).getAllConcepts()){
				getPaths(terminology.lookupConcept(cui));
				if(num % 1000 == 0)
					map.commit();
			}
			map.commit();
			map.compact();
			map.dispose();
			readOnly = true;
			//debug = false;
			ancestryMap = null;
		}
	}
	
	/**
	 * pre-build ancestory cache for a given terminology for faster ancestory access .
	 *
	 * @throws Exception the exception
	 */
	public void createAncestryCache() throws Exception {
		if(terminology instanceof NobleCoderTerminology){
			readOnly = false;
			//debug = true;
			num = 0;
			if(ancestryMap != null && ancestryMap instanceof JDBMMap){
				((JDBMMap) ancestryMap).dispose();
			}
			JDBMMap map = (JDBMMap) getAncesteryMap();
			for(String cui: ((NobleCoderTerminology)terminology).getAllConcepts()){
				getAncestors(terminology.lookupConcept(cui));
				if(num % 1000 == 0)
					map.commit();
			}
			map.commit();
			map.compact();
			map.dispose();
			readOnly = true;
			//debug = false;
			ancestryMap = null;
		}
	}

	/**
	 * Does A have an ancestor B.
	 *
	 * @param a the a
	 * @param b the b
	 * @return true, if successful
	 */
	public boolean hasAncestor(Concept a, Concept b){
		try {
			return a.equals(b) || getAncestors(a).containsKey(b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * get all parents of the node
	 * TODO:.
	 *
	 * @param c the c
	 * @return the list
	 * @throws Exception the exception
	 */
	 public List<ConceptPath> findPaths(Concept c) throws Exception {
		 return findPaths(c,getPathDepthLimit(),getMaxNumberOfPaths());
	 }
	
	/**
	 * get all parents of the node
	 * TODO:.
	 *
	 * @param c the c
	 * @return the map
	 * @throws Exception the exception
	 */
	 public Map<Concept,Integer> findAncestors(Concept c) throws Exception {
		 return findAncestors(c,getPathDepthLimit());
	 }
		
	 
	/**
	 * get all parents of the node
	 * TODO:.
	 *
	 * @param c the c
	 * @param depthLimit the depth limit
	 * @param maxPaths the max paths
	 * @return the list
	 * @throws Exception the exception
	 */
	 public List<ConceptPath> findPaths(Concept c, int depthLimit, int maxPaths) throws Exception {
		List<ConceptPath> paths = new ArrayList<ConceptPath>();
		
		// breadth first search
		Queue<ConceptNode> queue = new LinkedList<ConceptNode>();
		queue.add(new ConceptNode(c));
		// go over the queue until the end
		while(!queue.isEmpty()){
			ConceptNode cc = queue.poll();
			//System.out.println(cc.concept.getName());
			// if this is a goal, end
			if(cc.isGoal()){
				paths.add(cc.getPath());
				
				// if we found max paths, quit
				if(maxPaths > -1 && paths.size() >= maxPaths)
					return paths;
				
			}else if(depthLimit > -1 && cc.getPathLength() > depthLimit){
				//NOOP: we've exceeded max size and no root in sight
			}else{
				// add parents
				for(Concept p: cc.concept.getParentConcepts()){
					ConceptNode cn = new ConceptNode(p);
					cn.link = cc;
					queue.add(cn);
				}
			}
		}
		return paths;
	}
	
	/**
	 * Find ancestors.
	 *
	 * @param c the c
	 * @param depthLimit the depth limit
	 * @return the map
	 * @throws Exception the exception
	 */
	public Map<Concept,Integer> findAncestors(Concept c, int depthLimit) throws Exception { 
		Map<Concept,Integer> map = new LinkedHashMap<Concept, Integer>();
		
		// breadth first search
		Set<Concept> used = new HashSet<Concept>();
		Queue<ConceptNode> queue = new LinkedList<ConceptNode>();
		queue.add(new ConceptNode(c));
		used.add(c);
		// go over the queue until the end
		while(!queue.isEmpty()){
			ConceptNode cc = queue.poll();
			int n = cc.getPathLength()-1;
			if(n > 0)
				map.put(cc.concept,n);
			if((depthLimit > -1 && cc.getPathLength() > depthLimit) || cc.isGoal()){
				//NOOP: we've exceeded max size and no root in sight
			}else{
				// add parents
				for(Concept p: cc.concept.getParentConcepts()){
					if(!used.contains(p)){
						ConceptNode cn = new ConceptNode(p);
						cn.link = cc;
						queue.add(cn);
						used.add(p);
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * The Class ConceptNode.
	 */
	private class ConceptNode {
		
		/**
		 * Instantiates a new concept node.
		 *
		 * @param c the c
		 */
		public ConceptNode(Concept c) {
			concept = c;
		}
		
		/**
		 * Gets the path.
		 *
		 * @return the path
		 */
		public ConceptPath getPath() {
			ConceptPath path = new ConceptPath();
			path.add(concept);
			ConceptNode l = link;
			while(l != null){
				path.add(l.concept);
				l = l.link;
			}
			return path;
		}
		
		/**
		 * Gets the path length.
		 *
		 * @return the path length
		 */
		public int getPathLength() {
			int n = 1;
			ConceptNode l = link;
			while(l != null){
				n ++;
				l = l.link;
			}
			return n;
		}
		
		/**
		 * Checks if is goal.
		 *
		 * @return true, if is goal
		 */
		public boolean isGoal() {
			try {
				return concept.getParentConcepts().length == 0 || Arrays.asList(terminology.getRootConcepts()).contains(concept);
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
			return false;
		}
		private ConceptNode link;
		private Concept concept;
	}
	 

}
