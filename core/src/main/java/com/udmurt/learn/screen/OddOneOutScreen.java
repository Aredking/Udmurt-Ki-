package com.udmurt.learn.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** «Найди лишнее»: три слова из одной темы, одно — из другой. */
public class OddOneOutScreen extends BaseScreen {

    private static final int TOTAL = 10;

    private final Role role;
    private final Array<Word> pool;
    private final Array<String> categories;

    private final Label statusLabel;
    private final Table optionsTable;
    private final com.udmurt.learn.ui.ProgressBar progressBar;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private int round;
    private int correct;
    private boolean locked;

    public OddOneOutScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pool = WordBank.forRole(role);
        this.categories = WordBank.categories(role);

        optionStyle = skin.get("ghost", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Найди лишнее", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(24);
        statusLabel = Ui.label(skin, "", "h1");
        body.add(statusLabel).padBottom(6).row();
        body.add(Ui.label(skin, "Какое слово не подходит к остальным?", "muted")).padBottom(24).row();

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

        Array<String> cats = new Array<>(categories);
        cats.shuffle();

        String baseCategory = null;
        Array<Word> basePool = null;
        for (String category : cats) {
            Array<Word> words = WordBank.byCategory(category, role);
            if (words.size >= 3) {
                baseCategory = category;
                basePool = words;
                break;
            }
        }
        if (basePool == null) {
            basePool = pool;
        }

        Array<Word> base = new Array<>(basePool);
        base.shuffle();
        Array<Word> four = new Array<>();
        for (int i = 0; i < 3 && i < base.size; i++) four.add(base.get(i));

        Word odd = null;
        for (String category : cats) {
            if (category.equals(baseCategory)) continue;
            Array<Word> words = WordBank.byCategory(category, role);
            if (words.size > 0) {
                odd = WordBank.random(words);
                break;
            }
        }
        if (odd == null) {
            odd = WordBank.random(pool);
        }
        final String oddCategory = odd.category;
        four.add(odd);
        four.shuffle();

        optionsTable.clearChildren();
        locked = false;
        int column = 0;
        for (final Word word : four) {
            final TextButton button = new TextButton(word.udmurt, optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(button, word.category.equals(oddCategory));
                }
            });
            optionsTable.add(button).growX().height(76).pad(8);
            if (++column % 2 == 0) optionsTable.row();
        }
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
        stage.addAction(Actions.sequence(Actions.delay(0.6f), Actions.run(this::nextRound)));
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Готово!",
                "Правильных ответов: " + correct + " из " + TOTAL,
                correct,
                () -> goTo(new OddOneOutScreen(game)));
    }
}
