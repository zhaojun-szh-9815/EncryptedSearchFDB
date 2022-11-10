package edu.bu.search;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.crypto.sse.IEX2Lev;
import org.crypto.sse.RR2Lev;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * add, clear, download, update (index dictionary)
 */
public class ClusionRR2Lev {
    private static final FDB fdb = FDB.selectAPIVersion(520);
    private RR2Lev rr2Lev;
    private static final String PASS = "123";
    private static List<byte[]> listSK;
    private static final String RR2LEV_DICT_KEY = "twoLev_dict";
    private static final String STR_LENGTH = "_length";
    private static ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ClusionRR2Lev.class);

    static {
        try {
            listSK = IEX2Lev.keyGen(256, PASS, "salt/salt", 100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateRR2Lev(String path) throws Exception {

        ArrayList<File> listOfFile = new ArrayList<File>();
        TextProc.listf(path, listOfFile);

        TextProc.TextProc(false, path);

        int bigBlock = 1000;
        int smallBlock = 100;
        int dataSize = 10000;

        logger.info("\nBeginning of Encrypted Multi-map creation \n");
        logger.info("Number of keywords "+ TextExtractPar.lp1.keySet().size());
        logger.info("Number of pairs "+	TextExtractPar.lp1.keys().size());

        long startTime = System.nanoTime();
        rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        long endTime = System.nanoTime();

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

    }

    public long computeFilesSize(File path) {
        List<File> files = Arrays.asList(path.listFiles());
        long filesSize = 0;
        for(File file : files) {
            if(file.isFile()) {
                filesSize += file.length();
            } else {
                filesSize += computeFilesSize(file);
            }
        }
        return filesSize;
    }

    public List<String> queryToken(String keyword) throws Exception{
        if (rr2Lev == null) {
            logger.error("RR2Lev is null, download or generate it firstly.");
            return null;
        }

        byte[][] token = RR2Lev.token(listSK.get(0), keyword);
        List<String> res = rr2Lev.query(token, rr2Lev.getDictionary(), rr2Lev.getArray());
        return res;
    }

    public boolean uploadRR2Lev() throws Exception {
        if (rr2Lev == null) {
            logger.error("RR2Lev is null, generate it firstly.");
            return false;
        }

        MyRR2Lev myRR2Lev = new MyRR2Lev();
        myRR2Lev.setMultiDictionary(rr2Lev.getDictionary());

        String dict = mapper.writeValueAsString(myRR2Lev.getDictionary());
        List<String> strList = splitEqually(dict, 10000);

        logger.info("upload: dict length: " + dict.length());

        try (Database db = fdb.open()) {
            int length = strList.size();
            int index = 0;
            while (index < length) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                int curInx = index;
                db.run(transaction -> {
                    transaction.set(Tuple.from(DBIndex).pack(), Tuple.from(strList.get(curInx)).pack());
                    return null;
                });
                logger.debug("upload rr2lev: " + DBIndex);
                index += 1;
            }
            String DBLengthIndex = RR2LEV_DICT_KEY + STR_LENGTH;
            db.run(transaction -> {
                transaction.set(Tuple.from(DBLengthIndex).pack(), Tuple.from(length).pack());
                return null;
            });
            logger.info("upload: rr2lev length: " + length);

//            String arrStr = new String(myRR2Lev.get1DArr(), StandardCharsets.US_ASCII);
//            db.run(transaction -> {
//                transaction.set(Tuple.from(RR2LEV_ARR_KEY).pack(), Tuple.from(arrStr).pack());
//                return null;
//            });

            logger.info("Upload RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Upload RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void updateRR2Lev(String path) throws Exception {
        ArrayList<File> listOfFile = new ArrayList<File>();
        TextProc.listf(path, listOfFile);

        TextProc.TextProc(false, path);

        int bigBlock = 1000;
        int smallBlock = 100;
        int dataSize = 10000;

        logger.info("\nBeginning of Encrypted Multi-map creation \n");
        logger.info("Number of keywords "+ TextExtractPar.lp1.keySet().size());
        logger.info("Number of pairs "+	TextExtractPar.lp1.keys().size());

        long startTime = System.nanoTime();
        this.rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        long endTime = System.nanoTime();

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

    }

    public boolean clearRR2Lev() {
        try (Database db = fdb.open()){
            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            db.run(transaction -> {
                transaction.clear(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack());
                return null;
            });
            int len = length.intValue();
            int index = 0;
            while (index < len) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                db.run(transaction -> {
                    transaction.clear(Tuple.from(DBIndex).pack());
                    return null;
                });
                index += 1;
            }

//            db.run(transaction -> {
//                transaction.clear(Tuple.from(RR2LEV_ARR_KEY).pack());
//                return null;
//            });
            logger.info("Clear RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Clear RR2Lev: Fail");
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean downloadRR2Lev() {
        try (Database db = fdb.open()) {

            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            int len = length.intValue();
            String[] dictArr = new String[len];
            int index = 0;
            while (index < len) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                Object queryResult = db.run(transaction -> {
                    byte[] result = transaction.get(Tuple.from(DBIndex).pack()).join();
                    return Tuple.fromBytes(result).get(0);
                });
                String subStr = (String) queryResult;
                dictArr[index] = subStr;
                index += 1;
            }

            logger.info("dictArr: length = " + dictArr.length + ", first element length = " + dictArr[0].length());

            String str = String.join("", dictArr);

            logger.info("joined str length = " + str.length());

            TypeReference<Map<String, Collection<byte[]>>> tr_dict = new TypeReference<Map<String, Collection<byte[]>>>() {};
            Map<String, Collection<byte[]>> dictionary = mapper.readValue(str,tr_dict);

//            String queryResult_arr = db.run(transaction -> {
//                byte[] result = transaction.get(Tuple.from(RR2LEV_ARR_KEY).pack()).join();
//                return Tuple.fromBytes(result).getString(0);
//            });
//
//            logger.info("download: arr query result is null? " + String.valueOf(queryResult_arr == null));
//
//            byte[] bytes = queryResult_arr.getBytes();
//
//            logger.info("bytes length = " + bytes.length);

            byte[][] bytes = new byte[10000][];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = null;
            }
            MyRR2Lev myRR2Lev = new MyRR2Lev();
            myRR2Lev.setDictionary(dictionary);

            rr2Lev = new RR2Lev(myRR2Lev.getMultiDictionary(), myRR2Lev.getArr());
            logger.info("Download RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Download RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    /*public boolean uploadRR2Lev() throws JsonProcessingException {
        if (rr2Lev == null) {
            logger.error("RR2Lev is null, generate it firstly.");
            return false;
        }

        MyRR2Lev myRR2Lev = new MyRR2Lev();
        myRR2Lev.setMultiDictionary(rr2Lev.getDictionary());
        myRR2Lev.setArr(rr2Lev.getArray());

        String dict = mapper.writeValueAsString(myRR2Lev.getDictionary());
        String[] splitDict = dict.split("(?<=\\\\G.{5000})");

        try (Database db = fdb.open()) {
            db.run(transaction -> {
                transaction.set(Tuple.from(RR2LEV_DICT_KEY).pack(), Tuple.from(dict).pack());
                transaction.set(Tuple.from(RR2LEV_ARR_KEY).pack(), Tuple.from(myRR2Lev.getArr()).pack());
                return null;
            });
            logger.info("Upload RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Upload RR2Lev: Fail");
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean clearRR2Lev() {
        try (Database db = fdb.open()){
            db.run(transaction -> {
                transaction.clear(Tuple.from(RR2LEV_DICT_KEY).pack());
                transaction.clear(Tuple.from(RR2LEV_ARR_KEY).pack());
                return null;
            });
            logger.info("Clear RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Clear RR2Lev: Fail");
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean downloadRR2Lev() {
        byte[] bytes;
        try (Database db = fdb.open()) {
            Object queryResult_dict = db.run(transaction -> {
                byte[] result = transaction.get(Tuple.from(RR2LEV_DICT_KEY).pack()).join();
                return Tuple.fromBytes(result).get(0);
            });
            String str = (String) queryResult_dict;
            TypeReference<Map<String, Collection<byte[]>>> tr_dict = new TypeReference<Map<String, Collection<byte[]>>>() {};
            Map<String, Collection<byte[]>> dictionary = mapper.readValue(str,tr_dict);

            Object queryResult_arr = db.run(transaction -> {
                byte[] result = transaction.get(Tuple.from(RR2LEV_ARR_KEY).pack()).join();
                return Tuple.fromBytes(result).get(0);
            });
            bytes = (byte[]) queryResult_arr;
            TypeReference<byte[][]> tr_arr = new TypeReference<byte[][]>() {};
            byte[][] arr = mapper.readValue(bytes, tr_arr);

            MyRR2Lev myRR2Lev = new MyRR2Lev();
            myRR2Lev.setDictionary(dictionary);
            myRR2Lev.setArr(arr);

            rr2Lev.setDictionary(myRR2Lev.getMultiDictionary());
            rr2Lev.setArray(myRR2Lev.getArr());
            logger.info("Download RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Download RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }*/

}
