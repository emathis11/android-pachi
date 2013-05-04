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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Reads bits from an InputStream.
 */
public class BitReader {
    private InputStream _stream;
    private byte[] _buffer;
    private int _bufferPos;
    private int _bitmask;


    public BitReader(InputStream stream) {
        _stream = stream;
        _buffer = new byte[128];
        _bufferPos = _buffer.length;
        _bitmask = 0x1;
    }

    /**
     * Reads a single byte from the underlying stream.
     *
     * @throws IOException An error occured during reading, or the end of stream has been reached.
     */
    public boolean read() throws IOException {
        if (_bufferPos == _buffer.length) {
            int bytesRead = _stream.read(_buffer);
            if (bytesRead < 0)
                throw new EOFException();

            _bufferPos = _buffer.length - bytesRead;
            System.arraycopy(_buffer, 0, _buffer, _bufferPos, bytesRead);
        }

        boolean bit = (_buffer[_bufferPos] & _bitmask) != 0;

        if (_bitmask == 0x80) {
            _bitmask = 0x1;
            _bufferPos++;
        } else {
            _bitmask <<= 1;
        }

        return bit;
    }


    /**
     * Reads multiple bytes from the underlying stream and returns them in a long (byte order is
     * preserved : highest bytes in the stream will remain the highest bytes in the returned long).
     *
     * @param bitsToRead The number of bits to read, between 1 and 64 (included).
     * @throws IOException An error occured during reading, or the end of stream has been reached.
     */
    public long read(int bitsToRead) throws IOException {
        if (bitsToRead > 64)
            bitsToRead = 64;

        long result = 0;
        for (int i = bitsToRead - 1; i >= 0; i--) {
            if (read())
                result |= 1 << i;
        }

        return result;
    }
}
