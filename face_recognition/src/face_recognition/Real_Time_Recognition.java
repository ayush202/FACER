/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

/**
 *
 * @author ayush
 */
public class Real_Time_Recognition implements Runnable {

    int val;
    Real_time xyz;

    public Real_Time_Recognition(int x, Real_time xy) {
        val = x;
        xyz = xy;
    }

    @Override
    public void run() {
        try {
            ArrayList<String> ar1 = new ArrayList<>();
            BufferedReader brt = new BufferedReader(new FileReader(home.path_map));
            String string;
            while ((string = brt.readLine()) != null) {
                ar1.add(string);
            }
            if (ar1.size() < 2) {
                JOptionPane.showMessageDialog(null, "Train a person first", "FACER", JOptionPane.ERROR_MESSAGE);
            } else {
                xyz.setVisible(false);
                ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/openface-master"
                        + "/demos/classifier_webcam.py", "--captureDevice", "" + val,
                        "--width", "500", "--height", "500", "--verbose", home.path_to_python + "/openface-master/"
                        + "generated-embeddings/classifier.pkl");
                Process p = pb.inheritIO().start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()), 10);
                String s1 = "", s;
                while (p.isAlive()) {
                    p.waitFor(1, TimeUnit.NANOSECONDS);
                    while ((s = br.readLine()) != null) {
                        s1 += s;
                        System.out.println("s " + s);
                        System.out.println("inside thisdewj");
                    }
                    if(s1.length()!=0){
                     home.showMessage(s1);
                    }
                    while ((s = br1.readLine()) != null) {
                        s1 += s;
                        System.out.println("s " + s);
                        if ((s = br1.readLine()) != null) {
                            System.out.println("output1 " + s);
                        }
                    }
                    Thread.sleep(0, 1);
                }
                int y = p.exitValue();
                System.out.println("y value "+y);
                s1 = "";
                while ((s = br.readLine()) != null) {
                    s1 += s;
                    System.out.println("s " + s);
                }
                if(s1.length()!=0)
                     home.showMessage(s1);
                System.out.println("value of s1 "+s1);

                /*while((s=br1.readLine())!=null)
                {
                    s1+=s;
                    System.out.println("s "+s);
                    if((s=br1.readLine())!=null)
                        System.out.println("output "+s);
                }
                System.out.println(s);*/
                xyz.setVisible(true);
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
