//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;

public abstract class TextClipboardHandler extends ClipboardHandlerInterface
{
	protected abstract void deleteText(TextSelection selection, Caret caret);
	protected abstract void insertText(Marker marker, String text);
	protected abstract void replaceText(TextSelection selection, Caret caret, String replacement);
	protected abstract String getText(TextSelection selection);
	
	
	public Class<? extends Selection> getSelectionClass()
	{
		return TextSelection.class;
	}
	
	public Class<? extends Target> getTargetClass()
	{
		return Caret.class;
	}
	
	
	@Override
	public boolean deleteSelection(Selection selection, Target target)
	{
		if ( selection instanceof TextSelection  &&  target instanceof Caret )
		{
			TextSelection ts = (TextSelection)selection;
			deleteText( ts, (Caret)target );
			ts.clear();
			return true;
		}
		return false;
	}

	@Override
	public boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
	{
		if ( target instanceof Caret )
		{
			Caret caret = (Caret)target;
			if ( selection instanceof TextSelection )
			{
				TextSelection ts = (TextSelection)selection;
				replaceText( ts, caret, replacement );
				ts.clear();
			}
			else
			{
				caret.moveToStartOfNextItem();
				insertText( caret.getMarker(), replacement );
			}
			return true;
		}
		
		return false;
	}



	@Override
	public int getExportActions(Selection selection)
	{
		return COPY_OR_MOVE;
	}

	@Override
	public Transferable createExportTransferable(Selection selection)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			String selectedText = getText( ts );
			return new StringSelection( selectedText );
		}
		return null;
	}

	@Override
	public void exportDone(Selection selection, Target target, Transferable transferable, int action)
	{
		if ( action == MOVE )
		{
			if ( selection instanceof TextSelection  &&  target instanceof Caret )
			{
				TextSelection ts = (TextSelection)selection;
				deleteText( ts, (Caret)target );
				ts.clear();
			}
		}
	}

	
	@Override
	public boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		return target instanceof Caret  &&  dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
	}

	@Override
	public boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TextSelection ts = null;
		if ( selection instanceof TextSelection )
		{
			ts = (TextSelection)selection;
			
			if ( !ts.isValid() )
			{
				ts = null;
			}
		}
		
		if ( target instanceof Caret )
		{
			Caret caret = (Caret)target;

			if ( canImport( target, selection, dataTransfer ) )
			{
				try
				{
					String data = (String)dataTransfer.getTransferData( DataFlavor.stringFlavor );
					
					if ( ts != null )
					{
						replaceText( ts, caret, data );
						ts.clear();
					}
					else
					{
						caret.moveToStartOfNextItem();
						insertText( caret.getMarker(), data );
					}
				}
				catch (UnsupportedFlavorException e)
				{
					return false;
				}
				catch (IOException e)
				{
					return false;
				}
			}
		}
		
		return false;
	}
}
