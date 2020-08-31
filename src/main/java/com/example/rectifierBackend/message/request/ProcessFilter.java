package com.example.rectifierBackend.message.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ProcessFilter {

    private String insertCode;
    private String elementName;

    public String getInsertCode() {
        return insertCode;
    }

    public void setInsertCode(String insertCode) {
        this.insertCode = insertCode;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
