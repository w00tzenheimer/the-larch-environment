##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string

from Britefury.Kernel.Abstract import abstractmethod




class PyCodeGenError (Exception):
	def __init__(self, dbgSrc):
		super( PyCodeGenError, self ).__init__()
		self.dbgSrc = dbgSrc
		
		
class PyInvalidNameError (PyCodeGenError):
	def __init__(self, dbgSrc, name):
		super( PyInvalidNameError, self ).__init__( dbgSrc )
		self.name = name

class PyInvalidVarNameError (PyInvalidNameError):
	pass


class PyInvalidAttrNameError (PyInvalidNameError):
	pass


class PyInvalidMethodNameError (PyInvalidNameError):
	pass


class PyInvalidFunctionNameError (PyInvalidNameError):
	pass


class PyInvalidArgNameError (PyInvalidNameError):
	pass


class PyInvalidUnaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidUnaryOperatorError, self ).__init__( dbgSrc )
		self.op = op


class PyInvalidBinaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidBinaryOperatorError, self ).__init__( dbgSrc )
		self.op = op


_callPrecedence = 1
_methodCallPrecedence = 1
_subscriptPrecedence = 2
_attributePrecedence = 3
		
		
_binaryOperatorPrecedenceTable = {
	'**' : 4,
	'*' : 7,
	'/' : 7,
	'%' : 7,
	'+' : 8,
	'-' : 8,
	'<<' : 9,
	'>>' : 9,
	'&' : 10,
	'^' : 11,
	'|' : 12,
	'<' : 13,
	'<=' : 13,
	'==' : 13,
	'!=' : 13,
	'>=' : 13,
	'>' : 13,
	'is' : 14,
	'in' : 15,
	'and' : 17,
	'or' : 18,
}

_unaryOperatorPrecedenceTable = {
	'~' : ( 5, False ),
	'-' : ( 6, False ),
	'not' : ( 16, True ),
}



_pyIdentifierChars = string.ascii_letters + string.digits + '_'
_pyIdentifierStartChars = string.ascii_letters + '_'

def _isPyIdentifier(i):
	if len( i ) < 1:
		return False
	if i[0] not in _pyIdentifierStartChars:
		return False
	for c in i[1:]:
		if c not in _pyIdentifierChars:
			return False
	return True

def _isPyDottedIdentifier(i):
	parts = i.split( '.' )
	for p in parts:
		if not _isPyIdentifier( p ):
			return False
	return True


def filterIdentifierForPy(identifier):
	result = ''
	for c in identifier:
		if _isPyIdentifier( result + c ):
			result += c
	return result




def _indent(pysrc):
	return [ '  ' + line   for line in pysrc ]

def _passBlock(pysrc):
	if len( pysrc ) == 0:
		return [ 'pass' ]
	else:
		return pysrc



class PyNode (object):
	def __init__(self, dbgSrc):
		self._dbgSrc = dbgSrc
		
		
	def error(self, exceptionClass, *args):
		raise exceptionClass( self._dbgSrc, *args )
		

	def __cmp__(self, x):
		if type( self )  is  type( x ):
			return self._o_compareWith( x )
		else:
			return 1
		
	def _o_compareWith(self, x):
		return 0
	
	
	
class PyStatement (PyNode):
	def compileAsStmt(self):
		return [ self.compileAsExpr() ]
	


class PyExpression (PyStatement):
	def compileAsExpr(self, outerPredecence=None):
		thisPrecedence = self._o_getPrecedence()
		src = self._o_compileAsExpr()
		if outerPredecence is not None  and  thisPrecedence is not None  and  outerPredecence <=thisPrecedence:
			return '(' + src + ')'
		else:
			return src
		
	@abstractmethod
	def _o_compileAsExpr(self):
		pass
	
	def _o_getPrecedence(self):
		return None
	


	
	
	
	
class PySrc (PyExpression):
	def __init__(self, src, dbgSrc=None):
		super( PySrc, self ).__init__( dbgSrc )
		self.src = src
	
	def _o_compileAsExpr(self):
		return self.src
		
	def _o_compareWith(self, x):
		return cmp( self.src, x.src )
	
	
	
	
