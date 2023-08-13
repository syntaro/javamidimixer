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

import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;
/*
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.TouchEvent;
*/
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.swing.MXSwingAccordion;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManagerDialog;
import jp.synthtarou.midimixer.mx10input.MX10View;
import jp.synthtarou.midimixer.mx35cceditor.MX35View;
import jp.synthtarou.midimixer.mx60output.MX60View;
import jp.synthtarou.midimixer.mx80vst.MX80Panel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMainWindow extends javax.swing.JFrame {

    private static final MXDebugPrint _debug = new MXDebugPrint(MXMainWindow.class);

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

        JMenuItem helpMenu = new JMenuItem("Wiki");
        helpMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String wikiPage = "https://osdn.net/projects/midimixer/wiki/FrontPage";
                try {
                    Desktop.getDesktop().browse(new URI(wikiPage));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MXMainWindow.this, "Failed launch www-Browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        helpParent.add(helpMenu);
        JMenuItem helpMenu2 = new JMenuItem("Forum");
        helpMenu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String wikiPage = "https://osdn.net/projects/midimixer/forums/";
                try {
                    Desktop.getDesktop().browse(new URI(wikiPage));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MXMainWindow.this, "Failed launch www-Browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        helpParent.add(helpMenu2);
        JMenu appMenu = new JMenu(MXStatic.MX_APPNAME_WITH_VERSION);
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
                MXVersionDialog.showAsModal(MXMainWindow.this);
            }
        });

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));
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
                        e.printStackTrace();
                    }
                }
            });
        } else */{
            getContentPane().add(jTabbedPane1);
        }

    }

    /**
     * ウィンドウの初期化
     */
    public void initLatebind(Collection<MXReceiver> viewList) {
        setTitle(MXStatic.MX_APPNAME_WITH_VERSION);
        setSize(new Dimension(1200, 700));

        MXUtil.centerWindow(this);
        setVisible(true);

        int count = 0;
        for (MXReceiver re : viewList) {
            jTabbedPane1.add(re.getReceiverName(), re.getReceiverView());
            JMenuItem menu = new JMenuItem(re.getReceiverName());
            menu.addActionListener(new WindowMenuItemListener(count));
            count++;
            jMenuWindow.add(menu);
        }
        /*
        {
            JMenuItem menu = new JMenuItem("Master Key");
            menu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MXMain.getMain().getMasterKeys().createWindow();
                }
            });
            jMenuWindow.add(menu);
        }
         */
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

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuSaveNow = new javax.swing.JMenuItem();
        jMenuWindow = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jMenuFile.setText("File");

        jMenuSaveNow.setText("Save All Parameters");
        jMenuSaveNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveNowActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSaveNow);

        jMenuBar1.add(jMenuFile);

        jMenuWindow.setText("Window");
        jMenuBar1.add(jMenuWindow);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuSaveNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveNowActionPerformed
        _main.saveEverySettingToFile();
    }//GEN-LAST:event_jMenuSaveNowActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuSaveNow;
    private javax.swing.JMenu jMenuWindow;
    // End of variables declaration//GEN-END:variables

    /**
     * MainFrame内のTABを管理
     *
     * @return MainFrame内のTAB
     */
    public JTabbedPane getTabbedPanel() {
        return jTabbedPane1;
    }

    private void tabPanelStateChanged(javax.swing.event.ChangeEvent evt) {
        int x = jTabbedPane1.getSelectedIndex();
        if (x >= 0) {
            Component view = jTabbedPane1.getComponentAt(x);
            if (view != null) {
                view.requestFocusInWindow();
                if (view instanceof MX60View) {
                    MX60View v60 = (MX60View) view;
                    v60.refreshList();
                }
                if (view instanceof MX10View) {
                    MX10View v10 = (MX10View) view;
                    v10.refreshList();
                }
                if (view instanceof MX80Panel) {
                    MX80Panel v80 = (MX80Panel) view;
                    v80.onResizeSynth();
                }
                if (view instanceof MX35View) {
                    MX35View v35 = (MX35View) view;
                    v35.refreshTable();
                }
            }
        }
    }
}
