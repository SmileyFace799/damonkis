package damonkis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Monkeys {
    private static final String LEGAL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ";
    private static final int NUMBER_OF_CHARACTERS = LEGAL_CHARACTERS.length();

    private final Thread typeThread;
    private final Random random;
    private final String tryToFind;
    private final int tryToFindLength;
    private final List<Result> closestHistory;

    private Result closestResult;
    private long attempts;
    private Result lastGenerated;
    private boolean isTyping;

    public Monkeys(String tryToFind) {
        this.isTyping = false;
        this.random = new Random();

        this.tryToFind = tryToFind;
        this.tryToFindLength = tryToFind.length();

        this.closestResult = new Result("", 0, 0);
        this.closestHistory = new ArrayList<>();
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
        lastGenerated = new Result(generatedString, StringSimilarity.calculate(generatedString, tryToFind), attempts);
        if (lastGenerated.match() >= closestResult.match()) {
            if (lastGenerated.match() != 0 && lastGenerated.match() > closestResult.match()) {
                synchronized (this) {
                    this.closestHistory.add(lastGenerated);
                }
            }
            closestResult = lastGenerated;
        }
    }

    public Result getClosestResult() {
        return closestResult;
    }

    public String getProgress() {
        String progressString;
        if (closestResult.match() == 1) {
            progressString = String.format("""
                            The monkeys have finished typing "%s"
                            It took %,d attempts
                            
                            Historical breakthroughs:
                            %s
                            """,
                    tryToFind,
                    closestResult.attempts(),
                    String.join("\n", closestHistory
                            .stream()
                            .map(Result::getFormattedString)
                            .toList()
                    )

            );
        } else {
            progressString = String.format("""
                        The monkeys are typing. They are trying to type "%s"
                        Attempts so far: %,d
                        Closest match: %s
                        Last string generated: %s
                        
                        Historical breakthroughs:
                        %s
                        """,
                    tryToFind,
                    attempts,
                    closestResult.getFormattedString(),
                    lastGenerated.text(),
                    String.join("\n", getHistoryStrings()
                    )
            );
        }
        return progressString;
    }

    private synchronized List<String> getHistoryStrings() {
        return closestHistory
                .stream()
                .map(Result::getFormattedString)
                .toList();
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

    public record Result(String text, double match, long attempts) {
        public String getFormattedString() {
            return String.format("%s (%.2f%% match, took %,d attempts)", text, match * 100, attempts);
        }
    }
}
