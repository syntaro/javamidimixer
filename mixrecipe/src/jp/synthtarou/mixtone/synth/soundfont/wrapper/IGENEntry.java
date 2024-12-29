/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.mixtone.synth.soundfont.wrapper;

import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class IGENEntry {
    static XTGenOperatorMaster _genMaster = null;
    Number[] _listParameter;

    public IGENEntry(XTFile sfz, int igenFrom, int igenCount) {
        if (_genMaster == null) {
            _genMaster = XTGenOperatorMaster.getMaster();
        }
        
        _listParameter = new Number[XTGenOperatorMaster.endOper + 1];
        SFZElement.SFZElement_igen igen = sfz._igen;

        for (int i = 0; i < igenCount; ++ i) {
            XTRow row = igen.get(i + igenFrom);

            Number operator = row.numberColumn(SFZElement.IGEN_GENOPER, null);
            Number amount= row.numberColumn(SFZElement.IGEN_GENAMOUNT, null);
            if (operator == null || amount == null) {
                continue;
            }
            _listParameter[operator.intValue()] = amount;
        }
    }
    
    public Integer getGenerator(int param) {
        Number n = _listParameter[param];
        return (n == null) ? null : n.intValue();
    }

    public Integer getSampleId() {
        return getGenerator(XTGenOperatorMaster.sampleID);
    }

    public Integer getSampleModes() {
        return getGenerator(XTGenOperatorMaster.sampleModes);
    }

    public Integer getCourseTune() {
        return getGenerator(XTGenOperatorMaster.coarseTune);
    }

    public Integer getFineTune() {
        return getGenerator(XTGenOperatorMaster.fineTune);
    }

    public Integer getScaleTuning() {
        return getGenerator(XTGenOperatorMaster.scaleTuning);
    }

    public Integer getOverridingRootKey() {
        return getGenerator(XTGenOperatorMaster.overridingRootKey);
    }

    public Integer getPan() {
        return getGenerator(XTGenOperatorMaster.pan);
    }
    
    public boolean inKeyRange(int key) {
        Integer x = getGenerator(XTGenOperatorMaster.keyRange);
        if (x == null) {
            return false;
        }
        int lo  = (x >> 8) & 0xff;
        int hi = (x & 0xff);
        if (lo > hi) {
            int z = lo;
            lo = hi;
            hi = z;
        }
        return (lo <= key) && (key <= hi);
    }

    public boolean inVelocityRange(int velocity) {
        Integer x = getGenerator(XTGenOperatorMaster.velRange);
        if (x == null) {
            return false;
        }
        int lo  = (x >> 8) & 0xff;
        int hi = (x & 0xff);
        if (lo > hi) {
            int z = lo;
            lo = hi;
            hi = z;
        }
        return (lo <= velocity) && (velocity <= hi);
    }
}
