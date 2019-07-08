/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author ayush
 */
public class home extends javax.swing.JFrame {

    public static HashSet<Integer> comint;
    public static HashSet<JLabel> comja;
    public static String path_to_python,path,path_map,path_map_index;
 

    /**
     * Creates new form home
     */
    public home() {
        initComponents();
        comint = new HashSet<>();
        comja = new HashSet<>();
            String path1 = home.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path1, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            StringWriter error = new StringWriter();
            ex.printStackTrace(new PrintWriter(error));
            showMessage(error.toString());
            System.exit(1);
        }
            int t1 = path.lastIndexOf('/');
            path = path.substring(0, t1+1);
            JOptionPane.showMessageDialog(null, path);
            path="";
            path_map = path+"map.facer";
            path_map_index = path+"map_indexing.facer";
        try (BufferedReader br = new BufferedReader(new FileReader(path+"PATH_to_Python"))) {
            path_to_python = br.readLine();
        } catch (IOException ex) {
            StringWriter error = new StringWriter();
            ex.printStackTrace(new PrintWriter(error));
            showMessage(error.toString());
            System.exit(1);
        }
        Thread t = new Thread(new Start());
        t.start();
        try {
            t.join();
        } catch (InterruptedException ex) {
            StringWriter error = new StringWriter();
            ex.printStackTrace(new PrintWriter(error));
            showMessage(error.toString());
        }
    }

    static void moveFile(File src, File dest) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dest).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    static void showMessage(String s) {
        JTextArea jta = new JTextArea(s);
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 320);
            }
        };
        JOptionPane.showMessageDialog(
                null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void create_map_index() throws IOException {
        String path = path_to_python + "/openface-master/generated-embeddings/";
        BufferedReader br = new BufferedReader(new FileReader(path + "labels.csv"));
        PrintWriter p = new PrintWriter(new FileWriter(home.path_map_index));
        String s = "";
        int x = 0, cnt = 0, y;
        boolean start = true;
        while ((s = br.readLine()) != null) {
            cnt++;
            y = Integer.parseInt(s.split(",")[0]);
            if (x != y) {
                x = y;
                if (start) {
                    p.print(y + "," + cnt);
                    start = false;
                } else {
                    p.println("," + (cnt - 1));
                    p.print(y + "," + cnt);
                }
            }
        }
        p.println("," + cnt);
        if (x == 1) {
            File f = new File(home.path_map_index);
            f.delete();
        } else {
            p.flush();
            p.close();
        }
        br.close();
    }

    public static void Sort_Embed() throws IOException {
        String path = path_to_python
                + "/openface-master/generated-embeddings/";
        BufferedReader br = new BufferedReader(new FileReader(path + "labels.csv"));
        BufferedReader br1 = new BufferedReader(new FileReader(path + "reps.csv"));
        File tmp = File.createTempFile("tmp", ".csv");
        File tmp1 = File.createTempFile("tmp1", "csv");
        PrintWriter p = new PrintWriter(new FileWriter(tmp));
        PrintWriter p1 = new PrintWriter(new FileWriter(tmp1));
        String s = "", s1;
        ArrayList<Sort> ar = new ArrayList<>();
        while ((s = br.readLine()) != null) {
            s1 = br1.readLine();
            if (s.length() != 0) {
                ar.add(new Sort(s, s1));
            }
        }
        Collections.sort(ar);
        for (Sort x : ar) {
            p.println(x.label);
            p1.println(x.rep);
        }
        p.flush();
        p1.flush();
        p.close();
        p1.close();
        br.close();
        br1.close();
        File f = new File(path + "labels.csv");
        if (f.delete()) {
            moveFile(tmp, f);
        }
        f = new File(path + "reps.csv");
        if (f.delete()) {
            moveFile(tmp1, f);
        }
    }

    public static void Sort_map() throws IOException {
        File file1 = new File(home.path_map);
        File file2 = File.createTempFile("tmp", "facer");
        BufferedReader br = new BufferedReader(new FileReader(file1));
        PrintWriter p = new PrintWriter(new FileWriter(file2));
        ArrayList<String> ar = new ArrayList<>();
        String s = "";
        while ((s = br.readLine()) != null) {
            if (s.length() != 0) {
                ar.add(s);
            }
        }
        Collections.sort(ar);
        for (String string : ar) {
            p.println(string);
        }
        p.flush();
        p.close();
        br.close();
        File f = file1;
        if (file1.delete()) {
            file2.renameTo(f);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        f1 = new javax.swing.JFrame();
        jLabel4 = new javax.swing.JLabel();
        combo1 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        user = new javax.swing.JTextField();
        pass = new javax.swing.JPasswordField();
        jButton2 = new javax.swing.JButton();

        f1.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        f1.setTitle("FACER");
        f1.setMinimumSize(new java.awt.Dimension(518, 361));

        jLabel4.setText("Choose Action");

        combo1.setMaximumRowCount(5);

        jButton1.setText("GO");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout f1Layout = new javax.swing.GroupLayout(f1.getContentPane());
        f1.getContentPane().setLayout(f1Layout);
        f1Layout.setHorizontalGroup(
            f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, f1Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                .addGroup(f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combo1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(76, 76, 76))
        );
        f1Layout.setVerticalGroup(
            f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f1Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addGroup(f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(combo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(78, 78, 78))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("FACER");

        jLabel2.setText("Username");

        jLabel3.setText("Password");

        pass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passKeyReleased(evt);
            }
        });

        jButton2.setText("Exit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(228, 228, 228)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 196, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(user, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addComponent(pass))))
                .addGap(107, 107, 107))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(207, 207, 207)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(63, 63, 63)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(37, 37, 37))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void passKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passKeyReleased
        // TODO add your handling code here:
        String passw = new String(pass.getPassword());
        if (passw.length() == 5) {
            String use = user.getText();
            if (use.equals("admin") && passw.equals("admin")) {
                combo1.addItem("Train");
                //combo1.addItem("Display Images");
                combo1.addItem("Remove");
                combo1.addItem("Real Time Recognition");
                combo1.addItem("Find Images");
                combo1.addItem("Find Duplicate Images");
                combo1.addItem("All Clear");
                combo1.addItem("Exit");
                f1.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "The username or password is wrong");
            }
        }


    }//GEN-LAST:event_passKeyReleased

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String s = combo1.getSelectedItem().toString();
        switch (s) {
            case "Train":
                train obj = new train(this);
                String s1 = "<html><p>Select the directory<br>containing the images of "
                        + "the individual person.</p></html>";
                obj.label1.setText(s1);
                obj.label1.setVisible(false);
                obj.button1.setVisible(false);
                obj.r1.setVisible(false);
                obj.r2.setVisible(false);
                obj.setVisible(true);
                f1.dispose();
                break;
            /*case "Display Images":
                Multiple_Images frame = new Multiple_Images();
                frame.panel.setLayout(new BorderLayout());
                JPanel p = new JPanel(new GridLayout(0, 5, 5, 5));
                JScrollPane jsp = new JScrollPane(p);
                jsp.setPreferredSize(new Dimension(500, 500));
                jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                File dir = new File("/home/ayush/Desktop/images/kat");
                String[] EXTENSIONS = new String[]{
                    "gif", "png", "bmp", "jpeg", "jpg" // and other formats you need
                };
                FilenameFilter IMAGE_FILTER = new FilenameFilter() {
                    public boolean accept(final File dir, final String name) {
                        for (final String ext : EXTENSIONS) {
                            if (name.endsWith("." + ext)) {
                                return (true);
                            }
                        }
                        return (false);
                    }
                };
                if (dir.isDirectory()) { // make sure it's a directory
                    for (File f : dir.listFiles(IMAGE_FILTER)) {
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(f);
                            JLabel xyz = new JLabel();
                            xyz.setSize(100, 100);
                            xyz.setIcon(new ImageIcon(img.getScaledInstance(xyz.getWidth(), xyz.getHeight(),
                                    Image.SCALE_SMOOTH)));
                            xyz.setText(f.getName());
                            xyz.setOpaque(true);
                            xyz.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mousePressed(MouseEvent e) {
                                    JLabel lb = (JLabel) e.getSource();
                                    if (lb.getBackground() != Color.BLUE) {
                                        lb.setBackground(Color.BLUE);
                                    } else {
                                        lb.setBackground(getBackground());
                                    }
                                }
                            });
                            p.add(xyz);
                        } catch (final IOException e) {
                            System.out.println(e);
                        }
                    }
                    frame.panel.add(jsp, BorderLayout.CENTER);
                    frame.setLocation(300, 300);
                    frame.setVisible(true);
                    //frame.pack();
                }
                f1.dispose();
                break;*/
            case "Remove":
                comint.clear();
                comja.clear();
                f1.dispose();
                new Thread(new Remove_Selection(this)).start();
                break;
            case "Real Time Recognition":
                new Real_time().setVisible(true);
                f1.dispose();
                break;
            case "Find Images":
                comint.clear();
                comja.clear();
                new Thread(new Find_Images_initial(this)).start();
                f1.dispose();
                break;
            case "Find Duplicate Images":
                comint.clear();
                comja.clear();
                new Duplicate_Images().setVisible(true);
                f1.dispose();
                break;
            case "All Clear":
                try {
                    File f = new File(home.path_map);
                    f.delete();
                    f = new File(home.path_map_index);
                    f.delete();
                    Remove_Persons.deleteFolder(new File(path_to_python + "/openface-master/aligned-images"));
                    Remove_Persons.deleteFolder(new File(path_to_python + "/openface-master/generated-embeddings"));
                    System.exit(0);
                    break;
                } catch (Exception e) {
                    StringWriter error = new StringWriter();
                    e.printStackTrace(new PrintWriter(error));
                    home.showMessage(error.toString());
                    System.exit(1);
                }
            case "Exit":
                System.exit(0);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> combo1;
    public static javax.swing.JFrame f1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField pass;
    private javax.swing.JTextField user;
    // End of variables declaration//GEN-END:variables
}
