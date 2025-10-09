package cz.matfyz.core.rsd.utils;

import java.util.Arrays;
import java.text.Normalizer;

public class CharToIndexConverter {

    static final int[] map = new int[512];

    static {

        // 36 is default value for non specified characters
        Arrays.fill(map, 36);
        // Mapping '0' to '9' to integer values 0 to 9
        for (char c = '0'; c <= '9'; c++)
            map[c] = c - '0';

        // Mapping both 'a' to 'z' and 'A' to 'Z' to integer values 10 to 35
        for (char c = 'a'; c <= 'z'; c++)
            map[c] = c - 'a' + 10;

        for (char c = 'A'; c <= 'Z'; c++)
            map[c] = c - 'A' + 10;

        // Mapping modified letters to normalized versions (maybe a bad idea)
        char[] specialChars = new char[]{ 'á', 'é', 'í', 'ó', 'ú', 'â', 'ê', 'î', 'ô',
            'û', 'ã', 'õ', 'ä', 'ë', 'ï', 'ö', 'ü', 'à', 'è', 'ì', 'ò', 'ù', 'ā', 'ē',
            'ī', 'ō', 'ū', 'ç', 'å', 'ć', 'ď', 'ľ', 'ň', 'ř', 'š', 'ť', 'ž', 'ǎ', 'č',
            'ě', 'ǐ', 'ǒ', 'ǔ', 'ń', 'ś', 'ź', 'ż', 'å',
                                          'Á', 'É', 'Í', 'Ó', 'Ú', 'Â', 'Ê', 'Î', 'Ô',
            'Û', 'Ã', 'Õ', 'Ä', 'Ë', 'Ï', 'Ö', 'Ü', 'À', 'È', 'Ì', 'Ò', 'Ù', 'Ā', 'Ē',
            'Ī', 'Ō', 'Ū', 'Ç', 'Å', 'Ć', 'Ď', 'Ľ', 'Ň', 'Ř', 'Š', 'Ť', 'Ž', 'Ǎ', 'Č',
            'Ě', 'Ǐ', 'Ǒ', 'Ǔ', 'Ń', 'Ś', 'Ź', 'Ż', 'Å' };

        for (int i = 0; i < specialChars.length; i++) {
            final char original = specialChars[i];
            final char normalized = normalizeChar(original);
            map[original] = normalized - 'a' + 10;
        }

    }

    public static int convert(char c) {
        // char normalizedC = normalizeChar(c);
        return c > 512 ? 36 : map[c];
    }

    public static int convertWithEnding(char starting, char ending) {
        final int start = (starting > 512) ? 36 : map[starting];
        final int end = (ending > 512) ? 36 : map[ending];
        return (start * 37) + end;
    }

    public static char normalizeChar(char c) {
        String str = Character.toString(c);
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        str = str.toLowerCase();
        return str.charAt(0);
    }

}
