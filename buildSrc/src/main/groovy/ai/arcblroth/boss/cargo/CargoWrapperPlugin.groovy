package ai.arcblroth.boss.cargo

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

/**
 * Plugin to wrap Cargo invocations.
 * <br>
 * It would be nice to be able to use the
 * <a href="https://github.com/mozilla/rust-android-gradle">rust-android-gradle</a>
 * plugin, but that only works for Android projects. In the future, this code
 * should probably also be separated into its own separate plugin so other
 * people can use it, but for now a hacky solution will do :yeef:
 */
class CargoWrapperPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.configurations.create("default")
        project.extensions.create("cargo", CargoExtension.class)
        project.afterEvaluate {
            def buildTask = project.tasks.register("build", CargoTask.class)
            project.artifacts.add("default", buildTask)
        }
    }
}

class CargoExtension {
    /**
     * Location of the crate to build.
     * This defaults to the current project directory.
     */
    String crate = null

    /**
     * List of the locations of the outputs executables/libraries.
     * Each entry in this list is a pair between a target triple
     * and the name of the output file. Use "" (the empty string)
     * to specify the default target.
     */
    List<Tuple2<String, String>> outputs = []

    /**
     * Build profile to use. If this is not set to "debug",
     * the plugin assumes a release profile.
     */
    String profile = "debug"

    /**
     * Additional arguments to pass to Cargo.
     */
    List<String> arguments = []

    /**
     * Additional environmental variables to set while
     * launching Cargo.
     */
    Map<String, String> environment = [:]
}

/**
 * Main Cargo wrapper task.
 */
class CargoTask extends DefaultTask {

    private List<String> commandLine
    private Map<String, String> environment

    /**
     * This is an imperfect solution to incremental builds:
     * if a change is made to the source code, this directory
     * will change, and Gradle will run Cargo. But once the
     * first execution runs, the directory will change, and
     * Gradle will need to invoke Cargo a second time even if
     * no rebuild is necessary. Luckily, Cargo itself is pretty
     * fast at determining whether or not a rebuild is needed,
     * and will not modify this directory if no rebuild is
     * performed, thus allowing this task to be skipped if it
     * is invoked a third time.
     */
    @InputDirectory
    private File workingDir

    @OutputFiles
    private List<File> outputFiles

    CargoTask() {
        def config = project.extensions.getByType(CargoExtension.class)

        this.commandLine = []
        if (config.profile != "debug") {
            this.commandLine.add("--release")
        }
        config.arguments.forEach { this.commandLine.add(it) }
        this.commandLine.add("build")

        this.environment = config.environment

        this.workingDir = config.crate != null ? project.file(config.crate) : project.projectDir
        def targetDir = new File(this.workingDir, "target")
        this.outputFiles = config.outputs.collect {
            new File(targetDir, (it.v1.isEmpty() ? "" : it.v1 + File.separator) + config.profile + File.separator + it.v2)
        }
    }

    @TaskAction
    void build() {
        // These have to fetched here rather than
        // in the closure below, because Groovy's
        // handling of modifiers makes no sense
        def commandLine = this.commandLine
        def environment = this.environment
        def workingDir = this.workingDir
        project.exec { ExecSpec spec ->
            spec.commandLine("cargo")
            spec.args(commandLine)
            spec.workingDir(workingDir)
            spec.environment(environment)
        }.assertNormalExitValue()
    }

    File getWorkingDir() {
        return this.workingDir
    }

    List<File> getOutputFiles() {
        return this.outputFiles
    }
}