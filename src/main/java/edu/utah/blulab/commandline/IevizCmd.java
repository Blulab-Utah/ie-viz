package edu.utah.blulab.commandline;

import org.apache.commons.cli.*;


/**
 * Created by Bill on 9/20/2016.
 */
public class IevizCmd {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("myCmd", "myCommand", false, "will run myCommand()." );
        options.addOption("helloW", "helloWorld", true, "display hello word the number of time specify." );

        System.out.println("1st Argument: " + args[0]);

        try{
            CommandLine line = new BasicParser().parse( options, args );

            if( line.hasOption( "myCommand" ) ) {
                myCommand();
            }

            if(line.hasOption("helloWorld")){
                String repeat = line.getOptionValue("helloWorld");
                Integer repeatInt = new Integer(repeat);
                for(int i =0; i<repeatInt; i++){
                    System.out.println( "Hello world !");
                }
            }

        }catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
    }

    public static void myCommand(){
        System.out.println("myCommand() just got called");
    }
}
