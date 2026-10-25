package com.udmurt.learn.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.udmurt.learn.UdmurtGame;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.ui.Ui;

/** Окно №3: удмуртский алфавит и особые буквы. */
public class AlphabetScreen extends BaseScreen {

    private static final String ALPHABET =
            "АБВГДЕЁЖӜЗӞИӤЙКЛМНОӦПРСТУФХЦЧӴШЩЪЫЬЭЮЯ";

    public AlphabetScreen(UdmurtGame game) {
        super(game);
        build();
    }

    private void build() {
        root.add(header("Удмуртский алфавит", true)).growX().top().row();

        Table body = new Table();
        body.pad(20);
        body.top();

        body.add(centered(skin, "В удмуртском алфавите 38 букв. Есть 5 особых букв.", "muted"))
                .padBottom(14).row();

        Table grid = new Table();
        int column = 0;
        for (char letter : ALPHABET.toCharArray()) {
            Table tile = Ui.card(skin, "card");
            Label label = new Label(String.valueOf(letter), new LabelStyle(assets.fontTitle, Assets.TEXT));
            tile.add(label).expand().center();
            grid.add(tile).size(112, 112).pad(6);
            if (++column % 6 == 0) grid.row();
        }
        body.add(grid).padBottom(20).row();

        Table legend = Ui.card(skin, "panel");
        legend.pad(18);
        legend.add(Ui.label(skin, "Особые буквы", "h1")).left().colspan(2).padBottom(10).row();
        legendRow(legend, "Ӝ", "«дж» — слитный звонкий звук");
        legendRow(legend, "Ӟ", "«дз» — мягкий слитный звук");
        legendRow(legend, "Ӧ", "особый гласный, средний между «о» и «э»");
        legendRow(legend, "Ӵ", "«тш» — слитный мягкий звук");
        legendRow(legend, "Ӥ", "короткий «й» (неслоговой «и»)");
        body.add(legend).growX().left().row();

        ScrollPane scroll = new ScrollPane(body, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        root.add(scroll).expand().fill().row();
    }

    private void legendRow(Table legend, String letter, String description) {
        legend.add(Ui.label(skin, letter, "h1")).width(70).left().padBottom(6);
        legend.add(Ui.label(skin, description, "muted")).expandX().left().padBottom(6).row();
    }
}
