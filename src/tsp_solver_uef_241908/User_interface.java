package tsp_solver_uef_241908;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 11 of 11 (also notice the ".form" file / "Design" tab on NetBeans IDE) 
 * 
 * The user interface class. The application has 3 areas of text, one of them is editable. 
 * The editable one is of course for editing the graph. The other two are results and instructions. 
 * Main commands: new file, open file, save file, exit, run the 8 algorithms, about software. 
 * 
 * Open source Java code, feel free to edit and try your own improvements. 
 * Tested with Windows 11 
 * Apache NetBeans 17 
 * Java JRE 8u371 64bit 
 * Java JDK 18.0.2 64bit 
 * 
 * @author Tuomas Hyvönen 
 * @version 2.0 
 */
public class User_interface extends javax.swing.JFrame {
    final String VERSION = "v-29-4-2023";
    
    /**
     * Constructor that creates a new form "User_interface".
     */
    public User_interface() {
        initComponents();
        jTextArea3.setText("\n Choose FILE - NEW to begin creating your new"
                + "\n mathematical network (=graph)."
                + "\n\n If you already have a graph stored somewhere,"
                + "\n choose FILE - OPEN or copy-paste the text"
                + "\n to the Editor field on the left."
                + "\n\n Use keys F2...F9 to run the"
                + "\n implemented algorithms quickly."
                + "\n\n Please note that for huge graphs, computing"
                + "\n the TSP solution will consume a lot of"
                + "\n time and the window will freeze."
                + "\n Use the Operating System's own commands"
                + "\n to terminate this application in extreme situations."
                + "\n\n In Windows, you can press Control + Alt + Delete "
                + "\n and then choose the Task Manager."
                + "\n In Linux, you can press Control + Alt + F1...F6 "
                + "\n and then type kill commands."
                + "\n In MAC OS, you can press Option + Command + Esc.");
        setWindow();
    }
    
