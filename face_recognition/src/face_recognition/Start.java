/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author ayush
 */
public class Start implements Runnable {

    @Override
    public void run() {
        try {
            boolean check = new File(home.path_map).exists();
            if (!check) {
                int assign = 1;
                String path = home.path_to_python + "/openface-master/training-images/unknown";
                String fromClient = "";
                ServerSocket server = new ServerSocket(8080);
                System.out.println("wait for connection on port 8080");
                ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/openface-master" + "/util/align-dlib.py",
                        path, "--name", "" + assign, "align", "outerEyesAndNose", "--outDir",
                        home.path_to_python + "/openface-master/aligned-images", "--verbose");
                Process p = pb.inheritIO().start();
                Scanner sc = new Scanner(p.getErrorStream());
                BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder s = new StringBuilder();
                String s1, s2;
                if (!p.isAlive() && p.exitValue() == 1) {
                    while (sc.hasNextLine()) {
                        s1 = sc.nextLine();
                        s.append(s1 + "\n");
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    home.showMessage(s.toString());
                    System.exit(1);
                    return;
                }
                System.out.println("reached here1");
                s.delete(0, s.length());
                System.out.println(s);
                Socket client = server.accept();
                System.out.println("got connection on port 8080");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (!client.isClosed()) {
                    fromClient = in.readLine();
                    if (fromClient == null) {
                        continue;
                    }
                    System.out.println("received " + fromClient);
                    if (fromClient.equals("Bye")) {
                        client.close();
                    }
                    if (!p.isAlive() && p.exitValue() == 1) {
                        s.delete(0, s.length());
                        while (sc.hasNextLine()) {
                            s1 = sc.nextLine();
                            s.append(s1 + "\n");
                        }
                        //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                        System.out.println("returned with exit status 1");
                        home.showMessage(s.toString());
                        System.exit(1);
                        //return;
                    }
                }
                while (p.isAlive()) {
                    System.out.println("going on");
                }
                if (!p.isAlive() && p.exitValue() == 1) {
                    s.delete(0, s.length());
                    while (sc.hasNextLine()) {
                        s1 = sc.nextLine();
                        s.append(s1 + "\n");
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    home.showMessage(s.toString());
                    System.exit(1);
                }
                System.out.println("out of socket loop");
                System.out.println("wait for connection on port 8080");
                pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/pytorch_repo/test.py",
                        home.path_to_python + "/openface-master/aligned-images/" + assign,
                        home.path_to_python + "/openface-master/"
                        + "generated-embeddings", "--name", "" + assign);

                p = pb.inheritIO().start();
                sc = new Scanner(p.getErrorStream());
                br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                System.out.println("reached here1");
                s = new StringBuilder();
                if (!p.isAlive() && p.exitValue() == 1) {
                    s.delete(0, s.length());
                    while (sc.hasNextLine()) {
                        s1 = sc.nextLine();
                        s.append(s1 + "\n");
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    home.showMessage(s.toString());
                    System.exit(1);
                }
                client = server.accept();
                System.out.println("got connection on port 8080");
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                int len;
                while (!client.isClosed()) {
                    fromClient = in.readLine();
                    Thread.sleep(0, 10);
                    if (fromClient == null) {
                        continue;
                    } else if (fromClient.equals("started")) {
                        System.out.println("received " + fromClient);
                        len = Integer.parseInt(in.readLine());
                    } else if (fromClient.equals("added")) {
                        System.out.println("received " + fromClient);
                    } else if (fromClient.equals("Bye")) {
                        client.close();
                        System.out.println("socket closed");
                    }
                    if (!p.isAlive() && p.exitValue() == 1) {
                        s.delete(0, s.length());
                        while (sc.hasNextLine()) {
                            s1 = sc.nextLine();
                            s.append(s1 + "\n");
                        }
                        //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                        System.out.println("returned with exit status 1");
                        home.showMessage(s.toString());
                        System.exit(1);
                    }
                }
                while (p.isAlive()) {
                    System.out.println("going on");
                }
                System.out.println(p.isAlive() + " " + p.exitValue());
                server.close();
                FileWriter fw = new FileWriter(home.path_map);
                fw.write(1 + " " + "unknown1");
                fw.close();
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
