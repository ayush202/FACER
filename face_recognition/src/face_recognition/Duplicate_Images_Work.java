/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

/**
 *
 * @author ayush
 */
public class Duplicate_Images_Work implements Runnable {

    Duplicate_Images xyz;
    JPanel panel, panel1;

    public Duplicate_Images_Work(Duplicate_Images xy) {
        xyz = xy;
        panel = xyz.panel1;
        panel1 = xyz.panel2;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(8080);
            ProcessBuilder pb = new ProcessBuilder(home.path_to_python+"/venv/bin/python3",
                    home.path_to_python+"/pytorch_repo"+ "/duplicate_images.py");
            Show_Window oWindow = new Show_Window(xyz, false, 150, 45);
            oWindow.addLabel("Starting");
            oWindow.addProgressBar_Indeterminate(100);
            oWindow.setVisible(true);
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
                home.showMessage(s.toString());
                System.exit(1);
            }
            System.out.println("reached here");
            Thread.sleep(10);
            Socket client = server.accept();
            String fromClient;
            System.out.println("reached here1");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter pout = new PrintWriter(client.getOutputStream());
            while (!client.isClosed()) {
                fromClient = in.readLine();
                if (fromClient == null) {
                    continue;
                }
                if (fromClient.equals("started")) {
                    DefaultListModel model = (DefaultListModel)xyz.list.getModel();
                    StringBuilder data = new StringBuilder();
                    for (int i = 0; i < model.getSize(); i++) {
                        if(i==0)
                            data.append(model.getElementAt(i));
                        else
                            data.append(","+model.getElementAt(i));
                    }
                    pout.print(data);
                    pout.flush();
                    oWindow.closeWindow();
                } else if (fromClient.equals("length")) {
                    int len = Integer.parseInt(in.readLine());
                    oWindow = new Show_Window(xyz, false, 200, 45);
                    oWindow.addLabel("Finding Duplicates");
                    System.out.println(len);
                    oWindow.addProgressBar(len*2);
                    oWindow.setVisible(true);
                } else if (fromClient.equals("unable to load")
                        || fromClient.equals("one complete")) {
                    oWindow.setValue();
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
                System.out.println("received : " + fromClient);
                Thread.sleep(0, 30);
            }
            while (p.isAlive());
            System.out.println(p.isAlive() + " " + p.exitValue());
            oWindow.closeWindow();
            server.close();
            if (!p.isAlive() && p.exitValue() == 1) {
                 while (sc.hasNextLine()) {
                    s.append(sc.nextLine());
                }
                //JOptionPane.showMessageDialog(null, s,"Error",JOptionPane.ERROR_MESSAGE);                    
                System.out.println("returned with exit status 1");
                home.showMessage(s.toString());
                System.exit(1);
            }
            while (!server.isClosed());
            BufferedReader br = new BufferedReader(new FileReader("tmp.csv"));
            s1 = "";
            ArrayList<String> ar = new ArrayList<>();
            while ((s1 = br.readLine()) != null) {
                int t=-1;
                    for(int i=0;i<s1.length()-1;i++)
                    {
                        if(s1.charAt(i)==','&&s1.charAt(i+1)==',')
                        {
                            t=i;
                            break;
                        }
                    }
                    if(t==-1)
                        ar.add(s1);
                    else
                        ar.add(s1.substring(0, t));
            }
            panel.setLayout(new BorderLayout());
            JPanel p1 = new JPanel(new GridLayout(0, 5, 5, 5));
            JScrollPane jsp = new JScrollPane(p1);
            jsp.setPreferredSize(new Dimension(500, 500));
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            panel1.setLayout(new BorderLayout());
            JPanel p2 = new JPanel(new GridLayout(0, 5, 5, 5));
            JScrollPane jsp1 = new JScrollPane(p2);
            jsp1.setPreferredSize(new Dimension(500, 500));
            jsp1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            File f = new File("folder.jpeg");
            BufferedImage img = ImageIO.read(f);
            for (int i = 0; i < ar.size(); i++) {
                JLabel xy = new JLabel();
                xy.setSize(100, 100);
                xy.setIcon(new ImageIcon(img.getScaledInstance(xy.getWidth(),
                        xy.getHeight(), Image.SCALE_SMOOTH)));
                xy.setText("Type " + i);
                xy.setOpaque(true);
                xy.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        JLabel lb = (JLabel) e.getSource();
                        p2.removeAll();
                        p2.revalidate();
                        p2.repaint();
                        int x = Integer.parseInt(lb.getText().substring(5));
                        Show_Window oWindow = new Show_Window(xyz.f1, false, 200, 45);
                        oWindow.addLabel("Loading Images");
                        oWindow.addProgressBar(ar.get(x).split(",").length);
                        oWindow.setVisible(true);
                        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                int i = 1;
                                String st[] = ar.get(x).split(",");
                                for (String s : st) {
                                    String s1 = s;
                                    File f = new File(s);
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

                                        int width = img.getWidth();
                                        int height = img.getHeight();

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
                                        p2.add(xyz1);
                                        i++;
                                    } catch (Exception e) {
                                        System.out.println(i + " " + s);
                                        StringWriter error = new StringWriter();
                                        e.printStackTrace(new PrintWriter(error));
                                        home.showMessage(error.toString());
                                        Thread.sleep(400);
                                        System.exit(0);
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void process(List<Integer> chunks) {
                                for (Integer chunk : chunks) {
                                    oWindow.setValue();
                                }
                            }

                            @Override
                            protected void done() {
                                panel1.add(jsp1, BorderLayout.CENTER);
                                xyz.f2.setVisible(true);
                                xyz.f1.dispose();
                                oWindow.closeWindow();
                                home.comint.clear();
                                Iterator<JLabel> it = home.comja.iterator();
                                while (it.hasNext()) {
                                    JLabel next = it.next();
                                    next.setBackground(xyz.getContentPane().getBackground());
                                }
                            }

                        };
                        worker.execute();
                    }
                });
                p1.add(xy);
                Thread.sleep(0,50);
            }
            xyz.panel1.add(jsp, BorderLayout.CENTER);
            xyz.f1.setVisible(true);
            xyz.dispose();

        } catch (Exception e) {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            home.showMessage(error.toString());
            System.exit(1);
        }
    }
}
