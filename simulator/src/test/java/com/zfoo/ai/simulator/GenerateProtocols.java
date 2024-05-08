package com.zfoo.ai.simulator;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class GenerateProtocols {

    @Test
    public void es() throws IOException {
        var xmlProtocols = DomUtils.inputStream2Object(ClassUtils.getFileFromClassPath("protocol.xml"), XmlProtocols.class);
        var operation = new GenerateOperation();
        operation.setFoldProtocol(true);
        operation.getGenerateLanguages().add(CodeLanguage.ES);
        ProtocolManager.initProtocol(xmlProtocols, operation);
    }

}
