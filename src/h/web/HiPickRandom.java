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

import java.util.ArrayList;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author mirafun
 */
public class HiPickRandom {
    /**
     * Create a json request to pick random string with a given weight
     * @param map 
     * @param uniqueName optional name or description
     * @param pickXTimes how many times to pick [default 1]
     * @param maxPerString how many times same string can be chosen [default 1]
     * @return  
     * 
     * @throws IllegalArgumentException map is empty or if weight is <= 0
     */
    public static String createJSON(Map<String, Integer> map, String uniqueName, int pickXTimes, int maxPerString) {
        if(map.isEmpty()) throw new IllegalArgumentException("map is empty");
        if(pickXTimes < 1) throw new IllegalArgumentException("at least 1 pick required");
        if(maxPerString < 1) throw new IllegalArgumentException("at least 1 pick per string requires");
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"name\":").append('\"').append(uniqueName).append("\",");
        sb.append("\"pick\":").append(pickXTimes).append(",");
        sb.append("\"max\":").append(maxPerString).append(",");
        sb.append("\"in\":");
        sb.append('[');
        for(Map.Entry<String, Integer> e : map.entrySet()) {
            sb.append('\"');
            sb.append(e.getKey());
            sb.append('\"');
            sb.append(',');
            sb.append(e.getValue());
            sb.append(',');
        }
        sb.setCharAt(sb.length()-1, ']');
        sb.append('}');
        return sb.toString();
    }
    /**
     * the transaction of the submitted json obtained from createJSON method
     * @param obj 
     * @return  
     * @throws java.lang.Exception 
     */
    public static ArrayList<String> doPick(JSONObject obj) throws Exception {
        var trx_id = obj.getString("trx_id");
        var block = obj.getLong("block");
        var in_block = obj.getLong("trx_in_block");
        var op = obj.getJSONArray("op");
        if(!op.getString(0).equals("custom_json")) throw new IllegalArgumentException("not a custom_json");
        var data = op.getJSONObject(1);
        if(!data.getString("id").equals("hipickrandom")) System.out.println("warning: custom_json not 'hipickrandom'");
        var json = new JSONObject(data.getString("json"));
        var arr = json.getJSONArray("in");
        var pick = json.getInt("pick");
        var mx = json.getInt("max");

        var str = trx_id+block+in_block;        
        var rng = new PRNG256(str);
        
        var m = new int[arr.length()>>1];
        for(var i = 0; i < m.length; i++) m[i] = mx;
        var totalMax = mx*m.length;
        var list = new ArrayList<String>();
        if(totalMax <= 0) return list;
        loop:
        for(var k = 0; k < pick; k++) {
            var totalW = 0;
            for(var i = 0; i < m.length; i++) {
                if(m[i] <= 0) continue;
                totalW += arr.getInt((i<<1)+1);
            }
            totalW = rng.nextIntB(totalW);
            var wSum = 0;
            for(var i = 0; i < m.length; i++) {
                if(m[i] <= 0) continue;
                var w = arr.getInt((i<<1)+1);
                wSum += w;
                if(totalW < wSum) {
                    m[i]--;
                    totalMax--;
                    list.add(arr.getString(i<<1));
                    if(totalMax <= 0) return list;
                    continue loop;
                }
            }
            System.out.println("error: fix rng");
            m[m.length-1]--;
            list.add(arr.getString((m.length-1)<<1));  
        }
        return list;
    }
}
