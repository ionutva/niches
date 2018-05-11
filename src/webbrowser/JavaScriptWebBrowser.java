/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package webbrowser;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import hotnichesrevealed.HotNichesRevealedApp;
import java.awt.Color;
import java.awt.Desktop.Action;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;

import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.jdesktop.application.Application;
import org.junit.runners.Parameterized.RunAllParameterMethods;

/**
 * @author Christopher Deckers
 */
public class JavaScriptWebBrowser extends JPanel{
  public JWebBrowser webBrowser;
  public String javaScript2;

    public String getJavaScript() {
        return javaScript2;
    }

    public void setJavaScript(String javaScript) {
        this.javaScript2 = javaScript;

    }

    //get web page content!!!!

//public void run(){
//    SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//              do{
////                 try{
////                  Thread.sleep(50);
////                  //
////                  }catch(Exception e){}
//                     if(webBrowser.getHTMLContent()!=null) System.out.println("oook:"+webBrowser.getHTMLContent().length());
//
//               }
//              while(webBrowser.getHTMLContent()==null || webBrowser.getHTMLContent().length() < 42);
//
//              System.out.println("gggg:"+webBrowser.getHTMLContent());
//              //webBrowser.setHTMLContent(webBrowser.getHTMLContent().replace("cuvinte","cuvintele"));
////              while(true){
////              webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \"ook\";document.getElementById('gwt-debug-searchInput-limitResults-input').checked = true;document.getElementById('gwt-debug-ideas-table-header-GLOBAL_MONTHLY_SEARCHES-3').click();document.getElementById('gwt-debug-searchPanel-searchButton').click();");
////                }
//            }
//        });
//
//}

JWebBrowser myBrowser;
  public JavaScriptWebBrowser(JWebBrowser browser,String location, String javaScript) {

    super(new BorderLayout());
      myBrowser = browser;
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    webBrowser = new JWebBrowser();
    webBrowser.navigate(location);
    // Create an additional bar allowing to show/hide the menu bar of the web browser.
    //JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());
    menuBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    JPanel closePanel = new JPanel(new BorderLayout());
    JButton closeButton = new JButton("Close");
    JButton runButton = new JButton("Run");
    javaScript2 = javaScript;

                SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        webBrowser.executeJavascript(javaScript2);
                        Thread.sleep(1000);
                        Robot robot = new Robot();
                        webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').focus();");
                        Thread.sleep(1000);
                        robot.keyPress(KeyEvent.VK_SPACE);
                        robot.keyRelease(KeyEvent.VK_SPACE);
                        
                        webBrowser.executeJavascript("document.getElementById('gwt-debug-searchPanel-searchButton').focus();");
                        Thread.sleep(1000);
                        robot.keyPress(KeyEvent.VK_ENTER);
                        robot.keyRelease(KeyEvent.VK_ENTER);
                    } catch (Exception e) {
                    }
                }
            });



    closePanel.add(menuBarCheckBox,BorderLayout.WEST);
    add(closePanel, BorderLayout.NORTH);
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    firsttime = false;
  }  
  public JavaScriptWebBrowser(String location, String javaScript) {
    super(new BorderLayout());

    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    webBrowser = new JWebBrowser();
    webBrowser.navigate(location);
    // Create an additional bar allowing to show/hide the menu bar of the web browser.
    //JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());
    menuBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
//    buttonPanel.add(menuBarCheckBox);
//    add(buttonPanel, BorderLayout.SOUTH);

    //close
    JPanel closePanel = new JPanel(new BorderLayout());
    JButton closeButton = new JButton("Close");
    JButton runButton = new JButton("Run");
    //runButton.setBackground(Color.red);
    //runButton.setForeground(Color.red);

    closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                //seo2.SEO2App.myView.closeAdd();
            }
        });
    final String javaS = javaScript;
    runButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                //System.out.println("ok777"+webBrowser.getHTMLContent());
                if(javaScript2==null){
                    webBrowser.executeJavascript(javaS);
                }else{
                    webBrowser.executeJavascript(javaScript2);
                }
            }
        });
                

