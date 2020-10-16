package com.ustcinfo.hftnews.utils;

import java.io.*;

/**
 * IO流操作工具类
 *
 * @author zhang.yifan
 * @date 2020/9/25
 */
public class IOUtils {
    public IOUtils() {
    }

    public static String readStreamAsString(InputStream in, String charset) throws IOException {
        if (in == null) {
            return "";
        } else {
            Reader reader = null;
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];

            try {
                reader = new BufferedReader(new InputStreamReader(in, charset));
                int n;
                while((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                String result = writer.toString();
                return result;
            } finally {
                in.close();
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }

            }
        }
    }

    public static byte[] readStreamAsByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            boolean var3 = true;

            int len;
            while((len = in.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }

            output.flush();
            return output.toByteArray();
        }
    }

    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException var2) {
            }
        }

    }

    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException var2) {
            }
        }

    }

    public static boolean checkFile(File file) {
        if (file == null) {
            return false;
        } else {
            boolean exists = false;
            boolean isFile = false;
            boolean canRead = false;

            try {
                exists = file.exists();
                isFile = file.isFile();
                canRead = file.canRead();
            } catch (SecurityException var5) {
                return false;
            }

            return exists && isFile && canRead;
        }
    }


}
