package com.udmurt.learn.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.Word;
import com.udmurt.learn.data.WordBank;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;
import com.udmurt.learn.ui.Ui;

/**
 * Окно №4: игра «Найди пару».
 * Дошкольники ищут одинаковые картинки, школьники — слово и его перевод.
 */
public class MemoryGameScreen extends BaseScreen {

    private static class Card {
        Table table;
        Label label;
        Image image;
        int pairId;
        String content;
        Color color;
        boolean revealed;
        boolean matched;
    }

    private final Role role;
    private final int pairs;
    private final Array<Card> cards = new Array<>();
    private final Array<Card> deck = new Array<>();

    private final Label statusLabel;
    private final com.udmurt.learn.ui.ProgressBar progressBar;
    private final Drawable closedBg;
    private final Drawable revealedBg;
    private final Drawable matchedBg;

    private Card first;
    private boolean busy;
    private int moves;
    private int found;

    public MemoryGameScreen(UdmurtGame game) {
        super(game);
        this.role = game.getUser() != null ? game.getUser().role : Role.PRESCHOOL;
        this.pairs = role == Role.SCHOOL ? 6 : 4;

        closedBg = skin.getDrawable("card");
        revealedBg = skin.getDrawable("panel");
        matchedBg = assets.rounded(Color.valueOf("E7F5E9"), Assets.GREEN, 16);
        progressBar = new com.udmurt.learn.ui.ProgressBar(assets, 600);

        root.add(header("Найди пару", true)).growX().top().row();
        root.add(progressBar.table()).growX().pad(10, 22, 0, 22).row();

        Table body = new Table();
        body.pad(18);
        body.top();

        statusLabel = Ui.label(skin, "", "h1");
        body.add(statusLabel).padBottom(12).row();

        Table grid = new Table();
        body.add(grid).expand().top().row();

        root.add(body).expand().fill().row();

        buildDeck();
        int column = 0;
        for (Card card : deck) {
            grid.add(card.table).size(150, 150).pad(8);
            if (++column % 4 == 0) grid.row();
        }
        updateStatus();
    }

    private void buildDeck() {
        Array<Word> pool = role == Role.SCHOOL ? WordBank.forRole(role) : WordBank.withPicture(role);
        Array<Word> chosen = new Array<>(pool);
        chosen.shuffle();

        for (int i = 0; i < pairs && i < chosen.size; i++) {
            Word word = chosen.get(i);
            if (role == Role.SCHOOL) {
                deck.add(newCard(word, word.russian, i, Assets.TEXT));
                deck.add(newCard(word, word.udmurt, i, Assets.TEXT));
            } else {
                Color color = Assets.color(i);
                deck.add(newCard(word, word.image, i, color));
                deck.add(newCard(word, word.image, i, color));
            }
        }
        deck.shuffle();
    }

    private Card newCard(Word word, String content, int pairId, final Color color) {
        final Card card = new Card();
        card.pairId = pairId;
        card.content = content;
        card.color = color;

        card.table = new Table();
        card.table.setBackground(closedBg);

        Stack stack = new Stack();
        card.label = new Label("?", skin, role == Role.SCHOOL ? "h1" : "small");
        card.label.setColor(Assets.MUTED);
        card.label.setAlignment(com.badlogic.gdx.utils.Align.center);
        if (role == Role.PRESCHOOL && content != null) {
            Texture tex = assets.picture(content);
            if (tex != null) {
                card.image = new Image(tex);
                card.image.setScaling(Scaling.fit);
                card.image.setVisible(false);
                stack.add(card.image);
            }
        }
        stack.add(card.label);
        card.table.add(stack).expand().fill();

        card.table.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                reveal(card);
            }
        });
        return card;
    }

    private void reveal(Card card) {
        if (busy || card.matched || card.revealed) return;

        card.revealed = true;
        if (card.image != null) {
            card.label.setVisible(false);
            card.image.setVisible(true);
        } else {
            card.label.setText(card.content);
            card.label.setColor(card.color);
        }
        card.table.setBackground(revealedBg);

        if (first == null) {
            first = card;
            return;
        }

        moves++;
        if (first.pairId == card.pairId) {
            first.matched = true;
            card.matched = true;
            first.table.setBackground(matchedBg);
            card.table.setBackground(matchedBg);
            first = null;
            found++;
            updateStatus();
            if (found == pairs) {
                win();
            }
        } else {
            busy = true;
            final Card a = first;
            final Card b = card;
            first = null;
            stage.addAction(Actions.sequence(Actions.delay(0.8f), Actions.run(() -> hide(a, b))));
        }
        updateStatus();
    }

    private void hide(Card a, Card b) {
        reset(a);
        reset(b);
        busy = false;
    }

    private void reset(Card card) {
        card.revealed = false;
        if (card.image != null) {
            card.label.setVisible(true);
            card.image.setVisible(false);
        } else {
            card.label.setText("?");
            card.label.setColor(Assets.MUTED);
        }
        card.table.setBackground(closedBg);
    }

    private void updateStatus() {
        statusLabel.setText("Найдено пар: " + found + " / " + pairs + "   ·   Ходов: " + moves);
        progressBar.set((float) found / pairs);
    }

    private void win() {
        int stars = Math.max(1, pairs * 2 - moves + pairs);
        User user = game.getUser();
        if (user != null) {
            if (stars > user.bestMemory) user.bestMemory = stars;
            user.finishRun(stars, moves == pairs);
            game.onUserUpdated(user);
        }
        showResult("Ура, все пары найдены!",
                "Ходов: " + moves,
                stars,
                () -> goTo(new MemoryGameScreen(game)));
    }
}