class PyVar (PyExpression):
	def __init__(self, varName, dbgSrc=None):
		super( PyVar, self ).__init__( dbgSrc )
		if not _isPyDottedIdentifier( varName ):
			self.error( PyInvalidVarNameError, varName )
		self.varName = varName

	def _o_compileAsExpr(self):
		return self.varName
		
	def _o_compareWith(self, x):
		return cmp( self.varName, x.varName )
	
	
	
	
class PyLiteral (PyExpression):
	def __init__(self, value, dbgSrc=None):
		super( PyLiteral, self ).__init__( dbgSrc )
		self.value = value
		
	def _o_compileAsExpr(self):
		return self.value
		
	def _o_compareWith(self, x):
		return cmp( self.value, x.value )
	
	
	
	
class PyListLiteral (PyExpression):
	def __init__(self, subexps, dbgSrc=None):
		super( PyListLiteral, self ).__init__( dbgSrc )
		for e in subexps:
			assert isinstance( e, PyExpression )
		self.subexps = subexps
		
	def _o_compileAsExpr(self):
		return '[ ' + ', '.join( [ x.compileAsExpr()   for x in self.subexps ] )  +  ' ]'
		
	def _o_compareWith(self, x):
		return cmp( self.subexps, x.subexps )
	
	
	
	
class PyListComprehension (PyExpression):
	def __init__(self, itemExpr, itemName, srcIterableExpr, filterExpr=None, dbgSrc=None):
		super( PyListComprehension, self ).__init__( dbgSrc )
		assert isinstance( itemExpr, PyExpression )
		assert isinstance( srcIterableExpr, PyExpression )
		assert isinstance( itemName, str )
		assert filterExpr is None  or  isinstance( filterExpr, PyExpression )

		self.itemExpr = itemExpr
		self.itemName = itemName
		self.srcIterableExpr = srcIterableExpr
		self.filterExpr = filterExpr
		
	def _o_compileAsExpr(self):
		if self.filterExpr is None:
			return '[ %s   for %s in %s ]'  %  ( self.itemExpr.compileAsExpr(), self.itemName, self.srcIterableExpr.compileAsExpr() )
		else:
			return '[ %s   for %s in %s   if %s ]'  %  ( self.itemExpr.compileAsExpr(), self.itemName, self.srcIterableExpr.compileAsExpr(), self.filterExpr.compileAsExpr() )
		
	def _o_compareWith(self, x):
		return cmp( ( self.itemExpr, self.itemName, self.srcIterableExpr, self.filterExpr ),  ( x.itemExpr, x.itemName, x.srcIterableExpr, x.filterExpr ) )
	
	
	
	
class PyGetAttr (PyExpression):
	def __init__(self, a, attrName, dbgSrc=None):
		super( PyGetAttr, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		if not _isPyIdentifier( attrName ):
			self.error( PyInvalidAttrNameError, attrName )
		self.a = a
		self.attrName = attrName

	def _o_compileAsExpr(self):
		return '%s.%s'  %  ( self.a.compileAsExpr( _attributePrecedence ), self.attrName )
	
	def _o_getPrecedence(self):
		return _attributePrecedence
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.attrName ),  ( x.a, x.attrName ) )
		
	
	

