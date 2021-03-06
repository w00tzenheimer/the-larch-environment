//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class SpaceBin extends Pres
{
	private Pres child;
	private double width, height;
	private LSSpaceBin.SizeConstraint sizeConstraintX, sizeConstraintY;
	
	
	public SpaceBin(double width, double height, LSSpaceBin.SizeConstraint sizeConstraintX, LSSpaceBin.SizeConstraint sizeConstraintY, Object child)
	{
		this.child = coerce( child );
		this.width = width;
		this.height = height;
		this.sizeConstraintX = sizeConstraintX;
		this.sizeConstraintY = sizeConstraintY;
	}

	public SpaceBin(double width, double height, LSSpaceBin.SizeConstraint sizeConstraint, Object child)
	{
		this( width, height, sizeConstraint, sizeConstraint, child );
	}
	
	public SpaceBin(double width, double height, Object child)
	{
		this( width, height, LSSpaceBin.SizeConstraint.LARGER, child );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = Primitive.useContainerParams.get( style );
		LSElement childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
		return new LSSpaceBin( Primitive.containerParams.get( style ), width, height, sizeConstraintX, sizeConstraintY, childElem );
	}
}
