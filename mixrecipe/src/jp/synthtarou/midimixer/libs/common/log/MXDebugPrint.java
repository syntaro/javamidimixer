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
package jp.synthtarou.midimixer.libs.common.log;


import java.io.PrintStream;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDebugPrint {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXDebugPrint.class);
    private PrintStream _output = System.out;
    
    public static void dumpEnv() {
        if (_debug.isDebug()) {
            Properties props = System.getProperties();
            Enumeration keys = props.keys();
            _debug.println("System.getProperties = ");
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = props.getProperty(key);
                _debug.println("    " + key + " = " + value);
            }

            Map<String, String> envs = System.getenv();
            _debug.println("System.getenv = ");
            for (String key : envs.keySet()) {
                String value = (String)envs.get(key);
                _debug.println("    " + key + " = " + value);
            }
        }

    }
    
    public static void main(String[] args) {
        _debug.switchOn();
        _debug.printStackTrace();

        dumpEnv();
        
        test();
    }
    
    public static void test() {
        _debug.printStackTrace(new Throwable());
    }
    
    protected String _name;
    
    public MXDebugPrint(Class cls) {
        _name = cls.getName();
        if (_name != null) {
            int x = _name.lastIndexOf('.');
            if (x >= 0) {
                _name = _name.substring(x+1);
            }
            if (_target == null) {
                //when 1st initialize, i happens
                return;
            }
            if (_target.contains(_name)) {
                _switch = true;
            }
        }
    }
    
    public boolean isDebug() {
        return (_globalSwitch || _switch);
    }
    
    public void println(String text) {
        if (isDebug()) {
            _output.println(_name + ": " + text);
        }
    }

    public void print(String text) {
        if (isDebug()) {
            _output.print(_name + ": " + text);
        }
    }

    public void println() {
        if (isDebug()) {
            _output.println(_name + ": ");
        }
    }

    public void printStackTrace() {
        if (isDebug()) {
            Throwable th= new Throwable();
            StackTraceElement[] elems = th.getStackTrace();
            _output.println("print stack trace = ");
            for (int i = 1 ; i < elems.length; ++ i) {
                StackTraceElement x = elems[i];
                _output.println("    "  + x);
            }
        }
    }

    public void printStackTrace(Throwable th) {
        if (isDebug()) {
            StackTraceElement[] elems = th.getStackTrace();
            _output.println("print stack trace = " + th.getClass() + ":" + th.getMessage());
            for (int i = 0 ; i < elems.length; ++ i) {
                StackTraceElement x = elems[i];
                _output.println("    "  + x);
            }
        }
    }

    public void printStackTrace(Throwable th, String text) {
        if (isDebug()) {
            println(text);
            printStackTrace(th);
        }
    }

    private boolean _switch = false;
    private static boolean _globalSwitch = false;
    private static HashSet<String> _target = new HashSet();

    public static void initDebugLine(String[] args) {
        String debug1 = System.getenv("DEBUG");
        String debug2 = System.getProperty("DEBUG");
        
        Comparator<String> ignoreCase = new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        };
        TreeSet<String> test = new TreeSet(ignoreCase);
        test.add("true");
        test.add("yes");
        test.add("on");
        test.add("1");
        if (debug1 != null && test.contains(debug1)) {
            _globalSwitch = true;
        } else if (debug2 != null && test.contains(debug2)) {
            _globalSwitch = true;
        } else {
            if (debug1 != null) {
                _target.add(debug1);
            }
            if (debug2 != null) {
                _target.add(debug2);
            }
        }
        for (int i = 0; i < args.length; ++ i) {
            if (args[i].equalsIgnoreCase("--debug")) {
                _globalSwitch = true;
            }
        }
    }
   
    public static void parseArgs(String[] args) {
        for (String text : args) {
            if (text.startsWith("--debug=")) {
                _target.add(text.substring(8));
            }else if (text.equals("--degbug")) {
                _globalSwitch = true;
            }
        }
    }
    
    public static void globalSwitchOn() {
        _globalSwitch = true;
    }

    public static void globalSwitchOff() {
        _globalSwitch = false;
    }

    public void switchOn() {
        _switch = true;
    }

    public void switchOff() {
        _switch = false;
    }
}
