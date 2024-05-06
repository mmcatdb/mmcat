/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.miner.functions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.sql.Blob;
import java.sql.Clob; 

/**
 *
 * @author simek.jan
 */
public class BlobClobHashing {
    public static String BlobToHash(Blob blob) {
         try (InputStream is = blob.getBinaryStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return md5Hashing(buffer.toByteArray());
        }
        catch (Exception e) {
            return "";
        }        
    }
    
    public static String ClobToHash(Clob clob) {
        try {
            Reader reader = clob.getCharacterStream();
            char[] buffer = new char[1024];
            StringBuilder sb = new StringBuilder();
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
            String data = sb.toString();
            return md5Hashing(data.getBytes());
        }
        catch (Exception e) {
           return "";
        }
    }
    
    private static String md5Hashing(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytes);
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
