/*
 * Copyright (c) 2021, mirafun
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the copyright holder nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package h.web;

import jahive.tool.B58;
import jahive.tool.Tool;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mirafun
 */
public class PickRandom {
    /**
     votes are obtained from condenser.get_active_votes at the of picking
     */
    public static RandomRequest pickRandom(String votes, String author, String permlink) {
        JSONObject obj =  new JSONObject(votes);
        var arr = obj.getJSONArray("result");
        return pickRandom(arr, author, permlink);
    }
    public static RandomRequest pickRandom(JSONArray arr, String author, String permlink) {
        var list = new ArrayList<Pair>();
        var numall = arr.length();
        var point = new ArrayList<String>();
        for(int i = 0; i < arr.length(); i++) {
            var o = arr.getJSONObject(i);
            
            var name = o.getString("voter");
            var pe = o.getInt("percent");
            var time = o.getString("time");
            var rs = o.getLong("rshares");
            
            if(pe >= 10000) {
                list.add(new Pair(name, time, rs));
            }
        }
        
        if(list.isEmpty()) return null;
        Collections.sort(list);
        return new RandomRequest(list, point, numall, author, permlink);
    }
    public static class RandomRequest {
        public ArrayList<Pair> list;
        public ArrayList<String> point;
        public int numall;   
        public String author, permlink;
        public int numpoint;
        public RandomRequest(ArrayList<Pair> list, ArrayList<String> point, int numall, String author, String permlink) {
            this.list = list;
            this.point = point;
            this.numall = numall;
            this.author = author;
            this.permlink = permlink;
        }

        public RandomRequest(JSONObject j) {
            list = new ArrayList<>();
            point = new ArrayList<>();
            var li = j.getJSONArray("list");
            for(int i = 0; i < li.length(); i++) {
                list.add(new Pair(li.getJSONObject(i)));
            }
            li = j.getJSONArray("point");
            for(int i = 0; i < li.length(); i++) {
                point.add(li.getString(i));
            }
            numall = j.getInt("numall");
            author = j.getString("author");
            permlink = j.getString("permlink");
            numpoint = j.getInt("numpoint");
        }
       
//        public TreeMap<String, Integer> pickRandom(String block) {
//            return pickRandom(new JSONObject(block).getJSONObject("result"));
//        }
        public TreeMap<String, Integer> pickRandom(JSONObject obj) {
            var time = obj.getString("timestamp");
            var wi = obj.getLong("trx_in_block");
            var root = obj.getString("trx_id");
            var seed = ((long)(time.hashCode())*(wi))^root.hashCode();
            var random = new Random(seed);
            long total = 0;
            for(var o : list) { o.rsFunc = (long)Math.sqrt(o.rs); total += o.rsFunc; }
//            System.out.println("-----");
//            for(var o : list) System.out.println(o);
//            System.out.println("-----");
//            
            var pick = new TreeMap<String, Integer>();
            
            var r1 = pickByRsFunc(random, total);
            var r2 = pickRandom(random);
            var r3 = pickByRsFunc(random, total);
            var r4 = pickRandom(random);
            var r5 = pickByRsFunc(random, total);
            var r6 = pickRandom(random);
            var r7 = random.nextBoolean()?pickByRsFunc(random, total):pickRandom(random);
            
            add(pick, r1, 1500);
            add(pick, r2, 1500);
            add(pick, r3, 500);
            add(pick, r4, 500);
            add(pick, r5, 500);
            add(pick, r6, 500);
            add(pick, r7, 500);
            
            return pick;
        }
        /*public Pair pickRandomWeekly(String block) {
            return pickRandomWeekly(new JSONObject(block).getJSONObject("result"));
        }*/
        public Pair pickRandomWeekly(JSONObject obj) {
            var time = obj.getString("timestamp");
            var wi = obj.getLong("trx_in_block");
            var root = obj.getString("trx_id");
            var seed = 13*((long)(time.hashCode())*(wi))^root.hashCode();
            var random = new Random(seed);
            long total = 0;
            for(var o : list) { o.rsFunc = (long)Math.sqrt(o.rs); total += o.rsFunc; }
            
            return random.nextBoolean()?pickByRsFunc(random, total):pickRandom(random);
        }
        public void add(Map<String, Integer> pick, Pair p, int amount) {
            pick.put(p.name, amount+pick.getOrDefault(p.name, 0));
        }
        public Pair pickByRsFunc(Random r, long total) {
            var p = r.nextDouble()*total;
            long t = 0;
            for(var o : list) {
                t += o.rsFunc;
                if(p < t) { return o; }
            }
            return list.get(list.size()-1);
        }
        public Pair pickRandom(Random r) {
            return list.get(r.nextInt(list.size()));
        } 
        
