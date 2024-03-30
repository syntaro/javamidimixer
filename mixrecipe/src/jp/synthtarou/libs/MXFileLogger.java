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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.*;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXFileLogger {
    static MXFileLogger _instance = new MXFileLogger();
    
    public static void main(String[] args) {
        MXFileLogger.getLogger(MXFileLogger.class).warning("**ワーニング");
        MXFileLogger.getLogger(MXFileLogger.class).info("**インフォ");
        MXFileLogger.getLogger(MXFileLogger.class).severe("**エラー");
        Throwable e = new RuntimeException("runtime error");
        MXFileLogger.getLogger(MXFileLogger.class).log(Level.INFO, e.getMessage(), e);
        MXFileLogger.getLogger(MXFileLogger.class).info("**COMPLETE");
    }
    
    public static Logger getLogger(Class clz) {
        return _instance.get(clz.getName());
    }

    public static Logger getLogger(String name) {
        return _instance.get(name);
    }

    File _logFile;
    Handler _fileHandler;
    Handler _stdoutHandler;

    public MXFileLogger() {
        File base = MXUtil.getAppBaseDirectory();
        File logDir = new File(base, "log");
        logDir.mkdir();
        _logFile = new File(logDir, "MixRecipe.log");

        Logger logger = Logger.getLogger("MixRecipe");

        try {
            _fileHandler = new FileHandler(_logFile.getPath());
        }catch(IOException e){
            System.err.println("can't make logfile " + _logFile);
            _fileHandler = null;
            
        }
        _stdoutHandler = new StdOutHandler();

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %5$s %6$s (%2$s)%n");

        System.out.println("Loggin: " + _logFile);
    }
    
    HashMap<String, Logger> _cache = new HashMap();

    public synchronized Logger get(String name) {
        Logger alredy = _cache.get(name);
        if (alredy != null) {
            return alredy;
        }
        Logger newLogger = Logger.getLogger(name);
        
        if (_fileHandler != null) {
            newLogger.addHandler(_fileHandler);
            Formatter formatter1 = new SimpleFormatter();
            _fileHandler.setFormatter(formatter1);
        }
        /* stderr -> X stdout -> O */
        if (_stdoutHandler != null) {
            newLogger.addHandler(_stdoutHandler);
            Formatter formatter2 = new SimpleFormatter();
            _stdoutHandler.setFormatter(formatter2);

            newLogger.setUseParentHandlers(false);
        }
        _cache.put(name, newLogger);
        return newLogger;
    }

    static class StdOutHandler extends StreamHandler {
        public StdOutHandler() {   
            setOutputStream(System.out);
        }
        
        @Override
        public void publish(LogRecord record) {
            super.publish(record);
            super.flush();
        }
    }
}
