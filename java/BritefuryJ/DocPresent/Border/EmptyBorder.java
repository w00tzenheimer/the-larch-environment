//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Border;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class EmptyBorder extends Border
{
	private double leftMargin, rightMargin, topMargin, bottomMargin, roundingX, roundingY;
	private Color backgroundColour;
	
	
	public EmptyBorder()
	{
		this( 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null );
	}
	
	public EmptyBorder(Color backgroundColour)
	{
		this( 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, backgroundColour );
	}
	
	public EmptyBorder(double roundingX, double roundingY, Color backgroundColour)
	{
		this( 0.0, 0.0, 0.0, 0.0, roundingX, roundingY, backgroundColour );
	}
	
	public EmptyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, 0.0, 0.0, null );
	}
	
	public EmptyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, double roundingX, double roundingY)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, roundingX, roundingY, null );
	}
	
	public EmptyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, Color backgroundColour)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, 0.0, 0.0, backgroundColour );
	}
	
	public EmptyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, double roundingX, double roundingY, Color backgroundColour)
	{
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.roundingX = roundingX;
		this.roundingY = roundingY;
		this.backgroundColour = backgroundColour;
	}
	
	

	public double getLeftMargin()
	{
		return leftMargin;
	}

	public double getRightMargin()
	{
		return rightMargin;
	}

	public double getTopMargin()
	{
		return topMargin;
	}

	public double getBottomMargin()
	{
		return bottomMargin;
	}

	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
		if ( backgroundColour != null )
		{
			if ( roundingX != 0.0  ||  roundingY != 0.0 )
			{
				graphics.setColor( backgroundColour );
				graphics.fill( new RoundRectangle2D.Double( x, y, w, h, roundingX, roundingY ) );
			}
			else
			{
				graphics.setColor( backgroundColour );
				graphics.fill( new Rectangle2D.Double( x, y, w, h ) );
			}
		}
	}
}