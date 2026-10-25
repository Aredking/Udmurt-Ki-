package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
import com.udmurt.learn.ui.ProgressBar;
import com.udmurt.learn.ui.Ui;

/**
 * Мини-игра «Собери слово»: по картинке и переводу нужно выложить удмуртское
 * слово из букв-плиток. Идея из уроков Duolingo, но со своими правилами.
 */
public class BuildWordScreen extends BaseScreen {

    private static final int TOTAL = 8;
    private static final int MAX_TILE_COLUMNS = 6;
    private static final String LETTERS = "абвгдежзийклмнопрстуфхцчшщъыьэюяёӝӟӧӵӥ";

    private final Role role;
    private final Array<Word> words = new Array<>();

    private final ProgressBar progressBar;
    private final Image image = new Image();
    private final Label ruLabel;
    private final Label feedback;
    private final Table slotsTable;
    private final Table tilesTable;

    private final Array<Label> slots = new Array<>();
    private final Array<TextButton> tiles = new Array<>();
    private final StringBuilder answer = new StringBuilder();

    private Word current;
    private int round;
    private int correct;
    private boolean locked;

    public BuildWordScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;

        Array<Word> pool = WordBank.forRole(role);
        int maxLength = role == Role.SCHOOL ? 10 : 6;
        for (Word word : pool) {
            if (word.udmurt.length() <= maxLength) words.add(word);
        }
        if (words.size == 0) words.addAll(pool);
        words.shuffle();

        progressBar = new ProgressBar(assets, 600);
        root.add(header("Собери слово", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(16);
        body.top();

        Table prompt = Ui.card(skin, "panel");
        prompt.pad(14);
        image.setScaling(Scaling.fit);
        prompt.add(image).size(140, 140).padRight(16);
        ruLabel = Ui.label(skin, "", "title");
        prompt.add(ruLabel).left().expandX();
        body.add(prompt).growX().padBottom(12).row();

        slotsTable = new Table();
        body.add(slotsTable).growX().padBottom(4).row();

        feedback = centered(skin, "", "success");
        body.add(feedback).height(28).padBottom(4).row();

        tilesTable = new Table();
        body.add(tilesTable).growX().padBottom(10).row();

        body.add(Ui.button(skin, "← Стереть", "ghost", this::undo)).width(220).height(50).row();

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

        Texture texture = assets.picture(current.image);
        if (texture != null) {
            image.setDrawable(new TextureRegionDrawable(texture));
            image.setVisible(true);
        } else {
            image.setDrawable(null);
            image.setVisible(false);
        }
        ruLabel.setText(current.russian);
        feedback.setText("");
        progressBar.set((float) round / TOTAL);

        buildSlots(current.udmurt.length());
        buildTiles(current.udmurt);
    }

    private void buildSlots(int count) {
        slots.clear();
        slotsTable.clearChildren();
        for (int i = 0; i < count; i++) {
            Table box = new Table();
            box.setBackground(skin.getDrawable("panel"));
            Label letter = new Label("", skin, "h1");
            box.add(letter);
            slots.add(letter);
            slotsTable.add(box).size(52).pad(3);
        }
    }

    private void buildTiles(String word) {
        Array<Character> shuffled = new Array<>();
        for (char c : word.toCharArray()) shuffled.add(c);
        for (int i = 0; i < 2; i++) shuffled.add(randomLetter(word));
        for (int i = shuffled.size - 1; i > 0; i--) {
            int j = com.badlogic.gdx.math.MathUtils.random(i);
            Character tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }

        tiles.clear();
        tilesTable.clearChildren();
        int column = 0;
        for (final Character letter : shuffled) {
            final TextButton tile = new TextButton(String.valueOf(letter), skin, "ghost");
            tile.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    pick(tile, letter);
                }
            });
            tilesTable.add(tile).size(60, 60).pad(5);
            tiles.add(tile);
            if (++column % MAX_TILE_COLUMNS == 0) tilesTable.row();
        }
    }

    private char randomLetter(String word) {
        while (true) {
            char c = LETTERS.charAt(com.badlogic.gdx.math.MathUtils.random(LETTERS.length() - 1));
            if (word.indexOf(c) < 0) return c;
        }
    }

    private void pick(TextButton tile, char letter) {
        if (locked || tile.getTouchable() == Touchable.disabled) return;
        if (answer.length() >= current.udmurt.length()) return;

        tile.setTouchable(Touchable.disabled);
        tile.getColor().a = 0.3f;
        answer.append(letter);
        slots.get(answer.length() - 1).setText(String.valueOf(letter));

        if (answer.length() == current.udmurt.length()) check();
    }

    private void undo() {
        if (locked || answer.length() == 0) return;
        int last = answer.length() - 1;
        char letter = answer.charAt(last);
        answer.deleteCharAt(last);
        slots.get(last).setText("");
        for (TextButton tile : tiles) {
            if (tile.getTouchable() == Touchable.disabled
                    && tile.getText().toString().charAt(0) == letter) {
                tile.setTouchable(Touchable.enabled);
                tile.getColor().a = 1f;
                break;
            }
        }
    }

    private void check() {
        locked = true;
        if (answer.toString().equals(current.udmurt)) {
            correct++;
            feedback.setText("Верно!");
            feedback.setColor(Assets.GREEN);
            stage.addAction(Actions.sequence(Actions.delay(0.6f), Actions.run(this::nextRound)));
        } else {
            feedback.setText("Попробуй ещё раз");
            feedback.setColor(Assets.RED);
            stage.addAction(Actions.sequence(Actions.delay(0.7f), Actions.run(this::resetAttempt)));
        }
    }

    private void resetAttempt() {
        answer.setLength(0);
        for (Label slot : slots) slot.setText("");
        for (TextButton tile : tiles) {
            tile.setTouchable(Touchable.enabled);
            tile.getColor().a = 1f;
        }
        feedback.setText("");
        locked = false;
    }

    private void finish() {
        User user = game.getUser();
        if (user != null) {
            user.finishRun(correct, correct == TOTAL);
            game.onUserUpdated(user);
        }
        showResult("Слова собраны!", "Собрано слов: " + correct + " из " + TOTAL,
                correct, () -> goTo(new BuildWordScreen(game)));
    }
}