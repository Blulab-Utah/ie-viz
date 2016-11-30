package edu.utah.blulab.commandline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.out;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
    private static String ConfigFilePathname = "src/main/resources/config";
    private static String MacroFilePathname = "src/main/resources/macros";
    public static String USERNAME_PARAMETER = "KA_username";
    public static String PASSWORD_PARAMETER = "KA_password";

    public static void main(String[] args) {
        readResourceFiles();
        runArgs(args);
    }

    private static void runArgs(String[] args) {
        try {
            Options options = gatherOptions(args);
            CommandLine line = new BasicParser().parse(options, args);

            String macroName = line.getOptionValue("createmacro");
            if (macroName != null) {
                createMacro(macroName, args);
                return; // Don't execute the commands within the macro scope
            }

            macroName = line.getOptionValue("runmacro");
            if (macroName != null) {
                runMacro(macroName);
            }

            if (line.hasOption("run")) {
                String[] optVals = line.getOptionValues("run");
                runNLPTool(optVals);
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
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }

    private static Options gatherOptions(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("ont", "ontologies", false, "view a list of available ontologies");
        options.addOption("wf", "workflows", false, "provides a list of available NLP workflows.");
        options.addOption("eval", "evaluationTool", true, "specify which evaluation tool to output the results to");
        OptionBuilder builder = OptionBuilder.hasArgs(3).withArgName("<ontology> <documents> <workflow>");

        // LEE: 10/21/2016
        options.addOption("setKAPassword", true, "Store KA password in config file");
        options.addOption("setKAUsername", true, "Store KA username in config file");

        options.addOption("showconfig", false, "Show configuration file");
        options.addOption("listmacros", false, "List command arg macros");
        options.addOption("createmacro", true, "Create command arg macro");
        options.addOption("runmacro", true, "Run previously defined macro");

        builder.withLongOpt("runieviz");
        builder.withDescription("runs ieviz");
        options.addOption(builder.create("run"));
        return options;
    }

    // optvals:  <input file directory> <domain ontology> <nlp tool to run> 
    //          <output format>
    private static void runNLPTool(String[] optvals) {
        System.out.println("Running NLP tool; args=" + concatenate(optvals, ' '));
    }

    private static void runMacro(String mname) {
        if (mname != null) {
            String[] margs = ArgMacros.get(mname);
            if (margs != null) {
                runArgs(margs);
            } else {
                System.out.println("Error:  Macro \"" + mname + "\" not defined.");
            }
        }
    }

    private static void createMacro(String str, String[] args) {
        String[] margs = gatherMacroArgs(str, args);
        if (margs != null) {
            ArgMacros.put(str, margs);
            writeMacroFile();
            listMacros();
        }
    }

    private static void listMacros() {
        if (ArgMacros != null && !ArgMacros.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String macro : ArgMacros.keySet()) {
                sb.append(macro + ":");
                String[] args = (String[]) ArgMacros.get(macro);
                String argstr = concatenate(args, ' ');
                sb.append(argstr);
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
            int x = 1;
            if (KAAuthenticator.doAuthentication()) {
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

    public static void readMacroFile() {
        File file = new File(MacroFilePathname);
        if (file.exists()) {
            try {
                ArgMacros.clear();
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = null;
                int lineoffset = 0;
                while ((line = in.readLine()) != null) {
                    if (line.length() > 6) {
                        line = line.trim();
                        String[] lstrs = line.split("=");
                        String mname = lstrs[0];
                        String[] args = lstrs[1].split(",");
                        ArgMacros.put(mname, args);
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeMacroFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(MacroFilePathname));
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> iter = ArgMacros.keySet().iterator(); iter.hasNext();) {
                String property = iter.next();
                sb.append(property);
                sb.append("=");
                String[] args = (String[]) ArgMacros.get(property);
                String argstr = concatenate(args, ',');
                sb.append(argstr);
                sb.append("\n");
            }
            out.write(sb.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeConfigFile() {
        try {
            if (ConfigProperties != null) {
                BufferedWriter out = new BufferedWriter(new FileWriter(ConfigFilePathname));
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

    // LEE: 10/18/2016
    public static void readConfigFile() {
        File file = new File(ConfigFilePathname);
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

    public static void readResourceFiles() {
        readConfigFile();
        readMacroFile();
    }

    public static String[] gatherMacroArgs(String mname, String[] args) {
        String[] margs = null;
        if (args != null) {
            int mstart = -1;
            int j = 0;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (mstart < 0 && mname.equals(arg)) {
                    mstart = i + 1;
                    int marglen = args.length - mstart;
                    margs = new String[marglen];
                } else if (mstart > 0) {
                    margs[j++] = arg;
                }
            }
            return margs;
        }
        return margs;
    }

    private static String concatenate(String[] strs, char delim) {
        if (strs != null) {
            String cstr = "";
            for (int i = 0; i < strs.length; i++) {
                cstr += strs[i];
                if (i < strs.length - 1) {
                    cstr += delim;
                }
            }
            return cstr;
        }
        return null;
    }

}
