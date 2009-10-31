//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;


public class DPBorder extends DPBin
{
	public static EmptyBorder defaultBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0 );
	
	protected Border border;
	
	
	
	public DPBorder(ElementContext context)
	{
		this( context, defaultBorder, ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBorder(ElementContext context, Border border)
	{
		this( context, border, ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBorder(ElementContext context, ContainerStyleSheet styleSheet)
	{
		this( context, defaultBorder, styleSheet );
	}
	
	public DPBorder(ElementContext context, Border border, ContainerStyleSheet styleSheet)
	{
		super( context, styleSheet );
		
		this.border = border;
	}
	
	
	
	public Border getBorder()
	{
		return border;
	}
	
	public void setBorder(Border b)
	{
		if ( b != border )
		{
			if ( b.getLeftMargin() != border.getLeftMargin()  ||  b.getRightMargin() != border.getRightMargin()  ||  b.getTopMargin() != border.getTopMargin()  ||  b.getBottomMargin() != border.getBottomMargin() )
			{
				queueResize();
			}
			else
			{
				queueFullRedraw();
			}
			border = b;
		}
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		border.draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
	}
	

	
	protected void updateRequisitionX()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionX( child.refreshRequisitionX() );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
		layoutReqBox.borderX( border.getLeftMargin(), border.getRightMargin() );
	}

	protected void updateRequisitionY()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionY( child.refreshRequisitionY() );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
		layoutReqBox.borderY( border.getTopMargin(), border.getBottomMargin() );
	}

	
	
	
	protected void updateAllocationX()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			double prevWidth = child.layoutAllocBox.getAllocationX();
			double hborder = border.getLeftMargin() + border.getRightMargin();
			layoutAllocBox.allocateChildXAligned( child.layoutAllocBox, child.layoutReqBox, child.getAlignmentFlags(), border.getLeftMargin(), layoutAllocBox.getAllocationX() - hborder );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			LAllocV prevAllocV = child.layoutAllocBox.getAllocV();
			layoutAllocBox.allocateChildYAligned( child.layoutAllocBox, child.layoutReqBox, child.getAlignmentFlags(),
					border.getTopMargin(), layoutAllocBox.getAllocV().borderY( border.getTopMargin(), border.getBottomMargin() ) );
			child.refreshAllocationY( prevAllocV );
		}
	}
}
