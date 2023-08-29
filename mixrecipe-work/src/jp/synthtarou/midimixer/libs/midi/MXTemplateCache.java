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
package jp.synthtarou.midimixer.libs.midi;

import java.util.LinkedList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXTemplateCache {
    private static final MXTemplateCache _instance = new MXTemplateCache();
   
    public static MXTemplateCache getInstance() {
        return _instance;
    }

    public MXTemplate[] _cacheTemplate = new MXTemplate[256];
    
    public MXTemplateCache() {
    }
    
    public synchronized MXTemplate fromDword(int dword) {
        if (dword == 0) {
            return ZERO;
        }

        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;

        if (status >= 0x80 && status <= 0xef) {
            int command = status & 0xf0;

            if (_cacheTemplate[command] == null) {
                _cacheTemplate[command] = newCanonicalDword(dword);
            }
            if (_cacheTemplate[command].canReuseDword(dword)) {
                return _cacheTemplate[command];
            }
            else {
                System.out.println("ERROR 1 " + _cacheTemplate[command]);
            }
        }
        
        if (status >= 0xf1 && status <= 0xfe) {
            int command = status;
            
            if (_cacheTemplate[command] == null) {
                _cacheTemplate[command] = newCanonicalDword(dword);
            }
            if (_cacheTemplate[command].canReuseDword(dword)) {
                return _cacheTemplate[command];
            } 
            else {
                System.out.println("ERROR 2 " + _cacheTemplate[command]);
            }
        }
        
        int[] template = new int[3];
        template[0] = status;
        template[1] = data1;
        template[2] = data2;
        return new MXTemplate(template);
    }
    
    static final MXTemplate ZERO = new MXTemplate(new int[]{ 0, 0, 0 });
    
    LinkedList<MXTemplate> _cachedTemplate = new LinkedList();
    
    public synchronized MXTemplate fromTemplate(int[] template) {
        if (template == null || template.length == 0 || template[0] == 0) {
            return null;
        }

        for (MXTemplate t : _cachedTemplate) {
            if (t.canReuseTemplate(template)) {
                return t;
            }
        }

        MXTemplate t = new MXTemplate(template);
        _cachedTemplate.add(t);
        return t;
    }
 
    public synchronized MXTemplate fromBinary(byte[] data) {
        if (data == null || data.length == 0 || data[0] == 0) {
            return null;
        }

        if (data.length == 3) {
            int status = data[0] & 0xff;
            int data1 = data[1] & 0xff;
            int data2 = data[2] & 0xff;
            
            if (status >= 0x80 && status <= 0xef) {
                int dword = (status << 16) | (data1 << 8) | data2;
                return fromDword(dword);
            }
            if (status >= 0xf1 && status <= 0xfe) {
                int dword = (status << 16) | (data1 << 8) | data2;
                return fromDword(dword);
            }
        }

        int c = data[0] & 0xff;
        
        boolean seekCache = true;
        
        if (c == 0xff && data.length >= 100) {
            seekCache = false;
        }
        if (c == 0xf0 && data.length >= 100) {
            seekCache = false;
        }

        if (seekCache) {
            for (MXTemplate t : _cachedTemplate) {
                if (t.canReuseBinary(data)) {
                    return t;
                }
            }            
        }
        
        int[] template= new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
        
        MXTemplate t = new MXTemplate(template);
        if (seekCache) {
            _cachedTemplate.add(t);
        }
        return t;
    }
    
    protected MXTemplate newCanonicalDword(int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;

        if (status == 0xf0) {
            return null;
        }
        else if (status >= 0xf1 && status <= 0xff) {
            if (status == MXMidi.STATUS_MIDITIMECODE) {
                data1 = MXTemplate.DTEXT_VL;
            }
            if (status == MXMidi.STATUS_SONGPOSITION) {
                data1= MXTemplate.DTEXT_VL;
                data2 = MXTemplate.DTEXT_VH;
            }
            if (status == MXMidi.STATUS_SONGSELECT) {
                data1 = MXTemplate.DTEXT_VL;
            }
        }
        else if (status >= 0x80 && status <= 0xef) {
            status = status & 0xf0;
            switch (status) {
                case MXMidi.COMMAND_PROGRAMCHANGE:
                    data1 = MXTemplate.DTEXT_GL;
                    break;
                case MXMidi.COMMAND_CONTROLCHANGE:
                    data1 = MXTemplate.DTEXT_GL;
                    data2 = MXTemplate.DTEXT_VL;
                    break;
                case MXMidi.COMMAND_NOTEON:
                case MXMidi.COMMAND_NOTEOFF:
                case MXMidi.COMMAND_POLYPRESSURE:
                    data1 = MXTemplate.DTEXT_GL;
                    data2 = MXTemplate.DTEXT_VL;
                    break;
                case MXMidi.COMMAND_PITCHWHEEL:
                    data1 = MXTemplate.DTEXT_VL;
                    data2 = MXTemplate.DTEXT_VH;
                    break;
                case MXMidi.COMMAND_CHANNELPRESSURE:
                    data1 = MXTemplate.DTEXT_VL;
                    break;
                default:
                    break;
            }
        }
        
        int[] template = new int[3];
        template[0] = status;
        template[1] = data1;
        template[2] = data2;
        return new MXTemplate(template);
    }
}
