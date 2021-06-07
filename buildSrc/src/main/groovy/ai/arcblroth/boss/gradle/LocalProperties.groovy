package ai.arcblroth.boss.gradle

import org.gradle.api.Project

/**
 * Adds a meta-method to the root project with the signature
 * <blockquote><code>String getExecutableFromProperties(String name, String pathKey = name + "Path")</code></blockquote>
 * that resolves the absolute path to an executable with the given name based on the path provided
 * in the <code>local.properties</code> file of the root project. If no value is associated with
 * the given path key in the <code>local.properties</code> file, then the method will just return
 * <code>name</code>.
 * @param project Project to fetch the root project from.
 */
static def initLocalProperties(Project project) {
    def rootProject = project.rootProject
    def local = new Properties()
    if (rootProject.file("local.properties").exists()) {
        local.load(rootProject.file("local.properties").newDataInputStream())
    }
    rootProject.metaClass.getExecutableFromProperties = { String name, String pathKey = name + "Path" ->
        if(local.containsKey(pathKey)) {
            return new File(local.getProperty(pathKey), name).getAbsolutePath()
        } else {
            return name
        }
    }
}
