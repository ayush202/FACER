/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.awt.Color;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 *
 * @author ayush
 */
public class Remove_Persons implements Runnable {

    RemovePersons xyz;
    home obj;

    public Remove_Persons(RemovePersons xy, home abc) {
        xyz = xy;
        obj = abc;
    }

    static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    @Override
    public void run() {
        try {
            System.out.println("inside Remove_Persons");
            if (home.comint.size() == 0) {
                JOptionPane.showMessageDialog(null, "Select any person", "Facer", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JScrollPane pane = (JScrollPane) xyz.panel.getComponent(0);
            JViewport viewport = pane.getViewport();
            JPanel panel = (JPanel) viewport.getView();
            Iterator<Integer> iter = home.comint.iterator();
            Iterator<JLabel> iter1 = home.comja.iterator();
            int map_size = 1;
            while (iter.hasNext()) {
                map_size = 0;
                int element = iter.next();
                JLabel component = iter1.next();
                //if ((component instanceof JLabel)
                //& ((JLabel) component).getBackground() == Color.blue) {
                System.out.println("if statement");
                //String s = ((JLabel) component).getText().trim(), s1, s2[];
                String s, s1, s2[];
                int val = element;
                String path = home.path_to_python + "/openface-master/generated-embeddings/";
                String path1 = home.path_to_python + "/openface-master/aligned-images/" + val;
                BufferedReader br = new BufferedReader(new FileReader(path + "labels.csv"));
                BufferedReader br1 = new BufferedReader(new FileReader(path + "reps.csv"));
                File tmp = File.createTempFile("tmp", "csv");
                File tmp1 = File.createTempFile("tmp1", "csv");
                PrintWriter pw = new PrintWriter(new FileWriter(tmp));
                PrintWriter pw1 = new PrintWriter(new FileWriter(tmp1));
                boolean start = true;
                while ((s = br.readLine()) != null) {
                    s1 = br1.readLine();
                    s2 = s.split(",");
                    if (!s2[0].equals("" + val)) {
                        pw.println(s);
                        pw1.println(s1);
                        start = false;
                    }
                }
                br.close();
                br1.close();
                pw.close();
                pw1.close();
                File f = new File(path + "labels.csv");
                if (f.delete() && !start) {
                    obj.moveFile(tmp, f);
                }
                f = new File(path + "reps.csv");
                if (f.delete() && !start) {
                    obj.moveFile(tmp1, f);
                }

                deleteFolder(new File(path1));

                br = new BufferedReader(new FileReader(home.path_map));
                tmp = File.createTempFile("tmp", "facer");
                pw = new PrintWriter(new FileWriter(tmp));
                start = true;
                while ((s = br.readLine()) != null) {
                    s2 = s.split(" ");
                    if (!s2[0].equals("" + val)) {
                        if (start) {
                            start = false;
                            pw.print(s);
                        } else {
                            pw.print("\n" + s);
                        }
                        map_size++;
                    }
                }
                br.close();
                pw.close();
                f = new File(home.path_map);
                f.delete();
                if (!start) {
                    tmp.renameTo(f);
                } else {
                    deleteFolder(new File(path));
                }
                panel.remove(component);
                panel.revalidate();
                panel.repaint();
            }
            home.comint.clear();
            home.comja.clear();
            obj.Sort_Embed();
            obj.Sort_map();
            obj.create_map_index();
            System.out.println("map_size  " + map_size);
            if (map_size >= 2) {
                ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/openface-master/demos/classifier.py", "--verbose", "train",
                        home.path_to_python + "/openface-master" + "/generated-embeddings");
                JLabel label = new JLabel("Creating Classifier");
                label.setForeground(Color.white);
                JProgressBar jpb = new JProgressBar(1, 100);
                jpb.setSize(300, 5);
                JDialog jdi = new JDialog(xyz, false);
                jdi.setLayout(new FlowLayout());
                jdi.getContentPane().setBackground(Color.black);
                jdi.add(label);
                jpb.setIndeterminate(true);
                jdi.setUndecorated(true);
                jpb.setSize(200, 5);
                jdi.add(jpb);
                jdi.setLocationRelativeTo(null);
                jdi.setSize(200, 45);
                jdi.setVisible(true);
                Thread.sleep(10);
                Process p = pb.start();
                String fromClient;
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
                    home.showMessage(s);
                    System.exit(1);
                }
                while ((s = br1.readLine()) != null) {
                    System.out.println(s);
                }
                System.out.println("outside while loop");
                if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    home.showMessage(s);
                    System.exit(1);
                    return;
                }
                jdi.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Train one more person to start recognition", "Facer", JOptionPane.INFORMATION_MESSAGE);
                xyz.dispose();
                obj.f1.setVisible(true);
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
