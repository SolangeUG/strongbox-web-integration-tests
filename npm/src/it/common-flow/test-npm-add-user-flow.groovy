def baseScript = new GroovyScriptEngine("$project.basedir/src/it").with
{
    loadScriptByName('NpmIntegrationTest.groovy')
}
this.metaClass.mixin baseScript

println "Test test-npm-add-user-flow.groovy" + "\n\n"

def npmExec
def strongboxUsername = "admin"
def nonStrongboxUsername = "notarealuser"
def successMsg = "logged in as "

// Determine OS and appropriate commands
if (System.getProperty("os.name").toLowerCase().contains("windows"))
{
    npmExec = "cmd /c npm"
}
else
{
    npmExec = "npm"
}

def executionPath = getExecutionPathRoot(project)
def adduserDependencyRoot = executionPath.resolve("npm-add-user-test")

def strongboxUser = new Credentials(strongboxUsername,"password","a@a.com")
def nonStrongboxUser = new Credentials(nonStrongboxUsername,"zipZapZooey","derp@derpy.com")

//test that npm adduser works for strongbox user
def proc = createNpmAdduserProcess(npmExec, adduserDependencyRoot.toString())
assert loginToNpm(proc, strongboxUser, successMsg+strongboxUsername)

proc.destroy()

//test that npm adduser does not work for nonstrongbox user
proc = createNpmAdduserProcess(npmExec, adduserDependencyRoot.toString())
assert !loginToNpm(proc, nonStrongboxUser, successMsg+nonStrongboxUsername)

proc.destroy()

Process createNpmAdduserProcess(String npmExec, String directory)
{
    def processBuilder = new ProcessBuilder(npmExec, "adduser")
    processBuilder.directory(new File(directory))
    return processBuilder.start()
}

boolean loginToNpm(Process proc, Credentials creds, String returnMessage)
{
    def output = new StringBuilder()
    def promptReader = new PromptReader(proc.getInputStream(), output)
    def outputThread = new Thread(promptReader)
    def input = new OutputStreamWriter(proc.getOutputStream())

    outputThread.start()

    Thread.sleep(1000)
    input.write(creds.getUsername() + "\n")
    input.flush()

    Thread.sleep(1000)
    input.write(creds.getPassword() + "\n")
    input.flush()

    Thread.sleep(1000)
    input.write(creds.getEmail() + "\n")
    input.flush()

    Thread.sleep(1000)
    input.close()
    
    return output.toString().toLowerCase().contains(returnMessage)
}

class Credentials
{
    String username
    String password
    String email

    Credentials(String username, String password, String email)
    {
        this.username = username
        this.password = password
        this.email = email
    }

    String getUsername()
    {
        return username
    }

    String getPassword()
    {
        return password
    }

    String getEmail()
    {
        return email
    }
}

class PromptReader implements Runnable
{
    InputStream is
    StringBuilder sb

    PromptReader(InputStream is, StringBuilder sb)
    {
        this.is = is
        this.sb = sb
    }

    @Override
    void run()
    {
        byte[] b = new byte[1024]
        int size = 0
        try
        {
            while ((size = is.read(b)) != -1)
            {
                println new String(b)
                sb.append(new String(b))
            }
            is.close()
        }
        catch (IOException e)
        {
            e.printStackTrace()
        }
    }
}

