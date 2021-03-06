//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSTable;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocBox;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.Layout.TableLayout;
import BritefuryJ.LSpace.Layout.TableLayout.TablePackingParams;
import BritefuryJ.LSpace.StyleParams.TableStyleParams;
import BritefuryJ.Math.Point2;

public class LayoutNodeTable extends ArrangedLayoutNode
{
	private LReqBox columnBoxes[], rowBoxes[];
	private LAllocBox columnAllocBoxes[], rowAllocBoxes[];
	private double columnLines[][], rowLines[][];		// Format: one array per column/row. First element is the x/y position. Subsequent element PAIRS are start and end y/x of line segments.

	
	public LayoutNodeTable(LSTable element)
	{
		super( element );
	}


	protected static void drawCellLines(Graphics2D graphics, double[][] columnLines, double[][] rowLines)
	{
		for (double col[]: columnLines)
		{
			double x = col[0];
			for (int i = 1; i < col.length; i += 2)
			{
				double y1 = col[i], y2 = col[i+1];
				Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
				graphics.draw( line );
			}
		}
		
		for (double row[]: rowLines)
		{
			double y = row[0];
			for (int i = 1; i < row.length; i += 2)
			{
				double x1 = row[i], x2 = row[i+1];
				Line2D.Double line = new Line2D.Double( x1, y, x2, y );
				graphics.draw( line );
			}
		}
	}


	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSTable table = (LSTable)element;
		List<LSElement> layoutChildren = table.getLayoutChildren();
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			LSElement child = layoutChildren.get( i );
			childBoxes[i] = child.getLayoutNode().refreshRequisitionX();
		}

		columnBoxes = TableLayout.computeRequisitionX( layoutReqBox, childBoxes, packingParams, table.getNumColumns(), table.getNumRows(), getColumnSpacing(), getRowSpacing() );
		columnAllocBoxes = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSTable table = (LSTable)element;
		List<LSElement> layoutChildren = ( (LSTable)element ).getLayoutChildren();
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			LSElement child = layoutChildren.get( i );
			childBoxes[i] = child.getLayoutNode().refreshRequisitionY();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}

		rowBoxes = TableLayout.computeRequisitionY( layoutReqBox, childBoxes, packingParams, childAlignmentFlags, table.getNumColumns(), table.getNumRows(), getColumnSpacing(), getRowSpacing() );
		rowAllocBoxes = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = new LAllocBox( null );
		}
	}
	


	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSTable table = (LSTable)element;
		List<LSElement> layoutChildren = ( (LSTable)element ).getLayoutChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[layoutChildren.size()];
		double prevWidths[] = new double[layoutChildren.size()];
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			LSElement child = layoutChildren.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevWidths[i] = layoutNode.getAllocWidth();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, getAllocationBox(), columnAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, table.getNumColumns(), table.getNumRows(), getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (LSElement child: layoutChildren)
		{
			child.getLayoutNode().refreshAllocationX( prevWidths[i] );
			i++;
		}
		
		// Re-allocated - column and row lines are now invalid
		columnLines = null;
		rowLines = null;
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSTable table = (LSTable)element;
		List<LSElement> layoutChildren = ( (LSTable)element ).getLayoutChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[layoutChildren.size()];
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		LAllocV prevAllocVs[] = new LAllocV[layoutChildren.size()];
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			LSElement child = layoutChildren.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevAllocVs[i] = layoutNode.getAllocV();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateY( layoutReqBox, rowBoxes, childBoxes, getAllocationBox(), rowAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, table.getNumColumns(), table.getNumRows(), getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (LSElement child: layoutChildren)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
		
		// Re-allocated - column and row lines are now invalid
		columnLines = null;
		rowLines = null;
	}
	

	
	
	
	private boolean doesChildCoverCell(LSElement child, int x, int y)
	{
		LSTable table = (LSTable)element;
		TablePackingParams packing = table.getTablePackingParamsForChild( child );

		return x <= ( packing.x + packing.colSpan )  &&  y <= ( packing.y + packing.rowSpan );
	}
	
	public LSElement getChildCoveringCell(int x, int y)
	{
		LSTable table = (LSTable)element;
		int pos[] = getPositionOfChildCoveringCell( x, y );
		return pos != null  ?  table.getChildAt( pos[0], pos[1] )  :  null;
	}

	public int[] getPositionOfChildCoveringCell(int x, int y)
	{
		LSTable table = (LSTable)element;

		LSElement child = table.getChildAt( x, y );
		
		if ( child != null )
		{
			return new int[] { x, y };
		}
		else
		{
			int maxRadius = Math.max( x, y );
			for (int radius = 1; radius <= maxRadius; radius++)
			{
				// Column to left, going up
				if ( radius <= x )		// Ensure that the column, that is @radius spaces to the left is within the bounds of the table
				{
					int colX = x - radius;
					for (int i = 0; i < radius; i++)
					{
						int searchY = y - i;
						if ( searchY >= 0 )
						{
							child = table.getChildAt( colX, searchY );
							if ( child != null  &&  doesChildCoverCell( child, x, y ) )
							{
								return new int[] { colX, searchY };
							}
						}
					}
				}
				
				// Row above, going left
				if ( radius <= y )		// Ensure that the row, that is @radius spaces above is within the bounds of the table
				{
					int rowY = y - radius;
					for (int i = 0; i < radius; i++)
					{
						int searchX = x - i;
						if ( searchX >= 0 )
						{
							child = table.getChildAt( searchX, rowY );
							if ( child != null  &&  doesChildCoverCell( child, x, y ) )
							{
								return new int[] { searchX, rowY };
							}
						}
					}
				}
				
				// Cell above and to left
				if ( radius <= x  &&  radius <= y )
				{
					child = table.getChildAt( x - radius, y - radius );
					if ( child != null  &&  doesChildCoverCell( child, x, y ) )
					{
						return new int[] { x - radius, y - radius };
					}
				}
			}
			
			return null;
		}
	}

	
	private int getColumnForLocalPoint(Point2 localPos)
	{
		if ( columnAllocBoxes.length == 0 )
		{
			return -1;
		}
		else if ( columnAllocBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBox columnI = columnAllocBoxes[0];
			for (int i = 0; i < columnAllocBoxes.length - 1; i++)
			{
				LAllocBox columnJ = columnAllocBoxes[i+1];
				double iUpperX = columnI.getAllocPositionInParentSpaceX() + columnI.getAllocWidth();
				double jLowerX = columnJ.getAllocPositionInParentSpaceX();
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return i;
				}
				
				columnI = columnJ;
			}
			
			return columnAllocBoxes.length-1;
		}
	}

	
	
	private int getRowForLocalPoint(Point2 localPos)
	{
		if ( rowAllocBoxes.length == 0 )
		{
			return -1;
		}
		else if ( rowAllocBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBox rowI = rowAllocBoxes[0];
			for (int i = 0; i < rowAllocBoxes.length - 1; i++)
			{
				LAllocBox rowJ = rowAllocBoxes[i+1];
				double iUpperY = rowI.getAllocPositionInParentSpaceY() + rowI.getAllocHeight();
				double jLowerY = rowJ.getAllocPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					return i;
				}
				
				rowI = rowJ;
			}
			
			return rowAllocBoxes.length-1;
		}
	}

	
	
	public int[] getCellPositionUnder(Point2 localPos)
	{
		int x = getColumnForLocalPoint( localPos );
		int y = getRowForLocalPoint( localPos );
		return new int[] { x, y };
	}
	
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		int x = getColumnForLocalPoint( localPos );
		int y = getRowForLocalPoint( localPos );
		LSElement child = null;
		if ( x != -1  &&  y != -1 )
		{
			child = getChildCoveringCell( x, y );
		}
		if ( child != null )
		{
			return getLeafClosestToLocalPointFromChild( child, localPos, filter );
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	//
	// COLUMN AND ROW QUERY METHODS
	//
	//
	
	public double getColumnLeft(int column)
	{
		LAllocBoxInterface box = columnAllocBoxes[column];
		return box.getAllocPositionInParentSpaceX();
	}

	public double getColumnRight(int column)
	{
		LAllocBoxInterface box = columnAllocBoxes[column];
		return box.getAllocPositionInParentSpaceX() + box.getActualWidth();
	}

	public double getRowTop(int row)
	{
		LAllocBoxInterface box = rowAllocBoxes[row];
		return box.getAllocPositionInParentSpaceY();
	}

	public double getRowBottom(int row)
	{
		LAllocBoxInterface box = rowAllocBoxes[row];
		return box.getAllocPositionInParentSpaceY() + box.getAllocHeight();
	}


	
	
	
	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		LSTable table = (LSTable)element;
		return table.getLayoutChildren();
	}
	
	
	
	//
	//
	// BOUNDARIES
	//
	//
	
	private void refreshBoundaries()
	{
		if ( ( columnLines == null  ||  rowLines == null ) )
		{
			LSTable table = (LSTable)element;
			int numColumns = table.getNumColumns();
			int numRows = table.getNumRows();
			int numColLines = Math.max( numColumns - 1, 0 );
			int numRowLines = Math.max( numRows - 1, 0 );
			columnLines = new double[numColLines][];
			rowLines = new double[numRowLines][];
			
			
			// Create and initialise bitsets for the column and row lines
			BitSet columnBits[] = new BitSet[numColLines];
			BitSet rowBits[] = new BitSet[numRowLines];
			
			for (int i = 0; i < columnBits.length; i++)
			{
				columnBits[i] = new BitSet( numRows );
			}
			for (int i = 0; i < rowBits.length; i++)
			{
				rowBits[i] = new BitSet( numColumns );
			}
			
			
			// For each cell, set the bits that mark its boundary
			for (TablePackingParams entry: table.getTablePackingParamsArray())
			{
				int top = entry.y, bottom = entry.y + entry.rowSpan;
				int left = entry.x, right = entry.x + entry.colSpan;
				
				// NOTE: ignore lines that are on the table boundary
				
				// Top
				if ( top > 0 )
				{
					BitSet bits = rowBits[top-1];
					for (int x = left; x < right; x++)
					{
						bits.set( x );
					}
				}

				// Bottom
				if ( bottom < numRows )
				{
					BitSet bits = rowBits[bottom-1];
					for (int x = left; x < right; x++)
					{
						bits.set( x );
					}
				}
				
				// Left
				if ( left > 0 )
				{
					BitSet bits = columnBits[left-1];
					for (int y = top; y < bottom; y++)
					{
						bits.set( y );
					}
				}
				
				// Right
				if ( right < numColumns )
				{
					BitSet bits = columnBits[right-1];
					for (int y = top; y < bottom; y++)
					{
						bits.set( y );
					}
				}
			}
			
			ArrayList<Double> spanStarts = new ArrayList<Double>();
			ArrayList<Double> spanEnds = new ArrayList<Double>();
			
			double halfColumnSpacing = getColumnSpacing() * 0.5;
			double halfRowSpacing = getRowSpacing() * 0.5;

			for (int columnLine = 0; columnLine < numColLines; columnLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int y = 0;
				while ( y < numRows )
				{
					int spanIndices[] = getSpanFromBitSet( columnBits[columnLine], y );
					if ( spanIndices[0] == -1  ||  spanIndices[1] == -1 )
					{
						break;
					}
					double topSpacing = spanIndices[0] == 0  ?  0.0  :  halfRowSpacing;
					double bottomSpacing = spanIndices[1] == numRows-1  ?  0.0  :  halfRowSpacing;
					spanStarts.add( getRowTop( spanIndices[0] ) - topSpacing );
					spanEnds.add( getRowBottom( spanIndices[1] ) + bottomSpacing );
					y = spanIndices[1] + 1;
				}
				
				double col[] = new double[spanStarts.size()*2+1];
				col[0] = getColumnRight( columnLine )  +  halfColumnSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					col[i*2+1] = spanStarts.get( i );
					col[i*2+2] = spanEnds.get( i );
				}
				columnLines[columnLine] = col;
			}
			
			for (int rowLine = 0; rowLine < numRowLines; rowLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int x = 0;
				while ( x < numColumns )
				{
					int spanIndices[] = getSpanFromBitSet( rowBits[rowLine], x );
					if ( spanIndices[0] == -1  ||  spanIndices[1] == -1 )
					{
						break;
					}
					double leftSpacing = spanIndices[0] == 0  ?  0.0  :  halfColumnSpacing;
					double rightSpacing = spanIndices[1] == numColumns-1  ?  0.0  :  halfColumnSpacing;
					spanStarts.add( getColumnLeft( spanIndices[0] ) - leftSpacing );
					spanEnds.add( getColumnRight( spanIndices[1] ) + rightSpacing );
					x = spanIndices[1] + 1;
				}
				
				double row[] = new double[spanStarts.size()*2+1];
				row[0] = getRowBottom( rowLine )  +  halfRowSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					row[i*2+1] = spanStarts.get( i );
					row[i*2+2] = spanEnds.get( i );
				}
				rowLines[rowLine] = row;
			}
		}
	}
	
	private static int[] getSpanFromBitSet(BitSet bits, int startIndex)
	{
		int start = bits.nextSetBit( startIndex );
		if ( start == -1 )
		{
			return new int[] { -1, -1 };
		}
		int end = bits.nextClearBit( start );
		if ( end == -1 )
		{
			end = bits.length();
		}
		return new int[] { start, end - 1 };
	}
	
	

	protected static void drawTableCellLines(Graphics2D graphics, double[][] columnLines, double[][] rowLines)
	{
		for (double col[]: columnLines)
		{
			double x = col[0];
			for (int i = 1; i < col.length; i += 2)
			{
				double y1 = col[i], y2 = col[i+1];
				Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
				graphics.draw( line );
			}
		}
		
		for (double row[]: rowLines)
		{
			double y = row[0];
			for (int i = 1; i < row.length; i += 2)
			{
				double x1 = row[i], x2 = row[i+1];
				Line2D.Double line = new Line2D.Double( x1, y, x2, y );
				graphics.draw( line );
			}
		}
	}
	
	
	public void drawCellLines(Graphics2D graphics)
	{
		refreshBoundaries();
		drawTableCellLines( graphics, columnLines, rowLines );
	}



	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams)element.getStyleParams()).getRowExpand();
	}
}
