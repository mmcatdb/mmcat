package cz.matfyz.wrapperriak;

import java.nio.charset.Charset;

class RiakUtils {
    /**
     * Returns a deterministic and unique index name for a bucket type-bucket pair.
     * @param bucketType name of the bucket type
     * @param bucket name of the bucket
     * @return name of the index
     */
    static String createIndexName(String bucketType, String bucket) {
        return String.format("%s_%s_%s_%s", bucketType.length(), bucket.length(), bucketType, bucket);
    }

    private static boolean isValidChar(byte b) {
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

    /**
     * Encodes a URI path segment in accordance with RFC 2396. Invalid characters are percent-encoded using the given charset.
     * @param pathSegment raw path segment to encode
     * @param charset the {@link Charset} to use during encoding
     * @return the encoded path segment
     */
    static String encodeUriPathSegment(String pathSegment, Charset charset) {
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
