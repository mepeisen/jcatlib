/*
    This file is part of "xWorlds utilities".

    "xWorlds utilities" is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    "xWorlds utilities" is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with "nukkit xWorlds plugin". If not, see <http://www.gnu.org/licenses/>.

 */
package eu.xworlds.jcatlib.crypt;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * The cookie support.
 * 
 * <p>Migration from C++ library "libcat" CookieJar.cpp from Christopher A. Taylor.</p>
 * 
 * @see "https://code.google.com/archive/p/libcatid/"
 * 
 * @author mepeisen
 */
public class Cookie
{
    
    /** */
    private static final int EXPIRE_TIME = 4000; // ms
    /** */
    private static final int BIN_COUNT = 16; // power of 2
    /** */
    private static final int BIN_TIME = EXPIRE_TIME / BIN_COUNT;
    /** */
    private static final int BIN_MASK = BIN_COUNT - 1;
    
    /** the random key */
    private static final byte[] KEY = new byte[4 * 16];
    
    static
    {
        new Random().nextBytes(KEY);
    }
    
    /**
     * Hidden constructor
     */
    private Cookie()
    {
        // empty
    }
    
    /**
     * Generates a cookie from internet address
     * @param address the internet address
     * @return cookie as unsigned int
     */
    public static long Generate(InetAddress address)
    {
        final long epoch = GetEpoch();
        return (Hash(address.getAddress(), epoch) << 4) | (epoch & BIN_MASK);
    }
    
    /**
     * Returns the epoch
     * @return epoch (millis since startup)
     */
    private static long GetEpoch()
    {
        return (ManagementFactory.getRuntimeMXBean().getUptime() & 0xFFFFFFFFl) / BIN_TIME;
    }
    
    /**
     * Hash func
     * @param address the address
     * @param epoch the epoch
     * @return cookie
     */
    private static long Hash(byte[] address, long epoch)
    {
//      u32 x[16];
        final ByteBuffer buf = ByteBuffer.allocate(KEY.length);
        
//      memcpy(x, key, sizeof(x));
        buf.put(KEY);
        
//      const u32 *info32 = (const u32 *)address_info;
//      u32 *y = &x[4];
//      while (bytes >= 4)
//      {
//          *y++ += *info32++;
//          bytes -= 4;
//      }
  //
//      // Add final 1..3 address info bytes to the key
//      const u8 *info8 = (const u8 *)info32;
//      switch (bytes)
//      {
//      case 3: *y += ((u32)info8[2] << 16) | *(const u16*)info8; break;
//      case 2: *y += *(const u16*)info8; break;
//      case 1: *y += info8[0]; break;
//      }
        buf.position(4*4);
        buf.put(address);
        
//      x[6] += epoch;
        buf.position(6*4);
        long num = buf.getInt() & 0xFFFFFFFFl;
        num += epoch;
        buf.position(6*4);
        buf.putInt((int) (num & 0xFFFFFFFFl));

//      x[10] += epoch;
        buf.position(10*4);
        num = buf.getInt() & 0xFFFFFFFFl;
        num += epoch;
        buf.position(10*4);
        buf.putInt((int) (num & 0xFFFFFFFFl));
        
        return Salsa6(buf);
    }
    
    /**
     * Helper method for Salsa6 
     * @param buf  byte buf
     * @param targetPos target pos
     * @param src1Pos src pos
     * @param src2Pos src pos
     * @param rolDistance number of shifts
     */
    private static void Salsa6Helper(ByteBuffer buf, int targetPos, int src1Pos, int src2Pos, int rolDistance)
    {
        final long src1 = buf.getInt(src1Pos * 4) & 0xFFFFFFFFl;
        final long src2 = buf.getInt(src2Pos * 4) & 0xFFFFFFFFl;
        final int src = (int) ((src1 + src2) & 0xFFFFFFFFl);
        final int rolval = Integer.rotateLeft(src, rolDistance);
        final int target = buf.getInt(targetPos * 4);
        buf.putInt(targetPos * 4, target ^ rolval);
    }
    
    /**
     * Try to reconstruct the epoch from cookie
     * @param cookie cookie
     * @return the epoch
     */
    private static long ReconstructEpoch(long cookie)
    {
//        u32 epoch = GetEpoch();
        long epoch = GetEpoch();
        
//        u32 cookie_bin = cookie & BIN_MASK;
        long cookie_bin = cookie & BIN_MASK;

//        u32 cookie_epoch = (epoch & ~BIN_MASK) | cookie_bin;
        long cookie_epoch = (epoch & ~BIN_MASK) | cookie_bin;

//        if (cookie_bin > (epoch & BIN_MASK)) cookie_epoch -= BIN_COUNT;
        if (cookie_bin > (epoch & BIN_MASK)) cookie_epoch -= BIN_COUNT;

        return cookie_epoch;
    }
    
    /**
     * Verify cookie
     * @param address the address
     * @param cookie the cookie
     * @return true on success
     */
    public static boolean Verify(InetAddress address, long cookie)
    {
        return (Hash(address.getAddress(), ReconstructEpoch(cookie)) << 4) == (cookie & ~BIN_MASK);
    }
    
