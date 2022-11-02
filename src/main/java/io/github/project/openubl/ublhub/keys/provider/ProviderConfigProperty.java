/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.keys.provider;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProviderConfigProperty {
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String STRING_TYPE = "String";
    public static final String MULTIVALUED_STRING_TYPE = "MultivaluedString";
    public static final String SCRIPT_TYPE = "Script";

    public static final String FILE_TYPE = "File";
    public static final String ROLE_TYPE = "Role";

    public static final String LIST_TYPE = "List";
    public static final String MULTIVALUED_LIST_TYPE = "MultivaluedList";

    public static final String CLIENT_LIST_TYPE = "ClientList";
    public static final String PASSWORD = "Password";

    public static final String TEXT_TYPE = "Text";
    public static final String MAP_TYPE = "Map";

    private String name;
    private String label;
    private String helpText;
    private String type = STRING_TYPE;
    private Object defaultValue;
    private List<String> options;
    private boolean secret;

}
