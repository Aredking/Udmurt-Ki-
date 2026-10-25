package com.udmurt.learn.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Генерирует все ресурсы в рантайме: шрифты с поддержкой кириллицы,
 * картинки слов и «кожу» интерфейса. Так проект не зависит от внешних
 * атласов и одинаково выглядит на всех платформах.
 */
public class Assets implements Disposable {

    // Палитра
    public static final Color BG = Color.valueOf("FDF4E7");
    public static final Color BG_DARK = Color.valueOf("F2E3CB");
    public static final Color TEXT = Color.valueOf("2E2A26");
    public static final Color MUTED = Color.valueOf("8A8177");
    public static final Color PANEL = Color.valueOf("FFFFFF");
    public static final Color LINE = Color.valueOf("DCCFBB");
    public static final Color GREEN = Color.valueOf("43A047");
    public static final Color ORANGE = Color.valueOf("FB8C00");
    public static final Color BLUE = Color.valueOf("1E88E5");
    public static final Color PINK = Color.valueOf("D81B60");
    public static final Color PURPLE = Color.valueOf("8E24AA");
    public static final Color TEAL = Color.valueOf("00897B");
    public static final Color RED = Color.valueOf("E53935");
    public static final Color YELLOW = Color.valueOf("F6A609");
    public static final Color MINT = Color.valueOf("E7F5E9");
    public static final Color CREAM = Color.valueOf("FFF7E9");

    /** Цвет из «игровой» палитры по индексу (для чередования карточек). */
    public static Color color(int index) {
        Color[] colors = {GREEN, BLUE, ORANGE, PINK, PURPLE, TEAL, RED, YELLOW};
        return colors[index % colors.length];
    }

    /** Диаметр круглых кнопок-узлов «дорожки» обучения. */
    public static final int NODE_DIAMETER = 96;

    /** Круглая кнопка-узел дорожки обучения. */
    public TextButtonStyle circleButtonStyle(Color base) {
        TextButtonStyle style = new TextButtonStyle();
        style.up = circle(base, NODE_DIAMETER);
        style.over = circle(base.cpy().lerp(Color.WHITE, 0.14f), NODE_DIAMETER);
        style.down = circle(base.cpy().lerp(Color.BLACK, 0.22f), NODE_DIAMETER);
        style.font = fontBig;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;
        return style;
    }

