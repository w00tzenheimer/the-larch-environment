//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeColumn;
import BritefuryJ.LSpace.StyleParams.ColumnStyleParams;


public class LSColumn extends LSAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	protected final static int FLAG_HAS_REFPOINT_INDEX = FLAGS_CONTAINER_END * 0x1;
	
	protected final static int FLAGS_COLUMN_END = FLAGS_CONTAINER_END << 1;

	
	
	private int refPointIndex = 0;
	

	
	public LSColumn(LSElement[] items)
	{
		this( ColumnStyleParams.defaultStyleParams, items );
	}
	
	public LSColumn(ColumnStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeColumn( this );
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	
	
	//
	//
	// Ref-point index
	//
	//


	public void setRefPointIndex(int refPointIndex)
	{
		this.refPointIndex = refPointIndex;
		setFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public void clearRefPointIndex()
	{
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	
	
	public boolean hasRefPointIndex()
	{
		return testFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public int getRefPointIndex()
	{
		return refPointIndex;
	}
}
