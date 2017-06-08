package org.martus.client.bulletinstore.converters;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class BulletinsAsXmlToCsvConverterWithoutMetadataFields extends BulletinsAsXmlToCsvConverter
{
	public BulletinsAsXmlToCsvConverterWithoutMetadataFields(InputSource inputSourceToUse, String anOutFileName)
	{
		super(inputSourceToUse, anOutFileName);
	}

	@Override
	protected void readMetaHeaders(Document doc)
	{
	}

	@Override
	protected void addMetaDataToOutput(Element bulletin)
	{
	}

	@Override
	protected void addCustomGridColumns(ArrayList<String> colList)
	{
	}

	@Override
	protected String addCustomGridColumnsData(String gridLocalID)
	{
		return "";
	}
}
