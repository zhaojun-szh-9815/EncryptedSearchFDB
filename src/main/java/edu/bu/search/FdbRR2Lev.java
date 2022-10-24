package edu.bu.search;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.crypto.sse.RR2Lev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * used by FdbSearchService
 * Backup
 */
public class FdbRR2Lev implements Serializable {

    private List<Map<String, Collection<byte[]>>> mapList = new ArrayList<>();

    private List<RR2Lev> rr2LevList = new ArrayList<>();

    private byte[][] arr = new byte[10000][];

    public List<Map<String, Collection<byte[]>>> getMapList() {
        return mapList;
    }

    public void setMapList(List<Map<String, Collection<byte[]>>> mapList) {
        this.mapList = mapList;
    }

    public List<RR2Lev> getRr2LevList() {
        convert2RR2Lev();
        return rr2LevList;
    }

    public void addRR2LevList(RR2Lev rr2Lev){
        this.rr2LevList.add(rr2Lev);
        this.mapList.add(rr2Lev.dictionary.asMap());
    }

    public void convert2RR2Lev() {
        rr2LevList.clear();
        for (Map<String, Collection<byte[]>> map : mapList) {
            rr2LevList.add(new RR2Lev(getMultiDictionary(map), arr));
        }
    }

    private Multimap<String, byte[]> getMultiDictionary(Map<String, Collection<byte[]>> dictionary) {
        Multimap<String, byte[]> multimap = ArrayListMultimap.create();
        for (String key : dictionary.keySet()) {
            multimap.putAll(key, dictionary.get(key));
        }
        return multimap;
    }
}
