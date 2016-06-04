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
 * A {@link ClassVisitor} that generates classes in bytecode form. More
 * precisely this visitor generates a byte array conforming to the Java class
 * file format. It can be used alone, to generate a Java class "from scratch",
 * or with one or more {@link ClassReader ClassReader} and adapter class visitor
 * to generate a modified class from one or more existing Java classes.
 * 
 * @author Eric Bruneton
 */
public class ClassWriter extends ClassVisitor {

    /**
     * Flag to automatically compute the maximum stack size and the maximum
     * number of local variables of methods. If this flag is set, then the
     * arguments of the {@link MethodVisitor#visitMaxs visitMaxs} method of the
     * {@link MethodVisitor} returned by the {@link #visitMethod visitMethod}
     * method will be ignored, and computed automatically from the signature and
     * the bytecode of each method.
     * 
     * @see #ClassWriter(int)
     */
    public static final int COMPUTE_MAXS = 1;

    /**
     * Flag to automatically compute the stack map frames of methods from
     * scratch. If this flag is set, then the calls to the
     * {@link MethodVisitor#visitFrame} method are ignored, and the stack map
     * frames are recomputed from the methods bytecode. The arguments of the
     * {@link MethodVisitor#visitMaxs visitMaxs} method are also ignored and
     * recomputed from the bytecode. In other words, computeFrames implies
     * computeMaxs.
     * 
     * @see #ClassWriter(int)
     */
    public static final int COMPUTE_FRAMES = 2;

    /**
     * Pseudo access flag to distinguish between the synthetic attribute and the
     * synthetic access flag.
     */
    static final int ACC_SYNTHETIC_ATTRIBUTE = 0x40000;

    /**
     * Factor to convert from ACC_SYNTHETIC_ATTRIBUTE to Opcode.ACC_SYNTHETIC.
     */
    static final int TO_ACC_SYNTHETIC = ACC_SYNTHETIC_ATTRIBUTE
            / Opcodes.ACC_SYNTHETIC;

    /**
     * The type of instructions without any argument.
     */
    static final int NOARG_INSN = 0;

    /**
     * The type of instructions with an signed byte argument.
     */
    static final int SBYTE_INSN = 1;

    /**
     * The type of instructions with an signed short argument.
     */
    static final int SHORT_INSN = 2;

    /**
     * The type of instructions with a local variable index argument.
     */
    static final int VAR_INSN = 3;

    /**
     * The type of instructions with an implicit local variable index argument.
     */
    static final int IMPLVAR_INSN = 4;

    /**
     * The type of instructions with a type descriptor argument.
     */
    static final int TYPE_INSN = 5;

    /**
     * The type of field and method invocations instructions.
     */
    static final int FIELDORMETH_INSN = 6;

    /**
     * The type of the INVOKEINTERFACE/INVOKEDYNAMIC instruction.
     */
    static final int ITFMETH_INSN = 7;

    /**
     * The type of the INVOKEDYNAMIC instruction.
     */
    static final int INDYMETH_INSN = 8;

    /**
     * The type of instructions with a 2 bytes bytecode offset label.
     */
    static final int LABEL_INSN = 9;

    /**
     * The type of instructions with a 4 bytes bytecode offset label.
     */
    static final int LABELW_INSN = 10;

    /**
     * The type of the LDC instruction.
     */
    static final int LDC_INSN = 11;

    /**
     * The type of the LDC_W and LDC2_W instructions.
     */
    static final int LDCW_INSN = 12;

    /**
     * The type of the IINC instruction.
     */
    static final int IINC_INSN = 13;

    /**
     * The type of the TABLESWITCH instruction.
     */
    static final int TABL_INSN = 14;

    /**
     * The type of the LOOKUPSWITCH instruction.
     */
    static final int LOOK_INSN = 15;

    /**
     * The type of the MULTIANEWARRAY instruction.
     */
    static final int MANA_INSN = 16;

    /**
     * The type of the WIDE instruction.
     */
    static final int WIDE_INSN = 17;

    /**
     * The instruction types of all JVM opcodes.
     */
    static final byte[] TYPE;

    /**
     * The type of CONSTANT_Class constant pool items.
   