//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Coroutine;


public class Coroutine extends CoroutineBase
{
	private Runnable run = null;
	private boolean started, finished, terminated;
	private Thread thread = null;
	
	
	private Runnable myRun = new Runnable()
	{
		@Override
		public void run()
		{
			// Wait until this coroutine is yielded to
			halt();
			
			RuntimeException caughtException = null;
			
			// Do the work
			try
			{
				run.run();
			}
			catch (TerminateException e)
			{
				// Clear terminated flag
				terminated = false;
			}
			catch (RuntimeException e)
			{
				caughtException = e;
			}
			
			// We are done now - clear up
			finished = true;
			CoroutineBase.coFinish( Coroutine.this, caughtException );

			// null the thread - no longer running
			thread = null;
		}
	};
	
	
	public Coroutine(Runnable run, String name)
	{
		super( name, getCurrent() );
		this.run = run;
		started = finished = false;
		terminated = false;
	}

	public Coroutine(Runnable run)
	{
		this( run, "" );
	}


	
	
	@Override
	protected void initialise()
	{
		if ( thread == null )
		{
			thread = new Thread( myRun );
			thread.start();
			started = true;
		}
	}
	
	@Override
	protected Thread getThread()
	{
		return thread;
	}
	
	@Override
	protected boolean isRoot()
	{
		return false;
	}

	
	@Override
	public boolean hasStarted()
	{
		return started;
	}
	
	@Override
	public boolean isFinished()
	{
		return finished;
	}
	
	
	public void terminate()
	{
		if ( !isFinished()  &&  hasStarted() )
		{
			// Set the terminated flag and resume
			terminated = true;
			yieldTo();
		}
	}
	
	@Override
	protected boolean isTerminated()
	{
		return terminated;
	}
}
