package com.udmurt.learn.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Небольшие помощники для сборки интерфейса. */
public final class Ui {

    private Ui() {
    }

    public interface Action {
        void run();
    }

    public static Label label(Skin skin, String text) {
        return new Label(text, skin);
    }

    public static Label label(Skin skin, String text, String style) {
        return new Label(text, skin, style);
    }

    public static TextButton button(Skin skin, String text, String style, Action action) {
        TextButton button = new TextButton(text, skin, style);
        if (action != null) {
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    action.run();
                }
            });
        }
        return button;
    }

    public static Table card(Skin skin, String drawable) {
        Table table = new Table();
        table.setBackground(skin.getDrawable(drawable));
        return table;
    }
}
