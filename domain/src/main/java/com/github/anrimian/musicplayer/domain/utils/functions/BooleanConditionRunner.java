package com.github.anrimian.musicplayer.domain.utils.functions;

public class BooleanConditionRunner {

    private final int conditionsCount;
    private final Runnable runnable;

    private int conditions = 0;

    public BooleanConditionRunner(int conditionsCount, Runnable runnable) {
        this.conditionsCount = conditionsCount;
        this.runnable = runnable;
    }

    public void setCondition(boolean set) {
        if (set) {
            conditions++;
            if (conditions >= conditionsCount) {
                runnable.run();
            }
        } else {
            if (conditions > 0) {
                conditions--;
            }
        }
    }
}
