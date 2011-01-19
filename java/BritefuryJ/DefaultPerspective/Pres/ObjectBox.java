//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ObjectBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coerceNonNull( contents );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( GenericStyle.objectContentPadding, Double.class );
		StyleValues childStyle = GenericStyle.useObjectBorderAttrs( GenericStyle.useObjectBoxAttrs( style ) );
		DPElement contentsElement = contents.present( ctx, childStyle );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new Column( new Object[] { titlePres, contentsElement.padX( padding ) } ).alignHExpand() ).present( ctx, style );
	}
}