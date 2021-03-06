//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Cell.Presenters;

import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.CellEditPerspective;
import BritefuryJ.Controls.ColourPicker;
import BritefuryJ.Controls.ColourPicker.ColourPickerControl;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.Pres.Pres;

public class PresentersAWT extends ObjectPresenterRegistry
{
	public PresentersAWT()
	{
		registerJavaObjectPresenter( Color.class,  presenter_Color );
	}


	public static final ObjectPresenter presenter_Color = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			final Color colour = (Color)x;
			
			ColourPicker.ColourPickerListener listener = new ColourPicker.ColourPickerListener()
			{
				@Override
				public void onColourChanged(ColourPickerControl colourPicker, Color colour)
				{
					CellEditPerspective.notifySetCellValue( colourPicker.getElement(), colour );
				}
			};
			
			Pres control = new ColourPicker( colour, listener );
			return control.alignVRefYExpand();
		}
	};
}
