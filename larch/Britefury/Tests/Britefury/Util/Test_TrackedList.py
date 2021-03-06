##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************

import unittest

from Britefury.Util.TrackedList import TrackedListProperty

from BritefuryJ.ChangeHistory import ChangeHistory, Trackable

import random



class _Value (object):
	def __init__(self, x):
		self.__change_history__ = None
		self.x = x
	
	
	def __get_trackable_contents__(self):
		return [ self.x ]
		
		
	def isTracked(self):
		return self.__change_history__ is not None
		
		
	def __eq__(self, x):
		return isinstance( x, _Value )  and  self.x == x.x
	
	def __str__(self):
		return 'Value( %s )'  %  str( self.x )
	
	def __repr__(self):
		return 'Value( %s )'  %  str( self.x )
	
	def __cmp__(self, x):
		return cmp( self.x, x.x )


class _Sequence (object):
	def __init__(self):
		self.__change_history__ = None
		self._xs_ = []
	
	
	def __get_trackable_contents__(self):
		return self.xs.__get_trackable_contents__()
	
	
	@TrackedListProperty
	def xs(self):
		return self._xs_



class Test_TrackedList (unittest.TestCase):
	def setUp(self):
		self.history = ChangeHistory()
		self.s = _Sequence()
		self.history.track( self.s )
	
	def tearDown(self):
		self.history.stopTracking( self.s )
		self.s = None
		self.history = None
	
	
	def test_setitem(self):
		self.assertEqual( self.s.xs[:], [] )
		
		_two = _Value( -2 )
		self.s.xs.append( _two )
		self.assertEqual( self.s.xs[:], [ _two ] )
		self.assertTrue( _two.isTracked() )
		
		_one = _Value( -1 )
		self.s.xs[0] = _one
		self.assertEqual( self.s.xs[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.s.xs[1:] = _rng
		self.assertEqual( self.s.xs[:], [ _one ] + _rng )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertTrue( x.isTracked() )
		
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertFalse( x.isTracked() )
		
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], [ _two ] )
		self.assertFalse( _one.isTracked() )
		self.assertTrue( _two.isTracked() )
		
		
		self.history.redo()
		self.assertEqual( self.s.xs[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertFalse( x.isTracked() )

			
		self.history.redo()
		self.assertEqual( self.s.xs[:], [ _one ] + _rng )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertTrue( x.isTracked() )
		
			
			
		
	def test_delitem(self):
		self.assertEqual( self.s.xs[:], [] )
		
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.s.xs[:] = _rng
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		
		
		del self.s.xs[0]
		self.assertEqual( self.s.xs[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )

		
		del self.s.xs[1:3]
		self.assertEqual( self.s.xs[:], [ _rng[1], _rng[4] ] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, False, False, True ] )
		
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )

		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		
		
		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )
		
		
		self.history.redo()
		self.assertEqual( self.s.xs[:], [ _rng[1], _rng[4] ] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, False, False, True ] )
			
			
		
	def test_append(self):
		self.assertEqual( self.s.xs[:], [] )
		
		v = _Value( 2 )
		self.s.xs.append( v )
		self.assertTrue( v.isTracked() )
		
		self.assertEqual( self.s.xs[:], [ _Value( 2 ) ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], [] )
		self.assertFalse( v.isTracked() )

		self.history.redo()
		self.assertEqual( self.s.xs[:], [ _Value( 2 ) ] )
		self.assertTrue( v.isTracked() )
			
			
		
	def test_extend(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.assertEqual( self.s.xs[:], [] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, False, False, False, False ] )
		
		self.s.xs.extend( _rng )
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], [] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, False, False, False, False ] )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		                 
	

	def test_insert(self):
		v = _Value( 20 )
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.s.xs[:] = _rng
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertFalse( v.isTracked() )

		self.s.xs.insert( 2, v )
		self.assertEqual( self.s.xs[:], _rng[:2] + [ v ] + _rng[2:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertTrue( v.isTracked() )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertFalse( v.isTracked() )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng[:2] + [ v ] + _rng[2:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertTrue( v.isTracked() )
		                 
	

	def test_pop(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.s.xs[:] = _rng
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		v = self.s.xs.pop()
		self.assertTrue( v is _rng[-1] )
		self.assertEqual( self.s.xs[:], _rng[:-1] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, False ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng[:-1] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, False ] )
		                 
	

	def test_remove(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.s.xs[:] = _rng
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.s.xs.remove( _Value( 2 ) )
		self.assertEqual( self.s.xs[:], _rng[:2] + _rng[3:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, False, True, True ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng[:2] + _rng[3:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, False, True, True ] )
		                 
	

	def test_reverse(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		_rev = _rng[:]
		_rev.reverse()

		self.s.xs[:] = _rng
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.s.xs.reverse()
		self.assertEqual( self.s.xs[:], _rev )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rev )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		                 
	

	def test_sort(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		
		_shuf = _rng[:]
		r = random.Random( 12345 )
		r.shuffle( _shuf )

		self.s.xs[:] = _shuf
		self.assertEqual( self.s.xs[:], _shuf )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.s.xs.sort()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		
		self.history.undo()
		self.assertEqual( self.s.xs[:], _shuf )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )

		self.history.redo()
		self.assertEqual( self.s.xs[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
			
			
		
		