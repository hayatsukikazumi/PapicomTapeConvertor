/**
 * @(#)SimpleDialog.java  2005/08/04
 * 
 * Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * WAV2P6の共通ダイアログクラス
 * 
 * @author HayatsukiKazumi
 * @version 1.0.0
 */
public class SimpleDialog extends Dialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Button _okButton;

    /**
     * 指定されたパラメータでダイアログを構築する。
     * 
     * @param owner ダイアログのオーナー（開いた元）
     * @param title タイトル
     * @param content 内容
     */
    public SimpleDialog(Frame owner, String title, String content) {
        super(owner, title, true);
        setSize(320, 150);
        setResizable(false);
        setLayout(new BorderLayout(5, 5));

        add(getDummyCanvas(), BorderLayout.NORTH);
        add(getDummyCanvas(), BorderLayout.WEST);
        add(getDummyCanvas(), BorderLayout.EAST);

        MultiLabel c1 = new MultiLabel(content);
        c1.setSize(300, 80);
        add(c1, BorderLayout.CENTER);

        _okButton = new Button("  OK  ");
        Container c2 = new Container();
        c2.setLayout(new FlowLayout(FlowLayout.CENTER));
        c2.add(_okButton);

        add(c2, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        _okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                quit();
            }

        });

    }

    /**
     * エラーのダイアログを表示する。
     * 
     * @param owner ダイアログのオーナー（開いた元）
     * @param content 内容
     */
    public static void showErrorDialog(Frame owner, String errCode,
            String content) {
        SimpleDialog dlg = new SimpleDialog(owner, "エラー (" + errCode + ")",
                content);
        dlg.setToCenter();
        dlg.setVisible(true);
    }

    /**
     * バージョン情報のダイアログを表示する。
     * 
     * @param owner ダイアログのオーナー（開いた元）
     * @param name ソフト名
     * @param version バージョン
     * @param copyright 著作権情報
     */
    public static void showAboutDialog(Frame owner, String name,
            String version, String copyright) {
        String content = name + "\nVer. " + version + "\n" + copyright;
        SimpleDialog dlg = new SimpleDialog(owner, "このソフトについて", content);
        dlg.setToCenter();
        dlg.setVisible(true);
    }

    /**
     * このダイアログを消す。
     * 
     */
    private void quit() {
        setVisible(false);
        dispose();
    }

    /**
     * ダイアログを画面の中心に設定する。
     * 
     */
    private void setToCenter() {
        Toolkit tk = getToolkit();
        Dimension d = tk.getScreenSize();
        int x = (d.width - getWidth()) / 2;
        int y = (d.height - getHeight()) / 2;
        this.setLocation(x, y);
    }

    private Canvas getDummyCanvas() {
        Canvas c = new Canvas();
        c.setSize(1, 1);
        return c;
    }
}
