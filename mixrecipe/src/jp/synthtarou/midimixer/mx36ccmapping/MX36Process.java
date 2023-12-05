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
package jp.synthtarou.midimixer.mx36ccmapping;

import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.mx30surface.MGStatus;
import jp.synthtarou.midimixer.mx30surface.MX32Relation;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver implements MXSettingTarget {

    private MX36View _view;
    MXSetting _setting;
    MX36StatusList _list = MX36StatusList._instance;

    public MX36Process() {
        _setting = new MXSetting("CCMapping");
        _setting.setTarget(this);
        _view = new MX36View(this);
    }

    public void processStatus(MGStatus status) {
    }

    @Override
    public String getReceiverName() {
        return "CCMapping (Not Worked yet)";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MX32Relation relation = message.getRelation();
        if (relation != null) {
            while (relation != null) {
                for (MGStatus mxstatus : relation._listStatus) {
                    MX36Status found = null;
                    if (mxstatus._uiType == MGStatus.TYPE_DRUMPAD) {
                        continue;
                    }
                    for (MX36StatusList.Folder folder : _list._listFolder) {
                        found = folder.findBySurfacePosition(mxstatus);
                        if (found != null) {
                            break;
                        }
                    }
                    if (found == null) {
                        MX36Status newStatus = MX36Status.fromMGStatus(mxstatus);
                        _list.setFolder(_list._autoDetectedFolder, newStatus);
                    } else {
                        updateSurfaceValue(found, relation._base.getValue());
                    }
                }
                sendToNext(relation._base);
                relation = relation._chanMessage;
            }
        }else {
            sendToNext(message);
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
    }

    public void updateSurfaceValue(MX36Status status, int value) {
        updateSurfaceValue(status, status._surfaceValueRange.changeValue(value));
    }

    public void updateSurfaceValue(MX36Status status, RangedValue value) {
        if (status._surfaceValueRange.equals(value)) {
            return;
        }
        status._surfaceValueRange = value;
        updateOutputValue(status, value.changeRange(status._outValueRange._min, status._outValueRange._max));
        _list.reloadStatusOfTree(status);
    }

    public void updateOutputValue(MX36Status status, int value) {
        updateOutputValue(status, status._outValueRange.changeValue(value));
    }

    public void updateOutputValue(MX36Status status, RangedValue value) {
        if (status._outValueRange.equals(value)) {
            return;
        }
        status._outValueRange = value;
        _view._detailPanel.updateSliderByStatus();
        _list.reloadStatusOfTree(status);
        raiseSignal(status);
    }

    public void raiseSignal(MX36Status status) {
        MXMessage message = status.createOutMessage();
        if (message == null) {
            System.out.println("raiseSignal X(" + status + ")");
            return;
        }
        System.out.println("raiseSignel O(" + status + " ) message = " + message);
    }
}
