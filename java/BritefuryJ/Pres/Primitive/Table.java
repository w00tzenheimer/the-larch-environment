//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSTable;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Table extends Pres
{
	public static class TableCell
	{
		private Pres child;
		private int colSpan, rowSpan;
		
		
		public TableCell(Pres child, int colSpan, int rowSpan)
		{
			this.child = child;
			this.colSpan = colSpan;
			this.rowSpan = rowSpan;
		}

		public TableCell(Object child, int colSpan, int rowSpan)
		{
			this( Pres.coerce( child ), colSpan, rowSpan );
		}
	}
	
	
	
	private TableCell childCells[][];
	
	
	public Table()
	{
		childCells = new TableCell[0][];
	}
	
	public Table(Object children[][])
	{
		childCells = new TableCell[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			Object row[] = children[y];
			childCells[y] = new TableCell[row.length];
			for (int x = 0; x < row.length; x++)
			{
				if ( row[x] != null )
				{
					childCells[y][x] = new TableCell( coerce( row[x] ), 1, 1 );
				}
				else
				{
					childCells[y][x] = null;
				}
			}
		}
	}
	
	public Table(TableCell children[][])
	{
		childCells = new TableCell[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			TableCell row[] = children[y];
			childCells[y] = new TableCell[row.length];
			System.arraycopy( row, 0, this.childCells[y], 0, row.length );
		}
	}
	
	
	public void put(int x, int y, Pres child)
	{
		put( x, y, 1, 1, child );
	}
	
	public void put(int x, int y, int colSpan, int rowSpan, Pres child)
	{
		if ( y >= childCells.length  &&  child != null )
		{
			// We need to expand the number of rows
			TableCell newRows[][] = new TableCell[y+1][];
			System.arraycopy( childCells, 0, newRows, 0, childCells.length );
			
			for (int r = childCells.length; r <= y; r++)
			{
				newRows[r] = new TableCell[0];
			}
			childCells = newRows;
		}
		
		
		TableCell row[] = childCells[y];
		if ( x >= row.length  &&  child != null )
		{
			// We need to expand the number of columns in this row
			TableCell newCols[] = new TableCell[x+1];
			System.arraycopy( row, 0, newCols, 0, row.length );
			
			for (int c = row.length; c <= x; c++)
			{
				newCols[c] = null;
			}

			row = newCols;
			childCells[y] = row;
		}
		
		
		row[x] = child != null  ?  new TableCell( child, colSpan, rowSpan )  :  null;
		
		
		if ( child == null )
		{
			// We removed a child
			
			if ( x == ( row.length - 1 ) )
			{
				// We removed a child from the end of the row; shorten it
				
				int numColumns = 0;
				for (int c = 0; c < row.length; c++)
				{
					if ( row[c].child != null )
					{
						numColumns = Math.max( numColumns, c + 1 );
					}
				}
				TableCell newRow[] = new TableCell[numColumns];
				System.arraycopy( row, 0, newRow, 0, numColumns );
				childCells[y] = newRow;
				row = newRow;
				
				
				if ( y == ( childCells.length - 1 )  &&  numColumns == 0 )
				{
					// The row we just shortened is empty, and its the last row. Shorten the row list accordingly
					int numRows = 0;
					for (int r = 0; r < childCells.length; r++)
					{
						if ( childCells[r].length > 0 )
						{
							numRows = Math.max( numRows, r + 1 );
						}
					}
					
					TableCell newCells[][] = new TableCell[numRows][];
					System.arraycopy( childCells, 0, newCells, 0, numRows );
					childCells = newCells;
				}
			}
		}
	}
	
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = Primitive.useTableParams( style );
		HAlignment childHAlign = childStyle.get( Primitive.hAlign, HAlignment.class );
		VAlignment childVAlign = childStyle.get( Primitive.vAlign, VAlignment.class );
		LSTable.TableCell[][] elemCells = null;
		if ( childCells != null )
		{
			elemCells = new LSTable.TableCell[childCells.length][];
			for (int y = 0; y < childCells.length; y++)
			{
				TableCell row[] = childCells[y];
				LSTable.TableCell elemRow[] = new LSTable.TableCell[row.length];
				elemCells[y] = elemRow;
				for (int x = 0; x < row.length; x++)
				{
					TableCell cell = row[x];
					elemRow[x] = cell != null  ?  new LSTable.TableCell( cell.child.present( ctx, childStyle ).layoutWrap( childHAlign, childVAlign ), cell.colSpan, cell.rowSpan )  :  null;  
				}
			}
		}
		
		LSTable table = new LSTable( Primitive.tableParams.get( style ), elemCells );
		return applyTableBorder( style, table );
	}

	protected static LSElement applyTableBorder(StyleValues style, LSElement table)
	{
		AbstractBorder tableBorder = style.get( Primitive.tableBorder, AbstractBorder.class );
		if ( tableBorder != null )
		{
			LSBorder border = new LSBorder( tableBorder, table );
			return border;
		}
		else
		{
			return table;
		}
	}
}
