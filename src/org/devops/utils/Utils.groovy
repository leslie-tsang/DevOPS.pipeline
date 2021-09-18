package org.devops.utils

class Utils implements Serializable {
    private final _this

    Utils(_this) {
        this._this = _this
    }

    def info(msg) {
        _this.println(msg)
    }

    static def http_req(_script, req, req_valid_response_code = "100:999") {
        /*
            req = [
                method: "POST",
                uri: "http://host:port/uri",
                auth_id: "auth_default_id",
                content_type: "",
                body: "data",
                debug: false,
            ]
        */
        def req_content_type = req.content_type?.trim()

        // debug req
        if (req.getOrDefault("debug", false)) {
            _script.println("req_uri -> ${req.method} -> ${req.uri}\nreq_content_type -> ${req.content_type}\nreq_body -> `${req.body}`")
        }

        def _http_resp = _script.httpRequest(authentication: req.auth_id,
                httpMode: req.method,
                contentType: req_content_type ? "NOT_SET" : "APPLICATION_JSON_UTF8",
                customHeaders: req_content_type ? [[name: 'Content-Type', value: req.content_type]] : [],
                consoleLogResponseBody: true,
                ignoreSslErrors: true,
                requestBody: req.body,
                validResponseCodes: req_valid_response_code,
                url: req.uri,
        )

        return _http_resp
    }

    def remote_exec(host_ip, host_username, host_password, shell_cmd, host_port = 22, allowAnyHosts = true, passphrase = "") {
        def remote = [
                name         : "${host_username}@${host_ip}:${host_port}",
                host         : host_ip,
                port         : host_port,
                user         : host_username,
                password     : host_password,
                passphrase   : "",
                allowAnyHosts: allowAnyHosts,
                appendName   : true,
                logLevel     : "INFO",
        ]

        return this._this.sshCommand(remote: remote, command: shell_cmd)
    }

}
