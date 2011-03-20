//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.awt.Stroke;

import BritefuryJ.DocPresent.TableBackgroundPainter;
import BritefuryJ.DocPresent.Painter.Painter;


public class TableStyleParams extends ContainerStyleParams
{
	public static final TableStyleParams defaultStyleParams = new TableStyleParams( null, null, null, 3.0, false, 3.0, false, null, new BasicStroke( 1.0f ), Color.BLACK );
	
	
	protected final double columnSpacing;
	protected final boolean columnExpand;

	protected final double rowSpacing;
	protected final boolean rowExpand;
	
	protected final Stroke cellBoundaryStroke;
	protected final Paint cellBoundaryPaint;
	
	protected final TableBackgroundPainter tableBackgroundPainter;


	public TableStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, double columnSpacing, boolean columnExpand, double rowSpacing, boolean rowExpand,
			TableBackgroundPainter tableBackgroundPainter, Stroke cellBoundaryStroke, Paint cellBoundaryPaint)
	{
		super( background, hoverBackground, pointerCursor );
		
		this.columnSpacing = columnSpacing;
		this.columnExpand = columnExpand;

		this.rowSpacing = rowSpacing;
		this.rowExpand = rowExpand;
		
		this.cellBoundaryStroke = cellBoundaryStroke;
		this.cellBoundaryPaint = cellBoundaryPaint;
		
		this.tableBackgroundPainter = tableBackgroundPainter;
	}


	public double getColumnSpacing()
	{
		return columnSpacing;
	}

	public boolean getColumnExpand()
	{
		return columnExpand;
	}


	public double getRowSpacing()
	{
		return rowSpacing;
	}

	public boolean getRowExpand()
	{
		return rowExpand;
	}


	public Stroke getCellBoundaryStroke()
	{
		return cellBoundaryStroke;
	}
	
	public Paint getCellBoundaryPaint()
	{
		return cellBoundaryPaint;
	}
	
	
	public TableBackgroundPainter getTableBackgroundPainter()
	{
		return tableBackgroundPainter;
	}
}
