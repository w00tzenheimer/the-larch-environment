package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

import BritefuryJ.DocPresent.DPVBox;

public class VBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static VBoxStyleSheet defaultStyleSheet = new VBoxStyleSheet();
	
	
	protected DPVBox.Typesetting typesetting;
	protected DPVBox.Alignment alignment;


	public VBoxStyleSheet()
	{
		this( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, 0.0, null );
	}
	
	public VBoxStyleSheet(Color backgroundColour)
	{
		this( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, 0.0, backgroundColour );
	}
	
	public VBoxStyleSheet(DPVBox.Typesetting typesetting, DPVBox.Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		this( typesetting, alignment, spacing, bExpand, padding, null );
	}
	
	public VBoxStyleSheet(DPVBox.Typesetting typesetting, DPVBox.Alignment alignment, double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, padding, backgroundColour );
		
		this.typesetting = typesetting;
		this.alignment = alignment;
	}

	
	
	public DPVBox.Typesetting getTypesetting()
	{
		return typesetting;
	}

	public DPVBox.Alignment getAlignment()
	{
		return alignment;
	}
}
