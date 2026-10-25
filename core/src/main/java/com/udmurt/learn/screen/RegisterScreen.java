package com.udmurt.learn.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.PasswordField;
import com.udmurt.learn.ui.Ui;

/** Регистрация: ФИО (логин), пароль, повтор пароля и выбор роли. */
public class RegisterScreen extends BaseScreen {

    private boolean busy;
    private TextField nameField;
    private PasswordField passwordField;
    private PasswordField confirmField;
    private Label errorLabel;
    private Label statusLabel;
    private Label roleHint;
    private TextButton preschoolButton;
    private TextButton schoolButton;

    public RegisterScreen(UdmurtGame game) {
        super(game);
        build();
    }

    private void build() {
        Table card = Ui.card(skin, "panel");
        card.pad(34);

        card.add(Ui.label(skin, "Регистрация", "title")).padBottom(4).row();
        card.add(centered(skin, "Создайте профиль, чтобы начать учиться", "muted")).padBottom(22).row();

        nameField = new TextField("", skin);
        nameField.setMessageText("ФИО — фамилия, имя и отчество");
        nameField.setTextFieldFilter(russianOnly());
        card.add(nameField).growX().height(56).padBottom(12).row();

        passwordField = new PasswordField(skin, "Пароль");
        card.add(passwordField).growX().padBottom(12).row();

        confirmField = new PasswordField(skin, "Повторите пароль");
        card.add(confirmField).growX().padBottom(20).row();

        card.add(Ui.label(skin, "Кто учится?", "h1")).left().padBottom(10).row();

        preschoolButton = new TextButton("Дошкольник", skin, "toggle");
        schoolButton = new TextButton("Школьник", skin, "toggle");
        ButtonGroup<TextButton> group = new ButtonGroup<>(preschoolButton, schoolButton);
        group.setMinCheckCount(1);
        group.setMaxCheckCount(1);
        preschoolButton.setChecked(true);

        ChangeListener roleListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateRoleHint();
            }
        };
        preschoolButton.addListener(roleListener);
        schoolButton.addListener(roleListener);

        Table roles = new Table();
        roles.add(preschoolButton).growX().height(56).padRight(12);
        roles.add(schoolButton).growX().height(56);
        card.add(roles).growX().padBottom(10).row();

        roleHint = centered(skin, "", "muted");
        card.add(roleHint).growX().padBottom(14).row();

        errorLabel = centered(skin, "", "error");
        card.add(errorLabel).growX().height(40).padBottom(6).row();

        statusLabel = centered(skin, "", "muted");
        card.add(statusLabel).growX().height(30).padBottom(8).row();

        card.add(Ui.button(skin, "Зарегистрироваться", "primary", this::doRegister))
                .growX().height(58).padBottom(12).row();
        card.add(Ui.button(skin, "Уже есть аккаунт — войти", "ghost",
                () -> goTo(new LoginScreen(game)))).growX().height(52).row();

        root.add(card).width(640).expand().center();
        updateRoleHint();
    }

    private TextFieldFilter russianOnly() {
        return new TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                if (c == 'ё' || c == 'Ё') return true;
                if (Character.isWhitespace(c) || c == '-') return true;
                return c >= 'А' && c <= 'я';
            }
        };
    }

    private void updateRoleHint() {
        Role role = selectedRole();
        roleHint.setText(role.description);
    }

    private Role selectedRole() {
        return schoolButton.isChecked() ? Role.SCHOOL : Role.PRESCHOOL;
    }

    /** Количество слов в ФИО. */
    private static int wordCount(String value) {
        int count = 0;
        for (String part : value.trim().split("\\s+")) {
            if (!part.isBlank()) count++;
        }
        return count;
    }

    private void doRegister() {
        if (busy) return;

        try {
            String name = nameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();
            game.trace("register: submit name='" + name + "'");

            if (name.isEmpty()) {
                errorLabel.setText("Введите ФИО русскими буквами");
                return;
            }
            if (wordCount(name) < 3) {
                errorLabel.setText("ФИО должно состоять из 3 слов: фамилия, имя, отчество");
                return;
            }
            if (game.users.exists(name)) {
                errorLabel.setText("Такое ФИО уже зарегистрировано");
                return;
            }
            if (password.length() < 4) {
                errorLabel.setText("Пароль должен быть не короче 4 символов");
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setText("Пароли не совпадают");
                return;
            }

            User user = game.users.register(name, password, selectedRole());
            game.signIn(user);

            errorLabel.setText("");
            statusLabel.setText("Создаём аккаунт на сервере…");
            busy = true;
            game.syncWithServer(user, password, new Runnable() {
                @Override
                public void run() {
                    busy = false;
                    game.trace("register: sync callback, token=" + game.remoteToken());
                    try {
                        statusLabel.setText(game.remoteToken() == null
                                ? "Сервер недоступен — аккаунт сохранён локально" : "");
                        goTo(new MainMenuScreen(game));
                    } catch (Throwable t) {
                        game.logCrash(t);
                        errorLabel.setText("Ошибка: " + t.getClass().getSimpleName());
                    }
                }
            });
        } catch (Throwable t) {
            busy = false;
            game.logCrash(t);
            errorLabel.setText("Ошибка: "
                    + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
        }
    }
}
