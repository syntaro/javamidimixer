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
package jp.synthtarou.midimixer.libs.midi.programlist;

import jp.synthtarou.midimixer.libs.midi.programlist.database.PDDrumSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.text.MXLineReader;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class GMLevel1Drum extends PDDrumSet {
    public GMLevel1Drum() {
        try {
            InputStream stream = GMLevel1.class.getResourceAsStream("GMLevel1Drum.csv");
            readCSV(stream, "Shift_JIS");
            stream.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void readCSV(InputStream stream, String charset) {
        MXLineReader reader = new MXLineReader(stream, charset);
        String line;
        String folder = "";
        ArrayList<String> cells = new ArrayList();
        
        try {
            while( (line = reader.readLine()) != null ) {
                if (line.startsWith("#")) {
                    folder = line.substring(1);
                    continue;
                }

                MXUtil.split(line, cells, ',');

                if (cells.size() >= 4) {
                    try {
                        String noteNumber = cells.get(0);
                        String noteKey1 = cells.get(1);
                        String nameEnglish = cells.get(2);
                        String noteKey2 = cells.get(3);
                        
                        addNote(MXUtil.numberFromText(noteNumber), nameEnglish);
                    }catch(Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch(IOException e) {
            e.printStackTrace();;
        }
    }

}
