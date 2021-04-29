/**
 * @(#)WAVDecoder.java 2021/04/29
 *
 *                     Copyright(c) HayatsukiKazumi 2021 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * WAVフォーマットのデータのバイナリを変換するクラス
 *
 * @author HayatsukiKazumi
 * @version 1.2.0
 */
public abstract class WAVDecoder {

    public static final int FORMAT_ID_LINEAR_PCM = 1;
    public static final int FORMAT_ID_ALAW = 6;
    public static final int FORMAT_ID_MLAW = 7;

    /**
     * デコードする。
     * @param buf 元のバイナリデータ
     * @param off 変換位置
     * @return 変換後の値
     */
    public abstract float decode(byte[] buf, int off);

    /**
     * ファイルフォーマットに対応したデコーダを返す。
     * @param formatId フォーマットID
     * @param bytes 1音のバイト数
     * @return デコーダ
     * @throws UnsupportedAudioFileException サポートされていないフォーマット
     */
    public static WAVDecoder getDecoder(int formatId, int bytes) throws UnsupportedAudioFileException {
        switch (formatId) {
        case FORMAT_ID_LINEAR_PCM:
            switch (bytes) {
            case 1:
                return new LINEAR8Decoder();
            case 2:
                return new LINEAR16Decoder();
            case 3:
                return new LINEAR24Decoder();
            default:
                throw new UnsupportedAudioFileException("Format not supported. bytes = " + bytes);
            }
        case FORMAT_ID_ALAW:
            return new ALAWDecoder();
        case FORMAT_ID_MLAW:
            return new MLAWDecoder();
        default:
            throw new UnsupportedAudioFileException("Format not supported. formatId = " + formatId);
        }

    }

    static class LINEAR8Decoder extends WAVDecoder {
        @Override
        public float decode(byte[] buf, int off) {
            return ((buf[off] & 0xff) - 128) / (float) 128;
        }
    }

    static class LINEAR16Decoder extends WAVDecoder {
        @Override
        public float decode(byte[] buf, int off) {
            return ((buf[off + 1] << 8) | (buf[off] & 0xff)) / (float) 0x8000;
        }
    }

    static class LINEAR24Decoder extends WAVDecoder {
        @Override
        public float decode(byte[] buf, int off) {
            return ((buf[off + 2] << 16) | ((buf[off + 1] << 8) & 0xff00) | (buf[off] & 0xff)) / (float) 0x800000;
        }
    }

    static class ALAWDecoder extends WAVDecoder {
        @Override
        public float decode(byte[] buf, int off) {
            int src = buf[off] ^ 0x55;
            boolean isMinus = ((src & 0x80) != 0);
            int frac = src & 0x0f;
            int expo = (src & 0x70) >> 4;
            if (expo == 0) {
                frac <<= 1;
                frac++;
            } else {
                frac <<= 1;
                frac += 33;
                expo--;
                frac <<= expo;
            }

            return (isMinus ? -frac : --frac) / 4096.0f;
        }
    }

    static class MLAWDecoder extends WAVDecoder {
        @Override
        public float decode(byte[] buf, int off) {
            int src = buf[off] ^ 0xff;
            boolean isMinus = ((src & 0x80) != 0);
            int expo = (src & 0x70) >> 4;
            int frac = src & 0x0f;
            frac <<= 1;
            frac += 33;
            frac <<= expo;
            frac -= 33;
            if (isMinus) {
                frac = -frac;
                frac--;
            }

            return frac / 8192.0f;
        }
    }
}
