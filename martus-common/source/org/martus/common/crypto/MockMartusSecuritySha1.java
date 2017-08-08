/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

public class MockMartusSecuritySha1 extends AbstractMockMartusSecurity
{
	public static MockMartusSecuritySha1 createClient() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.loadSampleAccount();
		return security;
	}
	
	public static MockMartusSecuritySha1 createOtherClient() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.createKeyPairForOtherClient();
		return security;
	}
	
	public static MockMartusSecuritySha1 createHQ() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.createKeyPairForHQ();
		return security;
	}
	
	public static MockMartusSecuritySha1 createServer() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.createKeyPairForServer();
		return security;
	}
	
	public static MockMartusSecuritySha1 createOtherServer() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.createKeyPairForOtherServer();
		return security;
	}
	
	public static MockMartusSecuritySha1 createAmplifier() throws Exception
	{
		MockMartusSecuritySha1 security = new MockMartusSecuritySha1();
		security.createKeyPairForAmplifier();
		return security;
	}
	
	public MockMartusSecuritySha1() throws Exception
	{
	}

	public SignatureEngine createSignatureVerifier(String signedByPublicKey)
			throws Exception
	{
		if(fakeSigVerifyFailure)
			return null;

		return super.createSignatureVerifier(signedByPublicKey);
	}
}
