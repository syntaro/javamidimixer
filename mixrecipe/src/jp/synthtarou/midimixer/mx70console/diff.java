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
package jp.synthtarou.midimixer.mx70console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class diff {
    public static void main(String [] args) throws IOException {
        File f1 = new File("c:/midi/sysex/a.txt");
        File f2 = new File("c:/midi/sysex/b.txt");
        
        BufferedReader r1 = new BufferedReader(new FileReader(f1));
        BufferedReader r2 = new BufferedReader(new FileReader(f2));
        
        int countSame = 0;
        int countDiffer = 0;

        while(true) {
            String line1 = r1.readLine();
            String line2 = r2.readLine();
            if (line1 == null) {
                break;
            }
            if (line1.equals(line2)) {
                countSame ++;
            }else {
                countDiffer ++;
            }
        }
        
        System.out.println("same = " + countSame);
        System.out.println("different = " + countDiffer);
    }
}
