package org.utfpr.mf.tools;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.utfpr.mf.interfaces.IMfMigrationStep;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MfCacheController {

    public void save(Class<? extends IMfMigrationStep> step, String md5, Object data) throws IOException {

        File file = new File(System.getProperty("java.io.tmpdir"), "mf-cache-" + md5 + ".json");
        PrintStream out = new PrintStream(new FileOutputStream(file));
        Gson gson = new Gson();
        out.println(gson.toJson(data));
    }

    public <T> T load(Class<? extends IMfMigrationStep> step, String md5) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), "mf-cache-" + md5 + ".json");
        if(!file.exists()) {
            return null;
        }

        JsonReader jr = new JsonReader(new FileReader(file));
        Gson gson = new Gson();
        return gson.fromJson(jr, step);
    }


    public static String generateMD5(Object obj)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(obj.toString().getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String combineMD5(List<String> mds) {
        StringBuilder sb = new StringBuilder();
        for (String md : mds) {
            sb.append(generateMD5(md));
        }
        return generateMD5(sb);
    }

}
