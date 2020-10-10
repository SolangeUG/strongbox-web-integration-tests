import org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator

def baseScript = new GroovyScriptEngine( "$project.basedir/src/it" ).with {
    loadScriptByName( 'BaseNugetWebIntegrationTest.groovy' )
  }
this.metaClass.mixin baseScript

println "Test test-install-with-transitive-deps.groovy" + "\n\n"

def targetPath = getTargetPath(project)
def baseDir = targetPath.toString()
def configPath = "$baseDir/NuGet.config"

def nugetExec = System.getenv("NUGET_EXEC")
assert nugetExec?.trim() : "\"NUGET_EXEC\" environment variable need to be set"

def packageExtension = "nupkg"

def nugetArtifactGenerator = new NugetArtifactGenerator(baseDir)
nugetArtifactGenerator.generate("Org.Carlspring.Swit.Tiwtd.Transitive", "1.0.0", packageExtension)
nugetArtifactGenerator.generate("Org.Carlspring.Swit.Tiwtd", "1.0.0", packageExtension, "Org.Carlspring.Swit.Tiwtd.Transitive:1.0.0")

def storageUrl = getStorageUrl()

runCommand(targetPath, String.format(
    "$nugetExec push %s/%s/%s -ConfigFile %s",
    "Org.Carlspring.Swit.Tiwtd.Transitive",
    "1.0.0",
    "Org.Carlspring.Swit.Tiwtd.Transitive.1.0.0.nupkg",
    configPath))

runCommand(targetPath, String.format(
    "$nugetExec push %s/%s/%s -ConfigFile %s",
    "Org.Carlspring.Swit.Tiwtd",
    "1.0.0",
    "Org.Carlspring.Swit.Tiwtd.1.0.0.nupkg",
    configPath))

runCommand(targetPath.resolve(".."), String.format(
    "$nugetExec install %s -ConfigFile %s -source %s",
    "Org.Carlspring.Swit.Tiwtd",
    configPath,
    storageUrl))

assert targetPath.resolve("..").resolve("Org.Carlspring.Swit.Tiwtd.1.0.0").resolve("Org.Carlspring.Swit.Tiwtd.1.0.0.nupkg").toFile().exists()
assert targetPath.resolve("..").resolve("Org.Carlspring.Swit.Tiwtd.Transitive.1.0.0").resolve("Org.Carlspring.Swit.Tiwtd.Transitive.1.0.0.nupkg").toFile().exists()
