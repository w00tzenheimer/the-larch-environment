//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import BritefuryJ.CommandHistory.Command;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.Trackable;

public class DMObjectCommandTracker extends CommandTracker
{
	private static class SetCommand extends Command
	{
		private DMObject obj;
		private int i;
		private Object oldX, x;
		
		public SetCommand(DMObject obj, int i, Object oldX, Object x)
		{
			this.obj = obj;
			this.i = i;
			this.oldX = oldX;
			this.x = x;
		}

		
		protected void execute()
		{
			obj.set( i, x );
		}

		protected void unexecute()
		{
			obj.set( i, oldX );
		}
	}

	
	
	private static class UpdateCommand extends Command
	{
		private DMObject obj;
		private int[] indices;
		private Object[] oldContents;
		private Object[] newContents;
		
		public UpdateCommand(DMObject obj, int[] indices, Object[] oldContents, Object[] newContents)
		{
			this.obj = obj;
			this.indices = indices;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}

		
		protected void execute()
		{
			obj.update( indices, newContents );
		}

		protected void unexecute()
		{
			obj.update( indices, oldContents );
		}
	}

	
	
	
	
	public DMObjectCommandTracker(CommandHistory commandHistory)
	{
		super( commandHistory );
	}
	
	
	
	protected void track(Trackable t)
	{
		super.track( t );
		
		DMObject obj = (DMObject)t;
		for (Object x: obj.getFieldValuesImmutable())
		{
			if ( x instanceof Trackable )
			{
				commandHistory.track( (Trackable)x );
			}
		}
	}
	
	protected void stopTracking(Trackable t)
	{
		DMObject obj = (DMObject)t;
		for (Object x: obj.getFieldValuesImmutable())
		{
			if ( x instanceof Trackable )
			{
				commandHistory.stopTracking( (Trackable)x );
			}
		}

		super.stopTracking( t );
	}

	
	
	protected void onSet(DMObject obj, int i, Object oldX, Object x)
	{
		if ( x instanceof Trackable )
		{
			commandHistory.track( (Trackable)x );
		}
		commandHistory.addCommand( new SetCommand( obj, i, oldX, x ) );
		if ( oldX instanceof Trackable )
		{
			commandHistory.stopTracking( (Trackable)oldX );
		}
	}

	protected void onUpdate(DMObject obj, int[] indices, Object[] oldContents, Object[] newContents)
	{
		for (Object x: newContents)
		{
			if ( x instanceof Trackable )
			{
				commandHistory.track( (Trackable)x );
			}
		}
		commandHistory.addCommand( new UpdateCommand( obj, indices, oldContents, newContents ) );
		for (Object oldX: oldContents)
		{
			if ( oldX instanceof Trackable )
			{
				commandHistory.stopTracking( (Trackable)oldX );
			}
		}
	}
}