package com.udmurt.learn.data;

import com.badlogic.gdx.utils.Array;
import com.udmurt.learn.model.Role;

/**
 * Набор коротких удмуртских фраз для игры «Составь предложение».
 *
 * ВНИМАНИЕ: фразы собраны для демонстрации из уже известных слов; перед
 * реальным использованием перевод стоит проверить у носителя языка.
 */
public final class PhraseBank {

    public static final Array<Phrase> ALL = new Array<>();

    private PhraseBank() {
    }

    private static void add(String udmurt, String russian) {
        ALL.add(new Phrase(udmurt, russian));
    }

    static {
        // «Это …»
        add("Та шунды", "Это солнце");
        add("Та толэзь", "Это луна");
        add("Та писпу", "Это дерево");
        add("Та сяська", "Это цветок");
        add("Та книга", "Это книга");
        add("Та чорыг", "Это рыба");

        // «… и …»
        add("анай но атай", "мама и папа");
        add("нянь но йӧл", "хлеб и молоко");
        add("шунды но толэзь", "солнце и луна");
        add("кион но ӟичы", "волк и лиса");
        add("курег но чорыг", "курица и рыба");
        add("горд но вож", "красный и зелёный");
        add("син но пель", "глаз и ухо");

        // «Я …»
        add("Мон пинал", "Я ребёнок");
        add("Мон анай", "Я мама");
        add("Мон атай", "Я папа");
    }

    /** Фразы, доступные ребёнку с выбранной ролью. */
    public static Array<Phrase> forRole(Role role) {
        return new Array<>(ALL);
    }
}
