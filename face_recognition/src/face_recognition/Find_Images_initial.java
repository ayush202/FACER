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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

/**
 *
 * @author ayush
 */
public class Find_Images_initial implements Runnable {

    home xyz;

    public Find_Images_initial(home xy) {
        xyz = xy;
    }

    @Override
    public void run() {
        try {
            boolean check = new File(home.path_map_index).exists();
            if (!check) {
                JOptionPane.showMessageDialog(null, "There are no trained persons", "Facer", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(home.path_map));
            String s, s1[];
            ArrayList<String> ar = new ArrayList<>();
            String path = home.path_to_python + "/openface-master/aligned-images/";
            while ((s = br.readLine()) != null) {
                s1 = s.split(" ");
                if (s1[0].equals("1")) {
                    continue;
                }
                ar.add(s1[0]);
            }
            File files[] = new File[ar.size()];
            int i = 0;
            Random rand = new Random();
            ArrayList<String> ar1 = new ArrayList<>();
            for (String string : ar) {
                br = new BufferedReader(new FileReader(path + string + '/' + "index.txt"));
                ar1.clear();
                while ((s = br.readLine()) != null) {
                    ar1.add(s);
                }
                int y = 0;
                while (y == 0) {
                    y = rand.nextInt(ar1.size());
                }
                files[i] = new File(ar1.get(y - 1));
                System.out.println(files[i]);
                i++;

            }

            Find_Images frame = new Find_Images();
            frame.panel.setLayout(new BorderLayout());
            JPanel p = new JPanel(new GridLayout(0, 5, 5, 5));
            JScrollPane jsp = new JScrollPane(p);
            jsp.setPreferredSize(new Dimension(500, 500));
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            JDialog obj2 = new JDialog(xyz, false);
            obj2.setLayout(new FlowLayout());
            JProgressBar jpb = new JProgressBar(1, files.length);
            jpb.setStringPainted(true);
            obj2.getContentPane().setBackground(Color.black);
            JLabel lb = new JLabel("Loading Images");
            lb.setForeground(Color.WHITE);
            obj2.add(lb);
            obj2.add(jpb);
            jpb.setSize(300, 3);
            obj2.setSize(150, 45);
            obj2.setUndecorated(true);
            obj2.setLocationRelativeTo(null);
            System.out.println("reached here 3");
            obj2.setVisible(true);
            System.out.println("before swingworker");
            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int i = 1;
                    for (File f : files) {
                        BufferedImage img = null;
                        try {
                            publish(i);
                            Thread.sleep(10);
                            img = ImageIO.read(f);
                            JLabel xyz1 = new JLabel();
                            xyz1.setSize(100, 100);
                            xyz1.setIcon(new ImageIcon(img.getScaledInstance(xyz1.getWidth(), xyz1.getHeight(),
                                    Image.SCALE_SMOOTH)));
                            xyz1.setText(ar.get(i - 1) + "     ");
                            xyz1.setOpaque(true);
                            xyz1.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mousePressed(MouseEvent e) {
                                    for (JLabel lb : home.comja) {
                                        lb.setBackground(frame.getContentPane().getBackground());
                                        p.revalidate();
                                        p.repaint();
                                        home.comint.remove(Integer.parseInt(lb.getText().trim()));
                                        home.comja.remove(lb);
                                    }
                                    JLabel lb = (JLabel) e.getSource();
                                    lb.setBackground(Color.BLUE);
                                    home.comint.add(Integer.parseInt(lb.getText().trim()));
                                    home.comja.add(lb);
                                }
                            });
                            p.add(xyz1);
                            i++;
                        } catch (final IOException e) {
                            System.out.println(i + " " + files[i]);
                            System.out.println(e);
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
                    frame.panel.add(jsp, BorderLayout.CENTER);
                    frame.setVisible(true);
                    xyz.dispose();
                    obj2.dispose();
                }

            };
            worker.execute();
        } catch (Exception e) {
            System.out.println("error last");
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }
    }

}
