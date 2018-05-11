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


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.Desktop;
import java.net.URI;

import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import hotnichesrevealed.HotNichesRevealedApp;
import hotnichesrevealed.HotNichesRevealedView;

/**
 * @author Christopher Deckers
 */
public class WebBrowser extends JPanel { 
  public JWebBrowser webBrowser;
  public WebBrowser(String location) {
      super(new BorderLayout());
      showWebBrowser(location, false, 0);
    }
  public WebBrowser(String location, boolean defaultBrowser) {
      super(new BorderLayout());
      showWebBrowser(location, defaultBrowser,0);
    }
  public WebBrowser(String location, int index) {
      super(new BorderLayout());
      showWebBrowser(location, false,index);
    }
  static String mylocation;
   private void showWebBrowser(String location, boolean defaultBrowser,int index) {
    mylocation = location;
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    webBrowser = new JWebBrowser();
    webBrowser.setMenuBarVisible(false);
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
    JPanel upPanel = new JPanel(new BorderLayout());
    JPanel eastPanel = new JPanel(new BorderLayout());
    

    JButton defaultBrowserButton = new JButton("System Browser");

     

    upPanel.add(eastPanel,BorderLayout.EAST);
    if(defaultBrowser) {
        eastPanel.add(defaultBrowserButton,BorderLayout.WEST);
    }
    
    upPanel.add(menuBarCheckBox,BorderLayout.WEST);
    
    
    add(upPanel, BorderLayout.NORTH);
    //end close
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);

    add(webBrowserPanel, BorderLayout.CENTER);
    //webBrowser.getWebBrowserWindow().requestFocus();

  }
public void setlocation(String location){
    webBrowser.navigate(location);
}
 
}
