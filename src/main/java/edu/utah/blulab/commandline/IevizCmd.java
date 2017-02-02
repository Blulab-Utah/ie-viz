package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class IevizCmd {

    private Properties configProperties = null;
    private Map<String, String[]> argMacros = new HashMap<String, String[]>();

    private NLPTool NLPTool = null;
    private HashMap<String, NLPTool> NLPToolMap = new HashMap();

    private static Logger logger = Logger.getLogger(IevizCmd.class.getName());

    public static String USERNAME_PARAMETER = "KA_username";
    public static String PASSWORD_PARAMETER = "KA_password";
    private static String ResourceDirectoryName = "resources";

    private static String ConfigFileName = "config";
    private static String MacroFileName = "macros";

    // private static String ConfigFileName = ResourceDirectoryName +
    // File.separatorChar + "config";
    // private static String MacroFileName = ResourceDirectoryName +
    // File.separatorChar + "macros";
    private static String[][] optionInfo = {{"h", "help", "false", "print this message"},
    {"ont", "ontologies", "false", "view a list of available ontologies"},
    {"wf", "workflows", "false", "provides a list of available NLP workflows."},
    {"eval", "evaluationTool", "true", "specify which evaluation tool to output the results to"},
    {"setKAPassword", "true", "Store KA password in config file"},
    {"setKAUsername", "true", "Store KA username in config file"},
    {"showconfig", "false", "Show configuration file"}, {"listmacros", "false", "List command arg macros"},
    {"createmacro", "true", "Create command arg macro"}, {"rm", "runmacro", "true", "Run macro"},
    {"run", "runieviz", "runs ieviz", "<toolname> <ontology> <inputdir>", "3"},
    {"iterate", "false", "Allow iterative user input"},
    {"workbench", "false", "Start Evaluation Workbench"},
    };

    public static void main(String[] args) {
        IevizCmd iec = new IevizCmd();
        try {
            iec.readConfigFile();
            iec.readMacroFile();
            iec.localAuthentication();
            iec.runArgs(args);
        } catch (Exception e) {
            iec.handleError(e);
        }
    }

    private void run(String[] optVals) throws CommandLineException {
        try {
            String toolname = optVals[0].toLowerCase();
            String oname = optVals[1].toLowerCase();
            String inputdir = optVals[2];
            String outputdir = optVals[3];
            DomainOntology ontology = getDomainOntology(oname);
            NLPTool tool = this.NLPToolMap.get(toolname);
            if (tool == null) {
                if ("moonstone".equals(toolname)) {
                    tool = new MoonstoneNLPTool(ontology, inputdir);
                    this.NLPToolMap.put(toolname, tool);
                } else if ("dummy".equals(toolname)) {
                    tool = new DummyNLPTool(ontology, inputdir);
                    this.NLPToolMap.put(toolname, tool);
                }
            }
            if (tool != null) {
                tool.processFiles();
            } else {
                throw new CommandLineException("runNLPTool: Unable to process: Tool=" + toolname + ", Ontology="
                        + oname + ", Inputdir=" + inputdir + ", Outputdir=" + outputdir);
            }
        } catch (Exception e) {
            throw new CommandLineException("runNLPTool: " + e.toString());
        }
    }

    private void runArgs(String[] args) throws CommandLineException {
        try {
            Options options = extractOptions(args);
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);
            String astr;

            if ((astr = line.getOptionValue("rm")) != null) {
                runMacro(astr);
            } else if ((astr = line.getOptionValue("createmacro")) != null) {
                createMacro(astr, args);
            } else if (line.hasOption("run")) {
                String[] optVals = line.getOptionValues("run");
                run(optVals);
            } else if (line.hasOption("help")) {
                help(options);
            } else if (line.hasOption("ontologies")) {
                ontologies();
            } else if (line.hasOption("workflows")) {
                workflows();
            } else if (line.hasOption("evaluationTool")) {
                String optVal = line.getOptionValue("evaluationTool");
                evaluationTool(optVal);
            } else if (line.hasOption("showconfig")) {
                showConfigFile();
            } else if (line.hasOption("listmacros")) {
                listMacros();
            } else if ((astr = line.getOptionValue("setKAPassword")) != null) {
                setConfigProperty(PASSWORD_PARAMETER, astr);
            } else if ((astr = line.getOptionValue("setKAUsername")) != null) {
                setConfigProperty(USERNAME_PARAMETER, astr);
            } else if (line.hasOption("iterate")) {
                iterateUserInput();
            } else if (line.hasOption("workbench")) {
            	EvaluationWorkbenchTool ewt = new EvaluationWorkbenchTool();
            }
        } catch (Exception e) {
            throw new CommandLineException(e.toString());
        }
    }

    private Options extractOptions(String[] args) throws CommandLineException {
        Options options = new Options();
        CommandLine line = null;
        for (String[] oinfo : optionInfo) {
            try {
                switch (oinfo.length) {
                    case 3:
                        String opt = oinfo[0];
                        Boolean hasArg = Boolean.parseBoolean(oinfo[1]);
                        String description = oinfo[2];
                        options.addOption(opt, hasArg, description);
                        break;
                    case 4:
                        opt = oinfo[0];
                        String longer = oinfo[1];
                        hasArg = Boolean.parseBoolean(oinfo[2]);
                        description = oinfo[3];
                        options.addOption(opt, longer, hasArg, description);
                        break;
                    case 5:
                        opt = oinfo[0];
                        longer = oinfo[1];
                        description = oinfo[2];
                        String template = oinfo[3];
                        int numargs = Integer.parseInt(oinfo[4]);
                        Option o = OptionBuilder.hasArgs(numargs).withArgName(template).withLongOpt(longer)
                                .withDescription(description).create(opt);
                        options.addOption(o);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                throw new CommandLineException("Invalid command line definition: " + oinfo);
            }
        }
        return options;
    }

    private void iterateUserInput() throws CommandLineException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean quit = false;
        String str = null;
        String laststr = null;
        while (!quit) {
            System.out.print("IEViz Command Line> ");
            try {
                str = in.readLine();
                if (str != null && str.toLowerCase().contains("quit")) {
                    return;
                }
                if ("!!".equals(str) && laststr != null) {
                    str = laststr;
                }
                String[] args = str.split(" ");
                runArgs(args);
            } catch (Exception e) {
                System.out.println("Error executing command: " + str);
            }
        }
    }
    
    private DomainOntology getDomainOntology(String oname) {
        return null;
    }

    private void createMacro(String str, String[] args) throws CommandLineException {
        String[] margs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            margs[i - 1] = args[i];
        }
        argMacros.put(str, margs);
        writeMacroFile();
    }

    public void runMacro(String mname) throws CommandLineException {
        try {
            String[] margs = this.argMacros.get(mname);
            if (margs == null) {
                throw new CommandLineException("No macro defined for: " + mname);
            }
            this.runArgs(margs);
        } catch (Exception e) {
            throw new CommandLineException("Unable to run macro " + mname + ": " + e.toString());
        }
    }

    private void listMacros() throws CommandLineException {
        if (argMacros != null && !argMacros.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String macro : argMacros.keySet()) {
                sb.append(macro + ": ");
                String[] args = (String[]) argMacros.get(macro);
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i < args.length - 1) {
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
    private void showConfigFile() throws CommandLineException {
        if (configProperties != null) {
            for (Enumeration e = configProperties.propertyNames(); e.hasMoreElements();) {
                String property = (String) e.nextElement();
                String value = (String) configProperties.getProperty(property);
                System.out.println("Property=" + property + ", Value=" + value);
            }
        }
    }

    private void ontologies() throws CommandLineException {
        try {
            InputStream json = KAAuthenticator.Authenticator.openAuthenticatedConnection(
                    "https://blulab.chpc.utah.edu/KA/?act=searchd&c=Ontologyc&view=JSON&npp=200&q_status_=Active");
            String jsstr = Utilities.convertStreamToString(json);
            if (jsstr != null) {
                JSONArray jarray = new JSONArray(jsstr);
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jo = (JSONObject) jarray.get(i);
                    String name = jo.getString("name");
                    System.out.println(name);
                }
            } else {
                throw new CommandLineException("Unable to display ontologies: Server returned null string");
            }

        } catch (Exception e) {
            throw new CommandLineException("Unable to display ontologies: " + e.toString());
        }
    }

    private String getOntologyXML() throws CommandLineException {
        try {
            InputStream is = KAAuthenticator.Authenticator.openAuthenticatedConnection(
                    "https://blulab.chpc.utah.edu/KA/?act=searchd&c=Ontologyc&view=JSON&npp=200&q_status_=Active");
            String xml = Utilities.convertStreamToString(is);
            return xml;
        } catch (Exception e) {
            throw new CommandLineException("Unable to display ontologies: " + e.toString());
        }
    }

    private void workflows() throws CommandLineException {
    }

    private void evaluationTool(String optVal) throws CommandLineException {
    }

    public void help(Options options) throws CommandLineException {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("help", options);
    }

    public void readConfigFile() throws CommandLineException {
        try {
            File file = Utilities.getResourceFile(this.getClass(), ConfigFileName);
            if (file != null && file.exists()) {
                configProperties = new Properties();
                configProperties.load(new FileReader(file));
            } else {
                throw new CommandLineException("Unable to read config file:  File does not exist");
            }
        } catch (Exception e) {
            throw new CommandLineException("Unable to read config file: " + e.toString());
        }
    }

    public void writeConfigFile() throws CommandLineException {
        try {
            File file = Utilities.getResourceFile(this.getClass(), ConfigFileName);
            if (configProperties != null && file != null && file.exists()) {
                BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
                StringBuffer sb = new StringBuffer();
                for (Enumeration<Object> e = configProperties.keys(); e.hasMoreElements();) {
                    String property = (String) e.nextElement();
                    String value = configProperties.getProperty(property);
                    sb.append(property + " = " + value + "\n");
                }
                out.write(sb.toString());
                out.close();
            }
        } catch (Exception e) {
            throw new CommandLineException("Unable to write config file: " + e.toString());
        }
    }

    public void readMacroFile() throws CommandLineException {
        try {
            File file = Utilities.getResourceFile(this.getClass(), MacroFileName);
            if (file != null && file.exists()) {
                this.argMacros = new HashMap<String, String[]>();
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = in.readLine()) != null) {
                    String[] mstrs = line.split(" ");
                    String mname = mstrs[0];
                    String[] margs = new String[mstrs.length - 1];
                    for (int i = 1; i < mstrs.length - 1; i++) {
                        margs[i] = mstrs[i + 1];
                    }
                    this.argMacros.put(mname, margs);
                }
                in.close();
            }
        } catch (Exception e) {
            throw new CommandLineException("Unable to read Macro file: " + e.toString());
        }
    }

    public void writeMacroFile() throws CommandLineException {
        try {
            File file = Utilities.getResourceFile(this.getClass(), MacroFileName);
            if (file != null && file.exists() && this.argMacros != null && !this.argMacros.isEmpty()) {
                StringBuffer sb = new StringBuffer();
                BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
                for (String key : this.argMacros.keySet()) {
                    String[] margs = this.argMacros.get(key);
                    String mstr = "";
                    for (String marg : margs) {
                        mstr += marg + " ";
                    }
                    mstr = mstr.trim();
                    sb.append(mstr + "\n");
                }
                out.write(sb.toString());
                out.close();
            }
        } catch (Exception e) {
            throw new CommandLineException("Unable to write Macro file: " + e.toString());
        }
    }

    public String getConfigProperty(String property) throws CommandLineException {
        if (configProperties != null && property != null) {
            return configProperties.getProperty(property);
        }
        return null;
    }

    public String setConfigProperty(String property, String value) throws CommandLineException {
        if (configProperties != null && property != null && value != null) {
            configProperties.put(property, value);
            writeConfigFile();
        }
        return null;
    }

    protected void handleError(Exception e) {
        if (e instanceof CommandLineException) {
            CommandLineException cle = (CommandLineException) e;
            System.out.println(cle.getMessage());
        } else {
            e.printStackTrace();
        }
    }

    private boolean localAuthentication() throws CommandLineException {
        // Lee: TEST
        if (KAAuthenticator.Authenticator == null || KAAuthenticator.Authenticator == null) {
            try {
                KAAuthenticator auth = KAAuthenticator.Authenticator = new KAAuthenticator();
                String username = this.getConfigProperty(IevizCmd.USERNAME_PARAMETER);
                String password = this.getConfigProperty(IevizCmd.PASSWORD_PARAMETER);
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
