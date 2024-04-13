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
package jp.synthtarou.libs;

import jp.synthtarou.libs.log.MXFileLogger;
import java.util.LinkedList;
import java.util.logging.Level;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXQueue<T> {

    LinkedList<T> _queue;
    boolean _quit;

    public MXQueue() {
        _queue = new LinkedList<T>();
        _quit = false;
    }

    public synchronized void push(T obj) {
        _queue.add(obj);
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return _queue.isEmpty();
    }

    public synchronized T pop() {
        while (true) {
            while (_queue.isEmpty() && !_quit) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            if (!_queue.isEmpty()) {
                T value = _queue.removeFirst();
                return value;
            }
            notifyAll();
            if (_quit) {
                return null;
            }
        }
    }

    public synchronized void quit() {
        _quit = true;
        notifyAll();
    }

    public static void main(String[] args) {
        final MXQueue<Integer> que = new MXQueue<Integer>();
        new MXSafeThread("MXQueue1", () -> {
            while (true) {
                try {
                    Integer value = que.pop();
                    if (value == null) {
                        System.out.println("Thread burn!");
                        que.quit();
                        break;
                    }
                    System.out.println("Thread got " + value);
                    if (value == 100) {
                        System.out.println("Thread bingo!");
                        que.quit();
                        break;
                    }
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException ex) {
                        MXFileLogger.getLogger(MXQueue.class).log(Level.WARNING, ex.getMessage(), ex);
                        break;
                    }
                } catch (RuntimeException ex) {
                    MXFileLogger.getLogger(MXQueue.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }).start();
        new MXSafeThread("MXQuque1-1", () -> {
            for (int i = 0; i <= 100; ++i) {
                que.push(i);
                try {
                    Thread.sleep(60);
                } catch (InterruptedException ex) {
                    MXFileLogger.getLogger(MXQueue.class).log(Level.WARNING, ex.getMessage(), ex);
                    break;
                }
            }
        }).start();

        while (true) {
            Integer value = que.pop();
            if (value == null) {
                System.out.println("Main burn!");
                que.quit();
                break;
            }
            System.out.println("Main got " + value);
            if (value == 100) {
                System.out.println("Main bingo!");
                que.quit();
                break;
            }
            try {
                Thread.sleep(70);
            } catch (InterruptedException ex) {
                MXFileLogger.getLogger(MXQueue.class).log(Level.WARNING, ex.getMessage(), ex);
                break;
            }
        }
    }
}
