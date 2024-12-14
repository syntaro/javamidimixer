/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont.table;

import java.util.ArrayList;
import jp.synthtarou.mixtone.listmodel.TextListForDebug;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTTable extends ArrayList<XTRow> {
    XTHeader _header;
    
    public XTTable() {
        _header = new XTHeader();
    }

    public XTTable(XTTable parent, int from, int to) {
        _header = parent._header;
        for (int i = from; i <= to; ++ i) {
            XTRow row = parent.get(i);
            if (row != null) {
                add(row);
            }
        }
    }

    public XTTable(XTHeader header) {
        _header = header;
    }

    public XTHeader getHeader() {
        return _header;
    }
    
    public XTRow newRow() {
        XTRow row = new XTRow(this);
        add(row);
        return row;
    }

    public String toString() {
        TextListForDebug dumper = new TextListForDebug();
        getDump(dumper);
        return dumper.toString();
    }

    public void getDump(TextListForDebug list) {
        StringBuilder line = new StringBuilder();
        for (int x = 0; x < _header.size(); ++ x) {
            String text = _header.get(x);
            text = escape(text);
            if (x == 0) {
                line.append(text);
            }
            else {
                line.append(",");
                line.append(text);
            }
        }
        list.add(line.toString());
        for (int y = 0; y < size(); ++ y) {
            XTRow row = get(y);
            line = new StringBuilder();
            for (int x = 0; x < _header.size(); ++ x) {
                XTColumn col = row.get(x);
                String text = col._textValue;
                if (text != null) {
                    text = escape(text);
                }
                else {
                    text = String.valueOf(col._numberValue);
                }
                if (x == 0) {
                    line.append(text);
                }
                else {
                    line.append(",");
                    line.append(text);
                }
            }
            list.add(line.toString());
        }
        
    }
    
    public String escape(String text) {
        return TextListForDebug.quote(text);
    }
    
    public XTRow getOr(int x) {
        if (x < 0 || x >= size()) {
            return null;
        }
        return get(x);
    }
    
    public XTRow findRow(int column, Number target) {
        for (int y = 0; y < size(); ++ y) {
            XTRow row = get(y);
            Number n = row.get(column)._numberValue;
            if (n != null && n.equals(target)) {
                return row;
            }
        }
        return null;
    }
}

