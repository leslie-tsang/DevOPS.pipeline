package org.devops.atlassian

class Jira extends Atlassian_Basic {
    public final issue_type
    private String _issue_id

    Jira(_this, basic_issue_url, auth_credential_id) {
        super(_this, basic_issue_url, auth_credential_id, [
                "api": "2",
        ])

        this.issue_type = [
                null   : ["type": "Empty", "branch_prefix": ""],
                "10307": ["type": "Epic", "branch_prefix": "release"],
                "10315": ["type": "Story", "branch_prefix": ""],
                "10318": ["type": "Task", "branch_prefix": "feature"],
                "10303": ["type": "Bug", "branch_prefix": "bugfix"],
        ]
    }

    def issue_id_init(issue_id) {
        this._issue_id = issue_id
    }

    def issue_comment(comment) {
        def http_payload = [
                method: "POST",
                body  : this._this.writeJSON(returnText: true, json: [
                        'body': "{panel:title=DevOPS bot}\n${comment}\n{panel}",
                ]),
        ]

        return this._http_req("api", "issue/${this._issue_id}/comment", http_payload)
    }

    def issue_comment_git_info_printf(repo_uri, repo_branch) {
        return """
{code:bash}
# git clone
git clone --branch ${repo_branch} ${repo_uri}

# git checkout
git fetch
git checkout ${repo_branch}
{code}
"""
    }

    def issue_worklog(comment) {
        def http_payload = [
                method: "POST",
                body  : this._this.writeJSON(returnText: true, json: [
                        'comment': "{panel:title=DevOPS bot}\n${comment}\n{panel}",
                ]),
        ]

        return this._http_req("api", "issue/${this._issue_id}/worklog", http_payload)
    }

    def issue_search() {
        def http_payload = [
                method: "GET",
        ]

        def JQL = "project = ${projectKey} AND fixVersion = ${versionName} AND issuetype = Task"

        this._this.println(URLEncoder.encode(JQL, "UTF-8"))

        return this._http_req("api", "search/${this._issue_id}/comment", http_payload)
    }

    def version_comment(version_id, name, comment) {
        def http_payload = [
                method: "PUT",
                body  : this._this.writeJSON(returnText: true, json: [
                        'id'         : version_id,
                        'name'       : name,
                        'description': comment,
                ]),
        ]

        return this._http_req("api", "version/${version_id}", http_payload)
    }

    def project_fetch_component(project_key) {
        def http_payload = [
                method: "GET",
        ]

        return this._http_req("api", "project/${project_key}/components", http_payload)
    }


    def project_fetch_component_name_list(project_key) {
        def ret_list = []
        def http_resp = this.project_fetch_component(project_key)
        def http_resp_json = this._this.readJSON text: http_resp.content

        // reformat data struct
        for (item in http_resp_json) {
            ret_list.add(item["name"])
        }

        return ret_list
    }

}
