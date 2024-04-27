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
package jp.synthtarou.midimixer.mx13patch;

import java.util.ArrayList;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13Process extends MXReceiver<MX13View> {

    MX13View _view;
    ArrayList<MX13From> _list;

    public MX13Process(boolean isInput) {
        _list = new ArrayList<>();
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
            MX13From from = new MX13From(this, i);
            _list.add(from);
        }
        resetSetting();
        _view = new MX13View(this);
    }

    public void writeJsonTree(MXJsonValue tree) {
        MXJsonValue.HelperForStructure root = tree.new HelperForStructure();
        
        MXJsonValue.HelperForArray listFrom = null;
        MXJsonValue.HelperForArray listTo = null;
        MXJsonValue.HelperForArray listFilter = null;

        MXJsonValue.HelperForStructure currentFrom = null;
        MXJsonValue.HelperForStructure currentTo = null;
        MXJsonValue.HelperForStructure currentFilter = null;

        for (MX13From from : _list) {
            if (from.isItemChecked() == false) {
                continue;
            }
            currentFrom = null;
            for (MX13To to : from._listTo) {
                if(to.isItemChecked() == false) {
                    continue;
                }
                currentTo = null;
                for (MX13SignalType type : to._list) {
                    if (type.isItemChecked() == false) {
                        continue;
                    }

                    if (listFrom == null) {
                        listFrom = root.addFollowingArray("From");
                        currentFrom = null;
                    }
                    if (currentFrom == null) {
                        currentFrom = listFrom.addFollowingStructure();
                        currentFrom.setFollowingNumber("Port", from._port);
                        listTo = null;
                    }
                    if (listTo == null)     {
                        listTo = currentFrom.addFollowingArray("To");
                        currentTo = null;
                    }
                    if (currentTo == null) {
                        currentTo = listTo.addFollowingStructure();
                        currentTo.setFollowingNumber("Port", to._port);
                        listFilter = null;
                    }
                    if (listFilter == null)     {
                        listFilter = currentTo.addFollowingArray("Filter");
                        currentTo = null;
                    }
                    listFilter.addFollowingText(type._name);
                }
            }
        }
    }

    public void readJsonTree(MXJsonValue tree) {
        MXJsonValue.HelperForStructure root = tree.new HelperForStructure();
        
        MXJsonValue.HelperForArray listFrom = null;
        MXJsonValue.HelperForArray listTo = null;
        MXJsonValue.HelperForArray listFilter = null;

        MXJsonValue.HelperForStructure currentFrom = null;
        MXJsonValue.HelperForStructure currentTo = null;
        MXJsonValue.HelperForStructure currentFilter = null;
        
        clearSetting();
        
        listFrom = root.getFollowingArray("From");
       
        for (int from = 0; from < listFrom.count(); ++ from) {
            currentFrom = listFrom.getFollowingStructure(from);
            int fromPort = currentFrom.getFollowingInt("Port", -1);
            if (fromPort < 0) {
                continue;
            }
            listTo = currentFrom.getFollowingArray("To");
            for (int to = 0; to < listTo.count(); ++ to) {
                currentTo = listTo.getFollowingStructure(to);
                int toPort = currentTo.getFollowingInt("Port", -1);
                if (toPort < 0) {
                    continue;
                }
                
                listFilter = currentTo.getFollowingArray("Filter");
                
                for (int filter = 0; filter < listFilter.count(); ++ filter) {
                    String filterName = listFilter.getFollowingText(filter,null);
                    
                    MX13From theFrom = _list.get(fromPort);
                    theFrom.setItemChecked(true);
                    MX13To theTo = theFrom._listTo.get(toPort);
                    theTo.setItemChecked(true);
                    int theFilter = MX13SignalType.fromName(filterName);
                    if (theFilter >= 0) {
                        theTo._list.get(theFilter).setItemChecked(true);
                    }
                }
            }
        }
        setInformation();
    }

    public void resetSetting() {
        for (MX13From from : _list) {
            from.setItemChecked(from._port == 0 ? true : false);
            
            for (MX13To to : from._listTo) {
                to.setItemChecked(from._port == to._port ? true : false);
                to.resetSkip();;
            }
        }
        setInformation();

    }
    
    public void clearSetting() {
        for (MX13From from : _list) {
            from.setItemChecked(false);
            
            for (MX13To to : from._listTo) {
                to.setItemChecked(false);
                
                for (MX13SignalType type : to._list) {
                    type.setItemChecked(false);
                    
                }
            }
        }
    }
    
    @Override
    public String getReceiverName() {
        return "Patch";
    }

    @Override
    public MX13View getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MX13From from = _list.get(message.getPort());
        
        if (from.isItemChecked() == false) {
            return;
        }
        
        for (MX13To to : from._listTo) {
            MXMessage portMessage = null;
            
            if (to.isItemChecked() == false) {
                continue;
            }

            if (to.accept(message)) {
                if (portMessage == null) {
                    if (from._port == to._port) {
                        portMessage = message;
                    }
                    else {
                        portMessage = MXMessageFactory.fromClone(message);
                        portMessage.setPort(to._port);
                    }
                    sendToNext(message);
                }
            }
        }
    }

    public void setInformation() {
        if (_view == null) {
            return;
        }
        StringBuffer result = new StringBuffer();
        boolean reset;
        for (MX13From from : _list) {
            reset = true;
            if (from.isItemChecked() == false) {
                continue;
            }
            int countTo = 0;
            for (MX13To to : from._listTo) {
                if (to.isItemChecked() == false) {
                    continue;
                }
                countTo++;
            }
            boolean addedTo = false;
            for (MX13To to : from._listTo) {
                if (to.isItemChecked() == false) {
                    continue;
                }
                ArrayList<String> listTypes = new ArrayList<>();
                int countType = 0;
                for (MX13SignalType type : to._list) {
                    if (type.isItemChecked() == false) {
                        continue;
                    }
                    listTypes.add(type.itemToString());
                    countType++;
                }

                if (countType > 0) {
                    StringBuffer temp = new StringBuffer();
                    if (countTo == 1 && from._port == to._port) {
                        if (reset) {
                            temp.append(from.itemToString());
                        }
                    } else {
                        if (reset) {
                            temp.append(from.itemToString() + "->");
                        }
                        if (!addedTo) {
                            temp.append("{");
                        }
                        temp.append(to.itemToString());
                        addedTo = true;
                    }

                    if (countType == 1 && to._list.get(MX13SignalType.TYPE_ALL).isItemChecked()) {
                    } else {
                        temp.append(listTypes);
                    }
                    reset = false;
                    if (result.isEmpty() == false) {
                        result.append(", ");
                    }
                    result.append(temp.toString());
                }
            }
                if (addedTo) {
                    result.append("}");
                }
        }
        _view.setInformation(result.toString());
    }
}
