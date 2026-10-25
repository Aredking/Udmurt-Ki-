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

/**
 * «Гонка перевода»: отвечай на время. Верный ответ добавляет секунды,
 * ошибка отнимает жизнь. Игра заканчивается, когда вышло время или жизни.
 */
public class TranslateRaceScreen extends BaseScreen {

    private static final float START_TIME = 45f;
    private static final float BONUS_TIME = 3f;
    private static final int LIVES = 3;

    private final Role role;
    private final Array<Word> pool;

    private final Label timeLabel;
    private final Label livesLabel;
    private final Label scoreLabel;
    private final Label wordLabel;
    private final Label promptLabel;
    private final Table optionsTable;
    private final com.udmurt.learn.ui.ProgressBar timeBar;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private float timeLeft = START_TIME;
    private int lives = LIVES;
    private int correct;
    private boolean perfect = true;
    private boolean locked;
    private boolean over;
    private Word current;
    private boolean ruToUdmurt;

    public TranslateRaceScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pool = WordBank.forRole(role);

        optionStyle = skin.get("blue", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);
        timeBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Гонка перевода", true)).growX().top().row();
        root.add(timeBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);

        Table status = new Table();
        timeLabel = Ui.label(skin, "", "h1");
        scoreLabel = Ui.label(skin, "", "h1");
        livesLabel = Ui.label(skin, "", "h1");
        status.add(timeLabel).expandX().left();
        status.add(scoreLabel).expandX().center();
        status.add(livesLabel).right();
        body.add(status).growX().padBottom(10).row();

        Table card = Ui.card(skin, "panel");
        card.pad(20);
        wordLabel = centered(skin, "", "title");
        card.add(wordLabel).height(120).expand().center();
        body.add(card).height(160).width(620).padBottom(14).row();

        promptLabel = centered(skin, "", "h1");
        body.add(promptLabel).growX().padBottom(14).row();

        optionsTable = new Table();
        body.add(optionsTable).growX();
        root.add(body).expand().fill().row();

        nextQuestion();
    }

    @Override
    public void render(float delta) {
        if (!over) {
            timeLeft -= delta;
            if (timeLeft <= 0f) {
                timeLeft = 0f;
                updateStatus();
                finish();
                super.render(delta);
                return;
            }
            updateStatus();
        }
        super.render(delta);
    }

    private void updateStatus() {
        timeBar.set(timeLeft / START_TIME);
        timeLabel.setText("Время: " + (int) Math.ceil(Math.max(0, timeLeft)));
        scoreLabel.setText("★ " + correct);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < LIVES; i++) hearts.append(i < lives ? "★" : "☆");
        livesLabel.setText(hearts.toString());
    }

    private void nextQuestion() {
        if (over) return;
        current = WordBank.random(pool);
        ruToUdmurt = Math.random() < 0.5;

        if (ruToUdmurt) {
            wordLabel.setText(current.russian);
            promptLabel.setText("Как это по-удмуртски?");
        } else {
            wordLabel.setText(current.udmurt);
            promptLabel.setText("Что означает это слово?");
        }

        int optionCount = role == Role.SCHOOL ? 4 : 3;
        Array<Word> distractors = WordBank.distractors(current, pool, optionCount - 1);
        Array<String> answers = new Array<>();
        for (Word distractor : distractors) {
            answers.add(ruToUdmurt ? distractor.udmurt : distractor.russian);
        }
        String correctAnswer = ruToUdmurt ? current.udmurt : current.russian;
        answers.add(correctAnswer);
        answers.shuffle();

        optionsTable.clearChildren();
        locked = false;
        for (final String answer : answers) {
            final TextButton button = new TextButton(answer, optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(answer, button, correctAnswer);
                }
            });
            optionsTable.add(button).growX().height(64).pad(6);
        }
    }

    private void answer(String chosen, TextButton button, String correctAnswer) {
        if (locked || over) return;
        locked = true;

        if (answerEquals(chosen, correctAnswer)) {
            correct++;
            timeLeft = Math.min(START_TIME, timeLeft + BONUS_TIME);
            button.setStyle(goodStyle);
            button.setTouchable(Touchable.disabled);
            updateStatus();
            stage.addAction(Actions.sequence(Actions.delay(0.4f), Actions.run(this::nextQuestion)));
        } else {
            lives--;
            perfect = false;
            button.setStyle(badStyle);
            button.setTouchable(Touchable.disabled);
            updateStatus();
            if (lives <= 0) {
                stage.addAction(Actions.sequence(Actions.delay(0.6f), Actions.run(this::finish)));
            } else {
                stage.addAction(Actions.sequence(Actions.delay(0.7f), Actions.run(this::nextQuestion)));
            }
        }
    }

    private boolean answer(String a, String b) {
        return false;
    }

    private boolean answerEquals(String a, String b) {
        return a != null && a.equals(b);
    }

    private void finish() {
        if (over) return;
        over = true;
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, perfect);
            game.onUserUpdated(user);
        }
        showResult("Гонка окончена!",
                "Правильных ответов: " + correct,
                correct,
                () -> goTo(new TranslateRaceScreen(game)));
    }
}
