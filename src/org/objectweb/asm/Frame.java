/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm;

/**
 * Information about the input and output stack map frames of a basic block.
 * 
 * @author Eric Bruneton
 */
final class Frame {

    /*
     * Frames are computed in a two steps process: during the visit of each
     * instruction, the state of the frame at the end of current basic block is
     * updated by simulating the action of the instruction on the previous state
     * of this so called "output frame". In visitMaxs, a fix point algorithm is
     * used to compute the "input frame" of each basic block, i.e. the stack map
     * frame at the beginning of the basic block, starting from the input frame
     * of the first basic block (which is computed from the method descriptor),
     * and by using the previously computed output frames to compute the input
     * state of the other blocks.
     * 
     * All output and input frames are stored as arrays of integers. Reference
     * and array types are represented by an index into a type table (which is
     * not the same as the constant pool of the class, in order to avoid adding
     * unnecessary constants in the pool - not all computed frames will end up
     * being stored in the stack map table). This allows very fast type
     * comparisons.
     * 
     * Output stack map frames are computed relatively to the input frame of the
     * basic block, which is not yet known when output frames are computed. It
     * is therefore necessary to be able to represent abstract types such as
     * "the type at position x in the input frame locals" or "the type at
     * position x from the top of the input frame stack" or even "the type at
     * position x in the input frame, with y more (or less) array dimensions".
     * This explains the rather complicated type format used in output frames.
     * 
     * This format is the following: DIM KIND VALUE (4, 4 and 24 bits). DIM is a
     * signed number of array dimensions (from -8 to 7). KIND is either BASE,
     * LOCAL or STACK. BASE is used for types that are not relative to the input
     * frame. LOCAL is used for types that are relative to the input local
     * variable types. STACK is used for types that are relative to the input
     * stack types. VALUE depends on KIND. For LOCAL types, it is an index in
     * the input local variable types. For STACK types, it is a position
     * relatively to the top of input frame stack. For BASE types, it is either
     * one of the constants defined in FrameVisitor, or for OBJECT and
     * UNINITIALIZED types, a tag and an index in the type table.
     * 
     * Output frames can contain types of any kind and with a positive or
     * negative dimension (and even unassigned types, represented by 0 - which
     * does not correspond to any valid type value). Input frames can only
     * contain BASE types of positive or null dimension. In all cases the type
     * table contains only internal type names (array type descriptors are
     * forbidden - dimensions must be represented through the DIM field).
     * 
     * The LONG and DOUBLE types are always represented by using two slots (LONG
     * + TOP or DOUBLE + TOP), for local variable types as well as in the
     * operand stack. This is necessary to be able to simulate DUPx_y
     * instructions, whose effect would be dependent on the actual type values
     * if types were always represented by a single slot in the stack (and this
     * is not possible, since actual type values are not always known - cf LOCAL
     * and STACK type kinds).
     */

    /**
     * Mask to get the dimension of a frame type. This dimension is a signed
     * integer between -8 and 7.
     */
    static final int DIM = 0xF0000000;

    /**
     * Constant to be added to a type to get a type with one more dimension.
     */
    static final int ARRAY_OF = 0x10000000;

    /**
     * Constant to be added to a type to get a type with one less dimension.
     */
    static final int ELEMENT_OF = 0xF0000000;

    /**
     * Mask to get the kind of a frame type.
     * 
     * @see #BASE
     * @see #LOCAL
     * @see #STACK
     */
    static final int KIND = 0xF000000;

    /**
     * Flag used for LOCAL and STACK types. Indicates that if this type happens
     * to be a long or double type (during the computations of input frames),
     * then it must be set to TOP because the second word of this value has been
     * reused to store other data in the basic block. Hence the first word no
     * longer stores a valid long or double value.
     */
    static final int TOP_IF_LONG_OR_DOUBLE = 0x800000;

    /**
     * Mask to get the value of a frame type.
     */
    static final int VALUE = 0x7FFFFF;

    /**
     * Mask to get the kind of base types.
     */
    static final int BASE_KIND = 0xFF00000;

    /**
     * Mask to get the value of base types.
     */
    static final int BASE_VALUE = 0xFFFFF;

    /**
     * Kind of the types that are not relative to an input stack map frame.
     */
    static final int BASE = 0x1000000;

    /**
     * Base kind of the base reference types. The BASE_VALUE of such types is an
     * index into the type table.
     */
    static final int OBJECT = BASE | 0x700000;

    /**
     * Base kind of the uninitialized base types. The BASE_VALUE of such types
     * in an index into the type table (the Item at that index contains both an
     * instruction offset and an internal class name).
     */
    static final int UNINITIALIZED = BASE | 0x800000;

