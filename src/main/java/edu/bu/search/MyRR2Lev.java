package edu.bu.search;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.bu.util.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyRR2Lev implements Serializable {

    private Map<String, Collection<byte[]>> dictionary = new HashMap<>();
    private byte[][] arr = new byte[10000][];
    private static final Logger logger = LoggerFactory.getLogger(MyRR2Lev.class);

    public MyRR2Lev() {}

    public MyRR2Lev(Map<String, Collection<byte[]>> dictionary, byte[][] arr) throws FileNotFoundException {
        this.dictionary = dictionary;
        this.arr = arr;
    }

    public Map<String, Collection<byte[]>> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, Collection<byte[]>> dictionary) {
        this.dictionary = dictionary;
    }

    public byte[][] getArr() {
        return arr;
    }

    public Multimap<String, byte[]> getMultiDictionary() {
        Multimap<String, byte[]> multimap = ArrayListMultimap.create();
        for (String key : dictionary.keySet()) {
            multimap.putAll(key, dictionary.get(key));
        }
        return multimap;
    }

    public void setMultiDictionary(Multimap<String, byte[]> dictionary) {
        this.dictionary = dictionary.asMap();
        try (OutputStream os = new FileOutputStream(".\\src\\tmp\\dict.txt");){
            for (String key : dictionary.keys()) {
                os.write(key.getBytes());
                for (byte[] bytes : dictionary.get(key)) {
                    os.write(bytes);
                }
                os.flush();
            }
        } catch (IOException e) {
            logger.error("write dictionary error!");
            e.printStackTrace();
        }
        /*int mapSize = 0;
        for (String key : dictionary.keys()) {
            mapSize += key.getBytes(StandardCharsets.UTF_8).length;
            mapSize += dictionary.get(key).size();
        }
        logger.info("MultiMap Key-Value Size = " + mapSize);
        Analysis.setKeyValueSize(mapSize);*/
        size(dictionary);
    }

    public static void size(Multimap<String, byte[]> map) {
        try{
            // System.out.println("Index Size: " + map.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.close();
            // System.out.println("Data Size: " + baos.size());
            logger.info("MultiMap Size = " + baos.size());
            Analysis.setMapSize(baos.size());
            baos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
