package Britefury.Math;

import Britefury.Math.Point2;

public class AABox2 implements Cloneable {
	private Point2 lower, upper;
	
	
	public AABox2()
	{
		lower = new Point2( 1.0, 1.0 );
		upper = new Point2( -1.0, -1.0 );
	}
	
	public AABox2(Point2 a)
	{
		lower = a.clone();
		upper = lower;
	}
	
	public AABox2(Point2 a, Point2 b)
	{
		lower = new Point2( a.x < b.x  ?  a.x  :  b.x,  a.y < b.x  ?  a.y  :  b.y );
		upper = new Point2( a.x > b.x  ?  a.x  :  b.x,  a.y > b.x  ?  a.y  :  b.y );
	}
	
	
	
	public boolean containsPoint(Point2 p)
	{
		return p.x >= lower.x  &&  p.x <= upper.x  &&  p.y >= lower.y  &&  p.y <= upper.y;
	}
	
	public boolean intersects(AABox2 b)
	{
		return b.upper.x >= lower.x  &&  b.upper.y >= lower.y   &&   b.lower.x <= upper.x  &&  b.lower.y <= upper.y;
	}
	
	
	public Point2 getLower()
	{
		return lower;
	}
	
	public Point2 getUpper()
	{
		return upper;
	}
	
}