package io.softwarebyandroid.build;

import com.android.build.api.instrumentation.*;
import org.objectweb.asm.*;

/** Strip content-bearing diagnostics from the dependency in every build variant. */
public abstract class SvgPrivacyVisitorFactory implements AsmClassVisitorFactory<InstrumentationParameters.None> {
    @Override public boolean isInstrumentable(ClassData data) {
        return data.getClassName().startsWith("com.caverock.androidsvg.");
    }
    @Override public ClassVisitor createClassVisitor(ClassContext context, ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC && owner.equals("android/util/Log") &&
                            (name.equals("v") || name.equals("d") || name.equals("i") || name.equals("w") ||
                             name.equals("e") || name.equals("wtf") || name.equals("println")) &&
                            Type.getReturnType(descriptor).equals(Type.INT_TYPE)) {
                            // Preserve argument evaluation/stack balance, discard all content, return neutral status.
                            Type[] args = Type.getArgumentTypes(descriptor);
                            for (int i = args.length - 1; i >= 0; i--)
                                super.visitInsn(args[i].getSize() == 2 ? Opcodes.POP2 : Opcodes.POP);
                            super.visitInsn(Opcodes.ICONST_0);
                        } else super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        };
    }
}
