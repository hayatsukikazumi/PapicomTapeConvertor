package com.hayatsukikazumi.ptc;

import java.io.PrintStream;

/**
 * 変換結果のレポートを出力するクラス。
 * @author HayaTsukiKazumi
 * @version 1.1.0
 */
public class PTCReport {

    /**
     * ヘッダ部分のレポートを書き込む。
     * @param report 出力先
     * @param position 現在のbit位置
     * @param samplingRate サンプリング周波数
     * @param skip スキップ時間
     */
    public static void writeHeaderReport(PrintStream report, long position, int samplingRate, double skip) {
        report.print("Sampling Rate = ");
        report.print(samplingRate);
        report.println(" Hz");
        report.println();
        report.print(position);
        report.print("  -  Skiped ");
        report.print(skip);
        report.println(" sec.");
    }

    /**
     * ブランク部分のレポートを書き込む。
     * @param report 出力先
     * @param position 現在のbit位置
     */
    public static void writeBlankReport(PrintStream report, long position) {
        report.print(position);
        report.println("  -  Detected 2400Hz header signal.");
    }

    /**
     * 異常終了時のレポートを書き込む。
     * @param report 出力先
     * @param position 現在のbit位置
     */
    public static void writeBreakReport(PrintStream report, long position) {
        report.print(position);
        report.println("  -  Detected illegal end of file.");
    }

    /**
     * データ部分のレポートを書き込む。
     * @param report 出力先
     * @param position 現在のbit位置
     * @param writePos P6ファイルの書き込み位置
     * @param value P6ファイルに書き込んだ値
     * @param judge 信頼度の判定
     */
    public static void writeDataReport(PrintStream report, long position, int writePos, int value,
            FSKBitAnalyzer.SignalJudge judge) {
        report.print(position);
        report.print("  -  ");
        report.print(getHex(writePos, 4));
        report.print(":");
        report.print(getHex(value, 2));
        report.print(" (");
        report.print(judge.toString());
        report.println(")");
    }

    /**
     * 16進数表現の値を返す。
     * @param value 値
     * @param digit 桁数
     * @return 16進数表現した値
     */
    private static String getHex(int value, int digit) {
        String hex = Integer.toHexString(value).toUpperCase();
        StringBuffer sb = new StringBuffer();
        for (int i = hex.length(); i < digit; i++) {
            sb.append("0");
        }
        sb.append(hex);
        return sb.toString();
    }

}
