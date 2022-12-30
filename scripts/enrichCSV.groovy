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
@Grab('org.yaml:snakeyaml:1.33')
import org.yaml.snakeyaml.*
import groovy.yaml.*
import java.time.LocalDate

def file = new File(this.args[0])
def version = this.args[1]

def fileReader = new FileReader(file)
def yaml = new Yaml().load(fileReader)

yaml.metadata.name = 'ublhub-operator.v' + version
yaml.spec.annotations.containerImage = 'quay.io/projectopenubl/ublhub-operator:v' + version
yaml.spec.annotations.description = 'Ublhub is an enterprise solution for managing XML files based on UBL (Universal Business Language). Valid for Peru.'
yaml.spec.annotations.createdAt = LocalDate.now().toString()
//yaml.spec.install.spec.deployments[0].spec.template.spec.containers[0].image = 'quay.io/projectopenubl/ublhub-operator:v' + version
yaml.spec.version = version
yaml.spec.customresourcedefinitions.owned[0].displayName = 'Ublhub'
yaml.spec.customresourcedefinitions.owned[0].description = 'Ublhub'

// Workaround for moving annotations since OperatorHub.io complains about it
yaml.metadata.annotations = yaml.spec.annotations
yaml.spec.annotations = ""

DumperOptions options = new DumperOptions();
options.indent = 2
options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
options.defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
options.prettyFlow = true

new Yaml(options).dump(yaml, new FileWriter(file))
