package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.model.Achievements;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** Профиль ребёнка: достижения, прогресс занятий и выход из аккаунта. */
public class ProfileScreen extends BaseScreen {

    public ProfileScreen(UdmurtGame game) {
        super(game);
        build();
    }

    private void build() {
        User user = game.getUser();
        if (user == null) {
            goToMenu();
            return;
        }

        root.add(header("Профиль", true)).growX().top().row();

        Table body = new Table();
        body.pad(20);
        body.top();

        body.add(buildAccountRow(user)).growX().padBottom(16).row();
        body.add(buildModeRow(user)).growX().padBottom(16).row();
        body.add(buildStatsRow(user)).growX().padBottom(16).row();
        body.add(buildProgress(user)).growX().padBottom(18).row();
        body.add(Ui.label(skin, "Достижения", "h1")).left().padBottom(10).row();
        body.add(buildBadges(user)).row();
        body.add(buildButtons()).right().padTop(18).row();

        ScrollPane scroll = new ScrollPane(body, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        root.add(scroll).expand().fill().row();
    }

    private Table buildAccountRow(User user) {
        Table row = new Table();

        Table avatar = new Table();
        avatar.setBackground(assets.rounded(Assets.PURPLE, Color.WHITE, 42));
        String initial = user.name != null && !user.name.isBlank()
                ? user.name.trim().substring(0, 1).toUpperCase() : "?";
        Label letter = new Label(initial, new LabelStyle(assets.fontTitle, Color.WHITE));
        avatar.add(letter);
        row.add(avatar).size(88, 88).padRight(18);

        Table info = new Table();
        info.add(Ui.label(skin, user.name, "title")).left().row();
        info.add(Ui.label(skin,
                "Профиль: " + user.role.label.toLowerCase()
                        + " · Серия: " + user.streak + " дн.", "muted")).left().padTop(4).row();

        row.add(info).expandX().left();
        return row;
    }

    /** Переключатель режима: дошкольник или школьник. */
    private Table buildModeRow(final User user) {
        Table card = Ui.card(skin, "panel");
        card.pad(14);

        card.add(Ui.label(skin, "Режим занятий", "h1")).left().row();
        card.add(Ui.label(skin,
                        "Выберите, какие игры показывать: для малышей или для школьников.",
                        "muted"))
                .left().padTop(4).padBottom(12).row();

        Table buttons = new Table();
        buttons.add(modeButton(user, Role.PRESCHOOL, "Дошкольник"))
                .growX().height(54).padRight(10);
        buttons.add(modeButton(user, Role.SCHOOL, "Школьник"))
                .growX().height(54);
        card.add(buttons).growX().row();
        return card;
    }

    private TextButton modeButton(final User user, final Role role, String title) {
        boolean active = user.role == role;
        return Ui.button(skin, title, active ? "primary" : "ghost", new Ui.Action() {
            @Override
            public void run() {
                if (user.role == role) return;
                user.role = role;
                game.users.update(user);
                goTo(new ProfileScreen(game));
            }
        });
    }

    private Table buildStatsRow(User user) {
        Table row = new Table();
        row.add(stat(String.valueOf(user.stars), "★ Звёзд")).growX().pad(6);
        row.add(stat(String.valueOf(user.lessons), "Занятий")).growX().pad(6);
        row.add(statTwo("Викторина: " + user.bestQuiz + "/10",
                "Память: " + user.bestMemory + " ★")).growX().pad(6);
        return row;
    }

    private Table stat(String value, String caption) {
        Table box = Ui.card(skin, "panel");
        box.pad(12);
        box.add(Ui.label(skin, value, "title")).row();
        box.add(Ui.label(skin, caption, "muted")).row();
        return box;
    }

    private Table statTwo(String line1, String line2) {
        Table box = Ui.card(skin, "panel");
        box.pad(12);
        box.add(Ui.label(skin, "Рекорды", "muted")).left().padBottom(4).row();
        box.add(Ui.label(skin, line1, "small")).left().row();
        box.add(Ui.label(skin, line2, "small")).left().row();
        return box;
    }

    /** Полоса прогресса: сколько занятий пройдено из цели в 10. */
    private Table buildProgress(User user) {
        int target = 10;
        float fraction = MathUtils.clamp((float) user.lessons / target, 0f, 1f);

        Table wrap = new Table();
        wrap.add(Ui.label(skin, "Флагманская цель: " + user.lessons + " из " + target + " занятий", "h1"))
                .left().padBottom(8).row();

        Table track = new Table();
        track.setBackground(assets.rounded(Color.valueOf("E9E2D4"), null, 14));
        Table fill = new Table();
        fill.setBackground(assets.rounded(Assets.GREEN, null, 14));
        track.add(fill).width(420 * fraction).height(26).left();
        wrap.add(track).width(420).height(26).row();

        wrap.add(Ui.label(skin, milestoneHint(user.lessons), "muted")).left().padTop(6);
        return wrap;
    }

    private String milestoneHint(int lessons) {
        if (lessons >= 10) return "Цель достигнута! Ты молодец!";
        return "Ещё " + (10 - lessons) + " занятие(й) — и цель будет достигнута!";
    }

    private Table buildBadges(User user) {
        Table grid = new Table();
        Table row = new Table();
        int column = 0;
        for (Achievements.Badge badge : Achievements.forUser(user)) {
            row.add(badge(badge)).size(190, 120).pad(6);
            if (++column % 3 == 0) {
                grid.add(row).row();
                row = new Table();
            }
        }
        if (column % 3 != 0) grid.add(row);
        return grid;
    }

    private Table badge(Achievements.Badge achievement) {
        Table card = Ui.card(skin, achievement.earned ? "card" : "panel");
        card.pad(12);

        Label guess = new Label(achievement.earned ? "★" : "•",
                new LabelStyle(assets.fontBig,
                        achievement.earned ? Assets.GREEN : Color.valueOf("C9BBA3")));
        card.add(guess).padBottom(6).row();

        Label name = new Label(achievement.name,
                new LabelStyle(assets.fontSmall,
                        achievement.earned ? Assets.TEXT : Assets.MUTED));
        name.setAlignment(Align.center);
        card.add(name).growX().padBottom(3).row();

        Label desc = new Label(achievement.description,
                new LabelStyle(assets.fontSmall, Assets.MUTED));
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        card.add(desc).width(165).growX().row();
        return card;
    }

    private Table buildButtons() {
        Table buttons = new Table();
        buttons.add(Ui.button(skin, "‹ В меню", "primary", this::goToMenu)).height(52).padRight(14);
        buttons.add(Ui.button(skin, "Выйти из профиля", "danger", this::logout)).height(52);
        return buttons;
    }
}