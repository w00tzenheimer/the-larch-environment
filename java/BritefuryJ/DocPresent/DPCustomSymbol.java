package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.CustomSymbolStyleSheet;
import BritefuryJ.Math.Point2;


public class DPCustomSymbol extends DPContentLeaf
{
	public interface SymbolInterface
	{
		public HMetrics computeHMetrics();
		public VMetrics computeVMetrics();
		public void draw(Graphics2D graphics);
	}
	
	
	protected SymbolInterface symbol;
	
	
	
	public DPCustomSymbol(SymbolInterface symbol, String content)
	{
		this( CustomSymbolStyleSheet.defaultStyleSheet, symbol, content );
	}
	
	public DPCustomSymbol(CustomSymbolStyleSheet styleSheet, SymbolInterface symbol, String content)
	{
		super( styleSheet, content );
		
		this.symbol = symbol;
		
		queueResize();
	}
	
	
	public SymbolInterface getSymbol()
	{
		return symbol;
	}
	
	public void setSymbol(SymbolInterface symbol)
	{
		this.symbol = symbol;
		queueResize();
	}
	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		graphics.setColor( getColour() );
		symbol.draw( graphics );
	}
	
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return symbol.computeHMetrics();
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		return symbol.computeHMetrics();
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return symbol.computeVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return symbol.computeVMetrics();
	}
	
	
	protected Color getColour()
	{
		return ((CustomSymbolStyleSheet)styleSheet).getColour();
	}

	
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		int index = c.getMarker().getIndex();
		double x = index == 0  ?  0.0  :  symbol.computeHMetrics().width;
		Line2D.Double line = new Line2D.Double( x, 0.0, x, symbol.computeVMetrics().height );
		graphics.draw( line );
	}

	public int getContentPositonForPoint(Point2 localPos)
	{
		if ( localPos.x  >=  symbol.computeHMetrics().width * 0.5 )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}

