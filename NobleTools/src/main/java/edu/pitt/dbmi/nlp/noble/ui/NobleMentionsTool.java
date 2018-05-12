package edu.pitt.dbmi.nlp.noble.ui;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.eval.AnnotationEvaluation;
import edu.pitt.dbmi.nlp.noble.mentions.NobleMentions;
import edu.pitt.dbmi.nlp.noble.mentions.model.Composition;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.ontology.DefaultRepository;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * process a set of reports and generate an HTML to get.
 *
 * @author tseytlin
 */
public class NobleMentionsTool implements ActionListener {
    private final URL LOGO_ICON = getClass().getResource("/icons/NobleLogo256.png");
    private final URL CANCEL_ICON = getClass().getResource("/icons/cancel16.png");
    private JFrame frame;
    private JTextField input, output;
    private JList<DomainOntology> ontologyList;
    private JTextArea console;
    private JProgressBar progress;
    private JPanel buttonPanel, progressPanel, optionsPanel;
    private JButton run;
    private File lastFile;
    private long totalTime;
    private long processCount;
    private HTMLExporter htmlExporter;
    private CSVExporter csvExporter;
    private static boolean statandlone = false;
    private DefaultRepository repository = new DefaultRepository();
    private boolean cancelRun;
    private Map<String, Long> processTime, processTimeCount;


    // options
    private ButtonGroup annotationScope;
    private JCheckBox processHeaderAnchor;
    private JCheckBox processHeaderModifier;
    private JCheckBox normalizeAnchors;
    private JCheckBox scoreAnchors;
    private JCheckBox ignoreLabels;
    private JRadioButton sectionScope, paragraphScope;


    public void ievizProcess(Map<String,String> pathMap) throws Exception {
        statandlone = true;
        NobleMentionsTool nc = new NobleMentionsTool();
        if (pathMap.size()==3) {
            File ontology = new File(pathMap.get("ont"));
            File input = new File(pathMap.get("input"));
            File output = new File(pathMap.get("output"));
            File properties = null;

            //check if inputs exist
            for (File a : Arrays.asList(ontology, input, output)) {
                if (!a.exists()) {
                    System.err.println("Error: file or directory " + a + " doesn't exist");
                    System.exit(1);
                }
            }

            // find ontology
            for (File f : ontology.listFiles()) {
                if (f.getName().endsWith(".owl") && !ConText.IMPORTED_ONTOLOGIES.contains(FileTools.stripExtension(f.getName()))) {
                    ontology = f;
                    break;
                } else if (f.getName().endsWith(".properties")) {
                    properties = f;
                }
            }
            if (!ontology.isFile()) {
                System.err.println("Error: could not find domain ontology in " + ontology.getAbsolutePath());
                System.exit(1);
            }
            nc.process(ontology, input, output, properties);

        }
    }


