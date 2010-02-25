//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import BritefuryJ.DocPresent.Painter.Painter;

public class ContentLeafEditableStyleParams extends ContentLeafStyleParams
{
	public static final ContentLeafEditableStyleParams defaultStyleParams = new ContentLeafEditableStyleParams( null, true );
	
	private final boolean bEditable;
	
	
	
	public ContentLeafEditableStyleParams(Painter background, boolean bEditable)
	{
		super( background );
		
		this.bEditable = bEditable;
	}
	
	
	public boolean isEditable()
	{
		return bEditable;
	}
}
