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
package jp.synthtarou.midimixer.mx80vst;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import jp.synthtarou.midimixer.libs.vst.VSTInstance;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VSTVolume extends javax.swing.JPanel {

    CurvedSlider _slider;
    VSTInstance _instance;
    int _bus;

    /**
     * Creates new form VSTVolume
     */
    public VSTVolume(VSTInstance instance, int bus) {
        initComponents();
        _instance = instance;
        _bus = bus;
        
        _slider = new CurvedSlider(45);
        _slider.setValue(MXRangedValue.new7bit(_instance.getBusVolume(_bus)));
        _slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _instance.setBusVolume(_bus, _slider.getValue());
            }
        });
        
        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.fill = GridBagConstraints.BOTH;
        con.anchor = GridBagConstraints.CENTER;
        con.weightx = 1;
        con.weighty = 0;
        add(_slider, con);

        JLabel label;
        label = new JLabel(String.valueOf(bus + 1));
        label.setHorizontalAlignment(JLabel.CENTER);
        
        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 1;
        con.fill = GridBagConstraints.BOTH;
        con.anchor = GridBagConstraints.CENTER;
        con.weightx = 1;
        con.weighty = 0;
        add(label, con);
        
        setPreferredSize(new Dimension(45, 55));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    public void reload() {
        MXMain.invokeUI(() ->  {
            _slider.setValue(MXRangedValue.new7bit(_instance.getBusVolume(_bus)));
	});
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
