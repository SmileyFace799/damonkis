package damonkis.model;

public class StringSimilarity {
    private StringSimilarity() {
        throw new IllegalStateException("Utility class");
    }

    static double calculate(String x, String y) {
        int strLength = x.length();
        int matchingChars = 0;
        for (int i = 0; i < strLength; i++) {
            if (x.charAt(i) == y.charAt(i)) {
                matchingChars++;
            }
        }
        return (double) matchingChars / strLength;
    }
}