    /**
     * Sets properties of the user interface window and detects pressing enter.
     */
    public final void setWindow() {
        setLocationRelativeTo(null);
        setTitle("TSP Solver " + VERSION);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	addWindowListener(new WindowAdapter(){
            /**
             * windowClosing
             * @param we WindowEvent
             */
            @Override
            public void windowClosing(WindowEvent we){
                if(!jTextArea1.getText().equals("")) { 
                    int choice = JOptionPane.showConfirmDialog(null, 
                                "Unsaved data will be lost. Exit TSP Solver?", 
                                "Exit", 
                                JOptionPane.YES_NO_OPTION);
                    if(choice == 0)
                        System.exit(0);
                }
                else {
                    System.exit(0); // Do not ask if the user wants to exit 
                }                   // since the textarea is empty. 
            }
    	});
        jTextArea1.addKeyListener(new KeyAdapter() {
            /**
             * keyPressed
             * @param e KeyEvent
             */
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER && 
                        jTextArea1.getText().length() > 0 ) {
                    int number = 0;
                    try {
                        int offset=jTextArea1.getLineOfOffset(
                                jTextArea1.getCaretPosition());
                        int start=jTextArea1.getLineStartOffset(offset);
                        int end=jTextArea1.getLineEndOffset(offset);
                        if(jTextArea1.getText(start,(end-start)).length() < 1) {
                            return;
                        }
                        if(jTextArea1.getText(start, 
                                (end-start)).charAt(0) > 48 &&      // from '1' 
                           jTextArea1.getText(start, 
                                   (end-start)).charAt(0) < 58) {   // to '9' 
                            String s = jTextArea1.getText(start, (end-start));
                            Matcher matcher = Pattern.compile("\\d+").matcher(s);
                            matcher.find();
                            try{
                                number = Integer.parseInt(matcher.group());
                                if(number < Integer.MAX_VALUE) {
                                    number++;
                                }
                            }
                            catch(NumberFormatException ex) {
                                System.out.println(ex.getMessage());
                            }
                            jTextArea1.insert(number + " \n", 
                                    jTextArea1.getCaretPosition()+1);
                        }
                    }
                    catch (BadLocationException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            /**
             * keyTyped
             * @param e KeyEvent
             */
            @Override
            public void keyTyped(KeyEvent e) {
            }
            /**
             * keyReleased
             * @param e KeyEvent
             */
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER && 
                       jTextArea1.getCaretPosition() != 
                       jTextArea1.getDocument().getLength()) {
                                jTextArea1.replaceRange("", jTextArea1.getCaretPosition(), 
                                jTextArea1.getCaretPosition()+1);

                                int offset=jTextArea1.getLineOfOffset(
                                    jTextArea1.getCaretPosition());

                            int start=jTextArea1.getLineStartOffset(offset);
                            int end=jTextArea1.getLineEndOffset(offset);
                            if(jTextArea1.getText(start, 
                               (end-start)).charAt(0) > 48 &&      // from '1' 
                               jTextArea1.getText(start, 
                               (end-start)).charAt(0) < 58) {   // to '9' 
                                    jTextArea1.setCaretPosition(jTextArea1.getText().length()-4);
                            }
                            else {
                                jTextArea1.replaceRange("\n", jTextArea1.getCaretPosition(), 
                                jTextArea1.getCaretPosition());
                            }
                    }
                }
                catch(BadLocationException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();
        jMenuItemOpen = new javax.swing.JMenuItem();
        jMenuItemSave = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuRun = new javax.swing.JMenu();
        jMenuItemNNH = new javax.swing.JMenuItem();
        jMenuItem2MST = new javax.swing.JMenuItem();
        jMenuItemCHH = new javax.swing.JMenuItem();
        jMenuItemCHRI = new javax.swing.JMenuItem();
        jMenuItemLK3 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSOM_CH_NN = new javax.swing.JMenuItem();
        jMenuItemSOM_CH_NN_EVO = new javax.swing.JMenuItem();
        jMenuItemLK_SOM_CH_NN_EVO = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTabbedPane1.addTab("Editor", jScrollPane1);

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        jMenuFile.setText("File");

        jMenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemNew.setText("New");
        jMenuItemNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemNew);

        jMenuItemOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemOpen.setText("Open");
        jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpen);

        jMenuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemSave.setText("Save");
        jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSave);
        jMenuFile.add(jSeparator2);

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuRun.setText("Run");

