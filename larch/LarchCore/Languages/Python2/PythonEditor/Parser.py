##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************

import re
import string

from copy import deepcopy

from BritefuryJ.DocModel import DMObject, DMNode

from BritefuryJ.Parser import Action, Condition, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, SeparatedList, ObjectNode
from BritefuryJ.Parser.Utils.Tokens import identifier, decimalInteger, hexInteger, binInteger, oct0xInteger, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, ChainOperator, OperatorTable
from BritefuryJ.Util.RichString import RichStringBuilder

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

from LarchCore.Languages.Python2 import Schema
from LarchCore.Languages.Python2.PythonEditor import Keywords



#
#
#
# !!!!!! NOTES !!!!!!
# Octal integer starting within 0 are NOT handled at all. Octal integers starting with 0o will work
#
# from-import statements are parsed, but information on whether the imports were wrapped in parens, or had a trailing separator, is not obtained
#


def _incrementParens(node):
	def _xform(node, innerNodeXform):
		return node
	p = node['parens']
	numParens = 0
	if p is not None   and   ( isinstance( p, str )  or  isinstance( p, unicode ) ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	numParens += 1
	newNode = deepcopy( node )
	newNode['parens'] = str( numParens )
	return  newNode




class Python2Grammar (Grammar):
	__junk_regex__ = '[ ]*'

	#
	#
	# BASICS
	#
	#
	
	
	# Python identifier
	@Rule
	def pythonIdentifier(self):
		return identifier  &  ( lambda input, begin, end, x, bindings: x not in Keywords.nonIdentifierKeywordsSet )


	@Rule
	def dottedPythonIdentifer(self):
		return SeparatedList( self.pythonIdentifier(), '.', 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ).action( lambda input, begin, end, xs, bindings: '.'.join( xs ) )



	# Attribute name
	@Rule
	def attrName(self):
		return self.pythonIdentifier()





	#
	#
	# LITERALS
	#
	#
	
	
	
	
	# String literal
	def _stringLiteralformat(self, xs):
		flags = xs[:-1]
		if flags[0] == 'u'  or  flags[0] == 'U':
			format = 'unicode'
		elif flags[0] == 'b'  or  flags[0] == 'B':
			format = 'bytes'
		else:
			format = 'ascii'
		if flags[1] == 'r'  or  flags[1] == 'R':
			format += '-regex'
		return format



	@Rule
	def stringSLiteral(self):
		return ( ( Literal( 'u' ) | Literal( 'U' ) | Literal( 'b' ) | Literal( 'B' ) ).optional() + ( Literal( 'r' ) | Literal( 'R' ) ).optional()  +  singleQuotedString ).action(
			lambda input, begin, end, xs, bindings: Schema.StringLiteral( format=self._stringLiteralformat( xs ), quotation='single', value=xs[-1][1:-1] ) )

	@Rule
	def stringDLiteral(self):
		return ( ( Literal( 'u' ) | Literal( 'U' ) | Literal( 'b' ) | Literal( 'B' ) ).optional() + ( Literal( 'r' ) | Literal( 'R' ) ).optional()  +  doubleQuotedString ).action(
			lambda input, begin, end, xs, bindings: Schema.StringLiteral( format=self._stringLiteralformat( xs ), quotation='double', value=xs[-1][1:-1] ) )

	@Rule
	def shortStringLiteral(self):
		return self.stringSLiteral() | self.stringDLiteral()









	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return decimalInteger.action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='decimal', numType='int', value=xs ) )

	@Rule
	def decimalLongLiteral(self):
		return ( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='decimal', numType='long', value=xs[0] ) )

	@Rule
	def hexIntLiteral(self):
		return hexInteger.action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='hex', numType='int', value=xs[2:] ) )

	@Rule
	def hexLongLiteral(self):
		return ( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='hex', numType='long', value=xs[0][2:] ) )

	@Rule
	def binIntLiteral(self):
		return binInteger.action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='bin', numType='int', value=xs[2:] ) )

	@Rule
	def binLongLiteral(self):
		return ( binInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='bin', numType='long', value=xs[0][2:] ) )

	@Rule
	def octIntLiteral(self):
		return oct0xInteger.action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='oct', numType='int', value=xs[2:] ) )

	@Rule
	def octLongLiteral(self):
		return ( oct0xInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Schema.IntLiteral( format='oct', numType='long', value=xs[0][2:] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.binLongLiteral() | self.binIntLiteral() | self.octLongLiteral() | self.octIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()





	# Float literal
	@Rule
	def floatLiteral(self):
		return floatingPoint.action( lambda input, begin, end, xs, bindings: Schema.FloatLiteral( value=xs ) )




	# Imaginary literal
	@Rule
	def imaginaryLiteral(self):
		return Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ).action( lambda input, begin, end, xs, bindings: Schema.ImaginaryLiteral( value=xs ) )



	# Literal
	@Rule
	def literal(self):
		return self.shortStringLiteral() | self.imaginaryLiteral() | self.floatLiteral() | self.integerLiteral()


	
	
	
	#
	#
	# TARGETS (assignment, for-loop, etc)
	#
	#
	

	# Target (assignment, for-loop, ...)
	@Rule
	def singleTarget(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Schema.SingleTarget( name=xs ) )

	@Rule
	def tupleTarget(self):
		return SeparatedList( self.targetItem(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.TupleTarget( targets=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def targetListOrTargetItem(self):
		return self.tupleTarget()  |  self.targetItem()

	@Rule
	def parenTarget(self):
		return ( Literal( '(' )  +  self.targetListOrTargetItem()  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )

	@Rule
	def listTarget(self):
		return SeparatedList( self.targetItem(), '[', ']', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.ListTarget( targets=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def targetItem(self):
		return self.attributeRefOrSubscript()  |  self.parenTarget()  |  self.listTarget()  |  self.singleTarget()
		#return ( ( self.attributeRef()  ^  self.subscript() )  |  self.parenTarget()  |  self.listTarget()  |  self.singleTarget() )
		#return self.parenTarget()  |  self.listTarget()  |  self.singleTarget()




		
		
	#
	#
	# EXPRESSIONS
	#
	#
	

	# Load local variable
	@Rule
	def loadLocal(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Schema.Load( name=xs ) )



	# Tuples
	@Rule
	def tupleAsExpressionList(self):
		return SeparatedList( self.expression(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.TupleLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def tupleLiteral(self):
		return SeparatedList( self.expression(), '(', ')', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.TupleLiteral( values=xs, parens='1', trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def oldTupleAsExpressionList(self):
		return SeparatedList( self.oldExpression(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.TupleLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )



	# Parentheses
	@Rule
	def parenForm(self):
		return ( Literal( '(' ) + self.expression() + ')' ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )



	# List literal
	@Rule
	def listLiteral(self):
		return SeparatedList( self.expression(), '[', ']', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.ListLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )



	# List comprehension
	@Rule
	def listCompFor(self):
		return ( Keyword( Keywords.forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( Keywords.inKeyword )  +  self.oldTupleOrExpression() ).action(
			lambda input, begin, end, xs, bindings: Schema.ComprehensionFor( target=xs[1], source=xs[3] ) )

	@Rule
	def listCompIf(self):
		return ( Keyword( Keywords.ifKeyword )  +  self.oldExpression() ).action( lambda input, begin, end, xs, bindings: Schema.ComprehensionIf( condition=xs[1] ) )

	@Rule
	def listCompItem(self):
		return self.listCompFor() | self.listCompIf()

	@Rule
	def listComprehension(self):
		return ( Literal( '[' )  +  self.expression()  +  self.listCompFor()  +  ZeroOrMore( self.listCompItem() )  +  Literal( ']' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ListComp( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Generator expression
	@Rule
	def genExpFor(self):
		return ( Keyword( Keywords.forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( Keywords.inKeyword )  +  self.orTest() ).action( lambda input, begin, end, xs, bindings: Schema.ComprehensionFor( target=xs[1], source=xs[3] ) )

	@Rule
	def genExpIf(self):
		return ( Keyword( Keywords.ifKeyword )  +  self.oldExpression() ).action( lambda input, begin, end, xs, bindings: Schema.ComprehensionIf( condition=xs[1] ) )

	@Rule
	def genExpItem(self):
		return self.genExpFor() | self.genExpIf()

	@Rule
	def generatorExpression(self):
		return ( Literal( '(' )  +  self.expression()  +  self.genExpFor()  +  ZeroOrMore( self.genExpItem() )  +  Literal( ')' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.GeneratorExpr( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Dictionary literal
	@Rule
	def keyValuePair(self):
		return ( self.expression()  +  Literal( ':' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Schema.DictKeyValuePair( key=xs[0], value=xs[2] ) )

	@Rule
	def dictLiteral(self):
		return SeparatedList( self.keyValuePair(), '{', '}', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction(
			lambda input, begin, end, xs, bindings, bTrailingSep: Schema.DictLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )


	@Rule
	def dictComprehension(self):
		return ( Literal( '{' )  +  self.keyValuePair()  +  self.listCompFor()  +  ZeroOrMore( self.listCompItem() )  +  Literal( '}' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.DictComp( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )



	# Set literal
	@Rule
	def setLiteral(self):
		return SeparatedList( self.expression(), '{', '}', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction(
			lambda input, begin, end, xs, bindings, bTrailingSep: Schema.SetLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )


	@Rule
	def setComprehension(self):
		return ( Literal( '{' )  +  self.expression()  +  self.listCompFor()  +  ZeroOrMore( self.listCompItem() )  +  Literal( '}' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.SetComp( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Yield expression
	@Rule
	def yieldExpression(self):
		return ( Keyword( Keywords.yieldKeyword )  +  self.tupleOrExpression().optional() ).action( lambda input, begin, end, xs, bindings: Schema.YieldExpr( value=( xs[1]   if len( xs ) > 1   else None ) ) )


	@Rule
	def yieldAtom(self):
		return ( Literal( '(' )  +  self.yieldExpression()  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )



	# Enclosure
	@Rule
	def enclosure(self):
		return self.parenForm() | self.tupleLiteral() | self.listLiteral() | self.listComprehension() | self.generatorExpression() | self.dictLiteral() | self.dictComprehension() | self.setLiteral() | self.setComprehension() | self.yieldAtom()




	# Atom
	@Rule
	def atom(self):
		return ObjectNode( Schema.Expr )  |  self.enclosure() | self.literal() | self.loadLocal()




	# Attribute ref
	@Rule
	def attributeRef(self):
		return ( self.primary() + '.' + self.attrName() ).action( lambda input, begin, end, xs, bindings: Schema.AttributeRef( target=xs[0], name=xs[2] ) )




	# Subscript and slice
	@Rule
	def subscriptSlice(self):
		return ( Optional( self.expression() ) + ':' + Optional( self.expression() )  ).action( lambda input, begin, end, xs, bindings: Schema.SubscriptSlice( lower=xs[0], upper=xs[2] ) )

	@Rule
	def subscriptLongSlice(self):
		return ( ( Optional( self.expression() )  + ':' + Optional( self.expression() )  + ':' + Optional( self.expression() )  ).action( \
			lambda input, begin, end, xs, bindings: Schema.SubscriptLongSlice( lower=xs[0], upper=xs[2], stride=xs[4] ) ) )

	@Rule
	def subscriptEllipsis(self):
		return Literal( '...' ).action( lambda input, begin, end, xs, bindings: Schema.SubscriptEllipsis() )

	@Rule
	def subscriptItem(self):
		return self.subscriptLongSlice() | self.subscriptSlice() | self.subscriptEllipsis() | self.expression()

	@Rule
	def subscriptTuple(self):
		return SeparatedList( self.subscriptItem(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.SubscriptTuple( values=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def subscriptIndex(self):
		return self.subscriptTuple()  |  self.subscriptItem()

	@Rule
	def subscript(self):
		return ( self.primary() + '[' + self.subscriptIndex() + ']' ).action( lambda input, begin, end, xs, bindings: Schema.Subscript( target=xs[0], index=xs[2] ) )




	# Call
	def _checkCallArgs(self, input, begin, end, xs, bindings, bTrailingSep):
		bKW = False
		bArgList = False
		bKWArgList = False
		for x in xs:
			if isinstance( x, DMObject ):
				if x.isInstanceOf( Schema.CallKWArgList ):
					if bKWArgList:
						# Not after KW arg list (only 1 allowed)
						return False
					bKWArgList = True
					continue
				if x.isInstanceOf( Schema.CallArgList ):
					if bKWArgList | bArgList:
						# Not after KW arg list
						# Not after arg list (only 1 allowed)
						return False
					bArgList = True
					continue
				if x.isInstanceOf( Schema.CallKWArg ):
					if bKWArgList | bArgList:
						# Not after arg list or KW arg list
						return False
					bKW = True
					continue
			if bKWArgList | bArgList | bKW:
				# Not after KW arg list, or arg list, or KW arg
				return False
		return True

	@Rule
	def argName(self):
		return self.pythonIdentifier()

	@Rule
	def kwArg(self):
		return ( self.argName() + '=' + self.expression() ).action( lambda input, begin, end, xs, bindings: Schema.CallKWArg( name=xs[0], value=xs[2] ) )

	@Rule
	def argList(self):
		return ( Literal( '*' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Schema.CallArgList( value=xs[1] ) )

	@Rule
	def kwArgList(self):
		return ( Literal( '**' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Schema.CallKWArgList( value=xs[1] ) )

	@Rule
	def callArg(self):
		return self.kwArgList() | self.argList() | self.kwArg() | self.expression()

	@Rule
	def callArgs(self):
		# Result is ( list_of_args, bTrailingSep )
		return SeparatedList( self.callArg(), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( self._checkCallArgs ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )

	@Rule
	def call(self):
		return ( self.primary() + Literal( '(' ) + self.callArgs() + Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: Schema.Call( target=xs[0], args=xs[2][0], argsTrailingSeparator=xs[2][1] ) )



	# Primary
	@Rule
	def attributeRefOrSubscript(self):
		return self.attributeRef() | self.subscript()

	@Rule
	def primary(self):
		return self.call() | self.attributeRefOrSubscript() | self.atom()



	# Python operators
	@RuleList( [ 'powOp', 'invNegPosOp', 'mulDivModOp', 'addSubOp', 'lrShiftOp', 'andOP', 'xorOp', 'orOp', 'cmpOp', 'notTestOp', 'andTestOp', 'orTestOp' ] )
	def _operators(self):
		opTable = OperatorTable( 
			[
				InfixRightLevel( [ BinaryOperator( Literal( '**' ),  Schema.Pow, 'x', 'y' ) ] ),
				PrefixLevel( [ UnaryOperator( Literal( '~' ),  Schema.Invert, 'x' ),   UnaryOperator( Literal( '-' ),  Schema.Negate, 'x' ),   UnaryOperator( Literal( '+' ),  Schema.Pos, 'x' ) ], True ),
				InfixLeftLevel( [ BinaryOperator( Literal( '*' ),  Schema.Mul, 'x', 'y' ),   BinaryOperator( Literal( '/' ),  Schema.Div, 'x', 'y' ),   BinaryOperator( Literal( '%' ),  Schema.Mod, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '+' ),  Schema.Add, 'x', 'y' ),   BinaryOperator( Literal( '-' ),  Schema.Sub, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '<<' ),  Schema.LShift, 'x', 'y' ),   BinaryOperator( Literal( '>>' ),  Schema.RShift, 'x', 'y') ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '&' ),  Schema.BitAnd, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '^' ),  Schema.BitXor, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '|' ),  Schema.BitOr, 'x', 'y' ) ] ),
				InfixChainLevel( [
					ChainOperator( Literal( '<=' ),  Schema.CmpOpLte, 'y' ),
					ChainOperator( Literal( '<' ),  Schema.CmpOpLt, 'y' ),
					ChainOperator( Literal( '>=' ),  Schema.CmpOpGte, 'y' ),
					ChainOperator( Literal( '>' ),  Schema.CmpOpGt, 'y' ),
					ChainOperator( Literal( '==' ),  Schema.CmpOpEq, 'y' ),
					ChainOperator( Literal( '!=' ),  Schema.CmpOpNeq, 'y' ),
					ChainOperator( Keyword( Keywords.isKeyword ) + Keyword( Keywords.notKeyword ),  Schema.CmpOpIsNot, 'y' ),
					ChainOperator( Keyword( Keywords.isKeyword ),  Schema.CmpOpIs, 'y' ),
					ChainOperator( Keyword( Keywords.notKeyword ) + Keyword( Keywords.inKeyword ),  Schema.CmpOpNotIn, 'y' ),
					ChainOperator( Keyword( Keywords.inKeyword ),  Schema.CmpOpIn, 'y' ),
					],  Schema.Cmp, 'x', 'ops' ),
				PrefixLevel( [ UnaryOperator( Keyword( Keywords.notKeyword ),  Schema.NotTest, 'x' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Keyword( Keywords.andKeyword ),  Schema.AndTest, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Keyword( Keywords.orKeyword ),  Schema.OrTest, 'x', 'y' ) ] ),
				],  self.primary() )

		return opTable.buildParsers()



	@Rule
	def orOp(self):
		return self._operators()[7]


	@Rule
	def orTest(self):
		return self._operators()[-1]




	# Parameters (lambda, def statement, etc)
	def _checkParams(self, input, begin, end, xs, bindings, bTrailingSep):
		bDefaultValParam = False
		bParamList = False
		bKWParamList = False
		for x in xs:
			if isinstance( x, DMObject ):
				if x.isInstanceOf( Schema.KWParamList ):
					if bKWParamList:
						# Not after KW param list (only 1 allowed)
						return False
					bKWParamList = True
					continue
				elif x.isInstanceOf( Schema.ParamList ):
					if bKWParamList | bParamList:
						# Not after KW param list
						# Not after param list (only 1 allowed)
						return False
					bParamList = True
					continue
				elif x.isInstanceOf( Schema.DefaultValueParam ):
					if bKWParamList | bParamList:
						# Not after param list or KW param list
						return False
					bDefaultValParam = True
					continue
			if bKWParamList | bParamList | bDefaultValParam:
				# Not after KW param list, or param list, or default value param
				return False
		return True

	@Rule
	def paramName(self):
		return self.pythonIdentifier()

	@Rule
	def simpleParam(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Schema.SimpleParam( name=xs ) )

	@Rule
	def tupleParam(self):
		return SeparatedList( self.tupleParam() | self.simpleParam(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Schema.TupleParam( params=xs, paramsTrailingSeparator='1'   if bTrailingSep   else None ) )

	@Rule
	def defaultValueParam(self):
		return ( ( self.tupleParam() | self.simpleParam() ) + '=' + self.expression() ).action( lambda input, begin, end, xs, bindings: Schema.DefaultValueParam( param=xs[0], defaultValue=xs[2] ) )

	@Rule
	def paramList(self):
		return ( Literal( '*' )  +  self.paramName() ).action( lambda input, begin, end, xs, bindings: Schema.ParamList( name=xs[1] ) )

	@Rule
	def kwParamList(self):
		return ( Literal( '**' )  +  self.paramName() ).action( lambda input, begin, end, xs, bindings: Schema.KWParamList( name=xs[1] ) )

	@Rule
	def param(self):
		return self.kwParamList() | self.paramList() | self.defaultValueParam() | self.tupleParam() | self.simpleParam()

	@Rule
	def params(self):
		return SeparatedList( self.param(), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( self._checkParams ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )




	# Lambda expression_checkParams
	@Rule
	def oldLambdaExpr(self):
		return ( Keyword( Keywords.lambdaKeyword )  +  self.params()  +  Literal( ':' )  +  self.oldExpression() ).action(
			lambda input, begin, end, xs, bindings: Schema.LambdaExpr( params=xs[1][0], expr=xs[3], paramsTrailingSeparator=xs[1][1] ) )

	@Rule
	def lambdaExpr(self):
		return ( Keyword( Keywords.lambdaKeyword )  +  self.params()  +  Literal( ':' )  +  self.expression() ).action(
			lambda input, begin, end, xs, bindings: Schema.LambdaExpr( params=xs[1][0], expr=xs[3], paramsTrailingSeparator=xs[1][1] ) )




	# Conditional expression
	@Rule
	def conditionalExpression(self):
		return ( self.orTest()  +  Keyword( Keywords.ifKeyword )  +  self.orTest()  +  Keyword( Keywords.elseKeyword )  +  self.expression() ).action(
			lambda input, begin, end, xs, bindings: Schema.ConditionalExpr( condition=xs[2], expr=xs[0], elseExpr=xs[4] ) )



	# Expression and old expression (old expression is expression without conditional expression)
	@Rule
	def oldExpression(self):
		return self.oldLambdaExpr()  |  self.orTest()

	@Rule
	def expression(self):
		return self.lambdaExpr()  |  self.conditionalExpression()  |  self.orTest()



	# Tuple or (old) expression
	@Rule
	def tupleOrExpression(self):
		return self.tupleAsExpressionList()  |  self.expression()

	@Rule
	def oldTupleOrExpression(self):
		return self.oldTupleAsExpressionList()  |  self.oldExpression()
	
	
	@Rule
	def tupleOrExpressionOrYieldExpression(self):
		return self.tupleOrExpression()  |  self.yieldExpression()
	
	
	
	
	#
	#
	# UNPARSED
	#
	#
	
	@Rule
	def unparsed(self):
		return ObjectNode( Schema.UNPARSED )  |  ( ( ( RegEx( '[^\n]*' ) | ObjectNode( Schema.Expr ) ).oneOrMore()  +  Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.UnparsedStmt( value=Schema.UNPARSED( value=xs[0] ) ) ) )
	
	
	
	

	#
	#
	# SIMPLE STATEMENTS
	#
	#
	
	# Expression statement
	@Rule
	def exprStmt(self):
		return ( self.tupleOrExpression() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ExprStmt( expr=xs[0] ) )




	# Assert statement
	@Rule
	def assertStmt(self):
		return ( Keyword( Keywords.assertKeyword ) + self.expression()  +  Optional( Literal( ',' ) + self.expression() ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.AssertStmt( condition=xs[1], fail=xs[2][1]   if xs[2] is not None  else  None ) )




	# Assignment statement
	@Rule
	def assignmentStmt(self):
		return ( OneOrMore( ( self.targetListOrTargetItem()  +  '=' ).action( lambda input, begin, end, xs, bindings: xs[0] ) )  +  self.tupleOrExpressionOrYieldExpression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.AssignStmt( targets=xs[0], value=xs[1] ) )




	# Augmented assignment statement
	@Rule
	def augOp(self):
		return Choice( [ Literal( op )   for op in Keywords.augAssignOps ] )

	@Rule
	def augAssignStmt(self):
		return ( self.targetItem()  +  self.augOp()  +  self.tupleOrExpressionOrYieldExpression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.AugAssignStmt( op=xs[1], target=xs[0], value=xs[2] ) )




	# Pass statement
	@Rule
	def passStmt(self):
		return ( Keyword( Keywords.passKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.PassStmt() )



	# Del statement
	@Rule
	def delStmt(self):
		return ( Keyword( Keywords.delKeyword )  +  self.targetListOrTargetItem() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.DelStmt( target=xs[1] ) )



	# Return statement
	@Rule
	def returnStmt(self):
		return ( Keyword( Keywords.returnKeyword )  +  self.tupleOrExpression().optional() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ReturnStmt( value=( xs[1]   if len( xs ) > 1   else None ) ) )



	# Yield statement
	@Rule
	def yieldStmt(self):
		return ( Keyword( Keywords.yieldKeyword )  +  self.expression().optional() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.YieldStmt( value=( xs[1]   if len( xs ) > 1   else None ) ) )




	# Raise statement
	def _buildRaise(self, xs):
		excType = None
		excValue = None
		traceback = None
		if xs[1] is not None:
			excType = xs[1][0]
			if xs[1][1] is not None:
				excValue = xs[1][1][1]
				if xs[1][1][2] is not None:
					traceback = xs[1][1][2][1]
		return Schema.RaiseStmt( excType=excType, excValue=excValue, traceback=traceback )

	@Rule
	def raiseStmt(self):
		return ( Keyword( Keywords.raiseKeyword ) + Optional( self.expression() + Optional( Literal( ',' ) + self.expression() + Optional( Literal( ',' ) + self.expression() ) ) ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: self._buildRaise( xs ) )




	# Break statement
	@Rule
	def breakStmt(self):
		return ( Keyword( Keywords.breakKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.BreakStmt() )




	# Continue statement
	@Rule
	def continueStmt(self):
		return ( Keyword( Keywords.continueKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ContinueStmt() )




	# Import statement
	@Rule
	def _moduleIdentifier(self):
		return self.pythonIdentifier()

	# dotted name
	@Rule
	def moduleName(self):
		return SeparatedList( self._moduleIdentifier(), '.', 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ).action( lambda input, begin, end, xs, bindings: '.'.join( xs ) )


	# relative module name
	@Rule
	def _relModDotsModule(self):
		return ( ZeroOrMore( '.' ) + self.moduleName() ).action( lambda input, begin, end, xs, bindings: ''.join( xs[0] )  +  xs[1] )

	@Rule
	def _relModDots(self):
		return OneOrMore( '.' ).action( lambda input, begin, end, xs, bindings: ''.join( xs ) )

	@Rule
	def relativeModule(self):
		return ( self._relModDotsModule() | self._relModDots() ).action( lambda input, begin, end, xs, bindings: Schema.RelativeModule( name=xs ) )


	# ( <moduleName> 'as' <pythonIdentifier> )  |  <moduleName>
	@Rule
	def moduleImport(self):
		return ( self.moduleName() + Keyword( Keywords.asKeyword ) + self.pythonIdentifier() ).action( lambda input, begin, end, xs, bindings: Schema.ModuleImportAs( name=xs[0], asName=xs[2] ) )   |	\
		       self.moduleName().action( lambda input, begin, end, xs, bindings: Schema.ModuleImport( name=xs ) )


	# 'import' <separatedList( moduleImport )>
	@Rule
	def simpleImport(self):
		return ( Keyword( Keywords.importKeyword )  +  SeparatedList( self.moduleImport(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ImportStmt( modules=xs[1] ) )


	# ( <pythonIdentifier> 'as' <pythonIdentifier> )  |  <pythonIdentifier>
	@Rule
	def moduleContentImport(self):
		return ( self.pythonIdentifier() + Keyword( Keywords.asKeyword ) + self.pythonIdentifier() ).action( lambda input, begin, end, xs, bindings: Schema.ModuleContentImportAs( name=xs[0], asName=xs[2] ) )   |   \
		       self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Schema.ModuleContentImport( name=xs ) )


	# 'from' <relativeModule> 'import' ( <separatedList( moduleContentImport )>  |  ( '(' <separatedList( moduleContentImport )> ',' ')' )
	@Rule
	def fromImport(self):
		return ( Keyword( Keywords.fromKeyword ) + self.relativeModule() + Keyword( Keywords.importKeyword ) + \
			 (  \
				 SeparatedList( self.moduleContentImport(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER )  |  
				 SeparatedList( self.moduleContentImport(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL )\
				 ) + Literal( '\n' )  \
			 ).action( lambda input, begin, end, xs, bindings: Schema.FromImportStmt( module=xs[1], imports=xs[3] ) )


	# 'from' <relativeModule> 'import' '*'
	@Rule
	def fromImportAll(self):
		return ( Keyword( Keywords.fromKeyword ) + self.relativeModule() + Keyword( Keywords.importKeyword ) + '*' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.FromImportAllStmt( module=xs[1] ) )


	# Final :::
	@Rule
	def importStmt(self):
		return self.simpleImport() | self.fromImport() | self.fromImportAll()




	# Global statement
	@Rule
	def globalVar(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Schema.GlobalVar( name=xs ) )

	@Rule
	def globalStmt(self):
		return ( Keyword( Keywords.globalKeyword )  +  SeparatedList( self.globalVar(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.GlobalStmt( vars=xs[1] ) )





	# Exec statement
	@Rule
	def execCodeStmt(self):
		return ( Keyword( Keywords.execKeyword )  +  self.orOp() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ExecStmt( source=xs[1], locals=None, globals=None ) )

	@Rule
	def execCodeInGlobalsStmt(self):
		return ( Keyword( Keywords.execKeyword )  +  self.orOp()  +  Keyword( Keywords.inKeyword )  +  self.expression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ExecStmt( source=xs[1], globals=xs[3], locals=None ) )

	@Rule
	def execCodeInLocalsAndGlobalsStmt(self):
		return ( Keyword( Keywords.execKeyword )  +  self.orOp()  +  Keyword( Keywords.inKeyword )  +  self.expression()  +  ','  +  self.expression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ExecStmt( source=xs[1], globals=xs[3], locals=xs[5] ) )

	@Rule
	def execStmt(self):
		return self.execCodeInLocalsAndGlobalsStmt() | self.execCodeInGlobalsStmt() | self.execCodeStmt()
	
	
	
	
	# Print statement
	@Rule
	def printStmt(self):
		return ( Keyword( Keywords.printKeyword )  +  SeparatedList( self.expression(), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.PrintStmt( values=xs[1] ) )

	@Rule
	def redirectedPrintStmt(self):
		return ( Keyword( Keywords.printKeyword )  +  Literal( '>>' )  +  self.expression()  +  ( Literal( ',' )  +
		                                                                                        SeparatedList( self.expression(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ).optional() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.PrintStmt( destination=xs[2], values=xs[3][1]   if xs[3] is not None   else   [] ) )





	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#
		
	# If statement
	@Rule
	def ifStmtHeader(self):
		return ObjectNode( Schema.IfStmtHeader )  |  \
		       ( Keyword( Keywords.ifKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.IfStmtHeader( condition=xs[1] ) )



	# Elif statement
	@Rule
	def elifStmtHeader(self):
		return ObjectNode( Schema.ElifStmtHeader )  |  \
		       ( Keyword( Keywords.elifKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ElifStmtHeader( condition=xs[1] ) )



	# Else statement
	@Rule
	def elseStmtHeader(self):
		return ObjectNode( Schema.ElseStmtHeader )  |  \
		       ( Keyword( Keywords.elseKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ElseStmtHeader() )



	# While statement
	@Rule
	def whileStmtHeader(self):
		return ObjectNode( Schema.WhileStmtHeader )  |  \
		       ( Keyword( Keywords.whileKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.WhileStmtHeader( condition=xs[1] ) )



	# For statement
	@Rule
	def forStmtHeader(self):
		return ObjectNode( Schema.ForStmtHeader )  |  \
		       ( Keyword( Keywords.forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( Keywords.inKeyword )  +  self.tupleOrExpression()  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ForStmtHeader( target=xs[1], source=xs[3] ) )



	# Try statement
	@Rule
	def tryStmtHeader(self):
		return ObjectNode( Schema.TryStmtHeader )  |  \
		       ( Keyword( Keywords.tryKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.TryStmtHeader() )




	# Except statement
	@Rule
	def exceptAllStmtHeader(self):
		return ( Keyword( Keywords.exceptKeyword ) + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ExceptStmtHeader( exception=None, target=None ) )

	@Rule
	def exceptExcStmtHeader(self):
		return ( Keyword( Keywords.exceptKeyword )  +  self.expression() + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ExceptStmtHeader( exception=xs[1], target=None ) )

	@Rule
	def exceptExcIntoTargetStmtHeader(self):
		return ( Keyword( Keywords.exceptKeyword )  +  self.expression()  +  ( Literal( ',' ) | Keyword( Keywords.asKeyword ) )  +  self.targetItem() + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.ExceptStmtHeader( exception=xs[1], target=xs[3] ) )

	@Rule
	def exceptStmtHeader(self):
		return ObjectNode( Schema.ExceptStmtHeader )  |  \
		       self.exceptExcIntoTargetStmtHeader() | self.exceptExcStmtHeader() | self.exceptAllStmtHeader()




	# Finally statement
	@Rule
	def finallyStmtHeader(self):
		return ObjectNode( Schema.FinallyStmtHeader )  |  \
		       ( Keyword( Keywords.finallyKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.FinallyStmtHeader() )



	# With statement
	@Rule
	def withContext(self):
		return ( self.expression()  +  ( Keyword( Keywords.asKeyword )  +  self.targetItem() ).optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.WithContext( expr=xs[0], target=xs[1][1]   if xs[1] is not None   else None ) )

	@Rule
	def withStmtHeader(self):
		return ObjectNode( Schema.WithStmtHeader )  |  \
		       ( Keyword( Keywords.withKeyword )  + SeparatedList( self.withContext(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER )  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.WithStmtHeader( contexts=xs[1] ) )



	# Def statement
	@Rule
	def defStmtHeader(self):
		return ObjectNode( Schema.DefStmtHeader )  |  \
		       ( Keyword( Keywords.defKeyword )  +  self.pythonIdentifier()  +  '('  +  self.params()  +  ')'  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Schema.DefStmtHeader( name=xs[1], params=xs[3][0], paramsTrailingSeparator=xs[3][1] ) )



	# Decorator statement
	@Rule
	def decoStmtHeader(self):
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				args = xs[2][1][0]
				trailingSeparator = xs[2][1][1]
			else:
				args = None
				trailingSeparator = None
			return Schema.DecoStmtHeader( name=xs[1], args=args, argsTrailingSeparator=trailingSeparator )

		return ObjectNode( Schema.DecoStmtHeader )  |  \
		       ( Literal( '@' )  +  self.dottedPythonIdentifer()  +  Optional( Literal( '(' )  +  self.callArgs()  +  ')' ) + Literal( '\n' ) ).action( _action )



	# Class statement
	@Rule
	def classStmtHeader(self):
		bases = SeparatedList( self.expression(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				bases = xs[2][0]
				trailingSep = xs[2][1]
			else:
				bases = None
				trailingSep = None
			return Schema.ClassStmtHeader( name=xs[1], bases=bases, basesTrailingSeparator=trailingSep )
		return ObjectNode( Schema.ClassStmtHeader )  |  \
		       ( Keyword( Keywords.classKeyword )  +  self.pythonIdentifier()  +  Optional( bases )  +  ':' + Literal( '\n' ) ).action( _action )



	
	#
	#
	# COMMENT STATEMENT
	#
	#
	
	# Comment statement
	@Rule
	def commentStmt(self):
		return ObjectNode( Schema.CommentStmt )  |  \
		       ( RegEx( re.escape( '#' ) + '[' + re.escape( string.printable.replace( '\n', '' ) ) + ']*' ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.CommentStmt( comment=xs[0][1:] ) )
	
	
	@Rule
	def blankLine(self):
		return ObjectNode( Schema.BlankLine )  |  \
		       Literal( '\n' ).action( lambda input, begin, end, xs, bindings: Schema.BlankLine() )
	
	
	
	
	#
	#
	# COMPOUND STATEMENTS
	#
	#
	
	@Rule
	def ifStmt(self):
		byLine = ( self.ifStmtHeader()  +  self.compoundSuite()  +  self.elifBlock().zeroOrMore()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.IfStmt( condition=xs[0]['condition'], suite=xs[1], elifBlocks=xs[2], elseSuite=xs[3] ) )
		join = ( ObjectNode( Schema.IfStmt, elseSuite=None )  +  self.elifBlock().zeroOrMore()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.IfStmt( condition=xs[0]['condition'], suite=xs[0]['suite'], elifBlocks=list(xs[0]['elifBlocks']) + list(xs[1]), elseSuite=xs[2] ) )
		return byLine  |  join
		
	@Rule
	def elifBlock(self):
		return ( self.elifStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: Schema.ElifBlock( condition=xs[0]['condition'], suite=xs[1] ) )
	
	@Rule
	def elseBlock(self):
		return ( self.elseStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def whileStmt(self):
		byLine = ( self.whileStmtHeader()  +  self.compoundSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.WhileStmt( condition=xs[0]['condition'], suite=xs[1], elseSuite=xs[2] ) )
		join = ( ObjectNode( Schema.WhileStmt, elseSuite=None )  +  self.elseBlock() ).action(
			lambda input, begin, end, xs, bindings: Schema.WhileStmt( condition=xs[0]['condition'], suite=xs[0]['suite'], elseSuite=xs[1] ) )
		return byLine  |  join
		

	@Rule
	def forStmt(self):
		byLine = ( self.forStmtHeader()  +  self.compoundSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.ForStmt( target=xs[0]['target'], source=xs[0]['source'], suite=xs[1], elseSuite=xs[2] ) )
		join = ( ObjectNode( Schema.ForStmt, elseSuite=None )  +  self.elseBlock() ).action(
			lambda input, begin, end, xs, bindings: Schema.ForStmt( target=xs[0]['target'], source=xs[0]['source'], suite=xs[0]['suite'], elseSuite=xs[1] ) )
		return byLine  |  join
		
		
	@Rule
	def tryStmt(self):
		tryStmt1ByLine = ( self.tryStmtHeader()  +  self.compoundSuite()  +  self.exceptBlock().oneOrMore()  +  self.elseBlock().optional()  +  self.finallyBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.TryStmt( suite=xs[1], exceptBlocks=xs[2], elseSuite=xs[3], finallySuite=xs[4] ) )
		# No else or finally clause; add 1+ except blocks, and optionally, else and finally clauses
		tryStmt1JoinA = ( ObjectNode( Schema.TryStmt, elseSuite=None, finallySuite=None )  +  self.exceptBlock().oneOrMore()  +  self.elseBlock().optional()  +  self.finallyBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Schema.TryStmt( suite=xs[0]['suite'], exceptBlocks=list(xs[0]['exceptBlocks']) + list(xs[1]), elseSuite=xs[2], finallySuite=xs[3] ) )
		# 1 or more except blocks, no else or finally clause; add an else clause, and optionally, a finally clause
		tryStmt1JoinB = ( ObjectNode( Schema.TryStmt, elseSuite=None, finallySuite=None ).condition( lambda input, begin, end, xs, bindings: len( xs['exceptBlocks'] ) > 0 )  +  \
				  self.elseBlock()  +  self.finallyBlock().optional() ).action(
					  lambda input, begin, end, xs, bindings: Schema.TryStmt( suite=xs[0]['suite'], exceptBlocks=xs[0]['exceptBlocks'], elseSuite=xs[1], finallySuite=xs[2] ) )
		# 1 or more except blocks, no finally clause; add a finally clause
		tryStmt1JoinC = ( ObjectNode( Schema.TryStmt, finallySuite=None ).condition( lambda input, begin, end, xs, bindings: len( xs['exceptBlocks'] ) > 0 )  +  self.finallyBlock() ).action(
					  lambda input, begin, end, xs, bindings: Schema.TryStmt( suite=xs[0]['suite'], exceptBlocks=xs[0]['exceptBlocks'], finallySuite=xs[1] ) )
		tryStmt2ByLine = ( self.tryStmtHeader()  +  self.compoundSuite()  +  self.finallyBlock() ).action(
			lambda input, begin, end, xs, bindings: Schema.TryStmt( suite=xs[1], exceptBlocks=[], elseSuite=None, finallySuite=xs[2] ) )
		return tryStmt1ByLine | tryStmt1JoinA  |  tryStmt1JoinB  |  tryStmt1JoinC  |  tryStmt2ByLine
	
	@Rule
	def exceptBlock(self):
		return ( self.exceptStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: Schema.ExceptBlock( exception=xs[0]['exception'], target=xs[0]['target'], suite=xs[1] ) )
	
	@Rule
	def finallyBlock(self):
		return ( self.finallyStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def withStmt(self):
		return ( self.withStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Schema.WithStmt( contexts=xs[0]['contexts'], suite=xs[1] ) )
		

	@Rule
	def decorator(self):
		return self.decoStmtHeader().action( lambda input, begin, end, xs, bindings: Schema.Decorator( name=xs['name'], args=xs['args'], argsTrailingSeparator=xs['argsTrailingSeparator'] ) )
	
	@Rule
	def defStmt(self):
		byLine = ( self.decorator().zeroOrMore()  +  self.defStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Schema.DefStmt( decorators=xs[0], name=xs[1]['name'], params=xs[1]['params'], paramsTrailingSeparator=xs[1]['paramsTrailingSeparator'] , suite=xs[2] ) )
		join = ( self.decorator().oneOrMore()  +  ObjectNode( Schema.DefStmt ) ).action(
			lambda input, begin, end, xs, bindings: Schema.DefStmt( decorators=list(xs[0]) + list(xs[1]['decorators']), name=xs[1]['name'], params=xs[1]['params'], paramsTrailingSeparator=xs[1]['paramsTrailingSeparator'],
									       suite=xs[1]['suite'] ) )
		return byLine  |  join
	
	
	@Rule
	def classStmt(self):
		byLine = ( self.decorator().zeroOrMore()  +  self.classStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Schema.ClassStmt( decorators=xs[0], name=xs[1]['name'], bases=xs[1]['bases'], basesTrailingSeparator=xs[1]['basesTrailingSeparator'] , suite=xs[2] ) )
		join = ( self.decorator().oneOrMore()  +  ObjectNode( Schema.ClassStmt ) ).action(
			lambda input, begin, end, xs, bindings: Schema.ClassStmt( decorators=list(xs[0]) + list(xs[1]['decorators']), name=xs[1]['name'], bases=xs[1]['bases'], basesTrailingSeparator=xs[1]['basesTrailingSeparator'],
									       suite=xs[1]['suite'] ) )
		return byLine  |  join

	
	#return ( self.classStmtHeader()  +  self.compoundSuite() ).action(
			#lambda input, begin, end, xs, bindings: Schema.ClassStmt( name=xs[0]['name'], bases=xs[0]['bases'], basesTrailingSeparator=xs[0]['basesTrailingSeparator'] , suite=xs[1] ) )

	
	
	
	
	#
	#
	# STATEMENTS
	#
	#

	@Rule
	def simpleStmt(self):
		# Try to match print statement last; this way it can be used as a function
		return ObjectNode( Schema.SimpleStmt )  |  \
		       self.assertStmt() | self.assignmentStmt() | self.augAssignStmt() | self.passStmt() | self.delStmt() | self.returnStmt() | self.yieldStmt() | self.raiseStmt() | self.breakStmt() | \
		       self.continueStmt() | self.importStmt() | self.globalStmt() | self.execStmt() | self.redirectedPrintStmt() | self.exprStmt() | self.printStmt()

	@Rule
	def compoundStmtHeader(self):
		return ObjectNode( Schema.CompountStmtHeader )  |  \
		       self.ifStmtHeader() | self.elifStmtHeader() | self.elseStmtHeader() | self.whileStmtHeader() | self.forStmtHeader() | self.tryStmtHeader() | self.exceptStmtHeader() | self.finallyStmtHeader() | \
		       self.withStmtHeader() | self.defStmtHeader() | self.decoStmtHeader() | self.classStmtHeader()

	@Rule
	def compoundStmt(self):
		return self.ifStmt()  |  self.whileStmt()  |  self.forStmt()  |  self.tryStmt()  |  self.withStmt()  |  self.defStmt()  |  self.classStmt()  |  ObjectNode( Schema.CompoundStmt )
		       
	
	@Rule
	def singleLineStatementValid(self):
		return self.simpleStmt() | self.compoundStmtHeader() | self.commentStmt() | self.blankLine()

	@Rule
	def simpleSingleLineStatementValid(self):
		return self.simpleStmt() | self.commentStmt() | self.blankLine()

	@Rule
	def singleLineStatement(self):
		return self.singleLineStatementValid() | self.unparsed()


	
	
	#
	#
	# BLOCKS / STRUCTURE
	#
	#
	
	@Rule
	def indentedBlock(self):
		return self.compoundSuite().action( lambda input, begin, end, xs, bindings: Schema.IndentedBlock( suite=xs ) )
	
	
	@Rule
	def suiteItem(self):
		return self.commentStmt()  |  self.blankLine()  |  self.compoundStmt()  |  self.indentedBlock()  |  self.simpleStmt()  |  self.compoundStmtHeader()  |  self.emptyIndentation()  |  self.unparsed()
	
	
	@Rule
	def singleIndentedSuite(self):
		return ( ObjectNode( Schema.Indent )  +  self.suiteItem().oneOrMore()  +  ObjectNode( Schema.Dedent ) ).action( lambda input, begin, end, xs, bindings: xs[1] )  |  \
		       ObjectNode( Schema.IndentedBlock ).action( lambda input, begin, end, xs, bindings: xs['suite'] )
	
	@Rule
	def compoundSuite(self):
		return self.singleIndentedSuite().oneOrMore().action( lambda input, begin, end, xs, bindings: reduce( lambda a, b: list(a)+list(b), xs ) )

	
	@Rule
	def emptyIndentation(self):
		return ( self.emptyIndent() | self.emptyDedent() ).suppress()
	
	@Rule
	def emptyIndent(self):
		return ObjectNode( Schema.Indent )  +  self.emptyIndentation().optional() + ObjectNode( Schema.Dedent )
	
	@Rule
	def emptyDedent(self):
		return ObjectNode( Schema.Dedent )  +  self.emptyIndentation().optional() + ObjectNode( Schema.Indent )

	@Rule
	def suite(self):
		return self.suiteItem().zeroOrMore()









import unittest


class TestCase_Python2Parser (ParserTestCase):
	def _pythonRichString(self, *args):
		b = RichStringBuilder()
		for x in args:
			b.append( x )
		return b.richString()
	
	
	def setUp(self):
		#self.enableDebugView()
		pass
	
	
	def test_shortStringLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '\'abc\'', Schema.StringLiteral( format='ascii', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), '\"abc\"', Schema.StringLiteral( format='ascii', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'u\'abc\'', Schema.StringLiteral( format='unicode', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'u\"abc\"', Schema.StringLiteral( format='unicode', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'b\'abc\'', Schema.StringLiteral( format='bytes', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'b\"abc\"', Schema.StringLiteral( format='bytes', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'r\'abc\'', Schema.StringLiteral( format='ascii-regex', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'r\"abc\"', Schema.StringLiteral( format='ascii-regex', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'ur\'abc\'', Schema.StringLiteral( format='unicode-regex', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'ur\"abc\"', Schema.StringLiteral( format='unicode-regex', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'br\'abc\'', Schema.StringLiteral( format='bytes-regex', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'br\"abc\"', Schema.StringLiteral( format='bytes-regex', quotation='double', value='abc' ) )


	def test_integerLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '123', Schema.IntLiteral( format='decimal', numType='int', value='123' ) )
		self._parseStringTest( g.expression(), '123L', Schema.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringTest( g.expression(), '0x123', Schema.IntLiteral( format='hex', numType='int', value='123' ) )
		self._parseStringTest( g.expression(), '0x123L', Schema.IntLiteral( format='hex', numType='long', value='123' ) )
		self._parseStringTest( g.expression(), '0b101', Schema.IntLiteral( format='bin', numType='int', value='101' ) )
		self._parseStringTest( g.expression(), '0b101L', Schema.IntLiteral( format='bin', numType='long', value='101' ) )
		self._parseStringTest( g.expression(), '0o123', Schema.IntLiteral( format='oct', numType='int', value='123' ) )
		self._parseStringTest( g.expression(), '0o123L', Schema.IntLiteral( format='oct', numType='long', value='123' ) )


	def test_floatLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '123.0', Schema.FloatLiteral( value='123.0' ) )


	def test_imaginaryLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '123.0j', Schema.ImaginaryLiteral( value='123.0j' ) )


	def testTargets(self):
		g = Python2Grammar()
		self._parseStringTest( g.targetListOrTargetItem(), 'a', Schema.SingleTarget( name='a' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a)', Schema.SingleTarget( name='a', parens='1' ) )

		self._parseStringTest( g.targetListOrTargetItem(), '(a,)', Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a,b', Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b)', Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ], parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b,)', Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b),(c,d)', Schema.TupleTarget( targets=[ Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ], parens='1' ),
													 Schema.TupleTarget( targets=[ Schema.SingleTarget( name='c' ), Schema.SingleTarget( name='d' ) ], parens='1' ) ] ) )

		self._parseStringFailTest( g.targetListOrTargetItem(), '(a,) (b,)' )

		self._parseStringTest( g.targetListOrTargetItem(), '[a]', Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,]', Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,b]', Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,b,]', Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a],[b,]', Schema.TupleTarget( targets=[ Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ) ] ),
												      Schema.ListTarget( targets=[ Schema.SingleTarget( name='b' ) ], trailingSeparator='1' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[(a,)],[(b,)]', Schema.TupleTarget( targets=[ Schema.ListTarget( targets=[ Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) ] ),
													   Schema.ListTarget( targets=[ Schema.TupleTarget( targets=[ Schema.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) ] ) ] ) )

		self._parseStringTest( g.subscript(), 'a[x]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.attributeRefOrSubscript(), 'a[x]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.targetItem(), 'a[x]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x][y]', Schema.Subscript( target=Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ), index=Schema.Load( name='y' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a.b', Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a.b.c', Schema.AttributeRef( target=Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ), name='c' ) )

		self._parseStringTest( g.targetListOrTargetItem(), 'a.b[x]', Schema.Subscript( target=Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x].b', Schema.AttributeRef( target=Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ), name='b' ) )


	def testTupleLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '()', Schema.TupleLiteral( values=[], parens='1' ) )
		self._parseStringTest( g.expression(), '(())', Schema.TupleLiteral( values=[], parens='2' ) )
		self._parseStringTest( g.expression(), '(a)', Schema.Load( name='a', parens='1' ) )
		self._parseStringTest( g.expression(), '(a,)', Schema.TupleLiteral( values=[ Schema.Load( name='a' ) ], parens='1', trailingSeparator='1' ) )
		self._parseStringTest( g.expression(), '((a,))', Schema.TupleLiteral( values=[ Schema.Load( name='a' ) ], parens='2', trailingSeparator='1' ) )
		self._parseStringTest( g.expression(), '(a,b)', Schema.TupleLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ], parens='1' ) )
		self._parseStringTest( g.expression(), '(a,b,)', Schema.TupleLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ], parens='1', trailingSeparator='1' ) )


	def testListLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '[]', Schema.ListLiteral( values=[] ) )
		self._parseStringTest( g.expression(), '[a,b]', Schema.ListLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		self._parseStringTest( g.expression(), '[a,b,]', Schema.ListLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ], trailingSeparator='1' ) )


	def testListComprehension(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '[i  for i in a]', Schema.ListComp( resultExpr=Schema.Load( name='i' ),
										    comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ) ]
										    ) )
		self._parseStringFailTest( g.expression(), '[i  if x]', )
		self._parseStringTest( g.expression(), '[i  for i in a  if x]', Schema.ListComp( resultExpr=Schema.Load( name='i' ),
											  comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
													       Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ) ]
											  ) )
		self._parseStringTest( g.expression(), '[i  for i in a  for j in b]', Schema.ListComp( resultExpr=Schema.Load( name='i' ),
												comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
														     Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
												) )
		self._parseStringTest( g.expression(), '[i  for i in a  if x  for j in b]', Schema.ListComp( resultExpr=Schema.Load( name='i' ),
												      comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
															   Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ),
															   Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
												      ) )
		self._parseStringTest( g.expression(), '[i  for i in a  if x  for j in b  if y]', Schema.ListComp( resultExpr=Schema.Load( name='i' ),
													    comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
																 Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ),
																 Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ),
																 Schema.ComprehensionIf( condition=Schema.Load( name='y' ) ) ]
													    ) )



	def testGeneratorExpression(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '(i  for i in a)', Schema.GeneratorExpr( resultExpr=Schema.Load( name='i' ),
											 comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ) ]
											 ) )
		self._parseStringFailTest( g.expression(), '(i  if x)', )
		self._parseStringTest( g.expression(), '(i  for i in a  if x)', Schema.GeneratorExpr( resultExpr=Schema.Load( name='i' ),
											       comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
														    Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ) ]
											       ) )
		self._parseStringTest( g.expression(), '(i  for i in a  for j in b)', Schema.GeneratorExpr( resultExpr=Schema.Load( name='i' ),
												     comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
															  Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
												     ) )
		self._parseStringTest( g.expression(), '(i  for i in a  if x  for j in b)', Schema.GeneratorExpr( resultExpr=Schema.Load( name='i' ),
													   comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
																Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ),
																Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
													   ) )
		self._parseStringTest( g.expression(), '(i  for i in a  if x  for j in b  if y)', Schema.GeneratorExpr( resultExpr=Schema.Load( name='i' ),
														 comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
																      Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ),
																      Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ),
																      Schema.ComprehensionIf( condition=Schema.Load( name='y' ) ) ]
														 ) )


	def testDictLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '{}', Schema.DictLiteral( values=[] ) )
		self._parseStringTest( g.expression(), '{a:x,b:y}', Schema.DictLiteral( values=[ Schema.DictKeyValuePair( key=Schema.Load( name='a' ), value=Schema.Load( name='x' ) ),
											  Schema.DictKeyValuePair( key=Schema.Load( name='b' ), value=Schema.Load( name='y' ) ) ] ) )
		self._parseStringTest( g.expression(), '{a:x,b:y,}', Schema.DictLiteral( values=[ Schema.DictKeyValuePair( key=Schema.Load( name='a' ), value=Schema.Load( name='x' ) ),
											   Schema.DictKeyValuePair( key=Schema.Load( name='b' ), value=Schema.Load( name='y' ) ) ], trailingSeparator='1' ) )


	def tesDictComprehension(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '{i:i  for i in a}', Schema.DictComp( resultExpr=Schema.DictKeyValuePair( key=Schema.Load( name='i' ), value=Schema.Load( name='i' ) ),
		                                                                          comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ) ]
		) )
		self._parseStringFailTest( g.expression(), '{i:i  if x}', )
		self._parseStringTest( g.expression(), '{i:i  for i in a  if x}', Schema.DictComp( resultExpr=Schema.DictKeyValuePair( key=Schema.Load( name='i' ), value=Schema.Load( name='i' ) ),
		                                                                                comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
		                                                                                                     Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ) ]
		) )
		self._parseStringTest( g.expression(), '{i:i  for i in a  for j in b}', Schema.DictComp( resultExpr=Schema.DictKeyValuePair( key=Schema.Load( name='i' ), value=Schema.Load( name='i' ) ),
		                                                                                      comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
		                                                                                                           Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
		) )


	def testSetLiteral(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '{a,b}', Schema.SetLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		self._parseStringTest( g.expression(), '{a,b,}', Schema.SetLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.expression(), '{}', Schema.DictLiteral( values=[] ) )


	def testSetComprehension(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '{i  for i in a}', Schema.SetComp( resultExpr=Schema.Load( name='i' ),
		                                                                           comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ) ]
		) )
		self._parseStringFailTest( g.expression(), '{i  if x}', )
		self._parseStringTest( g.expression(), '{i  for i in a  if x}', Schema.SetComp( resultExpr=Schema.Load( name='i' ),
		                                                                                 comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
		                                                                                                      Schema.ComprehensionIf( condition=Schema.Load( name='x' ) ) ]
		) )
		self._parseStringTest( g.expression(), '{i  for i in a  for j in b}', Schema.SetComp( resultExpr=Schema.Load( name='i' ),
		                                                                                       comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='i' ), source=Schema.Load( name='a' ) ),
		                                                                                                            Schema.ComprehensionFor( target=Schema.SingleTarget( name='j' ), source=Schema.Load( name='b' ) ) ]
		) )


	def testYieldExpr(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '(yield 2+3)', Schema.YieldExpr( value=Schema.Add( x=Schema.IntLiteral( format='decimal', numType='int', value='2' ), y=Schema.IntLiteral( format='decimal', numType='int', value='3' ) ) ) )
		self._parseStringTest( g.expression(), '(yield)', Schema.YieldExpr( value=None ) )



	def testAttributeRef(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'a.b', Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ) )


	def testSubscript(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'a[x]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.expression(), 'a[x:p]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x:]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=Schema.Load( name='x' ), upper=None ) ) )
		self._parseStringTest( g.expression(), 'a[:p]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=None, upper=Schema.Load( name='p' ) ) ) )
		self._parseStringTest( g.expression(), 'a[:]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=None, upper=None ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ), stride=Schema.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ), stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[x::f]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=None, stride=Schema.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[:p:f]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=None, upper=Schema.Load( name='p' ), stride=Schema.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[::]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=None, upper=None, stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[::f]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=None, upper=None, stride=Schema.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x::]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=None, stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[:p:]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=None, upper=Schema.Load( name='p' ), stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[x,y]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptTuple( values=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p,y:q]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptTuple( values=[ Schema.SubscriptSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ) ), Schema.SubscriptSlice( lower=Schema.Load( name='y' ), upper=Schema.Load( name='q' ) ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f,y:q:g]', Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptTuple( values=[ Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ), stride=Schema.Load( name='f' ) ), Schema.SubscriptLongSlice( lower=Schema.Load( name='y' ), upper=Schema.Load( name='q' ), stride=Schema.Load( name='g' ) ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f,y:q:g,...]', Schema.Subscript( target=Schema.Load( name='a' ),
											index=Schema.SubscriptTuple( values=[ Schema.SubscriptLongSlice( lower=Schema.Load( name='x' ), upper=Schema.Load( name='p' ), stride=Schema.Load( name='f' ) ),
															     Schema.SubscriptLongSlice( lower=Schema.Load( name='y' ), upper=Schema.Load( name='q' ), stride=Schema.Load( name='g' ) ),
															     Schema.SubscriptEllipsis() ] ) ) )



	def testCall(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'a()', Schema.Call( target=Schema.Load( name='a' ), args=[] ) )
		self._parseStringTest( g.expression(), 'a(f)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ) ], argsTrailingSeparator='1' ) )
		self._parseStringTest( g.expression(), 'a(f,g)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.Load( name='g' ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.Load( name='g' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a,n=b)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.Load( name='g' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallKWArg( name='n', value=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a,n=b,*p)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.Load( name='g' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallKWArg( name='n', value=Schema.Load( name='b' ) ), Schema.CallArgList( value=Schema.Load( name='p' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,*p,**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallArgList( value=Schema.Load( name='p' ) ), Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,*p)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallArgList( value=Schema.Load( name='p' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,*p,**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallArgList( value=Schema.Load( name='p' ) ), Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(m=a,*p,**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.CallKWArg( name='m', value=Schema.Load( name='a' ) ), Schema.CallArgList( value=Schema.Load( name='p' ) ), Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(*p,**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.CallArgList( value=Schema.Load( name='p' ) ), Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(**w)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.CallKWArgList( value=Schema.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(**w+x)', Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.CallKWArgList( value=Schema.Add( x=Schema.Load( name='w' ), y=Schema.Load( name='x' ) ) ) ] ) )
		self._parseStringFailTest( g.expression(), 'a(m=a,f)' )
		self._parseStringFailTest( g.expression(), 'a(*p,f)' )
		self._parseStringFailTest( g.expression(), 'a(**w,f)' )
		self._parseStringFailTest( g.expression(), 'a(*p,m=a)' )
		self._parseStringFailTest( g.expression(), 'a(**w,m=a)' )
		self._parseStringFailTest( g.expression(), 'a(**w,*p)' )



	def testOperators(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'a**b', Schema.Pow( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), '~a', Schema.Invert( x=Schema.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), '-a', Schema.Negate( x=Schema.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), '+a', Schema.Pos( x=Schema.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), 'a*b', Schema.Mul( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a/b', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a%b', Schema.Mod( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a+b', Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a-b', Schema.Sub( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a<<b', Schema.LShift( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a>>b', Schema.RShift( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a&b', Schema.BitAnd( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a^b', Schema.BitXor( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a|b', Schema.BitOr( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a<=b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpLte( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a<b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpLt( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a>=b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpGte( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a>b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpGt( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a==b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpEq( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a!=b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpNeq( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a is not b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIsNot( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a is b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIs( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a not in b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpNotIn( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a in b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIn( y=Schema.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'not a', Schema.NotTest( x=Schema.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), 'a and b', Schema.AndTest( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a or b', Schema.OrTest( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )


	def testOperatorPrecedence(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'a + b < c', Schema.Cmp( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), ops=[ Schema.CmpOpLt( y=Schema.Load( name='c' ) ) ] ) )


	def testParens(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), '(a)', Schema.Load( name='a', parens='1' ) )
		self._parseStringTest( g.expression(), '(((a)))', Schema.Load( name='a', parens='3' ) )
		self._parseStringTest( g.expression(), '(a+b)', Schema.Add( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), '(a+b)*c', Schema.Mul( x=Schema.Add( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )


	def testParams(self):
		g = Python2Grammar()
		self._parseStringTest( g.params(), '', [ [], None ] )
		self._parseStringTest( g.params(), 'f', [ [ Schema.SimpleParam( name='f' ) ], None ] )
		self._parseStringTest( g.params(), 'f,', [ [ Schema.SimpleParam( name='f' ) ], '1' ] )
		self._parseStringTest( g.params(), 'f,g', [ [ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ) ], None ] )
		self._parseStringTest( g.params(), 'f,g,m=a', [ [ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ) ], None ] )
		self._parseStringTest( g.params(), 'f,g,m=a,n=b', [ [ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ),
								      Schema.DefaultValueParam( param=Schema.SimpleParam( name='n' ), defaultValue=Schema.Load( name='b' ) ) ], None ] )

		self._parseStringTest( g.params(), '(f)', [ [ Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ) ], paramsTrailingSeparator=None ) ], None ] )
		self._parseStringTest( g.params(), '(f,)', [ [ Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ) ], paramsTrailingSeparator='1' ) ], None ] )
		self._parseStringTest( g.params(), '(f,g)', [ [ Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ) ], paramsTrailingSeparator=None ) ], None ] )
		self._parseStringTest( g.params(), '(f,g,m)=a', [ [ Schema.DefaultValueParam( param=Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.SimpleParam( name='m' ) ],
												paramsTrailingSeparator=None ), defaultValue=Schema.Load( name='a' ) ) ], None ] )

		self._parseStringTest( g.params(), '(f,g), (h,i)', [ [ Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ) ], paramsTrailingSeparator=None ),
								       Schema.TupleParam( params=[ Schema.SimpleParam( name='h' ), Schema.SimpleParam( name='i' ) ], paramsTrailingSeparator=None )], None ] )
		self._parseStringTest( g.params(), '(f,g), (h,i)=a', [ [ Schema.TupleParam( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ) ], paramsTrailingSeparator=None ),
								       Schema.DefaultValueParam( param=Schema.TupleParam( params=[ Schema.SimpleParam( name='h' ), Schema.SimpleParam( name='i' ) ], paramsTrailingSeparator=None ),
											defaultValue=Schema.Load( name='a' ) ) ], None ] )

		self._parseStringTest( g.params(), 'f,g,m=a,n=b,*p', [ [ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ),
												Schema.DefaultValueParam( param=Schema.SimpleParam( name='n' ), defaultValue=Schema.Load( name='b' ) ), Schema.ParamList( name='p' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,*p,**w', [ [ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ),
																 Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,*p', [ [ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,**w', [ [ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ), Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'f,*p,**w', [ [ Schema.SimpleParam( name='f' ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'm=a,*p,**w', [ [ Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), '*p,**w', [ [ Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), '**w', [ [ Schema.KWParamList( name='w' ) ], None ] )
		self._parseStringFailTest( g.params(), 'm=a,f' )
		self._parseStringFailTest( g.params(), '*p,f' )
		self._parseStringFailTest( g.params(), '**w,f' )
		self._parseStringFailTest( g.params(), '*p,m=a' )
		self._parseStringFailTest( g.params(), '**w,m=a' )
		self._parseStringFailTest( g.params(), '**w,*p' )



	def testLambda(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'lambda f,m=a,*p,**w: f+m+p+w', Schema.LambdaExpr( 
			params=[ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( param=Schema.SimpleParam( name='m' ), defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ],
			expr=Schema.Add( x=Schema.Add( x=Schema.Add( x=Schema.Load( name='f' ), y=Schema.Load( name='m' ) ), y=Schema.Load( name='p' ) ), y=Schema.Load( name='w' ) ) ) )



	def testConditionalExpr(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'x   if y else   z', Schema.ConditionalExpr( condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ) )
		self._parseStringTest( g.expression(), '(x   if y else   z)   if w else   q', Schema.ConditionalExpr( condition=Schema.Load( name='w' ), expr=Schema.ConditionalExpr( parens='1', condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ), elseExpr=Schema.Load( name='q' ) ) )
		self._parseStringTest( g.expression(), 'w   if (x   if y else   z) else   q', Schema.ConditionalExpr( condition=Schema.ConditionalExpr( parens='1', condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ), expr=Schema.Load( name='w' ), elseExpr=Schema.Load( name='q' ) ) )
		self._parseStringTest( g.expression(), 'w   if q else   x   if y else   z', Schema.ConditionalExpr( condition=Schema.Load( name='q' ), expr=Schema.Load( name='w' ), elseExpr=Schema.ConditionalExpr( condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ) ) )
		self._parseStringFailTest( g.expression(), 'w   if x   if y else   z else   q' )



	def testTupleOrExpression(self):
		g = Python2Grammar()
		self._parseStringTest( g.tupleOrExpression(), 'a', Schema.Load( name='a' ) )
		self._parseStringTest( g.tupleOrExpression(), 'a,b', Schema.TupleLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		self._parseStringTest( g.tupleOrExpression(), 'a,2', Schema.TupleLiteral( values=[ Schema.Load( name='a' ), Schema.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )
		self._parseStringTest( g.tupleOrExpression(), 'lambda x, y: x+y,2', Schema.TupleLiteral(
			values=[ Schema.LambdaExpr( params=[ Schema.SimpleParam( name='x' ), Schema.SimpleParam( name='y' ) ],
						   expr=Schema.Add( x=Schema.Load( name='x' ), y=Schema.Load( name='y' ) ) ),
				 Schema.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )



	def test_structuralAtom(self):
		g = Python2Grammar()
		s = self._pythonRichString( Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseRichStringTest( g.atom(), s, Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )

		
		
	def test_embeddedStructuralExpression(self):
		g = Python2Grammar()
		s = self._pythonRichString( Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseRichStringTest( g.tupleOrExpression(), s, Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		s = self._pythonRichString( 'return ', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), '\n' )
		self._parseRichStringTest( g.singleLineStatement(), s, Schema.ReturnStmt( value=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )
		s = self._pythonRichString( 'x + ', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._parseRichStringTest( g.tupleOrExpression(), s, Schema.Add( x=Schema.Load( name='x' ), y=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )

		
		
	def testAssertStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'assert x\n', Schema.AssertStmt( condition=Schema.Load( name='x' ), fail=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'assert x,y\n', Schema.AssertStmt( condition=Schema.Load( name='x' ), fail=Schema.Load( name='y' ) ) )
		self._parseNodeTest( g.singleLineStatementValid(), Schema.AssertStmt( condition=Schema.Load( name='x' ), fail=None ), Schema.AssertStmt( condition=Schema.Load( name='x' ), fail=None ) )


	def testAssignmentStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'a=x\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ) ], value=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a=b=x\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ], value=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a,b=c,d=x\n', Schema.AssignStmt( targets=[ Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ),  Schema.SingleTarget( name='b' ) ] ),
											 Schema.TupleTarget( targets=[ Schema.SingleTarget( name='c' ),  Schema.SingleTarget( name='d' ) ] ) ], value=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a=(yield x)\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ) ], value=Schema.YieldExpr( value=Schema.Load( name='x' ) ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a = yield x\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ) ], value=Schema.YieldExpr( value=Schema.Load( name='x' ) ) ) )
		self._parseStringFailTest( g.singleLineStatementValid(), '=x' )


	def testAugAssignStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'a += b\n', Schema.AugAssignStmt( op='+=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a -= b\n', Schema.AugAssignStmt( op='-=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a *= b\n', Schema.AugAssignStmt( op='*=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a /= b\n', Schema.AugAssignStmt( op='/=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a %= b\n', Schema.AugAssignStmt( op='%=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a **= b\n', Schema.AugAssignStmt( op='**=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a >>= b\n', Schema.AugAssignStmt( op='>>=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a <<= b\n', Schema.AugAssignStmt( op='<<=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a &= b\n', Schema.AugAssignStmt( op='&=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a ^= b\n', Schema.AugAssignStmt( op='^=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a |= b\n', Schema.AugAssignStmt( op='|=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a += yield b\n', Schema.AugAssignStmt( op='+=', target=Schema.SingleTarget( name='a' ), value=Schema.YieldExpr( value=Schema.Load( name='b' ) ) ) )


	def testPassStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'pass\n', Schema.PassStmt() )


	def testDelStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'del x\n', Schema.DelStmt( target=Schema.SingleTarget( name='x' ) ) )


	def testReturnStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'return x\n', Schema.ReturnStmt( value=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'return\n', Schema.ReturnStmt() )


	def testYieldStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'yield x\n', Schema.YieldStmt( value=Schema.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'yield\n', Schema.YieldStmt() )


	def testRaiseStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'raise\n', Schema.RaiseStmt( excType=None, excValue=None, traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x\n', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=None, traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x,y\n', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=Schema.Load( name='y' ), traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x,y,z\n', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=Schema.Load( name='y' ), traceback=Schema.Load( name='z' ) ) )


	def testBreakStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'break\n', Schema.BreakStmt() )


	def testContinueStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'continue\n', Schema.ContinueStmt() )


	def testImportStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g._moduleIdentifier(), 'abc', 'abc' )
		self._parseStringTest( g.moduleName(), 'abc', 'abc' )
		self._parseStringTest( g.moduleName(), 'abc.xyz', 'abc.xyz' )
		self._parseStringTest( g._relModDotsModule(), 'abc.xyz', 'abc.xyz' )
		self._parseStringTest( g._relModDotsModule(), '...abc.xyz', '...abc.xyz' )
		self._parseStringTest( g._relModDots(), '...', '...' )
		self._parseStringTest( g.relativeModule(), 'abc.xyz', Schema.RelativeModule( name='abc.xyz' ) )
		self._parseStringTest( g.relativeModule(), '...abc.xyz', Schema.RelativeModule( name='...abc.xyz' ) )
		self._parseStringTest( g.relativeModule(), '...', Schema.RelativeModule( name='...' ) )
		self._parseStringTest( g.moduleImport(), 'abc.xyz', Schema.ModuleImport( name='abc.xyz' ) )
		self._parseStringTest( g.moduleImport(), 'abc.xyz as q', Schema.ModuleImportAs( name='abc.xyz', asName='q' ) )
		self._parseStringTest( g.simpleImport(), 'import a\n', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b\n', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a.b' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b as x\n', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b as x, c.d as y\n', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ), Schema.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._parseStringTest( g.moduleContentImport(), 'xyz', Schema.ModuleContentImport( name='xyz' ) )
		self._parseStringTest( g.moduleContentImport(), 'xyz as q', Schema.ModuleContentImportAs( name='xyz', asName='q' ) )
		self._parseStringTest( g.fromImport(), 'from x import a\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import a as p\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import a as p, b as q\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a,)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a as p)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a as p,)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import ( a as p, b as q )\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import ( a as p, b as q, )\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImportAll(), 'from x import *\n', Schema.FromImportAllStmt( module=Schema.RelativeModule( name='x' ) ) )
		self._parseStringTest( g.importStmt(), 'import a\n', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b\n', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a.b' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b as x\n', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b as x, c.d as y\n', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ), Schema.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a as p\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a as p, b as q\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a,)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a as p)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a as p,)\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import ( a as p, b as q )\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import ( a as p, b as q, )\n', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import *\n', Schema.FromImportAllStmt( module=Schema.RelativeModule( name='x' ) ) )
		self._parseStringTest( g.importStmt(), 'from ..x import *\n', Schema.FromImportAllStmt( module=Schema.RelativeModule( name='..x' ) ) )


	def testGlobalStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'global x\n', Schema.GlobalStmt( vars=[ Schema.GlobalVar( name='x' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'global x, y\n', Schema.GlobalStmt( vars=[ Schema.GlobalVar( name='x' ), Schema.GlobalVar( name='y' ) ] ) )


	def testExecStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'exec a\n', Schema.ExecStmt( source=Schema.Load( name='a' ), globals=None, locals=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'exec a in b\n', Schema.ExecStmt( source=Schema.Load( name='a' ), globals=Schema.Load( name='b' ), locals=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'exec a in b,c\n', Schema.ExecStmt( source=Schema.Load( name='a' ), globals=Schema.Load( name='b' ), locals=Schema.Load( name='c' ) ) )


		
	def testPrintStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'print a\n', Schema.PrintStmt( values=[ Schema.Load( name='a' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print a,\n', Schema.PrintStmt( values=[ Schema.Load( name='a' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print a, b\n', Schema.PrintStmt( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print >> x\n', Schema.PrintStmt( destination=Schema.Load( name='x' ), values=[] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print >> x, a\n', Schema.PrintStmt( destination=Schema.Load( name='x' ), values=[ Schema.Load( name='a' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print >> x, a,\n', Schema.PrintStmt( destination=Schema.Load( name='x' ), values=[ Schema.Load( name='a' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print >> x, a, b\n', Schema.PrintStmt( destination=Schema.Load( name='x' ), values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print\n', Schema.ExprStmt( expr=Schema.Load( name='print' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'print(a)\n', Schema.ExprStmt( expr=Schema.Call( target=Schema.Load( name='print' ), args=[ Schema.Load( name='a' ) ] ) ) )



	#
	# Compound statement headers
	#
		
	def testIfStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.ifStmtHeader(), 'if a:\n', Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.ifStmtHeader(), Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ), Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ), Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ) )


	def testElIfStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.elifStmtHeader(), 'elif a:\n', Schema.ElifStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.elifStmtHeader(), Schema.ElifStmtHeader( condition=Schema.Load( name='a' ) ), Schema.ElifStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.ElifStmtHeader( condition=Schema.Load( name='a' ) ), Schema.ElifStmtHeader( condition=Schema.Load( name='a' ) ) )


	def testElseStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.elseStmtHeader(), 'else:\n', Schema.ElseStmtHeader() )
		self._parseNodeTest( g.elseStmtHeader(), Schema.ElseStmtHeader(), Schema.ElseStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.ElseStmtHeader(), Schema.ElseStmtHeader() )


	def testWhileStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.whileStmtHeader(), 'while a:\n', Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.whileStmtHeader(), Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ), Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ), Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ) )


	def testForStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.forStmtHeader(), 'for x in y:\n', Schema.ForStmtHeader( target=Schema.SingleTarget( name='x' ), source=Schema.Load( name='y' ) ) )
		self._parseNodeTest( g.forStmtHeader(), Schema.ForStmtHeader( target=Schema.SingleTarget( name='x' ), source=Schema.Load( name='y' ) ),
				     Schema.ForStmtHeader( target=Schema.SingleTarget( name='x' ), source=Schema.Load( name='y' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.ForStmtHeader( target=Schema.SingleTarget( name='x' ), source=Schema.Load( name='y' ) ),
				     Schema.ForStmtHeader( target=Schema.SingleTarget( name='x' ), source=Schema.Load( name='y' ) ) )


	def testTryStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.tryStmtHeader(), 'try:\n', Schema.TryStmtHeader() )
		self._parseNodeTest( g.tryStmtHeader(), Schema.TryStmtHeader(), Schema.TryStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.TryStmtHeader(), Schema.TryStmtHeader() )


	def testExceptStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.exceptStmtHeader(), 'except:\n', Schema.ExceptStmtHeader( exception=None, target=None ) )
		self._parseStringTest( g.exceptStmtHeader(), 'except x:\n', Schema.ExceptStmtHeader( exception=Schema.Load( name='x' ), target=None ) )
		self._parseStringTest( g.exceptStmtHeader(), 'except x, y:\n', Schema.ExceptStmtHeader( exception=Schema.Load( name='x' ), target=Schema.SingleTarget( name='y' ) ) )
		self._parseNodeTest( g.exceptStmtHeader(), Schema.ExceptStmtHeader( exception=None, target=None ), Schema.ExceptStmtHeader( exception=None, target=None ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.ExceptStmtHeader( exception=None, target=None ), Schema.ExceptStmtHeader( exception=None, target=None ) )


	def testFinallyStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.finallyStmtHeader(), 'finally:\n', Schema.FinallyStmtHeader() )
		self._parseNodeTest( g.finallyStmtHeader(), Schema.FinallyStmtHeader(), Schema.FinallyStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.FinallyStmtHeader(), Schema.FinallyStmtHeader() )


	def testWithStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.withStmtHeader(), 'with a:\n', Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ) )
		self._parseStringTest( g.withStmtHeader(), 'with a as b:\n', Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=Schema.SingleTarget( name='b' ) ) ] ) )
		self._parseStringTest( g.withStmtHeader(), 'with a as b, c as d, e:\n', Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=Schema.SingleTarget( name='b' ) ),
		                                                                                                          Schema.WithContext( expr=Schema.Load( name='c' ), target=Schema.SingleTarget( name='d' ) ),
		                                                                                                          Schema.WithContext( expr=Schema.Load( name='e' ) ) ] ) )
		self._parseNodeTest( g.withStmtHeader(), Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ),
		                     Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ),
		                     Schema.WithStmtHeader( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ) )


	def testDecoStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.decoStmtHeader(), '@f\n', Schema.DecoStmtHeader( name='f', args=None ) )
		self._parseStringTest( g.decoStmtHeader(), '@f(x)\n', Schema.DecoStmtHeader( name='f', args=[ Schema.Load( name='x' ) ] ) )
		self._parseNodeTest( g.decoStmtHeader(), Schema.DecoStmtHeader( name='f', args=None ), Schema.DecoStmtHeader( name='f', args=None ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.DecoStmtHeader( name='f', args=None ), Schema.DecoStmtHeader( name='f', args=None ) )


	def testDefStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.defStmtHeader(), 'def f():\n', Schema.DefStmtHeader( name='f', params=[] ) )
		self._parseStringTest( g.defStmtHeader(), 'def f(x):\n', Schema.DefStmtHeader( name='f', params=[ Schema.SimpleParam( name='x' ) ] ) )
		self._parseNodeTest( g.defStmtHeader(), Schema.DefStmtHeader( name='f', params=[] ), Schema.DefStmtHeader( name='f', params=[] ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.DefStmtHeader( name='f', params=[] ), Schema.DefStmtHeader( name='f', params=[] ) )


	def testClassStmtHeader(self):
		g = Python2Grammar()
		self._parseStringTest( g.classStmtHeader(), 'class Q:\n', Schema.ClassStmtHeader( name='Q', bases=None ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x):\n', Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ) ] ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x,):\n', Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ) ], basesTrailingSeparator='1' ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x,y):\n', Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) )
		self._parseNodeTest( g.classStmtHeader(), Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ),
				     Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) )
		self._parseNodeTest( g.compoundStmtHeader(), Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ),
				     Schema.ClassStmtHeader( name='Q', bases=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) )


	def testCommentStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.commentStmt(), '#x\n', Schema.CommentStmt( comment='x' ) )
		self._parseStringTest( g.commentStmt(), '#' + string.printable.replace( '\n', '' ) + '\n', Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )
		self._parseNodeTest( g.commentStmt(), Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ), Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )



	def testBlankLine(self):
		g = Python2Grammar()
		self._parseStringTest( g.blankLine(), '\n', Schema.BlankLine() )





	def testFnCallStStmt(self):
		g = Python2Grammar()
		self._parseStringTest( g.expression(), 'x.y()', Schema.Call( target=Schema.AttributeRef( target=Schema.Load( name='x' ), name='y' ), args=[] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'x.y()\n', Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=Schema.Load( name='x' ), name='y' ), args=[] ) ) )
		self._parseNodeTest( g.singleLineStatementValid(), Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=Schema.Load( name='x' ), name='y' ), args=[] ) ),
				     Schema.ExprStmt( expr=Schema.Call( target=Schema.AttributeRef( target=Schema.Load( name='x' ), name='y' ), args=[] ) ) )




	def testDictInList(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'y = [ x, { a : b } ]\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='y' ) ], value=Schema.ListLiteral( values=[ Schema.Load( name='x' ), Schema.DictLiteral( values=[ Schema.DictKeyValuePair( key=Schema.Load( name='a' ), value=Schema.Load( name='b' ) ) ] ) ] ) ) )
		
		
		
		
	def test_unparsed(self):
		g = Python2Grammar()
		self._parseStringTest( g.singleLineStatement(), 'foo bar xyz\n', Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'foo bar xyz' ] ) ) )
		self._parseStringTest( g.singleLineStatement(), 'as\n', Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'as' ] ) ) )
		self._parseStringTest( g.suite(), 'as\n', [ Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'as' ] ) ) ] )
		


		
		
		
	def test_emptyIndentation(self):
		g = Python2Grammar()
		self._parseListTest( g.emptyIndentation(),
				     [
					     Schema.Indent(),
					     Schema.Dedent(),],
				     [
					     Schema.Indent(),
		                             None,
					     Schema.Dedent(), ] )
		self._parseListTest( g.emptyIndentation(),
				     [
					     Schema.Dedent(),
					     Schema.Indent(), ],
				     [
					     Schema.Dedent(),
		                             None,
					     Schema.Indent(), ] )
		self._parseListTest( g.emptyIndentation(),
				     [
					     Schema.Indent(),
					     Schema.Indent(),
					     Schema.Dedent(),
					     Schema.Dedent(), ],
				     [
					     Schema.Indent(),
					     Schema.Dedent(), ] )
		self._parseListTest( g.emptyIndentation(),
				     [
					     Schema.Dedent(),
					     Schema.Dedent(),
					     Schema.Indent(),
					     Schema.Indent(), ],
				     [
					     Schema.Dedent(),
					     Schema.Indent(), ] )

		
	def test_singleIndentedSuite(self):
		g = Python2Grammar()
		self._parseListTest( g.singleIndentedSuite(),
				     [
					     Schema.Indent(),
					     Schema.ContinueStmt(),
					     Schema.Dedent(), ],
				     [ Schema.ContinueStmt() ] )
		self._parseListTest( g.singleIndentedSuite(),
				     [
					     Schema.IndentedBlock( suite=[ Schema.ContinueStmt() ] ), ],
				     [ Schema.ContinueStmt() ] )

		
	def test_compoundSuite(self):
		g = Python2Grammar()
		self._parseListTest( g.compoundSuite(),
				     [
					     Schema.Indent(),
					     Schema.ContinueStmt(),
					     Schema.Dedent(), ],
				     [ Schema.ContinueStmt() ] )
		self._parseListTest( g.compoundSuite(),
				     [
					     Schema.Indent(),
					     Schema.ContinueStmt(),
					     Schema.Dedent(),
					     Schema.Indent(),
					     Schema.BreakStmt(),
					     Schema.Dedent(), ],
				     [ Schema.ContinueStmt(), Schema.BreakStmt() ] )
		self._parseListTest( g.compoundSuite(),
				     [
					     Schema.Indent(),
					     Schema.ContinueStmt(),
					     Schema.Dedent(),
					     Schema.IndentedBlock( suite=[ Schema.BreakStmt() ] ), ],
				     [ Schema.ContinueStmt(), Schema.BreakStmt() ] )

		
		
	def test_indentedBlock(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.IndentedBlock( suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.Indent(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.Dedent() ],
				     Schema.IndentedBlock( suite=[ Schema.IndentedBlock( suite=[ Schema.BlankLine() ] ) ] ) )


		
	def test_ifStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elifBlocks=[] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elifBlocks=[], elseSuite=[ Schema.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.IfStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ElifStmtHeader( condition=Schema.Load( name='b' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.CommentStmt( comment='y' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BreakStmt() ], elifBlocks=[] ),
					     Schema.ElifStmtHeader( condition=Schema.Load( name='b' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BreakStmt() ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.CommentStmt( comment='y' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.CommentStmt( comment='y' ) ] ) ] ),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='z' ),
					     Schema.Dedent() ],
				     Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.CommentStmt( comment='y' ) ] ) ],
						   elseSuite=[ Schema.CommentStmt( comment='z' ) ]) )

		
	def test_whileStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elseSuite=[ Schema.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ] ),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ], elseSuite=[ Schema.CommentStmt( comment='x' ) ] ) )
	
		
	def test_forStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.ForStmtHeader( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ), suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.ForStmtHeader( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ), suite=[ Schema.BlankLine() ], elseSuite=[ Schema.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ), suite=[ Schema.BlankLine() ] ),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ), suite=[ Schema.BlankLine() ], elseSuite=[ Schema.CommentStmt( comment='x' ) ] ) )

		
	def test_tryStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.FinallyStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[], finallySuite=[ Schema.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='k' ), target=Schema.SingleTarget( name='q' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ),
												Schema.ExceptBlock( exception=Schema.Load( name='k' ), target=Schema.SingleTarget( name='q' ), suite=[ Schema.CommentStmt( comment='y' ) ] )] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Schema.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent(),
					     Schema.FinallyStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    finallySuite=[ Schema.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent(),
					     Schema.FinallyStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='z' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Schema.CommentStmt( comment='y' ) ], finallySuite=[ Schema.CommentStmt( comment='z' ) ] ) )

		self._parseListFailTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ] )
		self._parseListFailTest( g.suiteItem(),
				     [
					     Schema.TryStmtHeader(),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent(),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ] )

		# Try with 1 except block, add another
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmt( suite=[ Schema.BlankLine() ],
							    exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ),
					     Schema.ExceptStmtHeader( exception=Schema.Load( name='k' ), target=Schema.SingleTarget( name='q' ) ),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='x' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ),
												Schema.ExceptBlock( exception=Schema.Load( name='k' ), target=Schema.SingleTarget( name='q' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ) )
		# Try with 1 except block, add an else
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmt( suite=[ Schema.BlankLine() ],
							    exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Schema.CommentStmt( comment='y' ) ] ) )
		# Try with 1 except block, add a finally
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmt( suite=[ Schema.BlankLine() ],
							    exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ),
					     Schema.FinallyStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    finallySuite=[ Schema.CommentStmt( comment='y' ) ] ) )
		# Try with 1 except block, add an else and a finally
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.TryStmt( suite=[ Schema.BlankLine() ],
							    exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ] ),
					     Schema.ElseStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='y' ),
					     Schema.Dedent(),
					     Schema.FinallyStmtHeader(),
					     Schema.Indent(),
					     	Schema.CommentStmt( comment='z' ),
					     Schema.Dedent() ],
				     Schema.TryStmt( suite=[ Schema.BlankLine() ], exceptBlocks=[ Schema.ExceptBlock( exception=Schema.Load( name='j' ), target=Schema.SingleTarget( name='p' ), suite=[ Schema.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Schema.CommentStmt( comment='y' ) ],  finallySuite=[ Schema.CommentStmt( comment='z' ) ] ) )
		
		
	def test_withStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.WithStmtHeader(contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.WithStmt( contexts=[ Schema.WithContext( expr=Schema.Load( name='a' ), target=None ) ], suite=[ Schema.BlankLine() ] ) )
	
		
	def test_defStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DefStmtHeader( name='f', params=[ Schema.SimpleParam( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.DefStmt( decorators=[], name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.DefStmtHeader( name='f', params=[ Schema.SimpleParam( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.DefStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ) ], name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.DecoStmtHeader( name='b', args=[ Schema.Load( name='y' ) ] ),
					     Schema.DefStmtHeader( name='f', params=[ Schema.SimpleParam( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.DefStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ), Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ],
						    name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.DefStmt( decorators=[ Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ], name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.BlankLine() ] ) ],
				     Schema.DefStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ), Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ],
						    name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
	
		
	def test_classStmt(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.ClassStmtHeader( name='A', bases=[ Schema.Load( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.ClassStmt( decorators=[], name='A', bases=[ Schema.Load( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.ClassStmtHeader( name='A', bases=[ Schema.Load( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.ClassStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ) ], name='A', bases=[ Schema.Load( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.DecoStmtHeader( name='b', args=[ Schema.Load( name='y' ) ] ),
					     Schema.ClassStmtHeader( name='A', bases=[ Schema.Load( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				     Schema.ClassStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ), Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ],
		                                       name='A', bases=[ Schema.Load( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.DecoStmtHeader( name='a', args=[ Schema.Load( name='x' ) ] ),
					     Schema.ClassStmt( decorators=[ Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ], name='A', bases=[ Schema.Load( name='x' ) ], suite=[ Schema.BlankLine() ] ) ],
				     Schema.ClassStmt( decorators=[ Schema.Decorator( name='a', args=[ Schema.Load( name='x' ) ] ), Schema.Decorator( name='b', args=[ Schema.Load( name='y' ) ] ) ],
		                                       name='A', bases=[ Schema.Load( name='x' ) ], suite=[ Schema.BlankLine() ] ) )
		
		
	def test_nestedStructure(self):
		g = Python2Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Schema.ClassStmtHeader( name='A', bases=[ Schema.Load( name='x' ) ] ),
					     Schema.Indent(),
					     	Schema.DefStmtHeader( name='f', params=[ Schema.SimpleParam( name='x' ) ] ),
						Schema.Indent(),
					     		Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ),
					     		Schema.Indent(),
								Schema.BlankLine(),
					     		Schema.Dedent(),
						Schema.Dedent(),
					     Schema.Dedent() ],
				     Schema.ClassStmt( decorators=[], name='A', bases=[ Schema.Load( name='x' ) ], suite=[
					     Schema.DefStmt( decorators=[], name='f', params=[ Schema.SimpleParam( name='x' ) ], suite=[ Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ] ) ] ) ] ) )
	
		
	def test_suite(self):
		g = Python2Grammar()
		self._parseListTest( g.suite(),
				     [
					     Schema.CommentStmt( comment='x' ),
					     Schema.BlankLine() ],
				      [
					     Schema.CommentStmt( comment='x' ),
					     Schema.BlankLine() ] )
		

	
		
	def test_header_indentedBlock(self):
		g = Python2Grammar()
		self._parseListTest( g.suite(),
				     [
					     Schema.WhileStmtHeader( condition=Schema.Load( name='x' ) ),
					     Schema.IndentedBlock( suite=[ Schema.CommentStmt( comment='a' ) ] ) ],
				      [
					     Schema.WhileStmt( condition=Schema.Load( name='x' ), suite=[ Schema.CommentStmt( comment='a' ) ] ) ] )
		

	
		
	def test_headers(self):
		g = Python2Grammar()
		self._parseListTest( g.suite(),
				     [
					     Schema.IfStmtHeader( condition=Schema.Load( name='x' ) ),
					     Schema.WhileStmtHeader( condition=Schema.Load( name='a' ) ),
					     Schema.Indent(),
					     	Schema.BlankLine(),
					     Schema.Dedent() ],
				      [
					     Schema.IfStmtHeader( condition=Schema.Load( name='x' ) ),
					     Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.BlankLine() ] ) ] )

		
	def test_richStringSuite(self):
		g = Python2Grammar()
		s = self._pythonRichString( 'while a:\n', Schema.Indent(), 'continue\n', Schema.Dedent() )
		self._parseRichStringTest( g.suite(), s, [ Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ContinueStmt() ] ) ] )
		
		
	def test_embeddedStructural(self):
		g = Python2Grammar()
		#s = self._pythonRichString( 'x = ', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), '\n' )
		s = self._pythonRichString( 'x = ', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), ' + c\n' )
		self._parseRichStringTest( g.suite(), s, [ Schema.AssignStmt( targets=[ Schema.SingleTarget( name='x' ) ], value=Schema.Add( x=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) ) ] )
		
		


def parserViewTest():
	#result, pos, dot = targetListOrTargetItem.traceParseStringChars( 'a.b' )
	#result, pos, dot = subscript.traceParseStringChars( 'a.b' )
	#print dot

	#g = Python2Grammar()
	#g.singleLineStatementValid().parseStringChars( 'raise' )

	from BritefuryJ.ParserDebugViewer import ParseViewFrame

	g = Python2Grammar()
	result = g.expression().traceParseStringChars( '[i for i in a]' )
	ParseViewFrame( result )
