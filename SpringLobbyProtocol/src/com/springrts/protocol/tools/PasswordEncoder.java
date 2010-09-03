/*
 * Copyright (C) 2010 NRV - nherve75@gmail.com
 * 
 * This file is part of SpringLobbyProtocol.
 * 
 * SpringLobbyProtocol is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 * 
 * SpringLobbyProtocol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SpringLobbyProtocol. If not, see http://www.gnu.org/licenses/
 * 
 */

package com.springrts.protocol.tools;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;

/**
 * Based on <a href="http://iharder.net/base64">http://iharder.net/base64</a> 
 * and <a href="http://www.twmacinta.com/myjava/fast_md5.php">http://www.twmacinta.com/myjava/fast_md5.php</a>
 * 
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class PasswordEncoder {
	public PasswordEncoder() {
		super();
	}

	private class MD5State {
		int state[];
		long count;
		byte buffer[];

		public MD5State() {
			buffer = new byte[64];
			count = 0;
			state = new int[4];

			state[0] = 0x67452301;
			state[1] = 0xefcdab89;
			state[2] = 0x98badcfe;
			state[3] = 0x10325476;

		}

		public MD5State(MD5State from) {
			this();
			int i;
			for (i = 0; i < buffer.length; i++)
				this.buffer[i] = from.buffer[i];
			for (i = 0; i < state.length; i++)
				this.state[i] = from.state[i];
			this.count = from.count;
		}
	}

	private final static byte EQUALS_SIGN = (byte) '=';
	private final static byte[] _STANDARD_ALPHABET = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/' };
	private MD5State state;
	private MD5State finals;
	private static byte padding[] = { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public String encodePassword(String plainPassword, String preferredEncoding) throws NoSuchAlgorithmException, DigestException {
		String encodedPassword = null;

		/*
		 * byte[] password = plainPassword.getBytes(); byte[] md5Digest = new byte[1024]; MessageDigest mdAlgorithm = MessageDigest.getInstance("md5"); mdAlgorithm.update(password, 0, password.length); int lgt = mdAlgorithm.digest(md5Digest, 0, md5Digest.length);
		 */

		Init();
		Update(plainPassword);
		byte[] md5Digest = Final();
		int lgt = md5Digest.length;

		encodedPassword = encodeBytes(md5Digest, 0, lgt, preferredEncoding);

		return encodedPassword;
	}

	private String encodeBytes(byte[] source, int off, int len, String preferredEncoding) {
		byte[] encoded = encodeBytesToBytes(source, off, len);

		try {
			return new String(encoded, preferredEncoding);
		} catch (java.io.UnsupportedEncodingException uue) {
			return new String(encoded);
		}

	}

	private byte[] encodeBytesToBytes(byte[] source, int off, int len) {

		if (source == null) {
			throw new NullPointerException("Cannot serialize a null array.");
		}

		if (off < 0) {
			throw new IllegalArgumentException("Cannot have negative offset: " + off);
		}

		if (len < 0) {
			throw new IllegalArgumentException("Cannot have length offset: " + len);
		}

		if (off + len > source.length) {
			throw new IllegalArgumentException("Cannot have offset of " + off + " and length of " + len + " with array of length " + source.length);
		}

		int encLen = (len / 3) * 4 + (len % 3 > 0 ? 4 : 0);
		byte[] outBuff = new byte[encLen];

		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for (; d < len2; d += 3, e += 4) {
			encode3to4(source, d + off, 3, outBuff, e);

			lineLength += 4;
		}

		if (d < len) {
			encode3to4(source, d + off, len - d, outBuff, e);
			e += 4;
		}

		if (e <= outBuff.length - 1) {
			byte[] finalOut = new byte[e];
			System.arraycopy(outBuff, 0, finalOut, 0, e);
			return finalOut;
		} else {
			return outBuff;
		}

	}

	private byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset) {
		byte[] ALPHABET = _STANDARD_ALPHABET;

		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0) | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0) | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

		switch (numSigBytes) {
		case 3:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
			return destination;

		case 2:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		case 1:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = EQUALS_SIGN;
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		default:
			return destination;
		}
	}

	private synchronized void Init() {
		state = new MD5State();
		finals = null;
	}

	private void Decode(byte buffer[], int shift, int[] out) {
		out[0] = ((int) (buffer[shift] & 0xff)) | (((int) (buffer[shift + 1] & 0xff)) << 8) | (((int) (buffer[shift + 2] & 0xff)) << 16) | (((int) buffer[shift + 3]) << 24);
		out[1] = ((int) (buffer[shift + 4] & 0xff)) | (((int) (buffer[shift + 5] & 0xff)) << 8) | (((int) (buffer[shift + 6] & 0xff)) << 16) | (((int) buffer[shift + 7]) << 24);
		out[2] = ((int) (buffer[shift + 8] & 0xff)) | (((int) (buffer[shift + 9] & 0xff)) << 8) | (((int) (buffer[shift + 10] & 0xff)) << 16) | (((int) buffer[shift + 11]) << 24);
		out[3] = ((int) (buffer[shift + 12] & 0xff)) | (((int) (buffer[shift + 13] & 0xff)) << 8) | (((int) (buffer[shift + 14] & 0xff)) << 16) | (((int) buffer[shift + 15]) << 24);
		out[4] = ((int) (buffer[shift + 16] & 0xff)) | (((int) (buffer[shift + 17] & 0xff)) << 8) | (((int) (buffer[shift + 18] & 0xff)) << 16) | (((int) buffer[shift + 19]) << 24);
		out[5] = ((int) (buffer[shift + 20] & 0xff)) | (((int) (buffer[shift + 21] & 0xff)) << 8) | (((int) (buffer[shift + 22] & 0xff)) << 16) | (((int) buffer[shift + 23]) << 24);
		out[6] = ((int) (buffer[shift + 24] & 0xff)) | (((int) (buffer[shift + 25] & 0xff)) << 8) | (((int) (buffer[shift + 26] & 0xff)) << 16) | (((int) buffer[shift + 27]) << 24);
		out[7] = ((int) (buffer[shift + 28] & 0xff)) | (((int) (buffer[shift + 29] & 0xff)) << 8) | (((int) (buffer[shift + 30] & 0xff)) << 16) | (((int) buffer[shift + 31]) << 24);
		out[8] = ((int) (buffer[shift + 32] & 0xff)) | (((int) (buffer[shift + 33] & 0xff)) << 8) | (((int) (buffer[shift + 34] & 0xff)) << 16) | (((int) buffer[shift + 35]) << 24);
		out[9] = ((int) (buffer[shift + 36] & 0xff)) | (((int) (buffer[shift + 37] & 0xff)) << 8) | (((int) (buffer[shift + 38] & 0xff)) << 16) | (((int) buffer[shift + 39]) << 24);
		out[10] = ((int) (buffer[shift + 40] & 0xff)) | (((int) (buffer[shift + 41] & 0xff)) << 8) | (((int) (buffer[shift + 42] & 0xff)) << 16) | (((int) buffer[shift + 43]) << 24);
		out[11] = ((int) (buffer[shift + 44] & 0xff)) | (((int) (buffer[shift + 45] & 0xff)) << 8) | (((int) (buffer[shift + 46] & 0xff)) << 16) | (((int) buffer[shift + 47]) << 24);
		out[12] = ((int) (buffer[shift + 48] & 0xff)) | (((int) (buffer[shift + 49] & 0xff)) << 8) | (((int) (buffer[shift + 50] & 0xff)) << 16) | (((int) buffer[shift + 51]) << 24);
		out[13] = ((int) (buffer[shift + 52] & 0xff)) | (((int) (buffer[shift + 53] & 0xff)) << 8) | (((int) (buffer[shift + 54] & 0xff)) << 16) | (((int) buffer[shift + 55]) << 24);
		out[14] = ((int) (buffer[shift + 56] & 0xff)) | (((int) (buffer[shift + 57] & 0xff)) << 8) | (((int) (buffer[shift + 58] & 0xff)) << 16) | (((int) buffer[shift + 59]) << 24);
		out[15] = ((int) (buffer[shift + 60] & 0xff)) | (((int) (buffer[shift + 61] & 0xff)) << 8) | (((int) (buffer[shift + 62] & 0xff)) << 16) | (((int) buffer[shift + 63]) << 24);
	}

	private void Transform(MD5State state, byte buffer[], int shift, int[] decode_buf) {
		int a = state.state[0], b = state.state[1], c = state.state[2], d = state.state[3], x[] = decode_buf;

		Decode(buffer, shift, decode_buf);

		a += ((b & c) | (~b & d)) + x[0] + 0xd76aa478; /* 1 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[1] + 0xe8c7b756; /* 2 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[2] + 0x242070db; /* 3 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[3] + 0xc1bdceee; /* 4 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[4] + 0xf57c0faf; /* 5 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[5] + 0x4787c62a; /* 6 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[6] + 0xa8304613; /* 7 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[7] + 0xfd469501; /* 8 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[8] + 0x698098d8; /* 9 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[9] + 0x8b44f7af; /* 10 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[10] + 0xffff5bb1; /* 11 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[11] + 0x895cd7be; /* 12 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[12] + 0x6b901122; /* 13 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[13] + 0xfd987193; /* 14 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[14] + 0xa679438e; /* 15 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[15] + 0x49b40821; /* 16 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & d) | (c & ~d)) + x[1] + 0xf61e2562; /* 17 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[6] + 0xc040b340; /* 18 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[11] + 0x265e5a51; /* 19 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[0] + 0xe9b6c7aa; /* 20 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[5] + 0xd62f105d; /* 21 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[10] + 0x02441453; /* 22 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[15] + 0xd8a1e681; /* 23 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[4] + 0xe7d3fbc8; /* 24 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[9] + 0x21e1cde6; /* 25 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[14] + 0xc33707d6; /* 26 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[3] + 0xf4d50d87; /* 27 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[8] + 0x455a14ed; /* 28 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[13] + 0xa9e3e905; /* 29 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[2] + 0xfcefa3f8; /* 30 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[7] + 0x676f02d9; /* 31 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[12] + 0x8d2a4c8a; /* 32 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += (b ^ c ^ d) + x[5] + 0xfffa3942; /* 33 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[8] + 0x8771f681; /* 34 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[11] + 0x6d9d6122; /* 35 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[14] + 0xfde5380c; /* 36 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[1] + 0xa4beea44; /* 37 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[4] + 0x4bdecfa9; /* 38 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[7] + 0xf6bb4b60; /* 39 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[10] + 0xbebfbc70; /* 40 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[13] + 0x289b7ec6; /* 41 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[0] + 0xeaa127fa; /* 42 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[3] + 0xd4ef3085; /* 43 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[6] + 0x04881d05; /* 44 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[9] + 0xd9d4d039; /* 33 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[12] + 0xe6db99e5; /* 34 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[15] + 0x1fa27cf8; /* 35 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[2] + 0xc4ac5665; /* 36 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (c ^ (b | ~d)) + x[0] + 0xf4292244; /* 49 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[7] + 0x432aff97; /* 50 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[14] + 0xab9423a7; /* 51 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[5] + 0xfc93a039; /* 52 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[12] + 0x655b59c3; /* 53 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[3] + 0x8f0ccc92; /* 54 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[10] + 0xffeff47d; /* 55 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[1] + 0x85845dd1; /* 56 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[8] + 0x6fa87e4f; /* 57 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[15] + 0xfe2ce6e0; /* 58 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[6] + 0xa3014314; /* 59 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[13] + 0x4e0811a1; /* 60 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[4] + 0xf7537e82; /* 61 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[11] + 0xbd3af235; /* 62 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[2] + 0x2ad7d2bb; /* 63 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[9] + 0xeb86d391; /* 64 */
		b = ((b << 21) | (b >>> 11)) + c;

		state.state[0] += a;
		state.state[1] += b;
		state.state[2] += c;
		state.state[3] += d;
	}

	private void Update(MD5State stat, byte buffer[], int offset, int length) {
		int index, partlen, i, start;
		finals = null;

		if ((length - offset) > buffer.length)
			length = buffer.length - offset;

		index = (int) (stat.count & 0x3f);
		stat.count += length;

		partlen = 64 - index;

		if (length >= partlen) {

			int[] decode_buf = new int[16];
			if (partlen == 64) {
				partlen = 0;
			} else {
				for (i = 0; i < partlen; i++)
					stat.buffer[i + index] = buffer[i + offset];
				Transform(stat, stat.buffer, 0, decode_buf);
			}
			for (i = partlen; (i + 63) < length; i += 64) {
				Transform(stat, buffer, i + offset, decode_buf);
			}

			index = 0;
		} else {
			i = 0;
		}

		if (i < length) {
			start = i;
			for (; i < length; i++) {
				stat.buffer[index + i - start] = buffer[i + offset];
			}
		}
	}

	private void Update(byte buffer[], int length) {
		Update(this.state, buffer, 0, length);
	}

	private void Update(String s) {
		byte chars[] = null;
		try {
			chars = s.getBytes("ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			chars = s.getBytes();
		}
		Update(chars, chars.length);
	}

	private byte[] Encode(int input[], int len) {
		int i, j;
		byte out[];

		out = new byte[len];

		for (i = j = 0; j < len; i++, j += 4) {
			out[j] = (byte) (input[i] & 0xff);
			out[j + 1] = (byte) ((input[i] >>> 8) & 0xff);
			out[j + 2] = (byte) ((input[i] >>> 16) & 0xff);
			out[j + 3] = (byte) ((input[i] >>> 24) & 0xff);
		}

		return out;
	}

	private synchronized byte[] Final() {
		byte bits[];
		int index, padlen;
		MD5State fin;

		if (finals == null) {
			fin = new MD5State(state);

			int[] count_ints = { (int) (fin.count << 3), (int) (fin.count >> 29) };
			bits = Encode(count_ints, 8);

			index = (int) (fin.count & 0x3f);
			padlen = (index < 56) ? (56 - index) : (120 - index);

			Update(fin, padding, 0, padlen);
			Update(fin, bits, 0, 8);

			/* Update() sets finals to null */
			finals = fin;
		}

		return Encode(finals.state, 16);
	}

}
