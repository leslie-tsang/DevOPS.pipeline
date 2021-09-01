package org.devops.atlassian

class Bitbucket extends Atlassian_Basic {
    private _project_key

    Bitbucket(_this, basic_uri, auth_credential_id) {
        super(_this, basic_uri, auth_credential_id, [
                "access-tokens"     : "1.0",
                "api"               : "1.0",
                "audit"             : "1.0",
                "branch-utils"      : "1.0",
                "branch-permissions": "2.0",
                "build-status"      : "1.0",
                "comment-likes"     : "1.0",
                "default-reviewers" : "1.0",
                "git"               : "1.0",
                "gpg"               : "1.0",
                "insights"          : "1.0",
                "jira"              : "1.0",
                "keys"              : "1.0",
                "required-builds"   : "1.0",
                "ssh"               : "1.0",
                "sync"              : "1.0",
        ])

    }

    def project_key_init(project_key) {
        this._project_key = project_key
    }

    def project_create(project_key, project_name, project_desc = "") {
        def http_payload = [
                method: "POST",
                body  : this._this.writeJSON(returnText: true, json: [
                        "key"        : project_key,
                        "name"       : project_name,
                        "description": project_desc,
                ]),
        ]

        return this._http_req("api", "projects", http_payload)
    }

    def project_permission_init(project_key) {
        def http_payload = [
                method      : "POST",
                content_type: "application/vnd.atl.bitbucket.bulk+json",
                body        : this._this.writeJSON(returnText: true, json: [
                        [
                                "type"   : "pull-request-only",
                                "matcher": [
                                        "id"       : "release/*",
                                        "displayId": "release/*",
                                        "type"     : [
                                                "id": "PATTERN",
                                        ],
                                ],
                        ],
                        [
                                "type"   : "fast-forward-only",
                                "matcher": [
                                        "id"       : "release/*",
                                        "displayId": "release/*",
                                        "type"     : [
                                                "id": "PATTERN",
                                        ],
                                ],
                        ],
                        [
                                "type"   : "no-deletes",
                                "matcher": [
                                        "id"       : "release/*",
                                        "displayId": "release/*",
                                        "type"     : [
                                                "id": "PATTERN",
                                        ],
                                ],
                        ],
                ]),
        ]

        return this._http_req("branch-permissions", "projects/${project_key}/restrictions", http_payload)
    }

    def project_repo_create(repo_name, repo_desc = "", repo_fork = false) {
        def http_payload = [
                method: "POST",
                body  : this._this.writeJSON(returnText: true, json: [
                        "name"         : repo_name,
                        "scmId"        : "git",
                        "forkable"     : repo_fork,
                        "defaultBranch": repo_desc,
                ]),
        ]

        return this._http_req("api", "projects/${this._project_key}/repos", http_payload)
    }

    def project_repo_fetch(repo_name) {
        def http_payload = [
                method: "GET",
        ]

        return this._http_req("api", "projects/${this._project_key}/repos/${repo_name}", http_payload)
    }

    def project_repo_url_fetch(repo_name) {
        def ret = [:]
        def http_resp = this.project_repo_fetch(repo_name)

        if (http_resp.status == 404) {
            // not exist
            http_resp = this.project_repo_create(repo_name)
        }

        def http_resp_json = this._this.readJSON text: http_resp.content

        // reformat data struct
        for (item in http_resp_json['links']['clone']) {
            switch (item["name"]) {
                case "ssh":
                    ret["ssh"] = item["href"]
                    break
                case "http":
                    ret["http"] = item["href"]
                    break
            }
        }

        return ret
    }


    def project_repo_branch_create(repo_name, branch_name, branch_name_source = "master") {
        def http_payload = [
                method: "POST",
                body  : this._this.writeJSON(returnText: true, json: [
                        'name'      : branch_name,
                        'startPoint': "refs/heads/${branch_name_source}",
                ]),
        ]

        return this._http_req("branch-utils", "projects/${this._project_key}/repos/${repo_name}/branches", http_payload)
    }


    def project_repo_branch_delete(repo_name, branch_name) {
        def http_payload = [
                method: "DELETE",
                body  : this._this.writeJSON(returnText: true, json: [
                        'name'  : branch_name,
                        "dryRun": false,
                ]),
        ]

        return this._http_req("branch-utils", "projects/${this._project_key}/repos/${repo_name}/branches", http_payload)
    }

    def project_repo_branch_permission_readonly(repo_name, branch_name) {
        def http_payload = [
                method      : "POST",
                content_type: "application/vnd.atl.bitbucket.bulk+json",
                body        : this._this.writeJSON(returnText: true, json: [
                        [
                                "type"   : "read-only",
                                "matcher": [
                                        "id"       : "refs/heads/${branch_name}",
                                        "displayId": "${branch_name}",
                                        "type"     : [
                                                "id": "BRANCH",
                                        ],
                                        "active"   : true,
                                ],
                        ],
                ]),
        ]

        return this._http_req("branch-permissions", "projects/${this._project_key}/repos/${repo_name}/restrictions", http_payload)
    }

    def project_repo_branch_permission_clean(repo_name, branch_name) {
        def http_resp_search = this._http_req("branch-permissions", "projects/${this._project_key}/repos/${repo_name}/restrictions?type=read-only&matcherType=BRANCH&matcherId=refs/heads/${branch_name}", [method: "GET"])

        def http_resp_search_response = this._this.readJSON text: http_resp_search.content

        for (item in http_resp_search_response['values']) {
            this._http_req("branch-permissions", "projects/${this._project_key}/repos/${repo_name}/restrictions/${item['id']}", [method: "DELETE"])
        }
    }

}
