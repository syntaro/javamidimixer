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
package jp.synthtarou.libs.navigator.legacy;

import javax.swing.JPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public interface INavigator<T> {
    public static String DEFAULT_TITLE = "Plesae Select";
    public static int TYPE_VIEWER = 1;
    public static int TYPE_SELECTOR = 2;
    public static int TYPE_EDITOR = 3;

    public int getNavigatorType();
    
    public static int RETURN_STATUS_NOTSET = -1;
    public static int RETURN_STATUS_APPROVED = 5;
    public static int RETURN_STATUS_CANCELED = 6;
    public static int RETURN_STATUS_REMOVED = 7;

    public int getReturnStatus();

    public T getReturnValue();

    public boolean isNavigatorRemovable();
    public boolean validateWithNavigator(T result);
    
    public JPanel getNavigatorPanel();
}
