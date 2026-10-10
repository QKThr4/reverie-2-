package com.liminalreverie.story;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Tiny server-side delayed-task queue (transient, cleared when the server stops). Used for short scripted moments. */
public final class Scheduler {
    private record Task(long at, Runnable run) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0L;

    private Scheduler() {}

    public static void later(int ticks, Runnable r) {
        TASKS.add(new Task(now + Math.max(0, ticks), r));
    }

    public static void tick() {
        now++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            Task t = it.next();
            if (t.at() <= now) {
                due.add(t);
                it.remove();
            }
        }
        for (Task t : due) {
            try {
                t.run().run();
            } catch (RuntimeException ignored) {
                // a scripted moment must never be able to crash the server tick
            }
        }
    }

    public static void clear() {
        TASKS.clear();
    }
}
