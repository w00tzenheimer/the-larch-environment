//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class InnerFragment extends Pres
{
	private Object model;
	private SimpleAttributeTable inheritedState;
	
	
	public InnerFragment(Object model)
	{
		this.model = model;
		this.inheritedState = null;
	}
	
	public InnerFragment(Object model, SimpleAttributeTable inheritedState)
	{
		this.model = model;
		this.inheritedState = inheritedState;
	}
	
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		FragmentView fragment = ctx.getFragment();
		
		SimpleAttributeTable s = inheritedState != null  ?  ctx.getInheritedState().withAttrs( inheritedState )  :  ctx.getInheritedState();
		
		return fragment.presentInnerFragment( model, ctx.getPerspective(), style, s );
	}
	
	
	
	public static InnerFragment[] map(Object models[])
	{
		InnerFragment fragments[] = new InnerFragment[models.length];
		for (int i = 0; i < models.length; i++)
		{
			fragments[i] = new InnerFragment( models[i] );
		}
		return fragments;
	}

	public static List<InnerFragment> map(List<Object> models)
	{
		ArrayList<InnerFragment> fragments = new ArrayList<InnerFragment>();
		fragments.ensureCapacity( models.size() );
		for (Object model: models)
		{
			fragments.add( new InnerFragment( model ) );
		}
		return fragments;
	}

	
	public static InnerFragment[] map(Object models[], SimpleAttributeTable inheritedState)
	{
		InnerFragment fragments[] = new InnerFragment[models.length];
		for (int i = 0; i < models.length; i++)
		{
			fragments[i] = new InnerFragment( models[i], inheritedState );
		}
		return fragments;
	}

	public static List<InnerFragment> map(List<Object> models, SimpleAttributeTable inheritedState)
	{
		ArrayList<InnerFragment> fragments = new ArrayList<InnerFragment>();
		fragments.ensureCapacity( models.size() );
		for (Object model: models)
		{
			fragments.add( new InnerFragment( model, inheritedState ) );
		}
		return fragments;
	}
}
