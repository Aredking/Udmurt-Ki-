package com.udmurt.learn.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.utils.JsonValue;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.data.ServerApi;
import com.udmurt.learn.data.ServerConfig;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.PasswordField;
import com.udmurt.learn.ui.Ui;

/** Начальный экран: вход и переход к регистрации. */
public class LoginScreen extends BaseScreen {

    private boolean busy;
    private TextField nameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Label statusLabel;

    public LoginScreen(UdmurtGame game) {
        super(game);
        build();
    }

    private void build() {
        Table card = Ui.card(skin, "panel");
        card.pad(40);

        card.add(Ui.label(skin, "Удмурт кыл", "title")).padBottom(4).row();
        card.add(centered(skin, "Учим удмуртский язык — играя!", "muted")).padBottom(28).row();

        nameField = new TextField("", skin);
        nameField.setMessageText("ФИО русскими буквами");
        nameField.setTextFieldListener(enterListener());
        card.add(nameField).growX().height(58).padBottom(14).row();

        passwordField = new PasswordField(skin, "Пароль");
        passwordField.setTextFieldListener(enterListener());
        card.add(passwordField).growX().padBottom(10).row();

        errorLabel = centered(skin, "", "error");
        card.add(errorLabel).growX().height(40).padBottom(6).row();

        statusLabel = centered(skin, "", "muted");
        card.add(statusLabel).growX().height(30).padBottom(8).row();

        TextButton login = Ui.button(skin, "Войти", "primary", this::doLogin);
        card.add(login).growX().height(58).padBottom(12).row();

        TextButton register = Ui.button(skin, "Регистрация", "accent", this::openRegister);
        card.add(register).growX().height(58).row();

        root.add(card).width(600).expand().center().row();

        TextButton serverButton = Ui.button(skin,
                "Сервер: " + ServerConfig.baseUrl(), "ghost", this::openServerSettings);
        root.add(serverButton).width(600).height(46).padTop(10).padBottom(10).row();

        if (game.platform.isAndroid() && !game.platform.hasTailscale()) {
            root.add(Ui.button(skin, "Установить Tailscale (доступ из любой сети)",
                    "accent", game.platform::installTailscale))
                    .width(600).height(46).padBottom(14);
        }
    }

    private TextFieldListener enterListener() {
        return new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\r' || c == '\n') doLogin();
            }
        };
    }

    private void doLogin() {
        if (busy) return;

        try {
            String name = nameField.getText().trim();
            String password = passwordField.getText();
            game.trace("login: submit name='" + name + "' passLen=" + password.length());

            if (name.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Введите ФИО и пароль");
                return;
            }

            final User local = game.users.authenticate(name, password);
            if (local != null) {
                game.trace("login: auth ok (local), role=" + local.role);
                errorLabel.setText("");
                statusLabel.setText("Подключение к серверу…");
                busy = true;
                game.signIn(local);
                game.trace("login: signed in, calling sync");
                game.syncWithServer(local, password, new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        game.trace("login: sync callback, token=" + game.remoteToken());
                        try {
                            statusLabel.setText(game.remoteToken() == null
                                    ? "Сервер недоступен — офлайн (" + ServerConfig.baseUrl() + ")"
                                    : "");
                            game.trace("login: opening MainMenu");
                            goTo(new MainMenuScreen(game));
                        } catch (Throwable t) {
                            game.logCrash(t);
                            errorLabel.setText("Ошибка: " + shortMessage(t));
                        }
                    }
                });
                return;
            }

            // Локально такого аккаунта (или пароля) нет — проверяем на сервере.
            game.trace("login: no local match, ask server for '" + name + "'");
            errorLabel.setText("");
            statusLabel.setText("Проверяем на сервере…");
            busy = true;
            game.server.login(name, password, new ServerApi.Listener() {
                @Override
                public void onSuccess(JsonValue json) {
                    busy = false;
                    try {
                        if (json == null || !json.has("user")) {
                            errorLabel.setText("Неверное ФИО или пароль");
                            statusLabel.setText("");
                            return;
                        }
                        JsonValue view = json.get("user");
                        Role role = "school".equalsIgnoreCase(view.getString("role", ""))
                                ? Role.SCHOOL : Role.PRESCHOOL;
                        User user = game.users.upsert(name, password, role);
                        game.signInRemote(user, json.getString("token", null));
                        game.trace("login: server auth ok, role=" + role);
                        errorLabel.setText("");
                        statusLabel.setText("");
                        goTo(new MainMenuScreen(game));
                    } catch (Throwable t) {
                        game.logCrash(t);
                        errorLabel.setText("Ошибка: " + shortMessage(t));
                    }
                }

                @Override
                public void onError(String message) {
                    busy = false;
                    game.trace("login: server err='" + message + "'");
                    if (message != null && message.toLowerCase().contains("wrong name or password")) {
                        errorLabel.setText("Неверное ФИО или пароль");
                        statusLabel.setText("");
                    } else {
                        errorLabel.setText("Нет связи с сервером");
                        statusLabel.setText("Проверьте адрес в кнопке «Сервер»: "
                                + ServerConfig.baseUrl());
                    }
                }
            });
        } catch (Throwable t) {
            busy = false;
            game.logCrash(t);
            errorLabel.setText("Ошибка: " + shortMessage(t));
        }
    }

    private static String shortMessage(Throwable t) {
        String message = t.getMessage();
        return message == null || message.isEmpty()
                ? t.getClass().getSimpleName()
                : t.getClass().getSimpleName() + ": " + message;
    }

    private void openRegister() {
        goTo(new RegisterScreen(game));
    }

    private void openServerSettings() {
        goTo(new ServerSettingsScreen(game));
    }
}
