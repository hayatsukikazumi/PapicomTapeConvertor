/**
 * @(#)P6TapeConvertor.java  2005/08/04
 *
 * Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * PAPICOM TAPE CONVERTOR（WAVファイル→P6ファイルに変換するソフトウェア）のメインクラス
 *
 * @author HayatsukiKazumi
 * @version 1.1.0
 */
public class P6TapeConvertor extends Frame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String APP_NAME = "PAPICOM TAPE CONVERTOR";

    private static final String APP_VERSION = "1.1.1";

    private static final String APP_COPYRIGHT = "Copyright(c) はやつきかづみ 2005-2019";

    private static final String MSG_OK = "変換処理が完了しました";

    private static final String MSG_ILLEGAL_END = "読み込み途中でファイルの終わりに達しました";

    private static final String MSG_WAV_FORMAT = "8ビットまたは16ビットのWAVファイルのみ対応しています";

    private static final String MSG_WRONG_FORMAT = "ファイルフォーマットが正しくありません";

    private static final String MSG_FAILED = "処理に失敗しました";

    private static final String MSG_CANNOT_READ = "ファイルを読み取ることができません";

    private static final String MSG_SKIP_TIME = "スキップ時間は、0〜1000の数値を入力して\nください（小数可）";

    private static final double SKIP_DEFAULT = 1.0;

    private MenuItem _open;

    private MenuItem _quit;

    private MenuItem _version;

    private TextField _skip;

    private Checkbox _negate;

    private Label _status;

    private FileDialog _selectFile;

    /**
     * コンストラクタ。
     *
     */
    public P6TapeConvertor() {
        super(APP_NAME);
        setSize(360, 180);
        setResizable(false);
        setLayout(null);

        MenuBar mb = new MenuBar();
        setMenuBar(mb);

        // メニューの設定
        Menu fm = new Menu("ファイル", true);
        _open = new MenuItem("開く", new MenuShortcut(KeyEvent.VK_O));
        fm.add(_open);
        _quit = new MenuItem("終了", new MenuShortcut(KeyEvent.VK_Q));
        fm.add(_quit);
        mb.add(fm);

        Menu hm = new Menu("ヘルプ", true);
        _version = new MenuItem("バージョン情報");
        hm.add(_version);
        mb.add(hm);

        // 画面の設定
        Label skipTime = new Label("スキップ時間");
        add(skipTime);
        skipTime.setBounds(40, 60, 108, 20);

        _skip = new TextField(5);
        add(_skip);
        _skip.setBounds(160, 58, 72, 24);

        Label sec = new Label("秒");
        add(sec);
        sec.setBounds(240, 60, 18, 20);

        _negate = new Checkbox();
        _negate.setLabel("極性反転する");
        add(_negate);
        _negate.setBounds(40, 90, 120, 20);

        Canvas line = new Canvas();
        line.setBackground(new Color(0xcccccc));
        add(line);
        line.setBounds(20, 125, 320, 2);

        _status = new Label();
        add(_status);
        _status.setBounds(40, 140, 280, 20);

        _selectFile = new FileDialog(this, "処理するWAVファイルを選択", FileDialog.LOAD);

        addListeners();
    }

    private void addListeners() {
        // WindowListener
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        // ActionListener（開く）
        _open.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionPerformed_open(e);
            }
        });

        // ActionListener（終了）
        _quit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionPerformed_quit(e);
            }
        });

        // ActionListener（バージョン）
        _version.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionPerformed_version(e);
            }
        });
    }

    /**
     * mainメソッド。
     * <p>
     * 実行オプション<br>
     * java -jar WAV2P6.jar [fileName] [-sXXXX] [-n]<br>
     * fileName : ファイル名<br>
     * -sXXXX : 頭だしスキップ時間をXXXX秒に設定する。<br>
     * -n : 極性反転モードにする。<br>
     * その他 ファイル名を指定する。<br>
     * ※ファイル名の指定がない場合は、GUI画面が開く。
     *
     * @param args パラメータ
     */
    public static void main(String[] args) {
        // パラメータ解析
        String fileName = null;
        String skip = String.valueOf(SKIP_DEFAULT);
        boolean negate = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-s")) {
                skip = args[i].substring(2);
            } else if (args[i].startsWith("-n")) {
                negate = true;
            } else if (args[i].startsWith("-")) {
                System.out.println(APP_NAME);
                System.out.println("Ver. " + APP_VERSION);
                System.out.println(APP_COPYRIGHT);
                System.out.println();
                System.out.println("使い方: java jar PTC.java [-sXXXX] [-n] file");
                System.out.println("  -sXXXX    スキップ時間を指定");
                System.out.println("  -n        極性反転する");
                System.exit(0);
                return;
            } else {
                fileName = args[i];
            }
        }

        // GUIモード、テキストモード切替
        if (fileName == null) {
            P6TapeConvertor window = new P6TapeConvertor();
            window._skip.setText(skip);
            window._negate.setState(negate);
            window.setVisible(true);
            return;
        }

        System.out.println(APP_NAME);
        System.out.println("Ver. " + APP_VERSION);
        System.out.println(APP_COPYRIGHT);
        System.out.println();
        System.out.println("Source File = " + fileName);

        // 実行する。
        int result = P6TapeConvertorMain.execute(null, fileName, skip, negate);
        switch (result) {
        case P6TapeConvertorMain.RESULT_FILE_NOT_FOUND:
            System.out.print(MSG_CANNOT_READ);
            break;
        case P6TapeConvertorMain.RESULT_ILLEGAL_END:
            System.out.print(MSG_ILLEGAL_END);
            break;
        case P6TapeConvertorMain.RESULT_IO_ERROR:
            System.out.print(MSG_FAILED);
            break;
        case P6TapeConvertorMain.RESULT_NOT_SUPPORTED_WAV:
            System.out.print(MSG_WAV_FORMAT);
            break;
        case P6TapeConvertorMain.RESULT_SKIP_TIME:
            System.out.print(MSG_SKIP_TIME);
            break;
        case P6TapeConvertorMain.RESULT_WRONG_FORMAT:
            System.out.print(MSG_WRONG_FORMAT);
            break;
        case P6TapeConvertorMain.RESULT_OK:
            System.out.print(MSG_OK);
            break;
        default:
        // 何もしない
        }
        System.out.println(" (" + result + ")");
        System.exit(result);
    }

    /**
     * このウインドウを閉じる。
     *
     */
    private void quit() {
        setVisible(false);
        dispose();
        _selectFile.dispose();
        System.exit(0);
    }

    /**
     * 開くメニューのアクション
     *
     * @param e イベント
     */
    private void actionPerformed_open(ActionEvent e) {

        // ファイル選択ダイアログを表示する。
        _selectFile.setVisible(true);

        // ファイルが選択されない場合は何もしない。
        if (_selectFile.getFile() == null) {
            return;
        }

        // ファイル名表示
        _status.setText("処理中 : " + _selectFile.getFile());

        // 実行する。
        int result = P6TapeConvertorMain.execute(_selectFile.getDirectory(),
                _selectFile.getFile(), _skip.getText(), _negate.getState());

        // ファイル名表示
        _status.setText("処理完了 : " + _selectFile.getFile());

        // エラーメッセージ設定
        String errMsg = null;
        switch (result) {
        case P6TapeConvertorMain.RESULT_FILE_NOT_FOUND:
            errMsg = MSG_CANNOT_READ;
            break;
        case P6TapeConvertorMain.RESULT_ILLEGAL_END:
            errMsg = MSG_ILLEGAL_END;
            break;
        case P6TapeConvertorMain.RESULT_IO_ERROR:
            errMsg = MSG_FAILED;
            break;
        case P6TapeConvertorMain.RESULT_NOT_SUPPORTED_WAV:
            errMsg = MSG_WAV_FORMAT;
            break;
        case P6TapeConvertorMain.RESULT_SKIP_TIME:
            errMsg = MSG_SKIP_TIME;
            break;
        case P6TapeConvertorMain.RESULT_WRONG_FORMAT:
            errMsg = MSG_WRONG_FORMAT;
            break;
        case P6TapeConvertorMain.RESULT_OK:
        default:
        // 何もしない
        }

        if (errMsg != null) {
            SimpleDialog.showErrorDialog(this, String.valueOf(result), errMsg);
        }
    }

    /**
     * 終了メニューのアクション
     *
     * @param e イベント
     */
    private void actionPerformed_quit(ActionEvent e) {
        quit();
    }

    /**
     * バージョン情報メニューのアクション
     *
     * @param e イベント
     */
    private void actionPerformed_version(ActionEvent e) {
        SimpleDialog
                .showAboutDialog(this, APP_NAME, APP_VERSION, APP_COPYRIGHT);
    }
}
