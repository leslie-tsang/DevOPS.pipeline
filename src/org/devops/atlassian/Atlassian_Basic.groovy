package org.devops.atlassian

import org.devops.utils.*

class Atlassian_Basic implements Serializable {
    final _this
    final _basic_uri
    final _auth_credential_id

    final _api_version

    Atlassian_Basic(_this, basic_uri, auth_credential_id, api_version) {
        this._this = _this
        this._basic_uri = basic_uri
        this._auth_credential_id = auth_credential_id

        this._api_version = api_version
    }

    def _http_req(req_api, req_api_cmd, req_payload) {
        def req = [
                method      : req_payload.method,
                content_type: req_payload.content_type,
                auth_id     : this._auth_credential_id,
                uri         : String.format("%s/rest/%s/%s/%s", this._basic_uri, req_api, this._api_version[req_api], req_api_cmd),
                body        : req_payload.body,
                debug       : true,
        ]

        def _http_resp = Utils.http_req(_this, req)

        return _http_resp
    }

}

