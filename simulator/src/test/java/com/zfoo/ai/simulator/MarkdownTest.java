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

package com.zfoo.ai.simulator;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.zfoo.ai.simulator.util.MarkdownUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 */
@Ignore
public class MarkdownTest {

    @Test
    public void flexmarkTest() {
        var parser = Parser.builder().build();
        var renderer = HtmlRenderer.builder().build();

        var markdownText = "这里是Markdown格式的文本";
        var htmlText = renderer.render(parser.parse(markdownText));
        System.out.println(htmlText);

        var mdText = MarkdownUtils.html2Md(htmlText);
        System.out.println(mdText);
    }

}