        jMenuItemNNH.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        jMenuItemNNH.setText("\"Nearest neighbor heuristic\" (NNH); get a quick solution without complex tricks, no evolution, no opts");
        jMenuItemNNH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNNHActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemNNH);

        jMenuItem2MST.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        jMenuItem2MST.setText("\"Double minimum spanning tree heuristic with Prim\" (2MST); max 2 times the optimal tour, no evolution, no opts");
        jMenuItem2MST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2MSTActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItem2MST);

        jMenuItemCHH.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        jMenuItemCHH.setText("\"Convex hull heuristic\" (CHH); rubber band around everything, same input = same result, no evolution, no opts");
        jMenuItemCHH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCHHActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemCHH);

        jMenuItemCHRI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItemCHRI.setText("\"Christofides heuristic with Prim\" (CHRI); improvement of 2MST, max 1.5 times the optimal if skills, no evolution, no opts");
        jMenuItemCHRI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCHRIActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemCHRI);

        jMenuItemLK3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        jMenuItemLK3.setText("\"Lin-Kernighan 3\" (LK-NNH-CHH-CHRI); try NNH+CHH+CHRI, choose the best and improve, no evolution, has opts");
        jMenuItemLK3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLK3ActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemLK3);
        jMenuRun.add(jSeparator1);

        jMenuItemSOM_CH_NN.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        jMenuItemSOM_CH_NN.setText("\"Kohonen self-organizing map with hull\" (SOM-CH-NN); convex hull input, NN for clusters, no evolution, no opts");
        jMenuItemSOM_CH_NN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSOM_CH_NNActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemSOM_CH_NN);

        jMenuItemSOM_CH_NN_EVO.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
        jMenuItemSOM_CH_NN_EVO.setText("\"SOM with neuron logic stacks\" (SOM-CH-NN-EVO); more than the CH input and NN for clusters, has evolution, no opts");
        jMenuItemSOM_CH_NN_EVO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSOM_CH_NN_EVOActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemSOM_CH_NN_EVO);

        jMenuItemLK_SOM_CH_NN_EVO.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        jMenuItemLK_SOM_CH_NN_EVO.setText("\"Lin-Kernighan + SOM with neuron logic stacks\" (LK-SOM-CH-NN-EVO); evolution & opts but first SOM-CH-NN-EVO");
        jMenuItemLK_SOM_CH_NN_EVO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLK_SOM_CH_NN_EVOActionPerformed(evt);
            }
        });
        jMenuRun.add(jMenuItemLK_SOM_CH_NN_EVO);

        jMenuBar1.add(jMenuRun);

        jMenuHelp.setText("Help");

        jMenuItemAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Exit option.
     * @param evt ActionEvent
     */
    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        int choice = JOptionPane.showConfirmDialog(null,
                                "Unsaved data will be lost. Exit TSP Solver?",
                                "Exit",
                                JOptionPane.YES_NO_OPTION);
        switch(choice){
            case 0:
                System.exit(0);
                break;
            case 1:
                break;
        }
    }//GEN-LAST:event_jMenuItemExitActionPerformed
    /**
     * New option.
     * @param evt ActionEvent
     */
    private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewActionPerformed
        if(!jTextArea1.getText().equals("")) {
            int choice = JOptionPane.showConfirmDialog(null,
                                "Unsaved data will be lost. Create a new file?",
                                "New",
                                JOptionPane.YES_NO_OPTION);
            switch(choice){
                case 0:
                        String basic_info = "NAME : \n" + 
                            "COMMENT : \n" + 
                            "TYPE : TSP\n" + 
                            //"DIMENSION : 5\n" + 
                            "EDGE_WEIGHT_TYPE : EUC_2D\n" + 
                            "NODE_COORD_SECTION \n" + 
                            "1 3.0 4.0\n" + 
                            "2 1.0 5.0\n" + 
                            "3 1.0 1.0\n" + 
                            "4 5.0 1.0\n" + 
                            "5 5.0 5.0\n" + 
                            "EOF";
                        jTextArea1.setText(basic_info);
                    break;
                case 1:
                    break;
            }
        }
        else {
            String basic_info = "NAME : \n" + 
                            "COMMENT : \n" + 
                            "TYPE : TSP\n" + 
                            //"DIMENSION : 5\n" + 
                            "EDGE_WEIGHT_TYPE : EUC_2D\n" + 
                            "NODE_COORD_SECTION \n" + 
                            "1 3.0 4.0\n" + 
                            "2 1.0 5.0\n" + 
                            "3 1.0 1.0\n" + 
                            "4 5.0 1.0\n" + 
                            "5 5.0 5.0\n" + 
                            "EOF";
            jTextArea1.setText(basic_info);
            
        }
    }//GEN-LAST:event_jMenuItemNewActionPerformed
    /**
     * Open option. 
     * 
     * If a Waterloo University website ".tsp" file cannot be opened, open 
     * the file with Notepad, then copy-paste the text to the jTextArea. 
     * 
     * The implementation here works but does not open all types of foreign TSPs.
     * 
     * @param evt ActionEvent
     */
    private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenActionPerformed
        int x;
    	File selectedFile;// = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".")); // the directory with the Jar is wanted 
        x = fileChooser.showOpenDialog(this);
        if (x == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            if(!jTextArea1.getText().equals("")) {
                int choice = JOptionPane.showConfirmDialog(null,
                                    "Unsaved data will be lost. Open the new file?",
                                    "Open",
                                    JOptionPane.YES_NO_OPTION);
                switch(choice){
                    case 0:
                            try(FileInputStream fis_file = new FileInputStream(
                                String.valueOf(selectedFile)); 
                                ObjectInputStream ois_file = 
                                new ObjectInputStream(fis_file)) {
                                jTextArea1.setText((String)ois_file.readObject());
                            }
                            catch(IOException | ClassNotFoundException e){
                                System.err.println(e);
                            }
                        break;
                    case 1: // do nothing, something could be added if wanted 
                            // like "Save the file before opening another?" could be the question 
                        break;
                }
            }
            else {
                try(FileInputStream fis_file = new FileInputStream(
                    String.valueOf(selectedFile)); 
                    ObjectInputStream ois_file = 
                    new ObjectInputStream(fis_file)) {
                    jTextArea1.setText((String)ois_file.readObject());
                }
                catch(IOException | ClassNotFoundException e){
                    System.err.println(e);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemOpenActionPerformed
    /**
     * Save option.
     * @param evt ActionEvent
     */
    private void jMenuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveActionPerformed
        int x;							
    	JTextField txtfield = new JTextField(44);
    	Object[] choiceobj = {txtfield, "Save tsp"};
    	do{
            int z = JOptionPane.showOptionDialog(null,
                "Directory will be the Jar file directory. Please write a file name without the extension (.tsp):\n", 
                "Save tsp",
                JOptionPane.PLAIN_MESSAGE, 
                3, null,
                choiceobj,
                choiceobj[0]);
            x = -1;
            if (z == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (txtfield.getText().length() < 1){
                x = 0;
                JOptionPane.showMessageDialog(null,
                "File name cannot be 0 characters long.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            }
    	} while(x!=-1);
    	File file = new File(txtfield.getText() + ".tsp");
    	int sav_choice;
    	if(file.exists()){
            sav_choice = JOptionPane.showConfirmDialog(null,
            "File already exists. Overwrite?",
            "Save",
            JOptionPane.YES_NO_OPTION);
    	}
    	else{
            try(FileOutputStream fosFile = 
                    new FileOutputStream(file); 
                    ObjectOutputStream oosFile = 
                        new ObjectOutputStream(fosFile)) {
                        oosFile.writeObject(jTextArea1.getText());
                        oosFile.flush();
            }
            catch(Exception e){
                System.err.println(e);
            }
            return;
    	}
        switch(sav_choice){
            case 0:
                try(FileOutputStream fosFile = 
                    new FileOutputStream(file); 
                    ObjectOutputStream oosFile = 
                        new ObjectOutputStream(fosFile)) {
                        oosFile.writeObject(
                            jTextArea1.getText());
                        oosFile.flush();
                }
                catch(Exception e){
                    System.err.println(e);
                }
                break;
            case 1: // do nothing 
                break;
        }
    }//GEN-LAST:event_jMenuItemSaveActionPerformed
    /**
     * Calling the NNH.
     * @param evt ActionEvent
     */
    private void jMenuItemNNHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNNHActionPerformed
        String result = TSP_Solver_UEF_241908.NearestNeighbour_Algorithm(
            jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemNNHActionPerformed
    /**
     * Calling the 2MST (Prim, since Kruskal and others are not implemented in this version).
     * @param evt ActionEvent
     */
    private void jMenuItem2MSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2MSTActionPerformed
        String result = TSP_Solver_UEF_241908.DoubleMST_Algorithm_Prim(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItem2MSTActionPerformed
    /**
     * Calling the CHH.
     * @param evt ActionEvent
     */
    private void jMenuItemCHHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCHHActionPerformed
        String result = TSP_Solver_UEF_241908.ConvexHull_Algorithm(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemCHHActionPerformed
    /**
     * About option.
     * @param evt ActionEvent
     */
    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
            JDialog about = new JDialog();
            about.setSize(750, 110);
            about.setLocationRelativeTo(null);
            about.setResizable(false);
            about.setTitle("About");
            JLabel txt1 = new JLabel(
                    "  TSP Solver " + VERSION + "  © Tuomas Hyvönen - This "
                            + "software is for finding solutions to the 2D Traveling Salesman Problem");
            JLabel txt2 = new JLabel("  University of Eastern Finland     "
                    + "https://www.uef.fi/en/unit/school-of-computing     https://github.com/tuomasth");
            JLabel txt3 = new JLabel("  Apache NetBeans is recommended for "
                    + "programming own logic fragments with Java for SOM-CH-NN-EVO (F8)");
            about.add(txt1, BorderLayout.NORTH);
            about.add(txt2, BorderLayout.CENTER);
            about.add(txt3, BorderLayout.SOUTH);
            about.setAlwaysOnTop(true);
            about.setVisible(true);
            
            about.addWindowListener(new WindowListener() {
                /**
                 * windowClosed
                 */
                @Override
                public void windowClosed(WindowEvent e) {
                }
                /**
                 * windowOpened
                 */
                @Override
                public void windowOpened(WindowEvent e) {
                }
                /**
                 * windowClosing, has enabling code 
                 */
                @Override
                public void windowClosing(WindowEvent e) {
                    jMenuFile.setEnabled(true);
                    jMenuRun.setEnabled(true);
                    jMenuHelp.setEnabled(true);
                    //System.out.println("about window closed");
                }
                /**
                 * windowIconified
                 */
                @Override
                public void windowIconified(WindowEvent e) {
                }
                /**
                 * windowDeiconified
                 */
                @Override
                public void windowDeiconified(WindowEvent e) {
                }
                /**
                 * windowActivated
                 */
                @Override
                public void windowActivated(WindowEvent e) {
                }
                /**
                 * windowDeactivated
                 */
                @Override
                public void windowDeactivated(WindowEvent e) {
                }
            });
            jMenuFile.setEnabled(false);
            jMenuRun.setEnabled(false);
            jMenuHelp.setEnabled(false);
            about.addKeyListener(new KeyListener() {
                /**
                 * keyPressed (escape button on about window) 
                 */
                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        jMenuFile.setEnabled(true);
                        jMenuRun.setEnabled(true);
                        jMenuHelp.setEnabled(true);
                        about.dispose();
                    }
                }
                /**
                 * keyTyped
                 */
                @Override
                public void keyTyped(KeyEvent e) {
                }
                /**
                 * keyReleased
                 */
                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
    }//GEN-LAST:event_jMenuItemAboutActionPerformed
    /**
     * Calling CHRI.
     * @param evt 
     */
    private void jMenuItemCHRIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCHRIActionPerformed
        String result = TSP_Solver_UEF_241908.Christofides_Algorithm(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemCHRIActionPerformed
    /**
     * Calling SOM CH NN.
     * @param evt 
     */
    private void jMenuItemSOM_CH_NNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSOM_CH_NNActionPerformed
        String result = TSP_Solver_UEF_241908.SOM_CH_NN_Algorithm(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemSOM_CH_NNActionPerformed
    /**
     * Calling LK3.
     * @param evt 
     */
    private void jMenuItemLK3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLK3ActionPerformed
        String result = TSP_Solver_UEF_241908.LK3_Algorithm(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemLK3ActionPerformed
    /**
     * Calling SOM CH NN EVO.
     * @param evt 
     */
    private void jMenuItemSOM_CH_NN_EVOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSOM_CH_NN_EVOActionPerformed
        String result = TSP_Solver_UEF_241908.SOM_CH_NN_EVO_Algorithm(
                jTextArea1.getText());
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemSOM_CH_NN_EVOActionPerformed
    /**
     * Calling LK SOM CH NN EVO.
     * @param evt 
     */
    private void jMenuItemLK_SOM_CH_NN_EVOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLK_SOM_CH_NN_EVOActionPerformed
        String result = TSP_Solver_UEF_241908.LK_SOM_CH_NN_EVO_Algorithm(
                jTextArea1.getText(), true);
        jTextArea2.setText(result);
    }//GEN-LAST:event_jMenuItemLK_SOM_CH_NN_EVOActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItem2MST;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemCHH;
    private javax.swing.JMenuItem jMenuItemCHRI;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemLK3;
    private javax.swing.JMenuItem jMenuItemLK_SOM_CH_NN_EVO;
    private javax.swing.JMenuItem jMenuItemNNH;
    private javax.swing.JMenuItem jMenuItemNew;
    private javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JMenuItem jMenuItemSOM_CH_NN;
    private javax.swing.JMenuItem jMenuItemSOM_CH_NN_EVO;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenu jMenuRun;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    // End of variables declaration//GEN-END:variables
}