    /**
     * Perform Salsa6 roles
     * @param buf buffer
     * @return Cookie
     */
    private static long Salsa6(ByteBuffer buf)
    {
        for (int ii = 6; ii > 0; ii -= 2)
        {
//            x[4] ^= CAT_ROL32(x[0] + x[12], 7);
            Salsa6Helper(buf, 4, 0, 12, 7);
//            x[8] ^= CAT_ROL32(x[4] + x[0], 9);
            Salsa6Helper(buf, 8, 4, 0, 9);
//            x[12] ^= CAT_ROL32(x[8] + x[4], 13);
            Salsa6Helper(buf, 12, 8, 4, 13);
//            x[0] ^= CAT_ROL32(x[12] + x[8], 18);
            Salsa6Helper(buf, 0, 12, 8, 18);
//            x[9] ^= CAT_ROL32(x[5] + x[1], 7);
            Salsa6Helper(buf, 9, 5, 1, 7);
//            x[13] ^= CAT_ROL32(x[9] + x[5], 9);
            Salsa6Helper(buf, 13, 9, 5, 9);
//            x[1] ^= CAT_ROL32(x[13] + x[9], 13);
            Salsa6Helper(buf, 1, 13, 9, 13);
//            x[5] ^= CAT_ROL32(x[1] + x[13], 18);
            Salsa6Helper(buf, 5, 1, 13, 18);
//            x[14] ^= CAT_ROL32(x[10] + x[6], 7);
            Salsa6Helper(buf, 14, 10, 6, 7);
//            x[2] ^= CAT_ROL32(x[14] + x[10], 9);
            Salsa6Helper(buf, 2, 14, 10, 9);
//            x[6] ^= CAT_ROL32(x[2] + x[14], 13);
            Salsa6Helper(buf, 6, 2, 14, 13);
//            x[10] ^= CAT_ROL32(x[6] + x[2], 18);
            Salsa6Helper(buf, 10, 6, 2, 18);
//            x[3] ^= CAT_ROL32(x[15] + x[11], 7);
            Salsa6Helper(buf, 3, 15, 11, 7);
//            x[7] ^= CAT_ROL32(x[3] + x[15], 9);
            Salsa6Helper(buf, 7, 3, 15, 9);
//            x[11] ^= CAT_ROL32(x[7] + x[3], 13);
            Salsa6Helper(buf, 11, 7, 3, 13);
//            x[15] ^= CAT_ROL32(x[11] + x[7], 18);
            Salsa6Helper(buf, 15, 11, 7, 18);
//            x[1] ^= CAT_ROL32(x[0] + x[3], 7);
            Salsa6Helper(buf, 1, 0, 3, 7);
//            x[2] ^= CAT_ROL32(x[1] + x[0], 9);
            Salsa6Helper(buf, 2, 1, 0, 9);
//            x[3] ^= CAT_ROL32(x[2] + x[1], 13);
            Salsa6Helper(buf, 3, 2, 1, 13);
//            x[0] ^= CAT_ROL32(x[3] + x[2], 18);
            Salsa6Helper(buf, 0, 3, 2, 18);
//            x[6] ^= CAT_ROL32(x[5] + x[4], 7);
            Salsa6Helper(buf, 6, 5, 4, 7);
//            x[7] ^= CAT_ROL32(x[6] + x[5], 9);
            Salsa6Helper(buf, 7, 6, 5, 9);
//            x[4] ^= CAT_ROL32(x[7] + x[6], 13);
            Salsa6Helper(buf, 4, 7, 6, 13);
//            x[5] ^= CAT_ROL32(x[4] + x[7], 18);
            Salsa6Helper(buf, 5, 4, 7, 18);
//            x[11] ^= CAT_ROL32(x[10] + x[9], 7);
            Salsa6Helper(buf, 11, 10, 9, 7);
//            x[8] ^= CAT_ROL32(x[11] + x[10], 9);
            Salsa6Helper(buf, 8, 11, 10, 9);
//            x[9] ^= CAT_ROL32(x[8] + x[11], 13);
            Salsa6Helper(buf, 9, 8, 11, 13);
//            x[10] ^= CAT_ROL32(x[9] + x[8], 18);
            Salsa6Helper(buf, 10, 9, 8, 18);
//            x[12] ^= CAT_ROL32(x[15] + x[14], 7);
            Salsa6Helper(buf, 12, 15, 14, 7);
//            x[13] ^= CAT_ROL32(x[12] + x[15], 9);
            Salsa6Helper(buf, 13, 12, 15, 9);
//            x[14] ^= CAT_ROL32(x[13] + x[12], 13);
            Salsa6Helper(buf, 14, 13, 12, 13);
//            x[15] ^= CAT_ROL32(x[14] + x[13], 18);
            Salsa6Helper(buf, 15, 14, 13, 18);
        }
//
//        return x[0] ^ x[5] ^ x[10] ^ x[15];        
        
        return (buf.getInt(0 * 4) & 0xFFFFFFFFl) ^
                (buf.getInt(5 * 4) & 0xFFFFFFFFl) ^
                (buf.getInt(10 * 4) & 0xFFFFFFFFl) ^
                (buf.getInt(15 * 4) & 0xFFFFFFFFl);
    }
    
}
