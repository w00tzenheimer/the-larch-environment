//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public class ElementPresentable implements Presentable
{
	private DPElement element;
	
	
	protected ElementPresentable(DPElement element)
	{
		this.element = element;
	}


	@Override
	public DPElement present(GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return element;
	}
}
