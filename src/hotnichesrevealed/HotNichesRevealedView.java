/*
 * HotNichesRevealedView.java
 */
package hotnichesrevealed;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import webbrowser.JavaScriptWebBrowser;
import webbrowser.WebBrowser;

/**
 * The application's main frame.
 */
public class HotNichesRevealedView extends FrameView implements Runnable, ClipboardOwner, ComponentListener {

    static String BING_API = "";
    protected static final Random generator = new Random();

    protected static void shuffle(int[] a) {
        int i, j;
        for (i = 0; i < a.length; ++i) {
            j = generator.nextInt(a.length);
            swap(a, i, j);
        }
    }

    protected static void swap(int[] a, int m, int n) {
        int t;
        t = a[m];
        a[m] = a[n];
        a[n] = t;
    }

    private String getDateTime() {
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, -2);
        return dateFormat.format(cal.getTime());
    }
    boolean stop = false;
    boolean running = false;

    @Action
    public void stop() {
        if (running == true) {
            jButton2.setText("Stopping...");
            jButton3.setText("Pause");
            stop = true;
            pause = false;
            running = false;
        }
    }
    boolean pause = false;

    @Action
    public void pause() {
        if (running) {
            pause = !pause;
            if (pause) {
                jButton3.setText("Continue");
            } else {
                jButton3.setText("Pause");
            }
        }
    }
    DefaultTableModel model = new DefaultTableModel();
    JTable table;
    JScrollPane scrollPane;

    HashSet<String> getSynonims(String syn) {
        HashSet<String> set = new HashSet<String>();
        set.add(syn);
        try {
            IndexWordSet wordSet = Dictionary.getInstance().lookupAllIndexWords(syn);
            IndexWord[] wordArray = wordSet.getIndexWordArray();
            for (IndexWord myWord : wordArray) {
                Synset[] synset = myWord.getSenses();
                for (Synset synonim : synset) {
                    Word[] finalWord = synonim.getWords();
                    for (Word lemma : finalWord) {
                        String myLemma = lemma.getLemma();
                        if (myLemma.contains("(") && myLemma.contains(")")) {
                            myLemma = myLemma.substring(0, myLemma.indexOf("("));
                        }
                        myLemma = myLemma.replaceAll("_", " ");
                        set.add(myLemma);
                    }
                }
            }
        } catch (Exception e) {
        }
        return set;
    }
    int nichesInPaste;
    ArrayList<ArrayList<String>> pasteStrings = new ArrayList<ArrayList<String>>();
    ArrayList<Color> myTableColors = new ArrayList<Color>();
    TableRowSorter<TableModel> firstSorter;
    String product1 = "";

    //resize
    void resize(java.awt.Component c) {
        //c.setBounds(dim.getX(), dim.getY(), (int) (dim.getWidth() * Xscale) - (dim.getX() - (int) (dim.getX() * Xscale)), (int) (dim.getHeight() * Yscale) - (dim.getY() - (int) (dim.getY() * Yscale)));
        c.setBounds((int) (Math.round(c.getX() * ratioWidth)), (int) (Math.round(c.getY() * ratioHeight)), (int) (Math.round(c.getWidth() * ratioWidth)), (int) (Math.round(c.getHeight() * ratioHeight)));
    }
    double ratioWidth;
    double ratioHeight;
    float Xscale;
    float Yscale;
    int lastWidth;
    int lastHeight;

    void resizeAllNow() {
        ArrayList<Component> allComp = getAllComponents(mainPanel);
        for (Component myComp : allComp) {
            resize(myComp);
            if (Xscale < 0.8) {
                myComp.setFont(verySmallFont);
            } else if (Xscale < 1) {
                myComp.setFont(smallFont);
            } else if (Xscale > 1.2) {
                myComp.setFont(largeFont);
            } else {
                myComp.setFont(defaultFont);
            }
        }
        //resize(jLabel9);
    }
    Font defaultFont;
    Font smallFont;
    Font verySmallFont;
    Font largeFont;
    int minWidth = 700;
    int minHeight = 600;
    int theWIDTH = 1157;
    int theHEIGHT = 916;

    @Override
    public void componentResized(ComponentEvent e) {

        if (getFrame().getWidth() < minWidth || getFrame().getHeight() < minHeight) {
            getFrame().setBounds(getFrame().getX(), getFrame().getY(), (getFrame().getWidth() < minWidth) ? minWidth : getFrame().getWidth(), (getFrame().getHeight() < minHeight) ? minHeight : getFrame().getHeight());
        }

        //System.out.println("gg:"+e.getComponent() .getClass().getName() + " --- Resized ");
        //SEO2View.super.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
        if (lastWidth == 0) {
            //HotNichesRevealedView.super.getFrame().setSize(1172, 876);
            lastWidth = HotNichesRevealedView.super.getFrame().getWidth();
        }
        if (lastHeight == 0) {
            //HotNichesRevealedView.super.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
            lastHeight = HotNichesRevealedView.super.getFrame().getHeight();
            ratioWidth = ((double) (HotNichesRevealedView.super.getFrame().getWidth())) / theWIDTH;
            ratioHeight = ((double) (HotNichesRevealedView.super.getFrame().getHeight())) / theHEIGHT;
            Xscale = ((float) HotNichesRevealedView.super.getFrame().getWidth()) / theWIDTH;
            resizeAllNow();
        } else {
            ratioWidth = ((double) (HotNichesRevealedView.super.getFrame().getWidth())) / lastWidth;
            ratioHeight = ((double) (HotNichesRevealedView.super.getFrame().getHeight())) / lastHeight;
            lastWidth = HotNichesRevealedView.super.getFrame().getWidth();
            lastHeight = HotNichesRevealedView.super.getFrame().getHeight();
            Xscale = ((float) HotNichesRevealedView.super.getFrame().getWidth()) / theWIDTH;
            Yscale = ((float) HotNichesRevealedView.super.getFrame().getHeight()) / theHEIGHT;
            resizeAllNow();


            //mainPanel.setBounds(mainPanel.getX(), mainPanel.getY(), mainPanel.getWidth(), (int)(mainPanel.getHeight()*2));
//        jPanel1.repaint();
//        jTabbedPane1.repaint();
//        mainPanel.repaint();
            //HotNichesRevealedView.super.getFrame().repack();
        }

    }

    public static ArrayList<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        ArrayList<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    public HotNichesRevealedView(SingleFrameApplication app) {
        super(app);

        initComponents();

        Image icon = Toolkit.getDefaultToolkit().getImage("c:\\HotNichesRevealed\\System\\hnr.gif");
        this.getFrame().setIconImage(icon);
        this.getFrame().pack();

        getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        lastWidth = mainPanel.getWidth();
//        lastHeight = mainPanel.getHeight();
//
//        ratioWidth = ((double)(mainPanel.getWidth()))/1152;
//        ratioHeight = ((double)(mainPanel.getHeight()))/876;
//        Xscale = ((float) mainPanel.getWidth()) / 1152;
//        Yscale = ((float) mainPanel.getHeight()) / 876;
//        resizeAllNow();


        getFrame().addComponentListener(this);

        defaultFont = mainPanel.getFont();
        largeFont = new Font("Tahoma-Plain-11", Font.PLAIN, 13);
        smallFont = new Font("Tahoma-Plain-11", Font.PLAIN, 10);
        verySmallFont = new Font("Tahoma-Plain-11", Font.PLAIN, 9);


        myThread.start();
        loadGeneralOptionsFromFile("c:/HotNichesRevealed/Data/default.go");
        NativeInterface.open();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                jPanel3.setLayout(new BorderLayout());

                jPanel3.setBorder(BorderFactory.createTitledBorder("Help"));
                final WebBrowser webBrowser = new WebBrowser("file:///c:/HotNichesRevealed/Help/Help.html", 1);

                jPanel3.add(webBrowser, BorderLayout.CENTER);

                // Create an additional bar allowing to show/hide the menu bar of the web browser.
//                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
//                JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());
//                menuBarCheckBox.addItemListener(new ItemListener() {
//
//                    @Override
//                    public void itemStateChanged(ItemEvent e) {
//                        webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
//                    }
//                });
//                buttonPanel.add(menuBarCheckBox);
//                jPanel43.add(buttonPanel, BorderLayout.SOUTH);

            }
        });
        nichesInPaste = 0;
        try {

            jLabel9.setIcon(new javax.swing.ImageIcon(ImageIO.read(new URL("http://www.hotnichesrevealed.com/" + "thismonthproduct.gif"))));
            URL urlAdds = new URL("http://www.hotnichesrevealed.com/hotnichesrevealedads.txt");
            URLConnection conn = urlAdds.openConnection();
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str = "";
            while ((str = in.readLine()) != null) {
                if (str.startsWith("product1:")) {
                    product1 = str.substring(str.indexOf(":") + 1);
                }
            }
        } catch (Exception e) {
        }

        try {
            JWNL.initialize(new FileInputStream("c:\\HotNichesRevealed\\System\\file_properties.xml"));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (JWNLException e) {
            //e.printStackTrace();
        }


        String date = getDateTime();
        jTextField2.setText(date.substring(0, 4));
        jTextField3.setText(date.substring(5, 7));
        jTextField4.setText(date.substring(8, 10));
        table = new JTable() {

            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = null;
                try {
                    c = super.prepareRenderer(renderer, rowIndex, vColIndex);

                    //if(getSelectedColumn()==vColIndex && getSelectedRow()==rowIndex){
                    if (isRowSelected(rowIndex) && isColumnSelected(vColIndex)) {
                        c.setBackground(Color.blue);//new Color(0xC6E2FF)
                    } else {
                        c.setBackground(myTableColors.get(rowIndex));//new Color(0xC6E2FF)
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                return c;
            }
        };

        table.setModel(model);
        table.setFont(new Font("Helvetica Bold", Font.PLAIN, 14));
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 5, 1119, 395);
        jPanel1.add(scrollPane);
        model.addColumn("#");
        model.addColumn("Hot Niches");

        model.addColumn("Popularity Score");
        model.addColumn("Page Score");
        model.addColumn("Domain Score");
        model.addColumn("PR Score");
        model.addColumn("Domain available");
        model.addColumn("# of monthly searches");
        model.addColumn("CPC");
        TableColumn col2 = table.getColumnModel().getColumn(0);
        col2.setPreferredWidth(1);
        TableColumn col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(220);
        firstSorter = new TableRowSorter<TableModel>(model);
//        Comparator comparatorMonthlySearches = new Comparator() {
//
//            public int compare(Object o1, Object o2) {
//                String s1 = o1.toString().replaceAll(",", "");
//                String s2 = o2.toString().replaceAll(",", "");
//                if (s1.equals("-")) {
//                    s1 = "N/A1";
//                }
//                if (s2.equals("-")) {
//                    s2 = "N/A1";
//                }
//                try {
//                    int i = Integer.parseInt(s1) - Integer.parseInt(s2);
//                    return i;
//                } catch (Exception e) {
//                }
//                return s1.compareTo(s2);
//            }
//        };
        Comparator comparatorMonthlySearches2 = new Comparator() {

            public int compare(Object o1, Object o2) {
                String s1 = o1.toString().replaceAll(",", "");
                String s2 = o2.toString().replaceAll(",", "");
                if (s1.equals("-") || s1.equals("Not Calculated")) {
                    s1 = "N/A1";
                }
                if (s2.equals("-") || s2.equals("Not Calculated")) {
                    s2 = "N/A1";
                }
                try {
                    int i = Integer.parseInt(s1) - Integer.parseInt(s2);
                    return i;
                } catch (Exception e) {
                }
                return s2.compareTo(s1);
            }
        };



        Comparator comparatorSimple = new Comparator() {

            public int compare(Object o1, Object o2) {
                try {
                    int i = Integer.parseInt(o1.toString()) - Integer.parseInt(o2.toString());
                    return i;
                } catch (Exception e) {
                }
                return o1.toString().compareTo(o2.toString());
            }
        };

        Comparator comparatorCPC = new Comparator() {

            public int compare(Object o1, Object o2) {
                String s1 = o1.toString();
                String s2 = o2.toString();
                if (s1.equals("-")) {
                    s1 = "N/A1";
                }
                if (s2.equals("-")) {
                    s2 = "N/A1";
                }
                if (s1.startsWith("$")) {
                    s1 = s1.substring(1);
                }
                if (s2.startsWith("$")) {
                    s2 = s2.substring(1);
                }
                try {
                    int i = (int) (Float.parseFloat(s1) * 100 - Float.parseFloat(s2) * 100);
                    return i;
                } catch (Exception e) {
                }
                return s2.compareTo(s1);
            }
        };

        firstSorter.setComparator(0, comparatorSimple);
        firstSorter.setComparator(2, comparatorSimple);
        firstSorter.setComparator(3, comparatorSimple);
        firstSorter.setComparator(4, comparatorSimple);
        firstSorter.setComparator(6, comparatorSimple);
        firstSorter.setComparator(7, comparatorMonthlySearches2);
        firstSorter.setComparator(8, comparatorCPC);
        table.setRowSorter(firstSorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        firstSorter.setSortKeys(sortKeys);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setCellSelectionEnabled(true);


        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = HotNichesRevealedApp.getApplication().getMainFrame();
            aboutBox = new HotNichesRevealedAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        HotNichesRevealedApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jProgressBar2 = new javax.swing.JProgressBar();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox();
        jTextField7 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField9 = new javax.swing.JTextField();
        jButton13 = new javax.swing.JButton();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jTextField8 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton10 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(null);

        jCheckBox2.setSelected(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(hotnichesrevealed.HotNichesRevealedApp.class).getContext().getResourceMap(HotNichesRevealedView.class);
        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N
        jPanel1.add(jCheckBox2);
        jCheckBox2.setBounds(50, 600, 300, 23);

        jCheckBox3.setSelected(true);
        jCheckBox3.setText(resourceMap.getString("jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jPanel1.add(jCheckBox3);
        jCheckBox3.setBounds(50, 480, 250, 23);

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        jPanel1.add(jTextField1);
        jTextField1.setBounds(300, 480, 60, 20);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(hotnichesrevealed.HotNichesRevealedApp.class).getContext().getActionMap(HotNichesRevealedView.class, this);
        jButton1.setAction(actionMap.get("revealHotNiches")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jPanel1.add(jButton1);
        jButton1.setBounds(10, 740, 350, 40);

        jButton2.setAction(actionMap.get("stop")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jPanel1.add(jButton2);
        jButton2.setBounds(940, 710, 90, 23);

        jButton3.setAction(actionMap.get("pause")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jPanel1.add(jButton3);
        jButton3.setBounds(1040, 710, 90, 23);

        jProgressBar1.setName("jProgressBar1"); // NOI18N
        jPanel1.add(jProgressBar1);
        jProgressBar1.setBounds(570, 760, 560, 20);

        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N
        jPanel1.add(jTextField2);
        jTextField2.setBounds(120, 430, 40, 20);

        jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
        jTextField3.setName("jTextField3"); // NOI18N
        jPanel1.add(jTextField3);
        jTextField3.setBounds(170, 430, 40, 20);

        jTextField4.setText(resourceMap.getString("jTextField4.text")); // NOI18N
        jTextField4.setName("jTextField4"); // NOI18N
        jPanel1.add(jTextField4);
        jTextField4.setBounds(220, 430, 40, 20);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(130, 410, 22, 14);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel1.add(jLabel2);
        jLabel2.setBounds(180, 410, 30, 14);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jPanel1.add(jLabel3);
        jLabel3.setBounds(230, 410, 19, 14);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel1.add(jLabel4);
        jLabel4.setBounds(10, 420, 100, 30);

        jButton4.setAction(actionMap.get("clearAll")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jPanel1.add(jButton4);
        jButton4.setBounds(1050, 400, 80, 23);

        jProgressBar2.setName("jProgressBar2"); // NOI18N
        jPanel1.add(jProgressBar2);
        jProgressBar2.setBounds(570, 740, 560, 20);

        jCheckBox4.setSelected(true);
        jCheckBox4.setText(resourceMap.getString("jCheckBox4.text")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox4);
        jCheckBox4.setBounds(50, 510, 240, 23);

        jCheckBox5.setSelected(true);
        jCheckBox5.setText(resourceMap.getString("jCheckBox5.text")); // NOI18N
        jCheckBox5.setName("jCheckBox5"); // NOI18N
        jPanel1.add(jCheckBox5);
        jCheckBox5.setBounds(50, 540, 240, 23);

        jTextField5.setText(resourceMap.getString("jTextField5.text")); // NOI18N
        jTextField5.setName("jTextField5"); // NOI18N
        jPanel1.add(jTextField5);
        jTextField5.setBounds(300, 510, 60, 20);

        jTextField6.setText(resourceMap.getString("jTextField6.text")); // NOI18N
        jTextField6.setName("jTextField6"); // NOI18N
        jPanel1.add(jTextField6);
        jTextField6.setBounds(300, 540, 60, 20);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALL", "EN", "ES", "DE", "PT", "FR", "IT", "RO", "HU", "RU" }));
        jComboBox1.setName("jComboBox1"); // NOI18N
        jPanel1.add(jComboBox1);
        jComboBox1.setBounds(300, 430, 60, 20);

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jPanel1.add(jLabel7);
        jLabel7.setBounds(300, 410, 60, 14);

        jButton5.setAction(actionMap.get("checkKeywordPhraseAction")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jPanel1.add(jButton5);
        jButton5.setBounds(380, 760, 170, 23);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Copy 100 niches list" }));
        jComboBox2.setName("jComboBox2"); // NOI18N
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBox2);
        jComboBox2.setBounds(380, 400, 170, 20);

        jTextField7.setText(resourceMap.getString("jTextField7.text")); // NOI18N
        jTextField7.setName("jTextField7"); // NOI18N
        jPanel1.add(jTextField7);
        jTextField7.setBounds(380, 740, 170, 20);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "United States", "United Kingdom", "Canada", "Australia", "Brazil", "Mexico", "Austria", "Belgium", "Denmark", "Finland", "France", "Germany", "Greece", "Ireland", "Italy", "Netherlands", "Norway", "Poland", "Spain", "Sweden", "Switzerland", "India", "Malaysia", "New Zealand", "Singapore", "Vietnam", "South Africa" }));
        jComboBox3.setName("jComboBox3"); // NOI18N
        jPanel1.add(jComboBox3);
        jComboBox3.setBounds(190, 650, 170, 20);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(resourceMap.getString("jRadioButton1.text")); // NOI18N
        jRadioButton1.setName("jRadioButton1"); // NOI18N
        jPanel1.add(jRadioButton1);
        jRadioButton1.setBounds(10, 670, 170, 23);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(resourceMap.getString("jRadioButton2.text")); // NOI18N
        jRadioButton2.setName("jRadioButton2"); // NOI18N
        jPanel1.add(jRadioButton2);
        jRadioButton2.setBounds(10, 690, 230, 23);

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText(resourceMap.getString("jRadioButton3.text")); // NOI18N
        jRadioButton3.setName("jRadioButton3"); // NOI18N
        jPanel1.add(jRadioButton3);
        jRadioButton3.setBounds(10, 710, 150, 23);

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jPanel1.add(jLabel8);
        jLabel8.setBounds(50, 650, 140, 20);

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel9);
        jLabel9.setBounds(380, 440, 750, 250);

        jButton8.setAction(actionMap.get("googleKeywordTool")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N
        jPanel1.add(jButton8);
        jButton8.setBounds(380, 690, 170, 23);

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setSelected(true);
        jRadioButton4.setText(resourceMap.getString("jRadioButton4.text")); // NOI18N
        jRadioButton4.setName("jRadioButton4"); // NOI18N
        jPanel1.add(jRadioButton4);
        jRadioButton4.setBounds(70, 450, 120, 23);

        buttonGroup2.add(jRadioButton5);
        jRadioButton5.setText(resourceMap.getString("jRadioButton5.text")); // NOI18N
        jRadioButton5.setName("jRadioButton5"); // NOI18N
        jRadioButton5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton5StateChanged(evt);
            }
        });
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButton5);
        jRadioButton5.setBounds(190, 450, 120, 23);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(370, 400, 10, 380);

        jCheckBox1.setSelected(true);
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jPanel1.add(jCheckBox1);
        jCheckBox1.setBounds(50, 630, 250, 20);

        jTextField9.setText(resourceMap.getString("jTextField9.text")); // NOI18N
        jTextField9.setName("jTextField9"); // NOI18N
        jPanel1.add(jTextField9);
        jTextField9.setBounds(300, 630, 60, 20);

        jButton13.setAction(actionMap.get("whoIs")); // NOI18N
        jButton13.setText(resourceMap.getString("jButton13.text")); // NOI18N
        jButton13.setName("jButton13"); // NOI18N
        jPanel1.add(jButton13);
        jButton13.setBounds(380, 710, 170, 23);

        jCheckBox6.setText(resourceMap.getString("jCheckBox6.text")); // NOI18N
        jCheckBox6.setName("jCheckBox6"); // NOI18N
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox6ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox6);
        jCheckBox6.setBounds(10, 480, 40, 23);

        jCheckBox7.setText(resourceMap.getString("jCheckBox7.text")); // NOI18N
        jCheckBox7.setName("jCheckBox7"); // NOI18N
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox7ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox7);
        jCheckBox7.setBounds(10, 510, 40, 23);

        jCheckBox8.setText(resourceMap.getString("jCheckBox8.text")); // NOI18N
        jCheckBox8.setName("jCheckBox8"); // NOI18N
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox8);
        jCheckBox8.setBounds(10, 540, 37, 23);

        jCheckBox9.setText(resourceMap.getString("jCheckBox9.text")); // NOI18N
        jCheckBox9.setName("jCheckBox9"); // NOI18N
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox9ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox9);
        jCheckBox9.setBounds(10, 600, 40, 23);

        jCheckBox10.setText(resourceMap.getString("jCheckBox10.text")); // NOI18N
        jCheckBox10.setName("jCheckBox10"); // NOI18N
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox10ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox10);
        jCheckBox10.setBounds(10, 630, 40, 23);

        jButton11.setAction(actionMap.get("saveAllSettings")); // NOI18N
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setName("jButton11"); // NOI18N
        jPanel1.add(jButton11);
        jButton11.setBounds(700, 710, 120, 23);

        jButton12.setAction(actionMap.get("loadSettings")); // NOI18N
        jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
        jButton12.setName("jButton12"); // NOI18N
        jPanel1.add(jButton12);
        jButton12.setBounds(570, 710, 120, 23);

        jCheckBox11.setText(resourceMap.getString("jCheckBox11.text")); // NOI18N
        jCheckBox11.setName("jCheckBox11"); // NOI18N
        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox11ActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBox11);
        jCheckBox11.setBounds(10, 570, 40, 23);

        jCheckBox12.setSelected(true);
        jCheckBox12.setText(resourceMap.getString("jCheckBox12.text")); // NOI18N
        jCheckBox12.setName("jCheckBox12"); // NOI18N
        jPanel1.add(jCheckBox12);
        jCheckBox12.setBounds(50, 570, 240, 23);

        jTextField8.setText(resourceMap.getString("jTextField8.text")); // NOI18N
        jTextField8.setName("jTextField8"); // NOI18N
        jPanel1.add(jTextField8);
        jTextField8.setBounds(300, 570, 60, 20);

        jTextField10.setText(resourceMap.getString("jTextField10.text")); // NOI18N
        jTextField10.setName("jTextField10"); // NOI18N
        jPanel1.add(jTextField10);
        jTextField10.setBounds(630, 400, 290, 20);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jPanel1.add(jLabel5);
        jLabel5.setBounds(570, 400, 60, 20);

        jButton6.setAction(actionMap.get("getBingAPI")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jPanel1.add(jButton6);
        jButton6.setBounds(930, 400, 100, 20);

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(null);
        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(null);

        jButton10.setAction(actionMap.get("ammseoSpecialOffer")); // NOI18N
        jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
        jButton10.setName("jButton10"); // NOI18N
        jPanel2.add(jButton10);
        jButton10.setBounds(230, 480, 240, 30);

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jPanel2.add(jLabel6);
        jLabel6.setBounds(30, 50, 0, 370);

        jLabel10.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel10.setForeground(resourceMap.getColor("jLabel10.foreground")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        jPanel2.add(jLabel10);
        jLabel10.setBounds(30, 430, 410, 30);

        jLabel11.setIcon(new javax.swing.ImageIcon("C:\\HotNichesRevealed\\System\\ammseo.gif")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        jPanel2.add(jLabel11);
        jLabel11.setBounds(30, 50, 700, 370);

        jLabel12.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        jPanel2.add(jLabel12);
        jLabel12.setBounds(30, 10, 570, 30);

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1187, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 819, Short.MAX_VALUE)
        );

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1187, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1017, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
        if (jCheckBox4.isSelected()) {
            jCheckBox5.setEnabled(true);
        } else {
            jCheckBox5.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBox4ActionPerformed
    String clipboard = null;
    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        if (jComboBox2.getSelectedIndex() != 0 && jComboBox2.getItemCount() > 1) {
            clipboard = new String();

//            for (int i = (jComboBox2.getSelectedIndex() - 1) * 100; i < model.getRowCount() && i < jComboBox2.getSelectedIndex() * 100; i++) {
//                boolean bPage = ((!jCheckBox3.isSelected()) || (jCheckBox3.isSelected() && ((!getData(i, 2).equals("-") && (Integer.parseInt(getData(i, 2)) <= Integer.parseInt(jTextField1.getText()))) || jTextField1.getText().equals("0"))));
//                boolean bDomain = ((!jCheckBox4.isSelected()) || (jCheckBox4.isSelected() && ((!getData(i, 3).equals("-") && Integer.parseInt(getData(i, 3)) <= Integer.parseInt(jTextField5.getText())) || jTextField5.getText().equals("0"))));
//                boolean bDomainAvailable = ((!jCheckBox5.isSelected()) || (!jCheckBox5.isEnabled()) || (jCheckBox5.isSelected() && (((!getData(i, 4).equals("-")) && Integer.parseInt(getData(i, 4)) <= Integer.parseInt(jTextField6.getText())) || jTextField6.getText().equals("0"))));
//                if(bPage && bDomain && bDomainAvailable && (!jCheckBox2.isEnabled() || !jCheckBox2.isSelected() || (jCheckBox2.isEnabled() && jCheckBox2.isSelected() && getData(i, 5).equals("Available")))){
//                    clipboard += getData(i, 1) + "\n";
//                }
//            }
            for (int i = 0; i < pasteStrings.get(jComboBox2.getSelectedIndex() - 1).size(); i++) {
                clipboard += pasteStrings.get(jComboBox2.getSelectedIndex() - 1).get(i) + "\n";
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(clipboard), this);
        }

        openGoogleKeywordTool();


    }//GEN-LAST:event_jComboBox2ActionPerformed
    JavaScriptWebBrowser googleWeb = null;

    public void openGoogleKeywordTool() {
        if (googleWeb == null) {
            googleWeb = new JavaScriptWebBrowser("https://adwords.google.com/select/KeywordToolExternal", "document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \"" + clipboard.replaceAll("\n", "\\\\n") + "\";document.getElementById('gwt-debug-searchInput-limitResults-input').checked = true;document.getElementById('gwt-debug-ideas-table-header-GLOBAL_MONTHLY_SEARCHES-3').click();");//;;
            //googleWeb = new JavaScriptWebBrowser("https://adwords.google.com/select/KeywordToolExternal", "if(window.onLoad){document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \""+clipboard.replaceAll("\n", "\\\\n")+"\";document.getElementById('gwt-debug-searchInput-limitResults-input').checked = true;document.getElementById('gwt-debug-ideas-table-header-GLOBAL_MONTHLY_SEARCHES-3').click();document.getElementById('gwt-debug-searchPanel-searchButton').click();}");
            jTabbedPane1.add(googleWeb, "GoogleKeywordTool");
        } else {
            googleWeb.webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \"" + clipboard.replaceAll("\n", "\\\\n") + "\";");
        }
        jTabbedPane1.setSelectedComponent(googleWeb);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {

                    Robot robot = new Robot();
                    googleWeb.webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').focus();");
                    Thread.sleep(500);
                    robot.keyPress(KeyEvent.VK_SPACE);
                    robot.keyRelease(KeyEvent.VK_SPACE);

                    googleWeb.webBrowser.executeJavascript("document.getElementById('gwt-debug-searchPanel-searchButton').focus();");
                    Thread.sleep(500);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                } catch (Exception e) {
                }
            }
        });

    }

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
//       if (jRadioButton5.isSelected()) {
//            jCheckBox2.setEnabled(true);
//        } else {
//            jCheckBox2.setEnabled(false);
//        }
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton5StateChanged
        if (jRadioButton5.isSelected() || jCheckBox9.isSelected()) {
            jCheckBox2.setEnabled(false);
        } else {
            jCheckBox2.setEnabled(true);
        }

    }//GEN-LAST:event_jRadioButton5StateChanged

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!product1.equals("")) {
                        Desktop.getDesktop().browse(new URI(product1));
                    }
                } catch (Exception ex2) {
                }
            }
        });
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jCheckBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox6ActionPerformed
        if (jCheckBox6.isSelected()) {
            jCheckBox3.setEnabled(false);
        } else {
            jCheckBox3.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBox6ActionPerformed

    private void jCheckBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox7ActionPerformed
        if (jCheckBox7.isSelected()) {
            jCheckBox4.setEnabled(false);
        } else {
            jCheckBox4.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBox7ActionPerformed

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        if ((!jCheckBox8.isSelected()) && ((jCheckBox4.isSelected() && jCheckBox4.isEnabled()) || jCheckBox7.isSelected())) {
            jCheckBox5.setEnabled(true);
        } else {
            jCheckBox5.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox9ActionPerformed
        if (!jCheckBox9.isSelected() && !jRadioButton5.isSelected()) {
            jCheckBox2.setEnabled(true);
        } else {
            jCheckBox2.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBox9ActionPerformed

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
        if (jCheckBox10.isSelected()) {
            jCheckBox1.setEnabled(false);
        } else {
            jCheckBox1.setEnabled(true);
        }

    }//GEN-LAST:event_jCheckBox10ActionPerformed

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
        if (jCheckBox11.isSelected()) {
            jCheckBox12.setEnabled(false);
        } else {
            jCheckBox12.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBox11ActionPerformed

    public String getData(int row_index, int col_index) {
        return table.getModel().getValueAt(row_index, col_index).toString();
    }
//    String loadStringFromURL(String URL){
//      
//    }

//    private String getSuggestion(String query) {
//        System.out.println("\nQuerying for " + query);
//        try {
//
//            query = URLEncoder.encode(query, "UTF-8");
//            String lang = jComboBox1.getSelectedItem().toString().toLowerCase();
//            String market = "";
//            if (lang.equals("all")) {
//                market = "";
//            } else {
//                if (lang.equals("en")) {
//                    market = "&Market=en-US";
//                }
//                if (lang.equals("es")) {
//                    market = "&Market=es-ES";
//                }
//                if (lang.equals("de")) {
//                    market = "&Market=de-DE";
//                }
//                if (lang.equals("pt")) {
//                    market = "&Market=pt-PT";
//                }
//                if (lang.equals("fr")) {
//                    market = "&Market=fr-FR";
//                }
//                if (lang.equals("it")) {
//                    market = "&Market=it-IT";
//                }
//                if (lang.equals("ro")) {
//                    market = "&Market=ro-RO";
//                }
//                if (lang.equals("hu")) {
//                    market = "&Market=hu-HU";
//                }
//                if (lang.equals("ru")) {
//                    market = "&Market=ru-RU";
//                }
//
//            }
////            URL url = new URL("http://boss.yahooapis.com/ysearch/spelling/v1/" + query
////                    + "?appid=" + API_KEY + "&count=10&format=json" + lang);
//
//
//            URL url = new URL("http://api.bing.net/json.aspx?AppId=" + bingAPI + "&Query=" + query + "&Sources=Spell&Version=2.0" + market + "&JsonType=callback&JsonCallback=SearchCompleted");//&count=10
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            JSONObject json = new JSONObject(response);
//            JSONArray ja = json.getJSONObject("SearchResponse").getJSONObject("Spell").getJSONArray("Results");
//
//
//
//
//            //System.out.println("res:" + ja.getJSONArray("SearchTerms"));
//            return ja.getJSONObject(0).getString("Value");
//        } catch (Exception e) {
//        }
//        return "";
//    }
    private String getSuggestion(String query) {
        System.out.println("\nQuerying for " + query);
        try {
            //query = URLEncoder.encode(query, "UTF-8");
            String lang = jComboBox1.getSelectedItem().toString().toLowerCase();
            String market = "";
            if (lang.equals("all")) {
                market = "";
            } else {
                if (lang.equals("en")) {
                    market = "&Market=%27en-US%27";
                }
                if (lang.equals("es")) {
                    market = "&Market=%27es-ES%27";
                }
                if (lang.equals("de")) {
                    market = "&Market=%27de-DE%27";
                }
                if (lang.equals("pt")) {
                    market = "&Market=%27pt-PT%27";
                }
                if (lang.equals("fr")) {
                    market = "&Market=%27fr-FR%27";
                }
                if (lang.equals("it")) {
                    market = "&Market=%27it-IT%27";
                }
                if (lang.equals("ro")) {
                    market = "&Market=%27ro-RO%27";
                }
                if (lang.equals("hu")) {
                    market = "&Market=%27hu-HU%27";
                }
                if (lang.equals("ru")) {
                    market = "&Market=%27ru-RU%27";
                }

            }


//            URL url = new URL("http://api.bing.net/json.aspx?AppId=" + BingAPI + "&Query=" + query + "&Sources=Spell&Version=2.0" + market + "&JsonType=callback&JsonCallback=SearchCompleted");//&count=10
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            JSONObject json = new JSONObject(response);
//            JSONArray ja = json.getJSONObject("SearchResponse").getJSONObject("Spell").getJSONArray("Results");


            String response = getBingResults("https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/SpellingSuggestions?Query=%27" + query + "%27" + market + "&$top=10&$format=JSON");
            JSONObject json = new JSONObject(response);
            JSONArray ja = json.getJSONObject("d").getJSONArray("results");
//            for (int i = 0; i < ja.length(); i++) {
//                resultLinks.add(ja.getJSONObject(i).getString("Url"));
//            }


            //System.out.println("res:" + ja.getJSONArray("SearchTerms"));
            return ja.getJSONObject(0).getString("Value");
        } catch (Exception e) {
        }
        return "";
    }

    String getBingResults(String bingUrl) {
        byte[] accountKeyBytes = org.apache.commons.codec.binary.Base64.encodeBase64((bingAPI + ":" + bingAPI).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);
        URL url;
        URLConnection urlConnection = null;
        String resultStringBuilder = "";
        try {
            //https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=%27cars%27&$top=50&$format=Atom
            //https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web?Query=%27cars%27&$top=50&$format=Atom

            url = new URL(bingUrl);
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String str = "";

            while ((str = in.readLine()) != null) {
                resultStringBuilder += str;
            }
            in.close();

        } catch (Exception ex) {
        }
        System.out.println(resultStringBuilder.toString());
        return resultStringBuilder.toString();

    }
    long monthlyTime;
    ArrayList<MyElements> monthySearchesStack = new ArrayList<MyElements>();
    Thread myThread = new Thread(this);
    boolean captchaEntered = false;
    int minimulInterval = 16000;
    //int minimulInterval = 10500;
    long lastCheckedOverload = 0;
    boolean rawNichesExist = false;

    @Override
    public void run() {
        //jButton5.setEnabled(false);

        while (true) {
            if (pause) {
                try {
                    Thread.sleep(500);
                    continue;
                } catch (Exception e) {
                }
            }
            try {
                long currentTime = System.currentTimeMillis();
                if (monthlyTime == 0) {
                    monthlyTime = currentTime;
                }
                System.out.println("bbb" + (currentTime - monthlyTime));
                if (currentTime - monthlyTime < minimulInterval) {
                    try {
                        Thread.sleep(minimulInterval - (currentTime - monthlyTime) + (int) (Math.random() * 4));
                    } catch (Exception e) {
                    }
                    currentTime = System.currentTimeMillis();
                }
                if (monthySearchesStack.size() > 0 && System.currentTimeMillis() - lastCheckedOverload > 108000 /*&& rawNichesExist == false*/) {

                    int myPosistion = monthySearchesStack.get(0).getPosition();
                    String searchesAndAdv = checkMonthlySearchesAndComp(monthySearchesStack.get(0).getKeyword());
                    if (searchesAndAdv.contains("Overload")) {
                        lastCheckedOverload = System.currentTimeMillis();
                    }
                    String myMonthlySearches = searchesAndAdv.substring(0, searchesAndAdv.indexOf(":"));
                    String myComp = searchesAndAdv.substring(searchesAndAdv.indexOf(":") + 1);
                    table.getModel().setValueAt(myMonthlySearches, myPosistion, 7);
                    table.getModel().setValueAt(myComp, myPosistion, 8);
//                String myCaptcha = "";
//                ClientHttpRequest myClient =null;
//                if (myMonthlySearches.equals("captcha")) {
//                    try {
//                        myClient = new ClientHttpRequest("http://www.keywordspy.com/sorry.aspx?ref=/research/search.aspx?q=" + "go" + "&type=keywords");
//
//                    } catch (Exception e) {
//                    }
//                    try{
//                       jLabel6.setIcon(new ImageIcon(new URL("http://www.keywordspy.com/captcha.aspx")));
//                    }
//                    catch(Exception e){}
//
//                    while (!captchaEntered) {
//                        try {
//                            Thread.sleep(200);
//                        } catch (Exception e) {
//                        }
//                    }
//                    captchaEntered = false;
//                    myCaptcha = jTextField8.getText();
//                                    try {
//                    //URL url = new URL("http://www.keywordspy.com/sorry.aspx?ref=/research/search.aspx?q="+monthySearchesStack.get(0).getKeyword()+"&type=keywords");
//                    //String page = URLEncoder.encode("http://www.keywordspy.com/sorry.aspx?ref=/research/search.aspx?q=" + monthySearchesStack.get(0).getKeyword() + "&type=keywords", "UTF-8");
//                    myClient.setParameter("Code", myCaptcha);
//                    InputStream is = myClient.post();
//                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
//                    String line = null;
//                    System.out.println("jjj:");
//                    while ((line = in.readLine()) != null) {
//                        System.out.println(line);
//                    }
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

//                }
                    //http://www.keywordspy.com/sorry.aspx?ref=/research/search.aspx?q=john&type=keywords
                    //System.out.println("testing:" + monthySearchesStack.get(0).getKeyword());
                    try {
                        monthySearchesStack.remove(0);
                    } catch (Exception e) {
                    }
                }
                monthlyTime = currentTime;
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                }
            } catch (Exception e) {
            }

        }
        //stop = false;
        //jButton5.setEnabled(true);
    }
    URLConnection spyConn;

    String checkMonthlySearchesAndComp(String keyword) {
        //keyword = "\""+keyword+"\"";
        String returnedString = "";
        try {
            String market = "";
            String marketSymbol = "";
            if (jComboBox3.getSelectedIndex() != 0) {
                market = "&market=";
                switch (jComboBox3.getSelectedIndex()) {
                    case 1:
                        marketSymbol = "uk";
                        break;
                    case 2:
                        marketSymbol = "ca";
                        break;
                    case 3:
                        marketSymbol = "au";
                        break;
                    case 4:
                        marketSymbol = "br";
                        break;
                    case 5:
                        marketSymbol = "mx";
                        break;
                    case 6:
                        marketSymbol = "at";
                        break;
                    case 7:
                        marketSymbol = "be";
                        break;
                    case 8:
                        marketSymbol = "dk";
                        break;
                    case 9:
                        marketSymbol = "fi";
                        break;
                    case 10:
                        marketSymbol = "fr";
                        break;
                    case 11:
                        marketSymbol = "de";
                        break;
                    case 12:
                        marketSymbol = "gr";
                        break;
                    case 13:
                        marketSymbol = "ie";
                        break;
                    case 14:
                        marketSymbol = "it";
                        break;
                    case 15:
                        marketSymbol = "nl";
                        break;
                    case 16:
                        marketSymbol = "no";
                        break;
                    case 17:
                        marketSymbol = "pl";
                        break;
                    case 18:
                        marketSymbol = "es";
                        break;
                    case 19:
                        marketSymbol = "se";
                        break;
                    case 20:
                        marketSymbol = "ch";
                        break;
                    case 21:
                        marketSymbol = "in";
                        break;
                    case 22:
                        marketSymbol = "my";
                        break;
                    case 23:
                        marketSymbol = "nz";
                        break;
                    case 24:
                        marketSymbol = "sg";
                        break;
                    case 25:
                        marketSymbol = "vn";
                        break;
                    case 26:
                        marketSymbol = "za";
                        break;
                }
                market += marketSymbol;
            }
            String search = "http://www.keywordspy.com/research/search.aspx?q=" + keyword + "&type=keywords" + market;

            search = search.replaceAll(" ", "+");
            URL url = new URL(search);
            System.out.println(search);
            spyConn = url.openConnection();
            spyConn.setRequestProperty("User-Agent", "Opera/9.00 (Windows NT 5.1; U; de)");
            String str = "";
            String finalText = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(spyConn.getInputStream()));
            while ((str = in.readLine()) != null) {
                finalText = (new StringBuilder()).append(finalText).append(str).toString();
            }
            in.close();
            if (finalText.contains("captcha")) {
                return "Overload:Overload";
            }
            int endSelected = finalText.indexOf("rel=\"svo\"");
            int startSelected = endSelected - 100;
            String selected = finalText.substring(startSelected, endSelected);
            //System.out.println(selected);
            int myPos = selected.lastIndexOf("/mo");
            if (myPos != -1) {
                String selected2 = selected.substring(0, myPos);
                returnedString = selected2.substring(selected2.lastIndexOf(">") + 1);
            } else {
                returnedString = "N/A";
            }
            endSelected = finalText.indexOf("rel=\"cpc\"");
            startSelected = finalText.indexOf("<td>CPC:</td>");
            selected = finalText.substring(startSelected, endSelected);
            System.out.println(selected);
            myPos = selected.indexOf("$");
            if (myPos != -1) {
                selected = selected.substring(selected.indexOf("$"), selected.lastIndexOf("</td>"));
                returnedString += ":" + selected;
            } else {
                returnedString += ":N/A";
            }
            if (jCheckBox10.isSelected() || (jCheckBox1.isSelected() && jCheckBox1.isEnabled())) {
                startSelected = finalText.indexOf("Related Keyword Overview") + 40;
                //System.out.println("ll:"+startSelected);
                endSelected = finalText.indexOf("</div>", startSelected);

                String newNicheString = finalText.substring(startSelected, endSelected);
                System.out.println("lo:" + newNicheString);
                String[] newRawNiches = newNicheString.split("/overview/keyword");///overview/keyword.aspx?q=
                //ArrayList<String> newNiches = new ArrayList<String>();
                String monthlySearches = "";
                String CPC = "";
                for (int i = 1; i < newRawNiches.length; i++) {
                    if (stop) {
                        //stop = false;
                        break;
                    }
                    //rawNichesExist = true;
                    System.out.println("len" + newRawNiches.length);
                    //newNiches.add(newRawNiches[i].substring(0, 100));
                    String myNiche = newRawNiches[i].substring(8, newRawNiches[i].indexOf("\""));
                    System.out.println("hhh:" + myNiche);
                    int end = newNicheString.indexOf("/mo", newNicheString.indexOf(myNiche));
                    int start = newNicheString.indexOf(":right\"", newNicheString.indexOf(myNiche)) + 8;
                    monthlySearches = newNicheString.substring(start, end);
                    System.out.println("monthly" + monthlySearches);
                    start = newNicheString.indexOf("$", newNicheString.indexOf(myNiche));
                    end = newNicheString.indexOf("<", start);
                    CPC = newNicheString.substring(start, end);
                    System.out.println("cpc" + CPC);
                    if (monthlySearches.equals("0")) {
                        monthlySearches = "?";
                        CPC = "?";
                    }
                    try {
                        if (jCheckBox1.isSelected() && jCheckBox1.isEnabled() && (Integer.parseInt(monthlySearches.replaceAll(",", "")) > Integer.parseInt(jTextField9.getText()))) {
                            addARow(myNiche, "-", "-", "-", "-", "-", monthlySearches, CPC);
                        } else {
                            checkKeywordPhrase(myNiche, "", monthlySearches, CPC);
                        }
                    } catch (Exception e) {
                    }
                }

            }
        } catch (java.net.SocketException e) {
            e.printStackTrace();
            JOptionPane op = new JOptionPane("Internet Connection Error!", JOptionPane.INFORMATION_MESSAGE);
            javax.swing.JDialog dialog = op.createDialog("Info");
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //rawNichesExist = false;
        return returnedString;

    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    synchronized void checkKeywordPhrase(String mySugestion, String domainName, String monthlySearches, String CPC) {
        if (stop) {
            return;
        }
        //check "-"
        String[] suggestionSplited = mySugestion.split("[ -]");
        boolean good = true;

        if (!domainName.equals("")) {
            for (int j = 0; j < suggestionSplited.length; j++) {
                if (!domainName.contains(suggestionSplited[j])) {
                    good = false;
                }
            }
        }
        if (good) {
            if (jRadioButton3.isSelected()) {
                addMyRow(mySugestion, monthlySearches, CPC);
                addSynonims(mySugestion, monthlySearches, CPC);
            } else if (jRadioButton2.isSelected()) {
                if (addMyRow(mySugestion, monthlySearches, CPC)) {
                    addSynonims(mySugestion, monthlySearches, CPC);
                }
            } else {
                addMyRow(mySugestion, monthlySearches, CPC);
            }
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    int lastRow = table.getRowCount() - 1;
//                    //table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
//                    table.editCellAt(lastRow, 0);
//                    table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
//                    //model.setValueAt(mySugestion, lastRow,0);
//                }
//            });
        }
    }

    void addSynonims(String mySugestion, String monthlySearches, String CPC) {
        String[] suggestionWords = mySugestion.split(" ");
        myList = new ArrayList<ArrayList<String>>();
        for (String suggestionWord : suggestionWords) {
            HashSet<String> synonimsSet = getSynonims(suggestionWord);
            ArrayList<String> synonimsList = new ArrayList<String>(synonimsSet);
            myList.add(synonimsList);
        }
        indexes = new int[myList.size()];
        for (int q = 0; q < myList.size(); q++) {
            indexes[q] = 0;
        }
        do {
            String myString = "";
            for (int q = 0; q < myList.size(); q++) {
                myString += myList.get(q).get(indexes[q]) + " ";
            }
            if (indexesNotAtStart()) {
                addMyRow(myString, monthlySearches, CPC);
            }
            //model.addRow(new Object[]{table.getRowCount() + 1, myString, "", "", ""});
            plusplus(0);
            if (stop) {
                //stop = false;
                return;
            }
        } while (allIndexesNotAtTop());
        if (!(indexes.length == 1 && myList.get(0).size() == 1)) {
            String myString = "";
            for (int q = 0; q < myList.size(); q++) {
                myString += myList.get(q).get(myList.get(q).size() - 1) + " ";
            }
            myString = myString.trim();
            if (indexesNotAtStart()) {
                addMyRow(myString, monthlySearches, CPC);
            }
        }

    }

    boolean indexesNotAtStart() {
        boolean notAtStart = false;
        for (int q = 0; q < myList.size(); q++) {
            if (indexes[q] != 0) {
                notAtStart = true;
            }
        }
        return notAtStart;

    }

    long StrToNum(String Str, long Check, long Magic) {
        long Int32Unit = 4294967296L; // 2^32
        int length = Str.length();
        for (int i = 0; i < length; i++) {
            Check *= Magic;
            if (Check >= Int32Unit) {
                Check = (Check - Int32Unit * (long) (Check / Int32Unit));
                Check = (Check < -2147483648) ? (Check + Int32Unit) : Check;
            }
            Check += Str.charAt(i);
        }
        return Check;
    }

    /*
     * Generate a hash for a url
     */
    long HashURL(String myString) {
        long Check1 = StrToNum(myString, 0x1505, 0x21);
        long Check2 = StrToNum(myString, 0, 0x1003F);

        Check1 >>= 2;
        Check1 = ((Check1 >> 4) & 0x3FFFFC0) | (Check1 & 0x3F);
        Check1 = ((Check1 >> 4) & 0x3FFC00) | (Check1 & 0x3FF);
        Check1 = ((Check1 >> 4) & 0x3C000) | (Check1 & 0x3FFF);

        long T1 = ((((Check1 & 0x3C0) << 4) | (Check1 & 0x3C)) << 2) | (Check2 & 0xF0F);
        long T2 = ((((Check1 & 0xFFFFC000) << 4) | (Check1 & 0x3C00)) << 0xA) | (Check2 & 0xF0F0000);

        return (T1 | T2);
    }

    /*
     * generate a checksum for the hash string
     */
    String CheckHash(long Hashnum) {
        int CheckByte = 0;
        long Flag = 0;

        String HashStr = Long.toString(Hashnum);
        if (HashStr.startsWith("-")) {
            HashStr = HashStr.substring(1);
        }
        int length = HashStr.length();

        for (int i = length - 1; i >= 0; i--) {
            int Re = Integer.parseInt(HashStr.substring(i, i + 1));
            if (1 == (Flag % 2)) {
                Re += Re;
                Re = (int) ((Re / 10) + (Re % 10));
            }
            CheckByte += Re;
            Flag++;
        }

        //System.out.println("cb="+CheckByte);
        CheckByte %= 10;
        if (0 != CheckByte) {
            CheckByte = (char) (10 - CheckByte);
            if (1 == (Flag % 2)) {
                if (1 == (CheckByte % 2)) {
                    CheckByte += 9;
                }
                CheckByte >>= 1;
            }
        }

        return "7" + ((int) CheckByte) + HashStr;
    }
    int agInt = 0;

    String getpagerank(String url) {
        String query = "http://toolbarqueries.google.com/tbr?client=navclient-auto&ch=" + CheckHash(HashURL(url)) + "&features=Rank&q=info:" + url + "&num=100&filter=0";
        String result = "";
        //System.out.println("q=" + query);
        try {
            URL myurl = new URL(query);
            URLConnection conn = myurl.openConnection();
            //conn.setRequestProperty("User-Agent", agents[(agInt++) % 4]);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str = "";

            while ((str = in.readLine()) != null) {
                result = (new StringBuilder()).append(result).append(str).toString();
            }
            in.close();
            //System.out.println("link:" + url + " " + result);
        } catch (Exception e) {
        }
        System.out.println("pr:" + result.substring(9, 10));
        return result.substring(9, 10);

    }

    String getmypagerank() {
        float sum = 0;
        float count = 0;
        for (int i = 0; i < 4 && myLinks.resultLinks.size() > i; i++) {
            try {
                sum += Integer.parseInt(getpagerank(myLinks.resultLinks.get(i)));
                count++;
            } catch (Exception e) {
            }
        }
        float ret = sum / count;
        System.out.println("count:" + count);
        for (String s : myLinks.resultLinks) {
            //System.out.println("link:" + s);
        }
        String retStr = Float.toString(ret) + "0000";
        return retStr.substring(0, retStr.indexOf(".") + 3);
    }

    boolean addMyRow(String myString, String monthlySearches, String CPC) {
        String popularityScore = "-";
        String domainScore = "-";
        String pageScore = "-";
        String domain = "-";
        String PRScore = "-";
        boolean isDomainAvailable = false;

        if (jCheckBox6.isSelected() || (jCheckBox3.isSelected() && jCheckBox3.isEnabled())) {
            popularityScore = getPopularityScore(myString);
        }
        boolean isCalculatingMonthlySearches = false;
        myLinks = new MyLinksElement();

        try {
            boolean bPopularityPassed = (!jCheckBox6.isSelected() && !jCheckBox3.isSelected()) || jCheckBox6.isSelected() || (!jCheckBox3.isSelected() && !jCheckBox3.isEnabled()) || (jCheckBox3.isSelected() && jCheckBox3.isEnabled() && (((!popularityScore.equals("-")) && (Integer.parseInt(popularityScore) >= 0) && (Integer.parseInt(popularityScore) <= Integer.parseInt(jTextField1.getText())))));
            if (jCheckBox7.isSelected() || (jCheckBox4.isSelected() && jCheckBox4.isEnabled() && bPopularityPassed)) {
                pageScore = getPageScore(myString);
            }
            boolean bPagePassed = (!jCheckBox7.isSelected() && !jCheckBox4.isSelected()) || jCheckBox7.isSelected() || (!jCheckBox4.isSelected() && !jCheckBox4.isEnabled()) || (jCheckBox4.isSelected() && jCheckBox4.isEnabled() && (((!pageScore.equals("-")) && (Integer.parseInt(pageScore) >= 0) && Integer.parseInt(pageScore) <= Integer.parseInt(jTextField5.getText()))));
            if (jCheckBox8.isSelected() || (jCheckBox5.isEnabled() && jCheckBox5.isSelected() && bPagePassed && bPopularityPassed)) {
                domainScore = getDomainScore(myString);

            }

            boolean bDomainScorePassed = (!jCheckBox8.isSelected() && !jCheckBox5.isSelected()) || jCheckBox8.isSelected() || ((!jCheckBox5.isSelected()) || (!jCheckBox5.isEnabled())) || (jCheckBox5.isSelected() && jCheckBox5.isEnabled() && (((!domainScore.equals("-")) && (Integer.parseInt(domainScore) >= 0) && Integer.parseInt(domainScore) <= Integer.parseInt(jTextField6.getText()))));
            if (jCheckBox11.isSelected() || (jCheckBox12.isSelected() && bPopularityPassed && bPagePassed && bDomainScorePassed)) {
                PRScore = getmypagerank();
//                if(PRScore.equals("Na")){
//                    PRScore = "-";
//                }
            }

            boolean bPRPassed = (!jCheckBox11.isSelected() && !jCheckBox12.isSelected()) || jCheckBox11.isSelected() || ((!jCheckBox12.isSelected()) || (!jCheckBox12.isEnabled())) || (jCheckBox12.isSelected() && jCheckBox12.isEnabled() && (((PRScore.equals("Na") || ((!PRScore.equals("Na") && (!PRScore.equals("-"))) && Float.parseFloat(PRScore) <= Float.parseFloat(jTextField8.getText()))))));
            if (jCheckBox9.isSelected() || (jCheckBox2.isSelected() && bPopularityPassed && bPagePassed && bDomainScorePassed && bPRPassed)) {
                isDomainAvailable = checkNiche(myString);
                domain = isDomainAvailable ? "Available" : "Not Available";
            }

            isCalculatingMonthlySearches = bPopularityPassed && bPagePassed && bDomainScorePassed && bPRPassed && (!jCheckBox2.isEnabled() || !jCheckBox2.isSelected() || jCheckBox9.isSelected() || (jCheckBox2.isEnabled() && jCheckBox2.isSelected()) && isDomainAvailable);
            //(jRadioButton4.isSelected() && isDomainAvailable) || (!jRadioButton4.isSelected() && !domainScore.equals("-"));
            if (isCalculatingMonthlySearches) {
                if (nichesInPaste % 100 == 0) {
                    pasteStrings.add(new ArrayList<String>());
                    jComboBox2.addItem(Integer.toString(nichesInPaste / 100));

                }
                pasteStrings.get(nichesInPaste / 100).add(myString);
                nichesInPaste++;
            }
            if (isCalculatingMonthlySearches && CPC.equals("") && monthlySearches.equals("")) {
                monthySearchesStack.add(new MyElements(table.getRowCount(), myString));
                if (System.currentTimeMillis() - lastCheckedOverload < 108000) {
                    monthlySearches = "Overload";
                    CPC = "Overload";
                } else {
                    monthlySearches = "Calculating";
                    CPC = "Calculating";
                }
            }
            if (monthlySearches.equals("")) {
                monthlySearches = "-";
            }
            if (CPC.equals("")) {
                CPC = "-";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        addARow(myString, popularityScore, pageScore, domainScore, PRScore, domain, monthlySearches, CPC);
        return (isCalculatingMonthlySearches/*!domainScore.equals("-")*/);
    }

    public void addARow(String myString, String popularityScore, String pageScore, String domainScore, String PRScore, String domain, String monthlySearches, String CPC) {
        model.addRow(new Object[]{table.getRowCount() + 1, myString, popularityScore, pageScore, domainScore, PRScore, domain, monthlySearches, CPC});
        Color grey = new Color(0xE0E0E0);
        Color white = new Color(0xF8F8F8);
        if (table.getRowCount() % 2 == 0) {
            myTableColors.add(0, grey);
        } else {
            myTableColors.add(0, white);
        }
    }

    boolean checkDomain(String domain) {
        try {
            String search = "http://www.checkdomain.com/cgi-bin/checkdomain.pl?domain=" + domain;
            URL url = new URL(search);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "RssReader/1.0.88.0 (http://www.rssreader.com) Microsoft Windows NT 5.1.2600.0");
            String str = "";
            String finalText = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((str = in.readLine()) != null) {
                finalText = (new StringBuilder()).append(finalText).append(str).toString();
            }
            in.close();
            if (finalText.contains("is still available!")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean checkNiche(String niche) {
        String domain = niche.replaceAll(" ", "");
        if (checkDomain(domain + ".net")) {
            return true;
        } else if (checkDomain(domain + ".org")) {
            return true;
        } else {
            return false;
        }
    }
//    int getPageLinksNo(String page) {
//        try {
//            page = URLEncoder.encode(page, "UTF-8");
//            URL url = new URL("http://boss.yahooapis.com/ysearch/se_inlink/v1/" + page + "?appid=" + API_KEY + "&format=json&count=0&omit_inlinks=domain");
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            //System.out.println("res"+response);
//            JSONObject json = new JSONObject(response);
//            int mylinks = Integer.parseInt(json.getJSONObject("ysearchresponse").getString("totalhits"));
//            return mylinks;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
//
//    int getDomainLinksNo(String page) {
//        page = page.substring(0, page.indexOf("/", 10));
//        return getPageLinksNo(page);
//    }
    long pageScoreNumber;
    static MyLinksElement myLinks;
    static long[] pageLinks = new long[10];
    static long[] domainLinks = new long[10];

    void newArrays() {
        pageLinks = new long[10];
        domainLinks = new long[10];
        for (int i = 0; i < 10; i++) {
            pageLinks[i] = -1;
            domainLinks[i] = -1;
        }

    }

    String getPageScore(String query) {
        //query = "\"" + query + "\"";

        pageScoreNumber = -1;
        newArrays();
        try {
            myLinks.resultLinks = getResultLinks(query);
            long pageSum = 0;
            for (int k = 0; k < myLinks.resultLinks.size(); k++) {
                new LinksThread(k, 0).start();

//                int noOfLinks = getPageLinksNo(myLink);
//                pageSum += noOfLinks;
//                myLinks.pageLinksNo.add(noOfLinks);
            }
            int y = 0;
            while (!allPagesCalculated(myLinks.resultLinks.size()) && y < 6000 / 80) {
                y++;
                try {
                    Thread.sleep(80);
                } catch (Exception e) {
                }
            }

            for (int k = 0; k < myLinks.resultLinks.size(); k++) {
                pageSum += pageLinks[k];
                //System.out.println("gooooo:"+pageLinks[k]);
            }
            pageScoreNumber = pageSum / myLinks.resultLinks.size();
            return Long.toString(pageScoreNumber);
        } catch (java.lang.ArithmeticException e) {
            pageScoreNumber = 0;
            return "00";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "-";
    }

    boolean allPagesCalculated(int size) {
        for (int i = 0; i < size; i++) {
            if (pageLinks[i] == -1) {
                return false;
            }
        }
        return true;
    }

    boolean allDomainsCalculated(int size) {
        for (int i = 0; i < size; i++) {
            if (domainLinks[i] == -1) {
                return false;
            }
        }
        return true;
    }

    String getDomainScore(String query) {
        long domainScore = 0;
        //query = "\"" + query + "\"";
        try {
            if (myLinks.resultLinks == null) {
                myLinks.resultLinks = getResultLinks(query);
            }
            for (int k = 0; k < myLinks.resultLinks.size(); k++) {
                new LinksThread(k, 1).start();
            }
            int y = 0;
            while (!allDomainsCalculated(myLinks.resultLinks.size()) && y < 6000 / 80) {
                y++;
                try {
                    Thread.sleep(80);
                } catch (Exception e) {
                }
            }
            for (int i = 0; i < myLinks.resultLinks.size(); i++) {
                domainScore += (pageLinks[i] / 20 + 1) * Math.cbrt(domainLinks[i]) / myLinks.resultLinks.size();
            }
            if (pageScoreNumber != -1) {
                return Long.toString(domainScore);
            }
        } catch (java.lang.ArithmeticException e) {
            domainScore = 0;
            return "0";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }

//    ArrayList<String> getResultLinks(String query) {
//        //System.out.println("resultLinks:"+query);
//        ArrayList<String> resultLinks = new ArrayList<String>();
//        try {
//            query = URLEncoder.encode(query, "UTF-8");
////            URL url = new URL("http://boss.yahooapis.com/ysearch/web/v1/" + query + "?appid=" + API_KEY + "&format=json");//&count=10
////            URLConnection connection = url.openConnection();
////            String line;
////            StringBuilder builder = new StringBuilder();
////            BufferedReader reader = new BufferedReader(
////                    new InputStreamReader(connection.getInputStream()));
////            while ((line = reader.readLine()) != null) {
////                builder.append(line);
////            }
////            String response = builder.toString();
////            JSONObject json = new JSONObject(response);
////            JSONArray ja = json.getJSONObject("ysearchresponse").getJSONArray("resultset_web");
//            URL url = new URL("http://api.bing.net/json.aspx?AppId=" + bingAPI + "&Query=" + query + "&Sources=Web&Version=2.0&Market=en-us&JsonType=callback&JsonCallback=SearchCompleted");//&count=10
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            JSONObject json = new JSONObject(response);
//            JSONArray ja = json.getJSONObject("SearchResponse").getJSONObject("Web").getJSONArray("Results");
//            for (int i = 0; i < ja.length(); i++) {
//                resultLinks.add(ja.getJSONObject(i).getString("Url"));
//                //System.out.println("link " + i + ":" + ja.getJSONObject(i).getString("url"));
//            }
//        } catch (Exception e) {
//            //e.printStackTrace();
//        }
//        return resultLinks;
//    }
    ArrayList<String> getResultLinks(String query) {
        //System.out.println("resultLinks:"+query);
        ArrayList<String> resultLinks = new ArrayList<String>();
        try {
            query = URLEncoder.encode(query, "UTF-8");
//            String response = loadStringFromURL("http://api.bing.net/json.aspx?AppId=" + BingAPI + "&Query=" + query + "&Sources=Web&Version=2.0&Market=en-us&JsonType=callback&JsonCallback=SearchCompleted");
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            JSONObject json = new JSONObject(response);
//            JSONArray ja = json.getJSONObject("SearchResponse").getJSONObject("Web").getJSONArray("Results");
//            for (int i = 0; i < ja.length(); i++) {
//                resultLinks.add(ja.getJSONObject(i).getString("Url"));
//                //System.out.println("link " + i + ":" + ja.getJSONObject(i).getString("url"));
//            }


            String response = getBingResults("https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=%27" + query + "%27&$top=10&$format=JSON");
            //response = response.substring(16);
            JSONObject json = new JSONObject(response);
            JSONArray ja = json.getJSONObject("d").getJSONArray("results");
            for (int i = 0; i < ja.length(); i++) {
                resultLinks.add(ja.getJSONObject(i).getString("Url"));
            }



        } catch (Exception e) {
            //e.printStackTrace();
        }
        return resultLinks;
    }

//    String getPopularityScore(String query) {
//
//        query = "intitle:\"" + query + "\"";
//        System.out.println("\nQuerying pop for " + query);
//        try {
//            query = URLEncoder.encode(query, "UTF-8");
////            query = URLEncoder.encode(query, "UTF-8");
////            URL url = new URL("http://boss.yahooapis.com/ysearch/web/v1/" + query + "?appid=" + API_KEY + "&format=json&count=0");
////            URLConnection connection = url.openConnection();
////            String line;
////            StringBuilder builder = new StringBuilder();
////            BufferedReader reader = new BufferedReader(
////                    new InputStreamReader(connection.getInputStream()));
////            while ((line = reader.readLine()) != null) {
////                builder.append(line);
////            }
////            String response = builder.toString();
////            JSONObject json = new JSONObject(response);
//
//            URL url = new URL("http://api.bing.net/json.aspx?AppId=" + bingAPI + "&Query=" + query + "&Sources=Web&Version=2.0&Market=en-us&JsonType=callback&JsonCallback=SearchCompleted");//&count=10
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            //System.out.println("response:" + response);
//            JSONObject json = new JSONObject(response);
//            JSONObject ja = json.getJSONObject("SearchResponse").getJSONObject("Web");
//
//            return ja.getString("Total");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
    String getPopularityScore(String query) {
        query = "intitle:\"" + query + "\"";
//        try {
//            query = URLEncoder.encode(query, "UTF-8");
//            String response = loadStringFromURL("http://api.bing.net/json.aspx?AppId=" + BingAPI + "&Query=" + query + "&Sources=Web&Version=2.0&Market=en-us&JsonType=callback&JsonCallback=SearchCompleted");
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            //System.out.println("response:" + response);
//            JSONObject json = new JSONObject(response);
//            JSONObject ja = json.getJSONObject("SearchResponse").getJSONObject("Web");
//
//            return ja.getString("Total");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (Exception e) {
        }

        return getBingCountResult(query);
    }

    String getBingCountResult(String query) {
        String count = "";
        try {
            String URLString = "https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Composite?Sources=%27web%27&Query=%27" + query + "%27&$top=1&$format=Atom";
            String myResult = getBingResults(URLString);
            //System.out.println(myResult);
            String sign = "WebTotal m:type=\"Edm.Int64\">";
            int index = myResult.indexOf(sign);
            count = myResult.substring(index + sign.length(), myResult.indexOf("<", index));
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("API key problem");
            //e.printStackTrace();
        }
        return count;

    }
    int[] indexes;
    ArrayList<ArrayList<String>> myList;

    void plusplus(int pos) {
        if (indexes[pos] < myList.get(pos).size() - 1) {
            indexes[pos] = indexes[pos] + 1;
        } else if ((myList.size() > 1) && (pos < myList.size() - 1)) {
            indexes[pos] = 0;
            plusplus(pos + 1);
        }
    }

    boolean allIndexesNotAtTop() {
        for (int j = 0; j < indexes.length; j++) {
            if (indexes[j] < myList.get(j).size() - 1) {
                return true;
            }
        }
        return false;
    }
    RevealHotNichesTask rv;

    @Action
    public Task revealHotNiches() {
//        if (myThread.getState().equals(Thread.State.TERMINATED)) {
//            myThread.start();
//        }
        rv = new RevealHotNichesTask(getApplication());
        return rv;
    }

    static void showMessage(String message) {
        JOptionPane op = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        javax.swing.JDialog dialog = op.createDialog("Info");

        //dialog.setModal(true);
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private class RevealHotNichesTask extends org.jdesktop.application.Task<Object, Void> implements Runnable {

        RevealHotNichesTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RevealHotNichesTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            if (!jTextField10.getText().equals("")) {
                stop = false;
                running = true;
                jButton1.setEnabled(false);
                try {
                    String search = "";//http://domain-daily.com/new/" + jTextField2.getText() + "-" + jTextField3.getText() + "-" + jTextField4.getText() + "/1.html";
                    URL url = null;//new URL(search);
//                //System.out.println("11");
                    URLConnection conn = null;//url.openConnection();
//                //conn.setConnectTimeout(1000);
//                //System.out.println("22");
//                conn.setRequestProperty("User-Agent", "RssReader/1.0.88.0 (http://www.rssreader.com) Microsoft Windows NT 5.1.2600.0");
                    String str = "";
                    String result = "";
                    BufferedReader in = null;//new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    //while ((str = in.readLine()) != null) {
//                    result = (new StringBuilder()).append(result).append(str).toString();
//                }
//                in.close();
//                if (!result.contains("No Records.")) {
                    System.out.println("second1");
                    search = "http://dailydomains.org/domain/" + jTextField2.getText() + jTextField3.getText() + jTextField4.getText();
                    url = new URL(search);
                    //System.out.println("11");
                    URLConnection conn2 = url.openConnection();
                    //conn.setConnectTimeout(1000);
                    //System.out.println("22");
                    conn2.setRequestProperty("User-Agent", "RssReader/1.0.88.0 (http://www.rssreader.com) Microsoft Windows NT 5.1.2600.0");
                    str = "";
                    result = "";
                    in = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    while ((str = in.readLine()) != null) {
                        result = (new StringBuilder()).append(result).append(str.trim()).toString();
                    }
                    in.close();
                    //System.out.println("res:"+result);
                    int selectionEnd = result.indexOf("/page/2\"><strong>Next") - 35;

                    //int selectionEnd = result.indexOf("1lafayette.com");
                    int selectionStart = result.substring(0, selectionEnd).lastIndexOf(">") + 1;
                    int numberOfPages = Integer.parseInt(result.substring(selectionStart, selectionEnd));
                    System.out.println("no pages:" + numberOfPages);
                    jProgressBar1.setMaximum(numberOfPages);
                    jProgressBar1.setStringPainted(true);
                    jProgressBar2.setStringPainted(true);
                    jProgressBar1.setString("Total Progress " + jProgressBar1.getPercentComplete() + " %");
                    jProgressBar2.setString("Thread Progress " + 0 + " %");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                    }
                    int[] allNumbers = new int[numberOfPages];
                    for (int i = 0; i < numberOfPages; i++) {
                        allNumbers[i] = i + 1;
                    }
                    shuffle(allNumbers);
                    for (int i = 0; i < numberOfPages; i++) {
                        jProgressBar1.setValue(i);
                        String procent = Double.toString(jProgressBar1.getPercentComplete() * 100);
                        procent += "00";
                        String finalProcent = procent.substring(0, procent.indexOf(".") + 4);
                        jProgressBar1.setString("Total Progress " + finalProcent + " %");
                        search = "http://dailydomains.org/domain/" + jTextField2.getText() + jTextField3.getText() + jTextField4.getText() + "/page/" + allNumbers[i];
                        System.out.println(allNumbers[i] + "myseach" + search);
                        url = new URL(search);
                        conn = url.openConnection();
                        conn.setRequestProperty("User-Agent", "Shareaza v1.2.3.45");
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        str = "";
                        result = "";
                        while ((str = in.readLine()) != null) {
                            result = (new StringBuilder()).append(result).append(str.trim()).toString();
                        }

                        String[] domains = result.split("valign=\"top\"");
                        for (int t = 1; t < domains.length - 1; t++) {
                            domains[t] = domains[t].substring(1, domains[t].indexOf("<")).trim();
                        }
                        domains[domains.length - 1] = domains[domains.length - 1].substring(1, domains[domains.length - 1].indexOf("<")).trim();
                        jProgressBar2.setMaximum(domains.length - 1);
                        for (int k = 1; k < domains.length; k++) {

                            jProgressBar2.setValue(k - 1);
                            procent = Double.toString(jProgressBar2.getPercentComplete() * 100);
                            finalProcent = procent.substring(0, procent.indexOf(".") + 2);
                            jProgressBar2.setString("Thread Progress " + finalProcent + " %");
                            if (stop) {
                                jProgressBar2.setValue(0);
                                jProgressBar2.setValue(0);
                                jProgressBar2.setString("Thread Progress " + "0 %");
                                jProgressBar1.setString("Total Progress " + "0 %");

                                jButton2.setText("Stop");
                                jButton1.setEnabled(true);
                                stop = false;
                                return null;
                            }
                            while (pause && !stop) {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }
                            if ((jRadioButton4.isSelected() && domains[k].endsWith(".com")) || !jRadioButton4.isSelected()) {
                                String domainName = domains[k].substring(0, domains[k].lastIndexOf("."));
                                //System.out.println("domainName:" + domainName);
                                String mySugestion = getSuggestion(domainName);
                                if (!mySugestion.equals("")) {
                                    checkKeywordPhrase(mySugestion, domainName, "", "");
                                }
                            }
                        }
                    }
//                } else {
//                    int selectionEnd = result.indexOf("<div class=\"footer\">");
//                    int selectionStart = selectionEnd - 38;
//                    String selection = result.substring(selectionStart, selectionEnd);
//                    int numberOfPages = Integer.parseInt(selection.substring(selection.indexOf(">") + 1, selection.indexOf("<")));
//                    jProgressBar1.setMaximum(numberOfPages);
//                    jProgressBar1.setStringPainted(true);
//                    jProgressBar2.setStringPainted(true);
//                    jProgressBar1.setString("Total Progress " + jProgressBar1.getPercentComplete() + " %");
//                    jProgressBar2.setString("Thread Progress " + 0 + " %");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException ex) {
//                    }
//                    int[] allNumbers = new int[numberOfPages];
//                    for (int i = 0; i < numberOfPages; i++) {
//                        allNumbers[i] = i + 1;
//                    }
//                    shuffle(allNumbers);
//                    for (int i = 0; i < numberOfPages; i++) {
//                        jProgressBar1.setValue(i);
//                        String procent = Double.toString(jProgressBar1.getPercentComplete() * 100);
//                        procent += "00";
//                        String finalProcent = procent.substring(0, procent.indexOf(".") + 4);
//                        jProgressBar1.setString("Total Progress " + finalProcent + " %");
//                        search = "http://domain-daily.com/new/" + jTextField2.getText() + "-" + jTextField3.getText() + "-" + jTextField4.getText() + "/" + allNumbers[i] + ".html";
//                        System.out.println(allNumbers[i] + "myseach" + search);
//                        url = new URL(search);
//                        conn = url.openConnection();
//                        conn.setRequestProperty("User-Agent", "Shareaza v1.2.3.45");
//                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        str = "";
//                        result = "";
//                        while ((str = in.readLine()) != null) {
//                            result = (new StringBuilder()).append(result).append(str).toString();
//                        }
//                        int indexStartDomains = result.indexOf("<p>") + 3;
//                        int indexEndDomains = result.indexOf("</p>");
//                        String domainsGross = result.substring(indexStartDomains, indexEndDomains);
//                        String[] domains = domainsGross.split("<br>");
//                        jProgressBar2.setMaximum(domains.length);
//                        for (int k = 0; k < domains.length; k++) {
//
//                            jProgressBar2.setValue(k);
//                            procent = Double.toString(jProgressBar2.getPercentComplete() * 100);
//                            finalProcent = procent.substring(0, procent.indexOf(".") + 2);
//                            jProgressBar2.setString("Thread Progress " + finalProcent + " %");
//                            if (stop) {
//                                jProgressBar2.setValue(0);
//                                jProgressBar2.setValue(0);
//                                jProgressBar2.setString("Thread Progress " + "0 %");
//                                jProgressBar1.setString("Total Progress " + "0 %");
//
//                                jButton2.setText("Stop");
//                                jButton1.setEnabled(true);
//                                stop = false;
//                                return null;
//                            }
//                            while (pause && !stop) {
//                                try {
//                                    Thread.sleep(1000);
//                                } catch (Exception e) {
//                                }
//                            }
//                            try {
//                                domains[k] = domains[k].substring(domains[k].indexOf("\">") + 2, domains[k].indexOf("</"));
//
//                            } catch (Exception e) {
//                            }
//                            if ((jRadioButton4.isSelected() && domains[k].endsWith(".com")) || !jRadioButton4.isSelected()) {
//                                String domainName = domains[k].substring(0, domains[k].lastIndexOf("."));
//                                //System.out.println("domainName:" + domainName);
//                                String mySugestion = getSuggestion(domainName);
//                                if (!mySugestion.equals("")) {
//                                    checkKeywordPhrase(mySugestion, domainName, "", "");
//                                }
//                            }
//
//                        }
//                    }
//                }
                } catch (java.net.SocketException e) {

                    ///e.printStackTrace();
                    showMessage("Internet Connection Error...\nPlease try later!");


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                showMessage("Please enter a Bing API key");
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            jProgressBar1.setValue(0);
            jProgressBar2.setValue(0);
        }
    }

    private Object makeObj(final String item) {
        return new Object() {

            public String toString() {
                return item;
            }
        };
    }

    @Action
    public void clearAll() {
        try {
            deleteAllRows(model);
            monthySearchesStack.removeAll(monthySearchesStack);
            jComboBox2.removeAllItems();
            jComboBox2.addItem(makeObj("Copy 100 niches list"));
            pasteStrings = new ArrayList<ArrayList<String>>();
            nichesInPaste = 0;
        } catch (Exception e) {
            jComboBox2.addItem(makeObj("Copy 100 niches list"));
        }
    }

    public void deleteAllRows(DefaultTableModel model) {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }

    @Action
    public void enterCaptcha() {
        captchaEntered = true;
        //jLabel6.setIcon(null);
    }

    @Action
    public Task checkKeywordPhraseAction() {
        return new CheckKeywordPhraseActionTask(getApplication());
//        myThread = new Thread(this);
//        oneTime = true;
//        myThread.start();
    }

    private class CheckKeywordPhraseActionTask extends org.jdesktop.application.Task<Object, Void> {

        CheckKeywordPhraseActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to CheckKeywordPhraseActionTask fields, here.
            super(app);
            //        if (myThread.getState().equals(Thread.State.TERMINATED)) {
            //            myThread = new Thread(this);
            //        }
            //        if (!myThread.isAlive()) {
            //            myThread.start();
            //        }
            //        stop = false;

            //        myThread = new Thread(this);
            //        oneTime = true;
            //        myThread.start();
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            checkKeywordPhrase(jTextField7.getText(), "", "", "");
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public void googleKeywordTool() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Desktop.getDesktop().browse(new URI("https://adwords.google.com/select/KeywordToolExternal"));
                } catch (Exception ex2) {
                }
            }
        });
    }

    @Action
    public void getYahooAPI() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Desktop.getDesktop().browse(new URI("https://developer.apps.yahoo.com/wsregapp/"));
                } catch (Exception ex2) {
                }
            }
        });
    }

    @Action
    public void ammseoSpecialOffer() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.ammseo.com"));
                } catch (Exception ex2) {
                }
            }
        });
    }

    void saveGeneralOptionsToFile(String generalOptionsFile) {
        try {
            if (generalOptionsFile.indexOf(".go") == -1) {
                generalOptionsFile += ".go";
            }
            FileWriter fstream = new FileWriter(generalOptionsFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("$BingAPI:");
            out.write(jTextField10.getText() + "\n");

            out.write("$Language:");
            out.write(jComboBox1.getSelectedItem().toString() + "\n");

            out.write("$Country:");
            out.write(jComboBox3.getSelectedItem().toString() + "\n");

            out.write("$AMMSEO:");
            if (jRadioButton4.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }

            out.write("$PopularityScore:");
            if (jCheckBox3.isSelected()) {
                out.write(jTextField1.getText() + "\n");
            } else {
                out.write("-" + "\n");
            }
            out.write("$PageScore:");
            if (jCheckBox4.isSelected()) {
                out.write(jTextField5.getText() + "\n");
            } else {
                out.write("-" + "\n");
            }
            out.write("$DomainScore:");
            if (jCheckBox5.isSelected()) {
                out.write(jTextField6.getText() + "\n");
            } else {
                out.write("-" + "\n");
            }
            out.write("$DomainAvailable:");
            if (jCheckBox2.isSelected()) {
                out.write("true" + "\n");
            } else {
                out.write("false" + "\n");
            }
            out.write("$Synonims:");
            if (jRadioButton1.isSelected()) {
                out.write("No" + "\n");
            } else if (jRadioButton2.isSelected()) {
                out.write("Passed" + "\n");
            } else {
                out.write("All" + "\n");
            }
            out.write("$AdditionalNiches:");
            if (jCheckBox1.isSelected()) {
                out.write(jTextField9.getText() + "\n");
            } else {
                out.write("-" + "\n");
            }
            out.write("$AllPopularity:");
            if (jCheckBox6.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }
            out.write("$AllPage:");
            if (jCheckBox7.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }
            out.write("$AllDomainScore:");
            if (jCheckBox8.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }
            out.write("$AllDomainAvailable:");
            if (jCheckBox9.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }
            out.write("$AllAdditionalNiches:");
            if (jCheckBox10.isSelected()) {
                out.write("true\n");
            } else {
                out.write("false\n");
            }
            out.close();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
    }

    @Action
    public void saveAllSettings() {
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".go");
            }

            @Override
            public String getDescription() {
                return ".go Files";
            }
        };
        String generalOptionsFile = null;
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser("c:\\HotNichesRevealed\\Data");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(jPanel1);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            generalOptionsFile = chooser.getSelectedFile().getPath();
        }
        saveGeneralOptionsToFile(generalOptionsFile);


    }

    @Action
    public void loadSettings() {
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".go");//general options
            }

            @Override
            public String getDescription() {
                return ".go Files";
            }
        };
        String generalOptionsFile = null;
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser("c:\\HotNichesRevealed\\Data");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(jPanel1);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            generalOptionsFile = chooser.getSelectedFile().getPath();
        }
        if (generalOptionsFile != null) {
            loadGeneralOptionsFromFile(generalOptionsFile);


        }
    }
    public String bingAPI;

    void loadGeneralOptionsFromFile(String generalOptionsFile) {
        try {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(generalOptionsFile));
            } catch (Exception e) {
            }
            //StringBuilder fileData = new StringBuilder(1000);
            String str1;
            while ((str1 = in.readLine()) != null) {
                if (str1.startsWith("$BingAPI:")) {
                    jTextField10.setText(str1.substring(str1.indexOf(":") + 1));
                }
                if (jTextField10.getText().equals("")) {
                    bingAPI = BING_API;
                } else {
                    bingAPI = jTextField10.getText();
                }
                if (str1.startsWith("$Language:")) {
                    jComboBox1.setSelectedItem(str1.substring(str1.indexOf(":") + 1));
                }
                if (str1.startsWith("$Country:")) {
                    jComboBox3.setSelectedItem(str1.substring(str1.indexOf(":") + 1));
                }
                if (str1.startsWith("$AMMSEO:")) {
                    if (str1.contains("true")) {
                        jRadioButton4.setSelected(true);
                    } else {
                        jRadioButton5.setSelected(true);
                    }
                }
                if (str1.startsWith("$AllPopularity:")) {
                    if (str1.contains("true")) {
                        jCheckBox6.setSelected(true);
                    } else {
                        jCheckBox6.setSelected(false);
                    }
                }
                if (str1.startsWith("$AllPage:")) {
                    if (str1.contains("true")) {
                        jCheckBox7.setSelected(true);
                    } else {
                        jCheckBox7.setSelected(false);
                    }
                }
                if (str1.startsWith("$AllDomainScore:")) {
                    if (str1.contains("true")) {
                        jCheckBox8.setSelected(true);
                    } else {
                        jCheckBox8.setSelected(false);
                    }
                }
                if (str1.startsWith("$AllDomainAvailable:")) {
                    if (str1.contains("true")) {
                        jCheckBox9.setSelected(true);
                    } else {
                        jCheckBox9.setSelected(false);
                    }
                }
                if (str1.startsWith("$AllAdditionalNiches:")) {
                    if (str1.contains("true")) {
                        jCheckBox10.setSelected(true);
                    } else {
                        jCheckBox10.setSelected(false);
                    }
                }
                if (str1.startsWith("$PopularityScore:")) {
                    if (str1.contains("-")) {
                        jCheckBox3.setSelected(false);
                    } else {
                        jCheckBox3.setSelected(true);
                        jTextField1.setText(str1.substring(str1.indexOf(":") + 1));
                    }
                }
                if (str1.startsWith("$PageScore:")) {
                    if (str1.contains("-")) {
                        jCheckBox4.setSelected(false);
                    } else {
                        jCheckBox4.setSelected(true);
                        jTextField5.setText(str1.substring(str1.indexOf(":") + 1));
                    }
                }
                if (str1.startsWith("$DomainScore:")) {
                    if (str1.contains("-")) {
                        jCheckBox5.setSelected(false);
                    } else {
                        jCheckBox5.setSelected(true);
                        jTextField6.setText(str1.substring(str1.indexOf(":") + 1));
                    }
                }
                if (str1.startsWith("$DomainAvailable:")) {
                    if (str1.contains("false")) {
                        jCheckBox2.setSelected(false);
                    } else {
                        jCheckBox2.setSelected(true);
                    }
                }
                if (str1.startsWith("$Synonims:")) {
                    if (str1.contains("No")) {
                        jRadioButton1.setSelected(true);
                    } else if (str1.contains("All")) {
                        jRadioButton3.setSelected(true);
                    } else {
                        jRadioButton2.setSelected(true);
                    }
                }
                if (str1.startsWith("$AdditionalNiches:")) {
                    if (str1.contains("-")) {
                        jCheckBox1.setSelected(false);
                    } else {
                        jCheckBox1.setSelected(true);
                        jTextField9.setText(str1.substring(str1.indexOf(":") + 1));
                    }
                }

            }
            if (jCheckBox6.isSelected()) {
                jCheckBox3.setEnabled(false);
            } else {
                jCheckBox3.setEnabled(true);
            }
            if (jCheckBox7.isSelected()) {
                jCheckBox4.setEnabled(false);
            } else {
                jCheckBox4.setEnabled(true);
            }
            if (jCheckBox8.isSelected()) {
                jCheckBox5.setEnabled(false);
            } else {
                jCheckBox5.setEnabled(true);
            }
            if (jCheckBox9.isSelected()) {
                jCheckBox2.setEnabled(false);
            } else {
                jCheckBox2.setEnabled(true);
            }
            if (jCheckBox10.isSelected()) {
                jCheckBox1.setEnabled(false);
            } else {
                jCheckBox1.setEnabled(true);
            }

            in.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action
    public void whoIs() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.who.is"));
                } catch (Exception ex2) {
                }
            }
        });

    }

    @Action
    public void getHotProductsRevealed() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.hotproductsrevealed.com"));
                } catch (Exception ex2) {
                }
            }
        });

    }

    @Action
    public void getBingAPI() {
        try {
            Desktop.getDesktop().browse(new URI("https://datamarket.azure.com/dataset/8818f55e-2fe5-4ce3-a617-0b8ba8419f65"));
        } catch (Exception ex2) {
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
