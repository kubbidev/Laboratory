package me.kubbidev.laboratory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kubbidev.laboratory.cooldown.Cooldown;
import me.kubbidev.laboratory.damage.DamageMetadata;
import me.kubbidev.laboratory.locale.TranslationManager;
import me.kubbidev.laboratory.serialize.*;
import me.kubbidev.laboratory.serialize.storage.GsonStorageHandler;
import me.kubbidev.laboratory.util.ArithmeticException;
import me.kubbidev.laboratory.util.DoubleEvaluator;
import me.kubbidev.laboratory.util.DurationParser;
import me.kubbidev.laboratory.util.FastMath;
import me.kubbidev.laboratory.util.DurationFormatter;
import me.kubbidev.laboratory.util.gson.GsonProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
    @Getter
    private static final Path applicationFolder = Paths.get("data").toAbsolutePath();

    @Getter
    private static final TranslationManager translationManager;

    static {
        translationManager = new TranslationManager();
        translationManager.reload();
    }

    public static void main(String[] args) {
        Set<Locale> installedLocales = translationManager.getInstalledLocales();

        log.info("Installed a total of: {} translation(s)", installedLocales.size());
        for (Locale locale : installedLocales) {
            log.info("@ {}", locale.toString());
        }

        Duration duration = DurationParser.parseDuration("2w 5h 45m");

        log.info(DurationFormatter.CONCISE.format(duration));
        log.info(DurationFormatter.CONCISE_LOW_ACCURACY.format(duration));
        log.info(DurationFormatter.LONG.format(duration));

        long r = 904281685607211028L;
        long k = 537669034137616384L;

        UUID uuid = UUID.fromString("6426aa6b-2ee3-4ccd-bd4c-4301e08dc69d");

        UUID uuid1 = new UUID(r, 0L);
        UUID uuid2 = new UUID(k, 0L);

        log.info(uuid1.toString());
        log.info(uuid2.toString());
        log.info("{}     {}", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        List<Long> longList = new ArrayList<>();
        // Populate the list with some values (including duplicates)
        longList.add(10L);
        longList.add(20L);
        longList.add(30L);
        longList.add(50L);
        longList.add(20L); // Duplicate value
        longList.add(40L);
        longList.add(50L);
        longList.add(20L); // Duplicate value

        // Print the original list
        log.info("Original list: {}", longList);

        // Remove the first occurrence of the long value from the end
        long valueToRemove = 50; // Get the value from the end
        for (int i = longList.size() - 1; i >= 0; i--) {
            if (longList.get(i).equals(valueToRemove)) {
                longList.remove(i);
                break; // Remove only the first occurrence from the end
            }
        }

        // Print the modified list
        log.info("List after removing the first occurrence from the end: {}", longList);

        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime nextMonday = currentDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(15)
                .withMinute(0)
                .withSecond(0);

        // that condition won't actually be true in practice, it's just a safeguard to
        // ensure that even if the current time happens to be after the specified time for the next occurrence,
        // we still schedule the task for the next Monday.
        if (!currentDate.isBefore(nextMonday)) {
            // if today is already after next Monday at 3 PM, schedule for the following Monday
            nextMonday = nextMonday.plusWeeks(1);
        }

        long s = nextMonday.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        log.info("<t:{}:R>", s);

        log.info(System.getProperty("java.version"));
        log.info("{} {} ({})",
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.vm.vendor"));

        UUID uniqueId = UUID.fromString("10e3fbd8-a684-1029-0cfa-71b784423014");
        log.info("Most  significant: {}", uniqueId.getMostSignificantBits());
        log.info("Least significant: {}", uniqueId.getLeastSignificantBits());

        String[] oldArray = {"main", "m", "test"};
        String[] newArray = new String[oldArray.length - 1];
        System.arraycopy(oldArray, 1, newArray, 0, oldArray.length - 1);

        log.info(Arrays.toString(newArray));

        float rad = (float) (2 * Math.PI / 3);
        log.info("mathCos: {}", Math.cos(rad));
        log.info("fastCos: {}", FastMath.cos(rad));

        BlockPosition pos1 = BlockPosition.of(
                (int) ((Math.random() + 1) * 12),
                (int) ((Math.random() + 1) * 12),
                (int) ((Math.random() + 1) * 12), "world");

        CircularRegion region = CircularRegion.of(pos1.toPositionCenter(), (int) ((Math.random() + 1) * 16));
        GsonStorageHandler<CircularRegion> storageHandler = new GsonStorageHandler<>(
                "circular-region", ".json", applicationFolder.resolve("region"),
                CircularRegion.class,
                GsonProvider.prettyPrinting());
        try {
            storageHandler.save(region);
            storageHandler.load().ifPresent(a -> log.info("loaded object: " + a));
        } catch (IOException e) {
            throw new RuntimeException("Exception while saving " + region, e);
        }

        double baseDamage = 24.0;
        double potionAmplifier = 3;

        //noinspection UnaryPlus
        double strengthModifier = +0.33 * potionAmplifier; // example strength modifier: increase 33.0%
        double weaknessModifier = -0.33 * potionAmplifier; // example weakness modifier: decrease 33.0%

        DamageMetadata damageMetadata = new DamageMetadata(baseDamage);
        damageMetadata.multiplicativeModifier(1.0 + strengthModifier);
        damageMetadata.multiplicativeModifier(1.0 + weaknessModifier);
        log.info("Damage: " + damageMetadata.getDamage());

        DoubleEvaluator evaluator = new DoubleEvaluator("14 * 9 + 5^x / 78.5");
        evaluator.registerVariable("x", 2);
        try {
            log.info("Eval: " + evaluator.eval());
        } catch (ArithmeticException e) {
            throw new RuntimeException(e);
        }

        double seconds = 45.5;

        Cooldown cooldown = Cooldown.of((long) (seconds * 1000L), TimeUnit.MILLISECONDS);
        cooldown.reset();

        log.info("Remaining millis: " + cooldown.remainingTime(TimeUnit.SECONDS));
        cooldown.reduceRemainingCooldown(0.1f);
        log.info("Remaining millis: " + cooldown.remainingTime(TimeUnit.SECONDS));
        cooldown.reduceInitialCooldown(0.1f);
        log.info("Remaining millis: " + cooldown.remainingTime(TimeUnit.SECONDS));
    }

}