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
package jp.synthtarou.midimixer.libs.common;

import java.util.LinkedList;
import jp.synthtarou.midimixer.MXThreadList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXQueue1<T> {

    LinkedList<T> _queue;
    boolean _quit;
    
    public MXQueue1() {
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
        while(true) {
            while (_queue.isEmpty() && !_quit) {
                try {
                    wait(1000);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!_queue.isEmpty()) {
                T value = _queue.removeFirst();
                return value;
            }
            if (_quit) {
                return null;
            }
        }
    }
    
    public synchronized void quit() {
        try {
            _quit = true;
            notifyAll();
        }catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        final MXQueue1<Integer> que = new MXQueue1<Integer>();
        MXThreadList.newThread("MXQueue1", new Runnable() {
            public void run() {
                while(true) {
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
                            ex.printStackTrace();
                        }
                    }catch(Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
        MXThreadList.newThread("MXQuque1-1", new Runnable() {
            public void run() {
                for (int i = 0; i <= 100; ++ i) {
                    que.push(i);
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
        
        while(true) {
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
                ex.printStackTrace();;
            }
        }
    }
}
