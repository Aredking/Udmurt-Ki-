package com.udmurt.learn.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.udmurt.learn.assets.Assets;

/** Горизонтальная полоса прогресса урока (заполняется зелёным). */
public class ProgressBar {

    private final float width;
    private final Table fill = new Table();
    private final Table track = new Table();
    private Cell<Table> cell;

public ProgressBar(Assets assets, float width) {
        this.width = width;
        track.setBackground(assets.rounded(Assets.BG_DARK, null, 9));
        fill.setBackground(assets.rounded(Assets.GREEN, null, 9));
        cell = track.add(fill).width(2).height(18).left();
        track.add().expandX();
    }

    public Table table() {
        return track;
    }

    public void set(float fraction) {
        float avail = track.getWidth() > 0 ? track.getWidth() : width;
        float clamped = Math.max(0.03f, Math.min(1f, fraction));
        cell.width(Math.max(2f, avail * clamped));
        track.invalidateHierarchy();
    }
}