    /**
     * Kind of the types that are relative to the local variable types of an
     * input stack map frame. The value of such types is a local variable index.
     */
    private static final int LOCAL = 0x2000000;

    /**
     * Kind of the the types that are relative to the stack of an input stack
     * map frame. The value of such types is a position relatively to the top of
     * this stack.
     */
    private static final int STACK = 0x3000000;

    /**
     * The TOP type. This is a BASE type.
     */
    static final int TOP = BASE | 0;

    /**
     * The BOOLEAN type. This is a BASE type mainly used for array types.
     */
    static final int BOOLEAN = BASE | 9;

    /**
     * The BYTE type. This is a BASE type mainly used for array types.
     */
    static final int BYTE = BASE | 10;

    /**
     * The CHAR type. This is a BASE type mainly used for array types.
     */
    static final int CHAR = BASE | 11;

    /**
     * The SHORT type. This is a BASE type mainly used for array types.
     */
    static final int SHORT = BASE | 12;

    /**
     * The INTEGER type. This is a BASE type.
     */
    static final int INTEGER = BASE | 1;

    /**
     * The FLOAT type. This is a BASE type.
     */
    static final int FLOAT = BASE | 2;

    /**
     * The DOUBLE type. This is a BASE type.
     */
    static final int DOUBLE = BASE | 3;

    /**
     * The LONG type. This is a BASE type.
     */
    static final int LONG = BASE | 4;

    /**
     * The NULL type. This is a BASE type.
     */
    static final int NULL = BASE | 5;

    /**
     * The UNINITIALIZED_THIS type. This is a BASE type.
     */
    static final int UNINITIALIZED_THIS = BASE | 6;

    /**
     * The stack size variation corresponding to each JVM instruction. This
     * stack variation is equal to the size of the values produced by an
     * instruction, minus the size of the values consumed by this instruction.
     */
    static final int[] SIZE;

    /**
     * Computes the stack size variation corresponding to each JVM instruction.
     */
    static {
        int i;
        int[] b = new int[202];
        String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDD"
                + "CDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCD"
                + "CDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFED"
                + "DDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;

        // code to generate the above string
        //
        // int NA = 0; // not applicable (unused opcode or variable size opcode)
        //
        // b = new int[] {
        // 0, //NOP, // visitInsn
        // 1, //ACONST_NULL, // -
        // 1, //ICONST_M1, // -
        // 1, //ICONST_0, // -
        // 1, //ICONST_1, // -
        // 1, //ICONST_2, // -
        // 1, //ICONST_3, // -
        // 1, //ICONST_4, // -
        // 1, //ICONST_5, // -
        // 2, //LCONST_0, // -
        // 2, //LCONST_1, // -
        // 1, //FCONST_0, // -
        // 1, //FCONST_1, // -
        // 1, //FCONST_2, // -
        // 2, //DCONST_0, // -
        // 2, //DCONST_1, // -
        // 1, //BIPUSH, // visitIntInsn
        // 1, //SIPUSH, // -
        // 1, //LDC, // visitLdcInsn
        // NA, //LDC_W, // -
        // NA, //LDC2_W, // -
        // 1, //ILOAD, // visitVarInsn
        // 2, //LLOAD, // -
        // 1, //FLOAD, // -
        // 2, //DLOAD, // -
        // 1, //ALOAD, // -
        // NA, //ILOAD_0, // -
        // NA, //ILOAD_1, // -
        // NA, //ILOAD_2, // -
        // NA, //ILOAD_3, // -
        // NA, //LLOAD_0, // -
        // NA, //LLOAD_1, // -
        // NA, //LLOAD_2, // -
        // NA, //LLOAD_3, // -
        // NA, //FLOAD_0, // -
        // NA, //FLOAD_1, // -
        // NA, //FLOAD_2, // -
        // NA, //FLOAD_3, // -
        // NA, //DLOAD_0, // -
        // NA, //DLOAD_1, // -
        // NA, //DLOAD_2, // -
        // NA, //DLOAD_3, // -
        // NA, //ALOAD_0, // -
        // NA, //ALOAD_1, // -
        // NA, //ALOAD_2, // -
        // NA, //ALOAD_3, // -
        // -1, //IALOAD, // visitInsn
        // 0, //LALOAD, // -
        // -1, //FALOAD, // -
        // 0, //DALOAD, // -
        // -1, //AALOAD, // -
        // -1, //BALOAD, // -
        // -1, //CALOAD, // -
        // -1, //SALOAD, // -
        // -1, //ISTORE, // visitVarInsn
        // 