// Jenkins pipeline to deploy helm-simple-web

helmDeployName = 'helm-simple-web'
helmNamespace = 'yonatan'
helmModes = ['install', 'uninstall']
helmDir = 'simple-web-etoro'
helmGitRepo = "https://github.com/yonikashi/${helmDir}.git"
defineProperties()

node {
    stage('Pull helm chart') {
        git url: helmGitRepo
    }
    stage('Deploy Helm') {
        try {
            dir(helmDir) {
                vmLogin()
                helmBaseCommand = "helm ${params.deployMode} ${helmDeployName}"
                if (params.deployMode == 'install') {
                    sh "${helmCommand} ${helmDeployName}/ --namspace ${helmNamespace} --values helm-simple-web/values.yaml"
                } else if (params.deployMode == 'uninstall') {
                    sh "${helmCommand} --namspace ${helmNamespace}"
                }
            }
        } catch (execption) {
            echo "ERROR: Failed to run deployment: ${execption}"
            currentBuild.result = 'FAILURE'
        }
    }
}

def vmLogin() {
    sh '''
        az login -i
        az aks get-credentials -n devops-interview-aks -g devops-interview-rg
        export KUBECONFIG=~/.kube/config
        kubelogin convert-kubeconfig -l msi
    '''
}

def defineProperties() {
    properties([
        parameters([
            choice(
                name: 'deployMode',
                description: 'Select the mode to run the helm chart',
                choices: helmModes
            )
        ])
    ])
}
