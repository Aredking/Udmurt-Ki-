package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/** Игра №1: перевод слова с картинки. */
public class PictureGameScreen extends BaseScreen {

    private static final int TOTAL = 10;

    private final Role role;
    private final Array<Word> questions = new Array<>();

    private final Label progressLabel;
    private final Label scoreLabel;
    private final com.udmurt.learn.ui.ProgressBar progressBar;
    private final Image pictureImage;
    private final Label promptLabel;
    private final Table optionsTable;

    private final TextButtonStyle optionStyle;
    private final TextButtonStyle goodStyle;
    private final TextButtonStyle badStyle;

    private int round;
    private int correct;
    private boolean locked;

    public PictureGameScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;

        Array<Word> pool = WordBank.withPicture(role);
        Array<Word> shuffled = new Array<>(pool);
        shuffled.shuffle();
        for (int i = 0; i < TOTAL; i++) {
            questions.add(shuffled.get(i % shuffled.size));
        }

        optionStyle = skin.get("blue", TextButtonStyle.class);
        goodStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Картинка и слово", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);

        Table status = new Table();
        progressLabel = Ui.label(skin, "", "h1");
        scoreLabel = Ui.label(skin, "", "h1");
        status.add(progressLabel).expandX().left();
        status.add(scoreLabel).right();
        body.add(status).growX().padBottom(10).row();

        Table pictureCard = Ui.card(skin, "panel");
        pictureCard.pad(10);
        pictureImage = new Image();
        pictureImage.setScaling(Scaling.fit);
        pictureCard.add(pictureImage).size(320, 240).expand().center();
        body.add(pictureCard).size(430, 300).padBottom(10).row();

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

        Word current = questions.get(round);
        round++;

        progressLabel.setText("Вопрос " + round + " / " + TOTAL);
        scoreLabel.setText("★ " + correct);
        promptLabel.setText("Что это по-удмуртски?");
        Texture texture = assets.picture(current.image);
        if (texture != null) {
            pictureImage.setDrawable(new TextureRegionDrawable(texture));
            pictureImage.setVisible(true);
        } else {
            pictureImage.setVisible(false);
        }

        int optionCount = role == Role.SCHOOL ? 4 : 3;
        Array<Word> options = WordBank.distractors(current, WordBank.withPicture(role), optionCount - 1);
        options.add(current);
        options = WordBank.shuffled(options);

        optionsTable.clearChildren();
        locked = false;
        progressBar.set((float) round / TOTAL);
        for (final Word option : options) {
            final TextButton button = new TextButton(option.udmurt, optionStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    answer(option, button, current);
                }
            });
            optionsTable.add(button).growX().height(66).pad(6);
        }
    }

    private void answer(Word chosen, TextButton button, Word correctWord) {
        if (locked) return;

        if (chosen.udmurt.equals(correctWord.udmurt)) {
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
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Молодец!", "Правильных ответов: " + correct + " из " + TOTAL,
                correct, () -> goTo(new PictureGameScreen(game)));
    }
}
