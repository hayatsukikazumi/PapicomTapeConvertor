package com.hayatsukikazumi.ptc;

/**
 * FSK変調の解析クラス
 * 
 * @author HayatsukiKazumi
 * @version 1.1.0
 */
public class FSKBitAnalyzer {

    public enum SignalJudge { A, B, C, D };

    private int _pos34;
    private int _pos54;
    private int _pos94;

    private double _avg = 0;
    private double _low = -0.1;
    private double _high = 0.1;

    /***
     * コンストラクタ。
     * @param sampleRate サンプリングレート(Hz)
     * @param lowFreq 低い側の周波数(Hz)
     */
    public FSKBitAnalyzer(int sampleRate, double lowFreq) {

        if (sampleRate <= 0 || lowFreq <= 0) {
            throw new IllegalArgumentException("Please set positive value in parameters.");
        }

        _pos34 = (int) (sampleRate / lowFreq * 2.75 / 8.0);
        _pos54 = (int) (sampleRate / lowFreq * 5.25 / 8.0);
        _pos94 = (int) (sampleRate / lowFreq * 8.25 / 8.0) + 1;
    }

    /**
     * 次の波形0位置を得る。
     * @param buf 信号バッファ
     * @param startPos 読み取り開始位置
     * @return 次の波形0位置
     */
    public int getNextZeroPosition(float[] buf, int startPos) {

        // 次のbit開始位置の最小値、最大値を算出
        int limitPos = Math.min(startPos + _pos94, buf.length);
        double max = buf[startPos];
        double min = buf[startPos];

        for (int i = startPos; i < limitPos; i++) {
            max = Math.max(max, buf[i]);
            min = Math.min(min, buf[i]);
        }

        _avg = (max + min) / 2.0;
        _high = (max + _avg * 3) / 4.0;
        _low = (min + _avg * 3) / 4.0;

        // 信号がLOWになるまで進む
        int spos = startPos + (int) _pos34;
        int pos = spos;
        ;
        while (buf[pos] > _low && pos < limitPos) {
            pos++;
        }

        // 信号がHIGHになるまで進む
        while (buf[pos] < _high && pos < limitPos) {
            pos++;
        }

        if (buf[pos] >= _avg && pos >= limitPos) {
            pos--;
            return pos;
        }

        

        // 信号がAVERAGEになるまで戻す
        while (buf[pos] >= _avg && pos > spos) {
            pos--;
        }

        return pos;
    }

    /**
     * 高周波数であるかを返す。
     * @param pos1 最初の信号0位置
     * @param pos2 次の信号0位置
     * @return 高周波数ならばtrue
     */
    public boolean isHighFrequency(int pos1, int pos2) {
        return (pos2 - pos1) <= _pos54;
    }

    /**
     * ビット判定。
     * @param buf 信号バッファ
     * @param pos1 最初の信号0位置
     * @param pos2 次の信号0位置
     * @param
     * @return 判定結果
     */
    public SignalJudge judgeBit(float[] buf, int pos1, int pos2, boolean isHighFreq) {

        float v34 = buf[pos1 + _pos34];
        float v54 = buf[pos1 + _pos54];

        SignalJudge judge;
        if (v34 < _low) { // おそらく高周波数
            if (v54 < _low) {
                judge = SignalJudge.C;
            } else if (v54 < _avg) {
                judge = isHighFreq ? SignalJudge.B : SignalJudge.D;
            } else {
                judge = isHighFreq ? SignalJudge.A : SignalJudge.D;
            }
        } else if (v34 < _avg) {
            if (v54 < _low) {
                judge = isHighFreq ? SignalJudge.D : SignalJudge.B; // おそらく低周波数（違ったー）
            } else if (v54 < _avg) {
                judge = SignalJudge.C;
            } else if (v54 < _high) {
                judge = isHighFreq ? SignalJudge.B : SignalJudge.D; // おそらく高周波数
            } else {
                judge = isHighFreq ? SignalJudge.A : SignalJudge.D;
            }
        } else if (v34 < _high) {
            if (v54 < _low) {
                judge = isHighFreq ? SignalJudge.D : SignalJudge.A; // おそらく低周波数
            } else if (v54 < _avg) {
                judge = isHighFreq ? SignalJudge.D : SignalJudge.B;
            } else if (v54 < _high) {
                judge = SignalJudge.C;
            } else {
                judge = isHighFreq ? SignalJudge.B : SignalJudge.D; // おそらく高周波数（違ったー）
            }
        } else { // おそらく低周波数
            if (v54 < _avg) {
                judge = isHighFreq ? SignalJudge.D : SignalJudge.A;
            } else if (v54 < _high) {
                judge = isHighFreq ? SignalJudge.D : SignalJudge.B;
            } else {
                judge = SignalJudge.C;
            }
        }

        return judge;
    }

    /**
     * 2つのSignalJudgeのうち低い方を返す。
     * @param a
     * @param b
     * @return
     */
    public static SignalJudge min(SignalJudge a, SignalJudge b) {
        return a.ordinal() < b.ordinal() ? b : a;
    }
}
