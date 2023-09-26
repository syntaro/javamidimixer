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

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXLog {

    public static final Logger _logger;
    
    public static void main(String[] args) {
        MXLog._logger.warning("**ワーニング");
        MXLog._logger.info("**インフォ");
        MXLog._logger.severe("**エラー");
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

    static {
        Logger logger = Logger.getLogger("MixRecipe");

        try {
            /* make file */
            File base = MXUtil.getAppBaseDirectory();
            File logDir = new File(base, "log");
            logDir.mkdir();
            File logFile = new File(logDir, "MixRecipe.log");

            Handler handler1 = new FileHandler(logFile.getPath());
            logger.addHandler(handler1);

            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %5$s %6$s (%2$s)%n");

            Formatter formatter1 = new SimpleFormatter();
            handler1.setFormatter(formatter1);

            /* stderr -> X stdout -> O */
            Handler handler2 = new StdOutHandler();
            logger.addHandler(handler2);
            logger.setUseParentHandlers(false);

            Formatter formatter2 = new SimpleFormatter();
            handler2.setFormatter(formatter2);



        } catch (IOException e) {

        }
        _logger = logger;
    }
}