class PyGetItem (PyExpression):
	def __init__(self, a, start, end=None, dbgSrc=None):
		super( PyGetItem, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		assert isinstance( start, PyExpression )
		assert end is None  or  isinstance( end, PyExpression )
		self.a = a
		self.start = start
		self.end = end
		
	def _o_compileAsExpr(self):
		if self.end is not None:
			return '%s[%s:%s]'  %  ( self.a.compileAsExpr( _subscriptPrecedence ), self.start.compileAsExpr(), self.end.compileAsExpr() )
		else:
			return '%s[%s]'  %  ( self.a.compileAsExpr( _subscriptPrecedence ), self.start.compileAsExpr() )
	
	def _o_getPrecedence(self):
		return _subscriptPrecedence
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.start, self.end ),  ( x.a, x.start, x.end ) )

	
	
	
class PyUnOp (PyExpression):
	operators = _unaryOperatorPrecedenceTable.keys()
	
	def __init__(self, op, a, dbgSrc=None):
		super( PyUnOp, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		if op not in _unaryOperatorPrecedenceTable:
			self.error( PyInvalidUnaryOperatorError, op )
		self.op = op
		self.a = a
		
	def _o_compileAsExpr(self):
		thisPrecedence, bUseSpace = _unaryOperatorPrecedenceTable[self.op]
		space = ''
		if bUseSpace:
			space = ' '
		return '%s%s%s'  %  ( self.op, space, self.a.compileAsExpr( thisPrecedence ) )
	
	def _o_getPrecedence(self):
		return _unaryOperatorPrecedenceTable[self.op][0]

	def _o_compareWith(self, x):
		return cmp( ( self.a, self.op ),  ( x.a, x.op ) )

	
	
		
class PyBinOp (PyExpression):
	operators = _binaryOperatorPrecedenceTable.keys()
	
	def __init__(self, a, op, b, dbgSrc=None):
		super( PyBinOp, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		assert isinstance( b, PyExpression )
		if op not in _binaryOperatorPrecedenceTable:
			self.error( PyInvalidBinaryOperatorError, op )
		self.a = a
		self.op = op
		self.b = b
		
	def _o_compileAsExpr(self):
		thisPrecedence = _binaryOperatorPrecedenceTable[self.op]
		return '%s %s %s'  %  ( self.a.compileAsExpr( thisPrecedence ), self.op, self.b.compileAsExpr( thisPrecedence ) )
	
	def _o_getPrecedence(self):
		return _binaryOperatorPrecedenceTable[self.op]

	def _o_compareWith(self, x):
		return cmp( ( self.a, self.b, self.op ),  ( x.a, x.b, x.op ) )

	
	
	
class PyCall (PyExpression):
	def __init__(self, a, params, dbgSrc=None):
		super( PyCall, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		for p in params:
			assert isinstance( p, PyExpression )
		self.a = a
		self.params = params
		
	def _o_compileAsExpr(self):
		if len( self.params ) == 0:
			return '%s()'  %  ( self.a.compileAsExpr( _callPrecedence ), )
		else:
			return '%s( %s )'  %  ( self.a.compileAsExpr( _callPrecedence ), ', '.join( [ p.compileAsExpr()   for p in self.params ] ) )
		
	def _o_getPrecedence(self):
		return _callPrecedence
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.params ),  ( x.a, x.params ) )

	
	

class PyMethodCall (PyExpression):
	def __init__(self, a, methodName, params, dbgSrc=None):
		super( PyMethodCall, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		for p in params:
			assert isinstance( p, PyExpression )
		if not _isPyIdentifier( methodName ):
			self.error( PyInvalidMethodNameError, methodName )
		self.a = a
		self.methodName = methodName
		self.params = params
		
	def _o_compileAsExpr(self):
		if len( self.params ) == 0:
			return '%s.%s()'  %  ( self.a.compileAsExpr( _methodCallPrecedence ), self.methodName )
		else:
			return '%s.%s( %s )'  %  ( self.a.compileAsExpr( _methodCallPrecedence ), self.methodName, ', '.join( [ p.compileAsExpr()   for p in self.params ] ) )
	
	def _o_getPrecedence(self):
		return _methodCallPrecedence
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.methodName, self.params ),  ( x.a, x.methodName, x.params ) )


	
	
