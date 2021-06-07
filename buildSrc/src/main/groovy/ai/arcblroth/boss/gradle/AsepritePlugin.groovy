package ai.arcblroth.boss.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

import javax.inject.Inject

/**
 * Plugin to automagically export Aseprite files into spritesheets.
 * Implicitly requires the <code>java</code> plugin to be applied first.
 * <br><br>
 * <small>Aseprite is (c) 2001-2021 Igara Studio S.A.</small>
 */
class AsepritePlugin implements Plugin<Project> {

    private ObjectFactory objectFactory
    private SourceDirectorySet aseprite

    @Inject
    AsepritePlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory
    }

    @Override
    void apply(Project project) {
        def main = project.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        this.aseprite = objectFactory.sourceDirectorySet("aseprite", main.displayName + " aseprite resources")
        this.aseprite.srcDir(new File(project.projectDir, "src/main/aseprite"))
        this.aseprite.filter.include("**/*.aseprite", "**/*.ase")
        this.aseprite.destinationDirectory.set(new File(project.buildDir, "resources/main/assets/"))
        main.extensions.add("aseprite", this.aseprite)

        project.extensions.create("aseprite", AsepriteExtension.class)

        project.afterEvaluate {
            def buildTask = project.tasks.create("buildAsepriteSpritesheets", AsepriteBuildTask.class) {
                setSourceSet(this.aseprite)
            }
            project.tasks.getByName("processResources").dependsOn(buildTask)
        }
    }
}

class AsepriteExtension {
    /**
     * Algorithm used to create each sprite sheet.
     * Must be one of "horizontal", "vertical",
     * "rows", "column", or "packed".
     */
    String sheetType = "packed"
}

class AsepriteBuildTask extends DefaultTask {

    private SourceDirectorySet src

    protected setSourceSet(SourceDirectorySet src) {
        this.src = src
    }

    @TaskAction
    buildSpritesheets() {
        def destination = src.destinationDirectory.get().getAsFile()
        if (!destination.exists()) {
            destination.mkdirs()
        }

        def sheetType = project.getExtensions().getByType(AsepriteExtension.class).sheetType
        def jsonSlurper = new JsonSlurper()
        this.src.forEach {
            def (sheet, meta) = getOutput(it)
            project.exec { ExecSpec spec ->
                spec.commandLine(project.rootProject.getExecutableFromProperties("aseprite"))
                spec.args(
                        "-b",
                        "--sheet", sheet.getAbsolutePath(),
                        "--sheet-type", sheetType,
                        "--data", meta.getAbsolutePath(),
                        it.getAbsolutePath()
                )
            }.assertNormalExitValue()

            // We do some extra processing on the meta json file
            // to strip unneeded fields and save a bit of space.
            def metaObj = jsonSlurper.parse(meta)
            metaObj.frames = metaObj["frames"].values().collect {
                ["frame": it["frame"], "duration": it["duration"]]
            }
            metaObj["image"] = metaObj["meta"]["image"]
            metaObj["size"] = metaObj["meta"]["size"]
            metaObj.remove("meta")

            meta.write(JsonOutput.toJson(metaObj))
        }
    }

    /**
     * Gets the output files for a given input invocation to Aseprite.
     * @param ase Input aseprite file.
     * @return A tuple containing the output spritesheet png file
     *         and output meta json file.
     */
    protected Tuple2<File, File> getOutput(File ase) {
        def output = this.src.destinationDirectory.get()
                .file(this.src.srcDirs[0].relativePath(ase))
                .getAsFile()
        return Tuple2.of(
                new File(getFileWithoutExtension(output) + ".png"),
                new File(getFileWithoutExtension(output) + ".json")
        )
    }

    static String getFileWithoutExtension(File file) {
        String name = file.getName()
        String path = file.getPath()
        if (name.lastIndexOf(".") == -1) {
            return path
        } else {
            return path.substring(0, path.lastIndexOf('.'))
        }
    }

    @InputFiles
    Iterable<File> getInputFiles() {
        return this.src
    }

    @OutputFiles
    Iterable<File> getOutputFiles() {
        this.src.collect {
            this.getOutput(it)
        }.flatten()
    }

}