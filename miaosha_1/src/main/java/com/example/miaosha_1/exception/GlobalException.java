package com.example.miaosha_1.exception;

import com.example.miaosha_1.result.CodeMsg;

public class GlobalException extends RuntimeException{

    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
