//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;


public class AbstractBoxStyleParams extends ContainerStyleParams
{
	public static final AbstractBoxStyleParams defaultStyleParams = new AbstractBoxStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 0.0 );
	
	
	protected final double spacing;


	public AbstractBoxStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, double spacing)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.spacing = spacing;
	}


	public double getSpacing()
	{
		return spacing;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Spacing", Pres.coerceNonNull( spacing ) ) );
	}
}
