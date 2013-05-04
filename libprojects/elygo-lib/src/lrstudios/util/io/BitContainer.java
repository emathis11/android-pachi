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


/**
 * Contains a bit array which grows automatically.
 */
public class BitContainer {
    protected static final String TAG = "BitContainer";

    protected long[] _bits;
    protected int _currentPos;
    protected int _currentReadingPos;


    public BitContainer() {
        _bits = new long[2];
        _currentPos = 0;
    }

    /**
     * Creates a new BitContainer with an initial value.
     */
    public BitContainer(byte[] initialValue) {
        this();
        append(initialValue);
    }

    /**
     * Creates a new BitContainer with the bits contained in the specified string
     * (this String must contain only the characters '0' and '1').
     */
    public BitContainer(String initialValue) {
        this();
        append(initialValue);
    }


    /**
     * Appends a bit at the end of the container.
     */
    public void append(boolean bit) {
        if (bit) {
            int index = (int) (_currentPos / Long.SIZE);
            int shiftPos = (int) (_currentPos % Long.SIZE);
            _bits[index] = _bits[index] | (1L << shiftPos);
        }

        _currentPos++;

        if (_currentPos == _bits.length * Long.SIZE) {
            long[] newArray = new long[_bits.length * 2];
            System.arraycopy(_bits, 0, newArray, 0, _bits.length);

            _bits = newArray;
        }
    }

    /**
     * Appends all bits contained in the specified array at the end of the container.
     */
    public void append(byte[] array) {
        for (byte b : array)
            append(b, 8);
    }

    /**
     * Appends the bits contained in the specified array at the end of the container.
     * Only the first "bitsToWrite" will be added.
     */
    public void append(long bits, int bitsToWrite) {
        for (int i = 0; i < bitsToWrite; i++)
            append(((bits >> i) & 1L) > 0);
    }

    /**
     * Appends all bits contained in the specified string. Any character which is not '1' or '0' will
     * be considered as a '0'.
     */
    public void append(String bits) {
        int len = bits.length();
        for (int i = 0; i < len; i++)
            append(bits.charAt(i) == '1');
    }

    /**
     * Returns the specified bits contained in the current container as an integer.
     * For example, if the container contains "0110010", get(1, 3) will extract the bits "110"
     * and return them as a long : "000...0000000110" (decimal value : 6).
     *
     * @param pos        The offset of the first bit.
     * @param bitsToRead The number of bits to read and return (max = 63).
     */
    public long get(int pos, int bitsToRead) {
        long finalValue = 0;
        int len = pos + bitsToRead;

        for (int i = pos; i < len; i++) {
            int index = i / Long.SIZE;
            int shiftPos = i % Long.SIZE;
            finalValue |= (_bits[index] >> shiftPos & 1L) << i - pos;
        }

        return finalValue;
    }

    /**
     * Returns the next bits to read as an integer.
     *
     * @param bitsToRead The number of bits to read and return (max = 63).
     */
    public long read(int bitsToRead) {
        long value = get(_currentReadingPos, bitsToRead);
        _currentReadingPos += bitsToRead;
        return value;
    }


    /**
     * Clears the content of the current container.
     */
    public void clear() {
        _bits = new long[1];
        _currentPos = 0;
    }

    /**
     * Converts this array into a byte array. If the length of the current container
     * isn't a multiple of 8, zeros will be appended to fill the space.
     */
    public byte[] toByteArray() {
        byte[] arr = new byte[(int) Math.ceil(_currentPos / 8.0)];

        for (int i = 0; i < _currentPos; i++) {
            int index = i / Long.SIZE;
            int shiftPos = i % Long.SIZE;
            long value = (_bits[index] >> shiftPos) & 1;

            if (value == 1) {
                int indexB = i / Byte.SIZE;
                int shiftPosB = i % Byte.SIZE;
                arr[indexB] |= (byte) (1 << shiftPosB);
            }
        }

        return arr;
    }


    public int getCurrentPosition() {
        return _currentPos;
    }

    public int getCurrentReadingPosition() {
        return _currentReadingPos;
    }


    /**
     * Returns a the content of this container as a human-readable string (composed of '0' and '1').
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < _currentPos; i++) {
            int index = i / Long.SIZE;
            int shiftPos = i % Long.SIZE;

            long value = (_bits[index] >> shiftPos) & 1;
            str.append(value);
        }

        return str.toString();
    }
}
