/**
 * @(#)WAVFilterInputStream.java 2005/08/04
 *
 *                               Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * WAVフォーマットを読み取るためのInputStream
 *
 * @author HayatsukiKazumi
 * @version 1.2.0
 */
public class WAVInputStream extends BufferedInputStream {
    /** チャネル数 */
    private int _channels;
    /** フォーマットID */
    private int _formatId;
    /** サンプリング周波数 */
    private int _samplingRate;
    /** ビット数 */
    private int _bits;
    /** バイト数 */
    private int _bytes;
    /** データサイズ */
    private int _dataSize;
    /** 処理用バッファ */
    private byte[] _buf;
    /** デコーダ */
    private WAVDecoder _decoder;

    /**
     * 元となるInputStreamからヘッダー部分を読みこむ。
     *
     * @param in 元となるInputStream
     * @throws WrongFormatException WAVファイルの形式が正しくない場合
     * @throws IOException 読み取りに失敗した場合
     */
    public WAVInputStream(InputStream in) throws UnsupportedAudioFileException, IOException {
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
     * 音声データを読み取る。 channelに負の数を指定した場合、出力先配列にはステレオ音声ではL,R,L,R,…の順に出力される。
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
                    buf[pos] = _decoder.decode(_buf, bpos);
                    pos++;
                    retlen++;
                }

                bpos += _bytes;
            }
        }

        return retlen;
    }

    /**
     * 実際にヘッダ部分を読み取る。
     *
     * @throws UnsupportedAudioFileException WAVファイルの形式が正しくない場合
     * @throws IOException 読み取りに失敗した場合
     */
    private void readHeader() throws UnsupportedAudioFileException, IOException {
        byte[] buf = new byte[4];
        int b = 0;

        // RIFFヘッダ
        b = read(buf);
        if (b == -1 || !"RIFF".equals(new String(buf))) {
            throw new UnsupportedAudioFileException();
        }

        // 空読み
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }

        // WAVEヘッダ
        b = read(buf);
        if (b == -1 || !"WAVE".equals(new String(buf))) {
            throw new UnsupportedAudioFileException();
        }

        // fmtヘッダ
        b = read(buf);
        if (b == -1 || !"fmt ".equals(new String(buf))) {
            throw new UnsupportedAudioFileException();
        }

        // 空読み
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }

        // フォーマット、チャンネル数
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }
        _formatId = (buf[0] & 255) | ((buf[1] & 255) << 8);
        _channels = buf[2] & 255;

        // サンプリングレート
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }
        _samplingRate = (buf[0] & 255) | ((buf[1] & 255) << 8)
                | ((buf[2] & 255) << 16);

        // 空読み
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }

        // ビット数
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
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
            throw new UnsupportedAudioFileException();
        }

        // データサイズ
        b = read(buf);
        if (b == -1) {
            throw new UnsupportedAudioFileException();
        }
        _dataSize = (buf[0] & 255) | ((buf[1] & 255) << 8)
                | ((buf[2] & 255) << 16) | ((buf[3] & 255 << 24));

        // リードバッファ
        _bytes = (_bits + 7) / 8;
        _buf = new byte[_bytes * _channels];

        // デコーダ
        _decoder = WAVDecoder.getDecoder(_formatId, _bytes);
    }
}
