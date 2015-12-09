package com.siemens.cto.aem.web.controller;

import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Controller
public class IndexController {

    private final static String DEV_MODE_COOKIE_NAME = "devMode";

    @Autowired
    @Qualifier("variableSource")
    private JavaScriptVariableSource variableSource;

    @Autowired
    @Qualifier("loginVariableSource")
    private JavaScriptVariableSource loginVariableSource;

    @RequestMapping(value = "/about")
    public String about() {
        return "aem/about";
    }

    @RequestMapping(value = "/")
    public String index() {
        return "aem/index";
    }

    @RequestMapping(value = "/sandbox")
    public String sandbox() {
        return "aem/sandbox";
    }

    @RequestMapping(value = "/index-page-scripts")
    public String indexPageScripts(@ModelAttribute(DEV_MODE_COOKIE_NAME) String modelDevMode,
                                   @CookieValue(value = DEV_MODE_COOKIE_NAME, defaultValue = "false") boolean devMode) {

        /**
         * If model contains devMode this means that this was called
         * after devMode was set and cookie is not yet added
         * therefore we disregard the cookie and check the model
         * instead
         */
        if (!StringUtils.isEmpty(modelDevMode)) {
            devMode = Boolean.valueOf(modelDevMode);
        }

        if (devMode) {
            return "aem/dev-index-page-scripts";
        }
        return "aem/prod-index-page-scripts";
    }

    @RequestMapping(value = "/devMode", method = RequestMethod.GET)
    public ModelAndView devMode(@RequestParam("val") String val, HttpServletResponse response) {
        Boolean devMode = Boolean.valueOf(val);
        response.addCookie(new Cookie(DEV_MODE_COOKIE_NAME, devMode.toString()));

        ModelAndView mv = new ModelAndView("aem/index");

        /**
         * We need to add cookie value to a model since the cookie won't be set
         * until the view is constructed
         */
        mv.addObject(DEV_MODE_COOKIE_NAME, devMode);
        return mv;
    }

    // TODO: Verify if this should be here since the controller's name is IndexController
    @RequestMapping(value = "/login")
    public String login() {
        return "aem/login";
    }

    @RequestMapping(value = "/login-page-scripts")
    public String loginPageScripts(@ModelAttribute(DEV_MODE_COOKIE_NAME) String modelDevMode,
                                   @CookieValue(value = DEV_MODE_COOKIE_NAME, defaultValue = "false") boolean devMode) {

        /**
         * If model contains devMode this means that this was called
         * after devMode was set and cookie is not yet added
         * therefore we disregard the cookie and check the model
         * instead
         */
        if (!StringUtils.isEmpty(modelDevMode)) {
            devMode = Boolean.valueOf(modelDevMode);
        }

        if (devMode) {
            return "aem/dev-login-page-scripts";
        }
        return "aem/prod-login-page-scripts";
    }

    @RequestMapping(value = "/page-constants")
    public String pageConstants(final Model aModel) {
        final Set<JavaScriptVariable> variables = variableSource.createVariables();
        return addJavaScriptVariablesToModel(aModel, variables);
    }

    @RequestMapping(value = "/login-page-constants")
    public String commonLogin(final Model aModel) {
        final Set<JavaScriptVariable> variables = loginVariableSource.createVariables();
        return addJavaScriptVariablesToModel(aModel,
                                             variables);
    }

    String addJavaScriptVariablesToModel(final Model aModel,
                                       Set<JavaScriptVariable> someVariables) {
        aModel.addAttribute("javaScriptVariables", someVariables);
        return "aem/page-constants";
    }
}