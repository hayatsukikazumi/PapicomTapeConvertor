package com.hayatsukikazumi.ptc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * WAV→P6の変換機能ロジッククラス
 *
 * @author HayatsukiKazumi
 * @version 1.1.0
 */
public class PTCLogic {

    private enum StartBitType {
        STREAM_END, START_BIT, LEADER
    };

    private static final double FREQ_LOW = 1200.0;
    private static final int BUF_HALF_SIZE = 512;
    private static final int BUF_READ_POS = BUF_HALF_SIZE * 3 / 2;
    private static final int MIN_LEADER_LEN = 28;

    private static final int LEADER_COUNT = 3;

    private WAVInputStream _in;

    private OutputStream _out;

    private PrintStream _report;

    private FSKBitAnalyzer _fsk;

    private int _samplingRate;

    private boolean _negate;

    private float[] _buf;
    int _pos;
    long _abspos;
    int _destpos;

    public PTCLogic() {
    }

    /**
     * 変換処理を実行する。
     * @param in 入力ストリーム
     * @param out 結果出力ストリーム
     * @param report レポート出力
     * @param skip スキップする時間(sec.)
     * @param negate 極性反転フラグ
     * @throws IOException
     * @throws IllegalEndDetectedException
     */
    public void analyze(WAVInputStream in, OutputStream out, PrintStream report, double skip, boolean negate)
            throws IOException, IllegalEndDetectedException {

        _in = in;
        _out = out;
        _report = report;
        _samplingRate = _in.getSamplingRate();
        _fsk = new FSKBitAnalyzer(_samplingRate, FREQ_LOW);

        _buf = new float[BUF_HALF_SIZE * 2];
        _negate = negate;
        _pos = _buf.length;
        _abspos = -_buf.length;
        _destpos = 0;

        // スキップする
        long topos = (long) (_samplingRate * skip) - BUF_HALF_SIZE;

        while (_abspos < topos) {
            if (!read()) {
                PTCReport.writeBreakReport(_report, _abspos + _pos);
                throw new IllegalEndDetectedException();
            }
            _pos += BUF_HALF_SIZE;
        }
        _pos = (int) (_abspos - topos);
        PTCReport.writeHeaderReport(_report, _abspos + _pos, _samplingRate, skip);

        // 頭出し
        _pos = _fsk.getNextZeroPosition(_buf, _pos);
        _pos = _fsk.getNextZeroPosition(_buf, _pos);

        int block = LEADER_COUNT;
        boolean inLeader = false;
        while (true) {
            // スタートビット検出
            switch (detectStartBit()) {
            case START_BIT:
                if (block == LEADER_COUNT) {
                    PTCReport.writeBreakReport(_report, _abspos + _pos);
                    throw new IllegalEndDetectedException();
                }

                inLeader = false;
                if (!readAndWriteByte()) {
                    PTCReport.writeBreakReport(_report, _abspos + _pos);
                    throw new IllegalEndDetectedException();
                }
                break;

            case LEADER:
                if (!inLeader) {
                    inLeader = true;
                    block--;
                    PTCReport.writeBlankReport(_report, _abspos + _pos);
                    if (block <= 0) {
                        return;
                    }
                }
                break;

            case STREAM_END:
                PTCReport.writeBreakReport(_report, _abspos + _pos);
                throw new IllegalEndDetectedException();
            }
        }

    }

    /**
     * データ読み取り
     * @return 読み取れた場合はtrue
     * @throws IOException
     */
    private boolean read() throws IOException {

        // 読み取り位置が移動位置より前なら何もしない
        if (_pos < BUF_READ_POS) return true;

        // バッファを半分移動
        System.arraycopy(_buf, BUF_HALF_SIZE, _buf, 0, BUF_HALF_SIZE);
        _pos -= BUF_HALF_SIZE;
        _abspos += BUF_HALF_SIZE;

        // 読み取り
        int len = _in.readSound(0, _buf, BUF_HALF_SIZE, BUF_HALF_SIZE);
        if (len <= 0) return false;

        // 極性反転
        if (_negate) {
            for (int i = BUF_HALF_SIZE; i < _buf.length; i++) {
                _buf[i] = -_buf[i];
            }
        }

        return true;
    }

    /**
     * スタートビットのところまで読み取り位置を進める。
     * @return スタートビットの型
     * @throws IOException
     */
    private StartBitType detectStartBit() throws IOException {
        for (int i = 0; i < MIN_LEADER_LEN; i++) {

            //読み込めない場合は、終了扱い
            if (!read()) return StartBitType.STREAM_END;

            int oldpos = _pos;
            _pos = _fsk.getNextZeroPosition(_buf, oldpos);
            if (!_fsk.isHighFrequency(oldpos, _pos)) {
                return StartBitType.START_BIT;
            }
        }

        return StartBitType.LEADER;
    }

    /**
     * １バイトのデータを読み込んで書き込む。
     * @return 
     * @throws IOException
     */
    private boolean readAndWriteByte() throws IOException {

        FSKBitAnalyzer.SignalJudge judge = FSKBitAnalyzer.SignalJudge.A;
        int value = 0;
        int oldpos = _pos;

        //下位ビットから８ビット分読み込む。
        for (int i = 0; i < 8; i++) {
            if (!read()) return false;

            oldpos = _pos;
            _pos = _fsk.getNextZeroPosition(_buf, oldpos);
            boolean mark = _fsk.isHighFrequency(oldpos, _pos);
            judge = FSKBitAnalyzer.min(judge, _fsk.judgeBit(_buf, oldpos, _pos, mark));
            if (mark) {
                value |= (1 << i);
                _pos = _fsk.getNextZeroPosition(_buf, _pos);    //高周波数の場合は1波スキップ
            }
        }

        _out.write(value);
        PTCReport.writeDataReport(_report, _abspos + _pos, _destpos, value, judge);
        _destpos++;

        return true;
    }

}
