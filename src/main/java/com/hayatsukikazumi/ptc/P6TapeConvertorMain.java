/**
 * @(#)P6TapeConvertorMain.java 2005/08/04
 * 
 * Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * WAV→P6の変換機能ロジッククラスを呼び出すクラス
 * 
 * @author HayatsukiKazumi
 * @version 1.1.0
 */
public class P6TapeConvertorMain {

    /** 処理結果（OK） */
    public static final int RESULT_OK = 0;

    /** 処理結果（スキップ時間指定誤り） */
    public static final int RESULT_SKIP_TIME = 1;

    /** 処理結果（サポートされていないWAVフォーマット） */
    public static final int RESULT_NOT_SUPPORTED_WAV = 2;

    /** 処理結果（不正なファイルを指定） */
    public static final int RESULT_WRONG_FORMAT = 3;

    /** 処理結果（ファイルが見つからない） */
    public static final int RESULT_FILE_NOT_FOUND = 4;

    /** 処理結果（途中で不正にファイル終了） */
    public static final int RESULT_ILLEGAL_END = 5;

    /** 処理結果（IOエラー） */
    public static final int RESULT_IO_ERROR = 6;

    /** 最大スキップ時間（秒） */
    public static final int SKIP_MAX_TIME = 1000;

    /** P6ファイルの拡張子 */
    public static final String EXT_P6 = ".P6";

    /** レポートファイルの拡張子 */
    public static final String EXT_REPORT = ".log";

    /**
     * 変換処理を実行する。
     * 
     * @param dirName ディレクトリ名
     * @param fileName ファイル名（必須）
     * @param skip スキップ時間（秒）（必須）
     * @param negate 極性反転フラグ
     * @return 処理結果
     */
    public static synchronized int execute(String dirName, String fileName,
            String skipTime, boolean negate) {
        // ファイルネーム生成
        int dot = fileName.lastIndexOf('.');
        String filePrefix;
        if (dot > 0) {
            filePrefix = fileName.substring(0, dot);
        } else {
            filePrefix = fileName;
        }

        File wavFile = new File(dirName, fileName);
        File p6File = new File(dirName, filePrefix + EXT_P6);
        File repFile = new File(dirName, filePrefix + EXT_REPORT);

        // ファイルが読み取り可能かをチェック
        if (!wavFile.canRead()) {
            return RESULT_FILE_NOT_FOUND;
        }

        // 既存と重ならないファイル名をつける
        int i = 0;
        while (p6File.exists() || repFile.exists()) {
            i++;
            p6File = new File(dirName, filePrefix + "_" + i + EXT_P6);
            repFile = new File(dirName, filePrefix + "_" + i + EXT_REPORT);
        }

        // スキップ時間のパラメータを取得
        double skip;
        try {
            skip = Double.parseDouble(skipTime);
        } catch (Exception e) {
            return RESULT_SKIP_TIME;
        }

        if (skip < 0 || skip > SKIP_MAX_TIME) {
            return RESULT_SKIP_TIME;
        }

        // ファイル読み書き開始
        WAVInputStream in = null;
        OutputStream out = null;
        PrintStream report = null;

        try {
            in = new WAVInputStream(new FileInputStream(wavFile));

            // WAVファイルが処理可能かチェック
            if (in.getChannels() != 1 || in.getFormatId() != 1
                    || in.getBits() != 8) {
                return RESULT_NOT_SUPPORTED_WAV;
            }

            out = new BufferedOutputStream(new FileOutputStream(p6File));
            report = new PrintStream(new FileOutputStream(repFile));
            report.print("Output File = ");
            report.println(p6File.getAbsolutePath());
            report.print("Negative Mode = ");
            report.println(negate);
            report.println();

            PTCLogic biz = new PTCLogic();
            biz.analyze(in, out, report, skip, negate);

        } catch (IOException e) {
            e.printStackTrace();
            return RESULT_IO_ERROR;
        } catch (IllegalEndDetectedException e) {
            return RESULT_ILLEGAL_END;
        } catch (WrongFormatException e) {
            return RESULT_WRONG_FORMAT;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (report != null) {
                report.close();
            }
        }

        return RESULT_OK;
    }
}
