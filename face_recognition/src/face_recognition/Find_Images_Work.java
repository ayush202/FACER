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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author ayush
 */
public class Find_Images_Work implements Runnable {

    JFileChooser filechoose;
    Find_Images xyz;
    JPanel panel;

    Find_Images_Work(JFileChooser fi, Find_Images xy) {
        filechoose = fi;
        xyz = xy;
        panel = xy.panel1;
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
                String fromClient = "";
                ServerSocket server = new ServerSocket(8080);
                ProcessBuilder pb = new ProcessBuilder(home.path_to_python + "/venv/bin/python3",
                        home.path_to_python + "/openface-master"
                        + "/demos/modified_classifier.py", "--find", ""
                        + home.comint.iterator().next(), "--path", path, "infer",
                        home.path_to_python + "/openface-master"
                        + "/generated-embeddings/classifier.pkl", "xyz", "--multi");
                JDialog jdi = new JDialog(xyz, false);
                JDialog jdi1 = new JDialog(xyz, false);
                jdi.setLayout(new FlowLayout());
                jdi1.setLayout(new FlowLayout());
                jdi.setUndecorated(true);
                jdi1.setUndecorated(true);
                JLabel label = new JLabel("Finding Images");
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
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
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
                JProgressBar jp = new JProgressBar(1, 200);
                jp.setSize(300, 5);
                jp.setIndeterminate(true);
                jdi1.add(jp);
                jdi1.setVisible(true);
                Thread.sleep(0, 10);
                Socket client = server.accept();
                int len;
                JProgressBar jpb = new JProgressBar();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                int x = 0;
                while (!client.isClosed()) {
                    fromClient = in.readLine();
                    if (fromClient == null) {
                        continue;
                    }
                    if (fromClient.equals("length")) {
                        len = Integer.parseInt(in.readLine());
                        System.out.println("length " + len);
                        jpb = new JProgressBar(1, len);
                        jpb.setSize(300, 5);
                        jpb.setStringPainted(true);
                        jdi.add(jpb);
                        jdi.setSize(200, 45);
                        jdi1.dispose();
                        jdi.setVisible(true);
                    } else if (fromClient.equals("one completed")) {
                        jpb.setValue(jpb.getValue() + 1);
                        Thread.sleep(0, 15);
                        System.out.println("value of x " + x++);
                    } else if (fromClient.equals("Bye")) {
                        client.close();
                        System.out.println("socket closed");
                    }
                    System.out.println("received : " + fromClient);
                    Thread.sleep(0, 50);
                }
                while (p.isAlive());
                System.out.println(p.isAlive() + " " + p.exitValue());
                jdi.dispose();
                server.close();
                if (!p.isAlive() && p.exitValue() == 1) {
                    while ((s1 = br.readLine()) != null) {
                        s = s + s1 + "\n";
                    }
                    //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                    System.out.println("returned with exit status 1");
                    home.showMessage(s.toString());
                    System.exit(1);
                    //return;
                }
                while (!server.isClosed());
                br = new BufferedReader(new FileReader("tmp.csv"));
                s = "";
                ArrayList<String> ar = new ArrayList<>();
                while ((s = br.readLine()) != null) {
                    int t = -1;
                    for (int i = 0; i < s.length() - 1; i++) {
                        if (s.charAt(i) == ',' && s.charAt(i + 1) == ',') {
                            t = i;
                            break;
                        }
                    }
                    if (t == -1) {
                        ar.add(s);
                    } else {
                        ar.add(s.substring(0, t));
                    }
                }
                if (ar.size() > 0) {
                    xyz.len = ar.size();
                    panel.setLayout(new BorderLayout());
                    JPanel p1 = new JPanel(new GridLayout(0, 5, 5, 5));
                    JScrollPane jsp = new JScrollPane(p1);
                    jsp.setPreferredSize(new Dimension(500, 500));
                    jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    JDialog obj2 = new JDialog(xyz, false);
                    JProgressBar jpb1 = new JProgressBar(1, ar.size());
                    jpb1.setStringPainted(true);
                    obj2.getContentPane().setBackground(Color.black);
                    JLabel lb = new JLabel("Loading Images");
                    lb.setForeground(Color.WHITE);
                    obj2.add(lb);
                    obj2.add(jpb1);
                    jpb1.setSize(300, 3);
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
                            for (String s : ar) {
                                String s1 = s;
                                s = s.split(",")[0];
                                System.out.println(s);
                                File f = new File(s.split(",")[0]);

                                BufferedImage img = null;
                                int x = -274;
                                try {
                                    publish(i);
                                    Thread.sleep(0, 50);
                                    img = ImageIO.read(f);
                                    //System.out.println(img.getWidth() + " " + img.getHeight() + " " + f.getAbsolutePath());
                                    Metadata metadata = ImageMetadataReader.readMetadata(new File(s));
                                    ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

                                    int orientation = 1;

                                    x = ExifIFD0Directory.TAG_ORIENTATION;
                                    System.out.println("x " + x);
                                    try {
                                        orientation = exifIFD0Directory.getInt(x);
                                    } catch (Exception e) {
                                        orientation = 1;
                                    }
                                    System.out.println(s + " orientation " + orientation);
                                    int width, height;
                                    width = img.getWidth();
                                    height = img.getHeight();
                                    AffineTransform affineTransform = new AffineTransform();
                                    switch (orientation) {
                                        case 1:
                                            break;
                                        case 2: // Flip X
                                            affineTransform.scale(-1.0, 1.0);
                                            affineTransform.translate(-width, 0);
                                            break;
                                        case 3: // PI rotation
                                            affineTransform.translate(width, height);
                                            affineTransform.rotate(Math.PI);
                                            break;
                                        case 4: // Flip Y
                                            affineTransform.scale(1.0, -1.0);
                                            affineTransform.translate(0, -height);
                                            break;
                                        case 5: // - PI/2 and Flip X
                                            affineTransform.rotate(-Math.PI / 2);
                                            affineTransform.scale(-1.0, 1.0);
                                            break;
                                        case 6: // -PI/2 and -width
                                            affineTransform.translate(height, 0);
                                            affineTransform.rotate(Math.PI / 2);
                                            break;
                                        case 7: // PI/2 and Flip
                                            affineTransform.scale(-1.0, 1.0);
                                            affineTransform.translate(-height, 0);
                                            affineTransform.translate(0, width);
                                            affineTransform.rotate(3 * Math.PI / 2);
                                            break;
                                        case 8: // PI / 2
                                            affineTransform.translate(0, width);
                                            affineTransform.rotate(3 * Math.PI / 2);
                                            break;
                                        default:
                                            break;
                                    }
                                    AffineTransformOp affineTransformOp
                                            = new AffineTransformOp(affineTransform,
                                                    AffineTransformOp.TYPE_BILINEAR);

                                    BufferedImage img1
                                            = new BufferedImage(img.getHeight(),
                                                    img.getWidth(), img.getType());
                                    if (orientation != 1) {
                                        img1 = affineTransformOp.filter(img, img1);
                                    } else {
                                        img1 = img;
                                    }

                                    JLabel xyz1 = new JLabel();
                                    xyz1.setSize(100, 100);
                                    xyz1.setIcon(new ImageIcon(img1.getScaledInstance(xyz1.getWidth(),
                                            xyz1.getHeight(), Image.SCALE_SMOOTH)));
                                    xyz1.setText(s1);
                                    xyz1.setOpaque(true);
                                    xyz1.addMouseListener(new MouseAdapter() {
                                        @Override
                                        public void mousePressed(MouseEvent e) {
                                            JLabel lb = (JLabel) e.getSource();
                                            if (lb.getBackground() != Color.BLUE) {
                                                lb.setBackground(Color.BLUE);
                                                //home.comint.add(Integer.parseInt(lb.getText().trim()));
                                                //home.comja.add(lb);
                                            } else {
                                                lb.setBackground(xyz.getContentPane().getBackground());
                                                //home.comint.remove(Integer.parseInt(lb.getText().trim()));
                                                //home.comja.remove(lb);
                                            }
                                        }
                                    });
                                    p1.add(xyz1);
                                    i++;
                                } catch (Exception e) {
                                    System.out.println(i + " " + s);
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
                                jpb1.setValue(chunk);
                            }
                        }

                        @Override
                        protected void done() {
                            panel.add(jsp, BorderLayout.CENTER);
                            xyz.f1.setVisible(true);
                            xyz.dispose();
                            obj2.dispose();
                            home.comint.clear();
                            Iterator<JLabel> it = home.comja.iterator();
                            while (it.hasNext()) {
                                JLabel next = it.next();
                                next.setBackground(xyz.getContentPane().getBackground());
                            }
                        }

                    };
                    worker.execute();
                } else {
                    JOptionPane.showMessageDialog(null, "No image found", "FACER", JOptionPane.INFORMATION_MESSAGE);
                }
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