class PyReturn (PyStatement):
	def __init__(self, value, dbgSrc=None):
		super( PyReturn, self ).__init__( dbgSrc )
		assert isinstance( value, PyExpression )
		self.value =value
		
	def compileAsStmt(self):
		return [ 'return %s'  %  ( self.value.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return cmp( self.value, x.value )



class PyIf (PyStatement):
	def __init__(self, ifElifSpecs, elseStatements=None, dbgSrc=None):
		"""
		ifElifSpecs is a list of tuples
		each tuple is a (condition, statement-list) pair
		
		elseStatements is a list of statements for the else clause, or None if one is not desired
		"""
		super( PyIf, self ).__init__( dbgSrc )
		
		assert isinstance( ifElifSpecs, list ), 'PyIf: if-elif specs must be a list'
		assert len( ifElifSpecs ) >= 1, 'PyIf: if-elif specs must contain at least 1 element'
		if elseStatements is not None:
			assert isinstance( elseStatements, list ), 'PyIf: else statements must be a list'
		for i in ifElifSpecs:
			assert isinstance( i, tuple ), 'PyIf: if-specification must be a tuple (condition, [statement*]); not a tuple'
			assert len( i ) == 2, 'PyIf: if-specification must be a tuple (condition, [statement*]); length != 2'
			assert isinstance( i[0], PyExpression )
			assert isinstance( i[1], list ), 'PyIf: if-specification must be a tuple (condition, [statement*]); second element not a list'
			
		self.ifElifSpecs = ifElifSpecs
		self.elseStatements = elseStatements
		
		
	def compileAsStmt(self):
		ifSrc = []
		bElif = False
		
		for i in self.ifElifSpecs:
			ifSrc.extend( self._p_compileIfBlock( i, bElif ) )
			bElif = True
		
		if self.elseStatements is not None:
			ifSrc.extend( self._p_compileElseBlock( self.elseStatements ) )
			
		return ifSrc
				

	def _o_compareWith(self, x):
		return 1


	def _p_compileIfBlock(self, t, bElif):
		condition, statements = t
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		
		if bElif:
			ifKeyword = 'elif'
		else:
			ifKeyword = 'if'
			
		return [ '%s %s:'  %  ( ifKeyword, condition.compileAsExpr() ) ]  +  _indent( stmtSrc )
	

	def _p_compileElseBlock(self, statements):
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		return [ 'else:' ]  +  _indent( stmtSrc )

	
	
class PySimpleIf (PyIf):
	def __init__(self, condition, statements, dbgSrc=None):
		assert isinstance( condition, PyNode ), 'PySimpleIf condition must be a PyNode'
		assert isinstance( statements,list ), 'PySimpleIf statement list must be a list'
		super( PySimpleIf, self ).__init__( [ ( condition, statements ) ], None, dbgSrc=dbgSrc )



class PyDef (PyStatement):
	def __init__(self, name, argNames, statements, dbgSrc=None):
		super( PyDef, self ).__init__( dbgSrc )
		if not _isPyIdentifier( name ):
			self.error( PyInvalidFunctionNameError, name )
		for argName in argNames:
			if not _isPyIdentifier(argName ):
				self.error( PyInvalidArgNameError, name )
		self.name = name
		self.argNames = argNames
		self.statements = statements
		
	def compileAsStmt(self):
		stmtSrc = []
		
		for statement in self.statements:
			stmtSrc.extend( statement.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
	
		return [ 'def %s(%s):'  %  ( self.name, ', '.join( self.argNames ) ) ]  +  _indent( stmtSrc )
	
	def _o_compareWith(self, x):
		return 1


	
	
	
class PyAssign_SideEffects (PyStatement):
	def __init__(self, target, value, dbgSrc=None):
		super( PyAssign_SideEffects, self ).__init__( dbgSrc )
		assert isinstance( target, PyExpression )
		assert isinstance( value, PyExpression )
		self.target = target
		self.value =value
		
	def compileAsStmt(self):
		return [ '%s = %s'  %  ( self.target.compileAsExpr(), self.value.compileAsExpr() ) ]
	
	def _o_compareWith(self, x):
		return cmp( ( self.target, self.value ),  ( x.target, x.value ) )

	
	
	
class PyDel_SideEffects (PyStatement):
	def __init__(self, target, dbgSrc=None):
		super( PyDel_SideEffects, self ).__init__( dbgSrc )
		assert isinstance( target, PyExpression )
		self.target = target
		
	def compileAsStmt(self):
		return [ 'del %s'  %  ( self.target.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return cmp( self.target, x.target )



	
	


	
	
import unittest




class TestCase_PyCodeGen_IdentifierCheck (unittest.TestCase):
	def test_isPyIdentifier(self):
		self.assert_( _isPyIdentifier( 'abcXYZ123_' ) )
		self.assert_( _isPyIdentifier( '_abcXYZ123' ) )
		self.assert_( not _isPyIdentifier( '123abcXYZ123_' ) )
		self.assert_( not _isPyIdentifier( 'abcXYZ123_$' ) )
	
	def test_isPyDottedIdentifier(self):
		self.assert_( _isPyDottedIdentifier( 'abcXYZ123_' ) )
		self.assert_( _isPyDottedIdentifier( '_abcXYZ123' ) )
		self.assert_( _isPyDottedIdentifier( 'abcXY.Z123_' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcXYZ123_$' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcXYZ.123_' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcX..YZ123_' ) )
		self.assert_( not _isPyDottedIdentifier( '.abcXYZ123_' ) )
		

class TestCase_PyCodeGen_Block (unittest.TestCase):
	def test_indent(self):
		self.assert_( _indent( [ 'a' ] )  ==  [ '  a' ] )
		self.assert_( _indent( [ 'a', 'b' ] )  ==  [ '  a' , '  b' ] )
		
	def test_passBlock(self):
		self.assert_( _passBlock( [ 'a' ] )  ==  [ 'a' ] )
		self.assert_( _passBlock( [] )  ==  [ 'pass' ] )
		

class TestCase_PyCodeGen_Node_check (unittest.TestCase):
	def test_PyVar(self):
		self.assertRaises( PyInvalidVarNameError, lambda: PyVar( '$' ) )
		self.assertRaises( PyInvalidVarNameError, lambda: PyVar( 'a..b' ) )
		PyVar( 'a.b' )

	def test_PyGetAttr(self):
		self.assertRaises( PyInvalidAttrNameError, lambda: PyGetAttr( PySrc( 'a' ), '$test' ) )
		
	def test_PyUnOp(self):
		self.assertRaises( PyInvalidUnaryOperatorError, lambda: PyUnOp( '$', PySrc( 'a' ) ) )
		PyUnOp( '-', PySrc( 'a' ) )
		PyUnOp( '~', PySrc( 'a' ) )
		PyUnOp( 'not', PySrc( 'a' ) )
		
	def test_PyBinOp(self):
		self.assertRaises( PyInvalidBinaryOperatorError, lambda: PyBinOp( PySrc( 'a' ), '$', PySrc( 'b' ) ) )
		for op in _binaryOperatorPrecedenceTable.keys():
			PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) )

	def test_PyMethodCall(self):
		self.assertRaises( PyInvalidMethodNameError, lambda: PyMethodCall( PySrc( 'a' ), '$', [ PySrc( 'b' ), PySrc( 'c' ) ] ) )

	def test_PyIf(self):
		self.assertRaises( AssertionError, lambda: PyIf( 1 ) )
		self.assertRaises( AssertionError, lambda: PyIf( [],  ) )
		self.assertRaises( AssertionError, lambda: PyIf( [ ( PySrc( 'True' ), PySrc( 'pass' ) ) ], 1 ) )

		self.assertRaises( AssertionError, lambda: PyIf( [ 1 ] ) )
		self.assertRaises( AssertionError, lambda: PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ], PySrc( 'test' ) ) ] ) )
		
		PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ] ) ] )

	def test_PyDef(self):
		self.assertRaises( PyInvalidFunctionNameError, lambda: PyDef( 'a.b', [], [] ) )
		self.assertRaises( PyInvalidArgNameError, lambda: PyDef( 'a', [ 'b.c' ], [] ) )


		