//     final SwingWorker worker = new SwingWorker() {
//
//            @Override
//            protected Object doInBackground(){
//                webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \"ok\";document.getElementById('gwt-debug-searchInput-limitResults-input').checked = true;document.getElementById('gwt-debug-ideas-table-header-GLOBAL_MONTHLY_SEARCHES-3').click();document.getElementById('gwt-debug-searchPanel-searchButton').click();");
//                try{
//                Thread.sleep(1000);
//                }catch(Exception e){}
//                return null;
//            }
//    };
//    int i = 10;
//    while(i>9){
//        System.out.println("ljs55");
//       worker.execute();
//    }

//    AbstractAction updateCursorAction = new AbstractAction() {
//
//    public void actionPerformed(ActionEvent e) {
//        Object o = webBrowser.executeJavascriptWithResult("return document.getElementById('gwt-debug-searchInput-keywordTextbox').value");
//        if(o==null || o.toString().equals("")){
//          System.out.println("exe" + webBrowser.executeJavascriptWithResult("document.getElementById('gwt-debug-searchInput-keywordTextbox').value = \"ok\";document.getElementById('gwt-debug-searchInput-limitResults-input').checked = true;document.getElementById('gwt-debug-ideas-table-header-GLOBAL_MONTHLY_SEARCHES-3').click();document.getElementById('gwt-debug-searchPanel-searchButton').click();"));
//       }
//
//}};
//
//
//    timer1 = new Timer(1000, updateCursorAction);
//    timer1.start();

   executeJS(javaS);

 
    //closePanel.add(closeButton,BorderLayout.EAST);
    //closePanel.add(runButton,BorderLayout.CENTER);
    closePanel.add(menuBarCheckBox,BorderLayout.WEST);
    
    add(closePanel, BorderLayout.NORTH);
    //end close
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);

    add(webBrowserPanel, BorderLayout.CENTER);
    

    //webBrowser.getWebBrowserWindow().requestFocus();
    firsttime = false;
  }
  boolean firsttime = true;
  public void executeJS(String javaS){
    timer = new Timer(300,new myActionListener(javaS));
    timer.start();

  }

  Timer timer;


  class myActionListener implements ActionListener{
  int times = 0;
  private String javaS;
  public myActionListener(String javaS) {
      this.javaS = javaS;
      
  }
        public void actionPerformed(ActionEvent evt) {
        times++;
        System.out.println("exe" + webBrowser.executeJavascriptWithResult(javaS));
        //Object o = webBrowser.executeJavascriptWithResult("return document.getElementById('gwt-debug-searchInput-keywordTextbox').value");
        Object o2 = webBrowser.executeJavascriptWithResult("return document.getElementById('gwt-debug-searchInput-limitResults-input').checked");
        //if((webBrowser.getHTMLContent()!=null && webBrowser.getHTMLContent().length() > 42 && o!=null && !o.toString().equals("")) || ((firsttime &&times >=15)||((!firsttime) && times >=5 ))){
        if(((o2!=null && !o2.toString().equals("false")) && webBrowser.getHTMLContent()!=null && webBrowser.getHTMLContent().length() > 42) || (times >=15)){
            
            if(timer!=null) timer.stop();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        
                        Robot robot = new Robot();
                        webBrowser.executeJavascript("document.getElementById('gwt-debug-searchInput-keywordTextbox').focus();");
                        Thread.sleep(500);
                        robot.keyPress(KeyEvent.VK_SPACE);
                        robot.keyRelease(KeyEvent.VK_SPACE);
                        
                        webBrowser.executeJavascript("document.getElementById('gwt-debug-searchPanel-searchButton').focus();");
                        Thread.sleep(500);
                        robot.keyPress(KeyEvent.VK_ENTER);
                        robot.keyRelease(KeyEvent.VK_ENTER);
                    } catch (Exception e) {
                    }
                }
            });
        }
        
    }
  }

public void setlocation(String location){
    webBrowser.navigate(location);
}

}
