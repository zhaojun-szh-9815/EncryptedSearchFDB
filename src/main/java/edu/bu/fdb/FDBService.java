package edu.bu.fdb;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FDBService {
    private static final FDB fdb = FDB.selectAPIVersion(520);
    private static final Logger logger = LoggerFactory.getLogger(FDBService.class);
    private static final int FILE_MAX_SIZE = 10*1024;
    private static final String STR_LENGTH = "LENGTH";
    private static final String DOWNLOAD_PATH = ".\\src\\test\\downloads\\";

    private static List<File> listFiles(File dirPath){
        List<File> Path = Arrays.asList(dirPath.listFiles());
        List<File> files = new ArrayList<>();
        for(File file : Path) {
            if(file.isFile()) {
                files.add(file);
            } else {
                files.addAll(listFiles(file));
            }
        }
        return files;
    }

    public static void DownloadAFile(String fileName) {
        byte[] buffer;
        int index = 0;
        try (Database db = fdb.open();
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(DOWNLOAD_PATH + fileName))) {
            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(fileName + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            int len = length.intValue();
            while (index < len) {
                String DBIndex = fileName + index;
                Object queryResult = db.run(transaction -> {
                    byte[] result = transaction.get(Tuple.from(DBIndex).pack()).join();
                    return Tuple.fromBytes(result).get(0);
                });
                buffer = (byte[]) queryResult;
                bos.write(buffer);
                index += 1;
            }
            logger.info("DownloadAFile: Success downloading " + fileName);
        } catch (Exception e) {
            logger.error("DownloadAFile: Facing error when downloading " + fileName);
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean UploadAll(String path) {
        File root = new File(path);
        List<File> files = listFiles(root);
        boolean flag = true;
        for (File file : files) {
            flag &= UploadAFile(file);
        }
        return flag;
    }

    public static boolean ClearAll(String path) {
        File root = new File(path);
        List<File> files = listFiles(root);
        boolean flag = true;
        for (File file : files) {
            flag &= ClearAFile(file);
        }
        return flag;
    }

    private static boolean ClearAFile(File file) {
        String fileName = file.getName();
        int index = 0;
        try (Database db = fdb.open()){
            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(fileName + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            db.run(transaction -> {
                transaction.clear(Tuple.from(fileName + STR_LENGTH).pack());
                return null;
            });
            int len = length.intValue();
            while (index <= len) {
                String DBIndex = fileName + index;
                db.run(transaction -> {
                    transaction.clear(Tuple.from(DBIndex).pack());
                    return null;
                });
                index += 1;
            }
            logger.info("ClearAFile: Success clear " + fileName);
            return true;
        } catch (Exception e) {
            logger.error("ClearAFile: Facing error when clear " + fileName);
            logger.error(e.getMessage());
            return false;
        }
    }

    private static boolean UploadAFile(File file) {
        byte[] buffer = new byte[FILE_MAX_SIZE];
        String fileName = file.getName();
        int index = 0;
        try (Database db = fdb.open();
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            int length;
            while ((length = bis.read(buffer)) != -1) {
                String DBIndex = fileName + index;
                db.run(transaction -> {
                    transaction.set(Tuple.from(DBIndex).pack(), Tuple.from(buffer).pack());
                    return null;
                });
                index += 1;
            }
            String DBLengthIndex = fileName + STR_LENGTH;
            int final_index = index;
            db.run(transaction -> {
                transaction.set(Tuple.from(DBLengthIndex).pack(), Tuple.from(final_index).pack());
                return null;
            });
            logger.info("UploadAFile: Success upload " + fileName);
            return true;
        } catch (Exception e) {
            logger.error("UploadAFile: Facing error when upload " + fileName);
            logger.error(e.getMessage());
            return false;
        }
    }

}
