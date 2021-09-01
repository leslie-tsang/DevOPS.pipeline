#!groovy

@Library('devops')

import org.devops.Test

def jira  = new Test(this, "thisaa")
//def utils = new org.devops.utils()

def G_WORKBENCH = ""
def G_GIT_REPO = "ssh://git@git.example.com:7999/dev/test.git"
def G_GIT_CredentialsID = "ssh_auth_devops"


pipeline {
    agent any

    environment {
        CC = "gcc"
    }

    options {
        timestamps() // output console log with timestamp
        skipDefaultCheckout() // disable default scm checkout behavior
        timeout(time: 1, unit: 'HOURS') // pipeline time 1 hour
    }

//    parameters {
//        gitParameter(name: 'G_BRANCH',
//                     type: 'PT_BRANCH',
//                     branchFilter: 'origin/(.*)',
//                     defaultValue: 'master',
//                     useRepository: "${G_GIT_REPO}")
//    }

    triggers {
        GenericTrigger( causeString: 'Generic Cause',
            genericVariables: [
                // gitlab
                [defaultValue: '', key: 'branchName', regexpFilter: '', value: '$.ref'],
                [defaultValue: '', key: 'object_kind', regexpFilter: '', value: '$.object_kind'],
                [defaultValue: '', key: 'before', regexpFilter: '', value: '$.before'],
                [defaultValue: '', key: 'after', regexpFilter: '', value: '$.after'],
                // bitbucket
                [defaultValue: '', key: 'actor_name', regexpFilter: '', value: '$.actor.name'],
                [defaultValue: '', key: 'branch_name', regexpFilter: '', value: '$.changes[0].ref.displayId'],
                [defaultValue: '', key: 'branch_hash_from', regexpFilter: '', value: '$.changes[0].fromHash'],
                [defaultValue: '', key: 'branch_hash_cur', regexpFilter: '', value: '$.changes[0].toHash'],
                // bitbucket debug payload -> {"test": true}
                [defaultValue: '', key: 'debug_mode', regexpFilter: '', value: '$.test'],
            ],
            genericRequestVariables: [
                [key: 'runOpts', regexpFilter: ''],
            ],
            printContributedVariables: true,
            printPostContent: true,
            regexpFilterExpression: '^$',
            regexpFilterText: '$debug_mode',
            silentResponse: true,
            token: 'devops-token-basic')
    }

    stages {
        stage('Git -> check out') {
            steps {
                script {
                    println "Git -> check out"
                    jira.abccc()
                    println "Git -> check out1"
                }

//                git(url: "${G_GIT_REPO}",
//                    branch: "${params.G_BRANCH}",
//                    credentialsId: "${G_GIT_CredentialsID}")
            }
        }
//        stage('Git -> compile') {
//            steps {
//                sh(script: 'make build')
//            }
//        }
    }

    post {
        always {
            script {
                println("always")
                currentBuild.description = "user -> ${actor_name}"
            }
        }

        success {
            script {
                println("success")
                currentBuild.description += "\n success"
            }
        }

        changed {
            script {
                println("changed")
                currentBuild.description += "\n changed"
            }
        }

        failure {
            script {
                println("failure")
                currentBuild.description += "\n failure"
            }
        }

        unstable {
            script {
                println("unstable")
                currentBuild.description += "\n unstable"
            }
        }

        aborted {
            script {
                println("aborted")
                currentBuild.description += "\n aborted"
            }
        }
    }

}
