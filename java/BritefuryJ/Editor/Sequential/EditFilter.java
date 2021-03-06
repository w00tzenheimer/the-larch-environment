//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;

public abstract class EditFilter implements TreeEventListener
{
	public enum HandleEditResult
	{
		HANDLED,
		NO_CHANGE,
		NOT_HANDLED,
		PASS_TO_PARENT
	}
	
	
	protected abstract SequentialController getSequentialController();
	
	
	protected boolean isSelectionEditEvent(EditEvent event)
	{
		return getSequentialController().isSelectionEditEvent( event );
	}
	
	protected boolean isEditEvent(EditEvent event)
	{
		return getSequentialController().isEditEvent( event );
	}
	
	
	protected abstract HandleEditResult handleEdit(LSElement element, LSElement sourceElement, EditEvent event);
	
	
	
	public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
	{
		if ( event instanceof EditEvent )
		{
			EditEvent editEvent = (EditEvent)event;
			
			if ( LSRegion.regionOf( sourceElement )  ==  LSRegion.regionOf( element ) )
			{
				if ( editEvent instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent ) )
				{
					HandleEditResult res = handleEdit( element, sourceElement, editEvent );
					
					if ( res == HandleEditResult.HANDLED )
					{
						FragmentView sourceFragment = (FragmentView)sourceElement.getFragmentContext();
						sourceFragment.queueRefresh();
						return true;
					}
					else if ( res == HandleEditResult.NO_CHANGE )
					{
						return true;
					}
					else if ( res == HandleEditResult.NOT_HANDLED )
					{
						return false;
					}
					else if ( res == HandleEditResult.PASS_TO_PARENT )
					{
						element.postTreeEventToParent( editEvent );
						return true;
					}
					else
					{
						throw new RuntimeException( "Invalid HandleEditResult" );
					}
				}
			}
		}
		return false;
	}
}
