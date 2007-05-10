##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMFrame import VMFrame
from Britefury.VirtualMachine.VMBlock import VMBlock
from Britefury.VirtualMachine.VMMessage import VMMessage
from Britefury.VirtualMachine.VMTag import VMTag
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.vcls_object import vcls_object, vcls_class, vobjectmsg_alloc
from Britefury.VirtualMachine.vcls_bool import vcls_bool, vfalse, vtrue
from Britefury.VirtualMachine.vcls_string import vcls_string, pyStrToVString
from Britefury.VirtualMachine.vcls_list import vcls_list
from Britefury.VirtualMachine.vcls_closure import vcls_closure
from Britefury.VirtualMachine.vcls_frame import vcls_frame
from Britefury.VirtualMachine.vcls_block import vcls_block
from Britefury.VirtualMachine.vcls_module import vcls_module
from Britefury.VirtualMachine.vcls_none import vnone
from Britefury.VirtualMachine.Registers import *


class VMMachine (object):
	baseBlock = VMBlock( 'builtins', None )

	tag_Object = VMTag( 'Builtin', 'Object' )
	tag_Class = VMTag( 'Builtin', 'Class' )
	tag_Bool = VMTag( 'Builtin', 'Bool' )
	tag_String = VMTag( 'Builtin', 'String' )
	tag_List = VMTag( 'Builtin', 'List' )
	tag_Closure = VMTag( 'Builtin', 'Closure' )
	tag_Frame = VMTag( 'Builtin', 'Frame' )
	tag_Block = VMTag( 'Builtin', 'Block' )
	tag_Module = VMTag( 'Builtin', 'Module' )
	tag_none = VMTag( 'Builtin', 'none' )
	tag_false = VMTag( 'Builtin', 'false' )
	tag_true = VMTag( 'Builtin', 'true' )


	reg_Object = baseBlock.allocLocalReg( tag_Object )
	reg_Class = baseBlock.allocLocalReg( tag_Class )
	reg_Bool = baseBlock.allocLocalReg( tag_Bool )
	reg_String = baseBlock.allocLocalReg( tag_String )
	reg_List = baseBlock.allocLocalReg( tag_List )
	reg_Closure = baseBlock.allocLocalReg( tag_Closure )
	reg_Frame = baseBlock.allocLocalReg( tag_Frame )
	reg_Block = baseBlock.allocLocalReg( tag_Block )
	reg_Module = baseBlock.allocLocalReg( tag_Module )
	reg_none = baseBlock.allocLocalReg( tag_none )
	reg_false = baseBlock.allocLocalReg( tag_false )
	reg_true = baseBlock.allocLocalReg( tag_true )

	baseBlock.initialise( [] )

	baseFrame = VMFrame( baseBlock )

	baseFrame.storeReg( reg_Object, vcls_object )
	baseFrame.storeReg( reg_Class, vcls_class )
	baseFrame.storeReg( reg_Bool, vcls_bool )
	baseFrame.storeReg( reg_String, vcls_string )
	baseFrame.storeReg( reg_List, vcls_list )
	baseFrame.storeReg( reg_Closure, vcls_closure )
	baseFrame.storeReg( reg_Frame, vcls_frame )
	baseFrame.storeReg( reg_Block, vcls_block )
	baseFrame.storeReg( reg_Module, vcls_module )
	baseFrame.storeReg( reg_none, vnone )
	baseFrame.storeReg( reg_false, vfalse )
	baseFrame.storeReg( reg_true, vtrue )




	def run(self, block, bDebug=False):
		self.frame = VMFrame( block, self.baseFrame )

		while self.frame is not None  and  self.frame.ip < len( self.frame.block.instructions ):
			instruction = self.frame.block.instructions[self.frame.ip]
			self.frame.ip += 1
			if bDebug:
				print '[%s]' % ( self.frame.block.name, )
				print '\t\t' + '>' * self.frame.depth()  + ' %s'  %  ( instruction.disassemble( self ), )
			instruction.execute( self )


	def pushFrame(self, frame):
		frame.parent = self.frame
		self.frame = frame

	def popFrame(self):
		p = self.frame.parent
		self.frame.parent = None
		self.frame = p







newBlock = VMBlock( 'newMessage', VMMachine.baseBlock, argNames=[], expandArgName='initArgs' )
newInstructions = [
	SendMessageInstruction( selfRegister(), 0, [] ),							# call self.alloc()			[]
	MoveInstruction( tempRegister( 0 ), resultRegister() ),					# T0 <- result
	SendMessageInstruction( tempRegister( 0 ), 1, [], localRegister( 0, 0 ) ),		# call T0.init( *initArgs )
	ReturnInstruction( tempRegister( 0 ) )								# return T0
]
newBlock.initialise( newInstructions, 1, [ pyStrToVString( 'alloc' ), pyStrToVString( 'init' ) ] )
newClosure = vobjectmsg_alloc( vcls_closure, None, [] )
newClosure._block = newBlock
newClosure._outerScope = None
vcls_object.setMessage( 'new', VMMessage( newClosure ) )



