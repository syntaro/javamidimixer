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
package jp.synthtarou.midimixer.libs.midi.programlist.database;

import java.util.Collection;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PDModule implements DoubleIndexElement {
    private static final MXDebugPrint _debug = new MXDebugPrint(PDModule.class);

    public PDModule() {
        this("");
    }
    
    public PDModule(String name) {
        _moduleName = name;
    }

    public PDBank simpleGet(int programNumber, int msb, int lsb) {
        if (_database == null) {
            _database = createDatabase();
        }
        PDProgram prog = _database.get(programNumber);
        if(prog != null) {
            if (msb >= 0 && lsb >= 0) {
                PDBank bank = prog.get(msb, lsb);
                if (bank != null) {
                    return bank;
                }
            }
            return prog._bankMap.first();
        }
        return null;
    }
    
    public String simpleGetName(int programNumber, int msb, int lsb) {
        PDBank bank = simpleGet(programNumber, msb, lsb);
        if (bank != null) {
            return bank.getName();
        }
        return null;
    }
    
    public String simpleGetDrum(int note) {
        if (_drumSet == null) {
            for (PDMap map : _mapSet.values()) {
                for (PDProgram program : map.listPrograms()) {
                    if (program._bankMap.size() == 0) { //FIX somethings
                        program.smartReserve(-1, -1, program.getName());
                    }
                    for (PDBank bank : program.listBanks()) {
                        PDDrumSet drum =  bank.getDrum();
                        if (drum != null) {
                            _drumSet = drum;
                        }
                    }
                }
            }
            if (_drumSet == null) {
                return "";
            }
        }
        return _drumSet.getNote(note);
    }
    
    public void dump() {
        for (PDMap map : _mapSet.values()) {
            System.out.println("MAP " + map.getId() + ":" + map.getName());
            for (PDProgram program : map.listPrograms()) {
                System.out.println("  PROGRAM " + program.getId() + ":" + program.getName());
                if (program._bankMap.size() == 0) { //FIX somethings
                    program.smartReserve(-1, -1, program.getName());
                }
                for (PDBank bank : program.listBanks()) {
                    System.out.println("   BANK " + bank.getBankMSB() +"-" + bank.getBankLSB() + ":" + bank.getName());
                    PDDrumSet drum =  bank.getDrum();
                    if (drum != null) {
                        for (int i = 0; i < 256; ++ i) {
                            String dname = drum.getNote(i);
                            if (dname != null) {
                                System.out.println("    DRUM " + i +    ":" + dname);
                            }
                        }
                    }
                }
            }
        }
    }

    public DoubleIndex<PDProgram> createDatabase() {
        DoubleIndex<PDProgram> ret = new DoubleIndex<PDProgram>();
        for (PDMap map : _mapSet.values()) {
            for (PDProgram program : map.listPrograms()) {
                ret.put(program);
                if (program._bankMap.size() == 0) { //FIX somethings
                    program.smartReserve(-1, -1, program.getName());
                }
            }
        }
        return ret;
    }

    public String simpleGetName(int programNumber) {
        return simpleGetName(programNumber, -1, -1);
    }
    
    DoubleIndex<PDMap> _mapSet = new DoubleIndex();
    DoubleIndex<PDProgram> _database;
    String _moduleName;
    PDDrumSet _drumSet = null;
 
    public PDMap get(String name) {
        return _mapSet.get(name);
    }

    public PDMap smartReserve(String name) {
        PDMap x = get(name);
        if (x == null) {
            x = new PDMap(name);
            _mapSet.put(x);
        }
        return x;
    }

    PDMap _root;
    PDProgram[] _listProgram;
    
    static int _seqOrder = 100;
    int _seq = _seqOrder;

    @Override
    public int getId() {
        return _seq;
    }

    @Override
    public String getName() {
        return _moduleName;
    }
    
    public Collection<PDMap> listMap() {
        return _mapSet.values();
    }
}

