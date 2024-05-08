/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.ai.simulator.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;

/**
 * @author godotg
 */
public abstract class MarkdownUtils {

    private static final Parser parser = Parser.builder().build();
    public static final HtmlRenderer renderer = HtmlRenderer.builder().build();
    public static final FlexmarkHtmlConverter mdConvert = FlexmarkHtmlConverter.builder().build();


    public static String md2Html(String md) {
        return renderer.render(parser.parse(md));
    }

    public static String html2Md(String html) {
        return mdConvert.convert(html);
    }
}
