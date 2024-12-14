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
public class ListModelINSTList extends AbstractListModel<String> {
    XTFile _sfz;
    SFZElement.SFZElement_inst _inst;
    
    public ListModelINSTList(XTFile sfz) {
        _sfz = sfz;
        _inst = (SFZElement.SFZElement_inst)sfz.getElement("inst");
    }
    
    public ListModelINSTBagTable getAsINSTBagTable(int x) {
        if (x >= 0 && x < _inst.size()) {
            XTTable bag = _inst.get(x).tableColumn(SFZElement.INST_BAGINDEX_TABLE);
            return new ListModelINSTBagTable(bag);
        }
        return null;
    }

    @Override
    public int getSize() {
        return _inst.size();
    }

    @Override
    public String getElementAt(int index) {
        XTRow row = _inst.get(index);
        String name = row.textColumn(SFZElement.INST_NAME);
        return index + " [] " + name;
    }
}
