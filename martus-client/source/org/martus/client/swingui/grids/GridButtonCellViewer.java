package org.martus.client.swingui.grids;

import org.martus.client.swingui.fields.UIButtonViewer;
import org.martus.common.MiniLocalization;

public class GridButtonCellViewer extends GridCellEditorAndRenderer {

	GridButtonCellViewer(MiniLocalization localizationToUse) {
		super(new UIButtonViewer(localizationToUse));
	}

}
