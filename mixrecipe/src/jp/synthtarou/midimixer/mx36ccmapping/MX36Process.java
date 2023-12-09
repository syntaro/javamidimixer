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
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordion;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordionFocusListener;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver implements MXSettingTarget {

    private MX36View _view;
    MXSetting _setting;
    MX36FolderList _list;

    public MX36Process() {
        _setting = new MXSetting("CCMapping");
        _setting.setTarget(this);
        _list = new MX36FolderList();
        _list._focus.setListener(new MXAccordionFocusListener() {
            @Override
            public void accordionFocus(MXAccordion accordion, JPanel panel, boolean flag) {
                if (panel instanceof  MX36StatusPanel) {
                    MX36StatusPanel panel36 = (MX36StatusPanel)panel;
                    MX36Status status = panel36.getStatus();
                    _view.focusStatus(status);
                }
            }
        });
        _view = new MX36View(this, _list);
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
        MGStatus[] result = message._mx30result;
        if (result != null && result.length > 0) {
            for (MGStatus status : result) {
                if (status._uiType == MGStatus.TYPE_DRUMPAD) {
                    continue;
                }
                MX36Status status2 = null;
                for (MX36Folder folder : _list._listFolder) 
                {
                    for (MX36Status seek : folder._list) {
                        if (seek._surfacePort == status._port
                         && seek._surfaceRow == status._row
                         && seek._surfaceColumn == status._column
                         && seek._surfaceUIType == status._uiType) {
                            status2 = seek;
                        }
                    }
                }
                if(status2 == null) {                    
                    MX36Folder folder2 = _list._autodetectedFolder;
                    status2 =  MX36Status.fromMGStatus(folder2, status);
                    folder2.insertSorted(status2);
                    folder2.refill(status2);
                }
                else {
                    MX36Folder folder2 = status2._folder;
                    updateSurfaceValue(status2, status.getValue());
                    folder2.refill(status2);
                }
            }
        }
        sendToNext(message);
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
        status._folder.refill(status);
        _view.refreshList();
    }

    public void raiseSignal(MX36Status status) {
        MXMessage message = status.createOutMessage();
        if (message == null) {
            //System.out.println("raiseSignal X(" + status + ")");
            return;
        }
        System.out.println("raiseSignel O(" + status + " ) message = " + message);
    }
}
