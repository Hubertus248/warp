package net.warpgame.engine.postbuild.buildclass.jar;


import net.warpgame.engine.postbuild.buildclass.BuildClasses;
import net.warpgame.engine.postbuild.processing.Context;
import net.warpgame.engine.postbuild.processing.Source;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Jaca777
 * Created 2018-07-01 at 18
 */
public class BuildJarLoader implements Source<BuildClasses> {

    private String processedRootPackage;
    private JarFile jarFile;

    public BuildJarLoader(String processedRootPackage, JarFile jarFile) {
        this.processedRootPackage = processedRootPackage;
        this.jarFile = jarFile;
    }

    @Override
    public BuildClasses get(Context cx) throws IOException {
        BuildClasses buildClasses = new BuildClasses();
        Enumeration<JarEntry> entries = jarFile.entries();
        loadEntries(jarFile, buildClasses, entries);
        return buildClasses;
    }


    private void loadEntries(
            JarFile jarFile,
            BuildClasses buildClasses,
            Enumeration<JarEntry> entries
    ) throws IOException {
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && isClass(entry) && isInProcessedPackage(entry)) {
                ClassNode buildClass = loadBuildClass(jarFile, entry);
                buildClasses.putClass(buildClass);
            }
        }
    }

    private ClassNode loadBuildClass(
            JarFile jarFile,
            JarEntry entry
    ) throws IOException {
        try(InputStream classData = jarFile.getInputStream(entry)) {
            byte[] bytes = IOUtils.readFully(classData, (int) entry.getSize());
            ClassNode classNode = new ClassNode(Opcodes.ASM6);
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            return classNode;
        } catch (Exception e) {
            System.out.println(entry.getName());
            throw e;
        }
    }

    private boolean isInProcessedPackage(JarEntry entry) {
        return entry.getName()
                .replace('/', '.')
                .startsWith(processedRootPackage);
    }

    private static boolean isClass(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }
}
