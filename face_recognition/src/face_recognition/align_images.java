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
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

/**
 *
 * @author ayush
 */
public class align_images implements Runnable {

    JFileChooser filechoose;
    train xyz;
    JFrame f1;
    JDialog f2;
    JTextArea jta;
    JPanel panel;
    ButtonGroup buttonGroup1, buttonGroup2;
    int assign;
    boolean check;
    String name1;

    public align_images(JFileChooser x, train xy, int m, boolean n, String s) {
        filechoose = x;
        xyz = xy;
        f1 = xyz.f1;
        f2 = xyz.f2;
        jta = xyz.jta;
        panel = xyz.panel;
        buttonGroup1 = xyz.buttonGroup1;
        buttonGroup2 = xyz.buttonGroup2;
        assign = m;
        check = n;
        name1 = s;
    }

    @Override
    public void run() {
        try {
            filechoose.setFileSelectionMode(filechoose.DIRECTORIES_ONLY);
            int val = filechoose.showOpenDialog(null);
            if (val == filechoose.APPROVE_OPTION) {
                String path = filechoose.getSelectedFile().getAbsolutePath();
                if (path.length() == 0) {
                    return;
                }
                xyz.setEnabled(false);
                xyz.label1.setVisible(false);
                xyz.button1.setVisible(false);
                String fromClient = "";
                ServerSocket server = new ServerSocket(8080);
                System.out.println("wait for connection on port 8080");
                ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                         home.path_to_python + "/openface-master"
                        + "/util/align-dlib.py", path, "--name", "" + assign, "align",
                         "outerEyesAndNose", "--outDir", home.path_to_python + "/openface-master/aligned-images", "--verbose");

                JDialog jdi1 = new JDialog(xyz, false);
                jdi1.setLayout(new FlowLayout());
                JLabel label1 = new JLabel("Starting...");
                label1.setForeground(Color.WHITE);
                jdi1.getContentPane().setBackground(Color.black);
                jdi1.add(label1);
                jdi1.setLocationRelativeTo(null);
                jdi1.setSize(200, 50);
                jdi1.setUndecorated(true);
                JProgressBar jp = new JProgressBar();
                jp.setIndeterminate(true);
                jp.setSize(200, 3);
                jdi1.add(jp);
                jdi1.setVisible(true);
                f2.setUndecorated(true);
                f2.setLocation(100, 200);
                f2.pack();
                jta.setText("");
                jta.setBackground(Color.orange);
                JDialog obj1 = new JDialog(xyz, false);
                obj1.setLayout(new FlowLayout());
                obj1.getContentPane().setBackground(Color.black);
                JLabel lb = new JLabel("Aligning Images");
                lb.setForeground(Color.white);
                obj1.add(lb);
                obj1.setSize(150, 45);
                obj1.setUndecorated(true);
                obj1.setLocationRelativeTo(null);
                System.out.println("reached here 3");
                Process p = pb.inheritIO().start();
                Scanner sc = new Scanner(p.getErrorStream());
                BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder s = new StringBuilder();
                String s1;
                if (!p.isAlive() && p.exitValue() == 1) {
                    while (sc.hasNextLine()) {
                        s.append(sc.nextLine());
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    home.showMessage(s.toString());
                    System.exit(1);
                    return;
                }
                System.out.println("reached here1");

                Socket client = server.accept();
                System.out.println("got connection on port 8080");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                f2.setVisible(true);
                File file = null;
                FileWriter fout = null;
                PrintWriter pout = null;
                JProgressBar jpb1 = new JProgressBar();
                int i = 0, len = -1;
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
                    System.out.println("received: " + fromClient);
                    jta.append(fromClient + "\n\n");
                    jta.setCaretPosition(jta.getText().length());
                    if (fromClient.equals("processed")) {
                        i++;
                        //int x = (i*100)/len; 
                        jpb1.setValue(i);
                        Thread.sleep(0, 10);
                    } else if (fromClient.equals("write to index")) {
                        String x = in.readLine();
                        System.out.println("received : " + x);
                        pout.println(x);
                    } else if (fromClient.equals("length")) {
                        len = Integer.parseInt(in.readLine());
                        jpb1 = new JProgressBar(1, len);
                        jpb1.setStringPainted(true);
                        obj1.add(jpb1);
                        jdi1.dispose();
                        obj1.setVisible(true);
                        System.out.println("length " + len);
                    } else if (fromClient.equals("Bye")) {
                        client.close();
                        System.out.println("socket closed");
                    }
                    if (!p.isAlive() && p.exitValue() == 1) {
                        while (sc.hasNextLine()) {
                            s.append(sc.nextLine());
                        }
                        //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                        System.out.println("returned with exit status 1");
                        System.out.println(s);
                        home.showMessage(s.toString());
                        System.exit(1);
                    }
                }
                obj1.dispose();
                server.close();
                pout.flush();
                pout.close();
                fout.close();
                System.out.println("i cnt " + i);
                while (p.isAlive()) {
                    System.out.println("going on");
                }
                if (!p.isAlive() && p.exitValue() == 1) {
                    while (sc.hasNextLine()) {
                        s.append(sc.nextLine());
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    home.showMessage(s.toString());
                    System.exit(1);
                    //return;
                }
                System.out.println("out of socket loop");
                f2.dispose();

                /*Process to perform Data Augmnetation*/
 /*
            pb = new ProcessBuilder(home.path_to_python+"/venv/bin/python", 
            home.path_to_python+"/data_augment.py",home.path_to_python+"/openface-master/aligned-images/"+assign,"outerEyesAndNose");
            path = home.path_to_python+"/openface-master/aligned-images/" + assign;
            File diFile = new File(path);*/
                String[] EXTENSIONS = new String[]{
                    "gif", "png", "bmp", "jpeg", "jpg" // and other formats you need
                };/*
            FilenameFilter IMAGE_FILTER2 = new FilenameFilter() {
                public boolean accept(final File diFile, final String name) {
                    for (final String ext : EXTENSIONS) {
                        if (name.endsWith("." + ext)) {
                            return (true);
                        }
                    }
                    return (false);
                }
            };
            System.out.println("diFile.list(IMAGE_FILTER2).length  " + diFile.list(IMAGE_FILTER2).length*6);
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
                augFrame.getContentPane().setBackground(Color.BLACK);
                JLabel label = new JLabel("Augmenting Data");
                label.setForeground(Color.white);
                augFrame.add(label);
                JProgressBar jpb2 = new JProgressBar(1, diFile.list(IMAGE_FILTER2).length*6);
                jpb2.setStringPainted(true);
                augFrame.getContentPane().setBackground(Color.black);
                augFrame.add(jpb2);
                augFrame.setSize(150, 45);
                augFrame.setUndecorated(true);
                augFrame.setLocationRelativeTo(null);
                augFrame.setVisible(true);
                 while (!client.isClosed()) {
                fromClient = in.readLine();
                if (fromClient == null) {
                    continue;
                }
                Thread.sleep(10);
                System.out.println("received: " + fromClient);
                jta.append(fromClient+"\n\n");
                jta.setCaretPosition(jta.getText().length());
                if(fromClient.equals("two saved")){
                    jpb2.setValue(jpb2.getValue()+2);
                }
                if (fromClient.equals("Bye")) {
                    client.close();
                    System.out.println("socket closed");
                }
            }
            server.close();
            augFrame.dispose();
            while(p.isAlive());
            s="";
            if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    return;
                }
            System.out.println("out of socket loop");
            
                 */
 /*Next step is to load iamges into the panel*/
                panel.setLayout(new BorderLayout());
                JPanel p1 = new JPanel(new GridLayout(0, 5, 5, 5));
                final JScrollPane jsp = new JScrollPane(p1);
                jsp.setPreferredSize(new Dimension(500, 500));
                jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                path = home.path_to_python + "/openface-master" + "/aligned-images/" + assign;
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
                    JDialog obj2 = new JDialog(xyz, false);
                    obj2.setLayout(new FlowLayout());
                    JProgressBar jpb = new JProgressBar(1, dir.list(IMAGE_FILTER).length);
                    jpb.setStringPainted(true);
                    obj2.getContentPane().setBackground(Color.black);
                    lb = new JLabel("Loading Images");
                    lb.setForeground(Color.WHITE);
                    obj2.add(lb);
                    obj2.add(jpb);
                    jpb.setSize(300, 3);
                    obj2.setSize(150, 45);
                    obj2.setUndecorated(true);
                    obj2.setLocationRelativeTo(null);
                    System.out.println("reached here 3");
                    obj2.setVisible(true);
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
                                } catch (final IOException e) {
                                    System.out.println("error not last");
                                    JOptionPane.showMessageDialog(null, e.getMessage());
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
                            obj2.dispose();
                            buttonGroup1.clearSelection();
                            buttonGroup2.clearSelection();
                            xyz.dispose();
                        }
                    };
                    worker.execute();
                }
            }

        } catch (Exception e) {
            System.out.println("error last");
            f2.dispose();
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }
    }
}
