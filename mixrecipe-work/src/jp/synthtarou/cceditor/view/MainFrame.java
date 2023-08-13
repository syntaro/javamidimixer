/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.synthtarou.cceditor.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import jp.synthtarou.cceditor.Main;
import static jp.synthtarou.cceditor.Main.centerWindow;
import jp.synthtarou.midimixer.libs.settings.MXSetting;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    
    DXControlSectionList _kontrol;
    
    public MainFrame() {
        initComponents();
        setTitle(Main.APPLICATION_NAME + " " + Main.APPLICATION_VERSION);

        _kontrol = new DXControlSectionList();
        getContentPane().add(_kontrol);
        setSize(800, 600);
        centerWindow(this);
        
        addWindowListener(new WindowAdapter(){
            public void windowOpened(WindowEvent e) {
                _kontrol.updateUI();
            }
            public void windowClosing(WindowEvent e) {
                MXSetting.saveEverySettingToFile();
            }
            public void windowClosed(WindowEvent e){
                System.exit(0);
            }
        });
    }

    public void updateUITree() {
        JComponent c = (JComponent)getContentPane();
        c.updateUI();
        _kontrol.updateUI();;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