        public JSONObject toJSON() {
//            public ArrayList<Pair> list;
//           public ArrayList<String> point;
//           public int numall;   
//           public String author, permlink;
//           public int numpoint;
            var j = new JSONObject();
            var li = new JSONArray();
            for(int i = 0; i < list.size(); i++) {
                li.put(list.get(i).toJSON());
            }
            j.put("list", li);
            li = new JSONArray();
            for(int i = 0; i < point.size(); i++) {
                li.put(point.get(i));
            }
            j.put("point", li);
            j.put("numall", numall);
            j.put("author", author);
            j.put("permlink", permlink);
            j.put("numpoint", numpoint);
            return j;
        }
        
        public String createJSON() throws Exception {
            var j = new JSONObject();
            j.put("author", author);
            j.put("permlink", permlink);
            j.put("timeA", timeA());
            j.put("timeB", timeB());
            j.put("nameA", nameA());
            j.put("nameB", nameB());
            j.put("hash", list.hashCode());
            j.put("num", size());
            j.put("numall", numall);
            j.put("numpoint", point.size());
            j.put("hash", B58.enUTF8(Tool.sha256(toJSON().toString().getBytes(StandardCharsets.UTF_8))));
            List<String> pList = point;
            if(pList.size() > 0) {
                var estimateSize = 0;
                for(int i = 0; i < pList.size(); i++) {
                    estimateSize += pList.get(i).length();
                    if(estimateSize > 1000) {
                        //list is getting too big, trim it
                        //one can reconstruct it from blocks
                        pList = pList.subList(0, i);
                        break;
                    }
                }
                j.put("point", new JSONArray(pList));
            }
            return j.toString();
        }
        public int size() { return list.size(); }
        public Pair a() { return list.get(0); }
        public Pair b() { return list.get(list.size()-1); }
        public String nameA() { return a().name; }
        public String nameB() { return b().name; }
        public String timeA() { return a().time; }
        public String timeB() { return b().time; }
    }
    public static class Pair implements Comparable<Pair>{
        public String name, time;
        public long rs;
        public long rsFunc;
        public Pair(JSONObject j) {
            name = j.getString("name");
            time = j.getString("time");
            rs = j.getLong("rs");
        }
        public Pair(String name, String time, long rs) {
            this.name = name;
            this.time = time;
            if(rs < 0) rs = 0;
            this.rs = rs;
        }
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + Objects.hashCode(this.time);
            hash = 97 * hash + (int) (this.rs ^ (this.rs >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {  return true;  }
            if (obj == null) {  return false;  }
            if (getClass() != obj.getClass()) { return false;  }
            final Pair other = (Pair) obj;
            if (this.rs != other.rs) { return false; }
            if (!Objects.equals(this.name, other.name)) {return false;  }
            if (!Objects.equals(this.time, other.time)) {return false;  }
            return true;
        }

        @Override
        public int compareTo(Pair o) {
            return time.compareTo(o.time);
        }
        
        public JSONObject toJSON() {
            var j = new JSONObject();
            j.put("name", name);
            j.put("time", time);
            j.put("rs", rs);
            return j;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Pair{name=").append(name);
            sb.append(", time=").append(time);
            sb.append(", rs=").append(rs);
            sb.append('}');
            return sb.toString();
        }
        
    }
    
}
