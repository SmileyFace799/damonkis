package damonkis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Monkeys {
    public static final Object HISTORY_LOCK = new Object();

    private static final String LEGAL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ";
    private static final int NUMBER_OF_CHARACTERS = LEGAL_CHARACTERS.length();

    private final Thread typeThread;
    private final Random random;
    private final String targetPhrase;
    private final int tryToFindLength;
    private final List<Result> newHistory;

    private Result closestResult;
    private Result lastGenerated;
    private long attempts;
    private boolean isTyping;

    public Monkeys(String targetPhrase) {
        this.isTyping = false;
        this.random = new Random();

        this.targetPhrase = targetPhrase;
        this.tryToFindLength = targetPhrase.length();

        this.closestResult = new Result("", 0, 0, false);
        this.newHistory = new ArrayList<>();
        this.attempts = 0;

        this.typeThread = new Thread(() -> {
            while (closestResult.match() != 1) {
                type();
            }
            this.isTyping = false;
        });
        typeThread.setDaemon(true);
    }

    private String generateString() {
        StringBuilder generated = new StringBuilder();
        for (int i = 0; i < tryToFindLength; i++) {
            generated.append(LEGAL_CHARACTERS.charAt(random.nextInt(NUMBER_OF_CHARACTERS)));
        }
        return generated.toString();
    }

    private void type() {
        attempts++;
        String generatedString = generateString();
        double generatedMatch = StringSimilarity.calculate(generatedString, targetPhrase);
        if (generatedMatch >= closestResult.match()) {
            lastGenerated = new Result(
                    generatedString,
                    generatedMatch,
                    attempts,
                    generatedMatch > closestResult.match()
            );
            if (lastGenerated.match() != 0) {
                synchronized (HISTORY_LOCK) {
                    newHistory.add(0, lastGenerated);
                }
            }
            closestResult = lastGenerated;
        }
    }

    public String getTargetPhrase() {
        return targetPhrase;
    }

    public List<Result> getNewHistory() {
        return Collections.unmodifiableList(newHistory);
    }

    public void clearNewHistory() {
        synchronized (HISTORY_LOCK) {
            newHistory.clear();
        }
    }

    public Result getClosestResult() {
        return closestResult;
    }

    public Result getLastGenerated() {
        return lastGenerated;
    }

    public long getAttempts() {
        return attempts;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public boolean isFinished() {
        return closestResult.match() == 1;
    }

    public synchronized void startTyping() {
        if (closestResult.match() == 1) {
            throw new IllegalStateException("The monkeys have already found the string");
        } else if (isTyping) {
            throw new IllegalStateException("The monkeys are already typing");
        }
        this.isTyping = true;
        typeThread.start();
    }

    public synchronized void stopTyping() {
        if (!isTyping) {
            throw new IllegalStateException("The monkeys aren't typing");
        }
        this.isTyping = false;
        typeThread.interrupt();
    }

    public record Result(String text, double match, long attempts, boolean first) {}
}
