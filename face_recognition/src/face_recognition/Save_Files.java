/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author ayush
 */
public class Save_Files implements Runnable {

    Find_Images xyz;

    public Save_Files(Find_Images xyz) {
        this.xyz = xyz;
    }

    @Override
    public void run() {
        try {
            JFileChooser filechoose = new JFileChooser();
            filechoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (filechoose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                Show_Window obj = new Show_Window(xyz.f1, false, 200, 45);
                obj.addLabel("Saving Images");
                obj.addProgressBar(xyz.len);
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        BufferedReader br = new BufferedReader(new FileReader("tmp.csv"));
                        String s = "", s1;
                        int t;
                        obj.setVisible(true);
                        String path = filechoose.getSelectedFile().getAbsolutePath();
                        int i = 1;
                        while ((s = br.readLine()) != null) {
                            s = s.split(",")[0];
                            t = s.lastIndexOf("/");
                            s1 = s.substring(t);
                            Files.copy(Paths.get(s), Paths.get(path + "/" + s1), StandardCopyOption.REPLACE_EXISTING);
                            publish(i);
                            i++;
                        }
                        return null;
                    }

                    protected void process(List<Integer> chunks) {
                        for (Integer chunk : chunks) {
                            obj.setValue();
                        }
                    }

                    protected void done() {
                        obj.setVisible(false);
                        JOptionPane.showMessageDialog(null, "All images has been saved", "Facer", JOptionPane.INFORMATION_MESSAGE);
                    }
                };
                worker.execute();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Facer", JOptionPane.ERROR_MESSAGE);
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }
    }

}
