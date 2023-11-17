package damonkis.view;

import damonkis.model.Monkeys;

public class StringFormatter {
    private StringFormatter() {
        throw new IllegalStateException("Utility class");
    }

    public static String getResultString(Monkeys.Result result) {
        return String.format(
                "%s (%.2f%% match, took %,d attempts)",
                result.text(), result.match() * 100, result.attempts()
        );
    }

    public static String getProgressString(Monkeys monkeys) {
        String targetPhrase = monkeys.getTargetPhrase();
        Monkeys.Result closestResult = monkeys.getClosestResult();
        Monkeys.Result lastGenerated = monkeys.getLastGenerated();
        long attempts = monkeys.getAttempts();

        String progressString;
        if (monkeys.isFinished()) {
            progressString = String.format("""
                            The monkeys have finished typing "%s"
                            It took %,d attempts
                            """,
                    targetPhrase,
                    closestResult.attempts()
            );
        } else {
            String monkeyStatus = monkeys.isTyping()
                    ? "The monkeys are typing"
                    : "The monkeys are currently taking a break from all the typing";
            progressString = String.format("""
                            %s. They are trying to type "%s"
                            Attempts so far: %,d
                            Closest match: %s
                            Last string generated: %s
                            """,
                    monkeyStatus, targetPhrase,
                    attempts,
                    getResultString(closestResult),
                    lastGenerated.text()
            );
        }
        return progressString;
    }
}
