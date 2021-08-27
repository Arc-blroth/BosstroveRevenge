package ai.arcblroth.boss.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

import java.util.function.Consumer

/**
 * Patches the given class file at build time.
 */
class ClassPatchTask extends DefaultTask {
    /**
     * The class file to patch.
     */
    private File classFile

    /**
     * A function that takes in the class as a
     * ClassNode and transforms that node.
     */
    @Internal
    private Consumer<ClassNode> transform

    @InputFile
    File getClassFile() {
        return classFile
    }

    @OutputFile
    File getOutputFile() {
        return getClassFile()
    }

    void classFile(File classFile) {
        this.classFile = classFile
    }

    Consumer<ClassNode> getTransform() {
        return transform
    }

    void transform(Consumer<ClassNode> transform) {
        this.transform = transform
    }

    @TaskAction
    void execute() {
        def classReader = null
        this.classFile.withInputStream {
            classReader = new ClassReader(it)
        }
        def classNode = new ClassNode()
        classReader.accept(classNode, 0)
        this.transform.accept(classNode)
        def classWriter = new ClassWriter(0)
        classNode.accept(classWriter)
        this.classFile.withOutputStream {
            it.write(classWriter.toByteArray())
        }
    }
}
