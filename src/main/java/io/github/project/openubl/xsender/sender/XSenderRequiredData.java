package io.github.project.openubl.xsender.sender;

import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;

public class XSenderRequiredData {

    private SunatUrlsEntity urls;
    private SunatCredentialsEntity credentials;

    public XSenderRequiredData(SunatUrlsEntity urls, SunatCredentialsEntity credentials) {
        this.urls = urls;
        this.credentials = credentials;
    }

    public SunatUrlsEntity getUrls() {
        return urls;
    }

    public void setUrls(SunatUrlsEntity urls) {
        this.urls = urls;
    }

    public SunatCredentialsEntity getCredentials() {
        return credentials;
    }

    public void setCredentials(SunatCredentialsEntity credentials) {
        this.credentials = credentials;
    }
}
