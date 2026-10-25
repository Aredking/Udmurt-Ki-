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
 * «Матч-безумие»: на время соединяй удмуртские слова с переводом.
 * Правильные пары повышают множитель, ошибка его сбрасывает.
 */
public class MatchMadnessScreen extends BaseScreen {

    private static final float START_TIME = 60f;
    private static final int PAIRS = 5;
    private static final int MAX_COMBO = 5;

    private static class Item {
        TextButton button;
        int pair;
        boolean matched;
        boolean leftSide;
    }

    private final Role role;
    private final Array<Word> pool;
    private final Array<Item> left = new Array<>();
    private final Array<Item> right = new Array<>();

    private final TextButtonStyle normalStyle;
    private final TextButtonStyle selectedStyle;
    private final TextButtonStyle badStyle;

    private final Label scoreLabel;
    private final Label comboLabel;
    private final com.udmurt.learn.ui.ProgressBar timeBar;
    private final Table leftColumn;
    private final Table rightColumn;

    private float timeLeft = START_TIME;
    private int score;
    private int combo;
    private boolean over;
    private Item selected;

    public MatchMadnessScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pool = WordBank.forRole(role);

        normalStyle = skin.get("ghost", TextButtonStyle.class);
        selectedStyle = skin.get("primary", TextButtonStyle.class);
        badStyle = skin.get("danger", TextButtonStyle.class);

        scoreLabel = Ui.label(skin, "★ 0", "h1");
        comboLabel = Ui.label(skin, "Комбо ×1", "h1");
        timeBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Матч-безумие", true)).growX().top().row();

        Table top = new Table();
        top.add(comboLabel).expandX().left();
        top.add(scoreLabel).right();
        root.add(top).growX().pad(10, 22, 0, 22).row();
        root.add(timeBar.table()).growX().pad(6, 22, 0, 22).row();

        Table board = new Table();
        leftColumn = new Table();
        rightColumn = new Table();
        board.add(leftColumn).growY().width(360).padRight(20);
        board.add(rightColumn).growY().width(360);
        root.add(board).expand().top().pad(14).row();
        root.add(Ui.label(skin, "Выбери слово и его перевод", "muted")).padBottom(10).row();

        newBoard();
        timeBar.set(1f);
    }

    @Override
    public void render(float delta) {
        if (!over) {
            timeLeft -= delta;
            timeBar.set(timeLeft / START_TIME);
            if (timeLeft <= 0f) {
                timeLeft = 0f;
                finish();
            }
        }
        super.render(delta);
    }

    private void newBoard() {
        Array<Word> chosen = new Array<>(pool);
        chosen.shuffle();
        left.clear();
        right.clear();
        leftColumn.clearChildren();
        rightColumn.clearChildren();

        for (int i = 0; i < PAIRS && i < chosen.size; i++) {
            Word word = chosen.get(i);
            left.add(newItem(word.udmurt, i, true));
            right.add(newItem(word.russian, i, false));
        }
        left.shuffle();
        right.shuffle();

        for (Item item : left) leftColumn.add(item.button).growX().height(60).pad(6).row();
        for (Item item : right) rightColumn.add(item.button).growX().height(60).pad(6).row();
    }

    private Item newItem(String text, int pair, boolean leftSide) {
        final Item item = new Item();
        item.pair = pair;
        item.leftSide = leftSide;
        item.button = new TextButton(text, normalStyle);
        item.button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tap(item);
            }
        });
        return item;
    }

    private void tap(Item item) {
        if (over || item.matched) return;
        if (selected == null) {
            select(item);
            return;
        }
        if (selected == item) {
            clearSelection();
            return;
        }
        if (selected.leftSide == item.leftSide) {
            clearSelection();
            select(item);
            return;
        }
        if (selected.pair == item.pair) {
            match(selected, item);
        } else {
            wrong(selected, item);
        }
    }

    private void select(Item item) {
        selected = item;
        item.button.setStyle(selectedStyle);
    }

    private void clearSelection() {
        if (selected != null) selected.button.setStyle(normalStyle);
        selected = null;
    }

    private void match(Item a, Item b) {
        a.matched = true;
        b.matched = true;
        combo = Math.min(MAX_COMBO, combo + 1);
        score += 10 * combo;
        a.button.setTouchable(Touchable.disabled);
        b.button.setTouchable(Touchable.disabled);
        a.button.getColor().a = 0.35f;
        b.button.getColor().a = 0.35f;
        selected = null;
        scoreLabel.setText("★ " + score);
        comboLabel.setText("Комбо ×" + combo);

        int matched = 0;
        for (Item item : left) if (item.matched) matched++;
        if (matched == PAIRS && matched > 0) {
            timeLeft += 6f;
            newBoard();
        }
    }

    private void wrong(Item a, Item b) {
        combo = 0;
        comboLabel.setText("Комбо ×1");
        final Item first = a;
        final Item second = b;
        selected = null;
        first.button.setStyle(badStyle);
        second.button.setStyle(badStyle);
        first.button.setTouchable(Touchable.disabled);
        second.button.setTouchable(Touchable.disabled);
        stage.addAction(Actions.sequence(Actions.delay(0.4f), Actions.run(() -> {
            if (!first.matched) { first.button.setStyle(normalStyle); first.button.setTouchable(Touchable.enabled); }
            if (!second.matched) { second.button.setStyle(normalStyle); second.button.setTouchable(Touchable.enabled); }
        })));
    }

    private void finish() {
        over = true;
        int starsEarned = score / 10;
        User user = game.getUser();
        if (user != null) {
            user.finishRun(starsEarned, false);
            game.onUserUpdated(user);
        }
        showResult("Время вышло!",
                "Соединено пар: " + starsEarned,
                starsEarned,
                () -> goTo(new MatchMadnessScreen(game)));
    }
}