    private Drawable circle(Color color, int diameter) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(color);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2 - 1);
        pixmap.setColor(color.cpy().lerp(Color.BLACK, 0.20f));
        pixmap.drawCircle(diameter / 2, diameter / 2, diameter / 2 - 2);
        return regionDrawable(pixmap);
    }

    /**
     * Иконка «глаз» для показа/скрытия пароля: {@code open=true} — открытый,
     * {@code false} — перечёркнутый.
     */
    public Drawable eye(boolean open) {
        int size = 72;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        float cx = size / 2f;
        float cy = size / 2f;
        float halfW = size * 0.34f;
        float halfH = size * 0.22f;
        int steps = 48;

        pixmap.setColor(MUTED);
        for (int side = 0; side < 2; side++) {
            float sign = side == 0 ? -1f : 1f;
            int px = 0;
            int py = 0;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                int x = Math.round(cx - halfW + halfW * 2f * t);
                int y = Math.round(cy + sign * halfH * (float) Math.sin(Math.PI * t));
                if (i > 0) pixmap.drawLine(px, py, x, y);
                pixmap.fillCircle(x, y, 1);
                px = x;
                py = y;
            }
        }

        pixmap.setColor(MUTED);
        pixmap.fillCircle((int) cx, (int) cy, Math.round(halfH * 0.95f));
        pixmap.setColor(TEXT);
        pixmap.fillCircle((int) cx, (int) cy, Math.round(halfH * 0.55f));

        if (!open) {
            pixmap.setColor(MUTED);
            int x1 = Math.round(cx - halfW * 1.15f);
            int y1 = Math.round(cy - halfH * 1.7f);
            int x2 = Math.round(cx + halfW * 1.15f);
            int y2 = Math.round(cy + halfH * 1.7f);
            int dist = Math.max(1, Math.round((float) Math.hypot(x2 - x1, y2 - y1)));
            for (int s = 0; s <= dist; s++) {
                float t = s / (float) dist;
                pixmap.fillCircle(Math.round(x1 + (x2 - x1) * t),
                        Math.round(y1 + (y2 - y1) * t), 2);
            }
        }
        return regionDrawable(pixmap);
    }


    private static final String CYRILLIC =
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
                    + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                    + "ӜӝӞӟӦӧӴӵӤӥ";

    public final SpriteBatch batch;
    public final Skin skin;
    public final BitmapFont fontTitle;
    public final BitmapFont fontBig;
    public final BitmapFont fontNormal;
    public final BitmapFont fontSmall;

    private final Array<Texture> textures = new Array<>();
    private final Array<BitmapFont> fonts = new Array<>();
    private final ObjectMap<String, Texture> pictures = new ObjectMap<>();

    public Assets() {
        batch = new SpriteBatch();
        skin = new Skin();

        FreeTypeFontGenerator regular = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MPLUSRounded1c-Regular.ttf"));
        FreeTypeFontGenerator bold = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MPLUSRounded1c-Bold.ttf"));

        String alphabet = FreeTypeFontGenerator.DEFAULT_CHARS
                + CYRILLIC + "«»—–…№°★☆•●■◆▶✓←→‹›";

        fontTitle = make(bold, 46, alphabet);
        fontBig = make(bold, 30, alphabet);
        fontNormal = make(regular, 22, alphabet);
        fontSmall = make(regular, 17, alphabet);

        regular.dispose();
        bold.dispose();

        buildStyles();
    }

    /** Текстура картинки из assets/images (null, если файла нет). */
    public Texture picture(String path) {
        if (path == null) return null;
        Texture texture = pictures.get(path);
        if (texture != null) return texture;
        if (!Gdx.files.internal(path).exists()) return null;
        texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pictures.put(path, texture);
        return texture;
    }

    private BitmapFont make(FreeTypeFontGenerator generator, int size, String characters) {
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.characters = characters;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.kerning = true;
        BitmapFont font = generator.generateFont(parameter);
        font.setUseIntegerPositions(false);
        fonts.add(font);
        return font;
    }

    // ------------------------------------------------------------------
    // Рисуемые элементы интерфейса
    // ------------------------------------------------------------------

    /** Скруглённый прямоугольник; при border != null добавляется обводка. */
    public Drawable rounded(Color fill, Color border, int radius) {
        int size = radius * 2 + 10;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        if (border != null) {
            fillRound(pixmap, 0, 0, size, size, radius, border);
        }
        int inset = border != null ? 2 : 0;
        fillRound(pixmap, inset, inset, size - inset * 2, size - inset * 2,
                Math.max(1, radius - inset), fill);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        return ninePatch(pixmap, radius);
    }

    /**
     * Объёмная кнопка: сверху цветная «крышка», снизу — тёмный «бортик».
     * Вместе со смещением текста при нажатии даёт мягкий эффект вдавливания.
     */
    public Drawable rounded3D(Color face, int radius) {
        return rounded3D(face, face.cpy().lerp(Color.BLACK, 0.32f), radius);
    }

    public Drawable rounded3D(Color face, Color lip, int radius) {
        int size = radius * 2 + 10;
        int height = size + lipHeight(radius);
        Pixmap pixmap = new Pixmap(size, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        fillRound(pixmap, 0, 0, size, height, radius, lip);
        fillRound(pixmap, 0, 0, size, size, radius, face);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        return ninePatch(pixmap, radius + 1, radius, radius + 1, radius + lipHeight(radius));
    }

    /** Насколько «бортик» выступает под кнопкой (и насколько текст уходит вниз). */
    public static int lipHeight(int radius) {
        return Math.max(6, radius / 2);
    }

    /** Сплошной прямоугольник заданного размера. */
    public Drawable solid(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return regionDrawable(pixmap);
    }

    private Drawable ninePatch(Pixmap pixmap, int split) {
        return ninePatch(pixmap, split + 1, split, split + 1, split);
    }

    private Drawable ninePatch(Pixmap pixmap, int left, int right, int top, int bottom) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        pixmap.dispose();
        TextureRegion region = new TextureRegion(texture);
        return new NinePatchDrawable(new NinePatch(region, left, right, top, bottom));
    }

    private Drawable regionDrawable(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private static void fillRound(Pixmap pixmap, int x, int y, int w, int h, int r, Color color) {
        pixmap.setColor(color);
        if (r < 1) {
            pixmap.fillRectangle(x, y, w, h);
            return;
        }
        pixmap.fillRectangle(x + r, y, w - 2 * r, h);
        pixmap.fillRectangle(x, y + r, w, h - 2 * r);
        pixmap.fillCircle(x + r, y + r, r);
        pixmap.fillCircle(x + w - r - 1, y + r, r);
        pixmap.fillCircle(x + r, y + h - r - 1, r);
        pixmap.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    /** Стиль «плитки» меню нужного цвета с крупным шрифтом. */
    public TextButtonStyle tileStyle(Color color) {
        return buttonStyle(color, 26, fontBig);
    }

    /** Базовый цветной стиль кнопки. */
    public TextButtonStyle buttonStyle(Color base, int radius, BitmapFont font) {
        TextButtonStyle style = new TextButtonStyle();
        style.up = rounded3D(base, radius);
        style.over = rounded3D(base.cpy().lerp(Color.WHITE, 0.14f), radius);
        style.down = rounded(base.cpy().lerp(Color.BLACK, 0.18f), null, radius);
        style.checked = rounded(base.cpy().lerp(Color.BLACK, 0.12f), null, radius);
        style.checkedOver = style.checked;
        style.pressedOffsetY = -lipHeight(radius);
        style.unpressedOffsetY = 0f;
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;
        style.checkedFontColor = Color.WHITE;
        style.disabled = rounded(Color.valueOf("CFCFCF"), null, radius);
        style.disabledFontColor = Color.valueOf("888888");
        return style;
    }

    private void buildStyles() {
        // Метки
        skin.add("default", new LabelStyle(fontNormal, TEXT));
        skin.add("title", new LabelStyle(fontTitle, TEXT));
        skin.add("h1", new LabelStyle(fontBig, TEXT));
        skin.add("header", new LabelStyle(fontBig, Color.WHITE));
        skin.add("small", new LabelStyle(fontSmall, TEXT));
        skin.add("muted", new LabelStyle(fontSmall, MUTED));
        skin.add("error", new LabelStyle(fontSmall, RED));
        skin.add("success", new LabelStyle(fontNormal, GREEN));

        // Кнопки
        skin.add("default", buttonStyle(GREEN, 16, fontNormal), TextButtonStyle.class);
        skin.add("primary", buttonStyle(GREEN, 16, fontNormal), TextButtonStyle.class);
        skin.add("accent", buttonStyle(ORANGE, 16, fontNormal), TextButtonStyle.class);
        skin.add("blue", buttonStyle(BLUE, 16, fontNormal), TextButtonStyle.class);
        skin.add("pink", buttonStyle(PINK, 16, fontNormal), TextButtonStyle.class);
        skin.add("purple", buttonStyle(PURPLE, 16, fontNormal), TextButtonStyle.class);
        skin.add("teal", buttonStyle(TEAL, 16, fontNormal), TextButtonStyle.class);
        skin.add("danger", buttonStyle(RED, 16, fontNormal), TextButtonStyle.class);

        TextButtonStyle ghost = new TextButtonStyle();
        ghost.up = rounded3D(Color.WHITE, LINE, 16);
        ghost.over = rounded3D(CREAM, LINE, 16);
        ghost.down = rounded(BG_DARK, null, 16);
        ghost.pressedOffsetY = -lipHeight(16);
        ghost.font = fontNormal;
        ghost.fontColor = TEXT;
        ghost.overFontColor = TEXT;
        ghost.downFontColor = TEXT;
        skin.add("ghost", ghost, TextButtonStyle.class);

        TextButtonStyle toggle = new TextButtonStyle();
        toggle.up = rounded(Color.WHITE, LINE, 18);
        toggle.over = rounded(CREAM, LINE, 18);
        toggle.down = rounded(BG_DARK, LINE, 18);
        toggle.checked = rounded(GREEN, null, 18);
        toggle.checkedOver = rounded(GREEN.cpy().lerp(Color.WHITE, 0.12f), null, 18);
        toggle.font = fontNormal;
        toggle.fontColor = TEXT;
        toggle.overFontColor = TEXT;
        toggle.downFontColor = TEXT;
        toggle.checkedFontColor = Color.WHITE;
        skin.add("toggle", toggle, TextButtonStyle.class);

        // Круглые узлы дорожки обучения (по цветам палитры)
        for (int i = 0; i < 8; i++) {
            skin.add("circle" + i, circleButtonStyle(color(i)), TextButtonStyle.class);
        }

        // Иконки «глаз» для полей пароля
        skin.add("eye-open", eye(true), Drawable.class);
        skin.add("eye-off", eye(false), Drawable.class);

        // Поля ввода
        TextFieldStyle field = new TextFieldStyle();
        field.font = fontNormal;
        field.fontColor = TEXT;
        field.messageFont = fontNormal;
        field.messageFontColor = MUTED;
        field.background = rounded(Color.WHITE, LINE, 12);
        field.focusedBackground = rounded(Color.WHITE, GREEN, 12);
        field.disabledBackground = rounded(Color.valueOf("EFEBE4"), LINE, 12);
        field.cursor = solid(TEXT, 3, 30);
        field.selection = rounded(new Color(0.12f, 0.53f, 0.90f, 0.35f), null, 6);
        skin.add("default", field, TextFieldStyle.class);

        // Панели и фоны
        skin.add("panel", rounded(Color.WHITE, LINE, 22), Drawable.class);
        skin.add("card", rounded(CREAM, LINE, 18), Drawable.class);
        skin.add("mint", rounded(MINT, LINE, 18), Drawable.class);
        skin.add("headerbar", rounded(GREEN, null, 0), Drawable.class);
        skin.add("scrim", rounded(new Color(0f, 0f, 0f, 0.45f), null, 0), Drawable.class);

        // Окно (для всплывающих сообщений)
        WindowStyle window = new WindowStyle();
        window.titleFont = fontBig;
        window.titleFontColor = TEXT;
        window.background = rounded(Color.WHITE, LINE, 22);
        skin.add("default", window, WindowStyle.class);

        // Прокрутка
        ScrollPaneStyle scroll = new ScrollPaneStyle();
        scroll.vScroll = solid(new Color(0f, 0f, 0f, 0.08f), 8, 8);
        scroll.vScrollKnob = rounded(Color.valueOf("C9BBA3"), null, 5);
        skin.add("default", scroll, ScrollPaneStyle.class);
    }

    @Override
    public void dispose() {
        for (BitmapFont font : fonts) font.dispose();
        for (Texture texture : textures) texture.dispose();
        for (Texture texture : pictures.values()) texture.dispose();
        skin.dispose();
        batch.dispose();
    }
}
