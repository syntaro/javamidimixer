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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import javax.swing.plaf.ColorUIResource;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SimpleRGBCanvas {
    public SimpleRGBCanvas() {
        this(100, 100);
    }

    public SimpleRGBCanvas(int width, int height) {
        prepare(width, height);
    }
    
    BufferedImage _image;
    Graphics _graphics;
    WritableRaster _raster;
    DataBufferByte _bitmapData;
    int _width;
    int _height;
    boolean _prepareReseted;

    public void dispose() {
        if (_graphics != null) {
            _graphics.dispose();
        }
        _image = null;
        _graphics = null;
        _raster = null;
    }
    
    public boolean isReady() {
        return (_bitmapData != null);
    }
    
    public void prepare(int width, int height) {
        if (_image != null) {
            if (_width != width || _height != height) {
                dispose();
            }
        }
        
        if (_image == null) {
            _image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            _graphics = _image.getGraphics();
            _raster = _image.getRaster();
            _width = width;
            _height = height;
            _bitmapData = (DataBufferByte)_raster.getDataBuffer();
            _prepareReseted = true;
        }
        else {
            _prepareReseted = false;
        }
    }

    public void setPixel(int x, int y, byte[] bgrData) {
        byte[] data = _bitmapData.getData();
        if (x < 0 || x >= _width || y < 0 || y >= _height) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        int address = (x * 3) + (y * _width) * 3;
        if (address < 0 || address + 2 >= data.length) {
            return;
        }

        data[address] = bgrData[0];
        data[address + 1] = bgrData[1];
        data[address + 2] = bgrData[2];
    }

    public byte[] _colorBuff = new byte[3];

    public synchronized void setPixel(int x, int y, int b, int g, int r) {
        _colorBuff[0] = (byte) b;
        _colorBuff[1] = (byte) g;
        _colorBuff[2] = (byte) r;
        setPixel(x, y, _colorBuff);
    }

    public byte[] getPixel(int x, int y, byte[] bgrData) {
        byte[] data = _bitmapData.getData();
        
        int address = (x * 3) + (y * _width) * 3;
        if (address < 0 || address + 2 >= data.length) {
            return null;
        }
        
        if (bgrData == null || bgrData.length != 3) {
            bgrData = new byte[3];
        }
        
        bgrData[0] = data[address];
        bgrData[1] = data[address + 1];
        bgrData[2] = data[address + 2];
        return  bgrData;
    }
    
    public void line(int x, int y, int x2, int y2, byte[] bgrData) {
        if (y != y2) {
           throw  new IllegalArgumentException("NotSupported Yet");
        }
        
        for (int sx = x; sx < x2; ++ sx) {
            setPixel(sx, y, bgrData);
        }
    }
    
    public static byte[] colorToBgr(Color col, byte[] bgrData) {
        if (bgrData == null || bgrData.length != 3) {
            bgrData = new byte[3];
        }
        bgrData[0] = (byte)col.getBlue();
        bgrData[1] = (byte)col.getGreen();
        bgrData[2] = (byte)col.getRed();
        return bgrData;
    }
    
    public static Color bgrToColor(byte[] bgrData) {
        return new ColorUIResource(bgrData[2], bgrData[1], bgrData[0]);
    }
}
