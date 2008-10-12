##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Parser import *


from Britefury.Grammar.Grammar import Grammar, Rule


from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase


from BritefuryJ.Parser.Utils import Tokens


class GrammarTestCase (ParserTestCase):
	
	class TestGrammarSimple (Grammar):
		@Rule
		def a(self):
			return Literal( 'a' )
		
		@Rule
		def b(self):
			return Literal( 'b' )
		
		@Rule
		def ab(self):
			return self.a() + self.b()
		
		@Rule
		def expr(self):
			return self.ab()
		
		
		
	class TestGrammarSimpleOverload (TestGrammarSimple):
		@Rule
		def a(self):
			return Literal( 'a' ) | Literal( 'c' )
		
		@Rule
		def b(self):
			return Literal( 'b' ) | Literal( 'd' )
		
		
		
	class TestGrammarRecursive (Grammar):
		@Rule
		def identifier(self):
			return Tokens.identifier
		
		@Rule
		def atom(self):
			return self.identifier()
		
		@Rule
		def mul(self):
			return ( self.mul() + '*' + self.atom() ).action( lambda input, begin, xs: [ 'mul', xs[0], xs[2] ] )  |  self.atom()
		
		@Rule
		def expr(self):
			return self.mul()
		
		
		
	class TestGrammarRecursiveOverload (TestGrammarRecursive):
		@Rule
		def add(self):
			return ( self.add() + '+' + self.mul() ).action( lambda input, begin, xs: [ 'add', xs[0], xs[2] ] )  |  self.mul()
		
		@Rule
		def expr(self):
			return self.add()
		
		
		
	class TestGrammarIndirectRecursiveOverload (TestGrammarRecursiveOverload):
		@Rule
		def paren(self):
			return ( Literal( '(' ) + self.expr() + Literal( ')' ) ).action( lambda input, begin, xs: xs[1] )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		
		
	def testProduction(self):
		g = self.TestGrammarSimple()
		
		a1 = g.a()
		
		self.assert_( isinstance( a1, Production ) )
		self.assert_( a1.getDebugName() == 'a' )

		
	def testRuleCacheing(self):
		g = self.TestGrammarSimple()
		
		a1 = g.a()
		a2 = g.a()
		self.assert_( a1 is a2 )
		
		
	def testForward(self):
		g = self.TestGrammarRecursive()
		
		m = g.mul()
		
		self.assert_( isinstance( m, Forward ) )
		

	def testSimpleGrammar(self):
		g = self.TestGrammarSimple()
		
		parser = g.expr()
		
		self._matchTest( parser, 'ab', [ 'a', 'b' ] )
		self._matchFailTest( parser, 'cb' )
		self._matchFailTest( parser, 'ad' )
		self._matchFailTest( parser, 'cd' )

		
	def testGrammarSimpleOverload(self):
		g = self.TestGrammarSimpleOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'ab', [ 'a', 'b' ] )
		self._matchTest( parser, 'cb', [ 'c', 'b' ] )
		self._matchTest( parser, 'ad', [ 'a', 'd' ] )
		self._matchTest( parser, 'cd', [ 'c', 'd' ] )
		
		
	def testGrammarRecursive(self):
		g = self.TestGrammarRecursive()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )

		
	def testGrammarRecursiveOverload(self):
		g = self.TestGrammarRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( parser, 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( parser, 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )

		
	def testGrammarIndirectRecursiveOverload(self):
		g = self.TestGrammarIndirectRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( parser, 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( parser, 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, '(a+b)*c', [ 'mul', [ 'add', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a*(b+c)', [ 'mul', 'a', [ 'add', 'b', 'c' ] ] )