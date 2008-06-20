##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGenerator


class Python25CodeGenerator (GSymCodeGenerator):
	# STRING LITERALS
	def stringLiteral(self, node, format, quotation, value):
		return repr( value )
	
	def intLiteral(self, node, format, numType, value):
		return repr( value )
	
	def floatLiteral(self, node, value):
		return repr( value )
	
	def imaginaryLiteral(self, node, value):
		return repr( value )
	

	def kwArg(self, node, name, value):
		return name + '=' + self( value )
	
	def argList(self, node, value):
		return '*' + self( value )
	
	def kwArgList(self, node, value):
		return '*' + self( value )
	
	def call(self, node, target, *params):
		return self( target ) + '( ' + ', '.join( [ self( p )   for p in params ] ) + ' )'
	
	def subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	
	def slice(self, node, target, first, second):
		return self( target ) + '[' + self( first ) + ':' + self( second ) + ']'
	
	def attr(self, node, target, name):
		return self( target ) + '.' + name

	
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	
	def invert(self, node, x):
		return '~( '  +  self( x )  +  ' )'
	
	def negate(self, node, x):
		return '-( '  +  self( x )  +  ' )'
	
	def pos(self, node, x):
		return '+( '  +  self( x )  +  ' )'
	
	
	def mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	def div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	def mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	def add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	def sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	
	def lshift(self, node, x, y):
		return '( '  +  self( x )  +  ' << '  +  self( y )  +  ' )'
	
	def rshift(self, node, x, y):
		return '( '  +  self( x )  +  ' >> '  +  self( y )  +  ' )'
	
	
	def bitAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' & '  +  self( y )  +  ' )'
	
	def bitXor(self, node, x, y):
		return '( '  +  self( x )  +  ' ^ '  +  self( y )  +  ' )'
	
	def bitOr(self, node, x, y):
		return '( '  +  self( x )  +  ' | '  +  self( y )  +  ' )'
	

	def lte(self, node, x, y):
		return '( '  +  self( x )  +  ' <= '  +  self( y )  +  ' )'
	
	def lt(self, node, x, y):
		return '( '  +  self( x )  +  ' < '  +  self( y )  +  ' )'
	
	def gte(self, node, x, y):
		return '( '  +  self( x )  +  ' >= '  +  self( y )  +  ' )'
	
	def gt(self, node, x, y):
		return '( '  +  self( x )  +  ' > '  +  self( y )  +  ' )'
	
	def eq(self, node, x, y):
		return '( '  +  self( x )  +  ' == '  +  self( y )  +  ' )'
	
	def neq(self, node, x, y):
		return '( '  +  self( x )  +  ' != '  +  self( y )  +  ' )'
	

	def cmpIsNot(self, node, x, y):
		return '( '  +  self( x )  +  ' is not '  +  self( y )  +  ' )'
	
	def cmpIs(self, node, x, y):
		return '( '  +  self( x )  +  ' is '  +  self( y )  +  ' )'
	
	def cmpNotIn(self, node, x, y):
		return '( '  +  self( x )  +  ' not in '  +  self( y )  +  ' )'
	
	def cmpIn(self, node, x, y):
		return '( '  +  self( x )  +  ' in '  +  self( y )  +  ' )'

	
	def boolNot(self, node, x):
		return '(not '  +  self( x )  +  ')'
	
	def boolAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' and '  +  self( y )  +  ' )'
	
	def boolOr(self, node, x, y):
		return '( '  +  self( x )  +  ' or '  +  self( y )  +  ' )'
	
	
	def var(self, node, name):
		return name
	
	def nilExpr(self, node):
		return '<NIL>'
	
	def listDisplay(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' ]'
	
	
	def python25Document(self, node, content):
		return self( content )
