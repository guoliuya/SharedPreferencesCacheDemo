package com.guoliuya.sharePreferencesCache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.UUID;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public static final String CACHE_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath() + File.separator + "yizhibo";
    public static final String CACHE_DOWNLOAD_DIR = CACHE_DIR + File.separator + "download";
    public static final String CACHE_SHARE_DIR = CACHE_DIR + File.separator + "share";
    public static final String CACHE_IMAGE_DIR = CACHE_DIR + File.separator + "image";
    public static final String CACHE_MUSIC = CACHE_DIR + File.separator + "music";
    public static final String CACHE_MUSIC_DOWNLOAD = CACHE_MUSIC + File.separator + "download";
    public static final String MUSIC_FILE_SUFFIX =".temp";
    public static final String CACHE_PUBLIC_BENEFIT_ACTIVITY =CACHE_DIR+File.separator+"benefit";
    public static final String CACHE_WEBVIEW=CACHE_DIR+File.separator+"cache";

    public static boolean checkCacheDir() {
        // BuildConfig.VERSION_CODE < 53 have yzb directory
        // TODO No need it next version
        String oldCacheDir = Environment
                .getExternalStorageDirectory().getAbsolutePath() + File.separator + "yzb";
        deleteDir(new File(oldCacheDir));
        // BuildConfig.VERSION_CODE < 67 have yilive directory
        oldCacheDir = Environment
                .getExternalStorageDirectory().getAbsolutePath() + File.separator + "yilive";
        deleteDir(new File(oldCacheDir));

        File noMediaFile = new File(CACHE_IMAGE_DIR + File.separator + ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return createDirectory(CACHE_DIR)
                && createDirectory(CACHE_IMAGE_DIR)
                && createDirectory(CACHE_SHARE_DIR) && createDirectory(CACHE_MUSIC) && createDirectory(CACHE_PUBLIC_BENEFIT_ACTIVITY)&&createDirectory(CACHE_WEBVIEW);
    }

    public static boolean createDirectory(String filePath) {
        if (null == filePath) {
            return false;
        }

        if (CACHE_IMAGE_DIR.equals(filePath)) {
            File noMediaFile = new File(CACHE_IMAGE_DIR + File.separator +".nomedia");
            if (!noMediaFile.exists()) {
                try {
                    noMediaFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File file = new File(filePath);

        return file.exists() || file.mkdirs();
    }

    public static void deleteDir(File file) {
        if (file.exists()) {
            File[] fileList = file.listFiles();
            if(fileList!=null){
                for (File f : fileList) {
                    if (f.isDirectory()) {
                        deleteDir(f);
                    } else {
                        f.delete();
                    }
                }
                file.delete();
            }
        }
    }

    public static void checkDirExist(String path) {
        File file = new File(path);
        if(!file.exists()){
            file.mkdir();
        }
    }

    public static boolean copyAssetData(Context context, String assetsFileName, String targetPath) {
        try {
            InputStream inputStream = context.getAssets().open(assetsFileName);
            FileOutputStream output = new FileOutputStream(targetPath + File.separator + assetsFileName);
            byte[] buf = new byte[10240];
            int count = 0;
            while ((count = inputStream.read(buf)) > 0) {
                output.write(buf, 0, count);
            }
            output.close();
            inputStream.close();
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void copyAssetsFiles(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyAssetsFiles(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileName(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }
        return filePath;
    }

    public static boolean isFileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            return false;
        }

        File f = new File(filePath);

        return f.exists();
    }

    public static String getFileNameBody(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            String fullFileName = getFileName(filePath);
            int dotPosition = fullFileName.lastIndexOf(".");
            if (dotPosition > 0) {
                return fullFileName.substring(0, dotPosition);
            } else {
                return fullFileName;
            }
        }

        return "";
    }

    public static InputStream readFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        InputStream is = null;

        try {
            if (isFileIsExist(filePath)) {
                File f = new File(filePath);
                is = new FileInputStream(f);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
        return is;
    }

    public static String readFileToString(String filePath) {
        return inputStream2String(readFile(filePath));
    }

    private static  String inputStream2String(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        try {
            while ((line = in.readLine()) != null){
                buffer.append(line);
            }
        } catch (IOException e) {
        }
        return buffer.toString();
    }

    public static String getRandomImageFileName() {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".jpg";
    }

    public static String getSimpleFolderSize(String path) {
        double size = getSize(new File(path));
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String sizeStr = "0 KB";
        if (size > 1024 * 1024) {
            sizeStr = decimalFormat.format(size / 1024 / 1024) + " MB";
        } else if (size > 1024) {
            sizeStr = decimalFormat.format(size / 1024) + " KB";
        }
        return sizeStr;
    }

    public static String getSimpleFolderSize(String path1,String path2) {
        double size1 = getSize(new File(path1));
        double size2 = getSize(new File(path2));
        double size = size1+size2;

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String sizeStr = "0 KB";
        if (size > 1024 * 1024) {
            sizeStr = decimalFormat.format(size / 1024 / 1024) + " MB";
        } else if (size > 1024) {
            sizeStr = decimalFormat.format(size / 1024) + " KB";
        }
        return sizeStr;
    }

    private static double getSize(File file) {
        double ss=0;
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            if(files!=null){
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        list.add(file2);
                    } else {
                        ss+=file2.length();
                    }
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                if(files!=null) {
                    for (File file2 : files) {
                        if (file2.isDirectory()) {
                            list.add(file2);
                        } else {
                            ss += file2.length();
                        }
                    }
                }
            }
        }
        return ss;
    }

    public static void saveString(String data, String path) {
        File file=new File(path);
        if(file.exists()){
            file.delete();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(path);
            fileWriter.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getSongPathByID(String songId){
        return FileUtil.CACHE_MUSIC_DOWNLOAD+File.separator+songId+FileUtil.MUSIC_FILE_SUFFIX;
    }
}
