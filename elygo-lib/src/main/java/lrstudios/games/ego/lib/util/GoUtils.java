package lrstudios.games.ego.lib.util;


public class GoUtils {
    private static final String BOARD_LETTERS = "ABCDEFGHJKLMNOPQRSTUVWXYZ";

    /**
     * Returns the characters representing the specified X coordinate (0 = A, 1 = B, ...).
     * The character 'I' isn't used.
     */
    public static String getCoordinateChars(int x) {
        int max = BOARD_LETTERS.length();
        int firstCharIndex = Math.min(max, x / max) - 1;
        int secondCharIndex = x % max;

        if (firstCharIndex >= 0)
            return Character.toString(BOARD_LETTERS.charAt(firstCharIndex)) + BOARD_LETTERS.charAt(secondCharIndex);
        else
            return Character.toString(BOARD_LETTERS.charAt(secondCharIndex));
    }
}
