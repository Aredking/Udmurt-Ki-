package com.udmurt.learn.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** Общая основа для всех экранов: фон, сцена и шапка. */
public abstract class BaseScreen extends ScreenAdapter {

    protected final UdmurtGame game;
    protected final Assets assets;
    protected final Skin skin;
    protected final ExtendViewport viewport;
    protected final Stage stage;
    protected final Table root;
    protected final Table content;

    protected BaseScreen(UdmurtGame game) {
        this.game = game;
        this.assets = game.assets;
        this.skin = game.assets.skin;
        this.viewport = new ExtendViewport(960, 620);
        this.stage = new Stage(viewport, game.assets.batch);
        this.root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        this.content = new Table();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Assets.BG);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    /** Безопасно переключает экран на следующий кадр. */
    protected void goTo(final Screen next) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.setScreen(next);
                dispose();
            }
        });
    }

    protected void goToMenu() {
        goTo(new MainMenuScreen(game));
    }

    protected void logout() {
        game.clearUser();
        goTo(new LoginScreen(game));
    }

    /** Шапка с заголовком, кнопкой «назад» и числом звёзд. */
    protected Table header(String title, boolean withBack) {
        Table header = new Table();
        header.setBackground(skin.getDrawable("headerbar"));
        header.pad(12, 18, 12, 18);

        if (withBack) {
            TextButton back = Ui.button(skin, "‹ Меню", "ghost", this::goToMenu);
            header.add(back).height(46).padRight(16);
        } else {
            header.add().width(4);
        }
        header.add(Ui.label(skin, title, "header")).expandX().left();

        User user = game.getUser();
        if (user != null) {
            header.add(Ui.label(skin, "★ " + user.stars, "header")).right();
        }
        return header;
    }

    /** Всплывающий итоговый блок поверх экрана. */
    protected void showResult(String title, String message, final com.udmurt.learn.ui.Ui.Action onAgain) {
        showResult(title, message, -1, onAgain);
    }

    /** Итоговый блок с наградой в звёздах (starsEarned < 0 — не показывать). */
    protected void showResult(String title, String message, int starsEarned,
                              final com.udmurt.learn.ui.Ui.Action onAgain) {
        Table scrim = new Table();
        scrim.setFillParent(true);
        scrim.setBackground(skin.getDrawable("scrim"));
        scrim.getColor().a = 0f;

        Table card = Ui.card(skin, "panel");
        card.pad(34);

        if (starsEarned >= 0) {
            Label glow = new Label("★", new Label.LabelStyle(assets.fontTitle, Assets.YELLOW));
            card.add(glow).padBottom(2).row();
            Label reward = new Label("+" + starsEarned + " ★",
                    new Label.LabelStyle(assets.fontBig, Assets.GREEN));
            card.add(reward).padBottom(12).row();
        }

        card.add(Ui.label(skin, title, "title")).padBottom(8).row();
        card.add(Ui.label(skin, message, "h1")).padBottom(24).row();
        card.add(Ui.button(skin, "Продолжить", "primary", onAgain))
                .growX().height(64).padBottom(12).row();
        card.add(Ui.button(skin, "В меню", "ghost", this::goToMenu))
                .growX().height(52);

        card.setTransform(true);
        card.setOrigin(Align.center);
        card.setScale(0.85f);
        card.addAction(Actions.scaleTo(1f, 1f, 0.22f, Interpolation.swingOut));

        scrim.add(card).width(560);
        stage.addActor(scrim);
        scrim.addAction(Actions.fadeIn(0.22f));
    }

    protected Label centered(Skin skin, String text, String style) {
        Label label = new Label(text, skin, style);
        label.setAlignment(Align.center);
        return label;
    }
}
