package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** «Разложи по категориям»: определи, к какой теме относится слово. */
public class SortCategoriesScreen extends BaseScreen {

    private static final int TOTAL = 10;

    private final Role role;
    private final Array<Word> pool;
    private final Array<String> categories;

    private final Label statusLabel;
    private final Label wordLabel;
    private final Stack wordStack;
    private final Image wordImage;
    private final Table optionsTable;
    private final com.udmurt.learn.ui.ProgressBar progressBar;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private int round;
    private int correct;
    private boolean locked;

    public SortCategoriesScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pool = WordBank.forRole(role);
        this.categories = WordBank.categories(role);

        optionStyle = skin.get("ghost", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Разложи по категориям", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);
        statusLabel = Ui.label(skin, "", "h1");
        body.add(statusLabel).padBottom(10).row();

        Table card = Ui.card(skin, "panel");
        card.pad(20);
        wordStack = new Stack();
        wordImage = new Image();
        wordImage.setScaling(Scaling.fit);
        wordImage.setVisible(false);
        wordLabel = centered(skin, "", "title");
        wordStack.add(wordImage);
        wordStack.add(wordLabel);
        card.add(wordStack).size(340, 150).expand().center();
        body.add(card).height(180).width(620).padBottom(16).row();

        optionsTable = new Table();
        body.add(optionsTable).growX();
        root.add(body).expand().fill().row();

        nextRound();
    }

    private void nextRound() {
        if (round >= TOTAL) {
            finish();
            return;
        }
        round++;
        progressBar.set((float) (round - 1) / TOTAL);
        statusLabel.setText("Вопрос " + round + " / " + TOTAL + "   ·   ★ " + correct);

        final Word word = WordBank.random(pool);
        wordLabel.setText(word.udmurt);
        showImage(word);

        Array<String> options = new Array<>();
        options.add(word.category);
        Array<String> others = new Array<>(categories);
        others.removeValue(word.category, false);
        others.shuffle();
        for (int i = 0; i < 3 && i < others.size; i++) options.add(others.get(i));
        options.shuffle();

        optionsTable.clearChildren();
        locked = false;
        int column = 0;
        for (final String category : options) {
            final TextButton button = new TextButton(category, optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(button, category.equals(word.category));
                }
            });
            optionsTable.add(button).growX().height(74).pad(8);
            if (++column % 2 == 0) optionsTable.row();
        }
    }

    private void showImage(Word word) {
        if (word.hasPicture()) {
            Texture texture = assets.picture(word.image);
            if (texture != null) {
                wordImage.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture));
                wordImage.setVisible(true);
                wordLabel.setVisible(false);
                return;
            }
        }
        wordImage.setVisible(false);
        wordLabel.setVisible(true);
    }

    private void answer(TextButton button, boolean right) {
        if (locked) return;
        locked = true;
        if (right) {
            correct++;
            button.setStyle(goodStyle);
            statusLabel.setText("Вопрос " + round + " / " + TOTAL + "   ·   ★ " + correct);
        } else {
            button.setStyle(badStyle);
        }
        progressBar.set((float) round / TOTAL);
        stage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.6f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(this::nextRound)));
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Категории разложены!",
                "Правильных ответов: " + correct + " из " + TOTAL,
                correct,
                () -> goTo(new SortCategoriesScreen(game)));
    }
}
