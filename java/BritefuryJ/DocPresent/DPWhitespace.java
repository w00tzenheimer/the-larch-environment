//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public class DPWhitespace extends DPContentLeafEditable
{
	protected double width;
	
	
	public DPWhitespace()
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, 0.0 );
	}
	
	public DPWhitespace(ContentLeafStyleSheet styleSheet)
	{
		this( styleSheet, 0.0 );
	}
	
	public DPWhitespace(double width)
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, width );
	}

	public DPWhitespace(ContentLeafStyleSheet styleSheet, double width)
	{
		super( styleSheet );
		this.width = width;
	}

	
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		if ( c.getMarker().getIndex() == 0 )
		{
			drawCaretAtStart( graphics );
		}
		else
		{
			drawCaretAtEnd( graphics );
		}
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		DPContentLeaf leaf = this;
		while ( leaf != null  &&  leaf.isWhitespace() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		if ( leaf != null )
		{
			leaf.drawCaretAtEnd( graphics );
		}
	}
	
	public void drawCaretAtEnd(Graphics2D graphics)
	{
		DPContentLeaf leaf = this;
		while ( leaf != null  &&  leaf.isWhitespace() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		if ( leaf != null )
		{
			leaf.drawCaretAtStart( graphics );
		}
	}

	
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return new HMetrics( width );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return new HMetrics( width );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return new VMetrics();
	}
	
	protected VMetrics computePreferredVMetrics()
	{
		return new VMetrics();
	}



	//
	// Marker methods
	//
	
	public int getMarkerPositonForPoint(Point2 localPos)
	{
		if ( localPos.x >= width * 0.5 )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	protected int getMarkerRange()
	{
		return 1;
	}

	
	public boolean isWhitespace()
	{
		return true;
	}
}
