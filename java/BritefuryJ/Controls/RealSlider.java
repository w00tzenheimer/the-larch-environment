//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.Paint;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class RealSlider extends Slider
{
	public static interface RealSliderListener
	{
		public void onSliderValueChanged(RealSliderControl spinEntry, double value);
	}

	public static class RealSliderControl extends SliderControl
	{
		private double min, max, pivot;
		private RealSliderListener listener;
		
	
		protected RealSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Paint pivotPaint, Painter valuePainter, double rounding, double min, double max, double pivot, RealSliderListener listener)
		{
			super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, pivotPaint, valuePainter, rounding );
			
			this.min = min;
			this.max = max;
			this.pivot = pivot;
			this.listener = listener;
	
			element.setFixedValue( value.elementValueFunction() );
		}
		
		
		
		protected double getSliderMin()
		{
			return min;
		}
		
		protected double getSliderMax()
		{
			return max;
		}
		
		protected double getSliderPivot()
		{
			return pivot;
		}
		
		protected double getSliderValue()
		{
			return (Double)value.getStaticValue();
		}

		
		protected void changeValue(double newValue)
		{
			double currentValue = getSliderValue();
			newValue = Math.min( Math.max( newValue, min ), max );
			if ( newValue != currentValue )
			{
				value.setLiteralValue( newValue );
				if ( listener != null )
				{
					listener.onSliderValueChanged( this, newValue );
				}
			}
		}
	}
	
	
	private static class CommitListener implements RealSliderListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onSliderValueChanged(RealSliderControl spinEntry, double value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private double min, max, pivot;
	private RealSliderListener listener;
	
	
	private RealSlider(LiveSource valueSource, double min, double max, double pivot, RealSliderListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.pivot = pivot;
		this.listener = listener;
	}
	
	public RealSlider(double initialValue, double min, double max, double pivot, RealSliderListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, pivot, listener );
	}
	
	public RealSlider(LiveInterface value, double min, double max, double pivot, RealSliderListener listener)
	{
		this( new LiveSourceRef( value ), min, max, pivot, listener );
	}
	
	public RealSlider(LiveValue value, double min, double max, double pivot)
	{
		this( new LiveSourceRef( value ), min, max, pivot, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected SliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Paint pivotPaint, Painter valuePainter, double rounding)
	{
		return new RealSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, pivotPaint, valuePainter, rounding, min, max, pivot, listener );
	}
}