/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.listmodel;

import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class TheConsole {
    JList<String> _attachdList;
    DefaultListModel<String> _listModel;
    ArrayList<String> _pushQueue;
    long _lastPushed;
    long _lastPopped;
    Thread _consoleThread;
    
    public TheConsole() {
        _listModel = new DefaultListModel<>();
        _attachdList = null;
        _pushQueue = new ArrayList<>();
        _lastPushed = -1;
    }

    public void attach(JList<String> attach) {
        _attachdList = attach;
        SwingUtilities.invokeLater(() -> {
            _attachdList.setModel(_listModel);
        });
    }
    
    public synchronized void add(String text) {
        _lastPushed = System.currentTimeMillis();
        _pushQueue.add(text);
        if(_consoleThread == null || _consoleThread.isAlive() == false) {
            _consoleThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doConsoleThread();
                }
            });
            _consoleThread.setDaemon(true);
            _consoleThread.start();
            try {
                while (_consoleThread.isAlive() == false) {
                    Thread.sleep(100);
                }
            }catch(InterruptedException ex) {
                
            }
        }
    }

    protected void doConsoleThread() {
        while(true) {
            try {
                processQueue();
                Thread.sleep(500);
            }catch(Throwable ex) {
                
            }
        }
    }

    protected void processQueue() {
        ArrayList<String> pop;
        _lastPopped = System.currentTimeMillis();
        synchronized (this) {
            if (_pushQueue.isEmpty()) {
                return;
            }
            pop = _pushQueue;
            _pushQueue = new ArrayList<>();
        }
        SwingUtilities.invokeLater(() -> {
            for (String next : pop) {
                _listModel.addElement(next);
            }
        });
    }
}
