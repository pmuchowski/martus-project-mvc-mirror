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
package org.martus.client.bulletinstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusConstants;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;

public class ExportEncryptedBulletinsInsideZip extends ExportEncryptedBulletins
{
	public ExportEncryptedBulletinsInsideZip(UiMainWindow mainWindowToUse, ProgressMeterInterface progressDlgToUse, File destinationZipFileToUse)
	{
		super(mainWindowToUse, progressDlgToUse);
		
		destinationZipFile = destinationZipFileToUse;
	}

	@Override
	protected void doPostExportWork()
	{
		zip();
	}

	public void zip()
	{
		FileOutputStream fileOutputStream = null;
		ZipOutputStream zipOutputStream = null;
		try
		{
			System.out.println("Destination file = " + destinationFolder.getAbsolutePath());
			fileOutputStream = new FileOutputStream(destinationZipFile);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
			System.out.println("Output to Zip : " + destinationZipFile);

			for (File exportedBulletinFile : getExportedBulletinFiles())
			{
				String name = exportedBulletinFile.getName();
				ZipEntry zipEntry = new ZipEntry(name);
				zipOutputStream.putNextEntry(zipEntry);
				
				FileInputStream inputStream = new FileInputStream(exportedBulletinFile);
				int got;
				byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
				while( (got = inputStream.read(bytes)) >= 0)
				{
					zipOutputStream.write(bytes, 0, got);
				}

				safleyCloseStream(inputStream);
				zipOutputStream.flush();
			}
			
			for (File exportedBulletinFile : getExportedBulletinFiles())
			{
				exportedBulletinFile.delete();
			}
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
		}
		finally
		{
			safleyCloseStream(zipOutputStream);
		}
	}

	private void safleyCloseStream(OutputStream outputStream)
	{
		try 
		{
			if (outputStream != null)
				outputStream.close();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	private void safleyCloseStream(InputStream inputStream)
	{
		try 
		{
			if (inputStream != null)
				inputStream.close();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private File destinationZipFile;
}
