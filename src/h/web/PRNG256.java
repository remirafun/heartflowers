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

import jahive.tool.Tool;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author mirafun
 */
public class PRNG256 {
    private int i;
    private byte[] bytes;

    public PRNG256(String seed) {
        try {
            i = 0;
            bytes = Tool.sha256(seed.getBytes(StandardCharsets.UTF_8));
            if(bytes.length == 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public int nextByte() {
		var b = this.bytes[this.i++];
		if(this.i >= this.bytes.length) {
			this.i = 0;
            try {
                this.bytes = Tool.sha256(bytes);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		}
		return b&0xff;
	}
	public int nextInt() {
		return ((this.nextByte() << 24) | (this.nextByte() << 24) | (this.nextByte() << 24) | this.nextByte());
    }
	public int nextIntBits(int bits) {
		return (this.nextInt()&0x7FFFFFFF)>>(32-bits);
	}		
	public int nextIntB(int bound) {
        if(bound <= 0) throw new IllegalArgumentException();
        var r = this.nextIntBits(31);
        var m = bound - 1;
        if ((bound & m) == 0)
            r = ((bound * r) >> 31);
        else {
            for(var u = r; u - (r = u % bound) + m < 0; u = this.nextIntBits(31)) {}
        }
        return r;
    }
}
