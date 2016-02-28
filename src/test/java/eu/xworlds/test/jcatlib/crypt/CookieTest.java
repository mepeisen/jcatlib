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
package eu.xworlds.test.jcatlib.crypt;

import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

import eu.xworlds.jcatlib.crypt.Cookie;

/**
 * Test case for Cookies support
 * 
 * @author mepeisen
 */
@RunWith(JUnit5.class)
public class CookieTest
{
 
    /**
     * Tests verification of a cookie
     * @throws UnknownHostException thrown on errors
     */
    @Test
    @DisplayName("cookie verification")
    void testCookieGeneration() throws UnknownHostException
    {
        final InetAddress address = InetAddress.getByAddress(new byte[]{127,0,0,1});
        
        final long cookie = Cookie.Generate(address);
        assertTrue(Cookie.Verify(address, cookie));
    }
 
    /**
     * Tests verification of a cookie fails after timeout
     * @throws Exception thrown on errors
     */
    @Test
    @DisplayName("cookie verification failes after timeout")
    void testCookieGenerationFailsTimeout() throws Exception
    {
        final InetAddress address = InetAddress.getByAddress(new byte[]{127,0,0,1});
        
        final long cookie = Cookie.Generate(address);
        Thread.sleep(5000);
        assertFalse(Cookie.Verify(address, cookie));
    }
    
}
