package com.udmurt.learn.screen;

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
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** Окно №5: викторина «Проверь себя». */
public class QuizScreen extends BaseScreen {

    private static final int TOTAL = 10;

    private static class Question {
        Word word;
        boolean ruToUdmurt;
    }

    private final Role role;
    private final Array<Word> pool;
    private final Array<Question> questions = new Array<>();

    private final Label progressLabel;
    private final Label scoreLabel;
    private final com.udmurt.learn.ui.ProgressBar progressBar;
    private final Label promptLabel;
    private final Label wordLabel;
    private final Table optionsTable;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private int round;
    private int correct;
    private boolean locked;

    public QuizScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pool = WordBank.forRole(role);

        Array<Word> shuffled = new Array<>(pool);
        shuffled.shuffle();
        for (int i = 0; i < TOTAL; i++) {
            Question q = new Question();
            q.word = shuffled.get(i % shuffled.size);
            q.ruToUdmurt = role != Role.SCHOOL || i % 2 == 0;
            questions.add(q);
        }

        optionStyle = skin.get("blue", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Викторина", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);

        Table status = new Table();
        progressLabel = Ui.label(skin, "", "h1");
        scoreLabel = Ui.label(skin, "", "h1");
        status.add(progressLabel).expandX().left();
        status.add(scoreLabel).right();
        body.add(status).growX().padBottom(10).row();

        Table card = Ui.card(skin, "panel");
        card.pad(20);
        wordLabel = centered(skin, "", "title");
        card.add(wordLabel).height(110).expand().center();
        body.add(card).height(150).width(620).padBottom(14).row();

        promptLabel = centered(skin, "", "h1");
        body.add(promptLabel).growX().padBottom(14).row();

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

        Question question = questions.get(round);
        round++;

        progressLabel.setText("Вопрос " + round + " / " + TOTAL);
        scoreLabel.setText("★ " + correct);

        final String correctAnswer;
        if (question.ruToUdmurt) {
            wordLabel.setText(question.word.russian);
            promptLabel.setText("Как это по-удмуртски?");
            correctAnswer = question.word.udmurt;
        } else {
            wordLabel.setText(question.word.udmurt);
            promptLabel.setText("Что означает это слово?");
            correctAnswer = question.word.russian;
        }

        int optionCount = role == Role.SCHOOL ? 4 : 3;
        Array<Word> distractors = WordBank.distractors(question.word, pool, optionCount - 1);

        Array<String> answers = new Array<>();
        for (Word distractor : distractors) {
            answers.add(question.ruToUdmurt ? distractor.udmurt : distractor.russian);
        }
        answers.add(correctAnswer);
        answers.shuffle();

        optionsTable.clearChildren();
        locked = false;
        progressBar.set((float) round / TOTAL);
        for (final String answer : answers) {
            final TextButton button = new TextButton(answer, optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(answer, button, correctAnswer);
                }
            });
            optionsTable.add(button).growX().height(66).pad(6);
        }
    }

    private void answer(String chosen, TextButton button, String correctAnswer) {
        if (locked) return;

        if (chosen.equals(correctAnswer)) {
            locked = true;
            correct++;
            button.setStyle(goodStyle);
            button.setTouchable(Touchable.disabled);
            scoreLabel.setText("★ " + correct);
            stage.addAction(Actions.sequence(Actions.delay(0.7f), Actions.run(this::nextRound)));
        } else {
            button.setStyle(badStyle);
            button.setTouchable(Touchable.disabled);
        }
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            if (correct > user.bestQuiz) user.bestQuiz = correct;
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Викторина пройдена!",
                "Правильных ответов: " + correct + " из " + TOTAL,
                correct,
                () -> goTo(new QuizScreen(game)));
    }
}
