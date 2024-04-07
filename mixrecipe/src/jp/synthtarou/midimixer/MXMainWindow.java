/*
 * Copyright 2023 Syntarou YOSHIDA.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer;

import jp.synthtarou.midimixer.progress.MXProgressDialog;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManagerDialog;
import jp.synthtarou.midimixer.mx00playlist.MX00View;
import jp.synthtarou.midimixer.mx10input.MX10View;
import jp.synthtarou.midimixer.mx12masterpiano.MX12Process;
import jp.synthtarou.midimixer.mx36ccmapping.MX36View;
import jp.synthtarou.midimixer.mx60output.MX60View;
import jp.synthtarou.midimixer.mx80vst.MX80View;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMainWindow extends javax.swing.JFrame {

    MXMain _main;
    JTabbedPane jTabbedPane1;

    /**
     * Constructor
     */
    public MXMainWindow(MXMain main) {
        _main = main;
        initComponents();
        jMenuBar1.add(Box.createHorizontalGlue());
        JMenu helpParent = new JMenu("Help");
        jMenuBar1.add(helpParent);

        jTabbedPane1 = new javax.swing.JTabbedPane();

        JMenuItem helpMenu = new JMenuItem("HomePage/サイト");
        helpMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String url = "https://syntaro.github.io/javamidimixer/";
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MXMainWindow.this, "Failed launch www-Browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        helpParent.add(helpMenu);

        JMenuItem helpMenu2 = new JMenuItem("Manual PDF");
        helpMenu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String url = "https://syntaro.github.io/javamidimixer/MIXRecipe.pdf";
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MXMainWindow.this, "Failed launch www-Browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        helpParent.add(helpMenu2);

        JMenuItem helpMenu3 = new JMenuItem("Issue/問題");
        helpMenu3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String url = "https://github.com/syntaro/javamidimixer/issues";
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MXMainWindow.this, "Failed launch www-Browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        helpParent.add(helpMenu3);

        JMenu appMenu = new JMenu(MXConfiguration.MX_APPLICATION);
        jMenuBar1.add(appMenu);

        JMenuItem themeMenu = new JMenuItem("Theme");
        appMenu.add(themeMenu);
        themeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                new ThemeManagerDialog(MXMainWindow.this, true).setVisible(true);
            }
        });

        JMenuItem versionMenu = new JMenuItem("About MIXRecipe");
        appMenu.add(versionMenu);
        versionMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                MXProgressDialog progress = new MXProgressDialog(MXMainWindow.this, true);
                progress.setMessageAsVersion();
                progress.setVisible(true);
            }
        });

        //getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));
        /*
        if (true) {
            new JFXPanel(); //initialize before runLater (safety call -> Platform.startup)
            if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
                System.out.println("Touch O");
            }else {
                System.out.println("Touch X"); //僕のWindowsではここになるのでサポートしない
            }
            if (Platform.isSupported(ConditionalFeature.SWING)) {
                System.out.println("Swing O");
            }else {
                System.out.println("Swing X");
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        JFXPanel jfx = new JFXPanel();
                        jfx.setLayout(new javax.swing.BoxLayout(jfx, javax.swing.BoxLayout.LINE_AXIS));
                        jfx.add(jTabbedPane1);
                        getContentPane().add(jfx);

                        Scene scene = new Scene(new Group(), javafx.scene.paint.Color.TRANSPARENT);
                        jfx.setScene(scene);
                        jfx.getScene().addEventFilter(TouchEvent.ANY, e -> System.out.println("ANY " + e));
                    }catch(Exception e) {
                        MXLogger2.getLogger(MXMainWindow.class).log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            });
        }
        */
        jPanelForTab.remove(jButtonDummyTab);;
        jPanelForTab.add(jTabbedPane1);
        jPanelForPiano.remove(jButtonDummyPiano);
        jPanelForPiano.add(_main.getPianoProcess().getReceiverView());
    }
    
    List<MXReceiver> _viewList = null;

    /**
     * ウィンドウの初期化
     */
    public void initLatebind(List<MXReceiver> viewList) {
        setTitle(MXConfiguration.MX_APPLICATION);
        setSize(new Dimension(1200, 800));

        MXUtil.centerWindow(this);
        setVisible(true);

        int count = 0;
        _viewList = viewList;
        for (MXReceiver re : viewList) {
            jTabbedPane1.add(re.getReceiverName(), re.getReceiverView());
            JMenuItem menu = new JMenuItem(re.getReceiverName());
            menu.addActionListener(new WindowMenuItemListener(count));
            count++;
            jMenuWindow.add(menu);
        }
        {
            JMenuItem menu = new JMenuItem("Free Console / SysEX");
            menu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MXMain.getMain().openFreeConsole();
                }
            });
            jMenuWindow.add(menu);
        }
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPanelStateChanged(evt);
            }
        });
    }

    class WindowMenuItemListener implements ActionListener {

        int _page;

        public WindowMenuItemListener(int page) {
            _page = page;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jTabbedPane1.setSelectedIndex(_page);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelForTab = new javax.swing.JPanel();
        jButtonDummyTab = new javax.swing.JButton();
        jPanelForPiano = new javax.swing.JPanel();
        jButtonDummyPiano = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuSaveNow = new javax.swing.JMenuItem();
        jMenuExitWithoutSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuSaveAndExit = new javax.swing.JMenuItem();
        jMenuWindow = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelForTab.setLayout(new javax.swing.BoxLayout(jPanelForTab, javax.swing.BoxLayout.LINE_AXIS));

        jButtonDummyTab.setText("jButton1");
        jPanelForTab.add(jButtonDummyTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanelForTab, gridBagConstraints);

        jPanelForPiano.setLayout(new javax.swing.BoxLayout(jPanelForPiano, javax.swing.BoxLayout.LINE_AXIS));

        jButtonDummyPiano.setText("jButton2");
        jPanelForPiano.add(jButtonDummyPiano);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanelForPiano, gridBagConstraints);

        jMenuFile.setText("File");

        jMenuSaveNow.setText("Save All Parameters");
        jMenuSaveNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveNowActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSaveNow);

        jMenuExitWithoutSave.setText("Exit Without Save");
        jMenuExitWithoutSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuExitWithoutSaveActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuExitWithoutSave);
        jMenuFile.add(jSeparator1);

        jMenuSaveAndExit.setText("Save And Exit");
        jMenuSaveAndExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveAndExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSaveAndExit);

        jMenuBar1.add(jMenuFile);

        jMenuWindow.setText("Window");
        jMenuBar1.add(jMenuWindow);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuSaveNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveNowActionPerformed
        _main.saveEverySettingToFile();
    }//GEN-LAST:event_jMenuSaveNowActionPerformed

    private void jMenuExitWithoutSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExitWithoutSaveActionPerformed
        // TODO add your handling code here:
        _main.exitWithoutSave();
    }//GEN-LAST:event_jMenuExitWithoutSaveActionPerformed

    private void jMenuSaveAndExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveAndExitActionPerformed
        _main.saveAndExit();
    }//GEN-LAST:event_jMenuSaveAndExitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDummyPiano;
    private javax.swing.JButton jButtonDummyTab;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuExitWithoutSave;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuSaveAndExit;
    private javax.swing.JMenuItem jMenuSaveNow;
    private javax.swing.JMenu jMenuWindow;
    private javax.swing.JPanel jPanelForPiano;
    private javax.swing.JPanel jPanelForTab;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables

    /**
     * MainFrame内のTABを管理
     *
     * @return MainFrame内のTAB
     */
    public JTabbedPane getTabbedPanel() {
        return jTabbedPane1;
    }
    
    public MXReceiver getSelectedReceiver() {
        int x = jTabbedPane1.getSelectedIndex();
        if (_viewList != null) {
            return _viewList.get(x);
        }
        return null;
    }

    int _prevTab = -1;

    private void tabPanelStateChanged(javax.swing.event.ChangeEvent evt) {
        int x = jTabbedPane1.getSelectedIndex();

        if (_prevTab >= 0 && _prevTab != x) {
            Component view = jTabbedPane1.getComponentAt(_prevTab);
            if (view != null) {
                if (view instanceof MX00View) {
                    MX00View v00 = (MX00View) view;
                    v00.tabDeactivated();
                }
            }
        }
        _prevTab = x;
        if (x >= 0) {
            Component view = jTabbedPane1.getComponentAt(x);
            if (view != null) {
                view.requestFocusInWindow();
                if (view instanceof MX60View) {
                    MX60View v60 = (MX60View) view;
                    v60.tabActivated();
                }
                if (view instanceof MX00View) {
                    MX00View v00 = (MX00View) view;
                    v00.tabActivated();
                }
                if (view instanceof MX10View) {
                    MX10View v10 = (MX10View) view;
                    v10.tabActivated();
                }
                if (view instanceof MX80View) {
                    MX80View v80 = (MX80View) view;
                    v80.tabActivated();
                }
                if (view instanceof MX36View) {
                    MX36View v36 = (MX36View) view;
                    v36.tabActivated();
                }
            }
            MX12Process piano = MXMain.getMain().getPianoProcess();
            piano.updateViewForSettingChange();
        }
    }
}
