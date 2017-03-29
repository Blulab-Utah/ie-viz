package edu.utah.blulab.marshallers.pycontext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


import edu.stanford.nlp.util.Triple;
import edu.utah.blulab.domainontology.*;

public class DOtoPyConText {
	

	public static void main(String[] args)  {
		System.out.println("Starting test app.");


		DOtoPyConText parser = new DOtoPyConText();
		try {
			File ontFile = new File(args[0]);
			String domainName = ontFile.getName().substring(0, ontFile.getName().indexOf("."));
			File modifiersFile = new File(ontFile.getParentFile().getPath() + "/" + domainName + "_modifiers.tsv");
            File targetsFile = new File(ontFile.getParentFile().getPath() + "/" + domainName + "_targets.tsv");
            File rulesFile = new File(ontFile.getParentFile().getPath() + "/" + domainName + "_rules.tsv");

            //System.out.println(modifiersFile.toString());

			if(!modifiersFile.exists()){
				modifiersFile.createNewFile();
			}
            if(!targetsFile.exists()){
                targetsFile.createNewFile();
            }
            if(!modifiersFile.exists()){
                rulesFile.createNewFile();
            }

			parser.createPyConTextFiles(ontFile, modifiersFile, targetsFile, rulesFile);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void createPyConTextFiles(File ontology, File modifierFile, File targetFile, File ruleFile) throws Exception{
	    DomainOntology domain = new DomainOntology(ontology.getPath(), false);

        //write modifier file
        System.out.println("Writing " + modifierFile.getName() + "...");
        BufferedWriter bw = new BufferedWriter(new FileWriter(modifierFile));
        ArrayList<Modifier> modDictionary = domain.createModifierDictionary();
        ArrayList<Modifier> pseudoModDictionary = domain.createPseudoModifierDictionary();
        ArrayList<Modifier> closureDictionary = domain.createClosureDictionary();

        ArrayList<Modifier> modifiers = new ArrayList<Modifier>();
        modifiers.addAll(modDictionary);
        modifiers.addAll(pseudoModDictionary);
        modifiers.addAll(closureDictionary);

        bw.write("literal\t category\t regex\t rule");


        for(Modifier modifier : modifiers){
            HashMap<String, String> regexTuples = new HashMap<String, String>();
            ArrayList<Triple<String, String, String>> tripleList = new ArrayList<Triple<String, String, String>>();

            ArrayList<LexicalItem> variants = modifier.getItems();
            for(LexicalItem lexicalItem : variants){
                ArrayList<String> lexicalItemLabels = new ArrayList<String>();
                lexicalItemLabels.add(lexicalItem.getPrefTerm());
                lexicalItemLabels.addAll(lexicalItem.getSynonym());
                lexicalItemLabels.addAll(lexicalItem.getAbbreviation());
                lexicalItemLabels.addAll(lexicalItem.getMisspelling());
                lexicalItemLabels.addAll(lexicalItem.getSubjExp());

                //once regex in modifier ontology are fixed can use match regex method
                if(lexicalItem.getRegex().size() > 0){
                    //System.out.println("Too mnay regex!!");
                    ArrayList<String> regex = lexicalItem.getRegex();
                    for(int i = 0; i < regex.size(); i++){
                        regexTuples.put(lexicalItemLabels.get(i), regex.get(i));
                        tripleList.add(new Triple<String, String, String>(lexicalItemLabels.get(i), regex.get(i),
                                lexicalItem.getActionEn(true)));
                    }
                    for(String variant : lexicalItemLabels){
                        if(!regexTuples.containsKey(variant)){
                            regexTuples.put(variant, "");
                            tripleList.add(new Triple<String, String, String>(variant, "",
                                    lexicalItem.getActionEn(true)));
                        }
                    }
                }
                for(String variant : lexicalItemLabels){
                    if(!regexTuples.containsKey(variant)){
                        regexTuples.put(variant, "");
                        tripleList.add(new Triple<String, String, String>(variant, "",
                                lexicalItem.getActionEn(true)));
                    }
                }

                //regexTuples.putAll(matchRegEx(lexicalItemLabels, lexicalItem.getRegex()));
            }

            if(regexTuples.isEmpty()){
                //System.out.println(modifier.getModName());
                bw.write(modifier.getModName() + "\t\t\t");
                bw.newLine();
            }else{
                for(Triple<String, String, String> triple : tripleList){
                    //System.out.println(modifier.getModName() + "\t" + entry.getKey() + "\t" + entry.getValue());
                    bw.write(modifier.getModName() + "\t" + triple.first() + "\t" + triple.second() + "\t"
                    + triple.third());
                    bw.newLine();
                }
            }

        }

        bw.close();

        //write targets file
        System.out.println("Writing " + targetFile.getName() + "...");
        bw = new BufferedWriter(new FileWriter(targetFile));

        bw.write("Lex\tType\tRegex\n");

        ArrayList<Anchor> anchors = domain.createAnchorDictionary();

        for(Anchor term : anchors){
            HashMap<String, String> regexTuples = new HashMap<String, String>();
            ArrayList<String> variants = new ArrayList<String>();

            List<ClassPath> parents = term.getClassPaths();
            Anchor ancestor = getAncestor(domain, parents);

            variants.add(term.getPrefTerm());
            //variants.addAll(term.getSynonym());
            for(String str : term.getSynonym()){
                if(!variants.contains(str)){
                    variants.add(str);
                }
            }
            for(String str : term.getMisspelling()){
                if(!variants.contains(str)){
                    variants.add(str);
                }
            }
            for(String str : term.getAbbreviation()){
                if(!variants.contains(str)){
                    variants.add(str);
                }
            }
            for(String str : term.getSubjExp()){
                if(!variants.contains(str)){
                    variants.add(str);
                }
            }
            //variants.addAll(term.getMisspelling());
            //variants.addAll(term.getAbbreviation());
            //variants.addAll(term.getSubjExp());

            if(term.getRegex().size() > 0){
                ArrayList<String> regex = term.getRegex();
                /**for(int i = 0; i < regex.size(); i++){
                    regexTuples.put(variants.get(i), regex.get(i));
                }
                for(String variant : variants){
                    if(!regexTuples.containsKey(variant)){
                        regexTuples.put(variant, "");
                    }
                }**/
                regexTuples.putAll(matchRegEx(variants, regex));
            }


            if(regexTuples.isEmpty()){
                //System.out.println(term.getPrefTerm() + domain.getDisplayName(term.getURI()) + "" + "\t");
                for(String str : variants){
                    if(ancestor != null){
                        //System.out.println(str + "\t" + domain.getDisplayName(ancestor.getURI()) + "" + "\t");
                        bw.write(str + "\t" + domain.getDisplayName(ancestor.getURI()) + "" + "\t");
                        bw.newLine();
                    }else{
                        //System.out.println(str + "\t" + domain.getDisplayName(term.getURI()) + "" + "\t");
                        bw.write(str + "\t" + domain.getDisplayName(term.getURI()) + "" + "\t");
                        bw.newLine();
                    }
                }


                //bw.write(term.getPrefTerm() + "\t" + domain.getDisplayName(term.getURI()) + "\t");
                //bw.newLine();
            }else{
                for(Map.Entry<String, String> entry : regexTuples.entrySet()){
                    //System.out.println(entry.getKey() + "\t" + domain.getDisplayName(term.getURI()) + "\t" +
                    //entry.getValue());
                    //TODO figure out parentage problem for regex tuples
                    bw.write(entry.getKey() + "\t" + domain.getDisplayName(term.getURI()) + "\t" +
                            entry.getValue());
                    bw.newLine();
                }
            }
        }

        bw.close();

        //write rules file
        //final String CATEGORY_RULE = "@CATEGORY_RULE";
        System.out.println("Writing " + ruleFile.getName() + "...");
        bw = new BufferedWriter(new FileWriter(ruleFile));

        //Get variables and parse out anchor and modifiers associated with each anchor
        for(Variable variable : domain.getAllVariables()){
            String varName = variable.getVarName();
            if(varName.isEmpty()){
                String temp = variable.getURI();
                temp.replaceAll("_", " ");
                varName = temp;
            }
            bw.write(varName + "\n");

            //Get anchor and write out with 1 tab
            bw.write("\thasAnchor\t");
            ArrayList<LogicExpression<Anchor>> varAnchors = variable.getAnchor();
            for(LogicExpression<Anchor> anchorExp : varAnchors){
                if(anchorExp.isSingleExpression()){
                    String anchorTermURI = anchorExp.remove(0).getURI();
                    String anchorTerm = domain.getDisplayName(anchorTermURI);
                    bw.write(anchorTerm + "\n");
                }else if(anchorExp.isOrExpression()){
                    String termList = "";
                    for(Anchor term : anchorExp){
                        String anchorTerm = domain.getDisplayName(term.getURI());
                        termList = termList + anchorTerm + ",";
                    }
                    termList = termList.substring(0, termList.length()-1);
                    bw.write(termList + "\n");

                }
            }

            //Get Modifiers and write
            HashMap<String, LogicExpression<Modifier>> modifierMap = variable.getModifiers();
            for(Map.Entry<String, LogicExpression<Modifier>> entry : modifierMap.entrySet()){
                String relation = entry.getKey();
                String relationDisplay = relation.substring(relation.indexOf("#")+1, relation.length());
                if(entry.getValue().isSingleExpression()){
                    String modifierURI = entry.getValue().remove(0).getUri();
                    String modValue = domain.getDisplayName(modifierURI);
                    bw.write("\t\t" + relationDisplay + "\t" + modValue + "\n");
                }else if(entry.getValue().isOrExpression()){
                    String modList = "";
                    for(Modifier mod : entry.getValue()){
                        String modValue = domain.getDisplayName(mod.getUri());
                        modList = modList + modValue + ",";
                    }
                    modList = modList.substring(0, modList.length()-1);
                    bw.write("\t\t" + relationDisplay + "\t" + modList + "\n");

                }
            }
            if(variable.hasNumericModifiers()){
                for(NumericModifier numericMod : variable.getNumericModifiers()){
                    //System.out.println(numericMod);
                    String numList = "";
                    for(String str : numericMod.getQuantityValueRange()){
                        numList = numList + str + ",";
                    }
                    numList = numList.substring(0, numList.length()-1);
                    bw.write("\t\thasQuantityValue\t" + numList + "\n");
                }
            }
        }


        bw.close();

    }


    public HashMap<String, String> matchRegEx(ArrayList<String> variants, ArrayList<String> regex){
        HashMap<String, String> tuples = new HashMap<String, String>();

        class comp implements Comparator<String>{
            public int compare(String o1, String o2) {
                if (o1.length() > o2.length()) {
                    return 1;
                } else if (o1.length() < o2.length()) {
                    return -1;
                } else {
                    return 0;
                }

            }
        }

        Collections.sort(variants, new comp());

        for(String r : regex){
            for(String v : variants){
                if(v.toLowerCase().matches(r)){
                    tuples.put(v.toLowerCase(), r);
                    //variants.remove(v);
                }
            }
        }

        for(String v : variants){
            if(!tuples.containsKey(v.toLowerCase())){
                tuples.put(v.toLowerCase(), v.toLowerCase());
            }

        }



        return tuples;
    }

    private Anchor getAncestor(DomainOntology domain, List<ClassPath> ancestors) throws Exception{


        for(ClassPath path : ancestors){
            for(int i =0; i<path.size(); i++){
                if(!path.get(i).getIRI().toString().equals("http://blulab.chpc.utah.edu/ontologies/v2/ConText.owl#Lexicon")
                        && !path.get(i).getIRI().toString().equals("http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl#Anchor")){
                    return domain.createAnchor(path.get(i).getIRI().toString());
                }
            }
        }

        return null;
    }




}
