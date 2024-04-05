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
package jp.synthtarou.libs.json;

import java.io.File;

/**
 *
 * @author Syntarou YOSHIDA
 */
public interface MXJsonSupport {

    /**
     * jsonファイルを読み取る
     * @param file ファイルを指定（デフォルトの場合null)
     * @return 読み取った場合 true
     */
    public boolean readJSonfile(File file);

    /**
     * jsonファイルを書き込む
     * @param file ファイルを指定（デフォルトの場合null)
     * @return 書き込んだ場合true
     */
    public boolean writeJsonFile(File file);


    /**
     *　設定をリセットする
     */
    public void resetSetting();
}