    /**
     * What .
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        statandlone = true;
        NobleMentionsTool nc = new NobleMentionsTool();
        if (args.length == 0) {
            nc.showDialog();
            nc.printUsage(System.out);
        } else if (args.length == 1 && "-docker".equals(args[0])) {
            File ontology = new File("C:\\Users\\Deep\\Documents\\noble\\ont");
            File input = new File("C:\\Users\\Deep\\Documents\\noble\\input");
            File output = new File("C:\\Users\\Deep\\Documents\\noble\\output");
            File properties = null;

            //check if inputs exist
            for (File a : Arrays.asList(ontology, input, output)) {
                if (!a.exists()) {
                    System.err.println("Error: file or directory " + a + " doesn't exist");
                    System.exit(1);
                }
            }

            // find ontology
            for (File f : ontology.listFiles()) {
                if (f.getName().endsWith(".owl") && !ConText.IMPORTED_ONTOLOGIES.contains(FileTools.stripExtension(f.getName()))) {
                    ontology = f;
                    break;
                } else if (f.getName().endsWith(".properties")) {
                    properties = f;
                }
            }
            if (!ontology.isFile()) {
                System.err.println("Error: could not find domain ontology in " + ontology.getAbsolutePath());
                System.exit(1);
            }
            nc.process(ontology, input, output, properties);

        } else if (args.length >= 3) {
            //check if inputs exist
            for (String a : args) {
                if (!new File(a).exists()) {
                    System.err.println("Error: file or directory " + a + " doesn't exist");
                    System.exit(1);
                }
            }
            // check for runtime properties
            File props = null;
            if (args.length > 3) {
                //nc.loadOptionsSettings(new File(args[3]));
                props = new File(args[3]);
            }
            //nc.process(new DomainOntology(args[0]),args[1],args[2]);
            nc.process(new File(args[0]), new File(args[1]), new File(args[2]), props);
        } else {
            nc.printUsage(System.out);
        }
    }

    /**
     * print usage statement.
     *
     * @param out the out
     */
    private void printUsage(PrintStream out) {
        out.println("Usage: java -jar NobleMentions-1.1.jar");
        out.println("\tInvoke NobleMentions UI and run it interactively");
        out.println("Usage: java -jar NobleMentions-1.1.jar <ontology file> <input directory> <output directory> [properties file]");
        out.println("\tRun NobleMentions via command line with a given properties file.");
        out.println("\t<ontology file> - domain ontology OWL file");
        out.println("\t<input directory> - directory full of input text files that need to be processed");
        out.println("\t<output directory> - directory where output will be saved");
        out.println("\t<properties file> - key=value pair file that sets runtime options");
        out.println("Usage: java -jar NobleMentions-1.1.jar -docker");
        out.println("\tInvoke NobleMentions assuming that it is running within a docker container");
        out.println("\t/ontology - domain ontology OWL file and its dependencies needs to be located here");
        out.println("\t/input - directory full of input text files that need to be processed should be located here");
        out.println("\t/output - directory where output will be saved");
        out.println("\t/ontology - key=value pair file that sets runtime options will be located at the same location as ontology");
        out.println("\n\n");
    }


