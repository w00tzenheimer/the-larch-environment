//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;

public class ContentLeafStyleParams extends ElementStyleParams
{
	public static final ContentLeafStyleParams defaultStyleParams = new ContentLeafStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null );
	
	
	public ContentLeafStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
	}



	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
	}
}
