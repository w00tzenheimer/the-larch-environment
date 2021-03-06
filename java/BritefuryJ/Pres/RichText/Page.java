//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleValues;

public class Page extends SequentialPres
{
	public Page(Object children[])
	{
		super( children );
	}
	
	public Page(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement xs[] = mapPresent( ctx, RichText.usePageAttrs( style ).withAttr( Primitive.hAlign, HAlignment.EXPAND ).withAttr( Primitive.vAlign, VAlignment.REFY ), children );
		return RichText.pageStyle( style ).applyTo( new Column( xs ).alignHExpand().alignVRefY() ).present( ctx, style );
	}
}
