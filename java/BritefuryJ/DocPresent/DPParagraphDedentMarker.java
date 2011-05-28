//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeParagraphDedentMarker;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPParagraphDedentMarker extends DPBlank
{
	public DPParagraphDedentMarker()
	{
		super( );
		
		layoutNode = new LayoutNodeParagraphDedentMarker( this );
	}
	
	public DPParagraphDedentMarker(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeParagraphDedentMarker( this );
	}
	
	protected DPParagraphDedentMarker(DPParagraphDedentMarker element)
	{
		super( element );
		
		layoutNode = new LayoutNodeParagraphDedentMarker( this );
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPParagraphDedentMarker clone = new DPParagraphDedentMarker( this );
		clone.clonePostConstuct( this );
		return clone;
	}
}
