/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.listmodel;

import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelPHDRList extends AbstractListModel<String>{
    XTFile _sfz;
    SFZElement.SFZElement_phdr _phdr;

    public ListModelPHDRList(XTFile sfz) {
        _sfz = sfz;
        _phdr = (SFZElement.SFZElement_phdr)sfz.getElement("phdr");
    }

    public ListModelPHDRBagTable getAsPHDRBagTable(int x) {
        XTTable bag = _phdr.get(x).tableColumn(SFZElement.PHDR_BAGINDEX_TABLE);
        if (bag == null) {
            return null;
        }
        return new ListModelPHDRBagTable(bag);
    }

    @Override
    public int getSize() {
        return  _phdr.size();
    }

    @Override
    public String getElementAt(int index) {
        XTRow row  = _phdr.get(index);
        int number = row.intColumn(SFZElement.PHDR_PRESETNO);
        String name = row.textColumn(SFZElement.PHDR_NAME);
        return index +" [" + number +"] = "+ name + "*" + row.getDump();
    }
}
