package me.kubbidev.laboratory.util;

import lombok.Getter;
import lombok.Setter;
import me.kubbidev.laboratory.scheduler.SchedulerAdapter;
import me.kubbidev.laboratory.scheduler.SchedulerTask;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTask implements Runnable {
    private final SchedulerAdapter schedulerAdapter;
    private final ScheduleSettings scheduleSettings;

    @Getter
    private boolean scheduled;

    @Setter
    @Getter
    private @Nullable DayOfWeek startingDay = null;

    @Setter
    @Getter
    private @Nullable LocalTime startingTime = null;

    @Nullable
    private SchedulerTask repeatingTask;

    public ScheduledTask(SchedulerAdapter schedulerAdapter, ScheduleSettings scheduleSettings) {
        this.schedulerAdapter = schedulerAdapter;
        this.scheduleSettings = scheduleSettings;
    }

    public void cancel() {
        if (this.repeatingTask != null) {
            this.repeatingTask.cancel();
        }
        this.scheduled = false;
    }

    public void schedule() {
        if (isScheduled()) {
            throw new IllegalStateException("Already scheduled");
        }
        this.scheduled = true;
        this.repeatingTask = this.schedulerAdapter.asyncLater(() -> {
            // run the task manually for the first time, if we don't do this the
            // execution will be at the next schedule date
            this.run();

            // and now that we are the right date time, normally schedule the repeating task
            this.repeatingTask = this.schedulerAdapter.asyncRepeating(this,
                    this.scheduleSettings.duration,
                    this.scheduleSettings.unit
            );
        }, calculateInitialMillisDelay(), TimeUnit.MILLISECONDS);
    }

    private long calculateInitialMillisDelay() {
        return Duration.between(LocalDateTime.now(), getNextScheduleDate()).toMillis();
    }

    public LocalDateTime getNextScheduleDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime targetTime = this.startingTime == null ? currentDate
                : currentDate.with(this.startingTime);

        DayOfWeek dayOfWeek = this.startingDay;
        if (dayOfWeek == null) {
            dayOfWeek = currentDate.plus(
                    this.scheduleSettings.duration,
                    this.scheduleSettings.unit.toChronoUnit()
            ).getDayOfWeek();
        }

        return targetTime.with(
                currentDate.isBefore(targetTime)
                        ? TemporalAdjusters.nextOrSame(dayOfWeek)
                        : TemporalAdjusters.next(dayOfWeek)
        );
    }

    public static ScheduleSettings scheduleSettings(long duration, TimeUnit unit) {
        return new ScheduleSettings(duration, unit);
    }

    public record ScheduleSettings(long duration, TimeUnit unit) {

    }
}