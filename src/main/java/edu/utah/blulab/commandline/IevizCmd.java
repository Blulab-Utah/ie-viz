package edu.utah.blulab.commandline;

import org.apache.commons.cli.*;

/**
 * Created by Bill Scuba on 9/20/2016.
 */
public class IevizCmd {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message" );
        options.addOption("ont", "ontologies", false, "view a list of available ontologies" );
        options.addOption("wf", "workflows", false, "provides a list of available NLP workflows.");
        options.addOption("eval", "evaluationTool", true, "specify which evaluation tool to output the results to");
        OptionBuilder builder = OptionBuilder.hasArgs(3).withArgName("ontology> <documents> <workflow");
        builder.withLongOpt("runieviz");
        builder.withDescription("runs ieviz");
        options.addOption(builder.create("run"));

        System.out.print("Arguments:");
        for (String arg : args) {
            System.out.print(" " + arg);
        }
        System.out.print("\n\n");

        try{
            CommandLine line = new BasicParser().parse( options, args );

            if(line.hasOption("run")){
                System.out.println("In run");
                String[] optVals = line.getOptionValues("run");
                for (String val : optVals){
                    System.out.println(val);
                }
                run(optVals);
            }

            if(line.hasOption("help")){
                help(options);
            }

            if(line.hasOption("ontologies")){
                ontologies();
            }

            if(line.hasOption("workflows")){
                workflows();
            }

            if(line.hasOption("evaluationTool")){
                String optVal = line.getOptionValue("evaluationTool");
                evaluationTool(optVal);
            }

        }catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
    }

    private static void run(String[] optVals) {
    }

    private static void ontologies() {
    }

    private static void workflows() {
    }

    private static void evaluationTool(String optVal) {
    }

    public static void help(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "help", options );
    }
}
