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

import java.io.IOException;
import java.io.InputStream;

/**
 * A Java class parser to make a {@link ClassVisitor} visit an existing class.
 * This class parses a byte array conforming to the Java class file format and
 * calls the appropriate visit methods of a given class visitor for each field,
 * method and bytecode instruction encountered.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class ClassReader {

    /**
     * True to enable signatures support.
     */
    static final boolean SIGNATURES = true;

    /**
     * True to enable annotations support.
     */
    static final boolean ANNOTATIONS = true;

    /**
     * True to enable stack map frames support.
     */
    static final boolean FRAMES = true;

    /**
     * True to enable bytecode writing support.
     */
    static final boolean WRITER = true;

    /**
     * True to enable JSR_W and GOTO_W support.
     */
    static final boolean RESIZE = true;

    /**
     * Flag to skip method code. If this class is set <code>CODE</code>
     * attribute won't be visited. This can be used, for example, to retrieve
     * annotations for methods and method parameters.
     */
    public static final int SKIP_CODE = 1;

    /**
     * Flag to skip the debug information in the class. If this flag is set the
     * debug information of the class is not visited, i.e. the
     * {@link MethodVisitor#visitLocalVariable visitLocalVariable} and
     * {@link MethodVisitor#visitLineNumber visitLineNumber} methods will not be
     * called.
     */
    public static final int SKIP_DEBUG = 2;

    /**
     * Flag to skip the stack map frames in the class. If this flag is set the
     * stack map frames of the class is not visited, i.e. the
     * {@link MethodVisitor#visitFrame visitFrame} method will not be called.
     * This flag is useful when the {@link ClassWriter#COMPUTE_FRAMES} option is
     * used: it avoids visiting frames that will be ignored and recomputed from
     * scratch in the class writer.
     */
    public static final int SKIP_FRAMES = 4;

    /**
     * Flag to expand the stack map frames. By default stack map frames are
     * visited in their original format (i.e. "expanded" for classes whose
     * version is less than V1_6, and "compressed" for the other classes). If
     * this flag is set, stack map frames are always visited in expanded format
     * (this option adds a decompression/recompression step in ClassReader and
     * ClassWriter which degrades performances quite a lot).
     */
    public static final int EXPAND_FRAMES = 8;

    /**
     * The class to be parsed. <i>The content of this array must not be
     * modified. This field is intended for {@link Attribute} sub classes, and
     * is normally not needed by class generators or adapters.</i>
     */
    public final byte[] b;

    /**
     * The start index of each constant pool item in {@link #b b}, plus one. The
     * one byte offset skips the constant pool item tag that indicates its type.
     */
    private final int[] items;

    /**
     * The String objects corresponding to the CONSTANT_Utf8 items. This cache
     * avoids multiple parsing of a given CONSTANT_Utf8 constant pool item,
     * which GREATLY improves performances (by a factor 2 to 3). This caching
     * strategy could be extended to all constant pool items, but its benefit
     * would not be so great for these items (because they are much less
     * expensive to parse than CONSTANT_Utf8 items).
     */
    private final String[] strings;

    /**
     * Maximum length of the strings contained in the constant pool of the
     * class.
     */
    private final int maxStringLength;

    /**
     * Start index of the class header information (access, name...) in
     * {@link #b b}.
     */
    public final int header;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ClassReader} object.
     * 
     * @param b
     *            the bytecode of the class to be read.
     */
    public ClassReader(final byte[] b) {
        this(b, 0, b.length);
    }

    /**
     * Constructs a new {@link ClassReader} object.
     * 
     * @param b
     *            the bytecode of the class to be read.
     * @param off
     *            the start offset of the class data.
     * @param len
     *            the length of the class data.
     */
    public ClassReader(final byte[] b, final int off, final int len) {
        this.b = b;
        // checks the class version
        if (readShort(off + 6) > Opcodes.V1_8) {
            throw new IllegalArgumentException();
        }
        // parses the constant pool
        items = new int[readUnsignedShort(off + 8)];
        int n = items.length;
        strings = new String[n];
        int max = 0;
        int index = off + 10;
        for (int i = 1; i < n; ++i) {
            items[i] = index + 1;
            int size;
            switch (b[index]) {
            case ClassWriter.FIELD:
            case ClassWriter.METH:
            case ClassWriter.IMETH:
            case ClassWriter.INT:
            case ClassWriter.FLOAT:
            case ClassWriter.NAME_TYPE:
            case ClassWriter.INDY:
                size = 5;
                break;
            case ClassWriter.LONG:
            case ClassWriter.DOUBLE:
                size = 9;
                ++i;
                break;
            case ClassWriter.UTF8:
                size = 3 + readUnsignedShort(index + 1);
                if (size > max) {
                    max = size;
                }
                break;
            case ClassWriter.HANDLE:
                size = 4;
                break;
            // case ClassWriter.CLASS:
            // case ClassWriter.STR:
            // case ClassWriter.MTYPE
            default:
                size = 3;
                break;
            }
            index += size;
        }
        maxStringLength = max;
        // the class header information starts just after the constant pool
        header = index;
    }

    /**
     * Returns the class's access flags (see {@link Opcodes}). This value may
     * not reflect Deprecated and Synthetic flags when bytecode is before 1.5
     * and those flags are represented by attributes.
     * 
     * @return the class access flags
     * 
     * @see ClassVisitor#visit(int, int, String, String, String, String[])
     */
    public int getAccess() {
        return readUnsignedShort(header);
    }

    /**
     * Returns the internal name of the class (see
     * {@link Type#getInternalName() getInternalName}).
     * 
     * @return the internal class name
     * 
     * @see ClassVisitor#visit(int, int, String, String, String, String[])
     */
    public String getClassName() {
        return readClass(header + 2, new char[maxStringLength]);
    }

    /**
     * Returns the internal of name of the super class (see
     * {@link Type#getInternalName() getInternalName}). For interfaces, the
     * super class is {@link Object}.
     * 
     * @return the internal name of super class, or <tt>null</tt> for
     *         {@link Object} class.
     * 
     * @see ClassVisitor#visit(int, int, String, String, String, String[])
     */
    public String getSuperName() {
        return readClass(header + 4, new char[maxStringLength]);
    }

    /**
     * Returns the internal names of the class's interfaces (see
     * {@link Type#getInternalName() getInternalName}).
     * 
     * @return the array of internal names for all implemented interfaces or
     *         <tt>null</tt>.
     * 
     * @see ClassVisitor#visit(int, int, String, String, String, String[])
     */
    public String[] getInterfaces() {
        int index = header + 6;
        int n = readUnsignedShort(index);
        String[] interfaces = new String[n];
        if (n > 0) {
            char[] buf = new char[maxStringLength];
            for (int i = 0; i < n; ++i) {
                index += 2;
                interfaces[i] = readClass(index, buf);
            }
        }
        return interfaces;
    }

    /**
     * Copies the constant pool data into the given {@link ClassWriter}. Should
     * be called before the {@link #accept(ClassVisitor,int)} method.
     * 
     * @param classWriter
     *            the {@link ClassWriter} to copy constant pool into.
     */
    void copyPool(final ClassWriter classWriter) {
        char[] buf = new char[maxStringLength];
        int ll = items.length;
        Item[] items2 = new Item[ll];
        for (int i = 1; i < ll; i++) {
            int index = items[i];
            int tag = b[index - 1];
            Item item = new Item(i);
            int nameType;
            switch (tag) {
            case ClassWriter.FIELD:
            case ClassWriter.METH:
            case ClassWriter.IMETH:
                nameType = items[readUnsignedShort(index + 2)];
                item.set(tag, readClass(index, buf), readUTF8(nameType, buf),
                        readUTF8(nameType + 2, buf));
                break;
            case ClassWriter.INT:
                item.set(readInt(index));
                break;
            case ClassWriter.FLOAT:
                item.set(Float.intBitsToFloat(readInt(index)));
                break;
            case ClassWriter.NAME_TYPE:
                item.set(tag, readUTF8(index, buf), readUTF8(index + 2, buf),
                        null);
                break;
            case ClassWriter.LONG:
                item.set(readLong(index));
                ++i;
                break;
            case ClassWriter.DOUBLE:
                item.set(Double.longBitsToDouble(readLong(index)));
                ++i;
                break;
            case ClassWriter.UTF8: {
                String s = strings[i];
                if (s == null) {
                    index = items[i];
                    s = strings[i] = readUTF(index + 2,
                            readUnsignedShort(index), buf);
                }
                item.set(tag, s, null, null);
                break;
            }
            case ClassWriter.HANDLE: {
                int fieldOrMethodRef = items[readUnsignedShort(index + 1)];
                nameType = items[readUnsignedShort(fieldOrMethodRef + 2)];
                item.set(ClassWriter.HANDLE_BASE + readByte(index),
                        readClass(fieldOrMethodRef, buf),
                        readUTF8(nameType, buf), readUTF8(nameType + 2, buf));
                break;
            }
            case ClassWriter.INDY:
                if (classWriter.bootstrapMethods == null) {
                    copyBootstrapMethods(classWriter, items2, buf);
                }
                nameType = items[readUnsignedShort(index + 2)];
                item.set(readUTF8(nameType, buf), readUTF8(nameType + 2, buf),
                        readUnsignedShort(index));
                break;
            // case ClassWriter.STR:
            // case ClassWriter.CLASS:
            // case ClassWriter.MTYPE
            default:
                item.set(tag, readUTF8(index, buf), null, null);
                break;
            }

            int index2 = item.hashCode % items2.length;
            item.next = items2[index2];
            items2[index2] = item;
        }

        int off = items[1] - 1;
        classWriter.pool.putByteArray(b, off, header - off);
        classWriter.items = items2;
        classWriter.threshold = (int) (0.75d * ll);
        classWriter.index = ll;
    }

    /**
     * Copies the bootstrap method data into the given {@link ClassWriter}.
     * Should be called before the {@link #accept(ClassVisitor,int)} method.
     * 
     * @param classWriter
     *            the {@link ClassWriter} to copy bootstrap methods into.
     */
    private void copyBootstrapMethods(final ClassWriter classWriter,
            final Item[] items, final char[] c) {
        // finds the "BootstrapMethods" attribute
        int u = getAttributes();
        boolean found = false;
        for (int i = readUnsignedShort(u); i > 0; --i) {
            String attrName = readUTF8(u + 2, c);
            if ("BootstrapMethods".equals(attrName)) {
                found = true;
                break;
            }
            u += 6 + readInt(u + 4);
        }
        if (!found) {
            return;
        }
        // copies the bootstrap methods in the class writer
        int boostrapMethodCount = readUnsignedShort(u + 8);
        for (int j = 0, v = u + 10; j < boostrapMethodCount; j++) {
            int position = v - u - 10;
            int hashCode = readConst(readUnsignedShort(v), c).hashCode();
            for (int k = readUnsignedShort(v + 2); k > 0; --k) {
                hashCode ^= readConst(readUnsignedShort(v + 4), c).hashCode();
                v += 2;
            }
            v += 4;
            Item item = new Item(j);
            item.set(position, hashCode & 0x7FFFFFFF);
            int index = item.hashCode % items.length;
            item.next = items[index];
            items[index] = item;
        }
        int attrSize = readInt(u + 4);
        ByteVector bootstrapMethods = new ByteVector(attrSize + 62);
        bootstrapMethods.putByteArray(b, u + 10, attrSize - 2);
        classWriter.bootstrapMethodsCount = boostrapMethodCount;
        classWriter.bootstrapMethods = bootstrapMethods;
    }

    /**
     * Constructs a new {@link ClassReader} object.
     * 
     * @param is
     *            an input stream from which to read the class.
     * @throws IOException
     *             if a problem occurs during reading.
     */
    public ClassReader(final InputStream is) throws IOException {
        this(readClass(is, false));
    }

    /**
     * Constructs a new {@link ClassReader} object.
     * 
     * @param name
     *            the binary qualified name of the class to be read.
     * @throws IOException
     *             if an exception occurs during reading.
     */
    public ClassReader(final String name) throws IOException {
        this(readClass(
                ClassLoader.getSystemResourceAsStream(name.replace('.', '/')
                        + ".class"), true));
    }

    /**
     * Reads the bytecode of a class.
     * 
     * @param is
     *            an input stream from which to read the class.
     * @param close
     *            true to close the input stream after reading.
     * @return the bytecode read from the given input stream.
     * @throws IOException
     *             if a problem occurs during reading.
     */
    private static byte[] readClass(final InputStream is, boolean close)
            throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        try {
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len == b.length) {
                    int last = is.read();
                    if (last < 0) {
                        return b;
                    }
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    c[len++] = (byte) last;
                    b = c;
                }
            }
        } finally {
            if (close) {
                is.close();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /**
     * Makes the given visitor visit the Java class of this {@link ClassReader}
     * . This class is the one specified in the constructor (see
     * {@link #ClassReader(byte[]) ClassReader}).
     * 
     * @param classVisitor
     *            the visitor that must visit this class.
     * @param flags
     *            option flags that can be used to modify the default behavior
     *            of this class. See {@link #SKIP_DEBUG}, {@link #EXPAND_FRAMES}
     *            , {@link #SKIP_FRAMES}, {@link #SKIP_CODE}.
     */
    public void accept(final ClassVisitor classVisitor, final int flags) {
        accept(classVisitor, new Attribute[0], flags);
    }

    /**
     * Makes the given visitor visit the Java class of this {@link ClassReader}.
     * This class is the one specified in the constructor (see
     * {@link #ClassReader(byte[]) ClassReader}).
     * 
     * @param classVisitor
     *            the visitor that must visit this class.
     * @param attrs
     *            prototypes of the attributes that must be parsed during the
     *            visit of the class. Any attribute whose type is not equal to
     *            the type of one the prototypes will not be parsed: its byte
     *            array value will be passed unchanged to the ClassWriter.
     *            <i>This may corrupt it if this value contains references to
     *            the constant pool, or has syntactic or semantic links with a
     *            class element that has been transformed by a class adapter
     *            between the reader and the writer</i>.
     * @param flags
     *            option flags that can be used to modify the default behavior
     *            of this class. See {@link #SKIP_DEBUG}, {@link #EXPAND_FRAMES}
     *            , {@link #SKIP_FRAMES}, {@link #SKIP_CODE}.
     */
    public void accept(final ClassVisitor classVisitor,
            final Attribute[] attrs, final int flags) {
        int u = header; // current offset in the class file
        char[] c = new char[maxStringLength]; // buffer used to read strings

        Context context = new Context();
        context.attrs = attrs;
        context.flags = flags;
        context.buffer = c;

        // reads the class declaration
        int access = readUnsignedShort(u);
        String name = readClass(u + 2, c);
        String superClass = readClass(u + 4, c);
        String[] interfaces = new String[readUnsignedShort(u + 6)];
        u += 8;
        for (int i = 0; i < interfaces.length; ++i) {
            interfaces[i] = readClass(u, c);
            u += 2;
        }

        // reads the class attributes
        String signature = null;
        String sourceFile = null;
        String sourceDebug = null;
        String enclosingOwner = null;
        String enclosingName = null;
        String enclosingDesc = null;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        int innerClasses = 0;
        Attribute attributes = null;

        u = getAttributes();
        for (int i = readUnsignedShort(u); i > 0; --i) {
            String attrName = readUTF8(u + 2, c);
            // tests are sorted in decreasing frequency order
            // (based on frequencies observed on typical classes)
            if ("SourceFile".equals(attrName)) {
                sourceFile = readUTF8(u + 8, c);
            } else if ("InnerClasses".equals(attrName)) {
                innerClasses = u + 8;
            } else if ("EnclosingMethod".equals(attrName)) {
                enclosingOwner = readClass(u + 8, c);
                int item = readUnsignedShort(u + 10);
                if (item != 0) {
                    enclosingName = readUTF8(items[item], c);
                    enclosingDesc = readUTF8(items[item] + 2, c);
                }
            } else if (SIGNATURES && "Signature".equals(attrName)) {
                signature = readUTF8(u + 8, c);
            } else if (ANNOTATIONS
                    && "RuntimeVisibleAnnotations".equals(attrName)) {
                anns = u + 8;
            } else if (ANNOTATIONS
                    && "RuntimeVisibleTypeAnnotations".equals(attrName)) {
                tanns = u + 8;
            } else if ("Deprecated".equals(attrName)) {
                access |= Opcodes.ACC_DEPRECATED;
            } else if ("Synthetic".equals(attrName)) {
                access |= Opcodes.ACC_SYNTHETIC
                        | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE;
            } else if ("SourceDebugExtension".equals(attrName)) {
                int len = readInt(u + 4);
                sourceDebug = readUTF(u + 8, len, new char[len]);
            } else if (ANNOTATIONS
                    && "RuntimeInvisibleAnnotations".equals(attrName)) {
                ianns = u + 8;
            } else if (ANNOTATIONS
                    && "RuntimeInvisibleTypeAnnotations".equals(attrName)) {
                itanns = u + 8;
            } else if ("BootstrapMethods".equals(attrName)) {
                int[] bootstrapMethods = new int[readUnsignedShort(u + 8)];
                for (int j = 0, v = u + 10; j < bootstrapMethods.length; j++) {
                    bootstrapMethods[j] = v;
                    v += 2 + readUnsignedShort(v + 2) << 1;
                }
                context.bootstrapMethods = bootstrapMethods;
            } else {
                Attribute attr = readAttribute(attrs, attrName, u + 8,
                        readInt(u + 4), c, -1, null);
                if (attr != null) {
                    attr.next = attributes;
                    attributes = attr;
                }
            }
            u += 6 + readInt(u + 4);
        }

        // visits the class declaration
        classVisitor.visit(readInt(items[1] - 7), access, name, signature,
                superClass, interfaces);

        // visits the source and debug info
        if ((flags & SKIP_DEBUG) == 0
                && (sourceFile != null || sourceDebug != null)) {
            classVisitor.visitSource(sourceFile, sourceDebug);
        }

        // visits the outer class
        if (enclosingOwner != null) {
            classVisitor.visitOuterClass(enclosingOwner, enclosingName,
                    enclosingDesc);
        }

        // visits the class annotations and type annotations
        if (ANNOTATIONS && anns != 0) {
            for (int i = readUnsignedShort(anns), v = anns + 2; i > 0; --i) {
                v = readAnnotationValues(v + 2, c, true,
                        classVisitor.visitAnnotation(readUTF8(v, c), true));
            }
        }
        if (ANNOTATIONS && ianns != 0) {
            for (int i = readUnsignedShort(ianns), v = ianns + 2; i > 0; --i) {
                v = readAnnotationValues(v + 2, c, true,
                        classVisitor.visitAnnotation(readUTF8(v, c), false));
            }
        }
        if (ANNOTATIONS && tanns != 0) {
        