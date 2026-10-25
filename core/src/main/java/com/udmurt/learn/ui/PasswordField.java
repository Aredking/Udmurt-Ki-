package com.udmurt.learn.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Поле пароля с кнопкой-«глазом»: показать или скрыть введённый пароль. */
public class PasswordField extends Table {

    private final TextField field;
    private final ImageButton toggle;

    public PasswordField(Skin skin, String messageText) {
        field = new TextField("", skin);
        field.setMessageText(messageText);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');

        ImageButtonStyle eyeStyle = new ImageButtonStyle();
        eyeStyle.imageUp = skin.getDrawable("eye-off");
        eyeStyle.imageChecked = skin.getDrawable("eye-open");
        toggle = new ImageButton(eyeStyle);

        toggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean visible = toggle.isChecked();
                field.setPasswordMode(!visible);
                field.setCursorPosition(field.getText().length());
            }
        });

        add(field).growX().height(56);
        add(toggle).size(46, 48).padLeft(10);
    }

    public String getText() {
        return field.getText();
    }

    public void setTextFieldListener(TextFieldListener listener) {
        field.setTextFieldListener(listener);
    }

    public TextField getField() {
        return field;
    }
}
