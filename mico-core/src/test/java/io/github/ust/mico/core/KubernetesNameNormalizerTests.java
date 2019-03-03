/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core;

import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(SpringRunner.class)
public class KubernetesNameNormalizerTests {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private KubernetesNameNormalizer normalizer = new KubernetesNameNormalizer();

    @Test
    public void normalizeName() {
        collector.checkThat("simple name should be the same",
            normalizer.normalizeName("name"),
            equalTo("name"));

        collector.checkThat("simple name with numbers should be the same",
            normalizer.normalizeName("name123"),
            equalTo("name123"));

        collector.checkThat("simple name with numbers and a dash should be the same",
            normalizer.normalizeName("name-123"),
            equalTo("name-123"));

        collector.checkThat("Uppercase should be normalized to lowercase",
            normalizer.normalizeName("naMe"),
            equalTo("name"));

        collector.checkThat("Space should be normalized to dash",
            normalizer.normalizeName("first second"),
            equalTo("first-second"));

        collector.checkThat("Combined characters should be replaced",
            normalizer.normalizeName("r̀r̂r̃r̈rʼŕřt̀t̂ẗţỳỹẙyʼy̎ýÿŷp̂p̈s̀s̃s̈s̊sʼs̸śŝŞşšd̂d̃d̈ďdʼḑf̈f̸g̀g̃g̈gʼģq\u200C\u200B́ĝǧḧĥj̈jʼḱk̂k̈k̸ǩl̂l̃l̈Łłẅẍc̃c̈c̊cʼc̸Çççćĉčv̂v̈vʼv̸b́b̧ǹn̂n̈n̊nʼńņňñm̀m̂m̃m̈\u200C\u200Bm̊m̌ǵß"),
            equalTo("rrrrrrrttttyyyyyyyyppsssssssssssddddddffgggggqgghhjjkkkkklllwxcccccccccccvvvvbbnnnnnnnnnmmmmmmg"));

        collector.checkThat("Special characters '_' and '.' should be replaced by a dash",
            normalizer.normalizeName("name.js_notes"),
            equalTo("name-js-notes"));

        collector.checkThat("Special characters except '_' and '.' should be removed",
            normalizer.normalizeName("name?\\/+*#!,"),
            equalTo("name"));

        collector.checkThat("Multiple dashes should be replaced by one",
            normalizer.normalizeName("first--._second"),
            equalTo("first-second"));

        collector.checkThat("First character MUST be a letter",
            normalizer.normalizeName("1-name"),
            equalTo("short-name-1-name"));
    }
}
