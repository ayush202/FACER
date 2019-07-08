/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import static face_recognition.home.path_to_python;
import static face_recognition.home.showMessage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author ayush
 */
public class temp{
    public static void main(String[] args) throws IOException, InterruptedException {
        /*ProcessBuilder pb = new ProcessBuilder("/home/ayush/PycharmProjects/face_recognition/venv/bin/python","/home/ayush/PycharmProjects/face_recognition/openface-master/demos/classifier_webcam.py");
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="",s1;
        while(p.isAlive()){
            while ((s = br1.readLine()) != null) {
                    System.out.println(s);
                }
        }
        if (!p.isAlive()&&p.exitValue()==1) {
            while((s1 = br.readLine())!=null)
                s = s+s1+"\n";
            throw new IOException(s);
        }*/
        try (BufferedReader br = new BufferedReader(new FileReader(new File("PATH_to_Python")))) {
            home.path_to_python = br.readLine();
        } catch (IOException ex) {
            showMessage(ex.toString());
        }
        home.Sort_Embed();
    }
}
