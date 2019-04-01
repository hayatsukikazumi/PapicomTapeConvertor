/**
 * @(#)MultiLabel.java  2005/08/04
 * 
 * Copyright(c) HayatsukiKazumi 2005 - All Rights Reserved.
 */
package com.hayatsukikazumi.ptc;

import java.awt.Container;
import java.awt.Label;
import java.util.StringTokenizer;

/**
 * 改行文字で改行された複数行のテキストを示すAWTコンポーネント。
 * 
 * @author HayatsukiKazumi
 * @version 1.0.0
 */
public class MultiLabel extends Container {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String _text;

    /**
     * 空のテキストでMultiLabelを作る。
     */
    public MultiLabel() {
        super();
        setLayout(null);
    }

    /**
     * パラメータのテキストでMultiLabelを作る。
     * 
     * @param text テキストの文字列
     */
    public MultiLabel(String text) {
        super();
        setLayout(null);
        setText(text);
    }

    /**
     * テキストの文字列を返す。
     * 
     * @return text テキストの文字列
     */
    public String getText() {
        return _text;
    }

    /**
     * テキストの文字列を設定する。
     * 
     * @param text テキストの文字列
     */
    public void setText(String text) {
        _text = text;
        replaceText(text);
    }

    private void replaceText(String text) {
        removeAll();
        setLayout(null);

        if (text == null || text.length() == 0) {
            return;
        }

        int x = 0;
        int y = 0;
        int width = 1000;
        int height = 20;
        StringTokenizer token = new StringTokenizer(text, "\r\n");

        while (token.hasMoreTokens()) {
            Label lbl = new Label(token.nextToken() + "  ");
            add(lbl);
            lbl.setBounds(x, y, width, height);
            y += height;
        }
    }
}
