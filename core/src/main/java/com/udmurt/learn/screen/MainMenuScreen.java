package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/**
 * Главное меню: извилистая «дорожка» занятий в стиле Duolingo. Каждый узел —
 * круглая кнопка с названием игры, узлы соединены дорожкой и прокручиваются.
 */
public class MainMenuScreen extends BaseScreen {

    private static final int NODE = Assets.NODE_DIAMETER;
    private static final int W = 640;
    private static final int ROW_H = 158;
    private static final int MARGIN = 34;
    private static final float AMPLITUDE = 120f;

    private Texture roadTexture;

    public MainMenuScreen(UdmurtGame game) {
        super(game);
        game.trace("MainMenu: ctor begin");
        build();
        game.trace("MainMenu: ctor end");
    }

    private void build() {
        User user = game.getUser();
        Role role = user != null && user.role != null ? user.role : Role.PRESCHOOL;

        root.add(header("Удмурт кыл", false)).growX().top().row();
        root.add(buildBanner(role)).growX().left().pad(8, 22, 0, 22).row();

        if (user != null) {
            root.add(buildProfileCard(user)).growX().pad(8, 22, 8, 22).row();
        }

        root.add(buildPath(role)).grow().pad(4, 12, 4, 12).row();

        root.add(Ui.button(skin, "Выйти из аккаунта", "ghost", this::logout))
                .height(48).right().pad(0, 22, 14, 22).row();
    }

    /** Небольшая цветная плашка с текущим режимом занятий. */
    private Table buildBanner(Role role) {
        Color accent = role == Role.SCHOOL ? Assets.BLUE : Assets.ORANGE;
        Table banner = new Table();
        banner.setBackground(assets.rounded(accent, null, 14));
        banner.pad(7, 14, 7, 14);

        Label text = new Label(bannerText(role), skin, "small");
        text.setColor(Color.WHITE);
        banner.add(text).left();
        return banner;
    }

    private static String bannerText(Role role) {
        return role == Role.SCHOOL
                ? "Школьный режим · чтение, письмо, викторины"
                : "Дошкольный режим · картинки и весёлые игры";
    }

    // ------------------------------------------------------------------
    // Дорожка
    // ------------------------------------------------------------------

    private ScrollPane buildPath(Role role) {
        Lesson[] lessons = lessons(role);
        int count = lessons.length;
        int height = MARGIN * 2 + count * ROW_H;

        int[] centerX = new int[count];
        int[] centerY = new int[count];
        for (int i = 0; i < count; i++) {
            centerX[i] = W / 2 + Math.round((float) (Math.sin(i * 0.85) * AMPLITUDE));
            centerY[i] = MARGIN + ROW_H / 2 + i * ROW_H;
        }

        roadTexture = buildRoad(centerX, centerY, W, height);

        Group path = new Group();
        path.setSize(W, height);

        Image road = new Image(new TextureRegionDrawable(new TextureRegion(roadTexture)));
        road.setSize(W, height);
        path.addActor(road);

        for (int i = 0; i < count; i++) {
            path.addActor(node(lessons[i], i, centerX[i], height - centerY[i]));
        }

        Table wrap = new Table();
        wrap.top();
        wrap.add(path).width(W).height(height);

        ScrollPane scroll = new ScrollPane(wrap, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        return scroll;
    }

    /** Круглый узел-кнопка с подписью названия игры. */
    private Table node(final Lesson lesson, int index, int centerX, int centerY) {
        TextButton circle = new TextButton(String.valueOf(index + 1), skin, "circle" + (index % 8));
        circle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goTo(Screens.of(game, lesson.key));
            }
        });

        Label caption = new Label(lesson.title, skin, "small");
        caption.setAlignment(Align.center);
        caption.setWrap(true);

        Table node = new Table();
        node.add(circle).size(NODE).row();
        node.add(caption).width(176).padTop(6).row();
        node.pack();

        node.setPosition(centerX - node.getWidth() / 2f,
                centerY + NODE / 2f - node.getHeight());
        return node;
    }

    /** Рисует дорожку между узлами в отдельную текстуру. */
    private Texture buildRoad(int[] centerX, int[] centerY, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        Color border = Assets.LINE;
        Color inner = Color.valueOf("F4E9D6");
        for (int i = 0; i < centerX.length - 1; i++) {
            segment(pixmap, centerX[i], centerY[i], centerX[i + 1], centerY[i + 1], 17, border);
        }
        for (int i = 0; i < centerX.length - 1; i++) {
            segment(pixmap, centerX[i], centerY[i], centerX[i + 1], centerY[i + 1], 8, inner);
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static void segment(Pixmap pixmap, int x1, int y1, int x2, int y2,
                                int radius, Color color) {
        pixmap.setColor(color);
        int distance = Math.max(1, Math.round((float) Math.hypot(x2 - x1, y2 - y1)));
        for (int step = 0; step <= distance; step++) {
            float t = step / (float) distance;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            pixmap.fillCircle(x, y, radius);
        }
    }

    /** Кликабельная карточка аккаунта: «Привет, …» → профиль. */
    private Table buildProfileCard(final User user) {
        final Table card = new Table();
        card.pad(10, 16, 10, 16);
        card.setBackground(assets.rounded(Assets.PANEL, Assets.LINE, 18));

        Table texts = new Table();
        texts.add(Ui.label(skin, "Привет, " + user.firstName() + "!", "h1")).left().row();
        texts.add(Ui.label(skin, user.role.description + " · ★ " + user.stars
                + " · занятий: " + user.lessons, "muted")).left().padTop(2).row();
        card.add(texts).expandX().left();

        card.add(Ui.label(skin, "Профиль ›", "muted")).right();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goTo(new ProfileScreen(game));
            }
        });
        return card;
    }

    // ------------------------------------------------------------------

    private static final class Lesson {
        final String key;
        final String title;

        Lesson(String key, String title) {
            this.key = key;
            this.title = title;
        }
    }

    private Lesson[] lessons(Role role) {
        if (role == Role.SCHOOL) {
            return new Lesson[] {
                    new Lesson("learn", "Учить слова"),
                    new Lesson("build", "Собери слово"),
                    new Lesson("missing", "Пропущенная буква"),
                    new Lesson("quiz", "Викторина"),
                    new Lesson("sentence", "Составь предложение"),
                    new Lesson("race", "Гонка перевода"),
                    new Lesson("alphabet", "Алфавит"),
                    new Lesson("quests", "Квесты"),
                    new Lesson("leaderboard", "Рейтинг"),
            };
        }
        return new Lesson[] {
                new Lesson("picture", "Картинка и слово"),
                new Lesson("memory", "Найди пару"),
                new Lesson("learn", "Учить слова"),
                new Lesson("alphabet", "Алфавит"),
                new Lesson("match", "Матч-безумие"),
                new Lesson("sort", "Разложи по категориям"),
                new Lesson("odd", "Найди лишнее"),
                new Lesson("quests", "Квесты"),
                new Lesson("leaderboard", "Рейтинг"),
        };
    }

    @Override
    public void dispose() {
        if (roadTexture != null) {
            roadTexture.dispose();
            roadTexture = null;
        }
        super.dispose();
    }
}
