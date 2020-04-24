#!/bin/sh
#
# Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
# and other contributors as indicated by the @author tags.
#
# Licensed under the Eclipse Public License - v 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.eclipse.org/legal/epl-2.0/
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

rm -r target/classes src/main/java
mkdir -p  target/classes
mkdir -p  src/main/java

for j in '../api' '../extensions'
do
    for i in `find $j -regex .*target/classes`
    do
        cp -r  $i/* target/classes/
    done
    for i in `find $j -regex .*src/main/java`
    do
        cp -r  $i/* src/main/java/
    done
done

#we don't care about classes in the 'graal' package, because they are only used in native image generation
find target/classes/ -name graal -exec rm -r {} \;

#antlr generated code
rm -r target/classes/io/quarkus/panacheql/internal

#we don't care about the document processor
rm -r target/classes/io/quarkus/annotation/processor/generate_doc

#needed to make sure the script always succeeds
echo "complete"
