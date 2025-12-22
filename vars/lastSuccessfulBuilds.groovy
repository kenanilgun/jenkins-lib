import jenkins.model.Jenkins
import hudson.model.Result
import com.cloudbees.hudson.plugins.folder.Folder

def call(Map args = [:]) {
  String targetJobName = (args.targetJobName ?: "").trim()
  int count = (args.count ?: 10) as int

  if (!targetJobName) {
    return ["TARGET_JOB_NAME is empty"]
  }

  // Çağıran job'ın fullName'ini pipeline'dan gönderiyoruz (ör: env.JOB_NAME)
  String currentFullName = (args.currentJobFullName ?: "").toString()
  String rootFolderName = currentFullName.contains("/") ? currentFullName.substring(0, currentFullName.indexOf("/")) : null

  def jenkins = Jenkins.get()
  def targetJob = null

  if (rootFolderName) {
    def rootFolder = jenkins.getItem(rootFolderName)
    if (rootFolder instanceof Folder) {
      for (item in rootFolder.getAllItems()) {
        if (item.name == targetJobName) { // sadece aynı root folder + altları
          targetJob = item
          break
        }
      }
    }
  } else {
    for (item in jenkins.getAllItems()) {
      if (item.name == targetJobName) {
        targetJob = item
        break
      }
    }
  }

  if (targetJob == null) {
    return ["JOB NOT FOUND: ${targetJobName} (scope: ${rootFolderName ?: 'ROOT'})"]
  }

  def out = []
  for (b in targetJob.builds) {
    if (b.result == Result.SUCCESS) {
      out << b.number.toString()
      if (out.size() >= count) break
    }
  }

  return out ?: ["NO SUCCESS BUILDS"]
}
