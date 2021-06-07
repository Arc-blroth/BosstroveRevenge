package ai.arcblroth.boss.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

import java.util.stream.Collectors

/**
 * Builds, links, and optimizes a set of GLSL shaders
 * under Vulkan semantics.
 */
class BuildShadersTask extends DefaultTask {

    /**
     * Map between a shader's source code file and
     * the name of the shader entrypoint function.
     */
    @Input
    Map<File, String> shaders = [:]

    /**
     * Output directory of the final
     * <code>shader.spv</code>
     * file as well as any intermediates
     * produced during the build.
     */
    @OutputDirectory
    File outputDir = null

    /**
     * Additional arguments to pass to
     * <code>glslangValidator</code>.
     */
    @Input
    List<String> validatorArgs = []

    /**
     * Additional arguments to pass to
     * <code>spirv-link</code>.
     */
    @Input
    List<String> linkerArgs = []

    /**
     * Additional arguments to pass to
     * <code>spirv-opt</code>.
     */
    @Input
    List<String> optimizerArgs = []

    @InputFiles
    Iterable<File> getShaderFiles() {
        return shaders.keySet()
    }

    @TaskAction
    void build() {
        // Compile!
        def compiledShaders = this.shaders.entrySet().stream().map {
            compileShader(project.file(it.key), it.value).getPath()
        }.collect(Collectors.toList())

        // Link!
        def linkerArgs = this.linkerArgs
        def outputFile = new File(this.outputDir, "shader.spv")
        project.exec { ExecSpec spec ->
            spec.commandLine(project.rootProject.getExecutableFromProperties("spirv-link", "spirvLink"))
            spec.args(linkerArgs + compiledShaders + ["-o", outputFile])
        }.assertNormalExitValue()

        // Optimize!
        def optimizerArgs = this.optimizerArgs
        if(optimizerArgs.isEmpty()) {
            optimizerArgs = ["-O"]
        }
        project.exec { ExecSpec spec ->
            spec.commandLine(project.rootProject.getExecutableFromProperties("spirv-opt", "spirvOpt"))
            spec.args(optimizerArgs + [outputFile.getPath(), "-o", outputFile.getPath()])
        }.assertNormalExitValue()
    }

    File compileShader(File shader, String entrypointName) {
        def outputFile = new File(this.outputDir, entrypointName + ".spv")
        def validatorArgs = this.validatorArgs
        project.exec { ExecSpec spec ->
            spec.commandLine(project.rootProject.getExecutableFromProperties("glslangValidator"))
            spec.args(["-V", "-e", entrypointName, "--sep", "main", "-o", outputFile.getPath()] + validatorArgs + [shader.getPath()])
        }.assertNormalExitValue()
        return outputFile
    }

}
