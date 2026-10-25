package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.ui.Ui;

/** Окно №2: изучение удмуртского языка по темам. */
public class LearnScreen extends BaseScreen {

    private final Role role;
    private final Table content;

    public LearnScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;

        root.add(header("Учить слова", true)).growX().top().row();
        content = new Table();
        content.pad(20);
        content.top();
        root.add(content).expand().fill().row();

        showCategories();
    }

    private void showCategories() {
        content.clearChildren();
        content.add(centered(skin, "Выбери тему", "h1")).padBottom(16).row();

        Array<String> categories = WordBank.categories(role);
        Table grid = new Table();
        int column = 0;
        for (String category : categories) {
            TextButton button = Ui.button(skin, category, "blue",
                    () -> showCategory(category));
            grid.add(button).width(240).height(70).pad(8);
            if (++column % 3 == 0) grid.row();
        }
        content.add(grid).expand().top();
    }

    private void showCategory(final String category) {
        content.clearChildren();

        Table bar = new Table();
        bar.add(Ui.label(skin, category, "title")).expandX().left().padRight(16);
        bar.add(Ui.button(skin, "‹ Темы", "ghost", this::showCategories)).height(48);
        content.add(bar).growX().padBottom(12).row();

        Array<Word> words = WordBank.byCategory(category, role);
        Table list = new Table();
        list.top();

        final boolean hideByDefault = role == Role.SCHOOL;
        int colorIndex = 0;
        for (Word word : words) {
            list.add(buildWordCard(word, hideByDefault, colorIndex++)).growX().padBottom(8).row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        content.add(scroll).expand().fill();
    }

    private Table buildWordCard(Word word, boolean hideByDefault, int colorIndex) {
        final Table card = Ui.card(skin, "mint");
        card.pad(12);

        Texture tex = word.hasPicture() ? assets.picture(word.image) : null;
        Actor picture;
        if (tex != null) {
            Image image = new Image(tex);
            image.setScaling(Scaling.fit);
            picture = image;
        } else {
            Label placeholder = new Label("●", skin, "small");
            placeholder.setColor(pickColor(colorIndex));
            picture = placeholder;
        }
        card.add(picture).size(70, 70).left().padRight(16);

        card.add(Ui.label(skin, word.udmurt, "h1")).expandX().left().padRight(16);

        final String translation = word.russian;
        final Label russian = Ui.label(skin, hideByDefault ? "•••" : translation, "muted");
        card.add(russian).width(220).right();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean hidden = "•••".contentEquals(russian.getText());
                russian.setText(hidden ? translation : "•••");
            }
        });
        return card;
    }

    private Color pickColor(int index) {
        Color[] colors = {Assets.ORANGE, Assets.BLUE, Assets.PINK, Assets.GREEN,
                Assets.PURPLE, Assets.TEAL, Assets.RED};
        return colors[index % colors.length];
    }
}
