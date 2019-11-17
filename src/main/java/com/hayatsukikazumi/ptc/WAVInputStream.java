/**
 * @(#)WAVFilterInputStream.java 2005/08/04
 *
 * Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * WAVフォーマットを読み取るためのInputStream
 *
 * @author HayatsukiKazumi
 * @version 1.1.1
 */
public class WAVInputStream extends BufferedInputStream {
	/** チャネル数 */
	private int _channels;

	private int _formatId;

	private int _samplingRate;

	private int _bits;

	private int _bytes;

	private int _dataSize;

	private byte[] _buf;

	/**
	 * 元となるInputStreamからヘッダー部分を読みこむ。
	 *
	 * @param in 元となるInputStream
	 * @throws WrongFormatException WAVファイルの形式が正しくない場合
	 * @throws IOException 読み取りに失敗した場合
	 */
	public WAVInputStream(InputStream in) throws WrongFormatException, IOException {
		super(in);
		readHeader();
	}

	/**
	 * チャンネル数を得る。
	 *
	 * @return チャンネル数
	 */
	public int getChannels() {
		return _channels;
	}

	/**
	 * フォーマットIDを得る。
	 *
	 * @return フォーマットID
	 */
	public int getFormatId() {
		return _formatId;
	}

	/**
	 * サンプリング周波数を得る。
	 *
	 * @return サンプリング周波数(Hz)
	 */
	public int getSamplingRate() {
		return _samplingRate;
	}

	/**
	 * ビット数を得る。
	 *
	 * @return ビット数
	 */
	public int getBits() {
		return _bits;
	}

	/**
	 * データサイズを得る。
	 *
	 * @return データサイズ
	 */
	public int getDataSize() {
		return _dataSize;
	}

	/**
	 * 音声データを読み取る。
	 * channelに負の数を指定した場合、出力先配列にはステレオ音声ではL,R,L,R,…の順に出力される。
	 *
	 * @param channel どのチャネルを取得するか（負の数：全部のチャネル）
	 * @param buf 出力先
	 * @param off 書き込み位置
	 * @param len 最大書き込み数
	 * @return 書き込んだ配列の数。既にストリームの終端に達している場合は-1
	 * @throws IOException
	 * @throws IllegalArgumentException channel &gt;= チャネル数の場合
	 */
	public int readSound(int channel, float[] buf, int off, int len) throws IOException {

		if (channel >= _channels) {
			throw new IllegalArgumentException("Illegal channel.");
		}

		int pos = off;
		int maxlen = (channel < 0) ? len : (len / _channels) * _channels;
		int retlen = 0;

		while (retlen < maxlen) {

			int d = read(_buf);
			if (d < _buf.length) {
				return (retlen == 0) ? -1 : retlen;
			}

			int bpos = 0;
			for (int j = 0; j < _channels; j++) {
				if (channel < 0 || j == channel) {
					buf[pos] = bytesToFloat(_buf, bpos, _bytes);
					pos++;
					retlen++;
				}

				bpos += _bytes;
			}
		}

		return retlen;
	}

	/**
	 * リトルエンディアン形式のバイト配列を-1〜1の浮動小数点に変換する。
	 * @param buf
	 * @param off
	 * @param len
	 * @return
	 */
	private static float bytesToFloat(byte[] buf, int off, int len) {

		switch (len) {
		case 1:
			return ((buf[off] & 0xff) - 128) / (float) 128;

		case 2:
			return ((buf[off + 1] << 8) | (buf[off] & 0xff)) / (float) 0x8000;

		case 3:
			return ((buf[off + 2] << 16) | ((buf[off + 1] << 8) & 0xff00) | (buf[off] & 0xff)) / (float) 0x800000;

		default:
			return 0;
		}
	}

	/**
	 * 実際にヘッダ部分を読み取る。
	 *
	 * @throws WrongFormatException WAVファイルの形式が正しくない場合
	 * @throws IOException 読み取りに失敗した場合
	 */
	private void readHeader() throws WrongFormatException, IOException {
		byte[] buf = new byte[4];
		int b = 0;

		// RIFFヘッダ
		b = read(buf);
		if (b == -1 || !"RIFF".equals(new String(buf))) {
			throw new WrongFormatException();
		}

		// 空読み
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}

		// WAVEヘッダ
		b = read(buf);
		if (b == -1 || !"WAVE".equals(new String(buf))) {
			throw new WrongFormatException();
		}

		// fmtヘッダ
		b = read(buf);
		if (b == -1 || !"fmt ".equals(new String(buf))) {
			throw new WrongFormatException();
		}

		// 空読み
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}

		// フォーマット、チャンネル数
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}
		_formatId = (buf[0] & 255) | ((buf[1] & 255) << 8);
		_channels = buf[2] & 255;

		// サンプリングレート
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}
		_samplingRate = (buf[0] & 255) | ((buf[1] & 255) << 8)
				| ((buf[2] & 255) << 16);

		// 空読み
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}

		// ビット数
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}
		_bits = buf[2] & 255;

		// dataヘッダが出るまで空読み
		while ((b = read()) != -1) {
			buf[0] = buf[1];
			buf[1] = buf[2];
			buf[2] = buf[3];
			buf[3] = (byte) b;

			if (buf[0] == 'd' && buf[1] == 'a' && buf[2] == 't'
					&& buf[3] == 'a') {
				break;
			}
		}

		if (b == -1) {
			throw new WrongFormatException();
		}

		// データサイズ
		b = read(buf);
		if (b == -1) {
			throw new WrongFormatException();
		}
		_dataSize = (buf[0] & 255) | ((buf[1] & 255) << 8)
				| ((buf[2] & 255) << 16) | ((buf[3] & 255 << 24));

		// リードバッファ
		_bytes = (_bits + 7) / 8;
		_buf = new byte[_bytes * _channels];
	}
}
