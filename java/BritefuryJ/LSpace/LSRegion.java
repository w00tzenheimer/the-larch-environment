//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.StyleParams.RegionStyleParams;

public class LSRegion extends LSProxy
{
	public static class SharableSelectionFilter implements ElementFilter
	{
		private LSRegion regionA;
		private ClipboardHandlerInterface handlerA;
		
		
		public SharableSelectionFilter(LSRegion regionA)
		{
			this.regionA = regionA;
			handlerA = regionA.getClipboardHandler();
		}
		
		
		public boolean testElement(LSElement element)
		{
			LSRegion regionB = regionOf( element );
			
			if ( regionB == regionA )
			{
				return true;
			}
			else
			{
				ClipboardHandlerInterface handlerB = regionB != null  ?  regionB.getClipboardHandler()  :  null;
				if ( handlerA != null )
				{
					return handlerA.canShareSelectionWith( handlerB );
				}
				else
				{
					return false;
				}
			}
		}
	}

	
	
	public static final int FLAG_EDITABLE = FLAGS_PROXY_END * 0x1;
	public static final int FLAG_SELECTABLE = FLAGS_PROXY_END * 0x2;
	
	
	protected final static int FLAGS_REGION_END = FLAGS_PROXY_END * 0x4;

	
	private ClipboardHandlerInterface clipboardHandler;

	
	
	
	public LSRegion(RegionStyleParams styleParams, LSElement child)
	{
		super( styleParams, child );
	
		setFlagValue( FLAG_EDITABLE, styleParams.getEditable() );
		setFlagValue( FLAG_SELECTABLE, styleParams.getSelectable() );
	}
	
	
	//
	//
	// CLIPBOARD HANDLER
	//
	//
	
	public void setClipboardHandler(ClipboardHandlerInterface handler)
	{
		clipboardHandler = handler;
	}
	
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return clipboardHandler;
	}
	
	

	
	
	//
	// REGION
	//
	
	protected boolean isRegion()
	{
		return true;
	}
	
	public static LSRegion regionOf(LSElement e)
	{
		while ( e != null )
		{
			if ( e.isRegion() )
			{
				return (LSRegion)e;
			}
			
			e = e.parent;
		}
		return null;
	}
	
	
	
	
	
	//
	//
	// EDITABILITY METHODS
	//
	//
	
	public void setEditable()
	{
		setFlag( FLAG_EDITABLE );
	}
	
	public void setNonEditable()
	{
		clearFlag( FLAG_EDITABLE );
	}
	
	public boolean isEditable()
	{
		return testFlag( FLAG_EDITABLE );
	}
	
	
	
	
	//
	//
	// SELECTABILITY METHODS
	//
	//
	
	public void setSelectable()
	{
		setFlag( FLAG_SELECTABLE );
	}
	
	public void setUnselectable()
	{
		clearFlag( FLAG_SELECTABLE );
	}
	
	public boolean isSelectable()
	{
		return testFlag( FLAG_SELECTABLE );
	}
	
	
	
	
	
	
	
	
	//
	//
	// SHARABLE SELECTION FILTER
	//
	//
	
	public SharableSelectionFilter sharableSelectionFilter()
	{
		return new SharableSelectionFilter( this );
	}
}
