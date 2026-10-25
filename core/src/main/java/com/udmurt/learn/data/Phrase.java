package com.udmurt.learn.data;

import com.badlogic.gdx.utils.Array;

/**
 * Простая фраза для игры «Составь предложение»: удмуртский текст и перевод.
 * Слова-плитки берутся из {@link #udmurt}, разбитого по пробелам.
 */
public class Phrase {

    public final String udmurt;
    public final String russian;
    public final Array<String> words;

    public Phrase(String udmurt, String russian) {
        this.udmurt = udmurt;
        this.russian = russian;
        this.words = new Array<>(udmurt.split(" "));
    }
}
