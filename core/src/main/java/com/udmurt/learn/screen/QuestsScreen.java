package com.udmurt.learn.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.model.Achievements;
import com.udmurt.learn.model.QuestPath;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** Квесты: карта-путь уровней, ежедневные задания и достижения. */
public class QuestsScreen extends BaseScreen {

    private final Table content = new Table();

    public QuestsScreen(UdmurtGame game) {
        super(game);
        User user = game.getUser();
        if (user == null) {
            goToMenu();
            return;
        }
        user.ensureToday();

        root.add(header("Квесты", true)).growX().top().row();

        final TextButton pathTab = new TextButton("Путь", skin, "toggle");
        final TextButton dailyTab = new TextButton("Задания дня", skin, "toggle");
        final TextButton achTab = new TextButton("Достижения", skin, "toggle");
        ButtonGroup<TextButton> group = new ButtonGroup<>(pathTab, dailyTab, achTab);
        group.setMinCheckCount(1);
        group.setMaxCheckCount(1);

        pathTab.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { if (pathTab.isChecked()) buildPath(); }
        });
        dailyTab.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { if (dailyTab.isChecked()) buildDaily(); }
        });
        achTab.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { if (achTab.isChecked()) buildAchievements(); }
        });
        pathTab.setChecked(true);

        Table tabs = new Table();
        tabs.add(pathTab).growX().height(50).padRight(8);
        tabs.add(dailyTab).growX().height(50).padRight(8);
        tabs.add(achTab).growX().height(50);
        root.add(tabs).growX().pad(12, 22, 6, 22).row();

        content.top();
        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        root.add(scroll).expand().fill().pad(0, 16, 0, 16).row();

        buildPath();
    }

    // ------------------------------------------------------------------
    // Путь
    // ------------------------------------------------------------------

    private void buildPath() {
        content.clearChildren();
        User user = game.getUser();
        int completed = QuestPath.completedCount(user);

        content.add(Ui.label(skin, "Пройдено узлов: " + completed
                + " из " + QuestPath.nodes().size, "h1"))
                .left().pad(8, 8, 12, 8).row();

        int index = 0;
        for (QuestPath.Node node : QuestPath.nodes()) {
            boolean done = node.goal.done(user);
            boolean unlocked = index <= completed;
            content.add(pathRow(node, done, unlocked)).growX().padBottom(8).row();
            index++;
        }
    }

    private Table pathRow(final QuestPath.Node node, boolean done, boolean unlocked) {
        Table card = Ui.card(skin, done ? "mint" : "panel");
        card.pad(12, 16, 12, 16);

        Label icon = new Label(done ? "★" : unlocked ? "▶" : "•",
                new LabelStyle(assets.fontBig,
                        done ? Assets.GREEN : unlocked ? Assets.BLUE : Color.valueOf("C9BBA3")));
        card.add(icon).width(44);

        Table texts = new Table();
        texts.add(Ui.label(skin, node.title, "h1")).left().row();
        texts.add(Ui.label(skin, node.description, "muted")).left().padTop(2).row();
        card.add(texts).expandX().left().padLeft(6);

        if (done) {
            card.add(new Label("пройдено", new LabelStyle(assets.fontSmall, Assets.GREEN)))
                    .right().padRight(10);
        } else if (unlocked) {
            card.add(Ui.button(skin, "Играть", "primary",
                    () -> goTo(screenFor(node.gameKey)))).width(150).height(52).right();
        } else {
            TextButton locked = Ui.button(skin, "Закрыто", "ghost", null);
            locked.setDisabled(true);
            locked.getColor().a = 0.6f;
            card.add(locked).width(150).height(52).right();
        }
        return card;
    }

    // ------------------------------------------------------------------
    // Ежедневные квесты
    // ------------------------------------------------------------------

    private void buildDaily() {
        content.clearChildren();
        User user = game.getUser();

        content.add(Ui.label(skin, "Задания дня", "h1")).left().pad(8, 8, 2, 8).row();
        content.add(Ui.label(skin, "Обновляются каждый день", "muted")).left().pad(0, 8, 12, 8).row();

        content.add(dailyCard("Звёздный день", "Заработай 20 ★ сегодня",
                Math.min(1f, user.dailyStars / 20f), user.dailyStars + " / 20",
                10, user.dailyStars >= 20, user.claimedStars, "stars")).growX().padBottom(10).row();

        content.add(dailyCard("Три занятия", "Пройди 3 игры сегодня",
                Math.min(1f, user.dailyGames / 3f), user.dailyGames + " / 3",
                10, user.dailyGames >= 3, user.claimedGames, "games")).growX().padBottom(10).row();

        content.add(dailyCard("Без ошибок", "Пройди игру без единой ошибки",
                user.dailyPerfect ? 1f : 0f, user.dailyPerfect ? "выполнено" : "не выполнено",
                15, user.dailyPerfect, user.claimedPerfect, "perfect")).growX().padBottom(10).row();
    }

    private Table dailyCard(String title, String desc, float fraction, String progressText,
                            int reward, boolean complete, boolean claimed, final String key) {
        Table card = Ui.card(skin, claimed ? "mint" : "panel");
        card.pad(14, 16, 14, 16);

        Table texts = new Table();
        texts.add(Ui.label(skin, title, "h1")).left().row();
        texts.add(Ui.label(skin, desc, "muted")).left().padTop(2).row();
        card.add(texts).expandX().left();

        Table barBox = new Table();
        com.udmurt.learn.ui.ProgressBar bar = new com.udmurt.learn.ui.ProgressBar(assets, 220);
        bar.set(fraction);
        barBox.add(bar.table()).width(220).height(18).padBottom(4).row();
        barBox.add(Ui.label(skin, progressText, "small")).left().row();
        card.add(barBox).width(240).padRight(14);

        if (claimed) {
            card.add(new Label("Получено", new LabelStyle(assets.fontSmall, Assets.GREEN)))
                    .width(150).right();
        } else if (complete) {
            card.add(Ui.button(skin, "Забрать " + reward + "★", "primary", () -> claim(key, reward)))
                    .width(150).height(54).right();
        } else {
            TextButton pending = Ui.button(skin, "В процессе", "ghost", null);
            pending.setDisabled(true);
            card.add(pending).width(150).height(54).right();
        }
        return card;
    }

    private void claim(String key, int reward) {
        User user = game.getUser();
        if (user == null) return;
        if ("stars".equals(key)) user.claimedStars = true;
        else if ("games".equals(key)) user.claimedGames = true;
        else user.claimedPerfect = true;
        user.addStars(reward);
        game.onUserUpdated(user);
        buildDaily();
    }

    // ------------------------------------------------------------------
    // Достижения
    // ------------------------------------------------------------------

    private void buildAchievements() {
        content.clearChildren();
        User user = game.getUser();

        content.add(Ui.label(skin, "Достижения: " + Achievements.earnedCount(user)
                + " из " + Achievements.forUser(user).size, "h1"))
                .left().pad(8, 8, 12, 8).row();

        Table grid = new Table();
        Table row = new Table();
        int column = 0;
        for (Achievements.Badge badge : Achievements.forUser(user)) {
            row.add(badgeCard(badge)).size(200, 120).pad(6);
            if (++column % 3 == 0) {
                grid.add(row).row();
                row = new Table();
            }
        }
        if (column % 3 != 0) grid.add(row);
        content.add(grid).left().row();
    }

    private Table badgeCard(Achievements.Badge achievement) {
        Table card = Ui.card(skin, achievement.earned ? "mint" : "panel");
        card.pad(12);
        Label icon = new Label(achievement.earned ? "★" : "•",
                new LabelStyle(assets.fontBig,
                        achievement.earned ? Assets.GREEN : Color.valueOf("C9BBA3")));
        card.add(icon).padBottom(6).row();
        Label name = new Label(achievement.name,
                new LabelStyle(assets.fontSmall, achievement.earned ? Assets.TEXT : Assets.MUTED));
        name.setAlignment(Align.center);
        card.add(name).growX().padBottom(3).row();
        Label desc = new Label(achievement.description, new LabelStyle(assets.fontSmall, Assets.MUTED));
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        card.add(desc).width(170).growX().row();
        return card;
    }

    // ------------------------------------------------------------------

    private Screen screenFor(String key) {
        return Screens.of(game, key);
    }
}
