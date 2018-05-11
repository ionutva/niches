/*
 * HotNichesRevealedApp.java
 */
package hotnichesrevealed;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class HotNichesRevealedApp extends SingleFrameApplication {

    String mess = "";

    boolean chC(String activationCode, String activate) {
        try{
         if (activationCode.charAt(2) == '0') {
              //String REALKEY = "honire12011";
              //Preferences p = Preferences.systemRoot();
              Properties prop = new Properties();
//              if (p.get(REALKEY, "HKLM12").equals("HKLM12")) {
//                   p.put(REALKEY, "2yuyusde5");
//              }
              //System.out.println("h"+prop.getProperty("hon"));
              try{
              prop.load(new FileInputStream(new File("c:\\HotNichesRevealed\\System\\lib\\s")));
              }catch(Exception e){}
                if(prop.getProperty("hon")==null){
                   prop.setProperty("hon", "ac1");
                   prop.store(new FileOutputStream(new File("c:\\HotNichesRevealed\\System\\lib\\s")), mess);
                }
                else if(prop.getProperty("hon").equals("ac1") && activate.equals("true")){
                 mess = "Only 1 Trial is allowed!!";
                 return false;
              }
          }
        }catch(Exception e){}

        String result = "";
        try {
            URL url = new URL("http://www.hotnichesrevealed.com/c2.php?c=" + activationCode + "&m=" + getMAC() + "&a=" + activate);
            //System.out.println("mac:" + getMAC());
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str = "";

            while ((str = in.readLine()) != null) {
                result = (new StringBuilder()).append(result).append(str).toString();
            }
            in.close();

        } catch (Exception e) {
        }
        if (!result.equals("ok")) {
            if(mess.contains("<br")){
                System.out.println(mess);
                mess = "Server unavailable";
            }else{
               mess = result;
            }
            return false;
        } else {
            return true;
        }
    }

    void ch() {
        File keyFile = new File("c:\\HotNichesRevealed\\System\\key.txt");
        if (keyFile.exists()) {
            String code = "";
            try {
                FileInputStream fstream = new FileInputStream(keyFile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = br.readLine()) != null) {
                    code+=line;
                }
                in.close();
            } catch (Exception e) {}
            if (chC(code, "false")) {
                showit();
            } else {
                HotNichesRevealedView.showMessage(mess);
                chF();
            }
        } else {
            chF();
        }
    }
    final JFrame licenseFrame = new JFrame("License");

    void chF() {

        licenseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        licenseFrame.setLayout(null);
        JLabel lregistrationCode = new JLabel("Registration Code:");
        lregistrationCode.setBounds(25, 20, 110, 20);
        JButton okButton = new JButton("Enter");
        JButton buyButton = new JButton("<html>Get a Free Trial<br/>or Buy License</html>");
        okButton.setBounds(25, 100, 110, 35);
        buyButton.setBounds(25, 140, 110, 35);
        final JTextField tflicenseCode = new JTextField();
        tflicenseCode.setBounds(25, 60, 110, 20);
        licenseFrame.add(tflicenseCode);
        licenseFrame.add(lregistrationCode);
        licenseFrame.add(okButton);
        licenseFrame.add(buyButton);
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (chC(tflicenseCode.getText(), "true")) {
                    try {
                        FileWriter fstream = new FileWriter("c:\\HotNichesRevealed\\System\\key.txt");
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write(tflicenseCode.getText());
                        out.close();
                    } catch (Exception ex) {
                    }
                    showit();
                } else {
                    HotNichesRevealedView.showMessage(mess);
                    if(mess.equals("Already activated!")){
                        try{
                        FileWriter fstream = new FileWriter("c:\\HotNichesRevealed\\System\\key.txt");
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write(tflicenseCode.getText());
                        out.close();
                        }catch(Exception ex2){}
                     ch();

                    }else{
                    File keyFile = new File("c:\\HotNichesRevealed\\System\\key.txt");
                    try {
                        keyFile.delete();
                    } catch (Exception ex) {
                    }
                    //System.exit(0);


                    }
                }
            }
        };
        ActionListener buyListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("www.hotnichesrevealed.com"));
                } catch (Exception ex) {
                }

            }
        };


        okButton.addActionListener(listener);
        buyButton.addActionListener(buyListener);
        licenseFrame.setBounds(100, 100, 170, 240);
        licenseFrame.setVisible(true);
    }

    void showit() {
        licenseFrame.setVisible(false);
        show(new HotNichesRevealedView(this));
    }

    String getMAC() {
        String theMAC = "";
        try {
            InetAddress address = InetAddress.getLocalHost();

            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    /*
                     * Extract each array of mac address and convert it to hexa with the
                     * following format 08-00-27-DC-4A-9E.
                     */
                    for (int i = 0; i < mac.length; i++) {
                        theMAC += String.format("%02x", mac[i]);
                    }
                } else {
                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified address is not found.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return theMAC;




    }

    /**
     * At startup create and show the main frame of the application.
     */
    public static HotNichesRevealedView myView;
    @Override
    protected void startup() {

        //System.exit(0);
        //System.out.println("mac="+getMAC());
        myView = new HotNichesRevealedView(this);
        ch();

    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of HotNichesRevealedApp
     */
    public static HotNichesRevealedApp getApplication() {
        return Application.getInstance(HotNichesRevealedApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(HotNichesRevealedApp.class, args);
    }
}
