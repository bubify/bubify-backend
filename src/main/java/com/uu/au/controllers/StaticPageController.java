package com.uu.au.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StaticPageController {

    private final Logger logger = LoggerFactory.getLogger(StaticPageController.class);

    @GetMapping("/autoclose")
    public @ResponseBody String autoClose() {
        return "<!doctype html><html lang=\"en\"><head> <meta charset=\"utf-8\"><title></title><script type=\"text/javascript\"> window.close();</script></head><body>Please close this window</script></body></html>";
    }

    @GetMapping("/github-auth-error")
    public @ResponseBody String gitHubAuthError() {
        return "<!doctype html><html lang=\"en\"><head> <meta charset=\"utf-8\"><title></title></head><body>Something went wrong when trying to link with GitHub. Please try again.</script></body></html>";
    }
}
