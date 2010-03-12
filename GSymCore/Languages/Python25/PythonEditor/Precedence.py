##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction, Transformation, TransformationFunction

from BritefuryJ.DocModel import DMObjectInterface, DMListInterface

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, ObjectNodeDispatchMethod, objectNodeMethodDispatch
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Util.NodeUtil import isStringNode

from GSymCore.Languages.Python25 import NodeClasses as Nodes


PRECEDENCE_NONE = None
PRECEDENCE_UNPARSED = None
PRECEDENCE_COMMENT = None
PRECEDENCE_STMT = 1000
PRECEDENCE_EXPR = 0
PRECEDENCE_TARGET = None


PRECEDENCE_CONDITIONAL = 100

PRECEDENCE_LAMBDAEXPR = 50

PRECEDENCE_OR = 14
PRECEDENCE_AND = 13
PRECEDENCE_NOT = 12
PRECEDENCE_CMP = 9
PRECEDENCE_BITOR = 8
PRECEDENCE_BITXOR = 7
PRECEDENCE_BITAND = 6
PRECEDENCE_SHIFT = 5
PRECEDENCE_ADDSUB = 4
PRECEDENCE_MULDIVMOD = 3
PRECEDENCE_INVERT_NEGATE_POS = 2
PRECEDENCE_POW = 1
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_ATTR = 0

PRECEDENCE_LITERALVALUE = 0
PRECEDENCE_LOAD = 0
PRECEDENCE_TUPLE = 0
PRECEDENCE_LISTDISPLAY = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_DICTDISPLAY = 0
PRECEDENCE_YIELDEXPR = 200

PRECEDENCE_CONTAINER_ELEMENT = 500
PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTINDEX = 500
PRECEDENCE_CONTAINER_CALLTARGET = 0
PRECEDENCE_CONTAINER_CALLARG = 500

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_YIELDEXPR = 199

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_LAMBDAEXPR = 50

# The condition and true-expression subtrees of a conditional expression should only expect orTest
PRECEDENCE_CONTAINER_CONDITIONALEXPR = 25

# The comprehension for statements should only expect orTest
PRECEDENCE_CONTAINER_COMPREHENSIONFOR = 25

# The comprehension if statements should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_COMPREHENSIONIF = 50


PRECEDENCE_CONTAINER_UNPARSED = None


PRECEDENCE_IMPORTCONTENT = 0





