/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.keys.component;

import org.keycloak.common.util.MultivaluedHashMap;

import java.util.concurrent.ConcurrentHashMap;

public class ComponentModel {

    private String id;
    private String name;
    private String providerId;
    private String providerType;
    private String parentId;
    private String subType;
    private org.keycloak.common.util.MultivaluedHashMap<String, String> config = new org.keycloak.common.util.MultivaluedHashMap<>();
    private transient ConcurrentHashMap<String, Object> notes = new ConcurrentHashMap<>();

    public ComponentModel() {}

    public ComponentModel(ComponentModel copy) {
        this.id = copy.id;
        this.name = copy.name;
        this.providerId = copy.providerId;
        this.providerType = copy.providerType;
        this.parentId = copy.parentId;
        this.subType = copy.subType;
        this.config.addAll(copy.config);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public org.keycloak.common.util.MultivaluedHashMap<String, String> getConfig() {
        return config;
    }

    public void setConfig(MultivaluedHashMap<String, String> config) {
        this.config = config;
    }

    public boolean contains(String key) {
        return config.containsKey(key);
    }

    public String get(String key) {
        return config.getFirst(key);
    }

    public String get(String key, String defaultValue) {
        String s = config.getFirst(key);
        return s != null ? s : defaultValue;
    }

    public int get(String key, int defaultValue) {
        String s = config.getFirst(key);
        return s != null ? Integer.parseInt(s) : defaultValue;
    }

    public long get(String key, long defaultValue) {
        String s = config.getFirst(key);
        return s != null ? Long.parseLong(s) : defaultValue;
    }

    public boolean get(String key, boolean defaultValue) {
        String s = config.getFirst(key);
        return s != null ? Boolean.parseBoolean(s) : defaultValue;
    }

    public void put(String key, String value) {
        config.putSingle(key, value);
    }

    public void put(String key, int value) {
        config.putSingle(key, Integer.toString(value));
    }

    public void put(String key, long value) {
        config.putSingle(key, Long.toString(value));
    }

    public void put(String key, boolean value) {
        config.putSingle(key, Boolean.toString(value));
    }

    public boolean hasNote(String key) {
        return notes.containsKey(key);
    }

    public <T> T getNote(String key) {
        return (T) notes.get(key);
    }

    public void setNote(String key, Object object) {
        notes.put(key, object);
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

}
