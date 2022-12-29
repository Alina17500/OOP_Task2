package ru.vsu.cs.volobueva.panels;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.netbeans.lib.awtextra.AbsoluteLayout;

import lombok.Getter;
import lombok.Setter;
import ru.vsu.cs.volobueva.engine.DominoBone;
import ru.vsu.cs.volobueva.engine.GameParameters;

@Getter
@Setter
public abstract class Game extends JPanel {
    private static final long serialVersionUID = -3490722431721194231L;
    private DominoBone selectedLeft;
    private DominoBone selectedRight;
    private List<DominoBone> dominoBones = new LinkedList<>(); // камни на панели

    public Game() {
        setBackground(GameParameters.color);
        setLayout(new AbsoluteLayout());
    }

    public void showBones() {
        dominoBones.forEach(DominoBone::showBone);
    }

    public void removeFromBones(DominoBone dominoBone) {
        dominoBones.remove(dominoBone);
        remove(dominoBone);
    }

    protected abstract void rebuildBonesLine(boolean frame);

    protected abstract void addToBones(DominoBone dominoBone);

    protected abstract void setTitle(String s);
}
