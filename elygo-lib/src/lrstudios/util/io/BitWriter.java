/*
 * This file is part of Elygo-lib.
 * Copyright (C) 2012   Emmanuel Mathis [emmanuel *at* lr-studios.net]
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

package lrstudios.util.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Writes bits to an OutputStream.
 */
public class BitWriter {
    private OutputStream _stream;
    private byte[] _buffer;
    private int _bufferPos;
    private int _nextBit;


    public BitWriter(OutputStream stream) {
        _stream = stream;
        _buffer = new byte[128];
        _bufferPos = 0;
        _nextBit = 0x1;
    }


    public void write(boolean bit) throws IOException {
        if (bit)
            _buffer[_bufferPos] |= _nextBit;

        if (_nextBit == 0x80) {
            _nextBit = 0x1;
            _bufferPos++;
            if (_bufferPos == _buffer.length)
                flush();
            _buffer[_bufferPos] = 0;
        } else {
            _nextBit <<= 1;
        }
    }

    /**
     * Writes the specified bits (starting from the highest bits) to the underlying OutputStream.
     *
     * @param bits        The integer containing the bits to write.
     * @param bitsToWrite The number of bits to write.
     * @throws IOException An error occured during writing.
     */
    public void write(long bits, int bitsToWrite) throws IOException {
        if (bitsToWrite > 64)
            bitsToWrite = 64;

        for (int i = bitsToWrite - 1; i >= 0; i--)
            write((bits & (1 << i)) != 0);
    }

    public void writeLittleEndian(long bits, int bitsToWrite) throws IOException {
        if (bitsToWrite > 64)
            bitsToWrite = 64;

        for (int i = 0; i < bitsToWrite; i++)
            write((bits & (1 << i)) != 0);
    }


    /**
     * Flushes the buffer (necessary after writing all bits).
     *
     * @throws IOException An error occured while writing to the OutputStream.
     */
    public void flush() throws IOException {
        if (_nextBit > 0x1)
            _bufferPos++;

        _stream.write(_buffer, 0, _bufferPos);
        _bufferPos = 0;
        _nextBit = 0x1;
    }
}
