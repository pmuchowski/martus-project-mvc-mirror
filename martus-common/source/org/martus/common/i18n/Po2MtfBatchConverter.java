/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.common.i18n;

import java.io.File;
import java.io.IOException;

import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class Po2MtfBatchConverter extends Po2Mtf
{
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Must include the directory of input po files!");
			return;
		}
		
		String poFolderPath = args[0];
		File poFolder = new File(poFolderPath);
		if (!poFolder.exists())
		{
			System.out.println("Incorrect po folder path.  po folder does not exist:" + poFolderPath);
		}
		File[] poFiles = poFolder.listFiles();
		if (poFiles == null)
		{
			System.out.println("po folder has no files");
			return;
		}

		try 
		{
			batchConvert(poFiles);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void batchConvert(File[] poFiles) throws IOException
	{
		for (File poFile : poFiles)
		{
			String poFileName = poFile.getName();
			if (isNonPoFileToIgnore(poFileName))
				continue;
			
			System.out.println(poFileName);
			int indexOfLangaugeBreakChar = poFileName.indexOf("-");
			
			int indexOfFileExtensionBreakChar = poFileName.indexOf(".");
			String languageCode = poFileName.substring(indexOfLangaugeBreakChar + 1, indexOfFileExtensionBreakChar);
			System.out.println(poFileName + "Language Code = " + languageCode);
			UnicodeReader reader = new UnicodeReader(poFile);
			
			File outMtfFile = new File(poFile.getParentFile(), "Martus-" + languageCode + ".mtf");
			UnicodeWriter writer = new UnicodeWriter(outMtfFile);
			convertToMtf(reader, writer, languageCode);
		}
	}

	private static boolean isNonPoFileToIgnore(String poFileName)
	{
		return poFileName.equals(".DS_Store");
	}
}
