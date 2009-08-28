//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

public class LinkStyleSheet extends StaticTextStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	public static LinkStyleSheet defaultStyleSheet = new LinkStyleSheet();
	
	
	public LinkStyleSheet()
	{
		super( defaultFont, Color.blue, false );
	}
	
	public LinkStyleSheet(Font font, Paint textPaint)
	{
		super( font, textPaint, false );
	}
	
	public LinkStyleSheet(Font font, Paint textPaint, boolean bMixedSizeCaps)
	{
		super( font, textPaint, bMixedSizeCaps );
	}
}