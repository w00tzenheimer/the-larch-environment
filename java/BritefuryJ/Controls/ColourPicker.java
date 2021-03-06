//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JColorChooser;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ColourPicker extends ControlPres
{
	public static interface ColourPickerListener
	{
		public void onColourChanged(ColourPickerControl colourPicker, Color colour);
	}
	
	
	
	public static class ColourPickerControl extends Control
	{
		private LSElement element;
		
		
		public ColourPickerControl(PresentationContext ctx, StyleValues style)
		{
			super( ctx, style );
		}



		@Override
		public LSElement getElement()
		{
			return element;
		}
	}

	
	
	private static class CommitListener implements ColourPickerListener
	{
		private LiveInterface value;
		
		public CommitListener(LiveInterface value)
		{
			this.value = value;
		}
		
		@Override
		public void onColourChanged(ColourPickerControl editableLabel, Color colour)
		{
			value.setLiteralValue( colour );
		}
	}
	
	

	private LiveSource valueSource;
	private ColourPickerListener listener;
	
	
	
	
	private ColourPicker(LiveSource valueSource, ColourPickerListener listener)
	{
		this.valueSource = valueSource;
		this.listener = listener;
	}
	
	
	public ColourPicker(Color initialColour, ColourPickerListener listener)
	{
		this( new LiveSourceValue( initialColour ), listener );
	}
	
	public ColourPicker(LiveInterface value, ColourPickerListener listener)
	{
		this( new LiveSourceRef( value ), listener );
	}	
	
	public ColourPicker(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ) );
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();
		final ColourPickerControl ctl = new ColourPickerControl( ctx, style );

		LiveFunction.Function displayFunction = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				Object v = value.getValue();
				final Color colour = v instanceof Color  ?  (Color)v  :  Color.GRAY;
				
				ClickElementInteractor swatchInteractor = new ClickElementInteractor()
				{
					@Override
					public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
					{
						return event.getButton() == 1;
					}

					@Override
					public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
					{
						Color newColour = JColorChooser.showDialog( element.getRootElement().getComponent(), "Choose colour", colour );

						if ( newColour != null )
						{
							listener.onColourChanged( ctl, newColour );
						}

						return true;
					}
				};
				
				
				StyleSheet swatchStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( colour ) ), Primitive.cursor.as( new Cursor( Cursor.HAND_CURSOR ) ) );
				Pres swatch = swatchStyle.applyTo( new Box( 30.0, 20.0 ) );
				swatch = swatch.withElementInteractor( swatchInteractor );
				swatch = swatchBorder.surround(swatch);
				return swatch;
			}
		};
		
		LiveFunction display = new LiveFunction( displayFunction );
		
		ctl.element = display.present( ctx, style );
		
		return ctl;
	}


	private static final SolidBorder swatchBorder = new SolidBorder(1.0, 1.0, 0.0, 0.0, new Color(0.2f, 0.2f, 0.2f), null);
}
