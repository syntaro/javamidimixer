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

import java.util.logging.Level;
import javax.swing.JPanel;
import jp.synthtarou.libs.MXQueue;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXMain;
import static jp.synthtarou.midimixer.MXMain._capture;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXReceiver<T extends JPanel> {

    public abstract String getReceiverName();

    public abstract T getReceiverView();

    private MXReceiver _nextReceiver;

    public final MXReceiver getNextReceiver() {
        return _nextReceiver;
    }

    public void setNextReceiver(MXReceiver next) {
        _nextReceiver = next;
    }

    public abstract void processMXMessage(MXMessage message);

    public void sendToNext(MXMessage message) {
        if (_nextReceiver != null) {
            messageDispatch(message, _nextReceiver);
        } else {
            MXFileLogger.getLogger(MXReceiver.class).warning("receiver not set " + message);
        }
    }

    private boolean _usingThis = true;

    public boolean isUsingThisRecipe() {
        return _usingThis;
    }

    public void setUsingThisRecipe(boolean usingThis) {
        _usingThis = usingThis;
    }

    static MXQueue<MessageQueueElement> _messageQueue = new MXQueue<>();
    static MXSafeThread _messageThread = null;

    static {
        _messageThread
                = new MXSafeThread("MessageProcess", new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            MessageQueueElement e = _messageQueue.pop();
                            if (e == null) {
                                break;
                            }
                            MXMessage message = e._message;
                            MXReceiver receiver = e._receiver;
                            messageDispatchBody(message, receiver);
                        }
                    }
                });
        _messageThread.setPriority(Thread.MAX_PRIORITY);
        _messageThread.setDaemon(true);
        _messageThread.start();
    }

    public static void waitQueueBeenEmpty() {
        try {
            while (true) {
                Thread.sleep(1);
                synchronized (MXTiming.mutex) {
                    if (_messageQueue.isEmpty()) {
                        return;
                    }
                }
            }
        } catch (InterruptedException ex) {
        }
    }

    static class MessageQueueElement {

        public MessageQueueElement(MXMessage message, MXReceiver receiver) {
            _message = message;
            _receiver = receiver;
        }

        MXMessage _message;
        MXReceiver _receiver;
    }

    public static void messageDispatch(MXMessage message, MXReceiver receiver) {
        if (Thread.currentThread() == _messageThread) {
            messageDispatchBody(message, receiver);
        } else {
            _messageQueue.push(new MessageQueueElement(message, receiver));
        }
    }

    static void messageDispatchBody(MXMessage message, MXReceiver receiver) {
        try {
            synchronized (MXTiming.mutex) {
                try {
                    if (message._timing == null) {
                        message._timing = new MXTiming();
                    }
                    if (receiver == MXMain.getMain().getInputProcess()) {
                        MXMain.addInsideInput(message);
                        if (_capture != null) {
                            _capture.processMXMessage(message);
                        }
                    }
                    if (receiver != null) {
                        receiver.processMXMessage(message);
                    }
                } catch (RuntimeException ex) {
                    MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMain.class).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
