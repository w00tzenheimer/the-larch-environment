//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;




public class TableLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		int i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan == 1 )
			{
				double totalPad = packing.paddingX * 2.0;
				LReqBox b = columnBoxes[packing.x];
				b.minWidth = Math.max( b.minWidth, child.minWidth + totalPad );
				b.prefWidth = Math.max( b.prefWidth, child.prefWidth + totalPad );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endColumn = packing.x + packing.colSpan;
				
				double minWidthAvailable = 0.0, prefWidthAvailable = 0.0;
				for (int c = packing.x; c < endColumn; c++)
				{
					LReqBox colBox = columnBoxes[c];
					
					double spacing = c != endColumn-1  ?  spacingX  :  0.0;
					
					minWidthAvailable += colBox.minWidth + spacing;
					prefWidthAvailable += colBox.prefWidth + spacing;
				}
				
				
				// Now compare with what is required
				if ( minWidthAvailable  <  child.minWidth  ||  prefWidthAvailable  <  child.prefWidth )
				{
					// Need more width; compute how much we need, and distribute among columns
					double colSpanRecip = 1.0 / (double)packing.colSpan;
					double additionalMinWidth = Math.max( child.minWidth - minWidthAvailable, 0.0 );
					double additionalMinWidthPerColumn = additionalMinWidth * colSpanRecip;
					double additionalPrefWidth = Math.max( child.prefWidth - prefWidthAvailable, 0.0 );
					double additionalPrefWidthPerColumn = additionalPrefWidth * colSpanRecip;
					
					for (int c = packing.x; c < endColumn; c++)
					{
						columnBoxes[c].minWidth += additionalMinWidthPerColumn;
						columnBoxes[c].prefWidth += additionalPrefWidthPerColumn;
					}
				}
			}
			
			i++;
		}

		return columnBoxes;
	}




	private static LReqBox[] computeRowYBoxes(LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingY)
	{
		LReqBox rowBoxes[] = new LReqBox[numRows];
		for (int i = 0; i < numRows; i++)
		{
			rowBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				double totalPad = packing.paddingY * 2.0;
				LReqBox b = rowBoxes[packing.y];
				b.setRequisitionY( Math.max( b.getReqHeight(), child.getReqHeight() + totalPad ),  0.0 );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					LReqBox rowBox = rowBoxes[r];
					
					double spacing = r != endRow-1  ?  spacingY  :  0.0;
					
					heightAvailable += rowBox.getReqHeight() + spacing;
				}
				
				
				// Now compare with what is required
				if ( heightAvailable  <  child.getReqHeight() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double additionalHeight = Math.max( child.getReqHeight() - heightAvailable, 0.0 );
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						LReqBox rowBox = rowBoxes[r];
						rowBox.setRequisitionY( rowBox.getReqHeight() + additionalHeightPerRow, 0.0 );
					}
				}
			}
			
			i++;
		}

		return rowBoxes;
	}





	private static LReqBox[] computeRowYBoxesWithBaselines(LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingY)
	{
		LReqBox rowBoxes[] = new LReqBox[numRows];
		for (int i = 0; i < numRows; i++)
		{
			rowBoxes[i] = new LReqBox();
			rowBoxes[i].setHasBaseline( true );
		}
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				LReqBox b = rowBoxes[packing.y];
				
				double ascent, descent;

				if ( child.hasBaseline() )
				{
					ascent = child.reqAscent + packing.paddingY;
					descent = child.reqDescent + packing.paddingY;
				}
				else
				{
					ascent = descent = child.getReqHeight() * 0.5 + packing.paddingY;
				}

				b.reqAscent = Math.max( b.reqAscent, ascent );
				b.reqDescent = Math.max( b.reqDescent, descent );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					LReqBox rowBox = rowBoxes[r];
					
					double spacing = r != endRow-1  ?  spacingY  :  0.0;
					
					heightAvailable += rowBox.getReqHeight() + spacing;
				}
				
				
				// Now compare with what is required
				if ( heightAvailable  <  child.getReqHeight() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double additionalHeight = Math.max( child.getReqHeight() - heightAvailable, 0.0 );
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						LReqBox rowBox = rowBoxes[r];
						rowBox.reqDescent += additionalHeightPerRow;
					}
				}
			}
			
			i++;
		}

		return rowBoxes;
	}
	
	
	
	public static LReqBox[] computeRequisitionX(LReqBox box, LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		LReqBox columnBoxes[] = computeColumnXBoxes( children, packingParams, numColumns, numRows, spacingX );
		
		double minWidth = 0.0, prefWidth = 0.0;
		for (LReqBox colBox: columnBoxes)
		{
			minWidth += colBox.minWidth;
			prefWidth += colBox.prefWidth;
		}
		double spacing = spacingX * (double)Math.max( columnBoxes.length - 1, 0 );
		minWidth += spacing;
		prefWidth += spacing;
		
		box.setRequisitionX( minWidth, prefWidth, 0.0, 0.0 );
		
		return columnBoxes;
	}



	public static LReqBox[] computeRequisitionY(LReqBox box, LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		LReqBox rowBoxes[];
		
		if ( rowAlignment == VAlignment.BASELINES )
		{
			rowBoxes = computeRowYBoxesWithBaselines( children, packingParams, numColumns, numRows, spacingX );
		}
		else
		{
			rowBoxes = computeRowYBoxes( children, packingParams, numColumns, numRows, spacingX );
		}
		
		double reqHeight = 0.0;
		for (LReqBox rowBox: rowBoxes)
		{
			reqHeight += rowBox.getReqHeight();
		}
		double spacing = spacingY * (double)Math.max( rowBoxes.length - 1, 0 );
		reqHeight += spacing;
		
		box.setRequisitionY( reqHeight, 0.0 );
		
		return rowBoxes;
	}
	
	
	
	
	public static void allocateX(LReqBox box, LReqBox columnBoxes[], LReqBox children[],
			LAllocBox allocBox, LAllocBox columnAllocBoxes[], LAllocBox childrenAlloc[], 
			TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		// Allocate space to the columns
		HorizontalLayout.allocateX( box, columnBoxes, allocBox, columnAllocBoxes, spacingX, bExpandX );
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];

			int startCol = packing.x;
			int endCol = packing.x + packing.colSpan;
			LAllocBox startColAlloc = columnAllocBoxes[startCol], endColAlloc = columnAllocBoxes[endCol-1];
			double xStart = startColAlloc.positionInParentSpaceX + packing.paddingX;
			double xEnd = endColAlloc.positionInParentSpaceX  +  endColAlloc.allocationX - packing.paddingX;
			double widthAvailable = xEnd - xStart;
			double cellWidth = Math.max( widthAvailable, child.minWidth );
			
			if ( cellWidth <= child.prefWidth )
			{
				allocBox.allocateChildX( childAlloc, xStart, cellWidth );
			}
			else
			{
				if ( colAlignment == HAlignment.LEFT )
				{
					allocBox.allocateChildX( childAlloc, xStart, child.prefWidth );
				}
				else if ( colAlignment == HAlignment.RIGHT )
				{
					allocBox.allocateChildX( childAlloc, Math.max( xEnd - child.prefWidth, 0.0 ), child.prefWidth );
				}
				else if ( colAlignment == HAlignment.CENTRE )
				{
					allocBox.allocateChildX( childAlloc, Math.max( xStart + ( widthAvailable - child.prefWidth ) * 0.5, 0.0 ), child.prefWidth );
				}
				else if ( colAlignment == HAlignment.EXPAND )
				{
					allocBox.allocateChildX( childAlloc, xStart, cellWidth );
				}
			}
		}
	}
	


	
	
	
	
	public static void allocateY(LReqBox box, LReqBox rowBoxes[], LReqBox children[],
			LAllocBox allocBox, LAllocBox rowAllocBoxes[], LAllocBox childrenAlloc[],
			TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		// Allocate space to the columns
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, spacingY, bExpandY );
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];

			if ( packing.rowSpan == 1  &&  rowAlignment == VAlignment.BASELINES  &&  child.hasBaseline()  &&  rowBoxes[packing.y].hasBaseline() )
			{
				LReqBox rowBox = rowBoxes[packing.y];
				LAllocBox rowAlloc = rowAllocBoxes[packing.y];

				double yOffset = rowBox.reqAscent - child.reqAscent;			// Row ascent includes padding; so yOffset also includes padding
				double yStart = rowAlloc.positionInParentSpaceY;
				double yPos = yStart + Math.max( yOffset, 0.0 );
				allocBox.allocateChildY( childAlloc, yPos, child.getReqHeight() );
			}
			else
			{
				int startRow = packing.y;
				int endRow = packing.y + packing.rowSpan;
				LAllocBox startRowAlloc = rowAllocBoxes[startRow], endRowAlloc = rowAllocBoxes[endRow-1];
				double yStart = startRowAlloc.positionInParentSpaceY + packing.paddingY;
				double yEnd = endRowAlloc.positionInParentSpaceY + endRowAlloc.getAllocationY() - packing.paddingY;
				double heightAvailable = yEnd - yStart;
				double reqHeight = child.getReqHeight();
				double cellHeight = Math.max( heightAvailable, reqHeight );
				
				if ( rowAlignment == VAlignment.TOP )
				{
					allocBox.allocateChildY( childAlloc, yStart, reqHeight );
				}
				else if ( rowAlignment == VAlignment.BOTTOM )
				{
					allocBox.allocateChildY( childAlloc, Math.max( yEnd - reqHeight, 0.0 ), reqHeight );
				}
				else if ( rowAlignment == VAlignment.CENTRE  ||  rowAlignment == VAlignment.BASELINES )
				{
					allocBox.allocateChildY( childAlloc, Math.max( yStart + ( heightAvailable - reqHeight ) * 0.5, 0.0 ), reqHeight );
				}
				else if ( rowAlignment == VAlignment.EXPAND )
				{
					allocBox.allocateChildY( childAlloc, yStart, cellHeight );
				}
			}
		}
	}
}
