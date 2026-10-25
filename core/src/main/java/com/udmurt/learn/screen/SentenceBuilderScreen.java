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
import com.udmurt.learn.data.Phrase;
import com.udmurt.learn.data.PhraseBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** «Составь предложение»: собери удмуртскую фразу из слов по её переводу. */
public class SentenceBuilderScreen extends BaseScreen {

    private static final int TOTAL = 6;

    private final Role role;
    private final Array<Phrase> phrases;

    private final Label statusLabel;
    private final Label promptLabel;
    private final Table answerTable;
    private final Table bankTable;
    private final com.udmurt.learn.ui.ProgressBar progressBar;

    private final TextButtonStyle wordStyle;
    private final TextButtonStyle chosenStyle;

    private Phrase current;
    private final Array<String> bank = new Array<>();
    private final Array<String> answer = new Array<>();

    private int round;
    private int correct;
    private boolean locked;

    public SentenceBuilderScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.phrases = new Array<>(PhraseBank.forRole(role));
        this.phrases.shuffle();

        wordStyle = skin.get("ghost", TextButtonStyle.class);
        chosenStyle = skin.get("blue", TextButtonStyle.class);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Составь предложение", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);
        statusLabel = Ui.label(skin, "", "h1");
        body.add(statusLabel).padBottom(10).row();

        Table card = Ui.card(skin, "panel");
        card.pad(18);
        promptLabel = centered(skin, "", "title");
        card.add(promptLabel).height(90).expand().center();
        body.add(card).height(120).width(620).padBottom(18).row();

        answerTable = new Table();
        Table answerBox = Ui.card(skin, "card");
        answerBox.pad(12);
        answerBox.add(answerTable).growX().minHeight(80);
        body.add(answerBox).growX().width(640).padBottom(16).row();

        bankTable = new Table();
        body.add(bankTable).growX().padBottom(18).row();

        body.add(Ui.button(skin, "Проверить", "primary", this::check)).width(260).height(60);
        root.add(body).expand().fill().row();

        nextRound();
    }

    private void nextRound() {
        if (round >= TOTAL) {
            finish();
            return;
        }
        round++;
        current = phrases.get((round - 1) % phrases.size);
        progressBar.set((float) (round - 1) / TOTAL);
        statusLabel.setText("Фраза " + round + " / " + TOTAL + "   ·   ★ " + correct);
        promptLabel.setText(current.russian);

        bank.clear();
        for (String word : current.words) bank.add(word);
        if (role == Role.SCHOOL && phrases.size > 1) {
            Phrase extra = phrases.get((round + 3) % phrases.size);
            bank.add(extra.words.first());
        }
        bank.shuffle();
        answer.clear();
        locked = false;
        refresh();
    }

    private void refresh() {
        answerTable.clearChildren();
        if (answer.isEmpty()) {
            answerTable.add(Ui.label(skin, "Нажимай на слова ниже…", "muted"));
        } else {
            for (int i = 0; i < answer.size; i++) {
                final int index = i;
                TextButton button = new TextButton(answer.get(i), chosenStyle);
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (locked) return;
                        bank.add(answer.removeIndex(index));
                        refresh();
                    }
                });
                answerTable.add(button).height(50).pad(4);
            }
        }

        bankTable.clearChildren();
        int column = 0;
        for (int i = 0; i < bank.size; i++) {
            final int index = i;
            TextButton button = new TextButton(bank.get(i), wordStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (locked) return;
                    answer.add(bank.removeIndex(index));
                    refresh();
                }
            });
            bankTable.add(button).height(54).pad(5);
            if (++column % 4 == 0) bankTable.row();
        }
    }

    private void check() {
        if (locked) return;
        if (answer.isEmpty()) return;

        StringBuilder built = new StringBuilder();
        for (int i = 0; i < answer.size; i++) {
            if (i > 0) built.append(' ');
            built.append(answer.get(i));
        }

        if (built.toString().equals(current.udmurt)) {
            locked = true;
            correct++;
            statusLabel.setText("Фраза " + round + " / " + TOTAL + "   ·   ★ " + correct);
            progressBar.set((float) round / TOTAL);
            stage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.5f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(this::nextRound)));
        } else {
            stage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(10, 0, 0.06f, com.badlogic.gdx.math.Interpolation.sine),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-20, 0, 0.06f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(10, 0, 0.06f)));
        }
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Фразы собраны!",
                "Правильно: " + correct + " из " + TOTAL,
                correct,
                () -> goTo(new SentenceBuilderScreen(game)));
    }
}