class TestCase_PyCodeGen_Node_cmp (unittest.TestCase):
	def test_PySrc(self):
		self.assert_( PySrc( 'a' )  ==  PySrc( 'a' ) )
		self.assert_( PySrc( 'a' )  !=  PySrc( 'b' ) )
	
	def test_PyVar(self):
		self.assert_( PyVar( 'a' )  ==  PyVar( 'a' ) )
		self.assert_( PyVar( 'a' )  !=  PyVar( 'b' ) )

	def test_PyLiteral(self):
		self.assert_( PyLiteral( '1' )  ==  PyLiteral( '1' ) )
		self.assert_( PyLiteral( '1' )  !=  PyLiteral( '2' ) )
		
	def test_PyListLiteral(self):
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] )  ==  PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ) )
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] )  !=  PyListLiteral( [ PySrc( 'a' ), PySrc( 'c' ) ] ) )

	def test_PyListComprehension(self):
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None )  ==  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None )  !=  PyListComprehension( PySrc( 'b' ), 'a', PySrc( 'x' ), None ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None )  !=  PyListComprehension( PySrc( 'a' ), 'b', PySrc( 'x' ), None ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None )  !=  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'y' ), None ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) )  ==  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ),PySrc( 'True' ) ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ))  !=  PyListComprehension( PySrc( 'b' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) )  !=  PyListComprehension( PySrc( 'a' ), 'b', PySrc( 'x' ), PySrc( 'True' ) ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) )  !=  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'y' ), PySrc( 'True' ) ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) )  !=  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'False' ) ) )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) )  !=  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ) )
		
	def test_PyGetAttr(self):
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) ==  PyGetAttr( PySrc( 'a' ), 'test' ) )
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) !=  PyGetAttr( PySrc( 'b' ), 'test' ) )
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) !=  PyGetAttr( PySrc( 'a' ), 'foo' ) )

	def test_PyGetItem(self):
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) ==  PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) !=  PyGetItem( PySrc( 'b' ), PySrc( '1' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) !=  PyGetItem( PySrc( 'a' ), PySrc( '2' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) ==  PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) !=  PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) !=  PyGetItem( PySrc( 'b' ), PySrc( '1' ), PySrc( '2' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) !=  PyGetItem( PySrc( 'a' ), PySrc( '2' ), PySrc( '2' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) !=  PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '3' ) ) )

	def test_PyUnOp(self):
		self.assert_( PyUnOp( '-', PySrc( 'a' ) ) ==  PyUnOp( '-', PySrc( 'a' ) ) )
		self.assert_( PyUnOp( '-', PySrc( 'a' ) ) !=  PyUnOp( '-', PySrc( 'b' ) ) )
		self.assert_( PyUnOp( '-', PySrc( 'a' ) ) !=  PyUnOp( '~', PySrc( 'a' ) ) )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) )  ==  PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) ) )
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) )  ==  PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ) )
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) )  !=  PyBinOp( PySrc( 'x' ), '+', PySrc( 'b' ) ) )
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) )  !=  PyBinOp( PySrc( 'a' ), '+', PySrc( 'y' ) ) )
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) )  !=  PyBinOp( PySrc( 'a' ), '-', PySrc( 'b' ) ) )

	def test_PyCall(self):
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) ==  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'x' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'y' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'z' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'w' ) ] ) )

	def test_PyMethodCall(self):
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) ==  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'x' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'x', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'x' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'x' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'x' ) ] ) )
		
	def test_PyReturn(self):
		self.assert_( PyReturn( PySrc( '1' ) )  ==  PyReturn( PySrc( '1' ) ) )
		self.assert_( PyReturn( PySrc( '1' ) )  !=  PyReturn( PySrc( '2' ) ) )

	def test_PyIf(self):
		self.assert_( PyIf( [ ( PySrc( 'True' ), [ PySrc( '1' ) ] ) ] )  !=  PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ] ) ] ) )
		
	def test_PyDef(self):
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [] )  !=  PyDef( 'foo', [ 'a', 'b' ], [] ) )

	def test_PyAssign_SideEffects(self):
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) ==  PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) )
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) !=  PyAssign_SideEffects( PySrc( 'b' ), PySrc( '1' ) ) )
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) !=  PyAssign_SideEffects( PySrc( 'a' ), PySrc( '2' ) ) )

	def test_PyDel_SideEffects(self):
		self.assert_( PyDel_SideEffects( PySrc( 'a' ) ) ==  PyDel_SideEffects( PySrc( 'a' ) ) )
		self.assert_( PyDel_SideEffects( PySrc( 'a' ) ) !=  PyDel_SideEffects( PySrc( 'b' ) ) )


		
