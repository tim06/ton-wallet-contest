package org.telegram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.github.tim06.wallet_contest.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativeLoader {

    private final static int LIB_VERSION = 30;
    private final static String LIB_NAME = "tmessages." + LIB_VERSION;
    private final static String LIB_SO_NAME = "lib" + LIB_NAME + ".so";
    private final static String LOCALE_LIB_SO_NAME = "lib" + LIB_NAME + "loc.so";

    private static volatile boolean nativeLoaded = false;

    @SuppressLint({"UnsafeDynamicallyLoadedCode", "SetWorldReadable"})
    private static boolean loadFromZip(Context context, File destDir, File destLocalFile, String folder) {
        try {
            for (File file : destDir.listFiles()) {
                file.delete();
            }
        } catch (Exception e) {
            Log.e("NativeLoader", e.getMessage());
            Log.e("NativeLoader", e.getMessage());
        }

        ZipFile zipFile = null;
        InputStream stream = null;
        try {
            zipFile = new ZipFile(context.getApplicationInfo().sourceDir);
            ZipEntry entry = zipFile.getEntry("lib/" + folder + "/" + LIB_SO_NAME);
            if (entry == null) {
                throw new Exception("Unable to find file in apk:" + "lib/" + folder + "/" + LIB_NAME);
            }
            stream = zipFile.getInputStream(entry);

            OutputStream out = new FileOutputStream(destLocalFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = stream.read(buf)) > 0) {
                Thread.yield();
                out.write(buf, 0, len);
            }
            out.close();

            destLocalFile.setReadable(true, false);
            destLocalFile.setExecutable(true, false);
            destLocalFile.setWritable(true);

            try {
                System.load(destLocalFile.getAbsolutePath());
                nativeLoaded = true;
            } catch (Error e) {
                Log.e("NativeLoader", e.getMessage());
            }
            return true;
        } catch (Exception e) {
            Log.e("NativeLoader", e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    Log.e("NativeLoader", e.getMessage());
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                    Log.e("NativeLoader", e.getMessage());
                }
            }
        }
        return false;
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public static synchronized void initNativeLibs(Context context) {
        if (nativeLoaded) {
            return;
        }

        try {
            try {
                System.loadLibrary("native-lib");
                System.loadLibrary(LIB_NAME);
                nativeLoaded = true;
                if (BuildConfig.DEBUG) {
                    Log.d("NativeLoader", "loaded normal lib");
                }
                return;
            } catch (Error e) {
                Log.e("NativeLoader", e.getMessage());
            }

            String folder;
            try {
                String str = Build.CPU_ABI;
                if (Build.CPU_ABI.equalsIgnoreCase("jniLibs/x86_64")) {
                    folder = "jniLibs/x86_64";
                } else if (Build.CPU_ABI.equalsIgnoreCase("jniLibs/arm64-v8a")) {
                    folder = "jniLibs/arm64-v8a";
                } else if (Build.CPU_ABI.equalsIgnoreCase("jniLibs/armeabi-v7a")) {
                    folder = "jniLibs/armeabi-v7a";
                } else if (Build.CPU_ABI.equalsIgnoreCase("armeabi")) {
                    folder = "armeabi";
                } else if (Build.CPU_ABI.equalsIgnoreCase("jniLibs/x86")) {
                    folder = "jniLibs/x86";
                } else if (Build.CPU_ABI.equalsIgnoreCase("mips")) {
                    folder = "mips";
                } else {
                    folder = "armeabi";
                    if (BuildConfig.DEBUG) {
                        Log.e("NativeLoader", "Unsupported arch: " + Build.CPU_ABI);
                    }
                }
            } catch (Exception e) {
                Log.e("NativeLoader", e.getMessage());
                folder = "armeabi";
            }

            String javaArch = System.getProperty("os.arch");
            if (javaArch != null && javaArch.contains("686")) {
                folder = "jniLibs/x86";
            }

            File destDir = new File(context.getFilesDir(), "lib");
            destDir.mkdirs();

            File destLocalFile = new File(destDir, LOCALE_LIB_SO_NAME);
            if (destLocalFile.exists()) {
                try {
                    if (BuildConfig.DEBUG) {
                        Log.d("NativeLoader", "Load local lib");
                    }
                    System.load(destLocalFile.getAbsolutePath());
                    nativeLoaded = true;
                    return;
                } catch (Error e) {
                    Log.e("NativeLoader", e.getMessage());
                }
                destLocalFile.delete();
            }

            if (BuildConfig.DEBUG) {
                Log.e("NativeLoader", "Library not found, arch = " + folder);
            }

            if (loadFromZip(context, destDir, destLocalFile, folder)) {
                return;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            System.loadLibrary("native-lib");
            System.loadLibrary(LIB_NAME);
            nativeLoaded = true;
        } catch (Error e) {
            Log.e("NativeLoader", e.getMessage());
        }
    }
}
