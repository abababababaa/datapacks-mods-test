package dev.datapacksmods.obsidian;

/** Tracks only the short burn window created by the sword; never a permanent mob tag. */
public interface ObsidianBurnTracker {
    long obsidian$getBurnDeadline();
    void obsidian$setBurnDeadline(long tick);
    boolean obsidian$claimHeadDrop();
}
