#!groovy

@Library('devops@dev')
import org.devops.*

def jira = new atlassian.Jira(this, "http://issue.example.com", 'devops_auth_bot')
def bitbucket = new atlassian.Bitbucket(this, "http://git.example.com", 'devops_auth_bot')
def utils = new utils.Utils(this)


pipeline {
    agent {
        node {
            label "master"
        }
    }

    options {
        quietPeriod(0)                  // disable global quietPeriod
        skipDefaultCheckout()           // disable default scm checkout behavior
        timeout(time: 1, unit: 'HOURS') // pipeline time 1 hour
        timestamps()                    // output console log with timestamp
    }

    triggers {
        GenericTrigger(causeString: 'Generic Cause',
                genericVariables: [
                        [defaultValue: '', key: 'webhook_payload', regexpFilter: '', value: '$'],
                ],
                genericRequestVariables: [
                        [key: 'user_id', regexpFilter: ''],
                        [key: 'project_key', regexpFilter: ''],
                ],
                // printContributedVariables: true,
                printPostContent: true,
                regexpFilterExpression: '^$',
                regexpFilterText: '',
                silentResponse: true,
                token: 'devops-token-jira')
    }

    stages {
        stage("jira -> fetch request data") {
            steps {
                script {
                    payload_json = readJSON text: webhook_payload
                    env.hook_event = payload_json["webhookEvent"]
                    env.hook_event_type = payload_json["issue_event_type_name"]

                    currentBuild.description = "project -> ${project_key}"
                    currentBuild.description += "\nuser -> ${user_id} -> ${env.hook_event}"

                    // init DevOPS git project setting
                    bitbucket.project_key_init(project_key)
                }
            }
        }

        stage("jira -> issue") {
            when {
                anyOf {
                    environment name: 'hook_event', value: 'jira:issue_created'
                    environment name: 'hook_event', value: 'jira:issue_updated'
                    environment name: 'hook_event', value: 'jira:issue_deleted'
                }
            }

            steps {
                script {
                    env.issue_id = payload_json["issue"]["id"]
                    env.issue_key = payload_json["issue"]["key"]
                    env.issue_type_id = payload_json["issue"]["fields"]["issuetype"]["id"]

                    jira.issue_id_init(env.issue_id)

                    switch (env.hook_event) {
                        case "jira:issue_created":
                            utils.info("issue_created ${env.issue_type_id} -> ${env.issue_id}")

                            def v_issue_comment = ""
                            def v_issue_component_cache = payload_json["issue"]["fields"]["components"]
                            def v_issue_version_cache = payload_json["issue"]["fields"]["fixVersions"]
                            def v_git_branch_prefix = jira.issue_type[env.issue_type_id]["branch_prefix"]
                            def v_git_branch_name = "${v_git_branch_prefix}/${env.issue_key}"

                            for (item_component in v_issue_component_cache) {
                                // ensure repo exist
                                def repo_uri = bitbucket.project_repo_url_fetch(item_component["name"])
                                v_issue_comment += "\nmodule [${item_component['name']}|${repo_uri['http']}]"

                                for (item_version in v_issue_version_cache) {
                                    // create bugfix branch from release branch
                                    bitbucket.project_repo_branch_create(item_component["name"], v_git_branch_name, "release/${item_version['name']}")

                                    // insert bugfix git branch fetch cmd
                                    v_issue_comment += "\nmodule [${item_component['name']}|${repo_uri['http']}] fix version -> ${item_version['name']}"
                                    v_issue_comment += """
{code:bash}
# git clone
git clone --branch ${v_git_branch_name} ${repo_uri['ssh']}

# git checkout
git checkout ${v_git_branch_name}
{code}
"""
                                }

                            }

                            jira.issue_comment("接管成功 \n ${v_issue_comment}")
                            break

                        case "jira:issue_updated":
                            utils.info("issue_updated -> ${env.hook_event_type}")

                            switch (env.hook_event_type) {
                                case "issue_updated":
                                    for (item in payload_json["changelog"]["items"]) {
                                        println("msg -> ${item['field']} ${item['toString']}")
                                    }
                                    break

                                case "issue_commented":
                                    break

                                case "issue_generic":
                                    env.project_key = payload_json["issue"]["fields"]["project"]["key"]
                                    env.issue_status_id = payload_json["issue"]["fields"]["status"]["id"]
                                    env.issue_status_name = payload_json["issue"]["fields"]["status"]["name"]

                                    switch (env.issue_status_id) {
                                        case "3":     // processing
                                            break
                                        case "10000": // pending
                                            break
                                        case "10001": // done
                                            break
                                        case "10100": // backlog
                                            break
                                        case "10101": // deploy
                                            break
                                        default:
                                            currentBuild.description += "\n unknown state -> ${env.issue_status_id} -> ${env.issue_status_name}"
                                    }

                                    currentBuild.description += "\n state -> ${env.issue_status_id} -> ${env.issue_status_name}"
                                    break

                                case "issue_comment_deleted":
                                    currentBuild.description += "\n issue comment deleted"
                                    break

                                default:
                                    utils.info("no hook_event_type matched -> ${env.hook_event_type}")
                            }
                            break

                        case "jira:issue_deleted":
                            utils.info("issue_deleted")
                            break

                        default:
                            utils.info("no event matched -> ${env.hook_event}")
                    }
                }
            }

        }

        stage("jira -> version") {
            when {
                anyOf {
                    environment name: 'hook_event', value: 'jira:version_created'
                    environment name: 'hook_event', value: 'jira:version_released'
                    environment name: 'hook_event', value: 'jira:version_unreleased'
                    environment name: 'hook_event', value: 'jira:version_deleted'
                }
            }

            steps {
                script {
                    env.webhook_version = payload_json["version"]["name"]
                    env.webhook_version_id = payload_json["version"]["id"]
                    env.webhook_version_ops_name = "release/${env.webhook_version}"

                    switch (env.hook_event) {
                        case "jira:version_created":
                            utils.info("version_created -> ${env.webhook_version} -> ${env.webhook_version_id}")

                            for (item in jira.project_fetch_component_name_list(project_key)) {
                                bitbucket.project_repo_branch_create(item, env.webhook_version_ops_name)
                            }

                            jira.version_comment(env.webhook_version_id, env.webhook_version, "DevOPS -> Branch ${env.webhook_version_ops_name}")
                            currentBuild.description += "\n branch create -> ${env.webhook_version_ops_name}"
                            break

                        case "jira:version_released":
                            utils.info("version_released -> ${env.webhook_version_ops_name}")

                            for (item in jira.project_fetch_component_name_list(project_key)) {
                                bitbucket.project_repo_branch_permission_readonly(item, env.webhook_version_ops_name)
                            }

                            currentBuild.description += "\n branch release -> ${env.webhook_version_ops_name}"
                            break

                        case "jira:version_unreleased":
                            utils.info("version_unreleased -> ${env.webhook_version_ops_name}")

                            for (item in jira.project_fetch_component_name_list(project_key)) {
                                bitbucket.project_repo_branch_permission_clean(item, env.webhook_version_ops_name)
                            }

                            currentBuild.description += "\n branch unreleased -> ${env.webhook_version_ops_name}"
                            break

                        case "jira:version_deleted":
                            utils.info("version_unreleased -> ${env.webhook_version_ops_name}")

                            for (item in jira.project_fetch_component_name_list(project_key)) {
                                bitbucket.project_repo_branch_delete(item, env.webhook_version_ops_name)
                            }

                            currentBuild.description += "\n branch deleted -> ${env.webhook_version_ops_name}"
                            break

                        default:
                            utils.info("no event matched -> ${env.hook_event}")
                    }

                }
            }
        }

        stage("jira -> project") {
            when {
                anyOf {
                    environment name: 'hook_event', value: 'project_created'
                }
            }

            steps {
                script {
                    env.project_id = payload_json["project"]["id"]
                    env.project_key = payload_json["project"]["key"]
                    env.project_name = payload_json["project"]["name"]

                    switch (env.hook_event) {
                        case "project_created":
                            bitbucket.project_create(env.project_key, env.project_name)
                            bitbucket.project_permission_init(env.project_key)
                            break

                        default:
                            utils.info("no event matched -> ${env.hook_event}")
                    }

                }
            }
        }
    }

    post {
        always {
            script {
                if(env.hook_event == "jira:issue_created"){
                    mail(subject: "DevOPS - Auto report - ${env.issue_key}",
                        to: "${payload_json["user"]["emailAddress"]}",
                        body: "report email\n title: ${payload_json['issue']['fields']['summary']}\n ${currentBuild.description} \n")
                }
            }
        }
    }

}
