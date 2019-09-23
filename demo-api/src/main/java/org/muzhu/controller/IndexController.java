package org.muzhu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther
 * 2019/9/20.
 */
@RequestMapping("index")
@RestController
public class IndexController {

    private static final Logger LOG = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping("call")
    public String doCall(String callerName) {
        LOG.info("receive callerName : {}", callerName);
        return callerName;
    }
}