class TestCase_PyCodeGen_Node_compile (unittest.TestCase):
	def test_PySrc(self):
		self.assert_( PySrc( 'a' ).compileAsExpr()  ==  'a' )
	
	def test_PyVar(self):
		self.assert_( PyVar( 'a' ).compileAsExpr()  ==  'a' )

	def test_PyLiteral(self):
		self.assert_( PyLiteral( '1' ).compileAsExpr()  ==  '1' )

	def test_PyListLiteral(self):
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ).compileAsExpr()  ==  '[ a, b ]' )
		
	def test_PyListComprehension(self):
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ).compileAsExpr()  ==  '[ a   for a in x ]' )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ).compileAsExpr()  ==  '[ a   for a in x   if True ]' )
		
	def test_PyGetAttr(self):
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ).compileAsExpr()  ==  'a.test' )
		
	def test_PyGetItem(self):
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ).compileAsExpr()  ==  'a[1]' )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ).compileAsExpr()  ==  'a[1:2]' )
		
	def test_PyUnOp(self):
		self.assert_( PyUnOp( '-', PySrc( 'a' ) ).compileAsExpr()  ==  '-a' )
		self.assert_( PyUnOp( '~', PySrc( 'a' ) ).compileAsExpr()  ==  '~a' )
		self.assert_( PyUnOp( 'not', PySrc( 'a' ) ).compileAsExpr()  ==  'not a' )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) ).compileAsExpr()  ==  'a %s b'  %  ( op, ) )
		
	def test_PyCall(self):
		self.assert_( PyCall( PySrc( 'a' ), [] ).compileAsExpr()  ==  'a()' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ).compileAsExpr()  ==  'a( b, c )' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a( b, c, d )' )

	def test_PyMethodCall(self):
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [] ).compileAsExpr()  ==  'a.b()' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a.b( c, d )' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'e' ) ] ).compileAsExpr()  ==  'a.b( c, d, e )' )
		
	def test_PyReturn(self):
		self.assert_( PyReturn( PySrc( '1' ) ).compileAsStmt()  ==  [ 'return 1' ] )

	def test_PyIf(self):
		pysrc1 = [
			'if True:',
			'  pass'
		]

		pysrc2 = [
			'if True:',
			'  pass',
			'elif False:',
			'  pass'		
		]

		pysrc3 = [
			'if True:',
			'  pass',
			'elif False:',
			'  pass',	
			'else:',
			'  pass'		
		]

		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [] ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [] ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ],   [ PySrc( 'pass' ) ]  ).compileAsStmt()  ==  pysrc3 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ],   []  ).compileAsStmt()  ==  pysrc3 )

	def test_PySimpleIf(self):
		pysrc1 = [
			'if True:',
			'  pass'
		]

		self.assert_( PySimpleIf( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PySimpleIf( PySrc( 'True' ),  [] ).compileAsStmt()  ==  pysrc1 )

	def test_PyDef(self):
		pysrc1 = [
			'def foo():',
			'  pass'
		]

		pysrc2 = [
			'def foo(a, b):',
			'  pass'
		]
		
		self.assert_( PyDef( 'foo', [], [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyDef( 'foo', [], [] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [] ).compileAsStmt()  ==  pysrc2 )

	def test_PyAssign_SideEffects(self):
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ).compileAsStmt()  ==  [ 'a = 1' ] )

	def test_PyDel_SideEffects(self):
		self.assert_( PyDel_SideEffects( PySrc( 'a' ) ).compileAsStmt()  ==  [ 'del a' ] )
		
		
	def test_precedence(self):
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PyBinOp( PySrc( 'b' ), '+', PySrc( 'c' ) ) ).compileAsExpr()  ==  'a + (b + c)' )
		self.assert_( PyBinOp( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ), '+', PySrc( 'c' ) ).compileAsExpr()  ==  '(a + b) + c' )
		self.assert_( PyBinOp( PySrc( 'a' ), '*', PyBinOp( PySrc( 'b' ), '+', PySrc( 'c' ) ) ).compileAsExpr()  ==  'a * (b + c)' )
		self.assert_( PyBinOp( PyBinOp( PySrc( 'a' ), '*', PySrc( 'b' ) ), '+', PySrc( 'c' ) ).compileAsExpr()  ==  'a * b + c' )
		self.assert_( PyMethodCall( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ), 'upper', [] ).compileAsExpr()  ==  '(a + b).upper()' )
		


		
		
		
if __name__ == '__main__':
	unittest.main()