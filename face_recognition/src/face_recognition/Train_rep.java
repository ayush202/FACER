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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 *
 * @author ayush
 */
public class Train_rep implements Runnable {

    int assign;
    train xyz;
    home xyz1;

    public Train_rep(int x, train obj, home xy) {
        assign = x;
        xyz = obj;
        xyz1 = xy;
    }

    @Override
    public void run() {
        try {
            String fromClient = "";
            ServerSocket server = new ServerSocket(8080);
            System.out.println("wait for connection on port 8080");
            ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                    home.path_to_python + "/pytorch_repo/test.py",
                    home.path_to_python + "/openface-master/aligned-images/" + assign,
                    home.path_to_python + "/openface-master/"
                    + "generated-embeddings", "--name", "" + assign);
            JDialog jdi = new JDialog(xyz.f1, false);
            JDialog jdi1 = new JDialog(xyz.f1, false);
            jdi.setLayout(new FlowLayout());
            jdi1.setLayout(new FlowLayout());
            jdi.setUndecorated(true);
            jdi1.setUndecorated(true);
            JLabel label = new JLabel("Creating Representations");
            label.setForeground(Color.WHITE);
            jdi.getContentPane().setBackground(Color.black);
            jdi.add(label);
            jdi.setLocationRelativeTo(null);
            jdi.setSize(200, 45);
            JLabel label1 = new JLabel("Starting...");
            label1.setForeground(Color.WHITE);
            jdi1.getContentPane().setBackground(Color.black);
            jdi1.add(label1);
            jdi1.setLocationRelativeTo(null);
            jdi1.setSize(150, 45);
            Process p = pb.inheritIO().start();
            Scanner sc = new Scanner(p.getErrorStream());
            BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println("reached here1");
            StringBuilder s = new StringBuilder();
            String s1;
            if (!p.isAlive() && p.exitValue() == 1) {
                s.delete(0, s.length());
                while (sc.hasNextLine()) {
                    s.append(sc.nextLine());
                }
                //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                System.out.println("returned with exit status 1");
                home.showMessage(s.toString());
                System.exit(1);
            }
            JProgressBar jp = new JProgressBar(1, 200);
            jp.setSize(300, 5);
            jp.setIndeterminate(true);
            jdi1.add(jp);
            jdi1.setVisible(true);
            Socket client = server.accept();
            System.out.println("got connection on port 8080");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            int len;
            JProgressBar jpb = new JProgressBar();
            while (!client.isClosed() && p.isAlive()) {
                fromClient = in.readLine();
                if (fromClient == null) {
                    continue;
                } else if (fromClient.equals("started")) {
                    System.out.println("received " + fromClient);
                    len = Integer.parseInt(in.readLine());
                    jpb = new JProgressBar(1, len);
                    jpb.setSize(300, 5);
                    jpb.setStringPainted(true);
                    jdi.add(jpb);
                    jdi.setSize(200, 45);
                    jdi1.dispose();
                    jdi.setVisible(true);
                } else if (fromClient.equals("added")) {
                    System.out.println("received " + fromClient);
                    jpb.setValue(jpb.getValue() + 1);
                    Thread.sleep(0, 10);
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
                    home.showMessage(s.toString());
                    System.exit(1);
                }
            }
            while (p.isAlive());
            in.close();
            sc.close();
            br1.close();
            System.out.println(p.isAlive() + " " + p.exitValue());
            server.close();
            while (!server.isClosed());
            jdi.dispose();
            if (xyz.map_size >= 2) {
                pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/openface-master/demos/classifier.py", "--verbose", "train",
                        home.path_to_python + "/openface-master"
                        + "/generated-embeddings");
                label.setText("Creating Classifier");
                jpb = new JProgressBar(1, 100);
                jpb.setSize(300, 5);
                jdi = new JDialog(xyz.f1, false);
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
                p = pb.start();
                sc = new Scanner(p.getErrorStream());
                br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                System.out.println("reached here1");
                s.delete(0, s.length());
                if (!p.isAlive() && p.exitValue() == 1) {
                    while (sc.hasNextLine()) {
                        s.append(sc.nextLine());
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    System.out.println(s);
                    home.showMessage(s.toString());
                    jdi.dispose();
                    System.exit(1);
                }
                while ((s1 = br1.readLine()) != null) {
                    System.out.println(s1);
                }
                System.out.println("outside while loop");
                s.delete(0, s.length());
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
                sc.close();
                br1.close();
                while (p.isAlive());

            } else {
                JOptionPane.showMessageDialog(null, "Train one more person to start recognition", "Facer", JOptionPane.INFORMATION_MESSAGE);
            }
            xyz.setEnabled(true);
            xyz.r1.setVisible(false);
            xyz.r2.setVisible(false);
            xyz.button1.setVisible(false);
            xyz.f1.setEnabled(true);
            xyz.f1.dispose();
            xyz.panel.removeAll();
            FileWriter fw;
            boolean check = new File(home.path_map).exists();
            String name1 = xyz.name1;
            try {
                fw = new FileWriter(home.path_map, true);
                if (check) {
                    fw.write("\n" + assign + " " + name1);
                } else {
                    fw.write(1 + " " + name1);
                }
                fw.close();
                xyz1.Sort_Embed();
                xyz1.Sort_map();
                xyz1.create_map_index();
                jdi.dispose();
                xyz.setVisible(true);
            } catch (IOException e) {
                StringWriter error = new StringWriter();
                e.printStackTrace(new PrintWriter(error));
                home.showMessage(error.toString());
                System.exit(1);
            }
        } catch (Exception e) {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }

    }
}
