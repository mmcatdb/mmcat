package cz.matfyz.wrapperriak;

import java.nio.charset.Charset;

public class RiakUtils {
    private static boolean isValidChar(byte b) {
        // RFC 2396
        if ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z'))
            return true;
        if (b >= '0' && b <= '9')
            return true;
        if (b == '-' || b == '_' || b == '.' || b == '!' || b == '~' || b == '*' || b == '\'' || b == '(' || b == ')')
            return true;
        //if (b == ':' || b == '@' || b == '&' || b == '=' || b == '+' || b == '$' || b == ',')
        //    return true;
        return false;
    }

    protected static String encodeUriPathSegment(String pathSegment, Charset charset) {
        byte[] bytes = pathSegment.getBytes(charset);

        for (var b : bytes) {
            if (!isValidChar(b)) {
                var sb = new StringBuilder(bytes.length);

                for (var c : bytes) {
                    if (isValidChar(c)) {
                        sb.append((char)c);
                    }
                    else {
                        sb.append('%');
                        sb.append(Character.forDigit((c >> 4) & 0x0F, 16));
                        sb.append(Character.forDigit(c & 0x0F, 16));
                    }
                }

                return sb.toString();
            }
        }

        return pathSegment;
    }
}
