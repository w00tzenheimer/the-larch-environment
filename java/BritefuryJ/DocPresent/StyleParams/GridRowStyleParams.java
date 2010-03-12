//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.Painter;

public class GridRowStyleParams extends ContainerStyleParams
{
	public static final GridRowStyleParams defaultStyleParams = new GridRowStyleParams( null, null, null );


	public GridRowStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor)
	{
		super( background, hoverBackground, pointerCursor );
	}
}