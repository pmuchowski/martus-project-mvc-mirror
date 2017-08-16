/*

Martus(TM) is a trademark of Beneficent Technology, Inc.
This software is (c) Copyright 2001-2017, Beneficent Technology, Inc.

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.common.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;

import org.martus.util.StreamableBase64;

public class AbstractMockMartusSecurity extends MartusSecurity
{
	public AbstractMockMartusSecurity() throws Exception
	{
	}

	public void speedWarning(String message)
	{
		//System.out.println("MockMartusSecurity.speedWarning: " + message);
	}

	public void readKeyPair(InputStream inputStream, char[] passPhrase) throws
			IOException,
			InvalidKeyPairFileVersionException,
			AuthorizationFailedException
	{
		if(fakeKeyPairVersionFailure)
			throw new InvalidKeyPairFileVersionException();

		if(fakeAuthorizationFailure)
			throw new AuthorizationFailedException();

		super.readKeyPair(inputStream, passPhrase);
	}

	public void createKeyPairForOtherClient() throws Exception
	{
//		createKeyPair();
//		System.out.println(Base64.encode(getKeyPairData(getKeyPair())));
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleOtherClientKeyPair));
	}

	public void createKeyPairForHQ() throws Exception
	{
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleHQKeyPair));
	}

	public void createKeyPairForServer() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPairForOtherServer() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPairForAmplifier() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPair()
	{
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPair(int publicKeyBits)
	{
		speedWarning("Calling MockMartusSecurity.createKeyPair " + publicKeyBits);
		super.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	KeyPair createSunKeyPair(int bitsInKey) throws Exception
	{
		int smallKeySizeForTesting = 1024;
		return super.createSunKeyPair(smallKeySizeForTesting);
	}

	public void loadSampleAccount() throws Exception
	{
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleKeyPair));
	}

	static final int SMALLEST_LEGAL_KEY_FOR_TESTING = 1024;

	public boolean shouldFakeSignatureVerifyFailure()
	{
		return fakeSigVerifyFailure;
	}

	public void endableFakeSigVerifyFailure()
	{
		this.fakeSigVerifyFailure = true;
	}

	public void disableFakeSigVerifyFailure()
	{
		this.fakeSigVerifyFailure = false;
	}

	private boolean fakeSigVerifyFailure;
	public boolean fakeAuthorizationFailure;
	public boolean fakeKeyPairVersionFailure;

	private final static String nonEncryptedSampleKeyPair =
			"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" +
			"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
			"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
			"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
			"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
			"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
			"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
			"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
			"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
			"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
			"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
			"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
			"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
			"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
			"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
			"hwAAAAgIbZPktljeCh3opk2hs84uU3zZK9Dd/Yu9pSU4nC6Y5BMN158f0KXBqd" +
			"/LhLa2xWaPAFwl0YPsfIEWdleKAIhKQsg0iE6oAgvPzgxquTiQ3/MDCppoP+4s" +
			"lXe4DjyOvmEZbJ0D7BgprZfrydQQr4KgdGEhNqu0Sq6c+3NQ1IqiP5eHNyABNq" +
			"YXZhLnV0aWwuSGFzaHRhYmxlE7sPJSFK5LgDAAJGAApsb2FkRmFjdG9ySQAJdG" +
			"hyZXNob2xkeHA/QAAAAAAACHcIAAAAAwAAAAB4c3IAEGphdmEudXRpbC5WZWN0" +
			"b3LZl31bgDuvAQIAA0kAEWNhcGFjaXR5SW5jcmVtZW50SQAMZWxlbWVudENvdW" +
			"50WwALZWxlbWVudERhdGF0ABNbTGphdmEvbGFuZy9PYmplY3Q7eHAAAAAAAAAA" +
			"AHVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAApwcHBwcH" +
			"BwcHBwc3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAACAR2PzzZAd" +
			"72TBHBdGSqfDakq4III0hZDb7A13hSr0HiKDSBNh/m7ld4DRFkYLsdNku05X1u" +
			"630y2u3GLlgeZkViRcwjzhAHO7lMQhnlmom3dykfNMv0cB4z1BRc3VLC+74oCa" +
			"baQyTpDqnYIwiXp7w3Y4U7p1tugfWG+wSiTuvEV4c3EAfgAK//////////////" +
			"/+/////gAAAAF1cQB+AA4AAABAa0tFUKBgvpZouwpHA3N134myWvAZyFtE6xOo" +
			"vPz7I8rd/GsJPH/8aO2S1s/MmHuehKuNbf/aYV2ft5/bhCKjEHhzcQB+AAr///" +
			"////////////7////+AAAAAXVxAH4ADgAAAECTHJOB858hABjyTaTOXO9vA3lQ" +
			"3HsemzhYNr+H4KpR5vPWUD5SbHCCsKRCda8foH3qKmE1d7bN+8QI5OjzG67ZeH" +
			"NxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAQHTskqjRMy6fRqba" +
			"jbjSr4zCBI0osNI+lTmDIEK0EZhiKnsafuzP0EaOHe1jbx5uvtbYiCdMYqdcWK" +
			"xLckYoJDl4c3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAABA0Gh7" +
			"osPMGWrOAe3+zwOoh++Wh+MDwLE6fPg6AH5GnrHZb5xYShmfY8+TXia4F3iyYR" +
			"FfC77to89Vt0RKAxHiX3hzcQB+AAr///////////////7////+AAAAAXVxAH4A" +
			"DgAAAEClpHpvKF3XYaQXCvNwf84HaDEdTvp/Lf4RecMJKcOX4GbZEDPPe7xj8/" +
			"+694gVx45bCBY3rDZtGChJauHjY4ineHNxAH4ACv///////////////v////4A" +
			"AAABdXEAfgAOAAAAARF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZG" +
			"VyLkpDRVJTQVB1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EAfgAFTAAO" +
			"cHVibGljRXhwb25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";

	private final static String nonEncryptedSampleOtherClientKeyPair =
			"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" +
			"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
			"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
			"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
			"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
			"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
			"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
			"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
			"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
			"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
			"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
			"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
			"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
			"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
			"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
			"hwAAAAgQDazR7FwQl6StKF5qmfSCFVh+QSjzgv0TU5XG5sbnDFfh12pj58ezW/" +
			"UINPQqbwWDgxME5Oag82XrJzUXHB+R7icwK+VgC8JX0M1NpzGws2+FXLJg89gU" +
			"YX/Afiey+Y09Y+TzoXo/UkxMYfLJ875TAV3Hu6QZiYHn9OyEF/nlnEWXhzcgAT" +
			"amF2YS51dGlsLkhhc2h0YWJsZRO7DyUhSuS4AwACRgAKbG9hZEZhY3RvckkACX" +
			"RocmVzaG9sZHhwP0AAAAAAAAh3CAAAAAAAAAAAeHNyABBqYXZhLnV0aWwuVmVj" +
			"dG9y2Zd9W4A7rwECAANJABFjYXBhY2l0eUluY3JlbWVudEkADGVsZW1lbnRDb3" +
			"VudFsAC2VsZW1lbnREYXRhdAATW0xqYXZhL2xhbmcvT2JqZWN0O3hwAAAAAAAA" +
			"AAB1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAKcHBwcH" +
			"BwcHBwcHNxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAgDO2INy6" +
			"B8RoZKMS5enoQUCWsleUk8HxXvPknZBw9aQnb004czI2aHiDzdTwusW5A+bUNN" +
			"2hgWDnNqO8jvc6KosIcwGWHavYfx/U1QPFDYZ+t3jV4vXS/Bg82j0HDParQ7ZR" +
			"Vx7oBDmOykH1MJaP3dE6R+rVELU2Zwne+dPaWiQReHNxAH4ACv////////////" +
			"///v////4AAAABdXEAfgAOAAAAQDElxW4UxHWaYXRlWKt9iGwZJHr9CMzLpeIz" +
			"ji1Wi70NosbCh2eavxiAm4KrqCI/MrhxFRwecy5+rHIfcVP7k8t4c3EAfgAK//" +
			"/////////////+/////gAAAAF1cQB+AA4AAABAZtox9VzfShy7hipnTBUwOedm" +
			"hjwXQyKn3hrrMrDd9jrKAcqgTo1xt8wB9ZVCrlhY42F5z2Z9hdHam9CHeXSPTX" +
			"hzcQB+AAr///////////////7////+AAAAAXVxAH4ADgAAAEEAtehBhUMjHQnc" +
			"8QtSpiS6OuAPPNTTiPZiRF7UT84ccKs8vm40ouIPCeaQRbUXC9gry4YqXqrOlf" +
			"UBTmELerzI2XhzcQB+AAr///////////////7////+AAAAAXVxAH4ADgAAAEEA" +
			"+0zsWDbJ/+ee+9j/devdcR8948Fnw9pFvG+GbJfBsBXWy1n95Hj7oZGNz6R0vT" +
			"EijnBYRY9U73kqcHCTJmViVXhzcQB+AAr///////////////7////+AAAAAXVx" +
			"AH4ADgAAAEEA3uScLLUJ7XjdEHnH+BImxGIcL51KA6p5hxpzzYGuYNDSy+362u" +
			"tI1nP5DTUGtwFUMIX3z1HD+8RXDN4EP8sF9XhzcQB+AAr///////////////7/" +
			"///+AAAAAXVxAH4ADgAAAAMBAAF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLn" +
			"Byb3ZpZGVyLkpDRVJTQVB1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EA" +
			"fgAFTAAOcHVibGljRXhwb25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";

	private final static String nonEncryptedSampleHQKeyPair =
			"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" +
			"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
			"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
			"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
			"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
			"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
			"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
			"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
			"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
			"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
			"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
			"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
			"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
			"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
			"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
			"hwAAAAgQDAW38yWeY7ljp/CaHjeadTtlet+/twglo2GNRY2krQYwan+EimYhsF" +
			"F5Ld1PpxjcPln0CIW6XyegArcNRZKsgpkoo6/72kn5YONsAj5w7hv8GHmivchx" +
			"9tNOi1PonKYfmtosOxeO340l+WMbGSRcn1uEZpggZ3XW5vBdz/leDpAXhzcgAT" +
			"amF2YS51dGlsLkhhc2h0YWJsZRO7DyUhSuS4AwACRgAKbG9hZEZhY3RvckkACX" +
			"RocmVzaG9sZHhwP0AAAAAAAAh3CAAAAAAAAAAAeHNyABBqYXZhLnV0aWwuVmVj" +
			"dG9y2Zd9W4A7rwECAANJABFjYXBhY2l0eUluY3JlbWVudEkADGVsZW1lbnRDb3" +
			"VudFsAC2VsZW1lbnREYXRhdAATW0xqYXZhL2xhbmcvT2JqZWN0O3hwAAAAAAAA" +
			"AAB1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAKcHBwcH" +
			"BwcHBwcHNxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAgD0OTcdr" +
			"pZqQSqz8H7iWarg1josZCmxh08SMCQrS7yBJ7ljuhBDb3vu0yddDvOLCPsXp0I" +
			"/3lyrp8pfXTm+nFrUeaPuODzCa8ZC/3VjPs5d06N1/gDWsRUDcheiDxpzCiJpi" +
			"6VdM6x0fnAjpDMiQ0tc8CjEygW7369Vnow8IaRsJeHNxAH4ACv////////////" +
			"///v////4AAAABdXEAfgAOAAAAQApPAQ0Ag11eCtUhg8JARURl3xFm7LFhcW+/" +
			"7tjSi4rn6Nga03iU56dqDTdVq5v1PfGDWvBjDXJqwCZCstCq5+R4c3EAfgAK//" +
			"/////////////+/////gAAAAF1cQB+AA4AAABAWEdhPZDZnMkGRy3Np3lCByaN" +
			"L1Gtxh96hx/TWcAsmZ1oY+KhUIvzB3mNwZkW8Nbq85fAU9mcCFY4KkWPtMeutX" +
			"hzcQB+AAr///////////////7////+AAAAAXVxAH4ADgAAAEAQGd4GMFlc39MC" +
			"11o0jMhvz6uCDmJSTM5flIxrULMfiEuh6L397+as6kujIM3u8ieNJM7SeLa84V" +
			"EVkPBWovZreHNxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAQQDm" +
			"HTfJm5hxlA88DiQuGAElAgT86dWZdT574Tv9L1SM8Q15V1BpDVWV3fafr3G2tR" +
			"PXDfLhnaObQp3o9xSH3VWveHNxAH4ACv///////////////v////4AAAABdXEA" +
			"fgAOAAAAQQDV/vfaHtCp7qdpxc3X+MhtpzACLMWGkvVgj389eam6FxyDQ6RZz1" +
			"WMfAFR5SmSdfrgMaFe7lv5kLB1OoE/+ghPeHNxAH4ACv///////////////v//" +
			"//4AAAABdXEAfgAOAAAAAwEAAXhzcgAtb3JnLmJvdW5jeWNhc3RsZS5qY2UucH" +
			"JvdmlkZXIuSkNFUlNBUHVibGljS2V5JSJqDlv6bIQCAAJMAAdtb2R1bHVzcQB+" +
			"AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeHBxAH4ADXEAfgAj";

}
