//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

class ScrollBarHelper
{
	public enum Axis
	{
		HORIZONTAL,
		VERTICAL
	}


	protected static class ScrollBarDragBarInteractor implements PushElementInteractor, DragElementInteractor, ElementPainter, Range.RangeListener
	{
		private LSElement element;
		private Axis axis;
		private Range range;
		private double padding, rounding, minSize;
		private Painter dragBoxPainter, dragBoxHoverPainter;
		private double dragStartValue = 0.0;
		private double[] dragBoxBounds = null;
		private AABox2 visibleDragBox = null;
		
		
		public ScrollBarDragBarInteractor(LSElement element, Axis axis, Range range, double padding, double rounding, double minSize, Painter dragBoxPainter, Painter dragBoxHoverPainter)
		{
			this.element = element;
			this.axis = axis;
			this.range = range;
			this.padding = padding;
			this.rounding = rounding;
			this.minSize = minSize;
			this.dragBoxPainter = dragBoxPainter;
			this.dragBoxHoverPainter = dragBoxHoverPainter;
			range.addListener( this );
		}
		
		
	
		@Override
		public boolean buttonPress(LSElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				refreshDragBox();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( axis == Axis.HORIZONTAL  &&  localPos.x < dragBoxBounds[0]    ||  axis == Axis.VERTICAL  &&  localPos.y < dragBoxBounds[0] )
				{
					range.move( -range.getPageSize() );
					return true;
				}
				else if ( axis == Axis.HORIZONTAL  &&  localPos.x > dragBoxBounds[1]    ||  axis == Axis.VERTICAL  &&  localPos.y > dragBoxBounds[1] )
				{
					range.move( range.getPageSize() );
					return true;
				}
			}

			return false;
		}

		@Override
		public void buttonRelease(LSElement element, PointerButtonEvent event)
		{
		}

		
		
		@Override
		public boolean dragBegin(LSElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				refreshDragBox();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( ( axis == Axis.HORIZONTAL  &&  localPos.x >= dragBoxBounds[0]  &&  localPos.x <= dragBoxBounds[1] )   ||
						( axis == Axis.VERTICAL  &&  localPos.y >= dragBoxBounds[0]  &&  localPos.y <= dragBoxBounds[1] ) )
				{
					dragStartValue = range.getBegin();
					return true;
				}
			}

			return false;
		}
		
		@Override
		public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
		}
		
		@Override
		public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
		{
			AABox2 box = element.getLocalAABox();
			Point2 localPos = event.getPointer().getLocalPos();
			Vector2 deltaPos = localPos.sub( dragStartPos );
			
			double visibleRange, delta;
			if ( axis == Axis.HORIZONTAL )
			{
				visibleRange = box.getWidth()  -  padding * 2.0;
				delta = deltaPos.x;
			}
			else if ( axis == Axis.VERTICAL )
			{
				visibleRange = box.getHeight()  -  padding * 2.0;
				delta = deltaPos.y;
			}
			else
			{
				throw new RuntimeException( "Invalid direction" );
			}

			double scaleFactor = ( range.getMax() - range.getMin() ) / visibleRange;
			range.moveBeginTo( dragStartValue  +  delta * scaleFactor );
		}

		
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}
	
		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			refreshDragBox();
			
			RoundRectangle2D.Double shape = new RoundRectangle2D.Double( visibleDragBox.getLowerX(), visibleDragBox.getLowerY(), visibleDragBox.getWidth(), visibleDragBox.getHeight(),
					rounding, rounding );
			
			if ( element.isHoverActive() )
			{
				dragBoxHoverPainter.drawShape( graphics, shape );
			}
			else
			{
				dragBoxPainter.drawShape( graphics, shape );
			}
		}
		
		
		private void refreshDragBox()
		{
			if ( dragBoxBounds == null )
			{
				AABox2 dragBarBox = element.getLocalAABox();
				dragBoxBounds = new double[] { 0.0, 0.0 };
				if ( axis == Axis.HORIZONTAL )
				{
					double visibleRange = dragBarBox.getWidth()  -  padding * 2.0;
					computeDragBoxBounds( visibleRange, dragBoxBounds );
					visibleDragBox = new AABox2( padding + dragBoxBounds[0], padding, padding + dragBoxBounds[1], dragBarBox.getUpperY() - padding );
				}
				else if ( axis == Axis.VERTICAL )
				{
					double visibleRange = dragBarBox.getHeight()  -  padding * 2.0;
					computeDragBoxBounds( visibleRange, dragBoxBounds );
					visibleDragBox = new AABox2( padding, padding + dragBoxBounds[0], dragBarBox.getUpperX() - padding, padding + dragBoxBounds[1] );
				}
				else
				{
					throw new RuntimeException( "Invalid direction" );
				}
			}
		}
		
		
		private void computeDragBoxBounds(double visibleRange, double bounds[])
		{
			double naturalScaleFactor = visibleRange / ( range.getMax() - range.getMin() );
			double value = Math.min( Math.max( range.getBegin(), range.getMin() ), range.getMax() );
			double end = Math.min( Math.max( range.getEnd(), range.getMin() ), range.getMax() );
			double dragBoxSize = ( end - value ) * naturalScaleFactor;
			if ( dragBoxSize < minSize )
			{
				dragBoxSize = minSize;
				double rangeSize = ( range.getMax() - range.getMin() ) - ( end - value );
				if ( rangeSize > 0.0 )
				{
					double actualScaleFactor = ( visibleRange - dragBoxSize )  /  rangeSize;
					bounds[0] = ( value - range.getMin() ) * actualScaleFactor;
					bounds[1] = bounds[0] + dragBoxSize;
					return;
				}
			}

			// Fallback - use natural scale factor
			bounds[0] = ( value - range.getMin() ) * naturalScaleFactor;
			bounds[1] = ( end - range.getMin() ) * naturalScaleFactor;
		}
	
	
	
		@Override
		public void onRangeModified(Range r)
		{
			dragBoxBounds = null;
			visibleDragBox = null;
			element.queueFullRedraw();
		}
	}

}
