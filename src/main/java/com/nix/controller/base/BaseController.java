package com.nix.controller.base;

import org.springframework.stereotype.Component;

@Component
public abstract class BaseController {
    protected final static int FAIL = 0;
    protected final static int SUCCESS = 1;
    protected final static String STATUS = "status";
    protected final static String MESSAGE = "msg";
}
