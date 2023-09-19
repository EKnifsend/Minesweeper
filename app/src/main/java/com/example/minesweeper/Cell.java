package com.example.minesweeper;

import android.graphics.Color;
import android.widget.TextView;
import android.content.Context;

public class Cell {

    private Context context;

    public final int ID;

    public TextView tv;

    private int adjacentBombs;
    private boolean isBomb;
    private boolean isFlagged = false;
    private boolean revealed = false;

    private String display;

    public Cell (Context context, int id, boolean isBomb, int adjacentBombs, TextView tv) {
        this.context = context;
        this.ID = id;
        this.isBomb = isBomb;
        this.adjacentBombs = adjacentBombs;
        this.tv = tv;

        setDisplay();
    }

    private void setDisplay() {
        if (isBomb) {
            display = context.getString(R.string.mine);
        }
        else if (adjacentBombs > 0) {
            display = String.valueOf(adjacentBombs);
        }
        else {
            display = "";
        }
    }

    public int getAdjacentBombs() {
        return adjacentBombs;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public void flag() {
        isFlagged = true;
        tv.setText(context.getString(R.string.flag));
    }

    public void unFlag() {
        isFlagged = false;
        tv.setText("");
    }
    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void changeText(String s) {
        tv.setText(s);
    }

    public void reveal() {
        revealed = true;
        isFlagged = false;

        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.LTGRAY);
        tv.setText(display);
    }
}