    /**
     * create dialog for noble coder.
     */
    public void showDialog() {
        if (frame == null) {
            frame = new JFrame("Noble Mentions");
            frame.setDefaultCloseOperation(statandlone ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
            frame.setJMenuBar(getMenuBar());
            frame.setIconImage(new ImageIcon(LOGO_ICON).getImage());

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
            GridBagLayout l = new GridBagLayout();
            l.setConstraints(panel, c);
            panel.setLayout(l);

            input = new JTextField(30);
            ontologyList = new JList(new DefaultListModel<DomainOntology>());
            JButton browse = new JButton("Browse");
            browse.addActionListener(this);
            browse.setActionCommand("i_browser");


            JButton options = new JButton("Options");
            options.setActionCommand("options");
            options.addActionListener(this);
            JButton add = new JButton("Add");
            add.setActionCommand("import");
            add.addActionListener(this);
            JButton remove = new JButton("Remove");
            remove.setActionCommand("remove");
            remove.addActionListener(this);
            JButton info = new JButton("Preview");
            info.setActionCommand("preview");
            info.addActionListener(this);
            JScrollPane scroll = new JScrollPane(ontologyList);
            scroll.setPreferredSize(new Dimension(100, 150));

            JButton eval = new JButton("Evaluate");
            eval.setActionCommand("eval");
            eval.addActionListener(this);

            panel.add(new JLabel("Input Schema"), c);
            c.gridx++;
            c.gridheight = 5;
            panel.add(scroll, c);
            c.gridx++;
            c.gridheight = 1;
            panel.add(add, c);
            c.gridy++;
            panel.add(remove, c);
            c.gridy++;
            panel.add(info, c);
            c.gridy++;
            panel.add(options, c);
            c.gridy++;
            panel.add(eval, c);
            c.gridy++;
            c.gridx = 0;
            panel.add(new JLabel("Input Directory "), c);
            c.gridx++;
            panel.add(input, c);
            c.gridx++;
            panel.add(browse, c);
            c.gridx = 0;
            c.gridy++;

            output = new JTextField(30);
            browse = new JButton("Browse");
            browse.addActionListener(this);
            browse.setActionCommand("o_browser");

            panel.add(new JLabel("Output Directory"), c);
            c.gridx++;
            panel.add(output, c);
            c.gridx++;
            panel.add(browse, c);
            c.gridx = 0;
            c.gridy++;
            panel.add(Box.createRigidArea(new Dimension(10, 10)), c);

            JPanel conp = new JPanel();
            conp.setLayout(new BorderLayout());
            conp.setBorder(new TitledBorder("Output Console"));
            console = new JTextArea(10, 40);
            //console.setLineWrap(true);
            console.setEditable(false);
            conp.add(new JScrollPane(console), BorderLayout.CENTER);
            //c.gridwidth=3;
            //panel.add(conp,c);c.gridy++;c.gridx=0;

            buttonPanel = new JPanel();
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
            run = new JButton("Run Noble Mentions");
            run.addActionListener(this);
            run.setActionCommand("run");
            buttonPanel.add(run, BorderLayout.CENTER);
            //panel.add(buttonPanel,c);

            JButton cancel = new JButton(new ImageIcon(CANCEL_ICON));
            cancel.setToolTipText("Cancel Run");
            cancel.addActionListener(this);
            cancel.setActionCommand("cancel");

            progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.setString("Please Wait. It will take a while ...");
            progress.setStringPainted(true);

            progressPanel = new JPanel();
            progressPanel.setLayout(new BorderLayout());
            progressPanel.add(progress, BorderLayout.CENTER);
            progressPanel.add(cancel, BorderLayout.EAST);


            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(panel, BorderLayout.NORTH);
            p.add(conp, BorderLayout.CENTER);
            p.add(buttonPanel, BorderLayout.SOUTH);


            // wrap up, and display
            frame.setContentPane(p);
            frame.pack();

            //center on screen
            Dimension d = frame.getSize();
            Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(new Point((s.width - d.width) / 2, (s.height - d.height) / 2));

            frame.setVisible(true);
            // load defaults
            loadDeafaults();
            loadSettings();
        } else {
            frame.setVisible(true);
        }
    }

    /**
     * save UI settings
     */
    private void saveSettings() {
        Properties p = new Properties();
        p.setProperty("ontology", ontologyList.getSelectedValue().toString());
        p.setProperty("input", input.getText());
        p.setProperty("output", output.getText());
        UITools.saveSettings(p, getClass());
    }

    /**
     * save UI settings
     */
    private void saveOptionsSettings() {
        if (ontologyList == null)
            return;
        DomainOntology ontology = ontologyList.getSelectedValue();
        if (ontology != null) {
            Properties p = getOptionsSettings();
            UITools.saveSettings(p, new File(ontology.getOntologyLocation().getParentFile(), ontology.getName() + ".properties"));
        }
    }


    private Properties getOptionsSettings() {
        getOptionsPanel();
        Properties p = new Properties();
        p.setProperty("annotation.relation.scope", annotationScope.getSelection().getActionCommand());
        p.setProperty("process.header.anchors", "" + processHeaderAnchor.isSelected());
        p.setProperty("process.header.modifiers", "" + processHeaderModifier.isSelected());
        p.setProperty("normalize.anchors", "" + normalizeAnchors.isSelected());
        p.setProperty("score.anchors", "" + scoreAnchors.isSelected());
        p.setProperty("ignore.labels", "" + ignoreLabels.isSelected());
        return p;
    }


    /**
     * save UI settings
     */
    private void loadOptionsSettings() {
        getOptionsPanel();
        DomainOntology ontology = ontologyList.getSelectedValue();
        if (ontology != null) {
            loadOptionsSettings(new File(ontology.getOntologyLocation().getParentFile(), ontology.getName() + ".properties"));
        }
    }

    /**
     * save UI settings
     */
    private void loadOptionsSettings(File file) {
        getOptionsPanel();
        final Properties p = UITools.loadSettings(file);
        sectionScope.setSelected("section".equals(p.getProperty("annotation.relation.scope")));
        processHeaderAnchor.setSelected(Boolean.parseBoolean(p.getProperty("process.header.anchors")));
        processHeaderModifier.setSelected(Boolean.parseBoolean(p.getProperty("process.header.modifiers")));
        normalizeAnchors.setSelected(Boolean.parseBoolean(p.getProperty("normalize.anchors")));
        scoreAnchors.setSelected(Boolean.parseBoolean(p.getProperty("score.anchors")));
        ignoreLabels.setSelected(Boolean.parseBoolean(p.getProperty("ignore.labels")));
    }


    private void loadOptions(NobleMentions nobleMentions) {
        getOptionsPanel();
        nobleMentions.setProcessAnchorsInHeader(processHeaderAnchor.isSelected());
        nobleMentions.setProcessModifiersInHeader(processHeaderModifier.isSelected());
        nobleMentions.getDomainOntology().setAnnotatioRelationSkope(annotationScope.getSelection().getActionCommand());
        nobleMentions.getDomainOntology().setNormalizeAnchorTerms(normalizeAnchors.isSelected());
        nobleMentions.getDomainOntology().setScoreAnchors(scoreAnchors.isSelected());
        saveOptionsSettings();
    }


    /**
     * save UI settings
     */
    private void loadSettings() {
        final Properties p = UITools.loadSettings(getClass());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (p.containsKey("input"))
                    input.setText(p.getProperty("input"));
                if (p.containsKey("output"))
                    output.setText(p.getProperty("output"));
                if (p.containsKey("ontology")) {
                    selectOntology(p.getProperty("ontology"));
                }
            }
        });

    }

    /**
     * Load deafaults.
     */
    private void loadDeafaults() {
        (new Thread(new Runnable() {
            public void run() {
                setBusy(true);
                refreshTemplateList();
                setBusy(false);
            }
        })).start();
    }

    /**
     * Refresh template list.
     */
    private void refreshTemplateList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                repository.reset();
                ((DefaultListModel<DomainOntology>) ontologyList.getModel()).removeAllElements();
                IOntology[] ontologies = repository.getOntologies();
                Arrays.sort(ontologies, new Comparator<IOntology>() {
                    public int compare(IOntology o1, IOntology o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (IOntology t : ontologies) {
                    // filter out ontologies that are dependency
                    if (ConText.IMPORTED_ONTOLOGIES.contains(t.getName()))
                        continue;
                    try {
                        ((DefaultListModel<DomainOntology>) ontologyList.getModel()).addElement(new DomainOntology(t));
                    } catch (IOntologyException e) {
                        e.printStackTrace();
                    }
                }
                ontologyList.validate();
            }
        });
    }


    /**
     * set busy .
     *
     * @param b the new busy
     */
    private void setBusy(boolean b) {
        final boolean busy = b;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buttonPanel.removeAll();
                if (busy) {
                    progress.setIndeterminate(true);
                    progress.setString("Please Wait. It may take a while ...");
                    progress.setStringPainted(true);
                    buttonPanel.add(progressPanel, BorderLayout.CENTER);
                    console.setText("");
                } else {
                    buttonPanel.add(run, BorderLayout.CENTER);
                }
                buttonPanel.validate();
                buttonPanel.repaint();

            }
        });
    }


    /**
     * Gets the menu bar.
     *
     * @return the menu bar
     */
    private JMenuBar getMenuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(this);
        file.add(exit);
        menu.add(file);
        return menu;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("run".equals(cmd)) {
            doRun();
        } else if ("cancel".equals(cmd)) {
            cancelRun = true;
        } else if ("i_browser".equals(cmd)) {
            doBrowse(input);
        } else if ("d_browser".equals(cmd)) {

        } else if ("o_browser".equals(cmd)) {
            doBrowse(output);
        } else if ("exit".equals(cmd)) {
            System.exit(0);
        } else if ("export".equals(cmd)) {
            doExport();
        } else if ("options".equals(cmd)) {
            doOptions();
        } else if ("import".equals(cmd)) {
            doImport();
        } else if ("remove".equals(cmd)) {
            doRemove();
        } else if ("preview".equals(cmd)) {
            doPreview();
        } else if ("eval".equals(cmd)) {
            doEvaluate();
        }
    }

    private JPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new JPanel();

            optionsPanel.setLayout(new BorderLayout());
            optionsPanel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(10, 10, 10, 10)));
            Color blue = new Color(100, 100, 255);

            // runtime options
            JPanel panel1 = new JPanel();

            TitledBorder border = new TitledBorder(new LineBorder(blue), "Runtime options");
            border.setTitleColor(blue);

            panel1.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 5, 0), border));
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
            GridBagLayout l = new GridBagLayout();
            l.setConstraints(panel1, c);
            panel1.setLayout(l);

            // init controls
            sectionScope = new JRadioButton("Section", false);
            sectionScope.setActionCommand(DomainOntology.SECTION_SCOPE);
            paragraphScope = new JRadioButton("Paragraph", true);
            paragraphScope.setActionCommand(DomainOntology.PARAGRAPH_SCOPE);
            annotationScope = new ButtonGroup();
            annotationScope.add(sectionScope);
            annotationScope.add(paragraphScope);

            processHeaderAnchor = new JCheckBox("Anchors");
            processHeaderModifier = new JCheckBox("Modifiers");

            panel1.add(new JLabel("Scope of annotation to annotation relation"), c);
            c.gridx++;
            panel1.add(sectionScope, c);
            c.gridx++;
            panel1.add(paragraphScope, c);
            c.gridy++;
            c.gridx = 0;

            panel1.add(new JLabel("Process section header for"), c);
            c.gridx++;
            panel1.add(processHeaderAnchor, c);
            c.gridx++;
            panel1.add(processHeaderModifier, c);
            c.gridy++;
            c.gridx = 0;

            // set dictionary options
            normalizeAnchors = new JCheckBox("Normalize anchor terms");
            scoreAnchors = new JCheckBox("Score matched anchor terms");
            ignoreLabels = new JCheckBox("Ignore class labels as valid terms");

            JPanel panel2 = new JPanel();
            border = new TitledBorder(new LineBorder(blue), "Dictionary building options");
            border.setTitleColor(blue);
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
            panel2.setBorder(border);
            panel2.add(normalizeAnchors);
            panel2.add(scoreAnchors);
            panel2.add(ignoreLabels);

            optionsPanel.add(panel1, BorderLayout.CENTER);
            optionsPanel.add(panel2, BorderLayout.SOUTH);


        }
        return optionsPanel;
    }

    private void doOptions() {
        loadOptionsSettings();
        boolean na = normalizeAnchors.isSelected();
        boolean sa = scoreAnchors.isSelected();
        boolean il = ignoreLabels.isSelected();

        int r = JOptionPane.showConfirmDialog(frame, getOptionsPanel(), "Runtime Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION == r) {
            DomainOntology ont = ontologyList.getSelectedValue();
            // if terminolgy rebuilding options changed, warn about them
            if (ont != null && ont.getTerminologyCacheLocation().exists() && (na != normalizeAnchors.isSelected() || sa != scoreAnchors.isSelected() || il != ignoreLabels.isSelected())) {
                int rr = JOptionPane.showConfirmDialog(frame,
                        "<html>You have changed one of the <font color=blue>dictionary building options</font>. <br>" +
                                "Are you sure want to re-generate cached dictionaries for <font color=red> " + ont.getName() + "</font> domain?<br>" +
                                "It will take additional time to re-generate dictionaries.", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (rr == JOptionPane.YES_OPTION) {
                    FileTools.deleteDirectory(ont.getTerminologyCacheLocation());
                    saveOptionsSettings();
                } else {
                    loadOptionsSettings();
                }
            } else {
                saveOptionsSettings();
            }
        } else {
            //roll back the settings
            loadOptionsSettings();
        }
    }


    private void doEvaluate() {
        AnnotationEvaluation ae = new AnnotationEvaluation();
        JDialog dialog = ae.getDialog(frame);
        DomainOntology ontology = ontologyList.getSelectedValue();
        if (ontology != null) {
            String name = ontology.getName() + "Instances.owl";
            ae.setSystemInstanceOntlogy(output.getText() + File.separator + name);
        }
        ae.setInputDocuments(input.getText());
        dialog.setVisible(true);
    }


    /**
     * do preview.
     */
    private void doPreview() {
        final DomainOntology t = ontologyList.getSelectedValue();
        if (t == null)
            return;
        new Thread(new Runnable() {
            public void run() {
                setBusy(true);
                TerminologyBrowser browser = new TerminologyBrowser();
                browser.setTerminologies(t.getTerminologies());
                browser.showDialog(null, "NobleMentions");
                setBusy(false);

            }
        }).start();


    }


    /**
     * do export of highlighted template.
     */
    private void doExport() {
        DomainOntology template = ontologyList.getSelectedValue();
        if (template != null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".owl");
                }

                public String getDescription() {
                    return "OWL File";
                }

            });
            chooser.setSelectedFile(new File(template.getOntology().getName()));
            int r = chooser.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = chooser.getSelectedFile();
                    Files.copy(new File(template.getOntology().getLocation()).toPath(), f.toPath());
                    //FileOutputStream out = new FileOutputStream(f);
                    //templateFactory.exportTemplate(template, out);
                    //out.close();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    private void doRemove() {
        DomainOntology ont = ontologyList.getSelectedValue();
        if (ont != null) {
            int r = JOptionPane.showConfirmDialog(frame, "<html>Are you sure you want to delete selected schema: <font color=red>" + ont.getName() + "<font>", "Question", JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == r) {
                if (ont.getOntologyLocation().exists())
                    ont.getOntologyLocation().delete();
                if (ont.getTerminologyCacheLocation().exists())
                    FileTools.deleteDirectory(ont.getTerminologyCacheLocation());
                refreshTemplateList();

            }

        }
    }


    /**
     * do export of highlighted template.
     */
    private void doImport() {
        final JFileChooser chooser = new JFileChooser(lastFile);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".owl");
            }

            public String getDescription() {
                return "OWL lastFile (extending Schema.owl)";
            }

        });
        int r = chooser.showOpenDialog(frame);
        if (r == JFileChooser.APPROVE_OPTION) {

            new Thread(new Runnable() {
                public void run() {
                    String ont = null;
                    setBusy(true);
                    try {
                        lastFile = chooser.getSelectedFile();
                        File newLocation = new File(repository.getOntologyLocation(), lastFile.getName());
                        Files.copy(lastFile.toPath(), newLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        ont = FileTools.stripExtension(newLocation.getName());

                        // remove terminology cache
                        File termCache = DomainOntology.getTerminologyCacheLocation(newLocation);
                        if (termCache.exists()) {
                            FileTools.deleteDirectory(termCache);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                    refreshTemplateList();
                    selectOntology(ont);

                    setBusy(false);
                }
            }).start();

        }
    }

    /**
     * select ontology
     *
     * @param ont
     */
    private void selectOntology(final String ont) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int index = -1;
                for (int i = 0; i < ontologyList.getModel().getSize(); i++) {
                    if (ontologyList.getModel().getElementAt(i).toString().equals(ont)) {
                        index = i;
                        break;
                    }
                }
                if (index > -1)
                    ontologyList.setSelectedIndex(index);
            }
        });
    }


    /**
     * check UI inputs.
     *
     * @return true, if successful
     */
    private boolean checkInputs() {
        if (ontologyList.getSelectedValuesList().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please Select the Ontology");
            return false;
        }
        if (!new File(input.getText()).exists()) {
            JOptionPane.showMessageDialog(frame, "Please Select Input Report Directory");
            return false;
        }
        return true;
    }

    /**
     * run the damn thing.
     */
    private void doRun() {
        (new Thread(new Runnable() {
            public void run() {
                if (!checkInputs()) {
                    return;
                }
                setBusy(true);
                updateOutputLocation();
                cancelRun = false;

                // save settings
                saveSettings();

                DomainOntology ontology = ontologyList.getSelectedValue();
                final String ontName = ontology.getName();

                // setup progress bar
                if (progress != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progress.setIndeterminate(true);
                            progress.setString("Loading " + ontName + " ...");
                        }
                    });
                }

                // create just-in-time instance lastFile
                try {
                    long t = System.currentTimeMillis();
                    progress("loading " + ontName + " ontology .. ");
                    ontology = new DomainOntology(ontology.getOntology().getLocation());
                    progress((System.currentTimeMillis() - t) + " ms\n");
                } catch (IOntologyException e1) {
                    UITools.showErrorDialog(frame,
                            "<html>Could not load imported ontologies.<br>"
                                    + "To procede in offline mode, please add imported ontologies to the local cache.<br> "
                                    + "Please see documentation for details.");
                    progress("\n" + e1.getMessage() + "\n");
                    if (e1.getCause() != null)
                        progress(e1.getCause().getMessage());
                    e1.printStackTrace();
                    setBusy(false);
                    return;
                }


                // check if it is valid
                if (!ontology.isOntologyValid()) {
                    JOptionPane.showMessageDialog(frame, "Selected ontology " + ontology.getName() + " is not a valid " + DomainOntology.SCHEMA_OWL + " ontology", "Error", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                    return;
                }

                try {
                    process(ontology, input.getText(), output.getText());
                } catch (Exception e) {
                    UITools.showErrorDialog(frame, e);
                }


                setBusy(false);

                // open in browser
                try {
                    UITools.browseURLInSystemBrowser(new File(output.getText() + File.separator + "index.html").toURI().toString());
                } catch (Exception ex) {
                    UITools.showErrorDialog(frame, ex);
                }


            }


        })).start();
    }


    /**
     * Do browse.
     *
     * @param text the text
     */
    private void doBrowse(JTextField text) {
        File file = new File(text.getText());
        JFileChooser fc = new JFileChooser(file);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.addChoosableFileFilter(new FileFilter() {
            public String getDescription() {
                return "Text files (.txt)";
            }

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".txt");
            }
        });

        int r = (output == text) ? fc.showSaveDialog(frame) : fc.showOpenDialog(frame);
        if (r == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            text.setText(file.getAbsolutePath());

            // if input, change output to default
            if (text == input) {
                setDefaultOutputLocation();
            }
        }
    }

    /**
     * set default output location, based on input file
     */
    private void setDefaultOutputLocation() {
        // derive output from input
        File file = new File(input.getText());
        if (file.exists()) {
            String prefix = file.getName();
            if (prefix.endsWith(".txt"))
                prefix = prefix.substring(0, prefix.length() - 4);
            prefix = prefix + File.separator + (new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date(System.currentTimeMillis())));
            output.setText(new File(file.getParent() + File.separator + "Output" + File.separator + prefix).getAbsolutePath());
        }
    }

    /**
     * set default output location, based latest date time
     */
    private void updateOutputLocation() {
        File file = new File(output.getText());
        Pattern pt = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2}");
        Matcher mt = pt.matcher(file.getName());
        if (mt.matches()) {
            String date = (new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date(System.currentTimeMillis())));
            output.setText(new File(file.getParentFile(), date).getAbsolutePath());
        }
    }


    private void process(File ont, File in, File out, File props) {
        try {
            // initialize ontology
            progress("loading " + ont.getName() + " ontology .. ");
            long t = System.currentTimeMillis();
            DomainOntology ontology = new DomainOntology(ont.getAbsolutePath());
            progress((System.currentTimeMillis() - t) + " ms\n\n");

            // init properties
            if (props != null) {
                loadOptionsSettings(props);
            }
            // process documents
            process(ontology, in.getAbsolutePath(), out.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * process  documents.
     *
     * @param ontology the templates to process
     * @param in       the in
     * @param out      the out
     */
    public void process(DomainOntology ontology, String in, String out) {
        // preload terminologies
        long t = System.currentTimeMillis();
        progress("loading anchors .. ");
        ontology.getAnchorTerminology();
        progress((System.currentTimeMillis() - t) + " ms\n");

        t = System.currentTimeMillis();
        progress("loading modifiers .. ");
        ontology.getModifierTerminology();
        progress((System.currentTimeMillis() - t) + " ms\n");

        t = System.currentTimeMillis();
        progress("loading sections .. ");
        ontology.getSectionTerminology();
        progress((System.currentTimeMillis() - t) + " ms\n");

        // start a new instance of noble mentions
        NobleMentions noble = new NobleMentions(ontology);

        // load options
        loadOptions(noble);

        // print runtime properties
        Properties p = getOptionsSettings();
        progress("\nruntime options: \n");
        for (Object key : new TreeSet(p.keySet())) {
            Object val = p.get(key);
            progress("  " + key + " = " + val + "\n");
        }
        progress("\n");


        // process lastFile
        List<File> files = FileTools.getFilesInDirectory(new File(in), ".txt");
        if (progress != null) {
            final int n = files.size();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setIndeterminate(false);
                    progress.setString("Processing Reports ..");
                    progress.setMaximum(n);
                }
            });
        }

        // process report
        File outputDir = new File(out);
        if (!outputDir.exists())
            outputDir.mkdirs();

        // initialize writers
        htmlExporter = new HTMLExporter(outputDir);
        csvExporter = new CSVExporter(outputDir);

        // reset stat counters
        processCount = 0;
        totalTime = 0;


        for (int i = 0; i < files.size(); i++) {
            try {
                process(noble, files.get(i));

                // cancel processing
                if (cancelRun)
                    break;

            } catch (Exception e) {
                progress("Error: " + e.getMessage());
                e.printStackTrace();
            }
            if (progress != null) {
                final int n = i + 1;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progress.setValue(n);
                    }
                });
            }
        }

        // wrap up
        try {
            ontology.write(new File(outputDir, ontology.getName() + ".owl"));
            htmlExporter.flush();
            csvExporter.flush();
        } catch (Exception e) {
            progress("Error: " + e.getMessage());
            e.printStackTrace();
        }


        // summary
        if (processCount > 0) {
            progress("\nTotal process time for all reports:\t" + totalTime + " ms\n");
            progress("Average process time per report:\t" + ((totalTime) / processCount) + " ms\n");
        }

        // print detailed run time
        printProcessTime();
    }

    private void addProcessTime(Document doc) {
        if (processTime == null)
            processTime = new LinkedHashMap<String, Long>();
        if (processTimeCount == null)
            processTimeCount = new HashMap<String, Long>();

        String suffix = " (Document)";
        for (String mod : doc.getProcessTime().keySet()) {
            long t = processTime.containsKey(mod + suffix) ? processTime.get(mod + suffix) : 0;
            long n = processTimeCount.containsKey(mod + suffix) ? processTimeCount.get(mod + suffix) : 0;
            processTime.put(mod + suffix, t + doc.getProcessTime().get(mod));
            processTimeCount.put(mod + suffix, n + 1);
        }
        suffix = " (Sentence)";
        for (Sentence s : doc.getSentences()) {
            for (String mod : s.getProcessTime().keySet()) {
                long t = processTime.containsKey(mod + suffix) ? processTime.get(mod + suffix) : 0;
                long n = processTimeCount.containsKey(mod + suffix) ? processTimeCount.get(mod + suffix) : 0;
                processTime.put(mod + suffix, t + s.getProcessTime().get(mod));
                processTimeCount.put(mod + suffix, n + 1);
            }
        }
    }

    private void printProcessTime() {
        if (processTime != null) {
            System.out.println("\n");
            final int count = 40;
            for (String mod : processTime.keySet()) {
                double t = processTime.get(mod);
                double n = processTimeCount.get(mod);
                String pad = StringUtils.pad(count - mod.length());
                System.out.println(mod + ":" + pad + "  " + TextTools.toString(t / n) + " ms");
            }
        }
    }

    /**
     * process report.
     *
     * @param templates  the templates
     * @param reportFile the report lastFile
     * @throws Exception the exception
     */
    private void process(NobleMentions noble, File reportFile) throws Exception {
        progress("processing report (" + (processCount + 1) + ") " + reportFile.getName() + " ... ");

        // read in the report, do first level proce
        Composition doc = noble.process(reportFile);

        processCount++;

        // now output HTML for this report
        htmlExporter.export(doc);
        csvExporter.export(doc);

        // do progress
        totalTime += noble.getProcessTime();
        progress(noble.getProcessTime() + " ms\n");

        // add runtime
        addProcessTime(doc);
    }

    /**
     * Progress.
     *
     * @param str the str
     */
    private void progress(String str) {
        System.out.print(str);
        if (console != null) {
            final String s = str;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    console.append(s);
                }
            });

        }
    }


}