class NodePrecedence (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 0
	
	
	def __call__(self, node):
		return objectNodeMethodDispatch( self, node )
	
	@ObjectNodeDispatchMethod( Nodes.CommentStmt )
	def CommentStmt(self, node):
		return PRECEDENCE_COMMENT
	
	@ObjectNodeDispatchMethod( Nodes.BlankLine )
	def BlankLine(self, node):
		return PRECEDENCE_COMMENT
	
	@ObjectNodeDispatchMethod( Nodes.UNPARSED )
	def UNPARSED(self, node):
		return PRECEDENCE_UNPARSED
	
	
	
	@ObjectNodeDispatchMethod( Nodes.Stmt )
	def Stmt(self, node):
		return PRECEDENCE_STMT
	
	@ObjectNodeDispatchMethod( Nodes.Expr )
	def Expr(self, node):
		return PRECEDENCE_EXPR
	
	@ObjectNodeDispatchMethod( Nodes.Pow )
	def Pow(self, node):
		return PRECEDENCE_POW
	
	@ObjectNodeDispatchMethod( Nodes.Invert )
	def Invert(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS
	
	@ObjectNodeDispatchMethod( Nodes.Negate )
	def Negate(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS

	@ObjectNodeDispatchMethod( Nodes.Pos )
	def Pos(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS
	
	@ObjectNodeDispatchMethod( Nodes.Mul )
	def Mul(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod( Nodes.Div )
	def Div(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod( Nodes.Mod )
	def Mod(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod( Nodes.Add )
	def Add(self, node):
		return PRECEDENCE_ADDSUB
	
	@ObjectNodeDispatchMethod( Nodes.Sub )
	def Sub(self, node):
		return PRECEDENCE_ADDSUB
	
	@ObjectNodeDispatchMethod( Nodes.LShift )
	def LShift(self, node):
		return PRECEDENCE_SHIFT
	
	@ObjectNodeDispatchMethod( Nodes.RShift )
	def RShift(self, node):
		return PRECEDENCE_SHIFT
	
	@ObjectNodeDispatchMethod( Nodes.BitAnd )
	def BitAnd(self, node):
		return PRECEDENCE_BITAND

	@ObjectNodeDispatchMethod( Nodes.BitXor )
	def BitXor(self, node):
		return PRECEDENCE_BITXOR

	@ObjectNodeDispatchMethod( Nodes.BitOr )
	def BitOr(self, node):
		return PRECEDENCE_BITOR
	
	@ObjectNodeDispatchMethod( Nodes.Cmp )
	def Cmp(self, node):
		return PRECEDENCE_CMP
	
	@ObjectNodeDispatchMethod( Nodes.NotTest )
	def NotTest(self, node):
		return PRECEDENCE_NOT
	
	@ObjectNodeDispatchMethod( Nodes.AndTest )
	def AndTest(self, node):
		return PRECEDENCE_AND
	
	@ObjectNodeDispatchMethod( Nodes.OrTest )
	def OrTest(self, node):
		return PRECEDENCE_OR

	@ObjectNodeDispatchMethod( Nodes.LambdaExpr )
	def LambdaExpr(self, node):
		return PRECEDENCE_LAMBDAEXPR
	
	
	@ObjectNodeDispatchMethod( Nodes.ConditionalExpr )
	def ConditionalExpr(self, node):
		return PRECEDENCE_CONDITIONAL

	
	
class NodeRightAssociativity (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 0
	
	def __call__(self, node):
		return objectNodeMethodDispatch( self, node )
	
	@ObjectNodeDispatchMethod( Nodes.BinOp )
	def BinOp(self, node):
		return False
	
	@ObjectNodeDispatchMethod( Nodes.Pow )
	def Pow(self, node):
		return True


_identity = DefaultIdentityTransformationFunction()
_precedence = NodePrecedence()
_rightAssoc = NodeRightAssociativity()
def _updatedNodeCopy(node, xform, **fieldValues):
	newNode = _identity( node, xform )
	newNode.update( fieldValues )
	return newNode
		

	
def _areParensRequired(childNode, outerPrecedence):
	childPrec = _precedence( childNode )
	return childPrec is not None   and   outerPrecedence is not None   and   childPrec > outerPrecedence
	
def getNumParens(node):
	try:
		p = node['parens']
	except KeyError:
		print 'Attempted to get number of parens for %s'  %  node
		raise
	
	numParens = 0
	if p is not None   and   isStringNode( p ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	return numParens

def _decrementParens(node, xform):
	numParens = getNumParens( node )
	numParens -= 1
	numParens = max( numParens, 0 )
	p = str( numParens )   if numParens > 0   else   None
	return _updatedNodeCopy( node, xform, parens=p )


def _computeBinOpContainmentPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1
	
def _transformBinOp(node, xform):
	outerPrec = _precedence( node )
	bRightAssociative = _rightAssoc( node )
	
	xPrec, yPrec = _computeBinOpContainmentPrecedenceValues( outerPrec, bRightAssociative )
	x = node['x']
	y = node['y']
	bXParens = _areParensRequired( x, xPrec )
	bYParens = _areParensRequired( y, yPrec )
	if bXParens  or  bYParens:
		if bXParens:
			x = _decrementParens( x, xform )
		else:
			x = xform( x, xform )
		if bYParens:
			y = _decrementParens( y, xform )
		else:
			y = xform( y, xform )
		result = _updatedNodeCopy( node, xform, x=x, y=y )
		return result
	else:
		raise DispatchError
	

def _transformOp(node, xform, outerPrec, fieldName):
	value = node[fieldName]
	bParens = _areParensRequired( value, outerPrec )
	if bParens:
		value = _decrementParens( value, xform )
		result = _updatedNodeCopy( node, xform, **{ fieldName : value } )
		return result
	else:
		raise DispatchError


def _transformOpMulti(node, xform, outerPrec, fieldNames):
	values = [ node[fieldName]   for fieldName in fieldNames ]
	bParens = [ _areParensRequired( value, outerPrec )   for value in values ]
	bNoParens = True
	for bParen in bParens:
		if bParen:
			bNoParens = False
			break
	if bNoParens:
		raise DispatchError
	else:
		values = [ ( _decrementParens( value, xform )   if bParen   else   value )   for value, bParen in zip( values, bParens ) ]
		m = {}
		for fieldName, value in zip( fieldNames, values ):
			m[fieldName] = value
		return _updatedNodeCopy( node, xform, **m )

	
def _transformUnaryOp(node, xform):
	outerPrec = _precedence( node )
	return _transformOp( node, xform, outerPrec, 'x' )


def _transformCmpOp(node, xform):
	outerPrec = _precedence( node )
	return _transformOp( node, xform, outerPrec, 'y' )

	
def _transformCmp(node, xform):
	outerPrec = _precedence( node )
	return _transformOp( node, xform, outerPrec, 'x' )

	
	
class RemoveUnNeededParensXform (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 1
	
	
	def __call__(self, node, xform):
		try:
			return objectNodeMethodDispatch( self, node, xform )
		except DispatchError:
			return TransformationFunction.cannotApplyTransformationValue
	
	@ObjectNodeDispatchMethod( Nodes.ComprehensionFor )
	def ComprehensionFor(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_COMPREHENSIONFOR, 'source' )
		
	@ObjectNodeDispatchMethod( Nodes.ComprehensionIf )
	def ComprehensionIf(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_COMPREHENSIONIF, 'condition' )
	
	@ObjectNodeDispatchMethod( Nodes.AttributeRef )
	def AttributeRef(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Nodes.Subscript )
	def Subscript(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Nodes.Call )
	def Call(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_CALLTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Nodes.BinOp )
	def BinOp(self, xform, node):
		return _transformBinOp( node, xform )
	
	@ObjectNodeDispatchMethod( Nodes.UnaryOp )
	def UnaryOp(self, xform, node):
		return _transformUnaryOp( node, xform )
	
	@ObjectNodeDispatchMethod( Nodes.Cmp )
	def Cmp(self, xform, node):
		return _transformCmp( node, xform )
	
	@ObjectNodeDispatchMethod( Nodes.CmpOp )
	def CmpOp(self, xform, node):
		return _transformCmpOp( node, xform )
	
	@ObjectNodeDispatchMethod( Nodes.LambdaExpr )
	def LambdaExpr(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_LAMBDAEXPR, 'expr' )
	
	@ObjectNodeDispatchMethod( Nodes.ConditionalExpr )
	def ConditionalExpr(self, xform, node):
		return _transformOpMulti( node, xform, PRECEDENCE_CONTAINER_CONDITIONALEXPR, [ 'expr', 'condition' ] )

	
_removeParensXform = Transformation( _identity, [ RemoveUnNeededParensXform() ] )

def _removeUnNeededParensFromObject(node, outerPrecedence):
	if node.isInstanceOf( Nodes.SimpleStmt )  or  node.isInstanceOf( Nodes.CompountStmtHeader )  or  node.isInstanceOf( Nodes.Expr ):
		x = _removeParensXform( DMNode.coerce( node ) )
		bParens = _areParensRequired( x, outerPrecedence )
		if bParens:
			x = _decrementParens( x, None )
		return DMNode.coerce( x )
	else:
		return node
	
def removeUnNeededParens(node, outerPrecedence):
	if isinstance( node, DMObjectInterface ):
		return _removeUnNeededParensFromObject( node, outerPrecedence )
	elif isinstance( node, List ):
		return [ removeUnNeededParens( x, outerPrecedence )   for x in node ]
	else:
		raise TypeError, 'Cannot apply removeUnNeededParens to %s'  %  ( type( node ), )
	

	

	
import unittest
from BritefuryJ.DocModel import DMNode
from GSymCore.Languages.Python25.PythonEditor import Parser


class Test_Precedence (unittest.TestCase):
	def _parseStringTest(self, parser, input, expected):
		result = parser.parseStringChars( input )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
		self.assert_( result.isValid() )
		
		value = removeUnNeededParens( result.value, None )
		
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value

		bSame = value == expected
		if not bSame:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
		self.assert_( bSame )

		
		
	def setUp(self):
		self._parser = Parser.Python25Grammar()
	
	def tearDown(self):
		self._parser = None
		
		
	def test_Load(self):
		self._parseStringTest( self._parser.expression(), 'a', Nodes.Load( name='a' ) )
		self._parseStringTest( self._parser.expression(), '(a)', Nodes.Load( parens='1', name='a' ) )	
		
	def test_BinOp(self):
		self._parseStringTest( self._parser.expression(), '(a+b)', Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )	
		self._parseStringTest( self._parser.expression(), '(((a+b)))', Nodes.Add( parens='3', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )	
		self._parseStringTest( self._parser.expression(), '(a*b)+c', Nodes.Add( x=Nodes.Mul( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)*c', Nodes.Mul( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)/(a+b)', Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)/(c+d)+e', Nodes.Add( x=Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='c' ), y=Nodes.Load( name='d' ) ) ), y=Nodes.Load( name='e' ) ) )
		self._parseStringTest( self._parser.expression(), '((a+b)/(a+b)+c)*x', Nodes.Mul( x=Nodes.Add( x=Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Load( name='c' ) ), y=Nodes.Load( name='x' ) ) )
		self._parseStringTest( self._parser.expression(), '(a/b)*(c+d)', Nodes.Mul( x=Nodes.Div( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='c' ), y=Nodes.Load( name='d' ) ) ) )
		self._parseStringTest( self._parser.expression(), '-(a+b)', Nodes.Negate( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) )	
		self._parseStringTest( self._parser.expression(), '-(a/b)', Nodes.Negate( x=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) )	
		self._parseStringTest( self._parser.expression(), '-a*b', Nodes.Mul( x=Nodes.Negate( x=Nodes.Load( name='a' ) ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( self._parser.expression(), '-(a/b)*c', Nodes.Mul( x=Nodes.Negate( x=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '-(a/b)*(c+d)', Nodes.Mul( x=Nodes.Negate( x=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Add( x=Nodes.Load( name='c' ), y=Nodes.Load( name='d' ) ) ) )
		
		
	def test_LambdaConditional(self):
		self._parseStringTest( self._parser.expression(), 'lambda: (x if y else z)', Nodes.LambdaExpr( params=[], expr=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) ) )
		self._parseStringTest( self._parser.expression(), 'lambda: x if y else z', Nodes.LambdaExpr( params=[], expr=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) ) )
		self._parseStringTest( self._parser.expression(), '(lambda: x) if y else z', Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.LambdaExpr( params=[], expr=Nodes.Load( name='x' ) ), elseExpr=Nodes.Load( name='z' ) ) )
		self._parseStringTest( self._parser.expression(), 'x if (lambda: y) else z', Nodes.ConditionalExpr( condition=Nodes.LambdaExpr( params=[], expr=Nodes.Load( name='y' ) ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) )
		
	def test_Statement(self):
		self._parseStringTest( self._parser.singleLineStatement(), '((a+b)/(a+b)+c)*x\n', Nodes.ExprStmt( expr=Nodes.Mul( x=Nodes.Add( x=Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Load( name='c' ) ), y=Nodes.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'y=((a+b)/(a+b)+c)*x\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='y' ) ], value=Nodes.Mul( x=Nodes.Add( x=Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Load( name='c' ) ), y=Nodes.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'y=(((a+b))/(a+b)+c)*x\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='y' ) ], value=Nodes.Mul( x=Nodes.Add( x=Nodes.Div( x=Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ), y=Nodes.Load( name='c' ) ), y=Nodes.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'x=(a+b)/(c+d)+e\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='x' ) ], value=Nodes.Add( x=Nodes.Div( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Add( x=Nodes.Load( name='c' ), y=Nodes.Load( name='d' ) ) ), y=Nodes.Load( name='e' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'x=a/(b+c+d)\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='x' ) ], value=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Add( x=Nodes.Add( x=Nodes.Load( name='b' ), y=Nodes.Load( name='c' ) ), y=Nodes.Load( name='d' ) ) ) ) )
