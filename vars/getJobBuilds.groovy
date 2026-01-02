// vars/getJobBuilds.groovy
import jenkins.model.Jenkins
import hudson.model.Result
import com.cloudbees.hudson.plugins.folder.Folder

def call(Map config = [:]) {
    def targetJobName = config.jobName
    def maxBuilds = config.maxBuilds ?: 10
    
    def jenkins = Jenkins.get()
    def currentJob = binding.variables.get("jenkinsProject")
    def currentFullName = currentJob?.fullName ?: ""

    def rootFolderName = currentFullName.contains("/") ? currentFullName.split("/")[0] : null
    def targetJob = null

    // Arama mantığı
    if (rootFolderName) {
        def rootFolder = jenkins.getItem(rootFolderName)
        if (rootFolder instanceof Folder) {
            targetJob = rootFolder.getAllItems().find { it.name == targetJobName }
        }
    } else {
        targetJob = jenkins.getAllItems().find { it.name == targetJobName }
    }

    if (!targetJob) return ["Job not found: ${targetJobName}"]

    def out = []
    targetJob.builds.each { b ->
        if (b.result == Result.SUCCESS && out.size() < maxBuilds) {
            out << b.number.toString()
        }
    }
    return out ?: ["No Success Builds"]
}
