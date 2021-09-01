Name
=================

DevOPS - Auto pipeline

Table of Contents
=================

* [Name](#name)
* [Description](#description)
    * [WebHook URI](#webhook-uri)
* [Dependence](#dependence)

Description
======


WebHook URI
======
```bash
# Jenkins host
CI_HOST=http://ci.example.com

# Webhook token
CI_TOKEN=devops-token-jira

printf "%s/generic-webhook-trigger/invoke?token=%s&project_key=\${project.key}\n" "$CI_HOST" "$CI_TOKEN"
```
[Back to TOC](#table-of-contents)

Dependence
======
> plugins

|              Plugin               |      | description                                                  |
| :-------------------------------: | ---- | :----------------------------------------------------------- |
|            Blue Ocean             | ☑️    | UI                                                           |
|          Docker Pipeline          | ☑️    |                                                              |
|           GitLab Plugin           |      | Allows GitLab to launch Jenkins pipeline and display their results in the GitLab UI. |
|         Publish Over SSH          | ☑️    | Send build artifacts over SSH                                |
|          Version Number           | ☑️    | Format building package version                              |
|      Embeddable Build Status      | ☑️    | Show building status                                         |
|        Crowd 2 Integration        | ☑️    | SSO                                                          |
| Role-based Authorization Strategy | ☑️    | RBAC support                                                 |
|           Git Parameter           | ☑️    |                                                              |
|      Generic Webhook Trigger      | ☑️    |                                                              |
|           HTTP Request            | ☑️    | pipeline http request support                                |
|      Pipeline Utility Steps       | ☑️    | add readJSON writeJSON support                               |
|        SSH Pipeline Steps         | ☑️    | SSH cmd support                                              |

[Back to TOC](#table-of-contents)


Example
======
```bash
# change jira and bitbucket host
## replace http://*.example.com in Jenkinsfile with your own host
## replace devops_auth_bot with your own jenkins auth credential 
vim pipeline/trigger.jira.service.Jenkinsfile

# configure system share libs
## `Manage Jenkins` -> `System Configuration` -> `Global Pipeline Libraries` -> `Library` -> `Add`

# configure auto pipeline

```
