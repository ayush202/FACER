/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

/**
 *
 * @author ayush
 */
public class integrated_implement implements Runnable {

    JPanel panel;
    JTextArea jta;
    JFrame f1;
    JDialog f2;
    ButtonGroup buttonGroup1, buttonGroup2;
    JTextField name;
    train xyz;
    int assign;
    boolean check;
    String name1;

    public integrated_implement(train obj, int x, boolean t, String s) {
        panel = obj.panel;
        buttonGroup1 = obj.buttonGroup1;
        buttonGroup2 = obj.buttonGroup2;
        name = obj.name;
        f1 = obj.f1;
        f2 = obj.f2;
        jta = obj.jta;
        xyz = obj;
        assign = x;
        check = t;
        name1 = s;
    }

    @Override
    public void run() {
        try {
            String fromClient = "";
            ServerSocket server = new ServerSocket(8080);
            System.out.println("wait for connection on port 8080");
            ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                    home.path_to_python + "/openface-master"
                    + "/demos/dataset_create.py", "--captureDevice", "0", "--width",
                    "500", "--height", "500", "--outDir", home.path_to_python + "/openface-master/aligned-images/", "--name", "" + assign);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println("reached here1");
            String s = "", s1;
            if (!p.isAlive() && p.exitValue() == 1) {
                while ((s1 = br.readLine()) != null) {
                    s = s + s1 + "\n";
                }
                //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                System.out.println("returned with exit status 1");
                System.out.println(s);
                home.showMessage(s.toString());
                System.exit(1);

                return;
            }
            Socket client = server.accept();
            System.out.println("got connection on port 8080");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            f2.setUndecorated(true);
            f2.setLocation(100, 200);
            f2.pack();
            jta.setText("");
            jta.setBackground(Color.orange);
            System.out.println("while loop goin to start");
            f2.setVisible(true);
            File file = null;
            FileWriter fout = null;
            PrintWriter pout = null;
            while (!client.isClosed()) {
                fromClient = in.readLine();
                if (fromClient == null) {
                    continue;
                }
                if (fromClient.equals("directory created")) {
                    file = new File(home.path_to_python + "/openface-master/aligned-images/" + assign + "/index.txt");
                    file.createNewFile();
                    System.out.println("value of assign " + assign + " " + file);
                    fout = new FileWriter(home.path_to_python + "/openface-master/aligned-images/" + assign + "/index.txt");
                    pout = new PrintWriter(fout);
                }
                Thread.sleep(10);
                if (fromClient.equals("saving image")) {
                    fromClient = in.readLine();
                    pout.println(fromClient);
                }
                System.out.println("received: " + fromClient);
                jta.append(fromClient + "\n\n");
                jta.setCaretPosition(jta.getText().length());
                if (fromClient.equals("Bye")) {
                    client.close();
                    System.out.println("socket closed");
                }
            }
            server.close();
            pout.flush();
            pout.close();
            fout.close();
            while (p.isAlive());
            if (!p.isAlive() && p.exitValue() == 1) {
                while ((s1 = br.readLine()) != null) {
                    s = s + s1 + "\n";
                }
                //JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);

                System.out.println("returned with exit status 1");
                System.out.println(s);
                home.showMessage(s.toString());
                System.exit(1);
                return;
            }
            System.out.println("out of socket loop");
            f2.dispose();
            /*Process to perform Data Augmnetation*//*
            pb = new ProcessBuilder(home.path_to_python+"/venv/bin/python3", 
            home.path_to_python+"/data_augment.py",home.path_to_python+"/openface-master/aligned-images/"+assign,"outerEyesAndNose");*/
            String path = home.path_to_python + "/openface-master/aligned-images/" + assign;
            File diFile = new File(path);
            String[] EXTENSIONS = new String[]{
                "gif", "png", "bmp", "jpeg", "jpg" // and other formats you need
            };/*
            FilenameFilter IMAGE_FILTER1 = new FilenameFilter() {
                public boolean accept(final File diFile, final String name) {
                    for (final String ext : EXTENSIONS) {
                        if (name.endsWith("." + ext)) {
                            return (true);
                        }
                    }
                    return (false);
                }
            };
            System.out.println("diFile.list(IMAGE_FILTER1).length  " + diFile.list(IMAGE_FILTER1).length*6);
            server = new ServerSocket(8080);
            System.out.println("wait for connection on port 8080");
            p = pb.start();
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println("reached here1");
           s="";
           if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    return;
                }
           client = server.accept();
            System.out.println("got connection on port 8080");
            if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    return;
                }
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
               JDialog augFrame = new JDialog(xyz,false);
                augFrame.setLayout(new FlowLayout());
                JLabel label = new JLabel("Augmenting Data");
                label.setForeground(Color.white);
                augFrame.add(label);
                JProgressBar jpb1 = new JProgressBar(1, diFile.list(IMAGE_FILTER1).length*6);
                jpb1.setStringPainted(true);
                augFrame.getContentPane().setBackground(Color.black);
                augFrame.add(jpb1);
                augFrame.setSize(150, 45);
                augFrame.setUndecorated(true);
                augFrame.setLocationRelativeTo(null);
                augFrame.setVisible(true);
                 while (!client.isClosed()) {
                fromClient = in.readLine();
                if (fromClient == null) {
                    continue;
                }
                Thread.sleep(100);
                System.out.println("received: " + fromClient);
                if(fromClient.equals("two saved")){
                    jpb1.setValue(jpb1.getValue()+2);
                }
                else if (fromClient.equals("Bye")) {
                    client.close();
                    System.out.println("socket closed");
                }
            }
            server.close();
            augFrame.dispose();
            while(p.isAlive());
            if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    return;
                }
            System.out.println("out of socket loop");*/

 /*Next step is to load iamges into the panel*/
            panel.setLayout(new BorderLayout());
            JPanel p1 = new JPanel(new GridLayout(0, 5, 5, 5));
            final JScrollPane jsp = new JScrollPane(p1);
            jsp.setPreferredSize(new Dimension(500, 500));
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            File dir = new File(path);
            FilenameFilter IMAGE_FILTER = new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    for (final String ext : EXTENSIONS) {
                        if (name.endsWith("." + ext)) {
                            return (true);
                        }
                    }
                    return (false);
                }
            };
            System.out.println("dir.list(IMAGE_FILTER).length  " + dir.list(IMAGE_FILTER).length);
            if (dir.isDirectory()) { // make sure it's a directory
                JDialog obj = new JDialog(xyz, false);
                obj.setLayout(new FlowLayout());
                JLabel label1 = new JLabel("Loading Images");
                label1.setForeground(Color.white);
                obj.add(label1);
                JProgressBar jpb = new JProgressBar(1, dir.list(IMAGE_FILTER).length);
                jpb.setStringPainted(true);
                obj.getContentPane().setBackground(Color.black);
                obj.add(jpb);
                jpb.setSize(300, 20);
                obj.setSize(150, 45);
                obj.setUndecorated(true);
                obj.setLocationRelativeTo(null);
                System.out.println("reached here 3");
                obj.setVisible(true);
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int i = 1;
                        for (File f : dir.listFiles(IMAGE_FILTER)) {
                            BufferedImage img = null;
                            try {
                                publish(i);
                                i++;
                                img = ImageIO.read(f);
                                JLabel xyz = new JLabel();
                                xyz.setSize(100, 100);
                                xyz.setIcon(new ImageIcon(img.getScaledInstance(xyz.getWidth(), xyz.getHeight(),
                                        Image.SCALE_SMOOTH)));
                                xyz.setText(f.getName());
                                xyz.setOpaque(true);
                                p1.add(xyz);
                                Thread.sleep(100);
                            } catch (final IOException e) {
                                System.out.println("error not last");
                                JOptionPane.showMessageDialog(null, e.getMessage());
                                StringWriter error = new StringWriter();
                                e.printStackTrace(new PrintWriter(error));
                                home.showMessage(error.toString());
                                System.exit(1);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void process(List<Integer> chunks) {
                        for (Integer chunk : chunks) {
                            jpb.setValue(chunk);
                        }
                    }

                    @Override
                    protected void done() {
                        panel.add(jsp, BorderLayout.CENTER);
                        xyz.text.setText("" + assign);
                        f1.setVisible(true);
                        obj.dispose();
                        buttonGroup1.clearSelection();
                        buttonGroup2.clearSelection();
                        xyz.dispose();
                    }
                };
                worker.execute();
            }
        } catch (Exception e) {
            System.out.println(e);
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }
    }
}
