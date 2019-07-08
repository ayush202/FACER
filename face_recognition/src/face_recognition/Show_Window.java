/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author ayush
 */
public class Show_Window{
    JDialog obj;
    JProgressBar jpb;
    public Show_Window(JFrame owner,boolean modal,int width,int height)
    {
        obj = new JDialog(owner, modal);
        obj.setLayout(new FlowLayout());
        obj.setUndecorated(true);
        obj.getContentPane().setBackground(Color.BLACK);
        obj.setSize(width,height);
        obj.setLocationRelativeTo(null);
    }
    public void addLabel(String value){
        JLabel lb = new JLabel(value);
        lb.setForeground(Color.WHITE);
        obj.add(lb);
    }
    public void addProgressBar(int length){
        jpb = new JProgressBar(0, length); 
        jpb.setStringPainted(true);
        jpb.setSize(300, 3);
        obj.add(jpb);
    }
    public void addProgressBar_Indeterminate(int length){
        jpb = new JProgressBar(0, length); 
        jpb.setIndeterminate(true);
        jpb.setSize(300, 3);
        obj.add(jpb);
    }
    public void setVisible(boolean val){
        obj.setVisible(val);
    }
    public void closeWindow()
    {
        obj.dispose();
    }
    public void setValue(){
        jpb.setValue(jpb.getValue()+1);
    }
}
