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
package org.martus.client.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;

public class BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments extends AbstractBulletinXmlExporter
{
	private MiniFieldSpec[] fieldsToExport;

	public BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments(MartusApp appToUse, MiniLocalization localizationToUse,
																																		 ProgressMeterInterface progressMeterToUse, MiniFieldSpec[] fieldsToExport)
	{
		super(appToUse, localizationToUse, progressMeterToUse);
		this.fieldsToExport = fieldsToExport;
	}

	@Override
	public boolean shouldGroupAttachments()
	{
		return true;
	}

	@Override
	protected FieldSpec[] getTopFieldsToExport(Bulletin bulletin) throws Exception
	{
		return findFieldSpecsInFieldSpecCollection(bulletin.getTopSectionFieldSpecs());
	}

	@Override
	protected FieldSpec[] getBottomFieldsToExport(Bulletin bulletin) throws Exception
	{
		return findFieldSpecsInFieldSpecCollection(bulletin.getBottomSectionFieldSpecs());
	}

	private FieldSpec[] findFieldSpecsInFieldSpecCollection(FieldSpecCollection fieldSpecs) throws Exception
	{
		List<FieldSpec> specList = new ArrayList<>();
		Map<String, Vector<FieldSpec>> colsSpecMap = new HashMap<>();

		for (MiniFieldSpec spec : fieldsToExport)
		{
			String[] tagParts = spec.getTag().split("\\.");
			String tag = tagParts[0];
			FieldSpec fieldSpec = fieldSpecs.findBytag(tag);

			if (fieldSpec != null)
			{
				if (fieldSpec.getType().isGrid() && tagParts.length > 1)
				{
					GridFieldSpec gridSpec = (GridFieldSpec) fieldSpec;
					Vector<FieldSpec> colsSpecs = colsSpecMap.get(tag);

					if (colsSpecs == null)
					{
						colsSpecs = gridSpec.getColumns();
						colsSpecMap.put(tag, colsSpecs);
						gridSpec.setColumns(new Vector());
					}

					FieldSpec colSpec = findColumnSpecByLabel(colsSpecs, tagParts[1]);

					gridSpec.addColumn(colSpec);
					fieldSpec = gridSpec;
				}

				specList.add(fieldSpec);
			}
		}

		return specList.toArray(new FieldSpec[specList.size()]);
	}

	private FieldSpec findColumnSpecByLabel(Vector<FieldSpec> columns, String gridColumnLabel)
	{
		for (FieldSpec columnSpec : columns)
		{
			if (columnSpec.getLabel().equals(gridColumnLabel))
				return columnSpec;
		}

		return null;
	}
}
