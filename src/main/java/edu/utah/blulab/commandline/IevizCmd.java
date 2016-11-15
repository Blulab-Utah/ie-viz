package edu.utah.blulab.commandline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Bill Scuba on 9/20/2016.
 */
public class IevizCmd {

    private static Properties ConfigProperties = null;
    private static Map<String, String[]> ArgMacros = new HashMap<String, String[]>();
    private static String resourceFileName = "resources/config";
    public static String USERNAME_PARAMETER = "KA_username";
    public static String PASSWORD_PARAMETER = "KA_password";

    public static void main(String[] args) {

        readConfigFile();

        // Lee: For running main() with authentication
        if (!localAuthentication()) {
            return;
        }
        runArgs(args);
    }

    private static void run(String[] optVals) {
    }

    private static void runArgs(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("ont", "ontologies", false, "view a list of available ontologies");
        options.addOption("wf", "workflows", false, "provides a list of available NLP workflows.");
        options.addOption("eval", "evaluationTool", true, "specify which evaluation tool to output the results to");
        OptionBuilder builder = OptionBuilder.hasArgs(3).withArgName("ontology> <documents> <workflow");

        // LEE: 10/21/2016
        options.addOption("setKAPassword", true, "Store KA password in config file");
        options.addOption("setKAUsername", true, "Store KA username in config file");

        options.addOption("showconfig", false, "Show configuration file");
        options.addOption("listmacros", false, "List command arg macros");
        options.addOption("createmacro", true, "Create command arg macro");

        builder.withLongOpt("runieviz");
        builder.withDescription("runs ieviz");
        options.addOption(builder.create("run"));

        try {
            CommandLine line = new BasicParser().parse(options, args);

            if (line.hasOption("run")) {
                System.out.println("In run");
                String[] optVals = line.getOptionValues("run");
                for (String val : optVals) {
                    System.out.println(val);
                }
                run(optVals);
            }

            if (line.hasOption("help")) {
                help(options);
            }

            if (line.hasOption("ontologies")) {
                ontologies();
            }

            if (line.hasOption("workflows")) {
                workflows();
            }

            if (line.hasOption("evaluationTool")) {
                String optVal = line.getOptionValue("evaluationTool");
                evaluationTool(optVal);
            }

            if (line.hasOption("showconfig")) {
                showConfigFile();
            }

            if (line.hasOption("listmacros")) {
                listMacros();
            }

            String KAPassword = line.getOptionValue("setKAPassword");
            if (KAPassword != null) {
                setConfigProperty(PASSWORD_PARAMETER, KAPassword);
            }

            String KAUsername = line.getOptionValue("setKAUsername");
            if (KAUsername != null) {
                setConfigProperty(USERNAME_PARAMETER, KAUsername);
            }

            String macroName = line.getOptionValue("createmacro");
            if (macroName != null) {
                createMacro(macroName, args);
            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }

    private static void createMacro(String str, String[] args) {
        ArgMacros.put(str, args);
        listMacros();
    }

    private static void listMacros() {
        if (ArgMacros != null && !ArgMacros.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String macro : ArgMacros.keySet()) {
                sb.append(macro + ": ");
                String[] args = (String[]) ArgMacros.get(macro);
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i < args.length-1) {
                        sb.append(" ");
                    }
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
        } else {
            System.out.println("No macros");
        }
    }

    // Lee, 11/10/2016
    private static void showConfigFile() {
        if (ConfigProperties != null) {
            for (Enumeration e = ConfigProperties.propertyNames(); e.hasMoreElements();) {
                String property = (String) e.nextElement();
                String value = (String) ConfigProperties.getProperty(property);
                System.out.println("Property=" + property + ", Value=" + value);
            }
        }
    }

    private static void ontologies() {

        // LEE: 10/21/2016
        try {
            InputStream json = KAAuthenticator.Authenticator.openAuthenticatedConnection(
                    "https://blulab.chpc.utah.edu/KA/?act=searchd&c=Ontologyc&view=JSON&npp=200&q_status_=Active");
            StringBuffer sb = KAAuthenticator.streamToString(json);
            json.close();
            String jsstr = sb.toString();
            JSONArray jarray = new JSONArray(jsstr);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jo = (JSONObject) jarray.get(i);
                String name = jo.getString("name");
                System.out.println(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void workflows() {
    }

    private static void evaluationTool(String optVal) {
    }

    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("help", options);
    }

    // LEE: 10/18/2016
    public static void readConfigFile() {
        File file = new File(resourceFileName);
        if (file.exists()) {
            try {
                ConfigProperties = new Properties();
                ConfigProperties.load(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeConfigFile() {
        try {
            if (ConfigProperties != null) {
                BufferedWriter out = new BufferedWriter(new FileWriter(resourceFileName));
                StringBuffer sb = new StringBuffer();
                for (Enumeration e = ConfigProperties.keys(); e.hasMoreElements();) {
                    String property = (String) e.nextElement();
                    String value = ConfigProperties.getProperty(property);
                    sb.append(property + " = " + value + "\n");
                }
                out.write(sb.toString());
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getConfigProperty(String property) {
        if (ConfigProperties != null && property != null) {
            return ConfigProperties.getProperty(property);
        }
        return null;
    }

    public static String setConfigProperty(String property, String value) {
        if (ConfigProperties != null && property != null && value != null) {
            ConfigProperties.put(property, value);
            writeConfigFile();
        }
        return null;
    }

    private static boolean localAuthentication() {
        // Lee: TEST
        if (KAAuthenticator.Authenticator == null || KAAuthenticator.Authenticator == null) {
            try {
                KAAuthenticator auth = KAAuthenticator.Authenticator = new KAAuthenticator();
                String username = IevizCmd.getConfigProperty(IevizCmd.USERNAME_PARAMETER);
                String password = IevizCmd.getConfigProperty(IevizCmd.PASSWORD_PARAMETER);
                auth.setUsername(username);
                auth.setPassword(password);
                auth.authenticate();
                if (auth.tempPass != null) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
