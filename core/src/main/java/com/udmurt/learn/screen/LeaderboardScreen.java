package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.ServerApi;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Рейтинг игроков. Работает всегда: сразу показывает локальные данные, а когда
 * сервер доступен — дополняет их общим списком со всех устройств.
 */
public class LeaderboardScreen extends BaseScreen {

    private Table listContainer;
    private Label statusLabel;
    private Label sourceLabel;

    public LeaderboardScreen(UdmurtGame game) {
        super(game);
        build();
        showLocal();
        loadServer();
    }

    private void build() {
        root.add(header("Рейтинг", true)).growX().top().row();

        Table body = new Table();
        body.pad(20);
        body.top();

        statusLabel = centered(skin, "", "muted");
        body.add(statusLabel).growX().padBottom(12).row();

        listContainer = new Table();
        listContainer.top();
        body.add(listContainer).growX().top().row();

        sourceLabel = centered(skin, "", "small");
        body.add(sourceLabel).growX().padTop(12).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Обновить", "primary", this::loadServer)).height(52).width(220);
        body.add(actions).padTop(10).row();

        root.add(new ScrollPane(body, skin)).grow().row();
    }

    /** Показывает рейтинг только по игрокам этого устройства. */
    private void showLocal() {
        List<Entry> entries = sorted(localMap());
        if (entries.isEmpty()) {
            statusLabel.setText("Пока никто не играл — стань первым!");
            listContainer.clearChildren();
            sourceLabel.setText("Локальный рейтинг");
            return;
        }
        statusLabel.setText("");
        render(entries, "Локальный рейтинг");
    }

    /** Запрашивает общий рейтинг с сервера и объединяет его с локальным. */
    private void loadServer() {
        sourceLabel.setText("Обновляем с сервера…");
        game.server.leaderboard(20, new ServerApi.Listener() {
            @Override
            public void onSuccess(JsonValue json) {
                Map<String, Entry> map = localMap();
                if (json != null && json.has("users")) {
                    for (JsonValue user : json.get("users")) {
                        String name = user.getString("name", "");
                        if (name.isEmpty()) continue;
                        int stars = user.getInt("stars", 0);
                        Entry existing = map.get(name.toLowerCase());
                        if (existing == null) {
                            map.put(name.toLowerCase(), new Entry(name, stars));
                        } else {
                            existing.stars = Math.max(existing.stars, stars);
                        }
                    }
                }
                List<Entry> entries = sorted(map);
                if (entries.isEmpty()) {
                    statusLabel.setText("Пока никто не играл — стань первым!");
                    listContainer.clearChildren();
                    sourceLabel.setText("");
                    return;
                }
                statusLabel.setText("");
                render(entries, "Общий рейтинг · онлайн");
            }

            @Override
            public void onError(String message) {
                // Сервер недоступен: оставляем локальный рейтинг на экране.
                if (listContainer.getChildren().size == 0) {
                    showLocal();
                }
                sourceLabel.setText("Сервер недоступен — показан локальный рейтинг");
            }
        });
    }

    private Map<String, Entry> localMap() {
        Map<String, Entry> map = new LinkedHashMap<>();
        Array<User> users = game.users.all();
        for (User user : users) {
            if (user.name == null || user.name.isBlank()) continue;
            map.put(user.name.toLowerCase(), new Entry(user.name, user.stars));
        }
        return map;
    }

    private static List<Entry> sorted(Map<String, Entry> map) {
        List<Entry> list = new ArrayList<>(map.values());
        list.sort((a, b) -> a.stars != b.stars
                ? Integer.compare(b.stars, a.stars)
                : a.name.compareToIgnoreCase(b.name));
        return list;
    }

    private void render(List<Entry> entries, String source) {
        listContainer.clearChildren();
        User current = game.getUser();
        int rank = 1;
        for (Entry entry : entries) {
            listContainer.add(playerRow(entry, current, rank)).growX().padBottom(8).row();
            rank++;
        }
        sourceLabel.setText(source);

        if (current == null || current.name == null) return;
        boolean hasSelf = false;
        for (Entry entry : entries) {
            if (current.name.equalsIgnoreCase(entry.name)) {
                hasSelf = true;
                break;
            }
        }
        if (!hasSelf) {
            statusLabel.setText("Ты пока не в списке — играй и набирай звёзды!");
        }
    }

    private Table playerRow(Entry entry, User current, int rank) {
        boolean self = current != null && current.name != null
                && current.name.equalsIgnoreCase(entry.name);

        Table row = Ui.card(skin, self ? "card" : "panel");
        row.pad(10, 16, 10, 16);

        Label place = new Label(rank + ".", new LabelStyle(assets.fontSmall, Assets.MUTED));
        row.add(place).width(48).left();

        Color rankColor = entry.stars >= 25 ? Assets.PURPLE
                : entry.stars >= 10 ? Assets.GREEN : Assets.TEXT;
        Label stars = new Label("★ " + entry.stars, new LabelStyle(assets.fontTitle, rankColor));
        row.add(stars).width(130).left();

        row.add(Ui.label(skin, entry.name, self ? "h1" : "small")).expandX().left();

        if (self) {
            Label you = new Label(" — это ты!", new LabelStyle(assets.fontSmall, Assets.GREEN));
            row.add(you).right().padRight(4);
        }
        return row;
    }

    private static final class Entry {
        final String name;
        int stars;

        Entry(String name, int stars) {
            this.name = name;
            this.stars = stars;
        }
    }
}
