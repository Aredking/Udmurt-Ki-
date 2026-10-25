package com.udmurt.learn.screen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.ProgressBar;
import com.udmurt.learn.ui.Ui;

/**
 * Мини-игра «Пропущенная буква»: показывается удмуртское слово с одной
 * скрытой буквой — нужно выбрать её из нескольких вариантов.
 */
public class MissingLetterScreen extends BaseScreen {

    private static final int TOTAL = 10;
    private static final String LETTERS = "абвгдежзийклмнопрстуфхцчшщъыьэюяёӝӟӧӵӥ";

    private final Array<Word> words = new Array<>();

    private final ProgressBar progressBar;
    private final Label ruLabel;
    private final Table wordTable;
    private final Label feedback;
    private final Table optionsTable;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private Word current;
    private int round;
    private int correct;
    private boolean locked;

    public MissingLetterScreen(UdmurtGame game) {
        super(game);
        Role role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;

        Array<Word> pool = WordBank.forRole(role);
        for (Word word : pool) {
            if (word.udmurt.length() >= 2) words.add(word);
        }
        if (words.size == 0) words.addAll(pool);
        words.shuffle();

        optionStyle = skin.get("ghost", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);

        progressBar = new ProgressBar(assets, 600);
        root.add(header("Пропущенная буква", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);
        body.top();

        Table card = Ui.card(skin, "panel");
        card.pad(24);
        ruLabel = Ui.label(skin, "", "h1");
        card.add(ruLabel).padBottom(14).row();
        wordTable = new Table();
        card.add(wordTable).row();
        body.add(card).growX().padBottom(8).row();

        feedback = centered(skin, "", "success");
        body.add(feedback).height(30).padBottom(6).row();

        optionsTable = new Table();
        body.add(optionsTable).growX().row();

        root.add(body).expand().fill().row();
        nextRound();
    }

    private void nextRound() {
        if (round >= TOTAL) {
            finish();
            return;
        }
        current = words.get(round % words.size);
        round++;

        int hide = MathUtils.random(current.udmurt.length() - 1);
        char answer = current.udmurt.charAt(hide);

        ruLabel.setText(current.russian);
        feedback.setText("");
        progressBar.set((float) round / TOTAL);

        wordTable.clearChildren();
        for (int i = 0; i < current.udmurt.length(); i++) {
            boolean hidden = i == hide;
            Label letter = new Label(hidden ? "_" : String.valueOf(current.udmurt.charAt(i)), skin, "title");
            letter.setColor(hidden ? Assets.MUTED : Assets.TEXT);
            wordTable.add(letter).pad(2);
        }

        Array<Character> choices = new Array<>();
        choices.add(answer);
        while (choices.size < 4) {
            char decoy = LETTERS.charAt(MathUtils.random(LETTERS.length() - 1));
            if (!choices.contains(decoy, false)) choices.add(decoy);
        }
        for (int i = choices.size - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            Character tmp = choices.get(i);
            choices.set(i, choices.get(j));
            choices.set(j, tmp);
        }

        optionsTable.clearChildren();
        locked = false;
        for (final Character choice : choices) {
            final TextButton button = new TextButton(String.valueOf(choice), optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(choice, button, answer);
                }
            });
            optionsTable.add(button).size(90, 76).pad(8);
        }
    }

    private void answer(char chosen, TextButton button, char correctAnswer) {
        if (locked) return;

        if (chosen == correctAnswer) {
            locked = true;
            correct++;
            button.setStyle(goodStyle);
            button.setTouchable(Touchable.disabled);
            feedback.setText("Верно!");
            feedback.setColor(Assets.GREEN);
            stage.addAction(Actions.sequence(Actions.delay(0.6f), Actions.run(this::nextRound)));
        } else {
            button.setStyle(badStyle);
            button.setTouchable(Touchable.disabled);
            feedback.setText("Не та буква, попробуй ещё");
            feedback.setColor(Assets.RED);
        }
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Отличная работа!", "Правильных букв: " + correct + " из " + TOTAL,
                correct, () -> goTo(new MissingLetterScreen(game)));
    }
}