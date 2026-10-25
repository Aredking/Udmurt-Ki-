package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.utils.JsonValue;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.ServerApi;
import com.udmurt.learn.data.ServerConfig;
import com.udmurt.learn.ui.Ui;

/**
 * Настройка адреса игрового сервера. Нужна, чтобы телефон нашёл сервер,
 * запущенный на компьютере (по умолчанию в Android-эмуляторе адрес 10.0.2.2).
 */
public class ServerSettingsScreen extends BaseScreen {

    private TextField urlField;
    private Label statusLabel;

    public ServerSettingsScreen(UdmurtGame game) {
        super(game);
        build();
    }

    private void build() {
        Table bar = new Table();
        bar.setBackground(skin.getDrawable("headerbar"));
        bar.pad(12, 18, 12, 18);
        bar.add(Ui.button(skin, "‹ Назад", "ghost", this::back)).height(46).padRight(16);
        bar.add(Ui.label(skin, "Сервер", "header")).expandX().left();

        root.add(bar).growX().top().row();
        root.add(card()).width(640).expand().center();
    }

    private Table card() {
        Table card = Ui.card(skin, "panel");
        card.pad(30);

        card.add(Ui.label(skin, "Адрес игрового сервера", "title")).left().padBottom(6).row();

        Label hint = new Label(
                "Рейтинг и вход работают через общий сервер. Из любой сети используйте адрес "
                        + "ПК в Tailscale, например http://100.94.94.121:8080 (по умолчанию). "
                        + "В одной Wi-Fi сети можно указать обычный IP, например "
                        + "http://192.168.31.128:8080",
                new LabelStyle(assets.fontSmall, Assets.MUTED));
        hint.setWrap(true);
        card.add(hint).width(560).left().padBottom(18).row();

        urlField = new TextField(ServerConfig.baseUrl(), skin);
        urlField.setMessageText("http://192.168.0.10:8080");
        urlField.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField field, char c) {
                if (c == '\r' || c == '\n') check();
            }
        });
        card.add(urlField).growX().height(58).padBottom(10).row();

        statusLabel = new Label("", new LabelStyle(assets.fontSmall, Color.WHITE));
        statusLabel.setWrap(true);
        card.add(statusLabel).width(580).left().height(52).padBottom(8).row();

        Table buttons = new Table();
        buttons.add(Ui.button(skin, "Проверить связь", "ghost", this::check))
                .growX().height(58).padRight(12);
        buttons.add(Ui.button(skin, "Сохранить", "primary", this::save))
                .growX().height(58);
        card.add(buttons).growX().padBottom(6).row();

        if (game.platform.isAndroid()) {
            addTailscaleSection(card);
        }

        return card;
    }

    /** Блок установки/запуска Tailscale (только на Android). */
    private void addTailscaleSection(Table card) {
        card.add(Ui.label(skin, "Доступ из любой сети", "h1"))
                .left().padTop(14).padBottom(4).row();

        Label tsStatus = new Label("", new LabelStyle(assets.fontSmall, Assets.MUTED));
        tsStatus.setWrap(true);

        if (game.platform.hasTailscale()) {
            tsStatus.setText("Tailscale установлен. Включите его и войдите тем же "
                    + "аккаунтом, что и на ПК.");
            card.add(Ui.button(skin, "Открыть Tailscale", "accent",
                    game.platform::openTailscale)).growX().height(56).padBottom(6).row();
        } else {
            tsStatus.setText("Установите Tailscale — вход и рейтинг заработают с любого "
                    + "интернета, а не только в одной Wi-Fi сети.");
            card.add(Ui.button(skin, "Установить Tailscale", "accent",
                    game.platform::installTailscale)).growX().height(56).padBottom(6).row();
        }
        card.add(tsStatus).width(580).left().row();
    }

    /** Сохраняет адрес и пересоздаёт клиент сервера. */
    private void apply() {
        String url = normalize(urlField.getText());
        if (!url.isEmpty()) {
            ServerConfig.setBaseUrl(url);
            game.reloadServer();
            urlField.setText(url);
        }
    }

    private void save() {
        String url = normalize(urlField.getText());
        if (url.isEmpty()) {
            status("Введите адрес сервера", Assets.RED);
            return;
        }
        apply();
        status("Сохранено: " + url, Assets.GREEN);
    }

    private void check() {
        String url = normalize(urlField.getText());
        if (url.isEmpty()) {
            status("Введите адрес сервера", Assets.RED);
            return;
        }
        apply();
        status("Проверяем " + url + " …", Assets.MUTED);
        game.server.leaderboard(1, new ServerApi.Listener() {
            @Override
            public void onSuccess(JsonValue json) {
                status("Связь есть! Сервер отвечает.", Assets.GREEN);
            }

            @Override
            public void onError(String message) {
                status("Нет связи с " + ServerConfig.baseUrl()
                        + ". Проверьте, что сервер запущен на ПК и брандмауэр "
                        + "разрешает входящий порт 8080.", Assets.RED);
            }
        });
    }

    private void back() {
        apply();
        goTo(new LoginScreen(game));
    }

    private void status(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setColor(color);
    }

    private static String normalize(String raw) {
        String url = raw == null ? "" : raw.trim();
        if (url.isEmpty()) return "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
