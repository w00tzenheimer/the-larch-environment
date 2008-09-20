//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;

public class FractionElement extends BranchElement
{
	public static class BarElement extends LeafElement
	{
		public BarElement(String content)
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, content );
		}

		public BarElement(FractionStyleSheet.BarStyleSheet styleSheet, String content)
		{
			super( new DPFraction.DPFractionBar( styleSheet, content ) );
		}



		public DPFraction.DPFractionBar getWidget()
		{
			return (DPFraction.DPFractionBar)widget;
		}
	}

	
	
	
	public static int NUMERATOR = DPFraction.NUMERATOR;
	public static int BAR = DPFraction.BAR;
	public static int DENOMINATOR = DPFraction.DENOMINATOR;
	
	public static int NUMCHILDREN = DPFraction.NUMCHILDREN;
	
	
	protected Element[] children;
	
	
	
	public FractionElement()
	{
		this( FractionStyleSheet.defaultStyleSheet, "/" );
	}
	
	public FractionElement(String content)
	{
		this( FractionStyleSheet.defaultStyleSheet, content );
	}
	
	public FractionElement(FractionStyleSheet styleSheet)
	{
		this( styleSheet, "/" );
	}
	
	public FractionElement(FractionStyleSheet styleSheet, String content)
	{
		super( new DPFraction( styleSheet ) );
		
		children = new Element[NUMCHILDREN];
		
		setChild( BAR, new BarElement( styleSheet.getBarStyleSheet(), content ) );
	}




	public Element getChild(int slot)
	{
		return children[slot];
	}
	
	public void setChild(int slot, Element child)
	{
		Element existingChild = children[slot];
		if ( child != existingChild )
		{
			if ( existingChild != null )
			{
				existingChild.setParent( null );
				existingChild.setElementTree( null );
			}
			
			children[slot] = child;
			DPWidget childWidget = null;
			if ( child != null )
			{
				childWidget = child.getWidget();
			}
			getWidget().setChild( slot, childWidget );
			
			
			if ( child != null )
			{
				child.setParent( this );
				child.setElementTree( tree );
			}
		}
	}
	
	
	
	
	public Element getNumeratorChild()
	{
		return getChild( NUMERATOR );
	}
	
	public Element getBarChild()
	{
		return getChild( BAR );
	}
	
	public Element getDenominatorChild()
	{
		return getChild( DENOMINATOR );
	}
	
	
	public void setNumeratorChild(Element child)
	{
		setChild( NUMERATOR, child );
	}
	
	public void setBarChild(Element child)
	{
		setChild( BAR, child );
	}
	
	public void setDenominatorChild(Element child)
	{
		setChild( DENOMINATOR, child );
	}
	


	public DPFraction getWidget()
	{
		return (DPFraction)widget;
	}


	public List<Element> getChildren()
	{
		Vector<Element> xs = new Vector<Element>();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( children[slot] != null )
			{
				xs.add( children[slot] );
			}
		}
		
		return xs;
	}



	public Element getContentLineFromChild(Element element)
	{
		return this;
	}
}